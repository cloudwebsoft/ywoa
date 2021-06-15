<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String status = ParamUtil.get(request, "status");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "status", status, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>任务</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script language="JavaScript">
//--------------展开任务----------------------------
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2 = eval("document.all.followImg" + t_id);
	var targetTR2 = eval("document.all.follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="forum/images/minus.gif";
			if (targetImg2.getAttribute("loaded")=="no"){
				o("hiddenframe").contentWindow.location.replace("task_tree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="forum/images/plus.gif";
		}
	}
}
</script>
<style type="text/css">
<!--
.STYLE1 {color: #FF0000}
-->
</style>
</head>
<body>
<iframe width=0 height=0 src="" id="hiddenframe" style="display:none"></iframe>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%@ include file="task_inc_menu_top.jsp"%>
<script>
<%
int index = 1;
if (status.equals("0")) {
	index = 1;
}
else if (status.equals("1")) {
	index = 2;
}
else if (status.equals("2")) {
	index = 3;
}
else if (status.equals("3")) {
	index = 5;
}
else if (status.equals("4")) {
	index = 4;
}
else
	index = 6;
%>
o("menu<%=index%>").className="current";
</script>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String querystring = fchar.getNullString(request.getQueryString());
String privurl = StrUtil.getUrl(request);
%>
<table width="90%" border="0" align="center">
  <tr>
    <td align="center">&nbsp;</td>
  </tr>
  <tr> 
    <td align="center">类型：<img src=images/task/icon-task.gif align="absmiddle"> 
      任务<img src=images/task/icon-subtask.gif align="absmiddle"> 子任务 <img src=images/task/icon-result.gif align="absmiddle"> 
      结果 <img src=images/task/icon-hurry.gif align="absmiddle"> 催办&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <a href="task_init.jsp?op=new&privurl=<%=privurl%>"><font color="red">发起任务</font></a></td>
  </tr>
</table>
<CENTER>
<%
  		String myname = privilege.getUser(request);		
		String querystr = "";
		String sql="";
		
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();

		TaskDb td = new TaskDb();
        if (status == "") {
            sql = "select distinct rootid from task where person=" +
						 StrUtil.sqlstr(myname) + " and type<" + TaskDb.TYPE_RESULT + " order by rootid desc";

        } else {
            sql =
                    "select distinct rootid from task where person=" +
						 StrUtil.sqlstr(myname) + " and status=" +
                    status + " and type<" +
                    TaskDb.TYPE_RESULT + " order by rootid desc";
        }		
		
		String op = ParamUtil.get(request, "op");
		if (op.equals("search")) {
			String title = ParamUtil.get(request, "title");
			String content = ParamUtil.get(request, "content");
			String beginDate = ParamUtil.get(request, "beginDate");
			String endDate = ParamUtil.get(request, "endDate");
			sql = "select distinct rootid from task where person=" +
						 StrUtil.sqlstr(myname) + " and type<" + TaskDb.TYPE_RESULT;
			 if (!title.equals(""))
					sql += " and title like " + StrUtil.sqlstr("%" + title + "%");
			if (!content.equals(""))
					sql += " and content like " + StrUtil.sqlstr("%" + content + "%");
			if (!beginDate.equals(""))
					sql += " and mydate>=" + StrUtil.sqlstr(beginDate);
			if (!endDate.equals(""))
					sql += " and mydate<=" + StrUtil.sqlstr(endDate);
        	if (!status.equals(""))
				sql += " and status=" + status;
			sql += " order by rootid desc";
			querystr += "op=search&title=" + StrUtil.UrlEncode(title) + "&content=" + StrUtil.UrlEncode(content) + "&beginDate=" + beginDate + "&endDate=" + endDate;
		}
		
		// out.print(sql);
		
		if(!status.equals("")){
			if (querystr.equals(""))
			   	querystr = "status=" + status;
			else
				querystr += "&status=" + status;
		}
		
		ListResult lr = td.listResult(sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (v!=null)
			ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
  <table width="100%" border="0" class="p9">
    <tr> 
      <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></td>
    </tr>
  </table>
  <TABLE width="98%" 
border=0 align=center cellPadding=1 cellSpacing=0 borderColor=#edeced class="tabStyle_1">
    <TBODY>
      <TR height=25> 
        <TD class="tabStyle_1_title" colSpan=3 align=middle noWrap>&nbsp;任务列表 <B>(点 <IMG 
      src="forum/images/plus.gif"> 即可展开任务列表)</B></TD>
        <TD width="8%" align=left noWrap class="tabStyle_1_title">开始日期</TD>
        <TD width="8%" align=left noWrap class="tabStyle_1_title">结束日期</TD>
        <TD width=12% align=left noWrap class="tabStyle_1_title">进度</TD>
        <TD width=9% align=left noWrap class="tabStyle_1_title">发起人</TD>
        <TD width=4% align=left noWrap class="tabStyle_1_title">[回]</TD>
        <TD width=9% align=left noWrap class="tabStyle_1_title">发起时间</TD>
      </TR>
<%		
int id = 0;	
String title = "",initiator="",mydate="";
int expression=0;
int i = 0,type=0,recount=0,isfinish=0;
while (ir!=null && ir.hasNext())
{
  td = (TaskDb)ir.next();
  i++;
  id = td.getId();
  
  title = td.getTitle();
  initiator = td.getInitiator();
  mydate = DateUtil.format(td.getMyDate(), "yy-MM-dd");
  expression = td.getExpression();
  type = td.getType();
  recount = td.getReCount();
  isfinish = td.getStatus();
%>
      <tr> 
        <td width=6% height="24" align=center noWrap><%if (type==TaskDb.TYPE_TASK) {%>
          <%if (isfinish==TaskDb.STATUS_FINISHED) {%>
          <img src="images/task/icon-yes.gif">
          <%}else if (isfinish==TaskDb.STATUS_NOTFINISHED){%>
          <img src="images/task/icon-notyet.gif">
          <%}else if (isfinish==TaskDb.STATUS_DISCARD) {%>
          <img src="images/task/icon-no.gif">
          <%}else if (isfinish==TaskDb.STATUS_ARRANGED) {%>
          <img src="images/task/icon-arranged.gif">
          <%}else if (isfinish==TaskDb.STATUS_URGENT) {%>
          <img src="images/task/icon-urgent.gif">
          <%}
		  }else if (type==TaskDb.TYPE_SUBTASK) {%>
[<%=TaskDb.getTaskStatusDesc(td.getStatus())%>]
<%}%></td>
        <td width=4% align=middle> 
          <%if (expression!=0) {%>
          <img src="forum/images/emot/em<%=expression%>.gif" border=0> 
          <%} else {%>
		&nbsp;
		<%}%></td>
        <td width="40%" align=left onMouseOver="this.style.backgroundColor='#eeeeee'" onMouseOut="this.style.backgroundColor=''"> 
        <%
		if (recount==0) {
		%> <img id=followImg<%=id%> title="展开回复" src="forum/images/minus.gif" loaded="no"> 
        <% }else { %> 
		<img id=followImg<%=id%> title=展开回复 style="CURSOR: hand" onclick="loadThreadFollow(<%=id%>,<%=id%>,'')" src="forum/images/plus.gif" loaded="no"> <% } %> <a href="task_show.jsp?showid=<%=id%>&rootid=<%=id%>" title="开始日期：<%=DateUtil.format(td.getBeginDate(), "yyyy-MM-dd")%> 结束日期：<%=DateUtil.format(td.getEndDate(), "yyyy-MM-dd")%>"><%=title%></a>
		
		</td>
        <td align=center noWrap><%=DateUtil.format(td.getBeginDate(), "yy-MM-dd")%></td>
        <td align=center noWrap><%=DateUtil.format(td.getEndDate(), "yy-MM-dd")%></td>
        <td align=left title="<%=td.getProgress()%>%">
			<table width="100%" border="0" cellpadding="0" cellspacing="0" style="height:6px">
				<tr>
				  <td bgcolor="#CCCCCC" style="height:6px;padding:0px"><img src="forum/images/vote/bar7.gif" width="<%=td.getProgress()%>%" height="6" /></td>
				</tr>
		  </table>
		</td>		
        <td align=center> 
		<%
		UserDb ud = new UserDb();
		ud = ud.getUserDb(initiator);
		%>
		<%=ud.getRealName()%>
		</td>
        <td align=center><font color=red>[<%=recount%>]</font></td>
        <td align=center><%=mydate%></td>
      </tr>
      <tr id=follow<%=id%> style="DISPLAY: none"> 
        <td noWrap align=middle>&nbsp;</td>
        <td align=middle>&nbsp;</td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" 
    onMouseOut="this.style.backgroundColor=''" align=left colspan="7"> 
        <div id=followDIV<%=id%> 
      style="WIDTH: 100%;BACKGROUND-COLOR: lightyellow" 
      onclick='loadThreadFollow(<%=id%>,<%=id%>,"")'>正在读取任务树，请稍侯……</div></td>
      </tr>
<%}%>
    </tbody>
  </table>
  <table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td height="23" align="center"><div align="right"> 
            <%
			out.print(paginator.getCurPageBlock("?"+querystr));
			%>
      &nbsp;</div> 
      </td>
    </tr>
  </table>
</CENTER>
<table width="90%" border="0" align="center">
  <tr>
    <td align="center">&nbsp;</td>
  </tr>
</table>
</body>
</html>
