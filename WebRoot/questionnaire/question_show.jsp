<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

	int formId = 0;
	try {
		formId = ParamUtil.getInt(request, "form_id");
	} catch(ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}

	String sItemId = "";
	
	QuestionnaireFormDb qfd = new QuestionnaireFormDb();
	qfd = qfd.getQuestionnaireFormDb(formId);
	
	if (!qfd.isOpen()) {
		out.print(SkinUtil.makeErrMsg(request, "该问卷尚未开放！"));
		return;
	}
	
	QuestionnaireItemDb qid = new QuestionnaireItemDb();
	int questionnaire_num = qid.getUserAttended(formId, privilege.getUser(request));
	if (questionnaire_num!=-1) {
		%>
        <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.print(SkinUtil.makeInfo(request, "您已经回答过问卷了！"));
		return;
	}
%>
<%@ taglib uri="/WEB-INF/tlds/DirListTag.tld" prefix="dirlist" %>
<%@ taglib uri="/WEB-INF/tlds/DocumentTag.tld" prefix="left_doc" %>
<%@ taglib uri="/WEB-INF/tlds/DocListTag.tld" prefix="dl" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>回答问卷：<%=qfd.getFormName()%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<%	
String op = ParamUtil.get(request, "op");
if(op.equals("add")) {
	QuestionnaireItemMgr qim = new QuestionnaireItemMgr();
	try {
		if(qim.create(request)) {
			out.print(StrUtil.jAlert_Redirect("提交成功，感谢您的参与！", "提示", "questionnaire_form_list.jsp"));
			return;
		}
	} catch(ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;
	}
}
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">问卷回答</td>
    </tr>
  </tbody>
</table>
<br>
<form id="form1" name="form1" action="question_show.jsp?op=add" method="post">
  <table class="tabStyle_1 percent98" width="98%" border="0" cellpadding="6" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" colspan="2"><%=StrUtil.toHtml(qfd.getDescription())%></td>
    </tr>
    <%
    String[] ary = {"A", "B", "C", "D", "E", "F"};        	
	QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
	String sql = "select item_id from oa_questionnaire_form_item where form_id=" + formId + " order by item_index";
	Vector vItem = qfid.list(sql);
	Iterator iItem = vItem.iterator();
	while(iItem.hasNext()) {
		qfid = (QuestionnaireFormItemDb)iItem.next();
		int itemId = qfid.getItemId();
		if(sItemId.equals("")) {
			sItemId = "" + itemId;
		} else {
			sItemId += ":" + itemId;
		}
%>
    <tr>
      <td width="29%" align="left"><%=qfid.getItemName()%><font color="#FF0000"><%=qfid.getCheckedType()==QuestionnaireFormItemDb.MUST_BE_FILLED ? "*" : ""%></font></td>
      <td width="71%" align="left" style="line-height:1.8"><%
		int itemType = qfid.getItemType();
		if(itemType == QuestionnaireFormItemDb.ITEM_TYPE_INPUT) {
%>
          <input id="item<%=itemId%>" name="item<%=itemId%>" />
          <%
		} else if(itemType == QuestionnaireFormItemDb.ITEM_TYPE_TEXTAREA) {
%>
          <textarea name="item<%=itemId%>" style="width:360px;height:150px"></textarea>
          <%
		} else if(itemType == QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP) {
			Iterator ir = qfid.getSubItems().iterator();
			int i=0;
			while (ir.hasNext()) {
				QuestionnaireFormSubitemDb qfsd = (QuestionnaireFormSubitemDb)ir.next();
		%>
				  <input type="radio" name="item<%=itemId%>" value="<%=qfsd.getId()%>" />
				  <%=ary[i]%>、&nbsp;<%=qfsd.getName()%>
				  <%
	  		i++;
	  		}
		} else if (itemType == QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
				Iterator ir = qfid.getSubItems().iterator();
			  int i=0;
			  while (ir.hasNext()) {
				QuestionnaireFormSubitemDb qfsd = (QuestionnaireFormSubitemDb)ir.next();
		%>
				  <input type="checkbox" name="item<%=itemId%>" value="<%=qfsd.getId()%>" />
				  <%=ary[i]%>、&nbsp;<%=qfsd.getName()%>
				  </br>
			  <%
				i++;
		  }
	}
%></td>
    </tr>
    <%}%>
    <tr>
      <td colspan="2" align="center"><input type="hidden" name="sItemId" value="<%=sItemId%>" />
          <input type="hidden" name="form_id" value="<%=formId%>" />
          <input class="btn" type="button" value="提交问卷" onclick="form1_onsubmit()" /></td>
    </tr>
  </table>
</form>
</body>
<script>
function form1_onsubmit() {
	jConfirm('您确定要提交么？', '提示', function(r) {
		if (r) {
			form1.submit();
		}
	});
}
</script>
</html>

