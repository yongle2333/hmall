package com.qiu.api.config;

import com.qiu.api.client.fallback.ItemClientFallbackFactory;
import com.qiu.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.bouncycastle.pqc.crypto.newhope.NHOtherInfoGenerator;
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

    //通过匿名内部类进行声明
    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {   //new这个接口就类似匿名内部类
            @Override
            public void apply(RequestTemplate template) {
                Long userId = UserContext.getUser();
                if(userId != null){
                    template.header("user-info",userId.toString());
                }

            }
        };
    }

    @Bean
    public ItemClientFallbackFactory itemClientFallbackFactory(){
        return new ItemClientFallbackFactory();
    }
}
