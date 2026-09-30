package org.fp.bt_qlsach.controller;

import jakarta.validation.Valid;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.service.MemberService;
import org.fp.bt_qlsach.dto.MemberForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.findAll());
        return "members/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new MemberForm());
        model.addAttribute("memberId", null);
        return "members/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") MemberForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "members/form";
        }
        try {
            memberService.create(form.getMemberCode(), form.getFullName(), form.getEmail(), form.getPhone());
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "members/form";
        }
        redirect.addFlashAttribute("successMessage", "Đã thêm độc giả.");
        return "redirect:/members";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", MemberForm.from(memberService.get(id)));
        model.addAttribute("memberId", id);
        return "members/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") MemberForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("memberId", id);
            return "members/form";
        }
        try {
            memberService.update(id, form.getMemberCode(), form.getFullName(), form.getEmail(), form.getPhone());
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("memberId", id);
            return "members/form";
        }
        redirect.addFlashAttribute("successMessage", "Đã cập nhật độc giả.");
        return "redirect:/members";
    }
}
