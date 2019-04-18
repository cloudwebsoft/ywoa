package com.cloudweb.oa.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.security.PasswordUtil;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cloudweb.oa.bean.User;
import com.cloudweb.oa.service.UserService;
import com.redmoon.oa.person.InvalidNameException;
import com.redmoon.oa.person.WrongPasswordException;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;

import java.util.Date;

@Controller
public class UserLoginController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    @RequestMapping("/user/loginView")
    public String loginView() {
        return "login";
    }

    @RequestMapping("/user/registerView")
    public String registerView() {
        return "register";
    }

    @ResponseBody
    @RequestMapping(value = "/public/user/login", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String userLogin(HttpServletResponse response) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        boolean re = false;
        try {
            re = pvg.login(request, response);
        } catch (WrongPasswordException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ErrMsgException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return json.toString();
    }

    @RequestMapping("/user/login")
    public ModelAndView login(User user) {
        ModelAndView mav = new ModelAndView();
        user.setName("test");
        user.setPassword("test");

        User u = userService.loginCheck(user);
        if (null == u) {
            mav.setViewName("login");
            mav.addObject("errorMsg", "用户名或密码有误！");
            mav.addObject("tmpName", "Test account");
            return mav;
            //return "";
        } else {
            //mav.setViewName("success");
            mav.setViewName("user/login");
            mav.addObject("user", u);
            mav.addObject("tmpName", "Jaosn");

            return mav;
            // return "user/login";
        }
    }

    @RequestMapping("/user/register")
    public ModelAndView register(User user) {
        ModelAndView mav = new ModelAndView();
        if (userService.register(user)) {
            mav.setViewName("register_succ");
            return mav;
        } else {
            mav.setViewName("register");
            mav.addObject("errorMsg", "用户名已被占用，请更换！！");
            return mav;
        }
    }

    @RequestMapping("/hello")
    public String login(
            @RequestParam(value = "name", required = false, defaultValue = "World")
                    String name, Model model) {

        model.addAttribute("name", name);
        model.addAttribute("tmpName", name);

        return "userLogin";
    }

    /**
     * 重置密码时发送邮件
     * @param userName
     * @param email
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/resetPwdSendLink", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String resetPwd(@RequestParam(value = "userName", required = true) String userName, @RequestParam(value = "email", required = true) String email) {
        JSONObject json = new JSONObject();
        boolean re = true;
        try {
            UserDb user = new UserDb();
            user = user.getUserDb(userName);
            if (!user.isLoaded()) {
                json.put("ret", 0);
                json.put("msg", "帐户不存在");
                return json.toString();
            }
            String userEmail = user.getEmail();
            if (!userEmail.equals(email)) {
                json.put("ret", 0);
                json.put("msg", "用户名或邮箱错误");
                return json.toString();
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
                sendmail.initSession(mailserver, smtp_port, name,
                        pwd_raw, "", isSsl);
            } catch (Exception ex) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
            }

            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            String action = "userName=" + userName + "|" + "email=" + email + "|timestamp=" + new Date().getTime();
            action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
            String t = Global.AppName + "重置密码";
            String c = "请点击下面的链接重置密码";
            c += "<br/>>>&nbsp;<a href='" + Global.getFullRootPath() +
                    "/public/user_reset_pwd_do.jsp?action=" + action +
                    "' target='_blank'>请点击此处</a>";
            c += "<br/>如果点击链接无响应，请拷贝链接至浏览器地址栏中访问：" + Global.getFullRootPath() +
                    "/public/user_reset_pwd_do.jsp?action=" + action;
            sendmail.initMsg(email, senderName, t, c, true);
            sendmail.send();
            sendmail.clear();

            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
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
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }



    /**
     * 重置密码
     * @param pwd
     * @param pwdConfirm
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
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
