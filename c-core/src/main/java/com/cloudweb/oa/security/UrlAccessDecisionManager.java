package com.cloudweb.oa.security;

import cn.js.fan.web.SkinUtil;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author qiumin
 * @create 2019/1/13 14:07
 * @desc
 **/
@Component
public class UrlAccessDecisionManager implements AccessDecisionManager {

    /**
     *
     * @param authentication 当前用户信息，和当前用户的拥有权限信息，即来自于userDetailService里的
     * @param object 即FilterInvocation对象，可以获取httpServletRequest请求对象
     * @param configAttributes  本次访问所需要的权限
     * @throws AccessDeniedException
     * @throws InsufficientAuthenticationException
     */
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        HttpServletRequest request = ((FilterInvocation) object).getRequest();

        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()) {
            ConfigAttribute ca = iterator.next();
            //当前请求需要的权限
            String needRole = ca.getAttribute();
            if ("ROLE_LOGIN".equals(needRole)) {
                // 即匿名用户/未登录
                if (authentication instanceof AnonymousAuthenticationToken) {
                    throw new BadCredentialsException(SkinUtil.LoadString(request, "err_not_login"));
                } else {// 登录但不具有此路径权限
                    break;
                }
            }
            //当前用户所具有的权限
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(needRole)) {
                    return;
                }
            }
        }
        String msg = SkinUtil.LoadString(request, "pvg_invalid");
        throw new AccessDeniedException(msg);
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
