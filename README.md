Đây là bản build chưa hoàn thiện app nghe nhạc của mình tại lớp khoa pham có sử dụng foreground service.

![image](https://github.com/anhoang241998/MusicApp/blob/master/video/pic.gif)

## Features
- Có 2 màn hình, màn hình play nhạc và màn hình setting. navigate giữa chúng bằng 1 thanh toolbar.
- Màn hình settings để set DarkMode cho app
- Play/Pause/Next/Previous nhạc ở player activity
- Có Notification ForegroundService để play nhạc khi đã kill app.
- Có thể Play/Pause/Next/Previous ở notification.
- Notification sử dụng Androidx Media Style.
- Có thể swipe to delete hoặc ấn vào nút delete trên notification để clear notification.
- App có sử dụng animation, tách màu từ ảnh ra gán cho background bằng Palette API.

## Not Done Features
- Chưa sử dụng được MediaBrowserService.
- Chưa track dc bài hát bằng thanh seekbar trên notification
- Chưa có thể mở lại đúng position mà người dùng rời khỏi khi kill Activity bằng thanh notification.
- Chưa làm recyclerView để tạo ra danh sách.
- Chưa có chức năng repeat 1 bài, repeat toàn bộ các bài, và shuffle tất cả các bài.
- Chưa có chức năng love.

## How to download / clone it
Các bạn có thể download (clone) về bằng 2 cách:

Cách 1: Clone về

Các bạn nhấn vào góc phải màn hình có nút clone or download. Các bạn clone về bằng cách sao chép cái đường link trong cái ô clone with HTTPS.

Các bạn nhấp phải chuột tại folder muốn lưu file hoặc ra desktop, chọn git bash here

Sau đó các bạn gõ câu lệnh: git clone ấn nút phải chuột paste link mới chép vô.

Vd: `git clone https://github.com/anhoang241998/MusicApp.git`

Vậy là xong.

Cách 2: download về

Cách này đơn giản hơn, các bạn chỉ cần nhấn vào góc phải màn hình có nút clone or download. Các bạn chọn Download ZIP
