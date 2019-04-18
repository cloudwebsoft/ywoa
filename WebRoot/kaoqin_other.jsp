<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.Calendar"%>
<%@ page import = "java.util.Date"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.StrUtil"%>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.util.DateUtil"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ include file="inc/inc.jsp"%>
<%@ page import="java.text.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>考勤</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("util/jscalendar/calendar-win2k-2.css"); </style>
<script src="inc/common.js"></script>
</head>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<%
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

BasicDataMgr bdm = new BasicDataMgr("kaoqin");

String userName = ParamUtil.get(request, "userName");
if (userName.equals(""))
	userName = privilege.getUser(request);

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
%>
<%@ include file="kaoqin_inc_menu_top.jsp"%>
<script>
$("menu3").className="current";
</script>
<table align="center" class="tabStyle_1 percent60">
  <tr>
    <td class="tabStyle_1_title" colspan="4">考勤</td>
  </tr>
  <form action="kaoqin.jsp?op=add" method="post" name="form1" id="form1" onsubmit="">
    <tr>
      <td align="center">类型 </td>
      <td><%
	  String opts = bdm.getOptionsStr("type");
	  opts = opts.replaceAll("<option value='考勤' selected>考勤</option>", "");
	  %>
          <select name="type">
            <%=opts%>
          </select>
		  <input name="userName" value="<%=userName%>" type="hidden" />
      </td>
      <td width="15%" align="center">去向</td>
      <td><select name="direction">
        <%=bdm.getOptionsStr("direction")%>
      </select></td>
    </tr>
    <tr>
      <td width="15%" align="center">事由</td>
      <td colspan="3"><textarea name="reason" cols="50" rows="8"></textarea></td>
    </tr>
    <tr>
      <td colspan="4" align="center">
	  	<input class="btn" name="submit" type="submit" value="发送" />
        &nbsp;&nbsp;&nbsp;
        <input class="btn" name="reset" type="reset" value="取消" />
      </td>
    </tr>
  </form>
</table>
</body>
</html>
