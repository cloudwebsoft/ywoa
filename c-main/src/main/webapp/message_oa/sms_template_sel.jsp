<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@page import="com.redmoon.oa.sms.SMSTemplateMgr"%>
<%@page import="com.redmoon.oa.sms.SMSTemplateDb"%>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.api.IBasicSelectCtl" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "read")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	/*String op = ParamUtil.get(request, "op");
	if (op.equals("delBatch")) {
    try {
		WYSmsMgr dm = new WYSmsMgr();
		dm.delBatch(request);
		out.print(StrUtil.Alert_Redirect("操作成功！", "sms_template_list.jsp"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
   } else if(op.equals("del")){
	String id  = ParamUtil.get(request, "id");
	try {
		WYSmsMgr dm = new WYSmsMgr();
		dm.del(id);
		out.print(StrUtil.Alert_Redirect("操作成功！", "sms_template_list.jsp"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	}*/
		
	String orderBy = ParamUtil.get(request, "orderBy");
	if (orderBy.equals(""))
		orderBy = "create_date";
	String sort = ParamUtil.get(request, "sort");
	if (sort.equals(""))
		sort = "asc";	
	String querystr = "";
	String type = ParamUtil.get(request,"sms_type");
	
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "sms_type", type, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderBy", orderBy, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	int pagesize = ParamUtil.getInt(request, "pageSize", 20);
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	String sql = "";
	if(!type.equals("")){
	sql ="select id from sms_template where type ="+StrUtil.sqlstr(type);	
   }else{
    sql ="select id from sms_template ";	
   }	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>    
    <title>短信模版列表</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "sms_template_sel.jsp?orderBy=" + orderBy + "&sort=" + sort + "&<%=querystr%>";
}
</script>
</head>
<%
	SMSTemplateDb stDb = new SMSTemplateDb();          		
	ListResult lr = stDb.listResult(sql, curpage, pagesize);
	long total = lr.getTotal();
	Iterator ir = lr.getResult().iterator();
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0)
	{
		curpage = 1;
		totalpages = 1;
	}
 %>
  <body>
    <table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="left">  
    <form name="formSearch" action="sms_template_sel.jsp" method="post">
		短信类型
        <%
			MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
			IBasicSelectCtl basicSelectCtl = macroCtlService.getBasicSelectCtl();
			out.print(basicSelectCtl.convertToHtmlCtl(request, "sms_type", "sms_type"));
		%>
        <input name="op" value="search" type="hidden">
        <input name="submit" type=submit value="搜索" class="tSearch">
    </form>
    </td>
  </tr>
</table>
<table border="0" cellspacing="0" id="grid">
	<thead>
	<tr>
		<th width="60">类型</th>
		<th width="490">短信内容</th>
		<th width="60">操作</th>
	</tr>
	</thead>
   <tbody>	
<%
	 int id=0;
	 while(ir.hasNext()){
		stDb = (SMSTemplateDb) ir.next();
		id = stDb.getInt("id");	   
%>
	<tr align="center">
		<td><%=stDb.getString("type")%></td>
        <td><%=stDb.getString("content")%></td>
		<td><a href="javascript:void(0)" onClick="setObj('<%=stDb.getString("content")%>')">选择</a></td>
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
	window.location.href = "wy_sms_list.jsp?pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "wy_sms_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "wy_sms_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
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
}

function setObj(th){
	window.opener.setObj(th);
	window.close();
}

$(function () {
	o("sms_type").value = "<%=type%>";
});
</script>
</html>
