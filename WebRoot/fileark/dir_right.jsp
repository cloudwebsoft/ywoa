<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="org.json.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>目录维护</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function form1_onsubmit() {	
	if (o("name").value=="") {
		jAlert("请填写名称！","提示");
		o("name").focus();
		return false;
	}
	
	form1.type.value = form1.seltype.value;
	// form1.root_code.value = window.parent.dirmainFrame.getRootCode();
}

function selTemplate(id) {
	if (form1.templateId.value!=id) {
		form1.templateId.value = id;
	}
}

function enableSelType() {
	if (confirm("如果该项中已经含有内容，则更改以后会造成问题，您要强制更改吗？")) {
		form1.seltype.disabled = false;
	}
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String code = ParamUtil.get(request, "code");

String action = ParamUtil.get(request, "action");
if (action.equals("AddChild")) {
	boolean re = false;
	try {
		Directory dir = new Directory();
		re = dir.AddChild(request);
		if (!re) {
			out.print(StrUtil.jAlert("添加节点失败，请检查编码是否重复！","提示"));
		}
		else {
			out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "dir_right.jsp?op=modify&code=" + StrUtil.UrlEncode(dir.getCode())));			
			%>
            <script>
			if (typeof(window.parent.leftFileFrame)!="undefined") {
				window.parent.leftFileFrame.location.reload();
			}
			</script>
            <%
		}	
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (action.equals("modify")) {
	boolean re = true;
	try {
		Directory dir = new Directory();
		re = dir.update(request);
		if (re) {
			out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "dir_right.jsp?op=modify&code=" + StrUtil.UrlEncode(code)));
			%>
			<script>
			if (typeof(window.parent.leftFileFrame)!="undefined") {	
				window.parent.leftFileFrame.location.reload();
			}
			</script>
			<%
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (action.equals("move")) {
	JSONObject json = new JSONObject();
	String parent_code = ParamUtil.get(request, "parent_code");
	int position = Integer.parseInt(ParamUtil.get(request, "position"));
	if("root".equals(code)){
		json.put("ret", "0");
		json.put("msg", "根节点不能移动！");
		out.print(json.toString());
		return;
	}
	if("#".equals(parent_code)){
		json.put("ret", "0");
		json.put("msg", "不能与根节点平级！");
		out.print(json.toString());	
		return;
	}
	
	Directory dir = new Directory();
	Leaf moveleaf = dir.getLeaf(code);
	int old_position = moveleaf.getOrders();//得到被移动节点原来的位置
	String old_parent_code = moveleaf.getParentCode();
	Leaf newParentLeaf = dir.getLeaf(parent_code);
	
	moveleaf.setParentCode(parent_code);
	int p = position + 1;
	moveleaf.setOrders(p);
	moveleaf.update();
	json.put("ret", "1");
	json.put("msg", "操作成功！");
	out.print(json.toString());	
	return;	
}

String parent_code = ParamUtil.get(request, "parent_code");
if (parent_code.equals(""))
	parent_code = "root";
Leaf lfParent = new Leaf();
lfParent = lfParent.getLeaf(parent_code);
if (lfParent==null) {
	out.print(cn.js.fan.web.SkinUtil.makeInfo(request, "目录已不存在！"));	
	return;
}

String parent_name = lfParent.getName();

String name = ParamUtil.get(request, "name");
String description = ParamUtil.get(request, "description");
String op = ParamUtil.get(request, "op");
boolean isHome = false;
int type = 0;
if (op.equals(""))
	op = "AddChild";
if (op.equals("AddChild")) {
	LeafPriv lp = new LeafPriv();
	lp.setDirCode(parent_code);
	if (!lp.canUserExamine(privilege.getUser(request))) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

Leaf leaf = null;
if (op.equals("modify")) {
	Directory dir = new Directory();
	leaf = dir.getLeaf(code);
	if (leaf==null) {
		out.print(SkinUtil.makeErrMsg(request, "节点已删除!"));
		return;
	}
	
	LeafPriv lp = new LeafPriv(code);
	if (!lp.canUserModify(privilege.getUser(request))) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	name = leaf.getName();
	description = leaf.getDescription();
	type = leaf.getType();
	isHome = leaf.getIsHome();
}
if ("AddChild".equals(op)) {
%>
<table cellSpacing=0 cellPadding=3 width="100%" align=center>
  <TBODY>
    <TR>
      <TD class="tdStyle_1" noWrap>目录</TD>
    </TR>
  </TBODY>
</table>
<%}else{%>
<%@ include file="dir_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<%}%>
<div class="spacerH"></div>
<form action="dir_right.jsp?action=<%=op%>" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
<table width="100%" class="tabStyle_1 percent80">
    <tr>
      <td align="center" valign="top" class="tabStyle_1_title">
        <%
		  if ("AddChild".equals(op)) {
			  out.print("增加");
		  }
		  else {
			  out.print("修改");
		  }
		  %>
      </td>
    </tr>
	<%if (op.equals("AddChild")) {%>
    <tr>
      <td align="left">
        父节点：<font color="blue"><%=parent_name.equals("")?"根结点":parent_name%></font>
  	  </td>
    </tr>
    <%}%>
    <tr>
      <td width="818" align="left">名称
        <input id="name" name="name" value="<%=name%>" />
        <input id="code" name="code" type="hidden" value="<%=code%>" <%=op.equals("modify")?"readonly":""%> />
&nbsp;
<select id="target" name="target">
  <option value="">默认</option>
  <option value="mainFileFrame">文件柜右侧</option>
  <option value="_parent">父窗口</option>
  <option value="_top">顶层窗口</option>
  <option value="_blank">新窗口</option>
  <option value="_self">本窗口</option>
</select>
<%if (op.equals("modify")) {%>
<script>
form1.target.value = "<%=leaf.getTarget()%>";
</script>
<%}%></td>
    </tr>
    <tr>
      <td align="left">链接
        <input id="description" name="description" value="<%=description%>" />
          <input type="hidden" name="parent_code" value="<%=parent_code%>" />
        (链接型节点的链接地址)&nbsp;
      </td>
    </tr>
    <tr>
      <td align="left"><%
				String disabled = "";
				if (op.equals("modify") && leaf.getType()>=1)
					disabled = "true";
				%>
        类型
          <select id="seltype" name="seltype">
            <option value="0">分类</option>
            <option value="1">文章</option>
            <option value="2" <%=op.equals("AddChild")?"selected":""%>>列表</option>
            <option value="3">链接</option>
        </select>
        启用
        <select id="isShow" name="isShow">
        <option value="1">是</option>
        <option value="0">否</option>
        </select>&nbsp;
        记录操作日志
        <select id="isLog" name="isLog">
        	<option value="1">是</option>
        	<option value="0">否</option>
        	</select>
          <script>
		  <%if (op.equals("modify")) {%>
			 o("seltype").value = "<%=type%>"
			 o("isShow").value = "<%=leaf.isShow()?1:0%>";
			 o("isLog").value = "<%=leaf.isLog()?1:0%>";
		  <%}%>
		  form1.seltype.disabled = "<%=disabled%>"
		  </script>
          <input type="hidden" name="root_code" value="" />
          <input type="hidden" name="type" value="<%=type%>" />
          <input type="hidden" name="templateId" value="<%=op.equals("modify")?""+leaf.getTemplateId():"-1"%>" size="3" />
          <%
		  com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		  if (cfg.getBooleanProperty("fullTextSearchSupported")) {%>
          <input id="isFulltext" name="isFulltext" value="1" type="checkbox" <%=op.equals("modify")?(leaf.isFulltext()?"checked":""):""%> />全文检索
          <%}%>
          </td>
    </tr>
    <tr>
      <td align="left"><span class="unnamed2">
        <%if (op.equals("modify")) {%>
        <script>
		var bcode = "<%=leaf.getCode()%>";
		</script>
        父节点
        <select id="parentCode" name="parentCode" title="蓝色表示'无内容'或'列表'项">
          <%
				Leaf rootlf = leaf.getLeaf("root");
				DirectoryView dv = new DirectoryView(rootlf);
				dv.ShowDirectoryAsOptionsWithCode(out, rootlf, rootlf.getLayer());
%>
        </select>
        <script>
		form1.parentCode.value = "<%=leaf.getParentCode()%>";
		</script>
        <%}%>
      </span>
      <%
	  boolean isExamine = false;
	  if (op.equals("modify")) {
		  isExamine = leaf.isExamine();
	  }
	  %>
      <input id="isExamine" name="isExamine" type="checkbox" <%=isExamine?"checked":""%> value="1" title="发文章的时候如果不需审核，则前台直接能看到" /> 
      文章需审核
      </td>
    </tr>
    <tr>
      <td align="left">
        <span style="display:none">
        <%if (op.equals("modify")) {%>
        <input type="checkbox" name="isHome" value="true" <%=isHome?"checked":""%> />
        <%}else{%>
        <input type="checkbox" name="isHome" value="true" checked="checked" />
        <%}%>
       	前台</span>
       	<%
       	cn.js.fan.module.cms.plugin.wiki.Config wikiCfg = cn.js.fan.module.cms.plugin.wiki.Config.getInstance();
       	boolean isUse = wikiCfg.getBooleanProperty("isUse");
       	if (isUse) {
       	%>
        <span style="display:"> 
                        应用插件
        <select id="pluginCode" name="pluginCode">
          <option value="<%=PluginUnit.DEFAULT%>">默认</option>
          <%			
		PluginMgr pm = new PluginMgr();
		Vector v = pm.getAllPlugin();
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			PluginUnit pu = (PluginUnit)ir.next();
		%>
          <option value="<%=pu.getCode()%>"><%=pu.getName(request)%></option>
          <%}%>
          <%if (op.equals("modify")) {%>
          <script>
				form1.pluginCode.value = "<%=leaf.getPluginCode()%>";
				</script>
          <%}%>
        </select>
        </span>
        <%
        }
			String sysChecked = "";
			String isOfficeNTKOShowChecked = "";
			if (op.equals("modify")) {
				if (leaf.isSystem())
					sysChecked = "checked";
				if (leaf.isOfficeNTKOShow())
					isOfficeNTKOShowChecked = "checked";
			}
			%>
        <input id="isSystem" name="isSystem" value="1" type="checkbox" <%=sysChecked%> title="置为系统目录后，可以防止被删除" />
        系统目录
        <input id="isOfficeNTKOShow" name="isOfficeNTKOShow" value="1" type="checkbox" <%=isOfficeNTKOShowChecked%> />  Word或Excel附件在正文中显示 </td>
    </tr>
    <tr>
      <td align="center" valign="top"><input name="Submit" type="submit" class="btn" value="确定" />
        &nbsp;&nbsp;&nbsp;
        <input type="reset" class="btn" value="重置" />
        <!--
		&nbsp;&nbsp;&nbsp;
        <input name="button" type="button" class="btn" onclick="enableSelType()" value="强制类型修改" />
		-->
        </td>
    </tr>
</table>
</form>
</body>
<script>
function changeLink() {
	o("description").value = "netdisk/netdisk_public_attach_list.jsp?dir_code=" + o("publicNetdiskDir").value;
}
</script>
</html>