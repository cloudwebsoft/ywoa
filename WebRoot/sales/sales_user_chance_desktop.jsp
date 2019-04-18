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

    <div class="portlet_content" style="margin:0px; padding:0px">

<div id="drag_<%=id%>_h" class="box">
	<!--<span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="sales/sales_user_chance_list.jsp">最新商机</a></span>-->
	<!--<div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>-->
	<!--<div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div>-->
	<!--<div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div>-->
	<div class="titleimg">
    <!--<img src="images/desktop/sales.user_chance.png" width="40" height="40" />-->
    <i class="fa <%=udsd.getIcon()%>"></i>
    &nbsp;&nbsp;</div>
    <div class="titletxt">&nbsp;&nbsp;<a href="sales/sales_user_chance_list.jsp"><%=udsd.getTitle()%></a></div>
</div> 
<%
String userName = privilege.getUser(request);
String formCode = "sales_chance";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

String sql = "select ch.id from " + fd.getTableNameByForm() + " ch, form_table_sales_customer c where ch.cws_id=c.id and c.sales_person=" + StrUtil.sqlstr(userName) + " order by ch.find_date desc";
// out.print(sql);

int pagesize = count;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
Vector v = lr.getResult();
Iterator ir = v.iterator();
%>
<table id="drag_<%=udsd.getId()%>_c" width="100%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1" style="width:100%">
      <thead>
        <tr>
          <td width="24%">客户</td>
          <td width="19%">商机阶段</td>
          <td width="19%">预计金额</td>
          <td width="19%">发现日期</td>
          <td width="19%">可能性</td>
        </tr>
      </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	com.redmoon.oa.visual.FormDAO fdaoCustomer = new com.redmoon.oa.visual.FormDAO();
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			i++;
			long fdaoId = fdao.getId();
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getFieldValue("customer")), customerfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getCreator());
			realName = user.getRealName();
			
			sql = "select sum(zj) from form_table_sales_chance c, form_table_sales_cha_product p where c.id=p.cws_id and c.id=" + fdaoId;
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			double sum = 0.0;
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				sum = rr.getDouble(1);
			}
		%>
        <tr>
          <td><a href="sales/customer_sales_chance_show.jsp?customerId=<%=fdao.getCwsId()%>&amp;parentId=<%=fdao.getCwsId()%>&amp;id=<%=fdaoId%>&amp;formCodeRelated=sales_chance&amp;formCode=sales_customer&amp;isShowNav=1" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=sod.getOptionName("sales_chance_state", fdao.getFieldValue("state"))%></td>
          <td><%=sum%></td>
          <td><%=fdao.getFieldValue("find_date")%></td>
          <td><div class="progressBar" style="">
            <div class="progressBarFore" style="width:<%=fdao.getFieldValue("possibility")%>%;"></div>
            <div class="progressText"> <%=fdao.getFieldValue("possibility")%>% </div>
          </div></td>
        </tr>
        <%
		}
%>
    </table>
    </div>
</div>