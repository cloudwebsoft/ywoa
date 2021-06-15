package com.redmoon.oa.workplan;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.util.HtmlUtils;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class WorkPlanMgr {
  Logger logger = Logger.getLogger( WorkPlanMgr.class.getName() );
  private FileUpload fileUpload;


public FileUpload getFileUpload() {
	return fileUpload;
}

public void setFileUpload(FileUpload fileUpload) {
	this.fileUpload = fileUpload;
}

public WorkPlanMgr() {

  }

  public FileUpload doUpload(ServletContext application,
                             HttpServletRequest request) throws
          ErrMsgException {
      fileUpload = new FileUpload();
      fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
      // String[] extnames = {"jpg", "gif", "png"};
      // fileUpload.setValidExtname(extnames);//设置可上传的文件类型

      int ret = 0;
      try {
          ret = fileUpload.doUpload(application, request);
          if (ret != FileUpload.RET_SUCCESS) {
              throw new ErrMsgException("ret=" + ret + " " +
                                        fileUpload.getErrMessage());
          }
      } catch (IOException e) {
          logger.error("doUpload:" + e.getMessage());
      }
      return fileUpload;
  }

  public boolean modify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;
      String errmsg = "";

      FileUpload fu = doUpload(application, request);
      String strId = fu.getFieldValue("id");
      int id = Integer.parseInt(strId);
      String title = StrUtil.getNullStr(fu.getFieldValue("title"));
      if (title.equals(""))
          errmsg += "计划标题不能为空！\\n";
      String content = fu.getFieldValue("content");
      String strBeginDate = fu.getFieldValue("beginDate");
      String strEndDate = fu.getFieldValue("endDate");
      String strTypeId = fu.getFieldValue("typeId");
      int typeId = StrUtil.toInt(strTypeId, -1);
      if (typeId==-1)
    	  errmsg += "请选择类型！\\n";
      String strdepts = fu.getFieldValue("depts");
      // if (strdepts==null || strdepts.equals(""))
      //     errmsg += "请选择部门！\\n";
      String strusers = fu.getFieldValue("users");
      // if (strusers==null || strusers.equals(""))
      //    errmsg += "请选择参与人员！\\n";
      String remark = fu.getFieldValue("remark");
      String principal = fu.getFieldValue("principal");
      if (principal==null || principal.equals(""))
          errmsg += "请选择负责人";

      java.util.Date beginDate = null;
      java.util.Date endDate = null;
      try {
          beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
          endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
      }
      catch (Exception e) {
          logger.error("modify:" + e.getMessage());
      }
      if (beginDate==null || endDate==null)
          errmsg += "请填写开始和结束日期！\\n";

      long projectId = StrUtil.toLong(fu.getFieldValue("projectId"), -1);

      int progress = StrUtil.toInt(fu.getFieldValue("progress"), 0);
      
      int checkStatus = StrUtil.toInt(fu.getFieldValue("checkStatus"), WorkPlanDb.CHECK_STATUS_NOT);

      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      WorkPlanDb wpd = getWorkPlanDb(id);
      wpd.setTitle(title);
      wpd.setContent(content);
      wpd.setBeginDate(beginDate);
      wpd.setEndDate(endDate);
      wpd.setTypeId(typeId);
      wpd.setRemark(remark);
      wpd.setPrincipal(principal);
      String[] depts = strdepts.split(",");
      String[] users = StrUtil.split(strusers, ",");
      String[] principals = StrUtil.split(principal, ",");

      wpd.setDepts(depts);
      wpd.setUsers(users);
      wpd.setProjectId(projectId);
      wpd.setPrincipals(principals);

      wpd.setProgress(progress);
      wpd.setCheckStatus(checkStatus);
      
      re = wpd.save(fu);

      if (re) {
          String strIsMessageRemind = StrUtil.getNullString(fu.getFieldValue(
                  "isMessageRemind"));

          UserMgr um = new UserMgr();
          String userName = privilege.getUser(request);
          String realName = um.getUserDb(userName).getRealName();

          boolean isToMobile = StrUtil.getNullStr(fu.getFieldValue("isToMobile")).equals("true");
          IMessage imsg = null;
          String t = SkinUtil.LoadString(request,
                                         "res.module.workplan",
                                         "msg_workplan_modified_title");
          t = t.replaceFirst("\\$title", title);
          t = t.replaceFirst("\\$user", realName);
          String c = SkinUtil.LoadString(request,
                                         "res.module.workplan",
                                         "msg_workplan_modified_content");

          if (isToMobile && strIsMessageRemind.equals("true")) {
              ProxyFactory proxyFactory = new ProxyFactory(
                      "com.redmoon.oa.message.MessageDb");
              /*
              Advisor adv = new Advisor();
              MobileAfterAdvice mba = new MobileAfterAdvice();
              adv.setAdvice(mba);
              adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
              proxyFactory.addAdvisor(adv);
              */
              imsg = (IMessage) proxyFactory.getProxy();
              c = c.replaceFirst("\\$user", realName);
              c = c.replaceFirst("\\$title", title);
              IMsgUtil imu = SMSFactory.getMsgUtil();

              int len = 0;
              if (users!=null)
            	  len = users.length;
              int plen = 0;
              if (principals!=null)
            	  plen = principals.length;

              for (int i = 0; i < len; i++) {
                  // 不给本人发
                  if (userName.equals(users[i]))
                      continue;

                  imsg.sendSysMsg(users[i], t, c);

                  // 为了防止与负责人重复，所以此处不给负责人发
                  boolean isFound = false;
                  for (int j=0; j<plen; j++) {
                      if (principals[j].equals(users[i])) {
                          isFound = true;
                          break;
                      }
                  }
                  if (!isFound) {
                      UserDb ud = um.getUserDb(users[i]);
                      imu.send(ud, t, MessageDb.SENDER_SYSTEM);
                  }
              }

              for (int j = 0; j < plen; j++) {
                  // 不给本人发
                  if (userName.equals(principals[j]))
                      continue;
                  UserDb ud = um.getUserDb(principals[j]);
                  imu.send(ud, t, MessageDb.SENDER_SYSTEM);
              }
          }
          else if (strIsMessageRemind.equals("true")) {
              // 发送信息
              MessageDb md = new MessageDb();
              c = c.replaceFirst("\\$user", realName);
              c = c.replaceFirst("\\$title", title);
              int len = 0;
              int plen = 0;
              if (users != null){
            	  len = users.length;
              }
              if (users != null){
                plen = principals.length;
              }
              String action = "action=" + MessageDb.ACTION_WORKPLAN + "|id=" + wpd.getId();
              for (int i = 0; i < len; i++) {
                  // md.sendSysMsg(users[i], t, c);
                  // 不给本人发
                  if (userName.equals(users[i]))
                      continue;
                  // 为了防止与负责人重复，所以此处不给负责人发
                  boolean isFound = false;
                  for (int j=0; j<plen; j++) {
                      if (principals[j].equals(users[i])) {
                          isFound = true;
                          break;
                      }
                  }
                  if (!isFound)
                      md.sendSysMsg(users[i], t, c, action);
              }

              for (int j = 0; j < plen; j++) {
                  // 不给本人发
                  if (userName.equals(principals[j]))
                      continue;
                  md.sendSysMsg(principals[j], t, c, action);
              }
          }
      }
      return re;
  }

  public WorkPlanDb getWorkPlanDb(HttpServletRequest request, int id, String op) throws ErrMsgException {
      Privilege privilege = new Privilege();

      if (op.equals("see")) {
          if (!privilege.canUserSeeWorkPlan(request, id))
              throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
      }
      else if (op.equals("edit")) {
          if (!privilege.canUserManageWorkPlan(request, id))
              throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
      }
      else if (op.equals("del")) {
          if (!privilege.canUserManageWorkPlan(request, id))
              throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
      }
      WorkPlanDb wpd = getWorkPlanDb(id);
      return wpd;
  }

  public WorkPlanDb getWorkPlanDb(int id) {
      WorkPlanDb addr = new WorkPlanDb();
      return addr.getWorkPlanDb(id);
  }

  public boolean delAttachment(HttpServletRequest request) throws ErrMsgException {
      int workPlanId = ParamUtil.getInt(request, "workPlanId");
      int attachId = ParamUtil.getInt(request, "attachId");
      WorkPlanDb wpd = getWorkPlanDb(request, workPlanId, "del");
      return wpd.delAttachment(attachId);
  }

  public boolean create(ServletContext application, HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;

      FileUpload fu = doUpload(application, request);
      String errmsg = "";
      String title = HtmlUtils.htmlEscapeDecimal(StrUtil.getNullStr(fu.getFieldValue("title"))) ;
      if (title.equals(""))
          errmsg += "计划标题不能为空！\\n";
      String content = fu.getFieldValue("content");
      String strBeginDate = fu.getFieldValue("beginDate");
      String strEndDate = fu.getFieldValue("endDate");
      String strTypeId = fu.getFieldValue("typeId");
      String flowId = fu.getFieldValue("flowId");
      int typeId = 0;
      if (!StrUtil.isNumeric(strTypeId))
          errmsg += "计划类型不能为空，请添加计划类型！\\n";
      else
          typeId = Integer.parseInt(strTypeId);
      String strdepts = fu.getFieldValue("depts");
      // if (strdepts==null || strdepts.equals(""))
      //     errmsg += "请选择部门！\\n";
      String strusers = fu.getFieldValue("users");
      // if (strusers==null || strusers.equals(""))
      //    errmsg += "请选择参与人员！\\n";
      String remark = fu.getFieldValue("remark");
      String principal = fu.getFieldValue("principal");
      if (principal==null || principal.equals(""))
          errmsg += "请选择负责人";

      java.util.Date beginDate = null;
      java.util.Date endDate = null;
      try {
          beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
          endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
      }
      catch (Exception e) {
          logger.error("create:" + e.getMessage());
      }
      if (beginDate==null || endDate==null)
          errmsg += "请填写开始和结束日期！\\n";
      
      if (DateUtil.compare(beginDate, endDate)==1) {
    	  errmsg += "开始日期不能大于结束日期！\\n";
      }

      long projectId = StrUtil.toLong(fu.getFieldValue("projectId"), -1);

      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      String unitCode = privilege.getUserUnitCode(request);

      WorkPlanDb wpd = new WorkPlanDb();
      wpd.setTitle(title);
      wpd.setContent(content);
      wpd.setBeginDate(beginDate);
      wpd.setEndDate(endDate);
      wpd.setTypeId(typeId);
      wpd.setRemark(remark);
      wpd.setPrincipal(principal);
      wpd.setFlowId(StrUtil.toInt(flowId, 0));
      // logger.info("create: users=" + strusers + " depts=" + strdepts);
      String[] depts = strdepts.split(",");
      String[] users = StrUtil.split(strusers, ",");
      String[] principals = StrUtil.split(principal, ",");

      wpd.setDepts(depts);
      wpd.setUsers(users);
      wpd.setPrincipals(principals);

      wpd.setAuthor(privilege.getUser(request));
      wpd.setProjectId(projectId);
      wpd.setUnitCode(unitCode);
      re = wpd.create(fu);
      if (re) {
    	  // 如果指定了模板，则根据模板生成task
    	  int templateId = StrUtil.toInt(fu.getFieldValue("templateId"), -1);
    	  if (templateId!=-1) {
    		  wpd = wpd.getWorkPlanDb(wpd.getId());
    		  
    		  WorkPlanTaskMgr wptm = new WorkPlanTaskMgr();
    		  wptm.copyGantt(request, wpd, templateId);
    	  }
    	  
          UserMgr um = new UserMgr();
          String userName = privilege.getUser(request);
          String realName = um.getUserDb(userName).getRealName();

          String strIsMessageRemind = StrUtil.getNullString(fu.getFieldValue(
                  "isMessageRemind"));
          boolean isToMobile = StrUtil.getNullStr(fu.getFieldValue("isToMobile")).equals("true");
          IMessage imsg = null;
          String t = SkinUtil.LoadString(request,
                                         "res.module.workplan",
                                         "msg_workplan_create_title");          
          try
          {
	          t = t.replaceFirst("\\$title", title);
	          t = t.replaceFirst("\\$user", realName);
          }
          catch(Exception e)
          {
        	  logger.error("replace:" + e.getMessage());
          }

          String c = SkinUtil.LoadString(request,
                                         "res.module.workplan",
                                         "msg_workplan_create_content");

          if (isToMobile && strIsMessageRemind.equals("true")) {
              ProxyFactory proxyFactory = new ProxyFactory(
                      "com.redmoon.oa.message.MessageDb");
              /*
              Advisor adv = new Advisor();
              MobileAfterAdvice mba = new MobileAfterAdvice();
              adv.setAdvice(mba);
              adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
              proxyFactory.addAdvisor(adv);
              */
              imsg = (IMessage) proxyFactory.getProxy();
              c = c.replaceFirst("\\$user", realName);
              c = c.replaceFirst("\\$title", title);
              IMsgUtil imu = SMSFactory.getMsgUtil();

              int len = 0;
              if (users!=null)
            	  len = users.length;
              for (int i = 0; i < len; i++) {
                  // 不给本人发
                  if (userName.equals(users[i]))
                      continue;

                  imsg.sendSysMsg(users[i], t, c);

                  UserDb ud = um.getUserDb(users[i]);
                  imu.send(ud, t, MessageDb.SENDER_SYSTEM);
              }
              int plen = 0;
              if (principals!=null)
            	  plen = principals.length;
              for (int j = 0; j < plen; j++) {
                  // 不给本人发
                  if (userName.equals(principals[j]))
                      continue;
                  UserDb ud = um.getUserDb(principals[j]);
                  imu.send(ud, t, MessageDb.SENDER_SYSTEM);
              }
          }
          else if (strIsMessageRemind.equals("true")) {
              // 发送信息
              MessageDb md = new MessageDb();
              c = c.replaceFirst("\\$user", realName);
              c = c.replaceFirst("\\$title", title);
              int len = 0;
              int plen = 0;
              if (users != null){
               len = users.length;
              }
              if (principals != null){
            	  plen = principals.length;
              }
              String action = "action=" + MessageDb.ACTION_WORKPLAN + "|id=" + wpd.getId();
              for (int i = 0; i < len; i++) {
                  // 不给本人发
                  if (userName.equals(users[i]))
                      continue;
                  // 为了防止与负责人重复，所以此处不给负责人发
                  boolean isFound = false;
                  for (int j=0; j<plen; j++) {
                      if (principals[j].equals(users[i])) {
                          isFound = true;
                          break;
                      }
                  }
                  if (!isFound)
                      md.sendSysMsg(users[i], t, c, action);
              }
              for (int j = 0; j < plen; j++) {
                  // 不给本人发
                  if (userName.equals(principals[j]))
                      continue;
                  md.sendSysMsg(principals[j], t, c, action);
              }
          }
      }
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "id");
      WorkPlanDb wpd = getWorkPlanDb(request, id, "del");
      if (wpd==null || !wpd.isLoaded())
          throw new ErrMsgException("该项已不存在！");

      return wpd.del();
  }
  
  public boolean remind(HttpServletRequest request) throws ErrMsgException{
	  int workplanId = ParamUtil.getInt(request, "workplanId");
	  return remind(workplanId);
  }
  
  public boolean remind(int workplanId){
	  WorkPlanDb workPlanDb = new WorkPlanDb(workplanId);
	  String workPlanTitle = workPlanDb.getTitle();
	  String[] users = workPlanDb.getUsers();
	  String title = "工作计划“" + workPlanTitle + "”变更提示";
	  String content = "工作计划："+workPlanTitle+"已经变更，请查看明细。";
      String action = "action=" + MessageDb.ACTION_WORKPLAN + "|id=" + workPlanDb.getId();	  
	  for(int i = 0; i < users.length; i ++){
		  sendMsg(users[i],title,content,action);
	  }
	  return true;
  }
  
  /**
   * 发送消息
   * @param title
   * @param content
   */
	private void sendMsg(String name, String title, String content, String action)
  {
		//判断是否需要发送短信
		// boolean isToMobile = config.getBooleanProperty("flowAutoSMSRemind");
		boolean isToMobile = com.redmoon.oa.sms.SMSFactory.isUseSMS();
		
		IMessage imsg = null;
		IMsgUtil imu = null;
		//发送系统消息
		try {
			ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
			imsg = (IMessage) proxyFactory.getProxy();
			imsg.sendSysMsg(name , title, content, action);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//发送短信
		if (isToMobile) {
			UserDb ud = new UserDb();
			ud = ud.getUserDb(name);
			imu = SMSFactory.getMsgUtil();
			try {
				imu.send(ud, title, MessageDb.SENDER_SYSTEM);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
  }
  
}
