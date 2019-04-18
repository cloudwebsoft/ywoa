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
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="noticeDb" scope="page" class="com.redmoon.oa.notice.NoticeDb"/>
<jsp:useBean id="deptUserDb" scope="page" class="com.redmoon.oa.dept.DeptUserDb"/>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, "paper.receive")) {
    // out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String userName = privilege.getUser(request);
int curpage = ParamUtil.getInt(request, "CPages", 1);
String op = ParamUtil.get(request, "op");
if (op.equals("del")) {

}

Vector ud = new Vector();
ud = deptUserDb.getDeptsOfUser(userName);
Iterator ir = ud.iterator();

String sql="";

String what = ParamUtil.get(request, "what");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

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
DeptUserDb dud = new DeptUserDb();
String[] ary = dud.getUnitsOfUser(userName);

PaperConfig pc = PaperConfig.getInstance();
// 取得收文角色
String swRoles = pc.getProperty("swRoles");
String[] swAry = StrUtil.split(swRoles, ",");
UserDb user = new UserDb();
user = user.getUserDb(userName);
// 检查用户是否为收文角色
int swLen = 0;
if (swAry!=null)
	swLen = swAry.length;
boolean isWithUnit = false;
// 如果没有在配置文件中限定收文角色，则允许有“收文处理”权限的用户，收取分发给单位的文件
if (swLen==0) {
	isWithUnit = true;
}
else {
	for (int i=0; i<swLen; i++) {
		if (user.isUserOfRole(swAry[i])) {
			isWithUnit = true;
			break;
		}
	}
}

PaperDistributeDb pdd = new PaperDistributeDb();
// 如果用户只存在于一个单位（没有兼职单位）
if (ary.length==1) {
	if (isWithUnit) {
		if (!isSearch) {
			sql = "select id from " + pdd.getTable().getName() + " where to_unit=" + StrUtil.sqlstr(myUnitCode) + " or to_unit=" + StrUtil.sqlstr(userName);
		}
		else {
			sql = "select id from " + pdd.getTable().getName() + " where (to_unit=" + StrUtil.sqlstr(myUnitCode) + " or to_unit=" + StrUtil.sqlstr(userName) + ") and title like " + StrUtil.sqlstr("%" + what + "%");
		}
	}
	else {
		if (!isSearch) {
			sql = "select id from " + pdd.getTable().getName() + " where to_unit=" + StrUtil.sqlstr(userName);
		}
		else {
			sql = "select id from " + pdd.getTable().getName() + " where to_unit=" + StrUtil.sqlstr(userName) + " and title like " + StrUtil.sqlstr("%" + what + "%");
		}
	}
}
else {
	if (isWithUnit) {
		String units = StrUtil.sqlstr(userName); // 加上用户自己
		
		for (int i=0; i<ary.length; i++) {
			if (units.equals("")) {
				units = StrUtil.sqlstr(ary[i]);
			}
			else {
				units += "," + StrUtil.sqlstr(ary[i]);
			}
		}
		units = "(" + units + ")";
		 
		if (!isSearch) {
			sql = "select id, count(distinct flow) from " + pdd.getTable().getName() + " where to_unit in " + units + " group by flow";
		}
		else {
			sql = "select id, count(distinct flow) from " + pdd.getTable().getName() + " where to_unit in " + units + " and title like " + StrUtil.sqlstr("%" + what + "%") + " group by flow";
		}
	}
	else {
		if (!isSearch) {
			sql = "select id from " + pdd.getTable().getName() + " where to_unit=" + StrUtil.sqlstr(userName);
		}
		else {
			sql = "select id from " + pdd.getTable().getName() + " where to_unit=" + StrUtil.sqlstr(userName) + " and title like " + StrUtil.sqlstr("%" + what + "%");
		}
	}
}

sql += " order by " + orderBy + " " + sort;
// out.println("swLen=" + swLen + " " + sql);
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>
		<%
			String kind = License.getInstance().getKind();
			if (kind.equalsIgnoreCase(License.KIND_COM)) {
		%>
		<lt:Label res="res.flow.Flow" key="notify"/>
		<%
		} else {
		%>
		<lt:Label res="res.flow.Flow" key="distribute"/>
		<%
			}
		%>
		列表</title>
	<%@ include file="../inc/nocache.jsp" %>
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>

	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<style>
		.unreaded {
			font-weight: bold;
		}
	</style>
</head>
<body>
<%
ListResult lr = pdd.listResult(sql, 1, curpage,pagesize);
Iterator iterator = lr.getResult().iterator();
int total = lr.getTotal();
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
		<form action="paper_received_list.jsp" method="get">
		<input id="op" name="op" value="search" type="hidden" />
        标题
		<input id="what" name="what" size="15" value="<%=what%>" />
		<input class="tSearch" value="搜索" type="submit" />
		</form>
	  </td>
	</tr>
</table>
      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      <thead>
        <tr>
		  <th width="354" name="title">标题</th> 
          <th width="124" name="from_unit">单位</th>
          <th width="90" name="from_unit">用户</th>
          <th width="140" name="dis_date">日期</th>
          <th width="130" align="center">操作</th>
        </tr>
      </thead>
      <tbody>
<%
String disBtnName = "分发";
String kindLic = com.redmoon.oa.kernel.License.getInstance().getKind();
if (kindLic.equalsIgnoreCase(com.redmoon.oa.kernel.License.KIND_COM)) {
	disBtnName = "知会";
}
DeptDb dd = new DeptDb();
WorkflowDb wf = new WorkflowDb();
java.util.Date curDay = DateUtil.parse(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
while(iterator.hasNext()) {
	pdd = (PaperDistributeDb)iterator.next();
	wf = wf.getWorkflowDb(pdd.getInt("flow"));
	user = user.getUserDb(pdd.getString("user_name"));
%>
        <tr id="<%=pdd.getLong("id")%>">
          <td id="tdTitle<%=pdd.getLong("id")%>" <%=pdd.getInt("is_readed")==0?"class='unreaded'":""%>><%=pdd.getString("title")%></td>
		  <td><%=dd.getDeptDb(pdd.getString("from_unit")).getName()%></td>
		  <td><%=user.getRealName()%></td>
          <td align="center"><%=(DateUtil.format(pdd.getDate("dis_date"), "yyyy-MM-dd HH:mm:ss"))%></td>
          <td align="center">
          <!--<a href="javascript:" onclick="addTab('<%=pdd.getString("title")%>', '<%=request.getContextPath()%>/paper/paper_show.jsp?paperId=<%=pdd.getLong("id")%>')">查看</a>-->        
          <%if (pdd.getInt("is_flow_display")==1) {
		        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
		        String desKey = ssoCfg.get("key");
        		String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(wf.getId()));
          %>
          <a href="javascript:;" onclick="$('#tdTitle<%=pdd.getLong("id")%>').removeClass('unreaded');addTab('<%=wf.getTitle()%>', '<%=request.getContextPath()%>/flow_modify.jsp?paperId=<%=pdd.getLong("id")%>&flowId=<%=wf.getId()%>&visitKey=<%=visitKey %>')">查看</a>
          <%}else{%>       
          <a href="javascript:;" onclick="$('#tdTitle<%=pdd.getLong("id")%>').removeClass('unreaded');addTab('<%=wf.getTitle()%>', '<%=request.getContextPath()%>/paper/paper_show.jsp?paperId=<%=pdd.getLong("id")%>')">查看</a>
          <%} %>   
          &nbsp;&nbsp;<a title="发起流程并附加此文件" href="javascript:" onclick="jConfirm('下一步请选择并发起流程！', '提示',function(r){if(r){ addTab('<%=StrUtil.HtmlEncode(pdd.getString("title"))%>', '<%=request.getContextPath()%>/flow_initiate1.jsp?isFromPaperSW=true&paperFlowId=<%=wf.getId()%>&<%=pdd.getInt("is_flow_display")==1?"flowId="+wf.getId()+"":"" %>')}})">处理</a>
          &nbsp;&nbsp;<a href="javascript:distributeDoc(<%=wf.getId()%>,'<%=wf.getTitle()%>',-1,-1, '<%=pdd.getInt("is_flow_display")==1?"true":"false" %>');"><%=disBtnName%></a>
          </td>
        </tr>
<%
}
%>
	</tbody>
</table>  
<%
	String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&cond=" + cond;
	// out.print(paginator.getPageBlock(request,"paper_received_list.jsp?"+querystr));
%>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "paper_received_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "paper_received_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "paper_received_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
$(document).ready(function() {
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
		page: <%=curpage%>,
		total: <%=total%>,
		useRp: true,
		rp: <%=pagesize%>,
		
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

}

function distributeDoc(flowId,title,actionId,myActionId,isFlowDisplay) {
	openWin("paper_distribute.jsp?isFlowDisplay=" + isFlowDisplay + "&flowId=" + flowId + "&actionId=" + actionId + "&myActionId=" + myActionId, 800, 600);
	return;
}
</script>
</body>
</html>