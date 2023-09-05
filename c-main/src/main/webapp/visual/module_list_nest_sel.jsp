<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModulePrivDb" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.visual.ModuleUtil" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.ArrayList" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    String nestFormCode = "";
    String nestType = ParamUtil.get(request, "nestType");
    String parentFormCode = ParamUtil.get(request, "parentFormCode");
    String nestFieldName = ParamUtil.get(request, "nestFieldName");
    long parentId = ParamUtil.getLong(request, "parentId", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
    int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;

    FormDb pForm = new FormDb();
    pForm = pForm.getFormDb(parentFormCode);
    FormField nestField = pForm.getFormField(nestFieldName);

    if (parentId != com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID) {
        com.redmoon.oa.visual.FormDAO fdaoPar = new com.redmoon.oa.visual.FormDAO();
        fdaoPar = fdaoPar.getFormDAO(parentId, pForm);
        flowId = fdaoPar.getFlowId();
    }

    JSONObject json = null;
    String filter = "";
    try {
        // 20131123 fgf 添加
        String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
        json = new JSONObject(defaultVal);
        nestFormCode = json.getString("destForm");
        filter = json.getString("filter");
    } catch (JSONException e) {
        e.printStackTrace();
        out.print(SkinUtil.makeErrMsg(request, "JSON解析失败！"));
        return;
    }

    String moduleCode = json.getString("sourceForm");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDbOrInit(moduleCode);
    String formCode = msd.getString("form_code");

    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    if (!fd.isLoaded()) {
        out.print(StrUtil.Alert_Back("表单不存在！"));
        return;
    }

    com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
    boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

    ModulePrivDb mpd = new ModulePrivDb(moduleCode);
    if (isNestSheetCheckPrivilege) {
        if (!mpd.canUserSee(privilege.getUser(request))) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String querystr = "";

    int pagesize = 20;
    String conds = filter; // ParamUtil.get(request, "filter");
    if ("none".equals(conds)) {
        conds = "";
    }

    String action = ParamUtil.get(request, "action");

    long mainId = ParamUtil.getLong(request, "mainId", -1);
    querystr = "op=" + op + "&action=" + action + "&formCode=" + formCode + "&parentFormCode=" + parentFormCode + "&nestFieldName=" + nestFieldName + "&nestType=" + nestType + "&parentId=" + parentId + "&mainId=" + mainId;

    com.alibaba.fastjson.JSONArray colProps = ModuleUtil.getColProps(msd, false);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=fd.getName()%>列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <link rel="stylesheet" href="../js/soul-table/soulTable.css" media="all">
	<link rel="stylesheet" href="../lte/css/font-awesome.min.css?v=4.4.0"/>
	<style>
		i {
			margin-right: 3px;
		}
        .page-main {
            margin: auto 15px;
        }
        .search-form input, select {
            vertical-align: middle;
        }

        .search-form select {
            width: 80px;
        }

        .search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]):not([type="submit"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }

        .cond-span {
            display: inline-block;
            float: left;
            text-align: left;
            width: 300px;
            height: 32px;
            margin: 3px 0;
        }

        .condBtnSearch {
            display: inline-block;
            float: left;
        }

        .cond-title {
            margin: 0 5px;
        }

        .page-main {
            margin: 10px;
        }

        <%=msd.getCss(ConstUtil.PAGE_TYPE_LIST)%>
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery.bgiframe.js"></script>
    <script src="../js/BootstrapMenu.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script>
        <%=ModuleUtil.getModuleListNestSelCondScriptFromWinOpener(conds)%>
    </script>
</head>
<body>
<div class="page-main">
    <table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
        <tr>
            <td height="23" align="center">
                <form id="searchForm" class="search-form" action="moduleListNestSel.do" method="get">
                    &nbsp;
                    <%
                        ArrayList<String> dateFieldNamelist = new ArrayList<String>();
                        String condsHtml = ModuleUtil.getConditionHtml(request, msd, dateFieldNamelist);
                        boolean isQuery = !"".equals(condsHtml);
                        if (isQuery) {
                    %>
					<%=condsHtml%>
                    <input type="hidden" name="op" value="search"/>
                    <input type="hidden" name="nestFormCode" value="<%=nestFormCode%>"/>
                    <input type="hidden" name="parentFormCode" value="<%=parentFormCode%>"/>
                    <input type="hidden" name="nestFieldName" value="<%=nestFieldName%>"/>
                    <input type="hidden" name="parentId" value="<%=parentId%>"/>
					<input type="hidden" name="nestType" value="<%=nestType%>"/>
					<span class="cond-span">
                            <button class="layui-btn layui-btn-primary layui-btn-sm" data-type="reload">
                                <i class="fa fa-search"></i>搜索
                            </button>
                        </span>
                    <%
                        }
                    %>
                </form>
            </td>
        </tr>
    </table>
    <input type="button" class="btn btn-default" value="选择" onClick="selBatch()"/>
    <table class="layui-hide" id="table_list" lay-filter="<%=moduleCode%>"></table>

    <form name="hidForm" action="" method="post">
        <input name="op" type="hidden"/>
        <input name="ids" type="hidden"/>
        <input type="hidden" name="nestType" value="<%=nestType%>"/>
        <input type="hidden" name="parentFormCode" value="<%=parentFormCode%>"/>
        <input type="hidden" name="nestFieldName" value="<%=nestFieldName%>"/>
        <input type="hidden" name="parentId" value="<%=parentId%>"/>
    </form>
</div>
<script src="../js/layui/layui.js" charset="utf-8"></script>
</body>
<script>
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
            // , toolbar: '#toolbar_list'
            , defaultToolbar: ['filter', 'print'/*, 'exports', {
				title: '提示'
				,layEvent: 'LAYTABLE_TIPS'
				,icon: 'layui-icon-tips'
			}*/]
            , drag: {toolbar: true}
            , method: 'post'
            , url: 'moduleList.do?isModuleListNestSel=true&<%=querystr%>&' + condStr
            , cols: [
                <%=colProps.toString()%>
            ]
            , id: 'tableList'
            , page: true
            , unresize: false
            , limit: <%=pagesize%>
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
			var json = $('.search-form').serializeJsonObject();
			json.orderBy = obj.field;
			json.sort = obj.type;
			table.reload('tableList', { //testTable是表格容器id
				initSort: obj // 记录初始排序，如果不设的话，将无法标记表头的排序状态。 layui 2.1.1 新增参数
				, where: json
			});
        });
    });

    $.fn.serializeJsonObject = function () {
        var json = {};
        var form = this.serializeArray();
        $.each(form, function () {
            if (json[this.name]) {
                if (!json[this.name].push) {
                    json[this.name] = [json[this.name]];
                }
                json[this.name].push();
            } else {
                json[this.name] = this.value || '';
            }
        });
        return json;
    };

    function initCalendar() {
        <%for (String ffname : dateFieldNamelist) {%>
        $('#<%=ffname%>').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });

        $('#<%=ffname%>').attr('autocomplete', 'off');
        <%}%>
    }

    $(function () {
        initCalendar();
    });

    function getIdsSelected() {
        var checkStatus = layui.table.checkStatus('tableList');
        var data = checkStatus.data;
        var ids = '';
        for (var i in data) {
            var json = data[i];
            if (ids == '') {
                ids = json.id;
            } else {
                ids += ',' + json.id;
            }
        }
        return ids;
    }

    function selBatch() {
        var ids = getIdsSelected();
        if (ids == "") {
            jAlert("请先选择记录！", "提示");
            return;
        }

        jConfirm("您确定要选择么？", "提示", function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "selBatchForNest.do",
                    data: {
                        nestType: "<%=nestType%>",
                        parentFormCode: "<%=parentFormCode%>",
                        nestFieldName: "<%=nestFieldName%>",
                        parentId: <%=parentId%>,
                        ids: ids,
                        flowId: <%=flowId%>
                    },
                    dataType: "json",
                    beforeSend: function(XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function(data, status) {
                        jAlert(data.msg, "提示");
                        if (data.ret == 1) {
                            if (data.script != "") {
                                try {
                                    eval(data.script);
                                }
                                catch (e) {
                                    console.log(e);
                                }
                            }

                            if (window.opener) {
                                window.opener.fireEvent({
                                    type: EVENT_TYPE.NEST_SELECT,
                                    moduleCode: "<%=nestFormCode%>",
                                });
                            }
                            window.close();
                        }
                    },
                    complete: function(XMLHttpRequest, status){
                        $('body').hideLoading();
                    },
                    error: function(){
                        //请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

	function sel(id, sth) {
		window.opener.setIntpuObjValue(id, sth);
		window.close();
	}
</script>
</html>
