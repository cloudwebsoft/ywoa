<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String qStr = request.getQueryString();
if (qStr!=null) {
	if (!cn.js.fan.security.AntiXSS.antiXSS(qStr).equals(qStr)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "参数非法！"));
		return;
	}
} 
String priv = "officeequip";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String opType = ParamUtil.get(request, "opType");
if (opType.equals("")) {
	opType = String.valueOf(OfficeOpDb.TYPE_BORROW);
}

int isLike = ParamUtil.getInt(request, "isLike", 1);
String officeCode = ParamUtil.get(request, "officeCode");
String officeName = ParamUtil.get(request, "officeName");
String fromDate = ParamUtil.get(request, "fromDate");
String toDate = ParamUtil.get(request, "toDate");

String sql;
String myname = privilege.getUser(request);
String querystr = "";
sql = "select id from office_equipment_op where 1=1";

sql += " and type = '" + opType + "'" ;

String cond = "";
String person = "";
if (privilege.isUserPrivValid(request, "officeequip")) {
	person = ParamUtil.get(request, "person");
} else {
	person = privilege.getUser(request);
}

if (!fromDate.equals("") && !toDate.equals("")) {
	java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
	d = DateUtil.addDate(d, 1);
	String toDate2 = DateUtil.format(d, "yyyy-MM-dd");
	sql += " and (opDate>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd") + " and opDate<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd") + ")";
}
else if (!fromDate.equals("")) {
	sql += " and opDate>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
}
else if (fromDate.equals("") && !toDate.equals("")) {
	sql += " and opDate<=" + SQLFilter.getDateStr(toDate, "yyyy-MM-dd");
}	

TreeSelectDb tsd = new TreeSelectDb();

if (op.equals("search")) {
	if (isLike == 0) {
		tsd = tsd.getTreeSelectDb(officeCode);
		if (tsd.getChildCount() == 0) {
			sql += " and office_code=" + StrUtil.sqlstr(officeCode);
		} else {
			sql += " and equipment_code in (";
			Vector vt = new Vector();
			vt = tsd.getAllChild(vt, tsd);
			Iterator iter = vt.iterator();
			while (iter.hasNext()) {
				TreeSelectDb t = (TreeSelectDb) iter.next();
				if (t.getChildCount() == 0) {
					sql += StrUtil.sqlstr(t.getCode()) + ",";
				}
			}
			sql = sql.substring(0, sql.length()-1) + ")";
		}
	} else {
		if (!officeName.equals("")) {
			sql += " and office_code in (select code from oa_tree_select where rootCode='office_equipment' and name like " + StrUtil.sqlstr("%" + officeName + "%") + ")";
		}
	}
}
 	
sql += cond;

sql += " order by id desc";

querystr +=  "op=" + op + "&isLike=" + isLike + "&officeCode=" + officeCode + "&officeName=" + StrUtil.UrlEncode(officeName) + "&person=" + person + "&fromDate=" + fromDate + "&toDate=" + toDate;
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
OfficeOpDb ood = new OfficeOpDb();
ListResult lr = ood.listResult(sql, curpage, pagesize);
long total1 = lr.getTotal();
Vector v = lr.getResult();
Iterator ir1 = null;
if (v!=null)
	ir1 = v.iterator();
paginator.init(total1, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>办公用品显示</title>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<script src="../inc/common.js"></script>
	    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<script type="text/javascript" src="../js/flexigrid.js"></script>

	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>

</head>
<style>
	.loading {
		display: none;
		position: fixed;
		z-index: 1801;
		top: 45%;
		left: 45%;
		width: 100%;
		margin: auto;
		height: 100%;
	}

	.SD_overlayBG2 {
		background: #FFFFFF;
		filter: alpha(opacity=20);
		-moz-opacity: 0.20;
		opacity: 0.20;
		z-index: 1500;
	}

	.treeBackground {
		display: none;
		position: absolute;
		top: -2%;
		left: 0%;
		width: 100%;
		margin: auto;
		height: 200%;
		background-color: #EEEEEE;
		z-index: 1800;
		-moz-opacity: 0.8;
		opacity: .80;
		filter: alpha(opacity=80);
	}
</style>
<body>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("del")) {
	ood = new OfficeOpDb();
	ood = ood.getOfficeOpDb(ParamUtil.getInt(request, "id"));
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	if (ood.del()) {
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "officeequip_return_list.jsp?opType=" + opType));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
%>
<script>
<%
if (opType.equals(String.valueOf(OfficeOpDb.TYPE_BORROW))) {
%>
o("menu6").className="current";
<%
}
else if (opType.equals(String.valueOf(OfficeOpDb.TYPE_RECEIVE))) {
%>
o("menu4").className="current";
<%}else{%>
o("menu7").className="current";
<%}%>
</script>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
  		<td align="center">
        <form id="formSearch" action="officeequip_return_list.jsp" method="get">
        &nbsp;<select id="isLike" name="isLike" onchange="changeIsLike()">
        	<option value="0">精确查询</option>
        	<option value="1">模糊查询</option>
        </select>
        &nbsp;&nbsp;
        用品名称&nbsp;
        <%
		tsd = tsd.getTreeSelectDb("office_equipment");
		TreeSelectView tsv = new TreeSelectView(tsd);
		StringBuffer sb = new StringBuffer();
		tsv.getTreeSelectAsOptions(sb, tsd, 1);
	  %>
	<select name="officeCode" id="officeCode">
	<%=sb%>
	</select>&nbsp;&nbsp;
	<input type="text" name="officeName" id="officeName" value="<%=officeName %>" style="display:none" />
	从
   	<input size="8" id="fromDate" name="fromDate" value="<%=fromDate%>" />
	至
    <input size="8" id="toDate" name="toDate" value="<%=toDate%>" />  	
	<input name="op" type="hidden" value="search" />
	<input name="opType" type="hidden" value="<%=opType%>" />
        <input class="tSearch" name="submit" type=submit value="搜索" />
        </form>
      </td>
	</tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
		<thead>
        <tr>
          <th width="250">用品名称</th>
          <th width="100">数量</th>
          <th width="171">用户</th>
          <th width="100">状态</th>
          <th width="171">时间</th>
          <th width="190">备注</th>
          <th width="100">操作员</th>
          <th width="100">操作</th>
        </tr>
    </thead>
        <%
		UserMgr um = new UserMgr();
		OfficeDb od1 = new OfficeDb();
		while (ir1!=null && ir1.hasNext()) {
		  ood = (OfficeOpDb)ir1.next();
		  tsd = new TreeSelectDb(ood.getOfficeCode());
		  UserDb ud = new UserDb(ood.getPerson());
		  
			String realName = "";
			if (!"".equals(ood.getOperator())) {
				UserDb user = um.getUserDb(ood.getOperator());
				realName = user.getRealName();
			}		  
	%>
        <tr>
          <td><%=tsd.getName()%></td>
          <td align='right'><%=ood.getCount()%></td>
          <td align='center'><%=ud.getRealName()%></td>
          <td align="center">
		  <%
		  if (ood.getType()==OfficeOpDb.TYPE_BORROW) {
			out.print("借用");
		  }
		  else if (ood.getType()==OfficeOpDb.TYPE_RECEIVE) {
		  	out.print("领用");
		  }
		  else {
		  	out.print("已还");
		  }
		  %>
          </td>
          <td align="center"><%=ood.getOpDate()%></td>
          <td><%=ood.getRemark()%></td>
          <td><%=realName%></td>
          <td align="center"><a href="#" onClick=" jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return;}else{window.location.href='?op=del&opType=<%=opType%>&id=<%=ood.getId()%>'}}) " style="cursor:pointer">删除</a>&nbsp;&nbsp;
            <%
			if (opType.equals("" + OfficeOpDb.TYPE_BORROW)) {
				if (privilege.isUserPrivValid(request, priv)) { %>
					<a href="javascript:;" onClick="location.href='officeequip_return_show.jsp?id=<%=ood.getId()%>'">归还</a>
				<%}
				else if (ood.getType() == OfficeOpDb.TYPE_BORROW) {
					if (privilege.getUser(request).equals(ood.getPerson())) {%>
					<a href="javascript:;" onClick="addTab('归还办公用品', 'officeequip/officeequip_flow_initiate.jsp?id=<%=ood.getId()%>')">归还</a>
					<%}			
				}
			}%>
          </td>
        </tr>
        <%}%>
</table>
<script>
	$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
			buttons : [
				<%if (opType.equals(String.valueOf(OfficeOpDb.TYPE_BORROW))) {%>			
				{name: '借用', bclass: 'add', onpress : action},
				<%}else if (opType.equals(String.valueOf(OfficeOpDb.TYPE_RECEIVE))) {%>
				{name: '领用', bclass: 'add', onpress : action},
				<%}%>
				{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
			],
		/*
		searchitems : [
			{display: 'ISO', name : 'iso'},
			{display: 'Name', name : 'name', isdefault: true}
			],
		sortname: "iso",
		sortorder: "asc",
		*/
		url: false,
		usepager: true,
		checkbox: false,
		page: <%=curpage%>,
		total: <%=total1%>,
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
	
	$('#fromDate').datetimepicker({
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d'
    });
    $('#toDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
    });		
});

function action(com, grid) {
	if (com=='借用') {
		window.location.href = "officeequip_borrow.jsp";
	}
	else if (com=='领用') {
		window.location.href = "officeequip_receive.jsp";
	}	
}

function changeSort(sortname, sortorder) {
	window.location.href = "officeequip_return_list.jsp?opType=<%=opType%>&<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "officeequip_return_list.jsp?opType=<%=opType%>&<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
	}
}

function rpChange(pageSize) {
	window.location.href = "officeequip_return_list.jsp?opType=<%=opType%>&<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

$(function() {
	$("#isLike").find("option[value='<%=isLike%>']").attr("selected", true);
	$("#officeCode").find("option[value='<%=officeCode%>']").attr("selected", true);
	changeIsLike();
});

function changeIsLike() {
	var index = $('#isLike').val();
	switch (index) {
	case "0":
		$('#officeCode').show();
		$('#officeName').hide();
		break;
	case "1":
		$('#officeCode').hide();
		$('#officeName').show();
		break;
	}
}
</script>
</body>
</html>
