<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ErrMsgException" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormMgr" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModulePrivDb" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String formCode = ParamUtil.get(request, "formCode");
    if ("".equals(formCode)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
    ModuleSetupDb msdRelated = new ModuleSetupDb();
    msdRelated = msdRelated.getModuleSetupDb(moduleCodeRelated);
    String formCodeRelated = msdRelated.getString("form_code");

    String menuItem = ParamUtil.get(request, "menuItem");
    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "menuItem", menuItem, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    String moduleCode = ParamUtil.get(request, "code");

    FormMgr fm = new FormMgr();
    FormDb fd = fm.getFormDb(formCodeRelated);

    String relateFieldValue = "";
    int parentId = ParamUtil.getInt(request, "parentId"); // 父模块的ID
    if (parentId == -1) {
        out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
        return;
    }

    String parentPageType = ParamUtil.get(request, "parentPageType");
    boolean isTabStyleHor = ParamUtil.getBoolean(request, "isTabStyleHor", true);
    request.setAttribute("isTabStyleHor", isTabStyleHor);

    // 用于表单域选择窗体宏控件及查询选择宏控件
    request.setAttribute("formCodeRelated", formCodeRelated);
    // 置嵌套表需要用到的pageType
    request.setAttribute("pageType", "add");

    ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
    if (!mpd.canUserAppend(privilege.getUser(request))) {
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
        return;
    }

    int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能模块设计-添加内容</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <style>
        input[readonly] {
            background-color: #ddd;
        }
        select[readonly] {
            background-color: #ddd;
        }
        textarea[readonly] {
            background-color: #ddd;
        }
        select {
            line-height: 27px;
            height: 29px !important;
            border: 1px solid #d4d4d4;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery.raty.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../inc/upload.js"></script>
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp?parentFormCode=<%=formCode%>"></script>
    <script src="../inc/ajax_getpage.jsp"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp?pageType=addRelate&parentId=<%=parentId%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&time=<%=Math.random()%>"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../js/select2/i18n/zh-CN.js"></script>
    <script src="../inc/map.js"></script>
</head>
<body>
<%
    com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(formCode);
    relateFieldValue = fdmMain.getRelateFieldValue(parentId, moduleCodeRelated);
    if (relateFieldValue == null) {
        out.print(StrUtil.jAlert_Back("请检查模块是否相关联！", "提示"));
        return;
    }

    if (isShowNav == 1) {
%>
<%@ include file="module_inc_menu_top.jsp" %>
<script>
    o("menu<%=menuItem%>").className = "current";
</script>
<%}%>
<%
    if (fd == null || !fd.isLoaded()) {
        out.println(StrUtil.jAlert("表单不存在！", "提示"));
        return;
    }
%>
<div class="spacerH"></div>
<form method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
    <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="left">
                <div>
                    <%
                        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
                        out.print(rd.rendForAdd());
                    %>
                </div>
            </td>
        </tr>
        <%if (fd.isHasAttachment()) {%>
        <tr>
            <td>
                <div style="clear:both">
                    <script>initUpload()</script>
                </div>
            </td>
        </tr>
        <%}%>
        <tr>
            <td height="30" align="center">
                <button id="btnAdd" class="btn btn-default">确定</button>
                <c:choose>
                    <c:when test="${isTabStyleHor}">
                        &nbsp;&nbsp;
                        <button id="btnBack" class="btn btn-default" type="button" name="btnBack" onclick="window.history.back()">返回</button>
                    </c:when>
                    <c:otherwise>
                        &nbsp;&nbsp;
                        <button id="btnClose" class="btn btn-default" type="button" name="btnBack" onclick="closeDlg()">关闭</button>
                    </c:otherwise>
                </c:choose>

                <input id="cws_id" name="cws_id" value="<%=relateFieldValue%>" type="hidden"/>
                <input id="helper" value="1" type="hidden"/>
            </td>
        </tr>
    </table>
    <span id="spanTempCwsIds"></span>
</form>
<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
<script src="../js/layui/layui.js" charset="utf-8"></script>
</body>
<script>
    var layer;
    layui.use('layer', function(){
        layer = layui.layer;
    });

    function closeDlg() {
        var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
        parent.layer.close(index);
    }

    <%
    if (msdRelated.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
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

    $(function () {
        SetNewDate();
    });

    // 控件完成上传后，调用Operate()
    function Operate() {
        // alert(redmoonoffice.ReturnMessage);
    }

    // 记录添加的嵌套表格2记录的ID
    function addTempCwsId(formCode, cwsId) {
        var name = "<%=com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS%>_" + formCode;
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

    $(function () {
        var f_helper = new LiveValidation('helper');

        $('#btnAdd').click(function () {
            if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
                jAlert("请检查表单中的内容填写是否正常！", "提示");
                return;
            }
            $('#btnAdd').attr("disabled", true);

            var formData = new FormData($('#visualForm')[0]);

            $.ajax({
                url: 'createRelate.do?code=<%=StrUtil.UrlEncode(moduleCode)%>&parentId=<%=parentId%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&moduleCodeRelated=<%=moduleCodeRelated%>' ,
                type: 'post',
                data: formData,
                async: true,
                // 下面三个参数要指定，如果不指定，会报一个JQuery的错误
                cache: false,
                contentType: false,
                processData: false,
                dataType: "html",
                beforeSend: function(XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data) {
                    var data = $.parseJSON($.trim(data));
                    if (data.ret=="0") {
                        jAlert(data.msg, "提示");
                        $('#btnAdd').attr("disabled", false);
                    }
                    else {
                        try {
                            onModuleAdd<%=moduleCodeRelated%>(data.id, <%=isTabStyleHor%>);
                        }
                        catch (e) {}
                        // 如果有父窗口，则自动刷新父窗口
                        if (window.opener != null) {
                            window.opener.location.reload();
                        }
                        <%
                        if (isTabStyleHor) {
                        %>
                        jAlert(data.msg, "提示", function() {
                            window.location.href = "module_list_relate.jsp?parentPageType=<%=parentPageType%>&code=<%=StrUtil.UrlEncode(moduleCode)%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&isShowNav=<%=isShowNav%>&moduleCodeRelated=<%=moduleCodeRelated%>";
                        });
                        <%
                        }
                        else {
                        %>
                        parent.layui.table.reload('table<%=moduleCodeRelated%>');
                        layer.open({
                            type: 1
                            ,offset: 'auto' //具体配置参考：http://www.layui.com/doc/modules/layer.html#offset
                            ,id: 'dlg' //防止重复弹出
                            ,content: '<div style="padding: 20px 100px;">'+ data.msg +'</div>'
                            ,btn: '确定'
                            ,btnAlign: 'c' //按钮居中
                            ,shade: 0 //不显示遮罩
                            ,yes: function(){
                                closeDlg();
                            }
                        });
                        <%
                        }
                        %>
                    }
                },
                complete: function(XMLHttpRequest, status){
                    $('body').hideLoading();
                },
                error: function (returndata) {
                    $('body').hideLoading();
                    $('#btnAdd').attr("disabled", false);
                    alert(returndata);
                }
            });
        });
    });
</script>
</html>
