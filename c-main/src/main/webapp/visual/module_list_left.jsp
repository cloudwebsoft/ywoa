<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.api.IBasicSelectCtl" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String userName = privilege.getUser(request);
    String code = ParamUtil.get(request, "code");
    String mainFormCode = code;
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(code);
    int is_workLog = msd.getInt("is_workLog");
    if (!msd.getString("code").equals(msd.getString("form_code"))) {
        ModuleSetupDb msdMain = msd.getModuleSetupDb(msd.getString("form_code"));
        is_workLog = msdMain.getInt("is_workLog");
        mainFormCode = msd.getString("form_code");
    }

    if (msd == null) {
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
        return;
    }

    if (msd.getInt("is_use") != 1) {
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块未启用！"));
        return;
    }

    String formCode = msd.getString("form_code");

    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);

    ModulePrivDb mpd = new ModulePrivDb(code);
    if (!mpd.canUserSee(privilege.getUser(request))) {
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=fd.getName()%>列表</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet"/>
    <link href="../lte/css/plugins/treeview/bootstrap-treeview.css" rel="stylesheet"/>
    <script src="../inc/common.js"></script>
    <script src="../lte/js/jquery.min.js?v=2.1.4"></script>
    <script src="../lte/js/bootstrap.min.js?v=3.3.6"></script>
    <script src="../lte/js/plugins/treeview/bootstrap-treeview.js"></script>
</head>
<body>
<%
    String viewList = msd.getString("view_list");
    String fieldTreeList = msd.getString("field_tree_list");
    FormField ff = fd.getFormField(fieldTreeList);
    if (ff == null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "字段" + fieldTreeList + "不存在！"));
        return;
    }

    String fieldName = ff.getName();
    MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
    IBasicSelectCtl basicSelectCtl = macroCtlService.getBasicSelectCtl();
    String basicCode = basicSelectCtl.getCode(ff);

    TreeSelectDb tsd = new TreeSelectDb();
    tsd = tsd.getTreeSelectDb(basicCode);

    JSONObject json = new JSONObject();
    TreeSelectView tsv = new TreeSelectView(tsd);
    tsv.getBootstrapJson(request, tsd, json);

    String rootLink = tsd.getLink(request);
    if (rootLink.contains("?")) {
        rootLink += "&isInFrame=true";
    }
    else {
        rootLink += "?isInFrame=true";
    }
    rootLink = request.getContextPath() + "/" + rootLink;
%>
<div style="width:90%; margin:20px auto">
    <div id="rootBox" style="cursor: pointer; margin-bottom: 10px">全部</div>
    <div id="treeView" class="test"></div>
</div>
</body>
<script>
    var data = <%=json.getJSONArray("nodes")%>;
    $(function () {
        $('#rootBox').click(function() {
            var link = "<%=rootLink%>";
            window.parent.document.getElementById("mainModuleFrame").contentWindow.location.href = link;
        });

        $('#treeView').treeview({
            // color: "#428bca",
            // enableLinks: true,
            data: data,
            onNodeSelected: function (event, node) {
                // console.log(node);
                // window.parent.frm.mainModuleFrame.location.href = "../" + node.link;
                if (node.href != "") {
                    var link = "../" + node.href;
                    if (link.indexOf("?") == -1) {
                        link += "?";
                    } else {
                        link += "&";
                    }
                    link += "<%=ConstUtil.PREFIX_REQ_PARAM%><%=fieldName%>=" + encodeURI(node.code) + "&isInFrame=true";
                    window.parent.document.getElementById("mainModuleFrame").contentWindow.location.href = link;
                }
            }
        });
    });
</script>
</html>
