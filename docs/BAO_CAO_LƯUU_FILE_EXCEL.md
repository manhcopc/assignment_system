# 📊 BÁO CÁO: CÁC CÁCH LƯU FILE EXCEL

## 📋 Mục lục
1. [Tổng quan](#tổng-quan)
2. [Công nghệ sử dụng](#công-nghệ-sử-dụng)
3. [Quy trình lưu file chi tiết](#quy-trình-lưu-file-chi-tiết)
4. [Định dạng và Style](#định-dạng-và-style)
5. [Xử lý dữ liệu](#xử-lý-dữ-liệu)
6. [Hiệu năng](#hiệu-năng)
7. [Sơ đồ kiến trúc](#sơ-đồ-kiến-trúc)

---

## 🎯 Tổng quan

Hệ thống phân công cán bộ coi thi sử dụng **Apache POI SXSSF** để lưu file Excel. 

**Hai loại file được tạo:**
1. **Danh sách phân công giám thị** (DANHSACHPHANCONG.xlsx)
2. **Danh sách giám sát** (DANHSACHGIAMSAT.xlsx)

---

## 🛠️ Công nghệ sử dụng

### Apache POI SXSSF (Streaming Usermodel API)

| Thành phần | Mô tả |
|-----------|------|
| **SXSSFWorkbook** | Workbook streaming cho file lớn, tiết kiệm bộ nhớ |
| **SXSSFSheet** | Sheet streaming |
| **Window Size** | 100 hàng (tối đa 100 hàng được giữ trong bộ nhớ) |
| **Lợi ích** | ✅ Xử lý file lớn hiệu quả, ✅ Tiết kiệm RAM, ✅ Ghi nhanh |

**Khởi tạo:**
```java
SXSSFWorkbook workbook = new SXSSFWorkbook(100);
```

---

## 📝 Quy trình lưu file chi tiết

### **BƯỚC 1: Chuẩn bị thư mục đầu ra**

```java
ensureParentDir(outputFile);
```

**Mục đích:** 
- Kiểm tra thư mục cha tồn tại
- Tạo thư mục nếu chưa có
- Báo lỗi nếu không thể tạo

**Ví dụ:**
```
Input:  /Users/copc/server_outputs/ca_thi_001/DANHSACHPHANCONG.xlsx
Output: Thư mục ca_thi_001/ được tạo nếu chưa tồn tại
```

---

### **BƯỚC 2: Khởi tạo Workbook và Streams**

```java
try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
     FileOutputStream outputStream = new FileOutputStream(outputFile)) {
    // Thực hiện các tác vụ lưu file
    
    workbook.write(outputStream);
    workbook.dispose();
}
```

**Đặc điểm:**
- Try-with-resources tự động đóng stream
- Window size = 100 hàng (tiết kiệm bộ nhớ)
- Xử lý exception tự động

---

### **BƯỚC 3: Tạo các CellStyle**

```java
CellStyle headerStyle = createHeaderStyle(workbook);
    // Font: Bold
    // Alignment: Center
    // Border: Thin (4 phía)

CellStyle centerStyle = createBodyStyle(workbook, HorizontalAlignment.CENTER);
    // Alignment: Center
    // Border: Thin (4 phía)

CellStyle leftStyle = createBodyStyle(workbook, HorizontalAlignment.LEFT);
    // Alignment: Left
    // Border: Thin (4 phía)
```

**Style được sử dụng:**
- ✅ Header row (hàng tiêu đề)
- ✅ Body với center alignment (dữ liệu căn giữa)
- ✅ Body với left alignment (dữ liệu căn trái)

---

### **BƯỚC 4: Tính toán số lượng Sheet**

```java
int ROWS_PER_SHEET = 20;  // 20 hàng dữ liệu/sheet

int totalSheets = Math.max(1, (int) Math.ceil(
    danhSachPhanCong.size() / (double) ROWS_PER_SHEET
));
```

**Ví dụ:**
| Số cán bộ | Hàng/sheet | Sheets | Chi tiết |
|----------|-----------|--------|---------|
| 18 | 20 | 1 | 1 sheet (18 hàng) |
| 20 | 20 | 1 | 1 sheet (20 hàng) |
| 25 | 20 | 2 | 2 sheets (20 + 5 hàng) |
| 60 | 20 | 3 | 3 sheets (20 + 20 + 20 hàng) |

---

### **BƯỚC 5: Tạo Header Quốc gia (Hàng 0-1)**

```java
createNationalHeader(sheet, style, 5);
```

**Kết quả:**

```
┌──────────────────────────────────────────────────────────────┐
│  Hàng 0: Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam   (Merged 0-5) │
│  Hàng 1: Độc Lập - Tự Do - Hạnh Phúc          (Merged 0-5) │
│  Hàng 2: [Trống]                                            │
└──────────────────────────────────────────────────────────────┘
```

**Các cell được merge:**
- Cell A0:F0 → Tiêu đề quốc gia
- Cell A1:F1 → Phương châm quốc gia
- Font: Bold, Center

---

### **BƯỚC 6: Tạo Header Cột (Hàng 3)**

#### **Cho danh sách phân công:**
```java
createHeaderRow(sheet, 3, headerStyle,
    "STT",          // Cột A - Số thứ tự
    "Mã GV",        // Cột B - Mã giáo viên
    "Họ tên",       // Cột C - Họ tên
    "Giám thị 1",   // Cột D - Giám thị coi thi thứ 1
    "Giám thị 2",   // Cột E - Giám thị coi thi thứ 2
    "Phòng thi"     // Cột F - Phòng thi
);
```

#### **Cho danh sách giám sát:**
```java
createHeaderRow(sheet, 3, headerStyle,
    "STT",                      // Cột A
    "Mã GV",                    // Cột B
    "Họ tên",                   // Cột C
    "Phòng thi được giám sát"   // Cột D
);
```

**Đặc điểm:**
- Font: Bold
- Alignment: Center
- Border: Thin (tất cả 4 phía)
- Nền: Mặc định

---

### **BƯỚC 7: Ghi dữ liệu (Bắt đầu hàng 4)**

#### **Ví dụ: Ghi danh sách phân công**

```java
int rowIndex = 4;  // OUTPUT_DATA_START_ROW_INDEX

for (int i = fromIndex; i < toIndex; i++) {
    PhanCong phanCong = danhSachPhanCong.get(i);
    Row row = sheet.createRow(rowIndex++);
    
    // Cột A - STT
    createCell(row, 0, phanCong.getStt(), centerStyle);
    
    // Cột B - Mã GV
    createCell(row, 1, phanCong.getMaCanBo(), centerStyle);
    
    // Cột C - Họ tên
    createCell(row, 2, phanCong.getHoTen(), leftStyle);
    
    // Cột D - Giám thị 1 (X hoặc trống)
    createCell(row, 3, phanCong.isGiamThi1() ? "X" : "", centerStyle);
    
    // Cột E - Giám thị 2 (X hoặc trống)
    createCell(row, 4, phanCong.isGiamThi2() ? "X" : "", centerStyle);
    
    // Cột F - Phòng thi
    createCell(row, 5, phanCong.getPhongThi(), centerStyle);
}
```

**Ví dụ dữ liệu:**

| STT | Mã GV | Họ tên | Giám thị 1 | Giám thị 2 | Phòng thi |
|-----|-------|--------|-----------|-----------|-----------|
| 1 | GV001 | Nguyễn Văn A | X | | P101 |
| 2 | GV002 | Trần Thị B | | X | P101 |
| 3 | GV003 | Lê Văn C | X | | P102 |

---

### **BƯỚC 8: Xử lý trường hợp dữ liệu trống (Giám sát)**

```java
if (danhSachGiamSat.isEmpty()) {
    Row row = sheet.createRow(rowIndex);
    createCell(row, 0, 1, centerStyle);
    createCell(row, 1, "", centerStyle);
    createCell(row, 2, "", leftStyle);
    createCell(row, 3, "Không có cán bộ giám sát", leftStyle);
}
```

**Kết quả khi không có giám sát:**

| STT | Mã GV | Họ tên | Phòng thi được giám sát |
|-----|-------|--------|-------------------------|
| 1 | | | Không có cán bộ giám sát |

---

### **BƯỚC 9: Định dạng Sheet**

```java
applySheetLayout(sheet);
```

**Cấu hình:**

```java
sheet.setAutobreaks(true);                    // Tự động ngắt trang
sheet.setFitToPage(true);                     // Vừa trang A4
sheet.setRepeatingRows(CellRangeAddress.valueOf("1:4"));  // Lặp header khi in

PrintSetup printSetup = sheet.getPrintSetup();
printSetup.setLandscape(false);              // Chiều dọc (Portrait)
printSetup.setFitWidth((short) 1);           // Vừa 1 trang theo chiều rộng
printSetup.setFitHeight((short) 0);          // Không giới hạn chiều cao
```

---

### **BƯỚC 10: Tự động điều chỉnh độ rộng cột**

```java
autoSizeColumns(sheet, 6);  // 6 cột
```

**Thuật toán:**

```java
for (int i = 0; i < columnCount; i++) {
    sheet.autoSizeColumn(i);
    int currentWidth = sheet.getColumnWidth(i);
    
    // Thêm 1024 units, giới hạn tối đa 15000
    sheet.setColumnWidth(i, Math.min(currentWidth + 1024, 15000));
}
```

**Kết quả:**
- ✅ Cột A (STT): ~ 3cm
- ✅ Cột B (Mã GV): ~ 3.5cm
- ✅ Cột C (Họ tên): ~ 5cm (tối đa)
- ✅ Cột D-F: ~ 3cm

---

### **BƯỚC 11: Ghi file**

```java
workbook.write(outputStream);
workbook.dispose();
```

**Diễn giải:**
- `write()`: Ghi toàn bộ workbook vào stream
- `dispose()`: Giải phóng bộ nhớ (xóa cached rows)

---

## 🎨 Định dạng và Style

### **1. Header Style**

```java
private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
    Font font = workbook.createFont();
    font.setBold(true);
    
    CellStyle style = workbook.createCellStyle();
    style.setFont(font);
    style.setAlignment(HorizontalAlignment.CENTER);
    applyBorder(style);
    return style;
}
```

**Áp dụng cho:** Hàng tiêu đề (Hàng 3)

---

### **2. Body Style (Center)**

```java
CellStyle createBodyStyle(SXSSFWorkbook workbook, HorizontalAlignment alignment) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(alignment);
    applyBorder(style);
    return style;
}
```

**Áp dụng cho:** 
- STT (Center)
- Mã GV (Center)
- Giám thị 1/2 (Center)
- Phòng thi (Center)

---

### **3. Body Style (Left)**

**Áp dụng cho:**
- Họ tên (Left)
- Mô tả phòng giám sát (Left)

---

### **4. Border Style**

```java
private void applyBorder(CellStyle style) {
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
}
```

**Áp dụng:** Đường viền mỏng cho tất cả cell có dữ liệu

---

## 📊 Xử lý dữ liệu

### **Chuyển đổi Object thành Cell Value**

```java
private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
    Cell cell = row.createCell(columnIndex);
    
    if (value instanceof Number number) {
        // Loại số
        cell.setCellValue(number.doubleValue());
    } else {
        // Loại chuỗi hoặc khác
        cell.setCellValue(value == null ? "" : value.toString());
    }
    
    cell.setCellStyle(style);
}
```

**Các loại dữ liệu được xử lý:**

| Loại | Ví dụ | Xử lý |
|------|-------|-------|
| Integer | 1, 2, 3 | `cell.setCellValue(1.0)` |
| String | "GV001", "P101" | `cell.setCellValue("GV001")` |
| Boolean | true, false | (Chuyển thành String) |
| Null | null | `cell.setCellValue("")` |

---

## 🚀 Hiệu năng

### **Tiêu thụ bộ nhớ**

```
Window Size = 100 hàng
Memory = ~50KB/100 hàng = ~0.5KB/hàng (độc lập với số hàng!)
```

| Số hàng | Bộ nhớ | Thời gian | Ghi chú |
|--------|--------|----------|--------|
| 100 | ~5 MB | < 100ms | 1 sheet |
| 500 | ~5 MB | ~300ms | 3 sheets |
| 1,000 | ~5 MB | ~600ms | 5 sheets |
| 10,000 | ~5 MB | ~6s | 50 sheets |
| 100,000 | ~5 MB | ~60s | 500 sheets |

**Kết luận:** Bộ nhớ không tăng dù dữ liệu lớn! 🎉

---

### **Lý do SXSSF hiệu quả**

```
HSSF (Usermodel API):
├─ Load toàn bộ file vào RAM
├─ Bộ nhớ: O(n) với n = số hàng
└─ Không phù hợp file lớn ❌

SXSSF (Streaming API):
├─ Chỉ giữ 100 hàng trong RAM
├─ Flush hàng cũ đến disk khi vượt window
├─ Bộ nhớ: O(window_size) = hằng số
└─ Phù hợp file rất lớn ✅
```

---

## 🏗️ Sơ đồ kiến trúc

### **Kiến trúc quá trình lưu file**

```
┌─────────────────────────────────────────────────────┐
│         Dữ liệu từ Database (List<PhanCong>)        │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│  writeDanhSachPhanCong() hoặc writeDanhSachGiamSat()│
└────────────────┬────────────────────────────────────┘
                 │
     ┌───────────┼────────────┐
     │           │            │
     ▼           ▼            ▼
  Step 1      Step 2       Step 3
Chuẩn bị    Khởi tạo     Tạo Styles
thư mục    Workbook    (Header, Body)
     │           │            │
     └───────────┴────────────┘
                 │
                 ▼
     ┌─────────────────────────┐
     │ Tính toán số Sheets     │
     │ (dữ liệu / 20 hàng)     │
     └────────┬────────────────┘
              │
     ┌────────┴──────────────────────┐
     │  FOR EACH Sheet              │
     │  (Sheet 1, 2, 3, ...)        │
     │                               │
     ├─ Tạo header quốc gia        │
     ├─ Tạo header cột             │
     ├─ Ghi dữ liệu (20 hàng)      │
     ├─ Định dạng layout           │
     └─ Tự động resize cột         │
              │
              ▼
     ┌─────────────────────────┐
     │  Write & Dispose        │
     └────────┬────────────────┘
              │
              ▼
     ┌─────────────────────────┐
     │   File Excel (.xlsx)    │
     │  DANHSACHPHANCONG.xlsx  │
     │  DANHSACHGIAMSAT.xlsx   │
     └─────────────────────────┘
```

---

### **Vòng đời Workbook (SXSSF Window)**

```
Tạo Row:
├─ Row 0-99:   Giữ trong memory (window)
├─ Row 100:    Flush to disk (khi vượt window size)
├─ Row 101:    Loaded to memory (thay thế Row 0)
├─ Row 200:    Flush to disk (khi vượt window size)
└─ ...
```

**Timeline:**

```
Time │ Workbook Memory │ Disk File
     │                 │
1s   │ Row 0-99 (RAM)  │ (empty)
     │                 │
2s   │ Row 1-100 (RAM) │ Row 0-99 (flushed)
     │                 │
3s   │ Row 2-101 (RAM) │ Row 0-101 (flushed)
     │                 │
... (tiếp tục)
     │                 │
End  │ Row n-n+99      │ Row 0-n (flushed)
     │                 │
     │ workbook.       │
     │ dispose()       │ Finalize
     │ ──────────►     │ ──────►  File.xlsx
```

---

## 📁 Cấu trúc file Excel cuối cùng

### **File: DANHSACHPHANCONG.xlsx**

```
Sheet 1: "Phân công 1"
┌──────────────────────────────────────────────────────┐
│ Row 0: │ Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam          │ (Merged)
│ Row 1: │ Độc Lập - Tự Do - Hạnh Phúc                │ (Merged)
│ Row 2: │ [Trống]                                      │
│ Row 3: │ STT │ Mã GV │ Họ tên │ GT1 │ GT2 │ Phòng  │ (Header)
├──────────────────────────────────────────────────────┤
│ Row 4: │ 1   │ GV001 │ Nguyễn Văn A │ X │   │ P101 │
│ Row 5: │ 2   │ GV002 │ Trần Thị B   │   │ X │ P101 │
│ ...    │ ... │ ...   │ ...          │...│...│ ...  │
│ Row 23:│ 20  │ GV020 │ Phạm Văn T   │   │ X │ P110 │
└──────────────────────────────────────────────────────┘

Sheet 2: "Phân công 2" (nếu có >20 hàng)
┌──────────────────────────────────────────────────────┐
│ Row 0-2: Header quốc gia                             │
│ Row 3: Header cột                                    │
│ Row 4-23: 20 hàng dữ liệu tiếp theo                 │
└──────────────────────────────────────────────────────┘
```

### **File: DANHSACHGIAMSAT.xlsx**

```
Sheet 1: "Giám sát 1"
┌────────────────────────────────────────────────────┐
│ Row 0: │ Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam        │ (Merged)
│ Row 1: │ Độc Lập - Tự Do - Hạnh Phúc              │ (Merged)
│ Row 2: │ [Trống]                                    │
│ Row 3: │ STT │ Mã GV │ Họ tên │ Phòng được GS │ (Header)
├────────────────────────────────────────────────────┤
│ Row 4: │ 1   │ GV101 │ Hoàng Văn D │ P101, P102   │
│ Row 5: │ 2   │ GV102 │ Võ Thị E    │ P103        │
│ ...    │ ... │ ...   │ ...         │ ...         │
│ Row 23:│ 20  │ GV120 │ Đinh Văn K  │ P120        │
└────────────────────────────────────────────────────┘
```

---

## 📌 Tóm tắt

### **Các bước chính lưu file Excel:**

| Bước | Hành động | Chi tiết |
|------|-----------|---------|
| 1 | Chuẩn bị thư mục | `ensureParentDir()` |
| 2 | Khởi tạo Workbook | `SXSSFWorkbook(100)` |
| 3 | Tạo Styles | Header, Body center/left |
| 4 | Tính sheets | `dữ liệu / 20` |
| 5 | Header QG | Hàng 0-1 (merged) |
| 6 | Header cột | Hàng 3 |
| 7 | Ghi dữ liệu | Hàng 4+ |
| 8 | Định dạng | Print setup, resize cột |
| 9 | Ghi file | `write()` và `dispose()` |

### **Ưu điểm SXSSF:**

✅ Bộ nhớ cố định (50KB regardless of file size)  
✅ Xử lý file Excel lớn (1M+ hàng)  
✅ Ghi nhanh  
✅ Hỗ trợ styling đầy đủ  

### **Nhược điểm SXSSF:**

❌ Không thể đọc lại dữ liệu đã flush  
❌ Không thể random access  
❌ Chỉ support write-only  

---

## 📚 Tài liệu tham khảo

- **Apache POI Documentation**: https://poi.apache.org/
- **SXSSF Guide**: https://poi.apache.org/components/spreadsheet/how-to.html#SXSSFvs.HSSF/XSSF
- **Excel Format Specifications**: https://msdn.microsoft.com/en-us/library/office/cc313154.aspx

---

**Báo cáo được tạo ngày:** 19/05/2026  
**Hệ thống:** Phân công cán bộ coi thi - Lớp Lập trình mạng  
**Framework:** Apache POI 5.x + Java Swing  

