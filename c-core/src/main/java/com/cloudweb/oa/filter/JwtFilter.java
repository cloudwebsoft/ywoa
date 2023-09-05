package com.cloudweb.oa.filter;

import cn.js.fan.web.Global;
import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IMobileService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.JwtUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.pvg.Privilege;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author : 
 * @date :
 * description : jwt token验证的过滤器
 */
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private IMobileService mobileService;

    @Autowired
    private AuthUtil authUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.getToken(httpServletRequest);
        // 判断token是否有效
        if (StringUtils.hasText(token)) {
            //创建AuthenticationToken
            UsernamePasswordAuthenticationToken authentication = jwtUtil.getAuthentication(token);
            if (authentication != null) {
                // SecurityContextHolder.getContext().setAuthentication(authentication);
                authUtil.doLoginByUserName(httpServletRequest, (String)authentication.getPrincipal());

                // 复制jwt，并重新设置签发时间(为当前时间)和失效时间
                String newJwt = jwtUtil.copyJwt(token);
                httpServletResponse.setHeader(jwtProperties.getHeader(),  newJwt);

                // String userName = (String)authentication.getPrincipal();
            }
            else {
                boolean re = authBySkey(httpServletRequest, httpServletResponse);
                if (!re) {
                    RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/mobile/tokenExpired");
                    rd.forward(httpServletRequest, httpServletResponse);
                    return;
                }
            }

            // 置当前所切换的角色
            if (Config.getInstance().isRoleSwitchable()) {
                Privilege.setCurRoleCode(httpServletRequest.getHeader(ConstUtil.CUR_ROLE_CODE));
            }
            // 置当前所切换的部门
            if (Config.getInstance().isDeptSwitchable()) {
                Privilege.setCurDeptCode(httpServletRequest.getHeader(ConstUtil.CUR_DEPT_CODE));
            }
        } else {
            // log.info("token无效：" + httpServletRequest.getRequestURI());
            authBySkey(httpServletRequest, httpServletResponse);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    /**
     * 通过skey
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    public boolean authBySkey(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // 如果token过期
        String skey = mobileService.getSkey(httpServletRequest);
        if (StringUtils.isEmpty(skey)) {
            return false;
        } else {
            String userName = mobileService.getUserNameBySkey(skey);
            if (userName == null) {
                return false;
            }
            // 登录
            authUtil.doLoginByUserName(httpServletRequest, userName);

            // 重新生成token
            String authToken = jwtUtil.generate(userName);
            httpServletResponse.setHeader(jwtProperties.getHeader(), authToken);
            // 重新生成skey
            skey = mobileService.generateSkey(userName);
            httpServletResponse.setHeader(com.cloudweb.oa.utils.ConstUtil.SKEY, skey);
            return true;
        }
    }
}

