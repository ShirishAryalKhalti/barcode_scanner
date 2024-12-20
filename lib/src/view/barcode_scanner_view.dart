import 'package:barcode_scanner/barcode_scanner.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class NativeScannerView extends StatefulWidget {
  const NativeScannerView({
    super.key,
    required this.onScanSuccess,
    required this.onError,
    this.resolution = ScannerResolution.hd720p,
    this.formats = const {BarcodeFormat.qrCode},
  });

  /// Callback that is called when a barcode is scanned.
  final void Function(List<String> codes) onScanSuccess;

  /// Callback that is called when an error occurs.
  final void Function(ScannerError error) onError;

  /// The resolution to use for the camera.
  ///
  /// Defaults to [ScannerResolution.hd720p].
  final ScannerResolution resolution;

  /// The barcode formats to scan for.
  ///
  /// Defaults to [BarcodeFormat.qrCode].
  final Set<BarcodeFormat> formats;

  @override
  State<NativeScannerView> createState() => _NativeScannerViewState();
}

class _NativeScannerViewState extends State<NativeScannerView> implements ScannerFlutterApi {
  final creationParams = <String, dynamic>{};
  final String viewType = 'barcode_scanner_view';

  @override
  initState() {
    ScannerFlutterApi.setUp(this);
    creationParams['scanner_resolution'] = widget.resolution.val;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return switch (defaultTargetPlatform) {
      TargetPlatform.android => AndroidView(
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
        ),
      TargetPlatform.iOS => UiKitView(
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
        ),
      _ => throw UnimplementedError(),
    };
  }

  @override
  void onScanSuccess(List<String> codes) => widget.onScanSuccess(codes);

  @override
  void onScanError(ScannerError error) => widget.onError(error);
}
