<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*,
				 java.text.*,
				 com.cloudwebsoft.framework.base.*,
				 cn.js.fan.db.*,
				 cn.js.fan.module.cms.site.*,
				 cn.js.fan.module.cms.*,
				 cn.js.fan.util.*,
				 com.redmoon.forum.person.UserMgr,
				 com.redmoon.forum.person.UserDb,
				 cn.js.fan.web.*,
				 cn.js.fan.module.pvg.*"
%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String siteCode = "cws_forum";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML><HEAD><TITLE>Flash图片管理</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="default.css" type=text/css rel=stylesheet>
<META content="MSHTML 6.00.3790.259" name=GENERATOR>
<style type="text/css">
<!--
.style1 {
	font-size: 14px;
	font-weight: bold;
}
-->
</style>
<script>
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var urlObj;
function SelectImage(urlObject) {
	urlObj = urlObject;
	openWin("media_frame.jsp?action=selectImage", 800, 600);
}
function SetUrl(visualPath) {
	urlObj.value = visualPath;
}
</script>
</HEAD>
<BODY text=#000000 bgColor=#eeeeee leftMargin=0 topMargin=0>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

long id = ParamUtil.getLong(request, "id");
String op = ParamUtil.get(request, "op");
SiteFlashImageDb sfd = new SiteFlashImageDb();
sfd = (SiteFlashImageDb)sfd.getQObjectDb(new Long(id));
if (op.equals("edit")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, sfd, "site_flash_image_save")) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flash_image_edit.jsp?id=" + id + "&siteCode=" + siteCode));
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
  <TR>
    <TD class=head><a href="flash_image_list.jsp?siteCode=<%=siteCode%>">Flash图片</a></TD>
  </TR></TBODY></TABLE>
<br>
      <table width="85%" align="center" class="frame_gray">
        <form id=form1 name=form1 action="?op=edit&siteCode=<%=siteCode%>" method=post>
          <tr>
            <td height="22" colspan="4" class="thead">编辑Flash图片</td>
          </tr>
          <tr>
            <td height="22">名称</td>
            <td height="22" colspan="3"><input name="name" value="<%=StrUtil.getNullStr(sfd.getString("name"))%>"></td>
          </tr>
          <tr>
            <td height="22">Flash图片设置</td>
            <td height="22">地址</td>
            <td height="22">链接</td>
            <td height="22">文字</td>
          </tr>
          <tr>
            <td height="22">图片1
              <input name="id" value="<%=id%>" type=hidden></td>
            <td><input name="url1" value="<%=StrUtil.getNullStr(sfd.getString("url1"))%>">
            <input type="button" value="选择" onclick="SelectImage(form1.url1)" /></td>
            <td><input name="link1" value="<%=StrUtil.getNullStr(sfd.getString("link1"))%>"></td>
            <td><input name="title1" value="<%=StrUtil.getNullStr(sfd.getString("title1"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片2 </td>
            <td><input name="url2" value="<%=StrUtil.getNullStr(sfd.getString("url2"))%>">
            <input name="button" type="button" onclick="SelectImage(form1.url2)" value="选择" /></td>
            <td><input name="link2" value="<%=StrUtil.getNullStr(sfd.getString("link2"))%>"></td>
            <td><input name="title2" value="<%=StrUtil.getNullStr(sfd.getString("title2"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片3 </td>
            <td><input name="url3" value="<%=StrUtil.getNullStr(sfd.getString("url3"))%>">
            <input name="button2" type="button" onclick="SelectImage(form1.url3)" value="选择" /></td>
            <td><input name="link3" value="<%=StrUtil.getNullStr(sfd.getString("link3"))%>"></td>
            <td><input name="title3" value="<%=StrUtil.getNullStr(sfd.getString("title3"))%>"></td>
          </tr>
          <tr>
            <td width="23%" height="22">图片4 </td>
            <td><input name="url4" value="<%=StrUtil.getNullStr(sfd.getString("url4"))%>">
            <input name="button3" type="button" onclick="SelectImage(form1.url4)" value="选择" /></td>
            <td><input name="link4" value="<%=StrUtil.getNullStr(sfd.getString("link4"))%>"></td>
            <td><input name="title4" value="<%=StrUtil.getNullStr(sfd.getString("title4"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片5</td>
            <td><input name="url5" value="<%=StrUtil.getNullStr(sfd.getString("url5"))%>">
            <input name="button4" type="button" onclick="SelectImage(form1.url5)" value="选择" /></td>
            <td><input name="link5" value="<%=StrUtil.getNullStr(sfd.getString("link5"))%>"></td>
            <td><input name="title5" value="<%=StrUtil.getNullStr(sfd.getString("title5"))%>"></td>
          </tr>
          <tr>
            <td height="22" colspan="4" align="center"><input name="submit32" type="submit" style="border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;" value=" 确 定 "></td>
          </tr>
        </form>
</table>
</BODY></HTML>
