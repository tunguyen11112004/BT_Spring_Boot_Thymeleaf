# Agent Operating Rules

Dự án: `BT_QLSach` (Spring Boot 3.5.6, Java 21, Maven JAR, Thymeleaf, Spring Data JPA, Flyway, MySQL).
Đọc trước: `.ai/00_project_brief.md`, rồi summary module liên quan trong `.ai/03_module_summaries/`.
Nghiệp vụ: `docs/BA/BA_Quan_ly_muon_tra_sach.md`.

## Context rules

1. Không đọc toàn bộ repo nếu chưa cần.
2. Dùng CodeGraph để tìm symbol, flow, callers/callees, impact trước khi mở file khi repo đã có `.codegraph/`. `projectPath` là root repo này.
3. Khi sửa code, chỉ đọc file liên quan trực tiếp và file test liên quan.
4. Mọi đề xuất thay đổi phải nêu rõ:
   - Files affected
   - Reason
   - Risk
   - Tests required
   - Rollback plan

## Output rules

- BA: use case, actor, precondition, main flow, alternative flow, business rules. Thiếu dữ liệu thì ghi Open Questions, không suy diễn nghiệp vụ.
- SA: component, sequence, data flow, API, DB impact, NFR.
- Tester: test scenario, test data, expected result, priority, automation candidate.
- Dev: small PR plan, patch summary, test command.
- DevOps: env, build, deploy, monitoring, backup, rollback.

## Task rules

- Không sửa code khi task chưa rõ phạm vi.
- Mỗi thay đổi chỉ xử lý một use case hoặc một nhóm lỗi liên quan.
- Không refactor lan ra ngoài task.
- Công thức phí phạt chỉ nằm ở `FineCalculationService`. Không đưa công thức vào controller.

## Safety rules

- Không tự ý xóa file.
- Không commit secrets. Không đưa `.env` thật vào prompt.
- Không chạy migration production nếu chưa có người duyệt.
- Không push thẳng `main`/`master`. Không tự merge.
- Auth, thanh toán, miễn giảm phí, xóa dữ liệu bắt buộc có người duyệt trước khi sửa.
