<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
String typeCode = ParamUtil.get(request, "typeCode");
String title = ParamUtil.get(request, "title");
String idcard = ParamUtil.get(request, "idcard");
String myname = ParamUtil.get(request, "userName");
String fqr = ParamUtil.get(request,"fqr");
String isOver = ParamUtil.get(request,"isOver");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>流程绩效 - 用户参与的流程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
function onTypeCodeChange(obj) {
	if(obj.options[obj.selectedIndex].value=='not'){
		alert(obj.options[obj.selectedIndex].text+' 不能被选择！'); 
		return false;
	}
	window.location.href = "flow_performance_user_attend.jsp?op=search&typeCode=" + obj.options[obj.selectedIndex].value;
}
</script>
</head>
<body>

<table id="searchTable" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
<form name="formSearch" action="flow_performance_user_attend.jsp" method="get">
		<select id="typeCode" name="typeCode" onChange="onTypeCodeChange(this)">
		  <option value="">不限</option>
<%
Leaf lf = new Leaf();
lf = lf.getLeaf("root");
DirectoryView dv = new DirectoryView(lf);
dv.ShowDirectoryAsOptions(request, out, lf, 1);
%>
		</select>
            &nbsp;&nbsp;标题&nbsp;&nbsp;
			<input name="title" value="<%=title%>" style="width:100px;">
			&nbsp;&nbsp;ID号&nbsp;&nbsp;
			<input name="idcard" value="<%=idcard%>" style="width:100px;">
			&nbsp;&nbsp;发起人&nbsp;&nbsp;
			<input name="fqr" value="<%=fqr%>" style="width:100px;">
			&nbsp;&nbsp;工作流状态&nbsp;&nbsp;
			<select id="isOver" name="isOver">
            <option value="">所有</option>
			<option value="1">处理中</option>
			<option value="2">已结束</option>
			<option value="0">未提交</option>
			<option value="-2">已拒绝</option>
			</select>
			<input name="userName" value="<%=myname%>" type="hidden">
			<input name="op" value="search" type="hidden">
		<input name="submit" type=submit value="搜索" class="tSearch">
		<script>
        o("typeCode").value = "<%=typeCode%>";
        </script>
        <script>
        o("isOver").value = "<%=isOver%>";
        </script>
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
MyActionDb mad = new MyActionDb();

String sql = "select id from flow_my_action where (user_name=" + StrUtil.sqlstr(myname) + " or proxy=" + StrUtil.sqlstr(myname) + ")";
if (op.equals("search")) {
	sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ")";
	if (!typeCode.equals("")) {
		sql += " and f.type_code=" + StrUtil.sqlstr(typeCode);
	}
	if (!title.equals("")) {
		sql += " and f.title like " + StrUtil.sqlstr("%" + title + "%");
	}
	if (!idcard.equals("")) {
		sql += " and f.id = " +StrUtil.sqlstr(idcard) ;
	}
	if(!fqr.equals("")){
	    sql += "  and f.userName like " + StrUtil.sqlstr("%" + fqr + "%");
	}
	if(!isOver.equals("")){
	  sql += "  and f.status ="+StrUtil.sqlstr(isOver);      
	}	 	
}
sql += " order by id desc";

// out.print(sql);

ListResult lr = mad.listResult(sql, curpage, pagesize);

int total = lr.getTotal();
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
<table id="grid" width="960">
<thead>
    <tr>
      <th width="89" align="center">操作</th>
      <th width="45">任务ID</th>
      <th width="35">等级</th>
      <th width="45">处理者</th>
      <th width="45">绩效</th>
      <th width="55">绩效原因</th>
      <th width="45">绩效修改</th>
      <th width="150">标题</th>
      <th width="71">发起人</th>
      <th width="67">处理步骤</th>
      <th width="67">状态</th>
      <th width="97">处理时间</th>
      <th width="118">流程类型</th>
    </tr>
</thead>    
  <tbody>
    <%
Leaf ft = new Leaf();

com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
while (ir.hasNext()) {
 	mad = (MyActionDb)ir.next(); 
	WorkflowDb wfd = new WorkflowDb();
	wfd = wfd.getWorkflowDb((int)mad.getFlowId());
	UserDb user = null;
	if (wfd.getUserName()!=null)
		user = um.getUserDb(wfd.getUserName());
	String userRealName = "";
	if (user!=null)
		userRealName = user.getRealName();	
	%>
    <tr>
      <td align="center"><a href="javascript:;" onclick="addTab('<%=wfd.getTitle()%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="查看流程过程">查看</a>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="modifyScore('<%=mad.getId()%>', '<%=NumberUtil.round(mad.getPerformance(), 2)%>', '<%=mad.getPerformanceReason()%>')">修改绩效</a></td>
      <td align="center"><%=wfd.getId()%></td>
      <td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%></td>
      <td><%=mad.getChecker()%></td>
      <td align="center"><%=NumberUtil.round(mad.getPerformance(), 2)%></td>
      <td align="center"><%=mad.getPerformanceReason()%></td>
      <td align="center"><%=mad.getPerformanceModifier()%></td>
      <td><a href="flow_modify.jsp?flowId=<%=wfd.getId()%>" title="<%=wfd.getTitle()%>">
        <%=StrUtil.getLeft(wfd.getTitle(), 40)%>
      </a></td>
      <td><%=userRealName%></td>
      <td>
	     <%
	     WorkflowActionDb wad = new WorkflowActionDb();
		 wad = wad.getWorkflowActionDb((int)mad.getActionId());
	  %>
	  <%=wad.getTitle()%>
      </td>
      <td>
		<%
        if (mad.getChecker().equals(UserDb.SYSTEM)) {
            out.print("超时跳过");
        }else{						
        %>
        <%=mad.getCheckStatusName()%>
        <%}%>
      </td>
      <td align="center"><%=DateUtil.format(mad.getCheckDate(), "yy-MM-dd HH:mm")%></td>
      <td><%
	  lf = ft.getLeaf(wfd.getTypeCode());
	  %>
        <%if (lf!=null) {%>
        <a href="flow_performance_user_attend.jsp?op=search&amp;typeCode=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a>
        <%}%></td>
    </tr>
<%}%>
  </tbody>
</table>
<%
	String querystr = "isOver="+StrUtil.UrlEncode(isOver)+"&fqr="+StrUtil.UrlEncode(fqr)+"&op="+op+"&userName=" + StrUtil.UrlEncode(myname) + "&typeCode=" + typeCode + "&title=" + StrUtil.UrlEncode(title);
    // out.print(paginator.getCurPageBlock("flow_performance_user_attend.jsp?"+querystr));
%>
<div id="dlg" style="display:none">
绩效分数：<input id="performance" name="performance" /><br>
修改原因：<input id="performanceReason" name="performanceReason" />
</div>
</body>
<script>
function modifyScore(mid, performance, performanceReason) {
	o("performance").value = performance;
	o("performanceReason").value = performanceReason;
	$("#dlg").dialog({title:"绩效分数", modal: true,
						buttons: {
							"取消":function() {
								$(this).dialog("close");
							},
							"确定": function() {
								ajaxModify("<%=privilege.getUser(request)%>", mid);
								$(this).dialog("close");
							}
						}, 
						closeOnEscape: true,
						draggable: true,
						resizable:true,
						width:350
					});
}

function ajaxModify(userName,mid) {
	$.ajax({
		type: "post",
		url: "flow_performance_user_list_do.jsp",
		data : {
			op: "modifyPerformance",
			performance: o("performance").value,
			performanceReason: o("performanceReason").value,
			userName: userName,
			mid: mid
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			data = data.trim();
			data = $.parseJSON(data);
			if (data.re) {
				alert(data.msg);
				location.reload();
			}
			else
				alert(data.msg);
		},
		complete: function(XMLHttpRequest, status){
			// HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}

</script>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_performance_user_attend.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_performance_user_attend.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "flow_performance_user_attend.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	sortname: "iso",
	sortorder: "asc",
	*/
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

function action(com, grid) {
}
</script>
</html>