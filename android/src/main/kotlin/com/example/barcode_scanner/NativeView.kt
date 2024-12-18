package com.example.barcode_scanner

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.platform.PlatformView

internal class NativeView(context: Context, id: Int, creationParams: Map<String?, Any?>?, private val lifecycleOwner: LifecycleOwner) :
    PlatformView {
    private val textView: CameraView

    override fun getView(): View {
        return textView
    }

    override fun dispose() {}

    init {
        textView = CameraView(context, lifecycleOwner)
//        textView.textSize = 72f
//        textView.setBackgroundColor(Color.rgb(255, 255, 255))
//        textView.text = "Rendered on a native Android view (id: $id)"
    }
}

//class CameraView(context: Context) : View() {
//
//
//    init {
//
//    }
//}