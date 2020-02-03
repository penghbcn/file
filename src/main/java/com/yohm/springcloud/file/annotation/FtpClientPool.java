package com.yohm.springcloud.file.annotation;

import javax.annotation.Resource;
import java.lang.annotation.*;

@Target({ElementType.FIELD,ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Resource
public @interface FtpClientPool {
}
