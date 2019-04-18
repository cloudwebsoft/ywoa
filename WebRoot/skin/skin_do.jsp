<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String op = ParamUtil.get(request, "op");
if(op.equals("change")) {
	UserSetupMgr usm = new UserSetupMgr();
	try {
		usm.modifySkin(request, response);
	}
	catch (ErrMsgException e) {
		out.println(fchar.Alert_Back(e.getMessage()));
	}
	return;
}
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<body>
</body>
</html>
