package com.qiu.common.config;

import com.qiu.common.interceptor.UserInfoInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author qiu
 * @version 1.0
 */
@Configuration      //配置类想要生效，需要被spring扫描到
@ConditionalOnClass(DispatcherServlet.class)   //判断一个类是否存在(springMVC核心api)
public class MvcConfig implements WebMvcConfigurer {  //WebMvcConfigurer属于springMVC包
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor());     //不设置拦截路径默认拦截所有路径
    }
}
