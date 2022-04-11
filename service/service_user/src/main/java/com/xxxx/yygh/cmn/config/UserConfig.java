package com.xxxx.yygh.cmn.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.xxxx.yygh.user.mapper")
public class UserConfig {
}
