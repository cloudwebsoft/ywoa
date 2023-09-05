<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String isFlow = ParamUtil.get(request, "isFlow");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>表单管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/BootstrapMenu.min.js"></script>
    <style>
        .search-form input,select {
            vertical-align:middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }

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
    <script language="JavaScript" type="text/JavaScript">
        <!--
        function presskey(eventobject) {
            if (event.ctrlKey && window.event.keyCode == 13) {
                <%if (isFlow.equals("")) {%>
                window.location.href = "?isFlow=0";
                <%}else{%>
                window.location.href = "?";
                <%}%>
            }
        }

        document.onkeydown = presskey;
        //-->
    </script>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    if (op.equals("import")) {
        try {%>
<script>
    $(".treeBackground").addClass("SD_overlayBG2");
    $(".treeBackground").css({"display": "block"});
    $(".loading").css({"display": "block"});
</script>
<%
    ModuleUtil.importSolution(application, request);
%>
<script>
    $(".loading").css({"display": "none"});
    $(".treeBackground").css({"display": "none"});
    $(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }
        out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "form_m.jsp"));
        return;
    }

    String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
    if (!flowTypeCode.equals("")) {
        Leaf flf = new Leaf();
        flf = flf.getLeaf(flowTypeCode);

        LeafPriv lp = new LeafPriv(flowTypeCode);
        if (!(lp.canUserExamine(privilege.getUser(request)))) {
            if (flowTypeCode.equals(Leaf.CODE_ROOT) && privilege.isUserPrivValid(request, "admin.unit")) {
            }
            // 如果是单位管理员，且流程或本单位的
            else if (!flowTypeCode.equals(Leaf.CODE_ROOT) && privilege.isUserPrivValid(request, "admin.unit") && flf.getUnitCode().equals(privilege.getUserUnitCode(request))) {
            } else {
                out.println(cn.js.fan.web.SkinUtil.makeInfo(request, "请选择流程"));
                return;
            }
        }
    } else {
        if (!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "admin.flow")) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String name = ParamUtil.get(request, "name");
%>
<%@ include file="form_inc_menu_top.jsp" %>
<script>
    try {
        <%if (!isFlow.equals("0")) {%>
        o("menu1").className = "current";
        <%}else{%>
        o("menu3").className = "current";
        <%}%>
    } catch (e) {
    }
</script>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td align="center">
            <form id="searchForm" name="searchForm" method="get" class="search-form">
                &nbsp;单位
                <select id="searchUnitCode" name="searchUnitCode">
                    <option value="">不限</option>
                    <%
                        String searchUnitCode = ParamUtil.get(request, "searchUnitCode");
                        if (License.getInstance().isPlatformGroup()) {
                            DeptDb dd = new DeptDb();
                            DeptView dv = new DeptView(request, dd);
                            StringBuffer sb = new StringBuffer();
                            dd = dd.getDeptDb(privilege.getUserUnitCode(request));
                    %>
                    <%=dv.getUnitAsOptions(sb, dd, dd.getLayer())%>
                    <%
                        }
                    %>
                </select>
                编码或名称
                <input type="hidden" name="op" value="search"/>
                <input type="hidden" name="flowTypeCode" value="<%=flowTypeCode%>"/>
                <input type="hidden" name="isFlow" value="<%=isFlow%>"/>
                <input name="name" value="<%=name%>"/>
                &nbsp;
                <input class="tSearch" type=submit value="搜索">

            </form>
        </td>
    </tr>
</table>
<%
    FormDb ftd = new FormDb();
    String sql = ftd.getSqlList(request);
    // out.print(sql);
%>
<table id="grid" width="98%" border="0" cellpadding="0" cellspacing="0">
    <thead>
    <tr>
        <th width="150" height="25" align="left">编码</th>
        <th width="280" height="25" align="left">名称</th>
        <th width="215" align="left">表格名称</th>
        <th width="150" align="left">流程类型</th>
        <th width="140" height="25" align="center">操作</th>
    </tr>
    </thead>
    <tbody>
    <%
        Iterator ir = ftd.list(sql).iterator();
        Directory dir = new Directory();
        while (ir.hasNext()) {
            ftd = (FormDb) ir.next();
    %>
    <tr id="<%=ftd.getCode()%>">
        <td width="18%" height="24" abbr="code"><%=ftd.getCode()%>
        </td>
        <td width="32%"><%=ftd.getName()%>
        </td>
        <td width="23%"><%=ftd.getTableNameByForm()%>
        </td>
        <td width="12%"><%
            Leaf lf = dir.getLeaf(ftd.getFlowTypeCode());
            if (lf != null)
                out.print(lf.getName(request));
        %>
        </td>
        <td width="15%" align="center"><%if (!ftd.isSystem()) {%>
            <a href="javascript:;" onclick="addTab('<%=ftd.getName()%>', '<%=request.getContextPath()%>/admin/form_edit.jsp?code=<%=ftd.getCode()%>')">修改</a>
            &nbsp;<a href="javascript:;"
                     onClick="del('<%=ftd.getCode()%>')">删除</a>
            <%}%>
            &nbsp;<a href="javascript:;" onclick="addTab('<%=ftd.getName()%>视图', '<%=request.getContextPath()%>/admin/form_view_list.jsp?formCode=<%=ftd.getCode()%>')">视图</a>
            &nbsp;<a href="javascript:;" onclick="addTab('<%=ftd.getName()%>模块', '<%=request.getContextPath()%>/visual/module_setup_list.jsp?formCode=<%=ftd.getCode()%>')">模块</a>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>

<div id="dlgImport" style="display:none">
    <div style="margin-bottom:10px">请选择导入文件</div>
    <form id="formImport" action="form_m.jsp?op=import" method=post enctype="multipart/form-data">
        <input type="file" name="file"/>
        <br/>
        <span styl="color:red">
注意：<br/>
原有相同编码的表单将会被覆盖<br/>
不存在的基础数据将会被自动创建
</span>
    </form>
</div>

</body>
<script>
    var flex;
    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    {name: '添加', bclass: 'add', onpress: action},
                    {name: '删除', bclass: 'delete', onpress: action},
                    <%
                    if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {
                    %>
                    {name: '导入', bclass: 'import1', onpress: action},
                    <%}%>
                    <%
                    if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {
                    %>
                    {name: '导出', bclass: 'export', onpress: action},
                    <%}%>
                    {name: '条件', bclass: 'btnseparator', type: 'include', id: 'searchTable'}
                ],
                /*
                searchitems : [
                    {display: 'ISO', name : 'iso'},
                    {display: 'Name', name : 'name', isdefault: true}
                    ],
                */
                url: false,
                usepager: false,
                checkbox: true,
                useRp: true,

                // title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: false,

                onReload: onReload,
                /*
                onRowDblclick: rowDbClick,
                onColSwitch: colSwitch,
                onColResize: colResize,
                onToggleCol: toggleCol,
                */
                autoHeight: true,
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );

        o("searchUnitCode").value = "<%=searchUnitCode%>";

    });

    function action(com, grid) {
        if (com == '添加') {
            addTab('添加表单', '<%=request.getContextPath()%>/admin/form_add.jsp?isFlow=<%=isFlow%>&flowTypeCode=<%=StrUtil.UrlEncode(flowTypeCodeTop)%>');
        } else if (com == '删除') {
            var ids = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
                if (ids=="")
                    ids = $(this).val();
                else
                    ids += "," + $(this).val();
            });
            del(ids);
        } else if (com == '导出') {
            var codes = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on']", grid.bDiv).each(function (i) {
                if($(this).is(":checked")) {
                    if (codes == "") {
                        codes = $(this).val();
                    } else {
                        codes += "," + $(this).val();
                    }
                }
            });

            if (codes.length == 0) {
                jAlert('请选择记录!', '提示');
                return;
            }
            window.open('solution_export.jsp?op=export&codes=' + codes);
        } else if (com == "导入") {
            importModule();
        }
    }

    function del(codes) {
        if (codes == "") {
            jAlert('请选择记录!', '提示');
            return;
        }

        jConfirm('您确定要删除么？', '提示', function (r) {
            if (!r) {
                return;
            }

            $.ajax({
                type: "post",
                url: "<%=request.getContextPath()%>/form/del.do",
                data: {
                    codes: codes
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $("body").eq(0).showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                    if (data.ret == "1") {
                        var ary = codes.split(',');
                        for (var key in ary) {
                            $('#' + ary[key]).remove();
                        }
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $("body").eq(0).hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    }

    function importModule() {
        $("#dlgImport").dialog({
            title: "导入模块",
            modal: true,
            // bgiframe:true,
            buttons: {
                "取消": function () {
                    $(this).dialog("close");
                },
                "确定": function () {
                    $('#formImport').submit();
                    $(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true,
            width: 300
        });
    }

    function onReload() {
        window.location.reload();
    }
</script>
</html>