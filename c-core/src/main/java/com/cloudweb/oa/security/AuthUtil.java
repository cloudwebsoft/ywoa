package com.cloudweb.oa.security;

import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.pvg.Authorization;
import com.redmoon.oa.pvg.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Component
public class AuthUtil {

    @Autowired
    UserAuthorityCache userAuthorityCache;

    @Autowired
    UserCache userCache;

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
        String unitCode = null;
        if (session != null) {
            unitCode = (String) session.getAttribute(ConstUtil.SESSION_UNITCODE);
        }
        if (unitCode == null) {
            // 如果为空，则根据userName重新赋予unitCode，因为通过jwtfilter后，虽然有了userName，但session中没有unitCode
            String userName = SpringUtil.getUserName();
            if (userName != null) {
                DeptUserDb dud = new DeptUserDb();
                unitCode = dud.getUnitOfUser(userName).getCode();
                if (session != null) {
                    session.setAttribute(ConstUtil.SESSION_UNITCODE, unitCode);
                }
            }
        }
        return unitCode;
    }

    /**
     * 判断用户是否已登录
     * @param request
     * @return
     */
    public boolean isUserLogin(HttpServletRequest request) {
        // 有可能已经通过jwtfilter登录，不一定要有session
        String userName = SpringUtil.getUserName();
        // 当对Spring Security permitAll的路径进行访问时，SprintUtil.getUserName取得的用户名为 ANONYMOUS_USER
        if (userName!=null && !ConstUtil.ANONYMOUS_USER.equals(userName)) {
            return true;
        }

        HttpSession session = request.getSession(true);
        String name = (String) session.getAttribute(ConstUtil.SESSION_NAME);
        return name != null;
    }

    /**
     * 取用户名
     * @return
     */
    public String getUserName() {
        String userName = SpringUtil.getUserName();
        // 当对Spring Security permitAll的路径进行访问时，SprintUtil.getUserName取得的用户名为 ANONYMOUS_USER
        if (ConstUtil.ANONYMOUS_USER.equals(userName)) {
            return null;
        }

        return userName;
    }

    public int getUserId() {
        String userName = getUserName();
        if (userName!=null) {
            User user = userCache.getUser(userName);
            return user.getId();
        }
        else {
            return 0;
        }
    }

    /**
     * 使用户登录，赋予session及通过spring security鉴权
     * @param request
     * @param userName
     */
    public void doLoginByUserName(HttpServletRequest request, String userName) {
        HttpSession session = request.getSession();
        session.setAttribute(Privilege.NAME, userName);

        Authorization auth = new Authorization(userName);
        session.setAttribute(Privilege.SESSION_OA_AUTH, auth);

        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(userName).getCode();
        session.setAttribute(Privilege.UNITCODE, unitCode);

        // spring security 手工认证
        UserDetailsService userDetailsService = SpringUtil.getBean(UserDetailsService.class);
        //根据用户名username加载userDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        //根据userDetails构建新的Authentication,这里使用了
        //PreAuthenticatedAuthenticationToken当然可以用其他token,如 UsernamePasswordAuthenticationToken              
        /*PreAuthenticatedAuthenticationToken authentication =
                new PreAuthenticatedAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());*/

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName, userDetails.getPassword(), userDetails.getAuthorities());
        //设置authentication中details
        authentication.setDetails(new WebAuthenticationDetails(request));
        //存放authentication到SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //在session中存放security context,方便同一个session中控制用户的其他操作
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    }
}
