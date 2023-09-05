<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE>日程安排</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script>
function findObj(theObj, theDoc){
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

var GetDate=""; 
function SelectDate(ObjName,FormatDate){
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = openWin("../util/calendar/calendar.htm", 286, 221);
}

function SetDate(){ 
	findObj(ObjName).value = GetDate;
}

function SelectDateTime(objName){
	var dt = openWin("../util/calendar/time.jsp", 266, 185);
	if (dt!=null)
		findObj(objName).value = dt;
}

function setPerson(userName, userRealName){
	form1.proxy.value = userName;
	form1.proxyUserRealName.value = userRealName;
}
</script>
</HEAD>
<BODY>
<div class="spacerH"></div>
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
	
	String op = ParamUtil.get(request, "op");
	UserMgr um = new UserMgr();
	
	if (op.equals("setProxy")) {
		/*
		boolean re = false;
		try {
			re = um.setProxy(request, userName);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
		if (re)
			out.print(StrUtil.Alert("设置代理成功！"));
		*/
	}
	
	UserDb ud = um.getUserDb(userName);
	String proxy = ud.getProxy();
	String proxyUserRealName = "";
	if (!proxy.equals("")) {
		proxyUserRealName = ud.getUserDb(proxy).getRealName();
	}
%>
	  <form id=form1 name=form1 action="?op=setProxy" method="post">
      <table class="tabStyle_1 percent80" align="center">
        <tr>
          <td class="tabStyle_1_title">设置 <%=ud.getRealName()%> 代理职位</td>
        </tr>
        <tr>
          <td>代&nbsp;&nbsp;理&nbsp;&nbsp;人
            <input name=proxyUserRealName value="<%=proxyUserRealName%>" readonly>
          <a href="#" onClick="javascript:openWin('user_sel.jsp',800,600)">选择用户</a>&nbsp;&nbsp;
          <input name=proxy type=hidden value="<%=ud.getProxy()%>">
          <input name="userName" type="hidden" value="<%=userName%>">
		<input type="button" class="btn" onClick="form1.proxy.value='';form1.proxyUserRealName.value=''" value="清除">		  </td>
        </tr>
        <tr>
          <td>开始时间
          <input name=proxyBeginDate size=10 readonly value="<%=DateUtil.format(ud.getProxyBeginDate(), "yyyy-MM-dd")%>">
          <img src="../images/form/calendar.gif" width="26" height="26" align=absMiddle style="cursor:hand" onClick="SelectDate('proxyBeginDate','yyyy-mm-dd')">
          <input style="WIDTH: 50px" value="<%=DateUtil.format(ud.getProxyBeginDate(), "HH:mm:ss")%>" name="proxyBeginTime" size="20">
&nbsp;<img style="CURSOR: hand" onClick="SelectDateTime('proxyBeginTime')" src="../images/form/clock.gif" align="absMiddle" width="18" height="18"></td>
        </tr>
        <tr>
          <td align="left">结束时间
            <input name=proxyEndDate size=10 readonly value="<%=DateUtil.format(ud.getProxyEndDate(), "yyyy-MM-dd")%>">
            <img src="../images/form/calendar.gif" width="26" height="26" align=absMiddle style="cursor:hand" onClick="SelectDate('proxyEndDate','yyyy-mm-dd')">
            <input style="WIDTH: 50px" value="<%=DateUtil.format(ud.getProxyEndDate(), "HH:mm:ss")%>" name="proxyEndTime" size="20">
&nbsp;<img style="CURSOR: hand" onClick="SelectDateTime('proxyEndTime')" src="../images/form/clock.gif" align="absMiddle" width="18" height="18"></td>
        </tr>
        <tr>
          <td align="center"><label>
            <input name="isUseMsg" type="checkbox" value="true" checked>短消息通知&nbsp;&nbsp;
            <input class="btn" type="submit" name="Submit" value="设置">
          &nbsp;&nbsp;&nbsp;</label></td>
        </tr>
        <tr>
          <td align="center">注意：将代理人设置为空将清除代理</td>
        </tr>
      </table>
</form>
</BODY>
</HTML>
