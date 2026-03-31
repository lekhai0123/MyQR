class VietQRGenerator {
  static const String bankBin = '970422';
  static const String accountNumber = '0767681248';
  static const String accountName = 'LE THANH KHAI';
  static const String guid = 'A000000727';
  static const String serviceCode = 'QRIBFTTA';

  /// Generates the EMVCo/VietQR string for the given amount and content.
  static String generate(int amount, {String content = 'CHUYEN TIEN NHANH'}) {
    String payload = '';
    payload += _formatTag('00', '01'); // Payload Format Indicator
    payload += _formatTag('01', '12'); // Point of Initiation Method: 12 (Dynamic)

    // Tag 38: Merchant Account Information
    String merchantInfo = '';
    merchantInfo += _formatTag('00', guid);
    
    String consumerInfo = '';
    consumerInfo += _formatTag('00', bankBin);
    consumerInfo += _formatTag('01', accountNumber);
    
    merchantInfo += _formatTag('01', consumerInfo);
    merchantInfo += _formatTag('02', serviceCode);
    
    payload += _formatTag('38', merchantInfo);

    // Tag 53: Transaction Currency (VND = 704)
    payload += _formatTag('53', '704');
    
    // Tag 54: Transaction Amount
    if (amount > 0) {
      payload += _formatTag('54', amount.toString());
    }

    // Tag 58: Country Code
    payload += _formatTag('58', 'VN');

    // Tag 62: Additional Data Field Template
    if (content.isNotEmpty) {
      String additionalData = _formatTag('08', content);
      payload += _formatTag('62', additionalData);
    }

    // Tag 63: CRC (Id + Length appended first)
    payload += '6304';
    
    String crc = _calculateCRC16(payload);
    return payload + crc;
  }

  /// Helper to format a TLV (Tag-Length-Value) component
  static String _formatTag(String identifier, String value) {
    String length = value.length.toString().padLeft(2, '0');
    return '$identifier$length$value';
  }

  /// Calculates the CRC-16/CCITT-FALSE checksum
  /// Polynomial: 0x1021, Initial Value: 0xFFFF
  static String _calculateCRC16(String data) {
    int crc = 0xFFFF;
    for (int i = 0; i < data.length; i++) {
      int c = data.codeUnitAt(i);
      crc ^= c << 8;
      for (int j = 0; j < 8; j++) {
        if ((crc & 0x8000) != 0) {
          crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
        } else {
          crc = (crc << 1) & 0xFFFF;
        }
      }
    }
    return crc.toRadixString(16).toUpperCase().padLeft(4, '0');
  }
}
