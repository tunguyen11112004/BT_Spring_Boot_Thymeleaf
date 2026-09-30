package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Book;
import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.Category;
import org.fp.bt_qlsach.entity.Member;
import org.fp.bt_qlsach.entity.PaymentMethod;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.BookRepository;
import org.fp.bt_qlsach.repository.BorrowingRepository;
import org.fp.bt_qlsach.repository.CategoryRepository;
import org.fp.bt_qlsach.repository.MemberRepository;
import org.fp.bt_qlsach.dto.BorrowLineCommand;
import org.fp.bt_qlsach.dto.CreateBorrowingCommand;
import org.fp.bt_qlsach.dto.ReturnLineCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LibraryFlowTest {

    @Autowired
    private BorrowingService borrowingService;
    @Autowired
    private ReturnService returnService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private WaiverService waiverService;
    @Autowired
    private FinePolicyService finePolicyService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BorrowingRepository borrowingRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("TL " + UUID.randomUUID());
        category = categoryRepository.saveAndFlush(category);
        finePolicyService.create(new BigDecimal("1000.00"), null, 0);
    }

    @Test
    void borrowSeveralBooksDecreasesStock() {
        Book first = newBook(5);
        Book second = newBook(4);
        Member member = newMember();

        Long id = borrowingService.create(command(member, LocalDate.now(), LocalDate.now().plusDays(7),
                List.of(new BorrowLineCommand(first.getId(), 2), new BorrowLineCommand(second.getId(), 1))));

        Borrowing borrowing = borrowingRepository.findDetailedById(id).orElseThrow();
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.BORROWING);
        assertThat(borrowing.getDetails()).hasSize(2);
        assertThat(reload(first).getAvailableQuantity()).isEqualTo(3);
        assertThat(reload(second).getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    void insufficientStockRollsBackTheWholeBorrowing() {
        Book enough = newBook(5);
        Book scarce = newBook(1);
        Member member = newMember();
        long before = borrowingRepository.count();

        assertThatThrownBy(() -> borrowingService.create(command(member, LocalDate.now(), LocalDate.now().plusDays(7),
                List.of(new BorrowLineCommand(enough.getId(), 2), new BorrowLineCommand(scarce.getId(), 5)))))
                .isInstanceOf(BusinessException.class);

        assertThat(borrowingRepository.count()).isEqualTo(before);
        assertThat(reload(enough).getAvailableQuantity()).isEqualTo(5);
        assertThat(reload(scarce).getAvailableQuantity()).isEqualTo(1);
    }

    @Test
    void fullReturnOnTimeHasNoFineAndIsReturned() {
        Book book = newBook(3);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        Long id = borrowingService.create(command(member, today, today,
                List.of(new BorrowLineCommand(book.getId(), 1))));

        returnService.confirm(id, List.of(line(id, 1)), today);

        Borrowing borrowing = borrowingRepository.findDetailedById(id).orElseThrow();
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.RETURNED);
        assertThat(borrowing.getTotalFineAmount()).isEqualByComparingTo("0");
        assertThat(borrowing.getUnpaidFineAmount()).isEqualByComparingTo("0");
        assertThat(reload(book).getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    void partialReturnOnTimeKeepsTicketOpenAndRestoresStock() {
        Book book = newBook(6);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        Long id = borrowingService.create(command(member, today, today.plusDays(5),
                List.of(new BorrowLineCommand(book.getId(), 2))));

        returnService.confirm(id, List.of(line(id, 1)), today);

        Borrowing borrowing = borrowingRepository.findDetailedById(id).orElseThrow();
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.PARTIALLY_RETURNED);
        assertThat(borrowing.getTotalFineAmount()).isEqualByComparingTo("0");
        assertThat(borrowing.getDetails().get(0).getReturnedQuantity()).isEqualTo(1);
        assertThat(reload(book).getAvailableQuantity()).isEqualTo(5);
    }

    @Test
    void returnThreeDaysLateCalculatesFine() {
        Book book = newBook(2);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        Long id = borrowingService.create(command(member, today.minusDays(10), today.minusDays(3),
                List.of(new BorrowLineCommand(book.getId(), 1))));

        returnService.confirm(id, List.of(line(id, 1)), today);

        Borrowing borrowing = borrowingRepository.findDetailedById(id).orElseThrow();
        assertThat(borrowing.getDetails().get(0).getLateDays()).isEqualTo(3);
        assertThat(borrowing.getDetails().get(0).getFineAmount()).isEqualByComparingTo("3000.00");
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.FINE_PENDING);
        assertThat(borrowing.getUnpaidFineAmount()).isEqualByComparingTo("3000.00");
    }

    @Test
    void partialLateReturnChargesOnlyTheQuantityReturnedThisTime() {
        Book book = newBook(8);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        Long id = borrowingService.create(command(member, today.minusDays(10), today.minusDays(3),
                List.of(new BorrowLineCommand(book.getId(), 4))));

        returnService.confirm(id, List.of(line(id, 2)), today);

        Borrowing borrowing = borrowingRepository.findDetailedById(id).orElseThrow();
        assertThat(borrowing.getDetails().get(0).getFineAmount()).isEqualByComparingTo("6000.00");
        assertThat(borrowing.getDetails().get(0).getReturnedQuantity()).isEqualTo(2);
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.OVERDUE);
        assertThat(reload(book).getAvailableQuantity()).isEqualTo(6);
    }

    @Test
    void partialPaymentUpdatesPaidAndUnpaid() {
        Long id = returnedWithFine(new BigDecimal("3000.00"));

        paymentService.pay(id, new BigDecimal("1000"), PaymentMethod.CASH, "lan 1", LocalDate.now());

        Borrowing borrowing = borrowingRepository.findById(id).orElseThrow();
        assertThat(borrowing.getPaidFineAmount()).isEqualByComparingTo("1000.00");
        assertThat(borrowing.getUnpaidFineAmount()).isEqualByComparingTo("2000.00");
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.FINE_PENDING);
    }

    @Test
    void overpaymentIsRejectedAndAmountsStayTheSame() {
        Long id = returnedWithFine(new BigDecimal("3000.00"));

        assertThatThrownBy(() -> paymentService.pay(id, new BigDecimal("3001"), PaymentMethod.TRANSFER, null, LocalDate.now()))
                .isInstanceOf(BusinessException.class);

        Borrowing borrowing = borrowingRepository.findById(id).orElseThrow();
        assertThat(borrowing.getPaidFineAmount()).isEqualByComparingTo("0");
        assertThat(borrowing.getUnpaidFineAmount()).isEqualByComparingTo("3000.00");
    }

    @Test
    void memberWithUnpaidFineCannotBorrowAgain() {
        Book first = newBook(2);
        Book second = newBook(2);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        Long id = borrowingService.create(command(member, today.minusDays(10), today.minusDays(3),
                List.of(new BorrowLineCommand(first.getId(), 1))));
        returnService.confirm(id, List.of(line(id, 1)), today);

        assertThatThrownBy(() -> borrowingService.create(command(member, today, today.plusDays(7),
                List.of(new BorrowLineCommand(second.getId(), 1)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("phí phạt");
        assertThat(reload(second).getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    void memberWithOverdueTicketCannotBorrowAgain() {
        Book held = newBook(2);
        Book next = newBook(2);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        borrowingService.create(command(member, today.minusDays(5), today.minusDays(1),
                List.of(new BorrowLineCommand(held.getId(), 1))));

        assertThatThrownBy(() -> borrowingService.create(command(member, today, today.plusDays(7),
                List.of(new BorrowLineCommand(next.getId(), 1)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("quá hạn");
        assertThat(reload(next).getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    void failedStockIncreaseRollsBackReturnAndFine() {
        Book first = newBook(4);
        Book second = newBook(4);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        Long id = borrowingService.create(command(member, today.minusDays(10), today.minusDays(3), List.of(
                new BorrowLineCommand(first.getId(), 1),
                new BorrowLineCommand(second.getId(), 1))));
        Book blocked = reload(second);
        blocked.setTotalQuantity(blocked.getAvailableQuantity());
        bookRepository.saveAndFlush(blocked);

        assertThatThrownBy(() -> returnService.confirm(id, List.of(lineOf(id, first.getId(), 1), lineOf(id, second.getId(), 1)), today))
                .isInstanceOf(BusinessException.class);

        Borrowing borrowing = borrowingRepository.findDetailedById(id).orElseThrow();
        assertThat(borrowing.getTotalFineAmount()).isEqualByComparingTo("0");
        assertThat(borrowing.getDetails()).allMatch(detail -> detail.getReturnedQuantity() == 0);
        assertThat(reload(first).getAvailableQuantity()).isEqualTo(3);
        assertThat(reload(second).getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    void onlyOneConcurrentBorrowOfTheLastCopySucceeds() throws Exception {
        Book book = newBook(1);
        Member first = newMember();
        Member second = newMember();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        AtomicInteger wins = new AtomicInteger();
        try {
            Future<Boolean> left = pool.submit(() -> borrowLastCopy(first, book, ready, start, wins));
            Future<Boolean> right = pool.submit(() -> borrowLastCopy(second, book, ready, start, wins));
            assertThat(ready.await(15, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            boolean leftOk = left.get(30, TimeUnit.SECONDS);
            boolean rightOk = right.get(30, TimeUnit.SECONDS);
            assertThat(List.of(leftOk, rightOk)).containsOnlyOnce(true);
        } finally {
            pool.shutdownNow();
        }
        assertThat(reload(book).getAvailableQuantity()).isZero();
        assertThat(wins.get()).isEqualTo(1);
    }

    private boolean borrowLastCopy(Member member, Book book, CountDownLatch ready, CountDownLatch start, AtomicInteger wins) {
        ready.countDown();
        try {
            if (!start.await(15, TimeUnit.SECONDS)) {
                return false;
            }
            borrowingService.create(command(member, LocalDate.now(), LocalDate.now().plusDays(7),
                    List.of(new BorrowLineCommand(book.getId(), 1))));
            wins.incrementAndGet();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Long returnedWithFine(BigDecimal expected) {
        Book book = newBook(1);
        Member member = newMember();
        LocalDate today = LocalDate.now();
        Long id = borrowingService.create(command(member, today.minusDays(10), today.minusDays(3),
                List.of(new BorrowLineCommand(book.getId(), 1))));
        returnService.confirm(id, List.of(line(id, 1)), today);
        assertThat(borrowingRepository.findById(id).orElseThrow().getUnpaidFineAmount()).isEqualByComparingTo(expected);
        return id;
    }

    private ReturnLineCommand line(Long borrowingId, int quantity) {
        Long detailId = borrowingRepository.findDetailedById(borrowingId).orElseThrow().getDetails().get(0).getId();
        return new ReturnLineCommand(detailId, quantity);
    }

    private ReturnLineCommand lineOf(Long borrowingId, Long bookId, int quantity) {
        Long detailId = borrowingRepository.findDetailedById(borrowingId).orElseThrow().getDetails().stream()
                .filter(detail -> detail.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow()
                .getId();
        return new ReturnLineCommand(detailId, quantity);
    }

    private CreateBorrowingCommand command(Member member, LocalDate borrowed, LocalDate due, List<BorrowLineCommand> lines) {
        return new CreateBorrowingCommand(member.getId(), borrowed, due, lines);
    }

    private Book newBook(int quantity) {
        Book book = new Book();
        book.setTitle("Sach " + UUID.randomUUID());
        book.setAuthor("Tac gia");
        book.setIsbn(UUID.randomUUID().toString().substring(0, 20));
        book.setCategory(category);
        book.setTotalQuantity(quantity);
        book.setAvailableQuantity(quantity);
        return bookRepository.saveAndFlush(book);
    }

    private Member newMember() {
        Member member = new Member();
        member.setMemberCode("T" + UUID.randomUUID().toString().substring(0, 8));
        member.setFullName("Doc gia test");
        member.setEmail("docgia@example.com");
        member.setPhone("0900000000");
        return memberRepository.saveAndFlush(member);
    }

    private Book reload(Book book) {
        return bookRepository.findById(book.getId()).orElseThrow();
    }
}
