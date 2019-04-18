<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="org.json.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.util.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="org.json.JSONArray"%>
<%
String op = ParamUtil.get(request, "op");
String code = ParamUtil.get(request, "code"); // 模块编码
String formCode = ParamUtil.get(request, "formCode");

if (op.equals("setMsgProp")) {
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	String msgProp = request.getParameter("msgProp");
	msd.set("msg_prop", msgProp);
	boolean re = msd.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", 1);
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", 0);
		json.put("msg", "操作失败！");
	}
	// System.out.println(getClass() + " json=" + json);
	out.print(json);
	return;
}
else if ("setValidate".equals(op)) {
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	String validateProp = request.getParameter("validateProp");
	msd.set("validate_prop", validateProp);
	boolean re = msd.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", 1);
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", 0);
		json.put("msg", "操作失败！");
	}
	// System.out.println(getClass() + " json=" + json);
	out.print(json);
	return;
}
else if (op.equals("setCols")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.setCols(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}
	out.print(json);
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>模块设置</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>

	<script src="../inc/common.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
	<script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
	<script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../inc/map.js"></script>

	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>

	<script src="../js/select2/select2.js"></script>
	<link href="../js/select2/select2.css" rel="stylesheet"/>

	<script src="../js/jquery.toaster.js"></script>

	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
	<script type="text/javascript" src="../js/jquery.flexbox.js"></script>

	<script>
		function window_onload() {
			getFieldOfForm($('#otherFormCode').val());
		}

		var errFunc = function (response) {
			window.status = response.responseText;
		}

		function doGetField(response) {
			var rsp = response.responseText.trim();
			$('#spanField').html(rsp);
			$('#otherField').append("<option value='cws_id'>cws_id</option>");
		}

		function getFieldOfForm(formCode) {
			var str = "formCode=" + formCode;
			var myAjax = new cwAjax.Request(
					"module_field_ajax.jsp",
					{
						method: "post",
						parameters: str,
						onComplete: doGetField,
						onError: errFunc
					}
			);
		}
	</script>
</head>
<body onload="window_onload()">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

if (op.equals("add")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.add(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}		
	return;
}
else if (op.equals("del")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.del(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}	
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("modify")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.modify(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}	

	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("addTag")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.addTag(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}	

	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("delTag")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.delTag(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}	
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("modifyTag")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.modifyTag(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("addBtn")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.addBtn(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("addBtnBatch")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.addBtnBatch(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;	
}
else if (op.equals("addCond")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.addCond(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}	
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
} else if (op.equals("delBtn")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.delBtn(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("modifyBtn")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.modifyBtn(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}

	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("setUse")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.setUse(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("setFilter")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.setFilter(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("addLink")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.addLink(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("delLink")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.delLink(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}	
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("modifyLink")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.modifyLink(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("setPromptIcon")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.setPromptIcon(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_list.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}

ModuleSetupDb vsd = new ModuleSetupDb();
vsd = vsd.getModuleSetupDb(code);
int work_log = vsd.getInt("is_workLog");
%>
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<div class="spacerH"></div>
<form method="post" action="module_field_list.jsp?op=setUse">
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
    <tr>
      <td colspan="6" align="center" class="tabStyle_1_title">模块信息</td>
    </tr>
    <tr>
      <td width="11%" align="center">
	  模块名称
	  <input name="code" value="<%=code%>" type="hidden" />
	  <input name="formCode" value="<%=formCode%>" type="hidden" />
      </td>
      <td width="22%" align="left"><input name="name" value="<%=StrUtil.getNullStr(vsd.getString("name"))%>" /></td>
      <%
      License license = License.getInstance();
      if (license.isPlatformSrc()) {
      %>
      <td width="9%" align="center" >模块状态</td>
      <td width="14%" align="left"><select name="isUse">
        <option value="1" <%=vsd.getInt("is_use")==1?"selected":""%>>启用</option>
        <option value="0" <%=vsd.getInt("is_use")==0?"selected":""%>>停用</option>
      </select></td>
      <%} %>
      <td width="11%" align="center">模块编码</td>
      <td width="33%" align="left" <%=license.isPlatformSrc() ? "" : "colspan=3"%>><%=vsd.getString("code")%></td>
	</tr>
    <tr>
      <td align="center" >模块描述</td>
      <td align="left" >
      <input id="description" name="description" value="<%=StrUtil.getNullStr(vsd.getString("description"))%>" />
      </td>
      <td align="center" >在位编辑</td>
      <td align="left" >
	  <select name="is_edit_inplace" title="在位编辑不支持相关事件脚本，且从模块暂不支持在位编辑">
        <option value="1" <%=vsd.getInt("is_edit_inplace")==1?"selected":""%>>是</option>
        <option value="0" <%=vsd.getInt("is_edit_inplace")==0?"selected":""%>>否</option>
      </select>      
      </td>
      <td align="center" >&nbsp;</td>
      <td align="left" >&nbsp;</td>
    </tr>
    <tr>
      <td align="center" >事件提醒</td>
      <td align="left" >
	  <img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>
      <a href="javascript:;" onclick="openMsgPropDlg()">配置</a>
      <span style="margin:10px"><img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:<%=StrUtil.getNullStr(vsd.getString("msg_prop")).equals("")?"none":"" %>"/></span>
	  <textarea id="msgProp" style="display:none"><%=StrUtil.getNullStr(vsd.getString("msg_prop"))%></textarea>      
      </td>
      <td align="center" >验证提醒</td>
      <td align="left" >
	  <img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>
      <a href="javascript:;" onclick="openCondition(o('validatePropHidden'), o('imgValidate'))">配置条件</a>
      <span style="margin:10px">
      <img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:<%=StrUtil.getNullStr(vsd.getString("validate_prop")).equals("")?"none":"" %>" id="imgValidate"/>
      </span>
      <textarea id="validatePropHidden" name="validatePropHidden" style="display:none"><%=StrUtil.getNullStr(vsd.getString("validate_prop"))%></textarea>      
      </td>
      <td align="center" >提醒信息</td>
      <td align="left" >
		<input id="validate_msg" name="validate_msg" title="验证提示" style="width:200px" value="<%=StrUtil.HtmlEncode(vsd.getString("validate_msg"))%>"  />      
      </td>
    </tr>
    <tr>
      <td align="center" >查看按钮</td>
      <td align="left" >
    <select id="btn_display_show" name="btn_display_show">
        <option value="1">显示</option>
        <option value="0">隐藏</option>
      </select>
      <script>
	  o("btn_display_show").value = "<%=vsd.getInt("btn_display_show")%>";
	  </script>     
      </td>
      <td align="center" >添加按钮</td>
      <td align="left" >    
     <select id="btn_add_show" name="btn_add_show">
      <option value="1">显示</option>
      <option value="0">隐藏</option>
      </select>
      <script>
	  o("btn_add_show").value = "<%=vsd.getInt("btn_add_show")%>";
	  </script>     
      </td>
      <td align="center" >编辑按钮</td>
      <td align="left" >
     <select id="btn_edit_show" name="btn_edit_show">
      <option value="1">显示</option>
      <option value="0">隐藏</option>
      </select>
      <script>
	  o("btn_edit_show").value = "<%=vsd.getInt("btn_edit_show")%>";
	  </script>              
      </td>
    </tr>
    <tr>
      <td align="center" >流程按钮</td>
      <td align="left" ><select id="btn_flow_show" name="btn_flow_show">
        <option value="1">显示</option>
        <option value="0">隐藏</option>
      </select>
      <script>
	  o("btn_flow_show").value = "<%=vsd.getInt("btn_flow_show")%>";
	  </script>       
      </td>
      <td align="center" >日志按钮</td>
      <td align="left" ><select id="btn_log_show" name="btn_log_show">
        <option value="1">显示</option>
        <option value="0">隐藏</option>
      </select>
      <script>
	  o("btn_log_show").value = "<%=vsd.getInt("btn_log_show")%>";
	  </script>      
      </td>
      <td align="center" >删除按钮</td>
      <td align="left" ><select id="btn_del_show" name="btn_del_show">
        <option value="1">显示</option>
        <option value="0">隐藏</option>
      </select>
      <script>
	  o("btn_del_show").value = "<%=vsd.getInt("btn_del_show")%>";
	  </script>      
      </td>
    </tr>
    <tr>
      <td align="center" >显示视图</td>
      <td align="left" >
      <select id="view_show" name="view_show" onchange="onChangeViewShow(this)">
      <option value="<%=ModuleSetupDb.VIEW_DEFAULT%>">默认</option>
      <%
	  FormViewDb fvd = new FormViewDb();
	  Iterator irv = fvd.getViews(formCode).iterator();
	  while (irv.hasNext()) {
	  	fvd = (FormViewDb)irv.next();
		%>
		<option value="<%=fvd.getInt("id")%>"><%=fvd.getString("name")%></option>
		<%
	  }
	  %>
      <option value="<%=ModuleSetupDb.VIEW_SHOW_TREE%>">树形视图</option>      
      <option value="<%=ModuleSetupDb.VIEW_SHOW_CUSTOM%>">自定义</option>      
      </select>
      <script>
	  o("view_show").value = "<%=vsd.getInt("view_show")%>";
	  </script>
      &nbsp;
	  <input type="checkbox" id="btn_edit_display" name="btn_edit_display" value="1" <%=vsd.getInt("btn_edit_display")==1?"checked":""%> title="显示视图中编辑按钮是否显示" />
      &nbsp;编辑按钮
      &nbsp;&nbsp;
      <input type="checkbox" id="btn_print_display" name="btn_print_display" value="1" <%=vsd.getInt("btn_print_display")==1?"checked":""%> title="显示视图中打印按钮是否显示" />
      &nbsp;打印按钮      
      </td>
      <td colspan="2" align="left" >
        <span id="urlModuleShow" style="display:none">
        &nbsp;&nbsp;显示页地址 <input title="如果页面是定制的，请输入显示页地址" name="url_show" value="<%=StrUtil.getNullStr(vsd.getString("url_show"))%>" />
        </span>
        <span id="fieldTreeModuleShow" style="display:none">
        &nbsp;&nbsp;树形字段 
        <select title="树形视图时，左侧树形结构对应的基础数据宏控件字段" id="field_tree_show" name="field_tree_show">
        <option value="">请选择</option>
        <%
        SelectMgr sm = new SelectMgr();        
	    MacroCtlMgr mm = new MacroCtlMgr();
        Iterator ir = fd.getFields().iterator();
        while (ir.hasNext()) {
        	FormField ff = (FormField)ir.next();
            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
        	if (mu!=null && mu.getCode().equals("macro_flow_select")) {
		        String valRaw = ff.getDefaultValueRaw();
		        SelectDb sd = sm.getSelect(valRaw);
		        if (sd.getType() == SelectDb.TYPE_TREE) {
		        	%>
		        	<option value="<%=ff.getName() %>"><%=ff.getTitle() %></option>
		        	<%
		        }      		
        	}
        }
        %>
        </select>
	    <script>
		o("field_tree_show").value = "<%=StrUtil.getNullStr(vsd.getString("field_tree_show"))%>";
		</script>        
        </span>        
      </td>
      <td colspan="2" align="left" >
      </td>
    </tr>
    <tr>
      <td align="center" >编辑视图</td>
      <td align="left" >
      <select id="view_edit" name="view_edit" onchange="onChangeViewEdit(this)">
      <option value="<%=ModuleSetupDb.VIEW_DEFAULT%>">默认</option>
      <option value="<%=ModuleSetupDb.VIEW_EDIT_CUSTOM%>">自定义</option>      
      <%
	  irv = fvd.getViews(formCode).iterator();
	  while (irv.hasNext()) {
	  	fvd = (FormViewDb)irv.next();
		%>
		<option value="<%=fvd.getInt("id")%>"><%=fvd.getString("name")%></option>
		<%
	  }
	  %>
      </select>
      <script>
	  o("view_edit").value = "<%=vsd.getInt("view_edit")%>";
	  </script>     
      </td>
      <td colspan="2" align="left" >
        <span id="urlModuleEdit" style="display:none">
        &nbsp;&nbsp;编辑页地址 <input title="如果页面是定制的，请输入列表页地址" name="url_edit" value="<%=StrUtil.getNullStr(vsd.getString("url_edit"))%>" />
        </span>
      </td>
      <td colspan="2" align="left" >&nbsp;</td>
    </tr>
    <tr>
      <td align="center" >列表视图</td>
      <td align="left" >
      <select id="view_list" name="view_list" onchange="onChangeViewList(this)">
      <option value="<%=ModuleSetupDb.VIEW_DEFAULT%>">默认</option>
      <option value="<%=ModuleSetupDb.VIEW_LIST_GANTT%>">任务看板</option>
      <option value="<%=ModuleSetupDb.VIEW_LIST_GANTT_LIST%>">任务看板/列表</option>
      <option value="<%=ModuleSetupDb.VIEW_LIST_TREE%>">树形</option>
      <option value="<%=ModuleSetupDb.VIEW_LIST_CUSTOM%>">自定义</option>
      </select>
      <script>	  
	  function onChangeViewList() {
		var val = $('#view_list').val();
	  	if (val=='<%=ModuleSetupDb.VIEW_LIST_CUSTOM%>') {
			$('#urlListRow').show();
			// $('#urlModuleEdit').hide();
			$('#fieldDateRow').hide();	
			$('#fieldRow').hide();
			$('#scaleRow').hide();	
			$('#fieldTreeList').hide();				
		}
		else if (val=='<%=ModuleSetupDb.VIEW_LIST_GANTT%>' || val=='<%=ModuleSetupDb.VIEW_LIST_GANTT_LIST%>') {
			$('#fieldDateRow').show();
			$('#fieldRow').show();
			$('#scaleRow').show();
			$('#urlListRow').hide();
			$('#fieldTreeList').hide();							
			// $('#urlModuleEdit').hide();					
		}
		else if (val=='<%=ModuleSetupDb.VIEW_LIST_TREE%>') {
			$('#fieldDateRow').hide();
			$('#fieldRow').hide();
			$('#scaleRow').hide();			
			$('#urlListRow').hide();
			$('#fieldTreeList').show();			
		}
		else {
			$('#fieldDateRow').hide();
			$('#fieldRow').hide();
			$('#scaleRow').hide();			
			$('#urlListRow').hide();
			$('#fieldTreeList').hide();							
			// $('#urlModuleEdit').hide();					
		}
	  }
	  
	  function onChangeViewEdit() {
		var val = $('#view_edit').val();
	  	if (val=='<%=ModuleSetupDb.VIEW_EDIT_CUSTOM%>') {
			$('#urlModuleEdit').show();
		}
		else {
			$('#urlModuleEdit').hide();			
		}
	  }
	  
	  function onChangeViewShow() {
		var val = $('#view_show').val();
	  	if (val=='<%=ModuleSetupDb.VIEW_SHOW_CUSTOM%>') {
			$('#urlModuleShow').show();
			$('#fieldTreeModuleShow').hide();	
		}
		else if (val=='<%=ModuleSetupDb.VIEW_SHOW_TREE%>') {
			$('#fieldTreeModuleShow').show();
			$('#urlModuleShow').hide();			
		}
		else {
			$('#urlModuleShow').hide();	
			$('#fieldTreeModuleShow').hide();					
		}
	  }
	  
	  $(function() {
		  o("view_list").value = "<%=vsd.getInt("view_list")%>";
		  o("view_edit").value = "<%=vsd.getInt("view_edit")%>";
		  onChangeViewList();
		  onChangeViewEdit();
		  onChangeViewShow();
	  });
	  </script>             
      </td>
      <td colspan="4" align="left" >
      <span id="urlListRow" style="display:none">
      &nbsp;&nbsp;列表页地址
      <input title="如果页面是定制的，请输入列表页地址" name="url_list" value="<%=StrUtil.getNullStr(vsd.getString("url_list"))%>" />
      </span>
      <span id="fieldTreeList" style="display:none">
        &nbsp;&nbsp;树形字段
        <select title="树形视图时，左侧树形结构对应的基础数据宏控件字段" id="field_tree_list" name="field_tree_list">
		</select>      
		<script>
		$(function() {
			$('#field_tree_list').html($('#field_tree_show').html());
			$('#field_tree_list').val("<%=StrUtil.getNullStr(vsd.getString("field_tree_list"))%>");
		});
		</script>
      </span>
      </td>
    </tr>
    <tr id="fieldDateRow">
      <td align="center" >日期字段</td>
      <td colspan="5" align="left" >
      <%
	  String opts = "", optsDate = "";
	  ir = fd.getFields().iterator();
	  while (ir.hasNext()) {
	  	FormField ff = (FormField)ir.next();
		if (ff.getFieldType()==FormField.FIELD_TYPE_DATE || ff.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
			optsDate += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
		}
		opts += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
	  }
	  %>
      开始日期：
      <select id="field_begin_date" name="field_begin_date">
      <option value="">无</option>
      <%=optsDate%>
      </select>
      结束日期：
      <select id="field_end_date" name="field_end_date">
      <option value="">无</option>
      <%=optsDate%>
      </select>     
      </td>
    </tr>
    
    <tr id="fieldRow">
      <td align="center" >显示字段</td>
      <td colspan="5" align="left" >
      名称：
      <select id="field_name" name="field_name">
      <option value="">无</option>
      <%=opts%>
      </select>       
      描述：
      <select id="field_desc" name="field_desc">
      <option value="">无</option>
      <%=opts%>
      </select>       
      标签：
      <select id="field_label" name="field_label">
      <option value="">无</option>
      <%=opts%>
      </select>
      </td>
    </tr>
    <tr id="scaleRow">
      <td align="center" >显示比例</td>
      <td colspan="5" align="left" >
      默认：
      <select id="scale_default" name="scale_default">
      <option value="">无</option>      
      <option value="hours">小时</option>
      <option value="days">天</option>
      <option value="weeks" selected>周</option>
      <option value="months">月</option>
      </select>
      最小：
      <select id="scale_min" name="scale_min">
      <option value="hours" selected>小时</option>
      <option value="days">天</option>
      <option value="weeks">周</option>
      <option value="months">月</option>
      </select>
      最大：
      <select id="scale_max" name="scale_max">
      <option value="hours">小时</option>
      <option value="days">天</option>
      <option value="weeks">周</option>
      <option value="months" selected>月</option>
      </select>
      <script>
      $('#field_begin_date').val("<%=StrUtil.getNullStr(vsd.getString("field_begin_date"))%>");
      $('#field_end_date').val("<%=StrUtil.getNullStr(vsd.getString("field_end_date"))%>");
      $('#field_name').val("<%=StrUtil.getNullStr(vsd.getString("field_name"))%>");
      $('#field_desc').val("<%=StrUtil.getNullStr(vsd.getString("field_desc"))%>");
      $('#field_label').val("<%=StrUtil.getNullStr(vsd.getString("field_label"))%>");
      <%
	  String scaleDefault = StrUtil.getNullStr(vsd.getString("scale_default"));
	  String scaleMin = StrUtil.getNullStr(vsd.getString("scale_min"));
	  String scaleMax = StrUtil.getNullStr(vsd.getString("scale_max"));
	  if ("".equals(scaleDefault)) {
	  	scaleDefault = "weeks";
	  }
	  if ("".equals(scaleMin)) {
	  	scaleMin = "hours";
	  }
	  if ("".equals(scaleMax)) {
	  	scaleMax = "months";
	  }
	  %>	  
      $('#scale_default').val("<%=scaleDefault%>");	  
      $('#scale_min').val("<%=scaleMin%>");	  
      $('#scale_max').val("<%=scaleMax%>");	  
      </script>      
      </td>
    </tr>
    <%if(code.equals("prj") || code.equals("prj_task") || code.equals("mobile_prj") || code.equals("mobile_prj_task") || code.equals("mobile_prj_task_for_prj") || code.equals("mobile_prj_task_created")){ %>
	    <tr>
	    	<td align="center" >关联汇报</td>
	    	<td colspan="5" align="left" >
	    		<input type="checkbox" id="is_workLog"  onclick="changeWorkLog()"/> 
	    		<input type="hidden" class="is_workLog" id="is_workLog_val" name="is_workLog_val" value="0" />
	    	</td>
	    </tr>
    <%} %>
    <tr>
      <td colspan="6" align="center" ><input class="btn btn-default" type="submit" value="确定" />
        &nbsp;&nbsp;&nbsp;&nbsp;
  <%
  String moduleUrlList = StrUtil.getNullStr(vsd.getString("url_list"));
  if (moduleUrlList.equals("")) {
  	moduleUrlList = request.getContextPath() + "/visual/module_list.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
  }
  else {
  	moduleUrlList = request.getContextPath() + "/" + moduleUrlList;
  }
  if (license.isPlatformSrc() && vsd.getInt("is_use") == 1) {
  %>
  <input class="btn btn-default" type="button" value="进入模块" onclick="addTab('<%=StrUtil.getNullStr(vsd.getString("name"))%>', '<%=moduleUrlList%>');" /></td>
    <%} %>
    </tr>
</table>
</form>
<%
String listField = StrUtil.getNullStr(vsd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(vsd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(vsd.getString("list_field_order"));
String[] fieldOrder = StrUtil.split(listFieldOrder, ",");
String listFieldLink = StrUtil.getNullStr(vsd.getString("list_field_link"));
String[] fieldsLink = StrUtil.split(listFieldLink, ",");

int len = 0;
if (fields!=null)
	len = fields.length;
int i;
%>
<table cellSpacing="0" class="tabStyle_1 percent98" cellPadding="3" width="95%" align="center">
<tr>
<td>
<jsp:include page="module_field_inc_preview.jsp">
<jsp:param name="code" value="<%=code%>" />
<jsp:param name="formCode" value="<%=formCode%>" />
<jsp:param name="from" value="module_field_list" />
</jsp:include> 
</td>
</tr>
</table>

<table cellSpacing="0" class="tabStyle_1 percent98" cellPadding="3" width="95%" align="center">
    <tr>
      <td class="tabStyle_1_title"  width="5%">序号</td>
      <td class="tabStyle_1_title"  width="15%">列表中显示的字段</td>
      <td class="tabStyle_1_title"  width="16%">描述</td>
      <td class="tabStyle_1_title"  width="8%">顺序号</td>
      <td class="tabStyle_1_title"  width="8%">宽度</td>
      <td class="tabStyle_1_title"  width="29%">链接</td>
      <td width="17%"  class="tabStyle_1_title">操作</td>
    </tr>
<%
JSONArray jsonAry = new JSONArray();
for (i=0; i<len; i++) {
	String fieldName = fields[i];
	String fieldNameRaw = fieldName;
	String title = "";
	if (fieldName.equals("cws_creator")) {
		title = "创建者";		
	}
	else if (fieldName.equals("ID")) {
		title = "ID";
	}
	else if (fieldName.equals("cws_progress")) {
		title = "进度";
	}
	else if (fieldName.equals("flowId")) {
	    title = "流程ID";
    }
	else if (fieldName.equals("cws_status")) {
		title = "状态";
	}	
	else if (fieldName.equals("cws_flag")) {
		title = "冲抵状态";
	}
	else if (fieldName.equals("colOperate")) {
		title = "操作";
	}
	else {
		if (fieldName.startsWith("main")) {
			String[] ary = StrUtil.split(fieldName, ":");
			fieldName = fieldName.substring(5);
			if (ary.length>1) {
				FormDb mainFormDb = fm.getFormDb(ary[1]);
				title = mainFormDb.getName() + "：" + mainFormDb.getFieldTitle(ary[2]);
			}
		}
		else if (fieldName.startsWith("other")) {
			String[] ary = StrUtil.split(fieldName, ":");
			if (fieldName.length()>6) {			
				fieldName = fieldName.substring(6);
			}
			if (ary.length<5) {
				title = "<font color='red'>格式非法</font>";
			}
			else {
				FormDb otherFormDb = fm.getFormDb(ary[2]);
				if (ary.length>=5)
					title = otherFormDb.getName() + "：" + otherFormDb.getFieldTitle(ary[4]);
				
				if (ary.length>=8) {
					FormDb oFormDb = fm.getFormDb(ary[5]);
					title += "：" + oFormDb.getFieldTitle(ary[7]);
				}
			}
		}
		else {
			title = fd.getFieldTitle(fieldName);
		}
	}
%>
<form name="formAdd" method="post" action="module_field_list.jsp?op=modify">
    <tr fieldName="<%=fieldNameRaw%>">
      <td align="center"><%=i+1%></td>
      <td><%=fieldName%>
        <input name="code" value="<%=code%>" type="hidden" />
        <input name="formCode" value="<%=formCode%>" type="hidden" />
      	<input name="fieldName" value="<%=fieldNameRaw%>" type="hidden" />
      </td>
      <td><%=title%></td>
      <td><input name="fieldOrder" size="5" value="<%=fieldOrder[i]%>" /></td>
      <td><input name="fieldWidth" size="5" value="<%=fieldsWidth[i].equals("#")?"":fieldsWidth[i]%>" />      </td>
      <td><input name="fieldLink" style="width:98%" value="<%=(fieldsLink==null || fieldsLink[i].equals("#"))?"":fieldsLink[i]%>" /></td>
      <td align="center">
	  <input class="btn btn-default" type="submit" value="修改" />
	  &nbsp;&nbsp;
	  <input class="btn btn-default" type="button" value="删除" onClick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='module_field_list.jsp?op=del&code=<%=code%>&formCode=<%=formCode%>&fieldName=<%=fieldNameRaw%>'}}) " style="cursor:pointer"/>	  </td>
    </tr>
</form>
<%}%>

<form name="formAddCol" method="post" action="module_field_list.jsp?op=add">
    <tr >
      <td colspan="7" align="left" style="PADDING-LEFT: 10px" class="tabStyle_1_title">添加列表中的字段</td>
    </tr>
    <tr >
      <td colspan="7" align="left" style="PADDING-LEFT: 10px">字段
		  <select name="fieldName">
			  <option value="ID">-ID-</option>
			  <option value="cws_creator">-创建者-</option>
			  <option value="cws_progress">-进度-</option>
			  <%if (fd.isFlow()) {%>
			  <option value="flowId">-流程ID-</option>
			  <option value="cws_status">-记录状态-</option>
			  <%}%>
			  <option value="cws_flag">-冲抵状态-</option>
			  <option value="colOperate">-操作列-</option>
<%
Vector v = fd.getFields();
ir = v.iterator();
while (ir.hasNext()) {
	FormField ff = (FormField) ir.next();
%>
        <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
  <%
}

ModuleRelateDb mrd = new ModuleRelateDb();
Vector v2 = mrd.getFormsRelatedWith(formCode);
Iterator ir2 = v2.iterator();
while (ir2.hasNext()) {
	FormDb frmDb = (FormDb)ir2.next();
	ir = frmDb.getFields().iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField) ir.next();
%>
        <option style="BACKGROUND: #eeeeee" value="main:<%=frmDb.getCode()%>:<%=ff.getName()%>"><%=frmDb.getName()%>：<%=ff.getTitle()%></option>
  <%}
}
%>
      </select>
        顺序号
        <input name="fieldOrder" size="5" value="<%=len>0?String.valueOf(StrUtil.toDouble(fieldOrder[len-1]) + 1):"1"%>" />
        宽度
	    <input name="fieldWidth" size="5">
	  <input name="formCode" value="<%=formCode%>" type="hidden" />
	  <input name="code" value="<%=code%>" type="hidden" />
      <input class="btn btn-default" type="submit" value="添加" /></td>
    </tr>

</form>	
</table>
<form id="formAdd2" name="formAdd2" method="post" action="module_field_list.jsp?op=add">
<table class="tabStyle_1 percent98" align="center" width="95%">
    <tr >
      <td  align="left" style="PADDING-LEFT: 10px" class="tabStyle_1_title">添加映射字段</td>
    </tr>    
    <tr >
      <td align="left" style="PADDING-LEFT: 10px">
本表字段
  <select name="fieldName">
<%
ir = v.iterator();
while (ir.hasNext()) {
	FormField ff = (FormField) ir.next();
%>
        <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
  <%
}%>
		<option value="id">ID</option>
		<option value="cws_id">cws_id</option>
</select>
  =
  表单
  <select id="otherFormCode" name="otherFormCode" onchange="getFieldOfForm(this.value)">
<%
	String sql = "select code from " + fd.getTableName() + " order by orders asc";	
	ir = fd.list(sql).iterator();
	while (ir.hasNext()) {
		FormDb fdb = (FormDb) ir.next();
%>		
	<option value="<%=fdb.getCode()%>"><%=fdb.getName()%></option>	
<%}%>
</select>
<script>
$('#otherFormCode').select2();
</script>
中的字段
  <span id="spanField"></span>
  <br />
  顺序号
<input name="fieldOrder" size="5" value="<%=len>0?String.valueOf(StrUtil.toDouble(fieldOrder[len-1]) + 1):""%>" />
宽度
<input name="fieldWidth" size="5" />
<input name="formCode" value="<%=formCode%>" type="hidden" />
<input name="code" value="<%=code%>" type="hidden" />
<input name="fieldType" value="1" type="hidden" />
<input class="btn btn-default" type="submit" value="添加" /></td>
    </tr>

</table>    
</form>

<form id="formAddMulti" name="formAddMulti" method="post" action="module_field_list.jsp?op=add" onsubmit="return formAddMulti_onsubmit()">
<table class="tabStyle_1 percent98" width="95%" align="center">
    <tr >
      <td align="left" style="PADDING-LEFT: 10px" class="tabStyle_1_title">添加多重映射字段</td>
    </tr>
    <tr >
      <td align="left" style="PADDING-LEFT: 10px">字段
<input name="fieldName" style="width:200px" />
<input name="fieldType" value="2" type="hidden" />
顺序号
<input name="fieldOrder" size="5" value="<%=len>0?String.valueOf(StrUtil.toDouble(fieldOrder[len-1]) + 1):"1"%>" />
宽度
<input name="fieldWidth" size="5" />
<input name="formCode" value="<%=formCode%>" type="hidden" />
<input name="code" value="<%=code%>" type="hidden" />
<input class="btn btn-default" type="submit" value="添加" />
<br />
规则：本表字段：对应表单编码：对应字段：获取字段：......</td>
    </tr>
</table>
</form>
<br />
<form action="module_field_list.jsp?op=setFilter" method="post" name="frmFilter" id="frmFilter" onsubmit="return frmFilter_onsbumit()">
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
    <tr>
      <td align="center"  class="tabStyle_1_title">过滤条件</td>
    </tr>
    <tr>
      <td width="91%" align="left" >
<%
String filter = StrUtil.getNullStr(vsd.getString("filter")).trim();
boolean isComb = filter.startsWith("<items>") || filter.equals("");
String cssComb = "", cssScript = "";
String kind;
if (isComb) {
	cssComb = "in active";
	kind = "comb";
}
else {
	cssScript = "in active";
	kind = "script";
	%>
	<script>
	$(function() {
		$('#trOrderBy').hide();
	});
	</script>
	<%
}
%>
<ul id="myTab" class="nav nav-tabs">
   <li class="dropdown active">
      <a href="#" id="myTabDrop1" class="dropdown-toggle" data-toggle="dropdown">
         	条件<b class="caret"></b></a>
      <ul class="dropdown-menu" role="menu" aria-labelledby="myTabDrop1">
         <li><a href="#comb" kind="comb" tabindex="-1" data-toggle="tab">组合条件</a></li>
         <li><a href="#script" kind="script" tabindex="-1" data-toggle="tab">脚本条件</a></li>
      </ul>
   </li>
</ul>
<div id="myTabContent" class="tab-content">
   <div class="tab-pane fade <%=cssComb %>" id="comb">
   		<div style="margin:10px">
      		<img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>&nbsp;<a href="javascript:;" onclick="openCondition(o('condition'), o('imgId'))">配置条件</a>&nbsp;
      		<img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:<%=(isComb && !filter.equals(""))?"":"none" %>;" id="imgId"/>
      		<textarea id="condition" name="condition" style="display:none" cols="80" rows="5"><%=filter %></textarea>
		</div>
   </div>
   <div class="tab-pane fade <%=cssScript %>" id="script">
      <textarea id="filter" name="filter" style="width:98%; height:200px"><%=StrUtil.HtmlEncode(filter)%></textarea>
      <br />
		字段：
        <select id="filterField" name="filterField" onchange="if (o('filterField').value!='') o('filter').value += o('filterField').value">
        <option value="">请选择字段</option>
		<%
        ir = v.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
        %>
            <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
        <%}%>
        </select>
        &nbsp;&nbsp;
      	<a href="javascript:;" onclick="o('filter').value += '{$request.key}';" title="从request请求中获取参数">request参数</a>
        &nbsp;&nbsp;
      	<a href="javascript:;" onclick="o('filter').value += ' {$curDate}';" title="当前日期">当前日期</a>
        &nbsp;&nbsp;
      	<a href="javascript:;" onclick="o('filter').value += ' ={$curUser}';" title="当前用户">当前用户</a>
        &nbsp;&nbsp;
      	<a href="javascript:;" onclick="o('filter').value += ' in ({$curUserDept})';" title="当前用户">当前用户所在的部门</a>
        &nbsp;&nbsp;        
      	<a href="javascript:;" onclick="o('filter').value += ' in ({$curUserRole})';" title="当前用户的角色">当前用户的角色</a>
        &nbsp;&nbsp;        
      	<a href="javascript:;" onclick="o('filter').value += ' in ({$admin.dept})';" title="用户可以管理的部门">当前用户管理的部门</a>
        &nbsp;&nbsp; 
        <span style="text-align:center">
      	<input type="button" value="设计器" class="btn btn-default" onclick="ideWin=openWinMax('../admin/script_frame.jsp', screen.width, screen.height);" />
      	<br />
        (注：条件不能以and开头，可以直接输入条件，也可以使用脚本，脚本中必须返回ret)
      	</span>      
   </div>
</div>
<script>
	function openWinMax(url) {
		return window.open(url, '', 'scrollbars=yes,resizable=yes,channelmode'); // 开启一个被F11化后的窗口起作用的是最后那个特效
	}
	
	var kind = "<%=kind%>";
	function frmFilter_onsbumit() {
		if (kind=="comb") {
			o("filter").value = o("condition").value;
		}
	}

	$(function(){
	   $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
	   kind = $(e.target).attr("kind");
	   if (kind=="script") {
	   	if (o("filter").value.indexOf("<items>")==0) {
	   		o("filter").value = "";
	   	}
		$('#trOrderBy').hide();
	   }
	   else {
		$('#trOrderBy').show();
	   }
	});
	});
</script>    	  
	  
      </td>
    </tr>
    <tr id="trOrderBy">
      <td align="left" >
      	排序字段
        <select id="orderby" name="orderby">
        <option value="">请选择字段</option>
			<option value="id">ID</option>
		<%
        ir = v.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
        %>
            <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
        <%}%>
        </select>      
        顺序
        <select id="sort" name="sort">
        <option value="desc">降序</option>
        <option value="asc">升序</option>
        </select>
        &nbsp;&nbsp;
		记录状态
        <select id="cws_status" name='cws_status'>
        <option value='<%=SQLBuilder.CWS_STATUS_NOT_LIMITED%>'>不限</option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>' selected><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>
        </select>     
        &nbsp;&nbsp;
        单位
        <select id="isUnitShow" name="isUnitShow" title="模块列表过滤条件中的单位下拉框">
        <option value="0">隐藏</option>
        <option value="1">显示</option>
        </select>
        默认
        <select id="unitCode" name="unitCode">
        <option value="-1">不限</option>
        <option value="0">本单位</option>
        </select>        
        <script>
		$(function() {
			$('#orderby').val("<%=vsd.getString("orderby")%>");
			$('#sort').val("<%=vsd.getString("sort")%>");
			$('#cws_status').val("<%=vsd.getInt("cws_status")%>");
			$('#isUnitShow').val("<%=vsd.getInt("is_unit_show")%>");
			$('#unitCode').val("<%=vsd.getInt("unit_code")%>");
		});
		</script>
      </td>
    </tr>
    <tr>
      <td align="center" ><input class="btn btn-default" type="submit" value="确定" />
        <input name="code" value="<%=code%>" type="hidden" />
        <input name="formCode" value="<%=formCode%>" type="hidden" />
      </td>
    </tr>
  </table>
</form>    
<br />
<form action="module_field_list.jsp?op=setPromptIcon" method="post" name="frmPromptIcon" id="frmPromptIcon">
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
  <tr>
    <td colspan="3" class="tabStyle_1_title">行首图标</td>
  </tr>
  <tr>
    <td width="52%" align="right">当
      <select id="promptField" name="promptField">
        <option value="">无</option>
        <%
		ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField) ir.next();
		%>
			<option value="<%=ff.getName()%>" fieldType="<%=ff.getFieldType()%>"><%=ff.getTitle()%></option>
		<%
		}
		%>
      </select>
      <select id="promptCondNum" name="promptCond">
      <option value="=">=</option>
      <option value=">=">>=</option>
      <option value=">">></option>
      <option value="<=">&lt;=</option>
      <option value="&lt;"><</option>
      </select>
      <select id="promptCondStr" name="promptCond" disabled style="display:none">
      <option value="=">=</option>
      <option value="<>"><></option>
      </select>
      <script>
	  $(function() {
		  $('#promptField').change(function() {
			  	var fieldType = this.options[this.selectedIndex].getAttribute("fieldType");
				if (fieldType==<%=FormField.FIELD_TYPE_INT%> || fieldType==<%=FormField.FIELD_TYPE_FLOAT%>
					 || fieldType==<%=FormField.FIELD_TYPE_LONG%> || fieldType==<%=FormField.FIELD_TYPE_PRICE%> || fieldType==<%=FormField.FIELD_TYPE_DOUBLE%>) {
					$('#promptCondNum').show();
					$("#promptCondNum").attr("disabled",false);
					$('#promptCondStr').hide();
					$("#promptCondStr").attr("disabled",true);
				}
				else {
					$('#promptCondNum').hide();
					$("#promptCondNum").attr("disabled",true);
					$('#promptCondStr').show();
					$("#promptCondStr").attr("disabled",false);
				}
		  });
	  });
	  </script>
      <input id="promptValue" name="promptValue" value="<%=StrUtil.HtmlEncode(StrUtil.getNullStr(vsd.getString("prompt_value")))%>" />
      时，行首显示图标 
    </td>
    <td width="23%">
	<%
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
	com.redmoon.forum.ui.FileViewer fileViewer = new com.redmoon.forum.ui.FileViewer(Global.getAppPath(request) + skinPath + "/icons/prompt/");
	fileViewer.init();
	%>
    <select id="promptIcon" name="promptIcon" class="js-example-templating js-states form-control">
    <option value="">无</option>
    <%
	while(fileViewer.nextFile()){
	  	if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1) {
			String fileName = fileViewer.getFileName();
	%>
        <option value="<%=fileName%>" style="background-image: url('<%=SkinMgr.getSkinPath(request)%>/icons/prompt/<%=fileName%>');"><%=fileName %></option>
        <%
	 	}
	}
	%>
    </select>
    <script>
	var mapPrompt = new Map();
	<%
	fileViewer.init();
	while(fileViewer.nextFile()) {
	  	if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1) {
			String fileName = fileViewer.getFileName();
	  	%>
	  		mapPrompt.put('<%=fileName%>', '<%=SkinMgr.getSkinPath(request)%>/icons/prompt/<%=fileName%>');
	  	<%
		}
	}
	%>	
	$(function () {
		$('#promptIcon').val("<%=StrUtil.getNullStr(vsd.getString("prompt_icon"))%>");
		$('#promptField').val("<%=StrUtil.getNullStr(vsd.getString("prompt_field"))%>");
		$('#promptCondNum').val("<%=StrUtil.getNullStr(vsd.getString("prompt_cond"))%>");
		$('#promptCondStr').val("<%=StrUtil.getNullStr(vsd.getString("prompt_cond"))%>");
	    // 带图片
	    $("#promptIcon").select2({
	        templateResult: formatStatePrompt,
	        templateSelection: formatStatePrompt
	    });
	});
	
	function formatStatePrompt(state) {
		if (!state.id) { return state.text; }		
		var $state = $(
		  '<span><img src="' + mapPrompt.get(state.text).value + '" class="img-flag" /> ' + state.text + '</span>'
		);
		return $state;
	}; 	
    </script>
    </td>
    <td width="25%" align="left">
    <input class="btn btn-default" type="submit" value="确定" />
    <input name="formCode" value="<%=formCode%>" type="hidden" />
    <input name="code" value="<%=code%>" type="hidden" />         
    </td>
  </tr>
</table>
</form>
<br />
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
  <tr>
    <td class="tabStyle_1_title" width="13%">导航标签名称</td>
    <td colspan="3" class="tabStyle_1_title">链接</td>
    <td class="tabStyle_1_title" width="13%">顺序号</td>
    <td width="22%" class="tabStyle_1_title">操作</td>
  </tr>
<%
sql = "select code from visual_module_setup where is_use=1 order by code asc"; // orders asc";
ModuleSetupDb msd = new ModuleSetupDb();
v = msd.list(sql);
ir = v.iterator();
String jsonStr = "";
while (ir.hasNext()) {
	msd = (ModuleSetupDb)ir.next();
	
	if (jsonStr.equals(""))
		jsonStr = "{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
	else
		jsonStr += ",{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";

}

String nav_tag_name = StrUtil.getNullStr(vsd.getString("nav_tag_name"));
String[] tags = StrUtil.split(nav_tag_name, ",");

String nav_tag_order = StrUtil.getNullStr(vsd.getString("nav_tag_order"));
String[] tagOrders = StrUtil.split(nav_tag_order, ",");

String nav_tag_url = StrUtil.getNullStr(vsd.getString("nav_tag_url"));
String[] tagUrls = StrUtil.split(nav_tag_url, ",");

len = 0;
if (tags!=null)
	len = tags.length;
for (i=0; i<len; i++) {
	String tagName = tags[i];
	%>
  <form action="module_field_list.jsp?op=modifyTag" method="post" name="formTag<%=i%>" id="formTag<%=i%>">
    <tr >
      <td align="center"><%=tagName%>
          <input name="formCode" value="<%=formCode%>" type="hidden" />
          <input name="code" value="<%=code%>" type="hidden" />          
          <input name="tagName" value="<%=tagName%>" type="hidden" />      </td>
      <td colspan="3">
      <%if (!tagUrls[i].startsWith("{")) {%>
      	<input name="tagUrl" size="35" value="<%=tagUrls[i]%>" />
      <%}else{
      	JSONObject json = new JSONObject(tagUrls[i]);
      	String tagModuleCode = json.getString("moduleCode");
      	ModuleSetupDb tagMsd = new ModuleSetupDb();
      	tagMsd = tagMsd.getModuleSetupDb(tagModuleCode);
      %>
      	<div id="tagModuleSel<%=i%>"></div>
      	<%
      	String tagMsdName = "";
      	if (tagMsd==null) {
      		out.println("<span style='color:red'>模块:" + tagModuleCode + "不存在！</span>");
      	}    
      	else {
      		tagMsdName = tagMsd.getString("name");
      	}
      	%>
        <input id="tagModuleCode<%=i%>" name="tagModuleCode" type="hidden" value="<%=tagModuleCode%>" />
        <input name="tagType" value="module" type="hidden" />
      	<script>
		var tagModuleSel = $('#tagModuleSel<%=i%>').flexbox({        
				"results":[<%=jsonStr%>], 
				"total":<%=v.size()%>
			},{
			initialValue:'<%=tagMsdName%>',
		    watermark: '请选择模块',
		    paging: false,
			width:200,
			maxVisibleRows: 10,
			onSelect: function() {
				o("tagModuleCode<%=i%>").value = $("input[name=tagModuleSel<%=i%>]").val();
			}
		});
		</script>
        <%}%>
      </td>
      <td><input name="tagOrder" size="5" value="<%=tagOrders[i]%>" /></td>
      <td align="center"><input class="btn btn-default" type="submit" value="修改" />
        &nbsp;&nbsp;
        <input class="btn btn-default" name="button" type="button" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='module_field_list.jsp?op=delTag&code=<%=code%>&formCode=<%=formCode%>&tagName=<%=StrUtil.UrlEncode(tagName)%>'}}) " value="删除" />      </td>
    </tr>
  </form>
  <%}%>
  <form action="module_field_list.jsp?op=addTag" method="post" name="formTag" id="formTag" onsubmit="if (o('tagNameAdd').value=='') {jAlert('名称不能为空！','提示'); return false;}">
    <tr >
      <td colspan="3" align="right" style="PADDING-LEFT: 10px">
        名称
        <span style="margin-bottom:10px;">
        <input id="tagNameAdd" name="tagNameAdd" />
        <select id="tagType" name="tagType">
          <option value="module">模块</option>
          <option value="link">链接</option>
        </select>
        <script>
		$(function() {
			$('#tagType').change(function() {
				if ($(this).val()=="module") {
					$('#tagModuleSel').show();
					$('#tagUrl').hide();
				}
				else {
					$('#tagModuleSel').hide();
					$('#tagUrl').show();
				}
			});
		});
		</script>
      </span></td>
      <td width="24%" align="left" style="PADDING-LEFT: 10px">
      	<input id="tagUrl" name="tagUrl" style="display:none" />
        <div id="tagModuleSel"></div>
        <input id="tagModuleCode" name="tagModuleCode" type="hidden" />
		<script>
		var tagModuleSel = $('#tagModuleSel').flexbox({        
				"results":[<%=jsonStr%>], 
				"total":<%=v.size()%>
			},{
			initialValue:'',
		    watermark: '请选择模块',
		    paging: false,
			width:200,
			maxVisibleRows: 10,
			onSelect: function() {
				o("tagModuleCode").value = $("input[name=tagModuleSel]").val();
				o("tagNameAdd").value = $("#tagModuleSel").find(".ffb-sel").eq(0).text();				
			}
		});
		</script>      
      </td>
      <td align="left">
      <input name="tagOrder" size="5" value="<%=tags!=null?StrUtil.toDouble(tagOrders[i-1])+1:1%>" />
      <input name="formCode" value="<%=formCode%>" type="hidden" />
      <input name="code" value="<%=code%>" type="hidden" /></td>
      <td align="center" style="PADDING-LEFT: 10px"><input class="btn btn-default" type="submit" value="添加" /></td>
    </tr>
  </form>
</table>
<br />
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
  <tr>
    <td class="tabStyle_1_title" width="9%">名称</td>
    <td width="46%" class="tabStyle_1_title">操作列链接</td>
    <td class="tabStyle_1_title" width="24%">链接可见角色</td>
    <td class="tabStyle_1_title" width="7%">顺序号</td>
    <td width="14%" class="tabStyle_1_title">操作</td>
  </tr>
  <%
String op_link_name = StrUtil.getNullStr(vsd.getString("op_link_name"));
String[] linkNames = StrUtil.split(op_link_name, ",");

String op_link_order = StrUtil.getNullStr(vsd.getString("op_link_order"));
String[] linkOrders = StrUtil.split(op_link_order, ",");

String op_link_url = StrUtil.getNullStr(vsd.getString("op_link_url"));
String[] linkHrefs = StrUtil.split(op_link_url, ",");

String op_link_field = StrUtil.getNullStr(vsd.getString("op_link_field"));
String[] linkFields = StrUtil.split(op_link_field, ",");
String op_link_cond = StrUtil.getNullStr(vsd.getString("op_link_cond"));
String[] linkConds = StrUtil.split(op_link_cond, ",");
String op_link_value = StrUtil.getNullStr(vsd.getString("op_link_value"));
String[] linkValues = StrUtil.split(op_link_value, ",");
String op_link_event = StrUtil.getNullStr(vsd.getString("op_link_event"));
String[] linkEvents = StrUtil.split(op_link_event, ",");
String op_link_role = StrUtil.getNullStr(vsd.getString("op_link_role"));
String[] linkRoles = StrUtil.split(op_link_role, "#");
if (linkNames!=null && linkRoles==null) {
	linkRoles = new String[linkNames.length];
	for (i=0; i<linkNames.length; i++) {
		linkRoles[i] = "";
	}
}

com.redmoon.oa.flow.Leaf rootlf = new com.redmoon.oa.flow.Leaf();
rootlf = rootlf.getLeaf(com.redmoon.oa.flow.Leaf.CODE_ROOT);
com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(rootlf);

len = 0;
if (linkNames!=null)
	len = linkNames.length;
for (i=0; i<len; i++) {
	String linkName = linkNames[i];
	
	String linkField = linkFields[i];
	String linkCond = linkConds[i];
	String linkValue = linkValues[i];
	String linkEvent = linkEvents[i];
	String linkRole = linkRoles[i];
	
	if (linkField.equals("#")) {
		linkField = "";
	}
	if (linkCond.equals("#")) {
		linkCond = "";
	}
	if (linkValue.equals("#")) {
		linkValue = "";
	}
	if (linkEvent.equals("#")) {
		linkEvent = "";
	}
	int m = i+1;
	%>
  <form action="module_field_list.jsp?op=modifyLink" method="post" name="formLink<%=i%>" id="formLink<%=i%>">
    <tr id="trLink<%=i %>">
      <td align="center"><%=linkName%>
          <input name="formCode" value="<%=formCode%>" type="hidden" />
          <input name="code" value="<%=code%>" type="hidden" />          
          <input name="linkName" value="<%=linkName%>" type="hidden" /></td>
      <td>
      <%
		  boolean isCombCond = true; // 是否为组合条件
		  if (!linkField.startsWith("<items>") && !"".equals(linkField)) {
			  out.print(linkField + linkCond + linkValue);
			  isCombCond = false;
		  }
		  else {
			  // System.out.println(getClass() + " linkField=" + linkField);
		  %>
            <img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>
            <a href="javascript:;" onclick="openCondition(o('linkConds<%=i%>'), o('imgConds<%=i%>'))" title="当满足条件时，显示链接">配置条件</a>
            <span style="margin:10px">
            <img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;<%="".equals(linkField)?"display:none":""%>" id="imgConds<%=i%>"/>
            </span>
            <textarea id="linkConds<%=i%>" name="linkFieldCond" style="display:none"><%=linkField%></textarea>
		  <%
	  }
	  %>
      <%if (linkEvent.equals("flow")) {%>
      	发起流程
      	<%
		try {
			JSONObject json = new JSONObject(StrUtil.decodeJSON(linkHrefs[i]));
			com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
			lf = lf.getLeaf(json.getString("flowTypeCode"));
			FormDb fdFlow = new FormDb();
			if (lf!=null) {
				fdFlow = fdFlow.getFormDb(lf.getFormCode());
			}
			String params = json.getString("params");
            %>
          <select id="flowTypeCode<%=m%>" name="flowTypeCode">
              <%
                  flowdv.ShowDirectoryAsOptions(request, out, rootlf, rootlf.getLayer());
              %>
          </select>
          <script>
              $(function() {
                  $('#flowTypeCode<%=m%>').val('<%=lf.getCode()%>');
              })
          </script>
            <textarea id="params<%=m%>" name="params" style="display: none;"><%=params%></textarea>
		  	<a href="javascript:;" onclick="editMap(<%=m%>)"><i class="fa fa-cog" style="margin-right:5px"></i>映射字段</a>
            <%
		}
		catch( JSONException e ) {
			e.printStackTrace();
		}
		%>
      	<input name="linkEvent" type="hidden" value="flow"/>
      	<input name="linkHref" type="hidden" value="<%=StrUtil.HtmlEncode(linkHrefs[i])%>" />
      <%}else{%>
       	事件
      <select id="linkEvent" name="linkEvent">
          <option value="link">链接</option>
          <option value="click">点击</option>
          <!--<option value="flow">发起流程</option> 编辑时，不能选“发起流程”-->
      </select>
      <input name="linkHref" size="30" value="<%=StrUtil.HtmlEncode(StrUtil.decodeJSON(linkHrefs[i]))%>" />
	  <%}%>
	  <script>
	  $(function() {
	  	  // 因为form的写法不合规范，所以不能用$("#formLink<%=i%> select[name='linkField']")来获取
		  $("#trLink<%=i%> select[name='linkEvent']").val("<%=linkEvent%>");
	  });
	  </script>
      </td>
      <td align="center">
      <%
	  	String roleCodes = "", descs = "";
	  	String roles = linkRoles[i];
		String[] roleAry = StrUtil.split(roles, ",");
		if (roleAry!=null) {
			for (int k=0; k<roleAry.length; k++) {
				RoleDb rd = new RoleDb();
				rd = rd.getRoleDb(roleAry[k]);
				String roleCode = rd.getCode();
				String desc = rd.getDesc();
				if (roleCodes.equals(""))
					roleCodes += roleCode;
				else
					roleCodes += "," + roleCode;
				if (descs.equals(""))
					descs += desc;
				else
					descs += "," + desc;		
			}	 
		}      
		%>
        <textarea title="为空则表示角色不限，均可以看见此按钮" id="roleDescsLinkAdd<%=i%>" name="roleDescs" style="width:100%; height:40px" readonly="readonly"><%=descs %></textarea>
        <input id="roleCodesLinkAdd<%=i%>" name="roleCodesLink" type="hidden" value="<%=roleCodes %>" />      
        <a href="javascript:;" onclick="selRoles('LinkAdd<%=i%>')">选择角色</a>      
      </td>
      <td align="center"><input name="linkOrder" size="5" value="<%=linkOrders[i]%>" /></td>
      <td align="center">
		  <%if (isCombCond) {%>
		  <input class="btn btn-default" type="submit" value="修改" />
		  <%}%>
        &nbsp;&nbsp;
        <input class="btn btn-default" name="button" type="button" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='module_field_list.jsp?op=delLink&code=<%=code%>&formCode=<%=formCode%>&linkName=<%=StrUtil.UrlEncode(linkName)%>'}}) " value="删除" />      </td>
    </tr>
  </form>
  <%}%>
    <script>
        var curM;
        var curParamId;
        function getMaps() {
            return $('#params' + curM).val();
        }
        function editMap(m) {
            curM = m;
			curParamId = "params" + m;
            openWin('../flow/form_data_map.jsp?formCode=<%=formCode%>&flowTypeCode=' + $('#flowTypeCode' + m).val(), 800, 600);
        }
    </script>
  <form action="module_field_list.jsp?op=addLink" method="post" name="formLink" id="formLink" onsubmit="if (o('linkNameAdd').value=='') {jAlert('名称不能为空！','提示'); return false;}">
    <tr >
      <td colspan="2" align="center" style="PADDING-LEFT: 10px">
<img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>
<a href="javascript:;" onclick="openCondition(o('linkCondsAdd'), o('imgCondsAdd'))" title="当满足条件时，显示链接">配置条件</a>
<span style="margin:10px">
<img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:none" id="imgCondsAdd"/>
</span>
<textarea id="linkCondsAdd" name="linkFieldCond" style="display:none"></textarea>
名称
<input id="linkNameAdd" name="linkName" size="10" />
事件
<select id="linkEventeAdd" name="linkEvent">
  <option value="link">链接</option>
  <option value="click">点击</option>
  <option value="flow">发起流程</option>
</select>
<input id="linkHref" name="linkHref" />
        <div id="divFlow" style="display:none">
          <select id="flowTypeCodeAdd" name="flowTypeCode">
        <%
        flowdv.ShowDirectoryAsOptions(request, out, rootlf, rootlf.getLayer());
        %>
          </select>
			<input id="paramsAdd" name="params" type="hidden"/>
          	<a href="javascript:;" id="btnFlowMap"><i class="fa fa-cog" style="margin-right:5px"></i>映射字段</a>
			<script>
				$(function () {
					$('#linkEventeAdd').change(function () {
						if ($(this).val() == 'flow') {
							$('#divFlow').show();
							$('#linkHref').hide();
						} else {
							$('#divFlow').hide();
							$('#linkHref').show();
						}
					});

					$('#btnFlowMap').click(function () {
						if ($('#flowTypeCodeAdd').val() == 'not') {
							jAlert('请选择流程！', '提示');
							return;
						}
						curParamId = "paramsAdd";
						openWin('../flow/form_data_map.jsp?formCode=<%=formCode%>&flowTypeCode=' + $('#flowTypeCodeAdd').val(), 800, 600);
					})
				});

				function setSequence(mapJson) {
					$('#' + curParamId).val(mapJson);
				}
			</script>
      </div>
</td>
      <td align="center"><textarea title="为空则表示角色不限，均可以看见此按钮" id="roleDescsLinkAdd" name="roleDescsLink" style="width:100%; height:40px" readonly="readonly"></textarea>
        <input id="roleCodesLinkAdd" name="roleCodesLink" type="hidden" />
        <a href="javascript:;" onclick="selRoles('LinkAdd')">选择角色</a>
	  </td>
      <td align="center" style="PADDING-LEFT: 10px"><input name="linkOrder" size="5" value="<%=linkNames!=null?StrUtil.toDouble(linkOrders[i-1])+1:1%>" />
        <input name="formCode" value="<%=formCode%>" type="hidden" />
      <input name="code" value="<%=code%>" type="hidden" /></td>
      <td align="center" style="PADDING-LEFT: 10px"><input class="btn btn-default" type="submit" value="添加" /></td>
    </tr>
  </form>
</table>
<br />
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
  <tr>
    <td class="tabStyle_1_title"  width="11%">按钮名称</td>
    <td class="tabStyle_1_title"  width="36%">脚本</td>
    <td class="tabStyle_1_title"  width="26%">可见角色</td>
    <td class="tabStyle_1_title"  width="5%">顺序号</td>
    <td class="tabStyle_1_title"  width="9%">样式</td>
    <td width="13%"  class="tabStyle_1_title">操作</td>
  </tr>
<%
String btn_name = StrUtil.getNullStr(vsd.getString("btn_name"));
String[] btnNames = StrUtil.split(btn_name, ",");

String btn_order = StrUtil.getNullStr(vsd.getString("btn_order"));
String[] btnOrders = StrUtil.split(btn_order, ",");

String btn_script = StrUtil.getNullStr(vsd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btn_script, "#");
if (btn_script.replaceAll("#", "").equals(""))
	btnScripts = null;
if (btnNames!=null && btnScripts==null) {
	btnScripts = new String[btnNames.length];
	for (i=0; i<btnNames.length; i++)
		btnScripts[i] = "";
}

String btn_role = StrUtil.getNullStr(vsd.getString("btn_role"));
String[] btnRoles = StrUtil.split(btn_role, "#");
if (btn_role.replaceAll("#", "").equals(""))
	btnRoles = null;
if (btnNames!=null && btnRoles==null) {
	btnRoles = new String[btnNames.length];
	for (i=0; i<btnNames.length; i++) {
		btnRoles[i] = "";
	}
}

String btn_bclass = StrUtil.getNullStr(vsd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btn_bclass, ",");
// 为了与以前的版本兼容,bluewind20140420
if (btnNames!=null) {
	if (btnBclasses==null || (btnBclasses.length!=btnNames.length)) {
		btnBclasses = new String[btnNames.length];
		for (i=0; i<btnNames.length; i++)
			btnBclasses[i] = "";
	}
}

boolean hasCond = false;
len = 0;
if (btnNames!=null)
	len = btnNames.length;
for (i=0; i<len; i++) {
	String btnName = btnNames[i];
	boolean isCond = false;
	JSONObject json = null;
	if (btnScripts[i].startsWith("{")) {
		json = new JSONObject(btnScripts[i]);		
		if (json.getString("btnType").equals("queryFields")) {
			continue;
		}
	}
	%>
  <form action="module_field_list.jsp?op=modifyBtn" method="post" name="formBtn<%=i%>" id="formBtn<%=i%>">
    <tr >
      <td align="center"><%=btnName%>
          <input name="formCode" value="<%=formCode%>" type="hidden" />
          <input name="code" value="<%=code%>" type="hidden" />
          <input name="btnName" value="<%=btnName%>" type="hidden" />
      <td>
      <%
	  // 非查询
	  // System.out.println(getClass() + " btnScripts[i]=" + btnScripts[i]);
	  if (!btnScripts[i].startsWith("{")) {%>
      	<textarea name="btnScript" style="width:100%" rows="2"><%=btnScripts[i].replaceAll("/\\*\\*/", "")%></textarea>
      <%}else{
		if (json.getString("btnType").equals("batchBtn")) {
			String batchField = json.getString("batchField");
			String batchValue = json.getString("batchValue");
			%>
			<div>
           	 置
            <select id="batchField<%=i%>" name="batchField">
			<%
            ir = fd.getFields().iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
            %>
                <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
            <%
            }
            %>              
            </select>
                                 为
            <input id="batchValue<%=i%>" name="batchValue" value="<%=batchValue%>" />
            <script>
			o("batchField<%=i%>").value = "<%=batchField%>";
			</script>
            </div>
			<%
		}
		else {
		  	isCond = true;
		}		
	  }
	  %>
      </td>
      <td align="center">
      <%
	  if (!isCond) {
	  	String roleCodes = "", descs = "";
	  	String roles = btnRoles[i];
		String[] roleAry = StrUtil.split(roles, ",");
		if (roleAry!=null) {
			for (int k=0; k<roleAry.length; k++) {
				RoleDb rd = new RoleDb();
				rd = rd.getRoleDb(roleAry[k]);
				String roleCode = rd.getCode();
				String desc = rd.getDesc();
				if (roleCodes.equals(""))
					roleCodes += roleCode;
				else
					roleCodes += "," + roleCode;
				if (descs.equals(""))
					descs += desc;
				else
					descs += "," + desc;		
			}	 
		}
	  %>
        <textarea title="为空则表示角色不限，均可以看见此按钮" style="width:100%; height:40px" id="roleDescs<%=i%>" name="roleDescs" readonly="readonly"><%=descs%></textarea>
        <input id="roleCodes<%=i%>" name="roleCodes" value="<%=roleCodes%>" type=hidden />
        <a href="javascript:;" onclick="selRoles('<%=i%>')">选择角色</a>           
      <%}%>   
      </td>
      <td align="center"><input name="btnOrder" size="5" value="<%=btnOrders[i]%>" /></td>
      <td align="center">
      <%if (!btnScripts[i].startsWith("{") || (btnScripts[i].startsWith("{") && !isCond)) {%>      
      <select id="btnBclass<%=i %>" name="btnBclass" class="js-example-templating js-states form-control">
      <%
      ArrayList<String[]> btnAry = CSSUtil.getFlexigridBtn();
      int btnAryLen = btnAry.size();
      for (int k=0; k<btnAryLen; k++) {
      	String[] ary = btnAry.get(k);
      	String selected = "";
      	if (btnBclasses[i].equals(ary[0])) {
      		selected = "selected";
      	}
      	%>
      	<option value="<%=ary[0] %>" <%=selected %> style="background-image: url('<%=SkinMgr.getSkinPath(request)%>/flexigrid/<%=ary[1] %>');"><%=ary[0]%></option>
      	<%
      }
      %>
      </select>      
      <script>
		$(function () {
		    //带图片
		    $("#btnBclass<%=i%>").select2({
		        templateResult: formatState,
		        templateSelection: formatState
		    });
		});      
      </script>
      <%}else{%>
          <input type="hidden" name="btnBclass" size="5" value="<%=btnBclasses[i]%>" />
          查询
      <%}%>
      </td>
      <td align="center"><input class="btn btn-default" name="submit2" type="submit" value="修改" />
        &nbsp;&nbsp;
        <input class="btn btn-default" name="button" type="button" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='module_field_list.jsp?op=delBtn&code=<%=code%>&formCode=<%=formCode%>&btnName=<%=StrUtil.UrlEncode(btnName)%>'}}) " value="删除" />      </td>
    </tr>
  </form>
  <%}%>
  <form action="module_field_list.jsp" method="post" name="formBtn" id="formBtn" onsubmit="if (getRadioValue('btnBatchOrScript')=='0') {$('#opAddBtn').val('addBtnBatch');} else {$('#opAddBtn').val('addBtn')}">
    <tr >
      <td align="center"><input id="btnName" name="btnName" size="8" />
      <input id="opAddBtn" name="op" type="hidden" value="addBtn" />
      </td>
      <td>
      <div style="margin-bottom:10px;">
      <input type="radio" name="btnBatchOrScript" value="0" checked />
      &nbsp;批处理
      <input type="radio" name="btnBatchOrScript" value="1" />&nbsp;脚本
      </div>
      <span id="spanBatch">
      置
      <select id="batchField" name="batchField">
<%
ir = fd.getFields().iterator();
while (ir.hasNext()) {
	FormField ff = (FormField) ir.next();
%>
        <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
<%
}
%>     
      </select>
      为
      <input id="batchValue" name="batchValue" />
      </span>
      <span id="spanScript" style="display:none">
      <textarea id="btnScript" name="btnScript" cols="50" rows="2"></textarea>
      </span>
      <script>
	  $('input[name=btnBatchOrScript]').click(function() {
		  if ($(this).val()==0) {
			  $('#spanBatch').show();
			  $('#spanScript').hide();
		  }
		  else {
			  $('#spanBatch').hide();
			  $('#spanScript').show();
		  }
	  });
	  </script>
      </td>
      <td align="center">
        <textarea title="为空则表示角色不限，均可以看见此按钮" id="roleDescsAdd" name="roleDescs" style="width:100%; height:40px" readonly="readonly"></textarea>
        <input id="roleCodesAdd" name="roleCodes" type="hidden" />      
        <a href="javascript:;" onclick="selRoles('Add')">选择角色</a>
        <script>
        var objCode, objDesc;
        function selRoles(param) {
        	var descId = "roleDescs" + param;
        	var codeId = "roleCodes" + param;
        	objCode = o(codeId);
        	objDesc = o(descId);
        	openWin('../role_multi_sel.jsp?roleCodes=' + objCode.value + '&unitCode=<%=StrUtil.UrlEncode(privilege.getUserUnitCode(request))%>', 526, 435);
        }
        
		function setRoles(roles, descs) {
			objCode.value = roles;
			objDesc.value = descs;
		}        
        </script>
      </td>
      <td align="center"><input name="btnOrder" size="5" value="<%=btnNames!=null?StrUtil.toDouble(btnOrders[i-1])+1:1%>" /></td>
      <td align="center">
      <select id="btnBclassAdd" name="btnBclass" class="js-example-templating js-states form-control">
      <%
      ArrayList<String[]> btnAry = CSSUtil.getFlexigridBtn();
      int btnAryLen = btnAry.size();
      for (int k=0; k<btnAryLen; k++) {
      	String[] ary = btnAry.get(k);
      	%>
      	<option value="<%=ary[0] %>" style="background-image: url('<%=SkinMgr.getSkinPath(request)%>/flexigrid/<%=ary[1] %>');"><%=ary[0]%></option>
      	<%
      }
      %>
      </select>
      <script>
      var map = new Map();
      <%
      for (int k=0; k<btnAryLen; k++) {
      	String[] ary = btnAry.get(k);
      	%>
     	map.put('<%=ary[0]%>', '<%=SkinMgr.getSkinPath(request)%>/flexigrid/<%=ary[1] %>');
      	<%
      }
      %>
      	var oMenuIcon;
		$(function () {
		    //带图片
		    oMenuIcon = $("#btnBclassAdd").select2({
		        templateResult: formatState,
		        templateSelection: formatState
		    });
		    
			var btnName = new LiveValidation('btnName');
			btnName.add( Validate.Presence );
		    
		});
		
		function formatState(state) {
		    if (!state.id) { return state.text; }
		    var $state = $(
		      '<span><img src="' + map.get(state.text).value + '" class="img-flag" /> ' + state.text + '</span>'
		    );
		    return $state;
		};      	
      	</script>
      </td>
      <td align="center">
      <input class="btn btn-default" type="submit" value="添加按钮" />
      <input name="formCode" value="<%=formCode%>" type="hidden" />
      <input name="code" value="<%=code%>" type="hidden" />      
      </td>
    </tr>
  </form>
</table>
<br />
</body>
<script>
  var work_log = "<%=work_log%>";
	if(work_log==1){
		$("#is_workLog").attr({"checked":"checked"});
	}else{
		$("#is_workLog").removeAttr("checked");
	}
function changeWorkLog(){
	//alert($("#is_workLog:checked").parent().html());
	if($("#is_workLog:checked").parent().html() != null){
		$(".is_workLog").val(1);
	}else{
		$(".is_workLog").val(0);
	}
}

function formAddMulti_onsubmit() {
	if (formAddMulti.fieldName.value=="") {
		jAlert("字段不能为空！","提示");
		return false;
	}
}

function getScript() {
	return $('#filter').val();
}

function setScript(script) {
	$('#filter').val(script);
}

function openWin(url,width,height) {
	var newwin=window.open(url,"fieldWin","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
	return newwin;
}

var curCondsObj, curImgObj;
function openCondition(condsObj, imgObj){
	curCondsObj = condsObj;
	curImgObj = imgObj
	
    openWin("",1024,568);

	var url = "module_combination_condition.jsp";
	var tempForm = document.createElement("form");
	tempForm.id="tempForm1";  
	tempForm.method="post";
	tempForm.action=url;  

	var hideInput = document.createElement("input");
	hideInput.type="hidden";
	hideInput.name= "condition";
	hideInput.value = curCondsObj.value;
	tempForm.appendChild(hideInput);   
	    
	hideInput = document.createElement("input");  
	hideInput.type="hidden";  
	hideInput.name= "fromValue";
	hideInput.value=  "" ;
	tempForm.appendChild(hideInput);   
			  
	hideInput = document.createElement("input");  
	hideInput.type="hidden";  
	hideInput.name= "toValue";
	hideInput.value=  ""
	tempForm.appendChild(hideInput);   
	    
	hideInput = document.createElement("input");  
	hideInput.type="hidden";  
	hideInput.name= "moduleCode";
	hideInput.value=  "<%=code %>";
	tempForm.appendChild(hideInput);   
	    
	hideInput = document.createElement("input");  
	hideInput.type="hidden";  
	hideInput.name= "operate";
	if (curCondsObj.name=="validatePropHidden") {
		hideInput.value=  "validate";
	}
	else {
		hideInput.value=  "";
	}
	tempForm.appendChild(hideInput);   
	
	document.body.appendChild(tempForm);
	tempForm.target="fieldWin";
	tempForm.submit();
	document.body.removeChild(tempForm);
}

function setCondition(val) {
	if (curCondsObj.name=="validatePropHidden") {
		curCondsObj.value = val;
		$.ajax({
			url: "module_field_list.jsp",
			type: "post",
			data: {
				op: "setValidate",
				code: "<%=code%>",
				validateProp: val
			},
			dataType: "json",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status) {
				jAlert(data.msg, "提示");
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
			}
		});			
	}
	else {
		curCondsObj.value = val;
	}
	if (val=="") {
		$(curImgObj).hide();
	}
	else {
		$(curImgObj).show();
	}		
}

function openMsgPropDlg(){
  openWin("module_msg_prop.jsp?moduleCode=<%=code%>", 700, 600);
}		 

function getMsgProp() {
	return o("msgProp").value;
}

function setMsgProp(msgProp) {
	o("msgProp").value = msgProp;
	$.ajax({
		url: "module_field_list.jsp",
		type: "post",
		data: {
			op: "setMsgProp",
			code: "<%=code%>",
			msgProp: msgProp
		},
		dataType: "json",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status) {
            jAlert(data.msg, "提示");
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
		}
	});	
}

<%
com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
String version = StrUtil.getNullStr(oaCfg.get("version"));
String spVersion = StrUtil.getNullStr(spCfg.get("version"));
%>

var ideWin;
var onMessage = function(e) {
	var d = e.data;
	var data = d.data;
	var type = d.type;
	if (type=="setScript") {
		setScript(data);
	}
	else if (type=="getScript") {
		var data={
		    "type":"openerScript",
		    "version":"<%=version%>",
		    "spVersion":"<%=spVersion%>",
		    "scene":"module.filter",	    
		    "data":getScript()
	    }
		ideWin.leftFrame.postMessage(data, '*');
	}
}

$(function() {
     if (window.addEventListener) { // all browsers except IE before version 9
         window.addEventListener("message", onMessage, false);
     } else {
         if (window.attachEvent) { // IE before version 9
             window.attachEvent("onmessage", onMessage);
         }
     }
});
</script>
</html>