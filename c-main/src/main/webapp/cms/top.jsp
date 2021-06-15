<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
UserDb user = new UserDb();
user = user.getUserDb(privilege.getUser(request));
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE><lt:Label res="res.label.cms.top" key="title"/></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="images/default.css" type=text/css rel=stylesheet>
<META content="MSHTML 6.00.3790.259" name=GENERATOR>
<style type="text/css">
<!--
.style1 {color: #FFFFFF}
-->
</style>
</HEAD>
<BODY leftMargin=0 topMargin=0>
<TABLE cellSpacing=0 cellPadding=0 width="100%" 
background="images/top_bg.png" border=0>
  <TBODY>
  <TR>
    <TD><IMG height=49 src="images/us_logo.gif" 
    width=182></TD>
    <TD style="PADDING-RIGHT: 20px">
      <TABLE width="100%" align=right class=wht>
        <TBODY>
        <TR>
          <TD align="right"><span class="style1"><span class="style2"><%=user.getRealName()%>，&nbsp;</span>&nbsp;&nbsp;&nbsp;</span><IMG height=7 hspace=5 
            src="images/arrow_white.gif" width=6 
            algin="absmiddle"><A href="javascript:location.reload();" 
            target=mainFrame>
            <lt:Label res="res.label.cms.top" key="refresh_cur_page"/></A> <IMG height=7 hspace=5 
            src="images/arrow_white.gif" width=6 
            algin="absmiddle"><A 
            href="main.jsp" 
            target=mainFrame><lt:Label res="res.label.cms.top" key="control_panel_home"/></A> <IMG height=7 hspace=5 
            src="images/arrow_white.gif" width=6 
            algin="absmiddle"><A 
            href="../index.jsp" 
            target=_blank><lt:Label res="res.label.cms.top" key="browse_website"/></A> <IMG height=7 hspace=5 
            src="images/arrow_white.gif" width=6 
            algin="absmiddle"><A 
            href="<%=request.getContextPath()%>/cms/logout.jsp" 
            target=_top><lt:Label res="res.label.cms.top" key="exit_sys"/></A> </TD>
        </TR>
        </TBODY></TABLE></TD></TR></TBODY></TABLE></BODY></HTML>
