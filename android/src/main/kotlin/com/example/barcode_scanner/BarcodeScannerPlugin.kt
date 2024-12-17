package com.example.barcode_scanner

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** BarcodeScannerPlugin */
class BarcodeScannerPlugin: FlutterPlugin {

  override fun onAttachedToEngine(binding: FlutterPluginBinding) {
    binding
            .platformViewRegistry
            .registerViewFactory("<platform-view-type>", NativeViewFactory())
}

override fun onDetachedFromEngine(binding: FlutterPluginBinding) {}
}
