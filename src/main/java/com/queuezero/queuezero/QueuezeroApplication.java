
package com.queuezero.queuezero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QueuezeroApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueuezeroApplication.class, args);
    }
}