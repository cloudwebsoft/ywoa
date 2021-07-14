<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.idiofileark.*" %>
<%@ page import = "org.json.*"%>
<%@page import="com.redmoon.oa.netdisk.UtilTools"%>
<%@page import="cn.js.fan.web.Global"%>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String name = privilege.getUser(request);
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");
String orderBy = ParamUtil.get(request, "orderBy");
String order = ParamUtil.get(request, "order");
String kind = ParamUtil.get(request, "kind");
JSONObject json = new JSONObject();
if (kind.equals(""))
	kind = "title";
String what = ParamUtil.get(request, "what");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "order", order, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "kind", kind, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
MessageDb md = new MessageDb();
int pagesize = ParamUtil.getInt(request, "pagesize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

//消息删除
if (op.equals("del")) {
	boolean re = false;
	try {
		re = Msg.doDustbin(request, true);
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "消息删除失败："+e.getMessage()));
		return;
	}
	String querystr = "action=" + action + "&CPages=" + curpage + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
	//out.print(StrUtil.Alert_Redirect("删除成功！", "message.jsp?" + querystr));
	if(re){
		json.put("ret","1");
		json.put("url",querystr);
		json.put("msg","删除成功!");
	}else{
		json.put("ret","0");
		json.put("url","");
		json.put("msg","删除失败!");
	}
	out.print(json.toString());
	return;
}
//消息会话
else if (op.equals("delChat")) {
	boolean re = false;
	try {
		re = Msg.doChat(request, true);
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "消息删除失败："+e.getMessage()));
		return;
	}
	String querystr = "action=" + action + "&CPages=" + curpage + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
	//out.print(StrUtil.Alert_Redirect("删除成功！", "message.jsp?" + querystr));
	if(re){
		json.put("ret","1");
		json.put("url",querystr);
		json.put("msg","删除成功!");
	}else{
		json.put("ret","0");
		json.put("url","");
		json.put("msg","删除失败!");
	}
	out.print(json);
	return;
}
//消息转存到文件柜
else if (op.equals("transmitToIdiofileark")) {
	IdiofilearkMgr imgr = new IdiofilearkMgr();
	boolean re = false;
	try {
		re = imgr.TransmitMsgToidiofileark(request);
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "消息转存至文件柜失败："+e.getMessage()));
		return;
	}
	String querystr = "action=" + action + "&CPages=" + curpage + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
	//out.print(StrUtil.Alert_Redirect("操作成功！", "message.jsp?" + querystr));
	if(re){
		json.put("ret","1");
		json.put("url",querystr);
		json.put("msg","操作成功!");
	}else{
		json.put("ret","0");
		json.put("url","");
		json.put("msg","操作失败!");
	}
	out.print(json);
	return;
} else //设为已读
	if (op.equals("setReaded")) {
		String ids = ParamUtil.get(request, "ids");
		
		if (ids==null) {
			out.print(StrUtil.jAlert_Back("请选择消息！","提示"));
			return;
		}
			if(ids != null){
				String[] newIds = ids.split(",");
				for (int i=0; i<newIds.length; i++) {
					md = (MessageDb)md.getMessageDb(StrUtil.toInt(newIds[i]));
					md.setReaded(true);
					md.save();		
				}
			}
			
			String sql = "select id from oa_message where isreaded=0 and box=0 and is_dustbin=0 and type=0 and receiver="+StrUtil.sqlstr(name);
			int total = md.list(sql).size();
		
		String querystr = "action=" + action + "&CPages=" + curpage + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
		json.put("ret","1");
		json.put("msg","操作成功!");
		json.put("url",querystr);
		json.put("total", total);
		out.print(json);
		//out.print(StrUtil.Alert_Redirect("操作成功！", "message.jsp?" + querystr));
		return;
	}
//
String sql = "select id from oa_message where is_sent=1 and receiver="+StrUtil.sqlstr(name)+" and box=" + MessageDb.INBOX + " and is_dustbin=0 and type<>10";
if (action.equals("search")) {
	if (kind.equals("title")) {
		sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
	} else if(kind.equals("content")) {
		sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
	} else if(kind.equals("sender")) {
		sql += " and sender in (select name from users where realname like " + StrUtil.sqlstr("%" + what + "%") + ")";
	} else{
		sql += " and box=" + MessageDb.INBOX + " and isreaded=0 and (content like " + StrUtil.sqlstr("%" + what + "%") + "or title like " + StrUtil.sqlstr("%" + what + "%") + ")";
	}
}
if(orderBy.equals("")) {
	sql += " order by rq desc";
} else {
	if(orderBy.equals("byTitle")) {
		sql += " order by title";
	} else if(orderBy.equals("bySender")) {
		sql += " order by sender";
	} else if(orderBy.equals("byDate")) {
		sql += " order by send_time";
	} else {
		sql += " order by " + orderBy;
	}
	if(order.equals("asc")) {
		sql += " asc";
	} else {
		sql += " desc";
	}
}
int total = md.getObjectCount(sql);
paginator.init(total, pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

int id,type;
String title="",sender="",receiver="",rq="";
boolean isreaded = true;
Iterator ir = md.list(sql, (curpage-1)*pagesize, curpage*pagesize-1).iterator();

UserSetupDb userSetupDb = new UserSetupDb();
userSetupDb = userSetupDb.getUserSetupDb(name);
boolean isMsgChat = userSetupDb.isMsgChat();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>消息中心</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery.toaster.js"></script>
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
function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}
</script>
</head>
<body>
<div class="message_content">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="message_tdStyle_1">收件箱
     <span class="grey5"> (
      <%
	  UserSetupDb usd = new UserSetupDb();
	  usd = usd.getUserSetupDb(name);
	  %>
     容量<%=UtilTools.getFileSize(usd.getMsgSpaceAllowed())%>，剩余<%=UtilTools.getFileSize(usd.getMsgSpaceAllowed() - usd.getMsgSpaceUsed())%>
      )</span>
      </td>
    </tr>
  </tbody>
</table>
<table  width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
	<tr>
      <td class="message_btnbox" colspan="2">
      		<!-- <img src="../skin/bluethink/images/message/message_revert.png" onclick="doMyreply()"/> -->
      		<img src="../skin/bluethink/images/message/message_furbish.png" onclick="window.location.reload()" />
      		<img src="../skin/bluethink/images/message/message_close.png"   onClick="doDel()" />
	      	<img src="../skin/bluethink/images/message/message_transmit.png"  onclick="doTransmit()" />
	      	<!-- <img src="../skin/bluethink/images/message/delChat.png"  onclick="doDelChat()" />-->
      	</td>
    </tr>
  <tr class="message_search">
  	<td align="left">
	<form name="formSearch" action="message.jsp" method="get">
	按
      <select id="kind" name="kind">
        <option value="title">标题</option>
        <option value="content">内容</option>
        <option value="sender">发件人</option>
        <option value="notreaded">未读消息</option>
      </select>
      <script>
		o("kind").value = "<%=kind%>";
	  </script>
      &nbsp;
      <input name=what size=20 value="<%=what%>">
      <input name="button" type="submit" value="搜索" class="btn">
	  <input name="action" value="search" type="hidden" />
	  </form>
	  </td>
    <td align="right" class="grey5">共 <b><%=paginator.getTotal() %></b> 条　
      <input id="pagesize" name="pagesize" size="1" style="width:20px" onKeyDown="if (window.event.keyCode==13) window.location.href='message.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=<%=orderBy%>&order=<%=order%>&pagesize=' + this.value" value="<%=pagesize%>" onchange="window.location.href='message.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=<%=orderBy%>&order=<%=order%>&pagesize=' + this.value" />
条/页　<b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<%
	String orderOp = "";
	if(order.equals("") || order.equals("desc")) {
		orderOp = "asc";
	} else {
		orderOp = "desc";
	}
	String imgSrc = "";
	if(orderOp.equals("desc")) {
		imgSrc = "../netdisk/images/arrow_up.gif";
	} else {
		imgSrc = "../netdisk/images/arrow_down.gif";
	}
%>
<form id="form1" name="form1" method="post" action="message.jsp">
<input name="CPages" value="<%=curpage%>" type="hidden" />
  <table id="mainTable" width="98%" align="center" class="message_tabStyle_1">
  	<thead>
    <tr class="message_tabStyle_1_tr">
      <td width="4%" align="center"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td  width="22%" align="center">
      	<a href="message.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=byTitle&order=<%=orderOp%>">
      		标题
<%
	if(orderBy.equals("byTitle")) {
%>
			<img src="<%=imgSrc%>"  />
<%
	}
%>
      </a>
      </td>
      <td width="24%" align="center" >
      <a href="listrecycle.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=bySender&order=<%=orderOp%>">
      		发件人
<%
	if(orderBy.equals("bySender")) {
%>		
			<img src="<%=imgSrc%>"  />
<%
	}
%>      </td>
     
      <td  width="9%" align="center">
	  <a href="message.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=msg_level&order=<%=orderOp%>">等级
<%
	if(orderBy.equals("msg_level")) {
%>		
			<img src="<%=imgSrc%>"  />
<%
	}
%>	  </a>
	  </td>
	  
	   <td  width="31%" align="center">
      	<a href="message.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=byDate&order=<%=orderOp%>">
      	日期
<%
	if(orderBy.equals("byDate")) {
%>		
		<img src="<%=imgSrc%>"  />
<%
	}
%>
      	</a>
	  </td>
	  <td  width="10%" align="center">操作</td>
    </tr>
    </thead>
    <input name="op" type="hidden" value="">
<%
	int i = 0;
	while (ir.hasNext()) {
 	      md = (MessageDb)ir.next(); 
		  i++;
		  id = md.getId();
		  String title1 = md.getTitle();
		  title = md.getTitle();
		  if(title.length()>30){
		  	 title = title.substring(0,30)+"...";
		  }
		  sender = md.getSender();
		  receiver = md.getReceiver();
		  rq = md.getRq();
		  type = md.getType();
		  isreaded = md.isReaded();
		  int msgLevel = md.getMsgLevel();
		  int receipt = md.getReCeiptState();
%>
    <tr >
      <td align="center" class="message_line"><input type="checkbox" name="ids" value="<%=id%>"></td>
      <td class="message_line">
      	<a href="javascript:;" onclick="showMsg(<%=id %>,<%=isMsgChat %>)" title="<%=title1 %>"><span id="title_span<%=id %>">
<%
	if (isreaded) {
%>
			<%=title%>
<%
	} else {
%>
			<b><%=title%></b>
<%
	}
%>
        </span></a>      </td>
      <td align="center" class="message_line">
	  <%if (sender.equals(MessageDb.SENDER_SYSTEM)) {%>
	  <%=sender%>
	  <%}else{%>
	  <a href="javascript:;" onclick="addTab('<%=md.getSenderRealName()%>', '<%=Global.getFullRootPath(request)%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(md.getSender())%>')"><%=md.getSenderRealName()%></a>
	  <%}%>	  </td>

      <td align="center" class="message_line">
<%
	if(msgLevel==0) {
		out.print("普通");
	} else if(msgLevel==1) {
		out.print("<font color='red'>紧急</font>");
	}
%>      </td>
      <td align="center" class="message_line">
	  <%=md.getSendTime()%>	  </td>
	  <td align="center" class="message_line">
	  <a href="javascript:;" onclick="showMsg(<%=id %>,<%=isMsgChat %>)">查看</a>
	  </td>
    </tr>
    <%}%>
  </table>
  <table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
    <tr class="message_style_tr">
      <td align="left">
      <input type="button" value="置为已读" class="blue_btn_90" onclick="setReaded('ids')" style="margin-top:10px" />
      </td>
      <td align="right">
      <%
	  String querystr = "action=" + action + "&op=" + op + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
 	  out.print(paginator.getCurPageBlock("message.jsp?"+querystr));
	  %>
      </td>
    </tr>
  </table>
</form>
</div>
</body>
<script>
function setReaded(checkboxname){
	var ids = getCheckboxValue("ids");
    var checkedboxs = 0;
	var checkboxboxs = document.getElementsByName(checkboxname);
	
	if (checkboxboxs!=null)	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked){
			   checkedboxs = 1;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].checked){
			   checkedboxs = 1;
			}
		}
	}

	if (checkedboxs==0){
	    jAlert("请先选择消息！","提示");
		return;
	}
	
	$.ajax({
		type:"post",
		url:"message.jsp",
		data :{
			op :"setReaded",
			ids:ids
		},
		dataType:"html",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			data = $.parseJSON(data.trim());
			//jAlert(data.msg,"提示");
			if(data.ret == "1"){
				var idAry = ids.split(',');
				for (i = 0; i < idAry.length; i++) {
					$('#title_span' + idAry[i]).html($('#title_span' + idAry[i]).text().trim());
				}
				parent.frames[0].document.getElementById('unreaded').innerText = (data.total == 0 ? '' : '(' + data.total + ')');
				$.toaster({priority : 'info', message : '操作成功' });
			} else {
				$.toaster({priority : 'info', message : '操作失败' });
			}
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
	
	//form1.action = "message.jsp";
    //form1.op.value = "setReaded"; 
	//form1.submit();
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

function doDel(){
	var ids = getCheckboxValue("ids");
	if(ids == ''){
		jAlert("请选择消息!","提示");
		return;
	}
	jConfirm('您确定要删除么？','提示',function(r){
		if(!r){return;}
		else{
			//form1.submit();
			$.ajax({
				type: "post",
				url: "message.jsp",
				data : {
					op: "del",
					ids: ids
		        },
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					//ShowLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data);
					if(data.ret == "1"){
						window.location.href = "message.jsp?"+data.url;
						parent.leftFrame.location.href="left_menu.jsp";
					}
				},
				complete: function(XMLHttpRequest, status){
					//HideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	})
}

function doDelChat(){
	var ids = getCheckboxValue("ids");
	if(ids == ''){
		jAlert("请选择消息!","提示");
		return;
	}
	jConfirm('您确定要删除么？','提示',function(r){
		if(!r){
			return;
		}else{
			//form1.submit();
			$.ajax({
			type: "post",
			url: "message.jsp",
			data : {
				op: "delChat",
				ids: ids
	        },
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				//ShowLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg,"提示");
				if(data.ret == "1"){
					window.location.href = "message.jsp?"+data.url;
					parent.leftFrame.location.href="left_menu.jsp";
				}
			},
			complete: function(XMLHttpRequest, status){
				//HideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
			});
		}
	})
}

function doTransmit(){
	var id = getCheckboxValue("ids");
	if(id.indexOf(",")>=0){
		jAlert("只能单个转发","提示");
		return;
	}
	if(id == ''){
		jAlert("请选择消息!","提示");
		return;
	}
	window.location.href='transmit.jsp?id='+id;
}
function showMsg(id,isMsgChat){
	if(isMsgChat){
		window.location.href='showmsg.jsp?id='+id;
	}else{
		window.location.href='showmsg1.jsp?id='+id;
	}
	
	
	
}
</script>
</html>