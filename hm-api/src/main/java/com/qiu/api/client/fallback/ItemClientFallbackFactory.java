package com.qiu.api.client.fallback;

import com.qiu.api.client.ItemClient;
import com.qiu.api.dto.ItemDTO;
import com.qiu.api.dto.OrderDetailDTO;
import com.qiu.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.remoting.RemoteInvocationFailureException;

import java.util.Collection;
import java.util.List;

/**
 * @author qiu
 * @version 1.0
 */
@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {

    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败！",cause);
                return CollUtils.emptyList();   //查不到就返回一个空集合
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品库存失败！",cause);
                throw new RuntimeException(cause);
            }


        };
    }
}
