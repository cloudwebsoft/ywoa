<%@ page contentType="text/html;charset=utf-8"%>
<div class="tabs1Box">
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/ymoa/showWorkLogInfo.action?userName=<s:property value="userName"/>"><span>日报</span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/ymoa/queryMyWeekWorkForShow.action?logType=1&userName=<s:property value="userName"/>"><span>周报</span></a></li>
    <li id="menu3"><a href="<%=request.getContextPath()%>/ymoa/queryMyMonthWorkForShow.action?logType=2&userName=<s:property value="userName"/>"><span>月报</span></a></li>
    
  </ul>
</div>
</div>

