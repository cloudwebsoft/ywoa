<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.emailpop3.*" %>
<%@ page import="org.json.*" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
    String name = privilege.getUser(request);
    String receiver = ParamUtil.get(request, "receiver");
    String receiver1 = ParamUtil.get(request, "receiver1");
    String receiver2 = ParamUtil.get(request, "receiver2");
    String title = ParamUtil.get(request, "title");
    String content = ParamUtil.get(request, "content");
    String isShowBack = ParamUtil.get(request, "isShowBack");
    
    String op = ParamUtil.get(request, "op");
    boolean isSuccess = false;
    JSONObject json = new JSONObject();
    
    
    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request,
                privilege, "receiver", receiver, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e
                .getMessage()));
        return;
    }
    
    String qStr = request.getQueryString();
    if (qStr != null) {
        if (!cn.js.fan.security.AntiXSS.antiXSS(qStr).equals(qStr)) {
            com.redmoon.oa.LogUtil.log(name, StrUtil.getIp(request),
                    com.redmoon.oa.LogDb.TYPE_HACK,
                    "CSRF message_oa/send.jsp");
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request,
                    SkinUtil.LoadString(request, "param_invalid")));
            return;
        }
    }
    int mailId = ParamUtil.getInt(request, "mailId", -1);
    MailMsgDb mmd = null;
    if (mailId != -1) {
        try {
            MailMsgMgr mmm = new MailMsgMgr();
            mmd = mmm.getMailMsgDb(request, mailId);
            title = mmd.getSubject();
            content = mmd.getContent();
        } catch (ErrMsgException e) {
            out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
            return;
        }
    }
    if (!privilege.isUserPrivValid(request, "message")) {
%>
<link type="text/css" rel="stylesheet"
      href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request,
                cn.js.fan.web.SkinUtil.LoadString(request,
                        "pvg_invalid")));
        return;
    }
    String netdiskFiles = ParamUtil.get(request, "netdiskFiles");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8"/>
    <title>撰写消息</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../inc/upload.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="../js/jquery.form.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script>
        $(document).ready(function () {
            $('#date').datetimepicker({value: '', step: 1, format: 'Y-m-d H:i:00', lang: 'ch'});
            //var options = {
            //    success:showResponse,  // post-submit callback
            //    beforeSubmit:    form_onsubmit
            //};
            //$('#form1').submit(function() {
            //    $(this).ajaxSubmit(options);
            //    return false;
            //});
        });

        function showResponse(data) {
            try {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    jAlert(data.msg, "提示");
                    window.location.href = "message.jsp";
                    parent.leftFrame.location.href = "left_menu.jsp";
                } else if (data.ret == "3") {
                    jAlert(data.msg, "提示");
                }
            } catch (err) {
                window.location.href = "message.jsp";
                parent.leftFrame.location.href = "left_menu.jsp";
            }
        }

        /* common.js中有公用的openWin方法
        function openWin(url,width,height)
        {
          var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,top="+(screen.height-600)/2+",left=" +(screen.width-800)/2 + ",fullscreen=3,width=792,height=550 ");
        }
        */
        var GetDate = "";

        function SelectDate(ObjName, FormatDate) {
            var PostAtt = new Array;
            PostAtt[0] = FormatDate;
            PostAtt[1] = o(ObjName);

            GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt, "dialogWidth:286px;dialogHeight:220px;status:no;help:no;");
        }

        function SetDate() {
            o(ObjName).value = GetDate;
        }

        function SelectDateTime(objName) {
            var dt = showModalDialog("../util/calendar/time.htm", "", "dialogWidth:266px;dialogHeight:125px;status:no;help:no");
            if (dt != null)
                o(objName).value = dt;
        }

        function getRadioValue(str) {
            var r = document.getElementsByName(str);
            for (var i = 0; i < r.length; i++) {
                if (r[i].checked) {
                    return r[i].value;
                }
            }
        }

        function setVisibility() {
            if (getRadioValue('send_now') == "no") {
                form.attributes['action'].value = "send.jsp?op=addDraft";
                document.getElementById("sendButton").disabled = "disabled";
                document.getElementById("sendButton").setAttribute("class", "grey_btn_90");
                document.getElementById("saveButton").setAttribute("class", "blue_btn_90");
            } else {
                form.attributes['action'].value = "send.jsp?op=saveMessage";
                document.getElementById("sendButton").disabled = "";
                document.getElementById("sendButton").setAttribute("class", "blue_btn_90");
                document.getElementById("saveButton").setAttribute("class", "grey_btn_90");
            }
        }

        function saveDraft() {
            $("#form1").attr("action", "send.jsp?op=addDraft");
            //document.getElementById("form1").action="send.jsp?op=addDraft";
            //form.action = "send.jsp?op=addDraft";
            if (!form_onsubmit()) {
                document.getElementById("saveButton").disabled = "";
                return;
            } else {
                document.getElementById("saveButton").disabled = "disabled";
                $("#form1").attr("action", "send.jsp?op=addDraft");
                //document.getElementById("form1").action = "send.jsp?op=addDraft";
                form.submit();
            }
        }

        /**
         function send() {
	o("sendButton").disabled = true;
	if (form_onsubmit()) {
		form.submit();
	}
	else
		o("sendButton").disabled = false;
}*/

        function setPerson(deptCode, deptName, user, userRealName) {
            if (type == 1) {
                form.receiver.value = user;
                form.userRealName.value = userRealName;
            } else if (type == 2) {
                form.receiver1.value = users;
                form.userRealName1.value = userRealNames;
            } else if (type == 3) {
                form.receiver2.value = users;
                form.userRealName2.value = userRealNames;
            }
        }

        function getSelUserNames() {
            if (type == 1) {
                return form.receiver.value;
            } else if (type == 2) {
                return form.receiver1.value;
            } else if (type == 3) {
                return form.receiver2.value;
            }
        }

        function getSelUserRealNames() {
            if (type == 1) {
                return form.userRealName.value;
            } else if (type == 2) {
                return form.userRealName1.value;
            } else if (type == 3) {
                return form.userRealName2.value;
            }
        }

        var type;

        function openWinUsers(i) {
            /*
            if (!isApple()){
                    if (navigator.userAgent.indexOf('Firefox') >= 0 || navigator.userAgent.toLowerCase().match(/chrome/)!=null){
                        openWin('../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;');
                    }else{
                        showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;');
                    }
            }else{
                openWin('../user_multi_sel.jsp', 600, 480);
            }*/
            //showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;');
            type = i;
            openWin('../user_multi_sel.jsp', 800, 600);
        }

        function openWinPersonGroup(i) {
            //showModalDialog('../user/persongroup_user_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;');
            type = i;
            openWin('../user/persongroup_user_multi_sel.jsp', 800, 600)
        }

        <%
        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(name);
        int messageToMaxUser = usd.getMessageToMaxUser();
        %>

        function getDept() {
            return "<%=usd.getMessageToDept()%>";
        }

        var messageToMaxUser = <%=messageToMaxUser%>;

        function setUsers(users, userRealNames) {
            var ary = users.split(",");
            var len = ary.length;
            if (len > messageToMaxUser) {
                jAlert("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！", "提示");
                return;
            }
            if (type == 1) {
                form.receiver.value = users;
                form.userRealName.value = userRealNames;
            } else if (type == 2) {
                form.receiver1.value = users;
                form.userRealName1.value = userRealNames;
            } else if (type == 3) {
                form.receiver2.value = users;
                form.userRealName2.value = userRealNames;
            }
        }

        function window_onload() {
            <%
            if (!netdiskFiles.equals("")) {
            %>
            setNetdiskFiles("<%=netdiskFiles%>");
            <%
            }
            %>


        }

        function form_onsubmit() {
            errmsg = "";
            if (form.receiver.value == "")
                errmsg += "请填写收件人！\n"
            if (form.title.value == "")
                errmsg += "请填写标题！\n"
            if (form.title.value.length > 200)
                errmsg += "不能大于200字符长度！\n"
            if (uEditor.getContentTxt() == "")
                errmsg += "请填写内容！\n"
            if (getRadioValue('send_now') == "no") {
                if (form.date.value == "") {
                    errmsg += "请选择定时发送日期！\n";
                }
                form.send_time.value = form.date.value;
            }
            if (errmsg != "") {
                jAlert(errmsg, "提示");
                document.getElementById("sendButton").disabled = "";
                return false;
            }
            document.getElementById("sendButton").disabled = "disabled";
            return true;
        }
    
    </script>
</head>
<body onLoad="window_onload()">
<%
    if (op.equals("saveMessage")) {
        try {
            isSuccess = Msg.AddMsg(application, request);
        } catch (ErrMsgException e) {
            //json.put("ret","3");
            //json.put("msg",e.getMessage());
            //out.print(json);
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }
        if (isSuccess) {
            //json.put("ret","1");
            //json.put("msg","操作成功!");
            //out.print(json.toString());
            out.print("<script>parent.leftFrame.location='left_menu.jsp';</script>");
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "message.jsp"));
            return;
        } else {
            //json.put("ret","0");
            //json.put("msg","操作失败!");
            //out.print(json);
            out.print(StrUtil.jAlert("操作失败！", "提示"));
            return;
        }
    } else if (op.equals("addDraft")) {
        try {
            isSuccess = Msg.AddDraftMsg(application, request);
        } catch (ErrMsgException e) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e
                    .getMessage()));
            return;
        }
        
        if (isSuccess) {
            out.print("<script>parent.leftFrame.location='left_menu.jsp?op=1';</script>");
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "listdraft.jsp"));
            
        } else {
            out.print(StrUtil.jAlert("操作失败！", "提示"));
        }
        return;
    } %>
<div class="spacerH"></div>
<form action="send.jsp?op=saveMessage" id="form1" method="post" enctype="multipart/form-data" name="form">
    <table align="center" cellspacing="0" class="tabStyle_1 percent100">
        <tr>
            <td class="tabStyle_1_title" colspan="2"
                style="background-color: #D4E2E6; text-align: left">
                <%
                    if (isShowBack.equals("")) {
                %>
                <img src="../skin/bluethink/images/message/message_back.png"
                     onclick="location.href='message.jsp'"
                     style="cursor: pointer; height: 27px; width: 65px; margin-top: 5px;"/>
                <%} %>
                
                <img src="../skin/bluethink/images/message/message_furbish.png"
                     onclick="window.location.reload()"
                     style="cursor: pointer; height: 27px; width: 65px;margin-top: 5px;"/>
            </td>
        </tr>
        <tr>
            <td align="right">
                收件人：
            </td>
            <td align="left">
                <input type="hidden" name="receiver" class="input1"
                       value="<%=receiver%>">
                <%
                    String userRealName = "";
                    if (!receiver.equals("")) {
                        String[] ary = StrUtil.split(receiver, ",");
                        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
                        for (int i = 0; i < ary.length; i++) {
                            ud = ud.getUserDb(ary[i]);
                            if (!ud.isLoaded())
                                continue;
                            if (userRealName.equals(""))
                                userRealName = ud.getRealName();
                            else
                                userRealName += "," + ud.getRealName();
                        }
                    }
                %>
                <input type="text" readonly name="userRealName" class="input1"
                       size="110" value="<%=userRealName%>" style="margin-bottom:4px;">
                <input type="hidden" name="isDraft" value="false">
                <input type="hidden" name="action"
                       value="<%=ParamUtil.get(request, "action")%>">
                <p>
                    <img src="../skin/bluethink/images/message/message_icon_uesr.png"
                         style="cursor: pointer; height: 15px; width: 15px;"/>
                    
                    <a href="javascript:;" onClick="openWinUsers(1)">选择用户</a>&nbsp;&nbsp;
                    <span style="display:none">
						<img
                                src="../skin/bluethink/images/message/message_icon_usergroup.png"
                                style="cursor: pointer; height: 15px; width: 15px;"/>
						<a href="javascript:;" onClick="openWinPersonGroup(1)">我的用户组</a>
						&nbsp;&nbsp;</span>|&nbsp;&nbsp;
                    <span id="spca"><a href="javascript:;" onClick="addcs()" style="text-decoration:underline">添加抄送</a></span>
                    <span id="spcs" style="display:none"><a href="javascript:;" onClick="deletecs()"
                                                            style="text-decoration:underline">删除抄送</a></span>
                    &nbsp;-&nbsp;
                    <span id="spma"><a href="javascript:;" onClick="addms()" style="text-decoration:underline">添加密送</a></span>
                    <span id="spms" style="display:none"><a href="javascript:;" onClick="deletems()"
                                                            style="text-decoration:underline">删除密送</a></span>
            </td>
        </tr>
        
        <tr id="trc" style="display:none">
            <td align="right">
                抄送：
            </td>
            <td align="left">
                <input type="hidden" name="receiver1" id="receiver1" class="input1"
                       value="<%=receiver1%>">
                <%
                    String userRealName1 = "";
                    if (!receiver1.equals("")) {
                        String[] ary = StrUtil.split(receiver1, ",");
                        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
                        for (int i = 0; i < ary.length; i++) {
                            ud = ud.getUserDb(ary[i]);
                            if (!ud.isLoaded())
                                continue;
                            if (userRealName1.equals(""))
                                userRealName1 = ud.getRealName();
                            else
                                userRealName1 += "," + ud.getRealName();
                        }
                    }
                %>
                <input type="text" readonly name="userRealName1" id="userRealName1" class="input1"
                       size="110" value="<%=userRealName1%>" style="margin-bottom:4px;">
                <p>
                    <img src="../skin/bluethink/images/message/message_icon_uesr.png"
                         style="cursor: pointer; height: 15px; width: 15px;"/>
                    
                    <a href="javascript:;" onClick="openWinUsers(2)">选择用户</a>&nbsp;&nbsp;
                    <span style="display:none">
						<img
                                src="../skin/bluethink/images/message/message_icon_usergroup.png"
                                style="cursor: pointer; height: 15px; width: 15px; "/>
						<a href="javascript:;" onClick="openWinPersonGroup(2)">我的用户组</a>
                        </span>
            </td>
        </tr>
        
        <tr id="trm" style="display:none">
            <td align="right">
                密送：
            </td>
            <td align="left">
                <input type="hidden" name="receiver2" class="input1"
                       value="<%=receiver2%>">
                <%
                    String userRealName2 = "";
                    if (!receiver2.equals("")) {
                        String[] ary = StrUtil.split(receiver2, ",");
                        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
                        for (int i = 0; i < ary.length; i++) {
                            ud = ud.getUserDb(ary[i]);
                            if (!ud.isLoaded())
                                continue;
                            if (userRealName2.equals(""))
                                userRealName2 = ud.getRealName();
                            else
                                userRealName2 += "," + ud.getRealName();
                        }
                    }
                %>
                <input type="text" readonly name="userRealName2" id="userRealName2" class="input1"
                       size="110" value="<%=userRealName2%>" style="margin-bottom:4px;">
                <p>
                    <img src="../skin/bluethink/images/message/message_icon_uesr.png"
                         style="cursor: pointer; height: 15px; width: 15px;"/>
                    
                    <a href="javascript:;" onClick="openWinUsers(3)">选择用户</a>&nbsp;&nbsp;
                    <span style="display:none">
						<img
                                src="../skin/bluethink/images/message/message_icon_usergroup.png"
                                style="cursor: pointer; height: 15px; width: 15px; "/>
						<a href="javascript:;" onClick="openWinPersonGroup(3)">我的用户组</a>
                        </span>
            </td>
        </tr>
        
        <tr>
            <td align="right">
                主题：
            </td>
            <td align="left">
                <input type="text" name="title" class="input1" size="110"
                       value="<%=title%>"/>
            </td>
        </tr>
        <tr>
            <td align="right">
                正文：
            </td>
            <td align="left" width="90%">
                
                <textarea id="hidContent" name="hidContent" style="display:none" cols="95"
                          rows="16"><%=content%></textarea>
                
                <div id="myEditor" style="height:200px;"></div>
            
            
            </td>
        </tr>
        <tr>
            <td align="right">
                附件：
            </td>
            <td align="left">
                <script>initUpload()</script>
            </td>
        </tr>
        
        <%
            if (mmd != null) {
        %>
        <tr>
            <td align="right">
                邮箱附件：
            </td>
            <td align="left">
                <%
                    java.util.Vector vAttach = mmd.getAttachments();
                    java.util.Iterator attir = vAttach.iterator();
                    while (attir.hasNext()) {
                        Attachment att = (Attachment) attir.next();
                %>
                <div id="mailFile<%=att.getId()%>">
                    <input name="mailFiles" value="<%=att.getId()%>" type="hidden"/><%=att.getName()%>&nbsp;&nbsp;
                    <a target="_self" href="javascript:;"
                       onClick="o('mailFile<%=att.getId()%>').outerHTML=''"
                       style="color: red; font-size: 18px">×</a>
                </div>
                <%
                    }
                %>
            </td>
        </tr>
        <%}%>
        <tr id="adOpt" style="display:none">
            <td colspan="2" style="padding:0px">
                <table cellspacing="0" style="width:100%">
                    <%
                        com.redmoon.clouddisk.Config cloudCfg = com.redmoon.clouddisk.Config.getInstance();
                        //System.out.println(cloudCfg.getProperty("isUsed"));
                        if (cloudCfg.getBooleanProperty("isUsed")) {
                    %>
                    
                    <tr>
                        <td align="right">
                            网盘文件：
                        </td>
                        <td align="left">
                            <div class="message_fj_btn2"
                                 onClick="openWin('../netdisk/clouddisk_list.jsp?mode=select', 800, 600)">
                                网盘文件
                            </div>
                            <div id="netdiskFilesDiv"
                                 style="line-height: 1.5; margin-top: 5px"></div>
                        </td>
                    </tr>
                    <%} %>
                    <tr>
                        <td align="right">
                            发送时间：
                        </td>
                        <td align="left" width="90%">
                            <input type="radio" name="send_now" value="yes" id="send_now_0"
                                   checked="checked" onClick="setVisibility()"/>
                            <label for="send_now_0" onClick="setVisibility()">
                                立即发送
                            </label>
                            <br/>
                            <input type="radio" name="send_now" value="no" id="send_now_1"
                                   onClick="setVisibility()"/>
                            <label for="send_now_1" onClick="setVisibility()">
                                定时发送
                            </label>
                            <input name="date" id="date" readonly/>
                            <input name="send_time" type="hidden"/>
                        </td>
                    </tr>
                    <%
                        if (com.redmoon.oa.sms.SMSFactory.isUseSMS()
                                && privilege.isUserPrivValid(request, "sms")) {
                    %>
                    <tr>
                        <td align="right" style="white-space: nowrap;">
                            手机短信提醒：
                        </td>
                        <td align="left">
                            <input type="radio" name="isToMobile" value="true"
                                   id="isToMobile_0" checked="checked"/>
                            <label for="isToMobile_0">
                                是
                            </label>
                            <input type="radio" name="isToMobile" value="false"
                                   id="isToMobile_1"/>
                            <label for="isToMobile_1">
                                否
                            </label>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    <tr>
                        <td align="right">
                            保存到发件箱：
                        </td>
                        <td align="left">
                            <input type="radio" name="isToOutBox" value="true"
                                   id="isToOutBox_0" checked="checked"/>
                            <label for="isToOutBox_0">
                                是
                            </label>
                            <input type="radio" name="isToOutBox" value="false"
                                   id="isToOutBox_1"/>
                            <label for="isToOutBox_1">
                                否
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            是否需要回执：
                        </td>
                        <td align="left">
                            <input type="radio" name="receipt_state" value="1"
                                   id="receipt_state_0"/>
                            <label for="receipt_state_0">
                                是
                            </label>
                            <input type="radio" name="receipt_state" value="0"
                                   id="receipt_state_1" checked="checked"/>
                            <label for="receipt_state_1">
                                否
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            消息等级：
                        </td>
                        <td align="left">
                            <input type="radio" name="msg_level" value="0" id="msg_level_0"
                                   checked="checked"/>
                            <label for="msg_level_0">
                                普通
                            </label>
                            <input type="radio" name="msg_level" value="1" id="msg_level_1"/>
                            <label for="msg_level_1">
                                紧急
                            </label>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input class="blue_btn_90" type="button" id="sendButton" name="sendButton"
                       value=" 发 送 "/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                
                <input class="grey_btn_90" type="button" id="saveButton" name="saveButton"
                       value="保存草稿" onClick="saveDraft()"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                
                <input class="grey_btn_90" type="button" id="adButton" name="adButton"
                       value="高级功能" onClick="advancedOption()"/>
            </td>
        </tr>
    </table>
</form>
<div id="result"></div>
</body>
<script>
    var uEditor;

    function setNetdiskFiles(ids) {
        getNetdiskFiles(ids);
    }

    function doGetNetdiskFiles(response) {
        var rsp = response.responseText.trim();
        o("netdiskFilesDiv").innerHTML += rsp;
    }

    var errFunc = function (response) {
        jAlert(response.responseText, "提示");
    }

    function getNetdiskFiles(ids) {
        var str = "ids=" + ids;
        var myAjax = new cwAjax.Request(
            "<%=cn.js.fan.web.Global.getFullRootPath(request)%>/netdisk/ajax_getfile.jsp",
            {
                method: "post",
                parameters: str,
                onComplete: doGetNetdiskFiles,
                onError: errFunc
            }
        );
    }

    function advancedOption() {
        if ($('#adOpt').is(':hidden')) {
            $('#adOpt').show();
        } else {
            $('#adOpt').hide();
        }
    }

    $(function () {

        uEditor = UE.getEditor('myEditor', {
            initialContent: $("#hidContent").val(),//初始化编辑器的内容
            //allowDivTransToP: false,//阻止转换div 为p
            toolleipi: true,//是否显示，设计器的 toolbars
            textarea: 'content',
            enableAutoSave: false,
            //选择自己需要的工具按钮名称,此处仅选择如下五个
            toolbars: [[
                'fullscreen', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', '|', 'forecolor',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify'
            ]],
            //focus时自动清空初始化时的内容
            //autoClearinitialContent:true,
            //关闭字数统计
            wordCount: false,
            //关闭elementPath
            elementPathEnabled: false,
            //默认的编辑区域高度
            initialFrameHeight: 295,
            initialFrameWidth: 756
            ///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
            //更多其他参数，请参考ueditor.config.js中的配置项
        });

        $('#sendButton').click(function() {
            $('#sendButton').attr('disabled', true);
            $('body').showLoading();
            $('#form1').submit();
        })
    });

    function addcs() {
        o("trc").style.display = "";
        o("spca").style.display = "none";
        o("spcs").style.display = "";
    }

    function deletecs() {
        o("receiver1").value = "";
        o("userRealName1").value = "";
        o("trc").style.display = "none";
        o("spca").style.display = "";
        o("spcs").style.display = "none";
    }

    function addms() {
        o("trm").style.display = "";
        o("spma").style.display = "none";
        o("spms").style.display = "";
    }

    function deletems() {
        o("receiver2").value = "";
        o("userRealName2").value = "";
        o("trm").style.display = "none";
        o("spma").style.display = "";
        o("spms").style.display = "none";
    }
</script>
</html>
