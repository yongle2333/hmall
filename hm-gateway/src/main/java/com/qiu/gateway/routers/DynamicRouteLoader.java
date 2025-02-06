package com.qiu.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.PasswordRecipientId;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author qiu
 * @version 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter writer;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct //这个注解表示，在项目创建时，初始化DynamicRouteLoader这个Bean的时候就会执行该方法
    public void initRouteConfigListener() throws NacosException {
        //1.项目启动时，先拉取一次配置，并添加 配置监听
        //拉取的配置信息
        String configInfo = nacosConfigManager.getConfigService()//****!!!****不记得可以看源码
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        //2.监听到配置变更，需要去更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        //3.第一次读取到配置，也需要更新到路由表
        updateConfigInfo(configInfo);
    }


    //更新路由表
    public void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置信息：{}",configInfo);
        //1.解析配置文件，转换为RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //2.删除旧的路由表
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        //3.更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            //3.1更新路由表
            writer.save(Mono.just(routeDefinition)).subscribe();    //响应式编程，要调用subscribe()
                                                                    //通过Mono.just封装
            //3.2记录路由id,便于下一次更新时删除路由
            routeIds.add(routeDefinition.getId());
        }
    }
}
