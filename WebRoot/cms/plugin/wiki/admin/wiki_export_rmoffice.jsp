<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String dir_code = ParamUtil.get(request, "dirCode");
if (dir_code.equals(""))
	dir_code = Leaf.CODE_WIKI;
Directory dir = new Directory();
Leaf leaf = dir.getLeaf(dir_code);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>wiki - 导出</title>
<script src="../../../../inc/common.js"></script>
<script>
function openWin(url,width,height){
	var newwin = window.open(url,"_blank","scrollbars=yes,resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}

var curObj;

function openSelRecommandDocWin() {
	curObj = formRcmd.recommand;
	openWin("wiki_list.jsp?action=sel", 800, 600);	
}

function selDoc(ids) {
	// 检查在notices中是否已包含了ids中的id，避免重复加入
	var ary = ids.split(",");
	var ntc = curObj.value;
	var ary2 = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		var founded = false;
		for (var j=0; j<ary2.length; j++) {
			if (ary[i]==ary2[j]) {
				founded = true;
				break;
			}
		}
		if (!founded) {
			if (ntc=="")
				ntc += ary[i];
			else
				ntc += "," + ary[i];
		}
	}
	curObj.value = ntc;
}

function delRecommand(id) {
	var ntc = formRcmd.recommand.value;
	var ary = ntc.split(",");
	var ary2 = new Array();
	var k = 0;
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			continue;
		}
		else {
			ary2[k] = ary[i];
			k++;
		}
	}
	ntc = "";
	for (i=0; i<ary2.length; i++) {
		if (ntc=="")
			ntc += ary2[i];
		else
			ntc += "," + ary2[i];
	}
	formRcmd.recommand.value = ntc;
	formRcmd.submit();
}

function up(id) {
	var ntc = formRcmd.recommand.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=0) {
				var tmp = ary[i-1];
				ary[i-1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	formRcmd.recommand.value = ntc;
	formRcmd.submit();
}

function down(id) {
	var ntc = formRcmd.recommand.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=ary.length-1) {
				var tmp = ary[i+1];
				ary[i+1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	formRcmd.recommand.value = ntc;
	formRcmd.submit();
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
LeafPriv lp = new LeafPriv(Leaf.CODE_WIKI);
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

cn.js.fan.module.cms.plugin.wiki.Config cfg = cn.js.fan.module.cms.plugin.wiki.Config.getInstance();

String op = ParamUtil.get(request, "op");
%>
<%@ include file="wiki_inc_menu_top.jsp"%>
<script>
o("menu7").className="current";
</script>
<div class="spacerH"></div>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="tabStyle_1 percent60">
  <thead>
  <tr>
    <td height=20 align="left">导出至Word</td>
  </tr>
  </thead>
  <tr>
    <td align="center" valign="top"><br>
<object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="../../../../activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                <param name="Encode" value="utf-8" />
                <param name="BackColor" value="0000ff00" />
                <param name="Server" value="<%=request.getServerName()%>" />
                <param name="Port" value="<%=request.getServerPort()%>" />
                <!--设置是否自动上传-->
                <param name="isAutoUpload" value="1" />
                <!--设置文件大小不超过1M-->
                <param name="MaxSize" value="<%=Global.FileSize%>" />
                <!--设置自动上传前出现提示对话框-->
                <param name="isConfirmUpload" value="1" />
                <!--设置IE状态栏是否显示信息-->
                <param name="isShowStatus" value="0" />
                <param name="PostScript" value="<%=Global.virtualPath%>/some.jsp" />
				<%
                com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();	  
                %>
                <param name="Organization" value="<%=license.getCompany()%>" />
                <param name="Key" value="<%=license.getKey()%>" />                
      </object>

<br/>
目录：
<span id="dirName"><%=leaf.getName()%></span>
<input id="dirCode" name="dirCode" type="hidden" value="<%=dir_code%>" />
&nbsp;&nbsp;
<a href="javascript:;" onclick="openWin('../../../../fileark/dir_sel.jsp?dirCode=' + o('dirCode').value, 640, 480);">选择目录</a>
&nbsp;&nbsp;&nbsp;&nbsp;
本机文件名：
<input id="fileName" name="fileName" value="d:\<%=leaf.getName()%>.doc" />&nbsp;(注意因权限问题，不能存于C盘)
<br/><br/>
<input value="确定" onclick="exportDoc()" type=button class="btn" />
<br>
<br></td>
  </tr>
</table>
</body>
<script>
function exportDoc() {
	if (document.getElementById('fileName').value=="") {
		alert("文件名不能为空！！");
		return;
	}
	redmoonoffice.Visible=0;
	redmoonoffice.OpenWordDocNotVisable("<%=Global.getFullRootPath(request)%>/cms/plugin/wiki/admin/wiki_export_doc.jsp?dirCode=" + o("dirCode").value);
	redmoonoffice.SaveAs(document.getElementById('fileName').value);
	redmoonoffice.isAutoUpload=0;
	redmoonoffice.close();
}

function selectNode(code, name) {
	o("dirCode").value = code;
	o("dirName").innerHTML = name;
	o("fileName").value = "d:\\" + name + ".doc";
}
</script>
</html>
