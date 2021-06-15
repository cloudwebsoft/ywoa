<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");
	
    String flowId = ParamUtil.get(request,"flowId");
    //System.out.println("flowId:"+flowId);
%>

function removeWorkplanDiv(id, name){
  $("#wp_"+id).remove(); 
  var val = o(name).value;
  var tmp = "," + val;
  tmp = tmp.replace("," + id, "");
  o(name).value = tmp.substring(1);
}

function selWorkplan(id,author,title,deptName,userRealNames,endTime,days) {
	var tmp = "," + inputObj.value + ",";
	if (tmp.indexOf("," + id + ",")!=-1) {
		// alert("计划已被选择！");
		return false;
	}
      
	var content = "<div id='wp_"+id+"'>"+"<a href='workplan/workplan_show.jsp?id="+id+"' target='_blank'>"+userRealNames+"："+title+"，"+"期限"+endTime+"，剩余" + days + "天</a>"+"&nbsp;&nbsp;"+"<a style='color:red; font-size:14px; padding-left:5px; cursor:pointer' href='javascript:void(0)' onclick='removeWorkplanDiv("+id+","+inputObj.name+")'>×</a>"+"</div>";	

	document.getElementById(inputObj.name+'_realshow').innerHTML += content;
	if (inputObj.value=="")
		inputObj.value = id;
	else
		inputObj.value +=","+id;
	return true;
}

function openWinWorkPlanList(obj) {
	inputObj = obj
	openWinForFlowAccess("<%=request.getContextPath()%>/workplan/workplan_add.jsp?action=sel&flowId=<%=flowId%>", 800, 600);
}

// function addInputObjValue(v) {
//	 inputObj.value += v;
// }
