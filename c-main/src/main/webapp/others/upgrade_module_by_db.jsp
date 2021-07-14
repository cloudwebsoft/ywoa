<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.PrimaryKey" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.cloudwebsoft.framework.db.Connection" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.flow.FormMgr" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%
	/**
	 * 模块同步工具，同步表单中的字段
	 * 以本应用为旧版数据库，其它数据源为新版数据库
	 */
    String op = ParamUtil.get(request, "op");
    if ("syncModule".equals(op)) {
        JSONObject json = new JSONObject();
        FormMgr formMgr = new FormMgr();
        String dbSource = ParamUtil.get(request, "dbSource");
        String formCode = ParamUtil.get(request, "formCode");
        String moduleCode = ParamUtil.get(request, "moduleCode");
        String action = ParamUtil.get(request, "action");

        ModuleSetupDb msd = new ModuleSetupDb();

        boolean re = false;
        String sqlNew = "select * from visual_module_setup where code=?";
        JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
        ResultIterator ri = jt.executeQuery(sqlNew, new Object[]{moduleCode});
        if (ri.hasNext()) {
            ResultRecord rr = (ResultRecord) ri.next();

            if ("edit".equals(action)) {
				msd = msd.getModuleSetupDb(moduleCode);

				String sql = "update visual_module_setup set list_field=?,query_field=?,list_field_width=?,list_field_order=?,is_use=?,name=?,nav_tag_name=?,nav_tag_url=?,nav_tag_order=?,sub_nav_tag_name=?,sub_nav_tag_url=?,sub_nav_tag_order=?,btn_name=?,btn_script=?,btn_order=?,url_list=?,list_field_link=?,scripts=?,view_show=?,view_edit=?,filter=?,btn_bclass=?,is_workLog=?,msg_prop=?,validate_prop=?,validate_msg=?,view_list=?,field_begin_date=?,field_end_date=?,field_name=?,field_desc=?,field_label=?,scale_default=?,scale_min=?,scale_max=?,url_edit=?,orderby=?,sort=?,op_link_name=?,op_link_url=?,op_link_order=?,prompt_icon=?,prompt_field=?,prompt_cond=?,prompt_value=?,url_show=?,btn_edit_show=?,op_link_field=?,op_link_cond=?,op_link_value=?,op_link_event=?,btn_display_show=?,btn_add_show=?,description=?,btn_edit_display=?,btn_print_display=?,btn_role=?,btn_flow_show=?,btn_log_show=?,btn_del_show=?,op_link_role=?,cws_status=?,is_unit_show=?,unit_code=?,field_tree_show=?,field_tree_list=?,is_edit_inplace=?,module_code_log=?,add_to_url=?,list_field_show=?,is_auto_height=?,list_field_title=?,other_multi_ws=?,other_multi_order=?,prop_stat=?,export_word_view=?,module_tree_basic=?,module_tree_field_code=?,module_tree_field_name=?,page_setup=?,css=? where code=?";
				try {
					JdbcTemplate jdbcTemplate = new JdbcTemplate();
					re = jdbcTemplate.executeUpdate(sql, new Object[]{
							rr.getString("list_field"),
							rr.getString("query_field"),
							rr.getString("list_field_width"),
							rr.getString("list_field_order"),
							rr.getString("is_use"),
							rr.getString("name"),
							rr.getString("nav_tag_name"),
							rr.getString("nav_tag_url"),
							rr.getString("nav_tag_order"),
							rr.getString("sub_nav_tag_name"),
							rr.getString("sub_nav_tag_url"),
							rr.getString("sub_nav_tag_order"),
							rr.getString("btn_name"),
							rr.getString("btn_script"),
							rr.getString("btn_order"),
							rr.getString("url_list"),
							rr.getString("list_field_link"),
							rr.getString("scripts"),
							rr.getString("view_show"),
							rr.getString("view_edit"),
							rr.getString("filter"),
							rr.getString("btn_bclass"),
							rr.getString("is_workLog"),
							rr.getString("msg_prop"),
							rr.getString("validate_prop"),
							rr.getString("validate_msg"),
							rr.getString("view_list"),
							rr.getString("field_begin_date"),
							rr.getString("field_end_date"),
							rr.getString("field_name"),
							rr.getString("field_desc"),
							rr.getString("field_label"),
							rr.getString("scale_default"),
							rr.getString("scale_min"),
							rr.getString("scale_max"),
							rr.getString("url_edit"),
							rr.getString("orderby"),
							rr.getString("sort"),
							rr.getString("op_link_name"),
							rr.getString("op_link_url"),
							rr.getString("op_link_order"),
							rr.getString("prompt_icon"),
							rr.getString("prompt_field"),
							rr.getString("prompt_cond"),
							rr.getString("prompt_value"),
							rr.getString("url_show"),
							rr.getString("btn_edit_show"),
							rr.getString("op_link_field"),
							rr.getString("op_link_cond"),
							rr.getString("op_link_value"),
							rr.getString("op_link_event"),
							rr.getString("btn_display_show"),
							rr.getString("btn_add_show"),
							rr.getString("description"),
							rr.getString("btn_edit_display"),
							rr.getString("btn_print_display"),
							rr.getString("btn_role"),
							rr.getString("btn_flow_show"),
							rr.getString("btn_log_show"),
							rr.getString("btn_del_show"),
							rr.getString("op_link_role"),
							rr.getString("cws_status"),
							rr.getString("is_unit_show"),
							rr.getString("unit_code"),
							rr.getString("field_tree_show"),
							rr.getString("field_tree_list"),
							rr.getString("is_edit_inplace"),
							rr.getString("module_code_log"),
							rr.getString("add_to_url"),
							rr.getString("list_field_show"),
							rr.getString("is_auto_height"),
							rr.getString("list_field_title"),
							rr.getString("other_multi_ws"),
							rr.getString("other_multi_order"),
							rr.getString("prop_stat"),
							rr.getString("export_word_view"),
							rr.getString("module_tree_basic"),
							rr.getString("module_tree_field_code"),
							rr.getString("module_tree_field_name"),
							rr.getString("page_setup"),
							rr.getString("css"),
							moduleCode
					}) == 1;
					if (re) {
						PrimaryKey pk = msd.getPrimaryKey();
						msd.refreshSave(pk);
					}
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
            else if ("create".equals(action)) {
            	String sql = "insert into visual_module_setup (form_code,list_field,query_field,list_field_width,list_field_order,is_use,name,nav_tag_name,nav_tag_url,nav_tag_order,sub_nav_tag_name,sub_nav_tag_url,sub_nav_tag_order,btn_name,btn_script,btn_order,url_list,list_field_link,scripts,view_show,view_edit,filter,btn_bclass,is_workLog,msg_prop,validate_prop,validate_msg,view_list,field_begin_date,field_end_date,field_name,field_desc,field_label,scale_default,scale_min,scale_max,url_edit,orderby,sort,op_link_name,op_link_url,op_link_order,prompt_icon,prompt_field,prompt_cond,prompt_value,url_show,btn_edit_show,op_link_field,op_link_cond,op_link_value,op_link_event,btn_display_show,btn_add_show,description,btn_edit_display,btn_print_display,btn_role,btn_flow_show,btn_log_show,btn_del_show,op_link_role,cws_status,is_unit_show,unit_code,field_tree_show,field_tree_list,is_edit_inplace,module_code_log,add_to_url,list_field_show,is_auto_height,list_field_title,other_multi_ws,other_multi_order,code,prop_stat,export_word_view,module_tree_basic,module_tree_field_code,module_tree_field_name,page_setup,css) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				try {
					JdbcTemplate jdbcTemplate = new JdbcTemplate();
					re = jdbcTemplate.executeUpdate(sql, new Object[]{
							rr.getString("form_code"),
							rr.getString("list_field"),
							rr.getString("query_field"),
							rr.getString("list_field_width"),
							rr.getString("list_field_order"),
							rr.getString("is_use"),
							rr.getString("name"),
							rr.getString("nav_tag_name"),
							rr.getString("nav_tag_url"),
							rr.getString("nav_tag_order"),
							rr.getString("sub_nav_tag_name"),
							rr.getString("sub_nav_tag_url"),
							rr.getString("sub_nav_tag_order"),
							rr.getString("btn_name"),
							rr.getString("btn_script"),
							rr.getString("btn_order"),
							rr.getString("url_list"),
							rr.getString("list_field_link"),
							rr.getString("scripts"),
							rr.getString("view_show"),
							rr.getString("view_edit"),
							rr.getString("filter"),
							rr.getString("btn_bclass"),
							rr.getString("is_workLog"),
							rr.getString("msg_prop"),
							rr.getString("validate_prop"),
							rr.getString("validate_msg"),
							rr.getString("view_list"),
							rr.getString("field_begin_date"),
							rr.getString("field_end_date"),
							rr.getString("field_name"),
							rr.getString("field_desc"),
							rr.getString("field_label"),
							rr.getString("scale_default"),
							rr.getString("scale_min"),
							rr.getString("scale_max"),
							rr.getString("url_edit"),
							rr.getString("orderby"),
							rr.getString("sort"),
							rr.getString("op_link_name"),
							rr.getString("op_link_url"),
							rr.getString("op_link_order"),
							rr.getString("prompt_icon"),
							rr.getString("prompt_field"),
							rr.getString("prompt_cond"),
							rr.getString("prompt_value"),
							rr.getString("url_show"),
							rr.getString("btn_edit_show"),
							rr.getString("op_link_field"),
							rr.getString("op_link_cond"),
							rr.getString("op_link_value"),
							rr.getString("op_link_event"),
							rr.getString("btn_display_show"),
							rr.getString("btn_add_show"),
							rr.getString("description"),
							rr.getString("btn_edit_display"),
							rr.getString("btn_print_display"),
							rr.getString("btn_role"),
							rr.getString("btn_flow_show"),
							rr.getString("btn_log_show"),
							rr.getString("btn_del_show"),
							rr.getString("op_link_role"),
							rr.getString("cws_status"),
							rr.getString("is_unit_show"),
							rr.getString("unit_code"),
							rr.getString("field_tree_show"),
							rr.getString("field_tree_list"),
							rr.getString("is_edit_inplace"),
							rr.getString("module_code_log"),
							rr.getString("add_to_url"),
							rr.getString("list_field_show"),
							rr.getString("is_auto_height"),
							rr.getString("list_field_title"),
							rr.getString("other_multi_ws"),
							rr.getString("other_multi_order"),
							moduleCode,
							rr.getString("prop_stat"),
							rr.getString("export_word_view"),
							rr.getString("module_tree_basic"),
							rr.getString("module_tree_field_code"),
							rr.getString("module_tree_field_name"),
							rr.getString("page_setup"),
							rr.getString("css")
					}) == 1;
					if (re) {
						msd.refreshCreate();
					}
				} catch (SQLException throwables) {
					throwables.printStackTrace();
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
    <title>通过数据库升级模块</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="index.css" rel="stylesheet" type="text/css">
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>

    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
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
<%
	String dbSource = ParamUtil.get(request, "dbSource");
	String formCode = ParamUtil.get(request, "formCode");
	if ("".equals(dbSource) || "".equals(formCode)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
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
        String sqlNew = "select * from visual_module_setup where form_code=? order by code asc";
        JdbcTemplate jt = new JdbcTemplate(new Connection(dbSource));
        ResultIterator ri = jt.executeQuery(sqlNew, new Object[]{formCode});

        ModuleSetupDb msd = new ModuleSetupDb();
        String sql = msd.getTable().getSql("listForForm") + StrUtil.sqlstr(formCode);
        sql += " order by kind asc, name asc";

        Vector v = msd.list(sql);

        while (ri.hasNext()) {
            ResultRecord rr = (ResultRecord) ri.next();
            String codeNew = "", nameNew = "", name = "";
            boolean isFound = false;
            codeNew = rr.getString("code");
            nameNew = rr.getString("name");
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                msd = (ModuleSetupDb) ir.next();
                if (msd.getCode().equals(codeNew)) {
                    name = msd.getName();
                    isFound = true;
                    break;
                }
            }
    %>
    <tr>
        <td><%=codeNew%>
        </td>
        <td><%=nameNew%>
        </td>
        <td>
            <%
                if (isFound) {
                    out.print(name);
                }
            %>
        </td>
        <td>
            <%
                if (!isFound) {
            %>
			<button class="btn btn-default" onclick="sync('<%=formCode%>', '<%=codeNew%>', '<%=nameNew%>', 'create')">创建</button>
			<%
            } else {
            %>
            <button class="btn btn-default" onclick="sync('<%=formCode%>', '<%=msd.getCode()%>', '<%=nameNew%>', 'edit')">同步</button>
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
    function sync(formCode, moduleCode, name, action) {
        jConfirm('您确定要同步"' + name + '"么？', '提示', function (r) {
            if (!r) {
                return;
            }

            $.ajax({
                type: "post",
                url: "upgrade_module_by_db.jsp",
                data: {
                    op: "syncModule",
                    formCode: formCode,
                    moduleCode: moduleCode,
                    dbSource: "<%=dbSource%>",
					action: action
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
