<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@page import="cn.js.fan.web.Global"%>
<div id="tabs1">
  <ul>
    <!-- <li id="menu1"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_type_list.jsp"><span>办公用品管理</span></a></li>-->
    <%
    String pv ="officeequip";
    com.redmoon.oa.pvg.Privilege pri = new com.redmoon.oa.pvg.Privilege();
    if (pri.isUserPrivValid(request, pv)) {
    %>
    <li id="menu1"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_all_list.jsp"><span>库存</span></a></li>
    <li id="menu2"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_frame.jsp?root_code=office_equipment"><span>品名管理</span></a></li>
    <li id="menu3"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_add_list.jsp"><span>入库登记</span></a></li>
  	<li id="menu4"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_return_list.jsp?opType=<%=OfficeOpDb.TYPE_RECEIVE%>"><span>领用登记</span></a></li>
    <!-- <li id="menu5"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_receive_search.jsp"><span>领用查询</span></a></li> -->
	<li id="menu6"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_return_list.jsp?opType=<%=OfficeOpDb.TYPE_BORROW%>"><span>借用登记</span></a></li>
	<li id="menu7"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_return_list.jsp?opType=<%=OfficeOpDb.TYPE_RETURN%>"><span>归还用品</span></a></li>
	<!-- <li id="menu8"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_return.jsp"><span>借用查询</span></a></li> -->
	<li id="menu9"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_tookstock_list.jsp?tookStock=1"><span>盘点</span></a></li>
	<%--<li id="menu11"><a class="black" href="<%=Global.getFullRootPath(request) %>/data_import/import.jsp?code=officeequip&menuItem=11"><span>导入</span></a></li>--%>
	<%} %>
	<li id="menu10"><a class="black" href="<%=Global.getFullRootPath(request) %>/officeequip/officeequip_search.jsp"><span>查询</span></a></li>
  </ul>
</div>