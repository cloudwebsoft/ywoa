<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.flow.Directory" %>
<%@ page import="com.redmoon.oa.flow.Leaf" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.cloudwebsoft.framework.db.DataSource" %>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb" %>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=Global.AppName%> - <%=Global.server%></title>
<style type="text/css">
<!--
body {
	margin-top: 0px;
	margin-bottom: 0px;
}
-->
</style>
<link href="css.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
body,td,th {
	font-size: 12px;
}
-->
</style>
<link href="index.css" rel="stylesheet" type="text/css">
</head>

<body>
<%
    JdbcTemplate jt = new JdbcTemplate(new DataSource("zjrj"));
    String sql = "select scripts from flow_predefined where typeCode=?";

    Directory dir = new Directory();
    Vector<Leaf> v = new Vector<>();
    Leaf lf = new Leaf();
    lf = lf.getLeaf(Leaf.CODE_ROOT);
    lf.getAllChild(v, lf);
    WorkflowPredefineDb workflowPredefineDb = new WorkflowPredefineDb();
    for (Leaf leaf : v) {
        String typeCode = leaf.getCode();
        ResultIterator ri = jt.executeQuery(sql, new Object[]{typeCode});
        if (ri.hasNext()) {
            ResultRecord rr = (ResultRecord)ri.next();
            String script = rr.getString(1);
            workflowPredefineDb = workflowPredefineDb.getDefaultPredefineFlow(typeCode);
            if (!workflowPredefineDb.getScripts().equals(script)) {
                out.print(leaf.getName() + "<br/>");
                workflowPredefineDb.setScripts(script);
                workflowPredefineDb.save();
            }
        }
    }
%>
</body>
</html>
