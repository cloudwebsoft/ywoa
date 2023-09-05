<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.security.SecurityUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：嵌套表格2中编辑行
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

String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode"); // 主模块编码
if ("".equals(formCode)) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码
String menuItem = ParamUtil.get(request, "menuItem");

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (isNestSheetCheckPrivilege && !mpd.canUserModify(privilege.getUser(request))) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

long id = ParamUtil.getLong(request, "id", -1);
if (id == -1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
	return;
}

boolean canUserData = mpd.canUserData(privilege.getUser(request));

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", String.valueOf(id));
// 置页面类型，20220130注意不能改为edit_relate，因为其它地方有关联，将来可优化
String pageType = "edit";
request.setAttribute("pageType", pageType);

// 这里是为了使嵌套表格2表单中又存在嵌套表格2宏控件时，在getNestSheet方法中，传递给当前编辑的表单中的嵌套表格2宏控件
// 同时也用于查询选择宏控件
request.setAttribute("formCode", formCodeRelated);

long actionId = ParamUtil.getLong(request, "actionId", -1);
request.setAttribute("actionId", String.valueOf(actionId));

// 用于com.redmoon.oa.visual.Render
request.setAttribute("pageKind", "nest_sheet_relate");

int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID，仅用于导航，如果导航不显示，则不用传递该参数，用例：module_show_realte.jsp编辑按钮

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
boolean isQuote = fdao.getCwsQuoteId() != ConstUtil.QUOTE_NONE;

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String moduleCode = ParamUtil.get(request, "moduleCode");
if ("".equals(moduleCode)) {
	moduleCode = formCodeRelated;
}

String relateFieldValue = String.valueOf(com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
com.redmoon.oa.visual.FormDAOMgr fdmParent = new com.redmoon.oa.visual.FormDAOMgr(formCode);
relateFieldValue = fdmParent.getRelateFieldValue(parentId, moduleCode);
if (relateFieldValue==null) {
	out.print(StrUtil.Alert_Back("请检查模块是否相关联！"));
	return;
}

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCode);

int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<!--原为ie-stand，但不支持360极速模式，window.opener会为null-->
	<%--<meta name="renderer" content="webkit">--%>
	<title>智能设计-编辑内容</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
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
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../inc/map.js"></script>
	<script src="../js/jquery.raty.min.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
	<script src="../inc/upload.js"></script>
	<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
	<script src="<%=request.getContextPath()%>/inc/flow_js.jsp?parentFormCode=<%=formCode%>"></script>
	<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.flexbox.js"></script>
	<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp?pageType=edit&moduleCode=<%=moduleCode%>&formCode=<%=formCode%>&parentId=<%=parentId%>&flowId=<%=flowId%>&actionId=<%=actionId%>&time=<%=Math.random()%>"></script>
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

	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<script>
		$(function () {
			SetNewDate();
		});

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

		function SubmitResult() {
			// 检查是否已选择意见
			if (getradio("resultValue") == null || getradio("resultValue") == "") {
				alert("您必须选择一项意见!");
				return false;
			}
			visualForm.op.value = 'finish';
			visualForm.submit();
		}

		// 控件完成上传后，调用Operate()
		function Operate() {
			// alert(redmoonoffice.ReturnMessage);
		}
	</script>
</head>
<body>
<form action="../flow/updateNestSheetRelated.do?parentId=<%=parentId%>&id=<%=id%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&actionId=<%=actionId %>&pageType=<%=pageType%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm" class="form-inline">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left">
		  <table width="100%">
			<tr>
			  <td>
				<%
				com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
				out.print(rd.rend(msd));
			  	%>
			  </td>
			</tr>
		  </table>
	  </td>
    </tr>
	<%if (fd.isHasAttachment()) {%>	
    <tr>
      <td align="left"><script>initUpload()</script>
	  </td>
    </tr>
    <tr>
      <td align="left">
      <%
		Iterator ir = fdao.getAttachments().iterator();
		while (ir.hasNext()) {
			com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next(); %>
          <table id="att<%=am.getId()%>" width="82%" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td width="5%" height="31" align="right"><img src="<%=Global.getRootPath()%>/images/attach.gif" /></td>
              <td>&nbsp; <a target="_blank" href="<%=Global.getRootPath()%>/visual/download.do?attachId=<%=am.getId()%>&visitKey=<%=SecurityUtil.makeVisitKey(am.getId())%>"><%=am.getName()%></a>
				  &nbsp;&nbsp;&nbsp;&nbsp;
				  [<a href="javascript:;" onclick="delAtt(<%=am.getId()%>)">删除</a>]<br />              </td>
            </tr>
          </table>
        <%}%>
        </td>
    </tr>
	<%}%>
	<tr>
		<td height="30" align="center">
			<input id="cws_id" name="cws_id" value="<%=relateFieldValue%>" type="hidden"/>
			<input name="id" value="<%=id%>" type="hidden"/>
			<input type="submit" class="btn btn-default" name="btnSubmit" value="确定"/>
			<input type="hidden" name="helper"/>
		</td>
	</tr>
</table>
</form>
</body>
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

	function delAtt(attId) {
		jConfirm('您确定要删除吗？','提示',function(r) {
			$.ajax({
				type: "post",
				url: "../flow/delAttachForNestSheetRelated.do",
				data: {
					parentId: <%=parentId%>,
					id: <%=id%>,
					formCode: "<%=formCode%>",
					formCodeRelated: "<%=formCodeRelated%>",
					attachId: attId
				},
				dataType: "html",
				beforeSend: function (XMLHttpRequest) {
					$("body").showLoading();
				},
				success: function (data, status) {
					data = $.parseJSON(data);
					jAlert(data.msg, "提示");
					if (data.ret == "1") {
						$('#att' + attId).remove();
					}
				},
				complete: function (XMLHttpRequest, status) {
					$("body").hideLoading();
				},
				error: function (XMLHttpRequest, textStatus) {
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		});
	}

	var lv_helper = new LiveValidation('helper');
	
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
			var re = LiveValidation.massValidate(lv_helper.formObj.fields);
			if (!re) {
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
		if (data.msg != null) {
			data.msg = data.msg.replace(/\\r/ig, "<BR>");
		}
        if(data.ret == "1") {
			jAlert(data.msg, "提示", function() {
				if (data.isVisual){
					doVisual(data.tds, data.token);
				} else {
					doFlow();
				}
			});
        }
        else {
			jAlert(data.msg, "提示");
		}
    }

    function doVisual(tds,token) {
		// 如果有父窗口，则自动刷新父窗口
        if (window.opener!=null) {
            window.opener.updateRow("<%=formCodeRelated%>", <%=fdao.getId()%>, tds, token);
			window.close();
        }
    }

    function doFlow(){
        // 如果有父窗口，则自动刷新父窗口
        if (window.opener!=null) {
			// 带分页重新加载
			// console.log('doFlow');
			window.opener.reloadNestSheetCtl<%=moduleCode%>();
            window.close();
        }
    }

	function setNotReadOnly() {
    	var isQuote = <%=isQuote%>;
		var obj = o("visualForm");
		for (var i = 0; i < obj.elements.length; i++) {
			var el = obj.elements[i];
			var $el = $(el);

			if ($el.attr('readonly')!=null) {
				// 是否启用只读
				var isUseReadOnly = true;
				var readOnlyType = $el.attr('readOnlyType');
				if (isQuote) {
					if (readOnlyType === '2') {
						isUseReadOnly = false;
					}
				}
				else {
					// 注意js中存在隐式转换，0=='' 为true
					if (readOnlyType === '0') {
						isUseReadOnly = false;
					}
				}

				if (!isUseReadOnly) {
					$el.removeAttr('readonly');
					console.log($el.attr('name') + ' ' + $el.attr('title') + ' ' + obj.elements[i].tagName);
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
	}

	$(function() {
		// 将仅编辑时只读的字段，变为可写
		setNotReadOnly();

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
			if ($(this).attr('kind')=='DATE' || $(this).attr('kind')=='DATE_TIME') {
				$(this).attr('autocomplete', 'off');
			}
		});

		var canUserData = <%=canUserData%>;

		// 初始化tip提示及如果拥有数据权限，则去除只读
		// 不能通过$("#visualForm").serialize()来获取所有的元素，因为radio或checkbox未被选中，则不会被包含
		$('#visualForm input, #visualForm select, #visualForm textarea').each(function() {
			// 如果不是富文本编辑宏控件，如果富文本编辑宏控件加上了form-control，则会因为生成ueditor时，外面包裹的div也带上了form-control，致富文本编辑器位置变成了浮于表单上
			if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && $(this).attr('type')!='hidden' && $(this).attr('type')!='file') {
				$(this).addClass('form-control');
			}

			if (canUserData) {
				$(this).removeAttr('readonly');
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
