<%@ page contentType="text/html;charset=utf-8"%>
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
<%
int id = ParamUtil.getInt(request, "id");
KaoqinPrivilege kpvg = new KaoqinPrivilege();
KaoqinDb kd = new KaoqinDb();
kd = kd.getKaoqinDb(id);
if (!kpvg.canAdminKaoqin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
	
String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>考勤</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
<script src="js/datepicker/jquery.datetimepicker.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
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
</head>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<%
if (op.equals("modify")) {
	boolean re = false;
	try {
		KaoqinMgr km = new KaoqinMgr();
		re = km.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "kaoqin_modify.jsp?id=" + id));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

BasicDataMgr bdm = new BasicDataMgr("kaoqin");
%>
<table width="100%" align="center" class="tabStyle_1">
  <tr>
    <td class="tabStyle_1_title" colspan=2>考勤</td>
  </tr>
  <form action="kaoqin_modify.jsp?op=modify&id=<%=id%>" method="post" name="form1" id="form1" onsubmit="">
    <tr>
      <td width="50%" align="right">类型：</td><td align="left"><select name="type">
            <%=bdm.getOptionsStr("type")%>		
      </select>
      </td>
      </td>
      <tr>
      <td width="50%" align="right">去向：</td><td>
	  <select name="direction">
        <%=bdm.getOptionsStr("direction")%>		
      </select>
	  <script>
	  form1.type.value="<%=kd.getType()%>";
	  form1.direction.value="<%=kd.getDirection()%>"
	  </script>
	  </td>
    </tr>
    <tr>
      <td width="50%" align="right">时间：</td>
      <td>
	  <input id="myDate" name="myDate" value="<%=DateUtil.format(kd.getMyDate(), "yyyy-MM-dd HH:mm:ss")%>" size=25 />
	  </td>
    </tr>
    <tr>
      <td width="50%" align="right">事由：</td>
      <td><textarea name="reason" cols="50" rows="8"><%=kd.getReason()%></textarea>      </td>
    </tr>
    <tr>
      <td colspan=2 align="center"><input name="submit" class="btn" type="submit" value="确定" />
        &nbsp;&nbsp;&nbsp;
        <input name="reset" type="button" value="关闭" class="btn" onclick="window.close()" />      </td>
    </tr>
  </form>
</table>
</body>
<script>
$('#myDate').datetimepicker({
	lang:'ch',
	datepicker:true,
	timepicker:true,
	format:'Y-m-d H:i:00',
	step:1
});
</script>
</html>
