<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String unitCode = privilege.getUserUnitCode(request);

QuestionnaireFormDb qfd = new QuestionnaireFormDb();
java.util.Date now = new java.util.Date();
String sql = "select form_id from " + qfd.getTableName() + " where is_open=1 and begin_date<=" + SQLFilter.getDateStr(DateUtil.format(now, "yyyy-MM-dd"), "yyyy-MM-dd") + " and end_date>" + SQLFilter.getDateStr(DateUtil.format(now, "yyyy-MM-dd"), "yyyy-MM-dd") + " order by create_date desc";

// out.print(sql);

Vector vForm = qfd.list(sql);
Iterator iForm = vForm.iterator();

String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>问卷调查-参与</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">问卷调查</td>
    </tr>
  </tbody>
</table>
<br>
<table width="98%" border="0" cellpadding="6" cellspacing="0" class="tabStyle_1 percent98">
  <tr>
    <td class="tabStyle_1_title" width="22%">问卷名称</td>
    <td class="tabStyle_1_title" width="48%">问卷描述</td>
    <td class="tabStyle_1_title" width="10%">日期</td>
    <td class="tabStyle_1_title" width="20%">操作</td>
  </tr>
  <%
	QuestionnairePriv qp = new QuestionnairePriv();
	String userName = privilege.getUser(request);
	while(iForm.hasNext()) {
		qfd = (QuestionnaireFormDb)iForm.next();
		if (!qp.canUserSee(request, qfd.getFormId())) {
			continue;
		}
%>
  <tr>
    <td align="left"><%=qfd.getFormName()%></td>
    <td align="left" title="<%=qfd.getDescription()%>"><%=StrUtil.getLeft(qfd.getDescription(),60)%></td>
    <td align="center"><%=StrUtil.FormatDate(qfd.getCreateDate(),"yyyy-MM-dd")%></td>
    <td align="center">
    	<%
		QuestionnaireItemDb qid = new QuestionnaireItemDb();
		int questionnaireNum = qid.getUserAttended(qfd.getFormId(), userName);
		if (questionnaireNum==-1) {
		%>
    	<a href="javascript:;" onclick="addTab('<%=qfd.getFormName()%>', '<%=request.getContextPath()%>/questionnaire/question_show.jsp?form_id=<%=qfd.getFormId()%>')">参与</a>
       	<%
		}
		boolean canSeeResult = false;
		if (qp.canUserAppend(request, qfd.getFormId())) {
			if (questionnaireNum!=-1) {
				canSeeResult = true;
			}
		}
		if (qp.canUserModify(request, qfd.getFormId())) {
			canSeeResult = true;
		}
		if (questionnaireNum!=-1) {
		%>
      	&nbsp;&nbsp;
        <a href="javascript:;" onclick="addTab('<%=qfd.getFormName()%>', '<%=request.getContextPath()%>/questionnaire/questionnaire_show.jsp?formId=<%=qfd.getFormId()%>&questionnaire_num=<%=questionnaireNum%>')">我的问卷</a>
      	<%
		}
		if (false && canSeeResult) {
		%>
      	&nbsp;&nbsp;
        <a href="javascript:;" onclick="addTab('<%=qfd.getFormName()%>', '<%=request.getContextPath()%>/questionnaire/questionnaire_show.jsp?formId=<%=qfd.getFormId()%>&questionnaire_num=<%=questionnaireNum%>">查看结果</a>
        <%}%>      	
      </td>
  </tr>
<%}%>
</table>
</body>
</html>
