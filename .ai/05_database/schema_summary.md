# Schema summary

Nguồn: `src/main/resources/db/migration/V1__init.sql`. Hibernate `ddl-auto=validate`.

| Bảng | Việc |
| --- | --- |
| `category` | Thể loại |
| `book` | Sách, tồn kho, cột `version` |
| `member` | Độc giả, `member_code` duy nhất |
| `borrowing` | Phiếu, trạng thái, các khoản phí |
| `borrowing_detail` | Dòng mượn và phí của dòng |
| `fine_policy` | Chính sách, cờ `active` |
| `fine_payment` | Lần thu |
| `fine_waiver` | Lần miễn giảm |

Tiền dùng `DECIMAL(12,2)`.
