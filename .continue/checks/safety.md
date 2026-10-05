# Safety check

- Không có secret, mật khẩu, hoặc token trong diff.
- Không có `.env` thật.
- Không xóa file ngoài phạm vi task.
- Không có thay đổi auth, thanh toán, miễn giảm, hoặc xóa dữ liệu mà thiếu ghi chú cần người duyệt.
- Có lệnh test hoặc lý do chưa chạy được test.
