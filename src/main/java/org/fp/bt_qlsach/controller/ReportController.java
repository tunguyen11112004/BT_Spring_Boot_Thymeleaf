package org.fp.bt_qlsach.controller;

import org.fp.bt_qlsach.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/overdue")
    public String overdue(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("today", today);
        model.addAttribute("rows", reportService.overdueReport(today));
        return "reports/overdue";
    }
}
