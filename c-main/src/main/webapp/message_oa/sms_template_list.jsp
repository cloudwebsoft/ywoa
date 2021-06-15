<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@page import="com.redmoon.oa.sms.SMSTemplateMgr"%>
<%@page import="com.redmoon.oa.sms.SMSTemplateDb"%>
<jsp:useBean id="privilege" scope="page"
	class="com.redmoon.oa.pvg.Privilege" />
<%
	String priv="admin";
	if (!privilege.isUserPrivValid(request,priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();

	String op = ParamUtil.get(request, "op");
	if (op.equals("delBatch")) {
		try {
			SMSTemplateMgr stMgr = new SMSTemplateMgr();
			stMgr.delBatch(request);
			out.print(StrUtil.Alert_Redirect("操作成功！",
					"sms_template_list.jsp"));
		} catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
	} else if (op.equals("del")) {
		String id = ParamUtil.get(request, "id");
		try {
			SMSTemplateMgr stMgr = new SMSTemplateMgr();
			stMgr.del(id);
			out.print(StrUtil.Alert_Redirect("操作成功！",
					"sms_template_list.jsp?CPages=" + curpage));
		} catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
	}

	String orderBy = ParamUtil.get(request, "orderBy");
	if (orderBy.equals(""))
		orderBy = "create_date";
	String sort = ParamUtil.get(request, "sort");
	if (sort.equals(""))
		sort = "asc";
	String querystr = "";
	String type = ParamUtil.get(request, "sms_type");
	int pagesize = ParamUtil.getInt(request, "pageSize", 20);
	String sql = "";
	if (!type.equals("")) {
		sql = "select id from sms_template where type ="
				+ StrUtil.sqlstr(type);
	} else {
		sql = "select id from sms_template";
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
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
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
			
	window.location.href = "sms_template_list.jsp?op=<%=op%>&orderBy=" + orderBy + "&sort=" + sort + "&<%=querystr%>";
}

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

var curObjId;
function selectNode(code, name) {
	$(curObjId).value = code;
	$(curObjId + "Desc").value = name;
}
</script>
	</head>
	<%!public String convertToHTMLCtl(HttpServletRequest request, String code) {
		StringBuffer str = new StringBuffer();
		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect(code);
		str.append("<select name='" + sd.getCode() + "' id = '" + sd.getCode()
				+ "'>");
		if (sd.getType() == SelectDb.TYPE_LIST) {
			Vector v = sd.getOptions(new JdbcTemplate());
			Iterator ir = v.iterator();
			while (ir.hasNext()) {
				SelectOptionDb sod = (SelectOptionDb) ir.next();
				String selected = "";
				if (sod.isDefault())
					selected = "selected";
				str.append("<option value='" + sod.getValue() + "' " + selected
						+ ">" + sod.getName() + "</option>");
			}
		} else {
			TreeSelectDb tsd = new TreeSelectDb();
			tsd = tsd.getTreeSelectDb(sd.getCode());
			TreeSelectView tsv = new TreeSelectView(tsd);
			StringBuffer sb = new StringBuffer();
			try {
				str.append(tsv.getTreeSelectAsOptions(sb, tsd, 1));
			} catch (ErrMsgException e) {
				e.printStackTrace();
			}
		}
		str.append("</select>");
		return str.toString();
	}%>
	<%
		SMSTemplateDb stDb = new SMSTemplateDb();
		ListResult lr = stDb.listResult(sql, curpage, pagesize);
		long total = lr.getTotal();
		Iterator ir = lr.getResult().iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages == 0) {
			curpage = 1;
			totalpages = 1;
		}
	%>

	<body>
		<table id="searchTable" width="98%" border="0" cellspacing="1"
			cellpadding="3" align="center">
			<tr>
				<td height="23" align="left">
					<form name="formSearch" action="sms_template_list.jsp"
						"" method="post">
						&nbsp;&nbsp;类型
<%
						out.print(convertToHTMLCtl(request, "sms_type"));
					%>
						<script>
		o("sms_type").value = "<%=type%>";
		</script>
						<input name="op" value="search" type="hidden">
						<input name="submit" type=submit value="搜索" class="tSearch">
					</form>
				</td>
			</tr>
		</table>
		<table id="grid" border="0">
			<thead>
				<tr>
					<th width="30">
						<input name="idsa" type="checkbox" onClick="selAllCheckBox('ids')">
					</th>
					<th width="128">
						类型
					</th>
					<th width="720">
						短信内容
					</th>
					<th width="316">
						操作
					</th>
				</tr>
			</thead>
			<tbody>
				<%
					int id = 0;
					while (ir.hasNext()) {
						stDb = (SMSTemplateDb) ir.next();
						id = stDb.getInt("id");
				%>
				<tr align="center">
					<td>
						<input type="checkbox" name="ids" value="<%=id%>" />
					</td>
					<td><%=stDb.getString("type")%></td>
					<td><%=stDb.getString("content")%></td>
					<td>
						<a href="sms_template_edit.jsp?id=<%=id%>">修改</a>&nbsp;&nbsp;&nbsp;&nbsp;
						<a onclick="del(<%=id%>)" href="javascript:void(0)">删除</a>
					</td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
	</body>
	<script>
function doOnToolbarInited() {
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "sms_template_list.jsp?pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "sms_template_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "sms_template_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '添加', bclass: 'add', onpress : action},
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
	if (com=="删除") {
		doDel();
	}else if(com=="添加"){
	  doAdd();
	}
}

function doDel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择模板！");
		return;
	}
	if(confirm("您确定要删除吗？"))	{
		window.location.href = "?op=delBatch&ids=" + ids;
	}
}
function doAdd() {
  window.location.href = "sms_template_add.jsp";
}
function del(id) {
	if(confirm("您确定要删除吗？")) {
	  window.location.href = "sms_template_list.jsp?op=del&id="+id + "&CPages=<%=curpage%>";
	}
}

$(document).ready(function(){
    $('#type').change(function(){
        selchange();
    });
})
</script>
</html>
