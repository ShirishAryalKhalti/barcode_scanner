package com.example.barcode_scanner

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors


class CameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraPreview()
//            MyApplicationTheme {

//                val permissionGranted = isCameraPermissionGranted.collectAsState().value
//
//
//                Box(modifier = Modifier.fillMaxSize()) {
//                    if (permissionGranted){
//                        CameraPreview()
//                    }else{
//                        Button(
//                            onClick = {
//                                handleCameraPermission()
//                            },
//                            modifier = Modifier.align(Alignment.Center)
//                        ) {
//                            Text("Start Preview")
//                        }
//                    }
//                }
////            }
        }
    }

}


@Composable
fun CameraPreview() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            setImageAnalysisAnalyzer(
                Executors.newSingleThreadExecutor(),
                BarcodeAnalyzer { barcode ->
                    Log.d("BarcodeAnalyzer", "Barcode detected: $barcode")
                    Toast.makeText(context,"This is toast", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }


    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_START
                controller = cameraController
            }
        },
        onRelease = {
            // release camera on composable is removed
            cameraController.unbind()
        },
        update = {
            println("Camera Update: $it")
        }
    )
}