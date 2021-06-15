<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
if(userName.equals("")){
	userName = privilege.getUser(request);
}
if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String typeId = ParamUtil.get(request, "typeId");

String orderBy = ParamUtil.get(request, "orderBy");

try {	
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "beginDate", beginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "endDate", endDate, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "typeId", typeId, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "kind", kind, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "beginDate", beginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "endDate", endDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}


if (orderBy.equals(""))
	orderBy = "beginDate";
	
if (!cn.js.fan.db.SQLFilter.isValidSqlParam(orderBy)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ monitor/yoaacc_excel.jsp orderBy=" + orderBy);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
	return;
}
	
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
long projectId = ParamUtil.getLong(request, "projectId", -1);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>工作计划列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "project_workplan_list.jsp?op=<%=op%>&userName=<%=StrUtil.UrlEncode(userName)%>&action=<%=action%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>&typeId=<%=typeId%>&projectId=<%=projectId%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%@ include file="prj_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="100%" height="26" valign="top">
          <form name="formSearch" action="project_workplan_list.jsp" method="get">
      <table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
        <tr>
            <td height="30" align="center">
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
              类型
              <select name="typeId" id="typeId">
			  	<option value="">不限</option>
                <%=opts%>
              </select>
			  <script>
			  o('typeId').value = "<%=typeId%>";
			  </script>
           	  开始日期
             	<input id="beginDate" name="beginDate" value="<%=beginDate%>" size=15 />
             	结束日期 
             	<input id="endDate" name="endDate" value="<%=beginDate%>" size=15 />
             	<select name="kind">
                  <option value="title">标题</option>
                  <option value="content">内容</option>
                </select>
              <input name=what size=20 value="<%=what%>" />
              &nbsp;
              <input class="btn" name="submit" type=submit value="搜索" />
              <input name="op" type="hidden" value="<%=op%>" />
              <input name="projectId" type="hidden" value="<%=projectId%>" />
			  <input class="btn" value="添加计划" onclick="window.location.href='../workplan/workplan_add.jsp?projectId=<%=projectId%>'" type="button" />
		    </td>
        </tr>
      </table>
          </form>
          <%if (action.equals("search")) {%>
            <script>
			  o('kind').value = "<%=kind%>";
			  o('typeId').value = "<%=typeId%>";
			  </script>
          <%}%>
		<%
		String sql;
		String myname = privilege.getUser(request);
		String querystr = "";
		sql = "select distinct p.id from work_plan p, work_plan_user u where p.project_id=" + projectId + " and u.workPlanId=p.id and u.userName=" + StrUtil.sqlstr(userName);
		// 如果是项目总管理员
		if (privilege.isUserPrivValid(request, "project.admin")) {
			sql = "select distinct p.id from work_plan p, work_plan_user u where p.project_id=" + projectId + " and u.workPlanId=p.id";		
		}
		if (action.equals("search")) {
			if (kind.equals("title"))
				sql += " and p.title like " + StrUtil.sqlstr("%" + what + "%");
			else
				sql += " and p.content like " + StrUtil.sqlstr("%" + what + "%");
			if (!beginDate.equals(""))
				sql += " and p.beginDate>=" + StrUtil.sqlstr(beginDate);
			if (!endDate.equals(""))
				sql += " and p.endDate<=" + StrUtil.sqlstr(endDate);
			if (!typeId.equals("all") && !typeId.equals(""))
				sql += " and p.typeId=" + typeId;
		}
		sql += " order by " + orderBy + " " + sort;
		
		// out.print(sql);
		
		querystr += "projectId=" + projectId + "&op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&action=search&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate + "&typeId=" + typeId;
		querystr += "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		WorkPlanDb wpd = new WorkPlanDb();
		
		ListResult lr = wpd.listResult(sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();
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
      <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
          <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
        </tr>
      </table> 
      <table width="98%" align="center" class="tabStyle_1 percent98">
        <tr> 
          <td width="34%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('title')">标题
		<%if (orderBy.equals("title")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%></td>		  
          <td width="8%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('author')">拟定者
		<%if (orderBy.equals("author")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="16%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('progress')">进度
		<%if (orderBy.equals("progress")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="11%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('beginDate')">开始日期
		<%if (orderBy.equals("beginDate")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="11%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('endDate')">结束日期
		<%if (orderBy.equals("endDate")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="10%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('typeId')">类型
		<%if (orderBy.equals("typeId")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="10%" class="tabStyle_1_title">操作</td>
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
		%>
        <tr class="highlight"> 
          <td><a href="javascript:;" onclick="addTab('<%=wpd.getTitle()%>', '<%=request.getContextPath()%>/workplan/workplan_show.jsp?id=<%=id%>')"><%=wpd.getTitle()%></a></td>
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
          <td align="center">
          <a href="javascript:;" onclick="addTab('编辑：<%=wpd.getTitle()%>', '<%=request.getContextPath()%>/workplan/workplan_edit.jsp?id=<%=id%>&projectId=<%=projectId%>')">编辑</a>
          &nbsp;&nbsp;
          <a onclick="delWorkplan(<%=wpd.getId()%>)" href="javascript:;">删除</a></td>
        </tr>
      <%
		}
%>      </table>
      <br>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr> 
          <td height="23" align="right"> 
              <%
			out.print(paginator.getCurPageBlock("project_workplan_list.jsp?"+querystr));
			%>          </td>
        </tr>
      </table>    </td>
  </tr>
  <tr> 
    <td height="9">&nbsp;</td>
  </tr>
</table>
</body>
<script>
function delWorkplan(id) {	
	jConfirm('您确定要删除么？','提示',function(r){ 
		    if(!r){
		        return;
		    }else{
				$.ajax({
				        type: "post",
				        url: "../workplan/workplan_do.jsp",
				        data: {
				            op: "del",
				            id: id
				        },
				        beforeSend: function(XMLHttpRequest){
				            
				        },
				        success: function(data, status){
				            data = $.parseJSON(data);
				            if (data.ret=="0") {
				                jAlert(data.msg, "提示");
				            }
				            else {
				                //jAlert("删除成功！", "提示");
				                jAlert_Redirect("删除成功！","提示","project_workplan_list.jsp?<%=querystr%>");
				            }
				        },
				        complete: function(XMLHttpRequest, status){
				        	
				        },
				        error: function(XMLHttpRequest, textStatus){
				            jAlert(XMLHttpRequest.responseText, "提示");
				        }
				    }); 
		}}) ;	
}

$(function(){
	$('#beginDate').datetimepicker({
		lang:'ch',
		datepicker:true,
		timepicker:false,
		format:'Y-m-d'
	});
	$('#endDate').datetimepicker({
		lang:'ch',
		datepicker:true,
		timepicker:false,
		format:'Y-m-d'
	});
})
</script>
</html>
