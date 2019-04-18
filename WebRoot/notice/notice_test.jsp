<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.notice.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
Privilege privilege = new Privilege();
boolean isUserPrivValid = privilege.isUserPrivValid(request, "notice") || privilege.isUserPrivValid(request, "notice.dept");
if (!isUserPrivValid) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
boolean isDeptNotice = !privilege.isUserPrivValid(request, "notice") && privilege.isUserPrivValid(request, "notice.dept");
String userName = privilege.getUser(request);
UserDb ud = new UserDb();
ud = ud.getUserDb(userName);
String depts = "";
DeptUserDb du = new DeptUserDb();
java.util.Iterator ir = du.getDeptsOfUser(userName).iterator();
String deptNames = "";
depts = "";
while (ir.hasNext()) {
	DeptDb dd = (DeptDb)ir.next();
	
	if (depts.equals("")) {
		depts = dd.getCode();
		deptNames = dd.getName();
	}
	else {
		depts += "," + dd.getCode();
		deptNames += "," + dd.getName();
	}
	
	// 加入子部门
	Vector v = new Vector();
	dd.getAllChild(v, dd);
	Iterator ir2 = v.iterator();
	while (ir2.hasNext()) {
		DeptDb dd2 = (DeptDb)ir2.next();
		if (("," + depts + ",").indexOf("," + dd2.getCode() + ",")==-1) {
			depts += "," + dd2.getCode();
			deptNames += "," + dd2.getName();
		}
	}
}
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	NoticeMgr am = new NoticeMgr();
	boolean re = false;
	try {
		  re = am.create(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("发布成功！", "notice_list.jsp"));
	} else {
		out.print(StrUtil.Alert_Back("发布失败！"));
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加通知</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../inc/upload.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script>
function getDepts() {
	return form1.depts.value;
}

function getDept() {
	return form1.depts.value;
}

function setUsers(users, userRealNames) {
	form1.receiver.value = users;
	form1.userRealNames.value = userRealNames;
}

function getSelUserNames() {
	return form1.receiver.value;
}

function getSelUserRealNames() {
	return form1.userRealNames.value;
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUsers() {
	showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
}

function openWinUserGroup() {
	openWin("../user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("../user_role_multi_sel.jsp", 520, 400);
}
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><a href="notice_list.jsp"><img src="../images/left/icon-notice.gif"><%=(isDeptNotice?"部门":"公共")%>通知</a></td>
    </tr>
  </tbody>
</table>
<form name="form1" action="notice_add.jsp?op=add" method="post" enctype="multipart/form-data">
<table width="100%" class="tabStyle_1">
<tbody>
  <tr>
    <td colspan="3" class="tabStyle_1_title">发布通知</td>
    </tr>
  <tr>
	<td width="22%">标&nbsp;&nbsp;&nbsp;&nbsp;题</td>
	<td colspan="2"><p>
	  <input name="title" id="title" size="80" maxlength="25">
	  <input type="checkbox" name="isShow" checked="checked" value="1" />
	  显示已查看通知人员
	</td>
  </tr>
  <tr>
    <td id="tdColor">颜&nbsp;&nbsp;&nbsp;&nbsp;色</td>
    <td colspan="2">
        <select id="color" name="color">
          <option value="" style="COLOR: black" selected>标题颜色</option>
          <option style="BACKGROUND: #000088" value="#000088">标题颜色</option>
          <option style="BACKGROUND: #0000ff" value="#0000ff">标题颜色</option>
          <option style="BACKGROUND: #008800" value="#008800">标题颜色</option>
          <option style="BACKGROUND: #008888" value="#008888">标题颜色</option>
          <option style="BACKGROUND: #0088ff" value="#0088ff">标题颜色</option>
          <option style="BACKGROUND: #00a010" value="#00a010">标题颜色</option>
          <option style="BACKGROUND: #1100ff" value="#1100ff">标题颜色</option>
          <option style="BACKGROUND: #111111" value="#111111">标题颜色</option>
          <option style="BACKGROUND: #333333" value="#333333">标题颜色</option>
          <option style="BACKGROUND: #50b000" value="#50b000">标题颜色</option>
          <option style="BACKGROUND: #880000" value="#880000">标题颜色</option>
          <option style="BACKGROUND: #8800ff" value="#8800ff">标题颜色</option>
          <option style="BACKGROUND: #888800" value="#888800">标题颜色</option>
          <option style="BACKGROUND: #888888" value="#888888">标题颜色</option>
          <option style="BACKGROUND: #8888ff" value="#8888ff">标题颜色</option>
          <option style="BACKGROUND: #aa00cc" value="#aa00cc">标题颜色</option>
          <option style="BACKGROUND: #aaaa00" value="#aaaa00">标题颜色</option>
          <option style="BACKGROUND: #ccaa00" value="#ccaa00">标题颜色</option>
          <option style="BACKGROUND: #ff0000" value="#ff0000">标题颜色</option>
          <option style="BACKGROUND: #ff0088" value="#ff0088">标题颜色</option>
          <option style="BACKGROUND: #ff00ff" value="#ff00ff">标题颜色</option>
          <option style="BACKGROUND: #ff8800" value="#ff8800">标题颜色</option>
          <option style="BACKGROUND: #ff0005" value="#ff0005">标题颜色</option>
          <option style="BACKGROUND: #ff88ff" value="#ff88ff">标题颜色</option>
          <option style="BACKGROUND: #ee0005" value="#ee0005">标题颜色</option>
          <option style="BACKGROUND: #ee01ff" value="#ee01ff">标题颜色</option>
          <option style="BACKGROUND: #3388aa" value="#3388aa">标题颜色</option>
          <option style="BACKGROUND: #000000" value="#000000">标题颜色</option>
        </select>
        
        <input type="checkbox" id="level" name="level" value="1" />
        <span title="重要通知将在桌面弹窗显示">重要通知</span>
        <input type="checkbox" id="isBold" name="isBold" value="true" />
        标题加粗
             
</td>
  </tr>
  <tr>
	<td>正&nbsp;&nbsp;&nbsp;&nbsp;文</td>
	<td colspan="2">
      <textarea id="content" name="content"></textarea>
      <script>
        CKEDITOR.replace('content', 
            {
                // skin : 'kama',
                // toolbar : 'Middle'
            });
      </script>    
    </td>
  </tr>

</tbody>
</table>
</form>
<br />
<script language="javascript">
<!--
function openWinPersonUserGroup() {
	openWin("../user/persongroup_user_multi_sel.jsp", 520, 400);
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:500px;dialogHeight:360px;status:no;help:no;')
	if (ret==null)
		return;
	form1.deptNames.value = "";
	form1.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.deptNames.value=="") {
			form1.depts.value += ret[i][0];
			form1.deptNames.value += ret[i][1];
		}
		else {
			form1.depts.value += "," + ret[i][0];
			form1.deptNames.value += "," + ret[i][1];
		}
	}
	if (form1.depts.value=="") {
		form1.depts.value = "<%=DeptDb.ROOTCODE%>";
		form1.deptNames.value = "所有部门";
	}
}
function clearUsers(){
	$("#userRealNames").val("");
	$("#receiver").val("");
}
//-->
</script>
</body>

</html>
