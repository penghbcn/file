package com.yohm.springcloud.file.component;

import com.yohm.springcloud.file.annotation.CronScheduled;
import com.yohm.springcloud.file.constant.CronConstant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduleJob {

    private String cron = "0 1/0 * * * ?";

    @CronScheduled(desc = "a")
    public void task1() {
        System.out.println(LocalDateTime.now());
    }
}
