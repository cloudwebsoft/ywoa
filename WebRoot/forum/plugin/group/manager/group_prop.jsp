<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<title><lt:Label res="res.label.blog.user.userconfig" key="title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../../../../common.css" type=text/css rel=stylesheet>
<style type="text/css">
<!--
.STYLE1 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_not_login")));
	return;
}

long id = ParamUtil.getLong(request, "id");
if (!GroupPrivilege.isManager(request, id)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(id));
if (gd==null) {
	out.print(StrUtil.Alert_Back("该朋友圈不存在!")); // SkinUtil.LoadString(request,"res.label.blog.user.userconfig", "activate_blog_fail")));
	return;
}

String user = privilege.getUser(request);
if (!GroupPrivilege.isManager(request, id)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	boolean re = false;
	try {
		re = gd.save(application, request, gd, "plugin_group_save");
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "group_prop.jsp?id=" + id));
		return;
	}
}
%>
<br>
<table width="80%" height="170" border="0" align="center" cellpadding="5" cellspacing="0" class="tableframe_gray">
<form id=form1 action="group_prop.jsp?op=modify&id=<%=id%>" method=post enctype="multipart/form-data">
  <tr>
    <td height="24" colspan="2" align="center" bgcolor="#617AA9"><span class="STYLE1"><lt:Label res="res.label.blog.user.userconfig" key="note_edit"/></span></td>
  </tr>
  <tr>
    <td width="25%" height="22">名称</td>
    <td width="75%" height="22"><label>
      <input name="name" type="text" id="title" value="<%=gd.getString("name")%>">
	  <input name="id" type="hidden" value="<%=id%>">
      <input name="msg_count" type="hidden" value="<%=gd.getInt("msg_count")%>">
      <input name="photo_count" type="hidden" value="<%=gd.getInt("photo_count")%>">
      <input name="total_count" type="hidden" value="<%=gd.getInt("total_count")%>">
      <input name="user_count" type="hidden" value="<%=gd.getInt("user_count")%>">
      <input name="recommand_point" type="hidden" value="<%=gd.getInt("recommand_point")%>">
      <input name="color" type="hidden" value="<%=gd.getString("color")%>">
      <input name="is_bold" type="hidden" value="<%=gd.getInt("is_bold")%>">
    </label></td>
  </tr>
  <tr>
    <td height="22">分类</td>
    <td height="22"><select name="catalog_code">
      <%
		  com.redmoon.forum.plugin.group.LeafChildrenCacheMgr lcc = new com.redmoon.forum.plugin.group.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
		  Iterator ir = lcc.getDirList().iterator();
		  while (ir.hasNext()) {
		  	com.redmoon.forum.plugin.group.Leaf lf = (com.redmoon.forum.plugin.group.Leaf)ir.next();
		  %>
      <option value="<%=lf.getCode()%>"><%=lf.getName()%></option>
      <%
		  }
		  %>
    </select>
	<script>
	form1.catalog_code.value = "<%=gd.getString("catalog_code")%>";
	</script>	</td>
  </tr>
  <tr>
    <td height="22">皮肤</td>
    <td height="22">
	<select name="skin_code">
	<%
     PluginMgr pm = new PluginMgr();
     PluginUnit pu = pm.getPluginUnit(GroupUnit.code);
     ir = pu.getSkins().iterator();
     while (ir.hasNext()) {
	 	Skin skin = (Skin) ir.next();
	 %>
	 	<option value="<%=skin.getCode()%>"><%=skin.getName()%></option>
	 <%
	 }
	 String skin_code = StrUtil.getNullStr(gd.getString("skin_code"));
	%>
	</select>
	<script>
	form1.skin_code.value = "<%=skin_code.equals("")?GroupSkin.DEFAULT_SKIN_CODE:skin_code%>";
	</script>	</td>
  </tr>
  <tr>
    <td height="22">是否仅允许成员浏览</td>
    <td height="22"><select name="is_public">
      <option value="1">否</option>
      <option value="0">是</option>
    </select>
      <script>
	form1.is_public.value = "<%=gd.getString("is_public")%>";
	  </script></td>
  </tr>
  <tr>
    <td height="22">是否允许新成员加入</td>
    <td height="22">
	<select name="is_open">
	<option value="1">是</option>
	<option value="0">否</option>
	</select>
	<script>
	form1.is_open.value = "<%=gd.getString("is_open")%>";
	</script>	</td>
    </tr>
  <tr>
    <td height="22">Logo</td>
    <td height="22"><input name="logo" type="file">
      <br>
	  <%if (!gd.getLogoUrl(request).equals("")) {%>
	  <img src="<%=gd.getLogoUrl(request)%>">
	  <%}%>	  </td>
  </tr>
  <tr>
    <td height="22">横幅</td>
    <td height="22"><input name="banner" type="file">
      <br>
	  <%if (!gd.getBannerUrl(request).equals("")) {%>
      <img src="<%=gd.getBannerUrl(request)%>">
	  <%}%>	  </td>
  </tr>
  <tr>
    <td height="22">描述</td>
    <td height="22"><textarea name="description" cols="50" rows="6" id="description"><%=gd.getString("description")%></textarea></td>
  </tr>
  <tr>
    <td height="22">公告</td>
    <td height="22"><textarea name="notice" cols="50" rows="6" id="notice"><%=StrUtil.getNullStr(gd.getString("notice"))%></textarea></td>
  </tr>
  
  <tr>
    <td colspan="2" align="center"><label>
      <input type="submit" name="Submit" value="<lt:Label res="res.label.blog.user.userconfig" key="modify"/>">
      &nbsp;&nbsp;
      <input type="reset" name="Submit2" value="<lt:Label res="res.label.blog.user.userconfig" key="reset"/>">
    </label></td>
  </tr></form>
</table>
<p>&nbsp;</p>
</body>
</html>