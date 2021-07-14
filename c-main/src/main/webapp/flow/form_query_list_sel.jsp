<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
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
	
	String formCode = ParamUtil.get(request, "formCode");
	
	FormDb fd = new FormDb();

	String op = ParamUtil.get(request, "op");
	FormQueryMgr aqm = new FormQueryMgr();
	
	String isSystem = ParamUtil.get(request, "isSystem");
	if (isSystem.equals(""))
		isSystem = "true";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery.form.js"></script>
</head>
<body>
<%
	if (isSystem.equals("true")) {
		if (!privilege.isUserPrivValid(request, "admin")) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}

	String tableCodes = "";
	String mainFormName = "";
	if (!formCode.equals("")) {
		ModuleRelateDb mrd = new ModuleRelateDb();
		Iterator ir = mrd.getModuleReverseRelated(formCode).iterator();
		while (ir.hasNext()) {
			mrd = (ModuleRelateDb)ir.next();
			String code = mrd.getString("code");
			if (tableCodes.equals(""))
				tableCodes = StrUtil.sqlstr(code);
			else
				tableCodes += "," + StrUtil.sqlstr(code);
				
			fd = fd.getFormDb(code);
			if (mainFormName.equals(""))
				mainFormName = fd.getName();
			else
				mainFormName += "、" + fd.getName();
		}
		
		if (tableCodes.equals("")) {
			%>
			<script>
			alert("没有关联表单可选择！");
			window.close();
			</script>
			<%
			return;	
		}
	}

	String sql, queryKey;
	if (!formCode.equals("")) {
		if (!isSystem.equals("true")) {
			sql = "select id from form_query where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and is_saved=1 and table_code in (" + tableCodes + ")";
			queryKey = ParamUtil.get(request, "queryKey");
			if(op.equals("query")) {
				sql = "select id from form_query where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and query_name like " + StrUtil.sqlstr("%" + queryKey + "%") + " and table_code in (" + tableCodes + ") and is_saved=1";
			}
		}
		else {
			sql = "select id from form_query where is_system=1 and is_saved=1 and table_code in (" + tableCodes + ")";
			queryKey = ParamUtil.get(request, "queryKey");
			if(op.equals("query")) {
				sql = "select id from form_query where is_system=1 and query_name like " + StrUtil.sqlstr("%" + queryKey + "%") + " and table_code in (" + tableCodes + ") and is_saved=1";
			}
		}
	}
	else {
		if (!isSystem.equals("true")) {		
			sql = "select id from form_query where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and is_saved=1";
			queryKey = ParamUtil.get(request, "queryKey");
			if(op.equals("query")) {
				sql = "select id from form_query where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and query_name like " + StrUtil.sqlstr("%" + queryKey + "%") + " and is_saved=1";
			}
		}
		else {
			sql = "select id from form_query where is_system=1 and is_saved=1";
			queryKey = ParamUtil.get(request, "queryKey");
			if(op.equals("query")) {
				sql = "select id from form_query where is_system=1 and query_name like " + StrUtil.sqlstr("%" + queryKey + "%") + " and is_saved=1";
			}
		}
	}
	
	String type = ParamUtil.get(request, "type");
	if (!type.equals("all")) {
		if (type.equals("script"))
			sql += " and is_script=1";
		else // normal 在报表设计器中选用时
			sql +=" and is_script=0";
	}
	
	sql += " order by id desc";
	
	// System.out.println(getClass() + " " + sql);
	
	String querystr = "op=" + op + "&type=" + type + "&queryKey=" + StrUtil.UrlEncode(queryKey) + "&formCode=" + formCode + "&isSystem=" + isSystem;
	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
			
	FormQueryDb aqd = new FormQueryDb();		
	ListResult lr = aqd.listResult(sql, curpage, pagesize);
	long total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = v.iterator();
		
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}

%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">选择查询&nbsp;-&nbsp;<%=mainFormName%></td>
    </tr>
  </tbody>
</table>
	  <form name="queryForm" method="post" action="form_query_list_sel.jsp?op=query&isSystem=<%=isSystem%>">
      <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
          <td><input type="text" name="queryKey" value="<%=queryKey%>" />
          <input name="formCode" value="<%=formCode%>" type="hidden" />
          <input name="type" value="<%=type%>" type="hidden" />
          &nbsp;&nbsp;&nbsp;&nbsp;<input type="submit" class="btn" value="查找" /></td>
          <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
        </tr>
      </table>
	  </form>
	    <table class="tabStyle_1 percent98" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td width="37%" class="tabStyle_1_title" height="24" >查询名称</td>
            <td width="28%" class="tabStyle_1_title" >表单</td>
            <td width="19%" class="tabStyle_1_title" >时间</td>
            <td width="16%" class="tabStyle_1_title">操作</td>
          </tr>
	    <%
		while (ir!=null && ir.hasNext()) {
			aqd = (FormQueryDb)ir.next();
			
			fd = fd.getFormDb(aqd.getTableCode());
		%>
        <tr align="center" >
          <td width="37%" height="22" align="left">
          <span id="queryName<%=aqd.getId()%>">
		  <%
		  String url = "";
		  if (aqd.isScript()) {
			  url = "form_query_script_list_do.jsp?id=" + aqd.getId();
          }else{
			  url = "form_query_list_do.jsp?id=" + aqd.getId();
		  }
		  %>
          <a target="_blank" href="<%=url%>">
		  <%=aqd.getQueryName()%>
          </a></span></td>
          <td width="28%" align="left">
		  <%if (aqd.isScript()) {%>
          脚本
          <%}else{%>
		  <%=fd.getName()%>
          <%}%>
          </td>
          <td width="19%" align="center"><%=DateUtil.format(aqd.getTimePoint(), "yyyy-MM-dd HH:mm:ss")%></td> 
          <td>
          <a href="javascript:;" onClick="sel('<%=aqd.getId()%>', '<%=aqd.getQueryName()%>')">选择</a>
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
			   out.print(paginator.getCurPageBlock("?"+querystr));
			 %>
            &nbsp;</div></td>
        </tr>
      </table>
</body>
<script>
function sel(id, title) {
	window.opener.doSelQuery(id, title);
	window.close();
}
</script>
</html>