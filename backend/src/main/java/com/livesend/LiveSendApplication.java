package com.livesend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LiveSendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveSendApplication.class, args);
    }
}
