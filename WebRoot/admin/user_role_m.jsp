<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.netdisk.UtilTools"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="roleMgr" scope="page" class="com.redmoon.oa.pvg.RoleMgr"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
if ("changeOrder".equals(op)) {
	String code = ParamUtil.get(request, "code");
	int order = ParamUtil.getInt(request, "order", -1);
	JSONObject json = new JSONObject();
	if (order==-1) {
		json.put("ret", "0");
		json.put("msg", "请输入整数");
		out.print(json);
		return;
	}
	RoleDb rd = new RoleDb();
	rd = rd.getRoleDb(code);
	rd.setOrders(order);
	boolean re = rd.save();
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
else if (op.equals("del")) {
	JSONObject json = new JSONObject();
	boolean re = roleMgr.del(request);
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
else if (op.equals("copy")) {
	JSONObject json = new JSONObject();
	boolean re = roleMgr.copy(request);
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
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理角色</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<script type="text/javascript" src="../js/jquery.toaster.js"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean isAdmin = privilege.isUserPrivValid(request, "admin");
String curUnitCode = ParamUtil.get(request, "curUnitCode");
String description = ParamUtil.get(request, "description");
String action = StrUtil.getNullString(request.getParameter("action"));

if (op.equals("add")) {
	try {
		if (roleMgr.add(request))
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_role_m.jsp?curUnitCode=" + StrUtil.UrlEncode(curUnitCode)));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
%>
<%@ include file="user_role_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%
String code;
String desc;
RoleDb roleDb = new RoleDb();
Vector result;
String sql;
if (curUnitCode.equals("")) {
	if (isAdmin) {
		// result = roleDb.list();
		sql = "select code from user_role where 1=1";
	}
	else {
		String unitCode = privilege.getUserUnitCode(request);
		sql = "select code from user_role where unit_code=" + StrUtil.sqlstr(unitCode);
		// result = roleDb.getRolesOfUnit(unitCode);
	}
}
else {
	sql = "select code from user_role where unit_code=" + StrUtil.sqlstr(curUnitCode);
	// result = roleDb.getRolesOfUnit(curUnitCode);
}

if (action.equals("search")) {
	if (!description.equals("")) {
		sql +=" and description like " + StrUtil.sqlstr("%" + description + "%");
	}
}

sql += " order by isSystem desc, unit_code asc, orders desc, description asc";

result = roleDb.list(sql);	

Iterator ir = result.iterator();
%>
<table width="98%" class="percent98">
  <tr>
    <td align="center">
    <form id="searchForm" name="searchForm" method="get">
      单位
      <select id="curUnitCode" name="curUnitCode">
      <option value="">不限</option>
      <%
        if (License.getInstance().isGroup() || License.getInstance().isPlatform()) {
            DeptDb dd = new DeptDb();
            DeptView dv = new DeptView(request, dd);
            StringBuffer sb = new StringBuffer();
            dd = dd.getDeptDb(privilege.getUserUnitCode(request));
            %>
            <%=dv.getUnitAsOptions(sb, dd, dd.getLayer())%>
            <%
        }
      %>
      </select>
      <script>
      o("curUnitCode").value = "<%=curUnitCode%>";
      </script>    
      名称
      <input type="hidden" name="action" value="search" />
      <input type="text" name="description" value="<%=description%>" />
      <input type="submit" class="btn" value="搜索" />
    </form></td>
  </tr>
</table>
<style>
.inputOrder {
	border:1px solid #ccc;
	width:40px;
	background-color:transparent;	
}
</style>
<script>
function sortNumber(a,b) {
	return b[1] - a[1];
}

// 排序
function sortTable() {
	var ary = new Array();
	$('#mainTable tr').each(function(i) {
		if ($(this).attr("isSystem") && $(this).attr("isSystem")!="true") {
			var code = $(this).attr("code");
			var value = $(this).find("input").val();
			ary[i] = new Array(code, value);
			// console.log(i);
		}
	});

	ary.sort(sortNumber);
	
	var trCur = null;
	for (i=0; i<ary.length-1; i++) {
		if (ary[i]) {
			if (trCur==null) {
				trCur = $('#tr' + ary[i][0]);
			}
			else {
				var tr = $('#tr' + ary[i][0]);
				tr.insertAfter(trCur);
				trCur = tr;
			}
		}
	}		
}

$(function() {
	$('.inputOrder').change(function() {
		var trId = "tr" + $(this).attr("code");
		$('#' + trId).fadeOut().fadeIn();
		
		$.ajax({
			type: "post",
			url: "user_role_m.jsp",
			data: {
				code: $(this).attr("code"),
				op: "changeOrder",
				order: $(this).val()
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
					sortTable();	
					$.toaster({
						"priority" : "info",
						"message" : data.msg
					});					
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#mainTable').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});	
	});
});
</script>
<%
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean isNetdiskUsed = cfg.getBooleanProperty("isNetdiskUsed");
%>
<table id="mainTable" cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
<thead>
    <tr>
      <td class="tabStyle_1_title" style="PADDING-LEFT: 10px" nowrap width="7%">排序号</td>
      <td class="tabStyle_1_title" nowrap width="15%">名称</td>
      <td class="tabStyle_1_title" nowrap width="9%">单位</td>
      <td class="tabStyle_1_title" nowrap width="15%">人员</td>
      <td class="tabStyle_1_title" nowrap width="6%" style="display:none">类别</td>
      <%if (isNetdiskUsed) {%>
      <td class="tabStyle_1_title" nowrap width="13%">云盘配额</td>
      <%}%>
      <td class="tabStyle_1_title" nowrap width="12%">内部邮箱配额</td>
      <td class="tabStyle_1_title" nowrap width="7%">系统</td>
      <td width="16%" nowrap class="tabStyle_1_title">操作</td>
    </tr>
    </thead>
    <tbody>
<%
DeptMgr dm = new DeptMgr();
int i = 0;
UserMgr um = new UserMgr();
RoleUser ru = new RoleUser();
while (ir.hasNext()) {
 	RoleDb rd = (RoleDb)ir.next();
	code = rd.getCode();
	desc = rd.getDesc();
	%>
    <tr class="highlight" id="tr<%=code%>" code="<%=code%>" isSystem="<%=rd.isSystem()%>">
      <td align="center">
	  <input id="order<%=code%>" code="<%=code%>" name="order" class="inputOrder" value="<%=rd.getOrders()%>" />
      </td>
      <td><a href="user_role_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(code)%>"><%=desc%></a></td>
      <td>
	  <%=dm.getDeptDb(rd.getUnitCode()).getName()%>
	  </td>
      <td>
      <%
      StringBuffer sb = new StringBuffer();
	  ListResult lr = ru.listResult(code, 1, 4);
	  if (lr != null) {
	  	Vector vt = lr.getResult();
		 Iterator irRu = vt.iterator();
		 int k = 0;
		 while (irRu.hasNext()) {
		 	ru = (RoleUser)irRu.next();
			UserDb user = um.getUserDb(ru.getUserName());
			StrUtil.concat(sb, "，", user.getRealName());
			k++;
			if (k==3) break;
		 }
		 out.print(sb);
		 if (vt.size() > 3) {
		 	out.print("，...");
		 }
	  }
	  %>
      </td>
      <td style="display:none">
        <%
	  if (rd.getType()==RoleDb.TYPE_NORMAL)
	  	out.print("普通");
	  else
	  	out.print("特定");
	  %>
      </td>
      <%if (isNetdiskUsed) {%>      
      <td><%=rd.getDiskQuota()==RoleDb.DISK_QUOTA_NOT_SET?"未指定":"" + UtilTools.getFileSize(rd.getDiskQuota())%></td>
      <%}%>
      <td><%=rd.getMsgSpaceQuota()==RoleDb.DISK_QUOTA_NOT_SET?"未指定":"" + UtilTools.getFileSize(rd.getMsgSpaceQuota())%></td>
      <td align="center"><%=rd.isSystem()?"是":"否"%></td>
      <td align="center">
	  <a id="edit<%=i %>" href="javascript:;" onclick="addTab('<%=desc%>', '<%=request.getContextPath()%>/admin/user_role_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(code)%>')">编辑</a>
	  <%
	  if (!code.equals(RoleDb.CODE_MEMBER)) {
		  i++;
		  if (!rd.isSystem() || privilege.isUserPrivValid(request, "admin")) {%>
			&nbsp;&nbsp;<a href="javascript:;" onclick="del('<%=code%>')" style="cursor:pointer">删除</a>
		  <%}%>
	  <%}%>
	  &nbsp;&nbsp;<a href="javascript:;" title="复制角色，仅复制权限" onclick="copy('<%=code%>', '<%=desc%>')" style="cursor:pointer">复制</a>
      <!--
      &nbsp;&nbsp;<a href="user_role_priv.jsp?roleCode=<%=StrUtil.UrlEncode(code)%>&desc=<%=StrUtil.UrlEncode(desc)%>">权限</a>
      -->
      </td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
<script>
<%
if("introduction".equals(flag)){
	%>
		jQuery(document).ready(function(){
			var tour = {
					id : "hopscotch",
					steps : [ {
						title : "提示",
						content : "此处可以修改角色的名称",
						target : "edit1",
						placement : "top",
						showNextButton : false,
						width : "180px",
						xOffset : -100,
						yOffset : -5,
						arrowOffset : 90
					}]
				};
			hopscotch.startTour(tour);
		});
<%}%>
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

function del(code) {
	jConfirm('您确定要删除么？', '提示', function(r) {
		if (!r) {
			return;
		}
		$.ajax({
			type: "post",
			url: "user_role_m.jsp",
			data: {
				code: code,
				op: "del"
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
					$('#tr' + code).remove();
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#mainTable').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});	
}

function copy(code, desc) {
	jConfirm('您确定要复制"' + desc + '"么？', '提示', function(r) {
		if (!r) {
			return;
		}
		$.ajax({
			type: "post",
			url: "user_role_m.jsp",
			data: {
				code: code,
				op: "copy"
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
					window.location.reload()
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#mainTable').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});	
}
</script>
</html>