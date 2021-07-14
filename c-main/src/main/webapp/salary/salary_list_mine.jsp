<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
String user_name = privilege.getUser(request);
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />		
	<%	
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
String year = ParamUtil.get(request, "year");
//if(year.equals("-请选择-")){

if(!year.equals("")){
	sql="select id from form_table_gzd where unit_code='root' and xm='"+user_name+"' and nf=" +StrUtil.sqlstr(year)+" order by "+ orderBy +" "+ sort; 
}else{
	sql = "select id from form_table_gzd where unit_code='root' and xm='"+user_name+"' order by " + orderBy +" "+ sort;
}
	  
//}else{
    //sql="select id from form_table_gzd where unit_code='root' and xm='"+user_name+"' and nf=" +StrUtil.sqlstr(year)+" order by id desc";  	
//}


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
			out.print(StrUtil.Alert_Redirect("删除成功！", "module_list.jsp?" + querystr + "&CPages=" + curpage));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<!-- 
<%@ include file="salary_inc_menu_top.jsp"%>
<script>
$("menu1").className="current"; 
</script>
-->
<%

com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
long total = lr.getTotal();
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

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
// String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = msd.getColAry(false, "list_field_width");

MacroCtlMgr mm = new MacroCtlMgr();
%>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
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
	&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn" type="button" value="<%=btnNames[i]%>" onClick="<%=StrUtil.HtmlEncode(btnScripts[i])%>" />
	<%
	}
}
%>	  
    </td>
    <td>
    <form id="DateQequest" name="DateQequest" method="post"  action="">
     年份<select name="year" id="year">
              <option value="selectYear">-请选择-</option>
              <option value="2008">2008</option>
              <option value="2009">2009</option>
              <option value="2010">2010</option>
              <option value="2011">2011</option>
              <option value="2012">2012</option>
              <option value="2013">2013</option>
              <option value="2014">2014</option>
              <option value="2015">2015</option>
              <option value="2016">2016</option>
              <option value="2017">2017</option>
              <option value="2018">2018</option>
              <option value="2019">2019</option>
              <option value="2020">2020</option>
              <option value="2021">2021</option>
              <option value="2022">2022</option>
              <option value="2023">2023</option>
              <option value="2024">2024</option>
              <option value="2025">2025</option>
              <option value="2026">2026</option>
              <option value="2027">2027</option>
        </select>
        <input class="tSearch" name="submit" type=submit value="搜索">
    </form></td>
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
    <th width="<%=wid%>" style="cursor:hand" abbr="<%=fieldName%>">
	<%=title%>	
	</th>
<%}%>
    <th width="70" style="cursor:hand">操作</th>
    </tr>
  </thead>
  <tbody>
  <%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
			
		%>
  <tr align="center" id="<%=id%>">
<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>	
		<td align="left">
		
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
				else {%>		
					<%=fdao.getFieldValue(fieldName)%>
				<%}
			}%>
		<%}else{
			String realName = "";
			if (fdao.getCreator()!=null) {
			UserDb user = um.getUserDb(fdao.getCreator());
			if (user!=null)
				realName = user.getRealName();
			}
		%>
		<%=realName%>
		<%}%>
		</td>
	<%}%>
    <td><a href="javascript:;" onclick="addTab('工资单', '<%=request.getContextPath()%>/salary/salary_show_mine.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>')">查看</a></td>
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
	window.location.href = "<%=request.getContextPath()%>/salary/salary_list_mine.jsp?op=<%=op%>&year="+ <%=year%> +"&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/visual/module_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		//{name: '添加', bclass: 'add', onpress : action},
		//{name: '删除', bclass: 'delete', onpress : action},
		//{name: '导出', bclass: 'edit', onpress : action},
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
	checkbox: false,
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
		window.open('exportExcel.do?<%=querystr%>');
	}
	else if (com=="添加") {
		window.location.href = "module_add.jsp?formCode=<%=formCode%>";
	}
	else if (com=='删除') {
		selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			alert('请选择一条记录!');
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
		window.location.href = "module_list.jsp?action=del&<%=querystr%>&CPages=<%=curpage%>&id=" + ids + "&pageSize=" + flex.attr('p').rp;
	}
	
}
window.onload = function(){
    var option = '<%=year%>';
            var selectObj = document.getElementById("year");
            for (var i=0; i < selectObj.options.length; i++){
                 if (selectObj.options[i].value == option) {
                    selectObj.options[i].selected = "selected";
                    break;      
                 }        
            }
}
</script>
</html>
