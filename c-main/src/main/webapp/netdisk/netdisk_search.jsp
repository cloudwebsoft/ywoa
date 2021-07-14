<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="cn.js.fan.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<LINK href="common.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择全局共享目录-菜单</title>
<script>
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table width="100%"  border="0" style="background-image:url(images/bg_left.jpg); background-repeat:no-repeat">
  <tr>
    <td height="73" align="left"><table width="100%"  border="0">
      <tr>
        <td height="33" align="left">&nbsp;</td>
      </tr>
      <tr>
        <td align="left">&nbsp;<img src="images/disk.gif" width="16" height="9">&nbsp;<a href="netdisk_frame.jsp" target="_parent">网络硬盘</a>&nbsp;&nbsp;<img src="images/public_share.gif" width="16" height="16" align="absmiddle">&nbsp;<a href="netdisk_public_share_frame.jsp" target="_parent">公共共享</a></td>
      </tr>
    </table></td>
  </tr>
</table>
<table width="100%"  border="0">
<%
String dirCode = ParamUtil.get(request, "dirCode");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dirCode", dirCode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

%>
<form name="form1" action="dir_list_search.jsp" target="mainFileFrame" method=post>
  <tr>
    <td width="5" align="left">&nbsp;</td>
  <td align="left">要搜索的文件名为：</td>
  </tr>
  <tr>
    <td align="left">&nbsp;</td>
    <td align="left"><input name="fileName" ></td>
  </tr>
  <tr>
    <td align="left">&nbsp;</td>
    <td align="left">搜索范围</td>
  </tr>
  <tr>
    <td align="left">&nbsp;</td>
    <td align="left">
<%
String root_code = privilege.getUser(request);
Directory dir = new Directory();
Leaf leaf = dir.getLeaf(root_code);
DirectoryView dv = new DirectoryView(leaf);
%>
<select name=dirCode>
<option value="">全部目录</option>
<%
dv.ShowDirectoryAsOptions(out, leaf, leaf.getLayer());
%>
</select>
<script>
form1.dirCode.value = "<%=dirCode%>";
</script>
	</td>
  </tr>
  <tr>
    <td align="left">&nbsp;</td>
    <td align="left"><input name="" value="立即搜索" type="submit"></td>
  </tr>
  </form>
</table>
</body>
</html>
