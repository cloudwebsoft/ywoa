<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_not_login")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>公共共享目录管理-top</title>
<link type="text/css" rel="stylesheet" href="../netdisk/clouddisk.css" />
<script language=JavaScript src='../netdisk/showDialog/jquery.min.js'></script>
<script src="../js/jquery-alerts/jquery.alerts.js"	type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"  type="text/css" media="screen" />
<script src= "../netdisk/js/clouddisk_public_share.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body >
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.netdisk.PublicDirectory"/>
<%
String root_code = ParamUtil.get(request, "root_code");
if (root_code.equals(""))
{
	root_code = "root";
}

PublicLeaf leaf = dir.getLeaf(root_code);
if (leaf==null) {
	root_code = "root";
	leaf = dir.getLeaf(root_code);
}
%>
<Script>
var root_code = "<%=root_code%>";
// 使框架的bottom能得到此root_code
function getRootCode() {
	return root_code;
}

function delPriv(rootCode,code){
	 jConfirm("您确定要删除权限么?","提示",function(r){
			if(!r){
				return;
				}else{
					window.location.href='netdisk_public_dir_top.jsp?op=del&root_code='+encodeURI(rootCode)+'+&delcode='+encodeURI(code);
				}
	 		})
}
</Script>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("AddChild")) {
	boolean re = false;
	try {
		re = dir.AddChild(request);
		if (!re) {
			out.print(StrUtil. jAlert_Back("添加节点失败，请检查编码是否重复！","提示"));
		}
		else {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示","netdisk_public_dir_top.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}

	return;
}
if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "delcode");
	try {
		dir.del(request, delcode);
		out.print(StrUtil.jAlert_Redirect("删除成功！","提示","netdisk_public_dir_top.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
		return;
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
}
if (op.equals("modify")) {
	boolean re = true;
	try {
		re = dir.modifyPublicLeaf(request);
		if (re){
			out.print(StrUtil.jAlert("修改完成!","提示"));
		}else{
			out.print(StrUtil.jAlert("修改失败!","提示"));
		}
			
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	
	
}
if (op.equals("move")) {
	try {
		dir.move(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
}
if (op.equals("removecache")) {
	String curcode = ParamUtil.get(request, "code");
	LeafChildrenCacheMgr.remove(curcode);
	out.print(StrUtil.jAlert(curcode + "缓存已被清除！","提示"));
}

String root_name = leaf.getName();
int root_layer = leaf.getLayer();
String root_description = leaf.getDescription();
boolean isHome = false;
%>
<div class="managePriv">

<%
PublicDirectoryView tv = new PublicDirectoryView(leaf);
tv.list(out);

%>

</div>
</body>
</html>
