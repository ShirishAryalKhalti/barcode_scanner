enum ScannerResolution {
  sd480p(0),
  hd720p(1),
  hd1080p(2);

  const ScannerResolution(this.val);
  final int val;
}

enum BarcodeFormat {
  aztec,
  codabar,
  code39,
  code93,
  code128,
  dataBar,
  dataBarExpanded,
  dataMatrix,
  ean8,
  ean13,
  itf,
  maxicode,
  pdf417,
  qrCode,
  microQrCode,
  rmqrCode,
  upcA,
  upcE,
}
