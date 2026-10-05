# AI Rules

Quy định này áp dụng cho mọi agent làm việc trên repo. Chi tiết vận hành nằm ở `AGENTS.md`. Prompt theo vai trò nằm ở `.ai/prompts/`. Tài liệu nghiệp vụ nằm ở `docs/BA/`.

## Lấy context

Chỉ đưa context theo thứ tự:

1. Project brief (`.ai/00_project_brief.md`)
2. Module summary
3. Use case trong `docs/BA/BA_Quan_ly_muon_tra_sach.md`
4. Symbol từ CodeGraph, nếu repo đã index
5. Đoạn file cần sửa
6. Git diff

Không đưa: `target/`, file sinh tự động, log dài, binary, toàn bộ repo mỗi lần hỏi.

Thứ tự khi sửa code:

1. CodeGraph tìm symbol và flow khi đã có index
2. CodeGraph impact trước khi sửa
3. Chỉ mở file cần thiết
4. Sau khi sửa, chọn test liên quan

## Prompt contract

Mỗi task gồm 6 phần: Role, Task, Context, Constraints, Output format, Validation checklist.

## An toàn

1. Chỉ đọc/ghi trong thư mục repo.
2. Database dev là MySQL Laragon, user `root`, không mật khẩu, database `bt_qlsach`. Không ghi production.
3. Không đưa `.env` thật vào prompt.
4. Không deploy production.
5. Không tự merge PR.
6. Không cài MCP không rõ nguồn.
7. Không dùng shell với credential production.
8. Migration cần người duyệt.
9. Mọi thay đổi đi qua review.
10. Log prompt/output không chứa dữ liệu nhạy cảm.

## Kiểm soát phạm vi

- Hỏi nhanh một function: CodeGraph trước nếu có index, không mở rộng sang module khác.
- Phân tích một module: dùng summary và use case liên quan, không dán source cả module.
- Sinh test: theo từng use case, dùng template trong `.ai/prompts/tester_agent.md`.
- Sửa bug: chỉ diff và file liên quan.
- Refactor lớn: tách thành thay đổi nhỏ.

Ghi chi phí khi có số liệu thật vào `.ai/token_ledger.md`. Không bịa token hoặc chi phí.

## Công cụ local

- CodeGraph, Serena, Context7: MCP trong Cursor. Cần reload MCP sau khi sửa `mcp.json`. Repo này chưa có `.codegraph/` cho đến khi chạy `codegraph init`.
- Ollama: `http://127.0.0.1:11434`, model đã kéo `qwen2.5-coder:3b`.
- LiteLLM chỉ lắng nghe máy này:

```
litellm --config .ai/litellm_config.yaml --host 127.0.0.1 --port 4000
```

Alias model: `local-coder`. Không ghi API key vào repo. Context7 chạy được không cần key; key chỉ để tăng hạn mức.

## Chạy ứng dụng

JDK 21: `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`. `JAVA_HOME` phải trỏ JDK này vì `java` trên PATH có thể là 17.

MySQL là Laragon, cổng 3306. Không khởi động MySQL portable.

```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
