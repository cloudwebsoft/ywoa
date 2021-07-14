<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
 
String myname = ParamUtil.get(request, "userName");
if(myname.equals("")){
	myname = privilege.getUser(request);
}
if (!myname.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, myname))) {
		out.print(StrUtil.jAlert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
}

String op = ParamUtil.get(request, "op");
String by = ParamUtil.get(request, "by");
String what = ParamUtil.get(request, "what");
String typeCode = ParamUtil.get(request, "typeCode");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "check_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String sql = "select m.id from flow_my_action m where checker<>user_name and checker=" + StrUtil.sqlstr(myname);
if (op.equals("search")) {
	if (by.equals("title")) {
		if (!typeCode.equals("")) {
			sql = "select m.id from flow f,flow_my_action m where f.id=m.flow_id and f.type_code=" + StrUtil.sqlstr(typeCode) + " and f.title like " + StrUtil.sqlstr("%" + what + "%") +  " and m.checker<>m.user_name and m.checker=" + StrUtil.sqlstr(myname);
		}
		else
			sql = "select m.id from flow f,flow_my_action m where f.id=m.flow_id and f.title like " + StrUtil.sqlstr("%" + what + "%") +  " and m.checker<>m.user_name and m.checker=" + StrUtil.sqlstr(myname);
	}
}
sql += " order by " + orderBy + " " + sort;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>我代理的流程列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
function onTypeCodeChange(obj) {
	if(obj.options[obj.selectedIndex].value=='not'){jAlert(obj.options[obj.selectedIndex].text+' <lt:Label res="res.flow.Flow" key="notBeSelect"/>','提示'); return false;}
	window.location.href = "flow_list_my_proxy.jsp?op=search&by=" + $("by").value + "&typeCode=" + obj.options[obj.selectedIndex].value;
}
function submitValidate(){
	var typeCode = $("#typeCode").val();
	var typeCodeText ;
	$("#typeCode").children().each(function (){
		if (this.selected){
			typeCodeText = this.text;
		}
	})
	if (typeCode=="not"){
		jAlert(typeCodeText+' 不能被选择！','提示'); 
		return false;
	}
}
</script>
</head>
<body>
<div class="tabs1Box">
<%@ include file="../user/user_proxy_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
</div>
<table id="searchTable" width="80%" border="0" align="center">
    <tr>
      <td align="center">
  <form name=form1 action="flow_list_my_proxy.jsp?op=search" method=post onsubmit="return submitValidate()">
      <lt:Label res="res.flow.Flow" key="type"/>
        <select id="typeCode" name="typeCode" >
          <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
          <%
		Leaf lf = new Leaf();
		lf = lf.getLeaf("root");
		DirectoryView dv = new DirectoryView(lf);
		dv.ShowDirectoryAsOptions(request, out, lf, 1);
	  %>
        </select>
        &nbsp;
        <select id="by" name="by">
          <option value="title"><lt:Label res="res.flow.Flow" key="tit"/></option>
        </select>
        &nbsp;
        <input name="what" value="<%=what%>">
        <input name="userName" value="<%=myname%>" type="hidden" />
      <input name="submit" type=submit value='<lt:Label res="res.flow.Flow" key="search"/>' class="tSearch">
  </form>
      </td>
    </tr>
</table>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	String str = LocalUtil.LoadString(request,"res.flow.Flow","identifyIllegal");
	out.print(StrUtil.makeErrMsg(str));
	return;
}
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
int curpage = Integer.parseInt(strcurpage);

MyActionDb mad = new MyActionDb();
ListResult lr = mad.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

int start = (curpage-1)*pagesize;
int end = curpage*pagesize;
%>
<table width="981" height="48" id="grid">
  <thead>
    <tr>
      <th width="37" align="center" abbr="id">ID</th>
      <th width="28" align="center"><lt:Label res="res.flow.Flow" key="rating"/></th>
      <th width="193"><lt:Label res="res.flow.Flow" key="tit"/></th>
      <th width="90" abbr="receive_date"><lt:Label res="res.flow.Flow" key="edgeTime"/></th>
      <th width="89" abbr="check_date"><lt:Label res="res.flow.Flow" key="handleTime"/></th>
      <th width="88" abbr="expire_date"><lt:Label res="res.flow.Flow" key="expirationDate"/></th>
      <th width="61" abbr="performance"><lt:Label res="res.flow.Flow" key="achievements"/></th>
      <th width="75"><lt:Label res="res.flow.Flow" key="originalHandler"/></th>
      <th width="111"><lt:Label res="res.flow.Flow" key="organ"/></th>
      <th width="73" align="center" abbr="action_status"><lt:Label res="res.flow.Flow" key="reachState"/></th>
      <th width="88" align="center"><lt:Label res="res.flow.Flow" key="operate"/></th>
    </tr>
  </thead>
  <tbody>
<%
java.util.Iterator ir = lr.getResult().iterator();	
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
Directory dir = new Directory();	
WorkflowDb wfd2 = new WorkflowDb();
while (ir.hasNext()) {
 	mad = (MyActionDb)ir.next();
	
	WorkflowDb wfd = wfd2.getWorkflowDb((int)mad.getFlowId());
	String userName = wfd.getUserName();
	String userRealName = "";
	if (userName!=null) {
		UserDb user = um.getUserDb(wfd.getUserName());
		userRealName = user.getRealName();
	}
	%>
    <tr>
      <td align="center"><%=wfd.getId()%></td>
      <td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%></td>       
      <td>
        <%
	  boolean isExpired = false;
	  java.util.Date chkDate = mad.getCheckDate();
	  if (chkDate==null)
		chkDate = new java.util.Date();
	  if (DateUtil.compare(chkDate, mad.getExpireDate())==1) {
		isExpired = true;
	  }
	  if (isExpired) {%>
        <img src="../images/flow/expired.png" align="absmiddle" alt="<lt:Label res='res.flow.Flow' key='timeOut'/>" />
        <%}%>      
      &nbsp;&nbsp;<a title="<%=wfd.getTitle()%>" href="javascript:;" onclick="addTab('<%=wfd.getTitle()%>', 'flow_modify.jsp?flowId=<%=wfd.getId()%>&actionId=<%=mad.getActionId()%>')"><%=StrUtil.getLeft(wfd.getTitle(), 40)%></a></td>
      <td align="center"><%=DateUtil.format(mad.getReceiveDate(), "yy-MM-dd HH:mm")%> </td>
      <td align="center"><%=DateUtil.format(mad.getCheckDate(), "yy-MM-dd HH:mm")%> </td>
      <td align="center"><%=DateUtil.format(mad.getExpireDate(), "yy-MM-dd HH:mm")%></td>
      <td align="center"><%=NumberUtil.round(mad.getPerformance(), 2)%></td>
      <td align="left"><%=um.getUserDb(mad.getUserName()).getRealName()%></td>
      <td align="left"><%=userRealName%></td>
      <td align="center" class="<%=WorkflowActionDb.getStatusClass(mad.getActionStatus())%>"><%=WorkflowActionDb.getStatusName(mad.getActionStatus())%>	  </td>
      <td align="center"><a href="javascript:;" onclick="addTab('<%=wfd.getTitle()%>', 'flow_modify.jsp?flowId=<%=wfd.getId()%>&actionId=<%=mad.getActionId()%>')"><lt:Label res="res.flow.Flow" key="show"/></a></td>
    </tr>
    <%}%>
  </tbody>
</table>
<%
	String querystr = "op=" + op + "&userName=" + StrUtil.UrlEncode(myname) + "&by=" + by + "&what=" + StrUtil.UrlEncode(what);
    // out.print(paginator.getCurPageBlock("?"+querystr));
%>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_list_my_proxy.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_list_my_proxy.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "flow_list_my_proxy.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '<lt:Label res="res.flow.Flow" key="search"/>', bclass: '', type: 'include', id: 'searchTable'}
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

function action(com, grid) {
}
$(document).ready(function (){
	o("typeCode").value = "<%=typeCode%>";
	o("by").value = "<%=by%>";
})
</script>
</html>