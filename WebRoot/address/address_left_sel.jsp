<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.address.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>通讯录框架-左侧</title>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
-->
</style>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String strtype = ParamUtil.get(request, "type");
int type = AddressDb.TYPE_USER;
if (!strtype.equals(""))
	type = Integer.parseInt(strtype);
String mode = ParamUtil.get(request, "mode");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (!mode.equals("show")) {	
	if (type==AddressDb.TYPE_PUBLIC) {
		if (!privilege.isUserPrivValid(request, "admin.address.public")) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
}
%>
<table width="100%"  border="0">
  <tr>
    <td align="left"><div class="tableframe" id="masterdiv">
      <table width="100%" style="cursor:hand" border="0" cellpadding="0" cellspacing="1">
        <tr>
          <td height="25" align="center" class="tabStyle_1_title"><%=(type==AddressDb.TYPE_PUBLIC)?"公共":""%>通讯录分组</td>
        </tr>
      </table>
      <table width="100%" border="0" cellpadding="0" cellspacing="0" class="submenu" id="sub1">
        <tr>
          <td height="22" align="center">
    		<%
			String userName = privilege.getUser(request);
			if (type==AddressDb.TYPE_PUBLIC)
				userName = AddressTypeDb.PUBLIC;
			String unitCode = privilege.getUserUnitCode(request);	
			if (type==AddressDb.TYPE_PUBLIC) {
				unitCode = Leaf.CODE_ROOT;
			}
			
			String root_code = ParamUtil.get(request, "root_code");
			if (root_code.equals("")) {
				root_code = userName;
			}
			
			Directory dir = new Directory();
			Leaf leaf = dir.getLeaf(root_code);
			if (leaf==null) {
				leaf = new Leaf();
				leaf.initRootOfUser(root_code, unitCode);
			}
			DirectoryView tv = new DirectoryView(leaf);
			tv.ListSimple(out, "mainAddressFrame", "address_list_sel.jsp", "type=" + type + "&mode=" + mode, "", "" ); // "tbg1", "tbg1sel");
			%>      
          </td>
        </tr>
		</table>
      </div></td>
  </tr>
</table>
</body>
</html>
