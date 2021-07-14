<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.sms.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@page import="com.redmoon.oa.sms.Config" %>
<%@page import="org.jdom.Element" %>
<%@page import="java.util.*" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>撰写消息</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <%@ include file="../inc/nocache.jsp" %>
    <style type="text/css">
        @import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css");
    </style>
    <style>
        .errMsgTable {
            width: 80%;
        }
    </style>
    <script src="../inc/common.js"></script>
</head>
<body>
<%@ include file="sms_user_inc_menu_top.jsp" %>
<script>
    o("menu5").className = "current";
</script>
<%
    Config cfg = new Config();
    Element root = cfg.getRootElement();
    Iterator ir = root.getChildren("sms").iterator();
    String code = "";
    while (ir.hasNext()) {
        Element e = (Element) ir.next();
        String isUsed = e.getAttributeValue("isUsed");
        if (isUsed.equals("true")) {
            code = e.getAttributeValue("code");
            break;
        }
    }
    if (!code.equals("qxtmobile") && !code.equals("mmmobile")) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "此短信接口不支持此功能！"));
        return;
    }
%>
<div class="spacerH"></div>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
    <tr>
        <td class="tabStyle_1_title" height="27">短信剩余条数</td>
    </tr>
    <tr>
        <td>
            <%
                String strURL = cfg.getIsUsedProperty("qxtMasSendSmsUrl");
                ;
                String userName = cfg.getIsUsedProperty("user_name");
                ;
                String pwd = cfg.getIsUsedProperty("password");
                String result = "";
                if (code.equals("qxtmobile")) {
                    QxtMasMobileMsgUtil qxt = new QxtMasMobileMsgUtil();
                    result = qxt.getSmsCount();
                    if (result.startsWith("failure")) {
                        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "查询时间过频繁，2次间隔时间应该大于60秒！"));
                        return;
                    }
                    String[] smsCount = result.split(";");
                    int tmp = smsCount[0].indexOf("=") + 1;
                    out.print(smsCount[0].substring(tmp));
                } else if (code.equals("mmmobile")) {
                    MmQxtMobileMsgUtil mm = new MmQxtMobileMsgUtil();
                    result = mm.getSmsCount();
                    String[] smsCount = result.split(",");
                    // 100, 9，表示充了100条，剩余9条
                    if (smsCount != null && smsCount.length > 1) {
                        out.print("充值累计" + smsCount[0] + "条，剩余" + smsCount[1] + "条");
                    }
                }
            %>
        </td>
    </tr>
</table>
</body>
</html>
