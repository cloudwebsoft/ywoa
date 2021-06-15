<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="com.redmoon.oa.emailpop3.pop3.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.mail.*"%>
<%@page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@page import="java.util.Date"%>
<%@page import="org.json.JSONObject"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService"/>
<jsp:useBean id="fnumber" scope="page" class="cn.js.fan.util.NumberUtil"/>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(SkinUtil.makeInfo(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = -1;
try {
	id = ParamUtil.getInt(request, "id");
} catch (ErrMsgException e) {
	out.print(SkinUtil.makeInfo(request, "请选择邮箱！"));
	return;
}





EmailPop3Db epd = new EmailPop3Db();
epd = epd.getEmailPop3Db(id);
if (!epd.getUserName().equals(privilege.getUser(request))) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int subMenu = ParamUtil.getInt(request, "subMenu", -1);
int subMenuButton = ParamUtil.getInt(request, "subMenuButton", -1);

String email = epd.getEmail();
String email_user = epd.getEmailUser();
String email_pwd = epd.getEmailPwd();
String mailserver = epd.getServer();
int port = epd.getPort();

String what = ParamUtil.get(request,"what");
String senderWhat = ParamUtil.get(request,"senderWhat");
String receiverWhat = ParamUtil.get(request,"receiverWhat");
String subjectWhat = ParamUtil.get(request,"subjectWhat");
String contentWhat = ParamUtil.get(request,"contentWhat");
String fileNameWhat = ParamUtil.get(request,"fileNameWhat");

if(what.equals("")){
	what = "搜索邮件";
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "createDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";




String sql = "select distinct a.id,a.sender,a.subject,a.mydate,a.is_readed,a.email_addr from email a left join email_attach b on a.id = b.emailId where  a.email_addr=" + StrUtil.sqlstr(email) + " and a.msg_type=" + MailMsgDb.TYPE_DUSTBIN;

if(!what.equals("") && !what.equals("搜索邮件")){
	sql += " and (a.sender like " + StrUtil.sqlstr("%" + what + "%")+" or a.receiver like "+ StrUtil.sqlstr("%" + what + "%")+" or a.subject like "+ StrUtil.sqlstr("%" + what + "%")+
	" or a.content like "+ StrUtil.sqlstr("%" + what + "%")+" or b.name like "+ StrUtil.sqlstr("%" + what + "%");
}

if(!senderWhat.equals("")){
	sql += " and (a.sender like " + StrUtil.sqlstr("%" + senderWhat + "%");
}
if(!receiverWhat.equals("")){
	sql += " and (a.receiver like " + StrUtil.sqlstr("%" + receiverWhat + "%");
}
if(!subjectWhat.equals("")){
	sql += " and (a.subject like " + StrUtil.sqlstr("%" + subjectWhat + "%");
}
if(!contentWhat.equals("")){
	sql += " and (a.content like " + StrUtil.sqlstr("%" + contentWhat + "%");
}
if(!fileNameWhat.equals("")){
	sql += " and (b.name like " + StrUtil.sqlstr("%" + fileNameWhat + "%");
}

if((!what.equals("") && !what.equals("搜索邮件")) || !senderWhat.equals("") || !receiverWhat.equals("") || !subjectWhat.equals("") || !contentWhat.equals("") || !fileNameWhat.equals("")){
	sql += ") ";
}

sql += " order by a.mydate desc";
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(SkinUtil.makeErrMsg(request, "标识非法！"));
	return;
}
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
int curpage = Integer.parseInt(strcurpage);
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql, Integer.parseInt(strcurpage), pagesize);
ResultRecord rr = null;

long total = jt.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}


String op = ParamUtil.get(request,"op");
if(op.equals("updateTotal")){
	
	JSONObject json = new JSONObject();
	
	String update_sql = "select distinct a.id,a.sender,a.subject,a.mydate,a.is_readed,a.email_addr from email a left join email_attach b on a.id = b.emailId where  a.email_addr=" + StrUtil.sqlstr(email) + " and a.msg_type=" + MailMsgDb.TYPE_DUSTBIN;

	if(!what.equals("") && !what.equals("搜索邮件")){
		update_sql += " and (a.sender like " + StrUtil.sqlstr("%" + what + "%")+" or a.receiver like "+ StrUtil.sqlstr("%" + what + "%")+" or a.subject like "+ StrUtil.sqlstr("%" + what + "%")+
		" or a.content like "+ StrUtil.sqlstr("%" + what + "%")+" or b.name like "+ StrUtil.sqlstr("%" + what + "%");
	}

	if(!senderWhat.equals("")){
		update_sql += " and (a.sender like " + StrUtil.sqlstr("%" + senderWhat + "%");
	}
	if(!receiverWhat.equals("")){
		update_sql += " and (a.receiver like " + StrUtil.sqlstr("%" + receiverWhat + "%");
	}
	if(!subjectWhat.equals("")){
		update_sql += " and (a.subject like " + StrUtil.sqlstr("%" + subjectWhat + "%");
	}
	if(!contentWhat.equals("")){
		update_sql += " and (a.content like " + StrUtil.sqlstr("%" + contentWhat + "%");
	}
	if(!fileNameWhat.equals("")){
		update_sql += " and (b.name like " + StrUtil.sqlstr("%" + fileNameWhat + "%");
	}

	if((!what.equals("") && !what.equals("搜索邮件")) || !senderWhat.equals("") || !receiverWhat.equals("") || !subjectWhat.equals("") || !contentWhat.equals("") || !fileNameWhat.equals("")){
		update_sql += ") ";
	}

	update_sql += " order by a.mydate desc";
	
	JdbcTemplate jt1 = new JdbcTemplate();
	ResultIterator ri1 = jt1.executeQuery(update_sql, Integer.parseInt(strcurpage), pagesize);
	total = jt1.getTotal();
	
	
	json.put("ret", "0");
	json.put("msg", total);
	out.print(json.toString());	
	return;
	
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>收件箱</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery.form.js"></script>
<script src="../js/jquery.xmlext.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link href="mail.css" type="text/css" rel="stylesheet" />
<link href="../skin/outside_mail.css" type="text/css" rel="stylesheet" />
<script src="<%=request.getContextPath() %>/js/jquery.toaster.email.js"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script>
var errFunc = function(response) {
	$('#bodyBox').hideLoading();
	window.status = 'Error ' + response.status + ' - ' + response.statusText;
	jAlert(response.responseText,"提示");
}

function doGetListBox(response){
	var rsp = response.responseText.trim();
	//o("listBox").innerHTML = rsp;
	$("#list_delete").html(rsp);
	
}

function getListBox() {
	var what = encodeURI(encodeURI("<%=what%>"));
	var senderWhat = encodeURI(encodeURI("<%=senderWhat%>"));
	var receiverWhat = encodeURI(encodeURI("<%=receiverWhat%>"));
	var subjectWhat = encodeURI(encodeURI("<%=subjectWhat%>"));
	var contentWhat = encodeURI(encodeURI("<%=contentWhat%>"));
	var fileNameWhat = encodeURI(encodeURI("<%=fileNameWhat%>"));
	var str = "email=<%=email%>&CPages=<%=strcurpage%>&pageSize=<%=pagesize%>&what="+what+"&senderWhat="+senderWhat+"&receiverWhat="+receiverWhat+"&subjectWhat="+subjectWhat+"&contentWhat="+contentWhat+"&fileNameWhat="+fileNameWhat+"&subMenu=<%=subMenu%>&subMenuButton=<%=subMenuButton%>";
	var myAjax = new cwAjax.Request( 
		"list_delete_ajax.jsp", 
		{ 
			method:"post",
			parameters:str,
			onComplete:doGetListBox,
			onError:errFunc
		}
	);
}

function windowOnload() {
	initDiv();
	getListBox();
}

function selPage(page) {
	o('curpage').value = page;
	getListBox();
}

function showPrompt(img, msg, title, time) {
	o("spanStatus").innerHTML = "<table border='0' cellpadding='0' cellspacing='0'><tr><td><img style='margin-right:8px' src='" + img + "' /></td><td title='" + title + "'>" + msg + "</td></tr></table>";
	o("spanStatus").style.display = "";
	if(time > 0) {
		window.setTimeout("delPrompt()", time);
	}
}

function delPrompt() {
	o('spanStatus').style.display = 'none';
	o('spanStatus').innerHTML='';
}

function doDelMail(response){
	$('#bodyBox').hideLoading();
	getListBox();
	var rsp = response.responseText.trim();
	if (rsp.substring(0, 1)=="-") {
		jAlert(rsp,"提示");
	}
	$("#selectAll").removeAttr("checked");
	$('tr', grid.hDiv).each(function() {
		$('thead tr', grid.hDivBox).each(function() {
			$(this).find('input')[0].checked = false;
		})
	})
	//showPrompt("images/alert.gif", rsp, rsp, 2000);
	$.toaster({priority : 'info', message : rsp });

	updateTotal();
}


function initDiv() {
	o("spanStatus").style.display = "none";
}

function doMoveEmail(response){
	$('#bodyBox').hideLoading();
	getListBox();
	var rsp = response.responseText.trim();
	//showPrompt("images/alert.gif", rsp, rsp, 2000);
	$.toaster({priority : 'info', message : rsp });
	$("#selectAll").removeAttr("checked");
	$('tr', grid.hDiv).each(function() {
		$('thead tr', grid.hDivBox).each(function() {
			$(this).find('input')[0].checked = false;
		})
	})
	parent.leftFrame.location.href="left_menu.jsp?popId=<%=epd.getId()%>&subMenu=<%=subMenu%>&subMenuButton=<%=subMenuButton%>";

	updateTotal();
}	

function move(box) {

	selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
	if (selectedCount == 0) {
		jAlert('请选择记录!','提示');
		return;
	}

	$('#bodyBox').showLoading();		
	var ids = "";
	$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
		if (ids=="")
			ids = $(this).val();
		else
			ids += "," + $(this).val();
	});	
	
	//showPrompt("images/loading.gif", "移动邮件中...", "移动邮件中...", 0);		
	var str = "op=move&id=<%=id%>&box=" + box + "&ids=" + ids;
	var myAjax = new cwAjax.Request( 
		"mail_move_ajax.jsp", 
		{ 
			method:"post",
			parameters:str,
			onComplete:doMoveEmail,
			onError:errFunc
		}
	);
}

function setIsReaded(isReaded) {
	selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
	if (selectedCount == 0) {
		jAlert('请选择记录!','提示');
		return;
	}
	$('#bodyBox').showLoading();	
		
	var ids = "";
	$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
		if (ids=="")
			ids = $(this).val();
		else
			ids += "," + $(this).val();
	});	
	
	//showPrompt("images/loading.gif", "邮件标记中...", "邮件标记中...", 0);		
	var str = "op=isReaded&id=<%=id%>&isReaded=" + isReaded + "&ids=" + ids;
	var myAjax = new cwAjax.Request( 
		"mail_isReaded_ajax.jsp", 
		{ 
			method:"post",
			parameters:str,
			onComplete:doIsReadedEmail,
			onError:errFunc
		}
	);
}

function doIsReadedEmail(response){
	$('#bodyBox').hideLoading();
	var rsp = response.responseText.trim();
	$.toaster({priority : 'info', message : rsp });
	//showPrompt("images/alert.gif", rsp, rsp, 2000);
	getListBox();
	$("#selectAll").removeAttr("checked");
	$('tr', grid.hDiv).each(function() {
		$('thead tr', grid.hDivBox).each(function() {
			$(this).find('input')[0].checked = false;
		})
	})
	
}
function showSingDiv(){
	var signTimes = $("#signTimes").val();
	if(signTimes == 0){
		$("#signDiv").attr("style","display:block");
		$("#signTimes").val("1");
	}else{
		$("#signDiv").attr("style","display:none");
		$("#signTimes").val("0");
	}
}
function showMoveToDiv(){
	var moveTimes = $("#moveTimes").val();
	if(moveTimes == 0){
		$("#moveToDiv").attr("style","display:block");
		$("#moveTimes").val("1");
	}else{
		$("#moveToDiv").attr("style","display:none");
		$("#moveTimes").val("0");
	}
}
function showSelectDiv(){
	var selectTimes = $("#selectTimes").val();
	if(selectTimes == 0){
		$("#selectDiv").attr("style","display:block");
		$("#selectTimes").val("1");
	}else{
		$("#selectDiv").attr("style","display:none");
		$("#selectTimes").val("0");
	}
}
function searchClick(){
	var search = $("#search").val();
	if(search == "搜索邮件"){
		$("#search").val("");
	}

	if($("#search").val() != "" ){
		searchChange();
	}
}
function searchChange(){
	$("#searchDivHidden").attr("style","display:block");
	var search = $("#search").val();
	var str = "<p onclick=\"searchDivClick(1)\">包含 "+search+" 的发件人</p>"+
				"<p onclick=\"searchDivClick(2)\">包含 "+search+" 的收件人</p>"+
				"<p onclick=\"searchDivClick(3)\">包含 "+search+" 的主题</p>"+
				"<p onclick=\"searchDivClick(4)\">包含 "+search+" 的全文</p>"+
				"<p onclick=\"searchDivClick(5)\">包含 "+search+" 的附件名</p>";
	$("#searchDivHidden").html(str);

	if (window.event.keyCode==13) {
		o('what').value= search;
		formSearch.submit();
	}
}

function searchDivClick(obj){
	var search = $("#search").val();
	if(obj == "1"){
		o('senderWhat').value= search;
	}else if (obj == "2"){
		o('receiverWhat').value= search;
	}else if (obj == "3"){
		o('subjectWhat').value= search;
	}else if (obj == "4"){
		o('contentWhat').value= search;
	}else if (obj == "5"){
		o('fileNameWhat').value= search;
	}
	parent.leftFrame.location.href="left_menu.jsp?popId=<%=epd.getId()%>&subMenu=<%=subMenu%>&subMenuButton=<%=subMenuButton%>";
	formSearch.submit();

}
function clickSearch(){
	var search = $("#search").val();
	o('what').value= search;
	parent.leftFrame.location.href="left_menu.jsp?popId=<%=epd.getId()%>&subMenu=<%=subMenu%>&subMenuButton=<%=subMenuButton%>";
	formSearch.submit();
}
</script>
</head>
<body onload="windowOnload()">
<div id="bodyBox">
<div id="rightMain">
<div class="inbox-wrap">
   <div class="inbox-right">
     <div class="inbox-toolbar"> 
	     <div class="inbox-right-btnbox inbox-toolbar-checkbox" id="selectAllDiv" onclick="showSelectDiv()" title="选择">
	      	<!-- <img src="images/inbox-checkbox.png" width="20" height="20"/> -->
	      	<input type="checkbox" name="selectAll" id="selectAll" onclick="selectAll()" style="margin-top:4px;"/>
	     </div>
	     <div class="inbox-right-btnbox" onclick="del()" title="删除"><img src="images/inbox-del.png" width="20" height="20"/></div>
	     <div class="inbox-right-btnbox" onclick="getListBox()" title="刷新"><img src="images/inbox-refresh.png" width="20" height="20"/></div>
	     <div class="inbox-right-btnbox inbox-toolbar-select" onclick="showSingDiv()" id="signSet" title="标记为"><img src="images/inbox-mark.png" width="20" height="20"/>标记为</div>
	     <div class="inbox-right-btnbox inbox-toolbar-select" onclick="showMoveToDiv()" id="moveTo" title="移动到"><img src="images/inbox-move.png" width="20" height="20"/>移动到</div>
	     <div class="inbox-toolbar-search" id="searchDiv" title="搜索">      
	     	 <%if(!senderWhat.equals("")){ %>
	         <input name="textfield" type="text" id="search" value="<%=senderWhat %>" onclick="searchClick()" onkeyup="searchChange()"/>
	         <%}else if(!receiverWhat.equals("")){ %>
	         <input name="textfield" type="text" id="search" value="<%=receiverWhat %>" onclick="searchClick()" onkeyup="searchChange()"/>
	         <%}else  if(!subjectWhat.equals("")){ %>
	         <input name="textfield" type="text" id="search" value="<%=subjectWhat %>" onclick="searchClick()" onkeyup="searchChange()"/>
	         <%}else  if(!contentWhat.equals("")){ %>
	         <input name="textfield" type="text" id="search" value="<%=contentWhat %>" onclick="searchClick()" onkeyup="searchChange()"/>
	         <%}else if(!fileNameWhat.equals("")){ %>
	         <input name="textfield" type="text" id="search" value="<%=fileNameWhat %>" onclick="searchClick()" onkeyup="searchChange()"/>
	         <%}else{ %>
	         <input name="textfield" type="text" id="search" value="<%=what %>" onclick="searchClick()" onkeyup="searchChange()"/>
	         <%} %>
	          <img src="images/inbox-search.png" alt="" onclick="clickSearch()"/>
	     </div>
     </div>
     <div class="selectDiv" style="display:none;" id="selectDiv">
     	<p onclick="changSelect('全选')">全选</p>
     	<p onclick="changSelect('不选')">不选</p>
     	<p onclick="changSelect('未读')">未读</p>
     	<p onclick="changSelect('已读')">已读</p>
     	<input type="hidden" name="selectTimes" id="selectTimes" value="0"/>
     </div>
     <script>
		$(function(){
			$(document).bind("click", function(e) {  
		    e = e || window.event; 
		    var dom =  e.srcElement|| e.target;
	    	if(dom.id != "selectAllDiv"){
		    	$("#selectDiv").attr("style","display:none");
				$("#selectTimes").val("0");
			}
			if(dom.id == "selectDiv"){
				$("#selectDiv").attr("style","display:block");
				$("#selectTimes").val("1");
			}
	    });
		});
	</script>
     <div class="signDiv" style="display:none;" id="signDiv">
     	<p onclick="setIsReaded(1)">已读</p>
     	<p onclick="setIsReaded(0)">未读</p>
     	<input type="hidden" name="signTimes" id="signTimes" value="0"/>
     </div>
     <script>
		$(function(){
			$(document).bind("click", function(e) {  
		    e = e || window.event; 
		    var dom =  e.srcElement|| e.target;
		    var signTimes = $("#signTimes").val();
	    	if(dom.id != "signSet"){
		    	$("#signDiv").attr("style","display:none");
				$("#signTimes").val("0");
			}
			if(dom.id == "signDiv"){
				$("#signDiv").attr("style","display:block");
				$("#signTimes").val("1");
			}
	    });
		});
	</script>
     <div class="moveToDiv" style="display:none;" id="moveToDiv">
     	<p onclick="move(<%=MailMsgDb.TYPE_DRAFT%>)">草稿箱</p>
     	<p onclick="move(<%=MailMsgDb.TYPE_SENDED%>)">已发送</p>
     	<p onclick="move(<%=MailMsgDb.TYPE_DUSTBIN%>)">已删除</p>
     	<input type="hidden" name="moveTimes" id="moveTimes" value="0"/>
     </div>
     <script>
		$(function(){
			$(document).bind("click", function(e) {  
		    e = e || window.event; 
		    var dom =  e.srcElement|| e.target;
	    	if(dom.id != "moveTo"){
		    	$("#moveToDiv").attr("style","display:none");
				$("#moveTimes").val("0");
			}
			if(dom.id == "moveToDiv"){
				$("#moveToDiv").attr("style","display:block");
				$("#moveTimes").val("1");
			}
	    });
		});
	</script>
	<div class="searchDiv" id="searchDivHidden" style="display:none;">
	</div>
	<script>
		$(function(){
			$(document).bind("click", function(e) {  
		    e = e || window.event; 
		    var dom =  e.srcElement|| e.target;
	    	if(dom.id != "searchDiv" && dom.parentNode.id != "searchDiv"){
		    	$("#searchDivHidden").attr("style","display:none");
				
			}
	    });
		});
	</script>
   	<div class="rightMainTitle" style="display:inline;">
  		<span id="spanStatus"></span>  
	</div>
   </div>
</div>
<table id="grid" >
 	<thead>
		<tr>
			<th width="30" align="center">
				</th>
			<th width="190" align="left">
				发件人
			</th>
			<th width="500" align="left">
				主题
			</th>
			<th width="180" align="left">
				时间
			</th>
		</tr>
    </thead>
	<tbody id="list_delete">
	</tbody>
</table>
  <div id="confMail"></div>
  <div id="addEmail"></div>
</div>
</div>
<form id="formSearch" action="list_delete.jsp" method="get">
	<input type="hidden" name="id" id="id" value="<%=id %>"/>
	<input type="hidden" name="what" />
	<input type="hidden" name="senderWhat" />
	<input type="hidden" name="receiverWhat" />
	<input type="hidden" name="subjectWhat" />
	<input type="hidden" name="contentWhat" />
	<input type="hidden" name="fileNameWhat" />
	<input type="hidden" name="subMenu" id="subMenu" value="<%=subMenu %>"/>
	<input type="hidden" name="subMenuButton" id="subMenuButton" value="<%=subMenuButton %>"/>
</form>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "list_delete.jsp?id=<%=id%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "list_delete.jsp?id=<%=id%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>";
}

function rpChange(pageSize) {
	window.location.href = "list_delete.jsp?id=<%=id%>&CPages=<%=curpage%>&pageSize=" + pageSize + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>";
}

function onReload() {
	window.location.reload();
}

var buttonObj;
$(function(){
		flex = $("#grid").flexigrid
		(
			{
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
			autoHeight: false,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 120
			}
		);
});



function del(){
	selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
	if (selectedCount == 0) {
		jAlert('请选择记录!','提示');
		return;
	}
	jConfirm("您确定要彻底删除么？","提示",function(r){
		if(!r){return}
		else{
			$('#bodyBox').showLoading();
			var ids = "";
			$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
				if (ids=="")
					ids = $(this).val();
				else
					ids += "," + $(this).val();
			});	
			//showPrompt("images/loading.gif", "删除邮件中...", "删除邮件中...", 0);
			var str = "email=<%=email%>&ids=" + ids + "&op=del&isDustbin=false";
			var myAjax = new cwAjax.Request( 
				"mail_del_ajax.jsp", 
				{ 
					method:"post",
					parameters:str,
					onComplete:doDelMail,
					onError:errFunc
				}
			);		
				
		}
	})
	
}

function changSelect(obj){
	var emailState = obj;
	if(emailState == "全选"){
		$("#selectAll").attr("checked","true");
		$('tr', grid.hDiv).each(function() {
			$('tbody tr', grid.bDiv).each(function() {
				$(this).find('input')[0].checked = true;
			})
			$('thead tr', grid.hDivBox).each(function() {
				$(this).find('input')[0].checked = true;
			})
		})
	}else if(emailState == "不选"){
		$("#selectAll").removeAttr("checked");
		$('tr', grid.hDiv).each(function() {
			$('tbody tr', grid.bDiv).each(function() {
				$(this).find('input')[0].checked = false;
			})
			$('thead tr', grid.hDivBox).each(function() {
				$(this).find('input')[0].checked = false;
			})
		})
	}else if(emailState == "未读"){
		$("#selectAll").removeAttr("checked");
		$('tr', grid.hDiv).each(function() {
			$('tbody tr', grid.bDiv).each(function() {
				$(this).find('input')[0].checked = false;
			})
			$('thead tr', grid.hDivBox).each(function() {
				$(this).find('input')[0].checked = false;
			})
		})
		
		$('.bDiv tbody tr', grid.bDiv).each(function() {
			var is_readed = $(this).attr("lang");
			if(is_readed == 0){
				$(this).find('input')[0].checked = true;
			}
		})
	}else if(emailState == "已读"){
		$("#selectAll").removeAttr("checked");
		$('tr', grid.hDiv).each(function() {
			$('tbody tr', grid.bDiv).each(function() {
				$(this).find('input')[0].checked = false;
			})
			$('thead tr', grid.hDivBox).each(function() {
				$(this).find('input')[0].checked = false;
			})
		})
		
		$('.bDiv tbody tr', grid.bDiv).each(function() {
			var is_readed = $(this).attr("lang");
			if(is_readed == 1){
				$(this).find('input')[0].checked = true;
			}
		})
	}
}
function selectAll(){
		if($("#selectAll").attr("checked")){
			$('tr', grid.hDiv).each(function() {
				$('tbody tr', grid.bDiv).each(function() {
					$(this).find('input')[0].checked = true;
				})
				$('thead tr', grid.hDivBox).each(function() {
					$(this).find('input')[0].checked = true;
				})
			})
		}else{
			$('tr', grid.hDiv).each(function() {
				$('tbody tr', grid.bDiv).each(function() {
					$(this).find('input')[0].checked = false;
				})
				$('thead tr', grid.hDivBox).each(function() {
					$(this).find('input')[0].checked = false;
				})
			})
		}
}

function updateTotal(){
	$.ajax({
		type: "post",
		url: "list_delete.jsp",
		dataType: "json",
		data: {
			op: "updateTotal",
			id: "<%=id%>",
			subMenu: "<%=subMenu%>",
			subMenuButton: "<%=subMenuButton%>",
			CPages:"<%=strcurpage%>",
			pageSize:"<%=pagesize%>",
			what:"<%=what%>",
			receiverWhat:"<%=receiverWhat%>",
			senderWhat:"<%=senderWhat%>",
			subjectWhat:"<%=subjectWhat%>",
			contentWhat:"<%=contentWhat%>",
			fileNameWhat:"<%=fileNameWhat%>"
			
		},
		success: function(datas, status){
			$(".pPageStat").text("显示第 1 条到 "+datas.msg+" 条, 共 "+datas.msg+" 条数据");
		},
		error: function(XMLHttpRequest, textStatus){
			
		}
	});	
}
</script>
</html>