<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.mail.SendMail"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="javax.mail.internet.MimeUtility"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<%@page import="com.redmoon.oa.flow.DirectoryView"%>
<%@ page import="com.redmoon.oa.sms.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
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
<script>
var curProxy, curProxyUserRealName;
function setPerson(deptCode, deptName, userName, userRealName)
{
	curProxy.value = userName;
	curProxyUserRealName.value = userRealName;
}
function delProxy(formObj){
	o(formObj).action="?op=del";//
	o(formObj).submit();
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String userName = ParamUtil.get(request, "userName");
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
	UserMgr um = new UserMgr();
	
	if (op.equals("refresh")) {
		UserProxyMgr upm = new UserProxyMgr();
		try {
			upm.resetProxy(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Back(str,"提示"));
		return;
	}
	else if (op.equals("del")) {
		int id = ParamUtil.getInt(request, "id");
		UserProxyDb upd = new UserProxyDb();
		upd = (UserProxyDb)upd.getQObjectDb(new Integer(id));
		String proxy = upd.getString("proxy");
		String users = upd.getString("person_related");
		String fromUserName = upd.getString("user_Name");
		String beginDate = upd.getString("begin_date");
		String endDate = upd.getString("end_date");
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		if (upd.del()) {
			//删除代理发送提醒
			//发送邮件提醒
			SendMail sendmail = WorkflowDb.getSendMail();
			UserDb toUserDb = new UserDb();
			toUserDb = toUserDb.getUserDb(proxy);
			UserDb formUserDb = new UserDb();
			formUserDb = formUserDb.getUserDb(fromUserName);
			String fromNick = "";
			String toNick = "";
			try {
				fromNick = MimeUtility.encodeText(formUserDb.getRealName());
				toNick = MimeUtility.encodeText(toUserDb.getRealName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			String fromEmail = Global.getEmail();
      		fromNick = fromNick + "<" + fromEmail + ">";
            // System.out.println(getClass() + "====" + fromNick);
			String toRealName = toUserDb.getRealName();
			//2016年10月21号-2016-10-30  XX不在，工作流程由XXX代理
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String title = cfg.get("proxy_del_msg_title");
			String content = cfg.get("proxy_del_msg_content");
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
			out.print(StrUtil.Alert_Redirect(str, "user_proxy_list.jsp?userName=" + StrUtil.UrlEncode(userName)));
		}
		else
			out.print(StrUtil.jAlert_Back(str,"提示"));
		return;
	}
	else if (op.equals("setProxy")) {
		int id = ParamUtil.getInt(request, "id");
		String proxy = ParamUtil.get(request, "proxy");
		
        if (proxy.equals(userName)) {
        	String str = LocalUtil.LoadString(request,"res.flow.Flow","userAgent");
			out.print(StrUtil.jAlert_Back(str,"提示"));
			return;
		}
		
		String starterParam = ParamUtil.get(request, "starter_param");
		java.util.Date beginDate = DateUtil.parse(ParamUtil.get(request, "proxyBeginDate") + " " + ParamUtil.get(request, "proxyBeginTime"), "yyyy-MM-dd HH:mm:ss");
		java.util.Date endDate = DateUtil.parse(ParamUtil.get(request, "proxyEndDate") + " " + ParamUtil.get(request, "proxyEndTime"), "yyyy-MM-dd HH:mm:ss");
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
		upd = (UserProxyDb)upd.getQObjectDb(new Integer(id));
		boolean re = upd.save(new JdbcTemplate(), new Object[]{userName,proxy,beginDate,endDate,starterParam,new Integer(proxyType),new java.util.Date(), flowCode, new Integer(id)});
		if (re) {
			String users = upd.getString("person_related");
			//发送邮件提醒
			//设置被代理人的邮箱地址
			SendMail sendmail = WorkflowDb.getSendMail();
			UserDb toUserDb = new UserDb();
			toUserDb = toUserDb.getUserDb(proxy);
			UserDb formUserDb = new UserDb();
			formUserDb = formUserDb.getUserDb(userName);
			// System.out.println(getClass() + "=toUser=" + proxy);
			String fromNick = "";
			String toNick = "";
			try {
				fromNick = MimeUtility.encodeText(formUserDb.getRealName());
				toNick = MimeUtility.encodeText(toUserDb.getRealName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			String fromEmail = Global.getEmail();
      		fromNick = fromNick + "<" + fromEmail + ">";
            // System.out.println(getClass() + "====" + fromNick);
			String toRealName = toUserDb.getRealName();
			// 2016年10月21号-2016-10-30  XX不在，工作流程由XXX代理
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String title = cfg.get("proxy_add_msg_title");
			String content = cfg.get("proxy_add_msg_content");
			content = content.replace("%beginDate",ParamUtil.get(request, "proxyBeginDate"));
			content = content.replace("%endDate",ParamUtil.get(request, "proxyEndDate"));			
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
			out.print(StrUtil.jAlert_Redirect(str,"提示", "user_proxy_list.jsp"));
		}
		else {
			String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
			out.print(StrUtil.jAlert_Back(str,"提示"));
		}
		return;
	}
	
	UserDb ud = um.getUserDb(userName);
%>
<%@ include file="user_proxy_inc_menu_top.jsp" %>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<div style="text-align:center; margin-bottom:5px;">
<input id="isUseMsg" name="isUseMsg" type="checkbox" value="true" checked="checked" />
<lt:Label res="res.flow.Flow" key="messageNotice"/>
<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
  <input name="isToMobile" value="true" type="checkbox" checked="checked" />
<lt:Label res="res.flow.Flow" key="smsAlert"/>
<%}%>
<input title="<lt:Label res='res.flow.Flow' key='resetAgents'/>" type="button" class="btn" value='<lt:Label res="res.flow.Flow" key="resetAll"/>' onclick="resetProxy()" />
</div>    
<%
	UserProxyDb upd = new UserProxyDb();
	String sql = "select id from " + upd.getTable().getName() + " where user_name=? order by id desc";
	Iterator ir = upd.list(sql, new Object[]{userName}).iterator();
	int i=0;
	while (ir.hasNext()) {
		upd = (UserProxyDb)ir.next();
		String proxy = StrUtil.getNullStr(upd.getString("proxy"));
		String proxyUserRealName = "";
		if (!proxy.equals("")) {
			proxyUserRealName = ud.getUserDb(proxy).getRealName();
			if (proxyUserRealName == null)
			{
				proxyUserRealName = "";
			}
		}
		i++;
		%>
	  <form id="form<%=i%>" name="form<%=i%>" action="?op=setProxy" method="post">
      <table class="tabStyle_1 percent80" align="center">
        <tr>
          <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="set"/> <%=ud.getRealName()%> <lt:Label res="res.flow.Flow" key="proxy"/>
          </td>
        </tr>
        <tr>
          <td><lt:Label res="res.flow.Flow" key="agent"/>
          <input id="proxyUserRealName<%=i%>" name="proxyUserRealName" value="<%=proxyUserRealName%>" readonly>
          <input class="btn" type="button" onclick="curProxy = o('proxy<%=i%>'); curProxyUserRealName = o('proxyUserRealName<%=i%>'); openWin('../user_sel.jsp', 800, 600)" value='<lt:Label res="res.flow.Flow" key="selectUser"/>' />&nbsp;&nbsp;
          <input id="oldProxy" name="oldProxy" type=hidden value="<%=proxy%>">
          <input id="proxy<%=i%>" name="proxy" type=hidden value="<%=proxy%>">
          <input name="userName" type="hidden" value="<%=userName%>">
		  <input style="display:none" class="btn" type="button" onClick="form1.proxy<%=i%>.value='';form1.proxyUserRealName<%=i%>.value=''" value='<lt:Label res="res.flow.Flow" key="remove"/>'></td>
        </tr>
        <tr>
          <td><lt:Label res="res.flow.Flow" key="flowDepart"/>
          <%
		  String deptNames = "";
		  String depts = "";
		  if (upd.getInt("proxy_type")==UserProxyDb.TYPE_DEPT) {
			  String[] arydepts = StrUtil.split(upd.getString("starter_param"), ",");
			  if (arydepts!=null) {
				int len = arydepts.length;
				DeptDb dd = new DeptDb();
				for (int k=0; k<len; k++) {
					if (depts.equals("")) {
						depts = arydepts[k];
						dd = dd.getDeptDb(arydepts[k]);
						deptNames = dd.getName();
					}
					else {
						depts += "," + arydepts[k];
						dd = dd.getDeptDb(arydepts[k]);
						deptNames += "," + dd.getName();
					}
				}
			  }
		  }
		  %>
            <input id="starter_param_show<%=i%>" name="starter_param_show" value="<%=deptNames%>" />
            <input id="starter_param<%=i%>" name="starter_param" value="<%=depts%>" type="hidden" />
            <input id="id" name="id" type="hidden" value="<%=upd.getInt("id")%>" />
          <input type="button" class="btn" onclick="openWinDepts('starter_param<%=i%>', 'starter_param_show<%=i%>')" value='<lt:Label res="res.flow.Flow" key="choose"/>' />
          <%if (upd.getInt("proxy_type")==UserProxyDb.TYPE_DEFAULT) {%>
          <font color="red">(<lt:Label res="res.flow.Flow" key="department"/>)</font>
          <%}%>          
          </td>
        </tr>
        <tr>
        	<td>
        	<lt:Label res="res.flow.Flow" key="typeProcess"/>
        	<select id="flow_code<%=i %>" name="flow_code" value="<%=upd.getString("flow_code") %>">
        		<%
        		Leaf lf = new Leaf();
        		lf = lf.getLeaf("root");
        		DirectoryView dv = new DirectoryView(lf);
        		dv.ShowDirectoryAsOptionsWithCode(out, lf, 1);
        		%>
        	</select>
        	<script>
        	$(function(){
            	$("#flow_code<%=i %>").find("option[value='<%=upd.getString("flow_code")%>']").attr("selected", true);
        	})
        	</script>
        	</td>
        </tr>
        <tr>
          <td><lt:Label res="res.flow.Flow" key="startTime"/>
          <input id="proxyBeginDate<%=i%>" name="proxyBeginDate" size=20 readonly />
			<script type="text/javascript">
			$(function(){
				$('#proxyBeginDate<%=i%>').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00'});
			})
        </script>
        </td>
        </tr>
        <tr>
          <td align="left"><lt:Label res="res.flow.Flow" key="endTime"/>
            <input id="proxyEndDate<%=i%>" name="proxyEndDate" size=20 readonly />
			<script type="text/javascript">
			$(function(){
				$('#proxyEndDate<%=i%>').datetimepicker({value:'<%=DateUtil.format(upd.getDate("end_date"),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00'});
			})	
        </script>
        </td>
        </tr>
        <tr>
        <td>相关人员&nbsp;
		<%
			String realNames = "";
			if(!StrUtil.getNullStr(upd.getString("person_related")).equals("")){
				String[] ary = StrUtil.split(upd.getString("person_related"), ",");
				  if (ary!=null) {
					int len = ary.length;
					UserDb usrd = new UserDb();
					for (int k=0; k<len; k++) {
						if (realNames.equals("")) {
							usrd = usrd.getUserDb(ary[k]);
							realNames = usrd.getRealName();
						}
						else {
							usrd = usrd.getUserDb(ary[k]);
							realNames += "," + usrd.getRealName();
						}
					}
				  }
			}
			out.print(realNames);
		%></td>
        </tr>	
        <tr>
          <td align="center"><label>&nbsp;&nbsp;
            <input class="btn" type="submit" name="Submit" value='<lt:Label res="res.flow.Flow" key="sure"/>'>&nbsp;&nbsp;&nbsp;&nbsp;
            <input class="btn" type="button" value='<lt:Label res="res.flow.Flow" key="delete"/>' onclick="delProxy('form<%=i%>')">
          </label></td>
        </tr>
        </table>
	</form>
<%}%>
</BODY>
<script>
var curObj, curObjShow;
function getDepts() {
	return curObj.value;
}

function openWinDepts(objId, objShowId) {
	curObj = o(objId);
	curObjShow = o(objShowId);
	
	var deptCode = curObj.value;
	openWin("../admin/organize/organize_dept_sel.jsp?deptCode="+deptCode, 450, 400, "yes");
	curObj.value = "";
	curObjShow.value = "";
	for (var i=0; i<ret.length; i++) {
		if (curObj.value=="") {
			curObj.value += ret[i][0];
			curObjShow.value += ret[i][1];
		}
		else {
			curObj.value += "," + ret[i][0];
			curObjShow.value += "," + ret[i][1];
		}
	}
	
}
function selectNode(code, name) {
	o("starter_param_show1").value = name;
	o("starter_param1").value = code;
}

function resetProxy() {
	jConfirm('<lt:Label res="res.flow.Flow" key="isResetIt"/>','提示',function(r){
		if(!r){return;}
		else{
			var url = 'user_proxy_list.jsp?op=refresh&userName=<%=StrUtil.UrlEncode(userName)%>';
			if (o('isUseMsg')) {
				if (o('isUseMsg').checked)
					url += "&isUseMsg=" + o("isUseMsg").value ;
			}
			if (o('isToMobile')) {
				if (o('isToMobile').checked) {
					url += "&isToMobile=" + o("isToMobile").value;
				}
			}
			window.location.href = url;
		}
	})
}
</script>
</HTML>