package com.example.barcode_scanner

import ScannerController
import ScannerFlutterApi
import android.app.Activity

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger


/** BarcodeScannerPlugin */
class BarcodeScannerPlugin: FlutterPlugin, ActivityAware {
    private var activity: Activity? = null
    private var pluginBinding: FlutterPluginBinding? = null
    private var binaryMessenger: BinaryMessenger? = null
    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        binaryMessenger = binding.binaryMessenger
        pluginBinding = binding
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {}

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        pluginBinding!!
            .platformViewRegistry
            .registerViewFactory("barcode_scanner_view",
                NativeViewFactory(binding.activity, binaryMessenger!!)
            )
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
