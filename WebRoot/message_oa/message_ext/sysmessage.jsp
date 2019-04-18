<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.idiofileark.*" %>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
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
if (kind.equals(""))
	kind = "title";
String what = ParamUtil.get(request, "what");

MessageDb md = new MessageDb();
int pagesize = ParamUtil.getInt(request, "pagesize", 10);

Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

if (op.equals("setReaded")) {
	String[] ids = ParamUtil.getParameters(request, "ids");
	if (ids==null) {
		out.print(StrUtil.Alert_Back("请选择消息！"));
		return;
	}
	for (int i=0; i<ids.length; i++) {
		md = (MessageDb)md.getMessageDb(StrUtil.toInt(ids[i]));
		md.setReaded(true);
		md.save();		
	}
	
	String querystr = "action=" + action + "&CPages=" + curpage + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
	out.print(StrUtil.Alert_Redirect("操作成功！", "sysmessage.jsp?" + querystr));
	return;
}
else if (op.equals("del")) {
	boolean re = false;
	try {
		re = Msg.doDustbin(request, true);
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "消息删除失败："+e.getMessage()));
		return;
	}
	
	String querystr = "action=" + action + "&CPages=" + curpage + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
	out.print(StrUtil.Alert_Redirect("操作成功！", "sysmessage.jsp?" + querystr));
	return;
}
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
	out.print(StrUtil.Alert_Redirect("操作成功！", "sysmessage.jsp?" + querystr));
	return;
}

String sql = "select id from oa_message where is_sent=1 and receiver="+StrUtil.sqlstr(name)+" and box=" + MessageDb.INBOX + " and is_dustbin=0  and type=10";
if (action.equals("search")) {
	if (kind.equals("title")) {
		sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
	} else if(kind.equals("content")) {
		sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
	} else if (kind.equals("notreaded")) {
		sql += " and box=" + MessageDb.INBOX + " and isreaded=0 and (content like " + StrUtil.sqlstr("%" + what + "%") + "or title like " + StrUtil.sqlstr("%" + what + "%") + ")";
	}
}
if(orderBy.equals("")) {
	// sql += " order by isreaded asc,rq desc";
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

// out.println(sql);
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
int i = 0;
Iterator ir = md.list(sql, (curpage-1)*pagesize, curpage*pagesize-1).iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>消息中心</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../../inc/common.js"></script>
<script src="../../js/jquery.js"></script>
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
<!-- <table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">收件箱
      (
      <%--
	 UserSetupDb usd = new UserSetupDb();
	  usd = usd.getUserSetupDb(name);
	  --%>
      容量<%--=NumberUtil.round((double)usd.getMsgSpaceQuota()/1024000, 2)--%>M，剩余<%--=//NumberUtil.round(((double)(usd.getMsgSpaceQuota() - usd.getMsgSpaceUsed()))/1024000, 2)--%>M
      )
      </td>
    </tr>
  </tbody>
</table> -->

<table class="percent98" width="98%" border="0" cellpadding="0" cellspacing="0" align="center" style="margin-top:20px;">
  <tr>
  	<td align="left">
	<form name="formSearch" action="sysmessage.jsp" method="get">
	按
      <select id="kind" name="kind">
        <option value="title">标题</option>
        <option value="content">内容</option>
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
    <td align="right">共 <b><%=paginator.getTotal() %></b> 条　
      <input id="pagesize" name="pagesize" size="1" style="width:20px" onKeyDown="if (window.event.keyCode==13) window.location.href='sys_sysmessage.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=<%=orderBy%>&order=<%=order%>&pagesize=' + this.value" value="<%=pagesize%>" onchange="window.location.href='sys_sysmessage.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=<%=orderBy%>&order=<%=order%>&pagesize=' + this.value" />
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
		imgSrc = "../../netdisk/images/arrow_up.gif";
	} else {
		imgSrc = "../../netdisk/images/arrow_down.gif";
	}
%>
<form id="form1" name="form1" method="post" action="sysmessage.jsp">
<input name="CPages" value="<%=curpage%>" type="hidden" />
  <table id="mainTable" width="98%" align="center" class="tabStyle_1 percent98">
  	<thead>
    <tr>
      <td width="4%" align="center" class="tabStyle_1_title"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td class="tabStyle_1_title" width="52%">
      	<a href="sysmessage.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=byTitle&order=<%=orderOp%>">
      		标题
<%
	if(orderBy.equals("byTitle")) {
%>
			<img src="<%=imgSrc%>" style="margin-left:12px" />
<%
	}
%>
      </a>
      </td>
      <td width="24%" class="tabStyle_1_title">
      		发送者
<%
	if(orderBy.equals("bySender")) {
%>		
			<img src="<%=imgSrc%>" style="margin-left:12px" />
<%
	}
%>      </td>
      <td class="tabStyle_1_title" width="11%">
      	<a href="sysmessage.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=byDate&order=<%=orderOp%>">
      	日期
<%
	if(orderBy.equals("byDate")) {
%>		
		<img src="<%=imgSrc%>" style="margin-left:12px" />
<%
	}
%>
      	</a>
	  </td>
      <td class="tabStyle_1_title" width="9%">
	  <a href="sysmessage.jsp?op=<%=op%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=msg_level&order=<%=orderOp%>">等级
<%
	if(orderBy.equals("msg_level")) {
%>		
			<img src="<%=imgSrc%>" style="margin-left:12px" />
<%
	}
%>	  </a>
	  </td>
    </tr>
    </thead>
    <input name="op" type="hidden" value="del">
<%
	while (ir.hasNext()) {
 	      md = (MessageDb)ir.next(); 
		  i++;
		  id = md.getId();
		  title = md.getTitle();
		  sender = md.getSender();
		  receiver = md.getReceiver();
		  rq = md.getRq();
		  type = md.getType();
		  isreaded = md.isReaded();
		  int msgLevel = md.getMsgLevel();
		  int receipt = md.getReCeiptState();
%>
    <tr>
      <td align="center" width="4%"><input type="checkbox" name="ids" value="<%=id%>"></td>
      <td>
		<a href="javascript:;" onclick="addTab('消息', '<%=request.getContextPath()%>/message_oa/message_ext/showmsg.jsp?id=<%=id%>')" title="<%=title%>">
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
        </a>      </td>
      <td align="center">
	  <%if (sender.equals(MessageDb.SENDER_SYSTEM)) {%>
	  <%=sender%>
	  <%}else{%>
	  <a target="_blank" href="../../user_info.jsp?userName=<%=StrUtil.UrlEncode(md.getSender())%>"><%=md.getSenderRealName()%></a>
	  <%}%>	  </td>
      <td align="center">
	  <%=md.getSendTime()%>	  </td>
      <td align="center">
<%
	if(msgLevel==0) {
		out.print("普通");
	} else if(msgLevel==1) {
		out.print("<font color='red'>紧急</font>");
	}
%>      </td>
    </tr>
    <%}%>
  </table>
  <table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
    <tr>
      <td align="left">
      <!--  转存至我的文档：
        <select name="dir_code" id="dir_code" onChange="javascript:submitChageDir('ids','transmitToIdiofileark')">
          <option style="color:#CCCCCC;" value="">请选择转移目录夹</option>
          <%--
	  com.redmoon.oa.idiofileark.Directory dir = new com.redmoon.oa.idiofileark.Directory();
	  com.redmoon.oa.idiofileark.Leaf leaf = dir.getRootNodeOfUserOrInit(name);
      com.redmoon.oa.idiofileark.DirectoryView dv = new com.redmoon.oa.idiofileark.DirectoryView(leaf);
      dv.ShowDirectoryAsOptions(out, leaf, 1);
	  --%>
        </select>-->	
      <input type="button" value="置为已读" class="btn" onclick="setReaded('ids')" />
      <input name="button" type="button" value="删除" onClick="if (getCheckboxValue('ids')=='') { alert('请选择消息！'); return;} if (confirm('您确定要删除么？')) form1.submit()" />
      </td>
      <td align="right"><%
	  String querystr = "action=" + action + "&op=" + op + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&orderBy=" + orderBy + "&order=" + order + "&pagesize=" + pagesize;
 	  out.print(paginator.getCurPageBlock("sysmessage.jsp?"+querystr));
	  %>
      </td>
    </tr>
  </table>
</form>
<br />
</body>
<script>
function submitChageDir(checkboxname, opType){
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
	if (o("dir_code").options[0].selected && o("dir_code").options[0].value==""){
	    return;
	}
	if (checkedboxs==0){
	    alert("请先选择消息！");
		return;
	}
	if (!confirm("您确定要转存么？"))
		return;
    form1.op.value = "transmitToIdiofileark"; 
	form1.submit();
}

function setReaded(checkboxname){
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
	    alert("请先选择消息！");
		return;
	}
	form1.action = "sys_sysmessage.jsp";
    form1.op.value = "setReaded"; 
	form1.submit();
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

</script>
</html>