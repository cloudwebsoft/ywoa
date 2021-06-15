<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE>标题</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="images/default.css" type=text/css rel=stylesheet>
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
    <TD><IMG height=49 src="images/us_logo.png" 
    width=182></TD>
    <TD style="PADDING-RIGHT: 20px">
      <TABLE width="459" align=right class=wht>
        <TBODY>
        <TR>
          <TD><span class="style1">::&nbsp;&nbsp;&nbsp;&nbsp;</span><IMG height=7 hspace=5 
            src="images/arrow_white.gif" width=6 
            algin="absmiddle"><A href="javascript:location.reload();" 
            target=mainFrame><lt:Label res="res.label.forum.admin.top" key="refresh"/></A> <IMG height=7 hspace=5 
            src="images/arrow_white.gif" width=6 
            algin="absmiddle"><A 
            href="main.jsp" 
            target=mainFrame><lt:Label res="res.label.forum.admin.top" key="control_panel"/></A><IMG height=7 hspace=5 
            src="images/arrow_white.gif" width=6 
            algin="absmiddle"><A 
            href="javascript:window.close()" 
            target=_top><lt:Label res="res.label.forum.admin.top" key="exit"/></A> </TD>
        </TR>
        <TR>
          <TD class=wht align=right>
		  <!--
		  跳转： <SELECT 
            onchange="if (this.options[this.selectedIndex].value!='') window.top.mainFrame.location.href=this.options[this.selectedIndex].value" 
            name=menu> <OPTION style="BACKGROUND-COLOR: #eeeeee" 
              selected>&nbsp;&nbsp;快速通道</OPTION> <OPTION 
              value=../>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;浏览网站</OPTION> 
              <OPTION 
              value=mod_payment.php?action=list>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;支付方式管理</OPTION> 
              <OPTION 
              style="BACKGROUND-COLOR: #eeeeee">&nbsp;&nbsp;商品及商品分类</OPTION> 
              <OPTION 
              value=mod_category.php?action=list>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;商品分类管理</OPTION> 
			</SELECT> -->
        </TD></TR></TBODY></TABLE></TD></TR></TBODY></TABLE></BODY></HTML>
