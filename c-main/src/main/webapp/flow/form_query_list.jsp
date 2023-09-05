<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@ page import="com.cloudweb.oa.entity.Menu" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudweb.oa.service.IMenuService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
		%>
        <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	boolean isSystem = ParamUtil.get(request, "isSystem").equals("true");
	
	// 管理员权限，才能操作系统查询
	if (isSystem && !privilege.isUserPrivValid(request, "admin")) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String op = ParamUtil.get(request, "op");
	FormQueryMgr aqm = new FormQueryMgr();
	
    if (op.equals("modifyName")) {
		response.setContentType("application/x-json"); 
		
		int id = ParamUtil.getInt(request, "id");
		String newName = ParamUtil.get(request, "newName");
		if (newName.equals("")) {
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			json.put("msg", "查询名称不能为空！");
			out.print(json);		
			return;
		}
		
		FormQueryDb aqd = aqm.getFormQueryDb(id);
		aqd.setQueryName(newName);
		boolean re = aqd.save();
		if (re) {
			// out.print("{\"ret\":\"1\", \"msg\":\"操作成功！\"}");
			JSONObject json = new JSONObject();
			json.put("ret", "1");
			json.put("msg", "操作成功！");
			out.print(json);
		}
		else {
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			json.put("msg", "操作失败！");
			out.print(json);
		}	
		return;	
	}
	else if(op.equals("addToMenu")) {
		boolean re = false;
		int id = ParamUtil.getInt(request, "id", -1);
		if(id == -1) {
			out.print(StrUtil.jAlert_Back("请指定需要设置为菜单项的查询！","提示"));
		}
		FormQueryDb archiveQueryDb = new FormQueryDb();
		archiveQueryDb = archiveQueryDb.getFormQueryDb(id);
		if(archiveQueryDb==null || !archiveQueryDb.isLoaded()) {
			out.print(StrUtil.jAlert_Back("指定的查询不存在！","提示"));
		}
		String name = archiveQueryDb.getQueryName();
		String codePrefix = "query_";//设置到菜单时的编码前缀
		String code = codePrefix + id;
		String parentCode = "2075285733";
		String link = "archive/form_query_list.jsp?id=" + id + "&op=query";
		String target = "mainFrame";
		String icon = "OAimg53.gif";

		com.cloudweb.oa.entity.Menu menu = new Menu();

		menu.setName(name);
		menu.setCode(code);
		menu.setParentCode(parentCode);
		menu.setLink(link);
		menu.setType(0);
		menu.setPreCode("");
		menu.setWidth(0);
		menu.setIsHasPath(0);
		menu.setIsResource(0);
		menu.setTarget(target);
		menu.setPvg(ConstUtil.PRIV_ADMIN);
		menu.setIcon(icon);
		menu.setIsUse(0);
		menu.setIsNav(0);
		IMenuService menuService = SpringUtil.getBean(IMenuService.class);
		re = menuService.create(menu);

		if(re) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_query_list.jsp?isSystem=" + isSystem));
		} else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>查询列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
	.search-form input, select {
		vertical-align: middle;
	}
	.search-form .tSearch {
		/*float: left;*/
	}
	.search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]):not([type="submit"]) {
		width: 80px;
		line-height: 20px; /*否则输入框的文字会偏下*/
	}
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/flexigrid.js"></script>
<script src="../js/jquery.form.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
</head>
<body>
<%@ include file="form_query_nav.jsp"%>
<script>
o("menu0").className="current"; 
</script>
<%
	String orderBy = ParamUtil.get(request, "orderBy");
	String sort = ParamUtil.get(request, "sort");
	if (orderBy.equals(""))
		orderBy = "id";
	if (sort.equals(""))
		sort = "desc";
	
	String sql = "";
	if (!isSystem)
		sql = "select id from form_query where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and is_saved=1";

	else {
		sql = "select id from form_query where is_system=1 and is_saved=1";
	}
	String queryKey = ParamUtil.get(request, "queryKey");
	if(op.equals("query")) {
		if (!isSystem) {
			sql = "select id from form_query where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and query_name like " + StrUtil.sqlstr("%" + queryKey + "%") + " and is_saved=1";
		} else {
			sql = "select id from form_query where is_system=1 and query_name like " + StrUtil.sqlstr("%" + queryKey + "%") + " and is_saved=1";
		}
	}
	
	sql += " order by " + orderBy + " " + sort;
	
	// System.out.println(getClass() + " " + sql);
	
	String querystr = "op=" + op + "&queryKey=" + StrUtil.UrlEncode(queryKey) + "&isSystem=" + ((isSystem)?"true":"false");
	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
			
	FormQueryDb aqd = new FormQueryDb();		
	ListResult lr = aqd.listResult(sql, curpage, pagesize);
	long total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = null;
	
	if (v!=null) {
		ir = v.iterator();
	}
		
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}

	FormDb fd = new FormDb();
%>
      	<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
			<tr>
				<td>
					<form name="queryForm" class="search-form" method="post" action="form_query_list.jsp?op=query">
						&nbsp;名称
						<input type="text" name="queryKey" value="<%=queryKey%>"/>&nbsp;&nbsp;&nbsp;&nbsp;<input type="submit" class="tSearch" value="查找"/>
					</form>
				</td>
			</tr>
        </table>
	    <table id="grid" border="0" cellpadding="2" cellspacing="0" >
        <thead>
          <tr>
            <th width="225" height="24" >查询名称</th>
            <th width="97" >表单</th>
            <th width="162" >主表关联查询</th>
            <th width="101" >类型</th>
            <th width="150" abbr="time_point" >时间</th>
            <th width="335">操作</th>
          </tr>
        </thead>
	    <%
		while (ir!=null && ir.hasNext()) {
			aqd = (FormQueryDb)ir.next();
			if (!aqd.isScript()) {
				fd = fd.getFormDb(aqd.getTableCode());
			}
		%>
        <tr align="center" id=<%=aqd.getId()%> isScript="<%=aqd.isScript()%>">
          <td width="225" height="22" align="left"><span id="queryName<%=aqd.getId()%>"><%=aqd.getQueryName()%></span></td>
          <td width="97" align="left"><%=!aqd.isScript()?fd.getName():""%></td>
          <td width="162" align="left">
         <%
         String queryRelated = aqd.getQueryRelated();
         if (!queryRelated.equals("")) {
             int queryRelatedId = StrUtil.toInt(queryRelated, -1);
             if (queryRelatedId!=-1) {
				 FormQueryDb fqd = aqd.getFormQueryDb(queryRelatedId);
				%>
				<a href="javascript:;" onClick="top.mainFrame.addTab('设计器', '<%=request.getContextPath()%>/flow/designer/designer.jsp?id=<%=fqd.getId()%>')"><%=fqd.getQueryName()%></a>
				<%
             }
         }          
         %>
          </td>
          <td width="101" align="left">
          <%if (aqd.isScript()) {%>
          自由查询
          <%}else{%>
          普通查询
          <%}%>
          </td>
          <td width="150" align="center"><%=DateUtil.format(aqd.getTimePoint(), "yyyy-MM-dd HH:mm:ss")%></td> 
          <td>
            <a href="javascript:;" onClick="rename('<%=aqd.getId()%>', '<%=aqd.getQueryName()%>')">重命名</a>&nbsp;&nbsp;
            <a href="javascript:;" onClick="addTab('查询授权', '<%=request.getContextPath()%>/flow/form_query_user.jsp?id=<%=aqd.getId()%>')">授权</a>&nbsp;&nbsp;
            <%if (!aqd.isScript()) {%>
            <a href="javascript:;" onClick="addTab('<%=aqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/form_query_list_do.jsp?id=<%=aqd.getId()%>&op=query')">查询</a>&nbsp;&nbsp;
            <%}else{%>
            <a href="javascript:;" onClick="addTab('<%=aqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp?id=<%=aqd.getId()%>&op=query')">查询</a>&nbsp;&nbsp;
            <%}%>
            <%if (!aqd.isScript()) {%>
            <a href="javascript:;" onClick="addTab('图表', '<%=request.getContextPath()%>/flow/form_query_chart_pie.jsp?id=<%=aqd.getId()%>')">图表</a>
            <%}%>
            <!--&nbsp;&nbsp;<a href="form_query_list.jsp?id=<%=aqd.getId()%>&op=addToMenu">添加到菜单</a>-->
            </td>
          </tr>
      <%}%>	 
      </table>
<form id="form1" name="form1" method="post" action="form_query_list.jsp">
	<input name="op" value="modifyDeptCode" type="hidden"/>
	<input name="deptCodes" type="hidden"/>
	<input name="id" type="hidden"/>
</form>

<div id="dlg" style="display:none">
<form id="frm" name="frm" action="form_query_list.jsp">
  <table width="100%" style="border:0px">
    <tr>
      <td>名称&nbsp;<input id="newName" name="newName" value="" /></td>
    </tr>
  <tr>
    <td><input name="op" type="hidden" value="modifyName" /><input id="id" name="id" value="check" type="hidden" /></td>
  </tr>
</table>
</form>
</div>
<div id="result"></div>
</body>
<script>
var flex;

var buttonObj;
buttonObj = [
		{name: '设计', bclass: 'edit', onpress : action},
		{name: '删除', bclass: 'delete', onpress : action},		
		{separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		];

$(document).ready(function() { 
    var options = { 
        //target:        '#output2',   // target element(s) to be updated with server response 
        //beforeSubmit:  function() {alert('d');},  // pre-submit callback 
        success:       showResponse,  // post-submit callback 
 
        // other available options: 
        //url:       url         // override for form's 'action' attribute 
        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
        dataType:  'json'        // 'xml', 'script', or 'json' (expected server response type) 
        //clearForm: true        // clear all form fields after successful submit 
        //resetForm: true        // reset the form after successful submit 
 
        // $.ajax options can be used here too, for example: 
        //timeout:   3000 
    };

    // bind to the form's submit event 
    $('#frm').submit(function() {
        $(this).ajaxSubmit(options); 
        return false; 
    });
	
	
	flex = $("#grid").flexigrid
	(
		{
		buttons : buttonObj,
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
	window.location.href = "form_query_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "form_query_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>";
}

function rpChange(pageSize) {
	window.location.href = "form_query_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>";
}

function onReload() {
	window.location.reload();
}

var curId;

function showResponse(data)  {
	if (data.ret==1) {
		var newName = $("#newName").val();
		$("#result").html(data.msg);
		$("#result").dialog({title:"提示", modal: true, buttons: { "确定": function() { $(this).dialog("close"); $("#queryName" + curId).html(newName)}}, closeOnEscape: true, draggable: true, resizable:true });
	}
	else {
		$("#result").html(data.msg);
		$("#result").dialog({title:"提示", modal: true, buttons: { "确定": function() { $(this).dialog("close");}}, closeOnEscape: true, draggable: true, resizable:true });
	}
}

function rename(id, name) {
	curId = id;
	$("#dlg #id").val(id);
	$("#newName").val(name);
	$("#dlg").dialog({title:"修改查询名称", modal: true, 
						buttons: {
							"取消":function() {
								$(this).dialog("close");
							},
							"确定": function() {
								$('#frm').submit();
								$(this).dialog("close");
							}
						}, 
						closeOnEscape: true, 
						draggable: true, 
						resizable:true,
						width:350
					});
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

function action(com, grid) {
	if (com=='设计')	{
		var selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount > 1 || selectedCount==0) {
			jAlert('请选择一条记录!','提示');
			return;
		}
		
		$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
			var id = $(this).val();
			var isScript = $("tr[id=" + id + "]").attr("isScript");
			if (isScript=="true") {
				addTab('设计器', '<%=request.getContextPath()%>/flow/form_query_script.jsp?id=' + id + '&isSystem=<%=isSystem%>');				
			}
			else {
				addTab('设计器', '<%=request.getContextPath()%>/flow/designer/designer.jsp?id=' + id + '&isSystem=<%=isSystem%>');
			}
		});
		
	}
	else if (com=="删除") {
		var selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择记录!','提示');
			return;
		}
		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return;}
			else{
				var ids = "";
				$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
					if (ids=="")
						ids = $(this).val();
					else
						ids += "," + $(this).val();
				});			
					
				window.location.href = "form_query_do.jsp?op=del&CPages=<%=curpage%>&isSystem=<%=isSystem%>&ids=" + ids;
			}
		})
	}
}
</script>
</html>
