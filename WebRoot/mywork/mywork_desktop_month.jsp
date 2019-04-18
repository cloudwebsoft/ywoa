<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);
int count = udsd.getCount();

String userName = privilege.getUser(request);
String sql = "select id from work_log where userName=" + StrUtil.sqlstr(userName) + " and log_type=" + WorkLogDb.TYPE_MONTH + " order by myDate desc";
WorkLogDb wld = new WorkLogDb();
ListResult lr = wld.listResult(sql, 1, count);
Iterator ir = lr.getResult().iterator();
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor" >
    <div id="drag_<%=id%>_h" class="box">
		<!-- <span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="mywork/mywork.jsp"><%=udsd.getTitle()%></a></span> -->
   		<!-- <div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div> -->
    	<!-- <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div> -->
    	<!-- <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div> -->
    	<div class="titleimg">
        <!--<img src="images/desktop/mywork.month.png" width="40" height="40" />-->
        <i class="fa <%=udsd.getIcon()%>"></i>
        &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/ymoa/queryMyMonthWork.action?logType=2"><%=udsd.getTitle()%></a></div>
    </div>
    <div id="drag_<%=udsd.getId()%>_c" class="article">
      <% 
    	if(ir.hasNext()){%>
        <table class='article_table'>
  	  <%
  	 	while (ir.hasNext()) {
         wld = (WorkLogDb)ir.next();%>	
  	 	  <tr>
          <td class='article_content'><a href="<%=request.getContextPath()%>/ymoa/showWorkLogById.action?workLogId=<%=wld.getId()%>" title="<%=StrUtil.HtmlEncode(wld.getContent())%>"><%=StrUtil.getAbstract(request, wld.getContent(), udsd.getWordCount(), "，")%></a></td>
          <td class='article_time' style="width:135px">[<%=wld.getLogItem()%>月&nbsp;<%=DateUtil.format(wld.getMyDate(), "yyyy-MM-dd")%>]</td>
          </tr>
  	 	<%}%>
    	</table>	
    	<%}else{%>
    		<div class='no_content'><img title='暂无工作月报' src='images/desktop/no_content.jpg'></div>
    	<%}
    %>
    </div>
</div>