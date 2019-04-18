<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.util.*"%>
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
// formCode = "contract";
if (formCode.equals("")) {
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

String relateFieldValue = "" + com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID;
int parentId = ParamUtil.getInt(request, "parentId"); // 父模块的ID
if (parentId==-1) {
	// out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
	// return;
	
	ModuleRelateDb mrd = new ModuleRelateDb();
	mrd = mrd.getModuleRelateDb(formCode, formCodeRelated);
	if (mrd==null) {
		out.print(StrUtil.Alert_Back("请检查模块是否相关联！"));
		return;
	}
}
else {
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
	relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
	if (relateFieldValue==null) {
		out.print(StrUtil.Alert_Back("请检查模块是否相关联！"));
		return;
	}
}

ModuleSetupDb msd = new ModuleSetupDb();
// System.out.println(getClass() + " formCodeRelated=" + formCodeRelated);

String moduleCode = ParamUtil.get(request, "moduleCode");
if (moduleCode.equals(""))
	moduleCode = formCodeRelated;
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
// 用于com.redmoon.oa.visual.Render
request.setAttribute("pageKind", "nest_sheet_relate");
request.setAttribute("actionId", String.valueOf(actionId));
// 用于com.redmoon.oa.flow.macroctl.NestSheetCtl
request.setAttribute("pageType", "add_relate");

// 用于区分嵌套表是在流程还是智能模块
boolean isVisual = false;
if (op.equals("saveformvalue")) {
    JSONObject json = new JSONObject();
	boolean re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (formCode.equals("project") && formCodeRelated.equals("project_members")) {
			re = fdm.createPrjMember(application, request);
		} else {	
			re = fdm.create(application, request, msd);
		}
	}
	catch (ErrMsgException e) {
		//out.print(StrUtil.Alert_Back(e.getMessage()));
		json.put("ret","0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	if (re) {
		String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String[] fields = StrUtil.split(listField, ",");
		int len = 0;
		if (fields!=null)
			len = fields.length;
		String tds = "";
		String token = "#@#";
		// int cwsId = ParamUtil.getInt(request, "cws_id", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
		int cwsId = StrUtil.toInt(fdm.getFieldValue("cws_id"), com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
		// System.out.println(getClass() + " cwsId=" + cwsId);
		// 在智能模块中添加操作时，添加嵌套表格2中的记录
		if (cwsId==com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID) {
			com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO();
			RequestUtil.setFormDAO(request, fdao);
			for (int i=0; i<len; i++) {
				String fieldName = fields[i];
				String v = StrUtil.getNullStr(fdao.getFieldHtml(request, fieldName)); // fdao.getFieldValue(fieldName);
				if (i==0)
					tds = v;
				else
					tds += token + v;
			}
			// System.out.println(getClass() + " tds=" + tds);
			isVisual = true;
		}
		else {
			com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO();
			RequestUtil.setFormDAO(request, fdao);			
			for (int i=0; i<len; i++) {
				String fieldName = fields[i];
				String v = StrUtil.getNullStr(fdao.getFieldHtml(request, fieldName)); // fdao.getFieldValue(fieldName);
				if (i==0)
					tds = v;
				else
					tds += token + v;
			}
		}
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("isVisual",isVisual);
		json.put("token",token);
		json.put("tds",tds);
		json.put("fdaoId",fdm.getVisualObjId());
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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>智能模块设计-添加内容</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" />
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../inc/map.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>
<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.flexbox.js"></script>

<link href="../js/select2/select2.css" rel="stylesheet" />
<script src="../js/select2/select2.js"></script>
<script src="../js/select2/i18n/zh-CN.js"></script>

<script src="../js/jquery.form.js"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script>
$(function() {
	SetNewDate();
});

function setradio(myitem,v)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio")
              {
                 if (radioboxs[i].value==v)
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
if (isShowNav==1) {
%>
<%@ include file="module_inc_menu_top.jsp"%>
<%}%>
<div class="spacerH"></div>
<form action="nest_sheet_add_relate.jsp?op=saveformvalue&menuItem=<%=menuItem%>&actionId=<%=actionId %>&parentId=<%=parentId%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=<%=isShowNav%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left">
      <%
      	com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
	  	out.print(rd.rendForAdd(msd));
	  %><br />
	<%if (fd.isHasAttachment()) {%>
		<script>initUpload()</script>
	<%}%>
		</td>
    </tr>
    <tr>
      <td height="30" align="center"><input class="btn" type="submit" name="Submit" value="确定" />
      <input name="cws_id" value="<%=relateFieldValue%>" type="hidden" />
      <%
      int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
      if (parentId!=-1 && flowId!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {%>
      <input name="cwsStatus" value="<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>" type="hidden" />
      <%}%>
      <input name="flowId" value="<%=flowId%>" type="hidden" />
	  </td>
    </tr>
</table>
<span id="spanTempCwsIds"></span>
</form>
</body>
<script>
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
    if(data.ret === "1"){
        if (data.isVisual){
            doVisual(data.fdaoId,data.tds,data.token);
        } else {
            doFlow(data.fdaoId,data.tds,data.token);
        }
    }
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
function doFlow(fdaoId,tds,token){
    // 如果有父窗口，则自动刷新父窗口
    if (window.opener!=null) {
        // 不能刷新，因为在insertRow还将调用onNestInsertRow事件
        // window.parent.refreshNestSheetCtl<%=moduleCode%>();
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
</script>
</html>
