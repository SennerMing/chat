package com.musician.club.chat.config;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CommonUtils {

    public static String getTest() {
        log.info("这是我的测试方法，不需要定义Logger属性，即可输出日志文件");
        return "hello,this is my test demo";
    }

}
