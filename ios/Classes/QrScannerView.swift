import AVFoundation
import Flutter
import SwiftUI

class ScannerControllerImpl: NSObject, ScannerController {
    var baseView: BaseView?

    init(baseView: BaseView) {
        self.baseView = baseView
        super.init()
    }

    func toggleTorch() throws -> Bool {
        guard let device: AVCaptureDevice = AVCaptureDevice.default(for: .video),
            device.hasTorch
        else { return isTorchOn }

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

    func startScanner() throws {
        DispatchQueue.main.async {
            self.baseView?.captureSession.startRunning()
        }
    }

    func stopScanner() throws {
        DispatchQueue.main.async {
            self.baseView?.captureSession.stopRunning()
        }
    }

    @State private var isTorchOn: Bool = false
}

@available(iOS 13.0, *)
class BarcodeScannerPlatformView: NSObject, FlutterPlatformView {
    private var qrScannerView: UIHostingController<BaseView>
    private var scannerController: ScannerControllerImpl

    init(
        frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger?
    ) {

        // Parse the creation parameters
        let params = args as? [String: Any] ?? [:]
        let resolution = params["resolution"] as? String ?? "720p"
        let cameraPosition = params["camera_position"] as? String ?? "back"

        print("Initial Setup Data: Resolution: \(resolution)")
        // Set up the host API

        let baseView = BaseView(messenger: messenger)
        scannerController = ScannerControllerImpl(baseView: baseView)
        ScannerControllerSetup.setUp(binaryMessenger: messenger!, api: scannerController)
        qrScannerView = UIHostingController(rootView: baseView)
        super.init()
        // Set the frame for the SwiftUI view
        qrScannerView.view.frame = frame

    }

    func view() -> UIView {
        return qrScannerView.view
    }

    func presetFromString(_ presetString: String) -> AVCaptureSession.Preset {
        switch presetString.lowercased() {
        case "hd4K3840x2160":
            return .hd4K3840x2160
        case "hd1920x1080":
            return .hd1920x1080
        case "hd1280x720":
            return .hd1280x720
        case "vga640x480":
            return .vga640x480
        default:
            return .hd1280x720
        }
    }
}

@available(iOS 13.0, *)
struct BaseView: View {
    var messenger: FlutterBinaryMessenger?

    // Camera session declaration
    @State var captureSession: AVCaptureSession = {
        let session: AVCaptureSession = AVCaptureSession()
        session.sessionPreset = .hd1280x720
        return session
    }()

    var body: some View {
        ZStack {

            QRScannerView(
                didFindCode: { codes in
                    print("Scanned Codes: \(codes.joined(separator: ", "))")
                    if let messenger = messenger {
                        let flutterApi = ScannerFlutterApi(binaryMessenger: messenger)
                        flutterApi.onScanSuccess(codes: codes) { result in
                            switch result {
                            case .success:
                                print("Scan result sent successfully.")
                            case .failure(let error):
                                print("Error sending scan result: \(error)")  // Handle PigeonError appropriately
                            }
                        }
                    } else {
                        print("Error: FlutterBinaryMessenger is nil.")
                    }
                },
                captureSession: $captureSession
            )
            .edgesIgnoringSafeArea(.all)

            Rectangle()
                .stroke(Color.red, lineWidth: 4)
                .frame(width: 300, height: 300)
                .cornerRadius(4)
                .opacity(0.5)
                .position(
                    x: UIScreen.main.bounds.width / 2, y: UIScreen.main.bounds.height / 2)

        }
    }
}

@available(iOS 13.0, *)
struct QRScannerView: UIViewControllerRepresentable {
    class Coordinator: NSObject, AVCaptureMetadataOutputObjectsDelegate {
        var parent: QRScannerView
        var isProcessing: Bool = false

        init(parent: QRScannerView) {
            self.parent = parent
        }

        func metadataOutput(
            _ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject],
            from connection: AVCaptureConnection
        ) {
            guard !isProcessing else { return }
            isProcessing = true

            let scannedCodes: [String] = metadataObjects.compactMap { metadataObject -> String? in
                guard
                    let readableObject: AVMetadataMachineReadableCodeObject = metadataObject
                        as? AVMetadataMachineReadableCodeObject
                else { return nil }
                return readableObject.stringValue
            }

            DispatchQueue.main.async { [weak self] in
                if !scannedCodes.isEmpty {
                    self?.parent.didFindCode(scannedCodes)
                }
                self?.isProcessing = false
            }
        }
    }

    var didFindCode: ([String]) -> Void
    @Binding var captureSession: AVCaptureSession

    init(didFindCode: @escaping ([String]) -> Void, captureSession: Binding<AVCaptureSession>) {
        self.didFindCode = didFindCode
        self._captureSession = captureSession
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = UIViewController()

        let cameraPosition: AVCaptureDevice.Position = AVCaptureDevice.Position.back

        // Open the camera device
        let device: AVCaptureDevice? = getDefaultCameraDevice(position: cameraPosition)

        if let cameraDevice: AVCaptureDevice = device {
            do {
                try cameraDevice.lockForConfiguration()
                if cameraDevice.isFocusModeSupported(.continuousAutoFocus) {
                    cameraDevice.focusMode = .continuousAutoFocus
                    print("ContinuousAutoFocus enabled.")
                }
                if #available(iOS 15.4, *), cameraDevice.isFocusModeSupported(.autoFocus) {
                    cameraDevice.automaticallyAdjustsFaceDrivenAutoFocusEnabled = false
                }

                // Optional: Set a lower frame rate to reduce processing overhead
                if cameraDevice.activeVideoMinFrameDuration.timescale > 30 {
                    cameraDevice.activeVideoMinFrameDuration = CMTime(value: 1, timescale: 30)
                    cameraDevice.activeVideoMaxFrameDuration = CMTime(value: 1, timescale: 30)
                }

                cameraDevice.unlockForConfiguration()
            } catch {
                print("Couldnot setup autofocus")
            }
        } else {
            print("Couldnot setup autofocus")
        }

        guard
            let videoCaptureDevice: AVCaptureDevice = AVCaptureDevice.default(
                device!.deviceType, for: .video, position: cameraPosition),
            let videoInput: AVCaptureDeviceInput = try? AVCaptureDeviceInput(
                device: videoCaptureDevice),
            captureSession.canAddInput(videoInput)
        else {
            return viewController
        }

        captureSession.addInput(videoInput)

        let metadataOutput = AVCaptureMetadataOutput()
        guard captureSession.canAddOutput(metadataOutput) else {
            return viewController
        }

        captureSession.addOutput(metadataOutput)
        metadataOutput.setMetadataObjectsDelegate(
            context.coordinator, queue: DispatchQueue.global(qos: .userInitiated))
        metadataOutput.metadataObjectTypes = [.qr]
        metadataOutput.rectOfInterest = CGRect(x: 0, y: 0, width: 0.8, height: 0.8)
        // Define scanning area
        // let scanningArea = CGRect(
        //     x: (UIScreen.main.bounds.width - 300) / 2,
        //     y: (UIScreen.main.bounds.height - 300) / 2,
        //     width: 300,
        //     height: 300)
        // metadataOutput.rectOfInterest = CGRect(
        //     x: scanningArea.origin.y / UIScreen.main.bounds.height,
        //     y: scanningArea.origin.x / UIScreen.main.bounds.width,
        //     width: scanningArea.height / UIScreen.main.bounds.height,
        //     height: scanningArea.width / UIScreen.main.bounds.width
        // )

        DispatchQueue.main.async {
            let previewLayer = AVCaptureVideoPreviewLayer(session: self.captureSession)
            previewLayer.frame = viewController.view.bounds
            previewLayer.videoGravity = .resizeAspectFill
            viewController.view.layer.addSublayer(previewLayer)

            self.captureSession.startRunning()
        }

        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    /// Get the default camera device for the given `position`.
    ///
    /// This function selects the most appropriate camera, when it is available.
    private func getDefaultCameraDevice(position: AVCaptureDevice.Position) -> AVCaptureDevice? {
        if #available(iOS 13.0, *) {
            // Find the built-in Triple Camera, if it exists.

            if let device: AVCaptureDevice = AVCaptureDevice.default(
                .builtInTripleCamera,
                for: .video,
                position: position)
            {
                return device
            }

            // Find the built-in Dual-Wide Camera, if it exists.
            if let device = AVCaptureDevice.default(
                .builtInDualWideCamera,
                for: .video,
                position: position)
            {
                return device
            }
        }

        // Find the built-in Dual Camera, if it exists.
        if let device = AVCaptureDevice.default(
            .builtInDualCamera,
            for: .video,
            position: position)
        {
            return device
        }

        // Find the built-in Wide-Angle Camera, if it exists.
        if let device = AVCaptureDevice.default(
            .builtInWideAngleCamera,
            for: .video,
            position: position)
        {
            return device
        }

        return nil
    }
}
