<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%
    String redirectUrl = ParamUtil.get(request, "redirectUrl");
%>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=0;"/>
    <title>登录</title>
    <link rel="stylesheet" type="text/css" href="js/layer_mobile/need/layer.css"/>
    <link rel="stylesheet" type="text/css" href="css/style.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/aes.min.js"></script>
    <script src="js/phone.js"></script>
    <script src="js/layer_mobile/layer.js"></script>
    <script src="js/login.js"></script>
</head>
<body>
<div class="whole">
    <div class="title"><%=Global.AppName%>
    </div>
    <div class="coordinates-icon">
        <div class="coordinates topAct">
            <img src="images/logo.png"/>
        </div>
    </div>
    <div class="login-form">
        <div class="user-name common-div">
            	<span class="eamil-icon common-icon">
                	<img src="images/user.png"/>
                </span>
            <input type="text" id="name" name="name" value="" placeholder="用户名"/>
        </div>
        <div class="user-pasw common-div">
            	<span class="pasw-icon common-icon">
                	<img src="images/password.png"/>
                </span>
            <input type="password" id="pwd" name="data" value="" placeholder=""/>
        </div>
        <div class="forgets">
            <img id="imgChk" class="img-autologin" src="images/checkbox_checked.png"/>
            <a href="javascript:;">自动登录</a>
        </div>
        <div class="login-btn common-div">登 录</div>
    </div>
</div>
</body>
<%
    com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
    String pwdName = scfg.getProperty("pwdName");
    String pwdAesKey = scfg.getProperty("pwdAesKey");
    String pwdAesIV = scfg.getProperty("pwdAesIV");
%>
<script>
    var isMyLoginAuto = false;

    $(function () {
        <%
        boolean isLoginAuto = scfg.getBooleanProperty("isLoginAuto");
        if (isLoginAuto || scfg.isRememberUserName()) {
        %>
        o("name").value = unescape(get_cookie("name"));
        if (o("name").value == "")
            o("name").focus();
        else
            o("pwd").focus();
        <%}
        if (isLoginAuto) {
        %>
        isMyLoginAuto = unescape(get_cookie("isMyLoginAuto"));
        if (isMyLoginAuto) {
            o("pwd").value = aesDecrypt(get_cookie("pwd"));
            $('#imgChk').attr("src", "images/checkbox_checked.png");
            // 自动登录
            login();
        } else {
            $('#imgChk').attr("src", "images/checkbox.png");
        }
        <%}%>

        $("#imgChk").on("tap click", function () {
            var src = $(this).attr("src");
            if (src == "images/checkbox.png") {
                $(this).attr("src", "images/checkbox_checked.png");
            } else {
                $(this).attr("src", "images/checkbox.png");
            }
        });
    });

    // AES密钥 (需要前端和后端保持一致，十六位)
    var KEY = "<%=pwdAesKey%>";
    // AES密钥偏移量 (需要前端和后端保持一致，十六位)
    var IV = "<%=pwdAesIV%>";

    /**
     * 加密（需要先加载aes.min.js文件）
     * @param word
     * @returns {*}
     */
    function aesMinEncrypt(word) {
        var _word = CryptoJS.enc.Utf8.parse(word),
            _key = CryptoJS.enc.Utf8.parse(KEY),
            _iv = CryptoJS.enc.Utf8.parse(IV);
        var encrypted = CryptoJS.AES.encrypt(_word, _key, {
            iv: _iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        return encrypted.toString();
    }

    /**
     * 解密
     * @param word
     * @returns {*}
     */
    function aesDecrypt(word) {
        var _key = CryptoJS.enc.Utf8.parse(KEY);
        var _iv = CryptoJS.enc.Utf8.parse(IV);
        var decrypted = CryptoJS.AES.decrypt(word, _key, {
            iv: _iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        return decrypted.toString(CryptoJS.enc.Utf8);
    }

    function login() {
        if ($('#name').val() == "" || $('#pwd').val() == "") {
            return;
        }
        $.ajax({
            type: "post",
            url: "../public/user/login.do",
            data: {
                "name": $('#name').val(),
                "<%=pwdName%>": aesMinEncrypt($('#pwd').val())
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest) {
            },
            success: function(data, status) {
                var data = $.parseJSON(data);
                if (data.ret == "1") {
                    saveCookie();

                    <%if (!"".equals(redirectUrl)) {%>
                    window.location.href = "<%=redirectUrl%>";
                    <%}else{%>
                    window.location.href = "../weixin/message/msg_new_list.jsp";
                    <%}%>
                } else {
                    alert(data.msg);
                }
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function () {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function saveCookie() {
        var expdate = new Date();
        var expday = 60;
        expdate.setTime(expdate.getTime() + (24 * 60 * 60 * 1000 * expday));

        document.cookie = "name=" + escape(o("name").value) + ";expires=" + expdate.toGMTString() + ";path=/";
        if (<%=isLoginAuto%>) {
            var src = $('#imgChk').attr("src");
            if (src == "images/checkbox_checked.png") {
                isMyLoginAuto = true;
            }
            if (isMyLoginAuto) {
                document.cookie = "isMyLoginAuto=" + escape(isMyLoginAuto) + ";expires=" + expdate.toGMTString() + ";path=/";
                document.cookie = "pwd=" + aesMinEncrypt(o("pwd").value) + ";expires=" + expdate.toGMTString() + ";path=/";
            }
        }
    }
</script>
</html>
