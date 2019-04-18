<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
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

Vector ud = new Vector();
ud = deptUserDb.getDeptsOfUser(userName);
Iterator ir = ud.iterator();

String sql="";

int flowId = ParamUtil.getInt(request, "flowId");

String action = ParamUtil.get(request, "action");
String what = ParamUtil.get(request, "what");
String type = ParamUtil.get(request, "type");
int status = ParamUtil.getInt(request, "status", -1);

String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>收文列表</title>
<%@ include file="../inc/nocache.jsp"%>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<style>
.unreaded {
	font-weight:bold;
}
</style>
</head>
<body>
<%@ include file="../flow_modify_inc_menu_top.jsp"%>
<script>
o("menu7").className="current"; 
</script>
<%
if (op.equals("del")) {
	int paperId = ParamUtil.getInt(request, "paperId");
	PaperDistributeDb pdd = new PaperDistributeDb();
	pdd = pdd.getPaperDistributeDb(paperId);
	
    if (pdd.getInt("is_readed")==0 && (pdd.getString("user_name").equals(privilege.getUser(request)) || privilege.isUserPrivValid(request, "paper.receives"))) {
	}
	else {
    	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String myUnitCode = privilege.getUserUnitCode(request);
	if (!myUnitCode.equals(pdd.getString("from_unit"))) {
		%>
		<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />	
		<%
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	if (pdd.del()) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "paper_distribute_flow_list.jsp?flowId=" + flowId + "&CPages=" + curpage + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&type=" + type + "&status=" + status));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("delBatch")) {
	String ids = ParamUtil.get(request, "ids");
	String[] ary = StrUtil.split(ids, ",");
	if (ary==null) {
		out.print(StrUtil.jAlert_Back("请选择记录！","提示"));
		return;
	}
	for (int i=0; i<ary.length; i++) {
		int paperId = StrUtil.toInt(ary[i]);
		PaperDistributeDb pdd = new PaperDistributeDb();
		pdd = pdd.getPaperDistributeDb(paperId);
		
	    if (pdd.getInt("is_readed")==0 && (pdd.getString("user_name").equals(privilege.getUser(request)) || privilege.isUserPrivValid(request, "paper.receives"))) {
		}
		else {
	    	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}		
		
		String myUnitCode = privilege.getUserUnitCode(request);
		if (!myUnitCode.equals(pdd.getString("from_unit"))) {
			%>
			<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />	
			<%
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		// 未看过的才能被收回
		if (pdd.getInt("is_readed")==0) {
			pdd.del();
		}
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "paper_distribute_flow_list.jsp?flowId=" + flowId + "&CPages=" + curpage + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&type=" + type + "&status=" + status));
	return;
}

PaperDistributeDb pdd = new PaperDistributeDb();

String orderBy = ParamUtil.get(request, "orderBy");
String sort = ParamUtil.get(request, "sort");
if (orderBy.equals("")) {
	orderBy = "id";
	sort = "desc";
}

// System.out.print(sql);
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
sql = "select id from " + pdd.getTable().getName() + " p where flow=" + flowId;

if (action.equals("search")) {
	if (type.equals("unit")) {
		sql = "select p.id from " + pdd.getTable().getName() + " p, department d where p.to_unit=d.code and d.name like " + StrUtil.sqlstr("%" + what + "%") + " and flow=" + flowId + " and kind=" + PaperDistributeDb.KIND_UNIT;
	}
	else if (type.equals("user")) {
		sql = "select p.id from " + pdd.getTable().getName() + " p, users u where p.to_unit=u.name and u.realname like " + StrUtil.sqlstr("%" + what + "%") + " and flow=" + flowId + " and kind=" + PaperDistributeDb.KIND_USER;
	}
	
	if (status!=-1) {
		sql += " and p.is_readed=" + status;
	}
}

sql += " order by " + orderBy + " " + sort;

// out.println(getClass() + " " + sql);
ListResult lr = pdd.listResult(sql, curpage, pagesize);
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
		<form action="paper_distribute_flow_list.jsp" method="get">
		<input id="action" name="action" value="search" type="hidden" />
		<input id="flowId" name="flowId" value="<%=flowId%>" type="hidden" />
        <input name="pageSize" value="<%=pagesize%>" type="hidden" />
        &nbsp;<lt:Label res="res.flow.Flow" key="state"/>
        <select id="status" name="status">
        <option value="-1"><lt:Label res="res.flow.Flow" key="limited"/></option>
        <option value="0"><lt:Label res="res.flow.Flow" key="uncollected"/></option>
        <option value="1"><lt:Label res="res.flow.Flow" key="received"/></option>
        </select>        
        &nbsp;&nbsp;
        <select id="type" name="type">
        <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
        <option value="unit"><lt:Label res="res.flow.Flow" key="unit"/></option>
        <option value="user"><lt:Label res="res.flow.Flow" key="personal"/></option>
        </select>
        <lt:Label res="res.flow.Flow" key="tit"/>
		<input id="what" name="what" size="15" value="<%=what%>" />
		<input class="tSearch" value='<lt:Label res="res.flow.Flow" key="search"/>' type="submit" />
		</form>
        <script>
		$(function() {
			o("type").value = "<%=type%>";
		});
		</script>
	  </td>
	</tr>
</table>
      <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
      <thead>
        <tr>
		  <th width="220" name="from_unit"><lt:Label res="res.flow.Flow" key="receivedUnits"/></th>
          <th width="262" name="is_readed" abbr="is_readed"><lt:Label res="res.flow.Flow" key="state"/></th>
          <th width="219" name="dis_date" abbr="dis_date"><lt:Label res="res.flow.Flow" key="receiptDate"/></th>
          <th width="267"><lt:Label res="res.flow.Flow" key="operate"/></th>
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
	String pid = String.valueOf(pdd.getLong("id"));
	if (pdd.getInt("is_readed")==1) {
		pid = "-1";
	}
%>
        <tr id="<%=pid%>">
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
          <td>
          <%=pdd.getInt("is_readed")==1?"<lt:Label res='res.flow.Flow' key='received'/>":"<font color='red'><lt:Label res='res.flow.Flow' key='uncollected'/></font>"%>
          </td>
          <td align="center"><%=(DateUtil.format(pdd.getDate("read_date"), "yyyy-MM-dd HH:mm"))%></td>
          <td align="center"><!--<a href="javascript:" onclick="addTab('<%=pdd.getString("title")%>', '<%=request.getContextPath()%>/paper/paper_show.jsp?paperId=<%=pdd.getLong("id")%>')">查看</a>-->
            <%if (pdd.getInt("is_readed")==0 && (pdd.getString("user_name").equals(privilege.getUser(request)) || privilege.isUserPrivValid(request, "paper.receives"))) {%>
            <a href="javascript:;" onclick="jConfirm('<lt:Label res="res.flow.Flow" key="isRecycle"/>','提示',function(r){if(!r){return;}else{window.location.href='<%=request.getContextPath()%>/paper/paper_distribute_flow_list.jsp?op=del&flowId=<%=flowId%>&paperId=<%=pdd.getLong("id")%>&CPages=<%=curpage%>&action=<%=action%>&what=<%=StrUtil.UrlEncode(what)%>&type=<%=type%>&status=<%=status%>'}}) "><lt:Label res="res.flow.Flow" key="recover"/></a>
            <%}%></td>
        </tr>
<%
}
%>
	</tbody>
</table>  
<%
	String querystr = "op=" + op + "&flowId=" + flowId + "&action=" + action + "&type=" + type + "&what=" + StrUtil.UrlEncode(what) + "&status=" + status;
	// out.print(paginator.getPageBlock(request,"paper_distribute_flow_list.jsp?"+querystr));
%>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "paper_distribute_flow_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "paper_distribute_flow_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>&sort=<%=sort%>";
}

function rpChange(pageSize) {
	window.location.href = "paper_distribute_flow_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
			{name: '<lt:Label res="res.flow.Flow" key="recover"/>', bclass: 'delete', onpress : action},
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
		
		// title: "通知",
		singleSelect: true,
		resizable: false,
		showTableToggleBtn: true,
		showToggleBtn: true,
		
		onChangeSort: changeSort,
		checkbox: true,
		
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

	o("status").value = "<%=status%>";
	
	// 去掉已查看的公文的checkbox
	$(".cth input[type='checkbox'][value='-1']", grid.bDiv).each(function(i) {
			$(this).hide();
		});
	
});

function action(com, grid) {
	if (com=='<lt:Label res="res.flow.Flow" key="recover"/>') {		
		selectedCount = $(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('<lt:Label res="res.flow.Flow" key="selectRecord"/>','提示');
			return;
		}
		jConfirm('<lt:Label res="res.flow.Flow" key="isRecycle"/>','提示',function(r){
			if(!r){return;}
			else{
				var ids = "";
				$(".cth input[type='checkbox'][value!='on'][value!='-1'][checked=true]", grid.bDiv).each(function(i) {
					if (ids=="")
						ids = $(this).val();
					else
						ids += "," + $(this).val();
				});	
				window.location.href='paper_distribute_flow_list.jsp?op=delBatch&flowId=<%=flowId%>&CPages=<%=curpage%>&action=<%=action%>&what=<%=StrUtil.UrlEncode(what)%>&type=<%=type%>&status=<%=status%>&ids=' + ids;
			}
		})
	}
}
</script>
</body>
</html>