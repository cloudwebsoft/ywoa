<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="noticeDb" scope="page" class="com.redmoon.oa.notice.NoticeDb"/>
<jsp:useBean id="deptUserDb" scope="page" class="com.redmoon.oa.dept.DeptUserDb"/>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />	
	<%
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
int curpage = ParamUtil.getInt(request, "CPages", 1);
String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	int paperId = ParamUtil.getInt(request, "paperId");
	PaperDistributeDb pdd = new PaperDistributeDb();
	pdd = pdd.getPaperDistributeDb(paperId);
	if (pdd.del()) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "paper_distribute_admin_list.jsp?CPages=" + curpage));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;
}
else if (op.equals("delBatch")) {
	String ids = ParamUtil.get(request, "ids");
	String[] ary = StrUtil.split(ids, ",");
	PaperDistributeDb pdd = new PaperDistributeDb();
	for (int i=0; i<ary.length; i++) {
		pdd = pdd.getPaperDistributeDb(StrUtil.toInt(ary[i]));
		pdd.del();	
	}
	out.print(StrUtil.Alert_Redirect("操作成功！", "paper_distribute_admin_list.jsp?CPages=" + curpage));
	return;
}

Vector ud = new Vector();
ud = deptUserDb.getDeptsOfUser(userName);
Iterator ir = ud.iterator();

String sql="";

String what = ParamUtil.get(request, "what");
String cond = ParamUtil.get(request, "cond");
if (cond.equals(""))
	cond = "title";
if (!cond.equals("title") && !cond.equals("content"))
	return;
	
boolean isSearch = op.equals("search") && !what.equals("");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String myUnitCode = privilege.getUserUnitCode(request);

PaperDistributeDb pdd = new PaperDistributeDb();
if (!isSearch) {
	sql = "select id from " + pdd.getTable().getName();
}
else {
	sql = "select id from " + pdd.getTable().getName() + " where title like " + StrUtil.sqlstr("%" + what + "%");
}

sql += " order by " + orderBy + " " + sort;
// out.print(sql);
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>收文列表</title>
	<%@ include file="../inc/nocache.jsp" %>
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<style>
		.unreaded {
			font-weight: bold;
		}
	</style>
</head>
<body>
<% 
ListResult lr = pdd.listResult(sql,curpage,pagesize);
Iterator iterator = lr.getResult().iterator();
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
	<tr>
		<td width="48%" height="30" align="left">
		<form action="paper_distribute_admin_list.jsp" method="get">
		<input id="op" name="op" value="search" type="hidden" />
        &nbsp;标题
		<input id="what" name="what" size="15" value="<%=what%>" />
		<input class="tSearch" value="搜索" type="submit" />
		</form>
	  </td>
	</tr>
</table>
      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      <thead>
        <tr>
		  <th width="322" name="title" abbr="title">标题</th> 
          <th width="140" abbr="to_unit">收文单位/人员</th>
          <th width="98" abbr="user_name">分发者</th>
          <th width="165" name="flow">流程</th>
          <th width="115" name="dis_date" abbr="dis_date">日期</th>
          <th width="151">操作</th>
        </tr>
      </thead>
      <tbody>
<%
UserDb user = new UserDb();
DeptDb dd = new DeptDb();
WorkflowDb wf = new WorkflowDb();
java.util.Date curDay = DateUtil.parse(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
while(iterator.hasNext()) {
	pdd = (PaperDistributeDb)iterator.next();
	wf = wf.getWorkflowDb(pdd.getInt("flow"));
	user = user.getUserDb(pdd.getString("user_name"));
%>
        <tr id="<%=pdd.getLong("id")%>">
          <td <%=pdd.getInt("is_readed")==0?"class='unreaded'":""%>><%=pdd.getString("title")%></td> 
		  <td>
		  <%if (pdd.getInt("kind")==PaperDistributeDb.KIND_UNIT) {%>
		  <%=dd.getDeptDb(pdd.getString("to_unit")).getName()%>
          <%}else{
		  	UserDb toUser = user.getUserDb(pdd.getString("to_unit"));
			%>
			<%=toUser.getRealName()%>
			<%
		  }%>
          </td>
		  <td><%=user.getRealName()%></td>
          <td>
          <%if (pdd.getInt("is_flow_display")==1) {%>
          <a href="javascript:;" onClick="addTab('<%=wf.getTitle()%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wf.getId()%>')">流程</a>
          <%}%>
          </td>
          <td align="center"><%=(DateUtil.format(pdd.getDate("dis_date"), "yyyy-MM-dd HH:mm"))%></td>
          <td align="center">
          <!--<a href="javascript:" onclick="addTab('<%=pdd.getString("title")%>', '<%=request.getContextPath()%>/paper/paper_show.jsp?paperId=<%=pdd.getLong("id")%>')">查看</a>-->
          <a href="javascript:;" onClick="addTab('<%=wf.getTitle()%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=pdd.getInt("flow")%>')">查看</a>
          &nbsp;&nbsp;
          <%if (pdd.getInt("is_readed")==0) {%>
          <a href="javascript:;" onClick="if (confirm('您确定要回收么？')) window.location.href='<%=request.getContextPath()%>/paper/paper_distribute_list_m.jsp?op=del&paperId=<%=pdd.getLong("id")%>&CPages=<%=curpage%>'">回收</a>
          <%}%>
          </td>
        </tr>
<%
}
%>
	</tbody>
</table>  
<%
	String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&cond=" + cond;
	// out.print(paginator.getPageBlock(request,"paper_received_list_m.jsp.jsp?"+querystr));
%>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "paper_distribute_admin_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "paper_distribute_admin_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "paper_distribute_admin_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
			{name: '删除', bclass: 'delete', onpress : action},
			{separator: true},			
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
		page: <%=curpage%>,
		total: <%=total%>,
		useRp: true,
		rp: <%=pagesize%>,
		checkbox : true,

		// title: "通知",
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
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
	
});

function action(com, grid) {
	if (com=='删除') {
		selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			alert('请选择记录!');
			return;
		}
		if (!confirm("您确定要删除么？"))
			return;
		
		var ids = "";
		$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
			if (ids=="")
				ids = $(this).val();
			else
				ids += "," + $(this).val();
		});			
			
		window.location.href = "paper_distribute_admin_list.jsp?op=delBatch&ids=" + ids + "&CPages=<%=curpage%>";
	}
}
</script>
</body>
</html>