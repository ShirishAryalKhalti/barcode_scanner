// Autogenerated from Pigeon (v22.4.0), do not edit directly.
// See also: https://pub.dev/packages/pigeon
@file:Suppress("UNCHECKED_CAST", "ArrayInDataClass")


import android.util.Log
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MessageCodec
import io.flutter.plugin.common.StandardMessageCodec
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

private fun wrapResult(result: Any?): List<Any?> {
  return listOf(result)
}

private fun wrapError(exception: Throwable): List<Any?> {
  return if (exception is BarcodeScannerException) {
    listOf(
      exception.code,
      exception.message,
      exception.details
    )
  } else {
    listOf(
      exception.javaClass.simpleName,
      exception.toString(),
      "Cause: " + exception.cause + ", Stacktrace: " + Log.getStackTraceString(exception)
    )
  }
}

private fun createConnectionError(channelName: String): BarcodeScannerException {
  return BarcodeScannerException("channel-error",  "Unable to establish connection on channel: '$channelName'.", "")}

/**
 * Error class for passing custom error details to Flutter via a thrown PlatformException.
 * @property code The error code.
 * @property message The error message.
 * @property details The error details. Must be a datatype supported by the api codec.
 */
class BarcodeScannerException (
  val code: String,
  override val message: String? = null,
  val details: Any? = null
) : Throwable()

/** Generated class from Pigeon that represents data sent in messages. */
data class ScannedCode (
  val text: String? = null,
  val format: String? = null
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ScannedCode {
      val text = pigeonVar_list[0] as String?
      val format = pigeonVar_list[1] as String?
      return ScannedCode(text, format)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      text,
      format,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ScannerError (
  val message: String? = null,
  val tag: String? = null
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ScannerError {
      val message = pigeonVar_list[0] as String?
      val tag = pigeonVar_list[1] as String?
      return ScannerError(message, tag)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      message,
      tag,
    )
  }
}
private open class ScannerPigeonCodec : StandardMessageCodec() {
  override fun readValueOfType(type: Byte, buffer: ByteBuffer): Any? {
    return when (type) {
      129.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ScannedCode.fromList(it)
        }
      }
      130.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ScannerError.fromList(it)
        }
      }
      else -> super.readValueOfType(type, buffer)
    }
  }
  override fun writeValue(stream: ByteArrayOutputStream, value: Any?)   {
    when (value) {
      is ScannedCode -> {
        stream.write(129)
        writeValue(stream, value.toList())
      }
      is ScannerError -> {
        stream.write(130)
        writeValue(stream, value.toList())
      }
      else -> super.writeValue(stream, value)
    }
  }
}

/** Generated class from Pigeon that represents Flutter messages that can be called from Kotlin. */
class ScannerFlutterApi(private val binaryMessenger: BinaryMessenger, private val messageChannelSuffix: String = "") {
  companion object {
    /** The codec used by ScannerFlutterApi. */
    val codec: MessageCodec<Any?> by lazy {
      ScannerPigeonCodec()
    }
  }
  fun onScanSuccess(codesArg: List<ScannedCode>, callback: (Result<Unit>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.barcode_scanner.ScannerFlutterApi.onScanSuccess$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(listOf(codesArg)) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(BarcodeScannerException(it[0] as String, it[1] as String, it[2] as String?)))
        } else {
          callback(Result.success(Unit))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
  fun onScanError(errorArg: ScannerError, callback: (Result<Unit>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.barcode_scanner.ScannerFlutterApi.onScanError$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(listOf(errorArg)) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(BarcodeScannerException(it[0] as String, it[1] as String, it[2] as String?)))
        } else {
          callback(Result.success(Unit))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
}
/** Generated interface from Pigeon that represents a handler of messages from Flutter. */
interface ScannerController {
  fun toggleTorch(): Boolean
  fun startScanner()
  fun stopScanner()
  fun disposeScanner()

  companion object {
    /** The codec used by ScannerController. */
    val codec: MessageCodec<Any?> by lazy {
      ScannerPigeonCodec()
    }
    /** Sets up an instance of `ScannerController` to handle messages through the `binaryMessenger`. */
    @JvmOverloads
    fun setUp(binaryMessenger: BinaryMessenger, api: ScannerController?, messageChannelSuffix: String = "") {
      val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.barcode_scanner.ScannerController.toggleTorch$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { _, reply ->
            val wrapped: List<Any?> = try {
              listOf(api.toggleTorch())
            } catch (exception: Throwable) {
              wrapError(exception)
            }
            reply.reply(wrapped)
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.barcode_scanner.ScannerController.startScanner$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { _, reply ->
            val wrapped: List<Any?> = try {
              api.startScanner()
              listOf(null)
            } catch (exception: Throwable) {
              wrapError(exception)
            }
            reply.reply(wrapped)
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.barcode_scanner.ScannerController.stopScanner$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { _, reply ->
            val wrapped: List<Any?> = try {
              api.stopScanner()
              listOf(null)
            } catch (exception: Throwable) {
              wrapError(exception)
            }
            reply.reply(wrapped)
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.barcode_scanner.ScannerController.disposeScanner$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { _, reply ->
            val wrapped: List<Any?> = try {
              api.disposeScanner()
              listOf(null)
            } catch (exception: Throwable) {
              wrapError(exception)
            }
            reply.reply(wrapped)
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
    }
  }
}
