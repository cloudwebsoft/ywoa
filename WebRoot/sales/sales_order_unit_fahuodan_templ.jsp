<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.sale.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="pragma" content="no-cache">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<title>发货单模板编辑</title>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script language="JavaScript">
<!--
function openWin(url,width,height)
{
	var newwin = window.open(url,"_blank","scrollbars=yes,resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
<body>
<%
if (!privilege.isUserPrivValid(request, "sales"))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

SalesFahuodanDb sfd = new SalesFahuodanDb();
sfd = sfd.getSalesFahuodanDb(privilege.getUserUnitCode(request));

String filePath = Global.realPath + "sales/template/fahuodan.htm";
if (op.equals("edit")) {
	String content = ParamUtil.get(request, "content");
	sfd.set("content", content);
	sfd.save();
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "sales_order_unit_fahuodan_templ.jsp"));
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="tdStyle_1">发货单模板</td>
  </tr>
</table>
<table width="100%" align="center">
  <form id=formFck name=formFck action="?op=edit" method="post">
    <tr>
      <td height="22">
<textarea id="content" name="content" style="display:none" rows="50" cols="200">
<%=cn.js.fan.security.AntiXSS.antiXSS(sfd.getString("content"))%>
</textarea>
<script>
CKEDITOR.replace('content', 
	{
	// skin : 'kama',
	fullPage : true
	});
</script> 
  </td>
    </tr>
    <tr>
      <td height="22" align="center"><input class="btn" type="submit" value="确 定"></td>
    </tr>
  </form>
</table>
</body>                                        
</html>                            
  