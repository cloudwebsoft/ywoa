<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="java.sql.SQLException"%>
<%@page import="org.json.JSONException"%>
<%@page import="com.redmoon.oa.monitor.RunStatusUtil"%>
<%@page import="org.json.JSONObject"%>
<%@page import="com.redmoon.oa.flow.FormMgr"%>
<jsp:useBean id="privilege" scope="page"
	class="com.redmoon.oa.pvg.Privilege" />
<%!
	private RunStatusUtil statusUtil = new RunStatusUtil();
	private void loadUsedStatus(Date startDate,Date endDate) throws ErrMsgException , JSONException , SQLException {
		//1.清空表单记录    2.根据用户单位列表，加载单位使用信息，并存入数据库。3.刷新缓存。
		String delSql = "delete from form_table_used_status";
		JdbcTemplate jtdel = new JdbcTemplate();
		jtdel.executeUpdate(delSql);
		
		String sql = "select id from form_table_yoa_acc ";
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		Vector v = fdao.list("yoa_acc",sql);
		Iterator ir = null;
		if (v!=null) {
			ir = v.iterator();
		}
		
		String sqlUsedStatus = "insert into form_table_used_status  "+
			"(flowId,flowTypeCode,cws_creator,cws_id,cws_order,unit_code,used_unit_id,used_unit,access_path,loginCount,netDiskFileCount, "+
			"netDiskSpace,netDiskUserSpace,documentCount,flowCount,messageCount,noticeCount,mobileAccessCouont,userCount,error,errorMessage,"+
			"diskSpace,diskUseredSpace,forumCount,shareFileCount,scheduleCount,projectCount,workPlanCount,workNoteCount,departmentCount,lastLoginDate,locationCount,cws_status,activeUserCount) "+
			"values(-1,'','admin','',?,'root',?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,?) ";
		int order = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			JSONObject json = statusUtil.getYOAStatus(fdao, startDate, endDate);
			long used_unit_id = fdao.getId();
			
			String used_unit = fdao.getFieldValue("used_unit");
			String access_path = fdao.getFieldValue("access_path");
			
			int loginCountI = -1;
			int netDiskFileCountI = -1;
			String netDiskSpaceS = "";
			String netDiskUserSpaceS = "";
			int documentCountI = -1;
			int flowCountI = -1;
			int messageCountI = -1;
			int noticeCountI = -1;
			int mobileAccessCountI = -1;
			int userCountI = -1;
			boolean errorB = false;
			String errorMessageS = "";
			String diskSpace = "";
			String diskUseredSpace = "";
			int forumCount = -1;
			int shareFileCount = -1;
			int scheduleCount = -1;
			int projectCount = -1;
			int workPlanCount = -1;
			int workNoteCount = -1;
			int departmentCount = -1;
			int activeUserCount = -1;
			String lastLoginDate = "";
			int locationCount = -1;
			try {
				loginCountI = json.getInt(RunStatusUtil.loginCount);
				
				netDiskFileCountI = json.getInt(RunStatusUtil.netDiskFileCount);
				netDiskSpaceS = json.getString(RunStatusUtil.netDiskSpace);
				netDiskUserSpaceS = json.getString(RunStatusUtil.netDiskUserSpace);
				documentCountI = json.getInt(RunStatusUtil.documentCount);
				flowCountI = json.getInt(RunStatusUtil.flowCount);
				messageCountI = json.getInt(RunStatusUtil.messageCount);
				noticeCountI = json.getInt(RunStatusUtil.noticeCount);
				mobileAccessCountI = json.getInt(RunStatusUtil.mobileAccessCount);
				userCountI = json.getInt(RunStatusUtil.userCount);
				errorB = json.getBoolean(RunStatusUtil.error);
				errorMessageS = json.getString(RunStatusUtil.errorMessage);
				diskSpace = json.getString(RunStatusUtil.DiskSpace);
				diskUseredSpace = json.getString(RunStatusUtil.DiskUseredSpace);
				forumCount = json.getInt(RunStatusUtil.forumCount);
				shareFileCount = json.getInt(RunStatusUtil.shareFileCount);
				scheduleCount = json.getInt(RunStatusUtil.scheduleCount);
				projectCount = json.getInt(RunStatusUtil.projectCount);
				workPlanCount = json.getInt(RunStatusUtil.workPlanCount);
				workNoteCount = json.getInt(RunStatusUtil.workNoteCount);
				departmentCount = json.getInt(RunStatusUtil.departmentCount);
				lastLoginDate = json.getString(RunStatusUtil.lastLoginDate);
				locationCount = json.getInt(RunStatusUtil.locationCount);
				activeUserCount = json.getInt(RunStatusUtil.activeUserCount);
			} catch(JSONException e) {
				errorB = true;
				errorMessageS = "无法解析JSON数据："+e.getMessage();
			} catch(Exception e) {
				errorB = true;
				errorMessageS = "程序异常："+e.getMessage();
			}
			JdbcTemplate jt = new JdbcTemplate();
			int count = jt.executeUpdate(sqlUsedStatus,new Object[]{order,used_unit_id,used_unit,access_path,loginCountI,netDiskFileCountI,netDiskSpaceS,
					netDiskUserSpaceS,documentCountI,flowCountI,messageCountI,noticeCountI,mobileAccessCountI,
					userCountI,errorB,errorMessageS,diskSpace,diskUseredSpace,forumCount,shareFileCount,
					scheduleCount,projectCount,workPlanCount,workNoteCount,departmentCount,lastLoginDate,locationCount,activeUserCount});
			System.out.println(getClass() + "============"+count);
			order++;
		} 
	}
%>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String preDate = ParamUtil.get(request,RunStatusUtil.preDateParm);
String beginDateS = ParamUtil.get(request,RunStatusUtil.beginDateParm);
String endDateS = ParamUtil.get(request,RunStatusUtil.endDateParm);

String formCode = ParamUtil.get(request, "formCode");

if(formCode != null && formCode.equals("used_status")) {
	Map<String,Date> returnValue = statusUtil.getQueryDateCondition(request);
	Date startDate = returnValue.get(RunStatusUtil.beginDateParm);
	Date endDate = returnValue.get(RunStatusUtil.endDateParm);
	//访问 使用状态 表单 处理
	//1.清空表单记录    2.根据用户单位列表，加载单位使用信息，并存入数据库。3.刷新缓存。
	loadUsedStatus(startDate,endDate);
}


FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "";

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
String sql = ary[0];
String sqlUrlStr = ary[1];

querystr = "op=" + op + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort;
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;
 //out.print(sql);

String action = ParamUtil.get(request, "action");
if (action.equals("del")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
		
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (fdm.del(request)) {
			out.print(StrUtil.Alert_Redirect("删除成功！", "yoa_used_status_list.jsp?" + querystr + "&CPages=" + curpage));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
		<title><%=fd.getName()%>列表</title>
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
		<style type="text/css">
@import url("../util/jscalendar/calendar-win2k-2.css");
</style>

		<script src="../inc/common.js"></script>
		<script type="text/javascript" src="../js/jquery.js"></script>
		<script type="text/javascript" src="../js/flexigrid.js"></script>
		<script type="text/javascript" src="../js/jquery-ui/jquery-ui.js"></script>
		<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
		<script type="text/javascript"
			src="../util/jscalendar/lang/calendar-zh.js"></script>
		<script type="text/javascript"
			src="../util/jscalendar/calendar-setup.js"></script>
	</head>
	<body>
		<div class="tabs1Box">
			<div id="tabs1">
				<ul>
					<li id="menu1">
						<a href="yoa_used_status_list.jsp?formCode=used_status"><span>云OA运行状态</span>
						</a>
					</li>
				</ul>
			</div>
		</div>
		<script>
$("menu1").className="current"; 
</script>
		<%
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
int total = lr.getTotal();
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
		
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
String[] fieldsOrder = StrUtil.split(listFieldOrder, ",");

MacroCtlMgr mm = new MacroCtlMgr();
%>
		<table id="searchTable" width="98%" border="0" cellspacing="1"
			cellpadding="3" align="center">
			<tr>
				<td height="23" align="left">
					<form id='formSearch'
						action="yoa_used_status_list.jsp?formCode=<%=formCode%>" method="post">
						<select id="preDate" name="preDate"
							onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
							<option selected="selected" value="">
								不限
							</option>
							<%
        java.util.Date[] aryDate = DateUtil.getDateSectOfToday();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								今天
							</option>
							<%
        aryDate = DateUtil.getDateSectOfYestoday();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								昨天
							</option>
							<%
        aryDate = DateUtil.getDateSectOfCurWeek();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								本周
							</option>
							<%
        aryDate = DateUtil.getDateSectOfLastWeek();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								上周
							</option>
							<%
        aryDate = DateUtil.getDateSectOfCurMonth();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								本月
							</option>
							<%
        aryDate = DateUtil.getDateSectOfLastMonth();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								上月
							</option>
							<%
        aryDate = DateUtil.getDateSectOfQuarter();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								本季度
							</option>
							<%
        aryDate = DateUtil.getDateSectOfCurYear();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								今年
							</option>
							<%
        aryDate = DateUtil.getDateSectOfLastYear();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								去年
							</option>
							<%
        aryDate = DateUtil.getDateSectOfLastLastYear();
        %>
							<option
								value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">
								前年
							</option>
							<option value="*">
								自定义
							</option>
						</select>
						<script>
				        o("preDate").value = "<%=preDate%>";
				        </script>
						<span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>"> 从 <input
								id="beginDate" name="beginDate" size="10" value="<%=beginDateS%>" /> <script
								type="text/javascript">
								/**
            Calendar.setup({
                inputField     :    "beginDate",      // id of the input field
                ifFormat       :    "%Y-%m-%d",       // format of the input field
                showsTime      :    false,            // will display a time selector
                singleClick    :    false,           // double-click mode
                align          :    "Tl",           // alignment (defaults to "Bl")		
                step           :    1                // show all years in drop-down boxes (instead of every other year as default)
            });
            **/
        </script> 至 <input id="endDate" name="endDate" size="10" value="<%=endDateS%>" /> 
        <script type="text/javascript">
        /**
            Calendar.setup({
                inputField     :    "endDate",      // id of the input field
                ifFormat       :    "%Y-%m-%d",       // format of the input field
                showsTime      :    false,            // will display a time selector
                singleClick    :    false,           // double-click mode
                align          :    "Tl",           // alignment (defaults to "Bl")		
                step           :    1                // show all years in drop-down boxes (instead of every other year as default)
            });
            **/
        </script> </span>
						<input name="submit" type=submit value="搜索" class="tSearch" />
                        红色：磁盘空间不足，橙红色：磁盘剩余空间小于100M
					</form>
				</td>
			</tr>
		</table>
		<table id="grid" border="0" cellpadding="2" cellspacing="0">
			<thead>
				<tr>
					<%
int len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	String title = "创建者";
	if (!fieldName.equals("cws_creator"))
		title = fd.getFieldTitle(fieldName);
	String w = fieldsWidth[i];
	int wid = StrUtil.toInt(w, 50);
	if (w.indexOf("%")==w.length()-1) {
		w = w.substring(0, w.length()-1);
		wid = 800*StrUtil.toInt(w, 20)/100;
	}
%>
					<th width="<%=wid%>" style="cursor: hand" abbr="<%=fieldName%>">
						<%=title%>
					</th>
					<%}%>

				</tr>
			</thead>
			<tbody>
				<%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
			String unitId = fdao.getFieldValue("used_unit_id");
			int unitIdI = StrUtil.toInt(unitId,-1);
			
			// System.out.println(isEprUnit+" = "+DateUtil.format(unitEndDateD,"yyyy-MM-dd HH:mm:ss")+" = "+unitEndDateS);
		%>
				<tr align="center" id="<%=id%>">
					<%
					double diskSpace = StrUtil.toDouble(fdao.getFieldValue("diskSpace").substring(0, fdao.getFieldValue("diskSpace").length()-2)); // 单位为G
					double diskUseredSpace = StrUtil.toDouble(fdao.getFieldValue("diskUseredSpace").substring(0, fdao.getFieldValue("diskUseredSpace").length()-2)); // 单位为G
					String aStyle = "";
					if (diskSpace < diskUseredSpace) {
						aStyle = " style='color:red' ";
					}
					else if (diskSpace - diskUseredSpace < 0.1) {
						aStyle = " style='color:#FF9900' ";
					}
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>
					<td align="left">
						<a<%=aStyle%> href="yoa_used_status_show.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>">
							<%
		if (!fieldName.equals("cws_creator")) {
			FormField ff = fd.getFormField(fieldName);
			if (ff==null) {
				out.print(fieldName + " 已不存在！");
			}
			else {
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				} else {%> 
				<%=fdao.getFieldValue(fieldName)%> <%}
			}%> <%}else{
			String realName = "";
			if (fdao.getCreator()!=null) {
				UserDb user = um.getUserDb(fdao.getCreator());
			if (user!=null)
				realName = user.getRealName();
			}
		%> <%=realName%> <%}%> </a>
					</td>
					<%}%>

				</tr>
				<%
  }
%>
			</tbody>
		</table>

	</body>
	<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "<%=request.getContextPath()%>/monitor/yoa_used_status_list.jsp?op=<%=op%>&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/monitor/yoa_used_status_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "<%=request.getContextPath()%>/monitor/yoa_used_status_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '导出', bclass: 'edit', onpress : action},
		{separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		],
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
	url: false,
	usepager: true,
	checkbox : true,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
	onChangeSort: changeSort,
	
	onChangePage: changePage,
	onRpChange: rpChange,
	onReload: onReload,
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {
	if (com=="导出") {
		window.open('yoaacc_excel.jsp?<%=querystr%>');
	}
	
}

</script>
</html>
