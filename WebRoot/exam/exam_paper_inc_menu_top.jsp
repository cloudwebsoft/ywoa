<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%
int paperIdTop = ParamUtil.getInt(request, "paperId", -1);
String isManualTop = ParamUtil.get(request,"isManual");
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="exam_paper_priv_m.jsp?paperId=<%=paperIdTop%>&isManual=<%=isManualTop %>"><span>设置参与人员</span></a></li>
    <li id="menu2"><a href="exam_paper_not_attend.jsp?paperId=<%=paperIdTop%>&isManual=<%=isManualTop %>"><span>未参与人员</span></a></li>
  </ul>
</div>

