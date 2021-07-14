<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.workplan.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%
int workplanId = ParamUtil.getInt(request, "id", -1);

if (workplanId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "id格式错误！"));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划历史</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<style type="text/css">
<!--
.wikiTable td {
	border-bottom:1px dashed #cccccc;
}
-->
</style>
</head>
<body>
<%
com.redmoon.oa.workplan.Privilege wppvg = new com.redmoon.oa.workplan.Privilege();
boolean canSee = wppvg.canUserSeeWorkPlan(request, (int)workplanId);
if (!canSee) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu7").className="current";
</script>
<div class="spacerH"></div>
<table id="mainTable" class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
		<thead>
          <tr>
          	<td class="tabStyle_1_title" width="7%" height="22" align="center"><input class="btn" type="button" value="版本对比" onClick="compare()" /></td>
            <td class="tabStyle_1_title" width="7%" align="center">ID</td>
            <td class="tabStyle_1_title" width="17%" align="center"><strong>更新时间</strong></td>
            <td class="tabStyle_1_title" width="12%" align="center"><strong>修改者</strong></td>
            <td class="tabStyle_1_title" width="46%" align="center"><strong>备注</strong></td>
            <td class="tabStyle_1_title" width="11%" align="center"><strong> 操作</strong></td>
          </tr>
        </thead>
      <%
	  UserMgr um = new UserMgr();
	  WorkPlanLogDb wpld = new WorkPlanLogDb();
	  String sql = wpld.getTable().getSql("listLog");
	  Iterator ir = wpld.list(sql, new Object[]{workplanId}).iterator();
	  while (ir.hasNext()) {
	  	wpld = (WorkPlanLogDb)ir.next();
		UserDb user = um.getUserDb(wpld.getString("user_name"));
		%>
          <tr class="wikiTable">
            <td height="25" align="center"><input id="<%=wpld.getLong("id")%>" name="ids" type="checkbox" value="<%=wpld.getLong("id")%>" /></td>
            <td align="center"><%=wpld.getLong("id")%></td>
            <td align="center"><%=DateUtil.format(wpld.getDate("create_date"), "yyyy-MM-dd HH:mm")%></td>
            <td align="center"><a href="javascript:;" onclick="addTab('用户信息', 'user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a></td>
            <td align="left"><%=wpld.getString("remark")%></td>
            <td align="center"><a href="workplan_his_gantt.jsp?id=<%=workplanId%>&hisId=<%=wpld.getLong("id")%>">查看</a></td>
          </tr>
          <%
	  }
	  %>
</table>
</body>
<script>
var c1;
var c2;
$(document).ready(function() { 
	$("input[name='ids']").bind("click", function() {
		if ($(this)[0].checked) {
			if (c1==null) {
				if (c2!=null) {
					if (c2.id!=$(this)[0].id)
						c1 = $(this)[0];
				}
				else
					c1 = $(this)[0];
			}
			else if (c2==null) {
				if (c1!=null) {
					if (c1.id!=$(this)[0].id)
						c2 = $(this)[0];
				}
				else
					c2 = $(this)[0];
			}
			else {
				c1.checked = false;
				c1 = c2;
				c2 = $(this)[0];
			}
		}
		else {
			// alert($(this)[0].id + " " + $(this)[0].checked);
			if (c1 && $(this)[0].id==c1.id)
				c1 = null;
			else if (c2 && $(this)[0].id==c2.id)
				c2 = null;
		}

	});
}
);

function compare() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请选择需要对比的记录！","提示");
		return;
	}
	var ary = ids.split(",");
	if (ary.length!=2) {
		jAlert("请选择两条记录进行对比！","提示");
		return;
	}
	window.location.href = 'workplan_his_compare.jsp?id=<%=workplanId%>&ids=' + ids;
}

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
</script>
</html>