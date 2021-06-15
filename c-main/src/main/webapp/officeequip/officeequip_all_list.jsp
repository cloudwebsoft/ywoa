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
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectDb"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectView"%>
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
	if (!privilege.isUserPrivValid(request, priv)) {
		if (!privilege.isUserPrivValid(request, "read")) {
			%>
			<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
			<%		
			out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		} else {
			response.sendRedirect("officeequip_search.jsp");
			return;
		}
	}
	
	String myname = privilege.getUser(request);
	String myUnitCode = privilege.getUserUnitCode(request);
	StringBuilder sql = new StringBuilder();
	String querystr = "";
	String op = ParamUtil.get(request, "op");
	String officeCode = ParamUtil.get(request, "officeCode");
	String officeName = ParamUtil.get(request, "officeName");
	int isLike = ParamUtil.getInt(request, "isLike", 1);
	String equation1 = ParamUtil.get(request, "equation1");
	if (equation1.equals("")) {
		equation1 = ">=";
	}
	int storageCount = ParamUtil.getInt(request, "storageCount", 0);
	String equation2 = ParamUtil.get(request, "equation2");
	if (equation2.equals("")) {
		equation2 = "=";
	}
	double price = ParamUtil.getDouble(request, "price", 0.0);
	String unitCode = ParamUtil.get(request, "unitCode");
	
	int tookStock = ParamUtil.getInt(request, "tookStock", 0); // 0表示库存，1表示盘点
	
	TreeSelectDb tsd = new TreeSelectDb();
	if (!op.equals("search")) {
		sql.append("select id from office_stocktaking where is_tookstock=" + tookStock);
		unitCode = myUnitCode;
	} else {
		sql.append("select id from office_stocktaking where is_tookstock=" + tookStock);
		if (isLike == 0) {
			tsd = tsd.getTreeSelectDb(officeCode);
			if (tsd.getChildCount() == 0) {
				sql.append(" and equipment_code=").append(StrUtil.sqlstr(officeCode));
			} else {
				sql.append(" and equipment_code in (");
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
				sql.append(" and equipment_code in (select code from oa_tree_select where rootCode='office_equipment' and name like ").append(StrUtil.sqlstr("%" + officeName + "%")).append(")");
			}
		}
		if (storageCount != 0) {
			sql.append(" and stock_num").append(equation1).append(storageCount);
		}
	}
	String orderBy = ParamUtil.get(request, "orderBy");
	if (orderBy.equals(""))
		orderBy = "id";
	String sort = ParamUtil.get(request, "sort");
	if (sort.equals(""))
		sort = "desc";
	sql.append( " order by ");
	sql.append(orderBy);
	sql.append( " ");
	sql.append(sort);
		
	querystr += "&tookStock=" + tookStock + "&op=" + op + "&officeCode=" + StrUtil.UrlEncode(officeCode) + "&officeName=" + StrUtil.UrlEncode(officeName) + "&equation1=" + equation1 + "&equation2=" + equation2 + "&price=" + price + "&storageCount=" + storageCount + "&unitCode=" + StrUtil.UrlEncode(unitCode);
	int pagesize = ParamUtil.getInt(request, "pageSize", 20); 
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	OfficeStocktakingDb od = new OfficeStocktakingDb();
	ListResult lr = od.listResult(sql.toString(), curpage, pagesize);
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
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>办公用品</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
	<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
	<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
	<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
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
	<script>
		$(function () {
			$("#isLike").find("option[value='<%=isLike%>']").attr("selected", true);
			$("#officeCode").find("option[value='<%=officeCode%>']").attr("selected", true);
			$("#equation1").find("option[value='<%=equation1%>']").attr("selected", true);
			$("#equation2").find("option[value='<%=equation2%>']").attr("selected", true);
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
</head>
<body>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<script>
<%if (tookStock==0) {%>
o("menu1").className="current";
<%}else{%>
o("menu9").className="current";
<%}%>
</script>
<%
String op1 = ParamUtil.get(request, "op");
if (op1.equals("del")) {
	OfficeMgr om = new OfficeMgr();
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = om.del(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "officeequip_all_list.jsp"));
	}
	return;
} else if (op1.equals("delbatch")) {
	OfficeMgr om = new OfficeMgr();
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = om.delBatch(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "officeequip_all_list.jsp"));
	}
	return;
}
%>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
  		<td align="center">
        <form id="formSearch" action="officeequip_all_list.jsp" method="get">
        &nbsp;<select id="isLike" name="isLike" onChange="changeIsLike()">
        	<option value="0">精确查询</option>
        	<option value="1">模糊查询</option>
        </select>
        &nbsp;
        用品名称
        <%
		tsd = tsd.getTreeSelectDb("office_equipment");
		TreeSelectView tsv = new TreeSelectView(tsd);
		StringBuffer sb = new StringBuffer();
		tsv.getTreeSelectAsOptions(sb, tsd, 1);
	  %>
	<select name="officeCode" id="officeCode" style="display:none">
	<%=sb%>
	</select>&nbsp;&nbsp;
	<input type="text" name="officeName" id="officeName" value="<%=officeName %>" />
        库存
        <select id="equation1" name="equation1">
        	<option value="&gt;">></option>
        	<option value="&gt;=">>=</option>
        	<option value="=">=</option>
        	<option value="&lt;="><=</option>
        	<option value="&lt;"><</option>
        </select>
        <input type="text" name="storageCount" size="10" value="<%=storageCount == 0 ? "" : storageCount + "" %>" />
        <input name="op" type="hidden" value="search" />
        <input class="tSearch" name="submit" type=submit value="搜索" />
        </form>
      </td>
	</tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
<thead>
	<tr>
		<th width="220">用品名称</th>
		<th width="109">库存</th>
		<th width="114">单位</th>
		<th width="93">价格</th>
		<th width="110">供应商</th>
		<th width="139">最后入库时间</th>
		<th width="139">备注</th>
		<th width="150">操作</th>
	</tr>
    </thead>
<%
	// OfficeTypeDb otdb = new OfficeTypeDb();
	int equipmentId, typeId;
	while (ir1!=null && ir1.hasNext()) {
		od = (OfficeStocktakingDb)ir1.next();
		if (od==null) {
			continue;
		}
		TreeSelectDb ts = new TreeSelectDb(od.getEquipmentCode());
		OfficeDb ofd = new OfficeDb();
		ofd = ofd.getOfficeDbLastAdded(od.getEquipmentCode());
		String mu = "", buyPerson = "", buyDate = "", abstracts = "";
		price = 0.0;
		if (ofd!=null) {
			mu = StrUtil.getNullString(ofd.getMeasureUnit());
			price = ofd.getPrice();
			buyPerson = ofd.getBuyPerson();
			buyDate = DateUtil.format(ofd.getBuyDate(), "yyyy-MM-dd");
			abstracts = StrUtil.getNullString(ofd.getAbstracts());
		}
%>
	<tr id="<%=od.getId()%>">
		<td><a href="officeequip_add_list.jsp?op=search&officeCode=<%=od.getEquipmentCode()%>"><%=ts.getName()%></a></td>
		<td align="right"><%=od.getStockNum()%></td>
		<td align="center"><%=mu%></td>
		<td align="right"><%=price%></td>
		<td align="right"><%=buyPerson%></td>
		<td align="center"><%=buyDate%></td>
		<td><%=abstracts%></td>
		<td>
        <a href="officeequip_add.jsp?officeName=<%=od.getEquipmentCode()%>">入库</a>
        &nbsp;
        <a href="officeequip_receive.jsp?officeName=<%=od.getEquipmentCode()%>">领用</a>
        &nbsp;
        <a href="officeequip_borrow.jsp?officeName=<%=od.getEquipmentCode()%>">借用</a>
        <%if (tookStock==1) {%>
        &nbsp;
        <a href="officeequip_tookstock.jsp?officeName=<%=od.getEquipmentCode()%>">盘点</a>
        <%}%>
        </td>
	</tr>
<%}%>
</table>
</body>
<script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
				<%
				if (tookStock==1) {
				%>
				{name: '盘点', bclass: 'add', onpress : action},
				<%}%>
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
			checkbox : true,
			page: <%=curpage%>,
			total: <%=total1%>,
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

			autoHeight: true,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 84
			}
		);
});
function changeSort(sortname, sortorder) {
	window.location.href = "officeequip_all_list.jsp?pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder+"<%=querystr%>";
}

function changePage(newp) {
	if (newp){
		window.location.href = "officeequip_all_list.jsp?CPages=" + newp + "&pageSize=" + flex.getOptions().rp+"<%=querystr%>";
	}
}

function rpChange(pageSize) {
	window.location.href = "officeequip_all_list.jsp?CPages=<%=curpage%>&pageSize=" + pageSize+"<%=querystr%>";
}

function onReload() {
	window.location.reload();
}

function action(com, grid) {
	if (com=="盘点") {
		window.location.href = "officeequip_tookstock.jsp";		
	}
}
</script>
</html>
