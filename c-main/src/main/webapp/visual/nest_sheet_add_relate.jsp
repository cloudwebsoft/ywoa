<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：嵌套表格2中添加行
- 访问规则：从nest_sheet_view.jsp中访问
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2013-5-29
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
*/
String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
if ("".equals(formCode)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
String menuItem = ParamUtil.get(request, "menuItem");

// 用于查询选择宏控件
request.setAttribute("formCode", formCodeRelated);

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);
if (fd==null || !fd.isLoaded()) {
	out.println(StrUtil.Alert("表单不存在！"));
	return;
}

String moduleCode = ParamUtil.get(request, "moduleCode");
if ("".equals(moduleCode)) {
	moduleCode = formCodeRelated;
}

String relateFieldValue = String.valueOf(com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
if (parentId==-1) {
	ModuleRelateDb mrd = new ModuleRelateDb();
	mrd = mrd.getModuleRelateDb(formCode, moduleCode);
	if (mrd==null) {
		out.print(StrUtil.Alert_Back("请检查模块是否相关联！"));
		return;
	}
}
else {
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
	relateFieldValue = fdm.getRelateFieldValue(parentId, moduleCode);
	if (relateFieldValue==null) {
		out.print(StrUtil.Alert_Back("请检查模块是否相关联！"));
		return;
	}
}

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCode);
if (msd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");
ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (isNestSheetCheckPrivilege && !mpd.canUserAppend(privilege.getUser(request))) {
	%>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
long actionId = ParamUtil.getLong(request, "actionId", -1);
request.setAttribute("actionId", String.valueOf(actionId));

// 用于com.redmoon.oa.visual.Render
request.setAttribute("pageKind", "nest_sheet_relate");
// 用于com.redmoon.oa.flow.macroctl.NestSheetCtl
String pageType = "add_relate";
request.setAttribute("pageType", pageType);

int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>智能模块设计-添加内容</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
	<link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
	<style>
		input[readonly] {
			background-color: #ddd;
		}

		select[readonly] {
			background-color: #ddd;
		}

		textarea[readonly] {
			background-color: #ddd;
		}
	</style>
	<meta http-equiv="pragma" content="no-cache"/>
	<meta http-equiv="Cache-Control" content="no-cache, must-revalidate"/>
	<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT"/>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<script src="../inc/map.js"></script>
	<script src="../js/jquery.raty.min.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
	<script src="../inc/upload.js"></script>
	<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
	<script src="<%=request.getContextPath()%>/inc/flow_js.jsp?parentFormCode=<%=formCode%>"></script>
	<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp?pageType=add_relate&moduleCode=<%=moduleCode%>&formCode=<%=formCode%>&parentId=<%=parentId%>&flowId=<%=flowId%>&actionId=<%=actionId%>&time=<%=Math.random()%>"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.flexbox.js"></script>

	<link href="../js/select2/select2.css" rel="stylesheet"/>
	<script src="../js/select2/select2.js"></script>
	<script src="../js/select2/i18n/zh-CN.js"></script>

	<script src="../js/jquery.form.js"></script>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

	<link rel="stylesheet" href="../js/poshytip/tip-yellowsimple/tip-yellowsimple.css" type="text/css" />
	<script type="text/javascript" src="../js/poshytip/jquery.poshytip.js"></script>

	<script src="../js/layui/layui.js" charset="utf-8"></script>

	<script>
		function setradio(myitem, v) {
			var radioboxs = document.all.item(myitem);
			if (radioboxs != null) {
				for (i = 0; i < radioboxs.length; i++) {
					if (radioboxs[i].type == "radio") {
						if (radioboxs[i].value == v)
							radioboxs[i].checked = true;
					}
				}
			}
		}

		// 控件完成上传后，调用Operate()
		function Operate() {
			// alert(redmoonoffice.ReturnMessage);
		}
	</script>
</head>
<body>
<%
	if (isShowNav == 1) {
%>
<%@ include file="module_inc_menu_top.jsp" %>
<%
	}
%>
<div class="spacerH"></div>
<form action="../flow/createNestSheetRelated.do?actionId=<%=actionId %>&parentId=<%=parentId%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&pageType=<%=pageType%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm" class="form-inline">
	<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
		<tr>
			<td align="left">
				<%
					com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
					out.print(rd.rendForAdd(msd));
				%><br/>
				<%if (fd.isHasAttachment()) {%>
				<script>initUpload()</script>
				<%}%>
			</td>
		</tr>
		<tr>
			<td height="30" align="center"><input class="btn btn-default" type="submit" value="确定"/>
				<input id="cws_id" name="cws_id" value="<%=relateFieldValue%>" type="hidden"/>
				<%
					if (parentId != -1 && flowId != com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
				%>
				<input name="cwsStatus" value="<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>" type="hidden"/>
				<%
					}
				%>
				<input name="flowId" value="<%=flowId%>" type="hidden"/>
			</td>
		</tr>
	</table>
<span id="spanTempCwsIds"></span>
</form>
</body>
<link rel="stylesheet" href="../js/jquery-contextmenu/jquery.contextMenu.min.css">
<script src="../js/jquery-contextmenu/jquery.contextMenu.js"></script>
<script src="../js/jquery-contextmenu/jquery.ui.position.min.js"></script>
<script>
	<%
	if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
	%>
		// 不能放在$(function中，原来的tabStyle_8风格会闪现
		// $(function() {
		var $table = $('#visualForm').find('.tabStyle_8');
		$table.addClass('layui-table');
		$table.removeClass('tabStyle_8');
		// })
	<%
	}
	%>

	// 记录添加的嵌套表格2记录的ID
	function addTempCwsId(formCode, cwsId) {
		var name = "<%=com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS%>_" + formCode;
		var inp;
		try {
			inp = document.createElement('<input type="hidden" name="' + name + '" />');
		} catch(e) {
			inp = document.createElement("input");
			inp.type = "hidden";
			inp.name = name;
		}
		inp.value = cwsId;

		spanTempCwsIds.appendChild(inp);
	}

	$(function() {
		var options = {
			//target:        '#output2',   // target element(s) to be updated with server response
			beforeSubmit:  preSubmit,  // pre-submit callback
			success:       showResponse  // post-submit callback

			// other available options:
			//url:       url         // override for form's 'action' attribute
			//type:      type        // 'get' or 'post', override for form's 'method' attribute
			//dataType:  null        // 'xml', 'script', or 'json' (expected server response type)
			//clearForm: true        // clear all form fields after successful submit
			//resetForm: true        // reset the form after successful submit

			// $.ajax options can be used here too, for example:
			//timeout:   3000
		};

		// bind to the form's submit event
		var lastSubmitTime = new Date().getTime();
		$('#visualForm').submit(function() {
			var lv_flowId = new LiveValidation('flowId');
			if (!LiveValidation.massValidate(lv_flowId.formObj.fields)) {
				jAlert(LiveValidation.liveErrMsg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
				return false;
			}

			// 通过判断时间，禁多次重复提交
			var curSubmitTime = new Date().getTime();
			// 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
			if (curSubmitTime - lastSubmitTime < 500) {
				lastSubmitTime = curSubmitTime;
				$('#visualForm').hideLoading();
				return false;
			}
			else {
				lastSubmitTime = curSubmitTime;
			}

			$(this).ajaxSubmit(options);
			return false;
		});
	});

	function preSubmit() {
		$('#visualForm').showLoading();
	}

	function showResponse(responseText, statusText, xhr, $form) {
		$('#visualForm').hideLoading();
		var data = $.parseJSON($.trim(responseText));
		if (data.ret === "1") {
			if (data.isVisual) {
				doVisual(data.fdaoId, data.tds, data.token);
			} else {
				doFlow(data.fdaoId, data.tds, data.token, data.sums);
			}
		}
		if (data.msg != null)
			data.msg = data.msg.replace(/\\r/ig, "<BR>");
		jAlert(data.msg, "提示");
	}

	function doVisual(fdaoId,tds,token){
		// 如果有父窗口，则自动刷新父窗口
		if (window.opener!=null) {
			window.opener.addTempCwsId("<%=formCodeRelated%>", fdaoId);
			try {
				window.opener.insertRow_<%=moduleCode%>("<%=formCodeRelated%>", fdaoId, tds, token);
				// 计算控件合计
				window.opener.callCalculateOnload();
			}
			catch (e) {
			}
			window.close();
		}
	}

	function doFlow(fdaoId,tds,token,sums){
		// 如果有父窗口，则自动刷新父窗口
		if (window.opener!=null) {
			// 不能刷新，因为在insertRow还将调用onNestInsertRow事件
			// window.parent.refreshNestSheetCtl<%=moduleCode%>();
			try {
				window.opener.insertRow_<%=moduleCode%>("<%=formCodeRelated%>", fdaoId, tds, token);
				// 计算控件合计
				// window.opener.callCalculateOnload();
				window.opener.callByNestSheet(sums, '<%=formCodeRelated%>');
			}
			catch (e) {
			}
			window.close();
		}
	}

	function setNotReadOnly() {
		var obj = o("visualForm");
		for (var i = 0; i < obj.elements.length; i++) {
			var el = obj.elements[i];
			var $el = $(el);
			if ($el.attr('readonly')!=null && ($el.attr('readOnlyType') == 1 || $el.attr('readOnlyType') == 2)) {
				$el.removeAttr('readonly');
				// console.log($el.attr('name') + ' ' + $el.attr('title') + ' ' + obj.elements[i].tagName);
				if (el.type == "radio") {
					// 删除其父节点span的readonly属性
					$el.parent().removeAttr('readonly');
					$el.removeAttr('onchange');
					$el.removeAttr('onfocus');
					$el.click(function() {
						$(this).attr('checked', true);
					});
				}
				else if (el.tagName == "SELECT") {
					$el.removeAttr('onchange');
					$el.removeAttr('onfocus');
				}
				else if (el.type == "checkbox") {
					$el.removeAttr('onclick');
				}
			}
		}
	}

	$(function() {
		// 将仅编辑时只读的字段，变为可写
		setNotReadOnly();

		$('input[type=radio]').each(function(i) {
			if ($(this).attr("readonly")==null) {
				$(this).addClass('radio-menu');
			}
		});

		// 不能用BootstrapMenu，因为chrome上会导致radio无法点击
		$.contextMenu({
			selector: '.radio-menu',
			trigger: 'hover',
			delay: 1000,
			callback: function(key, options) {
				if (key == 'cancel') {
					var $obj = options.$trigger;
					var name = $obj.attr('name');
					$('input[type=radio][name="' + name + '"]:checked').attr("checked", false);
				}
			},
			items: {
				"cancel": {name: "取消选择", icon: function($element, key, item){ return 'context-menu-icon context-menu-icon-quit'; }}
			}
		});

		$('input').each(function() {
			if ($(this).attr('kind')=='DATE' || $(this).attr('kind')=='DATE_TIME') {
				$(this).attr('autocomplete', 'off');
			}
		});

		SetNewDate();

		// 初始化tip提示
		// 不能通过$("#visualForm").serialize()来获取所有的元素，因为radio或checkbox未被选中，则不会被包含
		$('#visualForm input, #visualForm select, #visualForm textarea').each(function() {
			if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && $(this).attr('type')!='hidden' && $(this).attr('type')!='file') {
				$(this).addClass('form-control');
			}

			var tip = '';
			if ($(this).attr('type') == 'radio') {
				tip = $(this).parent().attr('tip');
			}
			else {
				tip = $(this).attr('tip');
			}
			if (null!=tip && ""!=tip) {
				$(this).poshytip({
					content: function(){return tip;},
					className: 'tip-yellowsimple',
					alignTo: 'target',
					alignX: 'center',
					offsetY: 5,
					allowTipHover: true
				});
			}
		});
	});
</script>
</html>
