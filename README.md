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

- Server bind tới địa chỉ `0.0.0.0` và chạy tại port `9999`, nên các máy trong cùng mạng LAN có thể kết nối bằng IP của máy server.
- Server lắng nghe nhiều client và xử lý mỗi client bằng một thread riêng.
- Client nhập IP server, số phòng thi cần sử dụng, số cán bộ giám sát mỗi ca và số ca thi rồi gửi sang server.
- Server kiểm tra:
  - số phòng thi nhập vào không lớn hơn số phòng trong `data/PHONGTHI.xlsx`;
  - tổng số cán bộ cần dùng mỗi ca = `số phòng thi * 2 + số cán bộ giám sát mỗi ca`;
  - tổng số cán bộ cần dùng mỗi ca không lớn hơn số cán bộ trong `data/CANBOCOITHI.xlsx`.
- Mỗi ca, mỗi phòng thi có đúng 2 giám thị:
  - giám thị thứ nhất được đánh `X` ở cột `Giám thị 1`;
  - giám thị thứ hai được đánh `X` ở cột `Giám thị 2`.
- Một cán bộ không xuất hiện 2 lần trong cùng một ca.
- Một cán bộ không coi lại phòng đã từng coi ở các ca trước.
- Hai cán bộ đã từng ghép cặp ở ca trước không được ghép lại ở ca sau.
- Cán bộ làm giám sát ở ca trước vẫn có thể làm giám thị ở ca sau.
- Sau khi phân công đủ giám thị cho một ca, các cán bộ chưa dùng trong ca đó được chọn làm giám sát.
- Phòng thi được giám sát được chia vòng tròn theo công thức `i % soPhongThi`.

## Build project

```bash
mvn clean package
```

Sau khi build, Maven tạo file jar có đầy đủ dependency tại:

```text
target/exam_assignment_system-1.0-SNAPSHOT.jar
```

## Chạy server trong mạng LAN

Mở terminal trên máy dùng làm server tại thư mục project và chạy:

```bash
mvn exec:java -Dexec.mainClass="vn.exam.server.ExamServer"
```

Server sẽ bind `0.0.0.0:9999`, lắng nghe nhiều client trong cùng mạng LAN và in IP của từng client khi có kết nối.

Server sẽ đọc dữ liệu từ:

- `data/CANBOCOITHI.xlsx`
- `data/PHONGTHI.xlsx`

Để các máy client kết nối được, hãy lấy IP LAN của máy server, ví dụ:

- Windows: chạy `ipconfig` và xem IPv4 Address của card mạng đang dùng.
- Linux/macOS: chạy `ip addr` hoặc `ifconfig` và xem địa chỉ dạng `192.168.x.x`/`10.x.x.x`.

Nếu client không kết nối được, hãy kiểm tra:

- server đã chạy chưa;
- hai máy có cùng mạng LAN không;
- firewall của máy server đã cho phép port `9999` chưa.

## Chạy client trong mạng LAN

Mở terminal trên máy client tại thư mục project và chạy:

```bash
mvn exec:java -Dexec.mainClass="vn.exam.client.ExamClient"
```

Nhập lần lượt:

1. IP server, ví dụ `192.168.1.10`;
2. số phòng thi cần sử dụng;
3. số cán bộ giám sát mỗi ca;
4. số ca thi.

Nếu xử lý thành công, client nhận file từ server và lưu tại:

```text
output/ket_qua_phan_cong.xlsx
```

## File kết quả

File Excel kết quả gồm 3 sheet:

1. `Danh sách phân công`
   - `Ca thi`, `STT`, `Mã GV`, `Họ và tên`, `Giám thị 1`, `Giám thị 2`, `Phòng thi`
2. `Danh sách giám sát`
   - `Ca thi`, `STT`, `Mã GV`, `Họ và tên`, `Phòng thi được giám sát`
3. `Thống kê`
   - `Nội dung`, `Giá trị`
   - Gồm: số phòng thi sử dụng, số cán bộ giám thị mỗi ca, số cán bộ giám sát mỗi ca, số ca thi, tổng số dòng phân công giám thị, tổng số dòng phân công giám sát.

## Xử lý lỗi

Chương trình hiển thị thông báo lỗi rõ ràng trên console trong các trường hợp phổ biến:

- thiếu file Excel đầu vào;
- số phòng thi nhập vào vượt quá dữ liệu trong file;
- không đủ cán bộ để phân công theo từng ca;
- dữ liệu đầu vào rỗng;
- lỗi tạo hoặc truyền file Excel kết quả;
- client nhập sai IP server, server chưa chạy, firewall chặn port `9999`, hoặc hai máy không cùng mạng LAN.
