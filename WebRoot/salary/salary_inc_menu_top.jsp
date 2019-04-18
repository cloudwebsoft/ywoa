<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%
String formCodeTop = ParamUtil.get(request, "formCode");
ModuleSetupDb msdTop = new ModuleSetupDb();
msdTop = msdTop.getModuleSetupDbOrInit(formCodeTop);
// String pageTypeTop = StrUtil.getNullStr((String)request.getAttribute("pageType"));

int parentIdTop = ParamUtil.getInt(request, "parentId", -1);
%>
<div class="tabs1Box">
<div id="tabs1">
  <ul>
    <li id="menu1">
	<%
    String moduleUrlList = StrUtil.getNullStr(msdTop.getString("url_list"));
    if (moduleUrlList.equals("")) {
      moduleUrlList = request.getContextPath() + "/" + "salary/salary_list.jsp?formCode=" + StrUtil.UrlEncode(formCodeTop);
    }
    else {
      moduleUrlList = request.getContextPath() + "/" + moduleUrlList;
    }
    %>
    <a href="<%=moduleUrlList%>"><span><%=msdTop.getString("name")%>列表</span></a>
    </li>
<%
// 当页面为edit、show、list即module_edit.jsp module_show.jsp module_list.jsp时不需要处理关联模块时
//if (!(pageTypeTop.equals("edit") || pageTypeTop.equals("show") || pageTypeTop.equals("list"))) {
if (parentIdTop==-1) {
	com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
	ModulePrivDb mpdTop = new ModulePrivDb(formCodeTop);
	if (mpdTop.canUserAppend(pvgTop.getUser(request))) {
	%>	
	<li id="menu2"><a href="<%=request.getContextPath()%>/salary/salary_add.jsp?formCode=<%=formCodeTop%>"><span>添加</span></a></li>
	<%}%>	
	<li id="menu3"><a href="<%=request.getContextPath()%>/salary/salary_senior_search.jsp?formCode=<%=formCodeTop%>"><span>高级查询</span></a></li>
	<%
}
int menuItemTop = 4;
String[] tagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_name")), ",");
String[] tagUrlsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_url")), ",");
int lenTop = 0;
if (tagsTop!=null)
	lenTop = tagsTop.length;
for (int i=0; i<lenTop; i++) {
	String url = tagUrlsTop[i];
	if (!url.startsWith("http")) {
		url = request.getContextPath() + "/" + url;
	}
%>
    <li id="menu<%=menuItemTop%>"><a href="<%=url%>?formCode=<%=formCodeTop%>"><span><%=tagsTop[i]%></span></a></li>
<%	
	menuItemTop++;
}
%>
<%
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
		<li id="menu<%=menuItemTop%>"><a href="<%=request.getContextPath()%>/visual/module_list_relate.jsp?parentId=<%=parentIdTop%>&menuItem=<%=menuItemTop%>&formCodeRelated=<%=mrdTop.getString("relate_code")%>&formCode=<%=formCodeTop%>"><span><%=fdTop.getFormDb(mrdTop.getString("relate_code")).getName()%></span></a></li>
	<%
		menuItemTop++;
	}
	%>
	<%
	// 其它标签
	String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_name")), ",");
	String[] subTagUrlsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_url")), ",");
	int subLenTop = 0;
	if (subTagsTop!=null)
		subLenTop = subTagsTop.length;
	for (int i=0; i<subLenTop; i++) {
	%>
		<li id="menu<%=menuItemTop%>"><a href="<%=ModuleUtil.filterViewEditTagUrl(request, subTagUrlsTop[i])%>&menuItem=<%=menuItemTop%>"><span><%=subTagsTop[i]%></span></a></li>
	<%	
		menuItemTop++;
	}
}
%>
  </ul>
</div>
</div>