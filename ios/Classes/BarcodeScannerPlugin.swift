import Flutter
import SwiftUI
import UIKit

@available(iOS 13.0, *)
public class BarcodeScannerPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(
      name: "barcode_scanner", binaryMessenger: registrar.messenger())

    let instance = BarcodeScannerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    let factory = FLNativeViewFactory(messenger: registrar.messenger())
    registrar.register(factory, withId: "<platform-view-type>")
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}

@available(iOS 13.0, *)
class FLNativeViewFactory: NSObject, FlutterPlatformViewFactory {
  private var messenger: FlutterBinaryMessenger

  init(messenger: FlutterBinaryMessenger) {
    self.messenger = messenger
    super.init()
  }

  func create(
    withFrame frame: CGRect,
    viewIdentifier viewId: Int64,
    arguments args: Any?
  ) -> FlutterPlatformView {
    return BarcodeScannerPlatformView(
      frame: frame, viewIdentifier: viewId, arguments: args, binaryMessenger: messenger)
  }

  /// Implementing this method is only necessary when the `arguments` in `createWithFrame` is not `nil`.
  public func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
    return FlutterStandardMessageCodec.sharedInstance()
  }
}
