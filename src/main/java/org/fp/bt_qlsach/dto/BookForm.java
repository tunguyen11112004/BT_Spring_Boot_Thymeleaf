package org.fp.bt_qlsach.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookForm {

    @NotBlank(message = "Nhập tên sách")
    @Size(max = 200, message = "Tên sách tối đa 200 ký tự")
    private String title;

    @NotBlank(message = "Nhập tác giả")
    @Size(max = 150, message = "Tên tác giả tối đa 150 ký tự")
    private String author;

    @NotBlank(message = "Nhập ISBN")
    @Size(max = 32, message = "ISBN tối đa 32 ký tự")
    private String isbn;

    @NotNull(message = "Chọn thể loại")
    private Long categoryId;

    @NotNull(message = "Nhập tổng số lượng")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer totalQuantity;

    public static BookForm from(org.fp.bt_qlsach.entity.Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());
        form.setCategoryId(book.getCategory().getId());
        form.setTotalQuantity(book.getTotalQuantity());
        return form;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
