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

    String skinPath = SkinMgr.getSkinPath(request);

    String op = StrUtil.getNullString(request.getParameter("op"));

    String code = ParamUtil.get(request, "code");
    if (!code.equals("")) {
        op = "add";
    }

    if (op.equals("edit")) {
        long groupId = ParamUtil.getLong(request, "groupId");
        String groupName = ParamUtil.get(request, "groupName");
        JSONObject json = new JSONObject();
        if (groupName.equals("")) {
            json.put("ret", "0");
            json.put("msg", "请输入菜单组名称");
            out.print(json);
            return;
        }

        int orders = ParamUtil.getInt(request, "orders");

        SlideMenuDb smd = new SlideMenuDb();
        smd = (SlideMenuDb) smd.getQObjectDb(new Long(groupId));
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
        SlideMenuDb smd = new SlideMenuDb();
        for (int i = 0; i < ids.length; i++) {
            smd = (SlideMenuDb) smd.getQObjectDb(new Long(StrUtil.toLong(ids[i])));
            smd.set("orders", new Integer(i + 1));
            smd.save();
        }
        json.put("ret", "1");
        json.put("msg", "操作成功！");
        out.print(json);
        return;
    } else if (op.equals("del")) {
        JSONObject json = new JSONObject();

        long menuId = ParamUtil.getLong(request, "menuId");
        SlideMenuDb smd = new SlideMenuDb();
        smd = (SlideMenuDb) smd.getQObjectDb(new Long(menuId));
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
    }
    long groupId = ParamUtil.getLong(request, "groupId");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>滑动菜单组-子项</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_slidemenu.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/tabpanel/Toolbar.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>

    <style>
        #sortable {
            list-style-type: none;
            margin: 0;
            padding: 0;
        }

        #sortable li {
            margin: 3px 3px 3px 0;
            padding: 1px;
            float: left;
            width: 100px;
            height: 100px;
            font-size: 10pt;
            text-align: center;
            color: black;
            font-weight: normal;
        }

        #sortable .ui-selecting {
            background: #FECA40;
        }

        #sortable .ui-selected {
            background: #F39814;
            color: black;
            font-weight: normal;
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
                                ids = $(this).attr("menuId");
                            else
                                ids += "," + $(this).attr("menuId");
                        });

                        $.ajax({
                            type: "post",
                            url: "slide_menu_main.jsp",
                            data: {
                                op: "sort",
                                groupId: "<%=groupId%>",
                                ids: ids
                            },
                            dataType: "html",
                            beforeSend: function (XMLHttpRequest) {
                                $('#sortable').showLoading();
                            },
                            success: function (data, status) {
                                data = $.parseJSON(data);
                                if (data.ret == "0") {
                                    jAlert(data.msg, "提示");
                                } else {
                                    // jAlert_Redirect(data.msg, "提示", "slide_menu_main.jsp");
                                }
                            },
                            complete: function (XMLHttpRequest, status) {
                                $('#sortable').hideLoading();
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


        });
    </script>

</head>
<body>
<%
    if (op.equals("add")) {
        SlideMenuDb smd = new SlideMenuDb();
        if (smd.isExist(groupId, code)) {
            out.print(StrUtil.jAlert_Back("菜单项已存在！", "提示"));
            return;
        }
        boolean re = smd.create(new JdbcTemplate(), new Object[]{code, new Integer(smd.getNextOrders(groupId)), new Long(groupId)});
        if (re) {
            // out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "slide_menu_main.jsp?groupId=" + groupId));
            response.sendRedirect("slide_menu_main.jsp?groupId=" + groupId);
            return;
        } else {
            out.print(StrUtil.jAlert_Back("操作失败！", "提示"));
            return;
        }
    }
%>
<div id="toolbar" style="height:25px; clear:both"></div>

<div style="margin:10px">
    <ul id="sortable">
        <%
            com.redmoon.oa.ui.menu.Leaf lf = new com.redmoon.oa.ui.menu.Leaf();
            SlideMenuDb smd = new SlideMenuDb();
            String sql = "select id from " + smd.getTable().getName() + " where group_id=? order by orders";
            Iterator ir = smd.list(sql, new Object[]{new Long(groupId)}).iterator();
            while (ir.hasNext()) {
                smd = (SlideMenuDb) ir.next();
                lf = lf.getLeaf(smd.getString("code"));
                if (lf == null)
                    lf = new com.redmoon.oa.ui.menu.Leaf();
        %>
        <li class="ui-state-default" menuId="<%=smd.getLong("id")%>" orders="<%=smd.getInt("orders")%>" title="<%=lf.getName()%>">
            <img src="../images/bigicons/<%=lf.getBigIcon()%>"/>
            <div style="margin-top:5px"><%=lf.getName()%>
            </div>
        </li>
        <%
            }
        %>
    </ul>
</div>

<div id="dlg" style="display:none">
    <form id="form1">
        组名称&nbsp;<input id="groupName" name="groupName"/>
        <input id="groupId" name="groupId" type="hidden"/>
        <input id="orders" name="orders" type="hidden"/>
    </form>
</div>

</body>
<script>
    var groupNameCtl = new LiveValidation('groupName');
    groupNameCtl.add(Validate.Presence, {failureMessage: '请填写名称！'});
    groupNameCtl.add(Validate.Length, {maximum: 6});

    var curIndex = -1;
    var toolbar;

    toolbar = new Toolbar({
        renderTo: 'toolbar',
        //border: 'top',
        items: [
            {
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
                                url: "slide_menu_main.jsp",
                                data: {
                                    op: "del",
                                    menuId: $("#sortable").children().eq(curIndex).attr("menuId"),
                                    groupId: <%=groupId%>
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
                                        jAlert_Redirect(data.msg, "提示", "slide_menu_main.jsp?groupId=<%=groupId%>");
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
            }
        ]
    });

    toolbar.render();

    $("#sortable li").dblclick(function () {
        // addTab($(this).attr("groupName"), "admin/slide_menu_main.jsp?id=" + $(this).attr("groupid"));
    });

</script>
</html>