<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.flow.Directory" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");
    String formCode = ParamUtil.get(request, "formCode");
    String name = ParamUtil.get(request, "name");

    ModuleSetupDb vsd = new ModuleSetupDb();
    vsd = vsd.getModuleSetupDbOrInit(formCode);
    FormDb fd = new FormDb();
    fd = fd.getFormDb(vsd.getString("form_code"));
    String searchName = fd.getName();
    if (searchName == null) {
        searchName = "";
    }

    String sql = vsd.getTable().getSql("listForForm") + StrUtil.sqlstr(formCode);
    if ("search".equals(op)) {
        if (!"".equals(name)) {
            sql += " and name like " + StrUtil.sqlstr("%" + name + "%");
        }
    }
    sql += " order by kind asc, name asc";
%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>表单模块</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js"></script>
    <script src="../js/jquery-alerts/cws.alerts.js"></script>
    <script src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">模块&nbsp;-&nbsp;<%=searchName%>
        </td>
    </tr>
    </tbody>
</table>
<table width="98%" class="percent98">
    <tr>
        <td align="center">
            <form id="searchForm" name="searchForm" method="get">
                名称
                <input type="hidden" name="op" value="search"/>
                <input name="name" value="<%=name%>"/>
                <input name="formCode" value="<%=formCode%>" type="hidden"/>
                <input type="submit" class="btn" value="搜索"/>
                &nbsp;&nbsp;
                <input class="btn" value="添加副模块" type="button" onclick="addModule()"/>
            </form>
        </td>
    </tr>
</table>
<table id="mainTable" class="tabStyle_1 percent98" width="93%" align="center" cellpadding="3" cellspacing="0">
    <thead>
    <tr>
        <td class="tabStyle_1_title" width="16%" align="center">编码</td>
        <td class="tabStyle_1_title" width="22%" height="25" align="center">名称</td>
        <td class="tabStyle_1_title" width="30%" align="center">描述</td>
        <td class="tabStyle_1_title" width="7%" align="center">类型</td>
        <td class="tabStyle_1_title" width="7%" align="center">状态</td>
        <td class="tabStyle_1_title" width="23%" align="center">操作</td>
    </tr>
    </thead>
    <%
        Vector v = vsd.list(sql);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            vsd = (ModuleSetupDb) ir.next();
    %>
    <tr class="highlight" id="tr<%=vsd.getString("code")%>">
        <td height="24"><%=vsd.getString("code")%>
        </td>
        <td height="24"><%=vsd.getString("name")%>
        </td>
        <td align="left"><%=StrUtil.getNullStr(vsd.getString("description"))%>
        </td>
        <td align="center">
            <%=vsd.getInt("kind") == ModuleSetupDb.KIND_MAIN ? "主模块" : "副模块"%>
        </td>
        <td align="center"><%=vsd.getInt("is_use") == 1 ? "启用" : "停用"%>
        </td>
        <td align="center">
            <a href="javascript:"
               onclick="addTab('<%=vsd.getString("name")%>', '<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=formCode%>&tabIdOpener=' + getActiveTabId() + '&code=<%=vsd.getString("code")%>')">维护</a>
            <%
                if (vsd.getInt("kind") == 1) {
            %>
            &nbsp;&nbsp;<a href="javascript:"
                           onclick="del('<%=vsd.getString("code")%>', '<%=vsd.getString("name")%>')"
                           style="cursor:pointer">删除</a>
            <%
                }
                License license = License.getInstance();

                if (license.isPlatformSrc()) {
            %>
            &nbsp;&nbsp;<a href="javascript:" onclick="clone('<%=vsd.getString("code")%>', '<%=vsd.getString("name")%>')">复制</a>
            &nbsp;&nbsp;<a href="javascript:" onclick="syncModule('<%=vsd.getString("code")%>', '<%=vsd.getString("name")%>')">同步</a>
            <%
                }
                String moduleUrlList = request.getContextPath() + "/visual/module_list.jsp?code=" + vsd.getString("code") + "&formCode=" + StrUtil.UrlEncode(vsd.getString("form_code"));
                if (license.isPlatformSrc() && vsd.getInt("is_use") == 1) {
            %>
            &nbsp;&nbsp;<a href="javascript:" onclick="addTab('<%=StrUtil.getNullStr(vsd.getString("name"))%>', '<%=moduleUrlList%>');">打开</a>
            <%
                }
            %>
        </td>
    </tr>
    <%
        }
    %>
</table>
<div id="dlg" style="display:none">
    <div>名称&nbsp;<input id="name" name="name"/></div>
</div>
<div id="dlgSync" style="display:none">
    <div>同步：&nbsp;<input id="cols" name="cols" type="checkbox" checked value="1"/>&nbsp;列&nbsp;<input id="query" name="query" type="checkbox" checked value="1"/>&nbsp;工具条按钮及查询</div>
    <div>
        模块：<select id="byModuleCode" name="byModuleCode">
        <option value="">请选择</option>
        <%
            ir = v.iterator();
            while (ir.hasNext()) {
                vsd = (ModuleSetupDb) ir.next();
        %>
        <option value="<%=vsd.getString("code")%>"><%=vsd.getString("name")%>
        </option>
        <%
            }
        %>
    </select>
    </div>
    <div>(同步后，本模块将变为与选择的模块一致)</div>
</div>
</body>
<script>
    $(document).ready(function () {
        $("#mainTable td").mouseout(function () {
            if ($(this).parent().parent().get(0).tagName != "THEAD") {
                $(this).parent().find("td").each(function (i) {
                    $(this).removeClass("tdOver");
                });
            }
        });

        $("#mainTable td").mouseover(function () {
            if ($(this).parent().parent().get(0).tagName != "THEAD") {
                $(this).parent().find("td").each(function (i) {
                    $(this).addClass("tdOver");
                });
            }
        });
    });

    function addModule() {
        $("#dlg").dialog({
            title: "添加副模块",
            modal: true,
            // bgiframe:true,
            buttons: {
                "取消": function () {
                    $(this).dialog("close");
                },
                "确定": function () {
                    if (o("name").value == "") {
                        jAlert("名称不能为空！", "提示");
                        return;
                    }
                    $.ajax({
                        type: "post",
                        url: "createSubModule.do",
                        data: {
                            action: "add",
                            formCode: "<%=formCode%>",
                            name: o('name').value
                        },
                        dataType: "json",
                        beforeSend: function (XMLHttpRequest) {
                            //ShowLoading();
                        },
                        success: function (data, status) {
                            var code = data.code;
                            window.location.reload();
                            addTab(o('name').value, "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=formCode%>&code=" + code);
                        },
                        complete: function (XMLHttpRequest, status) {
                            // HideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText, "提示");
                        }
                    });

                    $(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true,
            width: 300
        });
    }

    function clone(code, name) {
        o("name").value = name + "(复制)";
        $("#dlg").dialog({
            title: "复制模块：" + name,
            modal: true,
            // bgiframe:true,
            buttons: {
                "取消": function () {
                    $(this).dialog("close");
                },
                "确定": function () {
                    if (o("name").value == "") {
                        jAlert("名称不能为空！", "提示");
                        return;
                    }
                    $.ajax({
                        type: "post",
                        url: "cloneModule.do",
                        data: {
                            code: code,
                            action: "clone",
                            formCode: "<%=formCode%>",
                            name: o('name').value
                        },
                        dataType: "json",
                        beforeSend: function (XMLHttpRequest) {
                            //ShowLoading();
                        },
                        success: function (data, status) {
                            var code = data.code;
                            window.location.reload();
                            addTab(o('name').value, "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=formCode%>&code=" + code);
                        },
                        complete: function (XMLHttpRequest, status) {
                            // HideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText, "提示");
                        }
                    });

                    $(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true,
            width: 300
        });
    }

    function del(code, name) {
        jConfirm('您确定要删除 ' + name + ' 么？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "delModule.do",
                    data: {
                        code: code
                    },
                    dataType: "json",
                    beforeSend: function (XMLHttpRequest) {
                        //ShowLoading();
                    },
                    success: function (data, status) {
                        if (data.ret == "1") {
                            $('#tr' + code).remove();
                        } else {
                            jAlert(data.msg, "提示");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        // HideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, "提示");
                    }
                });
            }
        })
    }

    function syncModule(moduleCode, moduleName) {
        $("#dlgSync").dialog({
            title: "模块同步",
            modal: true,
            buttons: {
                "取消": function () {
                    $(this).dialog("close");
                },
                "确定": function () {
                    var cols = $('#cols').attr('checked') ? 1 : 0;
                    var query = $('#query').attr('checked') ? 1 : 0;
                    if (cols == 0 && query == 0) {
                        jAlert('请勾选同步列或查询', '提示');
                        return;
                    }
                    if ($('#byModuleCode').val() == '') {
                        jAlert('请选择同步的模块', '提示');
                        return;
                    }
                    if ($('#byModuleCode').val() == moduleCode) {
                        jAlert('不能选择相同的模块', '提示');
                        return;
                    }

                    $.ajax({
                        type: "post",
                        url: "syncModule.do",
                        data: {
                            formCode: "<%=formCode%>",
                            byModuleCode: $('#byModuleCode').val(),
                            cols: cols,
                            query: query,
                            moduleCode: moduleCode
                        },
                        dataType: "json",
                        beforeSend: function (XMLHttpRequest) {
                            $('body').showLoading();
                        },
                        success: function (data, status) {
                            jAlert(data.msg, '提示');
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('body').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText, "提示");
                        }
                    });

                    $(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true,
            width: 300
        });
    }
</script>
</html>