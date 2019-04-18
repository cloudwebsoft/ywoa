<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.kit.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 com.redmoon.forum.ui.*,
				 org.jdom.*,
                 java.util.*"
%>
<title><lt:Label res="res.label.forum.admin.config_theme" key="theme_mgr"/></title>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<LINK href="images/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style>
<body bgcolor="#FFFFFF">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.menu" key="theme"/></td>
  </tr>
</table>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int k = 0;
String code="", name = "", author = "", path = "", banner = "",height = "", filename = "",picSrc ="";
ThemeMgr tm = new ThemeMgr();
Element root = tm.getRootElement();
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
    try {
	  tm.modify(application, request);
	  out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "theme_config.jsp"));
	  return;
	} catch(ErrMsgException e) {
	  out.print(StrUtil.Alert(e.getMessage()));
	}
}

if(op.equals("add")) {
    try {
	  tm.create(application, request);
	  out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "theme_config.jsp"));
	  return;
	} catch(ErrMsgException e) {
	  out.print(StrUtil.Alert(e.getMessage()));
	}
}

if(op.equals("del")) {
    try {
	  tm.del(request);
	  out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "theme_config.jsp"));
	  return;
	} catch(ErrMsgException e) {
	  out.print(StrUtil.Alert(e.getMessage()));
	}
}

List list = root.getChildren();
if (list != null) {
	Iterator ir = list.iterator();
	while (ir.hasNext()) {
			 Element child = (Element) ir.next();
			 code = child.getAttributeValue("code");
			 name =  child.getChildText("name");
			 author = child.getChildText("author");
			 path = child.getChildText("path");
			 banner = child.getChildText("banner");
			 height = child.getChildText("height");
			 picSrc = Global.getRootPath() + path + "/" + banner;
%>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
<tr>
<td width="99%" align="center" valign="top"><br>
<table width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
<FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='theme_config.jsp?op=modify' enctype="MULTIPART/FORM-DATA">
  <tr>
    <td colspan="2" class="thead"><%=name%><input type="hidden" name="code" value="<%=code%>"/></td>
    </tr>
  <tr>
    <td width="11%" bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_theme" key="name"/></td>
    <td width="89%" bgcolor="#F6F6F6"><input type="input" name="name" value="<%=name%>"></td>
  </tr>
  <tr>
    <td colspan="2" bgcolor="#F6F6F6"><img src="<%=picSrc%>" height="<%=height%>"/><input type="hidden" value="<%=Global.getRealPath() + path + "/" + banner%>" name="picSrc" /></td>
    </tr>
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_theme" key="upload_pic"/></td>
    <td bgcolor="#F6F6F6"><input name=filename type=file id="filename"></td>
  </tr>
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_theme" key="height"/></td>
    <td bgcolor="#F6F6F6"><input type="input" name="height" value="<%=height%>"></td>
  </tr>
    <tr>
    <td></td>
    <td>
      <INPUT TYPE=submit value='<lt:Label key="op_modify"/>'>
      &nbsp;&nbsp;
<%if (!code.equals("default")) {%>	  
      <input type=button value='<lt:Label res="res.label.forum.admin.config_theme" key="del"/>' onClick="del('<%=code%>')">
<%}%>	  
    </td>
  </tr>
</FORM> 
</table>
</td>
</tr>
</table>
	<%	
	k++;
   }   
} 
%>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
<tr>
<td width="99%" align="center" valign="top"><FORM METHOD=POST id="form" name="form" ACTION='?op=add' enctype="MULTIPART/FORM-DATA">
<table width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
  <tr>
    <td colspan="2" class="thead"><lt:Label res="res.label.forum.admin.config_theme" key="add_topic"/></td>
    </tr>
  <tr >
    <td width="11%" bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_theme" key="name"/></td>
    <td width="89%" bgcolor="#F6F6F6"><input type="text" name="name"><input type="hidden" name="code" value="<%=RandomSecquenceCreator.getId(20)%>"></td>
  </tr>
  <tr>
    <td colspan="2" bgcolor="#F6F6F6"></td>
    </tr>
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_theme" key="upload_pic"/>    
      ：  			  </td>
    <td bgcolor="#F6F6F6"><input name=filename type=file id="filename"></td>
  </tr>
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_theme" key="height"/></td>
    <td bgcolor="#F6F6F6"><input type="text" name="height"></td>
  </tr>
    <tr>
    <td></td>
    <td>
      <INPUT TYPE=submit value='<lt:Label key="op_add"/>'>
    </td>
  </tr>
</table>
</FORM></td>
</tr>
</table>
<script>
function del(code) {
	window.location.href='theme_config.jsp?op=del&code=' + code;
}
</script>
</body>
</html>
