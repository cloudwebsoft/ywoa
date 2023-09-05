package com.cloudweb.oa.config;

import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.SysProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class CookieSerializerConfig {

    @Autowired
    SysProperties sysProperties;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // serializer.setCookieName("JSESSIONID");
        if (!StrUtil.isEmpty(sysProperties.getDomainName())) {
            serializer.setDomainName(sysProperties.getDomainName());
        }
        // serializer.setCookiePath("/");
        // serializer.setCookieMaxAge(3600);

        /*
        lax: 也是默认属性，与strict类似。但是通过外部链接访问会发送cookie。
        strict：只有在访问相同的站点下才会发送cookie
        none：同站与跨站都会返回cookie，即无限制
        */
        // 浏览器中查看header中提示：缺少使用none所需的secure属性
        // serializer.setSameSite("None");

        // 取消SameSite，设为null后端才能正常登录
        serializer.setSameSite(null);

        // httpOnly 是否允许js读取cookie
        // serializer.setUseHttpOnlyCookie(true);
        // 是否仅仅在https的链接下，才提交cookie
        // serializer.setUseSecureCookie(false);
        return serializer;
    }
}
