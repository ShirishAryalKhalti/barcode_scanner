import 'dart:async';
import 'dart:developer';

import 'package:barcode_scanner/barcode_scanner.dart';
import 'package:barcode_scanner_example/native_scanner_view.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late final ScannerController _controller;
  bool isDialogVisible = false;
  @override
  initState() {
    _controller = ScannerController();
    super.initState();
  }

  @override
  void dispose() {
    _controller.disposeScanner();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBody: true,
      extendBodyBehindAppBar: true,
      body: Stack(
        children: [
          NativeScannerView(
            resolution: ScannerResolution.hd720p,
            onScanSuccess: (codes) async {
              DateTime now = DateTime.now();
              unawaited(_controller.stopScanner());
              if (isDialogVisible) return;
              isDialogVisible = true;
              await _showQRDialog(context, codes);
              isDialogVisible = false;
              unawaited(_controller.startScanner());
            },
            onError: (error) {},
          ),
          Padding(
            padding: const EdgeInsets.all(24.0),
            child: Row(
              children: [
                ElevatedButton(
                  onPressed: _controller.startScanner,
                  child: const Text('Start'),
                ),
                ElevatedButton(
                  onPressed: _controller.stopScanner,
                  child: const Text('Stop'),
                ),
                ElevatedButton(
                  onPressed: _controller.toggleTorch,
                  child: const Text('Torch'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _showQRDialog(BuildContext context, List<String> codes) {
    return showCupertinoDialog(
      context: context,
      builder: (context) {
        return Dialog(
          child: Stack(
            children: [
              Padding(
                padding: const EdgeInsets.all(16),
                child: SingleChildScrollView(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      if (codes.length > 1)
                        Text(
                          '${codes.length} codes detected.',
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                      const SizedBox(height: 16),
                      for (final code in codes)
                        Padding(
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          child: Text(code),
                        ),
                    ],
                  ),
                ),
              ),
              Align(
                alignment: Alignment.bottomCenter,
                child: ElevatedButton(
                  style: ButtonStyle(minimumSize: WidgetStatePropertyAll(Size(MediaQuery.sizeOf(context).width, 60))),
                  onPressed: Navigator.of(context).pop,
                  child: const Text('Close'),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
