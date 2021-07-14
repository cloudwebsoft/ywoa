<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择到期时间</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<script src="../inc/livevalidation_standalone.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>

<script>
var curObj, curObjShow
function setPerson(deptCode, deptName, userName, userRealName)
{
	curObj.value = userName;
	curObjShow.value = userRealName;
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">选择到期时间</td>
    </tr>
  </tbody>
</table>
<%
String dt = ParamUtil.get(request, "dt");
%>
<table width="100%" align="center" cellPadding="0" cellSpacing="0" class="tabStyle_1 percent80" id="mapTable" style="padding:0px; margin:0px;">
  <tbody>
    <tr>
      <td height="28" class="tabStyle_1_title">请选择</td>
    </tr>
    <tr>
      <td align="center"><input id="mydate" name="mydate" size=20 value="<%=dt%>" readonly>
        <script type="text/javascript">
      Calendar.setup({
          inputField     :    "mydate",      // id of the input field
          ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
          showsTime      :    true,            // will display a time selector
          singleClick    :    true,           // double-click mode
          align          :    "",           	// alignment (defaults to "Bl")		
          step           :    1                // show all years in drop-down boxes (instead of every other year as default)
      });
      </script>      </td>
    </tr>
    <tr>
      <td align="center">
      <input class="btn" type="button" value="确定" onclick="setExpireDate()" />
      &nbsp;&nbsp;
      <input class="btn" type="button" value="取消" onclick="window.close()" />
      </td>
    </tr>
  </tbody>
</table>
</form>
</body>
<script>
function setExpireDate() {
	if (o("myDate").value=="") {
		alert("请选择日期！");
		return;
	}
	dialogArguments.setExpireDate(o("myDate").value);
	window.close();
}
</script>
</html>