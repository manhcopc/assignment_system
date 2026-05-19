# 📅 BÁO CÁO: LOGIC PHÂN CÔNG THEO CA (EXAM SESSION)

## 📋 Mục lục
1. [Khái niệm Ca Thi](#khái-niệm-ca-thi)
2. [Quy trình tạo Ca Thi](#quy-trình-tạo-ca-thi)
3. [Cấu trúc dữ liệu Ca Thi](#cấu-trúc-dữ-liệu-ca-thi)
4. [Vòng đời Ca Thi](#vòng-đời-ca-thi)
5. [Lịch sử phân công](#lịch-sử-phân-công)
6. [Sơ đồ quy trình](#sơ-đồ-quy-trình)

---

## 🎯 Khái niệm Ca Thi

**Ca thi (Exam Session)** là một phiên phân công cán bộ coi thi cho một kỳ thi cụ thể.

### Thông tin cơ bản của Ca Thi:

| Trường                 | Kiểu dữ liệu | Mô tả                       | Ví dụ                        |
| ---------------------- | ------------ | --------------------------- | ---------------------------- |
| **id**                 | LONG (PK)    | ID tự tăng, duy nhất        | 1, 2, 3, ...                 |
| **ma_ca_thi**          | VARCHAR      | Mã ca thi dựa vào thời gian | `ca_thi_20260519_083744_017` |
| **ten_ca**             | VARCHAR      | Tên ca thi tự động          | `Ca 1`, `Ca 2`, `Ca 3`       |
| **ten_ca_thi**         | VARCHAR      | Tên ca thi người dùng đặt   | `Ca thi buổi sáng`           |
| **so_luong_can_bo**    | INT          | Số cán bộ được phân công    | 30                           |
| **so_luong_phong_thi** | INT          | Số phòng thi                | 10                           |
| **ten_file_excel**     | VARCHAR      | Tên file input Excel        | `database`, `upload`         |

---

## 🔄 Quy trình tạo Ca Thi

### **BƯỚC 1: Client gửi yêu cầu phân công**

```java
// Từ ClientFrame.java
ExamSessionInfo sessionInfo = new ExamSessionInfo(
    soLuongCanBo,      // Ví dụ: 30 cán bộ
    soLuongPhongThi,   // Ví dụ: 10 phòng thi
    "Ca thi buổi sáng" // Tên ca thi (optional)
);
```

**Dữ liệu gửi lên:**
```java
out.writeUTF("ASSIGN_REQUEST");
out.writeInt(sessionInfo.getSoLuongCanBo());      // 30
out.writeInt(sessionInfo.getSoLuongPhongThi());   // 10
out.writeUTF(sessionInfo.getTenCaThi());          // "Ca thi buổi sáng"
```

---

### **BƯỚC 2: Server tạo phiên phân công (Session Name)**

```java
// Từ ClientHandler.java - processAssignmentRequest()
String sessionName = buildSessionName();
// Output: "ca_thi_20260519_083744_017"
//          └─ Timestamp: 2026-05-19 08:37:44.017

private String buildSessionName() {
    String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
    return "ca_thi_" + timestamp;
}
```

**Mục đích:**
- Tạo tên duy nhất cho mỗi ca thi
- Dùng timestamp để phân biệt các ca thi
- Tạo thư mục output: `/server_outputs/ca_thi_20260519_083744_017/`

---

### **BƯỚC 3: Lấy dữ liệu từ Database**

```java
// Từ ClientHandler.java - processAssignmentRequest()
PhanCongDAO dao = new PhanCongDAO();

// Lấy m cán bộ từ database
List<CanBo> danhSachCanBo = dao.findCanBoForAssignment(m);
//                                                      ↑
//                                          Từ ExamSessionInfo

// Lấy n phòng thi từ database
List<PhongThi> danhSachPhongThi = dao.findPhongThiForAssignment(n);
//                                                                ↑
//                                                    Từ ExamSessionInfo
```

**Query SQL:**

```sql
-- Lấy cán bộ
SELECT tt, ho_ten, ngay_sinh, ma_gv, don_vi_cong_tac
FROM can_bo
ORDER BY COALESCE(tt, 2147483647), ma_gv
LIMIT ?;
-- Sắp xếp: tt hợp lệ trước, null sau, rồi theo mã GV

-- Lấy phòng thi
SELECT stt, phong_thi, ghi_chu
FROM phong_thi
ORDER BY COALESCE(stt, 2147483647), phong_thi
LIMIT ?;
-- Sắp xếp: stt hợp lệ trước, null sau, rồi theo tên phòng
```

---

### **BƯỚC 4: Tải lịch sử phân công cũ**

```java
AssignmentHistory history = dao.loadHistory();
```

**Lịch sử bao gồm:**

```
history = {
  canBoPhong: Set<String> = {
    "GV001_P101", "GV001_P102", "GV002_P101", ...
  },
  canBoPair: Set<String> = {
    "GV001_GV002", "GV002_GV003", ...
  },
  giamSatPhong: Set<String> = {
    "GV101_P101", "GV102_P102", ...
  }
}
```

**Mục đích:** Tránh phân công lại cùng phòng, cùng cặp, hoặc giám sát lại phòng

---

### **BƯỚC 5: Gọi thuật toán phân công**

```java
PhanCongService.AssignmentResult result = new PhanCongService().phanCong(
    danhSachCanBo,      // 30 cán bộ
    danhSachPhongThi,   // 10 phòng thi
    history             // Lịch sử phân công cũ
);
```

**Kết quả:**
```java
result = {
  danhSachPhanCong: List<PhanCong> = [
    PhanCong(1, "GV001", "Nguyễn Văn A", true, false, "P101"),
    PhanCong(2, "GV002", "Trần Thị B", false, true, "P101"),
    ...
  ],
  danhSachGiamSat: List<GiamSat> = [
    GiamSat(1, "GV031", "Hoàng Văn C", [P101, P102], "P101, P102"),
    ...
  ]
}
```

---

### **BƯỚC 6: Lưu Ca Thi vào Database**

```java
// Từ ClientHandler.java - processAssignmentRequest()
long caThiId = dao.saveAssignmentResult(
    sessionName,                    // "ca_thi_20260519_083744_017"
    "database",                     // Nguồn dữ liệu
    sessionInfo,                    // m=30, n=10
    result.getDanhSachPhanCong(),   // 20 dòng
    result.getDanhSachGiamSat()     // 10 dòng
);
// Output: caThiId = 1
```

**Quá trình bên trong `saveAssignmentResult()`:**

#### **6.1 Tạo Ca Thi mới**

```sql
INSERT INTO ca_thi 
(ma_ca_thi, ten_ca, ten_ca_thi, so_luong_can_bo, so_luong_phong_thi, ten_file_excel)
VALUES 
('ca_thi_20260519_083744_017', 'Ca', 'Ca thi buổi sáng', 30, 10, 'database');

-- Kết quả: id = 1 (auto generated)
```

#### **6.2 Cập nhật tên Ca thi tự động**

```java
// Sau khi có ID, cập nhật lại ten_ca
String tenCaThi = "Ca " + caThiId;  // "Ca 1"
updateAutoTenCaThi(connection, caThiId);
```

```sql
UPDATE ca_thi 
SET ten_ca = 'Ca 1', 
    ten_ca_thi = 'Ca 1' 
WHERE id = 1;
```

#### **6.3 Lưu phân công coi thi**

```sql
INSERT INTO phan_cong_coi_thi 
(ca_thi_id, stt, ma_gv, ho_ten, vai_tro, phong_thi)
VALUES
(1, 1, 'GV001', 'Nguyễn Văn A', 'GIAM_THI_1', 'P101'),
(1, 2, 'GV002', 'Trần Thị B', 'GIAM_THI_2', 'P101'),
(1, 3, 'GV003', 'Lê Văn C', 'GIAM_THI_1', 'P102'),
...
```

#### **6.4 Lưu phân công giám sát**

```sql
INSERT INTO phan_cong_giam_sat 
(ca_thi_id, stt, ma_gv, ho_ten, phong_thi_duoc_giam_sat, mo_ta_phong_giam_sat)
VALUES
(1, 1, 'GV031', 'Hoàng Văn C', 'P101, P102', 'P101, P102'),
(1, 2, 'GV032', 'Võ Thị D', 'P103', 'P103'),
...
```

---

### **BƯỚC 7: Xuất file Excel**

```java
// Từ ClientHandler.java - processAssignmentRequest()
String tenCaThi = "Ca " + caThiId;  // "Ca 1"

ExcelUtil excelUtil = new ExcelUtil();
File phanCongFile = new File(outputDir, 
    "DANHSACHPHANCONG_Ca_1.xlsx");
File giamSatFile = new File(outputDir, 
    "DANHSACHGIAMSAT_Ca_1.xlsx");

excelUtil.writeDanhSachPhanCong(
    dao.findPhanCongByCaThi(caThiId), 
    phanCongFile
);
excelUtil.writeDanhSachGiamSat(
    dao.findGiamSatByCaThi(caThiId), 
    giamSatFile
);
```

---

### **BƯỚC 8: Gửi file về Client**

```java
out.writeUTF("SUCCESS");
out.writeUTF(tenCaThi);  // "Ca 1"
FileTransferUtil.sendMultipleFiles(out, 
    List.of(phanCongFile, giamSatFile)
);
```

---

## 📊 Cấu trúc dữ liệu Ca Thi

### **Bảng: ca_thi**

```sql
CREATE TABLE ca_thi (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ma_ca_thi VARCHAR(50) UNIQUE NOT NULL,           -- "ca_thi_20260519_083744_017"
    ten_ca VARCHAR(50),                             -- "Ca 1"
    ten_ca_thi VARCHAR(255),                        -- "Ca thi buổi sáng"
    so_luong_can_bo INT NOT NULL,                   -- 30
    so_luong_phong_thi INT NOT NULL,                -- 10
    ten_file_excel VARCHAR(255),                    -- "database"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_ma_ca_thi (ma_ca_thi),
    INDEX idx_created_at (created_at)
);
```

### **Bảng: phan_cong_coi_thi**

```sql
CREATE TABLE phan_cong_coi_thi (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ca_thi_id BIGINT NOT NULL,                      -- Khóa ngoại → ca_thi.id
    stt INT NOT NULL,                               -- 1, 2, 3, ...
    ma_gv VARCHAR(50) NOT NULL,                     -- "GV001"
    ho_ten VARCHAR(255) NOT NULL,                   -- "Nguyễn Văn A"
    vai_tro VARCHAR(50) NOT NULL,                   -- "GIAM_THI_1" hoặc "GIAM_THI_2"
    phong_thi VARCHAR(50) NOT NULL,                 -- "P101"
    
    FOREIGN KEY (ca_thi_id) REFERENCES ca_thi(id),
    INDEX idx_ca_thi_id (ca_thi_id),
    INDEX idx_ma_gv (ma_gv),
    INDEX idx_phong_thi (phong_thi)
);
```

### **Bảng: phan_cong_giam_sat**

```sql
CREATE TABLE phan_cong_giam_sat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ca_thi_id BIGINT NOT NULL,                      -- Khóa ngoại → ca_thi.id
    stt INT NOT NULL,                               -- 1, 2, 3, ...
    ma_gv VARCHAR(50) NOT NULL,                     -- "GV031"
    ho_ten VARCHAR(255) NOT NULL,                   -- "Hoàng Văn C"
    phong_thi_duoc_giam_sat VARCHAR(500) NOT NULL,  -- "P101, P102"
    mo_ta_phong_giam_sat VARCHAR(500),              -- "Từ P101 đến P102"
    
    FOREIGN KEY (ca_thi_id) REFERENCES ca_thi(id),
    INDEX idx_ca_thi_id (ca_thi_id),
    INDEX idx_ma_gv (ma_gv)
);
```

---

## 🔄 Vòng đời Ca Thi

```
┌─────────────────────────────────────────────────────────┐
│           VÒNG ĐỜI CỦA CA THI                           │
└─────────────────────────────────────────────────────────┘

1️⃣  CLIENT GỬI YÊU CẦU
    └─ ASSIGN_REQUEST với m, n, tên ca thi

2️⃣  SERVER TẠO SESSION NAME
    └─ "ca_thi_20260519_083744_017"

3️⃣  LẤY DỮ LIỆU TỪ DATABASE
    ├─ m cán bộ từ can_bo
    ├─ n phòng thi từ phong_thi
    └─ Lịch sử phân công cũ

4️⃣  THUẬT TOÁN PHÂN CÔNG (Backtracking)
    ├─ Lặp 100 lần nếu cần
    ├─ Phân công giám thị (2/phòng)
    └─ Phân công giám sát (còn lại)

5️⃣  LƯU VÀO DATABASE (Transaction)
    ├─ Tạo ca_thi mới (id = N)
    ├─ Cập nhật ten_ca = "Ca N"
    ├─ Lưu phan_cong_coi_thi (20 dòng)
    └─ Lưu phan_cong_giam_sat (10 dòng)

6️⃣  XUẤT FILE EXCEL
    ├─ DANHSACHPHANCONG_Ca_N.xlsx
    └─ DANHSACHGIAMSAT_Ca_N.xlsx

7️⃣  GỬI FILE VỀ CLIENT
    ├─ SUCCESS
    ├─ "Ca N"
    └─ 2 file Excel

8️⃣  LƯU LỊCH SỬ PHÂN CÔNG
    ├─ Cập nhật AssignmentHistory
    ├─ Lần tới sẽ tránh:
    │  ├─ Cùng cán bộ - cùng phòng
    │  ├─ Cùng cặp cán bộ
    │  └─ Cùng cán bộ giám sát - phòng
    └─ (Tự động khi loadHistory())
```

---

## 📚 Lịch sử phân công

### **Mục đích:**
Tránh phân công lại cán bộ cho phòng/cặp mà họ đã làm trước đó.

### **3 loại lịch sử:**

#### **1. Can Bo Phong (Cán bộ - Phòng)**

```
Dữ liệu: {
  "GV001_P101",
  "GV001_P102",
  "GV002_P101",
  "GV003_P103",
  ...
}

Query:
SELECT ma_gv, phong_thi FROM phan_cong_coi_thi;

Ý nghĩa: 
- GV001 đã coi P101 lần trước → Không phân công GV001 coi P101 lần này
```

#### **2. Can Bo Pair (Cặp cán bộ)**

```
Dữ liệu: {
  "GV001_GV002",  (GV001 < GV002 để tránh trùng lặp)
  "GV001_GV003",
  "GV002_GV005",
  ...
}

Query:
SELECT a.ma_gv AS ma_1, b.ma_gv AS ma_2
FROM phan_cong_coi_thi a
JOIN phan_cong_coi_thi b
    ON a.ca_thi_id = b.ca_thi_id
    AND a.phong_thi = b.phong_thi
    AND a.ma_gv < b.ma_gv;

Ý nghĩa:
- GV001 và GV002 đã coi chung P101 → Không phân công họ coi cùng nhau lần này
```

#### **3. Giam Sat Phong (Giám sát - Phòng)**

```
Dữ liệu: {
  "GV101_P101",
  "GV102_P101",
  "GV102_P102",
  ...
}

Query:
SELECT ma_gv, phong_thi_duoc_giam_sat FROM phan_cong_giam_sat;

Ý nghĩa:
- GV101 đã giám sát P101 → Không cho GV101 giám sát P101 lần này
- GV101 cũng không được làm giám thị coi P101 (để tránh cùng phòng)
```

---

## 🏗️ Sơ đồ quy trình

### **Sơ đồ quá trình phân công theo ca:**

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT SIDE                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. User nhập:                                         │
│     ├─ m = 30 (số cán bộ)                             │
│     ├─ n = 10 (số phòng)                              │
│     └─ Tên ca thi = "Ca thi buổi sáng"                │
│                                                         │
│  2. Click "Chạy phân công"                            │
│     └─ Gửi ASSIGN_REQUEST lên server                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
                        │
                        │ Network
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   SERVER SIDE                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  3. ClientHandler nhận request                         │
│     └─ Tạo session name: "ca_thi_20260519_083744_017" │
│                                                         │
│  4. PhanCongDAO.findCanBoForAssignment(30)            │
│     └─ Query: SELECT * FROM can_bo LIMIT 30          │
│     └─ Result: List<CanBo> [GV001, GV002, ...]       │
│                                                         │
│  5. PhanCongDAO.findPhongThiForAssignment(10)         │
│     └─ Query: SELECT * FROM phong_thi LIMIT 10       │
│     └─ Result: List<PhongThi> [P101, P102, ...]      │
│                                                         │
│  6. PhanCongDAO.loadHistory()                         │
│     ├─ Load canBoPhong từ phan_cong_coi_thi          │
│     ├─ Load canBoPair từ phan_cong_coi_thi (JOIN)   │
│     └─ Load giamSatPhong từ phan_cong_giam_sat       │
│                                                         │
│  7. PhanCongService.phanCong()                        │
│     ├─ Backtracking algorithm (max 100 retries)      │
│     ├─ → Phân công giám thị (2/phòng)               │
│     └─ → Phân công giám sát (còn lại)               │
│     └─ Result: AssignmentResult                      │
│                                                         │
│  8. PhanCongDAO.saveAssignmentResult()               │
│     ├─ Transaction begin                             │
│     ├─ INSERT ca_thi → id = 1                        │
│     ├─ UPDATE ten_ca = "Ca 1"                        │
│     ├─ INSERT phan_cong_coi_thi (20 rows)           │
│     ├─ INSERT phan_cong_giam_sat (10 rows)          │
│     └─ Transaction commit                            │
│                                                         │
│  9. ExcelUtil.writeDanhSachPhanCong()                │
│     └─ Xuất: DANHSACHPHANCONG_Ca_1.xlsx             │
│                                                         │
│ 10. ExcelUtil.writeDanhSachGiamSat()                 │
│     └─ Xuất: DANHSACHGIAMSAT_Ca_1.xlsx              │
│                                                         │
│ 11. FileTransferUtil.sendMultipleFiles()            │
│     └─ Gửi 2 file Excel về client                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
                        │
                        │ Network (2 files)
                        ▼
┌─────────────────────────────────────────────────────────┐
│                    CLIENT SIDE                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 12. Client nhận file                                  │
│     ├─ DANHSACHPHANCONG_Ca_1.xlsx                    │
│     ├─ DANHSACHGIAMSAT_Ca_1.xlsx                    │
│     └─ Lưu vào: client_downloads/                    │
│                                                         │
│ 13. Display success message                          │
│     └─ "Phân công thành công, Ca 1"                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Cấu trúc thư mục sau phân công

```
server_outputs/
├── ca_thi_20260519_083744_017/          (Session 1)
│   ├── DANHSACHPHANCONG_Ca_1.xlsx
│   └── DANHSACHGIAMSAT_Ca_1.xlsx
│
├── ca_thi_20260519_083939_673/          (Session 2)
│   ├── DANHSACHPHANCONG_Ca_2.xlsx
│   └── DANHSACHGIAMSAT_Ca_2.xlsx
│
└── ca_thi_20260519_084655_555/          (Session 3)
    ├── DANHSACHPHANCONG_Ca_3.xlsx
    └── DANHSACHGIAMSAT_Ca_3.xlsx

client_downloads/
├── DANHSACHPHANCONG_Ca_1.xlsx           (Download từ Server)
├── DANHSACHGIAMSAT_Ca_1.xlsx
├── DANHSACHPHANCONG_Ca_2.xlsx
├── DANHSACHGIAMSAT_Ca_2.xlsx
└── ...
```

---

## 🔑 Điểm quan trọng

### **1. Mã Ca Thi vs Tên Ca Thi**

| Mục      | Mã Ca Thi                    | Tên Ca Thi              |
| -------- | ---------------------------- | ----------------------- |
| Format   | `ca_thi_20260519_083744_017` | `Ca 1`                  |
| Tạo lúc  | Lúc bắt đầu phân công        | Sau khi lưu DB có ID    |
| Mục đích | Tạo session unique           | Hiển thị cho người dùng |
| Thay đổi | Không                        | Có thể cập nhật         |

### **2. Transaction Logic**

```java
connection.setAutoCommit(false);  // Bắt đầu transaction
try {
    createCaThi(...);                   // ✅ Success → lưu
    updateAutoTenCaThi(...);            // ✅ Success → lưu
    savePhanCongCoiThi(...);            // ✅ Success → lưu
    savePhanCongGiamSat(...);           // ✅ Success → lưu
    connection.commit();                // ✅ Commit tất cả
} catch (SQLException e) {
    connection.rollback();              // ❌ Rollback tất cả
    throw e;
}
```

**Ý nghĩa:** Nếu một trong bốn bước thất bại, toàn bộ 4 bước đều không được lưu.

### **3. Tải lịch sử khi nào?**

```
Lần phân công thứ 1:
└─ loadHistory() → empty (không có lịch sử cũ)

Lần phân công thứ 2:
├─ loadHistory() → Lịch sử từ lần 1
├─ Loại trừ cán bộ đã coi phòng lần 1
├─ Loại trừ cặp cán bộ từ lần 1
└─ Thuật toán phân công với constraints mới

Lần phân công thứ N:
└─ loadHistory() → Lịch sử từ lần 1, 2, ..., N-1 (tích lũy)
```

---

## 📊 Ví dụ thực tế

### **Trường hợp: Phân công lần đầu tiên**

```
Input:
├─ m = 6 cán bộ: GV001, GV002, GV003, GV004, GV005, GV006
├─ n = 2 phòng: P101, P102
└─ tenCaThi = "Ca thi buổi sáng"

Quy trình:
1. sessionName = "ca_thi_20260519_083744_017"
2. Lấy 6 cán bộ từ database
3. Lấy 2 phòng từ database
4. loadHistory() → rỗng (lần đầu)
5. phanCong(6 cán bộ, 2 phòng, empty history)
   ├─ Phân công P101: GV001, GV002
   ├─ Phân công P102: GV003, GV004
   └─ Giám sát: GV005 → P101, P102
                GV006 → (không có phòng)
6. INSERT INTO ca_thi → id = 1
7. UPDATE ca_thi SET ten_ca = "Ca 1"
8. INSERT phan_cong_coi_thi (4 rows)
9. INSERT phan_cong_giam_sat (1 row)
10. Xuất 2 file Excel
```

**Database sau:**
```
ca_thi:
├─ id=1, ma_ca_thi="ca_thi_20260519_083744_017", 
│  ten_ca="Ca 1", so_luong_can_bo=6, so_luong_phong_thi=2

phan_cong_coi_thi:
├─ (1, 1, "GV001", "Nguyễn Văn A", "GIAM_THI_1", "P101")
├─ (1, 2, "GV002", "Trần Thị B", "GIAM_THI_2", "P101")
├─ (1, 3, "GV003", "Lê Văn C", "GIAM_THI_1", "P102")
└─ (1, 4, "GV004", "Phạm Văn D", "GIAM_THI_2", "P102")

phan_cong_giam_sat:
└─ (1, 1, "GV005", "Hoàng Văn E", "P101, P102", "P101, P102")
```

---

## 📌 Tóm tắt

| Bước | Hành động            | Kết quả                        |
| ---- | -------------------- | ------------------------------ |
| 1    | Client gửi (m, n)    | ASSIGN_REQUEST                 |
| 2    | Tạo session name     | "ca_thi_YYYYMMDD_HHMMSS_SSS"   |
| 3    | Lấy dữ liệu DB       | List<CanBo>, List<PhongThi>    |
| 4    | Tải lịch sử          | AssignmentHistory (accumulate) |
| 5    | Thuật toán           | AssignmentResult (2 list)      |
| 6    | Lưu DB (transaction) | ca_thi.id = N                  |
| 7    | Cập nhật tên         | ten_ca = "Ca N"                |
| 8    | Lưu phân công        | phan_cong_coi_thi (2n rows)    |
| 9    | Lưu giám sát         | phan_cong_giam_sat (m-2n rows) |
| 10   | Xuất Excel           | 2 files                        |
| 11   | Gửi client           | SUCCESS + files                |

---

**Báo cáo được tạo ngày:** 19/05/2026  
**Hệ thống:** Phân công cán bộ coi thi - Lớp Lập trình mạng  

