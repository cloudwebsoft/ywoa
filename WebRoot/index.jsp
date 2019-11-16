<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.security.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.cloudwebsoft.framework.web.UserAgentParser" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="login" scope="page" class="cn.js.fan.security.Login"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    // 与nocache.jsp联用，以使得从oa.jsp回退至index.jsp时，session自动过期，只能置于index.jsp起始处，因为在Response输出后是无法创建Session
    privilege.logout(request, response);

    String skincode = ParamUtil.get(request, "skincode");
    if (skincode == null || skincode.equals("")) {
        skincode = UserSet.getSkin(request);
        if (skincode == null || skincode.equals("")) {
            skincode = UserSet.defaultSkin;
        }
    }
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
    com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
    com.redmoon.clouddisk.Config cfgNd = com.redmoon.clouddisk.Config.getInstance();
    boolean isUsed = cfgNd.getBooleanProperty("isUsed"); //判断云盘时候启动
    //是否需要重启tomcat
    String isRestart = cfg.get("isRestart");
    String browserValid = cfg.get("browserValid");
    boolean isBrowserForbid = !"".equals(browserValid);
    
    boolean systemIsOpen = cfg.getBooleanProperty("systemIsOpen");
    if (!systemIsOpen) {
        String op = ParamUtil.get(request, "op");
        String loginParam = cfg.get("systemLoginParam");
        // 判断登录参数是否相符
        if (!op.equals(loginParam)) {
            String systemStatus = cfg.get("systemStatus");
%>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css"/>
<%
            out.print(SkinUtil.makeInfo(request, systemStatus));
            return;
        }
    }

    String appName = cfg.get("enterprise");

    boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
    boolean reverseRTXLogin = !ParamUtil.get(request, "reverseRTXLogin").equals("false");

    String mainTitle = ParamUtil.get(request, "mainTitle");
    String mainPage = ParamUtil.get(request, "mainPage");

    String tokenHidden = cn.js.fan.security.Form.getTokenHideInput(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <%
        boolean isLte = skincode.equals(SkinMgr.SKIN_CODE_LTE);
        if (!isLte) { %>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8"/>
    <%} %>
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="Cache-Control" content="no-cache, must-revalidate"/>
    <meta http-equiv="expires" content="Wed, 26 Feb 1997 08:21:57 GMT"/>
    <title><%=appName%>
    </title>
    <link href="<%=skinPath%>/login.css" rel="stylesheet" type="text/css"/>
    <link href="images/favicon.ico" rel="SHORTCUT ICON"/>
    <%@ include file="inc/nocache.jsp" %>
    <%
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        if (scfg.isDefendBruteforceCracking()) {
            Login.initlogin(request, "redmoonoa");
        }
    %>
    <link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css"/>
    <link type="text/css" rel="stylesheet" href="<%=skinPath%>/jquery-ui/jquery-ui.css"/>
    <script src="inc/common.js"></script>
    <script src="js/jquery1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery.toaster.js"></script>
    <script src="js/jquery-ui/jquery-ui.js"></script>
    <script src="js/jquery.form.js"></script>
    <script src="js/check_browser.js"></script>
    <script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        // 浏览器是否合法
        var isBrowserValid = true;
        var browserInfo = "";

        // 判断是否为手机端，如果是则转至手机端界面
        var isMobile = false;
        var ua = navigator.userAgent.toLowerCase();
        if (ua.match(/MicroMessenger/i) == 'micromessenger') { // 微信浏览器判断
            isMobile = true;
        } else if (ua.match(/QQ/i) == 'qq') { // QQ浏览器判断
            // isMobile = true;
        } else if (ua.match(/WeiBo/i) == "weibo") {
            isMobile = true;
        } else if (/(iPhone|iPad|iPod|iOS)/i.test(ua)) {
            isMobile = true;
        } else if (/(Android)/i.test(ua)) {
            isMobile = true;
        }
        if (isMobile) {
            window.location.href = "<%=request.getContextPath()%>/wap/index.jsp";
        }
    </script>
    <script language="vbscript" id="clientEventHandlersVBS">
    <!--
    dim strAccount
    dim strSignature

    Function GetSignature()
        on error resume next
        Set RTXCRoot = RTXAX.GetObject("KernalRoot")
        Set rtcData = RTXCRoot.Sign

        strAccount = RTXCRoot.Account
        strSignature = rtcData.GetString("Sign")

        If Err.Number <> 0 Then
            'MsgBox "Error # " & CStr(Err.Number) & " " & Err.Description
            Err.Clear   ' Clear the error.
            return false
        Else
            loginForm.name.value = strAccount
            loginForm.signature.value = strSignature
            loginForm.op.value = "txt"
            loginForm.submit
            return true
        End If
    End Function

    Sub Window_OnLoad()
        dim re
        re = false

    <%if (isRTXUsed && reverseRTXLogin) {%>
        re = GetSignature()
    <%}%>
        If re=false Then
            // loginForm.name.focus
        End If
    end Sub
    -->
    </script>
    <script language="javascript">
        var _loginmode = 0;//0:用户名密码 方式 1:keyid 方式
        var _keyid = '';
        var _ischecking = 0; //0未检测 1检测中
        var _isInfixing = 0; //0未插入 1插入中
        var tempkeyid = '';
        var s_simnew;
        var intervalId;//定时器句柄

        var digitArray = new Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f');

        function toHex(n) {
            var result = ''
            var start = true;
            for (var i = 32; i > 0;) {
                i -= 4;
                var digit = (n >> i) & 0xf;
                if (!start || digit != 0) {
                    start = false;
                    result += digitArray[digit];
                }
            }
            return (result == '' ? '0' : result);
        }

        function init() {
            try {
                if (navigator.userAgent.indexOf("MSIE") > 0 && !navigator.userAgent.indexOf("opera") > -1) {
                    s_simnew = new ActiveXObject("Syunew6A.s_simnew6");
                } else {
                    s_simnew = document.getElementById('s_simnew61');
                }
                intervalId = window.setInterval(load, 1000); //启动事件监听
            } catch (e) {
                //alert("Syunew6A.s_simnew6 创建失败!");
                window.clearInterval(intervalId);
            }
        }

        function load() {
            if (isIE()) {
                var DevicePath = s_simnew.FindPort(0);
                var keyid = toHex(s_simnew.GetID_1(DevicePath)) + toHex(s_simnew.GetID_2(DevicePath));
                if (keyid == "ffffffd2ffffffd2" || keyid == '' || keyid == "ffffffa4ffffffa4") {
                    _isInfixing = 0; //0未插入
                    _keyid = keyid;
                } else {
                    _isInfixing = 1; //1插入中
                    _keyid = keyid;
                }
                if (_isInfixing == 0)//0未插入
                {
                    if (tempkeyid != '') {
                        extractUsb();//拔出USB逻辑
                    }
                } else {
                    if (tempkeyid != _keyid) {
                        infixUsb();//插入USB逻辑
                    }
                }
            }
        }

        $(function () {
            <%
            if (scfg.isRememberUserName()) {
            %>
            o("name").value = unescape(get_cookie("name"));
            if (o("name").value == "")
                o("name").focus();
            else
                o("pwd").focus();

            o("pwd").value = unescape(get_cookie("pwd"));
            if (o("pwd").value == "") {
                $('.isSavePwd').removeAttr("checked");
            } else {
                $('.isSavePwd').attr("checked", "checked");
            }
            <%}%>
        });

        function onunload() {
            var skincode = '<%=skincode%>';
            var expdate = new Date();
            var expday = 60;
            expdate.setTime(expdate.getTime() + (24 * 60 * 60 * 1000 * expday));
            document.cookie = "skincode" + "=" + skincode + ";expires=" + expdate.toGMTString();
            document.cookie = "name" + "=" + escape(o("name").value) + ";expires=" + expdate.toGMTString();
            if ($('.isSavePwd').is(":checked")) {
                document.cookie = "pwd" + "=" + escape(o("pwd").value) + ";expires=" + expdate.toGMTString();
            } else {
                document.cookie = "pwd" + "=;expires=" + expdate.toGMTString();
            }
        }

        $(document).ready(function () {
            o("os").value = getOS();
            controlPwdInputValue();
        });

        function controlPwdInputValue() {
            var input = document.getElementById('loginPwd');
            input.onfocus = function () {
                if (this.value == '请输入您的密码') {
                    this.value = '';
                    this.type = 'password';
                }
                ;
            };
            input.onblur = function () {
                if (!this.value) {
                    this.value = '请输入您的密码';
                }
                ;
            };
        }

        function checkForm(formData, jqForm, options) {
            var errmsg = "";
            if (loginForm.name.value == "")
                errmsg += "请输入用户名！\n"
            if (loginForm.pwd.value == "")
                errmsg += "请输入密码！\n"
            if (errmsg != "") {
                jAlert(errmsg, "提示");
                return false;
            }
        }

        function showDiv(obj) {
            $(obj).show();
            center(obj);
            $(window).scroll(function () {
                center(obj);
            });
            $(window).resize(function () {
                center(obj);
            });
        }

        function center(obj) {
            var windowWidth = document.documentElement.clientWidth;
            var windowHeight = document.documentElement.clientHeight;
            var popupHeight = $(obj).height();
            var popupWidth = $(obj).width();
            $(obj).css({
                "position": "absolute",
                "top": (windowHeight - popupHeight) / 2 + $(document).scrollTop(),
                "left": (windowWidth - popupWidth) / 2
            });
        }

        function showError(pRequest, pStatus, pErrorText) {
            alert('pStatus=' + pStatus + '\r\n\r\n' + 'pErrorText=' + pErrorText + '\r\n\r\npRequest=' + pRequest);
        }

        function onload() {
            init();
            checkWebEditInstalled();
            <%=isRTXUsed?"Window_OnLoad()":""%>;
        }
    </script>
</head>
<body onload="onload()" onunload="onunload()" scroll="no">
<div class="logincontent">
    <div class="login_topbox">
        <div class="login_topleft"><img src="<%=skinPath%>/images/login/login_logo.png"/></div>
    </div>
    <div class="loginbar_frame">
        <div class="loginbar_computer_bg"></div>
        <div class="loginbar_bgbox">
            <div class="loginbar_posit ">
                <div class="loginbar_case">
                    <form id="loginForm" name="loginForm" method="post" action="login_oa_ajax.jsp">
                        <div id="errMsgBox" class="input_box1" style='display:none'>
                            <img id="errMsgImg" src="<%=skinPath%>/images/login/login_errorico.png" width="18" height="18"/>
                            <span id="errMsgSpan">用户名不正确请重新输入</span>
                        </div>
                        <div class="input_box2">
                            <div class="span_user"></div>
                            <span></span>
                            <input type="text" id="loginName" name="name" value="请输入您的用户名" onKeyPress="return name_presskey(event)"/>
                        </div>
                        <div class="input_box2">
                            <div class="span_code"></div>
                            <span>
                                <input type="password" id="loginPwd" name="pwd" value="请输入您的密码"/>
                            </span>
                        </div>
                        <div class="input_box4">
                            <%
                                if (scfg.getBooleanProperty("isRememberPwdDisplay")) {
                            %>
                            <input type="checkbox" id="isSavePwd" name="isSavePwd" class="isSavePwd" checked="checked"/>
                            记住密码
                            <%}%>
                            <input name="signature" type="hidden"/>
                            <input name="op" type="hidden"/>
                            <input name="mainTitle" type="hidden" value="<%=mainTitle%>"/>
                            <input name="mainPage" type="hidden" value="<%=mainPage%>"/>
                            <input id="os" name="os" type="hidden"/>
                            <%=tokenHidden%>
                            <span id="send_to_desktop" data-step="1" data-intro="在桌面生成快捷方式" data-position="top">
				                <a href="send_to_desktop.jsp" title="发送至桌面快捷方式"><img alt="发送至桌面快捷方式" src="images/send_to_desktop.png"/></a>
			                </span>
                        </div>
                        <div class="login_button" onClick="loginSubmit()"><a></a></div>
                        <div class="QRcode" title="Android手机直接扫描二维码,Apple手机扫描二维码进入App Store下载"><img src="<%=skinPath%>/images/login/QRcode.png" width="75" height="21"/></div>
                        <div id="loginDownload" style="foat:left; width:120px;"><a id="linkDownload" href="activex/oa_client.exe">下载客户端</a>
                        </div>
                        <%
                            if (scfg.getIntProperty("isPwdCanReset")==1) {
                        %>
                        <div style="float: left; width:80px;"><a href="public/user_reset_pwd.jsp" title="通过邮箱重置密码">重置密码</a></div>
                        <%
                            }
                        %>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="qrCodeBox" style="position:absolute; display:none; width:139px; height:178px; background-color:white">
    <div id="androidQRCode">
        <img id="QRCodeimg" src="images/<%=cfg.get("qrcode_mobile_png_path")%>"/>
        <div style="text-align:center;"><a href="<%=cfg.get("qrcode_andriod_download_path")%>">手机客户端</a><br/></div>
    </div>
</div>
<script>
    if (!isIE8) {
        var strObj = '<OBJECT ID="Spindial1" WIDTH=3 HEIGHT=3 style="visibility: hidden;"';
        strObj += ' CLASSID="CLSID:B3AF7FDF-4123-499A-B38E-EDCE3821C10F"';
        strObj += ' CODEBASE="activex/cloudym.CAB#Version=1,2,0,1"></OBJECT>';
        $('body').append(strObj);
    }
</script>
<embed id="s_simnew61" type="application/npsyunew6-plugin" hidden="true"></embed><!--创建firefox,chrome等插件-->
<%if (isRTXUsed) {%>
<object id=RTXAX data=data:application/x-oleobject;base64,fajuXg4WLUqEJ7bDM/7aTQADAAAaAAAAGgAAAA== classid=clsid:5EEEA87D-160E-4A2D-8427-B6C333FEDA4D viewastext>
</object>
<%
    }
    String myDivPath = skinPath + "/login.jsp";
%>
<jsp:include page="<%=myDivPath %>"/>
<div class="logincontent-footer"></div>
</body>
<%
    String ip = Global.server;
    boolean isDomain = IpUtil.isDomain(ip);
    boolean isInnerIp = IpUtil.isInnerIP(ip);
    cfg.get("server");
%>
<script language="javascript">
    function viewandroid() {
        $("#QRCodeimg").attr("src", "images/yimioa_android.png");
    }

    function viewios() {
        $("#QRCodeimg").attr("src", "images/yimioa_ios.png");
    }

    var redirect;
    var imgPath = "<%=skinPath%>";

    function loginSubmit() {
        //插入USBkey 检测中
        if (_isInfixing == 1 && _ischecking == 1) {
            jAlert("正在为您检测KEY，请稍后登录", "提示");
            return;
        }

        if (_loginmode == 1) {
            loginByUsb();//usbkey方式
        } else {
            loginByname();//用户名密码方式
        }
    }

    function loginByname() {
        var userName = $('#loginName').val();
        var d = new Date();
        var today = d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
        var last_info_date = unescape(get_cookie("last_info_date"));
        if (last_info_date != '-1') {
            if (userName == 'admin') {
                if (last_info_date != today) {
                    if (!<%=isDomain%>) {
                        if (<%=isInnerIp%>) {
                            jConfirmEx("为确保您的系统正常使用，系统建议您先进行初始化设置<br/>点击“确定”进行配置，点击“取消”直接登录！", "提示", function (r) {
                                var expdate = new Date();
                                var expday = 60;
                                expdate.setTime(expdate.getTime() + (24 * 60 * 60 * 1000 * expday));
                                if (r == 0) {
                                    document.cookie = "last_info_date" + "=" + today + ";expires=" + expdate.toGMTString();
                                    doLogin();
                                } else if (r == 1) {
                                    document.cookie = "last_info_date" + "=" + today + ";expires=" + expdate.toGMTString();
                                    window.location.href = 'setup/setup3.jsp?db_type=mysql&isHideStep=1';
                                } else {
                                    today = '-1';
                                    document.cookie = "last_info_date" + "=" + today + ";expires=" + expdate.toGMTString();
                                    doLogin();
                                }

                            });
                        } else {
                            doLogin();
                        }
                    } else {
                        doLogin();
                    }
                } else {
                    doLogin();
                }
            } else {
                doLogin();
            }
        } else {
            doLogin();
        }
    }

    // 登录
    function doLogin() {
        if (!isBrowserValid) {
            jAlert("请勿使用" + browserInfo.toUpperCase() + "浏览器，请换用<%=browserValid%>", "提示");
            return;
        }
        // 某些浏览器下不兼容
        // $("#loginForm").submit();
        var userName = encodeURIComponent($('#loginName').val());
        $.ajax({
            url: "login_oa_ajax.jsp",
            data: {
                name: userName,
                pwd: $('#loginPwd').val(),
                signature: o("signature").value,
                op: o("op").value,
                mainTitle: o("mainTitle").value,
                mainPage: o("mainPage").value,
                os: o("os").value,
                form_token: o("<%=Form.TOKEN%>").value,
                isSavePwd: o("isSavePwd").value
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                // 过滤掉其它字符，只保留JSON字符串
                var m = data.match(/\{.*?\}/gi);
                if (m != null) {
                    if (m.length == 1) {
                        data = m[0];
                    }
                }

                try {
                    data = jQuery.parseJSON(data);
                } catch (e) {
                    alert(data);
                    return;
                }

                if (data == null)
                    return;

                data.msg = data.msg.replace(/\\r/ig, "<BR>");

                if (data.ret == "0") {
                    $('#errMsgBox').show();
                    $('#errMsgImg').show();
                    $('#errMsgSpan').html(data.msg);
                    return;
                } else {
                    $('#errMsgBox').hide();
                    window.location.href = data.redirect;
                }
            },
            complete: function (XMLHttpRequest, status) {
                // $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function loginByUsb() {
        $.ajax({
            url: "login_oa_ajax.jsp",
            data: {
                name: '',
                pwd: '',
                keyId: _keyid,
                signature: o("signature").value,
                op: o("op").value,
                mainTitle: o("mainTitle").value,
                mainPage: o("mainPage").value,
                os: o("os").value,
                form_token: o("<%=Form.TOKEN%>").value,
                isSavePwd: o("isSavePwd").value
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                // 过滤掉其它字符，只保留JSON字符串
                var m = data.match(/\{.*?\}/gi);
                if (m != null) {
                    if (m.length == 1) {
                        data = m[0];
                    }
                }

                try {
                    data = jQuery.parseJSON(data);
                } catch (e) {
                    alert(data);
                    return;
                }

                if (data == null)
                    return;

                data.msg = data.msg.replace(/\\r/ig, "<BR>");

                if (data.ret == "0") {
                    $('#errMsgBox').show();
                    $('#errMsgImg').show();
                    $('#errMsgSpan').html(data.msg);
                    return;
                } else {
                    $('#errMsgBox').hide();
                    window.location.href = data.redirect;
                }
            },
            complete: function (XMLHttpRequest, status) {
                // $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    // 插入USB
    function infixUsb() {
        checkUserByKeyId(_keyid);
        tempkeyid = _keyid;//防止相同keyid二次调用
    }

    // 拔出USB
    function extractUsb() {
        tempkeyid = '';//清空缓存的keyid
        $('#errMsgBox').hide();
        $('#errMsgImg').hide();
        $('#errMsgSpan').html('');
        _loginmode = 0;//用户名登录方式
        _ischecking = 0;
    }

    function sleep(milliseconds) {
        setTimeout(function () {
            var start = new Date().getTime();
            while ((new Date().getTime() - start) < milliseconds) {
                // Do nothing
            }
        }, 0);
    }

    // 检测用户信息
    function checkUserByKeyId(keyid) {
        $('#errMsgBox').show();
        $('#errMsgImg').hide();
        $('#errMsgSpan').html('KEY校验中，请稍后...');
        _ischecking = 1;//检测中
        // sleep(2000);//休眠2秒
        // 检测
        $.ajax({
            url: "checkuser_ajax.jsp",
            async: true,
            data: {
                keyid: keyid
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // $('#bodyBox').showLoading();
            },
            success: function (data, status) {
                _ischecking = 0;//未检测
                // 过滤掉其它字符，只保留JSON字符串
                var m = data.match(/\{.*?\}/gi);
                if (m != null) {
                    if (m.length == 1) {
                        data = m[0];
                    }
                }
                //alert(data);
                try {
                    data = jQuery.parseJSON(data);
                } catch (e) {
                    // 异常出错处理
                    $('#errMsgBox').show();
                    $('#errMsgImg').show();
                    $('#errMsgSpan').html('KEY校验失败，请尝试其它方式登录');
                    _loginmode = 0;//用户名登录方式
                    return;
                }
                if (data == null) {
                    $('#errMsgBox').show();
                    $('#errMsgImg').show();
                    $('#errMsgSpan').html('KEY校验失败，请尝试其它方式登录');
                    _loginmode = 0;//用户名登录方式
                    return;
                }

                data.msg = data.msg.replace(/\\r/ig, "<BR>");
                if (_isInfixing == 1)//插入中
                {
                    if (data.ret == "0") {
                        $('#errMsgBox').show();
                        $('#errMsgImg').show();
                        $('#errMsgSpan').html(data.msg);
                        _loginmode = 0;//用户名登录方式
                    } else {
                        $('#errMsgBox').show();
                        $('#errMsgImg').hide();
                        $('#errMsgSpan').html(data.msg);
                        _loginmode = 1;//keyid登录方式
                    }
                } else {
                    $('#errMsgBox').hide();
                    $('#errMsgImg').hide();
                    _loginmode = 0;//用户名登录方式
                }

            },
            complete: function (XMLHttpRequest, status) {
                // $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                _ischecking = 0;//未检测
                $('#errMsgBox').show();
                $('#errMsgImg').show();
                $('#errMsgSpan').html('KEY校验失败，请尝试其它方式登录');
                _loginmode = 0;//用户名登录方式
            }
        });
    }

    function name_presskey(event) {
        var event = event || window.event;
        var keyCode = event.keyCode;
        if (event.keyCode == 13) {
            event.keyCode = 9;
            if (typeof event.stopPropagation != "undefined") {
                event.stopPropagation();
            } else {
                event.cancelBubble = true;
            }
            o("loginPwd").focus();
            return false;
        }
    }

    $(function () {
        $("#loginPwd").keydown(function (e) {
            var e = e || event,
                keycode = e.which || e.keyCode;
            if (keycode == 13) {
                loginSubmit();
            }
        });
    });

    function checkWebEditInstalled() {
        var ProgID = "FHtmlEdit.HtmlEdit.1";
        var bCtlLoaded = true;
        var webeditObj;
        try {
            webeditObj = new ActiveXObject(ProgID);
        } catch (e) {
            bCtlLoaded = false;
        }

        if (!bCtlLoaded) {
            var options = {};
            var selectedEffect = "pulsate";
            if (isWow64()) {
                $('#linkDownload').attr("href", "activex/oa_client.exe");
            }
            $("#loginDownload").show(selectedEffect, options, 1000, callback);
            $("#loginDownload").css('float', 'left');
            return;
        }

        // 检测控件版本
        var isCtlVerCorrect = true;
        if (webeditObj && typeof (webeditObj.GetVersion) == "undefined") {
            isCtlVerCorrect = false;
        } else {
            if (webeditObj && webeditObj.GetVersion() != "6.10") {
                isCtlVerCorrect = false;
            }
        }

        if (!isCtlVerCorrect) {
            <%if(isUsed){%> //判断云盘是否启动
            var options = {};
            var selectedEffect = "pulsate";
            $("#loginDownload").html("<a id='loginDownload' href=\"activex/clouddisk.exe\">下载新版客户端</a>");
            $("#loginDownload").show(selectedEffect, options, 1000, callback);
            if (isWow64()) {
                $("#loginDownload").html("<a id='loginDownload' href=\"activex/clouddisk_x64.exe\">下载新版客户端</a>");
            }
            <%}else{%>
            $("#loginDownload").html("<a id='loginDownload' href=\"activex/oa_client.exe\">下载新版客户端</a>");
            <%}%>
            $("#loginDownload").css('float', 'left');
        }
    }

    function callback() {
    }

    $(function () {
        $('#loginName').focus(function () {
            if ($('#loginName').val() == "请输入您的用户名") {
                $('#loginName').val("");
            }
        });
        $('#loginPwd').focus(function () {
            if ($('#loginPwd').val() == "请输入您的密码") {
                $('#loginPwd').val("");
            }
        });

        //if (isWow64()) {
        //$('#linkDownload').attr("href", "activex/clouddisk_x64.exe");
        //}

        $('.QRcode').hover(function () {
                $('#qrCodeBox').show();
                $('#qrCodeBox').css("left", $('.QRcode').offset().left);
                $('#qrCodeBox').css("top", $('.QRcode').offset().top - $('#qrCodeBox').height() + 3);
            },
            function () {
                $('#qrCodeBox').hide();
            }
        );

        $('#qrCodeBox').hover(function () {
                $('#qrCodeBox').show();
                // $('#qrCodeBox').css("left", $('.QRcode').offset().left);
                // $('#qrCodeBox').css("top", $('.QRcode').offset().top - $('#qrCodeBox').height() + 3);
            },
            function () {
                $('#qrCodeBox').hide();
            }
        );
    });
    
    function showTip(msg) {
        var options = {
            'priority': 'info',
            'message': msg,
            'settings': {
                'toast': {
                    'css': {
                        'background': '#d4eefe',
                        'color': '#008ced',
                        'font-size': '15px',
                        'font-family': '宋体',
                        'filter': 'filter:alpha(opacity=80)',
                        'vertical-align': 'middle',
                        'border': '1px solid #a8deff',
                        '-moz-border-radius': '5px',
                        '-webkit-border-radius': '5px',
                        'border-radius': '5px',
                        'line-height': '20px',
                        'padding': '12px'
                    }
                },
                'toaster': {
                    'css': {
                        'min-width': '200px',
                        'max-width': '220px',
                        'height': '40px',
                        'position': 'fixed',
                        'top': '10px',
                        'left': '40%'
                    }
                }
            }
        };
        $.toaster(options);
    }
    
    $(document).ready(function () {
        <%
        if (License.getInstance().isTrial()) {
        %>
        showDiv($("#popup_box"));
        $('#full_name').focus();
        <%
        }
        
        // 猎豹浏览器在request的agent中含有 LEBROWSER，但是通过js获取的agent中则没有
        String browserType = UserAgentParser.getBrowser(request.getHeader("user-agent"));
        
        if (isBrowserForbid) {
        %>
            var browserValid = "<%=browserValid.toLowerCase()%>";
            var aryValid = browserValid.split(",");
            browserInfo = getBrowserInfo().toLowerCase();
            // 如果是chrome型的，则赋予browserType，以得到真正的浏览器类型
            if (browserInfo == "chrome") {
                browserInfo = "<%=browserType%>";
            }
            isBrowserValid = false;
            for (i in aryValid) {
                if (browserInfo == aryValid[i].toLowerCase()) {
                    isBrowserValid = true;
                }
            }
            if (!isBrowserValid) {
                jAlert("请勿使用" + browserInfo.toUpperCase() + "浏览器，请换用<%=browserValid%>", "提示");
            }
            // consoleLog(navigator.userAgent.toLocaleLowerCase());
        <%
        }
        
        if (isBrowserForbid) {
            String msg = "";
            if (isLte) {
                msg = "建议使用" + browserValid + "浏览器，IE需为9以上版本";
                if (browserType.equals("ie7") || browserType.equals("ie8")) {
        %>
                    jAlert("<%=msg%>", "提示");
        <%
                }
            }
            else {
                msg = "建议使用" + browserValid + "浏览器";
            }
        %>
            if (!isBrowserValid) {
                showTip('<%=msg%>');
            }
        <%
        }
        %>
    });
</script>
<%
    String type = License.getInstance().getType();
    if (false && !type.equals(License.TYPE_OEM)) {
%>
<script src="js/logo_show.js" type="text/javascript"></script>
<%}%>
</html>