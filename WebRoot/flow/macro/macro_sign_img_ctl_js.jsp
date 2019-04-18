<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%
Privilege pvg = new Privilege();
int flowId = ParamUtil.getInt(request, "flowId", -1);
String fieldName = ParamUtil.get(request, "fieldName");
String rootpath = request.getContextPath();

// System.out.println(getClass() + " fieldName=" + fieldName);
%>
var curStampId;
function openWinSignImg(obj) {
	curStampId = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/flow_ntko_stamp_choose.jsp?action=sign", 400, 160);
}


function insertSignImg(stampId,url) {
	$("#"+curStampId).val(stampId);
	var html = "<img class='span_" + curStampId + "' name ='span_" + curStampId + "' id='span_" + curStampId + "' src='<%=request.getContextPath()%>/img_show.jsp?path=" + encodeURI(url) + "' onclick='openWinSignImg("+curStampId+")'/>";
	o("span_" + curStampId).innerHTML = html;
    $("#" + curStampId).attr("type","hidden");
    $(".LV_validation_message").hide();
    $(".LV_presence").hide();
}
