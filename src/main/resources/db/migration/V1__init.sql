CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE TABLE book (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    isbn VARCHAR(32) NOT NULL,
    category_id BIGINT NOT NULL,
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_book_isbn UNIQUE (isbn),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_code VARCHAR(30) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20),
    PRIMARY KEY (id),
    CONSTRAINT uk_member_code UNIQUE (member_code)
);

CREATE TABLE borrowing (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    borrowed_date DATE NOT NULL,
    due_date DATE NOT NULL,
    returned_date DATE,
    status VARCHAR(30) NOT NULL,
    total_fine_amount DECIMAL(12, 2) NOT NULL,
    paid_fine_amount DECIMAL(12, 2) NOT NULL,
    waived_fine_amount DECIMAL(12, 2) NOT NULL,
    unpaid_fine_amount DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_borrowing_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE borrowing_detail (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrowing_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    returned_quantity INT NOT NULL,
    last_returned_date DATE,
    late_days INT NOT NULL,
    fine_amount DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detail_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowing (id),
    CONSTRAINT fk_detail_book FOREIGN KEY (book_id) REFERENCES book (id)
);

CREATE TABLE fine_policy (
    id BIGINT NOT NULL AUTO_INCREMENT,
    daily_fine_amount DECIMAL(12, 2) NOT NULL,
    max_fine_amount DECIMAL(12, 2),
    grace_days INT NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE fine_payment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrowing_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_date DATE NOT NULL,
    method VARCHAR(20) NOT NULL,
    note VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowing (id)
);

CREATE TABLE fine_waiver (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrowing_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    approved_by VARCHAR(120) NOT NULL,
    approved_date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_waiver_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowing (id)
);

CREATE INDEX idx_borrowing_member_status ON borrowing (member_id, status);
CREATE INDEX idx_borrowing_due_status ON borrowing (due_date, status);
CREATE INDEX idx_detail_borrowing ON borrowing_detail (borrowing_id);
CREATE INDEX idx_detail_book ON borrowing_detail (book_id);
CREATE INDEX idx_payment_borrowing ON fine_payment (borrowing_id);
CREATE INDEX idx_waiver_borrowing ON fine_waiver (borrowing_id);

INSERT INTO category (name, description) VALUES
    ('Văn học', 'Tiểu thuyết và truyện'),
    ('Kỹ năng', 'Sách phát triển kỹ năng'),
    ('Lập trình', 'Sách công nghệ và lập trình');

INSERT INTO book (title, author, isbn, category_id, total_quantity, available_quantity, version) VALUES
    ('Đắc Nhân Tâm', 'Dale Carnegie', '9786041230001', 2, 5, 5, 0),
    ('Nhà Giả Kim', 'Paulo Coelho', '9786041230002', 1, 4, 4, 0),
    ('Sapiens', 'Yuval Noah Harari', '9786041230003', 2, 3, 3, 0),
    ('Clean Code', 'Robert C. Martin', '9786041230004', 3, 2, 2, 0),
    ('Dế Mèn Phiêu Lưu Ký', 'Tô Hoài', '9786041230005', 1, 6, 6, 0);

INSERT INTO member (member_code, full_name, email, phone) VALUES
    ('DG001', 'Nguyễn Văn An', 'an.nguyen@example.com', '0901000001'),
    ('DG002', 'Trần Thị Bình', 'binh.tran@example.com', '0901000002'),
    ('DG003', 'Lê Hoàng Cường', 'cuong.le@example.com', '0901000003');

INSERT INTO fine_policy (daily_fine_amount, max_fine_amount, grace_days, active) VALUES
    (5000.00, 200000.00, 1, TRUE);
