# MyQR - Trình tạo VietQR siêu tốc

Ứng dụng Android tối giản giúp tạo mã VietQR tức thì, hỗ trợ widget màn hình chính.

## Tính năng chính
- **Tạo mã VietQR Offline**: Tự động tính toán mã CRC-16 theo tiêu chuẩn EMVCo/VietQR.
- **Widget màn hình chính**: Truy cập nhanh để nhận tiền mà không cần chờ ứng dụng load.
- **Native Quick View**: Sử dụng Android Translucent Activity (Kotlin) để hiển thị mã QR ngay lập tức từ widget, bỏ qua runtime của Flutter.
- **Giao diện hiện đại**: Hỗ trợ giao diện tối (Dark Mode) và phong cách Glassmorphism.

## Cài đặt & Chạy
1. Cài đặt Flutter: `flutter pub get`
2. Chạy ứng dụng: `flutter run`
3. Cấu hình Widget: Xem hướng dẫn chi tiết tại [WIDGET_SETUP_INSTRUCTIONS.md](file:///c:/Project/myproject/myqr/myqr/WIDGET_SETUP_INSTRUCTIONS.md)

## Công nghệ sử dụng
- **Frontend**: Flutter (Dart)
- **Native**: Kotlin, Android RemoteViews (Widget)
- **QR**: qr_flutter, intl
