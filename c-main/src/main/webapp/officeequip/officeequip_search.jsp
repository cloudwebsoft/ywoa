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
<%@page import="com.redmoon.oa.basic.TreeSelectView"%>
<%@page import="com.redmoon.oa.basic.TreeSelectDb"%>
<%@page import="com.redmoon.oa.person.*"%>
<%@page import="com.redmoon.oa.flow.WorkflowDb"%>
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
if (!privilege.isUserPrivValid(request, "read")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String priv="officeequip"; 
int isLike = ParamUtil.getInt(request, "isLike", 1);
int type = ParamUtil.getInt(request, "type", -1);
String officeCode = ParamUtil.get(request, "officeCode");
if (officeCode.equals("")) {
	officeCode = "office_equipment";
}
String officeName = ParamUtil.get(request, "officeName");
String person = "";
if (privilege.isUserPrivValid(request, priv)) {
	person = ParamUtil.get(request, "person");
} else {
	person = privilege.getUser(request);
}
String personReal = ParamUtil.get(request, "person_real");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String op = ParamUtil.get(request, "op");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "opDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

TreeSelectDb tsd = new TreeSelectDb();

StringBuilder sql = new StringBuilder();
if (true || op.equals("search")) {
	sql.append("select id from office_equipment_op where 1=1");
	if (type != -1) {
		sql.append(" and type=").append(type);
	}
	
	// 办公用品查询
	if (isLike == 0) {
		tsd = tsd.getTreeSelectDb(officeCode);
		if (tsd.getChildCount() == 0) {
			sql.append(" and office_code=").append(StrUtil.sqlstr(officeCode));
		} else {
			sql.append(" and office_code in (");
			Vector vt = new Vector();
			vt = tsd.getAllChild(vt, tsd);
			Iterator iter = vt.iterator();
			while (iter.hasNext()) {
				TreeSelectDb t = (TreeSelectDb) iter.next();
				if (t.getChildCount() == 0) {
					sql.append(StrUtil.sqlstr(t.getCode())).append(",");
				}
			}
			sql.setCharAt(sql.length() - 1, ')');
		}
	} else {
		if (!officeName.equals("")) {
			sql.append(" and office_code in (select code from oa_tree_select where rootCode='office_equipment' and name like ").append(StrUtil.sqlstr("%" + officeName + "%")).append(")");
		}
	}
	
	// 用户名查询
	if (isLike == 0) {
		if (privilege.isUserPrivValid(request, priv)) {
			if (!person.equals("")) {
				sql.append(" and person in (");
				String[] temp = person.split(",");
				for (int i = 0; i < temp.length; i++) {
					sql.append(StrUtil.sqlstr(temp[i])).append(i < temp.length - 1 ? "," : ")");
				}
			}
		} else {
			sql.append(" and person = ").append(StrUtil.sqlstr(person));
		}
	} else {
		if (!personReal.equals("")) {
			sql.append(" and person in (select name from users where realName like ").append(StrUtil.sqlstr("%" + personReal + "%")).append(")");
		}
	}
	if (!beginDate.equals("")) {
		sql.append(" and opDate>=").append(StrUtil.sqlstr(beginDate));
	}
	if (!endDate.equals("")) {
		sql.append(" and opDate<=").append(StrUtil.sqlstr(endDate));
	}
	sql.append(" order by opDate desc");
}

String querystr =  "&type=" + type + "&op=" + op + "&officeCode=" + StrUtil.UrlEncode(officeCode) + "&person=" + StrUtil.UrlEncode(person) + "&beginDate=" + StrUtil.UrlEncode(beginDate) + "&endDate=" + StrUtil.UrlEncode(endDate);

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
OfficeOpDb ood = new OfficeOpDb();
Iterator ir = null;
int totalpages = 0;
long total = 0;

if (!sql.toString().equals("")) {
	ListResult lr = ood.listResult(sql.toString(), curpage, pagesize);
	total = lr.getTotal();
	Vector v = lr.getResult();
	if (v != null)
		ir = v.iterator();
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	totalpages = paginator.getTotalPages();
	if (totalpages == 0) {
		curpage = 1;
		totalpages = 1;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>用品领用查询</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<script>
		$(function () {
			$("#type").find("option[value='<%=type%>']").attr("selected", true);
			$("#isLike").find("option[value='<%=isLike%>']").attr("selected", true);
			$("#officeCode").find("option[value='<%=officeCode%>']").attr("selected", true);
			changeType();
			changeIsLike();
		});

		function setUsers(users, userRealNames) {
			o("person").value = users;
			o("person_real").value = userRealNames;
		}

		function getSelUserNames() {
			return o("person").value;
		}

		function getSelUserRealNames() {
			return o("person_real").value;
		}

		function changeType() {
			var index = $('#type').val();
			var opStr;
			switch (index) {
				case "-1":
					opStr = "操作";
					break;
				case "0":
					opStr = "领用";
					break;
				case "1":
					opStr = "借用";
					break;
				case "2":
					opStr = "归还";
					break;
			}

			$('#oper').html(opStr);
			$('#oper2').html(opStr);
			$('#optime').html(opStr);
		}

		function changeIsLike() {
			var index = $('#isLike').val();
			switch (index) {
				case "0":
					$('#a_person').show();
					$('#person_real').attr("readonly", "readonly");
					$('#officeCode').show();
					$('#officeName').hide();
					break;
				case "1":
					$('#a_person').hide();
					$('#person_real').removeAttr("readonly");
					$('#officeCode').hide();
					$('#officeName').show();
					break;
			}
		}
	</script>
</head>
<body>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<script>
o("menu10").className="current";
</script>
<%

if (op.equals("del")) {
	querystr = "&type=" + type + "&op=search&officeCode=" + StrUtil.UrlEncode(officeCode) + "&person=" + StrUtil.UrlEncode(person) + "&beginDate=" + StrUtil.UrlEncode(beginDate) + "&endDate=" + StrUtil.UrlEncode(endDate);
	int id = ParamUtil.getInt(request, "id");
	ood = ood.getOfficeOpDb(id);
	boolean re = true;
	try {
		re = ood.del();
	} catch (ErrMsgException e) {
		out.print(StrUtil.jAlert("操作失败", "提示"));
		return;
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "officeequip_search.jsp?CPages=" + curpage + "&pageSize=" + pagesize + querystr));
	} else {
		out.print(StrUtil.jAlert("操作失败", "提示"));
	}
	return;
}

%>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
  		<td align="center">
        <form id="form1" name="form1" class="search-form" action="officeequip_search.jsp" method="get">
        &nbsp;<select id="isLike" name="isLike" onChange="changeIsLike()">
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
	<input name="officeName" id="officeName" value="<%=officeName %>" style="display:none" />
   <%if (privilege.isUserPrivValid(request, priv)) {%>
        <span id="oper"></span>人&nbsp;
      <input name="person" type="hidden" id="person" value="<%=person %>">
      <input id="person_real" name="person_real" type="text" size="20" value="<%=StrUtil.getNullStr(personReal) %>" readOnly>
        &nbsp;<a id="a_person" href="#" onClick="javascript:showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')">选择用户</a>
       <%} %>
        &nbsp;&nbsp;开始时间&nbsp;<input id="beginDate" name="beginDate" type="text" size="8" value="<%=beginDate %>" readOnly>
       &nbsp;&nbsp;
       结束时间&nbsp;<input id="endDate" name="endDate" type="text" size="8" value="<%=endDate %>" readOnly>
        <input name="op" type="hidden" value="search" />
        &nbsp;&nbsp;状态&nbsp;
        <select id="type" name="type" value="<%=type %>" onChange="changeType()">
        	<option value="-1">不限</option>
        	<option value="0">领用</option>
        	<option value="1">借用</option>
        	<option value="2">归还</option>
        </select>&nbsp;&nbsp;
        <input class="tSearch" name="submit" type=submit value="搜索">
        </form>
      </td>
	</tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
    <tr>
      <th width=220>办公用品</th>
      <th width=100 >状态</th>
      <th width=100 >数量</th>
      <th width=130 ><span id="oper2" style="display:none"></span>用户</th>
      <th width=130 ><span id="optime"></span>时间</th>
      <th width=130 >归还时间</th>
      <th width=130 >备注</th>
      <th width=100 >操作员</th>
      <th width=120 >操作</th>
    </tr>
    </thead>
      <%
		UserMgr um = new UserMgr();	  
		while (ir != null && ir.hasNext()) {
			OfficeOpDb oopDb = (OfficeOpDb)ir.next();
			TreeSelectDb tsDb = new TreeSelectDb(oopDb.getOfficeCode());
			UserDb ud = new UserDb(oopDb.getPerson());
			String opType = "";
			switch (oopDb.getType()) {
			case OfficeOpDb.TYPE_RECEIVE:
				opType = "领用";
				break;
			case OfficeOpDb.TYPE_BORROW:
				opType = "借用";
				break;
			case OfficeOpDb.TYPE_RETURN:
				opType = "归还";
				break;
			}
			String realName = "";
			if (!"".equals(oopDb.getOperator())) {
				UserDb user = um.getUserDb(oopDb.getOperator());
				realName = user.getRealName();
			}			
	%>
	 <tr>
      <td align='left'><%=tsDb.getName()%></td>
      <td align="center"><%=opType%></td>
      <td align='right'><%=oopDb.getCount()%></td>
      <td align="center"><%=StrUtil.getNullStr(ud.getRealName())%></td>
      <td align="center"><%=DateUtil.format(oopDb.getOpDate(), "yyyy-MM-dd")%></td>
      <td align="center"><%=oopDb.getType() == OfficeOpDb.TYPE_RECEIVE ? "----" : DateUtil.format(oopDb.getReturnDate(), "yyyy-MM-dd")%></td>
      <td><%=StrUtil.getNullStr(oopDb.getRemark())%></td>
      <td><%=realName%></td>
      <td align='center'>
      <%if (privilege.isUserPrivValid(request, priv)) { %>
      <a href="#" onClick="location.href='officeequip_search.jsp?op=del&id=<%=oopDb.getId()%>&type=<%=type %>&op=<%=op %>&officeCode=<%=StrUtil.UrlEncode(officeCode) %>&person=<%=StrUtil.UrlEncode(person) %>&beginDate=<%=StrUtil.UrlEncode(beginDate) %>&endDate=<%=StrUtil.UrlEncode(endDate) %>'">删除</a>
      <%} %>
      <%if (oopDb.getFlowid() != 0) {
      	WorkflowDb wfDb = new WorkflowDb(oopDb.getFlowid());
      %>
      	&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfDb.getTitle())%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfDb.getId()%>')" title="<%=wfDb.getTitle()%>">流程</a>
      <%} %>
      <%if (oopDb.getType() == OfficeOpDb.TYPE_BORROW) {
    	  	if (privilege.getUser(request).equals(oopDb.getPerson())) {%>
      			&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('归还办公用品', 'officeequip/officeequip_flow_initiate.jsp?id=<%=oopDb.getId()%>')">归还</a>
      		<%} else if (privilege.isUserPrivValid(request, priv)) { %>
      			&nbsp;&nbsp;<a href="#" onClick="location.href='officeequip_return_show.jsp?id=<%=oopDb.getId()%>'">归还</a>
      		<%}
   	  }%>
      <%if (oopDb.getType() == OfficeOpDb.TYPE_BORROW && privilege.isUserPrivValid(request, priv) && !privilege.getUser(request).equals(oopDb.getPerson())) {%>
     		&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('催还办公用品', 'message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(oopDb.getPerson())%>&title=<%=StrUtil.UrlEncode("请" + ud.getRealName() + "归还" + tsDb.getName())%>')">催还</a>
      <%}%>
      </td>
	 </tr>
    <%}%>
</table>
</body>
<script>
function initCalendar() {
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d',
    });
    $('#endDate').datetimepicker({
        lang:'ch',
        timepicker:false,
        format:'Y-m-d',
        formatDate:'Y/m/d',
    });
}

function doOnToolbarInited() {
	initCalendar();
}

$(function(){
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
			
			// title: "通知",
			singleSelect: true,
			resizable: false,
			showTableToggleBtn: true,
			showToggleBtn: false,
			
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
});
function changeSort(sortname, sortorder) {
	window.location.href = "officeequip_search.jsp?pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder+"<%=querystr%>";
}

function changePage(newp) {
	if (newp){
		window.location.href = "officeequip_search.jsp?CPages=" + newp + "&pageSize=" + flex.getOptions().rp+"<%=querystr%>";
	}
}

function rpChange(pageSize) {
	window.location.href = "officeequip_search.jsp?CPages=<%=curpage%>&pageSize=" + pageSize+"<%=querystr%>";
}

function onReload() {
	window.location.reload();
}
</script>
</html>
