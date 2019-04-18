<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>	
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划-选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>

<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script>
function sel(id,author,title,deptName,userName,endTime,days) {
	if (window.opener.selWorkplan(id,author,title,deptName,userName,endTime,days))
		window.close();
	else
		alert("计划已被选择！");
}
</script>
</head>
<body>
<%@ include file="workplan_list_sel_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<%
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv))
	{
		// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		// return;
	}
	String userName = ParamUtil.get(request, "userName");
	if(userName.equals("")){
		userName = privilege.getUser(request);
	}
	String op = ParamUtil.get(request, "op");
	String action = ParamUtil.get(request, "action");
	String kind = ParamUtil.get(request, "kind");
	String what = ParamUtil.get(request, "what");
	String beginDate = ParamUtil.get(request, "beginDate");
	String endDate = ParamUtil.get(request, "endDate");
	String typeId = ParamUtil.get(request, "typeId");
	
	try {
		com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "typeId", typeId, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "kind", kind, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "kind", kind, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "beginDate", beginDate, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "endDate", endDate, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;	
	}
	
	String orderBy = ParamUtil.get(request, "orderBy");
	if (orderBy.equals(""))
		orderBy = "beginDate";
	String sort = ParamUtil.get(request, "sort");
	if (sort.equals(""))
		sort = "desc";
	String querystr = "";
	// String sql = "select id from work_plan where author=" + StrUtil.sqlstr(privilege.getUser(request));
	String sql = "select distinct p.id, " + orderBy + " from work_plan p, work_plan_user u where u.workPlanId=p.id and u.userName=" + StrUtil.sqlstr(privilege.getUser(request));
	com.redmoon.oa.workplan.Privilege wpvg = new com.redmoon.oa.workplan.Privilege();
	boolean isMaster = wpvg.isWorkPlanMaster(request);
	if (isMaster) {
		sql = "select distinct p.id, " + orderBy + " from work_plan p";		
	}
	
	if (action.equals("search")) {
		if (!typeId.equals("")) {
			sql += " and p.typeId=" + typeId;
		}
		if (kind.equals("title")) {
			sql += " and p.title like " + StrUtil.sqlstr("%" + what + "%");
		}
		else if (kind.equals("content")) {
			sql += " and p.content like " + StrUtil.sqlstr("%" + what + "%");
		}
		if (!beginDate.equals("")) {
			sql += " and p.beginDate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
		}
		if (!endDate.equals("")) {
			sql += " and p.endDate<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
		}
	}
	
	sql += " order by " + orderBy + " " + sort;
	
	String urlStr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate + "&typeId=" + typeId;
	querystr = urlStr + "&orderBy=" + orderBy + "&sort=" + sort;
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" style="margin-top:5px">
  <tr> 
   <td align="center">
          <form name="formSearch" action="workplan_list_sel.jsp" method="get">
			<input name="action" value="search" type="hidden" />
		  <%
          WorkPlanTypeDb wptd = new WorkPlanTypeDb();
          String opts = "";
          Iterator ir = wptd.list().iterator();
          while (ir.hasNext()) {
            wptd = (WorkPlanTypeDb)ir.next();
            opts += "<option value='" + wptd.getId() + "'>" + wptd.getName() + "</option>";
          }
          %>
            &nbsp;类型
           	<select name="typeId" id="typeId">
           	<option value="">不限</option>
           	<%=opts%>
       	    </select>
              <script>
			  formSearch.typeId.value = "<%=typeId%>";
			  </script>
       	    	开始日期
                <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=15>
             	结束日期 
             	<input id="endDate" name="endDate" value="<%=endDate%>" size=15>
             	<select name="kind">
                  <option value="title">标题</option>
                  <option value="content">内容</option>
                </select>
              <input name=what size=20 value="<%=what%>">
              &nbsp;
              <input class="tSearch" name="submit" type=submit value="搜索">
              <input name="op" type="hidden" value="<%=op%>">
          </form>
          <%if (action.equals("search")) {%>
			  <script>
			  formSearch.kind.value = "<%=kind%>";
			  formSearch.typeId.value = "<%=typeId%>";
			  </script>
          <%}%>
    </td>
  </tr>
  <tr>
    <td width="72%" valign="top">
	<%
		int pagesize = ParamUtil.getInt(request, "pageSize", 20);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		WorkPlanDb wpd = new WorkPlanDb();
		
		ListResult lr = wpd.listResult(sql, curpage, pagesize);
		int total = lr.getTotal();
		Vector v = lr.getResult();
		if (v!=null)
			ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>&nbsp;
          </td>
        </tr>
      </table>
      <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <tr align="center">
          <td width="303" style="cursor:pointer" class="tabStyle_1_title" abbr="title">标题</th>		  
          <td width="80" style="cursor:pointer" class="tabStyle_1_title" abbr="author">拟定者</th>
          <td width="133" style="cursor:pointer" class="tabStyle_1_title" abbr="progress">进度</th>
          <td width="102" style="cursor:pointer" class="tabStyle_1_title" abbr="beginDate">开始日期</th>
          <td width="103" style="cursor:pointer" class="tabStyle_1_title" abbr="endDate">结束日期</th>
          <td width="84" style="cursor:pointer" class="tabStyle_1_title" abbr="typeId">类型</th>
          <td width="93" class="tabStyle_1_title">操作</th>
        </tr>
        <%	
	  	int i = 0;
		UserMgr um = new UserMgr(); 
		while (ir!=null && ir.hasNext()) {
			wpd = (WorkPlanDb)ir.next();
			i++;
			int id = wpd.getId();
			String sbeginDate = DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd");
			String sendDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");
            int days = DateUtil.datediff(wpd.getEndDate(), new Date());
			if (days<0)
				days = 0;	
		%>
		<tr> 
          <td><a target="_blank" href=workplan_show.jsp?id=<%=id%>><%=wpd.getTitle()%></a></td>
          <td align="center"><%=um.getUserDb(wpd.getAuthor()).getRealName()%></td>
          <td align="center">
		  <div class="progressBar" style="padding:0px; margin:0px; height:20px">
              <div class="progressBarFore" style="width:<%=wpd.getProgress()%>%;">
              </div>
              <div class="progressText">
              <%=wpd.getProgress()%>%
              </div>
          </div>		  
          </td>
          <td align="center"><%=sbeginDate%></td>
          <td align="center"><%=sendDate%></td>
          <td align="center"><%=wptd.getWorkPlanTypeDb(wpd.getTypeId()).getName()%></td>
		  <%
			//String principal = wpd.getPrincipal();
			//System.out.println(principal);
			DeptUserDb dud = new DeptUserDb();
            DeptDb dd = new DeptDb();
			String[] principalAry = wpd.getPrincipals();
			int len = 0;
			if (principalAry!=null)
				len = principalAry.length;
			String principals = "";
			for (int y=0; y<len; y++) {
				if (principalAry[y].equals(""))
					continue;
				UserDb user = um.getUserDb(principalAry[y]);
				if (!user.isLoaded())
					continue;
				if (principals.equals("")){
					principals = user.getRealName();
				}else{
					principals += "，" + user.getRealName();
				}
			}
		  %>
          <td align="center"><a href="javascript:sel('<%=id%>', '<%=wpd.getAuthor()%>','<%=wpd.getTitle()%>','<%=dd.getName()%>','<%=principals%>','<%=DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd")%>','<%=days%>')">选择</a></td>
        </tr>
        <%
		}
%>
      </table>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right"><b><%=curpage %>/<%=totalpages %></b>&nbsp;
          <%
		  out.print(paginator.getCurPageBlock("workplan_list_sel.jsp?"+querystr));
		  %>
          </td></tr>
    </table></td>
  </tr>
</table>
<script>
$(function(){
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
    $('#endDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
})
</script>
</body>
</html>
