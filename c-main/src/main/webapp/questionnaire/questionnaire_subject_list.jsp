<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if(op.equals("del")) {
	JSONObject json = new JSONObject();
	try {
		QuestionnaireFormItemMgr qfim = new QuestionnaireFormItemMgr();
		boolean re = qfim.del(request);
		if(re) {
			json.put("ret", 1);
			json.put("msg", "操作成功！");
		}
	} catch(ErrMsgException e) {
		json.put("ret", 0);
		json.put("msg", e.getMessage());
	}
	out.print(json);
	return;
}

int formId = ParamUtil.getInt(request, "form_id");
QuestionnaireFormDb qfd = new QuestionnaireFormDb();
qfd = qfd.getQuestionnaireFormDb(formId);
QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();

if(op.equals("add")) {
	try {
		QuestionnaireFormItemMgr qfim = new QuestionnaireFormItemMgr();
		boolean re = qfim.create(request);//返回的item_id
		if(re) {
			out.print(StrUtil.Alert_Redirect("问卷项目添加成功","questionnaire_subject_list.jsp?form_id=" + formId));
			return;
		}
	} catch(ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if(op.equals("edit")) {
	try {
		QuestionnaireFormItemMgr qfim = new QuestionnaireFormItemMgr();
		boolean re = qfim.save(request);
		if(re) {
			out.print(StrUtil.Alert_Redirect("问卷项目修改成功","questionnaire_subject_list.jsp?form_id=" + formId));
			return;
		}
	} catch(ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>问卷题目</title>
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
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
function delItem(itemId) {
	jConfirm('您确定要删除么？', '提示', function(r) {
		if (!r) {
			return;
		}
		$.ajax({
			type: "post",
			url: "questionnaire_subject_list.jsp",
			data: {
				op: "del",
				item_id: itemId
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#mainTable').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret==1) {
					$('#tr' + itemId).remove();
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#mainTable').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});			
	});
}
</script>
</head>
<body>
<%@ include file="questionnaire_subject_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<table class="percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="center"><%=qfd.getFormName()%></td>
  </tr>
</table>
<table id="mainTable" class="tabStyle_1 percent98" width="98%" border="0" cellpadding="6" cellspacing="0" style="margin:10px">
  <tr>
    <td width="5%" class="tabStyle_1_title">序号</td>
    <td width="17%" class="tabStyle_1_title">名称</td>
    <td width="11%" class="tabStyle_1_title">类型</td>
    <td width="6%" class="tabStyle_1_title">必填</td>
    <td width="41%" class="tabStyle_1_title">选项/累计选择次数*权重</td>
    <td width="20%" class="tabStyle_1_title">操作</td>
  </tr>
  <%
  	String[] ary = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "k"};
	String sql = "select item_id from oa_questionnaire_form_item where form_id=" + formId + " order by item_index";
	Vector vItem = qfid.list(sql);
	Iterator iItem = vItem.iterator();
	while(iItem.hasNext()) {
		qfid = (QuestionnaireFormItemDb)iItem.next();
%>
  <tr id="tr<%=qfid.getItemId()%>">
    <td align="center"><%=qfid.getItemIndex()%></td>
    <td><%=qfid.getItemName()%></td>
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
    <td align="center"><%=qfid.getCheckedType()==0? "是" : "否"%></td>
    <td style="line-height:1.8">
	  <%
		if(qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_INPUT || qfid.getItemType()==QuestionnaireFormItemDb.ITEM_TYPE_TEXTAREA) {
			;
		} else {
			Vector itemOptions = qfid.getSubItems();
			QuestionnaireStatistics qs = new QuestionnaireStatistics();
			int [] itemValueStatistics = qs.itemValueStatistics(qfid.getItemId());
			for(int i=0;i<itemOptions.size();i++) {
		%>
			  <%=ary[i]%>、&nbsp;<%=((QuestionnaireFormSubitemDb)itemOptions.elementAt(i)).getName()%>&nbsp;<font color="#FF0000">[<%=itemValueStatistics[i]%>]</font><br />
	  	<%	}
        }%>
	</td>
    <td align="center">
    <a href="questionnaire_subject_edit.jsp?form_id=<%=formId%>&item_id=<%=qfid.getItemId()%>">编辑</a>
    &nbsp;&nbsp;
    <a href="javascript:;"onclick="delItem(<%=qfid.getItemId()%>)">删除</a>
    &nbsp;&nbsp;
    <a href="javascript:;" onclick="addTab('题目', '<%=request.getContextPath()%>/questionnaire/questionnaire_item_answer.jsp?form_id=<%=formId%>&item_id=<%=qfid.getItemId()%>')">查看</a>
    <% if(itemType==2 || itemType==3) {%>
    &nbsp;&nbsp;
    <a href="javascript:;" onclick="addTab('<%=StrUtil.getAbstract(request, qfid.getItemName(), 10, "")%>', '<%=request.getContextPath()%>/questionnaire/questionnaire_item_showchart.jsp?form_id=<%=formId%>&item_id=<%=qfid.getItemId()%>')" >统计</a>
    <% }%>
    </td>
  </tr>
  <%
	}
%>
</table>
<br>
</body>
</html>
