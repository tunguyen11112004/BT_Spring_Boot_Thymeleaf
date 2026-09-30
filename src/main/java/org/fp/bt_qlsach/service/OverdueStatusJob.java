package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.service.BorrowingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class OverdueStatusJob {

    private final BorrowingService borrowingService;

    public OverdueStatusJob(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void refreshOverdue() {
        borrowingService.refreshOverdueStatuses(LocalDate.now());
    }
}
