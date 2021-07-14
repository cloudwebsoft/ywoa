<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int questionnaireNum = ParamUtil.getInt(request, "questionnaire_num", -1);
if(questionnaireNum == -1) {
	out.print(StrUtil.Alert_Redirect("请指定需要查看的问卷编号","questionnaire_form_m.jsp"));
	return;
}
int formId = ParamUtil.getInt(request, "formId");
QuestionnaireItemDb qid = new QuestionnaireItemDb();
int qNum = qid.getUserAttended(formId, privilege.getUser(request));
if (qNum!=questionnaireNum) {
	if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
		out.print(StrUtil.Alert_Back("您无权查看编号为" + questionnaireNum + "的答卷！"));
		return;
	}
}
/*
else {
	QuestionnaireFormDb qfd = new QuestionnaireFormDb();
	qfd = qfd.getQuestionnaireFormDb(formId);
	if (!qfd.isPublic()) {
		if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
			%>
			<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />			
			<%
			out.print(SkinUtil.makeInfo(request, "您无权查看编号为" + questionnaireNum + "的答卷！"));
			return;
		}
	}
}
*/

UserMgr um = new UserMgr();

String sql = "select id from oa_questionnaire_item where questionnaire_num=" + questionnaireNum;

Vector vItem = qid.list(sql);
Iterator iItem = vItem.iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>问卷查看</title>
<style>
a:link {
text-decoration:none;
color:#4B4B4B;
}
a:visited {
text-decoration:none;
color:#4B4B4B;
}
a:hover {
text-decoration:none;
color:#FF3300;
}
</style>
<script src="../inc/common.js"></script>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">问卷&nbsp;[编号：<%=questionnaireNum%>]&nbsp;的结果</td>
    </tr>
  </tbody>
</table>
<br>
<div id="divTitle" style="text-align:center; height:30px; font-weight:bold">
</div>
<table class="tabStyle_1 percent98" width="98%" border="0" cellpadding="6" cellspacing="0">
  <tr>
    <td width="6%" align="center" class="tabStyle_1_title">序号</td>
    <td class="tabStyle_1_title" width="41%">题目名称</td>
    <td class="tabStyle_1_title" width="8%">类型</td>
    <td class="tabStyle_1_title" width="45%">项目填写情况(用户选择的选项标注为<font color="#FF0000">红色</font>)</td>
  </tr>
  <%
    String[] ary = {"A", "B", "C", "D", "E", "F"};  
	while(iItem.hasNext()) {
		qid = (QuestionnaireItemDb)iItem.next();
		QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
		qfid = qfid.getQuestionnaireFormItemDb(qid.getItemId());
%>
  <tr>
    <td align="center"><%=qfid.getItemIndex()%></td>
    <td align="left"><%=qfid.getItemName()%></td>
    <%
		int itemType = qfid.getItemType();
		String sItemType = "";
		if(itemType==0) {
			sItemType = "填空";
		} else if (itemType==1) {
			sItemType = "问答";
		} else if (itemType==2) {
			sItemType = "单选";
		} else if (itemType==3) {
			sItemType = "多选";
		}
%>
    <td align="center"><%=sItemType%></td>
    <td align="left" style="line-height:1.8"><%
		if(qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_INPUT || qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_TEXTAREA) {
%>
        <%=qid.getItemValue()%>
        <%
		} else if(qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP) {
			Vector v = qfid.getSubItems();
			Iterator ir = v.iterator();
			int i=0;
			while (ir.hasNext()) {
				QuestionnaireFormSubitemDb qfsd = (QuestionnaireFormSubitemDb)ir.next();
				if(qfsd.getId() == StrUtil.toInt(qid.getItemValue(), -1)) {
%>
        <font color="#FF0000"><%=ary[i]%>、&nbsp;<%=qfsd.getName()%></font><br />
        <%
				}else{
		%>
        			<%=ary[i]%>、&nbsp;<%=qfsd.getName()%><br />
        <%
				}
				i++;
			}
		} else if(qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
			sql = "select subitem_id from oa_questionnaire_subitem where questionnaire_num=" + questionnaireNum + " and item_id=" + qid.getItemId();
			QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
			Iterator iSubitem = qsd.list(sql).iterator();
			Vector v = qfid.getSubItems();
			Iterator ir = v.iterator();
			int i=0;
			boolean isFirst = true;
			while (ir.hasNext()) {
				QuestionnaireFormSubitemDb qfsd = (QuestionnaireFormSubitemDb)ir.next();
				if(isFirst) {
					if(iSubitem.hasNext()) {
						qsd = (QuestionnaireSubitemDb)iSubitem.next();
					}
					isFirst = false;
				}
				if(qfsd.getId() == qsd.getSubitemValue()) {
%>
        <font color="#FF0000"><%=ary[i]%>、&nbsp;<%=qfsd.getName()%></font><br />
        <%
					if(iSubitem.hasNext()) {
						qsd = (QuestionnaireSubitemDb)iSubitem.next();
					}
				} else {
%>
        <%=ary[i]%>、&nbsp;<%=qfsd.getName()%><br />
        <%
				}
				i++;
			}
		}
%>    </td>
  </tr>
  <%
	}
%>
</table>
</body>
<script>
o("divTitle").innerHTML = "用户：<%=um.getUserDb(StrUtil.getNullString(qid.getUserName())).getRealName()%>&nbsp;&nbsp;&nbsp;&nbsp;填写日期：<%=DateUtil.format(qid.getFillDate(),"yyyy-MM-dd")%>";
</script>
</html>