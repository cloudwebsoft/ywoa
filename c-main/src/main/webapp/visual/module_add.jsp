<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
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

    String formCode = msd.getString("form_code");
    if (formCode.equals("")) {
        out.print(SkinUtil.makeErrMsg(request, "编码不能为空"));
        return;
    }

    FormMgr fm = new FormMgr();
    FormDb fd = fm.getFormDb(formCode);

    ModulePrivDb mpd = new ModulePrivDb(code);
    if (!mpd.canUserAppend(privilege.getUser(request))) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String modUrlList = StrUtil.getNullStr(msd.getString("url_list"));
    if (modUrlList.equals("")) {
        String privurl = ParamUtil.get(request, "privurl");
        if (privurl.equals("")) {
            modUrlList = request.getContextPath() + "/" + "visual/moduleListPage.do?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
        }
        else {
            modUrlList = privurl;
        }
    } else {
        modUrlList = request.getContextPath() + "/" + modUrlList;
    }

    request.setAttribute("modUrlList", modUrlList);

    // 置嵌套表需要用到的pageType
    request.setAttribute("pageType", ConstUtil.PAGE_TYPE_ADD);
    // 置NestSheetCtl需要用到的formCode
    request.setAttribute("formCode", formCode);

    if (fd == null || !fd.isLoaded()) {
        out.print(StrUtil.jAlert("表单不存在！", "提示"));
        return;
    }

    request.setAttribute("formCode", formCode);
    request.setAttribute("code", code);
    request.setAttribute("skinPath", SkinMgr.getSkinPath(request));
    request.setAttribute("privurl", StrUtil.UrlEncode(ParamUtil.get(request, "privurl")));
    request.setAttribute("nameTempCwsId", com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS);

    Map map = new HashMap();
    Enumeration reqParamNames = request.getParameterNames();
    while (reqParamNames.hasMoreElements()) {
        String paramName = (String) reqParamNames.nextElement();
        String[] paramValues = request.getParameterValues(paramName);
        if (paramValues.length == 1) {
            String paramValue = ParamUtil.getParam(request, paramName);
            // 过滤掉formCode等
            if (paramName.equals("code")
                    || paramName.equals("formCode")
                    || paramName.equals("moduleCode")
                    || paramName.equals("mainCode")
                    || paramName.equals("menuItem")
                    || paramName.equals("parentId")
                    || paramName.equals("moduleCodeRelated")
                    || paramName.equals("formCodeRelated")
                    || paramName.equals("mode") // 去掉mode及tagName，否则当存在mode=subTagRelated，关联模块中就会有问题
                    || paramName.equals("tagName")
                    || paramName.equals("id")
            ) {
                ;
            }
            else {
                map.put(paramName, paramValue);
            }
        }
    }
    request.setAttribute("map", map);
    request.setAttribute("isHasAttachment", fd.isHasAttachment());

    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
    request.setAttribute("rend", rd.rendForAdd(msd));

    com.alibaba.fastjson.JSONArray buttons = msd.getButtons(request, ConstUtil.PAGE_TYPE_ADD, null, 1);
    request.setAttribute("buttons", buttons);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>添加内容</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
    <link href="../flowstyle.css" rel="stylesheet" type="text/css"/>
    <link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <style>
        input,textarea {
            outline:none;
        }
        input[readonly]{
            background-color: #ddd;
        }
        select[readonly]{
            background-color: #ddd;
        }
        textarea[readonly]{
            background-color: #ddd;
        }

        select {
            /*line-height: 27px;
            height: 29px !important;*/
            border: 1px solid #d4d4d4;
        }
        <%=StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_ADD))%>
    </style>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>

    <link rel="stylesheet" href="../js/poshytip/tip-yellowsimple/tip-yellowsimple.css" type="text/css" />
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
    <script src="../js/BootstrapMenu.min.js"></script>
    <script src="../flow/form_js/form_js_${formCode}.jsp?pageType=<%=ConstUtil.PAGE_TYPE_ADD%>&code=${code}&time=<%=Math.random()%>"></script>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp" %>
<script>
    $('#menu1').addClass('current');
</script>
<div class="spacerH"></div>
<%@ include file="../inc/tip_phrase.jsp" %>
<script>
    function add() {
        // 表单合法性校验，用于在form_js_***中扩展
        try {
            var re = checkModuleForm();
            if ( "" !== re ) {
                jAlert(re, '提示');
                return;
            }
        }
        catch (e) {}

        try {
            ctlOnBeforeSerialize();
        } catch (e) {
        }

        var f_helper = new LiveValidation('cwsHelper');
        if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
            if (LiveValidation.liveErrMsg.length < 100) {
                jAlert(LiveValidation.liveErrMsg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
            else {
                jAlert("请检查表单中的内容填写是否正常！","提示");
            }
            return;
        }

        $('#btnOk').attr("disabled", true);
        $('#visualForm').submit();
    }

    $(function () {
        SetNewDate();

        $('#btnOK').click(function (e) {
            e.preventDefault();
            add();
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
            beforeSubmit: preSubmit,  // pre-submit callback
            success: showResponse  // post-submit callback
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
        if (data.ret != "1") {
            if (data.msg != null) {
                data.msg = data.msg.replace(/\\r/ig, "<BR>");
            }
            jAlert(data.msg, "提示");
            $('#btnOK').attr("disabled", false);
        } else {
            try {
                onModuleAdd<%=code%>(data.id);
            }
            catch (e) {}
            var url = "${modUrlList}";
            if (data.addToUrl && data.addToUrl!="") {
                url = data.addToUrl;
            }

            if (url.indexOf("?")!=-1) {
                url += "&${reqParams}"; // reqParams在module_inc_menu_top.jsp中定义
            }
            else {
                url += "?${reqParams}";
            }
            jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', url);
        }
    }
</script>
<form action="create.do?code=${code}&formCode=${formCode}&privurl=${privurl}" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm" class="form-inline">
    <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="left">
                ${rend}
            </td>
        </tr>
        <c:if test="${isHasAttachment}">
        <tr>
            <td style="padding-top:3px">
                <script>initUpload()</script>
            </td>
        </tr>
        </c:if>
        <tr>
            <td height="30" align="center" style="padding-top: 10px">
                <c:if test="${fn:length(buttons)==0}">
                    <button id="btnOK" class="btn btn-default">确定</button>
                    &nbsp;&nbsp;
                    <button id="btnBack" class="btn btn-default">返回</button>
                </c:if>
                <c:forEach items="${buttons}" var="button">
                    <c:choose>
                        <c:when test="${button.event=='click'}">
                            <button id="${button.id}" class="btn btn-default" title="${button.title}" onclick="${button.href}">${button.name}</button>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <c:if test="${button.target=='newTab'}">
                                <button id="${button.id}" class="btn btn-default" title="${button.title}" onclick="addTab('${button.name}', '${button.href}')">${button.name}</button>
                                &nbsp;&nbsp;&nbsp;&nbsp;
                            </c:if>
                            <c:if test="${button.target=='curTab'}">
                                <button id="${button.id}" class="btn btn-default" title="${button.title}" onclick="window.location.href='${button.href}'">${button.name}</button>
                                &nbsp;&nbsp;&nbsp;&nbsp;
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <input id="cwsHelper" name="cwsHelper" value="1" type="hidden"/>
                <c:forEach items="${map}" var="mymap" >
                <input type='hidden' name='<c:out value="${mymap.key}" />' value='<c:out value="${mymap.value}" />'/>
                </c:forEach>
            </td>
        </tr>
    </table>
    <span id="spanTempCwsIds"></span>
</form>
<br/>
</body>
<link rel="stylesheet" href="../js/jquery-contextmenu/jquery.contextMenu.min.css">
<script src="../js/jquery-contextmenu/jquery.contextMenu.js"></script>
<script src="../js/jquery-contextmenu/jquery.ui.position.min.js"></script>
<script src="../js/layui/layui.js" charset="utf-8"></script>
<script>
    <%
    if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
    %>
    // 不能放在$(function中，原来的tabStyle_8风格会闪现
    // $(function() {
    var $table = $('#visualForm').find('.tabStyle_8');
    if ($table[0] == null) {
        $table = $('#visualForm').find('.tabStyle_1');
    }
    $table.addClass('layui-table');
    $table.removeClass('tabStyle_8');
    // })
    <%
    }
    %>

    $(function() {
       $('#btnBack').click(function(e) {
           e.preventDefault();
           window.history.back();
       });
    });

    // 记录添加的嵌套表格2记录的ID
    function addTempCwsId(formCode, cwsId) {
        var name = "${nameTempCwsId}_" + formCode;
        var inp;
        try {
            inp = document.createElement('<input type="hidden" name="' + name + '" />');
        } catch (e) {
            inp = document.createElement("input");
            inp.type = "hidden";
            inp.name = name;
        }
        inp.value = cwsId;

        spanTempCwsIds.appendChild(inp);
    }

    $(function() {
        $('input[type=radio]').each(function(i) {
            var name = $(this).attr("name");
            if ($(this).attr("readonly")==null) {
                $(this).addClass('radio-menu');
            }
        });

        // 不能用BootstrapMenu，因为chrome上会导致radio无法点击
        $.contextMenu({
            selector: '.radio-menu',
            trigger: 'hover',
            delay: 1000,
            callback: function(key, options) {
                if (key == 'cancel') {
                    var $obj = options.$trigger;
                    var name = $obj.attr('name');
                    $('input[type=radio][name="' + name + '"]:checked').attr("checked", false);
                }
            },
            items: {
                "cancel": {name: "取消选择", icon: function($element, key, item){ return 'context-menu-icon context-menu-icon-quit'; }}
            }
        })

        $('input').each(function() {
            if ($(this).attr('kind')=='DATE' || $(this).attr('kind')=='DATE_TIME') {
                $(this).attr('autocomplete', 'off');
            }
        });

        // 初始化tip提示
        // 不能通过$("#visualForm").serialize()来获取所有的元素，因为radio或checkbox未被选中，则不会被包含
        $('#visualForm input, #visualForm select, #visualForm textarea').each(function() {
            // 如果不是富文本编辑宏控件，如果富文本编辑宏控件加上了form-control，则会因为生成ueditor时，外面包裹的div也带上了form-control，致富文本编辑器位置变成了浮于表单上
            if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && $(this).attr('type')!='hidden' && $(this).attr('type')!='file') {
                $(this).addClass('form-control');
            }

            var tip = '';
            if ($(this).attr('type') == 'radio') {
                tip = $(this).parent().attr('tip');
            }
            else {
                tip = $(this).attr('tip');
            }
            if (null!=tip && ""!=tip) {
                $(this).poshytip({
                    content: function(){return tip;},
                    className: 'tip-yellowsimple',
                    alignTo: 'target',
                    alignX: 'center',
                    offsetY: 5,
                    allowTipHover: true
                });
            }
        });
    });
</script>
</html>
