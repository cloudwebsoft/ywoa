<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@page import="cn.js.fan.db.Paginator"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="cn.js.fan.db.ListResult"%>
<%@page import="com.redmoon.oa.flow.MyActionDb"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="cn.js.fan.util.DateUtil"%>
<%@page import="cn.js.fan.web.Global"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    
    <title>超期流程列表</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script type="text/javascript" src="../inc/common.js"></script>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
  </head>
  
  <body>
  <%@ include file="admin_dept_user_inc_menu_top.jsp"%>
<script>
o("menu8").className="current";
</script>
<div class="spacerH"></div>
    <%	
	    String deptCode = ParamUtil.get(request,"deptCode");
    	String sql = "select fa.id from flow_my_action fa, flow f, users u, dept_user du where f.id = fa.flow_id and u.name = fa.user_name and u.name = du.USER_NAME and du.DEPT_CODE = "+StrUtil.sqlstr(deptCode)+" and fa.check_date > fa.expire_date order by du.orders";
    	MyActionDb wa = new MyActionDb();
    	Vector v = wa.list(sql);
    	WorkflowDb wf = null;
    %>
  <table id="mainTable" class="tabStyle_1 percent98" width="99%"  border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" width="13%" height="24" align="center">姓名</td>
      <td class="tabStyle_1_title" width="14%" align="center">流程标题</td>
      <td class="tabStyle_1_title" width="14%" align="center">到期时间</td>
      <td class="tabStyle_1_title" width="14%" align="center">处理时间</td>
      <td class="tabStyle_1_title" width="14%" align="center">延迟时间（小时）</td>
      <td class="tabStyle_1_title" width="14%" align="center">操作</td>
    </tr>
    <%
    	wa = new MyActionDb();
    	String sqlflow = "select f.id from  flow f where f.id = ?";
    	if(v!=null){
    		Iterator ir = v.iterator();
    		while(ir.hasNext()){
    			wa = (MyActionDb)ir.next();
    			long flowId = wa.getFlowId();
    			wf = new WorkflowDb();
    			wf = wf.getWorkflowDb((int)flowId);
    			
    			String flowTitle = wf.getTitle();//流程标题
    			String userName = wa.getUserName();//超期人员姓名
    			UserDb ud = new UserDb();
    			ud = ud.getUserDb(userName);
    			String realName = ud.getRealName();
    			Date expireDate = wa.getExpireDate();
    			Date checkDate = wa.getCheckDate();
    			int hour = DateUtil.datediffHour(checkDate,expireDate);
    %>
    <tr>
    	<td><%=StrUtil.getNullStr(realName)%></td>
    	<td><%=StrUtil.getNullStr(flowTitle)%></td>
    	<td><%=StrUtil.getNullStr(DateUtil.format(expireDate,"yyyy-MM-dd hh:mm:ss"))%></td>
    	<td><%=StrUtil.getNullStr(DateUtil.format(checkDate,"yyyy-MM-dd hh:mm:ss"))%></td>
    	<td><%=StrUtil.getNullStr(hour+"")%></td>
    	<td><a href="javascript:;" onclick="addTab('<%=flowTitle%>','<%=Global.getFullRootPath(request) %>/flow_modify.jsp?flowId=<%=flowId%>')">查看</a></td>
    </tr><%
    		}
    	}
    	
    %>
   </table>
  </body>
  <script>
  $(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
  });
  </script>
</html>
