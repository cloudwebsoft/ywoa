<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE>title</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="../../../admin/default.css" type=text/css rel=stylesheet>
<META content="MSHTML 6.00.3790.259" name=GENERATOR>
<style type="text/css">
<!--
.style1 {color: #FFFFFF}
.style2 {font-family: "宋体"}
-->
</style>
</HEAD>
<BODY leftMargin=0 topMargin=0>
<TABLE cellSpacing=0 cellPadding=0 width="100%" 
background="../../../admin/images/top_bg.png" border=0>
  <TBODY>
  <TR>
    <TD width="20%" valign="top"><IMG height=49 src="../../../admin/images/us_logo.gif" 
    width=182></TD>
    <TD width="80%" style="PADDING-RIGHT: 20px">
      <TABLE width="100%" align=right class=wht>
        <TBODY>
        <TR>
          <TD width="60%" rowspan="2" align="center">
			<%
			long id = ParamUtil.getLong(request, "id");
			// if (userName.equals("")) {
			// 	out.print(StrUtil.Alert(SkinUtil.LoadString(request,"res.label.blog.user.frame", "not_name")));
			//	return;
			//}
			GroupDb gd = new GroupDb();
			gd = (GroupDb)gd.getQObjectDb(new Long(id));
			%>
			<font color=white style="font-size:20px"><%=gd.getString("name")%></a>
		  </TD>
          <TD width="40%" align="right"><span class="style1"><span class="style2">&nbsp;</span>&nbsp;&nbsp;&nbsp;</span><IMG height=7 hspace=5 
            src="../../../admin/images/arrow_white.gif" width=6 
            algin="absmiddle"><A href="javascript:location.reload();" 
            target=mainFrame>
            <lt:Label res="res.label.blog.user.frame" key="reflash_page"/></A><IMG height=7 hspace=5 
            src="../../../admin/images/arrow_white.gif" width=6 
            algin="absmiddle"><A 
            href="../../../index.jsp" 
            target=_blank><lt:Label res="res.label.blog.user.frame" key="browse_web"/></A> <IMG height=7 hspace=5 
            src="../../../admin/images/arrow_white.gif" width=6 
            algin="absmiddle"><A 
            href="javascript:window.close()" 
            target=_top><lt:Label res="res.label.blog.user.frame" key="exit_system"/></A> </TD>
        </TR>
        <TR>
          <TD align=right class=wht>
<font color=white><%=Global.AppName%> - 朋友圈后台管理系统</font> </TD>
</TR></TBODY></TABLE></TD></TR></TBODY></TABLE></BODY></HTML>
