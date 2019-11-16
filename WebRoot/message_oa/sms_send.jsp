<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.sms.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "sms")) {
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
        return;
    }

    UserSetupDb usd = new UserSetupDb();
    usd = usd.getUserSetupDb(privilege.getUser(request));
    int messageToMaxUser = usd.getMessageToMaxUser();

    String name = privilege.getUser(request);
    UserDb user = new UserDb();
    user = user.getUserDb(name);
    String mobile = ParamUtil.get(request, "mobile");

    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mobile", mobile, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    String op = ParamUtil.get(request, "op");
    boolean re = false;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>手机短信</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <script src="../inc/common.js"></script>
    <style type="text/css">
        @import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css");
    </style>
    <script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script language=javascript>
        $(function () {
            $('#timeSend_Date').datetimepicker({value: '<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>', step: 10, format: 'Y-m-d H:i:00'});
        })

        function form1_onsubmit() {
            errmsg = "";
            if (form1.mobile.value == "")
                errmsg += "请填写手机号码！\n"
            if (form1.content.value == "")
                errmsg += "请填写内容！\n"
            // if (form1.content.value.length>70)
            //	errmsg += "请不要超过70个字符！\n";
            if (errmsg != "") {
                jAlert(errmsg, "提示");
                return false;
            }
        }

        function openAddressSelWin(type) {
            openWin("../address/address_frame_sel.jsp?type=" + type, 800, 600);
        }

        function setMobiles(mobiles) {
            if (mobiles.split(",").length ><%=messageToMaxUser%>) {
                jAlert("对不起，您一次最多只能发往<%=messageToMaxUser%>个用户！", "提示");
                // return;
            }
            form1.mobile.value = mobiles;
        }

        function importExcel() {
            openWin("sms_import_excel.jsp", 800, 100);
        }

        function setObj(th) {
            document.getElementById('content').innerHTML = th;
            countChar("content", "counter");
        }

        function countChar(textareaName, spanName) {
            document.getElementById(spanName).innerHTML = document.getElementById(textareaName).value.length;
        }

        //-->
    </script>
</head>
<body>
<%@ include file="sms_user_inc_menu_top.jsp" %>
<%
    if (op.equals("send")) {
        IMsgUtil imu = SMSFactory.getMsgUtil();
        String content = ParamUtil.get(request, "content");
        String isTimeSend = ParamUtil.get(request, "isTimeSend");
        boolean timing = "1".equals(isTimeSend) ? true : false;
        String timeSendDate = ParamUtil.get(request, "timeSend_Date");
        if ("".equals(timeSendDate)) {
            timeSendDate = DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss");
        }
        //String timeSendTime = ParamUtil.get(request,"timeSend");
        //if(timeSendTime == null||timeSendTime ==""){
        //timeSendTime = "00:00";
        //}
        java.util.Date timeSend = DateUtil.parse(timeSendDate, "yyyy-MM-dd hh:mm:ss");
        long batch = SMSSendRecordDb.getBatchCanUse();
        if (imu != null) {
            // content = user.getRealName() + "：" + content;
            mobile = mobile.replaceAll("，", ",");
            String[] ary = StrUtil.split(mobile, ",");
            int length = ary.length;//发送短信条数
            if (ary.length > 0) {
                if (ary.length > messageToMaxUser) {
                    out.print(StrUtil.jAlert_Back("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！", "提示"));
                    return;
                }
                Config cfg = new Config();
                int remain = cfg.canSendSMS(length, content.length());
                int count = cfg.getDivNumber(content.length());
                int realSendUserCount = 0;
                int i = 0;
                try {
                    for (; i < length && remain > 0; i++) {
                        if (imu.send(ary[i], content, name, timing, timeSend, batch)) {
                            realSendUserCount++;
                            remain--;
                        }
                    }
                } catch (ErrMsgException e) {
                    out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
                    return;
                }
                int realSendCount = realSendUserCount * count;
                if (i < length) {
                    if (cfg.getBoundary() == Config.SMS_BOUNDARY_YEAR) {
                        //out.print(StrUtil.Alert_Redirect(StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}), "sms_send.jsp"));
                        out.print(StrUtil.jAlert_Redirect(StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}), "提示", "sms_send.jsp"));
                    } else if (cfg.getBoundary() == Config.SMS_BOUNDARY_MONTH) {
                        out.print(StrUtil.jAlert_Redirect(StrUtil.format(cfg.getProperty("sms.alertMonthExceed"), new Object[]{cfg.getIsUsedProperty("monthTotal"), "" + realSendCount}), "提示", "sms_send.jsp"));
                    } else {
                        out.print(StrUtil.jAlert_Redirect("发送完毕，本次共发送短信" + realSendCount + "条。", "提示", "sms_send.jsp"));
                    }
                } else {
                    out.print(StrUtil.jAlert_Redirect("发送完毕，本次共发送短信" + realSendCount + "条。", "提示", "sms_send.jsp"));
                }
            }
            //out.print(StrUtil.Alert_Back("发送成功！"));
            return;
        } else {
            out.print(StrUtil.jAlert_Back("短信发送功能未开通！", "提示"));
        }
        return;
    }
%>
<script src="../inc/flow_dispose_js.jsp"></script>
<script>
    o("menu3").className = "current";

    /**function SelectDateTime(objName) {
		var dt = showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
		if (dt!=null)
			$(objName).value = dt.substring(0, 5);
	}*/
    function SelectDateTime(objName) {
        var dt = openWin("../util/calendar/time.htm?divId" + objName, "266px", "185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
    }

    function sel(dt, objName) {
        if (dt != null && objName != "")
            $(objName).value = dt.substring(0, 5);
    }
</script>
<br>
<div class="spacerH"></div>
<form action="?op=send" method="post" name="form1" onSubmit="return form1_onsubmit()">
    <table width="503" border="0" cellspacing="0" cellpadding="3" align="center" class="tabStyle_1 percent60">
        <thead>
        <tr>
            <td class="tabStyle_1_title" height="27" colspan="2">按号码发送</td>
        </tr>
        </thead>
        <tr>
            <td width="123" height="27">
                <div align="center">手机号码：</div>
            </td>
            <td width="368" height="27"><textarea name="mobile" style="width:98%" cols="28" rows="5"><%=mobile%></textarea>
                <div style="padding:3px"><a href="javascript:openAddressSelWin(0)">个人通讯录</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:openAddressSelWin(1)">公共通讯录</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:importExcel()">导入号码</a></div>
            </td>
        </tr>
        <tr>
            <td width="123" height="26">
                <div align="center">短信内容：</div>
            </td>
            <td width="368" height="26">
                <textarea name="content" id="content" style="width:98%; height:100px" cols="28" rows="5" onkeydown='countChar("content","counter");' onkeyup='countChar("content","counter");'></textarea></td>
        </tr>
        <tr>
            <td width="123" height="26">
                <div align="center">短信字数：</div>
            </td>
            <td width="368" height="26">已经输入<span id="counter" style="color:#FF0000">0</span>字
                &nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;" onClick="openWin('sms_template_sel.jsp', 640, 480)">短信模版</a></td>
        </tr>
        <tr>
            <td width="123" height="26">
                <div align="center">定时发送：</div>
            </td>
            <td width="368" height="26">
                <input type="checkbox" name="isTimeSend" value="1"/>
        </tr>
        <tr>
            <td width="123" height="26">
                <div align="center">发送时间：</div>
            </td>
            <td width="368" height="26">
                <input name=timeSend_Date id="timeSend_Date" size="18" />
        </tr>
        <tr>
            <td colspan="2" height="26" style="padding-left:14px;"><span><span style="color:#FF0000">注：<br/>
            短信字数超过70字，将自动转为2条短信<br/>
            群发时请用逗号分隔手机号码&nbsp;            </span></span></td>
        </tr>
        <tr>
            <td colspan="2" height="26">
                <div align="center">
                    <input type="submit" name="Submit" value="发送" class="btn">
                    <%
                        if (!"".equals(mobile)) {
                    %>
                    &nbsp;&nbsp;
                    <input type="button" class="btn" value="返回" onclick="window.history.back()"/>
                    <%
                        }
                    %>
                </div>
            </td>
        </tr>
    </table>
</form>
</body>
</html>
