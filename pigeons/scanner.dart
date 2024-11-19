import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/src/generated/scanner.g.dart',
    dartOptions: DartOptions(),
    swiftOut: 'ios/Classes/Scanner.swift',
    swiftOptions: SwiftOptions(),
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
