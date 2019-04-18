<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String dir_code = ParamUtil.get(request, "dir_code");
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
<title>wiki - 配置</title>
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
	if (!confirm("您确定要删除么？"))
		return;
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
if (op.equals("setRecommand")) {
	String recommand = ParamUtil.get(request, "recommand");
	cfg.setProperty("recommand", recommand);
	out.print(StrUtil.Alert_Redirect("操作成功！", "manager.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
	return;	
}
else if (op.equals("setDefaultScore")) {
	double defaultCreateScore = ParamUtil.getDouble(request, "defaultCreateScore", 0.0);
	double defaultEditScore = ParamUtil.getDouble(request, "defaultEditScore", 0.0);
	cfg.setProperty("defaultCreateScore", defaultCreateScore + "");
	cfg.setProperty("defaultEditScore", defaultEditScore + "");
	out.print(StrUtil.Alert_Redirect("操作成功！", "manager.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
	return;	
}
%>
<%@ include file="wiki_inc_menu_top.jsp"%>
<script>
o("menu6").className="current";
</script>
<div class="spacerH"></div>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0'>
  <tr>
    <td valign="top"><br>
      <form id="form2" name="form2" action="manager.jsp?op=setDefaultScore" method="post">
        <table width="73%" align="center" class="tabStyle_1 percent60">
          <thead>
          <tr>
            <td height="22">配置信息</td>
          </tr>
          </thead>
          <tr>
            <td height="22">默认发布文章得分
              <input id="defaultCreateScore" name="defaultCreateScore" value="<%=StrUtil.getNullString(cfg.getProperty("defaultCreateScore"))%>">
			默认编辑文章得分
              <input id="defaultEditScore" name="defaultEditScore" value="<%=StrUtil.getNullString(cfg.getProperty("defaultEditScore"))%>">
			<input name="submit" type="submit" class="btn" value="确 定">
            <input name="dir_code" value="<%=dir_code%>" type="hidden">
            </td>
          </tr>
        </table>
		</form>
        <br>
		<form name="formRcmd" action="manager.jsp?op=setRecommand" method="post">
        <table width="73%" align="center" class="tabStyle_1 percent60">
          <thead>
          <tr>
            <td height="22">推荐文章( 编号之间用，分隔 )</td>
          </tr>
          </thead>
          <tr>
            <td height="22">
			<input value="<%=StrUtil.getNullString(cfg.getProperty("recommand"))%>" name="recommand" size=60>
            <input name="button" type="button" class="btn" onClick="openSelRecommandDocWin()" value="选 择">
            <input type="submit" class="btn" value="确 定">
            <input name="dir_code" value="<%=dir_code%>" type="hidden"></td>
          </tr>
          <tr>
            <td height="22"><%
				DocumentMgr mm = new DocumentMgr();
				Document md = null;
				int[] v = cfg.getRecommandIds();
				int focuslen = v.length;
				if (focuslen==0)
					out.print("无推荐文章！");
				else {
					for (int k=0; k<focuslen; k++) {
						md = mm.getDocument(v[k]);
						if (md!=null && md.isLoaded()) {
							String color = StrUtil.getNullString(md.getColor());
							if (color.equals("")) {%>
              <%=md.getId()%>&nbsp;<a target="_blank" href="../../../../wiki_show.jsp?id=<%=md.getId()%>"><%=md.getTitle()%></a>
              <%}else{%>
              <%=md.getId()%>&nbsp;<a target="_blank" href="../../../../wiki_show.jsp?id=<%=md.getId()%>"><font color="<%=color%>"><%=md.getTitle()%></font></a>
              <%}%>              &nbsp;[<a href="javascript:delRecommand('<%=md.getId()%>')">
              <lt:Label key="op_del"/></a>]
              <%if (k!=0) {%>
              [<a href="javascript:up('<%=md.getId()%>')">
              <lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/></a>]
              <%}%>
              <%if (k!=focuslen-1) {%>
              &nbsp;[<a href="javascript:down('<%=md.getId()%>')">
              <lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/></a>]
              <%}%>
              <br>
              <%}else {%>
              <%=v[k]%>&nbsp;<font color=red>文章不存在</font>&nbsp;[<a href="javascript:delRecommand('<%=v[k]%>')">
              <lt:Label key="op_del"/></a>]<BR>
              <%}
			}
		}%>
            </td>
          </tr>
        </table>
      </form>
      <br>
      <br></td>
  </tr>
</table>
</body>
</html>
