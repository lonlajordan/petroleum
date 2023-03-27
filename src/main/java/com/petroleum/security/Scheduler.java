package com.petroleum.security;

import com.petroleum.repositories.LogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class Scheduler {
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final LogRepository logRepository;

    // Delete all logs every first of each month at midnight
    @Scheduled(cron = "@monthly", zone = "GMT+1")
    public void deleteAllLogs(){
        logRepository.deleteAll();
    }

}
