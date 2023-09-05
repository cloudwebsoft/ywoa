package com.cloudweb.oa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "jwt")
@Configuration
public class JwtProperties {

    /**
     * http请求头
     */
    private String header;

    /**
     * token起始标识
     */
    private String startWith;

    /**
     * token秘钥
     */
    private String secretKey;

    /**
     * token过期时间 单位/秒
     */
    private Long validateSecond;
}
