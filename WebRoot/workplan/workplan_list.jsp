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
String action = ParamUtil.get(request, "action");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

if(userName.equals("")){
	userName = privilege.getUser(request);
}

String op = ParamUtil.get(request, "op");
String kind = ParamUtil.get(request, "kind");
if (kind.equals(""))
	kind = "title";
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String typeId = ParamUtil.get(request, "typeId");
int progressFlag = ParamUtil.getInt(request, "progressFlag", -1);

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "beginDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>工作计划列表</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<%@ include file="workplan_inc_menu_top.jsp"%>
<%
if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
}
	String currentMenu = "menu1";
	if (op.equals("mine")) {
		currentMenu = "menu2";
	}
	else if (op.equals("favorite")) {
		currentMenu = "menu6";
	}
%>
<script>
o("<%=currentMenu%>").className="current";
</script>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0"><tr><td>
          <form id="formSearch" name="formSearch" action="workplan_list.jsp" method="get">
			<input name="action" value="search" type="hidden" />
			  <%
              WorkPlanTypeDb wptd = new WorkPlanTypeDb();
              String opts = "";
              Iterator ir = wptd.list().iterator();
              while (ir.hasNext()) {
                wptd = (WorkPlanTypeDb)ir.next();
                opts += "<option value='" + wptd.getId() + "' " + (typeId.equals("" + wptd.getId())?"selected":"") + ">" + wptd.getName() + "</option>";
              }
              %>
            &nbsp;类型
            <select name="typeId" id="typeId">
            <option value="" <%=typeId.equals("")?"selected":""%>>不限</option>
            <%=opts%>
            </select>
            &nbsp;进度
            <select id="progressFlag" name="progressFlag">
            <option value="-1" <%=progressFlag==-1?"selected":""%>>不限</option>
            <option value="0" <%=progressFlag==0?"selected":""%>>未完成</option>
            <option value="1" <%=progressFlag==1?"selected":""%>>已完成</option>
            </select>
            开始日期
            <input id="beginDate" name="beginDate" value="<%=beginDate%>" size=10>&nbsp;
            结束日期 
            <input id="endDate" name="endDate" value="<%=endDate%>" size=10>&nbsp;
            <select name="kind">
              <option value="title" <%=kind.equals("title")?"selected":""%>>标题</option>
              <option value="content" <%=kind.equals("content")?"selected":""%>>内容</option>
            </select>
          <input name=what size=20 value="<%=what%>">
          &nbsp;
          <input class="tSearch" name="submit" type=submit value="搜索">
          <input name="op" type="hidden" value="<%=op%>">
          <input name="userName" type="hidden" value="<%=userName%>">          
      </form>
        </td></tr></table>
<%
		String querystr = "";
		String title = ParamUtil.get(request, "title");
		String content = ParamUtil.get(request, "content");
		WorkPlanDb wpd = new WorkPlanDb();
		String sql = wpd.getQuery(action, op, kind, userName, title, content, what, progressFlag, typeId, beginDate, endDate, orderBy, sort);
		// out.print(sql);
		
		String urlStr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate + "&typeId=" + typeId + "&progressFlag=" + progressFlag;
		
		querystr = urlStr + "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = ParamUtil.getInt(request, "pageSize", 20);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();

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
<table id="grid">
  <thead>
        <tr>
          <th width="20" style="cursor:pointer" abbr="title">&nbsp;</th>
          <th width="30" style="cursor:pointer" abbr="ID">ID</th>          
          <th width="260" style="cursor:pointer" abbr="title">标题</th>		  
          <th width="50" style="cursor:pointer" abbr="author">创建者</th>
          <th width="50" style="cursor:pointer" abbr="author">状态</th>
          <th width="120" style="cursor:pointer" abbr="progress">进度</th>
          <th width="80" style="cursor:pointer" abbr="beginDate">开始日期</th>
          <th width="80" style="cursor:pointer" abbr="endDate">结束日期</th>
          <th width="45" style="cursor:pointer" abbr="endDate">总天数</th>
          <th width="70" style="cursor:pointer" abbr="endDate">天数</th>
          <th width="70" style="cursor:pointer" abbr="typeId">类型</th>
          <th width="60" style="cursor:pointer">流程关联</th>
          <th width="160">操作</th>
        </tr>
   </thead>
   <tbody>
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
        <tr>
          <td align="center">
			<%   
            int nowDays = DateUtil.datediff(wpd.getEndDate(), new Date());
            if(nowDays<0){
                // nowDays = 0;
            }
            int sumDays = DateUtil.datediff(wpd.getEndDate(), wpd.getBeginDate());
            float progress =(float)nowDays/sumDays;
            %>
            <%
            float r23 = (float)2/3;
            if(progress>r23) {
            %>
                <img src="../images/green.jpg" width="16" height="18" border="0" title="时间大于2/3" />
            <%}else if(progress<r23 && progress>((float)1/3)){%>
                <img src="../images/yel.jpg" width="16" height="18" border="0" title="时间介于1/3与2/3之间" />
            <%}else if(progress<((float)1/3) && progress>=0){%>
                <img src="../images/red.jpg" width="16" height="18" border="0" title="时间小于1/3" />
            <%}else {%>
                <img src="../images/red_hot.jpg" width="16" height="18" border="0" title="时间超期" />
            <%}%>
          </td>
          <td><%=wpd.getId()%></td>
          <td><a href="javascript:;" onclick="addTab('<%=wpd.getTitle()%>', '<%=request.getContextPath()%>/workplan/workplan_show.jsp?id=<%=id%>')"><%=wpd.getTitle()%></a></td>
          <td align="center"><%=um.getUserDb(wpd.getAuthor()).getRealName()%></td>
          <td align="center">
		  <%if (wpd.getCheckStatus()==WorkPlanDb.CHECK_STATUS_NOT) {%>
          未审
          <%}else{%>
          已审
          <%}%>          
          </td>
          <td align="center">
		  <div class="progressBar" style="padding:0px; margin:0px; height:20px">
              <div class="progressBarFore" style="width:<%=wpd.getProgress()%>%;padding:0px;">
              </div>
              <div class="progressText">
              <%=wpd.getProgress()%>%
              </div>
          </div>          
          </td>
          <td align="center"><%=sbeginDate%></td>
          <td align="center"><%=sendDate%></td>
          <td align="center">
          <%=sumDays%>
          </td>
          <td align="center">
            <%
			if (wpd.getProgress()<100) {
				if (nowDays<0) {%>
					<font color="red">过期<%=-nowDays%>天</font>
				<%} else {%>
					剩余<%=nowDays%>天
				<%}
			}%>
          </td>
          <td align="center"><%=wptd.getWorkPlanTypeDb(wpd.getTypeId()).getName()%></td>
          <td align="center">
		  <%
		    if(wpd.getFlowId()!=0){
		  %>
		  	<a target="_blank" href="../flow_modify.jsp?flowId=<%=wpd.getFlowId()%>"><%=wpd.getFlowId()%></a>
		  <%}else{%>
		  	无
		    <%}%>
		  </td>
          <td align="center">
          <%
          boolean canEdit = true;
          boolean canDel = true;
          try {
				WorkPlanMgr wpm = new WorkPlanMgr();
				WorkPlanDb wp = wpm.getWorkPlanDb(request, id, "edit");
			}
			catch (ErrMsgException e) {
				canEdit = false;
			}
			try {
				WorkPlanMgr wpm = new WorkPlanMgr();
				WorkPlanDb wp = wpm.getWorkPlanDb(request, id, "del");
			}
			catch (ErrMsgException e) {
				canDel = false;
			}
			if (canEdit) {
          %>
          <a href="javascript:;" onclick="addTab('<%=wpd.getTitle()%>', '<%=request.getContextPath()%>/workplan/workplan_edit.jsp?id=<%=id%>')">编辑</a>
          &nbsp;&nbsp;
          <%}
			if (canDel) {
			%>
          <a onclick="del(<%=id%>)" style="cursor:pointer">删除</a>
          &nbsp;&nbsp;
          <%} %>
          <%if (!op.equals("favorite")) {%>
          <a href="javascript:;" onclick="favorite(<%=id%>)">关注</a>
          <%}else{%>
          <a href="javascript:;" onclick="unfavorite(<%=id%>)">取消关注</a>
          <%}%>
          </td>
        </tr>
      <%
		}
%>
	</tbody>
</table>
</body>
<script type="text/javascript">
function initCalendar() {
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d',
    });
    $('#endDate').datetimepicker({
        lang:'ch',
        timepicker:false,
        format:'Y-m-d',
        formatDate:'Y/m/d',
    });
}

function doOnToolbarInited() {
	initCalendar();
}

function del(id){
	jConfirm('您确定要删除么？','提示',function(r){ 
	    if(!r){
	        return;
	    }else{
			$.ajax({
			        type: "post",
			        url: "workplan_do.jsp",
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
			                jAlert_Redirect("删除成功！","提示","workplan_list.jsp");
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
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "workplan_list.jsp?<%=urlStr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "workplan_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "workplan_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		buttons : [
			{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
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
		onToolbarInited: doOnToolbarInited,
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
	
	o("typeId").value = "<%=typeId%>";
	o("progressFlag").value = "<%=progressFlag%>";
	o("kind").value = "<%=kind%>";
});


function action(com, grid) {
}

function favorite(id) {
	$.ajax({
		type: "post",
		url: "workplan_do.jsp",
		data: {
			op: "favorite",
			workplanId: id
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('#grid').showLoading();
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
			$('#grid').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function unfavorite(id) {
	jConfirm('您确定要取消关注么？', '提示', function(r) {
		if (r) {
			$.ajax({
				type: "post",
				url: "workplan_do.jsp",
				data: {
					op: "unfavorite",
					workplanId: id
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					$('#grid').showLoading();
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
					$('#grid').hideLoading();				
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