# Project brief

- Tên: BT_QLSach
- Group: `org.fp`
- Package: `org.fp.bt_qlsach`
- Stack: Spring Boot 3.5.6, Java 21, Maven JAR, Thymeleaf, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway, MySQL, H2 cho test
- Mục tiêu: quản lý mượn trả sách, trả một phần, phí phạt, thu phí và miễn giảm
- Nghiệp vụ đã chốt: `docs/BA/BA_Quan_ly_muon_tra_sach.md`
- Database dev: MySQL Laragon, cổng 3306, user `root`, không mật khẩu, database `bt_qlsach`

## Package

- `controller`: màn hình
- `dto`: form và dữ liệu chuyển giữa các tầng
- `entity`: thực thể JPA
- `repository`: truy cập dữ liệu
- `service`: nghiệp vụ, gồm `FineCalculationService`
- `util`: nhãn hiển thị và `BusinessException`

## Open questions

Các điểm đề không chốt nằm ở mục 8 của tài liệu BA. Không bổ sung rule khi chưa có nguồn.
