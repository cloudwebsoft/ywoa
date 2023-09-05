package com.cloudweb.oa.controller;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.service.DataDictService;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.TransmitData;
import com.redmoon.oa.visual.FormDAO;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    DataDictService dataDictService;

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
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }
        boolean re = false;
        try {
            String sql = "update ft_data_dict_table set title=? where name=?";
            JdbcTemplate jt = new JdbcTemplate();
            re = jt.executeUpdate(sql, new Object[]{update_value, tableName})==1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
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
            LogUtil.getLog(getClass()).error(e);
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
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }
        boolean re = false;
        try {
            String sql = "select id from ft_data_dict_table where name=?";
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{tableName});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                long tableId = rr.getLong(1);
                sql = "update ft_data_dict_column set title=? where name=? and cws_id=?";
                re = jt.executeUpdate(sql, new Object[]{update_value, columnName, String.valueOf(tableId)})==1;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
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
            LogUtil.getLog(getClass()).error(e);
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
        return dataDictService.synTable(tableName);
    }

    @ResponseBody
    @RequestMapping(value = "/synAllTable", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String synAllTable(String dbSource) throws JSONException {
        JSONObject json = new JSONObject();

        if (dbSource==null || "".equals(dbSource)) {
            dbSource = Global.getDefaultDB();
        }

        TransmitData td = new TransmitData();
        ResultSet rsTable = null;
        Connection connection = new Connection(dbSource);
        try {
            // 为防止时间超长，可能被mysql自动关闭，故取至list中
            rsTable = td.getTableNames(connection.getCon());
            List<String> list = new ArrayList<String>();
            while (rsTable.next()) {
                String tableName = rsTable.getObject(3).toString();
                list.add(tableName);
            }

            for (String tableName : list) {
                DebugUtil.i(getClass(), "synAllTable", tableName);
                dataDictService.synTable(tableName);
            }
        } catch (Exception e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            LogUtil.getLog(getClass()).error(e);
            return json.toString();
        }
        finally {
            if (rsTable!=null) {
                try {
                    rsTable.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            connection.close();
        }
        json.put("ret", 1);
        json.put("msg", "操作成功");
        return json.toString();
    }
}
