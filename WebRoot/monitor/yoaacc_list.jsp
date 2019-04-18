<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="java.sql.SQLException"%>
<jsp:useBean id="privilege" scope="page"
	class="com.redmoon.oa.pvg.Privilege" />

<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	System.out.println(formCode);
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "";

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
String sql = ary[0];
String sqlUrlStr = ary[1];

querystr = "op=" + op + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort;
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;
// out.print(sql);

String action = ParamUtil.get(request, "action");
if (action.equals("del")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
		
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (fdm.del(request)) {
			out.print(StrUtil.Alert_Redirect("删除成功！", "yoaacc_list.jsp?" + querystr + "&CPages=" + curpage));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title><%=fd.getName()%>列表</title>
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
		<style type="text/css">
@import url("../util/jscalendar/calendar-win2k-2.css");
</style>

		<script src="../inc/common.js"></script>
		<script type="text/javascript" src="../js/jquery.js"></script>
		<script type="text/javascript" src="../js/flexigrid.js"></script>
		<script type="text/javascript" src="../js/jquery-ui/jquery-ui.js"></script>
		<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
		<script type="text/javascript"
			src="../util/jscalendar/lang/calendar-zh.js"></script>
		<script type="text/javascript"
			src="../util/jscalendar/calendar-setup.js"></script>
	</head>
	<body>
		<%@ include file="yoaacc_inc_menu_top.jsp"%>
		<script>
$("menu1").className="current"; 
</script>
		<%
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
int total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
		
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
String[] fieldsOrder = StrUtil.split(listFieldOrder, ",");

MacroCtlMgr mm = new MacroCtlMgr();
%>
		<table id="searchTable" width="98%" border="0" cellspacing="1"
			cellpadding="3" align="center">
			<tr>
				<td height="23" align="left">
					<%
String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, ",");
int len = 0;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
	%>
					&nbsp;&nbsp;&nbsp;&nbsp;
					<input class="btn" type="button" value="<%=btnNames[i]%>"
						onclick="<%=StrUtil.HtmlEncode(btnScripts[i])%>" />
					<%
	}
}
%> &nbsp;红色：表示已到期
				</td>
			</tr>
		</table>
		<table id="grid" border="0" cellpadding="2" cellspacing="0">
			<thead>
				<tr>
					<%
len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	String title = "创建者";
	if (!fieldName.equals("cws_creator"))
		title = fd.getFieldTitle(fieldName);
	String w = fieldsWidth[i];
	int wid = StrUtil.toInt(w, 50);
	if (w.indexOf("%")==w.length()-1) {
		w = w.substring(0, w.length()-1);
		wid = 800*StrUtil.toInt(w, 20)/100;
	}
%>
					<th width="<%=wid%>" style="cursor: hand" abbr="<%=fieldName%>">
						<%=title%>
					</th>
					<%}%>
					<th width="150" style="cursor: hand">
						操作
					</th>
				</tr>
			</thead>
			<tbody>
				<%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			
			boolean isEprUnit = false;
			String unitEndDateS = fdao.getFieldValue("end_user_date");
			Date unitEndDateD = DateUtil.parse(unitEndDateS,"yyyy-MM-dd");
			Date now = new Date();
			if(unitEndDateD == null) {
				isEprUnit = false;
			} else if(unitEndDateD.getTime() < now.getTime()) {
				isEprUnit = true;
			} else {
				isEprUnit = false;
			}
			String aStyle = "";
			if(isEprUnit) {
				aStyle = " style='color:red' ";
			} else {
				aStyle = "";
			}			
			
			k++;
			long id = fdao.getId();
		%>
				<tr align="center" id="<%=id%>">
					<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>
					<td align="left">
						<a <%=aStyle %>
							href="yoaacc_show.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>">
							<%if (!fieldName.equals("cws_creator")) {
			FormField ff = fd.getFormField(fieldName);
			if (ff==null) {
				out.print(fieldName + " 已不存在！");
			}
			else {
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				}
				else {%> <%=fdao.getFieldValue(fieldName)%> <%}
			}%> <%}else{
			String realName = "";
			if (fdao.getCreator()!=null) {
			UserDb user = um.getUserDb(fdao.getCreator());
			if (user!=null)
				realName = user.getRealName();
			}
		%> <%=realName%> <%}%> </a>
					</td>
					<%}%>
					<td>
						<%if (mpd.canUserManage(privilege.getUser(request))) {%>
						<a
							href="<%=request.getContextPath()%>/monitor/yoaacc_run_stat.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>">运行统计</a> &nbsp;&nbsp;&nbsp;&nbsp;
						<a
							href="<%=request.getContextPath()%>/monitor/yoaacc_edit.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>">编辑</a>
						&nbsp;&nbsp;&nbsp;&nbsp;
						<a onclick="if (!confirm('您确定要删除么？')) event.returnValue=false;"
							href="<%=request.getContextPath()%>/monitor/yoaacc_list.jsp?action=del&op=<%=op%>&id=<%=id%>&formCode=<%=formCode%>&CPages=<%=curpage%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=sqlUrlStr%>">删除</a>
						<%}%>
					</td>
				</tr>
				<%
  }
%>
			</tbody>
		</table>
		
	</body>
	<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "<%=request.getContextPath()%>/monitor/yoaacc_list.jsp?op=<%=op%>&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/monitor/yoaacc_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "<%=request.getContextPath()%>/monitor/yoaacc_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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
		{name: '导出', bclass: 'edit', onpress : action},
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
	checkbox : true,
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

function action(com, grid) {
	if (com=="导出") {
		window.open('yoaacc_excel.jsp?<%=querystr%>');
	}
	else if (com=="添加") {
		window.location.href = "yoaacc_add.jsp?formCode=<%=formCode%>";
	}
	else if (com=='删除') {
		selectedCount = $(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).length;
		if (selectedCount == 0) {
			alert('请选择一条记录!');
			return;
		}
		
		if (!confirm("您确定要删除么？"))
			return;
		
		var ids = "";
		$(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).each(function(i) {
			if (ids=="")
				ids = $(this).val();
			else
				ids += "," + $(this).val();
		});
		window.location.href = "yoaacc_list.jsp?action=del&<%=querystr%>&CPages=<%=curpage%>&id=" + ids + "&pageSize=" + flex.getOptions().rp;
	}
	
}

</script>
</html>
