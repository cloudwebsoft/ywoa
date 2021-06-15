<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%
String operation = ParamUtil.get(request, "operation");

String formCode = ParamUtil.get(request, "formCode");
// formCode = "contract";
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormMgr fm = new FormMgr();
FormDb fdAdd = fm.getFormDb(formCode);

// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "add");


%>
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../inc/map.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../inc/flow_dispose_js.jsp"></script>
<script src="../inc/flow_js.jsp"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>

<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery.bgiframe.js"></script>

<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

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

function visualForm_onsubmit() {
	document.getElementById("btn_submit").disabled=true;
}
</script>
<%
if (fdAdd==null || !fdAdd.isLoaded()) {
	out.println(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}
if (operation.equals("saveformvalue")) {
	boolean re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdAdd);
	try {
		
		re = fdm.create(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(com.cloudwebsoft.framework.security.AntiXSS.clean(e.getMessage()),"提示"));
		e.printStackTrace();
	}
	if (re) {
		String listViewPage = ParamUtil.get(request, RequestAttributeElement.NAME_FORWARD);
		if (listViewPage.equals("")) {
			com.redmoon.oa.visual.Config cfg = new com.redmoon.oa.visual.Config();
			listViewPage = cfg.getView(formCode, "list");
		}
		// System.out.println("visual_add.jsp formCode=" + formCode + " " + listViewPage);
		out.print(StrUtil.jAlert_Redirect("保存成功！","提示", Global.getRootPath() + "/"+ listViewPage));
	}
	return;
}
RequestAttributeMgr ramVisualAdd = new RequestAttributeMgr();
%>
<form action="?operation=saveformvalue&formCode=<%=StrUtil.UrlEncode(formCode)%>&<%=ramVisualAdd.renderURL(request)%>" method="post" enctype="multipart/form-data" onsubmit="return visualForm_onsubmit()" name="visualForm" id="visualForm">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left">
	  	  <%
	  		com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fdAdd);
			out.print(rd.rendForAdd());
		  %><br />
		<%if (fdAdd.isHasAttachment()) {%>
		<script>initUpload()</script>
        <%}%>
        </td>
    </tr>
    <tr>
      <td height="30" align="center">
      <input type="submit" name="Submit" id="btn_submit" value=" 添 加 " class="btn" />
      &nbsp;&nbsp;
      <input type="button" id="btn_submit" value=" 返 回 " class="btn" onclick="window.history.back()" />
      <br />
      <br />
      </td>
    </tr>
</table>
<span id="spanTempCwsIds"></span>
</form>
