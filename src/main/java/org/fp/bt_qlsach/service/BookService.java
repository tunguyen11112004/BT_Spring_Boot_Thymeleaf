package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Book;
import org.fp.bt_qlsach.entity.Category;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.BookRepository;
import org.fp.bt_qlsach.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim().replace("%", "").replace("_", "");
        if (kw.isEmpty()) {
            return bookRepository.findAll(pageable);
        }
        return bookRepository.search(kw, pageable);
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAllByOrderByTitleAsc();
    }

    @Transactional(readOnly = true)
    public Book get(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy sách."));
    }

    @Transactional
    public Book create(String title, String author, String isbn, Long categoryId, int totalQuantity) {
        if (totalQuantity < 1) {
            throw new BusinessException("Tổng số lượng phải lớn hơn 0.");
        }
        String normalizedIsbn = isbn.trim();
        if (bookRepository.findByIsbn(normalizedIsbn).isPresent()) {
            throw new BusinessException("ISBN đã tồn tại.");
        }
        Book book = new Book();
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setIsbn(normalizedIsbn);
        book.setCategory(requireCategory(categoryId));
        book.setTotalQuantity(totalQuantity);
        book.setAvailableQuantity(totalQuantity);
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, String title, String author, String isbn, Long categoryId, int totalQuantity) {
        Book book = get(id);
        String normalizedIsbn = isbn.trim();
        bookRepository.findByIsbn(normalizedIsbn)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("ISBN đã tồn tại.");
                });
        int onLoan = book.getTotalQuantity() - book.getAvailableQuantity();
        if (totalQuantity < onLoan) {
            throw new BusinessException("Tổng số lượng không được nhỏ hơn số đang cho mượn (" + onLoan + ").");
        }
        int delta = totalQuantity - book.getTotalQuantity();
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setIsbn(normalizedIsbn);
        book.setCategory(requireCategory(categoryId));
        book.setTotalQuantity(totalQuantity);
        book.setAvailableQuantity(book.getAvailableQuantity() + delta);
        return bookRepository.save(book);
    }

    private Category requireCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thể loại."));
    }
}
