<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<script src="../inc/nav.js"></script>
<%
String action_inc = cn.js.fan.util.ParamUtil.get(request, "action");
String userNameTop = ParamUtil.get(request, "userName");
String custom_inc_action = "";
if (action_inc.equals("")) {
	custom_inc_action = "我的";
}

long idTop = ParamUtil.getLong(request, "customerId", -1);
if (idTop==-1)
	idTop = ParamUtil.getLong(request, "id");

com.redmoon.oa.flow.FormMgr fmTop = new com.redmoon.oa.flow.FormMgr();
com.redmoon.oa.flow.FormDb fdTop = fmTop.getFormDb("sales_customer");
com.redmoon.oa.visual.FormDAOMgr fdmTop = new com.redmoon.oa.visual.FormDAOMgr(fdTop);
com.redmoon.oa.visual.FormDAO fdaoTop = fdmTop.getFormDAO(idTop);
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/sales/customer_show.jsp?id=<%=idTop%>&formCode=sales_customer"><span><%=fdaoTop.getFieldValue("customer")%></span></a></li>
	<li id="menu2"><a href="<%=request.getContextPath()%>/sales/linkman_list.jsp?op=listOfCustomer&customerId=<%=idTop%>"><span>联系人</span></a></li>
	<li id="menu3"><a href="<%=request.getContextPath()%>/sales/customer_visit_list.jsp?customerId=<%=idTop%>"><span>行动</span></a></li>
	<li id="menu4"><a href="<%=request.getContextPath()%>/sales/customer_sales_chance_list.jsp?customerId=<%=idTop%>&parentId=<%=idTop%>&formCodeRelated=sales_chance&formCode=sales_customer&menuItem=4"><span>商机</span></a></li>
	<li id="menu5"><a href="<%=request.getContextPath()%>/sales/customer_sales_order_list.jsp?customerId=<%=idTop%>&parentId=<%=idTop%>&formCodeRelated=sales_order&formCode=sales_customer&menuItem=5"><span>订单</span></a></li>
	<li id="menu6"><a href="<%=request.getContextPath()%>/sales/customer_service_list.jsp?customerId=<%=idTop%>&parentId=<%=idTop%>&formCodeRelated=sales_service&formCode=sales_customer&menuItem=6"><span>服务</span></a></li>
    <li id="menu7"><a href="<%=request.getContextPath()%>/sales/customer_contract_list.jsp?customerId=<%=idTop%>&parentId=<%=idTop%>&formCodeRelated=sales_service&formCode=sales_customer&menuItem=7"><span>合同</span></a></li>
<%
int menuItemTop = 8;

long parentIdTop = idTop;
if (parentIdTop==-1) {
	parentIdTop = ParamUtil.getInt(request, "parentId", -1);
	if (parentIdTop!=-1)
		idTop = parentIdTop;
}
String formCodeTop = "sales_customer";
ModuleSetupDb msdTop = new ModuleSetupDb();
msdTop = msdTop.getModuleSetupDbOrInit(formCodeTop);

// 当需要处理关联模块时
// if (pageTypeTop.equals("edit") || pageTypeTop.equals("show") || pageTypeTop.equals("list")) {
if (parentIdTop!=-1) {
	// 关联模块标签	
	FormDb fdTopRelate = new FormDb();
			
	ModuleRelateDb mrdTop = new ModuleRelateDb();
	java.util.Iterator irTop = mrdTop.getModulesRelated(formCodeTop).iterator();
	while (irTop.hasNext()) {
		mrdTop = (ModuleRelateDb)irTop.next();
		
		String relateCodeTop = mrdTop.getString("relate_code");
		if (relateCodeTop.equals("day_lxr") || relateCodeTop.equals("sales_chance") || relateCodeTop.equals("sales_order") || relateCodeTop.equals("sales_service")) {
			continue;
		}
	%>
		<li id="menu<%=menuItemTop%>"><a href="<%=request.getContextPath()%>/sales/module_list_relate.jsp?customerId=<%=idTop%>&parentId=<%=parentIdTop%>&menuItem=<%=menuItemTop%>&formCodeRelated=<%=mrdTop.getString("relate_code")%>&formCode=<%=formCodeTop%>"><span><%=fdTopRelate.getFormDb(mrdTop.getString("relate_code")).getName()%></span></a></li>
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
		<li id="menu<%=menuItemTop%>" tagName="<%=subTagsTop[i]%>"><a href="<%=ModuleUtil.filterViewEditTagUrl(request, formCodeTop, subTagsTop[i])%>&customerId=<%=idTop%>&menuItem=<%=menuItemTop%>"><span><%=subTagsTop[i]%></span></a></li>
	<%	
		menuItemTop++;
	}
}
%>        
  </ul>
</div>