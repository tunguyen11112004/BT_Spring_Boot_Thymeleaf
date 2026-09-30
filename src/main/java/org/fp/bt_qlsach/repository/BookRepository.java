package org.fp.bt_qlsach.repository;

import org.fp.bt_qlsach.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findAllByOrderByTitleAsc();

    @Query("""
            select b from Book b
            where lower(b.title) like lower(concat('%', :kw, '%'))
               or lower(b.author) like lower(concat('%', :kw, '%'))
               or lower(b.isbn) like lower(concat('%', :kw, '%'))
            """)
    Page<Book> search(@Param("kw") String keyword, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE book
            SET available_quantity = available_quantity - :qty, version = version + 1
            WHERE id = :id AND available_quantity >= :qty
            """, nativeQuery = true)
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE book
            SET available_quantity = available_quantity + :qty, version = version + 1
            WHERE id = :id AND available_quantity + :qty <= total_quantity
            """, nativeQuery = true)
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);
}
