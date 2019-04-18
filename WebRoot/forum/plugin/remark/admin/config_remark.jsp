<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.remark.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<%@ include file="../../../inc/nocache.jsp" %>
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title></title>
<LINK href="../../../admin/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style>
</head>
<body  bgcolor="#FFFFFF">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

RemarkConfig dc = RemarkConfig.getInstance();

String op = ParamUtil.get(request, "op");

if (op.equals("edit")) {
	String code = ParamUtil.get(request, "code");
	String name = ParamUtil.get(request, "name");
	String url = ParamUtil.get(request, "url");
	
	dc.setProperty("remark", "code", code, "name", name);
	dc.setProperty("remark", "code", code, "url", url);
	dc.cfg = null;
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_remark.jsp"));
	return;
}

if (op.equals("del")) {
	String code = ParamUtil.get(request, "code");
  	Element root = dc.getRoot();
	Iterator ir = root.getChild("remark").getChildren().iterator();
	while (ir.hasNext()) {
        Element child = (Element) ir.next();
        String code2 = child.getAttributeValue("code");
        if (code2.equals(code)) {
            root.getChild("remark").removeContent(child);
            dc.writemodify();
            break;
        }	
	}
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_remark.jsp"));
	return;	
}

if (op.equals("add")) {
	String code = ParamUtil.get(request, "code");
  	Element root = dc.getRoot();	
	Iterator ir = root.getChild("remark").getChildren().iterator();
	while (ir.hasNext()) {
        Element child = (Element) ir.next();
        String code2 = child.getAttributeValue("code");
        if (code2.equals(code)) {
            out.print(StrUtil.Alert_Back("该编码已存在!"));
			return;
        }	
	}
	
	String name = ParamUtil.get(request, "name");
	String url = ParamUtil.get(request, "url");
	
    Element esign = new Element("sign");
    esign.setAttribute(new Attribute("code", code));
    Element ename = new Element("name");
    ename.setText(name);
    esign.addContent(ename);
    Element eurl = new Element("url");
    eurl.setText(url);
    esign.addContent(eurl);

    root.getChild("remark").addContent(esign);
	dc.writemodify();
		
	out.print(StrUtil.Alert_Redirect("操作成功！", "config_remark.jsp"));
	return;
}

%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head>版主评价配置</TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr style="BACKGROUND-COLOR: #fafafa"> 
    <td width="100%" valign="top" bgcolor="#FFFFFF"><table width="100%" border="0" cellpadding="3" cellspacing="0">
          <tr>
            <td width="19%" class="thead">编码&nbsp;</td>
            <td width="20%" class="thead"><img src="../../../admin/images/tl.gif" align="absMiddle" width="10" height="15">名称</td>
            <td width="61%" class="thead"><img src="../../../admin/images/tl.gif" align="absMiddle" width="10" height="15">印章路径</td>
          </tr>
<%	  
  Element root = dc.getRoot();
  Iterator ir = root.getChild("remark").getChildren("sign").iterator();
  int k = 0;
  while (ir.hasNext()) {
  	Element e = (Element)ir.next();
	String code = e.getAttributeValue("code");
	String name = e.getChildText("name");
	String url = e.getChildText("url");
	k++;
%>
	<form name="formDig<%=k%>" action="config_remark.jsp?op=edit" method="post">
            <tr><td><input name="code" value="<%=code%>" readonly></td>
            <td><input name="name" value="<%=name%>"></td>
            <td><input name="url" value="<%=url%>" size="40">
              &nbsp;
              <input name="submit" type="submit" value="确定">
              <input name="submit3" type="button" value="删除" onClick="if (confirm('您确定要删除么？')) window.location.href='config_remark.jsp?op=del&code=<%=code%>'"></td></tr>
	</form>
            <%
  }
%>      
<form name="formDigAdd" action="config_remark.jsp?op=add" method="post">
            <tr>
              <td><input name="code"></td>
              <td><input name="name"></td>
              <td><input name="url" size="40">
              &nbsp;
<input name="submit4" type="submit" value="添加"></td>
            </tr>
		</form>
			</table>
    </td>
  </tr>
</table> 
</body>                                        
</html>                            
  