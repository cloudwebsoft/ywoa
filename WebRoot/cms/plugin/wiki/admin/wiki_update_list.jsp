<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<%@ page import="org.json.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = Leaf.CODE_WIKI;
String dir_name = ParamUtil.get(request, "dir_name");
Leaf leaf = dir.getLeaf(dir_code);

WikiDocUpdateDb wdud = new WikiDocUpdateDb();
String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("del")) {
	try {
		WikiDocUpdateMgr wdum = new WikiDocUpdateMgr();
		wdum.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	out.print(StrUtil.Alert_Redirect("删除成功！", "wiki_update_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
	return;
}
else if (op.equals("passBatch")) {
	try {
		WikiDocUpdateMgr wdum = new WikiDocUpdateMgr();
		wdum.pass(request);
	}
	catch (ResKeyException e) {
		out.print(StrUtil.Alert_Back(e.getMessage(request)));
		return;
	}
	
	out.print(StrUtil.Alert_Redirect("操作成功！", "wiki_update_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
	return;
}
else if (op.equals("mark")) {
	JSONObject json = new JSONObject();
	try {
		WikiDocUpdateMgr wdum = new WikiDocUpdateMgr();
		wdum.mark(request);
	}
	catch (ResKeyException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage(request));
		out.print(json);
		return;
	}
	json.put("ret", "1");
	json.put("msg", "操作成功！");
	out.print(json);
	//System.out.println(getClass() + " json=" + json);
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>管理wiki</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<script src="../../../../inc/common.js"></script>
<script src="../../../../js/jquery.js"></script>
<script src="../../../../js/jquery-ui/jquery-ui.js"></script>
</head>
<body>
<%@ include file="wiki_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%
// 用户需具有对目录的管理权限
LeafPriv lp = new LeafPriv(dir_code);
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String sql = "select w.id from " + wdud.getTable().getName() + " w, doc_content c where w.doc_id=c.doc_id and w.page_num=c.page_num";

String what = ParamUtil.get(request, "what");
int status = ParamUtil.getInt(request, "status", -1);
if (op.equals("search")) {
	if (!what.equals(""))
		sql += " and c.content like "+StrUtil.sqlstr("%"+what+"%"); 
	if (status!=-1)
		sql += " and w.check_status=" + status;
}

if (!dir_code.equals("") && !dir_code.equals("wiki"))
	sql += " and w.dir_code=" + StrUtil.sqlstr(dir_code);
sql += " order by w.edit_date desc";

// out.print(sql);

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 15;
int curpage = Integer.parseInt(strcurpage);
ListResult lr = wdud.listResult(sql, curpage, pagesize);

Paginator paginator = new Paginator(request, lr.getTotal(), pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

UserMgr um = new UserMgr();
%>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
  <form name="form1" action="wiki_update_list.jsp" method="get">
    <tr>
      <td align="center">
      状态
      <select id="status" name="status">
      <option value="-1">不限</option>
      <option value="<%=WikiDocUpdateDb.CHECK_STATUS_PASSED%>"><%=WikiDocUpdateDb.getCheckStatusDesc(WikiDocUpdateDb.CHECK_STATUS_PASSED)%></option>
      <option value="<%=WikiDocUpdateDb.CHECK_STATUS_NOTPASSED%>"><%=WikiDocUpdateDb.getCheckStatusDesc(WikiDocUpdateDb.CHECK_STATUS_NOTPASSED)%></option>
      <option value="<%=WikiDocUpdateDb.CHECK_STATUS_WAIT%>"><%=WikiDocUpdateDb.getCheckStatusDesc(WikiDocUpdateDb.CHECK_STATUS_WAIT)%></option>
      </select>
      <script>
	  o("status").value = "<%=status%>";
	  </script>
      关键字&nbsp;
          <input name="what" size=20 value="<%=what%>">
          <input class="btn" type="submit" value="搜索">
          <input name="op" type="hidden" value="search" />
	  </td>
    </tr>
  </form>
</table>
<table width="92%" border="0" align="center" class="p9">
  <tr>
    <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table class="tabStyle_1" cellSpacing="0" cellPadding="3" width="98%" align="center">
    <thead>
    <tr>
      <td noWrap width="3%">
        <input name="checkbox" type="checkbox" onClick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')" />
      </td>
      <td noWrap width="5%">编号</td>
      <td noWrap width="25%">标题</td>
      <td noWrap width="11%">用户</td>
      <td noWrap width="11%">时间</td>
      <td noWrap width="7%">得分</td>
      <td noWrap width="6%">状态</td>
      <td noWrap width="12%">原因</td>
      <td noWrap width="20%">管理</td>
    </tr>
  </thead>
  <tbody>
<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
String editPage = usd.isWebedit()?"fwebedit.jsp":"fwebedit_new.jsp";

Iterator ir = lr.getResult().iterator();
Document doc = new Document();
while (ir.hasNext()) {
 	wdud = (WikiDocUpdateDb)ir.next();
	doc = doc.getDocument(wdud.getInt("doc_id"));
	UserDb user = um.getUserDb(wdud.getString("user_name"));
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td><input name="ids" type="checkbox" value="<%=wdud.getLong("id")%>" /></td>
      <td><%=wdud.getLong("id")%></td>
      <td><a href="javascript:;" onclick="addTab('<%=doc.getTitle()%>', '<%=request.getContextPath()%>/wiki_show.jsp?id=<%=doc.getId()%>&pageNum=<%=wdud.getInt("page_num")%>')"><%=doc.getTitle()%></a></td>
      <td>
      <%if (user.isLoaded()) {%>
      <a href="javascript:;" onclick="addTab('用户信息', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=user.getName()%>')"><%=user.getRealName()%></a>
      <%}else{%>
      <%=wdud.getString("user_name")%>
      <%}%>
      </td>
      <td><%=DateUtil.format(wdud.getDate("edit_date"), "yy-MM-dd HH:mm")%></td>
      <td><%=NumberUtil.round(wdud.getFloat("score"), 1)%></td>
      <td>
      <%
	  out.print(WikiDocUpdateDb.getCheckStatusDesc(wdud.getInt("check_status")));
	  %>
      </td>
      <td>
      <span title="<%=StrUtil.toHtml(wdud.getString("reason"))%>"><%=StrUtil.getAbstract(request, wdud.getString("reason"), 10)%></span>
      </td>
      <td align="center">
      <%
	  if (wdud.getInt("page_num")>1) {
	  %>
   	  <a href="javascript:;" onclick="addTab('<%=doc.getTitle()%>', '<%=request.getContextPath()%>/doc_editpage.jsp?op=edit&doc_id=<%=wdud.getInt("doc_id")%>&CPages=<%=wdud.getInt("page_num")%>')"><lt:Label res="res.label.cms.doc" key="edit"/></a>
  	  <%
	  }
	  else {
	  %>
   	  <a target="_blank" href="<%=request.getContextPath()%>/<%=editPage%>?op=edit&id=<%=wdud.getInt("doc_id")%>&CPages=<%=wdud.getInt("page_num")%>&dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>"><lt:Label res="res.label.cms.doc" key="edit"/></a>
   	  <%}%>
      &nbsp;&nbsp;<a onClick="return confirm('您确定要删除么？')" href="wiki_update_list.jsp?op=del&ids=<%=wdud.getLong("id")%>&dir_code=<%=dir_code%>">删除</a>
      &nbsp;&nbsp;<a href="wiki_update_list.jsp?op=passBatch&ids=<%=wdud.getLong("id")%>&checkStatus=<%=WikiDocUpdateDb.CHECK_STATUS_PASSED%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>">通过</a>
      &nbsp;&nbsp;<a href="wiki_update_list.jsp?op=passBatch&ids=<%=wdud.getLong("id")%>&checkStatus=<%=WikiDocUpdateDb.CHECK_STATUS_NOTPASSED%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>">不通过</a></td>
    </tr>
    <%}%>
  </tbody>
</table>
<table width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="46%" align="left">
    <input class="btn" type="button" onClick="passBatch(<%=WikiDocUpdateDb.CHECK_STATUS_PASSED%>)" value="通过" >
&nbsp;&nbsp;<input class="btn" type="button" onClick="passBatch(<%=WikiDocUpdateDb.CHECK_STATUS_NOTPASSED%>)" value="不通过" >
&nbsp;&nbsp;<input class="btn" type="button" onClick="passBatch(<%=WikiDocUpdateDb.CHECK_STATUS_WAIT%>)" value="待审批" >
&nbsp;&nbsp;
<input class="btn" type="button" onclick="delBatch()" value="删除" />
&nbsp;&nbsp;
<input class="btn" type="button" onclick="mark()" value="评分" /></td>
    <td width="54%" align="right"><%
	String querystr = "op="+op+"&what="+StrUtil.UrlEncode(what) + "&status=" + status;
    out.print(paginator.getCurPageBlock("wiki_update_list.jsp?"+querystr));
%></td>
  </tr>
</table>
<div id="result" style="display:none">
分数：<input id="score" name="score" value="1.0" />
</div>
<div id="markResult" style="display:none">
</div>
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

function passBatch(checkStatus) {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择至少一条记录！");
		return;
	}
	window.location.href = "?op=passBatch&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&checkStatus=" + checkStatus + "&ids=" + ids;
}

function delBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择至少一条记录！");
		return;
	}
	if (confirm("您确定要删除么？"))
		window.location.href = "?op=del&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids;
}

function doMark(ids, score) {
	$.ajax({
		type: "post",
		url: "wiki_update_list.jsp",
		data : {
			op: "mark",
        	ids : ids,
        	score : score
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			$("#markResult").html(re.msg);
			$("#markResult").dialog({title:"提示", modal: true, buttons: { "确定": function() { $(this).dialog("close"); window.location.href="wiki_update_list.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>" }}, closeOnEscape: true, draggable: true, resizable:true });
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});
}

function mark() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择至少一条记录！");
		return;
	}
	
	$("#result").dialog({
		title:"提示",
		modal: true,
		buttons: { 
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				doMark(ids, $("#score").val());
				$(this).dialog("close");
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true
	});
	
}
</script>
</html>