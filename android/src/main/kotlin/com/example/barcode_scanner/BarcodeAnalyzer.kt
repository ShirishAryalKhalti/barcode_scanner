package com.example.barcode_scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader
import zxingcpp.BarcodeReader.Format.QR_CODE


typealias BarcodeListener = (barcode: String) -> Unit

class BarcodeAnalyzer(private val barcodeListener: BarcodeListener) : ImageAnalysis.Analyzer {

    //    private val scanner = BarcodeScanning.getClient()
    private val zxingScanner = BarcodeReader().apply {
        options = BarcodeReader.Options().apply {
            formats = setOf(QR_CODE)
            tryRotate = true
            tryInvert = true
        }
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
//        val mediaImage = image.image
//        if (mediaImage == null) return
        val resultString = try {
            image.use {
                zxingScanner.read(it)
            }.let { results ->
                val result = results.first()
//                barcodeListener("${result.text}")
            }
        } catch (e: Throwable) {
            e.message ?: "Error"
        }
    }

}