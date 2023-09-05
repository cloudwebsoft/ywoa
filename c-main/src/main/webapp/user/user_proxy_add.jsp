<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.mail.SendMail"%>
<%@ page import="javax.mail.internet.MimeUtility"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<%@page import="com.redmoon.oa.flow.DirectoryView"%>
<%@ page import="com.redmoon.oa.sms.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<%String users = ParamUtil.get(request, "users");%>
<!DOCTYPE html>
<html>
<HEAD><TITLE>设置代理</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../inc/livevalidation_standalone.js"></script>
<script>
var timeObjName;
function SelectDateTime(objName) {
	 openWin("../util/calendar/time.jsp",350, 185);
	 timeObjName = objName;
}
function setDateTime(val) {
    o(timeObjName).value = val;
}
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function openWinUsers() {
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	openWin('../user_multi_sel.jsp?unitCode=root', 800, 600);
}
function setUsers(users, userRealNames) {
	form1.users.value = users;
	form1.userRealNames.value = userRealNames;
}
function setPerson(deptCode, deptName, userName, userRealName)
{
	form1.proxy.value = userName;
	form1.proxyUserRealName.value = userRealName;
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String userName = ParamUtil.get(request, "userName");
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;	
	}
	
	if (userName.equals(""))
		userName = privilege.getUser(request);
		
	if (!userName.equals(privilege.getUser(request))) {
		if (!privilege.canAdminUser(request, userName)) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","permissionsIllegal");
			out.print(SkinUtil.makeErrMsg(request, str));
			return;
		}
	}
	
	String op = ParamUtil.get(request, "op");
	if (op.equals("add")) {
		String proxy = ParamUtil.get(request, "proxy");
        if (proxy.equals(userName)) {
        	String str = LocalUtil.LoadString(request,"res.flow.Flow","userAgent");
			out.print(StrUtil.jAlert_Back(str,"提示"));
			return;
		}
				
		String starterParam = ParamUtil.get(request, "starter_param");
		java.util.Date beginDate = DateUtil.parse(ParamUtil.get(request, "proxyBeginDate"), "yyyy-MM-dd HH:mm:ss");
		java.util.Date endDate = DateUtil.parse(ParamUtil.get(request, "proxyEndDate"), "yyyy-MM-dd HH:mm:ss");
		String beginDateStr = ParamUtil.get(request, "proxyBeginDate");
		String endDateStr =  ParamUtil.get(request, "proxyEndDate");
		int proxyType = UserProxyDb.TYPE_DEFAULT;
		if (!starterParam.equals(""))
			proxyType = UserProxyDb.TYPE_DEPT;
		
		if (beginDate==null || endDate==null) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","dateFormatError");
			out.print(StrUtil.jAlert_Back(str,"提示"));
			return;
		}
		
		if (DateUtil.compare(beginDate, endDate)==1) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","startDateNotEndDate");
			out.print(StrUtil.jAlert_Back(str,"提示"));
			return;
		}
		
		String flowCode = ParamUtil.get(request, "flow_code");
			
		UserProxyDb upd = new UserProxyDb();
		boolean re = upd.create(new JdbcTemplate(), new Object[]{userName,proxy,beginDate,endDate,starterParam,new Integer(proxyType),new java.util.Date(), flowCode, users});
		if (re) {
			//发送邮件提醒
			//设置被代理人的邮箱地址
			SendMail sendmail = WorkflowDb.getSendMail();
			UserDb toUserDb = new UserDb();
			toUserDb = toUserDb.getUserDb(proxy);
			//设置设置代理人的邮箱地址
			UserDb formUserDb = new UserDb();
			formUserDb = formUserDb.getUserDb(userName);
			String fromNick = "";
			String toNick = "";
			try {
				fromNick = MimeUtility.encodeText(formUserDb.getRealName());
				toNick = MimeUtility.encodeText(toUserDb.getRealName());
			} catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
			}
			String fromEmail = Global.getEmail();
      		fromNick = fromNick + "<" + fromEmail + ">";
			String toRealName = toUserDb.getRealName();
			//2016年10月21号-2016-10-30  XX不在，工作流程由XXX代理
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String title = cfg.get("proxy_add_msg_title");
			String content = cfg.get("proxy_add_msg_content");
			content = content.replace("%beginDate",beginDateStr);
			content = content.replace("%endDate",endDateStr);
			content = content.replace("%fromUser",formUserDb.getRealName());
			content = content.replace("%proxy",toUserDb.getRealName());
			
			MessageDb md = new MessageDb();
			if (SMSFactory.isUseSMS()) {
	            IMsgUtil imu = SMSFactory.getMsgUtil();
	            if (imu != null) {
	                try {
	                    imu.send(toUserDb, content, MessageDb.SENDER_SYSTEM);
	                } catch (ErrMsgException ex1) {
	                    ex1.printStackTrace();
	                }
	            }
	        }
	        // 发送信息
	        try {
	            md.sendSysMsg(toUserDb.getName(), title, content);
	        } catch (ErrMsgException ex2) {
	            ex2.printStackTrace();
	        }
	        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
	        if (flowNotifyByEmail) {			
				sendmail.initMsg(toUserDb.getEmail(), fromNick, title, content, true);
				sendmail.send();
				sendmail.clear();
			}
			try {
				if(!users.equals("")){
					//如果相关人员不为空的话，可以实例化后，发送邮件
					String ary[] = users.split(",");
					if(ary != null){
						for(int i=0;i<ary.length;i++){
							toUserDb = toUserDb.getUserDb(ary[i]);
							toNick = MimeUtility.encodeText(toUserDb.getRealName());
							if (SMSFactory.isUseSMS()) {
					            IMsgUtil imu = SMSFactory.getMsgUtil();
					            if (imu != null) {
					                try {
					                    imu.send(toUserDb, content, MessageDb.SENDER_SYSTEM);
					                } catch (ErrMsgException ex1) {
					                    ex1.printStackTrace();
					                }
					            }
					        }
					        // 发送信息
					        try {
					            md.sendSysMsg(toUserDb.getName(), title, content);
					        } catch (ErrMsgException ex2) {
					            ex2.printStackTrace();
					        }							
							if (flowNotifyByEmail) {
								if(!StrUtil.getNullStr(toUserDb.getEmail()).equals("")){
									sendmail.initMsg(toUserDb.getEmail(), fromNick, title, content, true);
									sendmail.send();
									sendmail.clear();
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
			String str = LocalUtil.LoadString(request,"res.common","info_op_success");
			out.print(StrUtil.jAlert_Redirect(str,"提示", "user_proxy_list.jsp?userName=" + StrUtil.UrlEncode(userName)));
		}
		else {
			String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
			out.print(StrUtil.jAlert_Back(str,"提示"));
		}
		return;
	}
	
	UserMgr um = new UserMgr();	
	UserDb ud = um.getUserDb(userName);
%>
<%@ include file="user_proxy_inc_menu_top.jsp" %>
	<script>
    o("menu2").className="current";
    </script>
    <div class="spacerH"></div>    
	  <form id=form1 name=form1 action="?op=add" method="post">
      <table class="tabStyle_1 percent80" align="center">
        <tr>
          <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="set"/> <%=ud.getRealName()%> <lt:Label res="res.flow.Flow" key="proxy"/></td>
        </tr>
        <tr>
          <td><lt:Label res="res.flow.Flow" key="agent"/>
          <input id="proxyUserRealName" name=proxyUserRealName readonly />
          <input class="btn" type="button" onclick="openWin('../user_sel.jsp', 800, 600)" value='<lt:Label res="res.flow.Flow" key="selectUser"/>' />&nbsp;&nbsp;
          <input id="proxy" name=proxy type=hidden />
          <input name="userName" type="hidden" value="<%=userName%>" />
            <input class="btn" type="button" onclick="form1.proxy.value='';form1.proxyUserRealName.value=''" value='<lt:Label res="res.flow.Flow" key="remove"/>'>
            &nbsp;(<lt:Label res="res.flow.Flow" key="watch"/>：<lt:Label res="res.flow.Flow" key="nullUser"/>) 
			<script>
              var proxyBeginDate = new LiveValidation('proxyUserRealName');
              proxyBeginDate.add(Validate.Presence);		
            </script>         
        </td>
        </tr>
        <tr>
          <td><lt:Label res="res.flow.Flow" key="flowDepart"/>
          <input id="starter_param_show" name="starter_param_show" />
          <input id="starter_param" name="starter_param" type="hidden" />
          <input type="button" class="btn" onclick="openWinDepts()" value='<lt:Label res="res.flow.Flow" key="choose"/>' />
          </td>
        </tr>
        <tr>
        	<td>
        	<lt:Label res="res.flow.Flow" key="typeProcess"/>
        	<select id="flow_code" name="flow_code">
        		<%
        		Leaf lf = new Leaf();
        		lf = lf.getLeaf("root");
        		DirectoryView dv = new DirectoryView(lf);
        		dv.ShowDirectoryAsOptionsWithCode(out, lf, 1);
        		%>
        	</select></td>
        </tr>
        <tr>
          <td><lt:Label res="res.flow.Flow" key="startTime"/>
          <input id="proxyBeginDate" name="proxyBeginDate" size=20 readonly>
			<script>
              var proxyBeginDate = new LiveValidation('proxyBeginDate');
              proxyBeginDate.add(Validate.Presence);		
            </script>             
            </td>
        </tr>
        <tr>
          <td align="left"><lt:Label res="res.flow.Flow" key="endTime"/>
            <input id="proxyEndDate" name="proxyEndDate" size=20 readonly>
			<script>
              var proxyEndDate = new LiveValidation('proxyEndDate');
              proxyEndDate.add(Validate.Presence);		
            </script>            
            </td>
        </tr>
        <tr>
          <td align="left">相关人员
         <input name="users" id="users" type="hidden">
         <textarea name="userRealNames" title="提醒相关人员" cols="60" rows="5" readOnly wrap="yes" id="userRealNames"></textarea>
         <input class="btn" title="添加人员" onClick="openWinUsers()" type="button" value="添 加" name="button">
         <input class="btn" title="清空人员" onClick="o('users').value='';o('userRealNames').value=''" type="button" value="清 空" name="button">
          </td>
        </tr>
        <tr style="display:none">
          <td align="left"><input name="isUseMsg" type="checkbox" value="true" checked />
          <lt:Label res="res.flow.Flow" key="messageNotice"/></td>
        </tr>
        <tr>
          <td align="center"><label>&nbsp;&nbsp;
            <input class="btn" type="submit" name="Submit" value='<lt:Label res="res.flow.Flow" key="sure"/>'>
          &nbsp;&nbsp;&nbsp;</label></td>
        </tr>
        <tr style="display:none">
          <td align="center"><lt:Label res="res.flow.Flow" key="proxyFlow"/></td>
        </tr>
      </table>
	</form>
</BODY>
<script>
	$(function(){
		$('#proxyBeginDate').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00'});
		$('#proxyEndDate').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00'});
	})
function getDepts() {
	return o("starter_param").value;
}
function selectNode(code, name) {
	o("starter_param_show").value = name;
	o("starter_param").value = code;
}
function openWinDepts() {
	var deptCode = o("starter_param").value;
	openWin("../admin/organize/organize_dept_sel.jsp?deptCode="+deptCode, 450, 400, "yes");
	o("starter_param").value = "";
	o("starter_param_show").value = "";
	for (var i=0; i<ret.length; i++) {
		if (o("starter_param").value=="") {
			o("starter_param").value += ret[i][0];
			o("starter_param_show").value += ret[i][1];
		}
		else {
			o("starter_param").value += "," + ret[i][0];
			o("starter_param_show").value += "," + ret[i][1];
		}
	}
	
}
</script>
</HTML>
