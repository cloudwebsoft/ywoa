package com.cloudweb.oa.service;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAO;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class DataDictService {
    public static String getDataType(int dataType) {
        String type = "varchar";
        if (dataType == java.sql.Types.VARCHAR) {
            type = "varchar";
        } else if (dataType == java.sql.Types.BOOLEAN) {
            type = "boolean";
        } else if (dataType == java.sql.Types.TIMESTAMP) {
            type = "timestamp";
        } else if (dataType == java.sql.Types.DATE) {
            type = "date";
        } else if (dataType == java.sql.Types.LONGVARCHAR) { // text类型
            type = "longvarchar";
        } else if (dataType == java.sql.Types.TINYINT) {
            type = "tinyint";
        } else if (dataType == java.sql.Types.INTEGER) {
            type = "int";
        } else if (dataType == java.sql.Types.BIT) {
            type = "boolean";
        } else if (dataType == java.sql.Types.BIGINT) {
            type = "bigint";
        } else if (dataType == java.sql.Types.DECIMAL) {
            type = "float";
        } else if (dataType == java.sql.Types.CHAR) {
            type = "char";
        } else if (dataType == java.sql.Types.REAL) {
            type = "float";
        } else if (dataType == java.sql.Types.DOUBLE) {
            type = "double";
        }
        return type;
    }

    public String synTable(String tableName) {
        JSONObject json = new JSONObject();
        boolean re = false;
        boolean isTableNew = false;

        String formCodeTable = "data_dict_table";
        FormDb fdTable = new FormDb();
        fdTable = fdTable.getFormDb(formCodeTable);
        String formCodeColumn = "data_dict_column";
        FormDb fdCol = new FormDb();
        fdCol = fdCol.getFormDb(formCodeColumn);

        // 如果为表单型的表，则取得表名
        String tableTitle = "";
        boolean isLog = false;
        boolean isForm = false;
        String formCode = "";
        FormDb fd = null;
        SelectMgr sm = new SelectMgr();

        if (tableName.toLowerCase().startsWith("ft_")) {
            formCode = tableName.substring("ft_".length());
            if (formCode.endsWith("_log")) {
                formCode = formCode.substring(0, formCode.length() - "_log".length());
                isLog = true;
            }
            fd = new FormDb();
            fd = fd.getFormDb(formCode);
            if (fd.isLoaded()) {
                isForm = true;
                tableTitle = fd.getName();
                if (isLog) {
                    tableTitle += "_日志";
                }
            }
        }

        Connection connection = new Connection(Global.getDefaultDB());
        JdbcTemplate jt = new JdbcTemplate();
        jt.setAutoClose(false);
        try {
            DatabaseMetaData dmd = connection.getCon().getMetaData();

            // 检查表是否存在
            String sql = "select id,title from ft_data_dict_table where name=?";
            ResultIterator ri = jt.executeQuery(sql, new Object[]{tableName});
            // 如果表存在，则同步
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                long tableId = rr.getLong(1);
                String tableTitleOld = rr.getString(2);
                // 如果与原有的表名不同，则更新表名
                if (!"".equals(tableTitle) && !tableTitle.equals(tableTitleOld)) {
                    FormDAO fdaoTab = new FormDAO();
                    fdaoTab = fdaoTab.getFormDAO(tableId, fdTable);
                    fdaoTab.setFieldValue("title", tableTitle);
                    fdaoTab.save();
                }

                Map<String, ResultRecord> map = new HashMap<String, ResultRecord>();
                sql = "select name,title from ft_data_dict_column where cws_id='" + tableId + "'";
                ResultIterator riCol = jt.executeQuery(sql);
                while (riCol.hasNext()) {
                    ResultRecord rrCol = riCol.next();
                    map.put(rrCol.getString(1).toLowerCase(), rrCol);
                }

                FormDAO fdao = new FormDAO(fdCol);
                ResultSet rs = dmd.getColumns(connection.getCon().getCatalog(), connection.getCon().getSchema(), tableName, null);
                while (rs.next()) {
                    String columnName = rs.getObject(4).toString().toLowerCase();
                    String def = StrUtil.getNullStr(rs.getString("COLUMN_DEF")); // 默认值
                    String type = DataDictService.getDataType(rs.getInt("DATA_TYPE"));
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    int nullable = rs.getInt("NULLABLE");
                    int isAutoincrement = "YES".equals(rs.getString("IS_AUTOINCREMENT")) ? 1 : 0;
                    String remarks = rs.getString("REMARKS");
                    String title = "";

                    ResultRecord rrCol = map.get(columnName);
                    if (isForm) {
                        title = remarks;
                        remarks = "";

                        String t = getSysColumnTitle(columnName);
                        if (!"".equals(t)) {
                            title = t;
                        }
                        else {
                            FormField ff = fd.getFormField(columnName);
                            if (ff == null) {
                                DebugUtil.e(getClass(), "表", tableName + "中字段:" + columnName + " 在表单中不存在");
                                continue;
                            }
                            if (ff.getType().equals(FormField.TYPE_MACRO) && "macro_flow_select".equals(ff.getMacroType())) {
                                String basicCode = ff.getDefaultValueRaw();
                                if (StringUtils.isEmpty(basicCode)) {
                                    basicCode = ff.getDescription();
                                }
                                SelectDb sd = sm.getSelect(basicCode);
                                Vector<SelectOptionDb> v = sd.getOptions(new JdbcTemplate());
                                for (SelectOptionDb sod : v) {
                                    if (!sod.isOpen()) {
                                        continue;
                                    }
                                    if ("".equals(remarks)) {
                                        remarks = sod.getValue() + "|" + sod.getName();
                                    } else {
                                        remarks += "," + sod.getValue() + "|" + sod.getName();
                                    }
                                }
                            }
                            else if (ff.getType().equals(FormField.TYPE_SELECT) || ff.getType().equals(FormField.TYPE_RADIO)) {
                                String[][] ary;
                                if (ff.getType().equals(FormField.TYPE_SELECT)) {
                                    ary = FormParser.getOptionsArrayOfSelect(fd, ff);
                                }
                                else {
                                    ary = FormParser.getOptionsArrayOfRadio(fd, ff);
                                }
                                if (ary != null) {
                                    for (String[] pairs : ary) {
                                        if ("".equals(remarks)) {
                                            remarks = pairs[1];
                                        }
                                        else {
                                            remarks += "," + pairs[1];
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else {
                        if (rrCol!=null) {
                            title = rrCol.getString("title");
                        }
                    }

                    if (rrCol==null) {
                        // 字段在数据字典中不存在则创建
                        fdao.setFieldValue("name", columnName);
                        fdao.setFieldValue("title", title);
                        fdao.setFieldValue("def", def); // 默认值
                        fdao.setFieldValue("data_type", type);
                        fdao.setFieldValue("len", String.valueOf(columnSize));
                        fdao.setFieldValue("nullable", String.valueOf(nullable));
                        fdao.setFieldValue("is_autoincrement", String.valueOf(isAutoincrement));
                        fdao.setFieldValue("remarks", remarks);
                        fdao.setCwsId(String.valueOf(tableId));
                        fdao.create();
                    }
                    else {
                        // 已存在则同步
                        sql = "update ft_data_dict_column set title=?,def=?,data_type=?,len=?,nullable=?,is_autoincrement=?,remarks=? where name=? and cws_id=?";
                        re = jt.executeUpdate(sql, new Object[]{title, def, type, columnSize, nullable, isAutoincrement, remarks, columnName, String.valueOf(tableId)})==1;
                        map.remove(columnName); // 删除掉已处理的记录，剩余的就是待删除的字段
                    }
                }
                rs.close();

                // 从数据字典中删除已被删的字段
                Set<String> ks = map.keySet();
                for (String colName : ks) {
                    sql = "delete from ft_data_dict_column where name=? and cws_id=?";
                    re = jt.executeUpdate(sql, new Object[]{colName, String.valueOf(tableId)}) == 1;
                }

                re = true;
            }
            else {
                // 如果不存在，则创建
                isTableNew = true;

                // 创建数据字典中的表
                FormDAO fdao = new FormDAO(fdTable);
                fdao.setFieldValue("name", tableName);
                fdao.setFieldValue("title", tableTitle);
                fdao.create();
                long tableId = fdao.getId();

                fdao = new FormDAO(fdCol);
                ResultSet rs = dmd.getColumns(connection.getCon().getCatalog(), connection.getCon().getSchema(), tableName, null);
                while (rs.next()) {
                    String columnName = rs.getObject(4).toString().toLowerCase();
                    String def = StrUtil.getNullStr(rs.getString("COLUMN_DEF")); // 默认值
                    String type = DataDictService.getDataType(rs.getInt("DATA_TYPE"));
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    int nullable = rs.getInt("NULLABLE");
                    int isAutoincrement = "YES".equals(rs.getString("IS_AUTOINCREMENT"))?1:0;
                    String remarks = rs.getString("REMARKS");
                    String title = remarks;
                    if (isForm) {
                        remarks = "";

                        String t = getSysColumnTitle(columnName);
                        if (!"".equals(t)) {
                            title = t;
                        }
                        else {
                            FormField ff = fd.getFormField(columnName);
                            if (ff.getType().equals(FormField.TYPE_MACRO) && "macro_flow_select".equals(ff.getMacroType())) {
                                String basicCode = ff.getDefaultValueRaw();
                                if (StringUtils.isEmpty(basicCode)) {
                                    basicCode = ff.getDescription();
                                }
                                SelectDb sd = sm.getSelect(basicCode);
                                Vector<SelectOptionDb> v = sd.getOptions(new JdbcTemplate());
                                for (SelectOptionDb sod : v) {
                                    if (!sod.isOpen()) {
                                        continue;
                                    }
                                    if ("".equals(remarks)) {
                                        remarks = sod.getValue() + "|" + sod.getName();
                                    } else {
                                        remarks += "," + sod.getValue() + "|" + sod.getName();
                                    }
                                }
                            }
                            else if (ff.getType().equals(FormField.TYPE_SELECT) || ff.getType().equals(FormField.TYPE_RADIO)) {
                                String[][] ary;
                                if (ff.getType().equals(FormField.TYPE_SELECT)) {
                                    ary = FormParser.getOptionsArrayOfSelect(fd, ff);
                                }
                                else {
                                    ary = FormParser.getOptionsArrayOfRadio(fd, ff);
                                }
                                if (ary != null) {
                                    for (String[] pairs : ary) {
                                        if ("".equals(remarks)) {
                                            remarks = pairs[1];
                                        }
                                        else {
                                            remarks += "," + pairs[1];
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 创建数据字典中的字段
                    fdao.setFieldValue("name", columnName);
                    fdao.setFieldValue("title", title);
                    fdao.setFieldValue("def", def);
                    fdao.setFieldValue("data_type", type);
                    fdao.setFieldValue("len", String.valueOf(columnSize));
                    fdao.setFieldValue("nullable", String.valueOf(nullable));
                    fdao.setFieldValue("is_autoincrement", String.valueOf(isAutoincrement));
                    fdao.setFieldValue("remarks", remarks);
                    fdao.setCwsId(String.valueOf(tableId));
                    fdao.create();
                }
                rs.close();

                re = true;
            }
        } catch (SQLException | ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            jt.close();
            connection.close();
        }

        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
                json.put("isTableNew", isTableNew);
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    public String getSysColumnTitle(String columnName) {
        String title = "";
        if ("id".equals(columnName)) {
            title = "ID";
        }
        else if ("flowid".equals(columnName)) {
            title = "流程ID";
        }
        else if ("flowtypecode".equals(columnName)) {
            title = "流程类型";
        }
        else if ("cws_log_user".equals(columnName)) {
            title = "操作用户";
        }
        else if ("cws_log_type".equals(columnName)) {
            title = "操作类型";
        }
        else if ("cws_log_date".equals(columnName)) {
            title = "操作日期";
        }
        else if ("cws_log_id".equals(columnName)) {
            title = "记录ID";
        }
        else if ("cws_creator".equals(columnName)) {
            title = "创建者";
        }
        else if ("cws_id".equals(columnName)) {
            title = "关联记录的ID";
        }
        else if ("cws_order".equals(columnName)) {
            title = "排序号";
        }
        else if ("unit_code".equals(columnName)) {
            title = "单位编码";
        }
        else if ("cws_parent_form".equals(columnName)) {
            title = "父表单编码";
        }
        else if ("cws_status".equals(columnName)) {
            title = "记录状态";
        }
        else if ("cws_finish_date".equals(columnName)) {
            title = "流程结束时间";
        }
        else if ("cws_modify_date".equals(columnName)) {
            title = "修改时间";
        }
        else if ("cws_create_date".equals(columnName)) {
            title = "创建时间";
        }
        else if ("cws_quote_id".equals(columnName)) {
            title = "引用记录的ID";
        }
        else if ("cws_quote_form".equals(columnName)) {
            title = "引用记录的表单编码";
        }
        else if ("cws_flag".equals(columnName)) {
            title = "冲抵状态";
        }
        else if ("cws_progress".equals(columnName)) {
            title = "进度";
        }
        return title;
    }
}
