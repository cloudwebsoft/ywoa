package com.cloudweb.oa.controller;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.service.DataDictService;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.hr.SalaryMgr;
import com.redmoon.oa.visual.FormDAO;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/datadict")
public class DataDictController {

    /**
     * 编辑数据字典中的表格名称
     * @param tableName
     * @param original_value
     * @param update_value
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/editTableTitle", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String editTableTitle(String tableName, String original_value, String update_value) {
        JSONObject json = new JSONObject();
        if (update_value.equals(original_value)) {
            try {
                json.put("ret", "-1");
                json.put("msg", "值未更改！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = false;
        try {
            String sql = "update form_table_data_dict_table set title=? where name=?";
            JdbcTemplate jt = new JdbcTemplate();
            re = jt.executeUpdate(sql, new Object[]{update_value, tableName})==1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败，请先同步！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 编辑数据字典中的表格名称
     * @param tableName
     * @param original_value
     * @param update_value
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/editColumnTitle", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String editColumnTitle(String tableName, String columnName, String original_value, String update_value) {
        JSONObject json = new JSONObject();
        if (update_value.equals(original_value)) {
            try {
                json.put("ret", "-1");
                json.put("msg", "值未更改！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = false;
        try {
            String sql = "select id from form_table_data_dict_table where name=?";
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{tableName});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long tableId = rr.getLong(1);
                sql = "update form_table_data_dict_column set title=? where name=? and cws_id=?";
                re = jt.executeUpdate(sql, new Object[]{update_value, columnName, tableId})==1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败，请先同步！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 编辑数据字典中的表格名称
     * @param tableName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/synTable", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String synTable(String tableName) {
        JSONObject json = new JSONObject();
        boolean re = false;
        boolean isTableNew = false;
        JdbcTemplate jt = new JdbcTemplate();
        jt.setAutoClose(false);
        try {
            // 检查表是否存在
            String sql = "select id from form_table_data_dict_table where name=?";
            ResultIterator ri = jt.executeQuery(sql, new Object[]{tableName});
            // 如果表存在，则同步
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long tableId = rr.getLong(1);

                Map<String, ResultRecord> map = new HashMap<String, ResultRecord>();
                sql = "select name from form_table_data_dict_column where cws_id=" + tableId;
                ResultIterator riCol = jt.executeQuery(sql);
                while (riCol.hasNext()) {
                    ResultRecord rrCol = (ResultRecord)riCol.next();
                    map.put(rrCol.getString(1).toLowerCase(), rrCol);
                }

                String formCodeColumn = "data_dict_column";
                FormDb fdCol = new FormDb();
                fdCol = fdCol.getFormDb(formCodeColumn);
                FormDAO fdao = new FormDAO(fdCol);

                Connection conn = jt.getConnection();
                DatabaseMetaData dmd = conn.getCon().getMetaData();
                ResultSet rs = dmd.getColumns(null, null, tableName, null);
                while (rs.next()) {
                    String columnName = rs.getObject(4).toString().toLowerCase();
                    String def = StrUtil.getNullStr(rs.getString("COLUMN_DEF")); // 默认值
                    String type = DataDictService.getDataType(rs.getInt("DATA_TYPE"));
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    int nullable = rs.getInt("NULLABLE");
                    int isAutoincrement = rs.getString("IS_AUTOINCREMENT").equals("YES") ? 1 : 0;
                    String remarks = rs.getString("REMARKS");

                    ResultRecord rrCol = map.get(columnName);
                    if (rrCol==null) {
                        // 字段在数据字典中不存在则创建
                        fdao.setFieldValue("name", columnName);
                        fdao.setFieldValue("def", def);
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
                        sql = "update form_table_data_dict_column set def=?,data_type=?,len=?,nullable=?,is_autoincrement=? where name=? and cws_id=?";
                        re = jt.executeUpdate(sql, new Object[]{def, type, columnSize, nullable, isAutoincrement, columnName, tableId})==1;
                        map.remove(columnName); // 删除掉已处理的记录，剩余的就是待删除的字段
                    }
                }
                // 从数据字典中删除已被删的字段
                Set ks = map.keySet();
                Iterator ir = ks.iterator();
                while (ir.hasNext()) {
                    String colName = (String)ir.next();
                    sql = "delete from form_table_data_dict_column where name=? and cws_id=?";
                    re = jt.executeUpdate(sql, new Object[]{colName, tableId})==1;
                }

                re = true;
            }
            else {
                // 如果不存在，则创建
                isTableNew = true;
                String formCodeTable = "data_dict_table";
                FormDb fdTable = new FormDb();
                fdTable = fdTable.getFormDb(formCodeTable);
                String formCodeColumn = "data_dict_column";
                FormDb fdCol = new FormDb();
                fdCol = fdCol.getFormDb(formCodeColumn);
                // 创建数据字典中的表
                FormDAO fdao = new FormDAO(fdTable);
                fdao.setFieldValue("name", tableName);
                // fdao.setFieldValue("title", "");
                fdao.create();
                long tableId = fdao.getId();

                fdao = new FormDAO(fdCol);
                Connection conn = jt.getConnection();
                DatabaseMetaData dmd_new = conn.getCon().getMetaData();
                ResultSet rs = dmd_new.getColumns(null, null, tableName, null);
                while (rs.next()) {
                    String columnName = rs.getObject(4).toString().toLowerCase();
                    String def = StrUtil.getNullStr(rs.getString("COLUMN_DEF")); // 默认值
                    String type = DataDictService.getDataType(rs.getInt("DATA_TYPE"));
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    int nullable = rs.getInt("NULLABLE");
                    int isAutoincrement = rs.getString("IS_AUTOINCREMENT").equals("YES")?1:0;
                    String remarks = rs.getString("REMARKS");

                    // 创建数据字典中的字段
                    fdao.setFieldValue("name", columnName);
                    fdao.setFieldValue("def", def);
                    fdao.setFieldValue("data_type", type);
                    fdao.setFieldValue("len", String.valueOf(columnSize));
                    fdao.setFieldValue("nullable", String.valueOf(nullable));
                    fdao.setFieldValue("is_autoincrement", String.valueOf(isAutoincrement));
                    fdao.setFieldValue("remarks", remarks);
                    fdao.setCwsId(String.valueOf(tableId));
                    fdao.create();
                }
                re = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            jt.close();
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
            e.printStackTrace();
        }
        return json.toString();
    }
}
