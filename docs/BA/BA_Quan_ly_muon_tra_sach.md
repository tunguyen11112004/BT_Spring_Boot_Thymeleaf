# Tài liệu phân tích nghiệp vụ

Hệ thống quản lý mượn trả sách và phí phạt thư viện nội bộ.

Nguồn: đề bài Spring Boot MVC nâng cao và hành vi đang chạy trong repo `BT_QLSach`.
Phạm vi: các chức năng đã có trên giao diện. Việc đề không mô tả được ghi ở Open Questions, không biến thành luật.

## 1. Tổng quan

Thủ thư theo dõi sách, độc giả, phiếu mượn, trả một phần hoặc trả hết, phí phạt quá hạn, thu phí và miễn giảm. Một phiếu thuộc một độc giả và có nhiều đầu sách. Tồn kho, phí và trạng thái phiếu phải đổi trong cùng một giao dịch: một dòng không hợp lệ thì cả lần ghi không được lưu một phần.

Ứng dụng là web server-side. Thủ thư thao tác trên màn hình Thymeleaf. Không có kênh tự phục vụ cho độc giả và không có đăng nhập.

Các nhóm chức năng:

- Quản lý thể loại ở mức đủ để tạo sách.
- Quản lý sách và độc giả.
- Lập phiếu mượn, xem, lọc và hủy phiếu.
- Trả sách, xem trước phí rồi xác nhận.
- Thu phí và miễn giảm.
- Chính sách phí phạt, chỉ một chính sách đang áp dụng.
- Báo cáo sách đang quá hạn.

## 2. Actor

| Actor | Mô tả | Vai trò trong hệ thống |
| --- | --- | --- |
| Thủ thư | Người vận hành thư viện nội bộ. | Thực hiện mọi màn hình: sách, độc giả, phiếu, trả, thu phí, miễn giảm, chính sách phí, báo cáo. |
| Độc giả | Người được cấp mã để mượn sách. | Là dữ liệu được quản lý. Không tự đăng nhập hay tự lập phiếu. |

## 3. Danh sách use case

| Mã | Tên | Actor |
| --- | --- | --- |
| UC-CAT-01 | Tạo thể loại | Thủ thư |
| UC-BOOK-01 | Tìm và xem danh sách sách | Thủ thư |
| UC-BOOK-02 | Tạo sách | Thủ thư |
| UC-BOOK-03 | Cập nhật sách | Thủ thư |
| UC-MEM-01 | Xem danh sách độc giả | Thủ thư |
| UC-MEM-02 | Tạo độc giả | Thủ thư |
| UC-MEM-03 | Cập nhật độc giả | Thủ thư |
| UC-BOR-01 | Xem và lọc phiếu mượn | Thủ thư |
| UC-BOR-02 | Tạo phiếu mượn | Thủ thư |
| UC-BOR-03 | Xem chi tiết phiếu | Thủ thư |
| UC-BOR-04 | Hủy phiếu | Thủ thư |
| UC-RET-01 | Xem trước phí trả | Thủ thư |
| UC-RET-02 | Xác nhận trả một phần hoặc trả hết | Thủ thư |
| UC-PAY-01 | Thu phí phạt | Thủ thư |
| UC-WAV-01 | Miễn giảm phí | Thủ thư |
| UC-POL-01 | Xem chính sách phí | Thủ thư |
| UC-POL-02 | Ban hành chính sách phí mới | Thủ thư |
| UC-RPT-01 | Xem báo cáo quá hạn | Thủ thư |

## 4. Chi tiết use case

### UC-CAT-01 — Tạo thể loại

Actor: Thủ thư

Tiền điều kiện: Thủ thư đang ở form tạo sách.

Luồng chính:

1. Thủ thư nhập tên thể loại trên form sách và lưu.
2. Hệ thống từ chối tên trống hoặc tên đã tồn tại.
3. Hệ thống lưu thể loại và quay lại form tạo sách.

Luồng thay thế:

- Không có màn danh sách, sửa hoặc ngừng dùng thể loại.

Luật: BR-CAT-01

### UC-BOOK-01 — Tìm và xem danh sách sách

Actor: Thủ thư

Tiền điều kiện: Hệ thống đang chạy.

Luồng chính:

1. Thủ thư mở danh sách sách.
2. Có thể nhập từ khóa theo tên, tác giả hoặc ISBN.
3. Hệ thống hiện tên, tác giả, ISBN, thể loại, số còn và tổng số lượng, có phân trang.

Luồng thay thế:

- Không có sách khớp từ khóa: danh sách trống.

Luật: BR-PAGE-01

### UC-BOOK-02 — Tạo sách

Actor: Thủ thư

Tiền điều kiện: Có ít nhất một thể loại.

Luồng chính:

1. Thủ thư nhập ISBN, tên, tác giả, thể loại và tổng số lượng.
2. Hệ thống kiểm tra dữ liệu.
3. Khi lưu thành công, số đang có bằng tổng số lượng.

Luồng thay thế:

- Tổng số lượng không lớn hơn 0, hoặc ISBN đã tồn tại: không lưu.

Luật: BR-BOOK-01, BR-BOOK-02, BR-ERR-01

### UC-BOOK-03 — Cập nhật sách

Actor: Thủ thư

Tiền điều kiện: Sách đã tồn tại.

Luồng chính:

1. Thủ thư sửa thông tin và lưu.
2. Nếu đổi tổng số lượng, số đang có được điều chỉnh theo số bản vẫn đang cho mượn.

Luồng thay thế:

- Tổng mới nhỏ hơn số bản đang cho mượn: không lưu.
- ISBN trùng sách khác: không lưu.

Luật: BR-BOOK-01, BR-BOOK-03

### UC-MEM-01 — Xem danh sách độc giả

Actor: Thủ thư

Luồng chính:

1. Thủ thư mở danh sách độc giả.
2. Hệ thống hiện mã, họ tên, email và điện thoại.

Luật: BR-PAGE-01

### UC-MEM-02 — Tạo độc giả

Actor: Thủ thư

Luồng chính:

1. Thủ thư nhập mã độc giả, họ tên, email và điện thoại.
2. Hệ thống kiểm tra mã chưa trùng và họ tên không rỗng.
3. Hệ thống lưu và quay về danh sách.

Luồng thay thế:

- Mã đã tồn tại: không lưu.

Luật: BR-MEM-01, BR-ERR-01

### UC-MEM-03 — Cập nhật độc giả

Actor: Thủ thư

Tiền điều kiện: Độc giả đã tồn tại.

Luồng chính:

1. Thủ thư sửa thông tin và lưu.
2. Mã mới không được trùng độc giả khác.

Luật: BR-MEM-01

### UC-BOR-01 — Xem và lọc phiếu mượn

Actor: Thủ thư

Luồng chính:

1. Thủ thư mở danh sách phiếu.
2. Có thể lọc theo độc giả, trạng thái, khoảng ngày mượn, hoặc chỉ phiếu quá hạn.
3. Mỗi dòng có mã, độc giả, ngày mượn, hạn trả, trạng thái và đường vào chi tiết.
4. Phiếu còn sách chưa trả mà đã qua hạn được làm mới sang trạng thái quá hạn khi mở danh sách.

Luồng thay thế:

- Chưa có phiếu khớp bộ lọc: danh sách trống.

Luật: BR-STA-01, BR-PAGE-01

### UC-BOR-02 — Tạo phiếu mượn

Actor: Thủ thư

Tiền điều kiện:

- Độc giả không còn phí chưa thu và không có phiếu quá hạn.
- Có sách còn tồn.

Luồng chính:

1. Thủ thư chọn độc giả, ngày mượn, hạn trả và ít nhất một đầu sách cùng số lượng.
2. Form gợi ý hạn trả bằng ngày mượn cộng 14 ngày. Thủ thư được sửa hạn trả.
3. Hệ thống kiểm tra hạn trả không trước ngày mượn, không trùng đầu sách, số lượng lớn hơn 0, và tổng cuốn đang chưa trả của độc giả sau phiếu này không quá 5.
4. Khi lưu thành công, số đang có của từng sách bị trừ đúng số lượng trên phiếu. Phiếu ở trạng thái đang mượn, phí ban đầu bằng 0.

Luồng thay thế:

- Thiếu ngày, hạn trả trước ngày mượn, không có sách, số lượng không lớn hơn 0, hoặc hai dòng cùng một sách: không tạo phiếu.
- Độc giả còn phí chưa thu hoặc đang có phiếu quá hạn: không tạo phiếu.
- Vượt 5 cuốn đang chưa trả: không tạo phiếu.
- Một đầu sách không đủ tồn: không tạo phiếu và không trừ tồn của các sách khác trong cùng phiếu.

Luật: BR-BOR-01, BR-BOR-02, BR-BOR-03, BR-BOR-04, BR-BOR-05, BR-TX-01

### UC-BOR-03 — Xem chi tiết phiếu

Actor: Thủ thư

Luồng chính:

1. Thủ thư mở một phiếu.
2. Hệ thống hiện độc giả, ngày, trạng thái, từng dòng sách, phí đã ghi, đã thu, đã miễn và còn phải thu.
3. Nếu phiếu còn trả một phần nhưng trạng thái là quá hạn, giao diện vẫn gắn nhãn đã trả một phần.

Luật: BR-STA-01, BR-STA-02

### UC-BOR-04 — Hủy phiếu

Actor: Thủ thư

Tiền điều kiện: Phiếu chưa trả cuốn nào và chưa phát sinh phí, đã thu hoặc đã miễn.

Luồng chính:

1. Thủ thư chọn hủy trên chi tiết phiếu.
2. Hệ thống chuyển phiếu sang đã hủy và cộng lại tồn kho đúng số lượng đã trừ.

Luồng thay thế:

- Đã trả một phần, đã trả hết, còn nợ phí, đã thu hoặc đã miễn: không hủy.
- Không hoàn được tồn: phiếu không bị hủy.

Luật: BR-BOR-06, BR-TX-01

### UC-RET-01 — Xem trước phí trả

Actor: Thủ thư

Tiền điều kiện: Phiếu còn mở: đang mượn, trả một phần, hoặc quá hạn.

Luồng chính:

1. Thủ thư mở màn trả, nhập số lượng trả và ngày trả của từng dòng còn nợ.
2. Chọn xem phí.
3. Hệ thống tính phí sẽ cộng thêm theo chính sách đang áp dụng, chưa ghi phiếu và chưa cộng tồn.

Luật: BR-FINE-01, BR-FINE-02

### UC-RET-02 — Xác nhận trả một phần hoặc trả hết

Actor: Thủ thư

Tiền điều kiện: Phiếu còn mở. Dòng được trả còn số lượng chưa trả.

Luồng chính:

1. Thủ thư nhập số lượng và ngày trả rồi xác nhận.
2. Hệ thống từ chối số lượng âm hoặc lớn hơn phần còn nợ.
3. Phí của lần này chỉ tính trên số lượng trả trong lần này. Trả đúng hạn hoặc trong số ngày miễn phạt thì phí lần này bằng 0.
4. Số đã trả, ngày trả gần nhất và phí của dòng được cộng. Tồn kho được cộng lại đúng số vừa trả.
5. Nếu vẫn còn sách chưa trả và đã quá hạn, trạng thái là quá hạn. Nếu chưa quá hạn mà đã trả một phần, trạng thái là trả một phần.
6. Nếu trả hết sách và không còn phí phải thu, trạng thái là đã trả. Nếu còn phí phải thu, trạng thái là còn nợ phí. Nếu phí đã được thu hoặc miễn hết, trạng thái là đã thanh toán phí.

Luồng thay thế:

- Phiếu đã trả hết, còn nợ phí sau khi trả hết, đã thanh toán phí, hoặc đã hủy: không trả thêm.
- Không nhập số lượng cho dòng nào: không ghi nhận.
- Một dòng không hợp lệ hoặc không cộng được tồn: cả lần trả được hoàn tác, phí và số đã trả không đổi.

Luật: BR-RET-01, BR-RET-02, BR-FINE-01, BR-FINE-02, BR-STA-01, BR-TX-01

### UC-PAY-01 — Thu phí phạt

Actor: Thủ thư

Tiền điều kiện: Phiếu chưa hủy và còn số phí phải thu lớn hơn 0.

Luồng chính:

1. Thủ thư nhập số tiền lớn hơn 0, chọn tiền mặt hoặc chuyển khoản, có thể ghi chú.
2. Hệ thống ghi một lần thu và trừ vào số còn phải thu.
3. Được thu nhiều lần.
4. Khi đã trả hết sách và số còn phải thu về 0 sau một lần thu, phiếu chuyển sang đã thanh toán phí.

Luồng thay thế:

- Số tiền không lớn hơn 0, thiếu phương thức, phiếu đã hủy, không còn phí, hoặc số tiền lớn hơn số còn phải thu: không ghi lần thu.

Luật: BR-PAY-01, BR-PAY-02, BR-TX-01

### UC-WAV-01 — Miễn giảm phí

Actor: Thủ thư

Tiền điều kiện: Phiếu chưa hủy và còn số phí phải thu lớn hơn 0.

Luồng chính:

1. Thủ thư nhập số tiền, lý do và người duyệt.
2. Hệ thống ghi một lần miễn giảm và trừ vào số còn phải thu.
3. Khi đã trả hết sách và không còn phí phải thu, phiếu chuyển sang đã thanh toán phí.

Luồng thay thế:

- Thiếu lý do hoặc người duyệt, số tiền không lớn hơn 0, hoặc vượt số còn phải thu: không ghi.

Luật: BR-WAV-01, BR-TX-01

### UC-POL-01 — Xem chính sách phí

Actor: Thủ thư

Luồng chính:

1. Thủ thư mở danh sách chính sách.
2. Hệ thống hiện phí mỗi ngày, số ngày miễn phạt, trần phí và chính sách nào đang áp dụng.

### UC-POL-02 — Ban hành chính sách phí mới

Actor: Thủ thư

Luồng chính:

1. Thủ thư nhập phí mỗi ngày, số ngày miễn phạt và trần phí. Trần có thể bỏ trống.
2. Hệ thống tắt mọi chính sách cũ và bật chính sách vừa tạo.
3. Lần trả sau dùng chính sách đang bật. Phí đã ghi trên dòng cũ không bị viết giảm.

Luồng thay thế:

- Phí mỗi ngày không lớn hơn 0, số ngày miễn phạt âm, hoặc trần âm: không lưu.

Luật: BR-POL-01, BR-FINE-02

### UC-RPT-01 — Xem báo cáo quá hạn

Actor: Thủ thư

Luồng chính:

1. Thủ thư mở báo cáo quá hạn.
2. Hệ thống làm mới trạng thái phiếu còn mở rồi liệt kê từng đầu sách còn chưa trả của phiếu quá hạn.
3. Mỗi dòng có phiếu, độc giả, hạn trả, số ngày trễ, số ngày tính phí, số cuốn còn nợ và phí dự kiến nếu trả hết phần còn lại trong ngày xem.

Luồng thay thế:

- Không có phiếu quá hạn: danh sách trống.
- Chưa có chính sách đang áp dụng: không lập được báo cáo.

Luật: BR-RPT-01, BR-FINE-01

## 5. Danh mục luật nghiệp vụ

| Mã | Luật |
| --- | --- |
| BR-CAT-01 | Tên thể loại không được rỗng và không được trùng. |
| BR-BOOK-01 | Sách có ISBN, tên, tác giả, thể loại, tổng số lượng và số đang có. ISBN không được trùng. |
| BR-BOOK-02 | Khi tạo sách, số đang có bằng tổng số lượng. Tổng số lượng phải lớn hơn 0. |
| BR-BOOK-03 | Khi sửa, tổng mới không được nhỏ hơn số bản đang cho mượn. Số đang có bằng tổng mới trừ số đang cho mượn. |
| BR-MEM-01 | Mã độc giả không được trùng. Họ tên không được rỗng. |
| BR-BOR-01 | Một phiếu thuộc một độc giả, có ngày mượn, hạn trả và ít nhất một đầu sách. Hạn trả không được trước ngày mượn. |
| BR-BOR-02 | Không có hai dòng trùng một đầu sách. Số lượng mỗi dòng phải lớn hơn 0. |
| BR-BOR-03 | Mỗi độc giả chỉ được giữ tối đa 5 cuốn đang chưa trả, tính cả phiếu mới. |
| BR-BOR-04 | Không tạo phiếu mới nếu độc giả còn phí chưa thu hoặc đang có phiếu quá hạn. |
| BR-BOR-05 | Tạo phiếu thành công thì trừ số đang có đúng số lượng trên phiếu. Hết tồn ở một dòng thì cả phiếu không được lưu. |
| BR-BOR-06 | Chỉ hủy phiếu chưa trả cuốn nào và chưa phát sinh, đã thu hoặc đã miễn phí. Hủy thì cộng lại tồn. |
| BR-RET-01 | Chỉ trả phiếu đang mượn, trả một phần hoặc quá hạn. Không trả phiếu đã trả, còn nợ phí sau khi trả hết, đã thanh toán phí, hoặc đã hủy. |
| BR-RET-02 | Số lượng trả không được âm và không được lớn hơn số còn đang mượn của dòng đó. |
| BR-FINE-01 | Số ngày trễ thô = lớn hơn hoặc bằng 0 của ngày trả trừ hạn trả. Số ngày tính phí = lớn hơn hoặc bằng 0 của số ngày trễ thô trừ số ngày miễn phạt. Phí lần trả = số ngày tính phí × số lượng trả lần này × phí mỗi ngày của chính sách đang bật. Trả đúng ngày hạn là đúng hạn. |
| BR-FINE-02 | Trần phí áp trên tổng phí của một dòng. Nếu phí đã ghi của dòng đã đạt hoặc vượt trần, lần trả sau không cộng thêm và không viết giảm phí cũ. |
| BR-STA-01 | Còn sách chưa trả: quá hạn nếu hạn trả trước ngày hiện tại, nếu không thì trả một phần khi đã trả ít nhất một cuốn, còn lại là đang mượn. Đã trả hết: còn nợ phí nếu còn phải thu, đã thanh toán phí nếu đã có phí hoặc đã thu hoặc đã miễn, còn lại là đã trả. Phiếu đã hủy giữ nguyên trạng thái. |
| BR-STA-02 | Khi phiếu quá hạn mà vẫn còn sách đã trả một phần, nhãn phụ vẫn báo đã trả một phần. Quá hạn được ưu tiên để lọc và để chặn mượn mới. |
| BR-PAY-01 | Số tiền thu phải lớn hơn 0, không vượt số còn phải thu, và phải có phương thức tiền mặt hoặc chuyển khoản. Được thu nhiều lần. Không thu phiếu đã hủy. |
| BR-PAY-02 | Số còn phải thu = tổng phí − đã thu − đã miễn. Khi đã trả hết sách và số còn phải thu về 0 sau khi thu hoặc miễn, trạng thái là đã thanh toán phí. |
| BR-WAV-01 | Miễn giảm bắt buộc có số tiền lớn hơn 0, lý do và người duyệt, không vượt số còn phải thu, không áp cho phiếu đã hủy. |
| BR-POL-01 | Chỉ một chính sách đang áp dụng. Tạo chính sách mới thì tắt các chính sách cũ. Phí mỗi ngày phải lớn hơn 0. Số ngày miễn phạt và trần phí không được âm. Trần có thể bỏ trống. |
| BR-RPT-01 | Quá hạn nghĩa là hạn trả trước ngày xem và phiếu còn sách chưa trả. Báo cáo ước tính phí nếu trả nốt phần còn lại trong ngày xem, không ghi phí đó vào phiếu. |
| BR-PAGE-01 | Danh sách sách và phiếu phân trang, mỗi trang 8 dòng. Sách sắp theo tên. Phiếu sắp theo mã giảm dần. |
| BR-ERR-01 | Lỗi nhập trên form hiện tại đúng trường. Lỗi nghiệp vụ hiện câu tiếng Việt, không hiện lỗi kỹ thuật. |
| BR-TX-01 | Tạo phiếu, trả sách, hủy phiếu, thu phí và miễn giảm chạy trong một giao dịch. Lỗi giữa chừng thì hoàn tác toàn bộ lần ghi đó. |

Công thức BR-FINE-01 nằm ở `FineCalculationService`. Controller không tính phí.

## 6. Ánh xạ màn hình

| Màn hình | Đường dẫn | Use case |
| --- | --- | --- |
| Trang chủ | `/` | Chuyển tới danh sách sách. |
| Danh sách sách | `/books` | UC-BOOK-01 |
| Form sách | `/books/new`, `/books/{id}/edit` | UC-BOOK-02, UC-BOOK-03. Tạo thể loại bằng POST `/books/categories`. |
| Danh sách độc giả | `/members` | UC-MEM-01 |
| Form độc giả | `/members/new`, `/members/{id}/edit` | UC-MEM-02, UC-MEM-03 |
| Danh sách phiếu | `/borrowings` | UC-BOR-01 |
| Tạo phiếu | `/borrowings/new` | UC-BOR-02. Lưu bằng POST `/borrowings`. |
| Chi tiết phiếu | `/borrowings/{id}` | UC-BOR-03, UC-BOR-04. Hủy bằng POST `/borrowings/{id}/cancel`. |
| Trả sách | `/borrowings/{id}/return` | UC-RET-01, UC-RET-02. Xem phí và xác nhận là hai thao tác POST. |
| Thu phí | `/borrowings/{id}/payments/new` | UC-PAY-01. Lưu bằng POST `/borrowings/{id}/payments`. Miễn giảm bằng POST `/borrowings/{id}/waivers`. |
| Chính sách phí | `/fine-policies` | UC-POL-01, UC-POL-02 |
| Báo cáo quá hạn | `/reports/overdue` | UC-RPT-01 |

## 7. Ánh xạ dữ liệu nghiệp vụ

| Đối tượng | Thông tin nghiệp vụ | Quan hệ |
| --- | --- | --- |
| Thể loại | Mã, tên. | Một thể loại có nhiều sách. |
| Sách | ISBN, tên, tác giả, tổng số lượng, số đang có, phiên bản tồn kho. | Nhiều sách thuộc một thể loại. |
| Độc giả | Mã, họ tên, email, điện thoại. | Một độc giả có nhiều phiếu. |
| Phiếu mượn | Ngày mượn, hạn trả, ngày trả hết, trạng thái, tổng phí, đã thu, đã miễn, còn phải thu. | Một phiếu có nhiều dòng, nhiều lần thu và nhiều lần miễn. |
| Dòng phiếu | Số lượng mượn, số đã trả, ngày trả gần nhất, số ngày trễ, phí của dòng. | Ghi nhận mượn và trả từng đầu sách. |
| Chính sách phí | Phí mỗi ngày, trần phí, số ngày miễn phạt, đang áp dụng hay không. | Lần trả dùng chính sách đang bật. |
| Lần thu | Số tiền, ngày thu, phương thức, ghi chú. | Gắn với một phiếu. |
| Lần miễn giảm | Số tiền, lý do, người duyệt, ngày duyệt. | Gắn với một phiếu. |

Trạng thái phiếu: đang mượn, trả một phần, đã trả, quá hạn, còn nợ phí, đã thanh toán phí, đã hủy. Còn nợ phí và đã thanh toán phí chỉ xuất hiện sau khi đã trả hết sách.

## 8. Open questions

- Đề không mô tả đăng nhập. Chưa tách vai trò thủ thư và quản trị.
- Đề không bắt buộc màn sửa hoặc ngừng dùng thể loại. Hệ thống hiện chỉ tạo thể loại từ form sách.
- Đề không nói độc giả có trạng thái khóa. Hệ thống hiện không khóa độc giả; chặn mượn mới bằng phiếu quá hạn hoặc phí chưa thu.
- Đề không nói ngừng sử dụng sách. Hệ thống hiện không có trạng thái ngừng cho mượn.
- Hạn trả do thủ thư nhập. Form chỉ gợi ý 14 ngày, không khóa giá trị này.
- Dữ liệu khởi tạo có chính sách 5.000 đồng mỗi ngày, miễn 1 ngày, trần 200.000 đồng. Đó là dữ liệu ban đầu, không phải mức phí cố định. Thủ thư có thể ban hành chính sách khác.
- Đề không nói được thu phí khi sách chưa trả hết. Hệ thống hiện cho thu hoặc miễn khi còn số phải thu và phiếu chưa hủy. Trạng thái còn nợ phí hoặc đã thanh toán phí chỉ gán sau khi trả hết sách.
- Đề không có báo cáo top sách hoặc số lượt mượn theo độc giả. Hệ thống hiện chỉ có báo cáo quá hạn.

## 9. Ngoài phạm vi

- Đăng nhập và phân quyền.
- Độc giả tự tra cứu hoặc tự gia hạn.
- Ngừng dùng thể loại, khóa độc giả, ngừng cho mượn sách.
- Top sách được mượn và thống kê theo độc giả.
- Kênh REST làm giao diện chính.
