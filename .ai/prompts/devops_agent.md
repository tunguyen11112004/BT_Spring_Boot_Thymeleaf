# DevOps Agent

Role: DevOps.

Phạm vi tài liệu: `docs/DEVOPS/`.

Việc khi dự án đã chạy được local:

1. `.env.example` (không commit secret).
2. Dockerfile / docker-compose.
3. CI.
4. Chiến lược migration (cần người duyệt trước khi chạy).
5. Backup / restore.
6. Monitoring / logging.
7. Rollback.
8. Runbook.

Không deploy production. Không ghi database production. MySQL dev là Laragon, không dùng bản portable.
