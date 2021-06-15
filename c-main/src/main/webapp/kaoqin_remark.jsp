<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "java.util.Calendar"%>
<%@ page import = "java.util.Date"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
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
<jsp:useBean id="kqprivilege" scope="page" class="com.redmoon.oa.kaoqin.KaoqinPrivilege"/>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<%
	int id = ParamUtil.getInt(request, "id");
	KaoqinDb kq = new KaoqinDb();
	kq = kq.getKaoqinDb(id);
	String userName = kq.getName();
	if (!userName.equals(privilege.getUser(request))) {
		if (!(kqprivilege.canAdminUser(request, userName))) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
	
	if (DateUtil.compare(new java.util.Date(), kq.getMyDate())==1) {
		if (!kqprivilege.canAdminUser(request, userName)) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
	
	String op = ParamUtil.get(request, "op");
	
	if(op.equals("save")) {
		boolean re = false;
		String reason = ParamUtil.get(request, "reason");
		try {
			kq.setReason(reason);
			re = kq.save();
		} catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		if(re) {
			out.print(StrUtil.Alert_Redirect("操作成功！", "kaoqin_sxb.jsp?userName=" + StrUtil.UrlEncode(userName)));
		}
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>考勤备注</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
input {
	font-size:12px;
	height:24px;
}
</style>
</head>
<body>
<%@ include file="kaoqin_inc_menu_top.jsp"%>
<br />
<form name="form" action="kaoqin_remark.jsp" method="post">
<table cellpadding="0" cellspacing="0" align="center" class="tabStyle_1 percent60">
	<thead>
	<tr>
		<td align="center"><%=DateUtil.format(kq.getMyDate(), "yyyy-MM-dd HH:mm:ss")%>&nbsp;考勤备注</td>
	</tr>
	</thead>
	<tr>
		<td>
			<input name="id" type="hidden" value="<%=id%>" />
			<input name="op" type="hidden" value="save" />
			<textarea style="width:98%; height:150px" name="reason"><%=kq.getReason()%></textarea>
		</td>
	</tr>
	<tr>
		<td align="center"><input type="submit" class="btn" value="确定" /></td>
	</tr>
</table>
</form>
</body>
</html>
