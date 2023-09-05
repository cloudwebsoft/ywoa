<%@ page contentType="text/html;charset=utf-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.api.IBasicSelectCtl" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
String code = (String)request.getAttribute("code");
String formCode = (String)request.getAttribute("formCode");
FormDb fd = new FormDb(formCode);
%>
<!DOCTYPE html>
<html>
<head>
<title>智能模块设计 - 导入设置</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../inc/livevalidation_standalone.js"></script>
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
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
%>
<%@ include file="../../../visual/module_setup_inc_menu_top.jsp"%>
<script>
o("menu6").className="current";
</script>
<div class="spacerH"></div>
<form id="form1" method="post" action="../visual/setModuleImportCols.do">
<div style="text-align:center; margin:10px auto">
设置名称：<input id="name" name="name" />
</div>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  	<tr>
        <td width="17%" class="tabStyle_1_title">列名</td>
        <td width="27%" class="tabStyle_1_title">字段名</td>
        <td width="28%" class="tabStyle_1_title">规则</td>
        <td width="28%" class="tabStyle_1_title">空值</td>
    </tr>
<c:forEach var="item" items="${cols}" varStatus="status">
  	<tr>
  	  <td>
      ${item}
      <input name="title${status.index}" value="${item}" type="hidden" />
      </td>
  	  <td>
		<select id="field${status.index}" name="field${status.index}">
        <option value="">不导入</option>
		<%=opts %>
		</select>
		<script>
		$("#field${status.index} option:contains('${item}')").each(function(){
		  if ($(this).text().indexOf('${item}')!=-1) {
		     $(this).attr('selected', true);
		  }
		});		
		</script>
	  </td>
  	  <td>
  	 	<select id="canNotRepeat${status.index}" name="canNotRepeat${status.index}">
  	 	<option value="1">不允许重复</option>
  	 	<option value="0" selected>无</option>
  	 	</select> 
  	  </td>
  	  <td><select id="canNotEmpty${status.index}" name="canNotEmpty${status.index}">
  	    <option value="0" selected="selected">无</option>
  	    <option value="1">不允许为空</option>
  	    <option value="2">为空则过滤掉</option>
	    </select></td>
    </tr>
</c:forEach>
</table>
<table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
<tr>
<td>
<b>主表基础数据清洗</b></td>
</tr>
</table>
<%
SelectMgr sm = new SelectMgr();
MacroCtlMgr mm = new MacroCtlMgr();
ir = fd.getFields().iterator();
MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
IBasicSelectCtl basicSelectCtl = macroCtlService.getBasicSelectCtl();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
		MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		if (mu!=null && mu.getCode().equals("macro_flow_select")) {
			String basicCode = basicSelectCtl.getCode(ff);
			SelectDb sd = sm.getSelect(basicCode);
			if (sd.getType() == SelectDb.TYPE_TREE) {
				continue;
			}
%>
    <table class="percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tr>
    <td>
    <input type="checkbox" name="is_clean_<%=ff.getName()%>" value="1" /><%=ff.getTitle()%>
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
%>
			<tr>
			  <td><%=sod.getName()%></td>
			  <td><%=sod.getValue()%></td>
			  <td><input name="<%=ff.getName()%>_<%=StrUtil.escape(sod.getValue())%>" value="<%=sod.getName()%>" onfocus="this.select()" /></td>
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
	<div style="text-align:center; margin-top: 10px">
		<input type="button" class="btn btn-default" value="确定" onclick="submitForm()" />
	</div>
	<input id="code" name="code" type="hidden" value="${code}" />
	<input id="formCode" name="formCode" type="hidden" value="${formCode}" />
	<input id="colCount" name="colCount" type="hidden" value="${fn:length(cols)}" />
	<input id="xlsTmpPath" name="xlsTmpPath" type="hidden" value="${xlsTmpPath}" />
</form>
<br />
</body>
<script language="javascript">
	var nameLive = new LiveValidation('name');
	nameLive.add(Validate.Presence);
	nameLive.add(Validate.Length, {minimum: 1, maximum: 45});
	var lv_formCode = new LiveValidation('formCode');
	
	function submitForm() {
		if (!LiveValidation.massValidate(lv_formCode.formObj.fields)) {
			jAlert("请检查表单中的内容填写是否正常！", "提示");
			return;
		}
		$.ajax({
			type: "post",
			url: "../visual/setModuleImportCols.do",
			contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
			data: $("#form1").serialize(),
			dataType: "html",
			beforeSend: function (XMLHttpRequest) {
				// $('#container').showLoading();
			},
			success: function (data, status) {
				data = $.parseJSON(data);
				if (data.ret == "1") {
					jAlert_Redirect("操作成功！", "提示", "../visual/module_import_list.do?code=" + $('#code').val() + "&formCode=" + $('#formCode').val());
				} else {
					jAlert(data.msg, "提示");
				}
			},
			complete: function (XMLHttpRequest, status) {
				// $('#container').hideLoading();
			},
			error: function (XMLHttpRequest, textStatus) {
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	}

	$(function() {
		$('input, select, textarea').each(function() {
			if (!$('body').hasClass('form-inline')) {
				$('body').addClass('form-inline');
			}
			// ffb-input 为flexbox的样式
			if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && !$(this).hasClass('tSearch') &&
					$(this).attr('type') != 'hidden' && $(this).attr('type') != 'file' && !$(this).hasClass('ffb-input')) {
				$(this).addClass('form-control');
				$(this).attr('autocomplete', 'off');
			}
		});
	})
</script>
</html>