//
//  ViewController.swift
//  Pods
//
//  Created by Khalti Private Limited on 24/12/2024.
//

import AVFoundation
import UIKit
import ZXingCpp



class ViewController: UIViewController {
    var flutterApi: ScannerFlutterApi
    
    init(flutterApi: ScannerFlutterApi) {
        self.flutterApi = flutterApi
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    let device = AVCaptureDevice.default(for: .video)!
    let captureSession = AVCaptureSession()
    lazy var preview = AVCaptureVideoPreviewLayer(session: captureSession)
    let queue = DispatchQueue(label: "com.zxing_cpp.ios")
    
    private let reader: ZXIBarcodeReader = {
        let reader = ZXIBarcodeReader()
        let options = reader.options
        options.tryDownscale = true
        options.tryHarder = true
        options.maxNumberOfSymbols = 1
        options.tryRotate = true
        options.formats = [14]  // QR on default
        return reader
    }()
    
    let zxingLock = DispatchSemaphore(value: 1)
    
    override func viewDidLoad() {
        super.viewDidLoad()

        self.preview.videoGravity = AVLayerVideoGravity.resizeAspectFill
        self.preview.frame = self.view.layer.bounds
        self.view.layer.addSublayer(self.preview)

        // setup camera session
        self.requestAccess {
            var discoverySession: AVCaptureDevice.DiscoverySession!
            if #available(iOS 13.0, *) {
                discoverySession = AVCaptureDevice.DiscoverySession(
                    deviceTypes: [
                        .builtInTripleCamera,
                        .builtInDualWideCamera,
                        .builtInDualCamera,
                        .builtInWideAngleCamera,
                    ],
                    mediaType: .video,
                    position: .back
                )
            } else {
                discoverySession = AVCaptureDevice.DiscoverySession(
                    deviceTypes: [
                        .builtInDualCamera,
                        .builtInWideAngleCamera,
                    ],
                    mediaType: .video,
                    position: .back
                )
            }

            let device = discoverySession.devices.first!

            let cameraInput = try! AVCaptureDeviceInput(device: device)
            self.captureSession.beginConfiguration()
            self.captureSession.sessionPreset = .hd1280x720
            self.captureSession.addInput(cameraInput)
            let videoDataOutput = AVCaptureVideoDataOutput()
            videoDataOutput.setSampleBufferDelegate(self, queue: self.queue)
            videoDataOutput.videoSettings = [
                kCVPixelBufferPixelFormatTypeKey as String:
                    kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange
            ]
            videoDataOutput.alwaysDiscardsLateVideoFrames = true

            self.captureSession.addOutput(videoDataOutput)
            self.captureSession.commitConfiguration()
            DispatchQueue.global(qos: .background).async {
                self.captureSession.startRunning()
            }
        }
    }

}

extension ViewController {
    func requestAccess(_ completion: @escaping () -> Void) {
        if AVCaptureDevice.authorizationStatus(for: AVMediaType.video) == .notDetermined {
            AVCaptureDevice.requestAccess(for: .video) { _ in
                completion()
            }
        } else {
            completion()
        }
    }
}

extension ViewController: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(
        _ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard self.zxingLock.wait(timeout: DispatchTime.now()) == .success else {
            return
        }
        let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)!
        if let downscaledBuffer = downscaleImageBuffer(imageBuffer) {
            if let result = try? reader.read(downscaledBuffer).first {
                print("Found barcode of format", result.format.rawValue, "with text", result.text)
                print("Barcode byte: \(result.bytes.base64EncodedString())")
                flutterApi.onScanSuccess(codes: [
                    result.text
                ]) { result in
                    switch result {
                        case .success:
                            print("Scan result sent successfully.")
                        case .failure(let error):
                            print("Error sending scan result: \(error)")  // Handle PigeonError appropriately
                    }
                }
            }
        }
        self.zxingLock.signal()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        preview.removeFromSuperlayer()
    }
}

extension ViewController {
    func downscaleImageBuffer(_ imageBuffer: CVImageBuffer) -> CVPixelBuffer? {
        let newWidth = 640  // Target width (adjust as needed)
        let newHeight = 480  // Target height (adjust as needed)

        // Create a CIImage from the CVPixelBuffer
        let ciImage = CIImage(cvPixelBuffer: imageBuffer)

        // Create a CIContext for rendering the image
        let context = CIContext()

        // Calculate the scale transformation
        let scale = CGAffineTransform(
            scaleX: CGFloat(newWidth) / CGFloat(CVPixelBufferGetWidth(imageBuffer)),
            y: CGFloat(newHeight) / CGFloat(CVPixelBufferGetHeight(imageBuffer)))

        // Apply the transformation to the CIImage
        let scaledCIImage = ciImage.transformed(by: scale)

        // Create a new pixel buffer to hold the scaled image
        var scaledImageBuffer: CVPixelBuffer?
        let attributes: [CFString: Any] = [
            kCVPixelBufferWidthKey: newWidth,
            kCVPixelBufferHeightKey: newHeight,
            kCVPixelBufferPixelFormatTypeKey: kCVPixelFormatType_32BGRA,
        ]

        // Create a pixel buffer for the scaled image
        let status = CVPixelBufferCreate(
            kCFAllocatorDefault, newWidth, newHeight, kCVPixelFormatType_32BGRA,
            attributes as CFDictionary, &scaledImageBuffer)

        guard status == kCVReturnSuccess, let scaledImageBuffer = scaledImageBuffer else {
            print("Error creating pixel buffer")
            return nil
        }

        // Render the scaled image onto the newly created pixel buffer
        context.render(scaledCIImage, to: scaledImageBuffer)

        return scaledImageBuffer
    }

}

extension ViewController {
    func startCamera() {
        DispatchQueue.main.async {
            self.captureSession.startRunning()
        }
    }

    func stopCamera() {
        DispatchQueue.main.async {
            self.captureSession.stopRunning()
        }
    }
    
    func dispose() {
        DispatchQueue.main.async {
            // Stop the capture session
            self.captureSession.stopRunning()
            // Remove all inputs and outputs
            self.captureSession.inputs.forEach { input in
                self.captureSession.removeInput(input)
            }
            self.captureSession.outputs.forEach { output in
                self.captureSession.removeOutput(output)
            }
            // Remove the preview layer
            self.preview.removeFromSuperlayer()
        }
    }
}

extension ViewController{
    func resolveBarcodeFormat(val: Int) -> String{
        switch val {
               case 1: return "Aztec"
               case 2: return "Codabar"
               case 3: return "Code 39"
               case 4: return "Code 93"
               case 5: return "Code 128"
               case 6: return "Data Bar"
               case 7: return "Data Bar Expanded"
               case 8: return "Data Matrix"
               case 9: return "EAN-8"
               case 10: return "EAN-13"
               case 11: return "ITF"
               case 13: return "PDF417"
               case 14: return "QR Code"
               case 15: return "Micro QR Code"
               case 16: return "RMQR Code"
               case 17: return "UPC-A"
               case 18: return "UPC-E"
               default: return ""
           }
    }
}
