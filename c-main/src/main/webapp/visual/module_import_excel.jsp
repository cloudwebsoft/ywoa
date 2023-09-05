<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModuleImportTemplateDb" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String code = ParamUtil.get(request, "code");
    String formCode = ParamUtil.get(request, "formCode");
    String cws_id = ParamUtil.get(request, "parentId");
    String menuItem = ParamUtil.get(request, "menuItem");
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
    boolean isAll = ParamUtil.getBoolean(request, "isAll", false);
    int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=fd.getName()%>导入</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <%
        if (!fd.isLoaded()) {
            out.print(StrUtil.jAlert_Back("表单不存在！", "提示"));
            return;
        }
    %>
</head>
<body>
<%
    if (isShowNav == 1) {
%>
<%@ include file="module_inc_menu_top.jsp" %>
<div class="spacerH"></div>
<%
    if ("".equals(menuItem)) {
%>
<script>
    o("menu1").className = "current";
</script>
<%
} else {
%>
<script>
    o("menu<%=menuItem%>").className = "current";
</script>
<%
        }
    }

    StringBuffer params = new StringBuffer();
    Enumeration reqParamNames = request.getParameterNames();
    while (reqParamNames.hasMoreElements()) {
        String paramName = (String) reqParamNames.nextElement();
        String[] paramValues = request.getParameterValues(paramName);
        if (paramValues.length == 1) {
            String paramValue = ParamUtil.getParam(request, paramName);
            // 过滤掉formCode等
            if (paramName.equals("code")
                    || paramName.equals("formCode")
                    || paramName.equals("moduleCode")
                    || paramName.equals("mainCode")
                    || paramName.equals("menuItem")
                    || paramName.equals("parentId")
                    || paramName.equals("moduleCodeRelated")
                    || paramName.equals("parentId")
                    || paramName.equals("op")
            ) {
                ;
            } else {
                StrUtil.concat(params, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
            }
        }
    }
%>
<form id="form1" style="margin-top: 10px;" action="importExcel.do?op=import&menuItem=<%=menuItem%>&code=<%=code%>&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isAll=<%=isAll%>&parentId=<%=cws_id%>&<%=params%>" method="post" enctype="multipart/form-data">
    <table width="525" border="0" align="center" cellspacing="0" class="tabStyle_1 percent60">
        <thead>
        <tr>
            <td class="tabStyle_1_title">请选择需导入的文件</td>
        </tr>
        </thead>
        <tr>
            <td width="319" align="center">
                <%
                    ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
                    String sql = mid.getTable().getSql("listForForm");
                    Vector v = mid.list(sql, new Object[]{formCode});
                    if (v.size() > 0) {
                %>
                模板
                <select id="templateId" name="templateId" title="默认按显示的列">
                    <%
                        Iterator ir = v.iterator();
                        while (ir.hasNext()) {
                            mid = (ModuleImportTemplateDb) ir.next();
                    %>
                    <option value="<%=mid.getLong("id")%>"><%=mid.getString("name")%>
                    </option>
                    <%
                        }
                    %>
                    <option value="">按模块显示的列</option>
                </select>
                <%}%>
                <input title="选择附件文件" type="file" size="20" name="excel"/>
                <input name="moduleCode" value="<%=code%>" type="hidden"/>
                <input class="btn btn-default" id="btnOk" type="submit" value="确定"/>
                &nbsp;
                <input class="btn btn-default" id="btnOk" type="button" value="返回" onclick="window.history.back()"/>
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    $('#btnOk').click(function (e) {
        e.preventDefault();
        if ($(this).is(":disabled")) {
            jAlert('请不要重复点击', '提示');
            return;
        }
        $(this).attr("disabled", true);
        $('#form1').submit();
    })
</script>
</html>
