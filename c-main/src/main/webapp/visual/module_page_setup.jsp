<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String moduleCode = ParamUtil.get(request, "code");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(moduleCode);

    if (!msd.isLoaded()) {
        out.print(StrUtil.jAlert_Back("该模块不存在！", "提示"));
        return;
    }

    String pageSetup = msd.getString("page_setup");
    if (StringUtils.isEmpty(pageSetup)) {
        pageSetup = "{}";
    }
    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
    com.alibaba.fastjson.JSONObject commonJson = jsonObject.getJSONObject("commonPage");
    int pageStyle = ConstUtil.PAGE_STYLE_DEFAULT;
    if (commonJson!=null) {
        pageStyle = commonJson.getIntValue("pageStyle");
    }
    com.alibaba.fastjson.JSONObject editJson = jsonObject.getJSONObject("editPage");
    boolean isReloadAfterUpdate = true;
    boolean isTabStyleHorEdit = true;
    if (editJson != null) {
        isReloadAfterUpdate = editJson.getBoolean("isReloadAfterUpdate");
        isTabStyleHorEdit = editJson.getBoolean("isTabStyleHor");
    }
    com.alibaba.fastjson.JSONObject showJson = jsonObject.getJSONObject("showPage");
    boolean isTabStyleHorShow = true;
    if (showJson != null) {
        isTabStyleHorShow = showJson.getBoolean("isTabStyleHor");
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能模块设计 - 页面设置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../js/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_slidemenu.css"/>
    <script src="../js/tabpanel/Toolbar.js" type="text/javascript"></script>
    <style>
        .toolbar {
            position: inherit;
        }

        .ul-sortalbe {
            list-style-type: none;
            margin: 0;
            padding: 0;
        }

        .ul-sortalbe li {
            margin: 3px 3px 3px 0;
            padding-top: 15px;
            float: left;
            width: 100px;
            height: 50px;
            font-size: 10pt;
            text-align: center;
            color: black;
            font-weight: normal;
        }

        .ul-sortalbe .ui-selecting {
            background: #FECA40;
        }

        .ul-sortalbe .ui-selected {
            background: #F39814;
            color: black;
            font-weight: normal;
        }

        .ui-state-disabled {
            background: #aaa;
        }

        .btn-icon {
            width: 16px;
            height: 16px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<%@ include file="module_setup_inc_menu_top.jsp" %>
<script>
    o("menu11").className = "current";
</script>
<div class="spacerH"></div>
<form id="formCommon">
    <table class="tabStyle_1 percent80" width="80%" align="center">
        <tr>
            <td colspan="2" class="tabStyle_1_title">通用设置</td>
        <tr>
            <td width="18%">页面风格</td>
            <td width="82%">
                <input name="pageStyle" type="radio" value="<%=ConstUtil.PAGE_STYLE_DEFAULT%>" checked/>&nbsp;默认
                <input name="pageStyle" type="radio" value="<%=ConstUtil.PAGE_STYLE_LIGHT%>"/>&nbsp;轻量
            </td>
        <tr>
            <td colspan="2" align="center">
                <button id="btnOkCommon" class="btn btn-default">保存</button>
                <input name="moduleCode" value="<%=moduleCode%>" type="hidden"/>
            </td>
    </table>
</form>

<form id="formEdit">
    <table class="tabStyle_1 percent80" width="80%" align="center">
        <tr>
            <td colspan="2" class="tabStyle_1_title">编辑页</td>
        <tr>
            <td width="18%">保存后自动刷新</td>
            <td width="82%" title="如果有附件需上传，建议保存后自动刷新以看到效果">
                <input name="isReloadAfterUpdate" type="radio" value="true" checked/>&nbsp;是
                <input name="isReloadAfterUpdate" type="radio" value="false"/>&nbsp;否
            </td>
        </tr>
        <tr>
            <td>关联模块显示方向</td>
            <td>
                <input name="isTabStyleHor" value="true" type="radio" checked/>&nbsp;横向
                <input name="isTabStyleHor" value="false" type="radio"/>&nbsp;纵向
            </td>
        </tr>
        <tr>
            <td>
                按钮
            </td>
            <td>
                <div id="toolbarPageEdit" class="toolbar-box"></div>
                <script>
                    var toolbarEdit;
                    function initToolbarEdit(toolbarId) {
                        // 不能在ready中初始化toolbar，因为有时onload时间可能比较长，会致toolbar要过很长时间才能显示
                        toolbarEdit = new Toolbar({
                            renderTo: toolbarId,
                            // border: 'top',
                            items: [
                                {
                                    type: 'button',
                                    text: '添加',
                                    title: '添加',
                                    bodyStyle: 'add',
                                    useable: 'T',
                                    handler: function () {
                                        createBtn('<%=moduleCode%>', 'editPage');
                                        return false;
                                    }
                                },
                                {
                                    type: 'button',
                                    text: '修改',
                                    title: '修改',
                                    bodyStyle: 'edit',
                                    useable: 'T',
                                    handler: function () {
                                        var objLi = $("#sortableEdit").children().eq(curIndexEdit)[0];
                                        editProp(objLi, 'editPage');
                                        return false;
                                    }
                                },
                                {
                                    type: 'button',
                                    text: '删除',
                                    title: '删除',
                                    bodyStyle: 'del',
                                    useable: 'T',
                                    handler: function () {
                                        $li = $("#sortableEdit").children().eq(curIndexEdit);
                                        if ($li.attr('btnId') == '<%=ConstUtil.BTN_OK%>') {
                                            jAlert('系统按钮不能被删除！', '提示');
                                            return false;
                                        }
                                        jConfirm("您确定要删除么？","提示",function(r) {
                                            if (!r) {
                                                return;
                                            } else {
                                                objLi = $li.remove();
                                            }
                                        });
                                        return false;
                                    }
                                }
                            ]
                        });
                        toolbarEdit.render();
                    }

                    initToolbarEdit('toolbarPageEdit');

                    var action = '';
                    var curPageType = '';
                    function createBtn(moduleCode, pageType) {
                        action = 'create';
                        curPageType = pageType;
                        openWin('module_page_btn_prop.jsp?moduleCode=<%=moduleCode%>', 800, 400);
                    }
                </script>
                <ul id="sortableEdit" class="ul-sortalbe">
                    <%
                        com.alibaba.fastjson.JSONArray btnProps = null;
                        if (editJson!=null && editJson.containsKey("btnProps")) {
                            btnProps = editJson.getJSONArray("btnProps");
                        }
                        if (btnProps == null || btnProps.size() == 0) {
                    %>
                    <li class="ui-state-default" title="按下图标可拖动，双击图标可编辑属性" btnId="<%=ConstUtil.BTN_OK%>">
                        <img class="btn-icon handle" src="../images/setup.png"/>
                        <span style="margin-top:5px">确定</span>
                        <textarea id="<%=ConstUtil.BTN_OK%>Prop" style="display:none"></textarea>
                    </li>
                    <%
                    }
                    else {
                        for (int i = 0; i < btnProps.size(); i++) {
                            com.alibaba.fastjson.JSONObject btnJson = btnProps.getJSONObject(i);
                            String btnId = btnJson.getString("id");
                            String btnName = "";
                            if (btnJson.containsKey("name")) {
                                btnName = btnJson.getString("name");
                            }
                            else {
                                btnName = ModuleUtil.getBtnDefaultName(btnId);
                            }
                            boolean enabled = true;
                            if (btnJson.containsKey("enabled")) {
                                enabled = btnJson.getBoolean("enabled");
                            }
                    %>
                    <li class="ui-state-default <%=enabled?"":"ui-state-disabled"%>" title="按下图标可拖动，双击图标可编辑属性" btnId="<%=btnId%>">
                        <img class="btn-icon handle" src="../images/setup.png"/>
                        <span style="margin-top:5px"><%=btnName%></span>
                        <textarea id="<%=btnId%>Prop" style="display:none"><%=btnJson.toString()%></textarea>
                    </li>
                    <%
                            }
                        }
                    %>
                </ul>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <button id="btnOkEdit" class="btn btn-default">保存</button>
                <input name="moduleCode" value="<%=moduleCode%>" type="hidden"/>
                <input id="btnPropsEdit" name="btnProps" type="hidden"/>
            </td>
    </table>
</form>
<form id="formShow">
    <table class="tabStyle_1 percent80" width="80%" align="center">
        <tr>
            <td colspan="2" class="tabStyle_1_title">详情页</td>
        </tr>
        <tr>
            <td width="18%">关联模块显示方向</td>
            <td>
                <input name="isTabStyleHor" value="true" type="radio" checked/>&nbsp;横向
                <input name="isTabStyleHor" value="false" type="radio"/>&nbsp;纵向
            </td>
        </tr>
        <tr>
            <td>
                按钮
            </td>
            <td>
                <div id="toolbarPageShow" class="toolbar-box"></div>
                <script src="../js/tabpanel/Toolbar.js" type="text/javascript"></script>
                <script>
                    var toolbarShow;
                    function initToolbarShow(toolbarId) {
                        // 不能在ready中初始化toolbar，因为有时onload时间可能比较长，会致toolbar要过很长时间才能显示
                        toolbarShow = new Toolbar({
                            renderTo: toolbarId,
                            // border: 'top',
                            items: [
                                {
                                    type: 'button',
                                    text: '添加',
                                    title: '添加',
                                    bodyStyle: 'add',
                                    useable: 'T',
                                    handler: function () {
                                        createBtn('<%=moduleCode%>', 'showPage');
                                        return false;
                                    }
                                },
                                {
                                    type: 'button',
                                    text: '修改',
                                    title: '修改',
                                    bodyStyle: 'edit',
                                    useable: 'T',
                                    handler: function () {
                                        var objLi = $("#sortableShow").children().eq(curIndexShow)[0];
                                        editProp(objLi, 'showPage');
                                        return false;
                                    }
                                },
                                {
                                    type: 'button',
                                    text: '删除',
                                    title: '删除',
                                    bodyStyle: 'del',
                                    useable: 'T',
                                    handler: function () {
                                        $li = $("#sortableShow").children().eq(curIndexShow);
                                        if ($li.attr('btnId') == '<%=ConstUtil.BTN_PRINT%>' || $li.attr('btnId') == '<%=ConstUtil.BTN_EDIT%>') {
                                            jAlert('系统按钮不能被删除！', '提示');
                                            return false;
                                        }
                                        jConfirm("您确定要删除么？","提示",function(r) {
                                            if (!r) {
                                                return;
                                            } else {
                                                objLi = $li.remove();
                                            }
                                        });
                                        return false;
                                    }
                                }
                            ]
                        });
                        toolbarShow.render();
                    }

                    initToolbarShow('toolbarPageShow');
                </script>
                <ul id="sortableShow" class="ul-sortalbe">
                    <%
                        if (showJson!=null && showJson.containsKey("btnProps")) {
                            btnProps = showJson.getJSONArray("btnProps");
                        }
                        if (btnProps == null || btnProps.size() == 0) {
                    %>
                    <li class="ui-state-default" title="按下图标可拖动，双击图标可编辑属性" btnId="<%=ConstUtil.BTN_PRINT%>">
                        <img class="btn-icon handle" src="../images/setup.png"/>
                        <span style="margin-top:5px">打印</span>
                        <textarea id="<%=ConstUtil.BTN_PRINT%>Prop" style="display:none"></textarea>
                    </li>
                    <li class="ui-state-default" title="按下图标可拖动，双击图标可编辑属性" btnId="<%=ConstUtil.BTN_EDIT%>">
                        <img class="btn-icon handle" src="../images/setup.png"/>
                        <span style="margin-top:5px">编辑</span>
                        <textarea id="<%=ConstUtil.BTN_EDIT%>Prop" style="display:none"></textarea>
                    </li>
                    <%
                        }
                        else {
                            for (int i = 0; i < btnProps.size(); i++) {
                                com.alibaba.fastjson.JSONObject btnJson = btnProps.getJSONObject(i);
                                String btnId = btnJson.getString("id");
                                String btnName = "";
                                if (btnJson.containsKey("name")) {
                                    btnName = btnJson.getString("name");
                                }
                                else {
                                    btnName = ModuleUtil.getBtnDefaultName(btnId);
                                }
                                boolean enabled = true;
                                if (btnJson.containsKey("enabled")) {
                                    enabled = btnJson.getBoolean("enabled");
                                }
                    %>
                    <li class="ui-state-default <%=enabled?"":"ui-state-disabled"%>" title="按下图标可拖动，双击图标可编辑属性" btnId="<%=btnId%>">
                        <img class="btn-icon handle" src="../images/setup.png"/>
                        <span style="margin-top:5px"><%=btnName%></span>
                        <textarea id="<%=btnId%>Prop" style="display:none"><%=btnJson.toString()%></textarea>
                    </li>
                    <%
                            }
                        }
                    %>
                </ul>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <button id="btnOkShow" class="btn btn-default">保存</button>
                <input name="moduleCode" value="<%=moduleCode%>" type="hidden"/>
                <input id="btnPropsShow" name="btnProps" type="hidden"/>
            </td>
    </table>
</form>
<br>
<script>
    $(function () {
        setRadioValue("pageStyle", "<%=pageStyle%>");
        setRadioValue("isReloadAfterUpdate", "<%=isReloadAfterUpdate%>");

        $("#formEdit").find("[name='isTabStyleHor'][value='<%=isTabStyleHorEdit%>']").attr("checked", true);
        $("#formShow").find("[name='isTabStyleHor'][value='<%=isTabStyleHorShow%>']").attr("checked", true);

        $('#btnOkCommon').click(function (e) {
            e.preventDefault();

            $.ajax({
                type: "post",
                url: "putCommonPageSetup.do",
                // contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                data: $('#formCommon').serialize(),
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });

        $('#btnOkEdit').click(function (e) {
            e.preventDefault();

            $('#btnPropsEdit').val(makeBtnProps('sortableEdit'));

            $.ajax({
                type: "post",
                url: "putEditPageSetup.do",
                // contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                data: $('#formEdit').serialize(),
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });

        $('#btnOkShow').click(function (e) {
            e.preventDefault();

            $('#btnPropsShow').val(makeBtnProps('sortableShow'));

            $.ajax({
                type: "post",
                url: "putShowPageSetup.do",
                // contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                data: $('#formShow').serialize(),
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    });

    var curIndexShow = -1;
    var curIndexEdit = -1;
    var curPropObj;
    var curLiObj;

    $(function () {
        $("#sortableShow")
            .sortable({
                handle: ".handle",
                stop: function () {
                }
            })
            .selectable({
                stop: function () {
                    $(".ui-selected", this).each(function () {
                        curIndexShow = $("#sortableShow li").index(this);
                    });
                }
            })
            .find("li")
            .addClass("ui-corner-all")
            // .prepend("<div class='handle'><span class='ui-icon ui-icon-carat-2-n-s'></span></div>"); // 只要有class为handle的元素，就可以拖动

        // 实现单选
        $("#sortableShow").selectable({
            selected: function (event, ui) {
                $(ui.selected).siblings().removeClass("ui-selected");
            }
        });

        $("#sortableShow li").dblclick(function () {
            editProp(this, 'showPage');
        });

        $("#sortableEdit")
            .sortable({
                handle: ".handle",
                stop: function () {
                }
            })
            .selectable({
                stop: function () {
                    $(".ui-selected", this).each(function () {
                        curIndexEdit = $("#sortableEdit li").index(this);
                    });
                }
            })
            .find("li")
            .addClass("ui-corner-all");

        // 实现单选
        $("#sortableEdit").selectable({
            selected: function (event, ui) {
                $(ui.selected).siblings().removeClass("ui-selected");
            }
        });

        $("#sortableEdit li").dblclick(function () {
            editProp(this, 'showPage');
        });
    });

    function editProp(objLi, pageType) {
        action = "edit";
        curPropObj = $(objLi).find('textarea')[0];
        curPageType = pageType;
        curLiObj = objLi;

        var btnId = $(objLi).attr('btnId');
        openWin('module_page_btn_prop.jsp?btnId=' + btnId + '&moduleCode=<%=moduleCode%>', 800, 400);
    }

    function setBtnProp(prop) {
        var strProp = prop;
        if (action == 'create') {
            prop = $.parseJSON(prop);
            if (curPageType == 'showPage') {
                var orders = $('#sortableShow').children().length;
                var html = '<li class="ui-state-default" title="按下图标可拖动，双击图标可编辑属性" btnId="' + prop.id + '" orders="' + orders + '" title="' + prop.title + '">';
                html += '<img class="btn-icon handle" src="../images/setup.png"/>';
                html += '<span style="margin-top:5px">' + prop.name + '</span>';
                html += '<textarea id="' + prop.id + 'Prop" style="display:none">' + strProp + '</textarea>';
                html += '</li>';
                $('#sortableShow').append(html);

                var objLi = $('#sortableShow').find("li[btnId='" + prop.id + "']")[0];
                $(objLi).find('.btn-icon').dblclick(function() {
                    editProp(objLi, curPageType);
                });
                if (!prop.enabled) {
                    $(objLi).addClass('ui-state-disabled')
                }
                else {
                    $(objLi).removeClass('ui-state-disabled')
                }
            }
            else {
                var orders = $('#sortableEdit').children().length;
                var html = '<li class="ui-state-default" title="按下图标可拖动，双击图标可编辑属性" btnId="' + prop.id + '" orders="' + orders + '" title="' + prop.title + '">';
                html += '<img class="btn-icon handle" src="../images/setup.png"/>';
                html += '<span style="margin-top:5px">' + prop.name + '</span>';
                html += '<textarea id="' + prop.id + 'Prop" style="display:none">' + strProp + '</textarea>';
                html += '</li>';
                $('#sortableEdit').append(html);

                var objLi = $('#sortableEdit').find("li[btnId='" + prop.id + "']")[0];
                $(objLi).find('.btn-icon').dblclick(function() {
                    editProp(objLi, curPageType);
                });
                if (!prop.enabled) {
                    $(objLi).addClass('ui-state-disabled')
                }
                else {
                    $(objLi).removeClass('ui-state-disabled')
                }
            }
        }
        else {
            curPropObj.value = prop;

            prop = $.parseJSON(prop);
            $(curLiObj).find('span').html(prop.name);
            if (!prop.enabled) {
                $(curLiObj).addClass('ui-state-disabled')
            }
            else {
                $(curLiObj).removeClass('ui-state-disabled')
            }
        }
    }

    function getBtnProp(prop) {
        if (action == 'create') {
            return '';
        }
        else {
            return curPropObj.value;
        }
    }

    function makeBtnProps(sortId) {
        var jsonAry = [];
        $('#' + sortId).children().each(function() {
            var json = {};
            var $textarea = $(this).find('textarea');
            if ($textarea.val() != '') {
                json = $.parseJSON($textarea.val());
            }
            else {
                json.id = $(this).attr('btnId');
            }
            jsonAry.push(json);
        })
        console.log(JSON.stringify(jsonAry));
        return JSON.stringify(jsonAry);
    }
</script>
</body>
</html>