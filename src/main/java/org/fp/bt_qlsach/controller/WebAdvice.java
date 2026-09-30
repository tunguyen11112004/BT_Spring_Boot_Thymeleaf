package org.fp.bt_qlsach.controller;

import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.PaymentMethod;
import org.fp.bt_qlsach.util.BusinessException;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@ControllerAdvice
public class WebAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(BigDecimal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                    return;
                }
                setValue(new BigDecimal(text.trim()));
            }
        });
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                    return;
                }
                setValue(LocalDate.parse(text.trim()));
            }
        });
    }

    @ModelAttribute("statuses")
    public BorrowingStatus[] statuses() {
        return BorrowingStatus.values();
    }

    @ModelAttribute("paymentMethods")
    public PaymentMethod[] paymentMethods() {
        return PaymentMethod.values();
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:" + safeReferer(request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleLock(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", "Dữ liệu vừa được người khác cập nhật. Vui lòng thử lại.");
        return "redirect:" + safeReferer(request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleIntegrity(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", "Dữ liệu bị trùng hoặc không hợp lệ.");
        return "redirect:" + safeReferer(request);
    }

    private String safeReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/books";
        }
        try {
            URI uri = URI.create(referer);
            if (uri.getHost() != null && !uri.getHost().equalsIgnoreCase(request.getServerName())) {
                return "/books";
            }
            String path = uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/books" : uri.getRawPath();
            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException ex) {
            return "/books";
        }
    }
}
