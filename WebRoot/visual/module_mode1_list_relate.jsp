<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
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
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "menuItem", menuItem, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

FormDb fd = new FormDb();

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%
fd = fd.getFormDb(formCodeRelated);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}

// 置页面类型
// request.setAttribute("pageType", "list");

String relateFieldValue = "";
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
ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (!mpd.canUserSee(privilege.getUser(request))) {
	%>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%	
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
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
				response.sendRedirect("module_mode1_show_relate.jsp?id=" + id + "&parentId=" + parentId + "&formCodeRelated=" + formCodeRelated + "&formCode=" + formCode);
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
int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
String sql = arySQL[0];
String sqlUrlStr = arySQL[1];

querystr = "op=" + op + "&menuItem=" + menuItem + "&formCode=" + formCode + "&formCodeRelated=" + formCodeRelated + "&parentId=" + parentId + "&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=" + isShowNav;
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;

String action = ParamUtil.get(request, "action");
if (action.equals("del")) {
	FormMgr fm = new FormMgr();
	FormDb fdRelated = fm.getFormDb(formCodeRelated);
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
	try {
		if (fdm.del(request)) {
			String privurl = ParamUtil.get(request, "privurl");
			if (!privurl.equals(""))
				out.print(StrUtil.jAlert_Redirect("删除成功！","提示", privurl));
			else
				out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "module_mode1_list_relate.jsp?" + querystr + "&CPages=" + curpage + "&isShowNav=" + isShowNav));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
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
			
	window.location.href = "module_mode1_list_relate.jsp?menuItem=<%=menuItem%>&parentId=<%=parentId%>&formCode=<%=formCode%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=<%=isShowNav%>";
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
			window.location.href = "module_mode1_list_relate.jsp?action=del&<%=querystr%>&CPages=<%=curpage%>&id=" + id + "&isShowNav=<%=isShowNav%>";
		}
	})
}
</script>
</head>
<body>
<%
if (isShowNav==1) {
%>
<%@ include file="module_mode1_inc_menu_top.jsp"%>
<script>
$("#menu<%=menuItem%>").addClass("current"); 
</script>
<%}%>
<div class="spacerH"></div>
<%
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
ListResult lr = fdao.listResult(formCodeRelated, sql, curpage, pagesize);
int total = lr.getTotal();
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
String[] fieldsWidth = msd.getColAry(false, "list_field_width");
%>
<table class="percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="48%" height="28" align="left"><input name="button" class="btn" type="button" onClick="window.location.href='module_mode1_add_relate.jsp?parentId=<%=parentId%>&menuItem=<%=menuItem%>&formCode=<%=formCode%>&amp;formCodeRelated=<%=formCodeRelated%>&isShowNav=<%=isShowNav%>'" value="添加" />
    &nbsp;&nbsp;
    <input name="button3" class="btn" type="button" onClick="window.location.href='module_mode1_search_relate.jsp?parentId=<%=parentId%>&amp;menuItem=<%=menuItem%>&amp;formCode=<%=formCode%>&amp;formCodeRelated=<%=formCodeRelated%>&isShowNav=<%=isShowNav%>'" value="查询" /></td>
    <td width="52%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
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
    <td class="tabStyle_1_title" width="<%=fieldsWidth[i]%>" <%if (!fieldName.startsWith("other:")) {%>style="cursor:hand" onClick="<%=doSort%>"<%}%>>
	<%=title%>
	<%if (orderBy.equals(fieldName)) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>	
	</td>
<%}%>
    <td class="tabStyle_1_title" title="按ID排序" style="cursor:hand" onClick="doSort('id')">操作
      <%if (orderBy.equals("id")) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>
    </td>
  </tr>
  <%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
		%>
  <tr align="center" class="highlight">
<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>	
		<td align="left">
		<%if (i==0) {%>
		<input type="checkbox" name="ids" value="<%=id%>" />
		<%}%>		
		<a href="module_mode1_show_relate.jsp?parentId=<%=parentId%>&id=<%=id%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&isShowNav=<%=isShowNav%>">
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
				// ff!=null 防止列被删除
				if (ff!=null && ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				}
				else {%>
					<%=fdao.getFieldValue(fieldName)%>
				<%}
			}%>
		<%}else{%>
			<%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>
		<%}%>
		</a></td>
	<%}%>
	<td>
		<%if (mpd.canUserManage(privilege.getUser(request))) {%>
		<a href="module_mode1_edit_relate.jsp?parentId=<%=parentId%>&id=<%=id%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&isShowNav=<%=isShowNav%>">编辑</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onClick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){event.returnValue=false;}else{ window.location.href='module_mode1_list_relate.jsp?action=del&op=<%=op%>&id=<%=id%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&menuItem=<%=menuItem%>&parentId=<%=parentId%>&CPages=<%=curpage%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=sqlUrlStr%>&isShowNav=<%=isShowNav%>'}}) " style="cursor:pointer">删除</a>
		<%}%></td>
  </tr>
  <%
  }
%>
</table>
<table width="98%" border="0" cellspacing="0" cellpadding="0" align="center" class="percent98">
  <tr>
    <td width="50%" height="23" align="left"><input class="btn" name="button2" type="button" onClick="selAllCheckBox('ids')" value="全选" />
      &nbsp;&nbsp;
      <input name="button2" type="button" class="btn" onClick="javascript:del()" value="删除" />
    &nbsp;&nbsp;
    <input name="button4" type="button" class="btn" onClick="javascript:window.open('module_excel_relate.jsp?<%=querystr%>')" value="导出至Excel" />
<%
String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, ",");
len = 0;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
	%>
	&nbsp;&nbsp;<input class="btn" type="button" value="<%=btnNames[i]%>" onClick="<%=StrUtil.HtmlEncode(btnScripts[i])%>" />
	<%
	}
}
%>	
	</td>
    <td width="50%" align="right"><%
		out.print(paginator.getCurPageBlock("module_mode1_list_relate.jsp?"+querystr));
	%></td>
  </tr>
</table>
</body>
</html>
