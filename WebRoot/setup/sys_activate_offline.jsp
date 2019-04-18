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
<title>离线激活系统</title>
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
        <TD align="left" class="tabStyle_1_title">离线激活</TD>
      </TR>
      <TR>
        <td align="center">
            <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../activex/cloudym.CAB#version=1,2,0,1" width=450 height=86 align="middle" id="webedit">
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
                <!--上传后的文件需放在服务器上的路径-->
                <param name="Server" value="<%=request.getServerName()%>">
                <param name="Port" value="<%=request.getServerPort()%>">
                <param name="VirtualPath" value="<%=Global.virtualPath%>">
                <param name="PostScript" value="<%=Global.virtualPath%>">
                <param name="PostScriptDdxc" value="">
                <param name="SegmentLen" value="204800">
                <param name="BasePath" value="">
                <param name="Organization" value="<%=license.getCompany()%>" />
                <param name="Key" value="<%=license.getKey()%>" />
            </object>
        </TD>
      </TR>
      <TR id="trOffline">
        <TD height="30" colspan="2" align="center">
              <textarea id="offlineCode" style="width: 450px;height: 120px;"></textarea>
              <br/>
              请粘贴激活码
          &nbsp;&nbsp;&nbsp;&nbsp;</TD>
      </TR>
      <TR>
        <TD height="30" colspan="2" align="center">
        <input id="btnBefore" type="button" class="btn" value="上一步" onclick="window.location.href='sys_activate.jsp'"/>
        &nbsp;&nbsp;&nbsp;&nbsp;
        <input id="btnOfflineActivate" type="button" class="btn" value="激活"/>
        </TD>
      </TR>
    </TBODY>
</TABLE>
<script>
    function ShowMsg(msg) {
        if (msg=="+") {
            alert("激活成功！");
        }
        else if (msg=="-") {
            alert("激活失败！");
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
        $('#btnOfflineActivate').click(function() {
            webedit.SetActivationCode($('#offlineCode').val());
        })

        checkWebEditInstalled();
    })
</script>
</body>
</html>
