<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html>
<html>
<head>
<title>资源回收</title>
<%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/swfobject.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript">
function getSelUserNames() {
	return form1.users.value;
}

function getSelUserRealNames() {
	return form1.userRealNames.value;
}

function openWinUsers() {
	openWin('../user_multi_sel.jsp', 800, 600);
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUserGroup() {
	openWin("../user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("../user_role_multi_sel.jsp", 520, 400);
}

function setUsers(users, userRealNames) {
	form1.users.value = users;
	form1.userRealNames.value = userRealNames;
}
function hideView(){
	$("#pieChart").hide();
}
</script>
<title>请假</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<!-- 
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>

<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
 -->

<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="myconfig" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("renewMsg")) {
	UserSetupDb usd = new UserSetupDb();
	String sql = "select name from users";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql);
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		String userName = rr.getString(1);
		usd = usd.getUserSetupDb(userName);
		sql = "select a.id from oa_message_attach a, message m where a.msgId=m.id and m.receiver=" + StrUtil.sqlstr(userName);
		ResultIterator ri2 = jt.executeQuery(sql);
		com.redmoon.oa.message.Attachment att;
		int count = 0;
		while (ri2.hasNext()) {
			ResultRecord rr2 = (ResultRecord)ri2.next();
			int id = rr2.getInt(1);
			att = new com.redmoon.oa.message.Attachment(id);
			count += att.getSize();
		}
		usd.setMsgSpaceUsed(count);
		usd.save();
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "reclaim.jsp"));
	return;	
}
else if (op.equals("clear")) {
	String[] modules = ParamUtil.getParameters(request, "module");
	if (modules==null) {
		out.print(StrUtil.jAlert_Back("请选择模块！","提示"));
		return;
	}
	String userStrs = ParamUtil.get(request, "users");
	String[] users = StrUtil.split(userStrs, ",");
	if (users==null) {
		out.print(StrUtil.jAlert_Back("请选择用户！","提示"));
		return;
	}
	
	String beginDate = ParamUtil.get(request, "beginDate");
	if ("".equals(beginDate)) {
		out.print(StrUtil.jAlert_Back("请填写开始日期！","提示"));
		return;
	}
	String endDate = ParamUtil.get(request, "endDate");
	String sql = "";
	for (String userName : users) {
		for (String module : modules) {
			if ("plan".equals(module)) {
				sql = "select id from user_plan where userName=" + StrUtil.sqlstr(userName);
				if ("".equals(endDate)) {
					sql += " and myDate<=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
				} else {
					sql += " and myDate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and myDate<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
				}
				JdbcTemplate jt = new JdbcTemplate();
				ResultIterator ri = jt.executeQuery(sql);
				PlanDb pd = new PlanDb();
				while (ri.hasNext()) {
					ResultRecord rr = ri.next();
					int id = rr.getInt(1);
					pd = pd.getPlanDb(id);
					pd.del();
				}
			} else if ("message".equals(module)) {
				sql = "select id from oa_message where receiver=" + StrUtil.sqlstr(userName);
				if (endDate.equals("")) {
					sql += " and send_time<=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
				} else {
					sql += " and send_time>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and send_time<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
				}
				JdbcTemplate jt = new JdbcTemplate();
				ResultIterator ri = jt.executeQuery(sql);
				com.redmoon.oa.message.MessageDb md = new com.redmoon.oa.message.MessageDb();
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord) ri.next();
					int id = rr.getInt(1);
					md = (com.redmoon.oa.message.MessageDb) md.getMessageDb(id);
					md.del();
				}
			} else if ("address".equals(module)) {
				sql = "select id from address where username=" + StrUtil.sqlstr(userName);
				if (endDate.equals("")) {
					sql += " and adddate<=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
				} else {
					sql += " and adddate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and adddate<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
				}
				JdbcTemplate jt = new JdbcTemplate();
				ResultIterator ri = jt.executeQuery(sql);
				com.redmoon.oa.address.AddressDb addr = new com.redmoon.oa.address.AddressDb();
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord) ri.next();
					int id = rr.getInt(1);
					addr = addr.getAddressDb(id);
					addr.del();
				}
			}
		}
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "reclaim.jsp"));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">资源回收</td>
    </tr>
  </tbody>
</table>
<br />
<%
String realPath = Global.getRealPath();
if (realPath.indexOf(":")==1) {
%>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
  <tr>
    <td class="tabStyle_1_title">硬盘空间</td>
  </tr>
  <tr>
    <td align="center">
<%
String version = System.getProperty("java.version");
if (version.contains("1.5") || version.contains("1.4")) {
	out.print("请安装JDK1.6以上版本！");
}
%>
        <div id="pieChart" style="padding-top:15px"></div>    </td>
  </tr>
  <tr>
    <td align="center">
    <input class="btn" type="button" onclick="hideView();jConfirm('您确定要重新计算么？该操作可能将耗费较长时间！','提示',function(r){if(!r){$('#pieChart').show();return;}else{window.location.href='reclaim.jsp?op=renewMsg'}}) " value="重新计算用户邮箱空间" /></td>
  </tr>
</table>
<%}%>
<form id="form1" name="form1" action="?" method="post">
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
  <tr>
    <td colspan="2" class="tabStyle_1_title">清除数据</td>
  </tr>
  <tr>
    <td width="16%" align="center">用户</td>
    <td width="84%" align="left"><textarea name="userRealNames" cols="30" rows="4" readonly="readonly" wrap="yes" id="userRealNames"></textarea>
      <input type="hidden" name="users" />
      <input type="hidden" name="op" value="clear" />
      <br />
      <input name="button" class="btn" type="button" onclick="openWinUsers()" value="选择用户" />
      <!-- <input name="button" class="btn" type="button" onclick="openWinUserGroup()" value="按用户组" />
      <input name="button" class="btn" type="button" onclick="openWinUserRole()" value="按角色" /> --></td>
  </tr>
  <tr>
    <td align="center">模块</td>
    <td align="left">
	<input name="module" type="checkbox" value="fileark" />个人文件柜&nbsp;&nbsp;
	<input name="module" type="checkbox" value="plan" />日程安排&nbsp;&nbsp;
	<input name="module" type="checkbox" value="message" />内部消息&nbsp;&nbsp;
	<input name="module" type="checkbox" value="address" />通讯录
	</td>
  </tr>
  <tr>
    <td align="center">时间</td>
    <td align="left">早于
      <input type="text" id="beginDate" name="beginDate" size="15" />
      <br />
        </td>
  </tr>
  <tr>
    <td colspan="2" align="center">
	<input type="submit" value="确定" class="btn" />
	</td>
    </tr>
</table>
<br />
</form>
</body>  
<script  type="text/javascript">
swfobject.embedSWF(
  "../flash/open-flash-chart.swf", "pieChart",
  "450", "350", "9.0.0", "expressInstall.swf",
  {"data-file":"reclaim_disk.jsp"} );
  $(function(){
	  $('#beginDate').datetimepicker({
	    lang:'ch',
	    datepicker:true,
	    timepicker:false,
	    format:'Y-m-d'
	   });
	})
</script>                                      
</html>                            
  