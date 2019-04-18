<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>通讯录-查看</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strtype = ParamUtil.get(request, "type");
int type = AddressDb.TYPE_USER;
if (!strtype.equals(""))
	type = StrUtil.toInt(strtype, AddressDb.TYPE_USER);
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" class="tdStyle_1">通讯录</td>
  </tr>
</table>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int id = ParamUtil.getInt(request, "id");

AddressDb addr = new AddressDb();
addr = addr.getAddressDb(id);

String person="",job="",tel="",mobile="",email="",address="",postalcode="",introduction="",business="";
person = addr.getPerson();
job = addr.getJob();
tel = addr.getTel();
mobile = addr.getMobile();
email = addr.getEmail();
address = addr.getAddress();
postalcode = StrUtil.getNullString(addr.getPostalcode());
if (postalcode.equals(""))
	postalcode = "&nbsp;";
introduction = addr.getIntroduction();
if (business.equals(""))
	business = "&nbsp;";
%>
<br />
<table width="43%" align="center" cellpadding="2" class="tabStyle_1 percent98">
	<tr> 
	  <td class="tabStyle_1_title" height="21" colspan="2" align="left">个人信息</td>
	</tr>
	  <tr> 
		<td width="20%" height="19" align="center">姓名</td>
		<td width="80%" height="19"><%=person%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">单位</td>
		<td height="19"><%=addr.getCompany()%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">职务</td>
		<td height="19"><%=addr.getJob()%></td>
	  </tr>
	  <tr>
	    <td height="19" align="center">手机</td>
	    <td height="19"><%=mobile%></td>
  </tr>
	  <tr>
	    <td height="19" align="center">短号</td>
	    <td height="19"><%=addr.getMSN()%></td>
  </tr>
	  <tr>
	  <tr> 
		<td height="19" align="center">Email</td>
		<td height="19"><%=addr.getEmail()%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">微信</td>
		<td height="19"><%=addr.getWeixin()%></td>
	  </tr>      
		<td height="19" align="center">电话</td>
		<td height="19"><%=addr.getTel()%></td>
	  </tr>      
	  <tr>
		<td height="19" align="center">传真</td>
		<td height="19"><%=addr.getFax()%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">QQ</td>
		<td height="19"><%=addr.getQQ()%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">网页</td>
		<td height="19"><%=addr.getWeb()%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">邮编</td>
		<td height="19"><%=addr.getPostalcode()%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">地址</td>
		<td height="19"><%=addr.getAddress()%></td>
	  </tr>
	  <tr>
		<td height="19" align="center">附注</td>
		<td height="19"><%=addr.getIntroduction()%> </td>
	  </tr>
</table>
</body>
</html>