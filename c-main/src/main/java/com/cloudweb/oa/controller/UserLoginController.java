package com.cloudweb.oa.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.js.fan.util.ParamUtil;
import com.cloudweb.oa.utils.PasswordUtil;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.service.UserRegistService;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.WorkflowUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.ui.SkinMgr;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.redmoon.oa.person.WrongPasswordException;

import cn.js.fan.util.ErrMsgException;

@Controller
public class UserLoginController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserRegistService userRegistService;

    @Autowired
    private IUserService userService;

    @Autowired
    ResponseUtil responseUtil;

    @RequestMapping("/user/loginView")
    public String loginView() {
        return "login";
    }

    @RequestMapping("/user/registerView")
    public String registerView() {
        return "register";
    }

    /**
     * 用于wap/index.jsp及red_bag.jsp登录
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/user/login", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String userLogin(HttpServletResponse response) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        boolean re = false;
        try {
            re = pvg.login(request, response);
            if (re) {
                HttpSession session = request.getSession();
                // spring security 手工认证
                UserDetailsService userDetailsService = SpringUtil.getBean(UserDetailsService.class);
                //根据用户名username加载userDetails
                String name = ParamUtil.get(request, "name");
                IUserService userService = SpringUtil.getBean(IUserService.class);
                User user = userService.getUserByLoginName(name);
                if (user == null) {
                    JSONObject json = new JSONObject();
                    json.put("ret", "0");
                    json.put("msg", "用户名或密码错误！");
                    return json.toString();
                }
                String userName = user.getName();

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
        } catch (WrongPasswordException | ErrMsgException | JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        JSONObject json = new JSONObject();
        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "登录成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "登录失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @RequestMapping("/public/userResetPwd")
    public String userResetPwd(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        return "th/public/user_reset_pwd.html";
    }

    @ResponseBody
    @RequestMapping(value = "/public/getUserEmailObscured", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public com.alibaba.fastjson.JSONObject getUserEmailObscured(@RequestParam(required = true) String userName) {
        User user = userService.getUser(userName);
        String email = "";
        boolean re = true;
        com.alibaba.fastjson.JSONObject json = responseUtil.getResultJson(re);
        if (user!=null) {
            email = user.getEmail();
            if (email != null) {
                int p = email.indexOf("@");
                String sufix = email.substring(p);
                String prefix = email.substring(0, p);
                if (prefix.length() > 2) {
                    email = prefix.substring(0, 2) + "*" + sufix;
                }
            }
        }
        json.put("email", email);

        return json;
    }

    /**
     * 重置密码时发送邮件
     * @param userName
     * @param email
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/resetPwdSendLink", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> resetPwdSendLink(@RequestParam(value = "userName", required = true) String userName, @RequestParam(value = "email", required = true) String email) {
        boolean re = false;
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        if (!user.isLoaded()) {
            return new Result<>(false, "帐户不存在");
        }
        String userEmail = user.getEmail();
        if (!userEmail.equals(email)) {
            return new Result<>(false, "用户名或邮箱错误");
        }

        String charset = Global.getSmtpCharset();
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
        String senderName = StrUtil.GBToUnicode(Global.AppName);
        senderName += "<" + Global.getEmail() + ">";

        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();
        boolean isSsl = Global.isSmtpSSL();
        try {
            sendmail.initSession(mailserver, smtp_port, name, pwd_raw, "", isSsl);
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(ex);
        }

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        String action = "userName=" + userName + "|" + "email=" + email + "|timestamp=" + System.currentTimeMillis();
        action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
        String t = Global.AppName + "重置密码";
        String c = "请点击下面的链接重置密码";
        c += "<br/>>>&nbsp;<a href='" + WorkflowUtil.getJumpUrl(WorkflowUtil.OP_RESET_PWD, action) +
                "' target='_blank'>请点击此处</a>";
        c += "<br/>如果点击链接无响应，请拷贝链接至浏览器地址栏中访问：" + WorkflowUtil.getJumpUrl(WorkflowUtil.OP_RESET_PWD, action);
        /*c += "<br/>>>&nbsp;<a href='" + Global.getFullRootPath() +
                "/public/user_reset_pwd_do.jsp?action=" + action +
                "' target='_blank'>请点击此处</a>";
        c += "<br/>如果点击链接无响应，请拷贝链接至浏览器地址栏中访问：" + Global.getFullRootPath(request) +
                "/public/user_reset_pwd_do.jsp?action=" + action;*/
        sendmail.initMsg(email, senderName, t, c, true);
        re = sendmail.send();
        sendmail.clear();
        return new Result<>(re);
    }

    /**
     * 重置密码
     * @param pwd
     * @param pwdConfirm
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/resetPwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String resetPwd(@RequestParam(value = "userName", required = true) String userName, @RequestParam(value = "pwd", required = true) String pwd, @RequestParam(value = "pwdConfirm", required = true) String pwdConfirm) {
        JSONObject json = new JSONObject();
        boolean re = true;
        try {
            if (!pwd.equals(pwdConfirm)) {
                json.put("ret", 0);
                json.put("msg", "密码与重复密码不一致");
                return json.toString();
            }

            com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
            int minLen = scfg.getIntProperty("password.minLen");
            int maxLen = scfg.getIntProperty("password.maxLen");
            int strenth = scfg.getIntProperty("password.strenth");
            PasswordUtil pu = new PasswordUtil();
            int r = pu.check(pwd, minLen, maxLen, strenth);
            if (r!=1) {
                json.put("ret", 0);
                json.put("msg", pu.getResultDesc(request));
                return json.toString();
            }

            UserDb user = new UserDb();
            user = user.getUserDb(userName);
            if (!user.isLoaded()) {
                json.put("ret", 0);
                json.put("msg", "帐户不存在");
                return json.toString();
            }
            String pwdMD5 = SecurityUtil.MD5(pwd);
            user.setPwdMD5(pwdMD5);
            user.setPwdRaw(pwd);
            re = user.save();
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    /**
     * 重置密码
     * @param pwd
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/checkPwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String checkPwd(@RequestParam(value = "pwd", required = true) String pwd) {
        JSONObject json = new JSONObject();
        try {
            com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
            int minLen = scfg.getIntProperty("password.minLen");
            int maxLen = scfg.getIntProperty("password.maxLen");
            int strenth = scfg.getIntProperty("password.strenth");
            PasswordUtil pu = new PasswordUtil();
            int r = pu.check(pwd, minLen, maxLen, strenth);
            if (r!=1) {
                json.put("ret", 0);
                json.put("msg", pu.getResultDesc(request));
                return json.toString();
            }
            json.put("ret", "1");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }
}
