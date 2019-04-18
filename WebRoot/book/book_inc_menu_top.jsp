<%@ page contentType="text/html; charset=utf-8"%>
<div id="tabs1">
  <ul>
    <li id="menu2"><a href="book_list.jsp"><span>图书借阅</span></a></li>
<%
com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
if (pvgTop.isUserPrivValid(request, "book.all")) {
%>	
    <li id="menu1"><a href="book_add.jsp"><span>图书添加</span></a></li>
    <li id="menu3"><a href="book_return_list.jsp"><span>图书归还</span></a></li>
  	<li id="menu4"><a href="book_type_list.jsp"><span>图书类别</span></a></li>
<%}%>	
  </ul>
</div>