<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="org.json.JSONObject" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="admin.flow";
	if (!privilege.isUserPrivValid(request,priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String typeCode = ParamUtil.get(request, "typeCode");
	String typeName = "";
	if (!typeCode.equals("")) {
		Leaf lf = new Leaf();
		lf = lf.getLeaf(typeCode);
		if (lf!=null)
			typeName = "&nbsp;-&nbsp;"+lf.getName()+"&nbsp;";
	}
	
	LeafPriv lp = new LeafPriv(typeCode);
	if (lp.canUserQuery(privilege.getUser(request)) || lp.canUserExamine(privilege.getUser(request))) {
		;
	}
	else {
		%>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
		return;
	}
	
	String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
	if (strcurpage.equals(""))
		strcurpage = "1";
	if (!StrUtil.isNumeric(strcurpage)) {
		out.print(StrUtil.makeErrMsg("标识非法！"));
		return;
	}
	int pagesize = 20;
	int curpage = Integer.parseInt(strcurpage);
	
	String op = ParamUtil.get(request, "op");
	String by = ParamUtil.get(request, "by");
	String what = ParamUtil.get(request, "what");
	String status = ParamUtil.get(request, "status");
	
	String fromDate = ParamUtil.get(request, "fromDate");
	String toDate = ParamUtil.get(request, "toDate");
	
	String action = ParamUtil.get(request, "action");
	if (action.equals("del")) {
		JSONObject json = new JSONObject();
		try {
			String ids = ParamUtil.get(request, "ids");
			String[] ary = StrUtil.split(ids, ",");
			if (ary!=null) {
				for (int i=0; i<ary.length; i++) {
					int flow_id = StrUtil.toInt(ary[i]);
					WorkflowMgr wm = new WorkflowMgr();
					WorkflowDb wf = wm.getWorkflowDb(flow_id);
					// 判断用户是否拥有管理权
					if (!lp.canUserExamine(privilege.getUser(request))) {
						json.put("ret", "0");
						json.put("msg", cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
						out.print(json);
						return;
					}
					wf.del();
				}
			}
		}
		catch (ErrMsgException e) {
			e.printStackTrace();
			// out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			json.put("ret", "0");
			json.put("msg", e.getMessage());
			out.print(json);
			return;
		}
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);
		return;
	}
	else if (action.equals("delToDustbin")) {
		String ids = ParamUtil.get(request, "ids");
		String[] ary = StrUtil.split(ids, ",");
		if (ary!=null) {
			for (int i=0; i<ary.length; i++) {
				int flowId = StrUtil.toInt(ary[i]);
				WorkflowMgr wm = new WorkflowMgr();
				wm.del(request, flowId);
			}
		}
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>流程列表</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<style>
		.search-form input, select {
			vertical-align: middle;
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
	<script>
		var isLeftMenuShow = true;
		function closeLeftMenu() {
			if (isLeftMenuShow) {
				window.parent.setCols("0,*");
				isLeftMenuShow = false;
				btnName.innerHTML = "打开菜单";
			} else {
				window.parent.setCols("200,*");
				isLeftMenuShow = true;
				btnName.innerHTML = "关闭菜单";
			}
		}
	</script>
</head>
<body>
<div id="bodyBox">
<%--<%@ include file="flow_inc_menu_top.jsp"%>
<script>
o("menu5").className="current";
</script>--%>
<%
WorkflowDb wf = new WorkflowDb();

// String sql = "select id from flow where status<>" + WorkflowDb.STATUS_NONE + " and status<>" + WorkflowDb.STATUS_DELETED;
String sql = "select id from flow where status>" + WorkflowDb.STATUS_NONE;
if (op.equals("search")) {
	if (by.equals("user")) {
		// sql = "select id from flow where username like " + StrUtil.sqlstr("%" + what + "%") +  " order by begin_date desc";
		if (!what.equals("")) {
			// 有管理权，则可在全部流程中搜索
			if (privilege.isUserPrivValid(request, "admin") && typeCode.equals("")) {
				sql = "select f.id from flow f, users u where f.username=u.name and u.realName like " + StrUtil.sqlstr("%" + what + "%");
			}
			else {
				if (!Leaf.CODE_ROOT.equals(typeCode)) {
					sql = "select f.id from flow f, users u where f.username=u.name and f.type_code=" + StrUtil.sqlstr(typeCode) + " and u.realName like " + StrUtil.sqlstr("%" + what + "%");
				}
				else {
					sql = "select f.id from flow f, users u where f.username=u.name and u.realName like " + StrUtil.sqlstr("%" + what + "%");
				}
			}
		}
		else {
			// 有管理权，则可在全部流程中搜索
			if (privilege.isUserPrivValid(request, "admin") && typeCode.equals("")) {
				sql = "select f.id from flow f where 1=1";
			}
			else {
				if (!Leaf.CODE_ROOT.equals(typeCode)) {
					sql = "select f.id from flow f where f.type_code=" + StrUtil.sqlstr(typeCode);
				}
				else {
					sql = "select f.id from flow f where 1=1";
				}
			}
		}
	}
	else if (by.equals("title")) {
		// 有管理权，则可在全部流程中搜索
		if (privilege.isUserPrivValid(request, "admin") && typeCode.equals("")) {
			sql = "select id from flow f where title like " + StrUtil.sqlstr("%" + what + "%");
		}
		else {
			if (!Leaf.CODE_ROOT.equals(typeCode)) {
				sql = "select id from flow f where title like " + StrUtil.sqlstr("%" + what + "%") + " and type_code=" + StrUtil.sqlstr(typeCode);
			}
			else {
				sql = "select id from flow f where title like " + StrUtil.sqlstr("%" + what + "%") ;
			}
		}
	}
	else if (by.equals("ID")) {
		if (privilege.isUserPrivValid(request, "admin")) {
			if (what.equals("")) {
				sql = "select id from flow f where 1=1";
			}
			else {
				sql = "select id from flow f where id=" + StrUtil.toInt(what, -1);
			}
		}
		else {
			if (what.equals("")) {
				if (!Leaf.CODE_ROOT.equals(typeCode)) {
					sql = "select id from flow f where 1=1 and type_code=" + StrUtil.sqlstr(typeCode);
				}
				else {
					sql = "select id from flow f where 1=1";
				}
			}
			else {
				if (!Leaf.CODE_ROOT.equals(typeCode)) {
					sql = "select id from flow f where id=" + what + " and type_code=" + StrUtil.sqlstr(typeCode);
				}
				else {
					sql = "select id from flow f where id=" + what;
				}
			}
		}
	}

	if (!fromDate.equals("") && !toDate.equals("")) {
		java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
		d = DateUtil.addDate(d, 1);
		String toDate2 = DateUtil.format(d, "yyyy-MM-dd");		
		sql += " and (BEGIN_DATE>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd") + " and BEGIN_DATE<=" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd") + ")";
	}
	else if (!fromDate.equals("")) {
		sql += " and BEGIN_DATE>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
	}
	else if (fromDate.equals("") && !toDate.equals("")) {
		sql += " and BEGIN_DATE<=" + SQLFilter.getDateStr(toDate, "yyyy-MM-dd");
	}
	
	if (!status.equals("")) {
		sql += " and status=" + status;
	}
	else
		sql += " and status<>" + WorkflowDb.STATUS_NONE + " and status<>" + WorkflowDb.STATUS_DELETED;
}
else {
	if (!typeCode.equals("")) {
		/*
		LeafPriv lp = new LeafPriv(typeCode);
		if (!lp.canUserSee(privilege.getUser(request))) {
			out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		*/
		if (!Leaf.CODE_ROOT.equals(typeCode)) {
			sql = "select id from flow f where type_code=" + StrUtil.sqlstr(typeCode);
			sql += " and f.status<>" + WorkflowDb.STATUS_NONE + " and status<>" + WorkflowDb.STATUS_DELETED;
		}
		else {
			sql = "select id from flow f where f.status<>" + WorkflowDb.STATUS_NONE + " and status<>" + WorkflowDb.STATUS_DELETED;
		}
	}
}

sql += " order by id desc";

// out.print(sql);

ListResult lr = wf.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
%>
<table width="100%" border="0" align="center">
<form name="form1" class="form-search" action="?op=search" method=post>
  <tr>
    <td align="center">按
	&nbsp;
	<select name="by">
	<!--<option value="">不限</option>-->
	<option value="title">标题</option>
	<option value="user">发起人</option>
	<option value="ID">流程号</option>
	</select>
	&nbsp;
	<input name="what" value="<%=what%>" size="10" />	
	&nbsp;流程状态&nbsp;
	<select name="status">
	<option value="">不限(不含<%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NONE)%>及<%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DELETED)%>)</option>
	<option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
	<option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
	<option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
	<option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
	<option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
	<option value="<%=WorkflowDb.STATUS_NONE%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NONE)%></option>
	<option value="<%=WorkflowDb.STATUS_DELETED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DELETED)%></option>
	</select>	
<%
if (op.equals("search")) {
%>
	<script>
	form1.by.value = "<%=by%>";
	form1.status.value = "<%=status%>";
	</script>
<%}%>
	&nbsp;从
    <input size="10" id="fromDate" name="fromDate" value="<%=fromDate%>" />
	
        至
    <input size="10" id="toDate" name="toDate" value="<%=toDate%>" />
		
    <input class="btn" value="搜索" type=submit />
	<input name="typeCode" type="hidden" value="<%=typeCode%>" />
	
	  <script>
		//if (typeof(window.parent.flowPredefineLeftFrame)=="object"){
		//	var btnN = "关闭菜单";
		//	if (window.parent.getCols()!="200,*"){
		//		btnN = "打开菜单";
		//		isLeftMenuShow=false;
		//	}
		//	document.write("&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">");
		//	document.write(btnN);
		//	document.write("</span></a>");
		//}
		</script>		
	</td>
  </tr>
</form>  
</table>
	<table style="margin-bottom:5px" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
		<tr>
			<td width="36%" height="24" align="left"><%if (lp.canUserExamine(privilege.getUser(request))) {%>
				<input class="btn" type="button" onClick="doDel()" value="删除"/>
				&nbsp;&nbsp;
				<input class="btn" type="button" title="使流程图的节点更新生效" onClick="refreshBatch()" value="更新"/>
				&nbsp;&nbsp;
				<input class="btn" type="button" title="放弃流程" onClick="discardBatch()" value="放弃"/>
				<%}%></td>
			<td width="64%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %>
			</b> 条　每页显示 <b><%=paginator.getPageSize() %>
			</b> 条　页次<%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %>&nbsp;&nbsp;&nbsp;&nbsp;
				<%
					String querystr = "op=" + op + "&by=" + by + "&typeCode=" + StrUtil.UrlEncode(typeCode) + "&what=" + StrUtil.UrlEncode(what) + "&status=" + status + "&fromDate=" + fromDate + "&toDate=" + toDate;
					out.print(paginator.getCurPageBlock("?" + querystr, "up"));%></td>
		</tr>
	</table>
<table id="mainTable" width="98%" class="tabStyle_1 percent98">
 <thead>
    <tr>
      <td width="3%" align="center" class="tabStyle_1_title" style="text-align:center"><input type="checkbox" onClick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids');" /></td>
      <td width="4%" align="left" class="tabStyle_1_title" style="text-align:left">&nbsp;</td>
      <td width="6%" align="center" class="tabStyle_1_title" style="text-align:center">ID</td>
      <td class="tabStyle_1_title" width="33%">标题</td>
      <td class="tabStyle_1_title" width="10%">类型</td>
      <td class="tabStyle_1_title" width="12%">开始时间</td>
      <td class="tabStyle_1_title" width="8%">发起人</td>
      <td class="tabStyle_1_title" width="7%">状态</td>
      <td class="tabStyle_1_title" width="17%">管理</td>
    </tr>
     </thead>
     <tbody>
    <%
Leaf ft = new Leaf();
ft = ft.getLeaf(typeCode);
FormDb fd = new FormDb();
if (!StrUtil.isEmpty(ft.getFormCode())) {
	fd = fd.getFormDb(ft.getFormCode());
}
FormDAO fdao = new FormDAO();
UserMgr um = new UserMgr();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next();

	if ("ID".equals(by)) {
		ft = ft.getLeaf(wfd.getTypeCode());
		fd = fd.getFormDb(ft.getFormCode());
		fdao = fdao.getFormDAO(wfd.getId(), fd);
	}
	else {
		if (!fd.isLoaded()) {
			ft = ft.getLeaf(wfd.getTypeCode());
			fd = fd.getFormDb(ft.getFormCode());
		}
		fdao = fdao.getFormDAO(wfd.getId(), fd);
	}

	UserDb user = null;
	if (wfd.getUserName()!=null) {
		user = um.getUserDb(wfd.getUserName());
	}
	String userRealName = "";
	if (user!=null) {
		userRealName = user.getRealName();
	}
	%>
	<tr id="tr_<%=wfd.getId()%>" class="highlight">
		<td align="center"><input type="checkbox" name="ids" value="<%=wfd.getId()%>"/></td>
		<td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%>
		</td>
		<td align="center"><%=wfd.getId()%>
		</td>
		<td><a href="javascript:;"
			   onClick="addTab('<%=StrUtil.HtmlEncode(wfd.getTitle())%>', '<%=request.getContextPath()%>/flowShowPage.do?flowId=<%=wfd.getId()%>')"><%=wfd.getTitle()%>
		</a></td>
		<td>
			<%
				Leaf lf = ft.getLeaf(wfd.getTypeCode());
				if (lf != null)
					out.print(lf.getName());
			%>
		</td>
		<td align="center"><%=DateUtil.format(wfd.getBeginDate(), "yy-MM-dd HH:mm:ss")%>
		</td>
		<td><%=userRealName%>
		</td>
		<td align="center" id="tdStatus<%=wfd.getId()%>"><%=wfd.getStatusDesc()%>
		</td>
		<td align="center">
			<%
				WorkflowPredefineDb wfp = new WorkflowPredefineDb();
				if (lf != null) {
					wfp = wfp.getPredefineFlowOfFree(lf.getCode());
				}
				String taburl = request.getContextPath() + "/" + (wfp.isLight() ? "flow_dispose_light_show.jsp" : "flowShowPage.do") + "?flowId=" + wfd.getId();
				String modifyUrl = request.getContextPath() + "/" + "visual/moduleEditPage.do?id=" + fdao.getId() + "&code=" + ft.getFormCode() + "&parentId=" + fdao.getId() + "&formCode=" + ft.getFormCode();
			%>
			<a href="javascript:;" onClick="addTab('<%=StrUtil.HtmlEncode(wfd.getTitle())%>', '<%=taburl%>')">查看</a>
			&nbsp;&nbsp; <a href="javascript:;"
							onClick="addTab('<%=StrUtil.HtmlEncode(wfd.getTitle())%>', '<%=modifyUrl%>')">修改</a>
			<!--
      <%if (lp.canUserExamine(privilege.getUser(request))) {%>
      	&nbsp;&nbsp;
		<a href="javascript: jConfirm('您确定要删除吗?','提示',function(r){ if(!r){return;}else{window.location.href='flow_list.jsp?action=del&ids=<%=wfd.getId() + "&CPages=" + strcurpage + "&op=" + op + "&by=" + by + "&typeCode=" + StrUtil.UrlEncode(typeCode) + "&what=" + what%>'}}) " style="cursor:pointer">删除</a>
	  <%}%>
      -->
			<%
				// if (lf.isDebug() && wfd.getStatus()!=WorkflowDb.STATUS_FINISHED && wfd.getStatus()!=WorkflowDb.STATUS_REFUSED && wfd.getStatus()!=WorkflowDb.STATUS_DISCARDED && privilege.isUserPrivValid(request, "admin")) {
				if (privilege.isUserPrivValid(request, "admin.flow")) {
			%>
			&nbsp;&nbsp;<a href="javascript:;" onClick="refreshFlow('<%=wfd.getId()%>')" title="使流程图的节点更新生效">更新</a>
			<%}%>
			<%if (wfd.getStatus() == WorkflowDb.STATUS_DELETED) {%>
			&nbsp;&nbsp;<a href="javascript:;" onClick="recover('<%=wfd.getId()%>')">恢复</a>
			<%}%>
			&nbsp;&nbsp; <a href="javascript:;"
							onClick="addTab('干预：<%=StrUtil.HtmlEncode(wfd.getTitle())%>', '<%=request.getContextPath()%>/admin/flow_intervene.jsp?flowId=<%=wfd.getId()%>')">干预</a>

		</td>
	</tr>
<%}%>
  </tbody>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
  <tr>
    <td width="48%" align="left">&nbsp;</td><td width="52%" align="right"><%
    out.print(paginator.getCurPageBlock("?"+querystr, "down"));
%></td>
  </tr>
</table>
</div>
</body>
<script language="javascript">
	function doDel() {
		var ids = getCheckboxValue("ids");
		if (ids == "") {
			jAlert("请选择流程！", "提示");
			return;
		}
		var action = "del";
		var msg = "您确定要删除吗？";
		<%if (status.equals(String.valueOf(WorkflowDb.STATUS_DELETED))) {%>
		msg = "您确定要彻底删除么？";
		<%}else{%>
		msg = "您确定要删除至回收站么？";
		action = "delToDustbin";
		<%}%>
		jConfirm(msg, "提示", function (r) {
			if (!r) {
				return;
			} else {
				$.ajax({
					type: "post",
					url: "flow_list.jsp",
					data: {
						action: action,
						typeCode: "<%=typeCode%>",
						ids: ids
					},
					dataType: "html",
					beforeSend: function (XMLHttpRequest) {
						$('#bodyBox').showLoading();
					},
					success: function (data, status) {
						data = $.parseJSON(data);
						jAlert(data.msg, "提示");
						if (data.ret == "1") {
							var ary = ids.split(',');
							for (var i = 0; i < ary.length; i++) {
								$('#tr_' + ary[i]).remove();
							}
						}
					},
					complete: function (XMLHttpRequest, status) {
						$('#bodyBox').hideLoading();
						// $('#tdStatus' + flowId).html('<%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%>');
					},
					error: function (XMLHttpRequest, textStatus) {
						// 请求出错处理
						jAlert(XMLHttpRequest.responseText, "提示");
					}
				});
				// window.location.href = "flow_list.jsp?action=" + action + "&ids=" + ids + "&CPages=<%=strcurpage%>&op=<%=op%>&by=<%=by%>&typeCode=<%=StrUtil.UrlEncode(typeCode)%>&what=<%=StrUtil.UrlEncode(what)%>&status=<%=status%>";
			}
		})
	}

function selAllCheckBox(checkboxname) {
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

function form1_onsubmit() {
	errmsg = "";
	if (form1.pwd.value!=form1.pwd_confirm.value)
		errmsg += "密码与确认密码不致，请检查！\n"
	if (errmsg!="")
	{
		jAlert(errmsg,"提示");
		return false;
	}
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
	$('#fromDate').datetimepicker({
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d'
     });
     $('#toDate').datetimepicker({
      	lang:'ch',
      	timepicker:false,
      	format:'Y-m-d'
      });
});

function recover(flowId) {
	jConfirm("您确定要恢复么？","提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
				type: "post",
				url: "../flow/recover.do",
				data: {
					flowId: flowId
				},
				dataType: "html",
				beforeSend: function (XMLHttpRequest) {
					$('#bodyBox').showLoading();
				},
				success: function (data, status) {
					data = $.parseJSON(data);
					jAlert(data.msg, "提示");
				},
				complete: function (XMLHttpRequest, status) {
					$('#bodyBox').hideLoading();
					$('#tdStatus' + flowId).html('<%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%>');
				},
				error: function (XMLHttpRequest, textStatus) {
					// 请求出错处理
					jAlert(XMLHttpRequest.responseText, "提示");
				}
			});
		}
	})	
}

function refreshBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请选择流程！","提示");
		return;
	}
	jConfirm("您确定要更新么？","提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
			type: "post",
			url: "../flow/refreshFlowBatch.do",
			data : {
				ids: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#bodyBox').showLoading();				
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg,"提示");
			},
			complete: function(XMLHttpRequest, status){
				$('#bodyBox').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});	
		}
	})
}

function discardBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请选择流程！","提示");
		return;
	}
	jConfirm("您确定要放弃么？","提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
				type: "post",
				url: "../flow/discardFlowBatch.do",
				data : {
					ids: ids
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					$('#bodyBox').showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data);
					jAlert(data.msg,"提示");
					if (data.ret=="1") {
						var ary = ids.split(",");
						for (var i=0; i<ary.length; i++) {
							$('#tdStatus' + ary[i]).html('<%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%>');
						}
					}
				},
				complete: function(XMLHttpRequest, status){
					$('#bodyBox').hideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					jAlert(XMLHttpRequest.responseText,"提示");
				}
			});
		}
	})
}

function refreshFlow(flowId) {
	jConfirm("您确定要更新么？","提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
			type: "post",
			url: "../flow/refreshFlow.do",
			data : {
				flowId: flowId
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#bodyBox').showLoading();				
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg,"提示");
			},
			complete: function(XMLHttpRequest, status){
				$('#bodyBox').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});	
		}
	})
}
</script>
</html>