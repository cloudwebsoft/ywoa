<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.fileark.Document" %>
<%@ page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
long flowId = ParamUtil.getLong(request, "flowId", -1);

String searchKind = ParamUtil.get(request, "searchKind");
String what = ParamUtil.get(request, "what");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "modifiedDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
String kind = ParamUtil.get(request, "kind");

try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "searchKind", searchKind, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>流程中的存档</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%@include file="../flow_modify_inc_menu_top.jsp"%>
<script>
o("menu9").className="current";
</script>
<div class="spacerH"></div>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_READ)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	try {
		if (docmanager.del(request, id, privilege, true))
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "提示", "flow_doc_list.jsp?flowId=" + flowId));
		else
			out.print(StrUtil.jAlert("删除失败！", "提示"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	return;
}
String dir_name = "";

String sql = "select class1,title,id,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level from document where examine<>" + Document.EXAMINE_DUSTBIN;
if (op.equals("search")) {
	if (searchKind.equals("title")) {
		sql += " and title like "+StrUtil.sqlstr("%"+what+"%");
	}
	else if (searchKind.equals("content")) {
		sql = "select distinct id, class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level from document as d, doc_content as c where d.id=c.doc_id and examine=" + Document.EXAMINE_PASS;
	 	sql += " and c.content like " + StrUtil.sqlstr("%" + what + "%");
	}
	else {
		sql += " and examine=" + Document.EXAMINE_PASS + " and keywords like " + StrUtil.sqlstr("%" + what + "%");
	}
}

if (!kind.equals("")) {
	sql += " and kind=" + StrUtil.sqlstr(kind);
}

sql += " and flow_id=" + flowId + " order by doc_level desc, examine asc, " + orderBy + " " + sort;

// out.print(sql);

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(SkinUtil.makeErrMsg(request, "标识非法！"));
	return;
}

int pagesize = 15;
int curpage = Integer.parseInt(strcurpage);
PageConn pageconn = new PageConn(Global.getDefaultDB(), Integer.parseInt(strcurpage), pagesize);
ResultIterator ri = pageconn.getResultIterator(sql);
ResultRecord rr = null;

Paginator paginator = new Paginator(request, pageconn.getTotal(), pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<form name="form1" action="flow_doc_list.jsp?op=search" method="post" style="display:none">
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
    <tr>
      <td align="center">
      	按
        <select id="searchKind" name="searchKind">
          <option value="title">标题</option>
		  <%if (!Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {%>
          <option value="content">内容</option>
		  <%}%>
          <option value="keywords">关键字</option>
        </select>
        &nbsp;
        <input name=what size=20 />
        &nbsp;
        <input class="btn" name="submit" type=submit value="搜索" />
        <input name="flowId" type="hidden" value="<%=flowId%>" />
        <%if (op.equals("search")) {%>
        <script>
        form1.searchKind.value = "<%=searchKind%>";
        </script>
        <%}%>
  	</td>
    </tr>
</table>
</form>
<table width="98%" border="0" align="center" class="percent98">
  <tr>
    <td width="56%" height="24" align="left">
	<input class="btn" type="button" value="对比" onclick="compare()" />
    </td>
    <td width="44%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table id="mainTable" class="tabStyle_1 percent98" cellSpacing="0" cellPadding="0" width="100%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" width="2%" noWrap><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids');" /></td>
      <td class="tabStyle_1_title" width="42%" height="28" noWrap>标题</td>
      <td class="tabStyle_1_title" width="9%" noWrap>作者    
      </td>
      <td class="tabStyle_1_title" width="9%" noWrap>修改时间
      </td>
      <td class="tabStyle_1_title" width="16%" noWrap>操作</td>
    </tr>
<%
String dir_code = "";
Document doc = new Document();		
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next(); 
	boolean isHome = rr.getInt("isHome")==1?true:false;
	String color = StrUtil.getNullStr(rr.getString("color"));
	boolean isBold = rr.getInt("isBold")==1;
	java.util.Date expireDate = rr.getDate("expire_date");
	doc = doc.getDocument(rr.getInt("id"));
	dir_code = rr.getString("class1");
	%>
    <tr>
      <td align="center"><input name="ids" type="checkbox" value="<%=rr.getInt("id")%>" /></td>
      <td height="24">
      <%if (rr.getInt("type")==1) {%>
	  <img height=15 alt="" src="../forum/images/f_poll.gif" width=17 border=0 />&nbsp;
	  <%} %>
	  <%if (DateUtil.compare(new java.util.Date(), expireDate)==2) {%>
	  	<a href="javascript:;" onclick="addTab('<%=doc.getTitle() %>', '<%=request.getContextPath() %>/doc_show.jsp?id=<%=rr.getInt("id")%>')" title="<%=rr.getString(2)%>">
		<%
		if (isBold)
			out.print("<B>");
		if (!color.equals("")) {
		%>
			<font color="<%=color%>">
		<%}%>
		<%=rr.getString("title")%>
		<%if (!color.equals("")) {%>
		</font>
		<%}%>
		<%
		if (isBold)
			out.print("</B>");
		%>
		</a>
	  <%}else{%>
	  		<a href="javascript:;" onclick="addTab('<%=doc.getTitle() %>', '<%=request.getContextPath() %>/doc_show.jsp?id=<%=rr.getInt("id")%>')" title="<%=rr.getString(2)%>">
			<%=rr.get("title")%></a>
	  <%}%></td>
      <td align="center">
      <%
	      UserDb ud = new UserDb();
	      ud = ud.getUserDb(doc.getAuthor());
	      String userName = "";
	      if (ud!=null && ud.isLoaded())
           userName = StrUtil.getNullStr(ud.getRealName());
      
      %>
      <%=userName.equals("") ?doc.getAuthor() : userName %>
      </td>
      <td align="center"><%
	  java.util.Date d = rr.getDate("modifiedDate");
	  if (d!=null)
	  	out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
	  %>      </td>
      <td align="center">
        <%
	  com.redmoon.oa.fileark.Leaf lf6 = dir.getLeaf(rr.getString("class1"));	  
	  com.redmoon.oa.fileark.LeafPriv lp = new com.redmoon.oa.fileark.LeafPriv(lf6.getCode());	  
	  if (lp.canUserModify(privilege.getUser(request))) {
	  %>
        <a href="javascript:;" onclick="addTab('编辑文件', 'fwebedit_new.jsp?op=edit&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(rr.getString("class1"))%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>')">编辑</a>&nbsp;&nbsp;
        <%
	  }
	  if (lp.canUserDel(privilege.getUser(request))) {
	  %>
        <a onclick="return confirm('您确定要删除吗？')" href="flow_doc_list.jsp?op=del&id=<%=rr.getString(3)%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>&flowId=<%=flowId%>">删除</a>&nbsp;&nbsp;
        <%}%>
      </td>
    </tr>
    <%}%>
  </tbody>
</table>
<table class="percent98" width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  
  <tr>
    <td width="55%" align="left">&nbsp;</td>
    <td width="45%" align="right"><%
	String querystr = "op="+op+"&flowId=" + flowId + "&what="+StrUtil.UrlEncode(what) + "&dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=" + op + "&searchKind=" + searchKind + "&kind=" + StrUtil.UrlEncode(kind);
    out.print(paginator.getCurPageBlock("flow_doc_list.jsp?"+querystr, "down"));
%></td>
  </tr>
</table>
</body>
<script>
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
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

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}

var compareWin;
function openWin(url,width,height) {
	compareWin = window.open(url, "compareWin", "toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,fullScreen=yes");
	compareWin.focus();
}

function compare() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请选择需要对比的记录！", "提示");
		return;
	}
	var ary = ids.split(",");
	if (ary.length!=2) {
		jAlert("请选择两条记录进行对比！", "提示");
		return;
	}
	// addTab("版本对比", "<%=request.getContextPath()%>/flow/flow_doc_compare.jsp?flowId=<%=flowId%>&ids=" + ids);
	openWin("<%=request.getContextPath()%>/flow/flow_doc_compare.jsp?flowId=<%=flowId%>&ids=" + ids, window.screen.width, window.screen.height - 60);
}

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>