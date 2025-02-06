package com.qiu.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.qiu.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author qiu
 * @version 1.0
 */

public class UserInfoInterceptor implements HandlerInterceptor {
    //controller之前执行，springMVC的拦截器
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取用户登录信息(网关获取的)
        String userInfo = request.getHeader("user-info");
        //判断是否获取用户，有，存入ThreadLocal
        if(StrUtil.isNotBlank(userInfo)){      //isNotBlank，能够判断空格，空字符串（null）之类的
            UserContext.setUser(Long.valueOf(userInfo));
        }
        //放行
        return true;
    }

    //完成用户清理
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
