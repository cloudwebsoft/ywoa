<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv))
{
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
String showType = ParamUtil.get(request, "showType");

String formCode = "project";
FormDb fd = new FormDb(formCode);

Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String action = ParamUtil.get(request, "action");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String prj_type = ParamUtil.get(request, "prj_type");
int status = ParamUtil.getInt(request, "status", 0);
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "begin_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
String querystr = "";
querystr += "formCode=project&showType=" + showType + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate + "&prj_type=" + prj_type + "&status=" + status;
querystr += "&orderBy=" + orderBy + "&sort=" + sort;

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "project_list.jsp?op=<%=op%>&userName=<%=StrUtil.UrlEncode(userName)%>&action=<%=action%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>&prj_type=<%=prj_type%>&showType=<%=showType%>&status=<%=status%>orderBy=" + orderBy + "&sort=" + sort;
}
</script>
<script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<!-- 
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>

<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
 -->

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

</head>
<body>
<%
if (op.equals("del")) {
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (fdm.del(request)) {
			out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "project_list.jsp?CPages=" + curpage + "&" + querystr));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}	
%>
<%@ include file="project_inc_menu_top.jsp"%>
<%
	String currentMenu = "menu1";
	if(showType.equals("mine")) {
		currentMenu = "menu2";
	}
	else if (showType.equals("favorite")) {
		currentMenu = "menu4";
	}
%>
<script>
o("<%=currentMenu%>").className="current";
</script>
<div class="spacerH"></div>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="100%" height="26" valign="top">
          <form name="formSearch" action="project_list.jsp" method="get">
      <table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
        <tr>
            <td height="30" align="center">
			<input name="action" value="search" type="hidden" />
              <%
	  Iterator irType = SelectMgr.getOptions("project_type").iterator();
	  String opts = "";
	  while (irType.hasNext()) {
      	SelectOptionDb sod = (SelectOptionDb) irType.next();
		opts += "<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>";
	  }
	  %>
              类型
              <select name="prj_type" id="prj_type">
			  	<option value="">不限</option>
                <%=opts%>
              </select>
              状态
              <select id="status" name="status">
              <option value="-1">不限</option>
              <%
			  Iterator ir2 = SelectMgr.getOptions("project_status").iterator();
			  opts = "";
			  while (ir2.hasNext()) {
				SelectOptionDb sod = (SelectOptionDb) ir2.next();
				opts += "<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>";
			  }
			  out.print(opts);
			  %>
              </select>
			  <script>
			  formSearch.prj_type.value = "<%=prj_type%>";
			  formSearch.status.value = "<%=status%>";
			  </script>
           	  开始日期
             	<input id="beginDate" name="beginDate" value="<%=beginDate%>" size=15>
             	结束日期 
             	<input id="endDate" name="endDate" value="<%=endDate%>" size=15>
             	<select id="kind" name="kind">
                  <option value="name">标题</option>
                  <option value="content">内容</option>
                </select>
<input id="what" name="what" size=20 value="<%=what%>">
              &nbsp;
              <input class="btn" name="submit" type=submit value="搜索">
              <input name="op" type="hidden" value="<%=op%>">
              <input name="showType" type="hidden" value="<%=showType%>">
		    </td>
        </tr>

      </table>
          </form>
          <%if (action.equals("search")) {%>
            <script>
			  formSearch.kind.value = "<%=kind%>";
			  formSearch.prj_type.value = "<%=prj_type%>";
			  </script>
          <%}%>
		<%
		String sql;
		String myname = privilege.getUser(request);
		
		sql = "select distinct p.id, " + orderBy + " from form_table_project p, form_table_project_members m where p.id=m.cws_id and m.prj_user=" + StrUtil.sqlstr(userName);
		if (showType.equals("mine"))
			sql = "select id from form_table_project p where p.cws_creator=" + StrUtil.sqlstr(userName);
		else if (showType.equals("favorite")) {
			sql = "select p.id from form_table_project p, project_favorite f where p.id=f.project_id and f.user_name=" + StrUtil.sqlstr(userName);
		}
		
		if (action.equals("search")) {
			if (!showType.equals("mine")) {
				if (status!=-1) {
					sql += " and p.status=" + status;
				}

				if (kind.equals("name"))
					sql += " and p.name like " + StrUtil.sqlstr("%" + what + "%");
				else
					sql += " and p.content like " + StrUtil.sqlstr("%" + what + "%");
				if (!beginDate.equals(""))
					sql += " and p.begin_Date>=" + StrUtil.sqlstr(beginDate);
				if (!endDate.equals(""))
					sql += " and p.end_Date<=" + StrUtil.sqlstr(endDate);
				if (!prj_type.equals("all") && !prj_type.equals(""))
					sql += " and p.prj_type=" + StrUtil.sqlstr(prj_type);
			}
			else {
				if (status!=-1) {
					sql += " and status=" + status;
				}
				
				if (kind.equals("name"))
					sql += " and name like " + StrUtil.sqlstr("%" + what + "%");
				else
					sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
				if (!beginDate.equals(""))
					sql += " and begin_Date>=" + StrUtil.sqlstr(beginDate);
				if (!endDate.equals(""))
					sql += " and end_Date<=" + StrUtil.sqlstr(endDate);
				if (!prj_type.equals("all") && !prj_type.equals(""))
					sql += " and prj_type=" + StrUtil.sqlstr(prj_type);
			}
		}
		sql += " order by " + orderBy + " " + sort;
		
		// out.print(sql);
		
		
		int pagesize = 10;

		UserMgr um = new UserMgr();
		
        FormDAO fdao = new FormDAO(fd);
		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql, curpage, pagesize);
		
		long total = jt.getTotal();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}

%>
      <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
          <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
        </tr>
      </table> 
      <table id="mainTable" width="98%" align="center" cellspacing="0" class="tabStyle_1 percent98">
        <tr> 
          <td width="31%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('name')">标题
		<%if (orderBy.equals("name")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%></td>
          <td width="7%" class="tabStyle_1_title" style="cursor:pointer" onclick="doSort('prj_type')">类型
            <%if (orderBy.equals("prj_type")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%></td>		  
          <td width="9%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('p.cws_creator')">拟定者
		<%if (orderBy.equals("p.cws_creator")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="13%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('progress')">进度
		<%if (orderBy.equals("progress")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="9%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('begin_date')">开始日期
		<%if (orderBy.equals("begin_date")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="9%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('end_date')">结束日期
          <%if (orderBy.equals("end_date")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>		  </td>
          <td width="7%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('end_date')">状态</td>
          <td width="15%" class="tabStyle_1_title">操作</td>
        </tr>
      <%	
	  	int i = 0;
	  	SelectOptionDb sod = new SelectOptionDb();
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			i++;
			long id = rr.getLong("id");
			fdao = fdao.getFormDAO(id, fd);
			
			String sbeginDate = fdao.getFieldValue("begin_date");
			String sendDate = fdao.getFieldValue("end_date");
		%>
        <tr class="highlight"> 
          <td><a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("name")%>', '<%=request.getContextPath()%>/project/project_show.jsp?parentId=<%=id%>&projectId=<%=id%>&formCode=project')"><%=fdao.getFieldValue("name")%></a></td>
          <td align="center"><%=fdao.getFieldValue("prj_type")%></td>
          <td align="center"><%=um.getUserDb(fdao.getCreator()).getRealName()%></td>
          <td align="center">
		  <div class="progressBar" style="">
              <div class="progressBarFore" style="width:<%=fdao.getFieldValue("progress")%>%;">
              </div>
              <div class="progressText">
              <%=fdao.getFieldValue("progress")%>%
              </div>
          </div>          
          </td>
          <td align="center"><%=sbeginDate%></td>
          <td align="center"><%=sendDate%></td>
          <td align="center">
		  <%
		  String statusText = sod.getOptionName("project_status", fdao.getFieldValue("status"));
		  out.print(statusText);
		  %>
		  </td>
          <td align="center">
			<a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("name")%>', '<%=request.getContextPath()%>/project/project_show.jsp?parentId=<%=id%>&projectId=<%=id%>&formCode=project')">查看</a>&nbsp;&nbsp;		  
		    <%
			String role = StrUtil.getNullStr(fdao.getFieldValue("prj_role"));
			if (role.equals("manager") || privilege.isUserPrivValid(request, "admin") || privilege.getUser(request).equals(fdao.getCreator())) {		  
		  %>
		  <a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("name")%>', '<%=request.getContextPath()%>/visual/project_edit.jsp?parentId=<%=id%>&id=<%=id%>&formCode=project')">编辑</a>&nbsp;&nbsp;
          <a onclick="return confirm('您确定要删除么？')" href="project_list.jsp?op=del&id=<%=id%>&CPages=<%=curpage%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=querystr%>">删除</a>
		  <%}%>
          <%if (!showType.equals("favorite")) {%>
          &nbsp;&nbsp;<a href="javascript:;" onclick="favorite(<%=id%>)">关注</a>
          <%}else{%>
          &nbsp;&nbsp;<a href="javascript:;" onclick="unfavorite(<%=id%>)">取消关注</a>
          <%}%>          
		  </td>
        </tr>
      <%
		}
%>      </table>

      <br>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr> 
          <td height="23" align="right"> 
              <%
			out.print(paginator.getCurPageBlock("project_list.jsp?"+querystr));
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
function favorite(id) {
	$.ajax({
		type: "post",
		url: "project_do.jsp",
		data: {
			op: "favorite",
			projectId: id
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('#mainTable').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="0") {
				jAlert(data.msg, "提示");
			}
			else {
				jAlert(data.msg, "提示");
			}
		},
		complete: function(XMLHttpRequest, status){
			$('#mainTable').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function unfavorite(id) {
	jConfirm('您确定要删除么？', '提示', function(r) {
		if (!r){return;}
		else{
			$.ajax({
				type: "post",
				url: "project_do.jsp",
				data: {
					op: "unfavorite",
					projectId: id
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					$('#mainTable').showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data);
					if (data.ret=="0") {
						jAlert(data.msg, "提示");
					}
					else {
						jAlert(data.msg, "提示");
						window.location.reload();
					}
				},
				complete: function(XMLHttpRequest, status){
					$('#mainTable').hideLoading();				
				},
				error: function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});

}
</script>
</html>
