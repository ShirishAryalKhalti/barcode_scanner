import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/src/generated/scanner.g.dart',
    dartOptions: DartOptions(),
    swiftOut: 'ios/Classes/Scanner.swift',
    swiftOptions: SwiftOptions(),
    kotlinOut: 'android/src/main/kotlin/com/example/scanner/Scanner.kt',
  ),
)
@FlutterApi()
abstract class ScannerFlutterApi {
  void onScanSuccess(List<String> codes);
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
}
