package com.qiu.gateway.filters;

import com.qiu.common.exception.UnauthorizedException;
import com.qiu.gateway.config.AuthProperties;
import com.qiu.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author qiu
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;    //构造函数注入

    private final JwtTool jwtTool;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher(); //判断类似/**的

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取用户信息request
        ServerHttpRequest request = exchange.getRequest();
        //2.判断是否需要做登录拦截
        if(isExclude(request.getPath().toString())){
            // 放行
            return chain.filter(exchange);
        }
        //3.获取token
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        //4.校验并解析token
        if(headers != null && !headers.isEmpty()){
            token = headers.get(0);
        }
        Long userId = null;
        try {
             userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            //拦截，设置响应状态码401
            //拿到响应
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //5.传递用户信息
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        //6.放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(pathPattern, path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
