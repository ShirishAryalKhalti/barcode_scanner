package com.example.barcode_scanner

import ScannedCode
import ScannerController
import ScannerError
import ScannerFlutterApi
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import zxingcpp.BarcodeReader

class NativeCameraView(
    private val context: Context,
    creationParams: Map<String?, Any?>?,
    private val activity: Activity,
    binaryMessenger: BinaryMessenger,
) : PlatformView, ScannerController{

    private var mCameraProvider: ProcessCameraProvider? = null
    private var linearLayout: LinearLayout = LinearLayout(context)
    private var preview: PreviewView = PreviewView(context)
    private var  scannerFlutterApi = ScannerFlutterApi(binaryMessenger)
    private lateinit var cameraExecutor: ExecutorService
    private var options: BarcodeReader.Options
    private var  barcodeReader: BarcodeReader
    private var camera: Camera? = null
    private var isTorchOn = false
    private val defaultResolution = Size(1280, 720)
    private var resolutionSelectorBuilder: ResolutionSelector.Builder
    private var imageAnalysisBuilder: ImageAnalysis


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }

    init {
        val resolutionValue = creationParams?.get("resolution") as Map<*, *>?
        val resolution = calculateCameraResolution(resolutionValue?.get("value"))
        resolutionSelectorBuilder = ResolutionSelector.Builder().setResolutionStrategy(
            ResolutionStrategy(
                resolution,
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
            )
        )
        imageAnalysisBuilder = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelectorBuilder.build())
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setImageQueueDepth(1)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()


        ScannerController.setUp(binaryMessenger, this)
        Log.d("CREATION_PARAMS", creationParams.toString())
        val linearLayoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        linearLayout.layoutParams = linearLayoutParams

        linearLayout.orientation = LinearLayout.VERTICAL

        preview.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        preview.scaleType = PreviewView.ScaleType.FILL_CENTER
        preview.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        linearLayout.addView(preview)
        setUpCamera()

        preview.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                preview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                preview.requestLayout()
            }
        })

        options = BarcodeReader.Options().apply {
//            formats = getBarcodeFormats(creationParams?.get("barcode_formats"))
            formats = setOf(BarcodeReader.Format.QR_CODE)
            tryRotate = true
            tryInvert = true
            tryHarder = true
            tryDownscale = true
            maxNumberOfSymbols = 1
            binarizer = BarcodeReader.Binarizer.GLOBAL_HISTOGRAM
        }
        barcodeReader = BarcodeReader(options)
    }

    private fun setUpCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysisBuilder.setAnalyzer(   
            cameraExecutor
        ) { imageProxy ->
            processImageProxy(barcodeReader, imageProxy)
        }
    }

    override fun getView(): View {
        return linearLayout
    }

    override fun dispose() {
        isTorchOn = false
        camera?.cameraControl?.enableTorch(false)
        cameraExecutor.shutdown()
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeReader,
        imageProxy: ImageProxy
    ) {
      imageProxy.let { image ->
            val codes = barcodeScanner.read(image)
            image.close()
            if (codes.isNotEmpty()) {
                val scannedCodes : MutableList<ScannedCode> = emptyList<ScannedCode>().toMutableList()
                for (code in codes) {
                    Log.d("QR_RESULT", "Barcode: ${code.text}")
                    if(code.text != null) {
                        scannedCodes.add(ScannedCode(code.text!!, format = code.format.name))
                    }
                }
                if(scannedCodes.isNotEmpty()){
                    activity.runOnUiThread { scannerFlutterApi.onScanSuccess(scannedCodes) {
                        it.onSuccess {
                            Log.d("Scanner", "Successfully sent codes to Flutter")
                        }.onFailure { error ->
                            Log.e("Scanner", "Error sending codes to Flutter", error)
                        }
                        }
                    }
                }
            }
        }
        imageProxy.close()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
         cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            mCameraProvider = cameraProvider
            // Preview
            val surfacePreview = Preview.Builder()
                .setResolutionSelector(resolutionSelectorBuilder.build())
                .build()
                .also {
                    it.surfaceProvider = preview.surfaceProvider
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                camera =  cameraProvider.bindToLifecycle(
                    activity as LifecycleOwner,
                    cameraSelector,
                    surfacePreview,
                    imageAnalysisBuilder,
                )


            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
                scannerFlutterApi.onScanError(ScannerError(exc.message)) {
                    it.onSuccess {
                        Log.d("Scanner", "Successfully sent error to Flutter")
                    }.onFailure { error ->
                        Log.e("Scanner", "Error sending error to Flutter", error)
                    }
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

     private fun calculateCameraResolution(value: Any?): Size {
        return when (value) {
            0 -> Size(640, 480)
            1 -> Size(1280, 720)
            2 -> Size(1920, 1080)
            else -> defaultResolution
        }
    }


//    @kotlin.ExperimentalStdlibApi
//    private fun getBarcodeFormats(formats: Any?): Set<BarcodeReader.Format> {
//        if(formats == null || formats !is List<*>) {
//            return setOf(BarcodeReader.Format.QR_CODE)
//        }
//
//        val barcodeFormats = mutableSetOf<BarcodeReader.Format>()
//        for (format in BarcodeReader.Format.entries) {
//            val value = format.name.lowercase().replace("_", "")
//            if (formats.contains(value)) {
//                barcodeFormats.add(format)
//            }
//        }
//        if(barcodeFormats.isEmpty()){
//            return setOf(BarcodeReader.Format.QR_CODE)
//        }
//        return barcodeFormats
//    }

    override fun toggleTorch(): Boolean {
        isTorchOn = !isTorchOn
        camera?.cameraControl?.enableTorch(isTorchOn)
        return isTorchOn
    }

    override fun startScanner() {
        startCamera()
    }

    override fun stopScanner() {
        mCameraProvider?.unbindAll()
    }

    override fun disposeScanner() {
        dispose()
    }
}