<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.text.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><% 

  	Date now = new Date();
	String op = ParamUtil.get(request, "op");
	String preDate = ParamUtil.get(request, "planDate");
	if(preDate==null||preDate==""){
	   preDate = DateUtil.format(now,"yyyy-MM-dd");
	}
	java.util.Date planDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(preDate);
   
	String orderBy = ParamUtil.get(request, "orderBy");
	String deptCode = ParamUtil.get(request, "deptCode");
	String what = ParamUtil.get(request, "what");	
	if (orderBy.equals(""))
		orderBy = "create_date";
	String sort = ParamUtil.get(request, "sort");
	if (sort.equals(""))
		sort = "asc";
		
	if (!SQLFilter.isValidSqlParam(what)) {
		com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ user_flow_month_list.jsp");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
		return;	
	}
	
		
	if (!"".equals(op)) {
		String op2 = com.cloudwebsoft.framework.security.AntiXSS.antiXSS(op);
		if (!op.equals(op2)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF flow/stat/user_flow_month_list.jsp");
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "op_invalid")));
			return;
		}
		op = op2;
	}
		
	if (!"".equals(orderBy)) {
		String orderBy2 = com.cloudwebsoft.framework.security.AntiXSS.antiXSS(orderBy);
		if (!orderBy.equals(orderBy2)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF flow/stat/user_flow_month_list.jsp");
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "op_invalid")));
			return;
		}
		orderBy = orderBy2;
	}	
		
	if (!"".equals(sort)) {
		String sort2 = com.cloudwebsoft.framework.security.AntiXSS.antiXSS(sort);
		if (!sort.equals(sort2)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF flow/stat/user_flow_month_list.jsp");
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "op_invalid")));
			return;
		}
		sort = sort2;	
	}	
		
	String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&sort=" + sort;
	int pagesize = ParamUtil.getInt(request, "pageSize", 60);
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	String sql = "select name from users where isvalid=1";
	if (op.equals("search")) {
		sql += " and realName like '%" + what+"%'";
	}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户日程列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../../inc/common.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../../js/jquery.js"></script>
<script src="../../js/jquery.form.js"></script>
<script type="text/javascript" src="../../js/flexigrid.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> 
@import url("../../util/jscalendar/calendar-win2k-2.css"); 
</style>
<script>
function selAllCheckBox(checkboxname){
var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
		if(checkboxboxs.checked==false)
		{
		  checkboxboxs.checked = true;
		}else{ checkboxboxs.checked = false;}
			
			
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
		 if(checkboxboxs[i].checked==false)
		{
		  checkboxboxs[i].checked = true;
		}else{ checkboxboxs[i].checked = false;}
		}
	}
}

function getIds() {
    var checkedboxs = 0;
	var checkboxboxs = document.getElementsByName("ids");
	var id = "";
	if (checkboxboxs!=null){
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked){
			   checkedboxs = 1;
			   id = checkboxboxs.value;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].checked){
			   checkedboxs = 1;
			   if (id=="")
				   id = checkboxboxs[i].value;
			   else
				   id += "," + checkboxboxs[i].value;
			}
		}
	}
	return id;
}
</script>
</head>
<body>
<%
	    UserDb userdb = new UserDb();
	    int total = userdb.getUserCount(sql);
		int totalpages;
		Paginator paginator1 = new Paginator(request, total, pagesize);
        //设置当前页数和总页数
	    totalpages = paginator1.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
		
//ModuleSetupDb msd = new ModuleSetupDb();
//MacroCtlMgr mm = new MacroCtlMgr();
%>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0" style=" margin-left:5px;">
  <tr>
    <td width="48%" height="30" align="left"><form name="formSearch" action="user_flow_month_list.jsp" method="get">
      
        </span> &nbsp;
        <input name="op" value="search" type="hidden">
        用户名:&nbsp;<input name="what" size="15" value="" />
		<%
			  int y = ParamUtil.getInt(request, "year", -1);
			  int m = ParamUtil.getInt(request, "month", -1); 
			  String sj="";
			  Calendar c1 = Calendar.getInstance();
			  int year = c1.get(Calendar.YEAR);
			  if(y==-1){
				y=year;
			  }
			  if(m==-1){
				m=c1.get(Calendar.MONTH)+1;
			  }
		%>
		<select name="year" >	
		<%
		  //out.print(y);
		
		  for(int i=0;i<30;i++){
			 if(y == year){
			 %>
			 <option value="<%=year%>" selected="selected"><%=year%></option>
		<%}else{%>
			 <option value="<%=year%>"><%=year%></option>
		<%
		   }year--;
		   }
		%>
		</select>
		<select name="month" onchange="onTypeCodeChange1(this)">
		<%
		for(int i=1;i<=12;i++){
		  if(m==i){
		%>
		<option value="<%=i%>" selected="selected"><%=i%>月</option>
		<%}else{%>
		<option value="<%=i%>"><%=i%>月</option>
		<%}
		}%>
		</select>
		<%
		 
			String date1 = "";
			String date2 = "";
			
		%>
        <input name="submit" type="submit" class="tSearch" value="搜索" />
      </form>
	</td>
  </tr>
</table>
<table id="grid" border="0">
  <thead>
    <tr>
      <th width="128" abbr="realname"><strong>用户名</strong></th>
      <th width="128" abbr="realname"><strong>流程名</strong></th>
      <th width="128" abbr="realname"><strong>流程Id</strong></th>
      <th width="128" ><strong>提交时间</strong></th>
      <th width="180" abbr="create_date"><strong>月份</strong></th>
      <th width="180" abbr="create_date"><strong>是否提交</strong></th>
     
    </tr>
  </thead>
  <tbody>
    <%
	    StringBuffer ss = new StringBuffer();
		ListResult lr = userdb.listResult(sql, curpage, pagesize);
		Vector v = lr.getResult();
		Iterator ir = null;
		if (v!=null)
			ir = v.iterator();
			
		while (ir.hasNext()) {	
			UserDb user = (UserDb)ir.next();
			
    %>
    <tr align="center">
      <td><%=user.getRealName()%></td>
	   <%
	  	 WorkflowDb wf = new WorkflowDb(); 
	     sql = "select id from flow where type_code = 'ygygz' and (year(mydate) = "+y+" and month(mydate) = "+m+" ) and userName="+StrUtil.sqlstr(user.getName())+"";
	  
	     Vector v1 = wf.list(sql);	
		 Iterator ir1 = null;
		 if (v1!=null)
			ir1 = v1.iterator();
		 String flow_name = "";
		 String id = "";
		 String mydate = "";
		 boolean re = false;	
		 while (ir1.hasNext()) {
 	       WorkflowDb wfd = (WorkflowDb)ir1.next(); 
		   flow_name =StrUtil.getLeft(wfd.getTitle(), 40);
		   id = String.valueOf(wfd.getId());
		   mydate = String.valueOf(DateUtil.format(wfd.getMydate(),"yyyy-MM-dd HH:mm:ss"));
		   re = true;
		 }	
	  %>
	  <td><%=flow_name%></td>
	  <td><%=id%></td>
      <td><%=mydate%></td>
	  <td><%=m%></td>
	  <td>
	  <% 
	     if(!re)
		  out.print("否");
		 else{
		  out.print("是");
		 }  
	  %></td>
    </tr>
    <%}%>	
  </tbody>
</table>
</body>
<script>
function doOnToolbarInited() {
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "user_flow_month_list.jsp?<%=querystr%>&pageSize=" + flex.attr('p').rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "user_flow_month_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.attr('p').rp;
}

function rpChange(pageSize) {
	window.location.href = "user_flow_month_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	*/
    url: false,
	usepager: true,
	checkbox : false,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	//title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
	onChangeSort: changeSort,
	
	onChangePage: changePage,
	onRpChange: rpChange,
	onReload: onReload,
	/*
	onRowDblclick: rowDbClick,
	onColSwitch: colSwitch,
	onColResize: colResize,
	onToggleCol: toggleCol,
	*/
	onToolbarInited: doOnToolbarInited,
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {
     if(com=="导出用户")
	{
	 doImport();
	}
	
}
function doImport(){
	<%if (!deptCode.equals("")) {%>
	window.location.href = "user_import_by.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>";
	<%}else{%>
	window.location.href = "user_import_by.jsp?deptCode=" + o("deptCode").value;
	<%}%>
}
</script>
</html>
