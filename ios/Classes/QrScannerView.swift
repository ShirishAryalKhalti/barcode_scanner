import AVFoundation
import Flutter
import SwiftUI

class BarcodeScannerPlatformView: NSObject, FlutterPlatformView {
    private var qrScannerView: ViewController
    private var scannerController: ScannerControllerImpl
    
    init(
        frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger
    ) {
        let params = args as? [String: Any] ?? [:]
        let resolution = params["resolution"] as? String ?? "720p"
        let cameraPosition = params["camera_position"] as? String ?? "back"
        print("Initial Setup Data: Resolution: \(resolution)")
        let flutterApi = ScannerFlutterApi(binaryMessenger: messenger)
        qrScannerView = ViewController(
            flutterApi: ScannerFlutterApi.init(binaryMessenger: messenger)
        )
        
        scannerController = ScannerControllerImpl(view: qrScannerView)
        ScannerControllerSetup.setUp(binaryMessenger: messenger, api: scannerController)
        qrScannerView.view.frame = frame
        super.init()
    }

    func view() -> UIView {
        return qrScannerView.view
    }

}

class ScannerControllerImpl: NSObject, ScannerController {
    var view: ViewController
    let device = AVCaptureDevice.default(for: .video)!
    @State private var isTorchOn: Bool = false

    init(view: ViewController) {
        self.view = view
        super.init()
    }

    func startScanner() throws {
        print("Starting scanner....")
        self.view.startCamera()
    }

    func stopScanner() throws {
        print("Stopping scanner....")
        self.view.stopCamera()
    }

    func disposeScanner() throws {
        device.torchMode = .off
        self.view.dispose()
    }

    func toggleTorch() throws -> Bool {
        if !device.hasTorch {
            return false
        }

        do {
            try device.lockForConfiguration()

            if device.torchMode == .on {
                device.torchMode = .off

            } else {
                try device.setTorchModeOn(level: 1.0)
            }

            device.unlockForConfiguration()
            isTorchOn = device.torchMode == .on
        } catch {
            print("Failed to toggle torch: \(error.localizedDescription)")
        }
        return isTorchOn
    }
}
