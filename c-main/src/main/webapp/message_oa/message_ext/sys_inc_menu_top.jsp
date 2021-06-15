<%@ page contentType="text/html;charset=utf-8" %>
<%
    String actionTop = ParamUtil.get(request, "action");
    String kindTop = ParamUtil.get(request, "kind");
    if (kindTop.equals("")) {
        kindTop = "title";
    }
    String actionTypeTop = ParamUtil.get(request, "actionType");
    String whatTop = ParamUtil.get(request, "what");
%>
<div id="tabs1">
    <ul>
        <li id="menu1"><a href="sys_message.jsp?isRecycle=0&action=<%=actionTop%>&actionType=<%=actionTypeTop%>&kind=<%=kindTop%>&what=<%=whatTop%>"><span>收件箱</span></a></li>
        <li id="menu2"><a href="sys_message.jsp?isRecycle=1&action=<%=actionTop%>&actionType=<%=actionTypeTop%>&kind=<%=kindTop%>&what=<%=whatTop%>"><span>垃圾箱</span></a></li>
    </ul>
</div>