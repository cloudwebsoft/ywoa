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
<%
    /**
     * 初始化，将流程由控件所作的图升级为矢量图
     */
    IMyflowUtil iMyflowUtil = SpringUtil.getBean(IMyflowUtil.class);
    WorkflowPredefineDb workflowPredefineDb = new WorkflowPredefineDb();
    String sql = "select id from flow_predefined";
    Iterator ir = workflowPredefineDb.list(sql).iterator();
    while (ir.hasNext()) {
        workflowPredefineDb = (WorkflowPredefineDb)ir.next();
        /*if (!workflowPredefineDb.getTypeCode().equals("yyspd")) {
            continue;
        }*/
        // if (StringUtils.isEmpty(workflowPredefineDb.getFlowJson())) {
            String flowJson = iMyflowUtil.toMyflow(workflowPredefineDb.getFlowString());
            workflowPredefineDb.setFlowJson(flowJson);
            DebugUtil.i(getClass(), "flowJson", flowJson);
            workflowPredefineDb.save();
        // }
    }
%>
<title>升级流程图</title>
升级流程图结束！