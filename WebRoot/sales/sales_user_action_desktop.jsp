<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.visual.*"%>
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

String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor">
    <div class="portlet_content" style="margin:0px; padding:0px">
<%
String formCode = "day_lxr";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String userName = privilege.getUser(request);
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
SelectOptionDb sod = new SelectOptionDb();

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "visit_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String sql = "select d.id from " + fd.getTableNameByForm() + " d, form_table_sales_linkman l, form_table_sales_customer c where c.id=l.customer and d.lxr=l.id and c.sales_person=" + StrUtil.sqlstr(userName) + " and d.is_visited='是'";
// String sql = "select id from " + fd.getTableNameByForm() + " d where d.cws_creator=" + StrUtil.sqlstr(userName) + " and d.is_visited='是'";
sql += " order by " + orderBy + " " + sort;

ListResult lr = fdao.listResult(formCode, sql, 1, count);
Vector v = lr.getResult();
Iterator ir = v.iterator();
%>
      
          	
    <div id="drag_<%=id%>_h" class="box">
		<!-- <span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="sales/sales_user_action_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>">最新行动</a></span> -->
        <!-- <div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div> -->
    	<!-- <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div> -->
    	<!-- <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div> -->
  		<div class="titleimg">
        <!--<img src="images/desktop/sales.user_action.png" width="40" height="40" />-->
        <i class="fa <%=udsd.getIcon()%>"></i>
        &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<a href="sales/sales_user_action_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>"><%=udsd.getTitle()%></a></div>
    </div>         
         
      <table id="drag_<%=udsd.getId()%>_c" class="tabStyle_1" style="width:100%" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <tr align="center">
          <td class="tabStyle_1_title" width="11%">联系人 </td>
          <td class="tabStyle_1_title" width="15%">客户</td>
          <td class="tabStyle_1_title" width="9%" >日期</td>
          <td class="tabStyle_1_title" width="24%">联系结果</td>
        </tr>
        <%	
	  	int i = 0;
		FormDb fdLinkman = new FormDb();
		fdLinkman = fdLinkman.getFormDb("sales_linkman");
		FormDb fdCustomer = new FormDb();
		fdCustomer = fdCustomer.getFormDb("sales_customer");
		com.redmoon.oa.visual.FormDAO fdaoLinkman = new com.redmoon.oa.visual.FormDAO();
		com.redmoon.oa.visual.FormDAO fdaoCustomer = new com.redmoon.oa.visual.FormDAO();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long myid = fdao.getId();
			fdaoLinkman = fdaoLinkman.getFormDAO(StrUtil.toInt(fdao.getFieldValue("lxr")), fdLinkman);
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toInt(fdaoLinkman.getFieldValue("customer")), fdCustomer);
		%>
        <tr align="center">
          <td width="11%" align="left"><a target="_blank" href="visual/module_show.jsp?id=<%=myid%>&amp;action=<%=action%>&amp;formCode=<%=formCode%>&amp;isShowNav=0"><%=fdaoLinkman.getFieldValue("linkmanName")%></a></td>
          <td width="15%" align="left"><a target="_blank" href="sales/customer_show.jsp?id=<%=fdaoLinkman.getFieldValue("customer")%>&amp;action=<%=StrUtil.UrlEncode(action)%>&amp;formCode=sales_customer"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td width="9%" align="center"><%=fdao.getFieldValue("visit_date")%></td>
          <td width="24%" align="left"><%=fdao.getFieldValue("contact_result")%></td>
        </tr>
        <%
		}
%>
      </table>
    </div>
</div>