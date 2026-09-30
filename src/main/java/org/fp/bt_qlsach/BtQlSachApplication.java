package org.fp.bt_qlsach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BtQlSachApplication {

    public static void main(String[] args) {
        SpringApplication.run(BtQlSachApplication.class, args);
    }
}
