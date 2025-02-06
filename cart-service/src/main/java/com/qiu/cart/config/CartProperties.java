package com.qiu.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author qiu
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "qiu.cart")
public class CartProperties {
    private Integer maxItems;

}
