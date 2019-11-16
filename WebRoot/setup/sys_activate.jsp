<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>激活系统</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/activebar2.js"></script>

</head>
<body>
<TABLE align="center" class="tabStyle_1 percent60" style="margin-top: 20px">
    <TBODY>
      <TR>
        <TD align="left" class="tabStyle_1_title">激活控件</TD>
      </TR>
      <TR>
        <td align="center">
            <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../activex/cloudym.CAB#version=1,3,0,0" width=450 height=86 align="middle" id="webedit">
                <param name="Encode" value="utf-8">
                <param name="MaxSize" value="<%=Global.MaxSize%>">
                <!--上传字节-->
                <param name="ForeColor" value="(255,255,255)">
                <param name="BgColor" value="(107,154,206)">
                <param name="ForeColorBar" value="(255,255,255)">
                <param name="BgColorBar" value="(0,0,255)">
                <param name="ForeColorBarPre" value="(0,0,0)">
                <param name="BgColorBarPre" value="(200,200,200)">
                <param name="FilePath" value="">
                <param name="Relative" value="2">
                <!--<param name="VirtualPath" value="<%=Global.virtualPath%>">-->

                <param name="Server" value="www.xiaocaicloud.com">
                <param name="Port" value="443">
                <param name="PostScript" value="public/license/onlineActivate.do">

<%--                <param name="Server" value="localhost">
                <param name="Port" value="8899">
                <param name="PostScript" value="oa_ide/public/license/onlineActivate.do">
                --%>

                <param name="InternetFlag" value=""> <!--webedit控件中自动根据Server判断是否为SSL链接-->
                <param name="PostScriptDdxc" value="">
                <param name="SegmentLen" value="204800">
                <param name="BasePath" value="">
                <param name="Organization" value="<%=license.getCompany()%>" />
                <param name="Key" value="<%=license.getKey()%>" />
            </object>
        </TD>
      </TR>
      <TR>
        <TD height="30" colspan="2" align="center">
            <input type="button" class="btn" value="激活" onclick="activate()">
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input type="button" class="btn" value="返回" onclick="window.history.back();">
        </TD>
      </TR>
      <TR>
          <TD height="30" colspan="2" style="line-height: 1.5">
              1、点击激活按钮前建议先清除浏览器缓存<br/>
              2、流程设计器控件和web在线编辑控件如未激活，会显示为试用版，并有弹窗提示
          </TD>
      </TR>
      <TR id="trOffline" style="display:none">
          <TD height="30" colspan="2" align="center">
              <div style="margin-bottom: 10px; line-height: 40px">
              <textarea id="offlineCode" style="width: 450px;height: 120px;"></textarea>
              <br/>
              <%
                  com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                  String cloudUrl = cfg.get("cloudUrl");
              %>
                  请复制验证码，到此处获取激活码：<a target="_blank" href="<%=cloudUrl%>/public/license/sys_activate.jsp"><%=cloudUrl%>/public/license/sys_activate.jsp</a>
              <br/>
              <input id="btnCopy" type="button" class="btn" value="复制验证码"/>
              &nbsp;&nbsp;&nbsp;&nbsp;
              <input id="btnOfflineActivate" type="button" class="btn" value="下一步：离线激活"/>
              </div>
          </TD>
      </TR>
    </TBODY>
</TABLE>
<script>
    function activate() {
        try {
            webedit.Activate();
        }
        catch (e) {
            alert("请安装新版客户端！");
        }
    }
    function ShowMsg(msg) {
        // console.log(msg);
        if (msg=="+") {
            alert("激活成功！");
        }
        else if (msg=="-") {
            alert("激活失败！");
        }
        else if (msg.indexOf("offline:")==0) {
            var offliceActivationCode = msg.substring("offline:".length);
            $('#trOffline').show();
            o('offlineCode').value = offliceActivationCode;
        }
        else if (msg.indexOf("test:")==0) {
            // 在控件中InvokeShowMsg生成test:...，仅用于测试
            var offliceActivationCode = msg.substring("test:".length);
            // $('#trOffline').show();
            o('offlineCode').value = offliceActivationCode;
        }
        else {
            alert(msg);
        }
    }

    function checkWebEditInstalled() {
        if (!isIE()) {
            jAlert("请使用IE", "提示");
            return;
        }
        var bCtlLoaded = false;
        try	{
            if (typeof(webedit.AddField)=="undefined")
                bCtlLoaded = false;
            if (typeof(webedit.AddField)=="unknown") {
                bCtlLoaded = true;
            }
        }
        catch (ex) {
        }
        if (!bCtlLoaded) {
            $('<div></div>').html('您还没有安装客户端控件，请点击确定此处下载安装！').activebar({
                'icon': '../images/alert.gif',
                'highlight': '#FBFBB3',
                'url': '../activex/oa_client.exe',
                'button': '../images/bar_close.gif'
            });
        }
    }

    $(function() {
        $('#btnCopy').click(function() {
            o('offlineCode').select();
            if (document.execCommand('copy')) {
                document.execCommand('copy');
            }
            jAlert("复制成功！", "提示");
        })

        $('#btnOfflineActivate').click(function() {
            window.location.href = 'sys_activate_offline.jsp';
        })

        checkWebEditInstalled();
    })
</script>
</body>
</html>
