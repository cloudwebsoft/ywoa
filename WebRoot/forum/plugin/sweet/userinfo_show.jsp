<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	out.print(StrUtil.Alert_Back("缺少用户名！"));
	return;
}
String boardcode = ParamUtil.get(request, "boardcode");

Leaf curleaf = new Leaf();
curleaf = curleaf.getLeaf(boardcode);

// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../../<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<title><%=Global.AppName%> - 显示用户信息</title>
<style type="text/css">
<!--
.style1 {
	color: #FF00FF;
	font-weight: bold;
}
.style2 {
	color: #FF0000;
	font-weight: bold;
}
.style3 {color: #993399}
-->
</style></head>
<body>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<%
SweetUserInfoDb suid = new SweetUserInfoDb();
suid = suid.getSweetUserInfoDb(userName);
String user = privilege.getUser(request);
BoardManagerDb bm = new BoardManagerDb();
bm = bm.getBoardManagerDb(boardcode, user);

boolean canEdit = false;
if (user.equals(userName))
	canEdit = true;
if (bm.isLoaded())
	canEdit = true;
// 如果用户信息不存在
if (!suid.isLoaded()) {
	// 如果是用户本人查看自己的，则重定向至录入页面
	if (user.equals(userName)) {
		response.sendRedirect("userinfo_add.jsp?boardcode="+StrUtil.UrlEncode(boardcode)+"&userName="+StrUtil.UrlEncode(userName));
		return;
	}
	else {
		if (boardcode.equals("")) {
			out.print(SkinUtil.makeErrMsg(request, "对不起，该用户的详细信息尚未填写！"));
			return;
		}
		// 如果浏览者是版主，则重定向至录入页面
		if (bm.isLoaded()) {
			response.sendRedirect("userinfo_add.jsp?boardcode="+StrUtil.UrlEncode(boardcode)+"&userName="+StrUtil.UrlEncode(userName));
			return;
		}
		else {
			out.print(SkinUtil.makeErrMsg(request, "对不起，该用户的详细信息尚未填写！"));
			return;
		}
	}
}
%>
<table class="tableCommon80" cellspacing="1" cellpadding="0" width="530" align="center" border="0">
  <thead>
    <tr>
      <td height="30" colspan="2"><%=StrUtil.toHtml(userName)%>的个人信息&nbsp;&nbsp;
          <%if (canEdit) {%>
          <a href="userinfo_edit.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&amp;boardcode=<%=StrUtil.UrlEncode(boardcode)%>">编辑信息</a>
          <%}%>
      </td>
    </tr>
  </thead>
    <tr>
      <td height="30" colspan="2"><%if (suid.isChecked()) {%>
          <span class="style1">信息准确，已由版主<%=suid.getManager()%>审核</span>
          <%}else{%>
          <span class="style2">信息尚未审核</span>
          <%}%>
          <strong>等级： <font color="red"><%=suid.getMemberDesc()%>会员</font></strong>
          <%if (suid.getMember()==suid.MEMBER_GOLD) {%>
          <img src="images/gold.gif" />
          <%}else if (suid.getMember()==suid.MEMBER_SILVER){%>
          <img src="images/silver.gif" />
          <%}%>
      </td>
    </tr>
    <%
	String photo = StrUtil.getNullString(suid.getPhoto());
	if (!photo.equals("")) {
	%>
    <tr align="center">
      <td height="30" align="right">照　　片： </td>
      <td height="30" align="left">　 <img src="<%=request.getContextPath()+"/"+photo%>" /></td>
    </tr>
    <%}%>
    <tr>
      <td width="108" height="30" align="right"> 性　　别：</td>
      <td width="419" height="30"> 　 <%=suid.getName()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 年　　龄： </div></td>
      <td height="30">　 <%=suid.getAge()%> 岁 *</td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 出生年月： </div></td>
      <td height="30">　<font color="#000000"> <%=DateUtil.format(suid.getBirthday(), "yyyy-MM-dd")%> </font></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 婚姻状况： </div></td>
      <td height="30">　 <%=suid.getMarriage()%> </td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 籍　　贯： </div></td>
      <td height="30">　<font color="#000000"> <%=StrUtil.toHtml(suid.getProvince())%></font></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 工 作 地： </div></td>
      <td height="30">　 <%=suid.getWorkAddress()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 身　　高： </div></td>
      <td height="30">　 <%=suid.getTall()%> cm *</td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 学　　历： </div></td>
      <td height="30">　<font color="#000000"> <%=suid.getXueli()%></font></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 职　　业： </div></td>
      <td height="30">　 <%=suid.getPostCode()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 月　　薪： </div></td>
      <td height="30">　 <%=suid.getPostCode()%></td>
    </tr>
    <%if (suid.getMember()<suid.MEMBER_GOLD) {%>
    <tr align="center">
      <td height="54" colspan="2"><%
	  UserDb userDb = new UserDb();
	  userDb = userDb.getUser(suid.getManager());
	  %>
          <span class="style3">===== 受该用户的会员等级限制，联系方式被屏蔽！请跟版主联系！<br />
            联系电话：<%=userDb.getMobile()%>&nbsp;&nbsp;<%=userDb.getPhone()%> =====</span> </td>
    </tr>
    <%}else{%>
    <tr>
      <td height="30"><div align="right"> 通信地址： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getAddress())%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 邮　　编： </div></td>
      <td height="30">　 <%=suid.getPostCode()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 电　　话： </div></td>
      <td height="30">　 <%=suid.getTel()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 电子邮件： </div></td>
      <td height="30">　 <%=suid.getEmail()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> OICQ： </div></td>
      <td height="30">　 <%=suid.getOICQ()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> ICQ ： </div></td>
      <td height="30">　 <%=suid.getICQ()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> MSN ： </div></td>
      <td height="30">　 <%=suid.getMSN()%></td>
    </tr>
    <%}%>
    <tr>
      <td height="98"><div align="right"> 自我介绍： </div></td>
      <td height="98">　 <%=StrUtil.toHtml(suid.getDescription())%></td>
    </tr>
    <tr>
      <td height="30" colspan="2"><div align="center"> <font color="#000000"><strong>个人爱好</strong></font> </div></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 喜欢的运动： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getSport())%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 喜欢的书籍： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getBook())%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 喜欢的音乐： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getMusic())%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 喜欢的名人： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getCelebrity())%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 其它爱好或特长： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getHobby())%></td>
    </tr>
    <tr>
      <td height="30" colspan="2"><div align="center"> <strong>交友类型</strong> </div></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 交友类型： </div></td>
      <td height="30">　 <%=suid.getFrendType()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 年　　龄： </div></td>
      <td height="30">　 <%=suid.getFrendAge()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 身　　高： </div></td>
      <td height="30">　 <%=suid.getTall()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 婚姻状况： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getFrendMarriage())%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 原　　籍： </div></td>
      <td height="30">　 <%=suid.getPostCode()%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 学　　历： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getFrendXueli())%></td>
    </tr>
    <tr>
      <td height="30"><div align="right"> 月　　薪： </div></td>
      <td height="30">　 <%=StrUtil.toHtml(suid.getSalary())%></td>
    </tr>
    <tr>
      <td height="105"><div align="right"> 其他要求： </div></td>
      <td height="105">　 <%=StrUtil.toHtml(suid.getFrendRequire())%></td>
    </tr>
  </tbody>
</table>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</body>
</html>
