<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.StrUtil,
                 cn.js.fan.util.XMLConfig,
                 com.redmoon.oa.kernel.License,
                 org.apache.commons.configuration.PropertiesConfiguration,
                 java.io.File,
                 java.io.FileInputStream,
                 java.net.URLDecoder,
                 java.net.URL"
%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%
    String database = "prj";
    // 添加cws_create_date cws_modify_date cws_finish_date抵字段
    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
    JdbcTemplate jt = new JdbcTemplate();
    ResultIterator ri = jt.executeQuery(sql);
    while (ri.hasNext()) {
        ResultRecord rr = (ResultRecord) ri.next();
        String tableName = rr.getString(1).toLowerCase();
        if (tableName.startsWith("ft_") && !tableName.endsWith("_log")) {
            // sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_create_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_modify_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_finish_date` datetime AFTER `cws_status`";
            // sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_create_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_modify_date` datetime AFTER `cws_status`";
            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_create_date` datetime AFTER `cws_status`";
            try {
                jt.executeUpdate(sql);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }
%>