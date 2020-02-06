package com.yohm.springcloud.file.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CronScheduled {
    String cron() default ""; //cron表达式 0 0/1 * * * ?

    String desc() default ""; //任务描述
}
