<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request,
				cn.js.fan.web.SkinUtil.LoadString(request,
						"pvg_invalid")));
		return;
	}

	String op = StrUtil.getNullString(request.getParameter("op"));
	String typeCode = ParamUtil.get(request, "typeCode");
	String title = ParamUtil.get(request, "title");
	String flowStatus = ParamUtil.get(request, "flowStatus");
	if (flowStatus.equals(""))
		flowStatus = "1000";

	String myname = ParamUtil.get(request, "userName");

	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request,
				privilege, "userName", myname, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request,
				privilege, "typeCode", typeCode, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request,
				privilege, "title", title, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request,
				privilege, "op", op, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request,
				privilege, "flowStatus", flowStatus, getClass()
						.getName());
	} catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e
				.getMessage()));
		return;
	}

	if (myname.equals("")) {
		myname = privilege.getUser(request);
	}
	if (!myname.equals(privilege.getUser(request))) {
		if (!(privilege.canAdminUser(request, myname))) {
			out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil
					.LoadString(request, "pvg_invalid")));
			return;
		}
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>
		<%
			String kind = com.redmoon.oa.kernel.License.getInstance().getKind();
			if (kind.equalsIgnoreCase(com.redmoon.oa.kernel.License.KIND_COM)) {
		%>
		<lt:Label res="res.flow.Flow" key="notify"/>
		<%
		} else {
		%>
		<lt:Label res="res.flow.Flow" key="distribute"/>
		<%
			}
		%>
	</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

	<script>
		function onTypeCodeChange(obj) {
			if (obj.options[obj.selectedIndex].value == 'not') {
				alert(obj.options[obj.selectedIndex].text + ' 不能被选择！');
				return false;
			}
			window.location.href = "flow_distribute.jsp?op=search&typeCode=" + obj.options[obj.selectedIndex].value + "&flowStatus=" + o("flowStatus").value;
		}

		function openWin(url, width, height) {
			if (width > window.screen.width)
				width = window.screen.width;
			if (height > window.screen.height)
				height = window.screen.height;
			var l = (window.screen.width - width) / 2;
			var t = (window.screen.height - height) / 2;
			var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=" + t + ",left=" + l + ",width=" + width + ",height=" + height);
		}

		function saveArchiveGov(flowId, actionId) {
			openWin("../visual/module_add.jsp?formCode=archive_files&flowId=" + flowId + "&actionId=" + actionId, 800, 600);
		}
	</script>
</head>
<body>
<div id="bodyBox">
<div id="dlg" style="display:none"></div>
<table id="searchTable" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
        <form id="formSearch" class="search-form" name="formSearch" action="flow_distribute.jsp" method="get">
          &nbsp;类型
          <select id="typeCode" name="typeCode" onchange="onTypeCodeChange(this)">
		  <option value="">不限</option>
		  <%
		  	Leaf lf = new Leaf();
		  	lf = lf.getLeaf("root");
		  	DirectoryView dv = new DirectoryView(lf);
		  	dv.ShowDirectoryAsOptions(request, out, lf, 1);
		  %>
          </select>&nbsp;&nbsp;标题&nbsp;&nbsp;
          <input name="title" value="<%=title%>" />
          <input name="userName" value="<%=myname%>" type="hidden" />
          <input name="op" value="search" type="hidden" />
          	 状态&nbsp;<select id="flowStatus" name="flowStatus">
          <option value="1000" selected>不限</option>
          <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
          <option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
          <option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
          <option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
          <option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
        </select>            
		<input name="submit" type=submit value="搜索" class="tSearch" />
        </form>
        </td>
    </tr>
</table>

<%
	String strcurpage = StrUtil.getNullString(request
			.getParameter("CPages"));
	if (strcurpage.equals(""))
		strcurpage = "1";
	if (!StrUtil.isNumeric(strcurpage)) {
		out.print(StrUtil.makeErrMsg("标识非法！"));
		return;
	}
	int pagesize = ParamUtil.getInt(request, "pageSize", 20);
	int curpage = Integer.parseInt(strcurpage);

	WorkflowDb wf = new WorkflowDb();

	String orderBy = ParamUtil.get(request, "orderBy");
	if (orderBy.equals(""))
		orderBy = "flow_id";
	String sort = ParamUtil.get(request, "sort");
	if (sort.equals(""))
		sort = "desc";

	String sql = "select distinct m.flow_id from flow_my_action m, flow_action a where m.action_id=a.id and a.can_distribute=1 and (m.user_name="
			+ StrUtil.sqlstr(myname)
			+ " or m.proxy="
			+ StrUtil.sqlstr(myname) + ")";
	if (op.equals("search")) {
		sql = "select distinct m.flow_id from flow_my_action m, flow_action a, flow f where f.id=m.flow_id and m.action_id=a.id and a.can_distribute=1 and (m.user_name="
				+ StrUtil.sqlstr(myname)
				+ " or m.proxy="
				+ StrUtil.sqlstr(myname) + ")";
		if (!typeCode.equals("")) {
			sql += " and f.type_code=" + StrUtil.sqlstr(typeCode);
		}
		if (!title.equals("")) {
			sql += " and f.title like "
					+ StrUtil.sqlstr("%" + title + "%");
		}
		if (flowStatus.equals("") || flowStatus.equals("1000"))
			;
		else {
			sql += " and f.status=" + flowStatus;
		}
	}

	sql += " order by " + orderBy + " " + sort;
	
	// out.print(sql);

	ListResult lr = wf.listResult(sql, curpage, pagesize);

	// ListResult lr = wf.listUserAttended(privilege.getUser(request), curpage, pagesize);
	long total = lr.getTotal();
	Paginator paginator = new Paginator(request, total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages == 0) {
		curpage = 1;
		totalpages = 1;
	}

	Vector v = lr.getResult();
	Iterator ir = null;
	if (v != null)
		ir = v.iterator();
%>
<table id="grid">
<thead>
    <tr>
      <th width="37" align="center" abbr="flow_id">ID</th>    
      <th width="28" align="center" abbr="flow_level">等级</th>
      <th width="250">标题</th>
      <th width="90" abbr="type_code">类型</th>
      <th width="70">处理步骤</th>
      <th width="80" abbr="begin_date">开始时间</th>
      <th width="70" abbr="userName">发起人</th>
      <th width="75">当前处理</th>
      <th width="80" abbr="status">状态</th>
      <th width="120" align="center">操作</th>
    </tr>
</thead>    
  <tbody>
<%
	WorkflowMgr wm = new WorkflowMgr();
	Leaf ft = new Leaf();
	MyActionDb mad = new MyActionDb();
	com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
	while (ir.hasNext()) {
		WorkflowDb wfd = (WorkflowDb) ir.next();
		UserDb user = null;
		if (wfd.getUserName() != null)
			user = um.getUserDb(wfd.getUserName());
		String userRealName = "";
		if (user != null)
			userRealName = user.getRealName();
%>
    <tr>
      <td align="center"><%=wfd.getId()%></td>    
      <td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%></td>
      <td><a href="../flow_modify.jsp?flowId=<%=wfd.getId()%>" title="<%=wfd.getTitle()%>"><%=wfd.getTitle()%></a></td>
      <td>
	  <%
	  	lf = ft.getLeaf(wfd.getTypeCode());
	  %>
	  <%
	  	if (lf != null) {
	  %>
	  	<a href="flow_distribute.jsp?op=search&typeCode=<%=StrUtil.UrlEncode(lf.getCode())%>&flowStatus=<%=flowStatus%>"><%=lf.getName(request)%></a>
	  <%
	  	}
	  %>
      </td>
      <td align="center"><%
      	WorkflowActionDb wad = new WorkflowActionDb();
      		wad = wad.getWorkflowActionDb((int) mad.getActionId());
      %>
      <%=StrUtil.getNullStr(wad.getTitle())%></td>
      <td align="center"><%=DateUtil.format(wfd.getBeginDate(),
								"yy-MM-dd HH:mm")%></td>
      <td><%=userRealName%></td>
      <td><%
      	String lastDate = "";
      		// sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " order by receive_date desc";
      		sql = "select id from flow_my_action where flow_id="
      				+ wfd.getId() + " and is_checked<>"
      				+ MyActionDb.CHECK_STATUS_NOT
      				+ " order by receive_date desc";
      		Iterator ir2 = mad.listResult(sql, 1, 1).getResult().iterator();

      		// CHECK_STATUS_NOT为0
      		sql = "select id from flow_my_action where flow_id="
      				+ wfd.getId() + " and is_checked="
      				+ MyActionDb.CHECK_STATUS_NOT
      				+ " order by receive_date desc";
      		ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
      		int k = 0;
      		while (ir2.hasNext()) {
      			mad = (MyActionDb) ir2.next();
      			if (k != 0) {
      				out.print("、");
      			}
      %>
        <%=um.getUserDb(mad.getUserName()).getRealName()%>
        <%
        	k++;
        		}
        %></td>
      <td class="<%=WorkflowDb.getStatusClass(wfd.getStatus())%>"><%=wfd.getStatusDesc()%></td>
      <td align="center">
      <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle())%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="查看流程进度、附言、修改流程标题等">查看</a>
      &nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:distributeDoc(<%=wfd.getId()%>,'<%=wfd.getTitle()%>',<%=mad.getActionId()%>,<%=mad.getId()%>);" title="将流程进行分发">分发</a>
      </td>
    </tr>
<%
	}
%>
  </tbody>
</table>
<%
	String querystr = "op=" + op + "&userName="
			+ StrUtil.UrlEncode(myname) + "&typeCode=" + typeCode
			+ "&flowStatus=" + flowStatus + "&title="
			+ StrUtil.UrlEncode(title);
%>
</div>
</body>
<script>
var disDepts = "";
var disNames = "";
function getDepts() {
	return disDepts;
}

/**分发流程**/
function distributeDoc(flowId,title,actionId,myActionId) {
	openWin("paper_distribute.jsp?flowId=" + flowId + "&actionId=" + actionId + "&myActionId=" + myActionId, 800, 600);
	return;
	
	var ret = showModalDialog('../unit_multi_sel.jsp?isOnlyUnitCheckable=true&isIncludeChild=false',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
				
	disDepts = "";
	disNames = "";
	for (var i=0; i<ret.length; i++) {
		if (disDepts=="") {
			disDepts += ret[i][0];
			disNames += ret[i][1];
		}
		else {
			disDepts += "," + ret[i][0];
			disNames += "," + ret[i][1];
		}
	}
	
	var str = "标题：<input name='paperTitle' value='"+title+"' /><BR />";
	str += "下级单位能否看到流程：<input id='isFlowDisplay' name='isFlowDisplay' type='radio' value='1' />是<input id='isFlowDisplay' name='isFlowDisplay' type='radio' value='0' checked />否<BR />";
	str += "单位：" + disNames;
	$("#dlg").html(str);
	$("#dlg").dialog({
		title:"请确定是否分发给以下部门",
		modal: true,
		buttons: {
			"取消":function() {
				$(this).dialog("close");
			},
			"确定": function() {				
				$.ajax({
					type: "post",
					url: "../flow/distribute.do",
					data: {
						flowId: flowId,
						actionId: actionId,
						myActionId: myActionId,
						title: o("paperTitle").value,
						depts: disDepts,
						isFlowDisplay: getRadioValue("isFlowDisplay")
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#bodyBox').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="1") {
							isDocDistributed = true;
							jAlert(data.msg, "提示");
						}
						else {
							jAlert(data.msg, "提示");
						}
					},
					complete: function(XMLHttpRequest, status){
						$('#bodyBox').hideLoading();				
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});
				
				$(this).dialog("close");
				// $('#bodyBox').showLoading();
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true,
		width:500
	});	

}



var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_distribute.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_distribute.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "flow_distribute.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
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
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
	
	o("typeCode").value = "<%=typeCode%>";
	o("flowStatus").value = "<%=flowStatus%>";
	
});

function action(com, grid) {
}

function favorite(id) {
	$.ajax({
		type: "post",
		url: "../flow/favorite.do",
		data: {
			flowId: id
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('#grid').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="0") {
				jAlert(data.msg, "提示");
			}
			else {
				jAlert(data.msg, "提示");
			}
		},
		complete: function(XMLHttpRequest, status){
			$('#grid').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function unfavorite(id) {
	jConfirm('您确定要删除么？', '提示', function(r) {
		if (r) {
			$.ajax({
				type: "post",
				url: "../flow/unfavorite.do",
				data: {
					flowId: id
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					$('#grid').showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data);
					if (data.ret=="0") {
						jAlert(data.msg, "提示");
					}
					else {
						jAlert(data.msg, "提示");
						window.location.reload();
					}
				},
				complete: function(XMLHttpRequest, status){
					$('#grid').hideLoading();				
				},
				error: function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});

}
</script>
</html>