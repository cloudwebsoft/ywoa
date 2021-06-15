<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.cloudweb.oa.api.IMyflowUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>流程调试 - 节点列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        body {
            background-image: none !important;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/flow/myflow.css">
    <link type="text/css" href="../js/flow/lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" rel="stylesheet"/>
    <script type="text/javascript" src="../js/flow/lib/raphael-min.js"></script>
    <script type="text/javascript" src="../js/flow/lib/jquery-ui-1.8.4.custom/js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.min.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.jpdl.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.editors.js"></script>
    <script type="text/javascript" src="../js/crypto-js.min.js"></script>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");

    if (op.equals("test")) {
        long myActionId = ParamUtil.getLong(request, "myActionId");
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        String userName = mad.getUserName();
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        String mainPage = "../flow_dispose.jsp?myActionId=" + myActionId;
        // 置 session中的调试标志
        Privilege.setAttribute(request, Privilege.SESSION_OA_FLOW_TESTER, privilege.getUser(request));
        privilege.doLogin(request, user.getName(), user.getPwdMD5());
%>
<script>
    // window.top.location.href = "../oa.jsp?mainTitle=<%=StrUtil.UrlEncode("待办流程")%>&mainPage=<%=mainPage%>";
    window.top.location.href = "<%=mainPage%>";
</script>
<%
        //response.sendRedirect("../oa.jsp?mainTitle=" + StrUtil.UrlEncode("待办流程") + "&mainPage=" + mainPage);
        return;
    }

    long myActionId = ParamUtil.getLong(request, "myActionId");
    MyActionDb mad = new MyActionDb();
    mad = mad.getMyActionDb(myActionId);
    long actionId = mad.getActionId();
    WorkflowActionDb wad = new WorkflowActionDb();
    wad = wad.getWorkflowActionDb((int) actionId);

    WorkflowDb wf = new WorkflowDb();
    wf = wf.getWorkflowDb((int) mad.getFlowId());

    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String flowExpireUnit = cfg.get("flowExpireUnit");
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
    <TBODY>
    <TR>
        <TD class="tdStyle_1">流程调试</TD>
    </TR>
    </TBODY>
</TABLE>
<br>
<table width="100%" class="tabStyle_1 percent98" style="table-layout: fixed; height: 515px;" border="0" align="center" cellpadding="0" cellspacing="0">
    <colgroup>
        <col width="80%">
        <col width="20%">
    </colgroup>
    <tr>
        <td colspan="2" class="tabStyle_1_title">请选择节点</td>
    </tr>
    <tr>
        <td width="80%" style="width: 80%; padding: 0px">
            <table id="designerTable" width="100%" style="table-layout:fixed; height: 515px" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td align="center">
                        <%
                            boolean canUserSeeFlowChart = cfg.getBooleanProperty("canUserSeeFlowChart");
                            if (canUserSeeFlowChart) {
                                String flowJson;
                                if (StringUtils.isEmpty(wf.getFlowJson())) {
                                    IMyflowUtil myflowUtil = SpringUtil.getBean(IMyflowUtil.class);
                                    flowJson = myflowUtil.toMyflow(wf.getFlowString());
                                } else {
                                    flowJson = wf.getFlowJson();
                                }
                        %>
                        <div id="myflow" style="height: 515px; overflow-y: auto;"></div>
                        <textarea id="flowJson" style="display:none;"><%=flowJson%></textarea>
                        <%
                        } else {
                            boolean isOem = License.getInstance().isOem();
                            String codeBase = "";
                            if (!isOem) {
                                codeBase = "codebase=\"../activex/cloudym.CAB#version=1,3,0,0\"";
                            }
                        %>
                        <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" <%=codeBase%> style="width:0px; height:0px;">
                            <param name="Workflow" value="<%=wf.getFlowString()%>"/>
                            <param name="Mode" value="view"/>
                            <param name="CurrentUser" value="<%=privilege.getUser(request)%>"/>
                            <param name="ExpireUnit" value="<%=flowExpireUnit%>">
                            <%
                                com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
                            %>
                            <param name="Organization" value="<%=license.getCompany()%>"/>
                            <param name="Key" value="<%=license.getKey()%>"/>
                            <param name="LicenseType" value="<%=license.getType()%>"/>
                        </object>
                        <%
                            }
                        %>
                    </td>
                </tr>
            </table>
        </td>
        <td valign="top" style="width: 20%">
            <strong>下一节点</strong>：<BR/>
            <%
                UserDb user = new UserDb();
                Iterator ir = wad.getLinkToActions().iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb nextwad = (WorkflowActionDb) ir.next();
                    Iterator irmad = mad.getActionDoing(nextwad.getId()).iterator();
            %>
            <%=nextwad.getTitle()%>&nbsp;:&nbsp;
            <%
                while (irmad.hasNext()) {
                    MyActionDb nextmad = (MyActionDb) irmad.next();
            %>
            <a href="flow_list_debugger.jsp?op=test&myActionId=<%=nextmad.getId()%>"><%=user.getUserDb(nextmad.getUserName()).getRealName()%>
            </a>
            <%
                }
            %>
            <BR/>
            <%
                }
            %>
            <br/>
            <strong>全部待办节点</strong>&nbsp;：<br/>
            <%
                Iterator irmad = mad.getFlowDoingWithoutAction(mad.getFlowId()).iterator();
                while (irmad.hasNext()) {
                    MyActionDb nextmad = (MyActionDb) irmad.next();
                    WorkflowActionDb nextwad = wad.getWorkflowActionDb((int) nextmad.getActionId());
            %>
            <%=nextwad.getTitle()%>&nbsp;:&nbsp;
            <a href="flow_list_debugger.jsp?op=test&myActionId=<%=nextmad.getId()%>"><%=user.getUserDb(nextmad.getUserName()).getRealName()%>
            </a><br/>
            <%
                }
            %>
            <br/>
            <a target="_top" href="../index.do"><span style="font-family:'宋体'">>>&nbsp;</span>重新登录</a>
        </td>
    </tr>
</table>
</body>
<script>
    <%
        if (canUserSeeFlowChart) {
            // 取出激活节点和已办节点
            JSONArray activeActions = new JSONArray();
            JSONArray finishActions = new JSONArray();
            JSONArray ignoreActions = new JSONArray();
            JSONArray discardActions = new JSONArray();
            JSONArray returnActions = new JSONArray();

            ir = wf.getActions().iterator();
            while (ir.hasNext()) {
                WorkflowActionDb wa = (WorkflowActionDb)ir.next();
                if (wa.getStatus()==WorkflowActionDb.STATE_DOING) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    activeActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_FINISHED) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    finishActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_IGNORED) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    ignoreActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_DISCARDED) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    discardActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_RETURN) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    returnActions.add(json);
                }
            }
        %>
    var $flow;
    $(function () {
        var flowData = $('#flowJson').val();
        console.log(flowData);
        $flow = $('#myflow').myflow({
            allowStateMultiLine: false,
            editable: false,
            restore: eval("(" + flowData + ")"),
            activeRects: {"rects": <%=activeActions.toString()%>},
            finishRects: {"rects": <%=finishActions.toString()%>},
            ignoreRects: {"rects": <%=ignoreActions.toString()%>},
            discardRects: {"rects": <%=discardActions.toString()%>},
            returnRects: {"rects": <%=returnActions.toString()%>},
            rootPath: "<%=request.getContextPath()%>"	    
        });
    });
    <%
        }
    %>

    function ShowDesigner() {
        if (!o("Designer"))
            return;
        if (o("Designer").style.width == "0px") {
            o("Designer").style.width = "100%";
            o("Designer").style.height = "515px";
            o("Designer").style.marginTop = "10px";
        } else {
            o("Designer").style.width = "0px";
            o("Designer").style.height = "0px";
            o("Designer").style.marginTop = "0px";
        }
    }

    $(function () {
        ShowDesigner();
    });
</script>
</html>