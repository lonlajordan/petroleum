package com.petroleum.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableScheduling
public class Scheduler {

    // Archive application files every day at midnight
    @Scheduled(cron = "@daily", zone = "GMT+1")
    public void archive(){
    }

}
