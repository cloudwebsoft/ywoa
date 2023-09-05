<%@page language="java" contentType="text/html;charset=utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.android.system.MobileAppIconConfigMgr" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="com.redmoon.oa.android.system.MobileAppIconConfigDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
    UserDb user = new UserDb();
    user = user.getUserDb(userName);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>我</title>
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="css/mui.css">
    <style>
        .photo {
            max-width: 80px;
            max-height: 80px;
            vertical-align: middle;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/mui.min.js"></script>
</head>
<body>
<div class="mui-content">
    <div class="mui-table mui-table-view-cell">
        <div class="mui-table-cell mui-col-xs-4"><span>
         <%
             if (!"".equals(user.getPhoto())) {
         %>
             <img class="photo" src="<%=request.getContextPath()%>/showImg.do?path=<%=user.getPhoto() %>"
                  style="width:130px"/>
          <%
          } else {
          %>
                <%if (user.getGender() == 0) {%>
                <img class="photoImg" src="<%=request.getContextPath()%>/images/man.png" style="width:29px"/>
                <%} else {%>
                <img class="photoImg" src="<%=request.getContextPath()%>/images/woman.png" style="width:29px"/>
                <%}%>
          <%} %>
        </span></div>
        <div class="mui-table-cell mui-col-xs-8"><span><%=user.getRealName()%></span></div>
    </div>
    <div class="mui-table mui-table-view-cell">
        <div class="mui-table-cell mui-col-xs-4"><span class="mui-icon mui-icon-compose" style="font-size: 30px"></span></div>
        <div class="mui-table-cell mui-col-xs-8">
            <span class="mui-push-right"></span>
            <span onclick="modifyPwd()">修改密码</span></div>
    </div>
    <div class="mui-table mui-table-view-cell">
        <div class="mui-table-cell mui-col-xs-4"><span class="mui-icon mui-icon-redo" style="font-size: 30px"></span></div>
        <div class="mui-table-cell mui-col-xs-8"><span onclick="logout()">退出登录</span></div>
    </div>
</div>

<jsp:include page="inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="tabId" value="me"/>
</jsp:include>
</body>
<script>
    function modifyPwd() {
        window.location.href = 'me_pwd.jsp';
    }
    function logout() {
        var btnArray = ['确认', '取消'];
        mui.confirm('确认删除要退出么？', '提示', btnArray, function(e) {
                if (e.index == 0) {
                    var expdate = new Date();
                    expdate.setTime(-1000);

                    document.cookie="isMyLoginAuto=;expires="+expdate.toGMTString() + ";path=/";
                    document.cookie="pwd=;expires="+expdate.toGMTString() + ";path=/";

                    window.location.href="../logout?redirectUrl=wap/index.jsp";
                }
            }
        );
    }
</script>
</html>
