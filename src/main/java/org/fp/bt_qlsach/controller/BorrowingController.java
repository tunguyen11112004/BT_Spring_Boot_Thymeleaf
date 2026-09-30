package org.fp.bt_qlsach.controller;

import jakarta.validation.Valid;
import org.fp.bt_qlsach.entity.Book;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.service.BookService;
import org.fp.bt_qlsach.service.BorrowingService;
import org.fp.bt_qlsach.service.MemberService;
import org.fp.bt_qlsach.service.PaymentService;
import org.fp.bt_qlsach.service.ReturnService;
import org.fp.bt_qlsach.service.WaiverService;
import org.fp.bt_qlsach.dto.BorrowLineCommand;
import org.fp.bt_qlsach.dto.BorrowingView;
import org.fp.bt_qlsach.dto.CreateBorrowingCommand;
import org.fp.bt_qlsach.dto.ReturnLineCommand;
import org.fp.bt_qlsach.dto.ReturnQuote;
import org.fp.bt_qlsach.dto.BorrowLineForm;
import org.fp.bt_qlsach.dto.BorrowingForm;
import org.fp.bt_qlsach.dto.PaymentForm;
import org.fp.bt_qlsach.dto.ReturnForm;
import org.fp.bt_qlsach.dto.ReturnLineForm;
import org.fp.bt_qlsach.dto.WaiverForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final ReturnService returnService;
    private final PaymentService paymentService;
    private final WaiverService waiverService;
    private final MemberService memberService;
    private final BookService bookService;

    public BorrowingController(BorrowingService borrowingService,
                               ReturnService returnService,
                               PaymentService paymentService,
                               WaiverService waiverService,
                               MemberService memberService,
                               BookService bookService) {
        this.borrowingService = borrowingService;
        this.returnService = returnService;
        this.paymentService = paymentService;
        this.waiverService = waiverService;
        this.memberService = memberService;
        this.bookService = bookService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long memberId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(defaultValue = "false") boolean overdueOnly,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        BorrowingStatus statusValue = (status == null || status.isBlank()) ? null : BorrowingStatus.valueOf(status);
        LocalDate fromDate = (from == null || from.isBlank()) ? null : LocalDate.parse(from);
        LocalDate toDate = (to == null || to.isBlank()) ? null : LocalDate.parse(to);
        model.addAttribute("borrowings", borrowingService.search(
                memberId, statusValue, fromDate, toDate, overdueOnly,
                PageRequest.of(Math.max(page, 0), 8, Sort.by(Sort.Direction.DESC, "id"))));
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("memberId", memberId);
        model.addAttribute("status", statusValue);
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("overdueOnly", overdueOnly);
        return "borrowings/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        List<Book> books = bookService.findAll();
        BorrowingForm form = new BorrowingForm();
        form.setBorrowedDate(LocalDate.now());
        form.setDueDate(LocalDate.now().plusDays(14));
        form.setLines(linesFor(books));
        model.addAttribute("form", form);
        model.addAttribute("books", books);
        model.addAttribute("members", memberService.findAll());
        return "borrowings/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BorrowingForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        List<Book> books = bookService.findAll();
        if (binding.hasErrors()) {
            model.addAttribute("books", books);
            model.addAttribute("members", memberService.findAll());
            return "borrowings/form";
        }
        try {
            Long id = borrowingService.create(new CreateBorrowingCommand(
                    form.getMemberId(),
                    form.getBorrowedDate(),
                    form.getDueDate(),
                    toCommands(form.getLines())));
            redirect.addFlashAttribute("successMessage", "Tạo phiếu mượn thành công.");
            return "redirect:/borrowings/" + id;
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("books", books);
            model.addAttribute("members", memberService.findAll());
            return "borrowings/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BorrowingView view = borrowingService.getView(id);
        model.addAttribute("borrowing", view.borrowing());
        model.addAttribute("payments", view.payments());
        model.addAttribute("waivers", view.waivers());
        if (!model.containsAttribute("waiverForm")) {
            model.addAttribute("waiverForm", new WaiverForm());
        }
        return "borrowings/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        borrowingService.cancel(id);
        redirect.addFlashAttribute("successMessage", "Đã hủy phiếu và hoàn tồn kho.");
        return "redirect:/borrowings/" + id;
    }

    @GetMapping("/{id}/return")
    public String returnForm(@PathVariable Long id, Model model) {
        BorrowingView view = borrowingService.getView(id);
        ReturnForm form = new ReturnForm();
        view.borrowing().getDetails().forEach(detail -> {
            ReturnLineForm line = new ReturnLineForm();
            line.setDetailId(detail.getId());
            line.setQuantity(0);
            form.getLines().add(line);
        });
        model.addAttribute("form", form);
        model.addAttribute("borrowing", view.borrowing());
        putEstimates(model, view);
        return "borrowings/return";
    }

    @PostMapping("/{id}/return")
    public String submitReturn(@PathVariable Long id,
                               @ModelAttribute("form") ReturnForm form,
                               @RequestParam String action,
                               Model model,
                               RedirectAttributes redirect) {
        List<ReturnLineCommand> commands = toReturnCommands(form.getLines());
        if ("confirm".equals(action)) {
            try {
                var quotes = returnService.confirm(id, commands, LocalDate.now());
                redirect.addFlashAttribute("successMessage",
                        "Đã ghi nhận trả sách. Phí phát sinh thêm: " + ReturnService.totalAdded(quotes) + " đ.");
                return "redirect:/borrowings/" + id;
            } catch (BusinessException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        } else {
            try {
                model.addAttribute("previewByDetail", index(returnService.preview(id, commands, LocalDate.now())));
                model.addAttribute("successMessage", "Đây là phí dự kiến, chưa ghi nhận trả sách.");
            } catch (BusinessException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
        BorrowingView view = borrowingService.getView(id);
        model.addAttribute("borrowing", view.borrowing());
        putEstimates(model, view);
        return "borrowings/return";
    }

    @GetMapping("/{id}/payments/new")
    public String paymentForm(@PathVariable Long id, Model model) {
        BorrowingView view = borrowingService.getView(id);
        model.addAttribute("borrowing", view.borrowing());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PaymentForm());
        }
        return "borrowings/payment";
    }

    @PostMapping("/{id}/payments")
    public String pay(@PathVariable Long id,
                      @Valid @ModelAttribute("form") PaymentForm form,
                      BindingResult binding,
                      Model model,
                      RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("borrowing", borrowingService.getView(id).borrowing());
            return "borrowings/payment";
        }
        try {
            paymentService.pay(id, form.getAmount(), form.getMethod(), form.getNote(), LocalDate.now());
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("borrowing", borrowingService.getView(id).borrowing());
            return "borrowings/payment";
        }
        redirect.addFlashAttribute("successMessage", "Đã ghi nhận thanh toán phí phạt.");
        return "redirect:/borrowings/" + id;
    }

    @PostMapping("/{id}/waivers")
    public String waive(@PathVariable Long id,
                        @Valid @ModelAttribute("waiverForm") WaiverForm form,
                        BindingResult binding,
                        RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("errorMessage", "Miễn giảm cần số tiền, lý do và người duyệt.");
            return "redirect:/borrowings/" + id;
        }
        waiverService.waive(id, form.getAmount(), form.getReason(), form.getApprovedBy(), LocalDate.now());
        redirect.addFlashAttribute("successMessage", "Đã ghi nhận miễn giảm phí.");
        return "redirect:/borrowings/" + id;
    }

    private void putEstimates(Model model, BorrowingView view) {
        if (!view.borrowing().canReturn()) {
            model.addAttribute("estimates", Map.of());
            return;
        }
        try {
            model.addAttribute("estimates", index(returnService.quoteRemaining(view.borrowing().getId(), LocalDate.now())));
        } catch (BusinessException ex) {
            model.addAttribute("estimates", Map.of());
        }
    }

    private Map<Long, ReturnQuote> index(List<ReturnQuote> quotes) {
        Map<Long, ReturnQuote> map = new HashMap<>();
        for (ReturnQuote quote : quotes) {
            map.put(quote.detailId(), quote);
        }
        return map;
    }

    private List<BorrowLineForm> linesFor(List<Book> books) {
        List<BorrowLineForm> lines = new ArrayList<>();
        for (Book book : books) {
            BorrowLineForm line = new BorrowLineForm();
            line.setBookId(book.getId());
            line.setQuantity(0);
            lines.add(line);
        }
        return lines;
    }

    private List<BorrowLineCommand> toCommands(List<BorrowLineForm> lines) {
        List<BorrowLineCommand> commands = new ArrayList<>();
        if (lines == null) {
            return commands;
        }
        for (BorrowLineForm line : lines) {
            commands.add(new BorrowLineCommand(line.getBookId(), line.getQuantity() == null ? 0 : line.getQuantity()));
        }
        return commands;
    }

    private List<ReturnLineCommand> toReturnCommands(List<ReturnLineForm> lines) {
        List<ReturnLineCommand> commands = new ArrayList<>();
        if (lines == null) {
            return commands;
        }
        for (ReturnLineForm line : lines) {
            commands.add(new ReturnLineCommand(line.getDetailId(), line.getQuantity() == null ? 0 : line.getQuantity()));
        }
        return commands;
    }
}
