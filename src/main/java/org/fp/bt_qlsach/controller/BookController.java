package org.fp.bt_qlsach.controller;

import jakarta.validation.Valid;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.service.BookService;
import org.fp.bt_qlsach.service.CategoryService;
import org.fp.bt_qlsach.dto.BookForm;
import org.fp.bt_qlsach.dto.CategoryForm;
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

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("books", bookService.search(q, PageRequest.of(Math.max(page, 0), 8, Sort.by("title"))));
        return "books/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new BookForm());
        }
        model.addAttribute("bookId", null);
        prepare(model);
        return "books/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepare(model);
            return "books/form";
        }
        try {
            bookService.create(form.getTitle(), form.getAuthor(), form.getIsbn(), form.getCategoryId(), form.getTotalQuantity());
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            prepare(model);
            return "books/form";
        }
        redirect.addFlashAttribute("successMessage", "Đã thêm sách.");
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", BookForm.from(bookService.get(id)));
        }
        model.addAttribute("bookId", id);
        prepare(model);
        return "books/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") BookForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("bookId", id);
            prepare(model);
            return "books/form";
        }
        try {
            bookService.update(id, form.getTitle(), form.getAuthor(), form.getIsbn(), form.getCategoryId(), form.getTotalQuantity());
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("bookId", id);
            prepare(model);
            return "books/form";
        }
        redirect.addFlashAttribute("successMessage", "Đã cập nhật sách.");
        return "redirect:/books";
    }

    @PostMapping("/categories")
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult binding,
                                 RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("errorMessage", "Tên thể loại không hợp lệ.");
            return "redirect:/books/new";
        }
        try {
            categoryService.create(form.getName(), form.getDescription());
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/books/new";
        }
        redirect.addFlashAttribute("successMessage", "Đã thêm thể loại.");
        return "redirect:/books/new";
    }

    private void prepare(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
    }
}
