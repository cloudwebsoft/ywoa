<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String code = ParamUtil.get(request, "code");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
String formCode = msd.getString("form_code");
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(code);
if (!mpd.canUserManage(privilege.getUser(request)) && !mpd.canUserLog(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String action = ParamUtil.get(request, "action");
if ("restore".equals(action)) {
	long id = ParamUtil.getLong(request, "id", -1);
	FormDAOLog fdaoLog = new FormDAOLog(fd);
	fdaoLog = fdaoLog.getFormDAOLog(id);
	
	Vector fields = fd.getFields();
	Iterator ir = fields.iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		ff.setValue(fdaoLog.getFieldValue(ff.getName()));
	}
	
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
	fdao.setFields(fields);
	fdao.create();
	
	JSONObject json = new JSONObject();		
	boolean re = false;
	re = fdaoLog.del();
	if (re) {
    	json.put("ret", "1");
		json.put("msg", "操作成功！");		
	}
	else {
    	json.put("ret", "0");
		json.put("msg", "操作失败！");
	}
	out.print(json);
	return;
}

String op = ParamUtil.get(request, "op");

long fdaoId = ParamUtil.getLong(request, "fdaoId", -1);
if (fdaoId==-1) {
	// out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
	// return;
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

String userName = ParamUtil.get(request, "userName");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
int logType = ParamUtil.getInt(request, "logType", -1);
java.util.Date beginDate=null, endDate=null;

if (!strBeginDate.equals("")) {
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
}

if (!strEndDate.equals("")) {
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	strEndDate = DateUtil.format(DateUtil.addDate(endDate, 1), "yyyy-MM-dd");
}

String 	sql1 = "select id from " + FormDb.getTableNameForLog(formCode) + " where 1=1";
String sql = "";
if (op.equals("search")) {

	if (fdaoId!=-1) {
		sql += " and cws_log_id=" + fdaoId;
	}
	if (logType!=-1) {
		sql += " and cws_log_type=" + logType;
	}
	if (!userName.equals("")) {
		sql += " and (cws_log_user like " + StrUtil.sqlstr("%" + userName + "%") + " or cws_log_user in (select name from users where realname like " + StrUtil.sqlstr("%" + userName + "%") + "))";
	}
	if (beginDate!=null) {
		sql += " and cws_log_date >=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and cws_log_date <" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}

//sql += " order by " + orderBy + " " + sort;
// out.print(sql);

querystr = "op=" + op + "&code=" + code + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort + "&fdaoId=" + fdaoId + "&userName=" + StrUtil.UrlEncode(userName) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&logType=" + logType;

if (action.equals("del")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
		
	com.redmoon.oa.visual.FormDAOLog fdaoLogDel = new com.redmoon.oa.visual.FormDAOLog(fd);
	long id = ParamUtil.getLong(request, "id");
	fdaoLogDel = fdaoLogDel.getFormDAOLog(id);
	if (fdaoLogDel.del())
		out.print(StrUtil.Alert_Redirect("删除成功！", "module_log_list.jsp?" + querystr + "&CPages=" + curpage));
	else
		out.print(StrUtil.Alert_Back("操作失败!"));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery.raty.min.js"></script>
<script>
$(function() {
	initGrid();
});
</script>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp"%>
<script>
o("menu4").className="current"; 
</script>
<%!
	public String getHtmlValue(FormField formField,String factValue,HttpServletRequest request){
		MacroCtlMgr mm = new MacroCtlMgr();
		String val = "";
		if (formField.getType().equals(FormField.TYPE_MACRO)){
			MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
			if (mu != null) {
				val = mu.getIFormMacroCtl().converToHtml(request, formField, factValue);
			}
		}else {
		    val = factValue;
		}
	    return val;
	}
%>
<%
//查出所有的记录，从开始遍历
//String sqlLog = "select * from " + FormDb.getTableNameForLog(formCode);

Vector<FormField> vector = fd.getFields();
JdbcTemplate jt = new JdbcTemplate();
com.redmoon.oa.visual.FormDAOLog fdaoLog = new com.redmoon.oa.visual.FormDAOLog(fd);

ListResult lr = fdaoLog.listResult(sql1 + sql + " order by id desc", curpage, pagesize);
//ResultIterator ri  = jt.executeQuery(sql + " order by id desc");
int total = lr.getTotal();
Iterator<FormDAOLog> ri = lr.getResult().iterator();
List<List> formFieldsList = new ArrayList<List>();
while(ri.hasNext()){
    FormDAOLog rr = ri.next();

	FormDAO fdao = new FormDAO(fd);
	Iterator ir = fd.getFields().iterator();
	while (ir.hasNext()) {
	    FormField ff = (FormField)ir.next();
		fdao.setFieldValue(ff.getName(), rr.getFieldValue(ff.getName()));
	}
	RequestUtil.setFormDAO(request, fdao);

    Long id = rr.getId();
    String cws_log_id = String.valueOf(rr.getLogId());
    String cws_log_type = String.valueOf(rr.getLogType());
    String cws_log_date = DateUtil.format(rr.getLogDate(),"yyyy-MM-dd HH:mm:ss");
    String cws_log_user = rr.getLogUser();
    //String cws_log_id = rr.getFieldValue("cws_log_id");
    //String cws_log_type = rr.getFieldValue("cws_log_type");
    //String cws_log_date = rr.getFieldValue("cws_log_date");
	//String cws_log_user = rr.getFieldValue("cws_log_user");
    //判断是否被删除，如果是被删除，则直接全部加入到列表中
	if (Integer.parseInt(cws_log_type) == FormDAOLog.LOG_TYPE_DEL){
		for (FormField formField : vector) {
			String formFieldValue = StrUtil.getNullStr(rr.getFieldValue(formField.getName()));
			List<String> formFieldList = new ArrayList<String>();
			formFieldList.add(id.toString());//logId;
			formFieldList.add(cws_log_date);//修改日期
			formFieldList.add(cws_log_user);//修改人
			formFieldList.add(cws_log_id);//修改id
			formFieldList.add(cws_log_type);//修改类型
			formFieldList.add(formField.getTitle());//修改字段的名称
			formFieldList.add(getHtmlValue(formField,formFieldValue,request));//修改前的值
			formFieldList.add("");//修改后的值
			formFieldsList.add(formFieldList);
		}
	    continue;
	}

	//取出上一条记录
    String sql2 = "select * from " + FormDb.getTableNameForLog(formCode) + " where 1=1";
    String sqlPrev = sql2 + sql + " and cws_log_id = '" + cws_log_id + "' and id < " + id;
	ResultIterator riPrev = jt.executeQuery(sqlPrev,1,1);
	if (riPrev.hasNext()){
		ResultRecord rrPrev = (ResultRecord)riPrev.next();
		//比较字段是否被修改
		for (FormField formField : vector){

            int fieldType = formField.getFieldType();
            String formFieldValuePrev = "";
            if (fieldType == FormField.FIELD_TYPE_FLOAT){
                //float
                 formFieldValuePrev = String.valueOf(rrPrev.getFloat(formField.getName()));
            }else if(fieldType == FormField.FIELD_TYPE_DOUBLE){
                formFieldValuePrev = String.valueOf(rrPrev.getDouble(formField.getName()));
            }else {
                formFieldValuePrev = rrPrev.getString(formField.getName());
            }
			String formFieldValue = rr.getFieldValue(formField.getName());
			if (!StrUtil.getNullStr(formFieldValue).equals(StrUtil.getNullStr(formFieldValuePrev))){
		        List<String> formFieldList = new ArrayList<String>();
		        formFieldList.add(id.toString());//logId;
		        formFieldList.add(cws_log_date);//修改日期
				formFieldList.add(cws_log_user);//修改人
				formFieldList.add(cws_log_id);//修改id
				formFieldList.add(cws_log_type);//修改类型
				formFieldList.add(formField.getTitle());//修改字段的名称
				formFieldList.add(getHtmlValue(formField,formFieldValuePrev,request));//修改前的值
				formFieldList.add(getHtmlValue(formField,formFieldValue,request));//修改后的值
				formFieldsList.add(formFieldList);
			}
		}
	}else{
	    //之前没有记录，则显示当前被修改的值
		for (FormField formField : vector) {
			String formFieldValue = rr.getFieldValue(formField.getName());
			if ("".equals(formFieldValue) || null == formFieldValue){
				continue;
			}
			List<String> formFieldList = new ArrayList<String>();
			formFieldList.add(id.toString());//logId;
			formFieldList.add(cws_log_date);//修改日期
			formFieldList.add(cws_log_user);//修改人
			formFieldList.add(cws_log_id);//修改id
			formFieldList.add(cws_log_type);//修改类型
			formFieldList.add(formField.getTitle());//修改字段的名称
			formFieldList.add("");//修改前的值
			formFieldList.add(getHtmlValue(formField,formFieldValue,request));//修改后的值
			formFieldsList.add(formFieldList);
		}
	}
}
%>
<table id="searchTable" width="100%" border="0" cellspacing="1" cellpadding="3" align="center" style="background-color: #f4f4f4;">
  <tr>
    <td height="23" align="left">
    <form id="searchForm" action="module_log_list.jsp">&nbsp;
    	类型&nbsp;
        <select id="logType" name="logType">
        <option value="-1">不限</option>
        <option value="<%=FormDAOLog.LOG_TYPE_CREATE%>">创建</option>
        <option value="<%=FormDAOLog.LOG_TYPE_EDIT%>">修改</option>
        <option value="<%=FormDAOLog.LOG_TYPE_DEL%>">删除</option>
        </select>
    	修改人&nbsp;
    	<input id="userName" name="userName" size="10" value="<%=userName%>" />
        原记录ID&nbsp;
        <input id="fdaoId" name="fdaoId" size="5" value="<%=fdaoId==-1?"":fdaoId%>" />
        开始时间&nbsp;
        <input id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        结束时间&nbsp;
        <input id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
      	<input type="hidden" name="op" value="search" />
        <input type="hidden" name="code" value="<%=code%>" />
        <input type="hidden" name="formCode" value="<%=formCode%>" />
        <input type="hidden" name="fdaoId" value="<%=fdaoId%>" />
        <input class="tSearch" name="submit" type="submit" value="搜索"/>
        </form>
    </td>
  </tr>
</table>

<table cellpadding="0" cellspacing="0" id="logTable">
  <thead>
  <tr>
    <th width="55" style="cursor:hand" align="center">原记录ID</th>
    <th width="150" style="cursor:hand" align="center">时间</th>
    <th width="80" style="cursor:hand" align="center">修改人</th>
    <th width="50" style="cursor:hand" align="center">类型</th>
	<th width="80" style="cursor:hand">字段名称</th>
	<th width="250" style="cursor:hand">修改前的值</th>
	<th width="250" style="cursor:hand">修改后的值</th>
	<th width="100" style="cursor:hand">操作</th>
  </tr>
  </thead>
  <tbody>
  <%
	  UserMgr um = new UserMgr();
	  for (List<String> formFieldList: formFieldsList){%>
  		<tr align="center" id="<%=formFieldList.get(0)%>">
            <td><%=formFieldList.get(3)%></td>
			<td><%=DateUtil.format(DateUtil.parse(formFieldList.get(1),"yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss")%>
			<td><%=um.getUserDb(formFieldList.get(2)).getRealName()%></td>
			<td>
				<%if (Integer.parseInt(formFieldList.get(4))==FormDAOLog.LOG_TYPE_CREATE) {%>
				创建
				<%}else if (Integer.parseInt(formFieldList.get(4))==FormDAOLog.LOG_TYPE_EDIT) {%>
				修改
				<%}else{%>
				删除
				<%}%>
			</td>
			<td><%=formFieldList.get(5)%></td>
			<td><%=formFieldList.get(6)%></td>
			<td><%=formFieldList.get(7)%></td>
			<td>
				<%if (mpd.canUserManage(privilege.getUser(request)) && Integer.parseInt(formFieldList.get(4)) == FormDAOLog.LOG_TYPE_DEL) {%>
				<a href="javascript:;" onclick="restore('<%=formFieldList.get(0)%>')">恢复</a>
				<%}%>
			</td>
		<%}
  %>
  </tbody>
</table>
</body>
<script>
function restore(id) {
	jConfirm('您确定要恢复么？', '提示', function(r) {
		if (!r) {
			return;
		}

		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/visual/module_log_list.jsp",
			data: {
				formCode: '<%=formCode%>',
				action: 'restore',
				id: id
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").eq(0).showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					$('tr[id=' + id + ']').remove();
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").eq(0).hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});		
	});	
}

function initCalendar() {
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
}

function doOnToolbarInited() {
	initCalendar();
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_log_list.jsp?op=<%=op%>&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/visual/module_log_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_log_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

function initGrid() {
	flex = $("#logTable").flexigrid
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
			nowrap: false,
			rp: <%=pagesize%>,
	
			// title: "通知",
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
}

function action(com, grid) {

}

$(document).ready(function() {
	o("logType").value = "<%=logType%>";
});
</script>
</html>
