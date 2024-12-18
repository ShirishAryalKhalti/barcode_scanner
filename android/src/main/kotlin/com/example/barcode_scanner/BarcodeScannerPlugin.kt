package com.example.barcode_scanner

import android.app.Activity
import androidx.lifecycle.LifecycleOwner

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

class BarcodeScannerPlugin : FlutterPlugin, ActivityAware {
    private var activity: Activity? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        binding
            .platformViewRegistry
            .registerViewFactory("<platform-view-type>", NativeViewFactory(::getLifecycleOwner))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {}

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    private fun getLifecycleOwner(): LifecycleOwner {
        val currentActivity = activity
        return if (currentActivity is LifecycleOwner) {
            currentActivity
        } else {
            throw IllegalStateException("Activity does not implement LifecycleOwner")
        }
    }
}



///** BarcodeScannerPlugin */
//class BarcodeScannerPlugin: FlutterPlugin, ActivityAware {
//
//    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
//        binding
//            .platformViewRegistry
//            .registerViewFactory("<platform-view-type>", NativeViewFactory())
//    }
//
//    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {}
//
//    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
//        val activity = binding.activity
//        val cameraView = CameraActivity(activity.applicationContext, activity) // Pass activity as LifecycleOwner
//        // ... register the cameraView with the platform view registry ...
//    }
//
//    override fun onDetachedFromActivityForConfigChanges() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onDetachedFromActivity() {
//        TODO("Not yet implemented")
//    }
//}

