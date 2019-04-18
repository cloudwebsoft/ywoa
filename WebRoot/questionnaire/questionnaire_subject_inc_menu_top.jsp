<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%
int formIdTop = ParamUtil.getInt(request, "form_id");
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="questionnaire_subject_list.jsp?form_id=<%=formIdTop%>"><span>问卷题目</span></a></li>
    <li id="menu2"><a href="questionnaire_subject_add.jsp?form_id=<%=formIdTop%>"><span>添加题目</span></a></li>
  </ul>
</div>

