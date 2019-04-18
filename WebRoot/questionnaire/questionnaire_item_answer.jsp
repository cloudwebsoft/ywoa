<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
int itemId = ParamUtil.getInt(request, "item_id",-1);
if(itemId == -1) {
	out.print(StrUtil.Alert_Back("请指定需要查看的问卷项"));
}
QuestionnaireItemDb qid = new QuestionnaireItemDb();
qid = qid.getQuestionnaireItemDb(itemId);

QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
qfid = qfid.getQuestionnaireFormItemDb(itemId);

QuestionnaireFormDb qfd = new QuestionnaireFormDb();
qfd = qfd.getQuestionnaireFormDb(qfid.getFormId());

String sql = "select id from oa_questionnaire_item where item_id=" + itemId + " order by fill_date asc";
Vector vQuestionnaire = qid.list(sql);
Iterator iQuestionnaire = vQuestionnaire.iterator();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>问卷答案</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">调查情况</td>
    </tr>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent98" width="98%" border="0" cellpadding="6" cellspacing="0">
  <tr>
    <td width="6%" class="tabStyle_1_title">编号</td>
    <td width="12%" class="tabStyle_1_title">参与用户</td>
    <td width="14%" class="tabStyle_1_title">填写日期</td>
    <td width="54%" class="tabStyle_1_title">调查结果</td>
    <td width="14%" class="tabStyle_1_title">操作</td>
  </tr>
  <%
	int currentQuestionnaireNum = -1;
	boolean isFirst = true;
	QuestionnaireFormSubitemDb qfsd = new QuestionnaireFormSubitemDb();
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
%>
  <tr>
    <td align="center"><%=questionnaireNum%></td>
    <td align="center"><%=qid.getUserName()%></td>
    <td align="center"><%=DateUtil.format(qid.getFillDate(),"yyyy-MM-dd")%></td>
    <td align="left"><%
		if (qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
			QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
			Iterator ir = qsd.getSubitems(questionnaireNum).iterator();
			while (ir.hasNext()) {
				qsd = (QuestionnaireSubitemDb)ir.next();
		%>
        <%=qfsd.getQuestionnaireFormSubitemDb(qsd.getSubitemValue()).getName()%><br>
        <%	
			}
		}else if (qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP) {
			qfsd = qfsd.getQuestionnaireFormSubitemDb(StrUtil.toInt(qid.getItemValue()));
		%>
        <%=qfsd.getName()%>
        <%}else{%>
        <%=qid.getItemValue()%>
        <%}%>
    </td>
    <td align="center"><a href="javascript:;" onclick="addTab('<%=StrUtil.getAbstract(request, qfid.getItemName(), 10, "")%>', '<%=request.getContextPath()%>/questionnaire/questionnaire_show.jsp?questionnaire_num=<%=questionnaireNum%>&formId=<%=qfid.getFormId() %>')">问卷详情</a></td>
  </tr>
  <%
	}
%>
</table>
</body>
</html>
