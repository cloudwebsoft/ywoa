<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = Leaf.CODE_WIKI;
	
Leaf leaf = new Leaf();
leaf = leaf.getLeaf(dir_code);
String orderBy = ParamUtil.get(request, "orderBy");
String sort = ParamUtil.get(request, "sort");
if (orderBy.equals(""))
	orderBy = "examine";
if (sort.equals(""))
	sort = "asc";
String action = ParamUtil.get(request, "action");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.cms.doc" key="artical_list"/></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../../../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../../../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../../../../inc/common.js"></script>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";	
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	o("orderBy").value = orderBy;
	o("sort").value = sort;
	form1.submit();
}	
</script>
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%
LeafPriv lp = new LeafPriv(dir_code);
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String name = "", titleKey = "", sBeginDate = "", sEndDate = "", sql = "", condition = "",querystr = "", strDoOperation = "";
titleKey = ParamUtil.get(request, "titleKey");
name = ParamUtil.get(request, "name");
sBeginDate = ParamUtil.get(request, "beginDate");
sEndDate = ParamUtil.get(request, "endDate");
int pageSize = ParamUtil.getInt(request, "pageSize", 20);

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	try {
		if (docmanager.del(request, id, privilege, true)){
		    String redirectUrl = "wiki_list.jsp?pageSize=" + pageSize + "&op=search";
			if(!titleKey.equals(""))
				redirectUrl = redirectUrl + "&titleKey=" + titleKey;
			if(!name.equals(""))
				redirectUrl = redirectUrl + "&name=" + name;
			if(!sBeginDate.equals(""))
				redirectUrl = redirectUrl + "&beginDate=" + sBeginDate;
			if(!sEndDate.equals(""))
				redirectUrl = redirectUrl + "&endDate=" + sEndDate;
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), redirectUrl));
			return;
		}else 
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
			return;
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if (op.equals("passExamine")) {
	try {
		docmanager.passExamineBatch(request);
		String redirectUrl = "wiki_list.jsp?pageSize=" + pageSize + "&op=search";
		if(!titleKey.equals(""))
			redirectUrl = redirectUrl + "&titleKey=" + titleKey;
		if(!name.equals(""))
			redirectUrl = redirectUrl + "&name=" + name;
		if(!sBeginDate.equals(""))
			redirectUrl = redirectUrl + "&beginDate=" + sBeginDate;
		if(!sEndDate.equals(""))
			redirectUrl = redirectUrl + "&endDate=" + sEndDate;
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), redirectUrl));
		return;
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if (op.equals("delBatch")) {
	try {
		docmanager.delBatch(request, true);
		String redirectUrl = "wiki_list.jsp?pageSize=" + pageSize + "&op=search";
		if(!titleKey.equals(""))
			redirectUrl = redirectUrl + "&titleKey=" + titleKey;
		if(!name.equals(""))
			redirectUrl = redirectUrl + "&name=" + name;
		if(!sBeginDate.equals(""))
			redirectUrl = redirectUrl + "&beginDate=" + sBeginDate;
		if(!sEndDate.equals(""))
			redirectUrl = redirectUrl + "&endDate=" + sEndDate;
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), redirectUrl));
		return;
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if (op.equals("lockBatch")) {
	try {
		WikiDocumentMgr wdm = new WikiDocumentMgr();
		wdm.lockBatch(request);
		String redirectUrl = "wiki_list.jsp?pageSize=" + pageSize + "&op=search";
		if(!titleKey.equals(""))
			redirectUrl = redirectUrl + "&titleKey=" + titleKey;
		if(!name.equals(""))
			redirectUrl = redirectUrl + "&name=" + name;
		if(!sBeginDate.equals(""))
			redirectUrl = redirectUrl + "&beginDate=" + sBeginDate;
		if(!sEndDate.equals(""))
			redirectUrl = redirectUrl + "&endDate=" + sEndDate;
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), redirectUrl));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("setLevel")) {
	try {
		WikiDocumentMgr wdm = new WikiDocumentMgr();
		wdm.setLevel(request);
		String redirectUrl = "wiki_list.jsp?pageSize=" + pageSize + "&op=search";
		if(!titleKey.equals(""))
			redirectUrl = redirectUrl + "&titleKey=" + titleKey;
		if(!name.equals(""))
			redirectUrl = redirectUrl + "&name=" + name;
		if(!sBeginDate.equals(""))
			redirectUrl = redirectUrl + "&beginDate=" + sBeginDate;
		if(!sEndDate.equals(""))
			redirectUrl = redirectUrl + "&endDate=" + sEndDate;
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), redirectUrl));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
%>
<%@ include file="wiki_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<form name="form1" method="get" action="wiki_list.jsp">
<table class="tabStyle_1 percent98" width="86%"  border="0" align="center" cellpadding="3" cellspacing="1">  
	<thead>
	  <tr>
	    <td colspan="2" align="center">搜索条件</td>
	    </tr>
	  <tr>
    </thead>
        <td width="45%" class="tbg1">&nbsp;发布者(多用户名中间请用半角逗号 "," 分割)</td>
        <td width="55%" class="tbg1" style="height:28px">&nbsp;&nbsp;<input type="text" name="name" value="<%=name%>" />
        <input name="op" id="op" value="search" type="hidden">
        <input name="dir_code" id="dir_code" value="<%=dir_code%>" type="hidden">
        </td>
	  </tr> 
	  <tr>
        <td width="45%" class="tbg1">&nbsp;标题关键字</td>
        <td width="55%" class="tbg1" style="height:28px">&nbsp;&nbsp;<input type="text" name="titleKey" value="<%=titleKey%>"/></td>
	  </tr>   
	  <tr>
        <td width="45%" class="tbg1">&nbsp;发表时间范围(格式 yyyy-mm-dd，不限制为空)</td>
        <td width="55%" class="tbg1" style="height:28px">
        &nbsp;&nbsp;从&nbsp;<input id="beginDate" name="beginDate" value="<%=sBeginDate%>" readonly>&nbsp;
        到&nbsp;<input id="endDate" name="endDate" value="<%=sEndDate%>" readonly>
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>        
        </td>
	  </tr>
	  <tr>
	    <td class="tbg1">&nbsp;搜索结果每页显示行数</td>
	    <td class="tbg1" style="height:28px">&nbsp;
	      <input type="text" name="pageSize" value="<%=pageSize%>"/>
          <input id="orderBy" name="orderBy" value="<%=orderBy%>" type="hidden">
          <input id="sort" name="sort" value="<%=sort%>" type="hidden">
          </td>
	    </tr>
	  <tr>
	    <td colspan="2" align="center" class="tbg1"><input class="btn" type=submit value="确定" /></td>
    </tr>     
    </table>
</form>
<%
int id = -1, docLevel = -1, docType = -1, examine = -1;
boolean isHome = false, isBold = false;
String color = "", title = "", class1 = "";
java.util.Date expireDate = null;
java.util.Date modifiedDate = null;

sql = "select d.id from document d, cms_wiki_doc s where s.doc_id=d.id and d.examine<>" + Document.EXAMINE_DUSTBIN;

if (!dir_code.equals("") && !dir_code.equals(WikiUnit.code))
	sql += " and class1=" + StrUtil.sqlstr(dir_code);

querystr = "dir_code=" + StrUtil.UrlEncode(dir_code);

if(!name.trim().equals("")) {
	String[] nameAry = StrUtil.split(name, ",");
	String strName = "";
	if (nameAry!=null) {
		int length = nameAry.length;
		for (int j=0; j<length; j++) {
			strName += "'" + nameAry[j] + "'";
			if(j < length - 1 && !nameAry[j].equals(""))
				strName += ",";
		}
	}
	
	condition += " and author in (" + strName + ")";
	querystr += "&name=" + StrUtil.UrlEncode(name);
}

if(!titleKey.trim().equals("")){
	condition += " and title like " + StrUtil.sqlstr("%"+titleKey+"%");
	querystr += "&titleKey=" + StrUtil.UrlEncode(titleKey);
}

if(!sBeginDate.trim().equals("") && !sEndDate.trim().equals("")){
	java.util.Date beginDate = DateUtil.parse(sBeginDate, "yyyy-MM-dd");
	java.util.Date endDate = DateUtil.parse(sEndDate, "yyyy-MM-dd");
	long lBeginDate = beginDate.getTime();
	long lEndDate = endDate.getTime() + 24*60*60*1000;
	condition += " and createDate>=" + lBeginDate + " and createDate<" + lEndDate;
	querystr += "&beginDate=" + StrUtil.UrlEncode(sBeginDate) + "&endDate=" + StrUtil.UrlEncode(sEndDate);
}else{
	if(!sBeginDate.trim().equals("")){
		java.util.Date beginDate = DateUtil.parse(sBeginDate, "yyyy-MM-dd");
		long lBeginDate = beginDate.getTime();	
		condition += " and createDate>=" + lBeginDate;
		querystr += "&sBeginDate=" + StrUtil.UrlEncode(sBeginDate);
	}else{
		if(!sEndDate.trim().equals("")){
			java.util.Date endDate = DateUtil.parse(sEndDate, "yyyy-MM-dd");
			long lEndDate = endDate.getTime() + 24*60*60*1000;
			condition += " and createDate<" + lEndDate;
			querystr += "&sEndDate=" + StrUtil.UrlEncode(sEndDate);
		}			
	}
}

querystr += "&orderBy=" + orderBy;
querystr += "&sort=" + sort;

if(!condition.equals("")){
	sql = sql + " " + condition;
}

sql += " order by " + orderBy + " " + sort;

// out.print(sql);

Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pageSize);
ResultIterator ri = pageconn.getResultIterator(sql);
paginator.init(pageconn.getTotal(), pageSize);

ResultRecord rr = null;

//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<table width="98%" border="0" align="center" class="p9">
  <tr>
    <td height="20" align="right"><lt:Label res="res.label.cms.doc" key="found_right_list"/><b><%=paginator.getTotal() %></b><lt:Label res="res.label.cms.doc" key="page_list"/><b><%=paginator.getPageSize() %></b><lt:Label res="res.label.cms.doc" key="page"/><b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %>&nbsp;</b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellspacing="1" cellpadding="3" width="98%" align="center">
	<thead>
    <tr>
      <td width="3%" align="center" nowrap><input name="ids2" type="checkbox" onClick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')"></td>
      <td width="7%" align="center" nowrap style="PADDING-LEFT: 10px">编号</td>
      <td width="24%" align="center" nowrap style="PADDING-LEFT: 10px"><lt:Label res="res.label.cms.doc" key="title"/></td>
      <td width="9%" align="center" nowrap>栏目</td>
      <td width="5%" align="center" nowrap>锁定</td>
      <td width="9%" align="center" nowrap>作者</td>
      <td width="5%" align="center" nowrap>次数</td>
      <td width="7%" align="center" nowrap><lt:Label res="res.label.cms.doc" key="modify_date"/></td>
      <td width="11%" align="center" nowrap style="cursor:pointer" onClick="doSort('examine')"><lt:Label res="res.label.cms.doc" key="check_state"/>
        <%if (orderBy.equals("examine")) {
			if (sort.equals("asc")) 
				out.print("<img src='../../../../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../../../../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>      
      </td>
      <td width="20%" align="center" nowrap>操作</td>
    </tr>
    </thead>
  <tbody>
<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
String pageUrl = usd.isWebedit()?"fwebedit.jsp":"fwebedit_new.jsp";

	Document doc = null;
	DocumentMgr dm = new DocumentMgr();
	WikiDocumentDb wdd = new WikiDocumentDb();
	while (ri.hasNext()) {
		rr = (ResultRecord)ri.next(); 
		id = rr.getInt("id");
		doc =  dm.getDocument(id);
		wdd = wdd.getWikiDocumentDb(id);
		
		isHome = doc.getIsHome();
		color = doc.getColor();
		isBold = doc.isBold();
		expireDate = doc.getExpireDate();
		modifiedDate = doc.getModifiedDate();
		docLevel = doc.getLevel();
		title = doc.getTitle();
		class1 = doc.getDirCode();
		docType = doc.getType();
		examine = doc.getExamine();
%>
    <tr>
      <td align="center"><input name="ids" type="checkbox" value="<%=id%>"></td>
      <td align="center"><%=id%></td>
      <td style="PADDING-LEFT: 10px">
<%
	  	if (docLevel == Document.LEVEL_TOP)
	  		out.print("[置顶]&nbsp;");
		if (DateUtil.compare(new java.util.Date(), expireDate)==2) {
%>
          <a target="_blank" href="../../../../doc_view.jsp?id=<%=id%>" title="<%=title%>">
<%
		if (isBold)
			out.print("<B>");
		if (!color.equals("")) {
%>
          <font color="<%=color%>">
<%
		}
%>
          <%=title%>
<%
		if (!color.equals("")) {
%>
          </font>
<%
		}
		if (isBold)
			out.print("</B>");
%>
          </a>
          <%}else{%>
          <a target="_blank" href="../../../../wiki_show.jsp?id=<%=id%>" title="<%=title%>"><%=title%></a>
          <%}%></td>
      <td align="center"><%
	  Leaf lf6 = dir.getLeaf(class1);
	  if (lf6!=null)
		  out.print("<a target='_blank' href='../../../../fileark/wiki_list.jsp?dir_code=" + lf6.getCode() + "'>" + lf6.getName() + "</a>");
	  %>
      </td>
      <td align="center"><%=wdd.getStatusDesc()%></td>
      <td align="center"><a href="../../../../user_info.jsp?userName=<%=StrUtil.UrlEncode(doc.getNick())%>" target="_blank"><%=doc.getAuthor()%></a></td>
      <td align="center"><%=wdd.getEditCount()%>
      </td>
      <td align="center"><%
	  if (modifiedDate!=null)
	  	out.print(DateUtil.format(modifiedDate, "yy-MM-dd"));
	  %></td>
      <td align="center"><%
	  if (examine==0)
	  	out.print("<font color='blue'>" + SkinUtil.LoadString(request, "res.label.cms.doc","no_check") + "</font>");
	  else if (examine==1)
	  	out.print("<font color='red'>" + SkinUtil.LoadString(request, "res.label.cms.doc","no_pass") + "</font>");
	  else if (examine==10)
	  	out.print("<font color='#FFCC00'>" + SkinUtil.LoadString(request, "res.label.webedit","dustbin") + "</font>");
	  else
	  	out.print(SkinUtil.LoadString(request, "res.label.cms.doc","pass"));
	  %></td>
      <td align="center">
	  <a target="_blank" href="<%=request.getContextPath()%>/<%=pageUrl%>?op=edit&id=<%=id%>&dir_code=<%=StrUtil.UrlEncode(class1)%>"><lt:Label res="res.label.cms.doc" key="edit"/>
	  </a>
      &nbsp;&nbsp;<a onClick="return confirm('您确定要删除吗？')" href="wiki_list.jsp?pageSize=<%=pageSize%>&op=del&id=<%=id%><%if(!querystr.equals("")) out.print("&" + querystr);%>"><lt:Label res="res.label.cms.doc" key="del"/>
	  </a>
  	  <%if (doc.getExamine()!=Document.EXAMINE_PASS) {%>
	  &nbsp;&nbsp;<a href="wiki_list.jsp?pageSize=<%=pageSize%>&op=passExamine&examine=<%=Document.EXAMINE_PASS%>&ids=<%=id%><%if(!querystr.equals("")) out.print("&" + querystr);%>">通过</a>
	  <%}else{%>
	  &nbsp;&nbsp;<a href="wiki_list.jsp?pageSize=<%=pageSize%>&op=passExamine&examine=<%=Document.EXAMINE_NOT%>&ids=<%=id%><%if(!querystr.equals("")) out.print("&" + querystr);%>">未审核</a>
	  <%}%> 
	  <%if (docLevel != Document.LEVEL_TOP) {%>
      &nbsp;&nbsp;<a onClick="return confirm('您确定要置顶吗？')" href="wiki_list.jsp?pageSize=<%=pageSize%>&op=setLevel&level=<%=WikiDocumentDb.LEVEL_TOP%>&ids=<%=id%><%if(!querystr.equals("")) out.print("&" + querystr);%>">置顶</a>
      <%}else{%>
      &nbsp;&nbsp;<a onClick="return confirm('您确定要取消置顶吗？')" href="wiki_list.jsp?pageSize=<%=pageSize%>&op=setLevel&level=<%=WikiDocumentDb.LEVEL_NONE%>&ids=<%=id%><%if(!querystr.equals("")) out.print("&" + querystr);%>">取消置顶</a>
      <%}%></td>
    </tr>
    <%}%>
  </tbody>
</table>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" align="right">&nbsp;</td>
  </tr>
  <tr>
    <td width="49%" align="left">
      <input class="btn" type="button" onClick="doDel('<%=querystr%>')" value="<lt:Label key="op_del"/>">
      &nbsp;
      <input class="btn" type="button" onClick="passExamineBatch()" value="通过" title="状态为草稿的文章将被忽略">      
      &nbsp;
      <input class="btn" type="button" onClick="lockBatch('<%=WikiDocumentDb.STATUS_LOCKED%>', '<%=querystr%>')" value="锁定" title="锁定后前台用户将不能编辑">      
      &nbsp;
      <input class="btn" type="button" onClick="lockBatch('<%=WikiDocumentDb.STATUS_UNLOCKED%>', '<%=querystr%>')" value="解锁" title="锁定后前台用户将不能编辑">      
      &nbsp;
      <input class="btn" type="button" onClick="setLevel('<%=WikiDocumentDb.LEVEL_TOP%>', '<%=querystr%>')" value="置顶">
      &nbsp;
      <input class="btn" type="button" onClick="setLevel('<%=WikiDocumentDb.LEVEL_NONE%>', '<%=querystr%>')" value="取消置顶">      
      <%if (action.equals("sel")) {%>
      &nbsp;
      <input class="btn" type="button" onClick="sel()" value="选择">
      <%}%>
      </td>
    <td width="51%" align="right">
<%
	strDoOperation = querystr;
	if(!querystr.equals(""))
		querystr = querystr + "&op=" + op;
	else
		querystr = "op=" + op;
    out.print(paginator.getCurPageBlock("wiki_list.jsp?pageSize="+ pageSize + "&" + querystr));
%>
</td>
  </tr>
</table>
</body>
<script>
function sel() {
	var ids = getCheckboxValue("ids");
	window.opener.selDoc(ids);
	window.close();
}

function doOperation() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("<lt:Label res="res.label.forum.topic_m" key="need_id"/>");
		return;
	}

	var tDirValue = tDirCode.value;
	window.location.href = "wiki_list.jsp?pageSize=" + form1.pageSize.value + "&op=moveBoard&tDirCode=" + tDirValue + "&ids=" + ids + "&<%=strDoOperation%>";
}

function lockBatch(status, querystr) {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择文章！");
		return;
	}
	if(querystr != "")
		window.location.href = "wiki_list.jsp?pageSize=" + form1.pageSize.value + "&op=lockBatch&ids=" + ids + "&status=" + status + "&" + querystr;
	else
		window.location.href = "wiki_list.jsp?pageSize=" + form1.pageSize.value + "&op=lockBatch&ids=" + ids + "&status=" + status;
}

function setLevel(level, querystr) {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择文章！");
		return;
	}
	if(querystr != "")
		window.location.href = "wiki_list.jsp?pageSize=" + form1.pageSize.value + "&op=setLevel&ids=" + ids + "&level=" + level + "&" + querystr;
	else
		window.location.href = "wiki_list.jsp?pageSize=" + form1.pageSize.value + "&op=setLevel&ids=" + ids + "&level=" + level;
}

function doDel(querystr) {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择文章！");
		return;
	}
	if(querystr != "")
		window.location.href = "wiki_list.jsp?pageSize=" + form1.pageSize.value + "&op=delBatch&ids=" + ids + "&" + querystr;
	else
		window.location.href = "wiki_list.jsp?pageSize=" + form1.pageSize.value + "&op=delBatch&ids=" + ids;
}

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

function passExamineBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择文章！");
		return;
	}
	window.location.href = "?op=passExamine&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&examine=<%=Document.EXAMINE_PASS%>&ids=" + ids;
}

</script>
</html>