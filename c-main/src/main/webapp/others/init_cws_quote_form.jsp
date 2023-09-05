<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="org.apache.commons.configuration.PropertiesConfiguration" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLDecoder" %>
<%
    // 在ft_***表中添加cws_quote_form字段
    URL confURL = getClass().getResource("/application.properties");
    String xmlpath = confURL.getFile();
    xmlpath = URLDecoder.decode(xmlpath);
    FileInputStream fis = new FileInputStream(xmlpath);

    // 须用load及save加utf-8参数的形式，否则会乱码
    PropertiesConfiguration conf = new PropertiesConfiguration();
    conf.load(fis, "utf-8");
    fis.close();

    String url = conf.getString("spring.datasource.url");
    int beginIndex = url.indexOf("//");
    int secondIndex = url.indexOf("/", beginIndex + 2);
    String database = url.substring(secondIndex + 1, url.lastIndexOf("?"));

    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
    JdbcTemplate jt = new JdbcTemplate();
    ResultIterator ri = jt.executeQuery(sql);
    while (ri.hasNext()) {
        ResultRecord rr = (ResultRecord) ri.next();
        String tableName = rr.getString(1).toLowerCase();
        if ((tableName.startsWith("ft_") && !tableName.endsWith("_log")) || "ft_log".equals(tableName)) {
            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_quote_form` varchar(20) AFTER `cws_status`";
            try {
                jt.executeUpdate(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
%>
<title>初始化cws_quote_form</title>
初始化cws_quote_form结束！