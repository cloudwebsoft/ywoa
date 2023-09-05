<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.ResultIterator"%>
<%@ page import="cn.js.fan.db.ResultRecord"%>
<%@ page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="cn.js.fan.web.DBInfo"%>
<%@ page import="com.alibaba.fastjson.JSONObject"%>
<%@ page import="com.cloudwebsoft.framework.db.Connection"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.flow.FormMgr" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<%
	/**
	 * 表单同步工具，同步表单中的字段
	 * 以本应用为旧版数据库，其它数据源为新版数据库
	 */
	String op = ParamUtil.get(request, "op");
	if ("syncForm".equals(op)) {
		JSONObject json = new JSONObject();
		FormMgr formMgr = new FormMgr();
		String dbSource = ParamUtil.get(request, "dbSource");
		String formCode = ParamUtil.get(request, "formCode");
		String sqlNew = "select * from form where code=?";
		JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
		ResultIterator ri = jt.executeQuery(sqlNew, new Object[]{formCode});
		if (ri.hasNext()) {
			ResultRecord rr = ri.next();

			FormDb ftd = new FormDb();
			ftd = ftd.getFormDb(formCode);
			ftd.setName(rr.getString("name"));
			ftd.setOldContent(rr.getString("content"));
			ftd.setContent(rr.getString("content"));
			ftd.setFlowTypeCode(rr.getString("flowTypeCode"));
			ftd.setHasAttachment(rr.getInt("has_attachment")==1);

			ftd.setIeVersion(rr.getString("ie_version"));
			ftd.setLog(rr.getInt("is_log")==1);
			ftd.setProgress(rr.getInt("is_progress")==1);

			ftd.setUnitCode(rr.getString("unit_code"));
			ftd.setOnlyCamera(rr.getInt("is_only_camera")==1);

			ftd.setFlow(rr.getInt("isFlow")==1);

			ftd.setCheckSetup(rr.getString("check_setup"));
			ftd.setViewSetup(rr.getString("view_setup"));

			boolean re = ftd.save();
			if (re) {
				formMgr.regenerateFormView(formCode);
			}
		}
		json.put("ret", 1);
		json.put("msg", "操作成功");
		out.print(json.toString());
		return;
	}
%>
<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>通过数据库升级表单</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<link href="index.css" rel="stylesheet" type="text/css">
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>

	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
	<script src="../js/bootstrap/js/bootstrap.min.js"></script>

	<link href="../js/select2/select2.css" rel="stylesheet"/>
	<script src="../js/select2/select2.js"></script>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

</head>
<body>
<div style="text-align:center;margin:20px">
	<strong>
	同步表单&nbsp;&nbsp;
	数据源</strong>
	<select id="dbSource" name="dbSource">
		<option value="">请选择</option>
		<%
			cn.js.fan.web.Config cfg = new cn.js.fan.web.Config();
			Iterator ir = cfg.getDBInfos().iterator();
			while (ir.hasNext()) {
				DBInfo di = (DBInfo)ir.next();
		%>
		<option value="<%=di.name%>" <%=di.isDefault?"selected":""%>><%=di.name%></option>
		<%
			}
		%>
	</select>
	&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;" onclick="if ('oa'==$('#dbSource').val()) {alert('不能修改默认的数据源oa'); return;} openWin('../admin/db_conn_edit.jsp?dbSource=' + $('#dbSource').val(), 640, 480)">修改数据源</a>
	&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="确定" onclick="window.location.href='upgrade_form_by_db.jsp?dbSource=' + o('dbSource').value;"/>
</div>
<div style="margin-left: 20px">
	注意本应用为旧版数据库，其它数据源为新版数据库<br/>
	新增表单可从新版系统的”表单管理”界面导出，导出时会自动带入基础数据及关联的流程，导入本应用后，流程将会显示在流程类型的”解决方案“中
</div>
<%
	String dbSource = ParamUtil.get(request, "dbSource");
%>
<script>
	$('#dbSource').val("<%=dbSource%>");
</script>
<%
	if ("".equals(dbSource)) {
		return;
	}
%>
<table cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1">
	<tr>
		<td class="tabStyle_1_title" width="20%">编码</td>
		<td class="tabStyle_1_title" width="30%">新版</td>
		<td class="tabStyle_1_title" width="30%">本版</td>
		<td class="tabStyle_1_title" width="20%">操作</td>
	</tr>
	<%
		FormDb fd = new FormDb();
		String sqlNew = "select code,name from form order by code asc";
		JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
		ResultIterator ri = jt.executeQuery(sqlNew);

		String sql = "select code from form order by code asc";
		Vector v = fd.list(sql);
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			String codeNew = "", nameNew = "", name="";
			boolean isFound = false;
			codeNew = rr.getString(1);
			nameNew = rr.getString(2);
			ir = v.iterator();
			while (ir.hasNext()) {
				fd = (FormDb)ir.next();
				if (fd.getCode().equals(codeNew)) {
					name = fd.getName();
					isFound = true;
					break;
				}
			}
	%>
	<tr>
		<td><%=codeNew%></td>
		<td><%=nameNew%></td>
		<td>
			<%
				if (isFound) {
					out.print(name);
				}
			%>
		</td>
		<td>
			<%
				if (isFound) {
			%>
			<button class="btn btn-default" onclick="sync('<%=fd.getCode()%>', '<%=nameNew%>')">同步</button>
			<button class="btn btn-default" onclick="window.open('upgrade_module_by_db.jsp?dbSource=<%=dbSource%>&formCode=<%=fd.getCode()%>')">模块</button>
			<%
				}
			%>
		</td>
	</tr>
	<%
		}
	%>
</table>
</body>
<script>
	function sync(formCode, name) {
		jConfirm('您确定要同步"' + name + '"么？', '提示', function (r) {
			if (!r) {
				return;
			}

			$.ajax({
				type: "post",
				url: "upgrade_form_by_db.jsp",
				data: {
					op: "syncForm",
					formCode: formCode,
					dbSource: "<%=dbSource%>"
				},
				dataType: "html",
				beforeSend: function (XMLHttpRequest) {
					$("body").showLoading();
				},
				success: function (data, status) {
					data = $.parseJSON(data);
					jAlert(data.msg, "提示", function() {
						window.location.reload();
					});
				},
				complete: function (XMLHttpRequest, status) {
					$("body").hideLoading();
				},
				error: function (XMLHttpRequest, textStatus) {
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		});
	}
</script>
</html>
