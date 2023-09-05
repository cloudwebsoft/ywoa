<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ErrMsgException" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.base.IFormMacroCtl" %>
<%@ page import="com.redmoon.oa.dept.DeptChildrenCache" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.pvg.RoleDb" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="org.json.JSONObject" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String userName = privilege.getUser(request);
    String op = ParamUtil.get(request, "op");
    String action = ParamUtil.get(request, "action");

    String code = ParamUtil.get(request, "moduleCode");
    if ("".equals(code)) {
        code = ParamUtil.get(request, "code");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "formCode");
        }
    }
    if ("".equals(code)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
        return;
    }

    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(code);
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

    boolean isEditInplace = msd.getInt("is_edit_inplace") == 1;
    String formCode = msd.getString("form_code");

    if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT) {
        response.sendRedirect(request.getContextPath() + "/visual/module_list_gantt.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
        return;
    } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR) {
        response.sendRedirect(request.getContextPath() + "/visual/module_list_calendar.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
        return;
    } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_TREE) {
        boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
        if (!isInFrame) {
            response.sendRedirect(request.getContextPath() + "/visual/module_list_frame.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
            return;
        }
    } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_MODULE_TREE) {
        boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
        if (!isInFrame) {
            response.sendRedirect(request.getContextPath() + "/visual/module_basic_tree_frame.jsp" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
            return;
        }
    } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CUSTOM) {
        String moduleUrlList = StrUtil.getNullStr(msd.getString("url_list"));
        if (!"".equals(moduleUrlList)) {
            response.sendRedirect(request.getContextPath() + "/" + moduleUrlList + "?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
            return;
        }
    }

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

    Config cfg = Config.getInstance();

    String orderBy = ParamUtil.get(request, "orderBy");
    String sort = ParamUtil.get(request, "sort");

    if ("".equals(orderBy)) {
        String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
        boolean isComb = filter.startsWith("<items>") || "".equals(filter);
        // 如果是组合条件，则赋予后台设置的排序字段
        if (isComb) {
            orderBy = StrUtil.getNullStr(msd.getString("orderby"));
            sort = StrUtil.getNullStr(msd.getString("sort"));
        }
        if ("".equals(orderBy)) {
            orderBy = "id";
        }
    }

    if ("".equals(sort)) {
        sort = "desc";
    }

    request.setAttribute("moduleCode", code);

    // 置嵌套表需要用到的页面类型
    request.setAttribute("pageType", ConstUtil.PAGE_TYPE_LIST);

    String unitCode = ParamUtil.get(request, "unitCode");

    boolean isAutoHeight = msd.getInt("is_auto_height")==1;
%>
<!DOCTYPE html>
<html>
<head>
    <title><%=msd.getName()%></title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <style type="text/css">
        .page-main {
            margin: 10px 15px 0 15px;
        }
        #attDiv {
            margin-top: 10px;
        }
        .inplace_field {
            display: block !important;
        }
        <%=StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_LIST))%>
    </style>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="../js/jquery.editinplace.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
    <script src="../js/jquery.raty.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp"></script>
    <script src="../inc/ajax_getpage.jsp"></script>
    <script src="../js/BootstrapMenu.min.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../js/select2/i18n/zh-CN.js"></script>
    <style type="text/css">
        @import url("../util/jscalendar/calendar-win2k-2.css");
    </style>
    <script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <link rel="stylesheet" href="../js/soul-table/soulTable.css" media="all">
    <script src="../js/layui/layui.js" charset="utf-8"></script>
    <script>
        var requestParams = [];
        <%
            StringBuffer params = new StringBuffer();
            Enumeration enu = request.getParameterNames();
            while(enu.hasMoreElements()) {
                String paramName = (String)enu.nextElement();
                if ("code".equals(paramName) || "formCode".equals(paramName)) {
                    continue;
                }
                String paramVal = ParamUtil.get(request, paramName);
                StrUtil.concat(params, "&", paramName + "=" + StrUtil.UrlEncode(paramVal));
        %>
        requestParams.push({name: '<%=paramName%>', value: '<%=paramVal%>'});
        <%
            }
        %>
    </script>
    <script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp?op=<%=op%>&code=<%=code%>&pageType=moduleList&<%=params.toString()%>&time=<%=Math.random()%>"></script>
    <style>
        i {
            margin-right: 3px;
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
        .search-box {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
        }
        .cond-span {
            display: flex;
            float: left;
            align-items: center;
            text-align: left;
            width: 50%;
            height: 32px;
            margin: 3px 0;
        }
        .condBtnSearch {
            display: inline-block;
            float: left;
        }
        .cond-title {
            margin: 0;
            padding-right: 3px;
            width: 35%;
            text-align: right;
        }
        .cond-ctl {
            width: 65%;
            align-items: center;
            display: flex;
        }
        .cond-ctl input:not([type="radio"]),select {
            width: 90% !important;
        }
        .cond-ctl input[name$='FromDate'],input[name$='ToDate'] {
            width: 38% !important;
            margin: 0 2px;
        }
        .cond-ctl select[name$='_cond'] {
            width: 25% !important;
            margin-right:3px;
        }
        .cond-ctl select[name$='_cond']+input {
            width: 65% !important;
        }
        .cond-ctl select[name$='_cond']+select {
            width: 65% !important;
        }

    </style>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp" %>
<%
    int menuItem = ParamUtil.getInt(request, "menuItem", 1);
%>
<script>
    $('#menu1').addClass('current');
</script>
<div class="page-main">
    <%
        String querystr = "op=" + op + "&code=" + code + "&orderBy=" + orderBy + "&sort=" + sort + "&unitCode=" + unitCode;

        // 将过滤配置中request中其它参数也传至url中，这样分页时可传入参数
        String requestParams = "";
        String requestParamInputs = "";

        Map map = ModuleUtil.getFilterParams(request, msd);
        Iterator irMap = map.keySet().iterator();
        while (irMap.hasNext()) {
            String key = (String) irMap.next();
            String val = (String) map.get(key);
            requestParams += "&" + key + "=" + StrUtil.UrlEncode(val);
            requestParamInputs += "<input type='hidden' name='" + key + "' value='" + val + "' />";
        }
        querystr += requestParams;

        // 用于传过滤条件
        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
        String[] ary = null;
        try {
            ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
        } catch (ErrMsgException e) {
            out.print(e.getMessage());
            return;
        }

        String sqlUrlStr = ary[1];
        if (!"".equals(sqlUrlStr)) {
            querystr += "&" + sqlUrlStr;
        }

        // 加上二开传入的参数
        querystr += "&" + params.toString();

        int defaultPageSize = cfg.getInt("modulePageSize");
        int pagesize = ParamUtil.getInt(request, "pageSize", defaultPageSize);

        String[] fields = msd.getColAry(false, "list_field");
        if (fields == null || fields.length == 0) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "显示列未配置！"));
            return;
        }

        MacroCtlMgr mm = new MacroCtlMgr();

        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
        String[] btnNames = StrUtil.split(btnName, ",");
        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
        String[] btnScripts = StrUtil.split(btnScript, "#");
        String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
        String[] btnBclasses = StrUtil.split(btnBclass, ",");
        String btnRole = StrUtil.getNullStr(msd.getString("btn_role"));
        String[] btnRoles = StrUtil.split(btnRole, "#");

        boolean isToolbar = true;
        if (btnNames != null) {
            int len = btnNames.length;
            for (int i = 0; i < len; i++) {
                if (btnScripts[i].startsWith("{")) {
                    JSONObject json = new JSONObject(btnScripts[i]);
                    if (((String) json.get("btnType")).equals("queryFields")) {
                        if (json.has("isToolbar")) {
                            isToolbar = json.getInt("isToolbar") == 1;
                        }
                        break;
                    }
                }
            }
        }

        boolean isButtonsShow = false;
        isButtonsShow = (msd.getInt("btn_add_show") == 1 && mpd.canUserAppend(privilege.getUser(request))) ||
                (msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(privilege.getUser(request))) ||
                mpd.canUserDel(privilege.getUser(request)) ||
                mpd.canUserManage(privilege.getUser(request)) ||
                mpd.canUserImport(privilege.getUser(request)) ||
                mpd.canUserExport(privilege.getUser(request)) ||
                (btnNames != null && isToolbar);

        Map btnCanShowMap = new HashMap();
        if (btnNames != null && btnBclasses != null) {
            int len = btnNames.length;
            for (int i = 0; i < len; i++) {
                boolean isToolBtn = false;
                if (!btnScripts[i].startsWith("{")) {
                    isToolBtn = true;
                } else {
                    JSONObject json = new JSONObject(btnScripts[i]);
                    String btnType = json.getString("btnType");
                    if ("batchBtn".equals(btnType) || "flowBtn".equals(btnType)) {
                        isToolBtn = true;
                    }
                }
                if (isToolBtn) {
                    // 检查是否拥有权限
                    if (!privilege.isUserPrivValid(request, "admin")) {
                        boolean canSeeBtn = false;
                        if (btnRoles != null && btnRoles.length > 0) {
                            String roles = btnRoles[i];
                            String[] codeAry = StrUtil.split(roles, ",");
                            if (codeAry != null) {
                                UserDb user = new UserDb();
                                user = user.getUserDb(privilege.getUser(request));
                                RoleDb[] rdAry = user.getRoles();
                                if (rdAry != null) {
                                    for (RoleDb rd : rdAry) {
                                        String roleCode = rd.getCode();
                                        for (String codeAllowed : codeAry) {
                                            if (roleCode.equals(codeAllowed)) {
                                                canSeeBtn = true;
                                                break;
                                            }
                                        }
                                        if (canSeeBtn) {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                canSeeBtn = true;
                            }
                        } else {
                            canSeeBtn = true;
                        }
                        if (canSeeBtn) {
                            btnCanShowMap.put(btnNames[i], "");
                        }
                    }
                }
            }

            if (btnCanShowMap.size() > 0) {
                isButtonsShow = true;
            }
        }

        String strSearchTableDis = "";
        if (btnNames == null) {
            strSearchTableDis = "display:none";
        }

        boolean isShowUnitCode = false;
        Vector vtUnit = new Vector();
        DeptDb dd = new DeptDb();
        String myUnitCode = "";

        if (msd.getInt("is_unit_show") == 1) {
            myUnitCode = privilege.getUserUnitCode(request);
            dd = dd.getDeptDb(myUnitCode);

            vtUnit.addElement(dd);

            // 向下找两级单位
            DeptChildrenCache dl = new DeptChildrenCache(dd.getCode());
            java.util.Vector vt = dl.getDirList();
            Iterator irDept = vt.iterator();
            while (irDept.hasNext()) {
                dd = (DeptDb) irDept.next();
                if (dd.getType() == DeptDb.TYPE_UNIT) {
                    vtUnit.addElement(dd);
                    DeptChildrenCache dl2 = new DeptChildrenCache(dd.getCode());
                    Iterator ir2 = dl2.getDirList().iterator();
                    while (ir2.hasNext()) {
                        dd = (DeptDb) ir2.next();
                        if (dd.getType() == DeptDb.TYPE_UNIT) {
                            vtUnit.addElement(dd);
                        }

                        DeptChildrenCache dl3 = new DeptChildrenCache(dd.getCode());
                        Iterator ir3 = dl3.getDirList().iterator();
                        while (ir3.hasNext()) {
                            dd = (DeptDb) ir3.next();
                            if (dd.getType() == DeptDb.TYPE_UNIT) {
                                vtUnit.addElement(dd);
                            }
                        }
                    }
                }
            }

            // 如果是集团单位，且能够管理模块
            if (vtUnit.size() > 1 && mpd.canUserManage(privilege.getUser(request))) {
                isShowUnitCode = true;
            }
        }

        com.alibaba.fastjson.JSONArray colProps = ModuleUtil.getColProps(msd, false);
    %>
    <div>
        <div id="searchFormBox" class="search-form-box">
            <form id="searchForm" class="search-form">
                <div class="layui-inline">
                    <%
                        if (isShowUnitCode) {
                    %>
                    <span class="cond-span">
                        <select id="unitCode" name="unitCode">
                        <%if (privilege.getUserUnitCode(request).equals(DeptDb.ROOTCODE)) {%>
                        <option value="-1">不限</option>
                        <%}%>
                        <%
                            Iterator irUnit = vtUnit.iterator();
                            while (irUnit.hasNext()) {
                                dd = (DeptDb) irUnit.next();
                                int layer = dd.getLayer();
                                String layerStr = "";
                                for (int i = 2; i < layer; i++) {
                                    layerStr += "&nbsp;&nbsp;";
                                }
                                if (layer > 1) {
                                    layerStr += "├";
                                }
                        %>
                        <option value="<%=dd.getCode()%>"><%=layerStr%><%=dd.getName()%></option>
                        <%}%>
                        </select>
                        </span>
                    <%
                        }
                        // 显示查询条件
                        ArrayList<String> dateFieldNamelist = new ArrayList<String>();
                        int len = 0;

                        String condsHtml = ModuleUtil.getConditionHtml(request, msd, dateFieldNamelist);
                        boolean isQuery = !"".equals(condsHtml);
                        out.print(condsHtml);

                        // 当doQuery时，需要取相关的数据，所以上面的隐藏输入框必须得有
                        if (isQuery || isShowUnitCode) {
                    %>
                    <button class="layui-btn layui-btn-primary layui-btn-sm" style="<%=strSearchTableDis %>" data-type="reload"><i class="fa fa-search"></i>搜索</button>
                    <%
                        }
                    %>
                    <input type="hidden" name="op" value="search"/>
                    <input type="hidden" name="moduleCode" value="<%=code%>"/>
                    <%=requestParamInputs%>
                    <input type="hidden" name="menuItem" value="<%=menuItem%>"/>
                    <input type="hidden" name="mainCode" value="<%=ParamUtil.get(request, "mainCode")%>"/>
                </div>
            </form>
        </div>
        <script>
            <%
            if (!isQuery && !isShowUnitCode) {
            %>
            $('#searchFormBox').hide();
            <%
            }
            %>
        </script>
        <div id="dlg" style="display:none">
            <%if (isShowUnitCode) {%>
            将数据迁移至：<select id="toUnitCode" name="toUnitCode">
            <%
                Iterator irUnit = vtUnit.iterator();
                while (irUnit.hasNext()) {
                    dd = (DeptDb) irUnit.next();
            %>
            <option value="<%=dd.getCode()%>"><%=dd.getName()%>
            </option>
            <%}%>
        </select>
            <%}%>
        </div>
        <%
            boolean hasView = false;
            FormViewDb formViewDb = new FormViewDb();
            Vector vtView = formViewDb.getViews(formCode);
            if (vtView.size() > 0) {
                hasView = true;
                Iterator irView = vtView.iterator();
        %>
        <div id="dlgView" style="display:none">
            <select id="formViewId" name="formViewId">
                <option value="<%=ConstUtil.MODULE_EXPORT_WORD_VIEW_FORM%>">根据表单</option>
                <%
                    while (irView.hasNext()) {
                        formViewDb = (FormViewDb) irView.next();
                %>
                <option value="<%=formViewDb.getLong("id")%>"><%=formViewDb.getString("name")%>
                </option>
                <%
                    }
                %>
            </select>
        </div>
        <%
            }
        %>
        <span id="switcher" style="cursor:pointer; position: absolute; display: none">
        <img id="switchBtn" src="../images/hide.png" title="显示/隐藏 查询区域"/>
        </span>
        <script>
            $(function () {
                var $box = $('#searchFormBox');
                var l = $box.offset().left + $box.width();
                var t = $box.offset().top;
                $('#switcher').css({'top': t + 'px', 'left': l + 'px'});

                var $btn = $('#switchBtn');
                var $form = $('#searchForm');
                $('#switcher').click(function () {
                    if ($btn.attr('src').indexOf("show.png") != -1) {
                        $form.show();
                        $btn.attr('src', '../images/hide.png');
                        $('#switcher').css({'top': t + 'px', 'left': l + 'px'});
                    } else {
                        $form.hide();
                        $btn.attr('src', '../images/show.png');
                        $('#switcher').css({'top': (t - 20) + 'px', 'left': l + 'px'});
                    }
                });
            });
        </script>

        <%
            boolean canManage = mpd.canUserManage(userName);
        %>
        <table class="layui-hide" id="table_list" lay-filter="<%=code%>"></table>
        <script type="text/html" id="toolbar_list">
            <div class="layui-btn-container">
                <%if (msd.getInt("btn_add_show") == 1 && mpd.canUserAppend(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="add" title="增加"><i class="fa fa-plus-circle"></i></i></i>增加</button>
                <%}%>
                <%if (msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-orange" lay-event="edit" title="修改"><i class="fa fa-pencil"></i>修改</button>
                <%}%>
                <%if (msd.getInt("btn_del_show") == 1 && (mpd.canUserDel(userName) || canManage)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-red" lay-event="delRows" title="删除"><i class="layui-icon layui-icon-delete"></i>删除</button>
                <%}%>
                <%if (canManage && isShowUnitCode) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="move" title="迁移"><i class="fa fa-share-square-o"></i>迁移</button>
                <%}%>
                <%if (mpd.canUserImport(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="importXls" title="导入Excel文件"><i class="fa fa-arrow-circle-o-down"></i>导入</button>
                <%}%>
                <%if (mpd.canUserExport(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="exportXls" title="导出Excel文件"><i class="fa fa-file-excel-o"></i>导出</button>
                <%}%>
                <%if (mpd.canUserExportWord(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="exportWord" title="生成word文件"><i class="layui-icon layui-icon-form"></i>生成</button>
                <%}%>
                <%
                    if (btnNames != null && btnBclasses != null) {
                        len = btnNames.length;
                        for (int i = 0; i < len; i++) {
                            boolean isToolBtn = false;
                            if (!btnScripts[i].startsWith("{")) {
                                isToolBtn = true;
                            } else {
                                JSONObject json = new JSONObject(btnScripts[i]);
                                String btnType = json.getString("btnType");
                                if ("batchBtn".equals(btnType) || "flowBtn".equals(btnType)) {
                                    isToolBtn = true;
                                }
                            }
                            if (isToolBtn) {
                                // 检查是否拥有权限
                                if (!privilege.isUserPrivValid(request, "admin")) {
                                    boolean canSeeBtn = false;
                                    if (btnRoles != null && btnRoles.length > 0) {
                                        String roles = btnRoles[i];
                                        String[] codeAry = StrUtil.split(roles, ",");
                                        // 如果codeAry为null，则表示所有人都能看到
                                        if (codeAry == null) {
                                            canSeeBtn = true;
                                        } else {
                                            UserDb user = new UserDb();
                                            user = user.getUserDb(privilege.getUser(request));
                                            RoleDb[] rdAry = user.getRoles();
                                            if (rdAry != null) {
                                                for (RoleDb roleDb : rdAry) {
                                                    String roleCode = roleDb.getCode();
                                                    for (String codeAllowed : codeAry) {
                                                        if (roleCode.equals(codeAllowed)) {
                                                            canSeeBtn = true;
                                                            break;
                                                        }
                                                    }
                                                    if (canSeeBtn) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        canSeeBtn = true;
                                    }

                                    if (!canSeeBtn) {
                                        continue;
                                    }
                                }
                %>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="event<%=i%>">
                    <i class="fa <%=btnBclasses[i]%>"></i>
                    <%=btnNames[i]%>
                </button>
                <%
                            }
                        }
                    }
                %>
                <%if (privilege.isUserPrivValid(request, "admin")) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary layui-border-blue" lay-event="manage"><i class="layui-icon layui-icon-set"></i>管理</button>
                <%}%>
            </div>
        </script>
    </div>
    <script>
        var mapEditable = new Map();
        var mapEditableOptions = new Map();
        var mapCheckboxPresent = new Map;
    <%
        if (isEditInplace) {
            // 取得当前用户的可写字段，对在位编辑的字段进行初始化
            String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
            if (fieldWrite!=null && !"".equals(fieldWrite)) {
                String[] fds = StrUtil.split(fieldWrite, ",");
                if (fds != null) {
                    for (String fieldName : fds) {
                        FormField ff = fd.getFormField(fieldName);
                        if (ff.getType().equals(FormField.TYPE_TEXTAREA) || ff.getType().equals(FormField.TYPE_TEXTFIELD)
                                || ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME) || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
    %>
    mapEditable.put("<%=fieldName%>", "<%=ff.getType()%>");
    <%
        if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
            ff.setValue("1");
            // 取得present
    %>
    mapCheckboxPresent.put("<%=fieldName%>", "<%=ff.convertToHtml()%>")
    <%
                    }
                }
            }
        }
    }
    else {
        Iterator ir = fd.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu!=null) {
                    IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                    String type = ifmc.getControlType();
                    if (type.equals(FormField.TYPE_TEXTFIELD)) {
    %>
    mapEditable.put("<%=ff.getName()%>", "<%=FormField.TYPE_TEXTFIELD%>");
    <%
    }
    else if (type.equals(FormField.TYPE_SELECT)) {
        StringBuffer sb = new StringBuffer();
        String opts = ifmc.getControlOptions(userName, ff);
        try {
            org.json.JSONArray arr = new org.json.JSONArray(opts);
            for (int i=0; i<arr.length(); i++) {
                JSONObject json = arr.getJSONObject(i);
                // 不能用getString，因为有些可能为int型
                StrUtil.concat(sb, ",", json.get("name") + ":" + json.get("value"));
            }
        }
        catch(org.json.JSONException e) {
            DebugUtil.e("moduleListPage.do", "选项json解析错误，字段：", ff.getTitle() + " " + ff.getName() + " 中选项为：" + opts);
            // e.printStackTrace();
        }
    %>
    mapEditable.put("<%=ff.getName()%>", "<%=FormField.TYPE_SELECT%>");
    mapEditableOptions.put("<%=ff.getName()%>", "<%=sb.toString()%>");
    <%
            }
        }
    }
    else if (ff.getType().equals(FormField.TYPE_SELECT)) {
        String[][] aryOpt = FormParser.getOptionsArrayOfSelect(fd, ff);
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<aryOpt.length; i++) {
            StrUtil.concat(sb, ",", aryOpt[i][0] + ":" + aryOpt[i][1]);
        }
    %>
    mapEditable.put("<%=ff.getName()%>", "<%=FormField.TYPE_SELECT%>");
    mapEditableOptions.put("<%=ff.getName()%>", "<%=sb.toString()%>");
    <%
    }
    else if (ff.getType().equals(FormField.TYPE_TEXTAREA) || ff.getType().equals(FormField.TYPE_TEXTFIELD)
            || ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME) || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
    %>
    mapEditable.put("<%=ff.getName()%>", "<%=ff.getType()%>");
    <%
        if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
            ff.setValue("1");
            // 取得present
    %>
    mapCheckboxPresent.put("<%=ff.getName()%>", "<%=ff.convertToHtml()%>")
    <%
                        }
                    }
                }
            }
        }
        %>
    </script>
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
                , toolbar: '#toolbar_list'
                , defaultToolbar: ['filter', 'print'/*, 'exports', {
				title: '提示'
				,layEvent: 'LAYTABLE_TIPS'
				,icon: 'layui-icon-tips'
			}*/]
                , drag: {toolbar: true}
                , method: 'post'
                , url: 'moduleList.do?<%=querystr%>'
                , cols: [
                    <%=colProps.toString()%>
                ]
                , id: 'tableList'
                , page: true
                , unresize: false
                , limit: <%=pagesize%>
                <%if (isAutoHeight) {%>
                , height: 'full-98'
                <%}%>
                , parseData: function (res) { //将原始数据解析成 table 组件所规定的数据
                    return {
                        "code": res.errCode, //解析接口状态
                        "msg": res.msg, //解析提示文本
                        "count": res.total, //解析数据长度
                        "data": res.rows //解析数据列表
                    };
                }
                ,done: function(res, curr, count){
                    tableData = res.data;
                    soulTable.render(this);
                }
            });

            //头工具栏事件
            table.on('toolbar()', function (obj) {
                var checkStatus = table.checkStatus(obj.config.id);
                switch (obj.event) {
                    case 'add':
                        window.location.href = "<%=request.getContextPath()%>/visual/moduleAddPage.do?code=<%=code%>&<%=params%>";
                        /*layer.open({
                            type: 2,
                            title: '增加',
                            shadeClose: true,
                            shade: 0.6,
                            area: ['90%', '90%'],
                            content: 'moduleAddPage.do?isTabStyleHor=false&code=<%=StrUtil.UrlEncode(code)%>&<%=params%>&isShowNav=0'
                        });*/
                        break;
                    case 'edit':
                        var data = checkStatus.data;
                        if (data.length == 0) {
                            layer.msg('请选择记录');
                            return;
                        } else if (data.length > 1) {
                            layer.msg('只能选择一条记录');
                            return;
                        }
                        var id = data[0].id;

                        var tabId = getActiveTabId();
                        <%if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {%>
                            addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/<%=msd.getString("url_edit")%>?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>" + "&tabIdOpener=" + tabId);
                        <%}else{%>
                            addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/moduleEditPage.do?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>" + "&tabIdOpener=" + tabId);
                        <%}%>
                        /*layer.open({
                            type: 2,
                            title: '修改',
                            shadeClose: true,
                            shade: 0.6,
                            area: ['90%', '90%'],
                            content: 'module_edit.jsp?isTabStyleHor=false&code=<%=StrUtil.UrlEncode(code)%>&id=' + id + '&formCode=<%=formCode%>&isShowNav=0'
                        });*/
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
                            //do something
                            try {
                                onBeforeModuleDel(ids);
                            } catch (e) {
                            }

                            $.ajax({
                                type: "post",
                                url: "moduleDel.do",
                                data: {
                                    code: "<%=code%>",
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
                    case 'importXls':
                        var url = "module_import_excel.jsp?formCode=<%=formCode%>&code=<%=code%>&isShowNav=0";
                        window.location.href = url;
                        break;
                    case 'exportXls':
                        var cols = "";
                        // 找出未隐藏的表头
                        $("div[lay-id='" + obj.config.id + "']").find('.layui-table th').each(function () {
                            if ($(this).data("field") && $(this).data("field") != "0" && $(this).data("field") != "colOperate") {
                                if (!$(this).hasClass('layui-hide')) {
                                    if (cols == "") {
                                        cols = $(this).data("field");
                                    } else {
                                        cols += "," + $(this).data("field");
                                    }
                                }
                            }
                        });
                    <%
                    String expUrl = "";
                    // 检查是否设置有模板
                    Vector vt = ModuleExportTemplateMgr.getTempaltes(request, msd.getString("form_code"));
                    if (vt.size()>0) {
                        String querystrTempl = "code=" + code;
                        expUrl = request.getContextPath() + "/visual/module_excel_sel_templ.jsp?" + querystrTempl;
                    }
                    else {
                        expUrl = request.getContextPath() + "/visual/exportExcel.do";
                    }
                    %>
                        // 生成表单，以post方式，否则IE11下，某些参数可能会有问题
                        // 如果用window.open方式，则IE11中当含有coo_address、coo_address_cond时，接收到coo_address的值为?_address_cond=0?_address=，而chrome中不会
                        var expForm = o("exportForm");
                        if (expForm != null) {
                            expForm.parentNode.removeChild(expForm);
                        }
                        expForm = document.createElement("FORM");
                        document.body.appendChild(expForm);

                        expForm.style.display = "none";
                        expForm.target = "_blank";
                        expForm.method = "post";
                        expForm.action = "<%=expUrl%>";
                        var fields = $("#searchForm").serializeArray();
                        jQuery.each(fields, function (i, field) {
                            expForm.innerHTML += "<input name='" + field.name + "' value='" + field.value + "'/>";
                        });
                        expForm.innerHTML += "<input name='cols' value='" + cols + "'/>";
                        // console.log(expForm);
                        expForm.submit();
                        break;
                    case 'exportWord':
                        var ids = getIdsSelected();
                        <%
                        int exportWordView = msd.getInt("export_word_view");
                        if (hasView && exportWordView == ConstUtil.MODULE_EXPORT_WORD_VIEW_SELECT) {
                        %>
                            $("#dlgView").dialog({
                                title:'请选择',
                                modal: true,
                                width: 350,
                                height: 160,
                                // bgiframe:true,
                                buttons: {
                                    '<lt:Label res="res.flow.Flow" key="cancel"/>': function() {
                                        $(this).dialog("close");
                                    },
                                    '<lt:Label res="res.flow.Flow" key="sure"/>': function() {
                                        window.open('<%=request.getContextPath()%>/visual/exportWord?formViewId=' + $('#formViewId').val() + '&ids=' + ids + "&code=<%=code%>&" + $("form").serialize());
                                        $(this).dialog("close");
                                    }
                                },
                                closeOnEscape: true,
                                draggable: true,
                                resizable:true
                            });
                        <%
                        }
                        else {
                        %>
                            window.open('<%=request.getContextPath()%>/visual/exportWord?ids=' + ids + "&code=<%=code%>&" + $("form").serialize());
                        <%
                        }
                        %>
                        break;
                    case 'move':
                        var ids = getIdsSelected();
                        if (ids=='') {
                            jAlert('请选择记录!','提示');
                            return;
                        }
                        jQuery("#dlg").dialog({
                            title: "迁移",
                            modal: true,
                            // bgiframe:true,
                            buttons: {
                                "取消": function() {
                                    jQuery(this).dialog("close");
                                },
                                "确定": function() {
                                    var ids = getIdsSelected();
                                    $.ajax({
                                        type: "post",
                                        url: "moduleSetUnitCode.do",
                                        data : {
                                            code: "<%=code%>",
                                            ids: ids,
                                            toUnitCode: $("#toUnitCode").val()
                                        },
                                        dataType: "html",
                                        beforeSend: function(XMLHttpRequest){
                                            //ShowLoading();
                                        },
                                        success: function(data, status){
                                            data = $.parseJSON(data);
                                            if (data.ret=="0") {
                                                jAlert(data.msg, "提示");
                                            }
                                            else {
                                                jAlert(data.msg, "提示");
                                                window.location.href = "<%=request.getRequestURL()+"?"+request.getQueryString()%>";
                                            }
                                        },
                                        complete: function(XMLHttpRequest, status){
                                            // HideLoading();
                                        },
                                        error: function(XMLHttpRequest, textStatus){
                                            // 请求出错处理
                                            alert("error:" + XMLHttpRequest.responseText);
                                        }
                                    });

                                    jQuery(this).dialog("close");
                                }
                            },
                            closeOnEscape: true,
                            draggable: true,
                            resizable:true,
                            width:300
                        });
                        break;
                    case 'manage':
                        addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=msd.getString("form_code")%>&code=<%=msd.getString("code")%>");
                        break;
                    //自定义头工具栏右侧图标 - 提示
                    case 'LAYTABLE_TIPS':
                        layer.alert('这是工具栏右侧自定义的一个图标按钮');
                        break;
                    <%
                    if (btnNames!=null) {
                        len = btnNames.length;
                        for (int i=0; i<len; i++) {
                            if (!btnScripts[i].startsWith("{")) {
                            %>
                    case 'event<%=i%>':
                        <%=ModuleUtil.renderScript(request, btnScripts[i])%>
                        break;
                    <%
                    }
                    else {
                        JSONObject json = new JSONObject(btnScripts[i]);
                        if ((json.get("btnType")).equals("batchBtn")) {
                            String batchField = json.getString("batchField");
                            String batchValue = json.getString("batchValue");
                        %>
                    case 'event<%=i%>':
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
                        jConfirm("您确定要<%=btnNames[i]%>么？", "提示", function (r) {
                            if (!r) {
                                return;
                            } else {
                                batchOp(ids, "<%=batchField%>", "<%=batchValue%>");
                            }
                        })
                        break;
                    <%
                        }
                        else if ("flowBtn".equals(json.get("btnType"))) {
                            String flowTypeCode = json.getString("flowTypeCode");
                            Leaf lf = new Leaf();
                            lf = lf.getLeaf(flowTypeCode);
                            if (lf == null) {
                                DebugUtil.e(getClass(), "流程型按钮 flowTypeCode", flowTypeCode + " 不存在");
                            }
                            else {
                        %>
                        case 'event<%=i%>':
                            addTab('<%=lf.getName()%>', '<%=request.getContextPath()%>/flow_initiate1_do.jsp?typeCode=<%=flowTypeCode%>');
                            break;
                            <%
                                }
                            }
                        }
                    }
                }
                %>
                }
            });

            $(document).on('click','.layui-table-cell',function(){
                var $parent = $(this).parent();
                var dataIndex = $parent.parent().attr('data-index');
                // 如果所点的是数据行
                if (dataIndex >= 0) {
                    var id = tableData[dataIndex].id;
                    var fieldName = $parent.attr('data-field');
                    editCol(this, id, fieldName);
                }
            })

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

        function doQuery() {
            layui.table.reload('tableList');
        }

        function initCalendar() {
            <%for (String ffname : dateFieldNamelist) {%>
            $('#<%=ffname%>').datetimepicker({
                lang: 'ch',
                timepicker: false,
                format: 'Y-m-d'
            });
            <%}%>
        }

        function editCol(celDiv, id, colName) {
            if (!<%=isEditInplace%>) {
                return;
            }
            if (!mapEditable.containsKey(colName)) {
                return;
            }

            var fieldType = mapEditable.get(colName).value.toLowerCase();
            var selectOptions = "";
            var opts = mapEditableOptions.get(colName);
            if (opts!=null) {
                selectOptions = opts.value;
            }

            console.log(colName, selectOptions);

            // 该插件会上传值：original_value、update_value
            $(celDiv).editInPlace({
                field_type: fieldType,
                url: "moduleEditInPlace.do",
                saving_text: "保存中...",
                saving_image: "../images/loading.gif",
                select_text: "请选择",
                select_options: selectOptions,
                checkbox_present: mapCheckboxPresent.get(colName) != null ? mapCheckboxPresent.get(colName).value : "",
                params: "colName=" + colName + "&id=" + id + "&code=<%=StrUtil.UrlEncode(code)%>",
                error:function(obj){
                    alert(JSON.stringify(obj));
                },
                success:function(data) {
                    data = $.parseJSON(data);
                    if (data.ret==-1) { // 值未更改
                        return;
                    }
                    else {
                        $.toaster({
                            "priority" : "info",
                            "message" : data.msg
                        });
                        doQuery();
                    }
                }
            });
        }

        $(function() {
            $('body').addClass('form-inline');
            $('input, select, textarea').each(function() {
                if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && !$(this).hasClass('tSearch') && $(this).attr('type') != 'hidden' && $(this).attr('type') != 'file') {
                    $(this).addClass('form-control');
                }
                $(this).attr('autocomplete', 'off');
            });
        })
    </script>
</div>
</body>
</html>