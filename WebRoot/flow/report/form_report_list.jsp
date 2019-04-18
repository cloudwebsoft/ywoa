<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
		%>
        <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String op = ParamUtil.get(request, "op");

	String sql = "";
	sql = FormSQLBuilder.getFormQueryReport(privilege.getUser(request));
	String queryKey = ParamUtil.get(request, "queryKey");
	if(op.equals("query")) {
		sql = "select id from form_query_report where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and title like " + StrUtil.sqlstr("%" + queryKey + "%") + " order by id desc";
	}
	
	// System.out.println(getClass() + " " + sql);
	
	String querystr = "op=" + op + "&queryKey=" + StrUtil.UrlEncode(queryKey);
	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();

	String action = ParamUtil.get(request, "action");
    if (action.equals("del")) {
		FormQueryReportDb fqrd = new FormQueryReportDb();
		long id = ParamUtil.getLong(request, "id");
		fqrd = (FormQueryReportDb)fqrd.getQObjectDb(new Long(id));
		boolean re = fqrd.del();
		if (re) {
			out.print(StrUtil.Alert_Redirect("操作成功！", "form_report_list.jsp?op=" + op + "&queryKey=" + StrUtil.UrlEncode(queryKey) + "&CPages=" + curpage));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}
		return;		
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>报表列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<script src="../../inc/common.js"></script>
<script src="../../js/jquery.js"></script>
<script src="../../js/jquery-ui/jquery-ui.js"></script>
<script src="../../js/jquery.form.js"></script>
</head>
<body>
<%@ include file="form_report_nav.jsp"%>
<script>
o("menu0").className="current"; 
</script>
<div class="spacerH"></div>
<%				
		FormQueryReportDb aqd = new FormQueryReportDb();		
		ListResult lr = aqd.listResult(sql, curpage, pagesize);
		int total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		
		if (v!=null)
			ir = v.iterator();
			
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
	  <form name="queryForm" method="post" action="form_report_list.jsp?op=query">
      <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
          <td><input type="text" name="queryKey" value="<%=queryKey%>" />&nbsp;&nbsp;&nbsp;&nbsp;<input type="submit" class="btn" value="查找" />
          <input name="action" value="<%=action%>" type="hidden" />
          </td>
          <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
        </tr>
      </table>
	  </form>
	    <table id="mainTable" class="tabStyle_1 percent98" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td width="62%" class="tabStyle_1_title" height="24" >报表名称</td>
            <td width="20%" class="tabStyle_1_title" >时间</td>
            <td width="18%" class="tabStyle_1_title">操作</td>
          </tr>
	    <%
		while (ir!=null && ir.hasNext()) {
			aqd = (FormQueryReportDb)ir.next();
		%>
        <tr align="center" >
          <td width="62%" height="22" align="left"><span id="queryName<%=aqd.getLong("id")%>"><%=aqd.getString("title")%></span></td>
          <td width="20%" align="center"><%=DateUtil.format(aqd.getDate("create_date"), "yyyy-MM-dd HH:mm:ss")%></td> 
          <td>
          <a href="javascript:;" onClick="addTab('<%=aqd.getString("title")%>', '<%=request.getContextPath()%>/flow/report/form_report_show_jqgrid.jsp?reportId=<%=aqd.getLong("id")%>')">查看</a>
          <%if (action.equals("sel")) {%>
          &nbsp;&nbsp;
          <a href="javascript:;" onClick="sel('<%=aqd.getInt("id")%>', '<%=aqd.getInt("query_id")%>', '<%=aqd.getString("title")%>')">选择</a>
          <%}else{%>
          &nbsp;&nbsp;
          <a href="javascript:;" onClick="addTab('设计器', '<%=request.getContextPath()%>/flow/report/designer.jsp?id=<%=aqd.getLong("id")%>')">设计器</a>
          &nbsp;&nbsp;          
          <a onClick="return confirm('您确定要删除么？')" href="form_report_list.jsp?id=<%=aqd.getLong("id")%>&op=<%=op%>&queryKey=<%=StrUtil.UrlEncode(queryKey)%>&action=del">删除</a>&nbsp;&nbsp;
          <%}%>
          </td>
          </tr>
      <%}%>	 
      </table>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr> 
          <td width="1%" height="23">&nbsp;</td>
          <td height="23" valign="baseline"> 
            <div align="right">
             <%
			   out.print(paginator.getCurPageBlock("form_report_list.jsp?"+querystr));
			 %>
            &nbsp;</div></td>
        </tr>
      </table>
<div id="result"></div>
</body>
<script>
function sel(reportId, queryId, reportTitle) {
	window.opener.doSelReport(reportId, queryId, reportTitle);
	window.close();
}

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
