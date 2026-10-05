# Architecture map

Luồng màn hình: Controller nhận form, gọi service, service đổi entity trong một transaction, repository ghi MySQL. Thymeleaf chỉ hiển thị.

Công thức phí không đi qua controller. `ReturnService` và `ReportService` gọi `FineCalculationService`.

Tồn kho đổi bằng câu lệnh có điều kiện trong `BookRepository`: trừ tồn chỉ khi số đang có còn đủ, cộng tồn chỉ khi không vượt tổng. `@Version` trên `Book` tăng cùng câu lệnh đó.

Trạng thái quá hạn được làm mới khi xem danh sách, khi lập báo cáo, và bởi lịch 00:05 giờ Việt Nam.
