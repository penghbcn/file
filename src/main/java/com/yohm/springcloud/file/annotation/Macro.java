package com.yohm.springcloud.file.annotation;

import java.lang.annotation.*;

/**
 * 用于在mybatis的映射接口上定义宏，适用于纯注解的使用方式。
 * <p>
 * 一个映射接口上可以标记多个@Macro，jdk8支持多个注解实例
 *
 * @author gaohang
 */
@Repeatable(Macros.class)
public @interface Macro {

    /**
     * 宏的名称
     */
    String name();

    /**
     * 用于做替换的文本内容，使用数组可以避免因内容太长导致字符串非常难以阅读，拆分成多个string即可
     */
    String[] content();
}