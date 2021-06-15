<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudweb.oa.utils.I18nUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="read";
	if (!privilege.isUserPrivValid(request,priv)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String code = ParamUtil.get(request, "moduleCode");
	if ("".equals(code)) {
		code = ParamUtil.get(request, "code");
		if ("".equals(code)) {
			code = ParamUtil.get(request, "formCode");
		}
	}
	if ("".equals(code)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
		return;
	}

	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	if (msd==null) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
		return;
	}

	if (!msd.isEditPageTabStyleHor()) {
		request.getRequestDispatcher("module_edit_v.jsp").forward(request, response);
		return;
	}

    String op = ParamUtil.get(request, "op");
    int parentId = ParamUtil.getInt(request, "parentId", -1);
	int id = ParamUtil.getInt(request, "id", -1);
	String formCode = msd.getString("form_code");
	
	String viewEdit = "module_edit.jsp";
	if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
		viewEdit = msd.getString("url_edit");
		response.sendRedirect(request.getContextPath() + "/" + msd.getString("url_edit") + "?parentId=" + parentId + "&id=" + id + "&code=" + code + "&formCode=" + formCode);
		return;
	}
	
	if ("".equals(formCode)) {
		out.print(SkinUtil.makeErrMsg(request, "表单编码不能为空！"));
		return;
	}
	
	ModulePrivDb mpd = new ModulePrivDb(code);
	if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	if (!ModulePrivMgr.canAccessData(request, msd, id)) {
		I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, i18nUtil.get("info_access_data_fail")));
		return;
	}

	// 置嵌套表需要用到的cwsId
	request.setAttribute("cwsId", "" + id);
	// 置嵌套表需要用到的页面类型
	request.setAttribute("pageType", ConstUtil.PAGE_TYPE_EDIT);
	// 置NestSheetCtl需要用到的formCode
	request.setAttribute("formCode", formCode);
	
	FormMgr fm = new FormMgr();
	FormDb fd = fm.getFormDb(formCode);
	
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
	if (!fdao.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, "记录不存在"));
		return;
	}

	if (op.equals("refreshAttach")) {
		Vector vAttach = fdao.getAttachments();
		request.setAttribute("vAttach", vAttach);
		request.setAttribute("canUserLog", mpd.canUserLog(privilege.getUser(request)));
		if (vAttach.size()>0) {
	%>
	<div id="attDiv">
		<table id="attTable" class="tabStyle_1 percent98" width="98%" border="0" align="center"
			   cellpadding="0" cellspacing="0">
			<tr>
				<td height="31" align="right" class="tabStyle_1_title">&nbsp;</td>
				<td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="fileName"/></td>
				<td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="creator"/></td>
				<td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="time"/></td>
				<td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="size"/></td>
				<td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="operate"/></td>
			</tr>
			<c:forEach items="${vAttach}" var="att" >
			<tr id="trAtt${att.id}%>">
				<td width="2%" height="31" align="center"><img src="../images/attach.gif"/></td>
				<td width="51%" align="left">
					&nbsp;
					<span id="spanAttLink${att.id}">
						<a href="../visual_getfile.jsp?attachId=${att.id}" target="_blank">
							<span id="spanAttName${att.id}">${att.name}</span>
						</a>
					</span>
				</td>
				<td width="10%" align="center">
						${att.creatorRealName}
				</td>
				<td width="15%" align="center">
					<fmt:formatDate value='${att.createDate}' pattern='yyyy-MM-dd HH:mm' />
				</td>
				<td width="11%" align="center">${att.fileSizeMb}M
				</td>
				<td width="11%" align="center">
					<a href="../visual_getfile.jsp?attachId=${att.id}" target="_blank">
						<lt:Label res="res.flow.Flow" key="download"/>
					</a>
					&nbsp;&nbsp;
					<a href="javascript:;" onClick="delAtt(${att.id})" style="cursor:pointer">删除</a>
					<c:if test="${canUserLog}">
						&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('${att.name} 日志', '${pageContext.request.contextPath}/visual/att_log_list.jsp?attId=${att.id}')">日志</a>
					</c:if>
					<c:if test="${att.previewUrl!=''}">
						&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('${att.name}', '${pageContext.request.contextPath}/${att.previewUrl}">预览</a>
					</c:if>
				</td>
			</tr>
			</c:forEach>
		</table>
	</div>
	<%
		}
		return;
	}
	
	int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

	request.setAttribute("code", code);
	request.setAttribute("id", id);
	request.setAttribute("parentId", parentId);
	request.setAttribute("skinPath", SkinMgr.getSkinPath(request));
    request.setAttribute("isShowNav", isShowNav);
    request.setAttribute("isHasAttachment", fd.isHasAttachment());

	StringBuffer requestParamBuf = new StringBuffer();
	Enumeration reqParamNames = request.getParameterNames();
	while (reqParamNames.hasMoreElements()) {
		String paramName = (String) reqParamNames.nextElement();
		String[] paramValues = request.getParameterValues(paramName);
		if (paramValues.length == 1) {
			String paramValue = ParamUtil.getParam(request, paramName);
			// 过滤掉formCode等
			if ("id".equals(paramName)) {
				;
			}
			else {
				StrUtil.concat(requestParamBuf, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
			}
		}
	}
	request.setAttribute("requestParams", requestParamBuf.toString());

    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
    request.setAttribute("rend", rd.rend(msd));

	com.alibaba.fastjson.JSONArray buttons = msd.getButtons(request, ConstUtil.PAGE_TYPE_EDIT, fdao, isShowNav);
	request.setAttribute("buttons", buttons);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>模块修改</title>
	<link type="text/css" rel="stylesheet" href="${skinPath}/css.css" />
	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<link href="../flowstyle.css" rel="stylesheet" type="text/css" />
	<style>
		.att_box {
			margin-top:5px;
		}
		input,textarea,button {
			outline:none;
		}
		input[readonly]{
			background-color: #ddd;
		}
		select[readonly]{
			background-color: #ddd;
		}
		textarea[readonly]{
			background-color: #ddd;
		}
		#attDiv {
			margin-top: 10px;
		}

		select {
			line-height: 27px;
			height: 29px !important;
			border: 1px solid #d4d4d4;
		}
		<%=msd.getCss(ConstUtil.PAGE_TYPE_EDIT)%>
	</style>
	<script src="../inc/common.js"></script>
	<script src="../inc/map.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../js/MyPaging/MyPaging.css">
	<script src="../js/MyPaging/MyPaging.js"></script>
	<link rel="stylesheet" href="../js/poshytip/tip-yellowsimple/tip-yellowsimple.css" type="text/css" />
	<script type="text/javascript" src="../js/poshytip/jquery.poshytip.js"></script>

	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
	
	<script src="../js/jquery.raty.min.js"></script>
	
	<script src="../inc/flow_dispose_js.jsp"></script>
	<script src="../inc/flow_js.jsp"></script>
	<script src="../inc/ajax_getpage.jsp"></script>
	<script src="../inc/upload.js"></script>	
	<script src="../js/jquery.bgiframe.js"></script>
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<link type="text/css" rel="stylesheet" href="${skinPath}/jquery-ui/jquery-ui-1.10.4.css" />
	
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	
	<link href="../js/select2/select2.css" rel="stylesheet" />
	<script src="../js/select2/select2.js"></script>
	<script src="../js/select2/i18n/zh-CN.js"></script>
	
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<script src="../js/jquery.form.js"></script>	
	<script type="text/javascript" src="../js/appendGrid/jquery.appendGrid-1.5.1.js"></script>
	<link type="text/css" rel="stylesheet" href="../js/appendGrid/jquery.appendGrid-1.5.1.css" />
	<script type="text/javascript" src="../js/jquery.toaster.js"></script>
	<script src="../js/BootstrapMenu.min.js"></script>
	<script src="../flow/form_js/form_js_${formCode}.jsp?pageType=<%=ConstUtil.PAGE_TYPE_EDIT%>&id=${id}&time=<%=Math.random()%>"></script>
</head>
<body>
<c:if test="${isShowNav==1}">
	<%@ include file="module_inc_menu_top.jsp"%>
	<script>
		o("menu1").className="current";
	</script>
	<div class="spacerH"></div>
</c:if>
<%@ include file="../inc/tip_phrase.jsp"%>
<script>
	function save() {
		// 表单合法性校验，用于在form_js_***中扩展
		try {
			var re = checkModuleForm();
			if ( "" !== re ) {
				jAlert(re, '提示');
				return;
			}
		}
		catch (e) {}

		try {
			ctlOnBeforeSerialize();
		}
		catch (e) {}

		var f_helper = new LiveValidation('cwsHelper');
		if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
			if (LiveValidation.liveErrMsg.length < 100) {
				jAlert(LiveValidation.liveErrMsg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
			}
			else {
				jAlert("请检查表单中的内容填写是否正常！","提示");
			}
			return;
		}

		$('#visualForm').submit();
	}

	$(function() {
		SetNewDate();
		$('#btnOK').click(function(e) {
			e.preventDefault();
			save();
		});
	});

    // ajaxForm序列化提交数据之前的回调函数
    function onBeforeSerialize() {
        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }
    }

    $(function () {
        var options = {
            beforeSerialize: onBeforeSerialize,
            //target:        '#output2',   // target element(s) to be updated with server response
            beforeSubmit: preSubmit,  // pre-submit callback
            success: showResponse  // post-submit callback

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

	function showResponse(responseText, statusText, xhr, $form)  {
		$('#visualForm').hideLoading();
		var data = responseText;
		if (!isJson(data)) {
			data = $.parseJSON($.trim(responseText));
		}
		if (data.ret=="1") {
			try {
				onModuleEdit<%=code%>(<%=id%>, "${param.tabIdOpener}");
			}
			catch (e) {}

			jAlert(data.msg, "提示", function () {
				<%
				if (msd.isReloadAfterUpdate()) {
				%>
				window.location.reload(); // 文件宏控件需要刷新
				<%
				}
				%>
			});
			// refreshAttach();
			reloadTab("${param.tabIdOpener}");
		}
		else {
			if (data.msg != null) {
				data.msg = data.msg.replace(/\\r/ig, "<BR>");
			}
			jAlert(data.msg, "提示");
		}
	}

	function refreshAttach() {
		<c:if test="${isHasAttachment}">
		$.ajax({
			type: "post",
			url: "module_edit.jsp",
			data: {
				op: "refreshAttach",
				id: "${id}",
				code: "${code}"
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#visualForm').showLoading();
			},
			success: function(data, status){
				// 删除编辑时界面上添加的文件
				delAllUploadFile();
				$('#tdAtt').html(data);
			},
			complete: function(XMLHttpRequest, status){
				$('#visualForm').hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
		});
		</c:if>
	}

	function delAtt(attId, fieldName) {
		jConfirm('您确定要删除吗？','提示',function(r){
			if(!r){
				return;
			}else{
				$.ajax({
					type: "post",
					url: "delAttach.do",
					data: {
						id: "${id}",
						formCode: "${formCode}",
						attachId: attId,
						code: "${code}"
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#visualForm').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="1") {
							jAlert(data.msg, "提示");
							$('#trAtt' + attId).remove();
							if (fieldName!=null) {
								$('#helper_' + fieldName).remove();
							}
						}
						else {
							jAlert(data.msg, "提示");
						}
					},
					complete: function(XMLHttpRequest, status){
						$('#visualForm').hideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						alert(XMLHttpRequest.responseText);
					}
				});
			}
		});
	}
</script>
<form action="update.do?id=${id}&code=${code}&isShowNav=${isShowNav}&parentId=${parentId}" method="post" enctype="multipart/form-data" id="visualForm" name="visualForm">
	<table style="margin-bottom:10px" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
		<tr>
			<td align="left">
			${rend}
			</td>
		</tr>
		<tr>
			<td align="left" id="tdAtt">
			</td>
		</tr>
        <c:if test="${isHasAttachment}">
		<tr>
			<td align="left"><script>initUpload()</script>
			</td>
		</tr>
        </c:if>
		<tr>
			<td align="center" style="padding-top: 10px">
				<input name="id" value="${id}" type="hidden" />
				<c:if test="${fn:length(buttons)==0}">
				<button id="btnOK" class="btn btn-default">确定</button>
				</c:if>
                <%--&nbsp;&nbsp;
                <button id="btnClose" class="btn btn-default">关闭</button>--%>

				<c:forEach items="${buttons}" var="button">
					<c:choose>
						<c:when test="${button.event=='click'}">
							<button id="${button.id}" class="btn btn-default" title="${button.title}" onclick="${button.href}">${button.name}</button>
							&nbsp;&nbsp;&nbsp;&nbsp;
						</c:when>
						<c:otherwise>
							<c:if test="${button.target=='newTab'}">
								<button id="${button.id}" class="btn btn-default" title="${button.title}" onclick="addTab('${button.name}', '${button.href}')">${button.name}</button>
								&nbsp;&nbsp;&nbsp;&nbsp;
							</c:if>
							<c:if test="${button.target=='curTab'}">
								<button id="${button.id}" class="btn btn-default" title="${button.title}" onclick="window.location.href='${button.href}'">${button.name}</button>
								&nbsp;&nbsp;&nbsp;&nbsp;
							</c:if>
						</c:otherwise>
					</c:choose>
				</c:forEach>
				<input id="cwsHelper" name="cwsHelper" value="1" type="hidden" />
			</td>
		</tr>
	</table>
</form>
<br/>
<link rel="stylesheet" href="../js/jquery-contextmenu/jquery.contextMenu.min.css">
<script src="../js/jquery-contextmenu/jquery.contextMenu.js"></script>
<script src="../js/jquery-contextmenu/jquery.ui.position.min.js"></script>
<script src="../js/layui/layui.js" charset="utf-8"></script>
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

	refreshAttach();

	$(function() {
        $('#btnClose').click(function() {
            closeActiveTab();
        });

		$('input[type=radio]').each(function(i) {
			var name = $(this).attr("name");
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
			if ($(this).attr('kind')=='DATE') {
				$(this).attr('autocomplete', 'off');
			}
		});

		// 初始化tip提示
		// 不能通过$("#visualForm").serialize()来获取所有的元素，因为radio或checkbox未被选中，则不会被包含
		$('#visualForm input, #visualForm select, #visualForm textarea').each(function() {
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
</body>
</html>
