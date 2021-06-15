<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>通讯录-详情</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td height="23" class="tdStyle_1">通讯录</td>
    </tr>
</table>
<br/>
<table width="43%" align="center" cellpadding="2" class="tabStyle_1 percent98">
    <tr>
        <td class="tabStyle_1_title" height="21" colspan="2" align="left">个人信息</td>
    </tr>
    <tr>
        <td width="20%" height="19" align="center">姓名</td>
        <td width="80%" height="19">${addr.person}</td>
    </tr>
    <tr>
        <td height="19" align="center">单位</td>
        <td height="19">${addr.company}</td>
    </tr>
    <tr>
        <td height="19" align="center">职务</td>
        <td height="19">${addr.job}</td>
    </tr>
    <tr>
        <td height="19" align="center">手机</td>
        <td height="19">${addr.mobile}</td>
    </tr>
    <tr>
        <td height="19" align="center">短号</td>
        <td height="19">${addr.MSN}</td>
    </tr>
    <tr>
    <tr>
        <td height="19" align="center">Email</td>
        <td height="19">${addr.email}</td>
    </tr>
    <tr>
        <td height="19" align="center">微信</td>
        <td height="19">${addr.weixin}</td>
    </tr>
    <td height="19" align="center">电话</td>
    <td height="19">${addr.tel}</td>
    </tr>
    <tr>
        <td height="19" align="center">传真</td>
        <td height="19">${addr.fax}</td>
    </tr>
    <tr>
        <td height="19" align="center">QQ</td>
        <td height="19">${addr.QQ}</td>
    </tr>
    <tr>
        <td height="19" align="center">网页</td>
        <td height="19">${addr.web}</td>
    </tr>
    <tr>
        <td height="19" align="center">邮编</td>
        <td height="19">${addr.postalcode}</td>
    </tr>
    <tr>
        <td height="19" align="center">地址</td>
        <td height="19">${addr.address}</td>
    </tr>
    <tr>
        <td height="19" align="center">附注</td>
        <td height="19">${addr.introduction}</td>
    </tr>
</table>
</body>
</html>