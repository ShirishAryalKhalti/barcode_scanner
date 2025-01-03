import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/src/generated/scanner.g.dart',
    dartOptions: DartOptions(),
    swiftOut: 'ios/Classes/Scanner.swift',
    swiftOptions: SwiftOptions(errorClassName: 'BarcodeScannerException'),
    kotlinOut: 'android/src/main/kotlin/com/example/barcode_scanner/Scanner.kt',
    kotlinOptions: KotlinOptions(errorClassName: 'BarcodeScannerException'),
  ),
)
class ScannedCode {
  String? text;
  String? format;
}

@FlutterApi()
abstract class ScannerFlutterApi {
  void onScanSuccess(List<ScannedCode> codes);
  void onScanError(ScannerError error);
}

class ScannerError {
  String? message;
  String? tag;
}

@HostApi()
abstract class ScannerController {
  bool toggleTorch();
  void startScanner();
  void stopScanner();
  void disposeScanner();
}
