package com.yohm.springcloud.file.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
public class MybatisConfig {

//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource.druid")
//    public DataSource dataSource(){
//        return new DruidDataSource();
//    }
//
//    @Bean
//    public SqlSessionFactory mySqlSessionFactory(){
//        SqlSessionFactory fac = new SqlSessionFactoryBuilder().
//    }
}
