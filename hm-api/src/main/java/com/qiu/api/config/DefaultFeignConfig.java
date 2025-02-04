package com.qiu.api.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * @author qiu
 * @version 1.0
 */

public class DefaultFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }
}
