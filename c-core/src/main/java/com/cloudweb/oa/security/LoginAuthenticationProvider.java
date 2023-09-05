package com.cloudweb.oa.security;

import cn.js.fan.security.Login;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.service.IUserService;
import com.cloudwebsoft.framework.security.AesUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author
 * @create
 * @desc
 **/
@Component("authenticationProvider")
public class LoginAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    @Qualifier(value = "userDetailServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    IUserService userService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    LoginUtil loginUtil;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取表单用户名
        String username = (String) authentication.getPrincipal();
        // 获取表单用户填写的密码
        String password = (String) authentication.getCredentials();
        if (StringUtils.isEmpty(password)) {
            // 在 LoginFailureAuthenticationHandler 中处理此错误信息
            throw new BadCredentialsException("auth_fail"); // "用户名或密码错误";
        }
        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        String pwdAesKey = myconfig.getProperty("pwdAesKey");
        String pwdAesIV = myconfig.getProperty("pwdAesIV");
        try {
            password = AesUtil.aesDecrypt(password, pwdAesKey, pwdAesIV);
        } catch (Exception e) {
            // todo: 如果出错，其实应该抛出异常，否则仍会以接收到的明码尝试登录，或者此处后台配置为可否加密，以方便postman测试
            LogUtil.getLog(getClass()).error(e);
        }

        // 通过登录名取到用户
        com.cloudweb.oa.entity.User user = userService.getUserByLoginName(username);
        if (user == null) {
            throw new BadCredentialsException("auth_fail");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getName());
        if (userDetails == null) {
            throw new BadCredentialsException("auth_fail");
        }

        String password1 = userDetails.getPassword();

        String pwdMD5 = DigestUtils.md5DigestAsHex(password.getBytes());

        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        if (scfg.isDefendBruteforceCracking()) {
            try {
                loginUtil.canLogin(username);
            }
            catch (ErrMsgException e) {
                throw new BadCredentialsException(e.getMessage());
            }
        }

        if (!Objects.equals(pwdMD5, password1)) {
            if (scfg.isDefendBruteforceCracking()) {
                try {
                    loginUtil.afterLoginFailure(username);
                } catch (ErrMsgException e) {
                    throw new BadCredentialsException(e.getMessage());
                }
            }
            // 在 LoginFailureAuthenticationHandler 中处理此错误信息
            throw new BadCredentialsException("auth_fail"); // "用户名或密码错误";
        }

        // username可能为工号，故此处需取 userDetails.getUsername()
        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
