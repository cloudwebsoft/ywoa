package com.cloudweb.oa.security;

import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Component
public class AuthUtil {
    @Autowired
    UserAuthorityCache userAuthorityCache;

    /**
     * 判断用户是否拥有权限
     * @param request HttpServletRequest
     * @param priv String 权限名称
     * @return boolean
     */
    public boolean isUserPrivValid(HttpServletRequest request, String priv) {
        if (!isUserLogin(request)) {
            return false;
        }
        return isUserPrivValid(SpringUtil.getUserName(), priv);
    }

    public boolean isUserPrivValid(String userName, String priv) {
        if (userName==null) {
            return false;
        }

        // PRIV_READ表示是否登录
        if (priv.equals(ConstUtil.PRIV_READ)) {
            return true;
        }

        // admin 享有所有权限
        if (userName.equals(ConstUtil.USER_ADMIN)) {
            return true;
        }

        List<String> list = userAuthorityCache.getUserAuthorities(userName);

        if (list.contains("admin")) {
            return true;
        }

        return list.contains(priv);
    }

    public String getUserUnitCode() {
        HttpServletRequest request = SpringUtil.getRequest();
        HttpSession session = request.getSession(true);
        return (String) session.getAttribute(ConstUtil.SESSION_UNITCODE);
    }

    public boolean isUserLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String name = (String) session.getAttribute(ConstUtil.SESSION_NAME);
        return name != null;
    }
}
