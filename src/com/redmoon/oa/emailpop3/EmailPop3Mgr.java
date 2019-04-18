package com.redmoon.oa.emailpop3;

import java.util.Iterator;

import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.*;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class EmailPop3Mgr {
  private EmailPop3Db epd;

  public EmailPop3Mgr() {

  }

  public EmailPop3Db getEmailPop3Db() {
      return epd;
  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;
      String errmsg = "";

      int id = ParamUtil.getInt(request, "id");
      String email = ParamUtil.get(request, "email");
      String emailUser = ParamUtil.get(request, "emailUser");
      String emailPwd = ParamUtil.get(request, "emailPwd");
      String server = ParamUtil.get(request, "server");
      int port = ParamUtil.getInt(request, "port");
      int smtpPort = ParamUtil.getInt(request, "smtpPort");
      String serverPop3 = ParamUtil.get(request, "serverPop3");
      boolean isDelete = ParamUtil.get(request, "isDelete").equals("1");

      if (email.equals(""))
          errmsg += "请输入EMAIL！\\n";
      if (!StrUtil.IsValidEmail(email))
          errmsg += "Email的格式错误！\\n";
      if (emailUser.equals(""))
          errmsg += "请输入用户名！\\n";
      if (emailPwd.equals(""))
          errmsg += "请输入密码！\\n";
      
      boolean isSsl = ParamUtil.getInt(request, "isSsl", 0)==1;
      boolean isDefault = ParamUtil.getInt(request,"isDefault",0)==1;

      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      EmailPop3Db ep = getEmailPop3Db(id);
      ep.setEmail(email);
      ep.setEmailUser(emailUser);
      emailPwd = ThreeDesUtil.encrypt2hex("cloudwebcloudwebcloudweb", emailPwd);
      ep.setEmailPwd(emailPwd);
      ep.setServer(server);
      ep.setPort(port);
      ep.setSmtpPort(smtpPort);
      ep.setServerPop3(serverPop3);
      ep.setDelete(isDelete);
      ep.setSsl(isSsl);
      ep.setDefault(isDefault);
      re = ep.save();
      
      if(isDefault){
    	  String sql = "select id from email_pop3 where id != "+id;
    	  EmailPop3Db emailPop3Db = new EmailPop3Db();
    	  Iterator ir = emailPop3Db.list(sql).iterator();
    	  while(ir.hasNext()){
    		  emailPop3Db = (EmailPop3Db)ir.next();
    		  emailPop3Db.setDefault(false);
    		  emailPop3Db.save();
    	  }
      }
      
      
      return re;
  }

  public EmailPop3Db getEmailPop3Db(int id) {
      EmailPop3Db ep = new EmailPop3Db();
      return ep.getEmailPop3Db(id);
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;

      String errmsg = "";
      String name = privilege.getUser(request);
      String email = ParamUtil.get(request, "email");
      String emailUser = ParamUtil.get(request, "emailUser");
      String emailPwd = ParamUtil.get(request, "emailPwd");
      String server = ParamUtil.get(request, "server");
      int port = ParamUtil.getInt(request, "port");
      int smtpPort = ParamUtil.getInt(request, "smtpPort");
      String serverPop3 = ParamUtil.get(request, "serverPop3");
      boolean isDelete = ParamUtil.get(request, "isDelete").equals("1");
      
      boolean isSsl = ParamUtil.getInt(request, "isSsl", 0)==1;
      boolean isDefault = ParamUtil.getInt(request, "isDefault", 0)==1;

      if (email.equals(""))
          errmsg += "请输入EMAIL！\\n";
      if (!StrUtil.IsValidEmail(email))
          errmsg += "Email的格式错误！\\n";
      if (emailUser.equals(""))
          errmsg += "请输入用户名！\\n";
      if (emailPwd.equals(""))
          errmsg += "请输入密码！\\n";

      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      EmailPop3Db ep = new EmailPop3Db();
      ep.setUserName(name);
      ep.setEmail(email);
      ep.setEmailUser(emailUser);
      ep.setEmailPwd(emailPwd);
      ep.setServer(server);
      ep.setPort(port);
      ep.setSmtpPort(smtpPort);
      ep.setServerPop3(serverPop3);
      ep.setDelete(isDelete);
      ep.setSsl(isSsl);
      ep.setDefault(isDefault);
      epd = ep;

      re = ep.create();
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "id");
      EmailPop3Db kd = getEmailPop3Db(id);
      if (kd==null || !kd.isLoaded())
          throw new ErrMsgException("该项已不存在！");

      Privilege privilege = new Privilege();
      // if (!privilege.getUser(request).equals(kd.getName()))
      //     throw new ErrMsgException(""非法操作！");
      return kd.del();
  }
}
