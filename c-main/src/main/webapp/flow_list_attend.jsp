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
/*
- 功能描述：我的流程
- 访问规则：
- 过程描述：
- 注意事项：
- 创建者：fgf
- 创建时间：
==================
- 修改者：
- 修改时间：20180125
- 修改原因：本页面仅用于流程宏控件中选择我的流程，而我的流程本身的功能已合并至flow_list.jsp
- 修改点：
*/

String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
String by = ParamUtil.get(request, "by");
String typeCode = ParamUtil.get(request, "typeCode");
String title = ParamUtil.get(request, "title");
int flowStatus = ParamUtil.getInt(request, "flowStatus", -100);
if (flowStatus==-100)
	flowStatus = 1000;
String starter = ParamUtil.get(request, "starter");
	
String action = ParamUtil.get(request, "action"); // sel 选择我的流程

String viewMode = ParamUtil.get(request, "viewMode");

String myname = ParamUtil.get(request, "userName");
if(myname.equals("")){
	myname = privilege.getUser(request);
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();

String fromDate = ParamUtil.get(request, "fromDate");
String toDate = ParamUtil.get(request, "toDate");

String noteStr = LocalUtil.LoadString(request,"res.flow.Flow","prompt");
// 参数是否非法
if (!myname.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, myname))) {
		out.print(StrUtil.jAlert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"),noteStr));
		return;
	}
}	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>我参与的流程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="js/flexigrid.js"></script>

<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>

<link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
<script src="js/datepicker/jquery.datetimepicker.js"></script>

<script>
function onTypeCodeChange(obj) {
	if(obj.options[obj.selectedIndex].value=='not'){
		jAlert(obj.options[obj.selectedIndex].text+' <lt:Label res="res.flow.Flow" key="notBeSelect"/>','<lt:Label res="res.flow.Flow" key="prompt"/>'); 
		return false;
	}
	window.location.href = "flow_list_attend.jsp?op=search&typeCode=" + obj.options[obj.selectedIndex].value + "&flowStatus=" + o("flowStatus").value + "&action=<%=action%>";
}
</script>
</head>
<body>
<%if (!action.equals("sel") && viewMode.equals("")) {%>
<%@ include file="flow_inc_menu_top.jsp"%>
<script>
<%if (viewMode.equals("favorite")) {%>
o("menu5").className="current"; 
<%}else{%>
o("menu1").className="current";
<%}%>
</script>
<%}%>
<table id="searchTable" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
        <form name="formSearch" action="flow_list_attend.jsp" method="get">
          &nbsp;<lt:Label res="res.flow.Flow" key="type"/>
          <select id="typeCode" name="typeCode" onchange="onTypeCodeChange(this)">
		  <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
          </select>&nbsp;&nbsp;
            <select id="by" name="by">
              <option value="title"><lt:Label res="res.flow.Flow" key="tit"/></option>
              <option value="flowId"><lt:Label res="res.flow.Flow" key="number"/></option>
            </select>
            &nbsp;          
          <input type="text" name="title" value="<%=title%>" />
          <input name="userName" value="<%=myname%>" type="hidden" />
          <input name="op" value="search" type="hidden" />
          <lt:Label res="res.flow.Flow" key="state"/>&nbsp;<select id="flowStatus" name="flowStatus">
          <option value="1000" selected><lt:Label res="res.flow.Flow" key="limited"/></option>
          <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
          <option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
          <option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
          <option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
          <option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
        </select>  
        <lt:Label res="res.flow.Flow" key="organ"/>
        <input type="text" name="starter" value="<%=starter%>" size="7" />
        <lt:Label res="res.flow.Flow" key="startDate"/>
   	 	<input size="8" id="fromDate" name="fromDate" value="<%=fromDate%>" />
		-
    	<input size="8" id="toDate" name="toDate" value="<%=toDate%>" />        
        <input type="hidden" name="action" value="<%=action%>" />         
		<input name="submit" type=submit value='<lt:Label res="res.flow.Flow" key="search"/>' class="tSearch" />
        <input name="viewMode" value="<%=viewMode%>" type="hidden" />
        </form>
        </td>
    </tr>
</table>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
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
if (orderBy.equals("")) {
	if (!viewMode.equals("favorite")) {
		orderBy = "flow_id";
	}
}
String sort = ParamUtil.get(request, "sort");
if (sort.equals("")) {
	sort = "desc";
}

String sql = "";
if (viewMode.equals("favorite")) {
	sql = "select flow_id from flow_favorite v,flow f where v.user_name=" + StrUtil.sqlstr(myname) + " and v.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE;
}
else {
	sql = "select distinct m.flow_id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and f.status<>" + WorkflowDb.STATUS_NONE;
	sql += " and m.is_checked<>" + MyActionDb.CHECK_STATUS_WAITING_TO_DO; // 等待前一节点结束
}
if (op.equals("search")) {
	if (viewMode.equals("favorite")) {
		sql = "select distinct v.flow_id from flow_favorite v, flow f, users u where v.flow_id=f.id and f.userName=u.name and v.user_name=" + StrUtil.sqlstr(myname) + " and f.status<>" + WorkflowDb.STATUS_NONE;
	}
	else {
		sql = "select distinct m.flow_id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and f.status<>" + WorkflowDb.STATUS_NONE;
		if (!"".equals(starter)) {
			sql = "select distinct m.flow_id from flow_my_action m, flow f, users u where m.flow_id=f.id and f.userName=u.name and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and f.status<>" + WorkflowDb.STATUS_NONE;
		}
		sql += " and m.is_checked<>" + MyActionDb.CHECK_STATUS_WAITING_TO_DO; // 等待前一节点结束
	}
	
	if (by.equals("title")) {
		if (!title.equals("")) {
			sql += " and f.title like " + StrUtil.sqlstr("%" + title + "%");
		}
	}
	else if (by.equals("flowId")) {
		if (!StrUtil.isNumeric(title)) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","mustNumber");
			out.print(StrUtil.Alert_Back(str));
			return;
		}
		else {
			sql += " and f.id=" + title;
		}
	}	
	
	if (!typeCode.equals("")) {
		sql += " and f.type_code=" + StrUtil.sqlstr(typeCode);
	}
	
	if (!fromDate.equals("")) {
		sql += " and f.mydate>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
	}
	if (!toDate.equals("")) {
		java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
		d = DateUtil.addDate(d, 1);
		String toDate2 = DateUtil.format(d, "yyyy-MM-dd");		
		sql += " and f.mydate<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd");
	}	

	if (flowStatus==1000) {
		;
	}
	else {
		sql += " and f.status=" + flowStatus;
	}
	if (!"".equals(starter)) {
		sql += " and u.realname like " + StrUtil.sqlstr("%" + starter + "%");
	}	
}

sql += " and f.status<>" + WorkflowDb.STATUS_DELETED;

if (viewMode.equals("favorite")) {
	if (orderBy.equals("")) {
		sql += " order by f.status desc, flow_id desc";
	}
} else {
	sql += " order by " + orderBy + " " + sort;
}

// out.print(sql);
ListResult lr = wf.listResult(sql, curpage, pagesize);

// ListResult lr = wf.listUserAttended(privilege.getUser(request), curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
%>
<table id="grid">
<thead>
    <tr>
      <th width="50" align="center" abbr="flow_id">ID</th>  
      <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>              
      <th width="28" align="center" abbr="flow_level"><lt:Label res="res.flow.Flow" key="rating"/></th>
      <%}%>
      <th width="400"><lt:Label res="res.flow.Flow" key="tit"/></th>
      <th width="70" abbr="userName"><lt:Label res="res.flow.Flow" key="organ"/></th>
      <th width="100" abbr="begin_date"><lt:Label res="res.flow.Flow" key="startTime"/></th>
      <%if (!action.equals("sel")) {%>
      <th width="70"><lt:Label res="res.flow.Flow" key="finallyApply"/></th>
      <th width="100" abbr="end_date"><lt:Label res="res.flow.Flow" key="finallyApplyTime"/></th>
      <th width="75"><lt:Label res="res.flow.Flow" key="currentHandle"/></th>
      <%}%>
      <th width="85" abbr="status"><lt:Label res="res.flow.Flow" key="state"/></th>
      <th width="120" align="center"><lt:Label res="res.flow.Flow" key="operate"/></th>
    </tr>
</thead>    
  <tbody>
<%
WorkflowPredefineDb wpd = new WorkflowPredefineDb();
Leaf ft = new Leaf();
MyActionDb mad = new MyActionDb();
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next(); 
	UserDb user = null;
	if (wfd.getUserName()!=null)
		user = um.getUserDb(wfd.getUserName());
	String userRealName = "";
	if (user!=null)
		userRealName = user.getRealName();
	Leaf lf = ft.getLeaf(wfd.getTypeCode());
	%>
    <tr>
      <td align="center"><%=wfd.getId()%></td>
      <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>            
      <td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%></td>    
      <%}%>  
      <td>
		<%
		if (wpd==null) {
			wpd = new WorkflowPredefineDb();
		}
	  	wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
		if (wpd!=null && wpd.isLight()) {
		%>
            <!--<a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle())%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>">-->
            <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replace("\r","").replace("&#039;","\\&#039;")%>', '<%=request.getContextPath()%>/flow_dispose_light_show.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>">            
			<%=MyActionMgr.renderTitle(request, wfd)%>
            </a>
		<%
		}
		else {
	  	%>
            <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle())%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>">
            <%=wfd.getTitle()%>
            </a>
      	<%}%>
      </td>
      <td><%=userRealName%></td>
      <td align="center"><%=DateUtil.format(wfd.getBeginDate(), "MM-dd HH:mm")%></td>
      <%if (!action.equals("sel")) {%>      
      <td>
	  <%
	  	String lastDate = "";
		// sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " order by receive_date desc";
	    sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " and is_checked<>" + MyActionDb.CHECK_STATUS_NOT + " order by receive_date desc";
		Iterator ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
	  	if (ir2.hasNext()) {
			mad = (MyActionDb)ir2.next();
			lastDate = DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm");
		%>
		<%=um.getUserDb(mad.getUserName()).getRealName()%>
		<%
		}
	  %>
	  </td>
      <td><%=lastDate%></td>
      <td><%
		// CHECK_STATUS_NOT为0
		sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " and is_checked=" + MyActionDb.CHECK_STATUS_NOT + " order by receive_date desc";
		ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
		int k = 0;
		while (ir2.hasNext()) {
			mad = (MyActionDb)ir2.next();
			if (k!=0) {
				out.print("、");
			}
		%>
        <%=um.getUserDb(mad.getUserName()).getRealName()%>
        <%
			k++;
		}
	  %></td>
      <%}%>
      <td class="<%=WorkflowDb.getStatusClass(wfd.getStatus())%>"><%=wfd.getStatusDesc()%></td>
      <td align="center">
		<%
        if (wpd!=null && wpd.isLight()) {
		%>
            <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>","").replace("&#039;","\\&#039;")%>', '<%=request.getContextPath()%>/flow_dispose_light_show.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>"><lt:Label res="res.flow.Flow" key="show"/></a>      
	  	<%}else{%>
			<a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r","")%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<lt:Label res='res.flow.Flow' key='viewProcess'/>"><lt:Label res="res.flow.Flow" key="show"/></a>
        <%}%>
	  
	  	<%
	  	if (!"sel".equals(action)) {
		  	if (!viewMode.equals("favorite")) {%>
	      	&nbsp;&nbsp;<a href="javascript:;" onclick="favorite(<%=wfd.getId()%>)"><lt:Label res="res.flow.Flow" key="attention"/></a>
			<%}else{%>
	        &nbsp;&nbsp;<a href="javascript:;" onclick="unfavorite(<%=wfd.getId()%>)"><lt:Label res="res.flow.Flow" key="cancelAttention"/></a>
	        <%}
        }%>
        <%if (action.equals("sel")) {%>
        &nbsp;&nbsp;<a href="javascript:;" onclick="selFlow('<%=wfd.getId()%>', '<%=wfd.getTitle()%>')"><lt:Label res="res.flow.Flow" key="choose"/></a>
        <%}%>
      </td>
    </tr>
<%}%>
  </tbody>
</table>
<%
	String querystr = "op="+op+"&action=" + action + "&viewMode=" + viewMode + "&userName=" + StrUtil.UrlEncode(myname) + "&typeCode=" + typeCode + "&flowStatus=" + flowStatus + "&by=" + by + "&title=" + StrUtil.UrlEncode(title) + "&starter=" + StrUtil.UrlEncode(starter) + "&fromDate=" + fromDate + "&toDate=" + toDate;
    // out.print(paginator.getCurPageBlock("flow_list_attend.jsp?"+querystr));
%>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_list_attend.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&action=<%=action%>";
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_list_attend.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp + "&action=<%=action%>";
}

function rpChange(pageSize) {
	window.location.href = "flow_list_attend.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize + "&action=<%=action%>";
}

function onReload() {
	window.location.reload();
}

$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
			{name: '<lt:Label res="res.flow.Flow" key="condition"/>', bclass: '', type: 'include', id: 'searchTable'}
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
	
	<%if (!"".equals(by)) {%>
	o("by").value = "<%=by%>";
    <%}%>	
	o("flowStatus").value = "<%=flowStatus%>";
	
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
	
	$(function() {
		$.ajax({
			type: "post",
			url: "flow/getTree.do",
			data: {
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				$("#typeCode").empty();				
				
				data = "<option value=''><lt:Label res="res.flow.Flow" key="limited"/></option>" + data;
										
				$("#typeCode").append(data);
				
				o("typeCode").value = "<%=typeCode%>";
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
				jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
			}
		});	
		
	});	
});

function action(com, grid) {
}

function favorite(id) {
	$.ajax({
		type: "post",
		url: "flow/favorite.do",
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
				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
			}
			else {
				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
			}
		},
		complete: function(XMLHttpRequest, status){
			$('#grid').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
		}
	});	
}

function unfavorite(id) {
	jConfirm('<lt:Label res="res.flow.Flow" key="toCancelAttention"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function(r) {
		if (r) {
			$.ajax({
				type: "post",
				url: "flow/unfavorite.do",
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
						jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
					}
					else {
						jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
						window.location.reload();
					}
				},
				complete: function(XMLHttpRequest, status){
					$('#grid').hideLoading();				
				},
				error: function(XMLHttpRequest, textStatus){
					jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
				}
			});
		}
	});

}

function selFlow(id, title) {
  	var dlg = window.opener ? window.opener : dialogArguments;

	dlg.setIntpuObjValue(id, "<a href='<%=request.getContextPath()%>/flow_modify.jsp?flowId=" + id + "' target='_blank'>" + title + "</a>");
	
	window.close();
}

</script>
</html>