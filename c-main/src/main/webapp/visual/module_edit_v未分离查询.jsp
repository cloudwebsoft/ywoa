<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudweb.oa.utils.I18nUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.base.IFormMacroCtl" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
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

    String op = ParamUtil.get(request, "op");
    int parentId = ParamUtil.getInt(request, "parentId", -1);
    int id = ParamUtil.getInt(request, "id");
    String formCode = msd.getString("form_code");

    String viewEdit = "module_edit.jsp";
    if (msd.getInt("view_edit") == ModuleSetupDb.VIEW_EDIT_CUSTOM) {
        viewEdit = msd.getString("url_edit");
        response.sendRedirect(request.getContextPath() + "/" + msd.getString("url_edit") + "?parentId=" + parentId + "&id=" + id + "&code=" + code + "&formCode=" + formCode);
        return;
    }

    if ("".equals(formCode)) {
        out.print(SkinUtil.makeErrMsg(request, "表单编码不能为空！"));
        return;
    }

    ModulePrivDb mpd = new ModulePrivDb(code);
    if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
        out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    if (!ModulePrivMgr.canAccessData(request, msd, id)) {
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, i18nUtil.get("info_access_data_fail")));
        return;
    }

    String userName = privilege.getUser(request);
    // 置嵌套表需要用到的cwsId
    request.setAttribute("cwsId", "" + id);
    // 置嵌套表需要用到的页面类型
    request.setAttribute("pageType", "edit");
    // 置NestSheetCtl需要用到的formCode
    request.setAttribute("formCode", formCode);

    FormMgr fm = new FormMgr();
    FormDb fd = fm.getFormDb(formCode);

    com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
    com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
    if (op.equals("refreshAttach")) {
        Vector vAttach = fdao.getAttachments();
        request.setAttribute("vAttach", vAttach);
        request.setAttribute("canUserLog", mpd.canUserLog(privilege.getUser(request)));
        if (vAttach.size() > 0) {
%>
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
						<a href="../visual_getfile.jsp?attachId=${att.id}" target="_blank">
							<span id="spanAttName${att.id}">${att.name}</span>
						</a>
					</span>
                </td>
                <td width="10%" align="center">
                        ${att.creatorRealName}
                </td>
                <td width="15%" align="center">
                    <fmt:formatDate value='${att.createDate}' pattern='yyyy-MM-dd HH:mm'/>
                </td>
                <td width="11%" align="center">${att.fileSizeMb}M
                </td>
                <td width="11%" align="center">
                    <a href="../visual_getfile.jsp?attachId=${att.id}" target="_blank">
                        <lt:Label res="res.flow.Flow" key="download"/>
                    </a>
                    &nbsp;&nbsp;
                    <a href="javascript:;" onClick="delAtt(${att.id})" style="cursor:pointer">删除</a>
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
<%
        }
        return;
    }

    int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

    request.setAttribute("code", code);
    request.setAttribute("id", id);
    request.setAttribute("parentId", parentId);
    request.setAttribute("skinPath", SkinMgr.getSkinPath(request));
    request.setAttribute("isShowNav", isShowNav);
    request.setAttribute("isHasAttachment", fd.isHasAttachment());

    StringBuffer requestParamBuf = new StringBuffer();
    Enumeration reqParamNames = request.getParameterNames();
    while (reqParamNames.hasMoreElements()) {
        String paramName = (String) reqParamNames.nextElement();
        String[] paramValues = request.getParameterValues(paramName);
        if (paramValues.length == 1) {
            String paramValue = ParamUtil.getParam(request, paramName);
            // 过滤掉formCode等
            if (paramName.equals("id")) {
                ;
            } else {
                StrUtil.concat(requestParamBuf, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
            }
        }
    }
    request.setAttribute("requestParams", requestParamBuf.toString());

    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
    request.setAttribute("rend", rd.rend(msd));
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>模块修改</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link href="../flowstyle.css" rel="stylesheet" type="text/css"/>
    <style>
        .page-main {
           margin: auto 15px;
        }
        .att_box {
            margin-top: 5px;
        }

        input, textarea, button {
            outline: none;
        }

        input[readonly] {
            background-color: #ddd;
        }

        select[readonly] {
            background-color: #ddd;
        }

        textarea[readonly] {
            background-color: #ddd;
        }

        #attDiv {
            margin-top: 10px;
        }

        <%=msd.getCss(ConstUtil.PAGE_TYPE_EDIT)%>
    </style>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>

    <link rel="stylesheet" href="../js/poshytip/tip-yellowsimple/tip-yellowsimple.css" type="text/css"/>
    <script type="text/javascript" src="../js/poshytip/jquery.poshytip.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <script src="../js/jquery.raty.min.js"></script>

    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp"></script>
    <script src="../inc/ajax_getpage.jsp"></script>
    <script src="../inc/upload.js"></script>
    <script src="../js/jquery.bgiframe.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link type="text/css" rel="stylesheet" href="${skinPath}/jquery-ui/jquery-ui-1.10.4.css"/>

    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>

    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../js/select2/i18n/zh-CN.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery.form.js"></script>
    <script type="text/javascript" src="../js/appendGrid/jquery.appendGrid-1.5.1.js"></script>
    <link type="text/css" rel="stylesheet" href="../js/appendGrid/jquery.appendGrid-1.5.1.css"/>

    <script type="text/javascript" src="../js/jquery.toaster.js"></script>

    <script src="../js/BootstrapMenu.min.js"></script>

    <script src="../flow/form_js/form_js_${formCode}.jsp?pageType=edit&id=${id}&time=<%=Math.random()%>"></script>
</head>
<body>
<div class="page-main">
<%@ include file="../inc/tip_phrase.jsp" %>
<script>
    function save() {
        // 表单合法性校验，用于在form_js_***中扩展
        try {
            var re = checkModuleForm();
            if ("" !== re) {
                layer.msg(re);
                return;
            }
        } catch (e) {
        }

        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }

        var f_helper = new LiveValidation('cwsHelper');
        if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
            if (LiveValidation.liveErrMsg.length < 100) {
                layer.msg(LiveValidation.liveErrMsg);
            } else {
                layer.msg('请检查表单中的内容填写是否正常');
            }
            return;
        }

        $('#visualForm').submit();
    }

    $(function () {
        SetNewDate();
        $('#btnOK').click(function (e) {
            e.preventDefault();
            save();
        });
    });

    // ajaxForm序列化提交数据之前的回调函数
    function onBeforeSerialize() {
        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }
    }

    $(function () {
        var options = {
            beforeSerialize: onBeforeSerialize,
            //target:        '#output2',   // target element(s) to be updated with server response
            beforeSubmit: preSubmit,  // pre-submit callback
            success: showResponse  // post-submit callback

            // other available options:
            //url:       url         // override for form's 'action' attribute
            //type:      type        // 'get' or 'post', override for form's 'method' attribute
            //dataType:  null        // 'xml', 'script', or 'json' (expected server response type)
            //clearForm: true        // clear all form fields after successful submit
            //resetForm: true        // reset the form after successful submit

            // $.ajax options can be used here too, for example:
            //timeout:   3000
        };

        // bind to the form's submit event
        var lastSubmitTime = new Date().getTime();
        $('#visualForm').submit(function () {
            // 通过判断时间，禁多次重复提交
            var curSubmitTime = new Date().getTime();
            // 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
            if (curSubmitTime - lastSubmitTime < 500) {
                lastSubmitTime = curSubmitTime;
                $('#visualForm').hideLoading();
                return false;
            } else {
                lastSubmitTime = curSubmitTime;
            }

            $(this).ajaxSubmit(options);
            return false;
        });
    });

    function preSubmit() {
        $('#visualForm').showLoading();
    }

    function showResponse(responseText, statusText, xhr, $form) {
        $('#visualForm').hideLoading();
        var data = responseText;
        if (!isJson(data)) {
            data = $.parseJSON($.trim(responseText));
        }
        if (data.ret == "1") {
            try {
                onModuleEdit<%=code%>(<%=id%>, "${param.tabIdOpener}");
            } catch (e) {
            }

            /*jAlert(data.msg, "提示", function () {
                window.location.reload(); // 文件宏控件需要刷新
            });*/
            <%
            if (msd.isReloadAfterUpdate()) {
            %>
            layer.open({
                type: 1
                ,offset: 'auto'
                ,id: 'dlg' //防止重复弹出
                ,content: '<div style="padding: 20px 100px;">'+ data.msg +'</div>'
                ,btn: '确定'
                ,btnAlign: 'c' //按钮居中
                ,shade: 0 //不显示遮罩
                ,yes: function() {
                    window.location.reload(); // 文件宏控件需要刷新
                }
            });
            <%
            } else {
            %>
                layer.msg(data.msg);
            <%
            }
            %>

            // refreshAttach();
            reloadTab("${param.tabIdOpener}");
        } else {
            if (data.msg != null) {
                data.msg = data.msg.replace(/\\r/ig, "<BR>");
            }
            layer.msg(data.msg);
        }
    }

    function refreshAttach() {
        <c:if test="${isHasAttachment}">
        $.ajax({
            type: "post",
            url: "module_edit.jsp",
            data: {
                op: "refreshAttach",
                id: "${id}",
                code: "${code}"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#visualForm').showLoading();
            },
            success: function (data, status) {
                // 删除编辑时界面上添加的文件
                delAllUploadFile();
                $('#tdAtt').html(data);
            },
            complete: function (XMLHttpRequest, status) {
                $('#visualForm').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                alert(XMLHttpRequest.responseText);
            }
        });
        </c:if>
    }

    function delAtt(attId, fieldName) {
        jConfirm('您确定要删除吗？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "delAttach.do",
                    data: {
                        id: "${id}",
                        formCode: "${formCode}",
                        attachId: attId,
                        code: "${code}"
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('#visualForm').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == "1") {
                            layer.msg(data.msg);
                            $('#trAtt' + attId).remove();
                            if (fieldName != null) {
                                $('#helper_' + fieldName).remove();
                            }
                        } else {
                            layer.msg(data.msg);
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('#visualForm').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }
</script>
<form action="update.do?id=${id}&code=${code}&isShowNav=${isShowNav}&parentId=${parentId}" method="post" enctype="multipart/form-data" id="visualForm" name="visualForm">
    <table style="margin-bottom:10px" width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="left">
                ${rend}
            </td>
        </tr>
        <tr>
            <td align="left" id="tdAtt">
            </td>
        </tr>
        <c:if test="${isHasAttachment}">
            <tr>
                <td align="left">
                    <script>initUpload()</script>
                </td>
            </tr>
        </c:if>
        <tr>
            <td align="center" style="padding-top: 10px">
                <input name="id" value="${id}" type="hidden"/>
                <button id="btnOK" class="layui-btn layui-btn-primary">确定</button>
                <input id="cwsHelper" name="cwsHelper" value="1" type="hidden"/>
            </td>
        </tr>
    </table>
</form>

<link rel="stylesheet" href="../js/jquery-contextmenu/jquery.contextMenu.min.css">
<script src="../js/jquery-contextmenu/jquery.contextMenu.js"></script>
<script src="../js/jquery-contextmenu/jquery.ui.position.min.js"></script>
<script>
    refreshAttach();

    $(function () {
        $('#btnClose').click(function () {
            closeActiveTab();
        });

        $('input[type=radio]').each(function (i) {
            var name = $(this).attr("name");
            if ($(this).attr("readonly") == null) {
                $(this).addClass('radio-menu');
            }
        });

        // 不能用BootstrapMenu，因为chrome上会导致radio无法点击
        $.contextMenu({
            selector: '.radio-menu',
            trigger: 'hover',
            delay: 1000,
            callback: function (key, options) {
                if (key == 'cancel') {
                    var $obj = options.$trigger;
                    var name = $obj.attr('name');
                    $('input[type=radio][name="' + name + '"]:checked').attr("checked", false);
                }
            },
            items: {
                "cancel": {
                    name: "取消选择", icon: function ($element, key, item) {
                        return 'context-menu-icon context-menu-icon-quit';
                    }
                }
            }
        });

        $('input').each(function () {
            if ($(this).attr('kind') == 'DATE') {
                $(this).attr('autocomplete', 'off');
            }
        });

        // 初始化tip提示
        // 不能通过$("#visualForm").serialize()来获取所有的元素，因为radio或checkbox未被选中，则不会被包含
        $('#visualForm input, #visualForm select, #visualForm textarea').each(function () {
            var tip = '';
            if ($(this).attr('type') == 'radio') {
                tip = $(this).parent().attr('tip');
            } else {
                tip = $(this).attr('tip');
            }
            if (null != tip && "" != tip) {
                $(this).poshytip({
                    content: function () {
                        return tip;
                    },
                    className: 'tip-yellowsimple',
                    alignTo: 'target',
                    alignX: 'center',
                    offsetY: 5,
                    allowTipHover: true
                });
            }
        });
    });

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
<script src="../js/layui/layui.js" charset="utf-8"></script>
<%
    ModuleRelateDb mrd = new ModuleRelateDb();
    Iterator ir = mrd.getModulesRelated(formCode).iterator();
    while (ir.hasNext()) {
        mrd = (ModuleRelateDb) ir.next();
        String moduleCodeRelated = mrd.getString("relate_code");

        ModulePrivDb mpdRelated = new ModulePrivDb(moduleCodeRelated);
        if (!mpdRelated.canUserSee(privilege.getUser(request))) {
            continue;
        }

        if (mpdRelated.canUserSee(userName) && mrd.getInt("is_on_tab")==1) {
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
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
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
	<div class="search-form-box">
        <form class="search-form search-form-<%=formCodeRelated%>">
		<div class="layui-inline">
            <%
                ArrayList<String> list = new ArrayList<String>();
                MacroCtlMgr mm = new MacroCtlMgr();

                int len = 0;
                boolean isQuery = false;

                if (btnNames!=null) {
                    len = btnNames.length;
                    for (int i=0; i<len; i++) {
                        if (btnScripts[i].startsWith("{")) {
                            Map<String, String> checkboxGroupMap = new HashMap<String, String>();
                            JSONObject json = new JSONObject(btnScripts[i]);
                            if (json.get("btnType").equals("queryFields")) {
                                isQuery = true;

                                String condFields = (String)json.get("fields");
                                String[] fieldAry = StrUtil.split(condFields, ",");
                                for (int j=0; j<fieldAry.length; j++) {
                                    String fieldName = fieldAry[j];
                                    String fieldTitle = "#";
                                    for (int n=0; n<fields.length; n++) {
                                        if (fieldName.equals(fields[n])) {
                                            fieldTitle = fieldsTitle[n];
                                            break;
                                        }
                                    }

                                    FormField ff = null;
                                    String title;
                                    String condType = (String)json.get(fieldName);
                                    String queryValue = ParamUtil.get(request, fieldName);
                                    if ("cws_status".equals(fieldName)) {
                                        title = "状态";
                                    }
                                    else if ("cws_flag".equals(fieldName)) {
                                        title = "冲抵状态";
                                    }
                                    else if ("ID".equals(fieldName)) {
                                        title = "ID";
                                    }
                                    else if ("cws_id".equals(fieldName)) {
                                        title = "关联ID";
                                    }
                                    else if ("flowId".equals(fieldName)) {
                                        title = "流程号";
                                    }
                                    else if ("flow:begin_date".equals(fieldName)) {
                                        title = "流程开始时间";
                                    }
                                    else if ("flow:end_date".equals(fieldName)) {
                                        title = "流程结束时间";
                                    }
                                    else {
                                        if (fieldName.startsWith("main:")) { // 关联的主表
                                            String[] aryField = StrUtil.split(fieldName, ":");
                                            if (aryField.length==3) {
                                                FormDb mainFormDb = fm.getFormDb(aryField[1]);
                                                ff = mainFormDb.getFormField(aryField[2]);
                                                if (ff==null) {
                                                    out.print(fieldName + "不存在");
                                                    continue;
                                                }
                                                // title = mainFormDb.getName() + "：" + ff.getTitle();
                                                title = ff.getTitle();
                                            }
                                            else {
                                                out.print(fieldName + " 不存在");
                                                continue;
                                            }
                                        }
                                        else if (fieldName.startsWith("sub:")) { // 关联的子表
                                            String[] aryField = StrUtil.split(fieldName, ":");
                                            String field = fieldName.substring(5);
                                            if (aryField.length==3) {
                                                FormDb mainFormDb = fm.getFormDb(aryField[1]);
                                                ff = mainFormDb.getFormField(aryField[2]);
                                                if (ff==null) {
                                                    out.print(fieldName + "不存在");
                                                    continue;
                                                }
                                                title = ff.getTitle();
                                            }
                                            else {
                                                title = field + " 不存在";
                                            }
                                        }
                                        else if (fieldName.startsWith("other:")) { // 映射的字段，多重映射不支持
                                            String[] aryField = StrUtil.split(fieldName, ":");
                                            if (aryField.length<5) {
                                                out.print(fieldName + "格式非法");
                                                continue;
                                            }
                                            else {
                                                FormDb otherFormDb = fm.getFormDb(aryField[2]);
                                                ff = otherFormDb.getFormField(aryField[4]);
                                                if (ff==null) {
                                                    out.print(fieldName + "不存在");
                                                    continue;
                                                }
                                                // title = otherFormDb.getName() + "：" + ff.getTitle();
                                                title = ff.getTitle();
                                            }
                                        }
                                        else {
                                            ff = fdRelated.getFormField(fieldName);
                                            if (ff==null) {
                                                out.print(fieldName + "不存在");
                                                continue;
                                            }
                                            if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                                                String desc = StrUtil.getNullStr(ff.getDescription());
                                                if (!"".equals(desc)) {
                                                    title = desc;
                                                }
                                                else {
                                                    title = ff.getTitle();
                                                }
                                                String chkGroup = StrUtil.getNullStr(ff.getDescription());
                                                if (!"".equals(chkGroup)) {
                                                    if (!checkboxGroupMap.containsKey(chkGroup)) {
                                                        checkboxGroupMap.put(chkGroup, "");
                                                    }
                                                    else {
                                                        continue;
                                                    }
                                                }
                                            }
                                            else {
                                                title = ff.getTitle();
                                            }
                                        }
                                        // 用于给convertToHTMLCtlForQuery辅助传值
                                        ff.setCondType(condType);
                                    }
                                    if (!"#".equals(fieldTitle)) {
                                        title = fieldTitle;
                                    }
            %>
            <span class="condSpan">
        			<%=title%>
               		<%
                        if ("cws_status".equals(fieldName)) {
                            String nameCond = ParamUtil.get(request, fieldName + "_cond");
                            if ("".equals(nameCond)) {
                                nameCond = condType;
                            }
                            int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);
                    %>
				          <select name="<%=fieldName%>_cond" style="display:none">
				            <option value="=" selected="selected">等于</option>
				          </select>
                          <select name='<%=fieldName%>'>
                          <option value='<%=SQLBuilder.CWS_STATUS_NOT_LIMITED%>'>不限</option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>' selected><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>
                          </select>
						<script>
						$(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            <%if (queryValueCwsStatus!=-20000) {%>
                            o("<%=fieldName%>").value = "<%=queryValueCwsStatus%>";
                            <%}	else {%>
                            o("<%=fieldName%>").value = "<%=msd.getInt("cws_status")%>";
                            <%}%>
                        });
						</script>
						<%
                        }
                        else if ("cws_flag".equals(fieldName)) {
                            String nameCond = ParamUtil.get(request, fieldName + "_cond");
                            if ("".equals(nameCond)) {
                                nameCond = condType;
                            }
                            int queryValueCwsFlag = ParamUtil.getInt(request, "cws_flag", -1);
                        %>
				          <select name="<%=fieldName%>_cond" style="display:none">
				            <option value="=" selected="selected">等于</option>
				          </select>
                          <select name='<%=fieldName%>'>
                          <option value='-1'>不限</option>
                          <option value='0'>否</option>
                          <option value='1'>是</option>
                          </select>
						<script>
						$(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            o("<%=fieldName%>").value = "<%=queryValueCwsFlag%>";
                        });
						</script>
						<%
                        }
                        else if ("ID".equals(fieldName)) {
                            String nameCond = ParamUtil.get(request, fieldName + "_cond");
                            if ("".equals(nameCond)) {
                                nameCond = condType;
                            }
                            String queryValueID = ParamUtil.get(request, "ID");
                        %>
				          <select name="ID_cond">
				            <option value="=" selected="selected">=</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
                              <option value="&lt;="><=</option>
				          </select>
	                      <input name="ID" size="5" />
						<script>
						$(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            o("<%=fieldName%>").value = "<%=queryValueID%>";
                        });
						</script>
						<%
                        }
                        else if ("cws_id".equals(fieldName)) {
                            String nameCond = ParamUtil.get(request, fieldName + "_cond");
                            if ("".equals(nameCond)) {
                                nameCond = condType;
                            }
                            String queryValueCwsId = ParamUtil.get(request, "cws_id");
                        %>
				          <select name="cws_id_cond">
				            <option value="=" selected="selected">=</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
                              <option value="&lt;="><=</option>
				          </select>
	                      <input name="cws_id" size="5" />
						<script>
						$(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            o("<%=fieldName%>").value = "<%=queryValueCwsId%>";
                        });
						</script>
						<%
                        }
                        else if ("flowId".equals(fieldName)) {
                            String nameCond = ParamUtil.get(request, fieldName + "_cond");
                            if ("".equals(nameCond)) {
                                nameCond = condType;
                            }
                            String queryValueID = ParamUtil.get(request, "flowId");
                        %>
				          <select name="flowId_cond">
				            <option value="=" selected="selected">=</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
                              <option value="&lt;="><=</option>
				          </select>
	                      <input name="flowId" size="5" />
						<script>
						$(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            o("<%=fieldName%>").value = "<%=queryValueID%>";
                        });
						</script>
						<%
                        }
                        else if ("flow:begin_date".equals(fieldName) || "flow:end_date".equals(fieldName) || ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                        %>
						<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
               			<%
                            String idPrefix = fieldName.replaceAll(":", "_");
                            if (condType.equals("0")) {
                                String fDate = ParamUtil.get(request, fieldName + "FromDate");
                                String tDate  = ParamUtil.get(request, fieldName + "ToDate");
                                list.add(idPrefix + "FromDate");
                                list.add(idPrefix + "ToDate");
                        %>
                              从
                              <input id="<%=idPrefix%>FromDate" name="<%=fieldName%>FromDate" size="15" style="width:80px" value = "<%=fDate%>" />
                              至
                              <input id="<%=idPrefix%>ToDate" name="<%=fieldName%>ToDate" size="15" style="width:80px" value = "<%=tDate%>" />
	  					<%
                        }
                        else {
                            list.add(idPrefix);
                        %>
                              <input id="<%=idPrefix%>" name="<%=fieldName%>" size="15" value = "<%=queryValue%>" />
						<%
                            }
                        } else if(ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu!=null) {
                                String queryValueRealShow = ParamUtil.get(request, fieldName + "_realshow");
                                // 用main及other映射字段的描述替换其name，以使得生成的查询控件的id及name中带有main及other
                                FormField ffQuery = (FormField)ff.clone();
                                ffQuery.setName(fieldName);
                                IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                                int fieldType = ifmc.getFieldType(ff);
                                if (fieldType==FormField.FIELD_TYPE_INT || fieldType==FormField.FIELD_TYPE_DOUBLE || fieldType==FormField.FIELD_TYPE_FLOAT || fieldType==FormField.FIELD_TYPE_LONG || fieldType==FormField.FIELD_TYPE_PRICE) {
                                    String nameCond = ParamUtil.get(request, fieldName + "_cond");
                                    if ("".equals(nameCond)) {
                                        nameCond = condType;
                                    }
                                    if (condType.equals(SQLBuilder.COND_TYPE_SCOPE)) {
                                        String fCond = ParamUtil.get(request, fieldName + "_cond_from");
                                        String tCond = ParamUtil.get(request, fieldName + "_cond_to");
                                        String fVal = ParamUtil.get(request, fieldName + "_from");
                                        String tVal = ParamUtil.get(request, fieldName + "_to");
                        %>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
								  <select name="<%=fieldName%>_cond_from">
									<option value=">">></option>
									<option value=">=" selected="selected">>=</option></option>
								  </select>
								  <%
                                      FormField ffTemp = (FormField)ffQuery.clone();
                                      ffTemp.setName(ffQuery.getName() + "_from");
                                      ffTemp.setCondType(ffQuery.getCondType());
                                      out.print(ifmc.convertToHTMLCtlForQuery(request, ffTemp));
                                  %>
								  <select name="<%=fieldName%>_cond_to">
									<option value="&lt;"><</option>
									<option value="&lt;=" selected="selected"><=</option></option>
								  </select>
								  <%
                                      ffTemp.setName(ffQuery.getName() + "_to");
                                      out.print(ifmc.convertToHTMLCtlForQuery(request, ffTemp));
                                  %>
								<script>
								$(document).ready(function() {
                                    o("<%=fieldName%>_cond_from").value = "<%=fCond%>";
                                    o("<%=fieldName%>_from").value = "<%=fVal%>";
                                    o("<%=fieldName%>_cond_to").value = "<%=tCond%>";
                                    o("<%=fieldName%>_to").value = "<%=tVal%>";
                                });
								</script>
								<%
                                }
                                else {
                                %>
								  <select name="<%=fieldName%>_cond">
									<option value="=" selected="selected">=</option>
									<option value=">">></option>
									<option value="&lt;"><</option>
									<option value=">=">>=</option></option>
                                      <option value="&lt;="><=</option>
								  </select>
								  <%
                                      out.print(ifmc.convertToHTMLCtlForQuery(request, ffQuery));
                                  %>
								<script>
								$(document).ready(function() {
                                    o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                                    o("<%=fieldName%>").value = "<%=queryValue%>";
                                });
								</script>
							<%
                                }
                            }
                            else {
                                out.print(ifmc.convertToHTMLCtlForQuery(request, ffQuery));
                            %>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
								<%
                                    if ("text".equals(ifmc.getControlType()) || "img".equals(ifmc.getControlType()) || "textarea".equals(ifmc.getControlType())) {
                                %>
								<a id="arrow<%=j %>" href="javascript:;"><i class="fa fa-caret-down"></i></a>
								<%
                                    }
                                %>
								<script>
								$(document).ready(function() {
                                    <%
                                    // 如果是多选
                                    if (condType.equals(SQLBuilder.COND_TYPE_MULTI)) {
                                        String[] aryVal = ParamUtil.getParameters(request, fieldName);
                                        if (aryVal!=null) {
                                            for (String s : aryVal) {
                                                if ("".equals(queryValue)) {
                                                    queryValue = s;
                                                }
                                                else {
                                                    queryValue += "," + s;
                                                }
                                            }
                                        }
                                        if (!"".equals(queryValue)) {
                                         %>
                                    $(document).ready(function() {
                                        setMultiCheckboxChecked("<%=fieldName%>", "<%=queryValue%>");
                                    });
                                    <%
                                    }
                                }
                                else {
                                %>
                                    o("<%=fieldName%>").value = "<%=queryValue%>";
                                    try {
                                        o("<%=fieldName%>_realshow").value = "<%=queryValueRealShow%>";
                                    } catch (e) {}
                                    <%
                                    }
                                    %>
                                    <%
                                    if ("text".equals(ifmc.getControlType()) || "img".equals(ifmc.getControlType()) || "textarea".equals(ifmc.getControlType())) {
                                    %>
                                    // 使=空或者<>空，获得焦点时即为选中状态，以便于修改条件的值
                                    $("input[name='<%=fieldName%>']").focus(function() {
                                        if ($(this).val()=='<%=SQLBuilder.IS_EMPTY%>' || $(this).val()=='<%=SQLBuilder.IS_NOT_EMPTY%>') {
                                            this.select();
                                        }
                                    });

                                    var menu = new BootstrapMenu('#arrow<%=j%>', {
                                        menuEvent: 'click',
                                        actions: [{
                                            name: '等于空',
                                            onClick: function() {
                                                $("input[name='<%=fieldName%>']").val('<%=SQLBuilder.IS_EMPTY%>');
                                            }
                                        }, {
                                            name: '不等于空',
                                            onClick: function() {
                                                $("input[name='<%=fieldName%>']").val('<%=SQLBuilder.IS_NOT_EMPTY%>');
                                            }
                                        }]
                                    });
                                    <%}%>
                                });
								</script>
						<%
                                }
                            }
                        }
                        else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
                            String nameCond = ParamUtil.get(request, fieldName + "_cond");
                            if ("".equals(nameCond)) {
                                nameCond = condType;
                            }
                            if (condType.equals(SQLBuilder.COND_TYPE_SCOPE)) {
                                String fCond = ParamUtil.get(request, fieldName + "_cond_from");
                                String tCond = ParamUtil.get(request, fieldName + "_cond_to");
                                String fVal = ParamUtil.get(request, fieldName + "_from");
                                String tVal = ParamUtil.get(request, fieldName + "_to");
                        %>
						<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
				          <select name="<%=fieldName%>_cond_from">
				            <option value=">">></option>
				            <option value=">=" selected="selected">>=</option></option>
				          </select>
	                      <input name="<%=fieldName%>_from" size="3" />
				          <select name="<%=fieldName%>_cond_to">
				            <option value="&lt;"><</option>
				            <option value="&lt;=" selected="selected"><=</option></option>
				          </select>
	                      <input name="<%=fieldName%>_to" size="3" />
						<script>
						$(document).ready(function() {
                            o("<%=fieldName%>_cond_from").value = "<%=fCond%>";
                            o("<%=fieldName%>_from").value = "<%=fVal%>";
                            o("<%=fieldName%>_cond_to").value = "<%=tCond%>";
                            o("<%=fieldName%>_to").value = "<%=tVal%>";
                        });
						</script>
						<%
                        }
                        else {
                        %>
				          <select name="<%=fieldName%>_cond">
				            <option value="=" selected="selected">=</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
                              <option value="&lt;="><=</option>
				          </select>
	                      <input name="<%=fieldName%>" size="5" />
						<script>
						$(document).ready(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            o("<%=fieldName%>").value = "<%=queryValue%>";
                        });
						</script>
						<%
                            }
                        }
                        else {
                            boolean isSpecial = false;
                            if (condType.equals(SQLBuilder.COND_TYPE_NORMAL)) {
                                if (ff.getType().equals(FormField.TYPE_SELECT)) {
                                    isSpecial = true;
                        %>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
								<select id="<%=fieldName %>" name="<%=fieldName %>">
								<%=FormParser.getOptionsOfSelect(fdRelated, ff) %>
								</select>
								<script>
								$(document).ready(function() {
                                    o("<%=fieldName%>").value = "<%=queryValue%>";
                                });
								</script>
								<%
                                }
                                else if (ff.getType().equals(FormField.TYPE_RADIO)) {
                                    isSpecial = true;
                                %>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
								<%
                                    String[][] aryRadio = FormParser.getOptionsArrayOfRadio(fdRelated, ff);
                                    for (int k=0; k<aryRadio.length; k++) {
                                        String val = aryRadio[k][0];
                                        String text = aryRadio[k][1];
                                %>
									<input type="radio" id="<%=fieldName %>" name="<%=fieldName %>" value="<%=val %>"/><%=text %>
								<%
                                    }
                                %>
								<script>
								$(function() {
                                    setRadioValue('<%=fieldName%>', '<%=queryValue%>');
                                })
								</script>
							<%
                            }
                            else if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                                isSpecial = true;
                            %>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
								<%
                                    String[][] aryChk = FormParser.getOptionsArrayOfCheckbox(fdRelated, ff);
                                    for (int k=0; k<aryChk.length; k++) {
                                        String val = aryChk[k][0];
                                        String fName = aryChk[k][1];
                                        String text = aryChk[k][2];
                                        queryValue = ParamUtil.get(request, fName);
                                %>
									<input type="checkbox" id="<%=fName %>" name="<%=fName %>" value="<%=val %>" style="<%=aryChk.length>1?"width:20px":""%>"/>
									<script>
									$(function() {
                                        o('<%=fName%>').checked = <%=queryValue.equals(val)?"true":"false"%>;
                                    })
									</script>
									<%if (aryChk.length>1) { %>
									<%=text %>
									<%} %>
								<%
                                        }
                                    }
                                }
                                else if (condType.equals(SQLBuilder.COND_TYPE_MULTI)) {
                                    if (ff.getType().equals(FormField.TYPE_SELECT)) {
                                        isSpecial = true;
                                %>
							<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
							<%
                                String[][] aryOpt = FormParser.getOptionsArrayOfSelect(fdRelated, ff);
                                for (int k=0; k<aryOpt.length; k++) {
                                    if ("".equals(aryOpt[k][1].trim())) {
                                        aryOpt[k][0] = "无";
                                    }
                            %>
							<input name="<%=fieldName%>" type="checkbox" value="<%=aryOpt[k][1]%>" style="width:20px"/><%=aryOpt[k][0]%>
							<%
                                }
                                String[] aryVal = ParamUtil.getParameters(request, fieldName);
                                if (aryVal!=null) {
                                    for (String s : aryVal) {
                                        if ("".equals(queryValue)) {
                                            queryValue = s;
                                        }
                                        else {
                                            queryValue += "," + s;
                                        }
                                    }
                                }
                                if (!"".equals(queryValue)) {
                            %>
							<script>
							$(document).ready(function() {
                                setMultiCheckboxChecked("<%=fieldName%>", "<%=queryValue%>");
                            });
							</script>
						<%
                                    }
                                }
                            }
                            if (!isSpecial) {
                        %>
							<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
		                    <input id="field<%=j%>" name="<%=fieldName%>" size="5" />
							<a id="arrow<%=j %>" href="javascript:;"><i class="fa fa-caret-down"></i></a>
							<script>
							$(document).ready(function() {
                                $("#field<%=j%>").value = "<%=queryValue%>";

                                $("#field<%=j%>").focus(function() {
                                    if ($(this).val()=='<%=SQLBuilder.IS_EMPTY%>' || $(this).val()=='<%=SQLBuilder.IS_NOT_EMPTY%>') {
                                        this.select();
                                    }
                                });

                                var menu = new BootstrapMenu('#arrow<%=j%>', {
                                    menuEvent: 'click',
                                    actions: [{
                                        name: '等于空',
                                        onClick: function() {
                                            $('#field<%=j%>').val('<%=SQLBuilder.IS_EMPTY%>');
                                        }
                                    }, {
                                        name: '不等于空',
                                        onClick: function() {
                                            $('#field<%=j%>').val('<%=SQLBuilder.IS_NOT_EMPTY%>');
                                        }
                                    }]
                                });
                            });
							</script>
						<%
                                }
                            }
                        %>
					</span>
            <%
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
                }
            %>
            <input type="hidden" name="code" value="<%=code%>" />
            <input type="hidden" name="formCodeRelated" value="<%=moduleCodeRelated%>" />
            <input type="hidden" name="formCode" value="<%=formCode%>" />
            <input type="hidden" name="parentId" value="<%=parentId%>" />
            <input type="hidden" name="op" value="search" />
            <input type="hidden" name="moduleCodeRelated" value="<%=moduleCodeRelated%>"/>
            <input type="hidden" name="mode" value="<%=mode%>" />
            <input type="hidden" name="tagName" value="<%=tagName%>" />
		</div>
        </form>
	</div>
    <%
        boolean canManage = mpdRelated.canUserManage(userName);
    %>
	<table class="layui-hide" id="table_<%=moduleCodeRelated%>" lay-filter="<%=moduleCodeRelated%>"></table>
	<script type="text/html" id="toolbar_<%=moduleCodeRelated%>">
		<div class="layui-btn-container">
            <%if (msdRelated.getInt("btn_add_show")==1 && mpdRelated.canUserAppend(userName)) {%>
			<button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="addRelate">增加</button>
            <%}%>
            <%if (msdRelated.getInt("btn_edit_show")==1 && mpdRelated.canUserModify(userName)) {%>
			<button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="editRelate">修改</button>
            <%}%>
            <%if (msdRelated.getInt("btn_edit_show")==1 && (mpdRelated.canUserDel(userName) || canManage)) {%>
            <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="delRows">删除</button>
            <%}%>
            <%if (mpd.canUserImport(userName)) {%>
            <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="importXls">导入</button>
            <%}%>
            <%if (mpdRelated.canUserExport(userName)) {%>
            <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="exportXls">导出</button>
            <%}%>
            <%
                if (btnNames!=null && btnBclasses!=null) {
                    len = btnNames.length;
                    for (int i=0; i<len; i++) {
                        boolean isToolBtn = false;
                        if (!btnScripts[i].startsWith("{")) {
                            isToolBtn = true;
                        }
                        else {
                            JSONObject json = new JSONObject(btnScripts[i]);
                            if (json.get("btnType").equals("batchBtn")) {
                                isToolBtn = true;
                            }
                        }
                        if (isToolBtn) {
                            // 检查是否拥有权限
                            if (!privilege.isUserPrivValid(request, "admin")) {
                                boolean canSeeBtn = false;
                                if (btnRoles!=null && btnRoles.length>0) {
                                    String roles = btnRoles[i];
                                    String[] codeAry = StrUtil.split(roles, ",");
                                    // 如果codeAry为null，则表示所有人都能看到
                                    if (codeAry == null){
                                        canSeeBtn = true;
                                    }
                                    else{
                                        UserDb user = new UserDb();
                                        user = user.getUserDb(privilege.getUser(request));
                                        RoleDb[] rdAry = user.getRoles();
                                        if (rdAry!=null) {
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
                                }
                                else {
                                    canSeeBtn = true;
                                }

                                if (!canSeeBtn) {
                                    continue;
                                }
                            }
            %>
            <button class="layui-btn layui-btn-sm layui-btn-primary" lay-event="event_<%=moduleCodeRelated%><%=i%>"><%=btnNames[i]%></button>
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
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String fieldTitle = fieldsTitle[i];

		String title = "";
		boolean sortable = true;

		if ("#".equals(fieldTitle)) {
			if (fieldName.startsWith("main:")) {
				String[] subFields = StrUtil.split(fieldName, ":");
				if (subFields.length == 3) {
					FormDb subfd = new FormDb(subFields[1]);
					title = subfd.getFieldTitle(subFields[2]);
					sortable = false;
				}
			} else if (fieldName.startsWith("other:")) {
				String[] otherFields = StrUtil.split(fieldName, ":");
				if (otherFields.length == 5) {
					FormDb otherFormDb = new FormDb(otherFields[2]);
					title = otherFormDb.getFieldTitle(otherFields[4]);
					sortable = false;
				}
			} else if (fieldName.equals("cws_creator")) {
				title = "创建者";
			}
			else if (fieldName.equals("ID")) {
				fieldName = "CWS_MID"; // ModuleController中也作了同样转换
				title = "ID";
			}
			else if (fieldName.equals("cws_progress")) {
				title = "进度";
			}
			else if (fieldName.equals("cws_status")) {
				title = "状态";
			}
			else if (fieldName.equals("flowId")) {
				title = "流程号";
			}
			else if (fieldName.equals("cws_flag")) {
				title = "冲抵状态";
			}
			else if (fieldName.equals("colOperate")) {
				title = "操作";
			}
			else if (fieldName.equals("cws_create_date")) {
				title = "创建时间";
			}
			else if (fieldName.equals("flow_begin_date")) {
				title = "流程开始时间";
			}
			else if (fieldName.equals("flow_end_date")) {
				title = "流程结束时间";
			}
			else if (fieldName.equals("cws_id")) {
				title = "关联ID";
			}
			else {
				title = fdRelated.getFieldTitle(fieldName);
			}
		}
		else {
			title = fieldTitle;
		}

		String w = fieldsWidth[i];
		int wid = StrUtil.toInt(w, 100);
		if (w.indexOf("%")==w.length()-1) {
			w = w.substring(0, w.length()-1);
			wid = 800*StrUtil.toInt(w, 20)/100;
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
		}
		else {
			props = "{title: '" + title + "', field : '" + fieldName + "', width : " + wid + ", sort : " + sortable + ", ";
			if ("ID".equals(title)) {
				props += "fixed: true, ";
			}
			props += "align: 'center', hide: false }";
		}

		StrUtil.concat(colProps, ",", props);
	}

	// 如果允许显示操作列，且未定义colOperate，则将其加入，宽度默认为150
	if (isColOperateShow && colProps.lastIndexOf("colOperate")==-1) {
		StrUtil.concat(colProps, ",", "{title:'操作', field:'colOperate', sort:false, width:150, fixed: 'right'}");
	}
%>
<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp?parentId=<%=parentId%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&pageType=moduleListRelate"></script>
<script>
	layui.use('table', function() {
		var table = layui.table;

		table.render({
			elem: '#table_<%=moduleCodeRelated%>'
			,toolbar: '#toolbar_<%=moduleCodeRelated%>'
			,defaultToolbar: ['filter', 'print'/*, 'exports', {
				title: '提示'
				,layEvent: 'LAYTABLE_TIPS'
				,icon: 'layui-icon-tips'
			}*/]
			,method: 'post'
            ,url: 'moduleListRelate.do?code=<%=code%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&parentId=<%=parentId%>&op=search&moduleCodeRelated=<%=moduleCodeRelated%>&mode=&tagName='
			,cols: [[
                {type:'checkbox'},
				<%=colProps.toString()%>
			]]
			,id: 'table<%=moduleCodeRelated%>'
			,page: true
            ,limit: <%=pagesize%>
			,height: 310
			,parseData: function(res){ //将原始数据解析成 table 组件所规定的数据
				return {
					"code": res.errCode, //解析接口状态
					"msg": res.msg, //解析提示文本
					"count": res.total, //解析数据长度
					"data": res.rows //解析数据列表
				};
			}
		});

		//头工具栏事件
		table.on('toolbar(<%=moduleCodeRelated%>)', function(obj){
			var checkStatus = table.checkStatus(obj.config.id);
			switch(obj.event){
				case 'addRelate':
					layer.open({
						type: 2,
						title: '增加',
						shadeClose: true,
						shade: 0.6,
						area: ['90%', '90%'],
						content: 'module_add_relate.jsp?isTabStyleHor=false&code=<%=StrUtil.UrlEncode(code)%>&parentId=<%=parentId%>&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isShowNav=0'
					});
					break;
				case 'editRelate':
					var data = checkStatus.data;
                    if (data.length == 0) {
                        layer.msg('请选择记录');
                        return;
                    }
                    else if (data.length > 1) {
                        layer.msg('只能选择一条记录');
                        return;
                    }
                    var id = data[0].id;
                    console.log(data);
                    console.log('id=' + id);
                    layer.open({
                        type: 2,
                        title: '修改',
                        shadeClose: true,
                        shade: 0.6,
                        area: ['90%', '90%'],
                        content: 'module_edit_relate.jsp?isTabStyleHor=false&code=<%=StrUtil.UrlEncode(code)%>&parentId=<%=parentId%>&id=' + id + '&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isShowNav=0'
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
                        console.log('id=' + json.id);
                        if (ids == '') {
                            ids = json.id;
                        }
                        else {
                            ids += ',' + json.id;
                        }
                    }
                    layer.confirm('您确定要删除么？', {icon: 3, title:'提示'}, function(index){
                        //do something
                        try {
                            onBeforeModuleDel<%=moduleCodeRelated%>(ids);
                        }
                        catch (e) {}

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
                            beforeSend: function(XMLHttpRequest){
                                $("body").showLoading();
                            },
                            success: function(data, status){
                                data = $.parseJSON(data);
                                layer.msg(data.msg);

                                if (data.ret==1) {
                                    // doQuery();
                                    doQuery('<%=moduleCodeRelated%>');
                                    try {
                                        onModuleDel<%=moduleCodeRelated%>(ids, false);
                                    }
                                    catch (e) {}
                                }
                            },
                            complete: function(XMLHttpRequest, status){
                                $("body").hideLoading();
                            },
                            error: function(XMLHttpRequest, textStatus){
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
                    $("div[lay-id='" + obj.config.id + "']").find('.layui-table th').each(function() {
                        if($(this).data("field") && $(this).data("field")!="0" && $(this).data("field")!="colOperate") {
                            if (!$(this).hasClass('layui-hide')) {
                                if (cols=="") {
                                    cols = $(this).data("field");
                                }
                                else {
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
                    jQuery.each( fields, function(i, field) {
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
                            <%=btnScripts[i]%>
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
                                    console.log('id=' + json.id);
                                    if (ids == '') {
                                        ids = json.id;
                                    }
                                    else {
                                        ids += ',' + json.id;
                                    }
                                }
                                jConfirm("您确定要<%=btnNames[i]%>么？","提示",function(r){
                                    if(!r){return;}
                                    else{
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

        $('.search-form-<%=formCodeRelated%> .layui-btn').on('click', function(e){
		    e.preventDefault();
            table.reload('table<%=moduleCodeRelated%>', {
                page: {
                    curr: 1 //重新从第 1 页开始
                }
                ,where: $('.search-form-<%=formCodeRelated%>').serializeJsonObject()
            }, 'data');
		});

        //监听表格排序问题
        table.on('sort(<%=moduleCodeRelated%>)', function(obj){ //注：sort lay-filter="对应的值"
            table.reload('table<%=moduleCodeRelated%>', { //testTable是表格容器id
                initSort: obj // 记录初始排序，如果不设的话，将无法标记表头的排序状态。 layui 2.1.1 新增参数
                ,where: {
                    orderBy: obj.field //排序字段
                    ,sort: obj.type //排序方式
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
            }
            else {
                ids += ',' + json.id;
            }
        }
        return ids;
    }

    function doQuery(moduleCodeRelated) {
        layui.table.reload('table' + moduleCodeRelated);
    }
</script>
</div>
</body>
</html>
