<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudweb.oa.api.IModuleFieldSelectCtl" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModulePrivDb" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.visual.ModuleUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    String moduleCode = ParamUtil.get(request, "formCode");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDbOrInit(moduleCode);

    FormDb fd = new FormDb();
    fd = fd.getFormDb(msd.getString("form_code"));
    if (!fd.isLoaded()) {
        out.print(StrUtil.Alert_Back("表单不存在！"));
        return;
    }

    ModulePrivDb mpd = new ModulePrivDb(moduleCode);
    com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
    boolean isModuleFieldSelectCtlCheckPrivilege = cfg.getBooleanProperty("isModuleFieldSelectCtlCheckPrivilege");
    if (isModuleFieldSelectCtlCheckPrivilege) {
        if (!mpd.canUserSee(privilege.getUser(request))) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String openerFormCode = ParamUtil.get(request, "openerFormCode");
    if ("".equals(openerFormCode)) {
        out.print(SkinUtil.makeErrMsg(request, "openerFormCode不能为空"));
        return;
    }
    String openerFieldName = ParamUtil.get(request, "openerFieldName");

    int mode = 1; // 默认为选择窗体

    FormDb openerFd = new FormDb();
    openerFd = openerFd.getFormDb(openerFormCode);
    if (!openerFd.isLoaded()) {
        out.print(SkinUtil.makeErrMsg(request, "表单：" + openerFormCode + " 不存在"));
        return;
    }
    FormField openerField = openerFd.getFormField(openerFieldName);
    if (openerField == null) {
        out.print(SkinUtil.makeErrMsg(request, "字段：" + openerFieldName + " 在表单：" + openerFormCode + " 中不存在"));
        return;
    }

    MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
    IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
    String desc = moduleFieldSelectCtl.formatJSONString(openerField.getDescription());
    com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(desc);
    String filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));
    if ("none".equals(filter)) {
        filter = "";
    }
    String byFieldName = json.getString("idField");
    String showFieldName = json.getString("showField");

    if (json.containsKey("mode")) {
        mode = json.getIntValue("mode");
    }

    String querystr = "";

    int pagesize = 20;
    // 过滤条件
    String conds = filter;

    querystr = "pageType=moduleListSel&op=" + op + "&formCode=" + moduleCode + "&byFieldName=" + StrUtil.UrlEncode(byFieldName) + "&showFieldName=" + StrUtil.UrlEncode(showFieldName) + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + openerFieldName;

    // 如果在宏控件中定义了条件conds，则解析条件，并从父窗口中取表单域的值{$fieldName}
    com.alibaba.fastjson.JSONArray colProps = ModuleUtil.getColProps(msd, true);

    ArrayList<String> dateFieldNamelist = new ArrayList<String>();
    String condsHtml = ModuleUtil.getConditionHtml(request, msd, dateFieldNamelist);
    boolean isQuery = !"".equals(condsHtml);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=fd.getName()%>列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../lte/css/font-awesome.min.css?v=4.4.0"/>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <link rel="stylesheet" href="../js/soul-table/soulTable.css" media="all">
    <style>
        i {
            margin-right: 3px;
        }

        .main-content {
            margin: 5px 20px;
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
            width: 200px;
            height: 32px;
        }

        .condBtnSearch {
            display: inline-block;
            float: left;
        }

        .cond-title {
            margin: 0 5px;
        }

        .helper-ctl {
            display: none;
        }

        <%=msd.getCss(ConstUtil.PAGE_TYPE_LIST)%>
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/BootstrapMenu.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script>
        <%=ModuleUtil.getModuleListSelCondScriptFromWinOpener(conds)%>
    </script>
</head>
<body>
<div class="spacerH"></div>
<div class="main-content">
    <%
        if (isQuery) {
    %>
    <table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
        <tr>
            <td height="23" align="center">
                <div id="searchFormBox" class="search-form-box">
                    <form id="searchForm" class="search-form" action="module_list_sel.jsp" method="get">
                        &nbsp;
                        <%
                            out.print(condsHtml);
                        %>
                        <input type="hidden" name="op" value="search"/>
                        <input type="hidden" name="formCode" value="<%=moduleCode%>"/>
                        <input type="hidden" name="filter" value="<%=StrUtil.HtmlEncode(conds) %>"/>
                        <input type="hidden" name="byFieldName" value="<%=byFieldName %>"/>
                        <input type="hidden" name="showFieldName" value="<%=showFieldName %>"/>
                        <input type="hidden" name="openerFormCode" value="<%=openerFormCode %>"/>
                        <input type="hidden" name="openerFieldName" value="<%=openerFieldName %>"/>
                        <span class="cond-span">
                            <button class="layui-btn layui-btn-primary layui-btn-sm" data-type="reload">
                                <i class="fa fa-search"></i>搜索
                            </button>
                        </span>
                    </form>
                </div>
            </td>
        </tr>
    </table>
    <%
        }
    %>
    <table class="layui-hide" id="table_list" lay-filter="<%=moduleCode%>"></table>
</div>
<script src="../js/layui/layui.js" charset="utf-8"></script>
<script>
    function sel(id, sth) {
        // 如果mode=1为选择窗体
        if ("<%=mode%>" === "1") {
            // console.log('id=' + id + ' sth=' + sth);
            window.opener.setIntpuObjValue(id, sth);
        } else {
            // var obj = window.opener.o("<%=openerFieldName%>");
            window.opener.$("#<%=openerFieldName%>").empty().append("<option id='" + id + "' value='" + id + "'>" + sth + "</option>").trigger('change');
        }
        // 在doFuns中已实现，且为SQL宏控件调用了事件方法，另外还生成了校验的JS
        // window.opener.onSelect<%=openerFieldName %>(id, sth);
        window.close();
    }

    // 不放在页尾，因为服务器卡的时候，如果页面加载还没完成就点了“选择”，会导致JS出错：setOpenerFieldValue is not defined
    function setOpenerFieldValue(openerField, val, isMacro, sourceValue, checkJs) {
        var obj = window.opener.o('helper_' + openerField);
        if (obj) {
            obj.parentNode.removeChild(obj);
        }

        if (isMacro) {
            window.opener.replaceValue(openerField, val, sourceValue, checkJs);
        } else {
            var obj = window.opener.o(openerField);
            if (obj) {
                if (obj.getAttribute("type") == "radio") {
                    window.opener.setRadioValue(obj.name, val);
                } else if (obj.getAttribute("type") == "checkbox") {
                    window.opener.setCheckboxChecked(obj.name, val);
                } else {
                    // obj.value = val;
                    if (obj.tagName != 'SPAN') {
                        obj.value = val;
                    } else {
                        // 不可写字段
                        obj.innerHTML = val;
                    }
                }
            } else {
                if (isIE()) {
                    console.error("字段：" + openerField + " 不存在！")
                }
            }

            var objShow = window.opener.o(openerField + "_show");
            if (objShow) {
                $(objShow).html(val);
            }
        }
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
            // , toolbar: '#toolbar_list'
            , defaultToolbar: ['filter', 'print'/*, 'exports', {
				title: '提示'
				,layEvent: 'LAYTABLE_TIPS'
				,icon: 'layui-icon-tips'
			}*/]
            , drag: {toolbar: true}
            , method: 'post'
            , url: 'moduleList.do?isModuleListSel=true&<%=querystr%>&' + condStr
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

    function doAtferSel() {
        // 应用显示规则
        var isDoViewJS = true;
        try {
            isDoViewJS = window.opener.isDoViewJSOnModuleListSel();
        } catch (e) {
        }

        if (isDoViewJS) {
            try {
                window.opener.doViewJS();
            } catch (e) {
                consoleLog(e);
            }
        }
    }
</script>
</body>
</html>
