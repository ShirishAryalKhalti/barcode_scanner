// Autogenerated from Pigeon (v22.4.0), do not edit directly.
// See also: https://pub.dev/packages/pigeon

import Foundation

#if os(iOS)
  import Flutter
#elseif os(macOS)
  import FlutterMacOS
#else
  #error("Unsupported platform.")
#endif

/// Error class for passing custom error details to Dart side.
final class PigeonError: Error {
  let code: String
  let message: String?
  let details: Any?

  init(code: String, message: String?, details: Any?) {
    self.code = code
    self.message = message
    self.details = details
  }

  var localizedDescription: String {
    return
      "PigeonError(code: \(code), message: \(message ?? "<nil>"), details: \(details ?? "<nil>")"
      }
}

private func wrapResult(_ result: Any?) -> [Any?] {
  return [result]
}

private func wrapError(_ error: Any) -> [Any?] {
  if let pigeonError = error as? PigeonError {
    return [
      pigeonError.code,
      pigeonError.message,
      pigeonError.details,
    ]
  }
  if let flutterError = error as? FlutterError {
    return [
      flutterError.code,
      flutterError.message,
      flutterError.details,
    ]
  }
  return [
    "\(error)",
    "\(type(of: error))",
    "Stacktrace: \(Thread.callStackSymbols)",
  ]
}

private func createConnectionError(withChannelName channelName: String) -> PigeonError {
  return PigeonError(code: "channel-error", message: "Unable to establish connection on channel: '\(channelName)'.", details: "")
}

private func isNullish(_ value: Any?) -> Bool {
  return value is NSNull || value == nil
}

private func nilOrValue<T>(_ value: Any?) -> T? {
  if value is NSNull { return nil }
  return value as! T?
}

/// Generated class from Pigeon that represents data sent in messages.
struct ScannerError {
  var message: String? = nil
  var tag: String? = nil



  // swift-format-ignore: AlwaysUseLowerCamelCase
  static func fromList(_ pigeonVar_list: [Any?]) -> ScannerError? {
    let message: String? = nilOrValue(pigeonVar_list[0])
    let tag: String? = nilOrValue(pigeonVar_list[1])

    return ScannerError(
      message: message,
      tag: tag
    )
  }
  func toList() -> [Any?] {
    return [
      message,
      tag,
    ]
  }
}

private class ScannerPigeonCodecReader: FlutterStandardReader {
  override func readValue(ofType type: UInt8) -> Any? {
    switch type {
    case 129:
      return ScannerError.fromList(self.readValue() as! [Any?])
    default:
      return super.readValue(ofType: type)
    }
  }
}

private class ScannerPigeonCodecWriter: FlutterStandardWriter {
  override func writeValue(_ value: Any) {
    if let value = value as? ScannerError {
      super.writeByte(129)
      super.writeValue(value.toList())
    } else {
      super.writeValue(value)
    }
  }
}

private class ScannerPigeonCodecReaderWriter: FlutterStandardReaderWriter {
  override func reader(with data: Data) -> FlutterStandardReader {
    return ScannerPigeonCodecReader(data: data)
  }

  override func writer(with data: NSMutableData) -> FlutterStandardWriter {
    return ScannerPigeonCodecWriter(data: data)
  }
}

class ScannerPigeonCodec: FlutterStandardMessageCodec, @unchecked Sendable {
  static let shared = ScannerPigeonCodec(readerWriter: ScannerPigeonCodecReaderWriter())
}

/// Generated protocol from Pigeon that represents Flutter messages that can be called from Swift.
protocol ScannerFlutterApiProtocol {
  func onScanSuccess(codes codesArg: [String], completion: @escaping (Result<Void, PigeonError>) -> Void)
  func onScanError(error errorArg: ScannerError, completion: @escaping (Result<Void, PigeonError>) -> Void)
}
class ScannerFlutterApi: ScannerFlutterApiProtocol {
  private let binaryMessenger: FlutterBinaryMessenger
  private let messageChannelSuffix: String
  init(binaryMessenger: FlutterBinaryMessenger, messageChannelSuffix: String = "") {
    self.binaryMessenger = binaryMessenger
    self.messageChannelSuffix = messageChannelSuffix.count > 0 ? ".\(messageChannelSuffix)" : ""
  }
  var codec: ScannerPigeonCodec {
    return ScannerPigeonCodec.shared
  }
  func onScanSuccess(codes codesArg: [String], completion: @escaping (Result<Void, PigeonError>) -> Void) {
    let channelName: String = "dev.flutter.pigeon.barcode_scanner.ScannerFlutterApi.onScanSuccess\(messageChannelSuffix)"
    let channel = FlutterBasicMessageChannel(name: channelName, binaryMessenger: binaryMessenger, codec: codec)
    channel.sendMessage([codesArg] as [Any?]) { response in
      guard let listResponse = response as? [Any?] else {
        completion(.failure(createConnectionError(withChannelName: channelName)))
        return
      }
      if listResponse.count > 1 {
        let code: String = listResponse[0] as! String
        let message: String? = nilOrValue(listResponse[1])
        let details: String? = nilOrValue(listResponse[2])
        completion(.failure(PigeonError(code: code, message: message, details: details)))
      } else {
        completion(.success(Void()))
      }
    }
  }
  func onScanError(error errorArg: ScannerError, completion: @escaping (Result<Void, PigeonError>) -> Void) {
    let channelName: String = "dev.flutter.pigeon.barcode_scanner.ScannerFlutterApi.onScanError\(messageChannelSuffix)"
    let channel = FlutterBasicMessageChannel(name: channelName, binaryMessenger: binaryMessenger, codec: codec)
    channel.sendMessage([errorArg] as [Any?]) { response in
      guard let listResponse = response as? [Any?] else {
        completion(.failure(createConnectionError(withChannelName: channelName)))
        return
      }
      if listResponse.count > 1 {
        let code: String = listResponse[0] as! String
        let message: String? = nilOrValue(listResponse[1])
        let details: String? = nilOrValue(listResponse[2])
        completion(.failure(PigeonError(code: code, message: message, details: details)))
      } else {
        completion(.success(Void()))
      }
    }
  }
}
/// Generated protocol from Pigeon that represents a handler of messages from Flutter.
protocol ScannerController {
  func toggleTorch() throws -> Bool
  func startScanner() throws
  func stopScanner() throws
  func disposeScanner() throws
}

/// Generated setup class from Pigeon to handle messages through the `binaryMessenger`.
class ScannerControllerSetup {
  static var codec: FlutterStandardMessageCodec { ScannerPigeonCodec.shared }
  /// Sets up an instance of `ScannerController` to handle messages through the `binaryMessenger`.
  static func setUp(binaryMessenger: FlutterBinaryMessenger, api: ScannerController?, messageChannelSuffix: String = "") {
    let channelSuffix = messageChannelSuffix.count > 0 ? ".\(messageChannelSuffix)" : ""
    let toggleTorchChannel = FlutterBasicMessageChannel(name: "dev.flutter.pigeon.barcode_scanner.ScannerController.toggleTorch\(channelSuffix)", binaryMessenger: binaryMessenger, codec: codec)
    if let api = api {
      toggleTorchChannel.setMessageHandler { _, reply in
        do {
          let result = try api.toggleTorch()
          reply(wrapResult(result))
        } catch {
          reply(wrapError(error))
        }
      }
    } else {
      toggleTorchChannel.setMessageHandler(nil)
    }
    let startScannerChannel = FlutterBasicMessageChannel(name: "dev.flutter.pigeon.barcode_scanner.ScannerController.startScanner\(channelSuffix)", binaryMessenger: binaryMessenger, codec: codec)
    if let api = api {
      startScannerChannel.setMessageHandler { _, reply in
        do {
          try api.startScanner()
          reply(wrapResult(nil))
        } catch {
          reply(wrapError(error))
        }
      }
    } else {
      startScannerChannel.setMessageHandler(nil)
    }
    let stopScannerChannel = FlutterBasicMessageChannel(name: "dev.flutter.pigeon.barcode_scanner.ScannerController.stopScanner\(channelSuffix)", binaryMessenger: binaryMessenger, codec: codec)
    if let api = api {
      stopScannerChannel.setMessageHandler { _, reply in
        do {
          try api.stopScanner()
          reply(wrapResult(nil))
        } catch {
          reply(wrapError(error))
        }
      }
    } else {
      stopScannerChannel.setMessageHandler(nil)
    }
    let disposeScannerChannel = FlutterBasicMessageChannel(name: "dev.flutter.pigeon.barcode_scanner.ScannerController.disposeScanner\(channelSuffix)", binaryMessenger: binaryMessenger, codec: codec)
    if let api = api {
      disposeScannerChannel.setMessageHandler { _, reply in
        do {
          try api.disposeScanner()
          reply(wrapResult(nil))
        } catch {
          reply(wrapError(error))
        }
      }
    } else {
      disposeScannerChannel.setMessageHandler(nil)
    }
  }
}
