package com.example.barcode_scanner

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraView(context: Context, private val lifecycleOwner: LifecycleOwner) : View(context) {

    private val rootLayout: RelativeLayout
    private val previewView: PreviewView

    init {
        // Create RelativeLayout programmatically
        rootLayout = RelativeLayout(context).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Create PreviewView programmatically
        previewView = PreviewView(context).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Add PreviewView to the RelativeLayout
        rootLayout.addView(previewView)

        // Set up the camera
        if (hasCameraPermission(context)) {
            setupCamera(context)
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupCamera(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            BarcodeAnalyzer { barcode ->
                                Log.d("CameraView", "Detected: $barcode")
                                Toast.makeText(context, "Detected: $barcode", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

//                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Use front camera for devices without rear camera
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, // This is now correct because lifecycleOwner is passed as Activity/Fragment
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Ensure rootLayout is added as the view for this custom view
        (context as? Activity)?.setContentView(rootLayout)
    }

    // Optionally, override the dispose method to clean up resources
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up any resources if needed (e.g., shutting down the camera executor)
    }
}



//class CameraView(context: Context, private val lifecycleOwner: LifecycleOwner) : View(context) {
//
//    private val rootLayout: RelativeLayout
//    private val previewView: PreviewView
//
//    init {
//        // Create RelativeLayout programmatically
//        rootLayout = RelativeLayout(context).apply {
//            layoutParams = RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT,
//                RelativeLayout.LayoutParams.MATCH_PARENT
//            )
//        }
//
//        // Create PreviewView programmatically
//        previewView = PreviewView(context).apply {
//            layoutParams = RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT,
//                RelativeLayout.LayoutParams.MATCH_PARENT
//            )
//        }
//
//        // Add PreviewView to the RelativeLayout
//        rootLayout.addView(previewView)
//
//        // Set up the camera
//        if (hasCameraPermission(context)) {
//            setupCamera(context)
//        } else {
//            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun hasCameraPermission(context: Context): Boolean {
//        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun setupCamera(context: Context) {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//        cameraProviderFuture.addListener({
//            try {
//                val cameraProvider = cameraProviderFuture.get()
//                val preview = Preview.Builder().build().also {
//                    it.setSurfaceProvider(previewView.surfaceProvider)
//                }
//
//                val imageAnalyzer = ImageAnalysis.Builder()
//                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                    .build()
//                    .also {
//                        it.setAnalyzer(
//                            Executors.newSingleThreadExecutor(),
//                            BarcodeAnalyzer { barcode ->
//                                Log.d("CameraView", "Detected: $barcode")
//                                Toast.makeText(context, "Detected: $barcode", Toast.LENGTH_SHORT).show()
//                            }
//                        )
//                    }
//
//                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
////                    (context) ?: throw IllegalStateException("Context must be a LifecycleOwner"),
//                    lifecycleOwner,
//                    cameraSelector,
//                    preview,
//                    imageAnalyzer
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }, ContextCompat.getMainExecutor(context))
//    }
//
////    override fun getView(): View {
////        return rootLayout
////    }
////
////    override fun dispose() {}
//}



//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.widget.RelativeLayout
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import java.util.concurrent.Executors
//
//class CameraActivity : AppCompatActivity() {
//
//    private lateinit var previewView: PreviewView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Create RelativeLayout programmatically
//        val rootLayout = RelativeLayout(this).apply {
//            layoutParams = RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT,
//                RelativeLayout.LayoutParams.MATCH_PARENT
//            )
//        }
//
//        // Create PreviewView programmatically
//        previewView = PreviewView(this).apply {
//            layoutParams = RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT,
//                RelativeLayout.LayoutParams.MATCH_PARENT
//            )
//        }
//
//        // Add PreviewView to the RelativeLayout
//        rootLayout.addView(previewView)
//
//        // Set the content view to the created layout
//        setContentView(rootLayout)
//
//        if (hasCameraPermission()) {
//            setupCamera()
//        } else {
//            requestCameraPermission()
//        }
//    }
//
//    private fun hasCameraPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestCameraPermission() {
//        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            setupCamera()
//        } else {
//            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun setupCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//            val preview = androidx.camera.core.Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView.surfaceProvider)
//            }
//
//            val imageAnalyzer = ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build()
//                .also {
//                    it.setAnalyzer(
//                        Executors.newSingleThreadExecutor(),
//                        BarcodeAnalyzer { barcode ->
//                            runOnUiThread {
//                                Toast.makeText(this, "Detected: $barcode", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    )
//                }
//
//            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//}
