# Module: ứng dụng mượn trả

Đọc use case đầy đủ ở `docs/BA/BA_Quan_ly_muon_tra_sach.md` trước khi sửa.

| Việc | Class |
| --- | --- |
| Tạo, hủy, tìm phiếu, làm mới quá hạn | `BorrowingService` |
| Xem trước và xác nhận trả | `ReturnService` |
| Tính phí | `FineCalculationService` |
| Thu phí | `PaymentService` |
| Miễn giảm | `WaiverService` |
| Chính sách đang áp dụng | `FinePolicyService` |
| Báo cáo quá hạn | `ReportService` |
| Sách, độc giả, thể loại | `BookService`, `MemberService`, `CategoryService` |

Test chính: `LibraryFlowTest`, `FineCalculationServiceTest`, `PaymentServiceTest`.
Lệnh: `.\mvnw.cmd test` với `JAVA_HOME` trỏ JDK 21.
