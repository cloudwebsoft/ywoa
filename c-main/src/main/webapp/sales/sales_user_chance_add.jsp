<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%
String myFormCode = "sales_chance";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="renderer" content="ie-stand">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加商机</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head> 
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ include file="sales_user_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<%
String priv="sales.user";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "add");
// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", myFormCode);

RequestAttributeElement rae = new RequestAttributeElement();
RequestAttributeMgr ram = new RequestAttributeMgr();
ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/sales_user_chance_list.jsp"));
%>
<%@ include file="../visual_add.jsp"%>
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
</script>
</html>
