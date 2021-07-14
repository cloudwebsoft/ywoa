<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>承诺书</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String userName = privilege.getUser(request);
    UserDb user = new UserDb();
    user = user.getUserDb(userName);
    String realName = user.getRealName();
%>
<table id="mainTable" class="tabStyle_1 percent60" style="margin-top: 20px" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tbody>
    <tr>
        <td align="center" noWrap class="tabStyle_1_title">*******管理系统</td>
    </tr>
    <tr class="highlight">
        <td><p align="center">承诺书 </p>
            <p>为打造高效完备的管理平台，营造诚实可信的信用环境，本人郑重作出如下信用承诺： <br/>
                （一）......； <br/>
            </p>
            <p>&nbsp;</p>
            <p align="right">承诺单位：<%=realName%>&nbsp;&nbsp;&nbsp;&nbsp;<br/>
                承诺日期：<%=DateUtil.format(new Date(), "yyyy-MM-dd")%> &nbsp;&nbsp;&nbsp;&nbsp;</p>
            <p>&nbsp;</p>
            <p align="center">注：您必须勾选&ldquo;已阅读&rdquo;才能进行下一步操作 <br/>
                <input type="checkbox" id="isAgree" name="isAgree" value="1"/>
                我已阅读并接受《******数据填报信用承诺书》 </p></td>
    </tr>
    <tr class="highlight">
        <td align="center">
            <button class="btn btn-default" onclick="ok()">确定</button>
        </td>
    </tr>
    </tbody>
</table>
<br/>
</body>
<script>
    function ok() {
        var isAgree = getCheckboxValue("isAgree");
        if (isAgree == "") {
            jAlert("您必须勾选“已阅读”才能进行下一步操作", "提示");
            return;
        }
        window.location.href = "../confirmAgreement.do?userName=<%=StrUtil.UrlEncode(userName)%>";
    }
</script>
</html>