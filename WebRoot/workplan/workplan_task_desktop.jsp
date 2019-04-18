<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
if (!privilege.isUserLogin(request))r {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);
int count = udsd.getCount();

String userName = privilege.getUser(request);
String sql = "select t.id from work_plan_task t, work_plan_task_user u where t.id=u.task_id and u.user_name=" + StrUtil.sqlstr(userName) + " order by start_date desc";
WorkPlanTaskDb wptd = new WorkPlanTaskDb();
ListResult lr = wptd.listResult(sql, 1, count);
Iterator ir = lr.getResult().iterator();
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor" >
    <div id="drag_<%=id%>_h" class="box">
		<!-- <span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="workplan/workplan_task_list.jsp"><%=udsd.getTitle()%></a></span> -->
        <!-- <div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div> -->
		<!-- <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div> -->
		<!-- <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div> -->
   		<div class="titleimg">
        <!--<img src="images/desktop/workplan.task.png" width="40" height="40" />-->
        <i class="fa <%=udsd.getIcon()%>"></i>
        &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<a href="workplan/workplan_task_list.jsp"><%=udsd.getTitle()%></a></div>
    </div>
    <div id="drag_<%=udsd.getId()%>_c" class="article">
      <% 
    	if(ir.hasNext()){%>
        <table class='article_table'>
  	  <%
  	 	while (ir.hasNext()) {
  	 		wptd = (WorkPlanTaskDb)ir.next();%>	
  	 	  <tr>
          <td class='article_content'>
          	<a href="workplan/workplan_task.jsp?id=<%=wptd.getLong("work_plan_id")%>&userName=<%=StrUtil.UrlEncode(userName)%>" title="<%=StrUtil.HtmlEncode(wptd.getString("name"))%>"><%=StrUtil.getAbstract(request, wptd.getString("name"), udsd.getWordCount(), "，")%></a>
          </td>
          <td class='article_time'><%=DateUtil.format(wptd.getDate("start_date"), "yyyy-MM-dd")%></td>
        </tr>
  	 	<%}%>
    	</table>	
    	<%}else{%>
    		<div class='no_content'><img title='暂无我的计划项' src='images/desktop/no_content.jpg'></div>
    	<%}
    %>
      
    </div>
</div>