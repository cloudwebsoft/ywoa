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
<div id="drag_<%=id%>" type="<%=DesktopUnit.TYPE_LIST%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor">
    <div class="">
<%
String userName = privilege.getUser(request);
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
SelectOptionDb sod = new SelectOptionDb();
String sql = "select id from form_table_sales_customer where sales_person=" + StrUtil.sqlstr(userName) + " order by id desc";	
String formCode = "sales_customer";
ListResult lr = fdao.listResult(formCode, sql, 1, count);
Vector v = lr.getResult();
Iterator ir = v.iterator();
%>
 	<div id="drag_<%=id%>_h" class="box">
          	<!-- <span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="javascript:;" onclick="addTab('我的客户', '<%=request.getContextPath()%>/sales/customer_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>')">最新客户</a></span> -->
            <!-- <span class="btnSpan"> -->
           	<!-- 	<div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div> -->
		    <!-- 	<div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div> -->
		    <!-- 	<div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div> -->
            <!-- </span> -->
            <div class="titleimg">
            <!--<img src="images/desktop/sales.user_customer.png" width="40" height="40" />-->
            <i class="fa <%=udsd.getIcon()%>"></i>
            &nbsp;&nbsp;</div>
            <div class="titletxt">&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('我的客户', '<%=request.getContextPath()%>/sales/customer_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>')"><%=udsd.getTitle()%></a></div>
      </div>
      <table id="drag_<%=udsd.getId()%>_c" class="tabStyle_1" style="width:100%" width="100%" border="0" align="center" cellspacing="0">
        <thead>
          <tr align="center">
            <td width="24%">客户</td>
            <td width="19%">销售方式</td>
            <td width="19%">性质</td>
            <td width="19%">电话</td>
            <td width="19%">发现日期</td>
          </tr>
        </thead>
        <%	
	  	int k = 0;
		while (ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long fdaoId = fdao.getId();
		%>
        <tr align="center">
          <td align="left"><a href="sales/customer_show.jsp?id=<%=fdaoId%>&amp;formCode=sales_customer" target="_blank"><%=fdao.getFieldValue("customer")%></a></td>
          <td align="left"><%=sod.getOptionName("xsfs", fdao.getFieldValue("sellMode"))%></td>
          <td align="left"><%=sod.getOptionName("qyxz", fdao.getFieldValue("enterType"))%></td>
          <td align="left"><%=fdao.getFieldValue("tel")%></td>
          <td align="left"><%=fdao.getFieldValue("find_date")%></td>
        </tr>
        <%
  }
%>
    </table>
    </div>
</div>