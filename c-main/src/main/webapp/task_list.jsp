<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>任务督办管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("util/jscalendar/calendar-win2k-2.css"); </style>
<script language="JavaScript">
//--------------展开任务----------------------------
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2 =eval("document.all.followImg" + t_id);
	var targetTR2 =eval("document.all.follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="forum/images/minus.gif";
			if (targetImg2.loaded=="no"){
				document.frames["hiddenframe"].location.replace("task_tree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="forum/images/plus.gif";
		}
	}
}
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">任务管理</td>
    </tr>
  </tbody>
</table>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request, priv) || !privilege.isUserPrivValid(request, "admin.task")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String querystring = fchar.getNullString(request.getQueryString());
String privurl = StrUtil.getUrl(request);

String title = ParamUtil.get(request, "title");
String content = ParamUtil.get(request, "content");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String person = ParamUtil.get(request, "person");
String status = ParamUtil.get(request, "status");
%>
<br>
<br>
<form action="task_list.jsp?op=search" name="form1" method="post">
<table width="62%" border="0" align="center" cellPadding="2" cellSpacing="0" class="tabStyle_1 percent60">
  <thead>
  <tr>
    <td colspan="6" noWrap>查询</td>
  </tr>
  </thead>
  <tr>
    <td width="14%" noWrap>用&nbsp;&nbsp;户&nbsp;&nbsp;名</td>
    <td width="19%" noWrap><input name="person" id="person" size="10" maxlength="80" value="<%=person%>"></td>
    <td width="15%">任务名称
    </td>
    <td width="18%">
      <input name="title" id="title" size="10" maxlength="80" value="<%=title%>" />
    </td>
    <td width="13%">任务内容</td>
    <td width="21%"><input name="content" id="content" size="10" maxlength="200" value="<%=content%>" /></td>
  </tr>
      <tr>
        <td noWrap>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态</td>
        <td noWrap><select name="status">
          <option value="">全部</option>
          <%for (int i=0; i<=9; i++) {%>
          <option value="<%=i%>"><%=TaskDb.getTaskStatusDesc(i)%></option>
          <%}%>
        </select>
		<script>
		form1.status.value = "<%=status%>";
		</script>
		</td>
        <td>开始日期
          </td>
        <td><input type="text" id="beginDate" name="beginDate" size="10" value="<%=beginDate%>" /><script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
            </script></td>
        <td>结束日期
          </td>
        <td><input type="text" id="endDate" name="endDate" size="10" value="<%=endDate%>" /><script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
          </script></td>
      </tr>
      <tr align="middle">
        <td colSpan="6" align="center" noWrap><input class="btn" type="submit" value="搜索">
        </td>
      </tr>
</table>
</form>
<CENTER>
<%
  		String myname = privilege.getUser(request);		
		String querystr = "";
		String sql="";
		
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		String unitCode = privilege.getUserUnitCode(request);
			
		TaskDb td = new TaskDb();
		// sql = TaskSQLBuilder.getUserJoinTask(myname, status);
        if (status == "") {
            sql = "select id from task where type<" + TaskDb.TYPE_RESULT + " and parentid=-1 and unit_code=" + StrUtil.sqlstr(unitCode) + " order by id desc";
        } else {
            sql =
                    "select id from task where status=" +
                    status + " and type<" +
                    TaskDb.TYPE_RESULT + " and parentid=-1 and unit_code=" + StrUtil.sqlstr(unitCode) + " order by id desc";
        }
		
		String op = ParamUtil.get(request, "op");
		if (op.equals("search")) {
			sql = "select id from task where type<" + TaskDb.TYPE_RESULT + " and parentid=-1";
			if (!person.equals("")) {
					sql += " and person like " + StrUtil.sqlstr("%" + person + "%");
			}
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
			sql += " and unit_code=" + StrUtil.sqlstr(unitCode) + " order by rootid desc";
		}
		querystr += "op=search&status=" + status + "&person=" + StrUtil.UrlEncode(person) + "&title=" + StrUtil.UrlEncode(title) + "&content=" + StrUtil.UrlEncode(content) + "&beginDate=" + beginDate + "&endDate=" + endDate;
		
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
  <TABLE width="100%" border=0 align=center cellPadding=1 cellSpacing=0 class="tabStyle_1">
  <thead>
      <TR height=25>
        <TD width="5%" align=middle noWrap>&nbsp;</TD> 
        <TD align=middle noWrap>&nbsp;任务列表</TD>
        <TD width="9%" align=left noWrap>开始日期</TD>
        <TD width="9%" align=left noWrap>结束日期</TD>
        <TD width=7% align=left noWrap>状态</TD>
        <TD width=6% align=left noWrap>发起人</TD>
        <TD width=11% align=left noWrap>发起时间</TD>
      </TR>
  </thead>
  <TBODY>
<%		
int id = 0;	
String initiator="",mydate="";
int expression=0;
int i = 0,type=0,recount=0,isfinish=0;
while (ir!=null && ir.hasNext())
{
  td = (TaskDb)ir.next();
  i++;
  id = td.getId();
  
  initiator = td.getInitiator();
  mydate = DateUtil.format(td.getMyDate(), "yy-MM-dd HH:mm");
  expression = td.getExpression();
  type = td.getType();
  recount = td.getReCount();
  isfinish = td.getStatus();
%>
      <tr>
        <td align=center noWrap><%if (type==TaskDb.TYPE_TASK) {%>
          <%if (isfinish==TaskDb.STATUS_FINISHED) {%>
          <img src="images/task/icon-yes.gif" />
          <%}else if (isfinish==TaskDb.STATUS_NOTFINISHED){%>
          <img src="images/task/icon-notyet.gif" />
          <%}else if (isfinish==TaskDb.STATUS_DISCARD) {%>
          <img src="images/task/icon-no.gif" />
          <%}else if (isfinish==TaskDb.STATUS_ARRANGED) {%>
          <img src="images/task/icon-arranged.gif" />
          <%}else if (isfinish==TaskDb.STATUS_URGENT) {%>
          <img src="images/task/icon-urgent.gif" />
        <%}
		  }%></td> 
        <td height="24" align=left noWrap><%if (expression!=0) {%>
          <img src="forum/images/emot/em<%=expression%>.gif" border=0> 
          <%} else {%>
		&nbsp;
		<%}%> 
        <a target="_blank" href="task_show.jsp?showid=<%=id%>&rootid=<%=td.getRootId()%>"><%=td.getTitle()%></a>		</td>
        <td width="9%" align=left noWrap><%=DateUtil.format(td.getBeginDate(), "yyyy-MM-dd")%></td>
        <td width="9%" align=left noWrap><%=DateUtil.format(td.getEndDate(), "yyyy-MM-dd")%></td>
        <td width=7% align=left><%=td.getTaskStatusDesc(td.getStatus())%></td>
        <td align=left width=6%><%
		UserDb ud = new UserDb();
		ud = ud.getUserDb(initiator);
		%>
        <%=ud.getRealName()%></td>
        <td align=left width=11%><%=mydate%></td>
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
<iframe width=0 height=0 src="" id="hiddenframe"></iframe>
</body>
</html>
