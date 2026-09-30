package org.fp.bt_qlsach.controller;

import jakarta.validation.Valid;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.service.FinePolicyService;
import org.fp.bt_qlsach.dto.FinePolicyForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fine-policies")
public class FinePolicyController {

    private final FinePolicyService finePolicyService;

    public FinePolicyController(FinePolicyService finePolicyService) {
        this.finePolicyService = finePolicyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("policies", finePolicyService.findAll());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new FinePolicyForm());
        }
        return "fine-policies/list";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") FinePolicyForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("policies", finePolicyService.findAll());
            return "fine-policies/list";
        }
        try {
            finePolicyService.create(form.getDailyFineAmount(), form.getMaxFineAmount(), form.getGraceDays());
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("policies", finePolicyService.findAll());
            return "fine-policies/list";
        }
        redirect.addFlashAttribute("successMessage", "Đã áp dụng chính sách phí mới. Chính sách cũ ngừng hiệu lực.");
        return "redirect:/fine-policies";
    }
}
