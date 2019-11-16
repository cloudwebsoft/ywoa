package com.redmoon.oa.flow.macroctl;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.db.SQLUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FormUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description: 通过SQL语句生成字符串或下拉菜单
 * SQL语句的格式[db]sql，其中db为数据库连接的名称，在config_cws.xml中定义 系统预定义了下列替换符 $starter 表示发起人
 * $tableName 表示表单的编码 {$表单域的编码或者名称} 表示表单域的值 $curDate 表示本日 $firstDayOfCurMonth
 * 表示本月第一天 $firstDayOfCurWeek 表示本周第一天 $firstDayOfCurYear 表示本年第一天
 * 如果SQL语句执行后的返回值为一个，则显示为字符串，如果有多个，则显示为下拉菜单 例： select id,fee from tableName
 * where user_name='$starter' and create_date=$curDate
 * 表示从表单中取出user_name为发起人，创建时间为当日的数据中的fee字段 如果仅有一条记录返回，则显示字段ID的值
 * 如果有一条以上的记录返回，则显示为下拉菜单，下拉菜单中的value为id，text为fee
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class SQLCtl extends AbstractMacroCtl {
    HttpServletRequest request;

    public SQLCtl() {
    }

    /**
     * 取出默认值中的sql语句
     *
     * @param defaultValue
     * @return
     */
    public static String[] getSql(String defaultValue) {
        try {
            String temp = URLDecoder.decode(defaultValue, "utf-8");
            defaultValue = temp;
        } catch (UnsupportedEncodingException e) {
            LogUtil.getLog(SQLCtl.class).error("SQL解析失败");
        }
        String[] ary = new String[2];
        String sql = defaultValue;
        Pattern pat = Pattern.compile("(\\[([A-Za-z0-9-_]+)\\])?(.*+)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        String db = "";
        Matcher mat = pat.matcher(defaultValue);
        if (mat.find()) {
            db = mat.group(2);
            sql = mat.group(3);
        }
        ary[0] = sql;
        ary[1] = db;
        return ary;
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        this.request = request;
        int flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);
        if (flowId==-1) {
            flowId = ParamUtil.getInt(request, "flowId", -1); // 来自于module_list_sel.jsp
        }
        String pageType = (String) request.getAttribute("pageType");
        if (pageType==null) {
            pageType = ParamUtil.get(request, "pageType"); // 来自于module_list_sel.jsp
        }

        String str = "";
        if (request.getAttribute("isSQLCtlJS_" + ff.getName()) == null) {
            str += "<script src='" + request.getContextPath()
                    + "/flow/macro/macro_sql_ctl_js.jsp?pageType=" + pageType
                    + "&formCode=" + StrUtil.UrlEncode(ff.getFormCode())
                    + "&fieldName=" + ff.getName() + "&flowId=" + flowId + "&isHidden=" + ff.isHidden() + "&editable=" + ff.isEditable()
                    + "'></script>\n";
            request.setAttribute("isSQLCtlJS_" + ff.getName(), "y");
        }

        str += "<span id='" + ff.getName() + "_box'><input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + StrUtil.getNullStr(ff.getValue()) + "' /></span>";
        return str;
    }

    public String convertToHTMLCtlXXX(HttpServletRequest request, FormField ff) {
        // SQL的格式
        String[] ary;
        String desc = StrUtil.getNullStr(ff.getDescription());
        if ("".equals(desc)) {
            ary = getSql(ff.getDefaultValue());
        } else {
            ary = getSql(desc);
        }
        String sql = ary[0];
        String db = ary[1];

        // 用表单中的值替换sql中的条件

        // Vector fields = new Vector();
        sql = parseAndSetValue(request, sql);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(ff.getFormCode());

        int flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);
        String pageType = (String) request.getAttribute("pageType");
        if ("show".equals(pageType)) {
            int visualId = flowId;
            com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
            fdao = fdao.getFormDAO(visualId, fd);

            sql = FormUtil.parseAndSetFieldValue(sql, fdao);
        } else {
            if (flowId == -1) {
                LogUtil.getLog(getClass()).info("flowId=" + flowId);
                LogUtil.getLog(getClass()).info("request.getRequestURL()=" + request.getRequestURL());
                // 用于nest_sheet_add_relate.jsp、nest_sheet_edit_relate.jsp页面
                if (request.getRequestURL().indexOf("_relate") != -1) {
                    flowId = ParamUtil.getInt(request, "flowId", -1);
                    LogUtil.getLog(getClass()).info("flowId2=" + flowId);
                    String formCode = ParamUtil.get(request, "formCode"); // 父表单的编码
                    fd = fd.getFormDb(formCode);
                    LogUtil.getLog(getClass()).info("formCode=" + formCode);

                    LogUtil.getLog(getClass()).info("sql=" + sql);

                    FormDAO fdao = new FormDAO();
                    fdao = fdao.getFormDAO(flowId, fd);
                    // 在父表单中找，从数据库中取字段
                    String sqlResult = FormUtil.parseAndSetFieldValue(sql, fdao);
                    // 如果在父表单中没有找到，则在嵌套表单中找字段
                    if (sqlResult.equals(FormUtil.ERROR_PARSE)) {
                        String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
                        fd = fd.getFormDb(formCodeRelated);
                        sql = parseAndSetFieldValue(request, flowId, fd, sql);
                    } else {
                        sql = sqlResult;
                    }

                    LogUtil.getLog(getClass()).info("sql2=" + sql);
                }
            } else {
                FormDAO fdao = new FormDAO();
                fdao = fdao.getFormDAO(flowId, fd);
                // 其实这里的解析与否已不重要，因为在 macro_sql_ctl_js.jsp中的onchange事件会再次ajax取值
                sql = FormUtil.parseAndSetFieldValue(sql, fdao);
            }

        }

        // 替换starter、tableName
        if (sql.indexOf("$starter") != -1) {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            sql = sql.replaceAll("\\$starter", wf.getUserName());
        }
        if (sql.indexOf("$tableName") != -1) {
            sql = sql.replaceAll("\\$tableName", FormDb.getTableName(fd
                    .getCode()));
        }

        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        sql = SQLUtil.change(sql, userName);

        ResultIterator ri = null;
        if (sql.equals(FormUtil.ERROR_PARSE)) {
            return "<input id='" + ff.getName() + "' name='"
                    + ff.getName() + "' value='' readonly />";
        }
        try {
            if (db != null && !"".equals(db)) {
                Connection conn = new Connection(db);
                if (conn.getCon() == null) {
                    return "数据库连接[" + db + "]不存在";
                } else {
                    JdbcTemplate jt = new JdbcTemplate(conn);
                    ri = jt.executeQuery(sql);
                }
            } else {
                JdbcTemplate jt = new JdbcTemplate();
                ri = jt.executeQuery(sql);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            return "";
        }

        String str = "";
        if (request.getAttribute("isSQLCtlJS_" + ff.getName()) == null) {
            str += "<script src='" + request.getContextPath()
                    + "/flow/macro/macro_sql_ctl_js.jsp?pageType=" + pageType
                    + "&formCode=" + StrUtil.UrlEncode(ff.getFormCode())
                    + "&fieldName=" + ff.getName() + "&flowId=" + flowId
                    + "'></script>\n";
            request.setAttribute("isSQLCtlJS_" + ff.getName(), "y");
        }

        if (ri.size() == 0) {
            return str + "<span id='" + ff.getName() + "'></span>";
        }

        // 如果返回值只有一行一列
        if (ri.size() == 1) {
            ResultRecord rr = (ResultRecord) ri.next();
            if (rr.getRow().size() == 1) { // 只有一列
                return str + "<input id='" + ff.getName() + "' name='"
                        + ff.getName() + "' value='" + rr.getString(1)
                        + "' readonly />";
            } else {
                ri.beforeFirst();
            }
        }
        String opts = "";

        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        while (ri.hasNext()) {
            ResultRecord rr = (ResultRecord) ri.next();
            if (rr.getRow().size() == 1) {
                opts += "<option value=\"" + rr.getString(1) + "\">"
                        + rr.getString(1) + "</option>";
            } else {
                opts += "<option value=\"" + rr.getString(1) + "\">"
                        + rr.getString(2) + "</option>";
            }

            LogUtil.getLog(getClass()).info(
                    "convertToHTMLCtl:" + rr.getString(1));

        }
        str += opts;
        str += "</select>";

        return str;
    }

    public static String parseAndSetFieldValueFromDAO(IFormDAO fdao, String strWithFields) {
        if (fdao == null) {
            return strWithFields;
        }
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        FormDb fd = fdao.getFormDb();
        Matcher m = p.matcher(strWithFields);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);
            String val = "";
            if (fieldTitle.equalsIgnoreCase("ID")) { // 仅支持convertToHtml时调用ID，因为增加记录时，ID为空
                val = String.valueOf(fdao.getId());
            } else {
                String fieldName = fieldTitle;
                // 制作大亚表单时发现，差旅费报销单中字段名称会有重复，所以这里先找编码，不行再找名称，防止名称重复
                FormField field = fd.getFormField(fieldTitle);
                if (field == null) {
                    field = fd.getFormFieldByTitle(fieldTitle);
                    if (field == null) {
                        LogUtil.getLog(SQLCtl.class).error(
                                "表单：" + fd.getName() + "，脚本：" + strWithFields
                                        + "中，字段：" + fieldTitle + " 不存在！");
                        // continue;
                    } else {
                        fieldName = field.getName();
                    }
                }
                val = StrUtil.getNullStr(fdao.getFieldValue(fieldName));
            }
            m.appendReplacement(sb, val);
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * 用于当与其它表单域联动时，从request中取值
     * @param request
     * @param fd
     * @param strWithFields
     * @return
     */
    public static String parseAndSetFieldValue(HttpServletRequest request, int flowId,
                                               FormDb fd, String strWithFields) {
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(strWithFields);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);

            // SQL语句中可能会出现{$id}，但此方式仅能用于parseAndSetFieldValueFromDAO中，以便于在列表中提取相关字段值
            // 此处仅从request中获取SQL条件中相关的字段值，故SQLCtl需具备仅用于列表的模式@task？？？

            String fieldName = fieldTitle;
            // 制作大亚表单时发现，差旅费报销单中字段名称会有重复，所以这里先找编码，不行再找名称，防止名称重复
            FormField field = fd.getFormField(fieldTitle);
            if (field == null) {
                field = fd.getFormFieldByTitle(fieldTitle);
                if (field == null) {
                    LogUtil.getLog(SQLCtl.class).error(
                            "表单：" + fd.getName() + "，脚本：" + strWithFields
                                    + "中，字段：" + fieldTitle + " 不存在！");
                    // continue;
                } else {
                    fieldName = field.getName();
                }
            }
            // System.out.println(getClass() + " scriptStr=" + scriptStr +
            // " fieldTitle=" + fieldTitle + " ff=" + ff);
            // String val = StrUtil.getNullStr(request.getParameter(field.getName()));
            String val = ParamUtil.get(request, fieldName);
            LogUtil.getLog(SQLCtl.class).info("val=" + val);
            // 表单域选择宏控件可能会取得下面的值，在替换时因为有%号，可能会报错
            // {formCode:clfsq, sourceFormCode:fbhtps, idField:id, showField:fbhtmc, filter:zbht %eq %lb$zbhtmc%rb, isParentSaveAndReload:true, maps:[{sourceField: fbdw, destField:dfdw},{sourceField: htzj, destField:htje},{sourceField: fbhtbh, destField:htbh}]}
            if (val.startsWith("{") && val.endsWith("}")) {
                val = "";
            }
            m.appendReplacement(sb, val);
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * 用于手机端获取控件
     *
     * @param request
     * @param flowId
     * @param ff
     * @return
     * @throws ErrMsgException
     * @Description:
     */
    public JSONObject getCtl(HttpServletRequest request, int flowId,
                             FormField ff) throws ErrMsgException {
        this.request = request;

        JSONObject field = new JSONObject();

        String[] ary;
        String desc = StrUtil.getNullStr(ff.getDescription());
        if ("".equals(desc)) {
            ary = getSql(ff.getDefaultValue());
        } else {
            ary = getSql(desc);
        }

        String sql = ary[0];
        String db = ary[1];

        FormDb fd = new FormDb();
        fd = fd.getFormDb(ff.getFormCode());
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(flowId, fd);

        sql = parseAndSetFieldValue(request, flowId, fd, sql);

        // 替换starter、tableName
        if (sql.indexOf("$starter") != -1) {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            sql = sql.replaceAll("\\$starter", wf.getUserName());
        }
        if (sql.indexOf("$tableName") != -1) {
            sql = sql.replaceAll("\\$tableName", FormDb.getTableName(fd
                    .getCode()));
        }

        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        sql = SQLUtil.change(sql, userName);

        JSONArray selects = new JSONArray();

        ResultIterator ri = null;
        try {
            if (db != null && !"".equals(db)) {
                Connection conn = new Connection(db);
                if (conn.getCon() == null) {
                    throw new ErrMsgException("数据库连接[" + db + "]不存在");
                } else {
                    JdbcTemplate jt = new JdbcTemplate(conn);
                    ri = jt.executeQuery(sql);
                }
            } else {
                JdbcTemplate jt = new JdbcTemplate();
                ri = jt.executeQuery(sql);
            }
        } catch (SQLException e) {
            LogUtil.getLog(SQLCtl.class).error(StrUtil.trace(e));
            throw new ErrMsgException(e.getMessage());
        }

        try {
            if (ri.size() == 0) {
                field.put("type", FormField.TYPE_TEXTFIELD);
                field.put("value", "");
            }

            // 如果返回值只有一行一列
            if (ri.size() == 1) {
                ResultRecord rr = (ResultRecord) ri.next();
                if (rr.getRow().size() == 1) { // 只有一列
                    field.put("type", FormField.TYPE_TEXTFIELD);
                    field.put("value", rr.getString(1));
                    field.put("text", rr.getString(1));
                } else {
                    field.put("type", FormField.TYPE_SELECT);
                    field.put("value", rr.getString(1));
                    field.put("text", rr.getString(2));
                }
            } else if (ri.size() > 1) {
                field.put("type", FormField.TYPE_SELECT);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    JSONObject option = new JSONObject();

                    if (rr.getRow().size() == 1) {
                        option.put("deptName", rr.getString(1));
                        option.put("deptCode", rr.getString(1));
                    } else {
                        option.put("deptName", rr.getString(2));
                        option.put("deptCode", rr.getString(1));
                    }
                    selects.put(option);
                }
            }

            field.put("title", ff.getTitle());
            field.put("code", ff.getName());
            field.put("desc", StrUtil.getNullStr(ff.getDescription()));
            field.put("options", selects);
            field.put("macroType", ff.getMacroType());
            field.put("editable", String.valueOf(ff.isEditable()));
            field.put("isHidden", String.valueOf(ff.isHidden()));
            field.put("isNull", String.valueOf(ff.isCanNull()));
            // field.put("fieldType", ff.getFieldType());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return field;
    }

    public ResultIterator getResultByDAO(IFormDAO fdao, FormField ff) {
        String[] ary;
        String desc = StrUtil.getNullStr(ff.getDescription());
        if ("".equals(desc)) {
            ary = getSql(ff.getDefaultValue());
        } else {
            ary = getSql(desc);
        }
        String sql = ary[0];
        String db = ary[1];

        FormDb fd = new FormDb();
        fd = fd.getFormDb(ff.getFormCode());
        if (fdao != null) {
            sql = parseAndSetFieldValueFromDAO(fdao, sql);
        }
        // 替换starter、tableName
        if (sql.indexOf("$starter") != -1) {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(fdao.getFlowId());
            sql = sql.replaceAll("\\$starter", wf.getUserName());
        }

        if (sql.indexOf("$tableName") != -1) {
            sql = sql.replaceAll("\\$tableName", FormDb.getTableName(fd
                    .getCode()));
        }

        if (request!=null) {
            Privilege pvg = new Privilege();
            String userName = pvg.getUser(request);
            sql = SQLUtil.change(sql, userName);
        }

        LogUtil.getLog(SQLCtl.class).info("getResultByDAO:" + sql);

        ResultIterator ri = null;
        try {
            if (db != null && !"".equals(db)) {
                Connection conn = new Connection(db);
                if (conn.getCon() == null) {
                    System.out.println(SQLCtl.class.getName() + " 数据库连接[" + db + "]不存在");
                    return null;
                } else {
                    JdbcTemplate jt = new JdbcTemplate(conn);
                    ri = jt.executeQuery(sql);
                }
            } else {
                JdbcTemplate jt = new JdbcTemplate();
                ri = jt.executeQuery(sql);
            }
        } catch (SQLException e) {
            LogUtil.getLog(SQLCtl.class).error(StrUtil.trace(e));
            return null;
        }
        return ri;
    }


    public static ResultIterator getResult(HttpServletRequest request, int flowId, FormField ff) {
        String[] ary;
        String desc = StrUtil.getNullStr(ff.getDescription());
        if ("".equals(desc)) {
            ary = getSql(ff.getDefaultValue());
        } else {
            ary = getSql(desc);
        }
        String sql = ary[0];
        String db = ary[1];

        FormDb fd = new FormDb();
        fd = fd.getFormDb(ff.getFormCode());

        sql = parseAndSetFieldValue(request, flowId, fd, sql);

        // 替换starter、tableName
        if (sql.indexOf("$starter") != -1) {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            sql = sql.replaceAll("\\$starter", wf.getUserName());
        }
        if (sql.indexOf("$tableName") != -1) {
            sql = sql.replaceAll("\\$tableName", FormDb.getTableName(fd
                    .getCode()));
        }

        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        sql = SQLUtil.change(sql, userName);

        LogUtil.getLog(SQLCtl.class).info("getResult:" + sql);

        ResultIterator ri = null;
        try {
            if (db != null && !"".equals(db)) {
                Connection conn = new Connection(db);
                if (conn.getCon() == null) {
                    System.out.println(SQLCtl.class.getName() + " 数据库连接[" + db + "]不存在");
                    return null;
                } else {
                    JdbcTemplate jt = new JdbcTemplate(conn);
                    ri = jt.executeQuery(sql);
                }
            } else {
                JdbcTemplate jt = new JdbcTemplate();
                ri = jt.executeQuery(sql);
            }
        } catch (SQLException e) {
            LogUtil.getLog(SQLCtl.class).error(StrUtil.trace(e));
            return null;
        }
        return ri;
    }


    /**
     * 当与其它表单域联动时，关联变换SQL控件的值
     *
     * @param request
     * @param flowId
     * @param ff
     * @return
     * @Description:
     */
    public static String getCtlHtml(HttpServletRequest request, int flowId, FormField ff) {
        // 20170323 fgf 将本控件的version改为2，启用description
        ResultIterator ri = getResult(request, flowId, ff);
        if (ri == null) {
            return "";
        }

        if (ri.size() == 0) {
            return "<span id='" + ff.getName() + "_box'><input id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' type='hidden' /></span>";
        }

        boolean isHidden = ParamUtil.getBoolean(request, "isHidden", false);
        boolean editable = ParamUtil.getBoolean(request, "editable", true);

        // 如果返回值只有一行一列
        if (ri.size() == 1) {
            ResultRecord rr = (ResultRecord) ri.next();
            if (rr.getRow().size() == 1) { // 只有一列
                String readonly = "";
                if (ff.isReadonly()) {
                    readonly = " class='readonly' readonly";
                }

                // 判断是否为数字型，如果是大数值，则进行format，以免显示为科学计数法
                // 当浮点型数据位数超过10位之后，数据变成科学计数法显示，可能为小数点前，也可能为小数点后
                String v = StrUtil.getNullStr(rr.getString(1));
                String regx = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";//科学计数法正则表达式
                Pattern pattern = Pattern.compile(regx);
                if (pattern.matcher(v).matches()) {
                    BigDecimal bd1 = new BigDecimal(v);
                    v = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
                }

                if (rr.getString(1) == null || "null".equals(rr.getString(1)) || "".equals(rr.getString(1))) {
                    readonly = " class='readonly' readonly type='hidden'";
                }
                return "<span id='" + ff.getName() + "_box' style='display:" + (isHidden ? "none" : "") + "'><input id='" + ff.getName() + "' name='" + ff.getName()
                        + "' title='" + ff.getTitle() + "' value='" + v + "' " + readonly + " /></span>";
            } else {
                ri.beforeFirst();
            }
        }
        String opts = "";
        String str = "";

        str += "<span id='" + ff.getName() + "_box' style='display:" + (isHidden ? "none" : "") + "'>";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' ";
        // System.out.println(SQLCtl.class.getName() + " editable=" + editable);
        if (!editable) {
            str += " style='background-color:#eeeeee' onfocus='this.defaultIndex=this.selectedIndex;' onchange='this.selectedIndex=this.defaultIndex;'";
        }
        str += ">";
        str += "<option value=''>无</option>";
        while (ri.hasNext()) {
            ResultRecord rr = (ResultRecord) ri.next();

            String v = StrUtil.getNullStr(rr.getString(1));
            String regx = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";//科学计数法正则表达式
            Pattern pattern = Pattern.compile(regx);
            if (pattern.matcher(v).matches()) {
                BigDecimal bd1 = new BigDecimal(v);
                v = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
            }

            if (rr.getRow().size() == 1) {
                opts += "<option value=\"" + v + "\">"
                        + v + "</option>";
            } else {
                opts += "<option value=\"" + v + "\">"
                        + rr.getString(2) + "</option>";
            }

            LogUtil.getLog(SQLCtl.class).info(
                    "convertToHTMLCtl:" + rr.getString(1));

        }
        str += opts;
        str += "</select></span>";
        return str;
    }

    /**
     * 取得用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     *
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        return super
                .getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    public String getSetCtlValueScript(HttpServletRequest request,
                                       IFormDAO IFormDao, FormField ff, String formElementId) {
        if (ff.getValue() == null || ff.getValue().equals("")) {
            return "";
        } else if (ff.getValue().equals(ff.getDefaultValue())) // 如果等于原来的SQL语句
            return "";
        else {
            // 判断是否为数字型，如果是大数值，则进行format，以免显示为科学计数法
            // 当浮点型数据位数超过10位之后，数据变成科学计数法显示，可能为小数点前，也可能为小数点后
            String v = StrUtil.getNullStr(ff.getValue());
            String regx = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";//科学计数法正则表达式
            Pattern pattern = Pattern.compile(regx);
            if (pattern.matcher(v).matches()) {
                BigDecimal bd1 = new BigDecimal(v);
                v = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
            }
            return "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '" + v + "');\n";
/*
			return super.getSetCtlValueScript(request, IFormDao, ff,
					formElementId);*/
        }
    }

    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 参数ff来自于数据库，当控件被禁用时，可以根据数据库的值来置被禁用的控件的显示值及需要保存的隐藏type=hidden的值
        // 数据库中没有数据时，当前用户的值将被置为空，否则将被显示为用户的真实姓名，由此实现当前用户宏控件当被禁用时，不会被解析为当前用户
        // 且如果已被置为某个用户，则保持其值不变
        String v = StrUtil.getNullStr(ff.getValue());
        if (!v.equals("")) {
            if (ff.getValue().equals(ff.getDefaultValueRaw())) {
                v = "";
            }
        }

        v = v.replaceAll("\'", "&#039;");

        // 判断是否为数字型，如果是大数值，则进行format，以免显示为科学计数法
        // 当浮点型数据位数超过10位之后，数据变成科学计数法显示，可能为小数点前，也可能为小数点后
        String regx = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";//科学计数法正则表达式
        Pattern pattern = Pattern.compile(regx);
        if (pattern.matcher(v).matches()) {
            BigDecimal bd1 = new BigDecimal(v);
            v = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        }

        // String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType()
        // + "','" + v + "',\"" + v + "\");\n";
        // disable，使其为只读，这样就可以实现隐藏了（因为hide时，一定会被disable，而如果按上句disable，则无法隐藏），并且隐藏后仍可以ajax得到SQL的值
        // 如：日常报销中的月度剩余预算SQL控件，在发起节点后需显示于嵌套表的列表中，而在嵌套表格2不能添加和编辑时，开始节点隐藏，需要记录其值
        // SQL控件是个例外，在不可写时，将保存其值
        String str = "$('#" + ff.getName() + "').attr('readonly', 'readonly');\n";
        str += "$('#" + ff.getName() + "').val('" + v + "');\n";

        return str;
    }

    public String getReplaceCtlWithValueScript(IFormDAO fdao, FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")
                && !ff.getValue().equals(ff.getDefaultValueRaw())) {
            v = ff.getValue();

            if (v.startsWith("select%20")) {
                v = "";
            } else {
                ResultIterator ri = getResultByDAO(fdao, ff);
                // 如果返回值只有一行一列
                boolean isOnlyOneCol = false;
                if (ri.size() == 1) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    if (rr.getRow().size() == 1) { // 只有一列
                        v = StrUtil.getNullStr(rr.getString(1));
                        isOnlyOneCol = true;
                    } else {
                        ri.beforeFirst();
                    }
                }
                if (!isOnlyOneCol) {
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        if (rr.getString(1) != null && rr.getString(1).equals(v)) {
                            if (rr.getRow().size() > 1) { // 如果有多列，则取第2列的值
                                v = StrUtil.getNullStr(rr.getString(2));
                            }
							break;
                        }
                    }
                }
            }
        }
        // 判断是否为数字型，如果是大数值，则进行format，以免显示为科学计数法
        // 当浮点型数据位数超过10位之后，数据变成科学计数法显示，可能为小数点前，也可能为小数点后
        String regx = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";//科学计数法正则表达式
        Pattern pattern = Pattern.compile(regx);
        if (pattern.matcher(v).matches()) {
            BigDecimal bd1 = new BigDecimal(v);
            v = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        }
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
                + "','" + v + "');\n";
    }

    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        this.request = request;

        fieldValue = StrUtil.getNullStr(fieldValue);
        if (fieldValue.toLowerCase().startsWith("select")) {
            return "";
        }

        if (request == null) {
            return fieldValue;
        }

        if (fieldValue != null && !fieldValue.equals("")) {
            IFormDAO ifdao = RequestUtil.getFormDAO(request);

            ResultIterator ri = getResultByDAO(ifdao, ff);
            // 如果返回值只有一行一列
            if (ri!=null && ri.size() == 1) {
                ResultRecord rr = (ResultRecord) ri.next();
                if (rr.getRow().size() == 1) { // 只有一列
                    return rr.getString(1);
                } else {
                    ri.beforeFirst();
                }
            }

            while (ri!=null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                // System.out.println(rr.getString(1) + "--" + rr.getString(2) + "=" + fieldValue);
                if (rr.getString(1) != null && rr.getString(1).equals(fieldValue)) {
                    if (rr.getRow().size() == 1) {
                        fieldValue = rr.getString(1);
                    } else {
                        fieldValue = rr.getString(2);
                    }
                }
            }
        } else {
            // 如果数据为空，则从数据库中再取一次，如果返回值只有一行一列，则说明是系统需动态取某个值，比如取项目的N个投资方
            IFormDAO ifdao = RequestUtil.getFormDAO(request);
            ResultIterator ri = getResultByDAO(ifdao, ff);
            // 如果返回值只有一行一列
            if (ri!=null && ri.size() == 1) {
                ResultRecord rr = (ResultRecord) ri.next();
                if (rr.getRow().size() == 1) { // 只有一列
                    return rr.getString(1);
                }
            }
        }

        // 判断是否为数字型，如果是大数值，则进行format，以免显示为科学计数法
        // 当浮点型数据位数超过10位之后，数据变成科学计数法显示，可能为小数点前，也可能为小数点后
        String regx = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";//科学计数法正则表达式
        Pattern pattern = Pattern.compile(regx);
        if (pattern.matcher(fieldValue).matches()) {

            BigDecimal bd1 = new BigDecimal(fieldValue);
            fieldValue = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        }

        return fieldValue;
    }

    public String getControlType() {
        return "text";
    }

    public String getControlValue(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    public String getControlText(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    public Object getValueForCreate(int flowId, FormField ff) {
        // 2015-01-28 fgf 这里置为空字符串，以免在DisableCtl中因为sql语句中的'***'导致js出错
        return "";
    }

    public static String parseAndSetValue(HttpServletRequest request, String sql) {
        Pattern p = Pattern.compile(
                "\\{\\$(cwsMyDept)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);

            Privilege pvg = new Privilege();
            String userName = pvg.getUser(request);

            String myDeptCode = "";
            DeptUserDb udd = new DeptUserDb();
            Vector vdept = udd.getDeptsOfUser(userName);
            if (vdept != null && vdept.size() > 0) {
                myDeptCode = ((DeptDb) vdept.get(0)).getCode();
            }

            // System.out.println(getClass() + " scriptStr=" + scriptStr + " fieldTitle=" + fieldTitle + " ff=" + ff);
            m.appendReplacement(sb, myDeptCode);
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * 用于手机端
     *
     * @param ff
     * @return
     * @Description:
     */
    public String getMetaData(FormField ff) {
        String[] ary;
        String desc = StrUtil.getNullStr(ff.getDescription());
        if ("".equals(desc)) {
            ary = getSql(ff.getDefaultValue());
        } else {
            ary = getSql(desc);
        }

        String sql = ary[0];

        String formCode = ff.getFormCode();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);
            FormField field = fd.getFormField(fieldTitle);
            if (field == null) {
                field = fd.getFormFieldByTitle(fieldTitle);
                if (field != null) {
                    fieldTitle = field.getName();
                }
            }
            // if (field!=null) {
            StrUtil.concat(sb, ",", fieldTitle);
            // }
        }

        return sb.toString();
    }

    public String getHideCtlScript(FormField ff, String formElementId) {
        // 因为SQL控件是动态生成的，所以如果需隐藏，需隐藏其父节点，否则隐藏不了
        String str = FormField.getHideCtlScript(ff, formElementId);
        str += "\n$('#" + ff.getName() + "_box').hide();\n";
        return str;
    }

    /**
     * 取得根据名称（而不是值）查询时需用到的SQL语句，如果没有特定的SQL语句，则返回空字符串
     *
     * @param request
     * @param ff      当前被查询的字段
     * @param value
     * @param isBlur  是否模糊查询
     * @return
     */
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
        if (isBlur) {
            return "select " + ff.getName() + " from form_table_" + ff.getFormCode() + " where " + ff.getName() + " like " +
                    StrUtil.sqlstr("%" + value + "%");
        } else {
            return "select " + ff.getName() + " from form_table_" + ff.getFormCode() + " where " + ff.getName() + "=" + StrUtil.sqlstr(value);
        }
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        int flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);
        String pageType = (String) request.getAttribute("pageType");

        String str = "";
        if (request.getAttribute("isSQLCtlJS_" + ff.getName()) == null) {
            str += "<script src='" + request.getContextPath()
                    + "/flow/macro/macro_sql_ctl_js.jsp?pageType=" + pageType
                    + "&formCode=" + StrUtil.UrlEncode(ff.getFormCode())
                    + "&fieldName=" + ff.getName() + "&flowId=" + flowId + "&isHidden=" + ff.isHidden() + "&editable=" + ff.isEditable()
                    + "'></script>\n";
            request.setAttribute("isSQLCtlJS_" + ff.getName(), "y");
        }

        str += "<span id='" + ff.getName() + "_box'><input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + StrUtil.getNullStr(ff.getValue()) + "' /></span>";
        return str;
    }
}
