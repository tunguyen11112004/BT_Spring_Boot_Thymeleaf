# Repo inventory

| Path | Vai trò |
| --- | --- |
| `pom.xml` | Spring Boot 3.5.6, Java 21, JAR, Web, Thymeleaf, JPA, Validation, Flyway, MySQL, H2 test |
| `src/main/java/org/fp/bt_qlsach/BtQlSachApplication.java` | Entry point, bật lịch cập nhật quá hạn |
| `src/main/java/org/fp/bt_qlsach/controller/` | Sách, độc giả, phiếu, trả, thu phí, chính sách, báo cáo |
| `src/main/java/org/fp/bt_qlsach/service/FineCalculationService.java` | Công thức phí |
| `src/main/resources/db/migration/V1__init.sql` | Schema và dữ liệu khởi tạo |
| `src/main/resources/templates/` | Giao diện Thymeleaf |
| `src/test/java/org/fp/bt_qlsach/service/` | Test phí, luồng mượn trả, thanh toán |
| `docs/BA/BA_Quan_ly_muon_tra_sach.md` | Use case và luật nghiệp vụ |
| `AGENTS.md`, `AI_RULES.md`, `.cursor/rules/` | Quy định vận hành agent |

Chưa có: `.codegraph/`, Docker, CI. Chạy `codegraph init` ở root repo nếu cần CodeGraph.
