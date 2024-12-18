package com.example.barcode_scanner

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory


class NativeViewFactory(private val lifecycleOwnerProvider: () -> LifecycleOwner) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        val lifecycleOwner = lifecycleOwnerProvider()
        val creationParams = args as Map<String?, Any?>?
        return NativeView(context, id, creationParams, lifecycleOwner)
//        return CameraNativeView(context, lifecycleOwner)
    }
}


//class NativeViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
//    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
//        val creationParams = args as Map<String?, Any?>?
//        return NativeView(context, viewId, creationParams)
//    }
//}