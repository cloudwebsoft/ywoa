<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="read";
	if (!privilege.isUserPrivValid(request,priv)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String code = ParamUtil.get(request, "code");
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	if (msd==null) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
		return;
	}

    String op = ParamUtil.get(request, "op");
    int parentId = ParamUtil.getInt(request, "parentId", -1);
	int id = ParamUtil.getInt(request, "id");
	String formCode = msd.getString("form_code");
	
	String viewEdit = "module_edit.jsp";
	if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
		viewEdit = msd.getString("url_edit");
		response.sendRedirect(request.getContextPath() + "/" + msd.getString("url_edit") + "?parentId=" + parentId + "&id=" + id + "&code=" + code + "&formCode=" + formCode);
		return;
	}
	
	if (formCode.equals("")) {
		out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
		return;
	}
	
	ModulePrivDb mpd = new ModulePrivDb(code);
	if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	// 置嵌套表需要用到的cwsId
	request.setAttribute("cwsId", "" + id);
	// 置嵌套表需要用到的页面类型
	request.setAttribute("pageType", "edit");
	// 置NestSheetCtl需要用到的formCode
	request.setAttribute("formCode", formCode);
	
	FormMgr fm = new FormMgr();
	FormDb fd = fm.getFormDb(formCode);
	
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
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
	else if (op.equals("saveformvalue")) {
		JSONObject json = new JSONObject();
		boolean re = false;
		try {
			re = fdm.update(application, request, msd);
		}
		catch (ErrMsgException e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage());
			out.print(json);
			// e.printStackTrace();
			return;
		}
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
	else if (op.equals("delAttach")) {
		JSONObject json = new JSONObject();
		int attachId = ParamUtil.getInt(request, "attachId");
		com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
		boolean re = att.del();
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
	
	int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

	request.setAttribute("code", code);
	request.setAttribute("id", id);
	request.setAttribute("parentId", parentId);
	request.setAttribute("skinPath", SkinMgr.getSkinPath(request));
    request.setAttribute("isShowNav", isShowNav);
    request.setAttribute("isHasAttachment", fd.isHasAttachment());

    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
    request.setAttribute("rend", rd.rend(msd));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>编辑内容</title>
	<meta name="renderer" content="ie-stand" />
	<link type="text/css" rel="stylesheet" href="${skinPath}/css.css" />
	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
	<link href="../flowstyle.css" rel="stylesheet" type="text/css" />
	<style>
		.att_box {
			margin-top:5px;
		}

		input[readonly]{
			background-color: #ddd;
		}

		#attDiv {
			margin-top: 10px;
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
	
	<script src="../inc/map.js"></script>
	<script src="../js/jquery.raty.min.js"></script>
	
	<script src="../inc/upload.js"></script>
	<script src="../inc/flow_dispose_js.jsp"></script>
	<script src="../inc/flow_js.jsp"></script>
	<script src="../inc/ajax_getpage.jsp"></script>
	
	<script src="../js/jquery.bgiframe.js"></script>
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<link type="text/css" rel="stylesheet" href="${skinPath}/jquery-ui/jquery-ui-1.10.4.min.css" />
	
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	
	<link href="../js/select2/select2.css" rel="stylesheet" />
	<script src="../js/select2/select2.js"></script>
	<script src="../js/select2/i18n/zh-CN.js"></script>
	
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	
	<script type="text/javascript" src="../js/appendGrid/jquery.appendGrid-1.5.1.js"></script>
	<link type="text/css" rel="stylesheet" href="../js/appendGrid/jquery.appendGrid-1.5.1.css" />
	
	<script src="../js/jquery.form.js"></script>
	<script type="text/javascript" src="../js/jquery.toaster.js"></script>
	
	<script src="../flow/form_js/form_js_${formCode}.jsp?pageType=edit&id=${id}"></script>
	<script>
		$(function() {
			SetNewDate();
		});

		// ajaxForm序列化提交数据之前的回调函数
		function onBeforeSerialize() {
			try {
				ctlOnBeforeSerialize();
			}
			catch (e) {}
		}

		function save() {
			try {
				ctlOnBeforeSerialize();
			}
			catch (e) {}

			var f_helper = new LiveValidation('cwsHelper');
			if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
				if (LiveValidation.liveErrMsg.length < 100)
					jAlert(LiveValidation.liveErrMsg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
				else
					jAlert("请检查表单中的内容填写是否正常！","提示");
				return;
			}

			$('#visualForm').submit();
		}

		$(function() {
			$('#btnOK').click(function() {
				save();
			});

			var options = {
				beforeSerialize:  onBeforeSerialize,
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
			var data = $.parseJSON($.trim(responseText));
			if (data.ret=="1") {
				jAlert(data.msg, "提示");
				refreshAttach();
				reloadTab("{$param.tabIdOpener}");
			}
			else {
				if (data.msg != null)
					data.msg = data.msg.replace(/\\r/ig, "<BR>");
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
						url: "module_edit.jsp",
						data: {
							op: "delAttach",
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
<form action="module_edit.jsp?op=saveformvalue&id=${id}&code=${code}&isShowNav=${isShowNav}&parentId=${parentId}" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
	<table style="margin-bottom:10px" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
		<tr>
			<td align="left"><table width="100%">
				<tr>
					<td>${rend}</td>
				</tr>
			</table></td>
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
				<input id="btnOK" type="button" class="btn" value=" 确定 " />
				<input id="cwsHelper" name="cwsHelper" value="1" type="hidden" />
			</td>
		</tr>
	</table>
</form>
</body>
<script>
	refreshAttach();
</script>
</html>
