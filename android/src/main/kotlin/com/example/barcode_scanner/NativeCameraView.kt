package com.example.barcode_scanner

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
import io.flutter.plugin.platform.PlatformView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import zxingcpp.BarcodeReader

class NativeCameraView(
    private val context: Context, id: Int, creationParams: Map<String?, Any?>?,
    private val activity: Activity
) : PlatformView {

    private var mCameraProvider: ProcessCameraProvider? = null
    private var linearLayout: LinearLayout = LinearLayout(context)
    private var preview: PreviewView = PreviewView(context)

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var options: BarcodeReader.Options
    private  lateinit var  barcodeReader: BarcodeReader

    private val cameraResolution = Size(1920, 1080)
    private val selectorBuilder = ResolutionSelector.Builder().setResolutionStrategy(
        ResolutionStrategy(
            cameraResolution,
            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
        )
    )

    private var analysisUseCase: ImageAnalysis = ImageAnalysis.Builder()
        .setResolutionSelector(selectorBuilder.build())
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .build()

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }

    init {
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

        linearLayout.addView(preview)
        setUpCamera()

        preview.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                preview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                preview.requestLayout()
            }
        })
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
        options = BarcodeReader.Options().apply {
            formats = setOf(BarcodeReader.Format.QR_CODE)
            tryRotate = true
            tryInvert = true
            tryHarder = true
            tryDownscale = true

        }
        barcodeReader = BarcodeReader(options)
        analysisUseCase.setAnalyzer(
            cameraExecutor
        ) { imageProxy ->
            processImageProxy(barcodeReader, imageProxy)
        }
    }

    override fun getView(): View {
        return linearLayout
    }

    override fun dispose() {
        cameraExecutor.shutdown()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeReader,
        imageProxy: ImageProxy
    ) {
      imageProxy.let { image ->
            val codes = barcodeScanner.read(image)
            if (codes.isNotEmpty()) {
                for (code in codes) {
                    Log.d("QR_RESULT", "Barcode: ${code.text}")
                }
            }
          image.close()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        preview.scaleType = PreviewView.ScaleType.FILL_CENTER
        preview.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            mCameraProvider = cameraProvider

            // Preview
            val surfacePreview = Preview.Builder()
                .setResolutionSelector(selectorBuilder.build())
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
                cameraProvider.bindToLifecycle(
                    activity as LifecycleOwner,
                    cameraSelector,
                    surfacePreview,
                    analysisUseCase,
                )
            } catch (exc: Exception) {
                // Do nothing on exception
            }
        }, ContextCompat.getMainExecutor(context))
    }

}