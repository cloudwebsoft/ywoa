<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>表单视图</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<style>
    .loading {
        display: none;
        position: fixed;
        z-index: 1801;
        top: 45%;
        left: 45%;
        width: 100%;
        margin: auto;
        height: 100%;
    }
    
    .SD_overlayBG2 {
        background: #FFFFFF;
        filter: alpha(opacity=20);
        -moz-opacity: 0.20;
        opacity: 0.20;
        z-index: 1500;
    }
    
    .treeBackground {
        display: none;
        position: absolute;
        top: -2%;
        left: 0%;
        width: 100%;
        margin: auto;
        height: 200%;
        background-color: #EEEEEE;
        z-index: 1800;
        -moz-opacity: 0.8;
        opacity: .80;
        filter: alpha(opacity=80);
    }
</style>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    
    String op = ParamUtil.get(request, "op");
    String name = ParamUtil.get(request, "name");
    String formCode = ParamUtil.get(request, "formCode");
    
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    
    String action = ParamUtil.get(request, "action");
    if (action.equals("del")) {
        FormViewMgr ftm = new FormViewMgr();
        boolean re = false;
        try {%>
<script>
    $(".treeBackground").addClass("SD_overlayBG2");
    $(".treeBackground").css({"display": "block"});
    $(".loading").css({"display": "block"});
</script>
<%
    re = ftm.del(request);
%>
<script>
    $(".loading").css({"display": "none"});
    $(".treeBackground").css({"display": "none"});
    $(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
            if (re) {
                out.print(StrUtil.jAlert_Redirect("删除成功！", "提示", "form_view_list.jsp?op=" + op + "&formCode=" + StrUtil.UrlEncode(formCode)));
            } else {
                out.print(StrUtil.jAlert_Back("删除失败！", "提示"));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
        return;
    }
%>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">视图&nbsp;-&nbsp;<%=fd.getName()%>
        </td>
    </tr>
    </tbody>
</table>
<table width="98%" class="percent98">
    <tr>
        <td align="center">
            <form id="searchForm" name="searchForm" method="get">
                名称
                <input type="hidden" name="formCode" id="formCode" value="<%=formCode %>"/>
                <input type="hidden" name="op" value="search"/>
                <input name="name" value="<%=name%>"/>
                <input type="submit" class="btn" value="搜索"/>
                <input class="btn" value="添加" type="button" onclick="addTab('添加视图', '<%=request.getContextPath()%>/admin/form_view_add.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>')"/>
            </form>
        </td>
    </tr>
</table>
<form action="form_view_list.jsp" method=post>
    <table id="mainTable" class="tabStyle_1 percent98" width="93%" align="center" cellpadding="3" cellspacing="0">
        <thead>
        <tr>
            <td class="tabStyle_1_title" width="59%" height="25" align="center">名称</td>
            <td class="tabStyle_1_title" width="23%" height="25" align="center">操作</td>
        </tr>
        </thead>
        <%
            FormViewDb fvd = new FormViewDb();
            Iterator ir = fvd.list(formCode, op, name).iterator();
            Directory dir = new Directory();
            while (ir.hasNext()) {
                fvd = (FormViewDb) ir.next();
        %>
        <tr class="highlight">
            <td width="59%" height="24"><%=fvd.getString("name")%>
            </td>
            <td width="23%" align="center">
                <a href="javascript:;"
                   onclick="addTab('<%=fvd.getString("name")%>', '<%=request.getContextPath()%>/admin/form_view_edit.jsp?id=<%=fvd.getInt("id")%>')">修改</a>
                &nbsp;&nbsp;
                <a href="javascript:;"
                   onClick="jConfirm('您确定要删除表单<%=fvd.getString("name")%>吗？','提示',function(r){ if(!r){return;}else{window.location.href='form_view_list.jsp?action=del&formCode=<%=StrUtil.UrlEncode(formCode)%>&id=<%=fvd.getInt("id")%>'}}) "
                   style="cursor:pointer">删除</a>
                &nbsp;&nbsp;
                <a href="javascript:;"
                   onclick="addTab('显示规则-<%=fvd.getString("name")%>', '<%=request.getContextPath()%>/admin/form_view_show_rule.jsp?formViewId=<%=fvd.getInt("id")%>')">显示规则</a>
            </td>
        </tr>
        <%}%>
    </table>
</form>
<br/>
</body>
<script>
    $(document).ready(function () {
        $("#mainTable td").mouseout(function () {
            if ($(this).parent().parent().get(0).tagName != "THEAD")
                $(this).parent().find("td").each(function (i) {
                    $(this).removeClass("tdOver");
                });
        });

        $("#mainTable td").mouseover(function () {
            if ($(this).parent().parent().get(0).tagName != "THEAD")
                $(this).parent().find("td").each(function (i) {
                    $(this).addClass("tdOver");
                });
        });
    });
</script>
</html>