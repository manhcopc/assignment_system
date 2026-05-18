# Exam Assignment System

Ứng dụng Java Socket client/server dùng Apache POI để đọc danh sách cán bộ coi thi, danh sách phòng thi và tạo file Excel phân công coi thi.

## Yêu cầu môi trường

- Java 8 trở lên
- Maven
- Không dùng database
- Không dùng giao diện Swing, chỉ chạy console

## Cấu trúc dữ liệu đầu vào

Đặt các file Excel đầu vào trong thư mục `data/`:

### `data/CANBOCOITHI.xlsx`

File có sheet đầu tiên với hàng tiêu đề và các cột:

1. `TT`
2. `Mã GV`
3. `Họ Tên`
4. `Ngày sinh`
5. `Đơn vị công tác`

### `data/PHONGTHI.xlsx`

File có sheet đầu tiên với hàng tiêu đề và các cột:

1. `STT`
2. `Phòng thi`
3. `Ghi chú`

## Quy tắc xử lý

- Server chạy tại port `9999`.
- Client nhập số phòng thi cần sử dụng và số cán bộ giám sát rồi gửi sang server.
- Server kiểm tra:
  - số phòng thi nhập vào không lớn hơn số phòng trong `data/PHONGTHI.xlsx`;
  - tổng số cán bộ cần dùng = `số phòng thi * 2 + số cán bộ giám sát`;
  - tổng số cán bộ cần dùng không lớn hơn số cán bộ trong `data/CANBOCOITHI.xlsx`.
- Mỗi phòng thi có đúng 2 giám thị:
  - giám thị thứ nhất được đánh `X` ở cột `Giám thị 1`;
  - giám thị thứ hai được đánh `X` ở cột `Giám thị 2`.
- Các cán bộ còn lại theo số giám sát nhập vào được đưa sang danh sách giám sát.
- Phòng thi được giám sát được chia vòng tròn theo công thức `i % soPhongThi`.

## Build project

```bash
mvn clean package
```

Sau khi build, Maven tạo file jar có đầy đủ dependency tại:

```text
target/exam_assignment_system-1.0-SNAPSHOT.jar
```

## Chạy server

Mở terminal thứ nhất tại thư mục project và chạy:

```bash
java -cp target/exam_assignment_system-1.0-SNAPSHOT.jar vn.exam.server.ExamServer
```

Server sẽ đọc dữ liệu từ:

- `data/CANBOCOITHI.xlsx`
- `data/PHONGTHI.xlsx`

## Chạy client

Mở terminal thứ hai tại thư mục project và chạy:

```bash
java -cp target/exam_assignment_system-1.0-SNAPSHOT.jar vn.exam.client.ExamClient
```

Nhập lần lượt:

1. số phòng thi cần sử dụng;
2. số cán bộ giám sát.

Nếu xử lý thành công, client nhận file từ server và lưu tại:

```text
output/ket_qua_phan_cong.xlsx
```

## File kết quả

File Excel kết quả gồm 3 sheet:

1. `Danh sách phân công`
   - `STT`, `Mã GV`, `Họ và tên`, `Giám thị 1`, `Giám thị 2`, `Phòng thi`
2. `Danh sách giám sát`
   - `STT`, `Mã GV`, `Họ và tên`, `Phòng thi được giám sát`
3. `Thống kê`
   - `Nội dung`, `Giá trị`

## Xử lý lỗi

Chương trình hiển thị thông báo lỗi rõ ràng trên console trong các trường hợp phổ biến:

- thiếu file Excel đầu vào;
- số phòng thi nhập vào vượt quá dữ liệu trong file;
- không đủ cán bộ để phân công;
- dữ liệu đầu vào rỗng;
- lỗi tạo hoặc truyền file Excel kết quả.
