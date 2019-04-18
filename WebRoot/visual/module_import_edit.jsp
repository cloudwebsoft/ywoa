<%@ page contentType="text/html;charset=utf-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="org.json.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String code = ParamUtil.get(request, "code");
String formCode = ParamUtil.get(request, "formCode");
long id = ParamUtil.getLong(request, "id", -1);

boolean isAfterEdit = false;
if (id==-1) {
	code = (String)request.getAttribute("code");
	formCode = (String)request.getAttribute("formCode");
	id = StrUtil.toLong((String)request.getAttribute("id"), -1);
	isAfterEdit = true;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计 - 导入设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script src="../inc/livevalidation_standalone.js"></script>
<%
FormDb fd = new FormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

if (id==-1) {
	out.print(StrUtil.jAlert_Back("标识非法！","提示"));
	return;
}

if (isAfterEdit) {
	out.print(StrUtil.jAlert("操作成功！", "提示"));
}
%>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

StringBuffer opts = new StringBuffer();
Iterator ir = fd.getFields().iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	opts.append("<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>");
}

ModuleRelateDb mrd = new ModuleRelateDb();
ir = mrd.getModulesRelated(formCode).iterator();
while (ir.hasNext()) {
	mrd = (ModuleRelateDb)ir.next();
	String relateCode = mrd.getString("relate_code");
	FormDb fd2 = new FormDb(relateCode);
	
	Iterator irField = fd2.getFields().iterator();
	while (irField.hasNext()) {
		FormField ff = (FormField)irField.next();
		opts.append("<option value='nest." + relateCode + "." + ff.getName() + "'>" + fd2.getName() + "：" + ff.getTitle() + "</option>");
	}	
}

ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
mid = mid.getModuleImportTemplateDb(id);
String name = mid.getString("name");
String rules = mid.getString("rules");
JSONArray arr = null;
try {
	arr = new JSONArray(rules);
}
catch (JSONException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
%>
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu6").className="current";
<%
if (arr==null) {
	out.print(StrUtil.jAlert_Back("规则为空或非法！","提示"));
	return;
}
%>
</script>
<div class="spacerH"></div>
<form id="form1" method="post" action="module_import_edit.do">
<div style="text-align:center; margin:10px auto">
设置名称：<input id="name" name="name" value="<%=name%>" />
</div>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  	<tr>
        <td width="16%" class="tabStyle_1_title">列名</td>
        <td width="29%" class="tabStyle_1_title">字段名</td>
        <td width="32%" class="tabStyle_1_title">重复</td>
        <td width="23%" class="tabStyle_1_title">空值</td>
    </tr>
<%
int len = arr.length();
for (int i=0; i<len; i++) {
	JSONObject json = (JSONObject)arr.get(i);
	String title = json.getString("title");
	String fieldName = json.getString("name");
	int canNotRepeat = json.getInt("canNotRepeat");
	int canNotEmpty = 0;
	if (json.has("canNotEmpty")) {
		canNotEmpty = json.getInt("canNotEmpty");
	}
%>
  	<tr>
  	  <td>
      <%=title %>
      <input name="title<%=i %>" value="<%=title %>" type="hidden" />
      </td>
  	  <td>
		<select id="field<%=i %>" name="field<%=i %>">
        <option value="">不导入</option>        
		<%=opts %>
		</select>
	  </td>
  	  <td>
  	 	<select id="canNotRepeat<%=i %>" name="canNotRepeat<%=i %>">
  	 	<option value="1">不允许重复</option>
  	 	<option value="0" selected>无</option>
  	 	</select> 
  	  </td>
  	  <td>
      <select id="canNotEmpty<%=i%>" name="canNotEmpty<%=i%>">
  	 	<option value="0" selected>无</option>      
  	 	<option value="1">不允许为空</option>
  	 	<option value="2">为空则滤除</option>
      </select>
	  <script>
      $("#field<%=i%>").val("<%=fieldName%>");		
      $("#canNotRepeat<%=i%>").val("<%=canNotRepeat%>");		
      $("#canNotEmpty<%=i%>").val("<%=canNotEmpty%>");		
      </script>  	 	
      </td>
    </tr>
<%}%>
</table>
<table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
<tr>
<td>
<b>主表基础数据清洗</b></td>
</tr>
</table>
<%
String strJson = StrUtil.getNullStr(mid.getString("cleans"));
JSONArray ary = null;
if (!"".equals(strJson)) {
	ary = new JSONArray(strJson);
}
SelectMgr sm = new SelectMgr();
MacroCtlMgr mm = new MacroCtlMgr();
ir = fd.getFields().iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
		MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		if (mu!=null && mu.getCode().equals("macro_flow_select")) {
			SelectDb sd = sm.getSelect(ff.getDefaultValueRaw());
			boolean isClean = false;
			JSONObject json = null;
			if (ary!=null) {
				for (int i=0; i<ary.length(); i++) {
					json = ary.getJSONObject(i);
					if (ff.getName().equals(json.get("fieldName"))) {
						isClean = true;
						break;
					}
				}
			}
%>
    <table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tr>
    <td>
    <input type="checkbox" name="is_clean_<%=ff.getName()%>" value="1" <%=isClean?"checked":"" %> /><%=ff.getTitle()%>
          （勾选后才能清洗数据）
    </td>
    </tr>
    </table>
    <table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
        <tr>
            <td width="32%" class="tabStyle_1_title">名称</td>
            <td width="30%" class="tabStyle_1_title">值</td>
            <td width="38%" class="tabStyle_1_title">对应的名称</td>
        </tr>
<%
		Vector v = sd.getOptions(new JdbcTemplate());
		Iterator irBasic = v.iterator();
		while (irBasic.hasNext()) {
			SelectOptionDb sod = (SelectOptionDb) irBasic.next();
			if (!sod.isOpen())
				continue;
			String val = sod.getValue();
%>
			<tr>
			  <td><%=sod.getName()%></td>
			  <td><%=sod.getValue()%></td>
			  <td>
			  <%
			  String otherVal = "";
			  if (isClean && json!=null) {
			  	Iterator keys = json.keys();
			  	while (keys.hasNext()) {
			  		String key = (String)keys.next();
			  		String toVal = json.getString(key);
			  		if (val.equals(toVal)) {
			  			otherVal = key;
			  			break;
			  		}
			  	}
			  }
			  %>
			  <input name="<%=ff.getName()%>_<%=StrUtil.escape(sod.getValue())%>" value="<%=otherVal%>" onfocus="this.select()" />
			  </td>
			</tr>
<%
		}
%>
	</table>
<%
		}
	}
}
%>
<input id="code" name="code" type="hidden" value="<%=code %>" />
<input id="formCode" name="formCode" type="hidden" value="<%=formCode %>" />
<input id="id" name="id" type="hidden" value="<%=id%>" />
<input id="colCount" name="colCount" type="hidden" value="<%=len%>" />
<div style="text-align:center">
	<input type="button" value="确定" onclick="submitForm()" />
</div>
</form>
<br />
</body>
<script language="javascript">
var name = new LiveValidation('name');
name.add( Validate.Presence );
name.add(Validate.Length, { minimum: 1, maximum: 45 } );

var lv_formCode = new LiveValidation('formCode');

function submitForm() {
	if (!LiveValidation.massValidate(lv_formCode.formObj.fields)) {
		jAlert("请检查表单中的内容填写是否正常！", "提示");
		return;
	}	
	$.ajax({
		type: "get",
		url: "module_import_edit.do",
		data: $("#form1").serialize(),
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			// $('#container').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			jAlert(data.msg, "提示");
		},
		complete: function(XMLHttpRequest, status){
			// $('#container').hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});		
}
</script>
</html>