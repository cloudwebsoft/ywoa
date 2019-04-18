<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.ui.menu.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="org.json.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String userName = ParamUtil.get(request, "userName");

    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    if (userName.equals(""))
        userName = privilege.getUser(request);

    if (userName.equals(UserDb.SYSTEM)) {
        if (!privilege.isUserPrivValid(request, "admin")) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String op = StrUtil.getNullString(request.getParameter("op"));
    if (op.equals("add")) {
        String groupName = ParamUtil.get(request, "groupName");
        JSONObject json = new JSONObject();
        if (groupName.equals("")) {
            json.put("ret", "0");
            json.put("msg", "请输入菜单组名称");
            out.print(json);
            return;
        }

        SlideMenuGroupDb smgd = new SlideMenuGroupDb();
        boolean re = smgd.create(new JdbcTemplate(), new Object[]{userName, groupName, new Integer(smgd.getNextOrders(userName))});
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            out.print(json);
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
            out.print(json);
        }
        return;
    } else if (op.equals("edit")) {
        String groupName = ParamUtil.get(request, "groupName");
        JSONObject json = new JSONObject();
        if (groupName.equals("")) {
            json.put("ret", "0");
            json.put("msg", "请输入菜单组名称");
            out.print(json);
            return;
        }

        long groupId = ParamUtil.getLong(request, "groupId", -1);
        if (groupId == -1) {
            json.put("ret", "0");
            json.put("msg", "标识不能为空！");
            out.print(json);
            return;
        }

        int orders = ParamUtil.getInt(request, "orders");

        SlideMenuGroupDb smd = new SlideMenuGroupDb();
        smd = (SlideMenuGroupDb) smd.getQObjectDb(new Long(groupId));
        boolean re = smd.save(new JdbcTemplate(), new Object[]{groupName, new Integer(orders), new Long(groupId)});
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            out.print(json);
            return;
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
            out.print(json);
            return;
        }
    } else if (op.equals("sort")) {
        JSONObject json = new JSONObject();
        String strIds = ParamUtil.get(request, "ids");
        if (strIds.equals("")) {
            json.put("ret", "0");
            json.put("msg", "标识不能为空！");
            out.print(json);
            return;
        }

        String[] ids = StrUtil.split(strIds, ",");
        SlideMenuGroupDb smd = new SlideMenuGroupDb();
        for (int i = 0; i < ids.length; i++) {
            smd = (SlideMenuGroupDb) smd.getQObjectDb(new Long(StrUtil.toLong(ids[i])));
            smd.set("orders", new Integer(i + 1));
            smd.save();
        }
        json.put("ret", "1");
        json.put("msg", "操作成功！");
        out.print(json);
        return;
    } else if (op.equals("del")) {
        JSONObject json = new JSONObject();

        long groupId = ParamUtil.getLong(request, "groupId", -1);
        if (groupId == -1) {
            json.put("ret", "0");
            json.put("msg", "标识不能为空！");
            out.print(json);
            return;
        }

        SlideMenuGroupDb smd = new SlideMenuGroupDb();
        smd = (SlideMenuGroupDb) smd.getQObjectDb(new Long(groupId));
        boolean re = smd.del();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            out.print(json);
            return;
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
            out.print(json);
            return;
        }
    } else if (op.equals("uploadWallpaper")) {
        JSONObject json = new JSONObject();

        WallpaperMgr wm = new WallpaperMgr();
        boolean re = false;
        try {
            re = wm.create(application, request);
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            out.print(json);
            return;
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            out.print(json);
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
            out.print(json);
        }
        return;
    } else if (op.equals("setWallpaper")) {
        JSONObject json = new JSONObject();
        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(userName);
        String fileName = ParamUtil.get(request, "fileName");
        usd.setWallpaper(fileName);
        boolean re = usd.save();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            out.print(json);
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
            out.print(json);
        }
        return;
    } else if (op.equals("restoreSlideMenu")) {
        SlideMenuGroupDb smgd = new SlideMenuGroupDb();
        smgd.init(userName);
        out.print(StrUtil.Alert_Redirect("操作成功!", "slide_menu_group.jsp"));
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>滑动菜单组</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_slidemenu.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js" type="text/javascript"></script>
    <script src="../js/jquery.form.js"></script>
    <script src="../js/tabpanel/Toolbar.js" type="text/javascript"></script>
    <script src="../js/jquery-ui/jquery-ui.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
    <%
        String wallpaperPath = UserSetupMgr.getWallpaperPath(userName);
    %>
    <style>
        html, body {
            height: 100%;
        }

        html, body {
            background: url(<%=request.getContextPath() + "/" + wallpaperPath%>);
            filter: "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod='scale')";
            -moz-background-size: 100% 100%;
            background-size: 100% 100%;
        }

        #sortable {
            list-style-type: none;
            margin: 20px 0px 0px 0px;
            padding: 0;
        }

        #sortable li {
            margin: 3px 3px 3px 0;
            padding: 1px;
            float: left;
            width: 130px;
            height: 50px;
            font-size: 10pt;
            text-align: center;
            line-height: 1;
        }

        #sortable .ui-selecting {
            background: #FECA40;
        }

    </style>
    <script>
        var curIndex = -1;

        $(function () {
            // $( "#sortable" ).selectable();
            // $( "#sortable" ).sortable();

            $("#sortable")
                .sortable({
                    handle: ".handle",
                    stop: function () {
                        var ids = "";
                        $("#sortable li").each(function () {
                            if (ids == "")
                                ids = $(this).attr("groupId");
                            else
                                ids += "," + $(this).attr("groupId");
                        });

                        $.ajax({
                            type: "post",
                            url: "slide_menu_group.jsp",
                            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                            data: {
                                op: "sort",
                                ids: ids
                            },
                            dataType: "html",
                            beforeSend: function (XMLHttpRequest) {
                                $('body').showLoading();
                            },
                            success: function (data, status) {
                                data = $.parseJSON(data);
                                if (data.ret == "0") {
                                    jAlert(data.msg, "提示");
                                } else {
                                    // jAlert_Redirect(data.msg, "提示", "slide_menu_group.jsp");
                                }
                            },
                            complete: function (XMLHttpRequest, status) {
                                $('body').hideLoading();
                            },
                            error: function (XMLHttpRequest, textStatus) {
                                // 请求出错处理
                                alert(XMLHttpRequest.responseText);
                            }
                        });

                    }
                })
                .selectable({
                    stop: function () {
                        $(".ui-selected", this).each(function () {
                            curIndex = $("#sortable li").index(this);
                        });
                    }
                })
                .find("li")
                .addClass("ui-corner-all")
                .prepend("<div class='handle'><span class='ui-icon ui-icon-carat-2-n-s'></span></div>");

            // 实现单选
            $("#sortable").selectable({
                selected: function (event, ui) {
                    $(ui.selected).siblings().removeClass("ui-selected");
                }

            });

            var options = {
                //target:        '#output2',   // target element(s) to be updated with server response
                //beforeSubmit:  automaticOnmodule_codeSubmit,  // pre-submit callback
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
            $('#formWallpaper').submit(function () {
                $(this).ajaxSubmit(options);
                return false;
            });
        });

        function showResponse(responseText, statusText, xhr, $form) {
            var data = $.parseJSON($.trim(responseText));
            jAlert_Redirect(data.msg, "提示", "slide_menu_group.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
        }
    </script>

</head>
<body>

<div id="toolbar" style="height:25px; clear:both"></div>

<div id="container" style="margin:10px">
    <ul id="sortable">
        <%
            SlideMenuGroupDb smgd = new SlideMenuGroupDb();
            String sql = "select id from " + smgd.getTable().getName() + " where user_name=? order by orders";
            Iterator ir = smgd.list(sql, new Object[]{userName}).iterator();
            while (ir.hasNext()) {
                smgd = (SlideMenuGroupDb) ir.next();
        %>
        <li class="portalLiBg" title="双击编辑菜单组" groupId="<%=smgd.getLong("id")%>" groupName="<%=smgd.getString("name")%>" orders="<%=smgd.getInt("orders")%>"><%=smgd.getString("name")%>
        </li>
        <%
            }
        %>
    </ul>
</div>

<div id="dlg" style="display:none">
    <form id="form1">
        组名称&nbsp;<input id="groupName" name="groupName" onKeyDown="onGroupNamePresskey()"/>
        <input id="groupId" name="groupId" type="hidden"/>
        <input id="orders" name="orders" type="hidden"/>
    </form>
</div>

<div id="dlgWallpaper" style="display:none">
    <form id="formWallpaper" method="post" action="slide_menu_wallpaper_do.jsp" enctype="multipart/form-data">
        壁纸&nbsp;<input id="wallpaperFile" name="wallpaperFile" type="file"/>&nbsp;
        <!--<input class="btn" type="submit" value="上传" />-->
        <input name="userName" value="<%=userName%>" type="hidden"/>
    </form>
</div>

</body>
<script>
    function onGroupNamePresskey() {
        if (window.event.keyCode == 13) {
            window.event.keyCode = 9;
        }
    }

    var groupNameCtl = new LiveValidation('groupName');
    groupNameCtl.add(Validate.Presence, {failureMessage: '请填写名称！'});
    groupNameCtl.add(Validate.Length, {maximum: 6});

    var toolbar;

    toolbar = new Toolbar({
        renderTo: 'toolbar',
        //border: 'top',
        items: [
            {
                type: 'button',
                text: '添加',
                title: '添加',
                bodyStyle: 'add',
                useable: 'T',
                handler: function () {
                    $("#dlg").dialog({
                        title: "请输入组名称",
                        modal: true,
                        bgiframe: true,
                        width: 300,
                        height: 120,
                        // bgiframe:true,
                        buttons: {
                            "取消": function () {
                                $(this).dialog("close");
                            },
                            "确定": function () {
                                if (!LiveValidation.massValidate(groupNameCtl.formObj.fields))
                                    return false;
                                $.ajax({
                                    type: "post",
                                    url: "slide_menu_group.jsp",
                                    data: {
                                        op: "add",
                                        userName: "<%=userName%>",
                                        groupName: o("groupName").value
                                    },
                                    dataType: "html",
                                    beforeSend: function (XMLHttpRequest) {
                                        $('#dlg').showLoading();
                                    },
                                    success: function (data, status) {
                                        data = $.parseJSON(data);
                                        if (data.ret == "0") {
                                            jAlert(data.msg, "提示");
                                        } else {
                                            jAlert_Redirect(data.msg, "提示", "slide_menu_group.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
                                        }
                                    },
                                    complete: function (XMLHttpRequest, status) {
                                        $('#dlg').hideLoading();
                                    },
                                    error: function (XMLHttpRequest, textStatus) {
                                        // 请求出错处理
                                        alert(XMLHttpRequest.responseText);
                                    }
                                });

                                $(this).dialog("close");
                            }
                        },
                        closeOnEscape: true,
                        draggable: true,
                        resizable: true
                    });

                }
            }
            , '-', {
                type: 'button',
                text: '修改',
                title: '修改',
                bodyStyle: 'edit',
                useable: 'T',
                handler: function () {
                    if (curIndex == -1) {
                        jAlert("请选择菜单组！", "提示");
                        return false;
                    }
                    //
                    $("#groupName").val($("#sortable").children().eq(curIndex).attr("groupName"));
                    $("#groupId").val($("#sortable").children().eq(curIndex).attr("groupId"));
                    $("#orders").val($("#sortable").children().eq(curIndex).attr("orders"));
                    $("#dlg").dialog({
                        title: "请输入组名称",
                        modal: true,
                        width: 300,
                        height: 120,
                        // bgiframe:true,
                        buttons: {
                            "取消": function () {
                                $(this).dialog("close");
                            },
                            "确定": function () {
                                if (!LiveValidation.massValidate(groupNameCtl.formObj.fields))
                                    return false;
                                if (o("groupName").value == "") {
                                    jAlert("请输入菜单组名称", "提示");
                                    return false;
                                }

                                $.ajax({
                                    type: "post",
                                    url: "slide_menu_group.jsp",
                                    data: {
                                        op: "edit",
                                        groupId: o("groupId").value,
                                        groupName: o("groupName").value,
                                        orders: o("orders").value
                                    },
                                    dataType: "html",
                                    beforeSend: function (XMLHttpRequest) {
                                        $('#dlg').showLoading();
                                    },
                                    success: function (data, status) {
                                        data = $.parseJSON(data);
                                        if (data.ret == "0") {
                                            jAlert(data.msg, "提示");
                                        } else {
                                            jAlert_Redirect(data.msg, "提示", "slide_menu_group.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
                                        }
                                    },
                                    complete: function (XMLHttpRequest, status) {
                                        $('#dlg').hideLoading();
                                    },
                                    error: function (XMLHttpRequest, textStatus) {
                                        // 请求出错处理
                                        alert(XMLHttpRequest.responseText);
                                    }
                                });

                                $(this).dialog("close");
                            }
                        },
                        closeOnEscape: true,
                        draggable: true,
                        resizable: true
                    });
                }
            }, '-', {
                type: 'button',
                text: '删除',
                title: '删除',
                bodyStyle: 'del',
                useable: 'T',
                handler: function () {
                    if (curIndex == -1) {
                        jAlert("请选择菜单组！", "提示");
                        return false;
                    }
                    // if (!confirm("您确定要删除么？"))
                    // 	return false;

                    jConfirm('您确定要删除么？', '提示', function (r) {
                        if (r) {
                            $.ajax({
                                type: "post",
                                url: "slide_menu_group.jsp",
                                data: {
                                    op: "del",
                                    groupId: $("#sortable").children().eq(curIndex).attr("groupId")
                                },
                                dataType: "html",
                                beforeSend: function (XMLHttpRequest) {
                                    $('#dlg').showLoading();
                                },
                                success: function (data, status) {
                                    data = $.parseJSON(data);
                                    if (data.ret == "0") {
                                        jAlert(data.msg, "提示");
                                    } else {
                                        jAlert_Redirect(data.msg, "提示", "slide_menu_group.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
                                    }
                                },
                                complete: function (XMLHttpRequest, status) {
                                    $('#dlg').hideLoading();
                                },
                                error: function (XMLHttpRequest, textStatus) {
                                    // 请求出错处理
                                    alert(XMLHttpRequest.responseText);
                                }
                            });
                        }
                    });

                }
            }, '-', {
                type: 'button',
                text: '上传壁纸',
                title: '上传壁纸',
                bodyStyle: 'wallpaper',
                useable: 'T',
                handler: function () {
                    $("#dlgWallpaper").dialog({
                        title: "上传壁纸",
                        modal: true,
                        width: 350,
                        height: 120,
                        // bgiframe:true,
                        buttons: {
                            "取消": function () {
                                $(this).dialog("close");
                            },
                            "确定": function () {
                                $("#formWallpaper").submit();

                                $(this).dialog("close");
                            }
                        },
                        closeOnEscape: true,
                        draggable: true,
                        resizable: true
                    });

                }
            }, '-', {
                type: 'button',
                text: '选择壁纸',
                title: '选择壁纸',
                bodyStyle: 'wallpaper_sel',
                useable: 'T',
                handler: function () {
                    openWin('wallpaper_sel.jsp', 800, 600);
                }
            }
            <%if (!userName.equals(UserDb.SYSTEM)) {%>
            , '-', {
                type: 'button',
                text: '恢复默认',
                title: '恢复默认',
                bodyStyle: 'recover',
                useable: 'T',
                handler: function () {
                    if (confirm('您确定要恢复默认滑动菜单组么？')) {
                        window.location.href = 'slide_menu_group.jsp?op=restoreSlideMenu';
                    }
                }
            }
            <%}%>
        ]
    });

    toolbar.render();

    $("#sortable li").dblclick(function () {
        addTab($(this).attr("groupName"), "<%=request.getContextPath()%>/admin/slide_menu_frame.jsp?id=" + $(this).attr("groupid"));
    });

    function selWallpaper(fileName) {
        $.ajax({
            type: "post",
            url: "slide_menu_group.jsp",
            data: {
                op: "setWallpaper",
                fileName: fileName,
                userName: "<%=userName%>"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#container').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    jAlert(data.msg, "提示");
                } else {
                    jAlert_Redirect(data.msg, "提示", "slide_menu_group.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#container').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }
</script>
</html>