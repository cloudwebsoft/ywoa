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
<%@ page import="com.redmoon.oa.flow.*"%>
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
%>
<div id="drag_<%=id%>" class="portlet drag_div bor" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" >
    <div id="drag_<%=id%>_h" class="portlet_topper" style="height:3px;padding:0px;margin:0px; font-size:1px; border:0px">
    </div>
    <div class="portlet_content" style="margin:0px; padding:0px">
<%
FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

String userName = privilege.getUser(request);
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
com.redmoon.oa.visual.FormDAO fdaoCustomer = new com.redmoon.oa.visual.FormDAO();
SelectOptionDb sod = new SelectOptionDb();
String sql = "select o.id from form_table_sales_order o, form_table_sales_customer c where o.customer=c.id and c.sales_person=" + StrUtil.sqlstr(userName) + " order by o.id desc";
// String sql = "select id from form_table_sales_order where cws_creator=" + StrUtil.sqlstr(userName) + " order by id desc" ;		
String formCode = "sales_order";
ListResult lr = fdao.listResult(formCode, sql, 1, count);
Vector v = lr.getResult();
Iterator ir = v.iterator();
%>
  
          <div id="drag_<%=id%>_h" class="box">
			<!-- <span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="sales_user_order_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>">最新订单</a></span> -->
            <!-- <div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div> -->
		    <!-- <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div> -->
		    <!-- <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div> -->
		    <div class="titleimg">
            <!--<img src="images/desktop/sales.user_order.png" width="40" height="40" />-->
            <i class="fa <%=udsd.getIcon()%>"></i>
            &nbsp;&nbsp;</div>
            <div class="titletxt">&nbsp;&nbsp;<a href="sales_user_order_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>"><%=udsd.getTitle()%></a></div>
        </div>
      <table id="drag_<%=udsd.getId()%>_c" class="tabStyle_1" style="width:100%" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <thead>
          <tr align="center">
            <td width="24%">客户</td>
            <td width="19%">订单来源</td>
            <td width="19%">订单状态</td>
            <td width="19%">促成人员</td>
            <td width="19%">促成日期</td>
          </tr>
        </thead>
        <%	
	  	int k = 0;
		while (ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long fdaoId = fdao.getId();
			
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getCwsId()), customerfd);			
		%>
        <tr align="center">
          <td align="left"><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>订单', '<%=request.getContextPath()%>/visual/module_mode1_show.jsp?parentId=<%=id%>&id=<%=fdaoId%>&formCode=sales_order')"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td align="left"><%=sod.getOptionName("sales_order_source", fdao.getFieldValue("source"))%></td>
          <td align="left"><%=sod.getOptionName("sales_order_state", fdao.getFieldValue("status"))%></td>
          <td align="left">&nbsp;</td>
          <td align="left"><%=fdao.getFieldValue("order_date")%></td>
        </tr>
        <%
  }
%>
    </table>
    </div>
</div>