<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.util.TwoDimensionCode"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>系统变量</title>
<%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}
</script>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="myconfig" scope="page" class="com.redmoon.forum.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin";
if (!privilege.isUserPrivValid(request, priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="config_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<%
XMLConfig cfg = new XMLConfig("config_cws.xml", false, "utf-8");
String op = ParamUtil.get(request, "op");
if (op.equals("setup")) {
	Enumeration e = request.getParameterNames();
	while (e.hasMoreElements()) {
		String fieldName = (String)e.nextElement();
		if (fieldName.startsWith("Application")) {
			String value = ParamUtil.get(request, fieldName);
			cfg.set(fieldName, value);
		}
	}
	cfg.writemodify();
	Global.init();
	TwoDimensionCode.generate2DCodeByMobileClient();//生成手机端二维码
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "config_all.jsp"));
	return;
}
%>
<form action="?op=setup" method="post" name="form1" id="form1">
<table class="tabStyle_1 percent80" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" height="24" colspan="2" align="center">系统变量</td>
    </tr>
    <tr>
      <td width="36%" height="24" align="right">服&nbsp;务&nbsp;器：</td>
      <td width="64%" align="left"><input name="Application.server" value="<%=Global.server%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">端口：</td>
      <td align="left"><input name="Application.port" value="<%=Global.port%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">描述：</td>
      <td align="left"><input type="text" name="Application.desc" value="<%=Global.desc%>"/>
        ( 描述：用在RSS中 )</td>
    </tr>
    <tr>
      <td height="24" align="right">虚拟路径：</td>
      <td align="left"><input type="text" name="Application.virtualPath" value="<%=Global.virtualPath%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">真实路径：</td>
      <td align="left"><input type="text" name="Application.realPath" value="<%=Global.getRealPath()%>"/>
        ( 请使用 / 符号 )</td>
    </tr>
    <tr>
      <td height="24" align="right">备份路径：</td>
      <td align="left"><input type="text" name="Application.bak_path" value="<%=cfg.get("Application.bak_path")%>"/>
        ( 相对目录，如：bak )</td>
    </tr>
    <tr>
      <td height="24" align="right">浏览器上传时单个文件的最大尺寸：</td>
      <td align="left"><input type="text" name="Application.FileSize" value="<%=Global.FileSize%>"/>
        ( 单位：K ) </td>
    </tr>
    <tr>
      <td height="24" align="right">WebEdit控件上传时的最大尺寸：</td>
      <td align="left"><input type="text" name="Application.WebEdit.MaxSize" value="<%=cfg.get("Application.WebEdit.MaxSize")%>"/>
        ( 单位：字节，含HTML代码和附件总的大小 )</td>
    </tr>
    <tr>
      <td height="24" align="right">服务器端接收上传文件的最大并发数：</td>
      <td align="left"><input type="text" name="Application.WebEdit.maxUploadingFileCount" value="<%=cfg.get("Application.WebEdit.maxUploadingFileCount")%>"/>
        ( WebEdit控件断点上传时 )</td>
    </tr>
    <tr>
      <td height="24" align="right">服务器是否直接支持中文：</td>
      <td align="left"><select name="Application.isRequestSupportCN">
        <option value="true">是</option>
        <option value="false" selected="selected">否</option>
      </select>
          <script>
		var supobj = findObj("Application.isRequestSupportCN");
		supobj.value = "<%=Global.requestSupportCN%>";
		    </script>
        ( Tomcat 选否，Resin选是，<span class="STYLE1">注意慎重选用，否则在提交后可能会出现乱码</span> ) </td>
    </tr>
    <tr>
      <td height="24" colspan="2" align="center"><input name="button" type="button" onclick="form1.submit()" value=" 确 定 "  class="btn"/></td>
    </tr>
</table>
  </form>
</body>                                        
</html>                            
  