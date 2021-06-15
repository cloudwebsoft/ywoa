<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.*"%>
<%@ page import="java.text.*"%>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="org.apache.http.client.utils.URIBuilder" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>解析获取表单域</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script src="../inc/common.js"></script>
	<script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
	<script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
	<script src="../js/json2.js"></script>
	<script type="text/javascript" src="../js/formpost.js"></script>
</head>
<body>
<%
	// 创建此文件的原因是，webedit上传页面所处的位置必须在第二级目录下
	License license = License.getInstance();
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	String url = cfg.get("cloudUrl");
	URIBuilder uriBuilder = new URIBuilder(url);
	String host = uriBuilder.getHost();
	int port = uriBuilder.getPort();
	if (port==-1) {
		port = 80;
	}
	String path = uriBuilder.getPath();
	if (path.startsWith("/")) {
		path = path.substring(1);
	}

	boolean isServerConnectWithCloud = cfg.getBooleanProperty("isServerConnectWithCloud");
	if (!isServerConnectWithCloud) {
%>
<TABLE align="center" class="tabStyle_1 percent60" style="margin-top: 20px; width:450px">
	<TR>
		<TD align="left" class="tabStyle_1_title">上传助手</TD>
	</TR>
	<TR>
		<td align="center">
			<object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../../../../activex/cloudym.CAB#version=1,3,0,0" width=450 height=86 align="middle" id="webedit">
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
				<param name="Server" value="<%=host%>">
				<param name="Port" value="<%=port%>">
				<param name="VirtualPath" value="<%=Global.virtualPath%>">
				<param name="PostScript" value="">
				<param name="PostScriptDdxc" value="">
				<param name="SegmentLen" value="204800">
				<param name="BasePath" value="">
				<param name="InternetFlag" value="">
				<param name="Organization" value="<%=license.getCompany()%>" />
				<param name="Key" value="<%=license.getKey()%>" />
			</object>
		</TD>
	</TR>
</table>
<%
	}
%>
</body>
<script>
	<%
        if (!isServerConnectWithCloud) {
    %>
	$(function () {
		checkWebEditInstalled();
	})
	<%
        }
    %>

	function checkWebEditInstalled() {
		var bCtlLoaded = false;
		try	{
			if (typeof(o("webedit").AddField)=="undefined")
				bCtlLoaded = false;
			if (typeof(o("webedit").AddField)=="unknown") {
				bCtlLoaded = true;
			}
		}
		catch (ex) {
		}
		if (!bCtlLoaded) {
			alert('您还没有安装客户端控件，请点击确定下载安装！');
			window.open('../activex/oa_client.exe');
		}
	}

	function parseFields(content) {
		var we = o("webedit");
		we.PostScript = "<%=path%>/public/module/parseForm.do";
		// loadDataToWebeditCtrl(o("myFormEdit"), o("webedit"));
		we.AddField("content", content);
		we.AddField("cwsVersion", "<%=cfg.get("version")%>");
		we.UploadToCloud();

		var data = $.parseJSON(we.ReturnMessage);
		if (data.ret=="1") {
			// $('#fieldsAry').val(JSON.stringify(data.fields));
			return data.fields;
		}
		else {
			return "";
		}
	}

/*	$(function() {
		parseFields();
	})*/

</script>
</html>