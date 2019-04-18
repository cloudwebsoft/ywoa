<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import="com.redmoon.oa.visual.ModuleRelateDb"%>
<%@ page import="com.redmoon.oa.visual.ModuleUtil"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%
long projectIdTop = ParamUtil.getLong(request, "projectId", -1);
if (projectIdTop==-1) {
	Long prjId = (Long)request.getAttribute("projectId");
	if (prjId!=null) {
		projectIdTop = prjId.longValue();
	}
}
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/project/project_show.jsp?parentId=<%=projectIdTop%>&formCode=project&projectId=<%=projectIdTop%>"><span>项目</span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/project/project_workplan_list.jsp?projectId=<%=projectIdTop%>"><span>计划</span></a></li>
    <!--<li id="menu3"><a href="<%=request.getContextPath()%>/project_task_list.jsp?projectId=<%=projectIdTop%>"><span>任务</span></a></li>-->
    <li id="menu4"><a href="<%=request.getContextPath()%>/project/project_flow_list.jsp?projectId=<%=projectIdTop%>"><span>流程</span></a></li>
    <!--<li id="menu5"><a href="<%=request.getContextPath()%>/project/project_doc_list.jsp?projectId=<%=projectIdTop%>"><span>文档</span></a></li>-->
    <li id="menu5"><a href="<%=request.getContextPath()%>/fileark/document_list_m.jsp?projectId=<%=projectIdTop%>"><span>文档</span></a></li>
    <!-- <li id="menu6"><a href="<%=request.getContextPath()%>/jump.jsp?fromWhere=oa&toWhere=forum&action=board&boardcode=<%="cws_prj_" + projectIdTop%>" target="_blank"><span>交流</span></a></li> -->
    
<%
int menuItemTop = 7;

long parentIdTop = projectIdTop;
if (parentIdTop==-1) {
	parentIdTop = ParamUtil.getInt(request, "parentId", -1);
	if (parentIdTop!=-1)
		projectIdTop = parentIdTop;
}
String formCodeTop = "project";
ModuleSetupDb msdTop = new ModuleSetupDb();
msdTop = msdTop.getModuleSetupDbOrInit(formCodeTop);

// 当需要处理关联模块时
// if (pageTypeTop.equals("edit") || pageTypeTop.equals("show") || pageTypeTop.equals("list")) {
if (parentIdTop!=-1) {
	// 关联模块标签	
	FormDb fdTop = new FormDb();
			
	ModuleRelateDb mrdTop = new ModuleRelateDb();
	java.util.Iterator irTop = mrdTop.getModulesRelated(formCodeTop).iterator();
	while (irTop.hasNext()) {
		mrdTop = (ModuleRelateDb)irTop.next();
	%>
		<li id="menu<%=menuItemTop%>"><a href="<%=request.getContextPath()%>/project/module_list_relate.jsp?projectId=<%=projectIdTop%>&parentId=<%=parentIdTop%>&menuItem=<%=menuItemTop%>&formCodeRelated=<%=mrdTop.getString("relate_code")%>&formCode=<%=formCodeTop%>"><span><%=fdTop.getFormDb(mrdTop.getString("relate_code")).getName()%></span></a></li>
	<%
		menuItemTop++;
	}
	%>
	<%
	// 其它标签
	String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_name")), "\\|");
	String[] subTagUrlsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_url")), "\\|");
	int subLenTop = 0;
	if (subTagsTop!=null)
		subLenTop = subTagsTop.length;
	for (int i=0; i<subLenTop; i++) {
	%>
		<li id="menu<%=menuItemTop%>" tagName="<%=subTagsTop[i]%>"><a href="<%=ModuleUtil.filterViewEditTagUrl(request, formCodeTop, subTagsTop[i])%>&projectId=<%=projectIdTop%>&menuItem=<%=menuItemTop%>"><span><%=subTagsTop[i]%></span></a></li>
	<%	
		menuItemTop++;
	}
}
%>    
  </ul>
</div>
