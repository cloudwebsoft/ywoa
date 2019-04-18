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
String priv = "admin.workplan";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String unitCode = ParamUtil.get(request, "unitCode");
if (unitCode.equals("")) {
	unitCode = privilege.getUserUnitCode(request);
}
else {
	if (!privilege.canUserAdminUnit(request, unitCode)) {
		// 检查用户能否管理该单位
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
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
int progressFlag = ParamUtil.getInt(request, "progressFlag", -1);

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "beginDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
String querystr = "op=" + op + "&action=search&what=" + StrUtil.UrlEncode(what) + "&kind=" + kind + "&beginDate=" + beginDate + "&endDate=" + endDate + "&typeId=" + typeId + "&progressFlag=" + progressFlag;

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>


</head>
<body>
		<table width="100%" style="background-color:#efefef"><tr><td>
          <form name="formSearch" action="workplan_all_list.jsp" method="get">
			<input name="action" value="search" type="hidden" />
			  <%
			  if (op.equals("delBatch")) {
					String[] ary = StrUtil.split(ParamUtil.get(request, "ids"), ",");
					if (ary!=null) {
						WorkPlanDb wpd = new WorkPlanDb();
						for (int i=0; i<ary.length; i++) {
							wpd = wpd.getWorkPlanDb(StrUtil.toInt(ary[i]));
							wpd.del();
						}
						out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "workplan_all_list.jsp?" + querystr));
						return;
					}
				}
			
              WorkPlanTypeDb wptd = new WorkPlanTypeDb();
              String opts = "";
              Iterator ir = wptd.list().iterator();
              while (ir.hasNext()) {
                wptd = (WorkPlanTypeDb)ir.next();
                opts += "<option value='" + wptd.getId() + "' " + (typeId.equals("" + wptd.getId())?"selected":"") + ">" + wptd.getName() + "</option>";
              }
              %>
              &nbsp;&nbsp;类型
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
          </form>
        </td></tr></table>
        <table id="searchTable"><tr><td></td></tr></table>
		<%
		
		String myname = privilege.getUser(request);

		String sql = "select id from work_plan where unit_code=" + StrUtil.sqlstr(unitCode);

		if (action.equals("search")) {
			if (kind.equals("title"))
				sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
			else
				sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
			if (!beginDate.equals(""))
				sql += " and beginDate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
			if (!endDate.equals(""))
				sql += " and endDate<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
			if (!typeId.equals("all") && !typeId.equals(""))
				sql += " and typeId=" + typeId;
			if (progressFlag!=-1) {
				if (progressFlag==0)
					sql += " and progress<100";
				else
					sql += " and progress=100";
			}				
		}
		sql += " order by " + orderBy + " " + sort;
		
		// out.print(sql);
				
		String sortquerystr = querystr;
		querystr += "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = 20;
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
<table id="grid">    
		<thead>
        <tr>
        	<th width="30"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')" /></th>
          <th width="32" style="cursor:pointer" abbr="ID">ID</th>                  
          <th width="315" style="cursor:pointer" abbr="title">标题</th>		  
          <th width="78" style="cursor:pointer" abbr="author">拟定者</th>
          <th width="137" style="cursor:pointer" abbr="progress">进度</th>
          <th width="95" style="cursor:pointer" abbr="beginDate">开始日期</th>
          <th width="95" style="cursor:pointer" abbr="endDate">结束日期</th>
          <th width="106" style="cursor:pointer" abbr="typeId">类型</th>
          <th width="91">操作</th>
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
        <tr id="<%=id%>">
        	<td><input type="checkbox" name="ids" value="<%=id%>" /></td>
          <td><%=id%></td>
          <td><a href="javascript:;" onclick="addTab('<%=wpd.getTitle()%>', 'workplan/workplan_show.jsp?id=<%=id%>&isShowNav=1&userName=<%=StrUtil.UrlEncode(wpd.getAuthor())%>')"><%=wpd.getTitle()%></a></td>
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
          <td align="center"><a href="workplan_edit.jsp?id=<%=id%>&isShowNav=1&userName=<%=StrUtil.UrlEncode(wpd.getAuthor())%>">编辑</a>&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='workplan_do.jsp?op=del&id=<%=id%>&privurl=workplan_all_list.jsp'}})" on style="cursor:pointer">删除</a></td>
        </tr>
      <%
		}
%>   
	</tbody>   
</table>
</body>
<script type="text/javascript">
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
function selAllCheckBox(checkboxname) {
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

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}	
	
function initCalendar() {
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });

    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
}

function doOnToolbarInited() {
	initCalendar();
}

var flex;


function changeSort(sortname, sortorder) {
	window.location.href = "workplan_all_list.jsp?<%=sortquerystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}


function changePage(newp) {
	if (newp)
		window.location.href = "workplan_all_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "workplan_all_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = jQuery("#grid").flexigrid
(
	{
	buttons : [
		{name: '删除', bclass: 'delete', onpress : action},
		{separator: true},		
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
	checkbox: false,
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
	//onToolbarInited: doOnToolbarInited,
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {
	if (com=='删除') {

		var ids = getCheckboxValue("ids");
		if (ids=="") {
			jAlert('请选择一条记录!','提示');
			return;
		}

		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return;}
			else{
				window.location.href = "workplan_all_list.jsp?op=delBatch&ids=" + ids + "&<%=querystr%>&CPages=<%=curpage%>&pageSize=<%=pagesize%>";
			}
		});
	}
}
</script>
</html>