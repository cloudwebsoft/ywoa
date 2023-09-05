<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.person.UserMgr" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ page import="com.cloudweb.oa.utils.I18nUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudweb.oa.service.ModuleService" %>
<%@ page import="com.cloudweb.oa.service.ModuleLogService" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="com.cloudweb.oa.cond.CondUtil" %>
<%@ page import="com.cloudweb.oa.cond.CondUnit" %>
<%@ page import="com.redmoon.oa.pvg.RoleDb" %>
<%@ page import="com.redmoon.oa.visual.Attachment" %>
<%@ page import="com.redmoon.oa.base.IAttachment" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

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
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
        return;
    }

    int id = ParamUtil.getInt(request, "id", -1);
    if (id == -1) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
        return;
    }

    ModulePrivDb mpd = new ModulePrivDb(code);
    if (!mpd.canUserView(privilege.getUser(request))) {
        boolean canShow = false;
        // 从嵌套表格查看时访问
        String visitKey = ParamUtil.get(request, "visitKey");
        if (!"".equals(visitKey)) {
            String fId = String.valueOf(id);
            com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
            String desKey = ssoconfig.get("key");
            visitKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, visitKey);
            if (visitKey.equals(fId)) {
                canShow = true;
            }
        }
        if (!canShow) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    // 检查数据权限，判断用户是否可以存取此条数据
    if (!ModulePrivMgr.canAccessData(request, msd, id)) {
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, i18nUtil.get("info_access_data_fail")));
        return;
    }

    String formCode = msd.getString("form_code");
    if ("".equals(formCode)) {
        out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
        return;
    }

    if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_TREE) {
        boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
        if (!isInFrame) {
            response.sendRedirect(request.getContextPath() + "/" + "visual/module_show_frame.jsp?id=" + id + "&code=" + code);
            return;
        }
    }

    String op = ParamUtil.get(request, "op");
    int parentId = ParamUtil.getInt(request, "parentId", -1);
    if (parentId == -1) {
        parentId = id;
    }

    String userName = privilege.getUser(request);
    Config cfg = Config.getInstance();
    // 创建浏览日志
    if (cfg.getBooleanProperty("isModuleLogRead") && !formCode.equals(ConstUtil.MODULE_CODE_LOG) && !formCode.equals(ConstUtil.MODULE_CODE_LOG_READ) && !formCode.equals(ConstUtil.FORM_FORMULA)) {
        ModuleLogService moduleLogService = SpringUtil.getBean(ModuleLogService.class);
        moduleLogService.logRead(formCode, code, id, userName, privilege.getUserUnitCode(request));
    }

    request.setAttribute("moduleCode", code);

    // 置嵌套表及关联查询选项卡生成链接需要用到的cwsId
    request.setAttribute("cwsId", "" + id);
    // 置嵌套表需要用到的页面类型
    request.setAttribute("pageType", ConstUtil.PAGE_TYPE_SHOW);
    // 置NestSheetCtl需要用到的formCode
    request.setAttribute("formCode", formCode);

    FormMgr fm = new FormMgr();
    FormDb fd = fm.getFormDb(formCode);

    com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);

    int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

    request.setAttribute("id", id);
    request.setAttribute("code", code);
    request.setAttribute("isShowNav", isShowNav);
    request.setAttribute("isHasAttachment", fd.isHasAttachment());
    request.setAttribute("skinPath", SkinMgr.getSkinPath(request));

    com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);

    fdm.runScriptOnSee(request, privilege, msd, fdao);

    Vector<IAttachment> vAttach = fdao.getAttachments();

    request.setAttribute("vAttach", vAttach);
    request.setAttribute("canUserLog", mpd.canUserLog(privilege.getUser(request)));

    request.setAttribute("btn_print_display", msd.getInt("btn_print_display") == 1);
    request.setAttribute("btn_edit_display", msd.getInt("btn_edit_display") == 1 && (mpd.canUserModify(privilege.getUser(request)) || mpd.canUserManage(privilege.getUser(request))));

    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
    request.setAttribute("rend", rd.report(msd));
%>
<!DOCTYPE html>
<html>
<head>
    <title>智能模块设计-重直显示</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <style type="text/css">
        .page-main {
            margin: auto 15px;
        }

        #attDiv {
            margin-top: 10px;
        }

        <%=msd.getCss(ConstUtil.PAGE_TYPE_SHOW)%>
    </style>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery.raty.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp"></script>
    <script src="../flow/form_js/form_js_${formCode}.jsp?pageType=show&id=${id}&moduleCode=${code}&time=<%=Math.random()%>"></script>
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
    <style>
        #loading {
            position: fixed;
            z-index: 400;
            width: 100%;
            height: 100%;
            top: 0;
            left: 0;
            text-align: center;
            font-size: 0.9rem;
            color: #595758;
            background-color: #ffffff;
            /*
            filter: alpha(Opacity=60);
            -moz-opacity: 0.6;
            opacity: 0.6;
            */
        }

        .cond-title {
            margin: 0 5px;
        }
    </style>
</head>
<body>
<div id="loading">
    <img src="../images/loading.gif" alt="loading.." style="margin-top:50px;"/>
</div>
<div class="page-main">
    <div id="visualDiv">
        <table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
                <td align="left">
                    <form name="visualForm" id="visualForm">
                        <table width="100%">
                            <tr>
                                <td>
                                    ${rend}
                                </td>
                            </tr>
                        </table>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="left"></td>
            </tr>
            <c:if test="${isHasAttachment}">
                <tr>
                    <td align="left">
                        <c:if test="${fn:length(vAttach)>0}">
                            <div id="attDiv">
                                <table id="attTable" class="tabStyle_1 percent98" width="98%" border="0" align="center"
                                       cellpadding="0" cellspacing="0">
                                    <tr>
                                        <td height="31" align="right" class="tabStyle_1_title">&nbsp;</td>
                                        <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="fileName"/></td>
                                        <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="creator"/></td>
                                        <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="time"/></td>
                                        <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="size"/></td>
                                        <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="operate"/></td>
                                    </tr>
                                    <c:forEach items="${vAttach}" var="att">
                                        <tr id="trAtt${att.id}%>">
                                            <td width="2%" height="31" align="center"><img src="../images/attach.gif"/></td>
                                            <td width="51%" align="left">
                                                &nbsp;
                                                <span id="spanAttLink${att.id}">
                                <a href="preview.do.jsp?attachId=${att.id}&visitKey=${att.visitKey}" target="_blank">
                                    <span id="spanAttName${att.id}">${att.name}</span>
                                </a>
                                </span>
                                            </td>
                                            <td width="10%" align="center">${att.creatorRealName}
                                            </td>
                                            <td width="15%" align="center"><fmt:formatDate value='${att.createDate}' pattern='yyyy-MM-dd HH:mm'/>
                                            </td>
                                            <td width="11%" align="center">${att.fileSizeMb}M
                                            </td>
                                            <td width="11%" align="center">
                                                <a href="download.do?attachId=${att.id}&visitKey=${att.visitKey}&isDownload=true" target="_blank">
                                                    <lt:Label res="res.flow.Flow" key="download"/>
                                                </a>
                                                <c:if test="${canUserLog}">
                                                    &nbsp;&nbsp;<a href="javascript:;" onClick="addTab('${att.name} 日志', '${pageContext.request.contextPath}/visual/att_log_list.jsp?attId=${att.id}')">日志</a>
                                                </c:if>
                                                <c:if test="${att.previewUrl!=''}">
                                                    &nbsp;&nbsp;<a href="javascript:;" onClick="addTab('${att.name}', '${pageContext.request.contextPath}/${att.previewUrl}">预览</a>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </table>
                            </div>
                        </c:if>
                    </td>
                </tr>
            </c:if>
            <tr>
                <td height="30" align="center" style="padding: 10px 0px">
                    <input name="id" value="${id}" type="hidden"/>
                    <c:if test="${btn_print_display}">
                        <button class="btn btn-default" onclick="showFormReport()">打印</button>
                    </c:if>
                    <!--
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <input class="btn" type="button" onclick="exportToWord()" value="导出Word"/>
                    -->
                    <c:if test="${btn_edit_display}">
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <button class="btn btn-default" onclick="window.location.href='moduleEditPage.do?parentId=${id}&id=${id}&isShowNav=${isShowNav}&code=${code}'">编辑</button>
                    </c:if>
                </td>
            </tr>
        </table>
    </div>
    <form id="formWord" name="formWord" target="_blank" action="module_show_word.jsp" method="post">
        <textarea name="cont" style="display:none"></textarea>
    </form>
    <script src="../js/layui/layui.js" charset="utf-8"></script>
    <script>
        function getPrintContent() {
            var str = "<div style='text-align:center;margin-top:10px'>" + $('#visualDiv').html() + "</div>";
            return str;
        }

        function showFormReport() {
            window.open('../print_preview.jsp?print=true', '', 'left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
        }

        function exportToWord() {
            o("cont").value = o("formDiv").innerHTML;
            o("formWord").submit();
        }

        // i从1开始
        function getCellValue(i, j) {
            var obj = document.getElementById("cwsNestTable");
            var cel = obj.rows.item(i).cells;

            var fieldType = Main_Tab.rows[0].cells[j].getAttribute("type");
            var macroType = Main_Tab.rows[0].cells[j].getAttribute("macroType");

            // 标值控件
            if (macroType == "macro_raty") {
                if (cel[j].children[0] && cel[j].children[0].tagName == "SPAN") {
                    var ch = cel[j].children[0].children;
                    for (var k = 0; k < ch.length; k++) {
                        if (ch[k].tagName == "INPUT") {
                            return ch[k].value;
                        }
                    }
                }
            }
            // 在clear_color时，会置宏控件所在单元格的value属性为控件的值
            else if (cel[j].getAttribute("value")) {
                return cel[j].getAttribute("value");
            } else {
                if (cel[j].children.length > 0) {
                    var cellDiv = cel[j].children[0];
                    return cellDiv.innerText.trim();
                } else {
                    return cel[j].innerText.trim();
                }
            }
            return "";
        }

        /*
            $.when($.ajax(), $.ajax()).then(function() {
                // 所有 AJAX 请求已完成
                try {
                    // 可在form_js_***.jsp中写此方法
                    onAjaxLoaded();
                }
                catch (e) {}
                $("#loading").hide();
            })
            */
        // 前提：所有ajax请求都是用jQuery的$.ajax发起的，而非原生的XHR；
        var ajaxBack = $.ajax;
        var ajaxCount = 0;
        var allAjaxDone = function () {
            // 所有 AJAX 请求已完成
            try {
                // 可在form_js_***.jsp中写此方法
                onAjaxLoaded();
            } catch (e) {
            }
            $("#loading").hide();
        };
        // 由于get/post/getJSON等，最后还是调用到ajax，因此只要改ajax函数即可
        $.ajax = function (setting) {
            ajaxCount++;
            var cb = setting.complete;
            setting.complete = function () {
                if ($.isFunction(cb)) {
                    cb.apply(setting.context, arguments);
                }
                ajaxCount--;
                if (ajaxCount == 0 && $.isFunction(allAjaxDone)) {
                    allAjaxDone();
                }
            };
            ajaxBack(setting);
        };

        $(function () {
            // 如果没有ajax，则ajaxCount为0，应置loading为hide
            if (ajaxCount == 0) {
                $("#loading").hide();
            }
        })

        <%
        if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
        %>
        // 不能放在$(function中，原来的tabStyle_8风格会闪现
        // $(function() {
        var $table = $('#visualForm').find('.tabStyle_8');
        $table.addClass('layui-table');
        $table.removeClass('tabStyle_8');
        // })
        <%
        }
        %>
    </script>
    <%
        // -----------以下为关联模块部分---------------
    %>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <%
        ArrayList<String> dateFieldNamelist = new ArrayList<String>();
        ModuleRelateDb mrd = new ModuleRelateDb();
        Iterator ir = mrd.getModulesRelated(formCode).iterator();
        while (ir.hasNext()) {
            mrd = (ModuleRelateDb) ir.next();
            String moduleCodeRelated = mrd.getString("relate_code");

            ModulePrivDb mpdRelated = new ModulePrivDb(moduleCodeRelated);
            if (!mpdRelated.canUserSee(privilege.getUser(request))) {
                continue;
            }

            if (mpdRelated.canUserSee(userName) && mrd.getInt("is_on_tab") == 1) {
                // 条件检查
                String conds = StrUtil.getNullStr(mrd.getString("conds"));
                if (!"".equals(conds)) {
                    String cond = ModuleUtil.parseConds(request, fdao, conds);
                    javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
                    javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
                    try {
                        Boolean ret = (Boolean) engine.eval(cond);
                        if (!ret.booleanValue()) {
                            continue;
                        }
                    } catch (javax.script.ScriptException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            ModuleSetupDb msdRelated = msd.getModuleSetupDbOrInit(moduleCodeRelated);
            int defaultPageSize = cfg.getInt("modulePageSize");
            int pagesize = ParamUtil.getInt(request, "pageSize", defaultPageSize);
            String mode = ""; // subTagRelated 通过选项卡标签关联
            String tagName = "";

            String formCodeRelated = msdRelated.getString("form_code");
            FormDb fdRelated = new FormDb();
            fdRelated = fdRelated.getFormDb(formCodeRelated);
            String[] fields = msdRelated.getColAry(false, "list_field");
            String[] fieldsWidth = msdRelated.getColAry(false, "list_field_width");
            String[] fieldsShow = msdRelated.getColAry(false, "list_field_show");
            String[] fieldsTitle = msdRelated.getColAry(false, "list_field_title");

            String btnName = StrUtil.getNullStr(msdRelated.getString("btn_name"));
            String[] btnNames = StrUtil.split(btnName, ",");
            String btnScript = StrUtil.getNullStr(msdRelated.getString("btn_script"));
            String[] btnScripts = StrUtil.split(btnScript, "#");
            String btnBclass = StrUtil.getNullStr(msdRelated.getString("btn_bclass"));
            String[] btnBclasses = StrUtil.split(btnBclass, ",");
            String btnRole = StrUtil.getNullStr(msdRelated.getString("btn_role"));
            String[] btnRoles = StrUtil.split(btnRole, "#");
    %>
    <style>
        <%=msdRelated.getCss(ConstUtil.PAGE_TYPE_LIST)%>
    </style>
    <div>
        <div id="searchFormBox<%=moduleCodeRelated%>" class="search-form-box">
            <form id="searchForm<%=moduleCodeRelated%>" class="search-form search-form-<%=formCodeRelated%>">
                <div class="layui-inline">
                    <%
                        MacroCtlMgr mm = new MacroCtlMgr();
                        int len = 0;
                        boolean isQuery = false;

                        if (btnNames != null) {
                            len = btnNames.length;
                            for (int i = 0; i < len; i++) {
                                if (btnScripts[i].startsWith("{")) {
                                    Map<String, String> checkboxGroupMap = new HashMap<String, String>();
                                    JSONObject json = new JSONObject(btnScripts[i]);
                                    if (json.get("btnType").equals("queryFields")) {
                                        isQuery = true;

                                        String condFields = (String) json.get("fields");
                                        String condTitles = "";
                                        if (json.has("titles")) {
                                            condTitles = (String) json.get("titles");
                                        }
                                        String[] fieldAry = StrUtil.split(condFields, ",");
                                        String[] titleAry = StrUtil.split(condTitles, ",");
                                        isQuery = true;
                                        for (int j = 0; j < fieldAry.length; j++) {
                                            String fieldName = fieldAry[j];
                                            String fieldTitle = "#";
                                            if (titleAry != null) {
                                                fieldTitle = titleAry[j];
                                                if ("".equals(fieldTitle)) {
                                                    fieldTitle = "#";
                                                }
                                            }

                                            String condType = (String) json.get(fieldName);
                                            CondUnit condUnit = CondUtil.getCondUnit(request, msd, fdRelated, fieldName, fieldTitle, condType, checkboxGroupMap, dateFieldNamelist);
                                            out.print("<span class=\"cond-span\">");
                                            out.print("<span class=\"cond-title\">");
                                            out.print(condUnit.getFieldTitle());
                                            out.print("</span>");
                                            out.print(condUnit.getHtml());
                                            out.print("</span>");
                                            out.print("<script>");
                                            out.print(condUnit.getScript());
                                            out.print("</script>");
                                        }
                                    }
                                }
                            }
                        }

                        // 当doQuery时，需要取相关的数据，所以上面的隐藏输入框必须得有
                        if (isQuery) {
                    %>
                    <button class="layui-btn" data-type="reload">搜索</button>
                    <%
                        }
                    %>
                    <input type="hidden" name="code" value="<%=code%>"/>
                    <input type="hidden" name="formCodeRelated" value="<%=moduleCodeRelated%>"/>
                    <input type="hidden" name="formCode" value="<%=formCode%>"/>
                    <input type="hidden" name="parentId" value="<%=parentId%>"/>
                    <input type="hidden" name="op" value="search"/>
                    <input type="hidden" name="moduleCodeRelated" value="<%=moduleCodeRelated%>"/>
                    <input type="hidden" name="mode" value="<%=mode%>"/>
                    <input type="hidden" name="tagName" value="<%=tagName%>"/>
                </div>
            </form>
        </div>

        <span id="switcher<%=moduleCodeRelated%>" style="cursor:pointer; position: absolute">
        <img id="switchBtn<%=moduleCodeRelated%>" src="../images/hide.png" title="显示/隐藏 查询区域"/>
    </span>
        <script>
            $(function () {
                var $box = $('#searchFormBox<%=moduleCodeRelated%>');
                var l = $box.offset().left + $box.width();
                var t = $box.offset().top;
                $('#switcher<%=moduleCodeRelated%>').css({'top': t + 'px', 'left': l + 'px'});

                var $btn = $('#switchBtn<%=moduleCodeRelated%>');
                var $form = $('#searchForm<%=moduleCodeRelated%>');
                $('#switcher<%=moduleCodeRelated%>').click(function () {
                    if ($btn.attr('src').indexOf("show.png") != -1) {
                        $form.show();
                        $btn.attr('src', '../images/hide.png');
                        $('#switcher<%=moduleCodeRelated%>').css({'top': t + 'px', 'left': l + 'px'});
                    } else {
                        $form.hide();
                        $btn.attr('src', '../images/show.png');
                        $('#switcher<%=moduleCodeRelated%>').css({'top': (t - 20) + 'px', 'left': l + 'px'});
                    }
                });
            });
        </script>

        <%
            boolean canManage = mpdRelated.canUserManage(userName);
        %>
        <table class="layui-hide" id="table_<%=moduleCodeRelated%>" lay-filter="<%=moduleCodeRelated%>"></table>
        <script type="text/html" id="toolbar_<%=moduleCodeRelated%>">
            <div class="layui-btn-container">
                <%if (msdRelated.getInt("btn_add_show") == 1 && mpdRelated.canUserAppend(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="addRelate">增加</button>
                <%}%>
                <%if (msdRelated.getInt("btn_edit_show") == 1 && mpdRelated.canUserModify(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="editRelate">修改</button>
                <%}%>
                <%if (msdRelated.getInt("btn_edit_show") == 1 && (mpdRelated.canUserDel(userName) || canManage)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="delRows">删除</button>
                <%}%>
                <%if (mpd.canUserImport(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="importXls">导入</button>
                <%}%>
                <%if (mpdRelated.canUserExport(userName)) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="exportXls">导出</button>
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
                                if (json.get("btnType").equals("batchBtn")) {
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
                <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="event_<%=moduleCodeRelated%><%=i%>"><%=btnNames[i]%>
                </button>
                <%
                            }
                        }
                    }
                %>
                <%if (privilege.isUserPrivValid(request, "admin")) {%>
                <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="manage">管理</button>
                <%}%>
            </div>
        </script>
    </div>
    <%
        StringBuffer colProps = new StringBuffer();

        String promptField = StrUtil.getNullStr(msdRelated.getString("prompt_field"));
        String promptValue = StrUtil.getNullStr(msdRelated.getString("prompt_value"));
        String promptIcon = StrUtil.getNullStr(msdRelated.getString("prompt_icon"));
        boolean isPrompt = false;
        if (!promptField.equals("") && !promptIcon.equals("")) {
            isPrompt = true;
        }
        if (isPrompt) {
            colProps.append("{display:'', name:'colPrompt', width:20}");
        }

        boolean isColOperateShow = true;

        len = fields.length;
        for (int i = 0; i < len; i++) {
            String fieldName = fields[i];
            String fieldTitle = fieldsTitle[i];

            Object[] aryTitle = CondUtil.getFieldTitle(fdRelated, fieldName, fieldTitle);
            String title = (String) aryTitle[0];
            boolean sortable = (Boolean) aryTitle[1];

            String w = fieldsWidth[i];
            int wid = StrUtil.toInt(w, 100);
            if (w.indexOf("%") == w.length() - 1) {
                w = w.substring(0, w.length() - 1);
                wid = 800 * StrUtil.toInt(w, 20) / 100;
            }
            wid += 30; // 因为layui table有排序符号

            if (fieldsShow[i].equals("0")) {
                if (fieldName.equals("colOperate")) {
                    isColOperateShow = false;
                }
                continue;
            }

            String props;
            if (fieldName.equals("colOperate")) {
                props = "{title:'操作', field:'colOperate', width:" + wid + ", sort:false, width:150, fixed: 'right'}";
            } else {
                props = "{title: '" + title + "', field : '" + fieldName + "', width : " + wid + ", sort : " + sortable + ", ";
                if ("ID".equals(title)) {
                    props += "fixed: true, ";
                }
                props += "align: 'center', hide: false }";
            }

            StrUtil.concat(colProps, ",", props);
        }

        // 如果允许显示操作列，且未定义colOperate，则将其加入，宽度默认为150
        if (isColOperateShow && colProps.lastIndexOf("colOperate") == -1) {
            StrUtil.concat(colProps, ",", "{title:'操作', field:'colOperate', sort:false, width:150, fixed: 'right'}");
        }
    %>
    <script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp?parentId=<%=parentId%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&pageType=moduleListRelate"></script>
    <script>
        layui.use('table', function () {
            var table = layui.table;

            table.render({
                elem: '#table_<%=moduleCodeRelated%>'
                , toolbar: '#toolbar_<%=moduleCodeRelated%>'
                , defaultToolbar: ['filter', 'print'/*, 'exports', {
				title: '提示'
				,layEvent: 'LAYTABLE_TIPS'
				,icon: 'layui-icon-tips'
			}*/]
                , method: 'post'
                , url: 'moduleListRelate.do?code=<%=code%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&parentId=<%=parentId%>&op=search&moduleCodeRelated=<%=moduleCodeRelated%>&mode=&tagName='
                , cols: [[
                    {type: 'checkbox'},
                    <%=colProps.toString()%>
                ]]
                , id: 'table<%=moduleCodeRelated%>'
                , page: true
                , limit: <%=pagesize%>
                , height: 310
                , parseData: function (res) { //将原始数据解析成 table 组件所规定的数据
                    return {
                        "code": res.errCode, //解析接口状态
                        "msg": res.msg, //解析提示文本
                        "count": res.total, //解析数据长度
                        "data": res.rows //解析数据列表
                    };
                }
            });

            //头工具栏事件
            table.on('toolbar(<%=moduleCodeRelated%>)', function (obj) {
                var checkStatus = table.checkStatus(obj.config.id);
                switch (obj.event) {
                    case 'addRelate':
                        layer.open({
                            type: 2,
                            title: '增加',
                            shadeClose: true,
                            shade: 0.6,
                            area: ['90%', '90%'],
                            content: 'moduleAddRelatePage.do?isTabStyleHor=false&code=<%=StrUtil.UrlEncode(code)%>&parentId=<%=parentId%>&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isShowNav=0'
                        });
                        break;
                    case 'editRelate':
                        var data = checkStatus.data;
                        if (data.length == 0) {
                            layer.msg('请选择记录');
                            return;
                        } else if (data.length > 1) {
                            layer.msg('只能选择一条记录');
                            return;
                        }
                        var id = data[0].id;
                        layer.open({
                            type: 2,
                            title: '修改',
                            shadeClose: true,
                            shade: 0.6,
                            area: ['90%', '90%'],
                            content: 'moduleEditRelatePage.do?isTabStyleHor=false&code=<%=StrUtil.UrlEncode(code)%>&parentId=<%=parentId%>&id=' + id + '&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isShowNav=0'
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
                            //do something
                            try {
                                onBeforeModuleDel<%=moduleCodeRelated%>(ids);
                            } catch (e) {
                            }

                            $.ajax({
                                type: "post",
                                url: "moduleDelRelate.do",
                                data: {
                                    code: "<%=moduleCodeRelated%>",
                                    mode: "<%=mode%>",
                                    parentId: "<%=parentId%>",
                                    parentModuleCode: "<%=code%>",
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
                                        doQuery('<%=moduleCodeRelated%>');
                                        try {
                                            onModuleDel<%=moduleCodeRelated%>(ids, false);
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
                        var url = "module_import_excel.jsp?formCode=<%=formCodeRelated%>&code=<%=code%>&moduleCodeRelated=<%=moduleCodeRelated%>&parentId=<%=parentId%>&isShowNav=0";
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
                    Vector vt = ModuleExportTemplateMgr.getTempaltes(request, msdRelated.getString("form_code"));
                    if (vt.size()>0) {
                        String querystr = "op=" + op + "&mode=" + mode + "&tagName=" + StrUtil.UrlEncode(tagName) + "&code=" + code + "&formCode=" + formCode + "&moduleCodeRelated=" + moduleCodeRelated + "&formCodeRelated=" + moduleCodeRelated + "&parentId=" + parentId;
                        expUrl = request.getContextPath() + "/visual/module_excel_sel_templ.jsp?mode=" + mode + "&isRelate=true&" + querystr;
                    }
                    else {
                        expUrl = request.getContextPath() + "/visual/exportExcelRelate.do";
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
                        var fields = $(".search-form-<%=formCodeRelated%>").serializeArray();
                        jQuery.each(fields, function (i, field) {
                            expForm.innerHTML += "<input name='" + field.name + "' value='" + field.value + "'/>";
                        });
                        expForm.innerHTML += "<input name='cols' value='" + cols + "'/>";
                        expForm.submit();
                        break;
                    case 'manage':
                        addTab("<%=msdRelated.getString("name")%>", "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=msdRelated.getString("form_code")%>&code=<%=msdRelated.getString("code")%>");
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
                    case 'event_<%=moduleCodeRelated%><%=i%>':
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
                    case 'event_<%=moduleCodeRelated%><%=i%>':
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
                    }
                }
            }
            %>
                }
            });

            $('.search-form-<%=formCodeRelated%> .layui-btn').on('click', function (e) {
                e.preventDefault();
                table.reload('table<%=moduleCodeRelated%>', {
                    page: {
                        curr: 1 //重新从第 1 页开始
                    }
                    , where: $('.search-form-<%=formCodeRelated%>').serializeJsonObject()
                }, 'data');
            });

            //监听表格排序问题
            table.on('sort(<%=moduleCodeRelated%>)', function (obj) { //注：sort lay-filter="对应的值"
                table.reload('table<%=moduleCodeRelated%>', { //testTable是表格容器id
                    initSort: obj // 记录初始排序，如果不设的话，将无法标记表头的排序状态。 layui 2.1.1 新增参数
                    , where: {
                        orderBy: obj.field //排序字段
                        , sort: obj.type //排序方式
                    }
                });
            });
        });
    </script>
    <%
        }
    %>
    <script>
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

        function getIdsSelected(moduleCodeRelated) {
            var checkStatus = layui.table.checkStatus('table' + moduleCodeRelated);
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

        function doQuery(moduleCodeRelated) {
            layui.table.reload('table' + moduleCodeRelated);
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

        $('input').each(function () {
            if ($(this).attr('kind') == 'DATE') {
                $(this).attr('autocomplete', 'off');
            }
        });
    </script>
</div>
</body>
</html>