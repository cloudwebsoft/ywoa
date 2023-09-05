<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ErrMsgException" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.pvg.PrivDb" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.Iterator" %>
<!DOCTYPE html>
<html>
<head>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>基础数据管理</title>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery.toaster.js" type="text/javascript"></script>
</head>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
    String op = ParamUtil.get(request, "op");
%>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int kind = ParamUtil.getInt(request, "kind", -1);
    String code = ParamUtil.get(request, "code");
    String userName = privilege.getUser(request);
    SelectKindPriv skp = new SelectKindPriv();
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        if (skp.canUserAppend(userName, kind) || skp.canUserModify(userName, kind) || skp.canUserDel(userName, kind)) {
        } else {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    boolean canDel = true;
    if (kind != -1) {
        canDel = skp.canUserDel(userName, kind);
    }
%>
<%@ include file="basic_select_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<table width="98%" border="0" align="center">
    <tr>
        <td align="center">
            <form id="formSearch" action="basic_select_list.jsp?op=search" method="post">
                编码/名称
                <input name="code"/>
                <%
                    SelectKindDb wptd = new SelectKindDb();

                    if (privilege.isUserPrivValid(request, "admin")) {
                        String opts = "";

                        Iterator ir = wptd.list().iterator();
                        while (ir.hasNext()) {
                            wptd = (SelectKindDb) ir.next();
                            opts += "<option value='" + wptd.getId() + "'>" + wptd.getName() + "</option>";
                        }
                %>
                &nbsp;
                类型
                <select name="kind" id="kind">
                    <option value="-1">不限</option>
                    <%=opts%>
                </select>
                <%
                } else {
                %>
                <input id="kind" name="kind" type="hidden" value="<%=kind %>"/>
                <%
                    }%>
                <script>
                    o("code").value = "<%=code%>";
                    o("kind").value = "<%=kind%>";
                </script>
                <input type="submit" class="btn btn-default" value="确定"/>
            </form>
        </td>
    </tr>
</table>

<table id="mainTable" width="100%" border="0" align="center" cellspacing="0" class="tabStyle_1 percent98">
    <tr align="center">
        <td width="16%" class="tabStyle_1_title">编码</td>
        <td width="22%" class="tabStyle_1_title">名称</td>
        <td width="15%" class="tabStyle_1_title">预览</td>
        <td width="12%" class="tabStyle_1_title">类别</td>
        <td width="13%" class="tabStyle_1_title">类型</td>
        <td width="22%" class="tabStyle_1_title">操作</td>
    </tr>
    <%
        SelectKindDb skd = new SelectKindDb();
        SelectMgr sm = new SelectMgr();
        Iterator ir = sm.list(request).iterator();
        while (ir.hasNext()) {
            SelectDb sd = (SelectDb) ir.next();
            skd = skd.getSelectKindDb(sd.getKind());
            String kindName = "";
            if (skd.isLoaded()) {
                kindName = skd.getName();
            }
    %>
    <tr align="center" id="tr<%=sd.getCode()%>">
        <td align="left"><%=sd.getCode()%>
        </td>
        <td align="left">&nbsp;<span title="<%=sd.getCode()%>"><%=sd.getName()%></span></td>
        <td align="left">
            <select name="select">
                <%if (sd.getType() == SelectDb.TYPE_LIST) {%>
                <%
                    Iterator ir2 = sd.getOptions(new JdbcTemplate()).iterator();
                    while (ir2.hasNext()) {
                        SelectOptionDb sod = (SelectOptionDb) ir2.next();
                        String selected = "";
                        if (sod.isDefault()) {
                            selected = "selected";
                        }
                        String clr = "";
                        if (!sod.getColor().equals("")) {
                            clr = " style='color:" + sod.getColor() + "' ";
                        }
                %>
                <option value="<%=sod.getValue()%>" <%=selected%> <%=clr%>><%=sod.getName()%>
                </option>
                <%
                    }
                %>
                <%
                } else {
                    TreeSelectDb tsd = new TreeSelectDb();
                    tsd = tsd.getTreeSelectDb(sd.getCode());
                    TreeSelectView tsv = new TreeSelectView(tsd);
                    StringBuffer sb = new StringBuffer();
                    tsv.getTreeSelectAsOptions(sb, tsd, 1);
                %>
                <%=sb%>
                <%}%>
            </select></td>
        <td><%=sd.getType() == SelectDb.TYPE_LIST ? "列表" : "树状"%>
        </td>
        <td>
            <a href="basic_select_list.jsp?kind=<%=skd.getId()%>"><%=kindName%>
            </a>
        </td>
        <td>
            <a href="basic_select_edit.jsp?kind=<%=kind %>&code=<%=StrUtil.UrlEncode(sd.getCode())%>">修改</a>
            &nbsp;&nbsp;
            <%if (canDel) { %>
            <a href="javascript:;" onclick="del('<%=sd.getCode()%>', '<%=sd.getName()%>') ">删除</a>
            &nbsp;&nbsp;
            <%} %>
            <%if (sd.getType() == SelectDb.TYPE_LIST) {%>
            <a href="javascript:;" onclick="addTab('<%=sd.getName()%>选择项', '<%=request.getContextPath()%>/admin/basic_select_option.jsp?kind=<%=sd.getKind() %>&code=<%=StrUtil.UrlEncode(sd.getCode())%>')">选择项</a>
            <%} else {%>
            <a href="javascript:;" onclick="addTab('<%=sd.getName()%>选择项', '<%=request.getContextPath()%>/admin/basic_tree_select_frame.jsp?kind=<%=sd.getKind() %>&root_code=<%=StrUtil.UrlEncode(sd.getCode())%>')">选择项</a>
            <%}%>
        </td>
    </tr>
    <%}%>
</table>
</body>
<script>
    function del(code, name) {
        jConfirm('您确定要删除 ' + name + ' 么？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "../basicdata/del.do",
                    data: {
                        code: code
                    },
                    dataType: "json",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        if (data.ret == 1) {
                            $.toaster({priority : 'info', message : data.msg });
                            $('#tr' + code).remove();
                        }
                        else {
                            jAlert(data.msg, "提示");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function () {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

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
