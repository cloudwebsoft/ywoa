<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");
String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
String menuItem = ParamUtil.get(request, "menuItem");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCodeRelated);

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}

// 置页面类型
// request.setAttribute("pageType", "list");

String relateFieldValue = "";
// parentId为客户的ID
int parentId = ParamUtil.getInt(request, "parentId", -1);
if (parentId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
	return;
}
else {
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
	relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
	if (relateFieldValue==null) {
		out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
		return;
	}
}

String op = ParamUtil.get(request, "op");
// 判断有无浏览的权限
if (!SalePrivilege.canUserSeeCustomer(request, parentId)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

ModuleSetupDb parentMsd = new ModuleSetupDb();
parentMsd = parentMsd.getModuleSetupDbOrInit(formCode);

ModuleRelateDb mrd = new ModuleRelateDb();
Iterator ir = mrd.getModulesRelated(formCode).iterator();
while (ir.hasNext()) {
	mrd = (ModuleRelateDb)ir.next();
	String code = mrd.getString("relate_code");
	if (code.equals(formCodeRelated)) {
		if (mrd.getInt("relate_type") == ModuleRelateDb.TYPE_SINGLE) {
			// 获取与formCode关联的表单型（单条记录）的ID
			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
			fdao = fdao.getFormDAOOfRelate(fd, relateFieldValue);
			if (fdao!=null) {
				long id = fdao.getId();			
				response.sendRedirect("module_show_relate.jsp?id=" + id + "&parentId=" + parentId + "&code=" + formCodeRelated + "&parentModuleCode=" + formCode);
				return;
			}
		}
	}
	
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "";
int pagesize = ParamUtil.getInt(request, "pagesize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
String sql = arySQL[0];
String sqlUrlStr = arySQL[1];

querystr = "customerId=" + parentId + "&op=" + op + "&menuItem=" + menuItem + "&formCode=" + formCode + "&formCodeRelated=" + formCodeRelated + "&parentId=" + parentId + "&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=" + isShowNav;
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;	

String action = ParamUtil.get(request, "action");
if (action.equals("del")) {
	if (!SalePrivilege.canUserManageCustomer(request, parentId)) {
		%>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
		return;		
	}
	FormMgr fm = new FormMgr();
	FormDb fdRelated = fm.getFormDb(formCodeRelated);
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
	try {
		if (fdm.del(request)) {
			String privurl = ParamUtil.get(request, "privurl");
			if (!privurl.equals(""))
				out.print(StrUtil.jAlert_Redirect("删除成功！","提示", privurl));
			else
				out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "customer_sales_chance_list.jsp?" + querystr + "&CPages=" + curpage + "&isShowNav=" + isShowNav));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
 %>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "customer_sales_chance_list.jsp?customerId=<%=parentId%>&menuItem=<%=menuItem%>&parentId=<%=parentId%>&formCode=<%=formCode%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=<%=isShowNav%>";
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null){
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

function getIds() {
    var checkedboxs = 0;
	var checkboxboxs = document.getElementsByName("ids");
	var id = "";
	if (checkboxboxs!=null)
	{
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

function del(){
	var id = getIds();
	if (id==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要删除吗？","提示",function(r){
		if(!r){return;}
		else{
			window.location.href = "customer_sales_chance_list.jsp?action=del&<%=querystr%>&CPages=<%=curpage%>&id=" + id + "&isShowNav=<%=isShowNav%>";
		}
	})
}
</script>
</head>
<body>
<%
if (isShowNav==1) {
%>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu4").className="current"; 
</script>
<%}%>
<%	
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
ListResult lr = fdao.listResult(formCodeRelated, sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
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
msd = msd.getModuleSetupDbOrInit(formCodeRelated);
		
// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
%>
<table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr>
    <th width="30" style="cursor:pointer"><input name="checkbox" type="checkbox" onClick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></th>
<%
com.redmoon.oa.flow.FormMgr fm = new com.redmoon.oa.flow.FormMgr();

MacroCtlMgr mm = new MacroCtlMgr();
int len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	String title = "创建者";
	String doSort = "doSort('" + fieldName + "')";
	if (!fieldName.equals("cws_creator")) {
		if (fieldName.startsWith("main")) {
			String[] ary = StrUtil.split(fieldName, ":");
			FormDb mainFormDb = fm.getFormDb(ary[1]);
			title = mainFormDb.getFieldTitle(ary[2]);
			doSort = "";	
		}
		else if (fieldName.startsWith("other")) {
			String[] ary = StrUtil.split(fieldName, ":");
			FormDb otherFormDb = fm.getFormDb(ary[2]);
			title = otherFormDb.getFieldTitle(ary[4]);
		}
		else {
			title = fd.getFieldTitle(fieldName);
		}
	}
	
%>
    <th width="120" style="cursor:pointer"><%=title%></th>
<%}%>
    <th width="150" style="cursor:pointer">操作</th>
  </tr>
  </thead>
  <%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
		%>
  <tr align="center" class="highlight">
    <td align="center">
      <input type="checkbox" name="ids" value="<%=id%>" />
    </td>
<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>	
		<td align="left"><a href="customer_sales_chance_show.jsp?customerId=<%=parentId%>&parentId=<%=parentId%>&id=<%=id%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&isShowNav=<%=isShowNav%>">
		<%if (!fieldName.equals("cws_creator")) {
			if (fieldName.startsWith("main")) {
				String[] ary = StrUtil.split(fieldName, ":");
				FormDb mainFormDb = fm.getFormDb(ary[1]);
				com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
				out.print(fdmMain.getFieldValueOfMain(parentId, ary[2]));
			}
			else if (fieldName.startsWith("other:")) {
				// String[] ary = StrUtil.split(fieldName, ":");
				
				// FormDb otherFormDb = fm.getFormDb(ary[2]);
				// com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
				// out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
				out.print(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName));
			}
			else{
				FormField ff = fd.getFormField(fieldName);
				if (ff!=null) {
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
						}
					}
					else {
						if (ff.getName().equals("possibility")) {
							%>
						  <div class="progressBar">
							  <div class="progressBarFore" style="width:<%=fdao.getFieldValue("possibility")%>%;">
							  </div>
							  <div class="progressText">
							  <%=fdao.getFieldValue("possibility")%>%
							  </div>
						  </div>
							<%
						}
						else {
						%>
						<%=fdao.getFieldValue(fieldName)%>
					<%	}
					}
				}
				else
					out.print(fieldName + " 不存在！");
			}%>
		<%}else{%>
			<%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>
		<%}%>
		</a></td>
	<%}%>
	<td>
		<%if (SalePrivilege.canUserManageCustomer(request, parentId)) {%>
		<a href="customer_sales_chance_edit.jsp?customerId=<%=parentId%>&parentId=<%=parentId%>&id=<%=id%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&isShowNav=<%=isShowNav%>">编辑</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onClick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return false;}else{window.location.href='customer_sales_chance_list.jsp?action=del&op=<%=op%>&customerId=<%=parentId%>&id=<%=id%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&menuItem=<%=menuItem%>&parentId=<%=parentId%>&CPages=<%=curpage%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=sqlUrlStr%>&isShowNav=<%=isShowNav%>'}}) " style="cursor:pointer">删除</a>
		<%}%></td>
  </tr>
  <%
  }
%>
</table>
<script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
			{name: '添加', bclass: 'add', onpress : actions},
			{name: '删除', bclass: 'delete', onpress : actions},
			{name: '导出', bclass: 'export', onpress : actions}
			],
			/*
			searchitems : [
				{display: 'ISO', name : 'iso'},
				{display: 'Name', name : 'name', isdefault: true}
				],
			*/
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
			autoHeight: true,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 84
			}
		);
});

function changeSort(sortname, sortorder) {
	window.location.href = "customer_sales_chance_list.jsp?pagesize=" + flex.getOptions().rp + "&<%=querystr%>";
}

function changePage(newp) {
	if (newp){
		window.location.href = "customer_sales_chance_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp + "&<%=querystr%>";
		}
}

function rpChange(pageSize) {
	window.location.href = "customer_sales_chance_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize + "&<%=querystr%>";
}

function onReload() {
	window.location.reload();
}
function actions(com, grid) {
	if(com=='添加'){
		window.location.href='customer_sales_chance_add.jsp?customerId=<%=parentId%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>&formCode=<%=formCode%>&amp;formCodeRelated=<%=formCodeRelated%>&isShowNav=<%=isShowNav%>';
	}
	else if (com=='删除'){
		del();
	}
	else if (com=='导出'){
	  javascript:window.open('../visual/exportExcelRelate.do?<%=querystr%>');
	}
}
</script>
</body>
</html>
