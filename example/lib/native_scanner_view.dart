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
  });

  final void Function(List<String> codes) onScanSuccess;
  final void Function(ScannerError error) onError;
  final ScannerResolution resolution;

  @override
  State<NativeScannerView> createState() => _NativeScannerViewState();
}

class _NativeScannerViewState extends State<NativeScannerView> implements ScannerFlutterApi {
  final creationParams = <String, dynamic>{};

  @override
  initState() {
    ScannerFlutterApi.setUp(this);
    creationParams['scanner_resolution'] = widget.resolution.val;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = '<platform-view-type>';
    // Pass parameters to the platform side.

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

enum ScannerResolution {
  sd480p(0),
  hd720p(1),
  hd1080p(2);

  const ScannerResolution(this.val);
  final int val;
}
