<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	int formId = ParamUtil.getInt(request, "form_id", -1);
	if(formId == -1) {
		out.print(StrUtil.Alert_Redirect("请指定需要查看的问卷","questionnaire_form_m.jsp"));
	}
	
	String op = ParamUtil.get(request, "op");
	if (op.equals("del")) {
		int questionnaireNum = ParamUtil.getInt(request, "questionnaireNum");
		QuestionnaireItemDb qid = new QuestionnaireItemDb();
		qid.delAnswer(questionnaireNum);
		out.print(StrUtil.Alert_Redirect("删除成功！","questionnaire_result.jsp?form_id=" + formId));
		return;
	}
	
	QuestionnaireFormDb qfd = new QuestionnaireFormDb();
	qfd = qfd.getQuestionnaireFormDb(formId);
	
	String realName = "";
	String sql = "select id from oa_questionnaire_item where form_id=" + formId;
	if ("search".equals(op)) {
		realName = ParamUtil.get(request, "realName");
		if (!"".equals(realName)) {
			sql = "select q.id from oa_questionnaire_item q, users u where q.user_name=u.name and q.form_id=" + formId + " and u.realname like " + StrUtil.sqlstr(realName);
		}
	}
	QuestionnaireItemDb qid = new QuestionnaireItemDb();
	Vector vQuestionnaire = qid.list(sql);
	Iterator iQuestionnaire = vQuestionnaire.iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>问卷调查-调查情况</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">问卷调查结果&nbsp;-&nbsp;<%=qfd.getFormName()%></td>
    </tr>
  </tbody>
</table>
<br>
<form id="formSearch" action="questionnaire_result.jsp" method="post">
<table width="98%">
<tr>
<td align="center">
用户
<input id="realName" name="realName" value="<%=realName %>" />
<input type="submit" value="搜索" />
<input id="op" name="op" value="search" type="hidden" />
<input id="form_id" name="form_id" value="<%=formId%>" type="hidden" />
</td>
</tr>
</table>
</form>
<table id="mainTable" width="98%" border="0" cellpadding="6" cellspacing="0" class="tabStyle_1 percent80">
  <tr>
    <td width="7%" class="tabStyle_1_title">编号</td>
    <td width="15%" class="tabStyle_1_title">用户</td>
    <td width="56%" class="tabStyle_1_title">填写日期</td>
    <td width="22%" class="tabStyle_1_title">操作</td>
  </tr>
  <%
	int currentQuestionnaireNum = -1;
	boolean isFirst = true;
	UserMgr um = new UserMgr();
	while(iQuestionnaire.hasNext()) {
		qid = (QuestionnaireItemDb)iQuestionnaire.next();
		int questionnaireNum = qid.getQuestionnaireNum();
		if(isFirst) {
			currentQuestionnaireNum = questionnaireNum;
			isFirst = false;
		} else {
			if(currentQuestionnaireNum == questionnaireNum) {
				continue;
			} else {
				currentQuestionnaireNum = questionnaireNum;
			}
		}
		String userRealName = um.getUserDb(qid.getUserName()).getRealName();
%>
  <tr>
    <td align="center"><%=questionnaireNum%></td>
    <td><%=userRealName%></td>
    <td><%=DateUtil.format(qid.getFillDate(),"yyyy-MM-dd")%></td>
    <td align="center">
    <a href="javascript:;" onclick="addTab('<%=userRealName%>的问卷', '<%=request.getContextPath()%>/questionnaire/questionnaire_show.jsp?formId=<%=formId%>&questionnaire_num=<%=questionnaireNum%>')">问卷详情</a>
    &nbsp;&nbsp;
    <a href="javascript:;" onclick="if (confirm('您确定要删除吗？')) window.location.href='questionnaire_result.jsp?op=del&amp;form_id=<%=formId%>&amp;questionnaireNum=<%=questionnaireNum%>'">删除</a></td>
  </tr>
  <%
	}
%>
</table>
</body>
<script>
$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>
