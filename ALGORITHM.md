# �� Thuật Toán Phân Công Cán Bộ Coi Thi - Backtracking + Retry

## 📖 Tổng Quan

Hệ thống sử dụng **Thuật toán Backtracking với cơ chế Retry (Thử lại)**. Khi không tìm được phương án phân công hợp lệ ở lần thử hiện tại, thuật toán sẽ:
1. **Rollback** toàn bộ phân công của ca hiện tại
2. **Xáo trộn** lại thứ tự cán bộ và phòng thi
3. **Thử lại** với trạng thái mới (tối đa 100 lần)

## 🏗️ Cấu Trúc Chính

### Giai Đoạn 1: Phân Công Giám Thị Coi Thi (2 người/phòng)

**Ràng buộc:**
- ✅ Mỗi phòng thi phải có **đúng 2 giám thị**
- ✅ Một cán bộ **không được phân lại cùng phòng** ở các ca khác
- ✅ Một cặp cán bộ **không được làm giám thị chung** nếu đã làm trước đó

**Quy trình (Backtracking):**
```
for mỗi phòng thi (từ phòng 0 đến phòng n-1):
    for mỗi cán bộ i có thể làm giám thị 1:
        if (cán bộ i không thỏa ràng buộc):
            continue
        
        thêm i vào danh sách cán bộ dùng trong ca
        
        for mỗi cán bộ j có thể làm giám thị 2:
            if (cán bộ j không thỏa ràng buộc):
                continue
            if (cặp (i,j) đã ghép trước đó):
                continue
            
            thêm j vào danh sách cán bộ dùng trong ca
            thêm phân công (phòng, i, j) vào danh sách
            
            if (phân công hết tất cả phòng):
                return TRUE  // Thành công!
            
            // Backtrack nếu không thành công
            xóa phân công (phòng, i, j)
            xóa j khỏi danh sách cán bộ dùng
        
        xóa i khỏi danh sách cán bộ dùng
    
    return FALSE  // Không tìm được phương án
```

### Giai Đoạn 2: Phân Công Giám Sát

**Trường hợp A:** Số giám sát ≤ Số phòng thi
- Mỗi giám sát được phân cho 1 phòng

**Trường hợp B:** Số giám sát > Số phòng thi  
- Các giám sát được phân vòng tròn: `phòng = (index) % số phòng`

**Ràng buộc:**
- ✅ Cán bộ **không được giám sát phòng mà đã coi thi** trước đó
- ✅ Cán bộ **không được giám sát cùng phòng 2 lần** ở các ca khác

### Giai Đoạn 3: Cơ Chế Retry (Thử Lại)

**Khi không tìm được phương án:**
```
for attempt = 0 to MAX_RETRY (100 lần):
    try:
        // Nếu đây là lần thử lại (attempt > 0):
        if (attempt > 0):
            xáo trộn lại danh sách cán bộ
            xáo trộn lại danh sách phòng thi
            reset constraint
        
        // Phân công từng ca thi
        for mỗi ca thi:
            // Giai đoạn 1: Phân công giám thị (backtracking)
            if (!phân công giám thị cho ca này):
                throw exception
            
            // Giai đoạn 2: Phân công giám sát
            ...
        
        return kết quả thành công
    
    catch exception:
        if (attempt == MAX_RETRY):
            throw "Không tìm được phương án sau 100 lần thử"
        // Tiếp tục thử lại
```

## 📊 Phức Tạp Tính Toán

| Yếu Tố | Độ Phức Tạp |
|--------|------------|
| **Backtracking (1 ca)** | O(n²) nơi n = số cán bộ |
| **Nhiều ca thi** | O(soCaThi × n²) |
| **Retry (tệ nhất)** | O(100 × soCaThi × n²) |
| **Retry (bình thường)** | O(n log n) do xáo trộn |
| **Không gian** | O(n) cho các danh sách ràng buộc |

## 🔑 Các Lớp Chính

### 1. **AssignmentConstraint**
Quản lý tất cả các ràng buộc (constraints):
- `phongDaCoi`: Map<maGV, Set<tenPhong>> - phòng đã coi
- `capDaGhep`: Set<"maGV1|maGV2"> - cặp đã ghép
- `giamSatPhong`: Map<maGV, Set<tenPhong>> - giám sát per phòng

**Các phương thức chính:**
```java
// Kiểm tra ràng buộc
coTheCoiPhong(canBo, phongThi, canBoDaDungTrongCa)
coTheGhepCap(canBo1, canBo2)
coTheGiamSatPhong(canBo, phongThi)

// Ghi nhận ràng buộc
ghiNhanPhongDaCoi(canBo, phongThi)
ghiNhanCapDaGhep(canBo1, canBo2)
ghiNhanGiamSatPhong(canBo, phongThi)

// Rollback (khôi phục lại)
rollbackPhongDaCoi(canBo, phongThi)
rollbackCapDaGhep(canBo1, canBo2)
rollbackGiamSatPhong(canBo, phongThi)
```

### 2. **BacktrackingAssignmentEngine**
Engine chính thực hiện thuật toán backtracking + retry:

```java
PhanCongResult phanCongNhieuCa(
    List<CanBo> canBoList,
    List<PhongThi> phongThiList,
    int soPhongThi,
    int soGiamThi,
    int soCaThi
)
```

**Kết quả trả về:**
```java
class PhanCongResult {
    List<PhanCong> danhSachPhanCong;   // Danh sách phân công giám thị
    List<GiamSat> danhSachGiamSat;     // Danh sách giám sát
    int retryCount;                      // Số lần retry (0 = lần đầu thành công)
}
```

### 3. **AssignmentService**
Dịch vụ tích hợp gọi `BacktrackingAssignmentEngine`:
```java
AssignmentResult phanCongNhieuCa(
    List<CanBo> canBoList,
    List<PhongThi> phongThiList,
    int soPhongThi,
    int soGiamThi,
    int soCaThi
)
```

## 💡 Ví Dụ Minh Họa

**Input:**
- 6 cán bộ: GV1, GV2, GV3, GV4, GV5, GV6
- 2 phòng thi: P101, P102
- 1 ca thi

**Quy trình:**

### Ca 1:
```
Phân công giám thị:
- P101: GV1 + GV2
- P102: GV3 + GV4

Constraint sau ca 1:
  phongDaCoi = {
    GV1 -> {P101},
    GV2 -> {P101},
    GV3 -> {P102},
    GV4 -> {P102}
  }
  capDaGhep = {GV1|GV2, GV3|GV4}

Phân công giám sát:
- GV5 giám sát P101
- GV6 giám sát P102

Constraint sau ca 1:
  giamSatPhong = {
    GV5 -> {P101},
    GV6 -> {P102}
  }
```

### Ca 2 (nếu có):
```
Backtracking sẽ tìm:
- P101: KHÔNG GV1, KHÔNG GV2 (đã coi)
        Có thể: GV3, GV4, GV5, GV6
        Thử GV3 + GV5 ✓ (không ghép cùng ai trước)
- P102: KHÔNG GV3, KHÔNG GV4, KHÔNG GV5 (đã coi)
        KHÔNG GV3|GV4 (cặp cũ)
        Có thể: GV1, GV2, GV6
        Thử GV1 + GV2 ✓ (không cùng ca)
        Nhưng GV1|GV2 là cặp cũ ✗
        Thử GV1 + GV6 ✓ (chưa ghép)

Phân công giám thị ca 2:
- P101: GV3 + GV5
- P102: GV1 + GV6

Phân công giám sát ca 2:
- GV2 giám sát ? (KHÔNG P101 - đã coi, KHÔNG P102 - đã coi)
  → Không thể! Trigger RETRY

Retry lần 1:
- Xáo trộn: [GV4, GV1, GV6, GV3, GV2, GV5]
- Xáo trộn phòng: [P102, P101]
- Thử phân công lại...
```

## ✅ Ưu Điểm

1. **Đảm bảo tìm được phương án** nếu tồn tại
2. **Tránh phân công lặp lại** nhờ các ràng buộc
3. **Hỗ trợ hạn chế giám sát per phòng** qua constraint
4. **Dễ hiểu và bảo trì** - code rõ ràng từng giai đoạn
5. **Retry tự động** - xáo trộn giúp tìm phương án mới
6. **Rollback toàn bộ** khi gặp vấn đề - đảm bảo tính nhất quán

## ❌ Nhược Điểm

1. **Có thể mất thời gian** nếu max_retry nhỏ hoặc bài toán khó
2. **Không tối ưu về "công bằng"** - chỉ tìm một phương án hợp lệ
3. **Không có heuristic tìm kiếm** (như Greedy, Min-cost matching)
4. **O(n²) cho backtracking** - có thể chậm với cán bộ rất nhiều

## 🚀 Cách Sử Dụng

```java
// Cách 1: Sử dụng AssignmentService (được khuyên)
AssignmentService service = new AssignmentService();
AssignmentResult result = service.phanCongNhieuCa(
    canBoList, phongThiList, 
    soPhongThi, soGiamThi, soCaThi
);

// Cách 2: Tuỳ chỉnh số lần retry
AssignmentService service = new AssignmentService(50);  // Max 50 lần retry
AssignmentResult result = service.phanCongNhieuCa(...);

// Cách 3: Sử dụng engine trực tiếp
BacktrackingAssignmentEngine engine = new BacktrackingAssignmentEngine(100);
BacktrackingAssignmentEngine.PhanCongResult result = engine.phanCongNhieuCa(...);
System.out.println("Retry count: " + result.retryCount);  // Xem số lần thử
```

## 📝 Lưu Ý Quan Trọng

1. **Validator input** xảy ra trong `AssignmentService.validateInput()`
2. **Ràng buộc được reset** ở mỗi lần retry để tìm phương án khác
3. **Xáo trộn** giúp tránh stuck ở cùng một thứ tự cán bộ
4. **Rollback** chỉ xảy ra khi không tìm được phương án trong lần thử hiện tại
5. **Max retry = 100** là giá trị mặc định, có thể tuỳ chỉnh

