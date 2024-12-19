import 'package:barcode_scanner/barcode_scanner.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class NativeScannerView extends StatefulWidget {
  const NativeScannerView({
    super.key,
    required this.onScanSuccess,
    required this.onError,
  });

  final void Function(List<String> codes) onScanSuccess;
  final void Function(ScannerError error) onError;
  @override
  State<NativeScannerView> createState() => _NativeScannerViewState();
}

class _NativeScannerViewState extends State<NativeScannerView> implements ScannerFlutterApi {
  @override
  initState() {
    ScannerFlutterApi.setUp(this);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = '<platform-view-type>';
    // Pass parameters to the platform side.
    final Map<String, dynamic> creationParams = <String, dynamic>{'resolution': '1080p', 'camera_position': 'back'};

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

// if the selected quality is not available then [hd1280x720] will be used as default
enum ScannerResolution {
  hd4K3840x2160,
  hd1920x1080,
  hd1280x720,
  vga640x480,
}
