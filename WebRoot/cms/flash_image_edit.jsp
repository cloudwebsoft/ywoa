<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*,
				 java.text.*,
				 com.cloudwebsoft.framework.base.*,
				 cn.js.fan.db.*,
				 cn.js.fan.module.cms.site.*,
				 cn.js.fan.module.cms.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.ui.*,
				 cn.js.fan.web.*,
				 cn.js.fan.module.pvg.*"
%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String siteCode = Leaf.ROOTCODE;
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML><HEAD><TITLE>图片轮播 - 编辑</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var urlObj;
function SelectImage(urlObject) {
	urlObj = urlObject;
	openWin("flash_image_sel.jsp", 800, 600);
}
function setImgUrl(visualPath, id, title) {
	o("url" + urlObj).value = visualPath;
	o("link" + urlObj).value = "<%=request.getContextPath()%>/doc_show.jsp?id=" + id;
	o("title" + urlObj).value = title;
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

long id = ParamUtil.getLong(request, "id");
String op = ParamUtil.get(request, "op");
SiteFlashImageDb sfd = new SiteFlashImageDb();
sfd = (SiteFlashImageDb)sfd.getQObjectDb(new Long(id));
siteCode = sfd.getString("site_code");
if (op.equals("edit")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, sfd, "site_flash_image_save")) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "flash_image_edit.jsp?id=" + id + "&siteCode=" + siteCode));
		}
		else {
			out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
  <TR>
    <TD class="tdStyle_1"><a href="flash_image_list.jsp?siteCode=<%=siteCode%>">图片轮播</a></TD>
  </TR></TBODY></TABLE>
<br>
      <table width="85%" align="center" class="tabStyle_1 percent80">
        <form id=form1 name=form1 action="flash_image_edit.jsp?op=edit&siteCode=<%=siteCode%>" method=post>
          <thead>
          <tr>
            <td height="22" colspan="4" class="tabStyle_1_title">编辑图片</td>
          </tr>
          </thead>
          <tr>
            <td height="22">名称</td>
            <td height="22" colspan="3"><input name="name" value="<%=StrUtil.getNullStr(sfd.getString("name"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片设置</td>
            <td height="22">地址</td>
            <td height="22">链接</td>
            <td height="22">文字</td>
          </tr>
          <tr>
            <td height="22">图片1
              <input name="id" value="<%=id%>" type=hidden></td>
            <td><input name="url1" value="<%=StrUtil.getNullStr(sfd.getString("url1"))%>">
            <input type="button" value="选择" onclick="SelectImage(1)" /></td>
            <td><input name="link1" value="<%=StrUtil.getNullStr(sfd.getString("link1"))%>"></td>
            <td><input name="title1" value="<%=StrUtil.getNullStr(sfd.getString("title1"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片2 </td>
            <td><input name="url2" value="<%=StrUtil.getNullStr(sfd.getString("url2"))%>">
            <input name="button" type="button" onclick="SelectImage(2)" value="选择" /></td>
            <td><input name="link2" value="<%=StrUtil.getNullStr(sfd.getString("link2"))%>"></td>
            <td><input name="title2" value="<%=StrUtil.getNullStr(sfd.getString("title2"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片3 </td>
            <td><input name="url3" value="<%=StrUtil.getNullStr(sfd.getString("url3"))%>">
            <input name="button2" type="button" onclick="SelectImage(3)" value="选择" /></td>
            <td><input name="link3" value="<%=StrUtil.getNullStr(sfd.getString("link3"))%>"></td>
            <td><input name="title3" value="<%=StrUtil.getNullStr(sfd.getString("title3"))%>"></td>
          </tr>
          <tr>
            <td width="23%" height="22">图片4 </td>
            <td><input name="url4" value="<%=StrUtil.getNullStr(sfd.getString("url4"))%>">
            <input name="button3" type="button" onclick="SelectImage(4)" value="选择" /></td>
            <td><input name="link4" value="<%=StrUtil.getNullStr(sfd.getString("link4"))%>"></td>
            <td><input name="title4" value="<%=StrUtil.getNullStr(sfd.getString("title4"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片5</td>
            <td><input name="url5" value="<%=StrUtil.getNullStr(sfd.getString("url5"))%>">
            <input name="button4" type="button" onclick="SelectImage(5)" value="选择" /></td>
            <td><input name="link5" value="<%=StrUtil.getNullStr(sfd.getString("link5"))%>"></td>
            <td><input name="title5" value="<%=StrUtil.getNullStr(sfd.getString("title5"))%>"></td>
          </tr>
          <tr>
            <td height="22" colspan="4" align="center">
            <input name="submit32" type="submit" class="btn" value="确 定">&nbsp;&nbsp;&nbsp;&nbsp;
            <input type="button" value="返 回" class="btn" onClick="window.location.href='flash_image_list.jsp?siteCode=<%=StrUtil.UrlEncode(siteCode)%>'" />
            </td>
          </tr>
        </form>
</table>
</BODY></HTML>
