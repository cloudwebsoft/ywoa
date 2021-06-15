<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="org.json.*" %>
<%@ page import="cn.js.fan.module.cms.site.*" %>
<%@ page import="com.redmoon.kit.util.FileUpload" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    long portalId = ParamUtil.getLong(request, "portalId");
    String op = StrUtil.getNullStr((String)request.getAttribute("op"));
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>用户桌面设置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <style>
        #sortable {
            list-style-type: none;
            margin: 0px auto;
            padding: 0;
            height: 160px;
        }

        #sortable li {
            margin: 3px 3px 3px 0;
            padding: 1px;
            float: left;
            width: 120px;
            height: 120px;
            font-size: 10pt;
            text-align: center;
            color: black;
            font-weight: normal;
        }

        .label {
            border: 1px solid #ccc;
            margin: 5px 5px;
            padding: 5px 5px;
            border-radius:5px;
            display: block;
            float: left;
        }
        .close {
            margin-left: 5px;
            cursor: pointer;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script src="../inc/map.js"></script>

    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
    <script type="text/javascript" src="../js/jquery.flexbox.js"></script>

    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <script>
        $(function () {
            $(".desktop-icon").select2({
                width: 200,
                templateResult: formatState,
                templateSelection: formatState
            });
        });

        function formatState(state) {
            if (!state.id) {
                return state.text;
            }
            var $state = $(
                '<span><i class="fa ' + state.id + '"></i>&nbsp;&nbsp;' + state.text + '</span>'
            );
            return $state;
        };
    </script>
</head>
<body>
<%
    if (op.equals("add")) {
        int ret = StrUtil.toInt((String)request.getAttribute("ret"), 1);
        String msg = (String)request.getAttribute("msg");
        if (ret==0) {
            out.print(StrUtil.jAlert_Back(msg, "提示"));
        }
        else {
            out.print(StrUtil.jAlert_Redirect(msg, "提示", "desktop_setup.jsp?portalId=" + portalId));
        }
        return;
    }

    PortalDb ptd = new PortalDb();
    ptd = (PortalDb) ptd.getQObjectDb(new Long(portalId));
    UserDesktopSetupDb udsd = new UserDesktopSetupDb();
    String sql = udsd.getSqlByPortalId(portalId);
    Vector v = udsd.list(sql);
    Iterator ir = v.iterator();
    DesktopMgr dm = new DesktopMgr();

    String iconOpts = "";
    ArrayList<String[]> fontAry = CSSUtil.getFontBefore();
    int fontAryLen = fontAry.size();
    for (int m = 0; m < fontAryLen; m++) {
        String[] ary = fontAry.get(m);
        iconOpts += "<option value='" + ary[0] + "'>";
        iconOpts += "<i class='fa " + ary[0] + "'></i>";
        iconOpts += ary[0];
        iconOpts += "</option>";
    }
%>
<div class="spacerH"></div>
<%
    com.redmoon.oa.ui.menu.Leaf lfMenu = new com.redmoon.oa.ui.menu.Leaf();
    PortalMenuDb pmd = new PortalMenuDb();
    sql = pmd.getTable().getSql("listForPortal");
    Vector vt = pmd.list(sql, new Object[]{new Long(portalId)});
    int size = vt.size();
    if (size>0) {
%>
<div style="width:100%; border-bottom: 1px #ccc solid; margin-bottom: 10px">
<ul id="sortable" style="margin: 0px auto">
    <%
        Iterator irMenu = vt.iterator();
        while (irMenu.hasNext()) {
            pmd = (PortalMenuDb) irMenu.next();
            lfMenu = lfMenu.getLeaf(pmd.getString("code"));
            if (lfMenu == null) {
                lfMenu = new com.redmoon.oa.ui.menu.Leaf();
            }
    %>
    <li class="ui-state-default" style="cursor: pointer" menuId="<%=pmd.getLong("id")%>" orders="<%=pmd.getInt("orders")%>" title="<%=lfMenu.getName()%>" onclick="addTab('<%=lfMenu.getName()%>', '<%=request.getContextPath() + "/" + lfMenu.getLink(request)%>')">
        <img src="../images/bigicons/<%=lfMenu.getBigIcon()%>"/>
        <div style="margin-top:5px"><%=lfMenu.getName()%></div>
    </li>
    <%
        }
    %>
</ul>
    <script>
        $('#sortable').width('<%=size*125%>px');
    </script>
</div>
<%
    }
%>
<script>
    var tabId = getActiveTabId();
</script>
<table width="100%" class="percent98">
    <tr>
        <td width="100%" align="center">
            <input class="btn" type="button" value="门户菜单" onclick="addTab('门户菜单', '<%=request.getContextPath()%>/admin/portal_menu_frame.jsp?tabId=' + tabId + '&portalId=<%=portalId%>')"/>
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input class="btn" type="button" value="进入门户" onclick="addTab('门户', 'desktop.jsp?mode=force&portalId=<%=portalId%>')"/>
        </td>
    </tr>
</table>
<table class="percent98" align="center">
    <tr>
        <td width="20%" class="tabStyle_1_title">模块</td>
        <td width="29%" class="tabStyle_1_title">属性</td>
        <td class="tabStyle_1_title">每行字数</td>
        <td width="7%" class="tabStyle_1_title">位置</td>
        <td width="15%" class="tabStyle_1_title">图标</td>
        <%if (false && ptd.isSystem()) {%>
        <td width="6%" class="tabStyle_1_title">允许删除</td>
        <%}%>
        <td width="17%" class="tabStyle_1_title">操作</td>
    </tr>
</table>
    <%
        int k = 0;
        while (ir.hasNext()) {
            udsd = (UserDesktopSetupDb) ir.next();
            DesktopUnit du = dm.getDesktopUnit(udsd.getModuleCode());
            if (du == null) {
                udsd.del();
                continue;
            }
            k++;
    %>
    <form name="form<%=k%>" id="form<%=k%>" action="editDesktopSetupItem.do" method="post">
        <table class="percent98" align="center" style="border-bottom: 1px dotted #ccc">
        <tr id="tr<%=k%>">
            <td width="20%"><input name="title" value="<%=udsd.getTitle()%>" style="width:90%"/>
                <input name="id" value="<%=udsd.getId()%>" type="hidden"/>
                <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
            <td width="29%">
                <%
                    if (du.getType().equals(DesktopUnit.TYPE_LIST)) {
                %>
                行数&nbsp;&nbsp;<input name=count value="<%=udsd.getCount()%>" size="4">
                <%
                } else {
                %>
                字数或高度&nbsp;&nbsp;
                <input name=count value="<%=udsd.getCount()%>" size="4">
                <%
                    }
                    if ("visual".equals(udsd.getModuleCode())) {
                        String metaData = udsd.getMetaData();
                        JSONObject json = new JSONObject(metaData);
                        String formCode = udsd.getModuleItem();
                        FormDb fd = new FormDb();
                        fd = fd.getFormDb(formCode);
                        if (fd.isLoaded()) {
                            String fieldTitle = json.getString("fieldTitle");
                            String fieldDate = json.getString("fieldDate");
                            String title = "", date = "";
                            FormField ffTitle = fd.getFormField(fieldTitle);
                            if (ffTitle != null) {
                                title = ffTitle.getTitle();
                            }
                            FormField ffDate = fd.getFormField(fieldDate);
                            if (ffDate != null) {
                                date = ffDate.getTitle();
                            }
                %>
                <%=title%>，<%=date%>
                <%
                    }
                } else if ("flow".equals(udsd.getModuleCode())) {
                    String metaData = udsd.getMetaData();
                    if (!"".equals(metaData)) {
                        com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
                        lf = lf.getLeaf(metaData);
                        if (lf != null) {
                            out.print(lf.getName());
                        } else {
                            out.println("流程:" + metaData + " 不存在!");
                        }
                    }
                } else if ("fileark".equals(udsd.getModuleCode())) {
                %>
                <select id="moduleItemFikeark<%=k%>">
                    <option value="not" selected="selected">请选择目录</option>
                    <%
                        Directory dir = new Directory();
                        Leaf lf = dir.getLeaf("root");
                        DirectoryView dv = new DirectoryView(request, lf);
                        dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
                    %>
                </select>
                <script>
                    var mapSel<%=k%> = new Map();
                </script>
                <div id="filearkSelected<%=k%>">
                    <%
                        String[] ary = StrUtil.split(udsd.getModuleItem(), ",");
                        if (ary!=null) {
                            Leaf lfItem = new Leaf();
                            for (String item : ary) {
                                lfItem = lfItem.getLeaf(item);
                                if (lfItem==null) {
                                    lfItem = new Leaf();
                                    continue;
                                }
                    %>
                    <span class="label"><span><%=lfItem.getName()%></span><input type="hidden" name="moduleItem" value="<%=item%>"/><a class="close">x</a></span>
                    <script>
                        mapSel<%=k%>.put('<%=item%>', '');
                    </script>
                    <%
                            }
                        }
                    %>
                </div>
                <script>
                    var oldVal<%=k%> = $('#moduleItemFikeark<%=k%>').val();
                    $('#moduleItemFikeark<%=k%>').change(function() {
                        if (this.options[this.selectedIndex].value == 'not') {
                            jAlert(this.options[this.selectedIndex].text + ' 不能被选择！', '提示');
                            $(this).val(oldVal);
                            return false;
                        } else {
                            var v = $(this).val();
                            var t = this.options[this.selectedIndex].text;
                            t = t.replace('├『', '');
                            t = t.replace('』', '');
                            t = $.trim(t);
                            t = t.replace('╋ ', '');
                            if (!mapSel<%=k%>.containsKey(v)) {
                                oldVal<%=k%> = v;
                                mapSel<%=k%>.put(v, '');
                                $('#filearkSelected<%=k%>').append('<span class="label"><span>'+t+'</span><input type="hidden" name="moduleItem" value="'+v+'"/><a class="close">x</a></span>');
                            }
                            else {
                                jAlert(t + ' 已被选择', '提示');
                            }
                        }
                    });

                    $("#filearkSelected<%=k%>").on("click",".close",function() {
                        $(this).parent().remove();
                    })
                </script>
                <%
                    }
                    else {
                %>
                <input name="moduleItem" value="<%=udsd.getModuleItem()%>" type="hidden"/>
                <%
                    }
                %>
            </td>
            <td width="6%" align="center">
                <%if (du.getType().equals(DesktopUnit.TYPE_LIST)) {%>
                <input name="wordCount" value="<%=udsd.getWordCount()%>" size="2"/>
                <%}%>
            </td>
            <td width="7%" align="center">
                <select name="td" id="td<%=k%>">
                    <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                    <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                    <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                </select>
                <script>
                    o('td<%=k%>').value = "<%=udsd.getTd()%>";
                </script>
            </td>
            <td width="15%" align="center">
                <select id="icon<%=k%>" name="icon" style="width:150px" class="desktop-icon js-example-templating js-states form-control">
                    <%=iconOpts%>
                </select>
                <script>
                    $(function () {
                        $('#icon<%=k%>').val('<%=udsd.getIcon()%>').trigger("change");
                    });
                </script>
            </td>
            <%if (false && ptd.isSystem()) {%>
            <td width="6%" align="center">
                <input name="canDelete" type="checkbox" value="1" <%=udsd.isCanDelete() ? "checked" : ""%> />
            </td>
            <%}%>
            <td width="17%" align="center">
                <input id="btnEdit<%=k%>" class="btn" type="button" value="修改"/>
                <script>
                    $('#btnEdit<%=k%>').click(function () {
                        $.ajax({
                            type: "post",
                            url: "editDesktopSetupItem.do",
                            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                            data: $('#form<%=k%>').serialize(),
                            dataType: "html",
                            beforeSend: function (XMLHttpRequest) {
                                $("body").showLoading();
                            },
                            success: function (data, status) {
                                data = $.parseJSON(data);
                                jAlert(data.msg, "提示");
                            },
                            complete: function (XMLHttpRequest, status) {
                                $("body").hideLoading();
                            },
                            error: function (XMLHttpRequest, textStatus) {
                                // 请求出错处理
                                alert(XMLHttpRequest.responseText);
                            }
                        });
                    });
                </script>
                <%
                    boolean canDelete = false;
                    if (ptd.isSystem()) {
                        canDelete = true;
                    } else {
                        // 取得对应的系统门户
                        UserDesktopSetupDb udsdSys = new UserDesktopSetupDb();
                        udsdSys = udsdSys.getUserDesktopSetupDb((int) udsd.getSystemId());
                        if (!udsdSys.isLoaded()) {
                            canDelete = true;
                        } else if (udsdSys.isCanDelete()) {
                            canDelete = true;
                        }
                    }
                    if (canDelete) {
                %>
                <input id="btnDel<%=k%>" class="btn" value="删除" type="button"/>
                <script>
                    $('#btnDel<%=k%>').click(function() {
                        jConfirm('您确定要删除么？', '提示', function (r) {
                            if (!r) {
                                return;
                            } else {
                                $.ajax({
                                    type: "post",
                                    url: "delDesktopSetupItem.do",
                                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                                    data: {
                                        id: <%=udsd.getId()%>
                                    },
                                    dataType: "html",
                                    beforeSend: function (XMLHttpRequest) {
                                        $("body").showLoading();
                                    },
                                    success: function (data, status) {
                                        data = $.parseJSON(data);
                                        jAlert(data.msg, "提示");
                                        if (data.ret==1) {
                                            $('#form<%=k%>').remove();
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
                            }
                        })
                    })
                </script>
                <%}%>
<%--                <%if (ptd.isSystem()) {%>
                <input id="btnCopy<%=k%>" class="btn" value="复制" title="复制到所有用户的桌面上" type="button" onclick="copy('<%=udsd.getId()%>')"/>
                <%}%>--%>
            </td>
        </tr>
        </table>
    </form>
    <%}%>
</table>
<table width="100%" class="percent98" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td width="33%" valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">通知</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">标题</td>
                        <td width="85%" align="left"><input name="title" value="通知"/>
                            <input type="hidden" name="moduleCode" value="notice"/>
                            <input type="hidden" name="moduleItem" value=""/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">位置</td>
                        <td align="left"><select name="td" id="tdNotice">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconNotice" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td colspan="2" align="center"><input type="submit" class="btn" value="添加"/>
                        </td>
                    </tr>
                </form>
            </table>
        </td>
        <td width="33%" valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">工作计划</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="工作计划"/>
                            <input type="hidden" name="moduleCode" value="workplan"/>
                            <input type="hidden" name="moduleItem" value=""/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdWorkplan">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconWorkplan" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit44" type="submit" class="btn" value="添加"/>
                        </td>
                    </tr>
                </form>
            </table>
        </td>
        <td width="34%" valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">日程安排</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="日程安排">
                            <input type=hidden name="moduleCode" value="plan">
                            <input type=hidden name="moduleItem" value="">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdPlan">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconPlan" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit3" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form id="flowForm" action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">待办流程</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="待办流程"/>
                            <input type=hidden name="moduleCode" value="flow"/>
                            <input type=hidden name="moduleItem" value=""/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;类型</td>
                        <td align="left">
                            <select id="typeCode" name="typeCode" onchange="onTypeCodeChange(this)">
                                <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
                            </select>
                            <script>
                                $(function () {
                                    $.ajax({
                                        type: "post",
                                        url: "../flow/getTree.do",
                                        data: {

                                        },
                                        dataType: "html",
                                        beforeSend: function (XMLHttpRequest) {
                                        },
                                        success: function (data, status) {
                                            $("#typeCode").empty();

                                            data = '<option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>' + data;

                                            $("#typeCode").append(data);
                                        },
                                        complete: function (XMLHttpRequest, status) {
                                        },
                                        error: function (XMLHttpRequest, textStatus) {
                                            alert(XMLHttpRequest.responseText);
                                        }
                                    });
                                });

                                function onTypeCodeChange(obj) {
                                    if (obj.options[obj.selectedIndex].value == 'not') {
                                        jAlert(obj.options[obj.selectedIndex].text + ' <lt:Label res="res.flow.Flow" key="notBeSelect"/>', '提示');
                                    } else {
                                        flowForm.title.value = $.trim(obj.options[obj.selectedIndex].text);
                                    }
                                }
                            </script>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdFlowDoing">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconFlowDoing" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit4" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
        <td>
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">我发起的流程</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="我发起的流程">
                            <input type=hidden name="moduleCode" value="flowMine">
                            <input type=hidden name="moduleItem" value="">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdFlowStart">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconFlowStart" name="icon" style="width:150px" class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit4" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
        <td>
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">我参与的流程</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="我参与的流程">
                            <input type=hidden name="moduleCode" value="flowAttended">
                            <input type=hidden name="moduleItem" value="">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdFlowAttend">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconFlowAttend" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit4" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">我关注的流程</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="我关注的流程">
                            <input type=hidden name="moduleCode" value="flowFavorite">
                            <input type=hidden name="moduleItem" value="">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdFlowFav">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconFlowFav" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit4" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
        <td valign="top">
            <form id="formFileark" action="addDesktopSetupItem.do" method="post">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">文件柜目录</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left">
                            <input id="titleFileark" name="title" value="文件柜目录"/>
                            <input type="hidden" name="moduleCode" value="fileark"/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;目录</td>
                        <td align="left">
                            <select id="moduleItemFikeark">
                                <option value="not" selected="selected">请选择目录</option>
                                <%
                                    Directory dir = new Directory();
                                    Leaf lf = dir.getLeaf("root");
                                    DirectoryView dv = new DirectoryView(request, lf);
                                    dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
                                %>
                            </select>

                            <div id="filearkSelected" style="margin: 10px 0px">

                            </div>
                            <script>
                                var mapSel = new Map();
                                var oldVal = $('#moduleItemFikeark').val();
                                $('#moduleItemFikeark').change(function() {
                                    if (this.options[this.selectedIndex].value == 'not') {
                                        jAlert(this.options[this.selectedIndex].text + ' 不能被选择！', '提示');
                                        $(this).val(oldVal);
                                        return false;
                                    } else {
                                        var v = $(this).val();
                                        var t = this.options[this.selectedIndex].text;
                                        t = t.replace('├『', '');
                                        t = t.replace('』', '');
                                        t = $.trim(t);
                                        t = t.replace('╋ ', '');
                                        if (!mapSel.containsKey(v)) {
                                            // 如果还没被选择的，则置标题
                                            if (mapSel.size()==0) {
                                                $('#titleFileark').val(t);
                                            }
                                            oldVal = v;
                                            mapSel.put(v, '');
                                            $('#filearkSelected').append('<span class="label"><span>'+t+'</span><input type="hidden" name="moduleItem" value="'+v+'"/><a class="close">x</a></span>');
                                        }
                                        else {
                                            jAlert(t + ' 已被选择', '提示');
                                        }
                                    }
                                });

                                $("#filearkSelected").on("click",".close",function(){
                                    $(this).parent().remove();
                                })
                            </script>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdFileark">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconFileark" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit2" type="submit" class="btn" value="添加"/></td>
                    </tr>
            </table>
            </form>
        </td>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form name="formDoc" action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">文件柜目录中的最新文件</td>
                    </tr>
                    <tr>
                        <td width="16%" align="left">&nbsp;标题</td>
                        <td width="84%" align="left"><input name="title" value="文章">
                            <input type=hidden name="moduleCode" value="document">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/>
                            <input type="hidden" name="count" value="500"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;编号</td>
                        <td align="left">
                            <select name="moduleItem"
                                    onchange="if(this.options[this.selectedIndex].value=='not'){jAlert(this.options[this.selectedIndex].text+' 不能被选择！','提示');return false;} else {formDoc.title.value=this.options[this.selectedIndex].text}">
                                <option value="not" selected="selected">请选择目录</option>
                                <%
                                    dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
                                %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdFilearkNew">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconFilearkNew" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit22" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
    </tr>
    <tr>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form name="formModule" action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">预置模块</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value=""/>
                            <input type="hidden" name="moduleItem" value=""/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;名称</td>
                        <td align="left">
                            <select name="moduleCode" onchange="if (this.value=='') formModule.title.value=''; else formModule.title.value=this.options[this.selectedIndex].text">
                            <option value="">请选择</option>
                            <%
                                DesktopMgr dtm = new DesktopMgr();
                                ir = dtm.getAllDeskTopUnit().iterator();
                                while (ir.hasNext()) {
                                    DesktopUnit du = (DesktopUnit) ir.next();
                                    if (du.getCode().startsWith("sales.")) {
                                        if (!privilege.isUserPrivValid(request, "sales.user")) {
                                            continue;
                                        }
                                    }
                                    if (!du.getClassName().equals("com.redmoon.oa.ui.desktop.IncludeDesktopUnit"))
                                        continue;
                                    if (du.getCode().equals("flashImage")) {
                                        continue;
                                    }
                            %>
                            <option value="<%=du.getCode()%>"><%=du.getName()%>
                            </option>
                            <%
                                }
                            %>
                        </select></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdPredefine">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconPredefine" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit4222" type="submit" class="btn" value="添加"/>
                        </td>
                    </tr>
                </form>
            </table>
        </td>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">待办事项</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="待办事项"/>
                            <input type="hidden" name="moduleCode" value="todolist"/>
                            <input type="hidden" name="moduleItem" value=""/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdWait">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconWait" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit432" type="submit" class="btn" value="添加"/>
                        </td>
                    </tr>
                </form>
            </table>
        </td>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">我的邮箱</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="我的邮箱">
                            <input type=hidden name="moduleCode" value="mail">
                            <input type=hidden name="moduleItem" value="">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdEmail">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconEmail" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit422" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
    </tr>
    <tr>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form id="formFlashImg" action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">图片轮播</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="图片轮播">
                            <input type="hidden" name="moduleCode" value="flashImage">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/>
                            <input type="hidden" name="count" value="0"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;名称</td>
                        <td align="left">
                            <select name="moduleItem"
                                    onchange="formFlashImg.title.value=this.options[this.selectedIndex].text">
                                <%
                                    SiteFlashImageDb pd = new SiteFlashImageDb();
                                    String sqlFlashImg = "select id from " + pd.getTable().getName() + " where site_code=" + StrUtil.sqlstr(com.redmoon.oa.fileark.Leaf.ROOTCODE) + " order by id desc";
                                    Iterator irFlashImg = pd.list(sqlFlashImg).iterator();
                                    while (irFlashImg.hasNext()) {
                                        pd = (SiteFlashImageDb) irFlashImg.next();
                                %>
                                <option value="<%=pd.getLong("id")%>"><%=pd.getString("name")%>
                                </option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdImage">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconImage" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit422" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">内部邮件</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="内部邮件">
                            <input type=hidden name="moduleCode" value="msg">
                            <input type=hidden name="moduleItem" value="">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdMsg">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconMsg" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit422" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
        <td valign="top">
            <%if (ptd.isSystem()) {%>
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form id="frmQueryScript" action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">自由查询</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="自由查询"/>
                            <input type="hidden" name="moduleCode" value="query.script"/>
                            <input type="hidden" name="moduleItem" value=""/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/>
                            <a href="javascript:;" onClick="selQuery()">选择</a>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdQueryFree">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconQueryFree" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input name="submit" type="submit" class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
            <%}%>
        </td>
    </tr>
    <tr>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98" style="display:none">
                <form id="formForum" action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">论坛新贴</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value="论坛新贴"/>
                            <input type=hidden name="moduleCode" value="forum">
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;版块</td>
                        <td align="left">
                            <select name="moduleItem"
                                    onchange="if(this.options[this.selectedIndex].value=='not') {jAlert('请选择版块！','提示'); this.value='';} else {formForum.title.value=this.options[this.selectedIndex].text;}">
                                <option value="" selected>
                                    论坛新贴
                                </option>
                                <%
                                    com.redmoon.forum.Privilege forumPvg = new com.redmoon.forum.Privilege();
                                    com.redmoon.forum.Directory boards = new com.redmoon.forum.Directory();
                                    com.redmoon.forum.Leaf leaf = boards.getLeaf(com.redmoon.forum.Leaf.CODE_ROOT);
                                    com.redmoon.forum.DirectoryView forumdv = new com.redmoon.forum.DirectoryView(leaf);
                                    forumdv.ShowDirectoryAsOptions(request, forumPvg, out, leaf, leaf.getLayer());
                                %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdForum">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td colspan="2" align="center"><input name="submit43" type=submit class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
    </tr>
    <tr>
        <td valign="top">
            <table width="100%" border="0" align="center" class="tabStyle_1 percent98">
                <form id="formVisual" action="addDesktopSetupItem.do" method="post">
                    <tr>
                        <td colspan="2" align="center" class="tabStyle_1_title">智能模块</td>
                    </tr>
                    <tr>
                        <td width="18%" align="left">&nbsp;标题</td>
                        <td width="82%" align="left"><input name="title" value=""/>
                            <input type="hidden" name="moduleCode" value="visual"/>
                            <input type="hidden" name="portalId" value="<%=portalId%>"/></td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;模块</td>
                        <td align="left">
                            <%
                                ModuleSetupDb msd = new ModuleSetupDb();
                                v = msd.listUsed();
                                ir = v.iterator();
                                String jsonStr = "";
                                while (ir.hasNext()) {
                                    msd = (ModuleSetupDb) ir.next();

                                    if (jsonStr.equals(""))
                                        jsonStr = "{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
                                    else
                                        jsonStr += ",{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";

                                }
                            %>
                            <div id="moduleItemSel"></div>
                            <input id="moduleItem" name="moduleItem" type="hidden"/>
                            <script>
                                var moduleItemSel = $('#moduleItemSel').flexbox({
                                    "results": [<%=jsonStr%>],
                                    "total":<%=v.size()%>
                                }, {
                                    initialValue: '',
                                    watermark: '请选择模块',
                                    paging: false,
                                    maxVisibleRows: 10,
                                    onSelect: function () {
                                        o("moduleItem").value = $("input[name=moduleItemSel]").val();
                                        getFieldOptions(o("moduleItem").value);

                                        formVisual.title.value = $("#moduleItemSel").find(".ffb-sel").eq(0).text();
                                    }
                                });

                                function getFieldOptions(code) {
                                    var str = "op=getOptions&code=" + code;
                                    var myAjax = new cwAjax.Request(
                                        "../visual/module_field_ajax.jsp",
                                        {
                                            method: "post",
                                            parameters: str,
                                            onComplete: doGetFieldOptions,
                                            onError: errFunc
                                        }
                                    );
                                }

                                function doGetFieldOptions(response) {
                                    var rsp = response.responseText.trim();

                                    $("#fieldTitle").empty();
                                    $("#fieldTitle").append(rsp);
                                    $("#fieldDate").empty();
                                    $("#fieldDate").append(rsp);
                                }

                                var errFunc = function (response) {
                                    window.status = response.responseText;
                                }
                            </script>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;标题</td>
                        <td align="left"><select id="fieldTitle" name="fieldTitle">
                        </select>
                            （左侧）
                            <select id="fieldDate" name="fieldDate">
                            </select>
                            （右侧）
                        </td>
                    </tr>
                    <tr>
                        <td align="left">&nbsp;位置</td>
                        <td align="left"><select name="td" id="tdModule">
                            <option value="<%=UserDesktopSetupDb.TD_LEFT%>">左侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_RIGHT%>">右侧</option>
                            <option value="<%=UserDesktopSetupDb.TD_SIDEBAR%>">边栏</option>
                        </select></td>
                    </tr>
                    <%if (false && ptd.isSystem()) {%>
                    <tr>
                        <td align="left">&nbsp;删除</td>
                        <td align="left"><input type="checkbox" name="canDelete" value="1" checked="checked"/>
                            允许
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td align="left">&nbsp;图标</td>
                        <td align="left">
                            <select id="iconModule" name="icon" style="width:150px"
                                    class="desktop-icon js-example-templating js-states form-control">
                                <%=iconOpts%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input type="submit" class="btn" value="添加"/></td>
                    </tr>
                </form>
            </table>
        </td>
    </tr>
</table>

<div id="result"></div>
</body>
<script>
    function selQuery() {
        openWin("../flow/form_query_list_sel.jsp?type=script", 800, 600);
    }

    function doSelQuery(id, title) {
        frmQueryScript.title.value = title;
        frmQueryScript.moduleItem.value = id;
    }

    function showResponse(data) {
        if (data.ret == 1) {
            $("#result").html(data.msg);
        } else {
            $("#result").html(data.msg);
        }
        $("#result").dialog({
            title: "提示", modal: true, buttons: {
                "确定": function () {
                    $(this).dialog("close");
                }
            }, closeOnEscape: true, draggable: true, resizable: true
        });
    }

    $(document).ready(function () {
        //var options = {
        //target:        '#output2',   // target element(s) to be updated with server response 
        //beforeSubmit:  function() {alert('d');},  // pre-submit callback 
        //success:       showResponse,  // post-submit callback 

        // other available options: 
        //url:       url         // override for form's 'action' attribute 
        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
        //dataType:  'json'        // 'xml', 'script', or 'json' (expected server response type) 
        //clearForm: true        // clear all form fields after successful submit 
        //resetForm: true        // reset the form after successful submit 

        // $.ajax options can be used here too, for example: 
        //timeout:   3000 
        //};

        // bind to the form's submit event
        //$('#formDesktopCode').submit(function() {
        //$(this).ajaxSubmit(options); 
        //return false;
        //});
    });

    function selTemplate(id) {
        formDoc.moduleItem.value = id;
    }

    function copy(id) {
        jConfirm("您确定要复制到所有用户的桌面上么？", "提示", function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "copyDesktopSetupItem.do",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        id: id
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $("body").showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        jAlert(data.msg, "提示");
                    },
                    complete: function (XMLHttpRequest, status) {
                        $("body").hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        })
    }
</script>
</html>