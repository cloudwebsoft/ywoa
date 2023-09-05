<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.jdom.*" %>
<%@ page import="org.jdom.output.*" %>
<%@ page import="org.jdom.input.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.cloudweb.oa.service.IUserAuthorityService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.cloudweb.oa.api.IMyflowUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="java.util.Date" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="com.redmoon.oa.LogDb" %>
<%
    // 周一
    String sqlIn = "select count(id) from log where log_type=" + LogDb.TYPE_LOGIN + " and log_date>=? and log_date<?";
    String sqlOut = "select count(id) from log where log_type=" + LogDb.TYPE_LOGOUT + " and log_date>=? and log_date<?";
    JdbcTemplate jt = new JdbcTemplate();
    Date d = DateUtil.getDate(2023, 4, 22);
    for (int i=1; i<=5; i++) {
        for (int j=8; j<=22; j++) {
            // Date begin = DateUtil.addHour(d, j).getTime();
            Date end = DateUtil.addHour(d, j+1).getTime();
            ResultIterator ri = jt.executeQuery(sqlIn, new Object[]{d, end});
            int in = 0;
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                in = rr.getInt(1);
            }
            ri = jt.executeQuery(sqlOut, new Object[]{d, end});
            int o = 0;
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                o = rr.getInt(1);
            }
            int onlineCount = in - o;
            out.print(DateUtil.format(d, "yyyy-MM-dd") + " " + j + ":00-" + (j+1) + ":00 在线：" + onlineCount + "<br/>");
        }
        d = DateUtil.addDate(d, 1);
    }
%>
结束！