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
        self.view.dispose()
    }

    func toggleTorch() throws -> Bool {
        return self.view.toggleTorch()
    }
}
