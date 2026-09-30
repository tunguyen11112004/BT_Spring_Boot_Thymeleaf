package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingDetail;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.Book;
import org.fp.bt_qlsach.entity.Member;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.BookRepository;
import org.fp.bt_qlsach.repository.BorrowingRepository;
import org.fp.bt_qlsach.repository.FinePaymentRepository;
import org.fp.bt_qlsach.repository.FineWaiverRepository;
import org.fp.bt_qlsach.repository.MemberRepository;
import org.fp.bt_qlsach.dto.BorrowLineCommand;
import org.fp.bt_qlsach.dto.BorrowingView;
import org.fp.bt_qlsach.dto.CreateBorrowingCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BorrowingService {

    public static final int MAX_OPEN_COPIES = 5;

    private static final List<BorrowingStatus> OPEN = List.of(
            BorrowingStatus.BORROWING,
            BorrowingStatus.PARTIALLY_RETURNED,
            BorrowingStatus.OVERDUE);

    private final BorrowingRepository borrowingRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final FinePaymentRepository finePaymentRepository;
    private final FineWaiverRepository fineWaiverRepository;

    public BorrowingService(BorrowingRepository borrowingRepository,
                            MemberRepository memberRepository,
                            BookRepository bookRepository,
                            FinePaymentRepository finePaymentRepository,
                            FineWaiverRepository fineWaiverRepository) {
        this.borrowingRepository = borrowingRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.finePaymentRepository = finePaymentRepository;
        this.fineWaiverRepository = fineWaiverRepository;
    }

    @Transactional
    public Long create(CreateBorrowingCommand command) {
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy độc giả."));
        if (command.borrowedDate() == null || command.dueDate() == null) {
            throw new BusinessException("Phiếu mượn phải có ngày mượn và hạn trả.");
        }
        if (command.dueDate().isBefore(command.borrowedDate())) {
            throw new BusinessException("Hạn trả không được trước ngày mượn.");
        }
        LocalDate today = LocalDate.now();
        if (borrowingRepository.countUnpaidFine(member.getId(), BorrowingStatus.CANCELLED) > 0) {
            throw new BusinessException("Độc giả còn phí phạt chưa thanh toán, không thể tạo phiếu mới.");
        }
        if (borrowingRepository.countOverdue(member.getId(), today, OPEN) > 0) {
            throw new BusinessException("Độc giả đang có phiếu quá hạn, không thể tạo phiếu mới.");
        }

        List<BorrowLineCommand> requested = command.lines() == null ? List.of() : command.lines();
        for (BorrowLineCommand line : requested) {
            if (line.quantity() < 0) {
                throw new BusinessException("Số lượng mượn phải lớn hơn 0.");
            }
        }
        List<BorrowLineCommand> lines = requested.stream().filter(line -> line.quantity() > 0).toList();
        if (lines.isEmpty()) {
            throw new BusinessException("Mỗi phiếu phải có ít nhất một đầu sách.");
        }

        Set<Long> seen = new HashSet<>();
        int newCopies = 0;
        List<PreparedLine> prepared = new ArrayList<>();
        for (BorrowLineCommand line : lines) {
            if (line.bookId() == null || !seen.add(line.bookId())) {
                throw new BusinessException("Không được có hai dòng trùng một đầu sách trên cùng phiếu.");
            }
            Book book = bookRepository.findById(line.bookId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy sách."));
            prepared.add(new PreparedLine(book, line.quantity()));
            newCopies += line.quantity();
        }
        long outstanding = borrowingRepository.sumOutstanding(member.getId(), OPEN);
        if (outstanding + newCopies > MAX_OPEN_COPIES) {
            throw new BusinessException("Mỗi độc giả chỉ được mượn tối đa 5 cuốn đang chưa trả.");
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setMember(member);
        borrowing.setBorrowedDate(command.borrowedDate());
        borrowing.setDueDate(command.dueDate());
        borrowing.setStatus(BorrowingStatus.BORROWING);
        for (PreparedLine line : prepared) {
            BorrowingDetail detail = new BorrowingDetail();
            detail.setBook(line.book());
            detail.setQuantity(line.quantity());
            detail.setReturnedQuantity(0);
            detail.setLateDays(0);
            borrowing.addDetail(detail);
        }
        borrowing.refreshStatus(today);
        borrowingRepository.saveAndFlush(borrowing);
        Long borrowingId = borrowing.getId();

        for (PreparedLine line : prepared) {
            int updated = bookRepository.decreaseStock(line.book().getId(), line.quantity());
            if (updated != 1) {
                throw new BusinessException("Sách \"" + line.book().getTitle()
                        + "\" không đủ số lượng. Toàn bộ phiếu được hoàn tác.");
            }
        }
        return borrowingId;
    }

    @Transactional
    public void refreshOverdueStatuses(LocalDate today) {
        List<Borrowing> open = borrowingRepository.findByStatusIn(OPEN);
        for (Borrowing borrowing : open) {
            borrowing.refreshStatus(today);
        }
    }

    @Transactional
    public Page<Borrowing> search(Long memberId,
                                  BorrowingStatus status,
                                  LocalDate from,
                                  LocalDate to,
                                  boolean overdueOnly,
                                  Pageable pageable) {
        refreshOverdueStatuses(LocalDate.now());
        Specification<Borrowing> spec = BorrowingSpecs.filter(memberId, status, from, to, overdueOnly);
        return borrowingRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public BorrowingView getView(Long id) {
        Borrowing borrowing = borrowingRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu mượn."));
        return new BorrowingView(
                borrowing,
                finePaymentRepository.findByBorrowingIdOrderByIdDesc(id),
                fineWaiverRepository.findByBorrowingIdOrderByIdDesc(id));
    }

    @Transactional
    public void cancel(Long id) {
        Borrowing borrowing = borrowingRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu mượn."));
        if (!borrowing.canCancel()) {
            throw new BusinessException("Chỉ hủy được phiếu chưa trả sách và chưa phát sinh phí.");
        }
        List<PreparedLine> lines = new ArrayList<>();
        for (BorrowingDetail detail : borrowing.getDetails()) {
            lines.add(new PreparedLine(detail.getBook(), detail.getQuantity()));
        }
        borrowing.setStatus(BorrowingStatus.CANCELLED);
        borrowingRepository.saveAndFlush(borrowing);
        for (PreparedLine line : lines) {
            int updated = bookRepository.increaseStock(line.book().getId(), line.quantity());
            if (updated != 1) {
                throw new BusinessException("Không thể hoàn tồn kho. Phiếu chưa được hủy.");
            }
        }
    }

    private record PreparedLine(Book book, int quantity) {
    }
}
