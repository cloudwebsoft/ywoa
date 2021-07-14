<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:url value="/mywork/showWorkLogInfo" var="d1">
    <c:param name="userName" value="${userName}"/>
</c:url>
<c:url value="/mywork/queryMyWeekWorkForShow" var="d2">
    <c:param name="logType" value="1"/>
    <c:param name="userName" value="${userName}"/>
</c:url>
<c:url value="/mywork/queryMyMonthWorkForShow" var="d3">
    <c:param name="logType" value="2"/>
    <c:param name="userName" value="${userName}"/>
</c:url>
<div class="tabs1Box">
    <div id="tabs1">
        <ul>
            <li id="menu1"><a href="${d1}"><span>日报</span></a></li>
            <li id="menu2"><a href="${d2}"><span>周报</span></a></li>
            <li id="menu3"><a href="${d3}"><span>月报</span></a></li>
        </ul>
    </div>
</div>

