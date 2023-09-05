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
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");
    String name = ParamUtil.get(request, "name");
    String searchName = "";

    ModuleSetupDb vsd = new ModuleSetupDb();
    String sql = vsd.getTable().getSql("list");
    if ("search".equals(op)) {
        if (!"".equals(name)) {
            sql += " and name like " + StrUtil.sqlstr("%" + name + "%");
        }
    }
    sql += " order by name asc";

    JSONArray arr = new JSONArray();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("title", "编码");
    jsonObject.put("field", "code");
    jsonObject.put("width", 200);
    jsonObject.put("sort", false);
    jsonObject.put("align", "left");
    arr.add(jsonObject);
    jsonObject = new JSONObject();
    jsonObject.put("title", "名称");
    jsonObject.put("field", "name");
    jsonObject.put("width", 200);
    jsonObject.put("sort", false);
    jsonObject.put("align", "left");
    arr.add(jsonObject);
    jsonObject = new JSONObject();
    jsonObject.put("title", "描述");
    jsonObject.put("field", "desc");
    jsonObject.put("width", 300);
    jsonObject.put("sort", false);
    jsonObject.put("align", "left");
    arr.add(jsonObject);
    jsonObject = new JSONObject();
    jsonObject.put("title", "类型");
    jsonObject.put("field", "kind");
    jsonObject.put("width", 150);
    jsonObject.put("sort", false);
    jsonObject.put("align", "left");
    arr.add(jsonObject);
    jsonObject = new JSONObject();
    jsonObject.put("title", "操作");
    jsonObject.put("field", "colOperate");
    jsonObject.put("width", 150);
    jsonObject.put("sort", false);
    jsonObject.put("fixed", "right");
    arr.add(jsonObject);

    String colProps = arr.toString();
    colProps = colProps.substring(1);
    colProps = colProps.substring(0, colProps.length() - 1);
%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>模块列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <style>
        .page-main {
            margin: 10px 15px 0px 15px;
        }
        .search-form input,select {
            vertical-align:middle;
        }
        .search-form select {
            width: 80px;
        }
        .search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }
        .cond-title {
            margin: 0 5px;
        }
    </style>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js"></script>
    <script src="../js/jquery-alerts/cws.alerts.js"></script>
    <script src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <link rel="stylesheet" href="../js/soul-table/soulTable.css" media="all">
    <script src="../js/layui/layui.js" charset="utf-8"></script>
</head>
<body>
<div class="page-main">
    <div id="searchFormBox" class="search-form-box">
        <form id="searchForm" name="searchForm" class="search-form" method="get">
            <div class="layui-inline">
                名称
                <input type="hidden" name="op" value="search"/>
                <input name="name" value="<%=name%>"/>
                <button type="submit" class="layui-btn layui-btn-primary layui-btn-sm">搜索</button>
            </div>
        </form>
    </div>
    <table class="layui-hide" id="table_list"></table>
    <script type="text/html" id="toolbar_list">
        <div class="layui-btn-container">
            <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="syncPvg" title="同步权限"><i class="layui-icon layui-icon-add-circle-fine"></i></i></i>同步权限</button>
        </div>
    </script>

    <div id="dlg" style="display:none">
        <div>名称&nbsp;<input id="name" name="name"/></div>
    </div>
    <div id="dlgSync" style="display:none">
        <div>
            模块：<select id="byModuleCode" name="byModuleCode">
            <option value="">请选择</option>
        </select>
        </div>
        <div>(同步后权限将与所选择的模块一致)</div>
    </div>
</div>
</body>
<script>
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

    var tableData;
    layui.config({
        base: '../js/',   // 第三方模块所在目录
        // version: 'v1.6.2' // 插件版本号
    }).extend({
        soulTable: 'soul-table/soulTable',
        tableChild: 'soul-table/tableChild',
        tableMerge: 'soul-table/tableMerge',
        tableFilter: 'soul-table/tableFilter',
        excel: 'soul-table/excel',
    });

    layui.use(['table', 'soulTable'], function () {
        var table = layui.table;
        var soulTable = layui.soulTable;

        table.render({
            elem: '#table_list'
            , toolbar: '#toolbar_list'
            , defaultToolbar: ['filter', 'print'/*, 'exports', {
				title: '提示'
				,layEvent: 'LAYTABLE_TIPS'
				,icon: 'layui-icon-tips'
			}*/]
            , drag: {toolbar: true}
            , method: 'post'
            , url: 'listSetup.do'
            , cols: [[
                {type: 'checkbox', align: 'center'},
                <%=colProps%>
            ]]
            , id: 'tableList'
            , page: true
            , unresize: false
            , limit: 50
            , height: 'full-98'
            , parseData: function (res) { //将原始数据解析成 table 组件所规定的数据
                return {
                    "code": res.errCode, //解析接口状态
                    "msg": res.msg, //解析提示文本
                    "count": res.total, //解析数据长度
                    "data": res.rows //解析数据列表
                };
            }
            , done: function (res, curr, count) {
                tableData = res.data;
                soulTable.render(this);
            }
        });

        //头工具栏事件
        table.on('toolbar()', function (obj) {
            var checkStatus = table.checkStatus(obj.config.id);
            switch (obj.event) {
                case 'syncPvg':
                    layer.open({
                        type: 1,
                        title: '修改',
                        shadeClose: true,
                        shade: 0.6,
                        area: ['90%', '90%'],
                        content: $('#dlg')
                    });
                    break;
                case 'delRows':
                    var data = checkStatus.data;
                    if (data.length == 0) {
                        layer.msg('请选择记录');
                        return;
                    }

                    var ids = '';
                    for (var i in data) {
                        var json = data[i];
                        if (ids == '') {
                            ids = json.id;
                        } else {
                            ids += ',' + json.id;
                        }
                    }
                    layer.confirm('您确定要删除么？', {icon: 3, title: '提示'}, function (index) {
                        $.ajax({
                            type: "post",
                            url: "moduleDel.do",
                            data: {
                                ids: ids
                            },
                            dataType: "html",
                            beforeSend: function (XMLHttpRequest) {
                                $("body").showLoading();
                            },
                            success: function (data, status) {
                                data = $.parseJSON(data);
                                layer.msg(data.msg);

                                if (data.ret == 1) {
                                    // doQuery();
                                    doQuery();
                                    try {
                                        onModuleDel(ids, false);
                                    } catch (e) {
                                    }
                                }
                            },
                            complete: function (XMLHttpRequest, status) {
                                $("body").hideLoading();
                            },
                            error: function (XMLHttpRequest, textStatus) {
                                // 请求出错处理
                                alert(XMLHttpRequest.responseText);
                            }
                        });
                        layer.close(index);
                    });
                    // layer.msg(checkStatus.isAll ? '全选': '未全选');
                    break;
            }
        });

        $('.search-form .layui-btn').on('click', function (e) {
            e.preventDefault();
            table.reload('tableList', {
                page: {
                    curr: 1 //重新从第 1 页开始
                }
                , where: $('.search-form').serializeJsonObject()
            }, 'data');
        });

        //监听表格排序问题
        table.on('sort()', function (obj) { //注：sort lay-filter="对应的值"
            table.reload('tableList', { //testTable是表格容器id
                initSort: obj // 记录初始排序，如果不设的话，将无法标记表头的排序状态。 layui 2.1.1 新增参数
                , where: {
                    orderBy: obj.field //排序字段
                    , sort: obj.type //排序方式
                }
            });
        });
    });
</script>
</html>