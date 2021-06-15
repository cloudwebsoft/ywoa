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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    // 取从模块编码
    String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
    if ("".equals(moduleCodeRelated)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "缺少关联模块编码！"));
        return;
    }
    String menuItem = ParamUtil.get(request, "menuItem");
    // 取主模块编码
    String moduleCode = ParamUtil.get(request, "code");

    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
    if (msd == null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
        return;
    }
    String formCodeRelated = msd.getString("form_code");

    ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
    if (!mpd.canUserModify(privilege.getUser(request))) {
%>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    int id = ParamUtil.getInt(request, "id", -1);
    if (id==-1) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
        return;
    }

    // 检查数据权限，判断用户是否可以存取此条数据
    ModuleSetupDb parentMsd = new ModuleSetupDb();
    parentMsd = parentMsd.getModuleSetupDb(moduleCode);
    if (parentMsd==null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "父模块不存在！"));
        return;
    }
    String parentFormCode = parentMsd.getString("form_code");
    String mode = ParamUtil.get(request, "mode");
    // 是否通过选项卡标签关联
    boolean isSubTagRelated = "subTagRelated".equals(mode);
    String relateFieldValue = "";
    int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
    if (parentId==-1) {
        out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
        return;
    }
    else {
        if (!isSubTagRelated) {
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
            relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
            if (relateFieldValue==null) {
                // 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                relateFieldValue = SQLBuilder.IS_NOT_RELATED;
            }
        }
    }
    if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, i18nUtil.get("info_access_data_fail")));
        return;
    }

    boolean isTabStyleHor = ParamUtil.getBoolean(request, "isTabStyleHor", true);
    request.setAttribute("isTabStyleHor", isTabStyleHor);

    // 置嵌套表需要用到的cwsId
    request.setAttribute("cwsId", "" + id);
    // 置页面类型
    request.setAttribute("pageType", "edit");
    // 用于表单域选择窗体宏控件
    request.setAttribute("formCode", formCodeRelated); // 这里是为了使嵌套表在getNestSheet方法中，传递给当前编辑的表单中的嵌套表

    FormMgr fm = new FormMgr();
    FormDb fd = fm.getFormDb(formCodeRelated);

    request.setAttribute("formCodeRelated", formCodeRelated);

    com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);

    int isShowNav = ParamUtil.getInt(request, "isShowNav", 0);

    String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能设计-编辑内容</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <style>
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
            line-height: 27px;
            height: 29px !important;
            border: 1px solid #d4d4d4;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery.raty.min.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../inc/upload.js"></script>
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp?parentFormCode=<%=parentFormCode%>"></script>
    <script src="../inc/ajax_getpage.jsp"></script>
    <script src="../flow/form_js/form_js_<%=formCodeRelated%>.jsp?pageType=edit&parentId=<%=parentId%>&id=<%=id%>&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&time=<%=Math.random()%>"></script>
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
    if (isShowNav == 1) {
%>
<%@ include file="module_inc_menu_top.jsp" %>
<script>
    o("menu<%=menuItem%>").className = "current";
</script>
<div class="spacerH"></div>
<%
    } else {
        out.print("<BR>");
    }
%>
<div id="visualDiv" style="margin-top: 20px">
<form method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
    <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="left">
                <table width="100%">
                    <tr>
                        <td><%
                            com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
                            out.print(rd.rend(msd));
                        %>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <%if (fd.isHasAttachment()) {%>
        <tr>
            <td align="left" style="padding-left: 10px">
                <script>initUpload()</script>
            </td>
        </tr>
        <%}%>
        <tr>
            <td align="left" style="padding-left: 10px">
                <%
                com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
                Iterator ir = fdao.getAttachments().iterator();
                while (ir.hasNext()) {
                    com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next();
                %>
               <div id="attBox<%=am.getId()%>" style="margin: 10px 0px">
                   <img src="<%=Global.getRootPath()%>/images/attach.gif"/>
                    &nbsp; <a target="_blank" href="<%=Global.getRootPath()%>/visual_getfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>&nbsp;&nbsp;&nbsp;&nbsp;
                    [<a href="javascript:;" onclick="delAttach(<%=am.getId()%>)" style="cursor:pointer">删除</a>]
                </div>
                <%
                }
                %>
            </td>
        </tr>
        <tr>
            <td height="30" align="center">
                <input name="id" value="<%=id%>" type="hidden"/>
                <input id="cws_id" name="cws_id" value="<%=relateFieldValue%>" type="hidden"/>
                <button id="btnOk" class="btn btn-default">确定</button>
                <c:if test="${!isTabStyleHor}">
                &nbsp;&nbsp;
                <button id="btnClose" class="btn btn-default">关闭</button>
                </c:if>
                <input id="helper" value="1" type="hidden"/>
            </td>
        </tr>
    </table>
</form>
</div>
<script src="../js/layui/layui.js" charset="utf-8"></script>
</body>
<script>
    /*var layer;
    layui.use('layer', function() {
        layer = layui.layer;
        // loading加载效果
        var index = layer.load(2, {time: 2*1000,shade: [0.3, '#393D49']});
        setTimeout(function (data) {
            // 最后数据加载完 让 loading层消失
            layer.close(index);
        }, 300);
    });*/

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

    var layer;
    layui.use('layer', function(){
        layer = layui.layer;
    });

    $(function () {
        SetNewDate();

        $('#btnClose').click(function() {
            closeDlg();
        })
    });

    function delAttach(attId) {
        jConfirm('您确定要删除吗？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    url: "delAttachRelate.do",
                    type: "post",
                    data: {
                        id: <%=id%>,
                        parentId: <%=parentId%>,
                        code: "<%=moduleCode%>",
                        formCodeRelated: "<%=formCodeRelated%>",
                        attachId: attId,
                        mode: "<%=mode%>",
                        moduleCodeRelated: "<%=moduleCodeRelated%>"
                    },
                    beforeSend: function(XMLHttpRequest){
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        var data = $.parseJSON($.trim(data));
                        if (data.ret=="0") {
                            jAlert(data.msg, "提示");
                        }
                        else {
                            jAlert(data.msg, "提示");
                            $('#attBox' + attId).remove();
                        }
                    },
                    complete: function(XMLHttpRequest, status){
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        })
    }

    // 控件完成上传后，调用Operate()
    function Operate() {
        // alert(redmoonoffice.ReturnMessage);
    }

    $(function () {
        var f_helper = new LiveValidation('helper');

        $('#btnOk').click(function (e) {
            e.preventDefault();

            if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
                jAlert("请检查表单中的内容填写是否正常！", "提示");
                return;
            }
            $('#btnOk').attr("disabled", true);

            var formData = new FormData($('#visualForm')[0]);

            $.ajax({
                url: 'updateRelate.do?id=<%=id%>&parentId=<%=parentId%>&code=<%=StrUtil.UrlEncode(moduleCode)%>&formCodeRelated=<%=formCodeRelated%>&isShowNav=<%=isShowNav%>&moduleCodeRelated=<%=moduleCodeRelated%>&tabIdOpener=<%=tabIdOpener%>' ,
                type: 'post',
                data: formData,
                async: true,
                // 下面三个参数要指定，如果不指定，会报一个JQuery的错误
                cache: false,
                contentType: false,
                processData: false,
                dataType: "html",
                beforeSend: function(XMLHttpRequest){
                    $('body').showLoading();
                },
                success: function (data) {
                    var data = $.parseJSON($.trim(data));
                    if (data.ret=="0") {
                        jAlert(data.msg, "提示");
                        $('#btnOk').attr("disabled", false);
                    }
                    else {
                        try {
                            onModuleEdit<%=moduleCodeRelated%>(<%=id%>, '<%=tabIdOpener%>', <%=isTabStyleHor%>);
                        }
                        catch (e) {}

                        reloadTab("<%=tabIdOpener%>");

                        // 如果有父窗口，则自动刷新父窗口
                        if (window.opener != null) {
                            window.opener.location.reload();
                            window.opener.focus();
                            window.close();
                        }
                        else {
                            <%
                            if (isTabStyleHor) {
                            %>
                            jAlert(data.msg, "提示", function() {
                                window.location.reload();
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
                    }
                },
                complete: function(XMLHttpRequest, status){
                    $('body').hideLoading();
                },
                error: function (returndata) {
                    $('body').hideLoading();
                    $('#btnOk').attr("disabled", false);
                    alert(returndata);
                }
            });
        });
    });

    function closeDlg() {
        var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
        parent.layer.close(index)
    }
</script>
</html>
