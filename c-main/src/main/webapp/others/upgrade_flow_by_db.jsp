<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.cloudwebsoft.framework.db.DataSource" %>
<%@ page import="org.apache.ibatis.annotations.Param" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.cloudwebsoft.framework.db.Connection" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%
	/**
	 * 流程同步工具，同步表单中的字段
	 * 以本应用为旧版数据库，其它数据源为新版数据库
	 */
	String op = ParamUtil.get(request, "op");
	if ("sync".equals(op)) {
		JSONObject json = new JSONObject();
		String dbSource = ParamUtil.get(request, "dbSource");
		String typeCode = ParamUtil.get(request, "typeCode");
		String sqlNew = "select * from flow_directory where code=?";
		JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
		ResultIterator ri = jt.executeQuery(sqlNew, new Object[]{typeCode});
		if (ri.hasNext()) {
			ResultRecord rr = ri.next();

			Leaf lf = new Leaf();
			lf = lf.getLeaf(typeCode);
			if (lf == null) {
				json.put("ret", 0);
				json.put("msg", "本系统中流程不存在，请在新系统的表单管理中导出，然后导入本系统，导入时会带有流程及基础数据");
				out.print(json.toString());
				return;
			}

			String sql = "update flow_directory set name=" + StrUtil.sqlstr(rr.getString("name")) +
					",description=" + StrUtil.sqlstr(rr.getString("description")) +
					",parent_code=" + StrUtil.sqlstr(rr.getString("parent_code")) +
					",type=" + rr.getInt("type") + ",isHome=" + (rr.getInt("isHome")==1 ? "1" : "0") + ",doc_id=" + rr.getInt("doc_id") + ",template_id=" + rr.getInt("template_id") +
					",orders=" + rr.getInt("orders") + ",layer=" + rr.getInt("layer") + ",child_count=" + rr.getInt("child_count") + ",pluginCode=" + StrUtil.sqlstr(rr.getString("pluginCode")) +
					",formCode=" + StrUtil.sqlstr(rr.getString("formCode")) + ",dept=" + StrUtil.sqlstr(rr.getString("dept")) + ",is_open=" + (rr.getInt("is_open")==1 ? 1 : 0) + ",unit_code=" + StrUtil.sqlstr(rr.getString("unit_code")) + ",is_debug=" + (rr.getInt("is_debug")==1 ? 1 : 0) +
					",is_mobile_start=" + (rr.getInt("is_mobile_start")==1 ? 1 : 0) +
					",is_mobile_location=" + (rr.getInt("is_mobile_location")==1 ? 1 : 0) +
					",is_mobile_camera=" + (rr.getInt("is_mobile_camera")==1 ? 1 : 0) +
					",query_id=" + rr.getInt("query_id") +
					",query_role=" + StrUtil.sqlstr(rr.getString("query_role")) +
					",query_cond_map=" + StrUtil.sqlstr(rr.getString("query_cond_map")) +
					",col_props=" + StrUtil.sqlstr(rr.getString("col_props")) +
					",cond_props=" + StrUtil.sqlstr(rr.getString("cond_props")) +
					",params=" + StrUtil.sqlstr(rr.getString("params")) +
					" where code=" + StrUtil.sqlstr(typeCode);

			JdbcTemplate jdbcTemplate = new JdbcTemplate();
			boolean re = jdbcTemplate.executeUpdate(sql) == 1;

			LeafChildrenCacheMgr.removeAll();
			lf.removeAllFromCache();

			WorkflowPredefineDb workflowPredefineDb = new WorkflowPredefineDb();
			workflowPredefineDb = workflowPredefineDb.getDefaultPredefineFlow(typeCode);

			// 更新flow_predefined
			sqlNew = "select * from flow_predefined where typeCode=?";
			ri = jt.executeQuery(sqlNew, new Object[]{typeCode});
			if (ri.hasNext()) {
				rr = ri.next();

				sql = "update flow_predefined set flowString=?,typeCode=?,title=?,return_back=?,IS_DEFAULT_FLOW=?," +
						"dir_code=?,examine=?,is_reactive=?,is_recall=?,return_mode=?,return_style=?,role_rank_mode=?," +
						"props=?,views=?,scripts=?,is_light=?,link_prop=?,write_prop=?,is_distribute=?,write_db_prop=?," +
						"msg_prop=?,is_plus=?,is_transfer=?,is_reply=?,download_count=?,can_del_on_return=? where id=?";
				PrimaryKey primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
				String flowString = rr.getString("flowString");
				if (flowString.equals("")) {
					flowString = " "; // 适应SQLSERVER
				}
				PreparedStatement ps = null;
				Conn conn = new Conn(Global.getDefaultDB());
				try {
					ps = conn.prepareStatement(sql);
					if (flowString.equals("")) {
						flowString = " "; // 适应SQLSERVER
					}
					ps.setString(1, flowString); //适应Oracle，LONG类型需放在第一个
					ps.setString(2, typeCode);
					ps.setString(3, rr.getString("title"));
					ps.setInt(4, rr.getInt("return_back"));
					ps.setInt(5, rr.getInt("IS_DEFAULT_FLOW"));
					ps.setString(6, rr.getString("dir_code"));
					ps.setInt(7, rr.getInt("examine"));
					ps.setInt(8, rr.getInt("is_reactive"));
					ps.setInt(9, rr.getInt("is_recall"));
					ps.setInt(10, rr.getInt("return_mode"));
					ps.setInt(11, rr.getInt("return_style"));
					ps.setInt(12, rr.getInt("role_rank_mode"));
					ps.setString(13, rr.getString("props"));
					ps.setString(14, rr.getString("views"));
					ps.setString(15, rr.getString("scripts"));
					ps.setInt(16, rr.getInt("is_light"));
					ps.setString(17, rr.getString("link_prop"));
					ps.setString(18, rr.getString("write_prop"));
					ps.setInt(19, rr.getInt("is_distribute"));
					ps.setString(20, rr.getString("write_db_prop"));
					ps.setString(21, rr.getString("msg_prop"));
					ps.setInt(22, rr.getInt("is_plus"));
					ps.setInt(23, rr.getInt("is_transfer"));
					ps.setInt(24, rr.getInt("is_reply"));
					ps.setInt(25, rr.getInt("download_count"));
					ps.setInt(26, rr.getInt("can_del_on_return"));
					ps.setInt(27, workflowPredefineDb.getId());
					re = conn.executePreUpdate() == 1 ? true : false;
					if (re) {
						WorkflowPredefineCache rc = new WorkflowPredefineCache(workflowPredefineDb);
						primaryKey.setValue(new Integer(workflowPredefineDb.getId()));
						rc.refreshSave(primaryKey);
					}
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error(e);
				} finally {
					if (conn != null) {
						conn.close();
						conn = null;
					}
				}
			}
		}
		json.put("ret", 1);
		json.put("msg", "操作成功");
		out.print(json.toString());
		return;
	}
	else if ("syncScripts".equals(op)) {
		JSONObject json = new JSONObject();
		String dbSource = ParamUtil.get(request, "dbSource");
		String typeCode = ParamUtil.get(request, "typeCode");
		String sqlNew = "select * from flow_directory where code=?";
		JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
		ResultIterator ri = jt.executeQuery(sqlNew, new Object[]{typeCode});
		if (ri.hasNext()) {
			ResultRecord rr = ri.next();

			Leaf lf = new Leaf();
			lf = lf.getLeaf(typeCode);

			boolean re = true;

			LeafChildrenCacheMgr.removeAll();
			lf.removeAllFromCache();

			WorkflowPredefineDb workflowPredefineDb = new WorkflowPredefineDb();
			workflowPredefineDb = workflowPredefineDb.getDefaultPredefineFlow(typeCode);

			JdbcTemplate jdbcTemplate = new JdbcTemplate();
			// 更新flow_predefined
			sqlNew = "select * from flow_predefined where typeCode=?";
			ri = jt.executeQuery(sqlNew, new Object[]{typeCode});
			if (ri.hasNext()) {
				rr = ri.next();

				String sql = "update flow_predefined set scripts=? where id=?";
				PrimaryKey primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
				PreparedStatement ps = null;
				Conn conn = new Conn(Global.getDefaultDB());
				try {
					ps = conn.prepareStatement(sql);
					ps.setString(1, rr.getString("scripts"));
					ps.setInt(2, workflowPredefineDb.getId());
					re = conn.executePreUpdate() == 1;
					if (re) {
						WorkflowPredefineCache rc = new WorkflowPredefineCache(workflowPredefineDb);
						primaryKey.setValue(new Integer(workflowPredefineDb.getId()));
						rc.refreshSave(primaryKey);
					}
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error(e);
				} finally {
					conn.close();
				}
			}
		}
		json.put("ret", 1);
		json.put("msg", "操作成功");
		out.print(json.toString());
		return;
	}
	else if ("syncScriptsBatch".equals(op)) {
		JSONObject json = new JSONObject();
		String typeCodes = ParamUtil.get(request, "typeCodes");
		String[] arr = StrUtil.split(typeCodes, ",");
		if (arr == null) {
			json.put("ret", 0);
			json.put("msg", "请选择流程");
			out.print(json.toString());
			return;
		}
		String dbSource = ParamUtil.get(request, "dbSource");
		for (String typeCode : arr) {
			String sqlNew = "select * from flow_directory where code=?";
			JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
			ResultIterator ri = jt.executeQuery(sqlNew, new Object[]{typeCode});
			if (ri.hasNext()) {
				ResultRecord rr = ri.next();

				Leaf lf = new Leaf();
				lf = lf.getLeaf(typeCode);

				boolean re = true;

				LeafChildrenCacheMgr.removeAll();
				lf.removeAllFromCache();

				WorkflowPredefineDb workflowPredefineDb = new WorkflowPredefineDb();
				workflowPredefineDb = workflowPredefineDb.getDefaultPredefineFlow(typeCode);

				JdbcTemplate jdbcTemplate = new JdbcTemplate();
				// 更新flow_predefined
				sqlNew = "select * from flow_predefined where typeCode=?";
				ri = jt.executeQuery(sqlNew, new Object[]{typeCode});
				if (ri.hasNext()) {
					rr = ri.next();

					String sql = "update flow_predefined set scripts=? where id=?";
					PrimaryKey primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
					PreparedStatement ps = null;
					Conn conn = new Conn(Global.getDefaultDB());
					try {
						ps = conn.prepareStatement(sql);
						ps.setString(1, rr.getString("scripts"));
						ps.setInt(2, workflowPredefineDb.getId());
						re = conn.executePreUpdate() == 1;
						if (re) {
							WorkflowPredefineCache rc = new WorkflowPredefineCache(workflowPredefineDb);
							primaryKey.setValue(workflowPredefineDb.getId());
							rc.refreshSave(primaryKey);
						}
					} catch (SQLException e) {
						LogUtil.getLog(getClass()).error(e);
					} finally {
						conn.close();
					}
				}
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
	<title>通过数据库升级流程</title>
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
	<strong>同步流程&nbsp;&nbsp;数据源</strong>
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
	&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="确定" onclick="window.location.href='upgrade_flow_by_db.jsp?dbSource=' + o('dbSource').value;"/>
	（注意本应用为旧版数据库，其它数据源为新版数据库）
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
<div style="width: 95%; margin: 10px auto">
	<input type="button" class="btn btn-default" onclick="syncScriptsBatch()" value="批量同步脚本"/>
</div>
<table cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1">
	<tr>
		<td class="tabStyle_1_title" width="30">
			<input type="checkbox" id="all"/>
		</td>
		<td class="tabStyle_1_title" width="20%">编码</td>
		<td class="tabStyle_1_title" width="25%">新版</td>
		<td class="tabStyle_1_title" width="25%">本版</td>
		<td class="tabStyle_1_title" width="20%">操作</td>
	</tr>
	<%
		Leaf lf = new Leaf();
		lf = lf.getLeaf(Leaf.CODE_ROOT);
		String sqlNew = "select code,name from flow_directory where code<>'root' order by code asc";
		JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
		ResultIterator ri = jt.executeQuery(sqlNew);

		Vector v = new Vector();
		lf.getAllChild(v, lf);

		while (ri.hasNext()) {
			ResultRecord rr = ri.next();
			String codeNew = "", nameNew = "", name="";
			boolean isFound = false;
			codeNew = rr.getString(1);
			nameNew = rr.getString(2);
			ir = v.iterator();
			while (ir.hasNext()) {
				lf = (Leaf)ir.next();
				if (lf.getCode().equals(codeNew)) {
					name = lf.getName();
					isFound = true;
					break;
				}
			}
	%>
	<tr>
		<td align="center">
			<%
				if (isFound) {
			%>
			<input class="chk" type="checkbox" value="<%=codeNew%>"/>
			<%
				}
			%>
		</td>
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
			<button class="btn btn-default" onclick="sync('<%=codeNew%>', '<%=nameNew%>')">同步流程</button>
			&nbsp;&nbsp;
			<button class="btn btn-default" onclick="syncScripts('<%=codeNew%>', '<%=nameNew%>')">同步脚本</button>
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
	function sync(typeCode, name) {
		jConfirm('您确定要同步"' + name + '"么？', '提示', function (r) {
			if (!r) {
				return;
			}

			$.ajax({
				type: "post",
				url: "upgrade_flow_by_db.jsp",
				data: {
					op: "sync",
					typeCode: typeCode,
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

	function syncScripts(typeCode, name) {
		jConfirm('您确定要同步"' + name + '"么？', '提示', function (r) {
			if (!r) {
				return;
			}

			$.ajax({
				type: "post",
				url: "upgrade_flow_by_db.jsp",
				data: {
					op: "syncScripts",
					typeCode: typeCode,
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

	$(function() {
		$('#all').click(function() {
			$('.chk').prop('checked', $(this).prop('checked'));
		})
	})

	function syncScriptsBatch() {
		var codes = '';
		$('.chk').each(function() {
			if ($(this).prop('checked')) {
				if (codes == '') {
					codes = $(this).val();
				}
				else {
					codes += ',' + $(this).val();
				}
			}
		});
		if (codes == '') {
			jAlert('请选择流程', '提示');
			return;
		}
		jConfirm('您确定要批量同步脚本么？', '提示', function (r) {
			if (!r) {
				return;
			}

			$.ajax({
				type: "post",
				url: "upgrade_flow_by_db.jsp",
				data: {
					op: "syncScriptsBatch",
					typeCodes: codes,
					dbSource: "<%=dbSource%>"
				},
				dataType: "html",
				beforeSend: function (XMLHttpRequest) {
					$("body").showLoading();
				},
				success: function (data, status) {
					data = $.parseJSON(data);
					jAlert(data.msg, "提示", function() {
						// window.location.reload();
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
