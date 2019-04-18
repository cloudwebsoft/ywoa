<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String unitCode = privilege.getUserUnitCode(request);

	String op = ParamUtil.get(request, "op");
	
	String action = ParamUtil.get(request, "action");
	String searchName = ParamUtil.get(request, "searchName");
    String beginDate = ParamUtil.get(request, "beginDate");
    String endDate = ParamUtil.get(request, "endDate");
    int curpage	= ParamUtil.getInt(request, "CPages", 1);
	
	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	String queryStr = "action=" + action + "&searchName=" + StrUtil.UrlEncode(searchName) + "&beginDate=" + beginDate + "&endDate=" + endDate;
	
	if(op.equals("add")) {
		try {
			String des = ParamUtil.get(request, "description");
			if (des.length() >200){
				out.print(StrUtil.Alert_Back("问卷描述不能超过200字符，请修改!"));
			}
			QuestionnaireFormMgr qfm = new QuestionnaireFormMgr();
			int flag = qfm.create(request);//返回的form_id
			if(flag != -1) {
				out.print(StrUtil.Alert_Redirect("问卷添加成功", "questionnaire_form_m.jsp"));
				return;
			}
		} catch(ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
	}
	else if(op.equals("del")) {
		try {
			QuestionnaireFormMgr qfm = new QuestionnaireFormMgr();
			boolean re = qfm.del(request);
			if(re) {
				out.print(StrUtil.Alert_Redirect("问卷删除成功", "questionnaire_form_m.jsp?" + queryStr));
				return;
			}
		} catch(ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
	}
	
	QuestionnaireFormDb qfd = new QuestionnaireFormDb();
   	String sql = "select form_id from " + qfd.getTableName() + " where unit_code=" + StrUtil.sqlstr(unitCode);	
	
	if (action.equals("search")) {
		if (!searchName.equals("")) {
			sql += " and form_name like " + StrUtil.sqlstr("%" + searchName + "%");
		}
		if (!beginDate.equals("")){
			sql += " and begin_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
		}
		if (!endDate.equals("")){
			sql += " and end_date<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
		}
	}
	sql += " order by create_date desc";
	
	ListResult lr = qfd.listResult(sql, curpage, pagesize);
	
	// ListResult lr = wf.listUserAttended(privilege.getUser(request), curpage, pagesize);
	int total = lr.getTotal();
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}	
	
	Iterator iForm = lr.getResult().iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>问卷表单生成第一步</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script>
function delForm(formId) {
	if(confirm("确定删除该问卷？")) {
		document.getElementById('op').value = "del";
		document.getElementById('form_id').value = formId;
		form.submit();
	}
}
</script>
</head>
<body>
<%@ include file="questionnaire_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form action="questionnaire_form_m.jsp" method="get" name="form1" id="form1">
  <table width="62%" border="0" align="center" cellpadding="2" cellspacing="0" class="percent60">
    <tr>
      <td align="center" nowrap="nowrap">名称
        <input name="searchName" id="searchName" size="10" maxlength="80" value="" />
      	<script>
		form1.searchName.value = "<%=searchName%>";
		</script>开始日期 
		<input type="text" id="beginDate" name="beginDate" size="10" value="" />
        <script>
		form1.beginDate.value = "<%=beginDate%>";
		</script>
      	 结束日期 
        <input type="text" id="endDate" name="endDate" size="10" value="" />
        <script>
		form1.endDate.value = "<%=endDate%>";
		</script>
          <input name="action" value="search" type="hidden" />
      <input class="btn" type="submit" value="搜索" /></td>
    </tr>
  </table>
</form>
<table width="98%" border="0" cellpadding="0" cellspacing="0" class="percent98">
  <tr><td align="right">
&nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>
</td></tr></table>
<table class="tabStyle_1 percent98" width="98%" border="0" cellpadding="6" cellspacing="0">
  <tr>
    <td class="tabStyle_1_title" width="18%">问卷名称</td>
    <td class="tabStyle_1_title" width="22%">问卷描述</td>
    <td class="tabStyle_1_title" width="10%">开始日期</td>
    <td class="tabStyle_1_title" width="10%">结束日期</td>
    <td class="tabStyle_1_title" width="6%" align="center">启用</td>
    <td class="tabStyle_1_title" width="10%">创建日期</td>
    <td class="tabStyle_1_title" width="24%">操作</td>
  </tr>
<%
	while(iForm.hasNext()) {
		qfd = (QuestionnaireFormDb)iForm.next();
%>
  <tr>
    <td align="left"><%=qfd.getFormName()%></td>
    <td align="left" title="<%=qfd.getDescription()%>"><%=StrUtil.getLeft(qfd.getDescription(),60)%></td>
    <td align="center"><%=DateUtil.format(qfd.getBeginDate(), "yyyy-MM-dd")%></td>
    <td align="center"><%=DateUtil.format(qfd.getEndDate(), "yyyy-MM-dd")%></td>
    <td align="center" title="<%=qfd.getDescription()%>"><%=qfd.isOpen()?"是":"否"%></td>
    <td align="center"><%=StrUtil.FormatDate(qfd.getCreateDate(),"yyyy-MM-dd")%></td>
    <td align="center">
    <a href="questionnaire_form_edit.jsp?form_id=<%=qfd.getFormId()%>">编辑</a>
    &nbsp;
    <a href="javascript:;" onclick="addTab('<%=qfd.getFormName()%>题目', '<%=request.getContextPath()%>/questionnaire/questionnaire_subject_list.jsp?form_id=<%=qfd.getFormId()%>')">题目</a>
    &nbsp;
    <a href="questionnaire_form_m.jsp?op=del&form_id=<%=qfd.getFormId()%>&<%=queryStr%>" onclick="if (!confirm('您确定要删除么？')) return false">删除</a>
    &nbsp;
    <a href="javascript:;" onclick="addTab('<%=qfd.getFormName()%>', '<%=request.getContextPath()%>/questionnaire/questionnaire_result.jsp?form_id=<%=qfd.getFormId()%>')">答卷</a>
    &nbsp;
    <a href="javascript:;" onclick="addTab('<%=qfd.getFormName()%>权限', '<%=request.getContextPath()%>/questionnaire/questionnaire_priv_list.jsp?questId=<%=qfd.getFormId()%>')">权限</a>
    </td>
  </tr>
  <%
	}
%>
</table>
<table width="98%" class="percent98">
      <tr>
        <td align="right">
		<%
		out.print(paginator.getCurPageBlock("questionnaire_form_m.jsp?" + queryStr ));
		%>
        </td>
  </tr>
</table>
</body>
<script>
$(function(){
	$('#beginDate').datetimepicker({
	   	lang:'ch',
	   	timepicker:false,
	   	format:'Y-m-d',
	   	formatDate:'Y/m/d'
   });
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
		format:'Y-m-d',
		formatDate:'Y/m/d'
	});
})
</script>
</html>
