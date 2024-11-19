import 'barcode_scanner_platform_interface.dart';

export './src/generated/scanner.g.dart';

class BarcodeScanner {
  Future<String?> getPlatformVersion() {
    return BarcodeScannerPlatform.instance.getPlatformVersion();
  }
}
