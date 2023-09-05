<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.redmoon.oa.dept.DeptMgr" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.dept.DeptView" %>
<%@ page import="com.redmoon.oa.pvg.RoleDb" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.redmoon.oa.pvg.UserGroupDb" %>
<%@ page import="com.redmoon.oa.fileark.DocPriv" %>
<%@ page import="com.cloudweb.oa.service.IGroupService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.entity.Group" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>部门/角色/用户组选择</title>
    <link href="<%=SkinMgr.getSkinPath(request)%>/css.css" rel="stylesheet"/>
    <link href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" rel="stylesheet"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"/>
    <link href="../js/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="../js/jstree/themes/default/style.css" rel="stylesheet"/>
    <link href="../js/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet"/>

    <style>
        html, body {
            height: 100%;
        }
        .sel-item {
            padding: 5px;
        }
        .sel-item-hover {
            background-color: #eee;
        }
        .sel-item img {
            width:20px;
            height:20px;
            margin-right: 5px;
            vertical-align: middle;
        }
        .sel-item-choosed {
            padding: 5px;
            cursor: default;
        }
        .sel-item-choosed img {
            width: 20px;
            height: 20px;
            margin-right: 5px;
        }
        .sel-del {
            float: right;
            cursor: pointer;
        }
        .sel-item-choosed-hover {
            background-color: #eee;
        }
        a:hover {
            text-decoration: none;
        }
        .tabDiv {
            margin-top: 10px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js"></script>
    <script src="../js/jquery-alerts/cws.alerts.js"></script>
    <script src="../js/bootstrap/js/bootstrap.min.js"></script>
    <script src="../js/jstree/jstree.js"></script>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/bootstrap/js/bootstrap.min.js"></script>
    <script src="../js/jquery.toaster.js"></script>
    <script src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<%
    Privilege pvg = new Privilege();
    if (!pvg.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String dirCode = ParamUtil.get(request, "dirCode");
    if (dirCode.equals("")) {
        dirCode = pvg.getUserUnitCode(request);
    }

    String deptCodes = ParamUtil.get(request, "deptCodes");
    String[] depts = StrUtil.split(deptCodes, ",");
    DeptMgr dir = new DeptMgr();
    DeptDb leaf = dir.getDeptDb(dirCode);
    DeptView tv = new DeptView(request, leaf);
    String jsonData = tv.getJsonString();
    boolean isDept = ParamUtil.getBoolean(request, "isDept", true);
%>
<div style="width: 100%; height: 100%">
    <div style="width: 60%; float: left; height: 90%">
        <div id="tabs" style="height:100%; overflow-y: auto">
            <ul>
                <%
                    if (isDept) {
                %>
                <li><a href="#tabs-1">部门</a></li>
                <%
                    }
                %>
                <li><a href="#tabs-2">角色</a></li>
                <li><a href="#tabs-3">用户组</a></li>
                <li><a href="#tabs-4" class="tabs-person">人员</a></li>
            </ul>
            <%
                if (isDept) {
            %>
            <div id="tabs-1" class="tabDiv">
                <div id="deptTree"></div>
            </div>
            <%
                }
            %>
            <div id="tabs-2" class="tabDiv">
                <div class="form-inline form-group text-center">
                    <input id="role" name="role" class="form-control"/>
                    <button id="btnSearchRole" class="btn btn-default">查询</button>
                </div>
                <div id="roleBox">
                </div>
            </div>
            <div id="tabs-3" class="tabDiv">
                <div class="form-inline form-group text-center">
                    <input id="group" name="group" class="form-control"/>
                    <button id="btnSearchGroup" class="btn btn-default">查询</button>
                </div>
                <div id="groupBox">
                </div>
            </div>
        </div>
    </div>
    <div style="width: 40%; float: right; border: 1px solid #ccc; padding: 5px; height: 90%; overflow-y: auto">
        <div style="padding: 5px; line-height: 2.0; background-color: #eee; text-align: center">
            权限范围
        </div>
        <div id="scopeBox" style="height: 93%; border: 1px solid #ccc">
        </div>
    </div>
    <div style="clear: both; padding: 10px; text-align: center">
        <%--<button id="btnUser" class="btn btn-default" onclick="selUsers()">选人</button>--%>
        <%--&nbsp;&nbsp;--%>
        <button id="btnClear" class="btn btn-default" onclick="$('.sel-item-choosed').remove()">清空</button>
        &nbsp;&nbsp;
        <button id="btnOk" class="btn btn-default">确定</button>
        &nbsp;&nbsp;
        <button id="btnCancel" class="btn btn-default" onclick="window.close()">取消</button>
    </div>
</div>
</body>
<script>
    function selUsers() {
        openWin('../user_multi_sel.jsp', 800, 600);
    }

    function getSelUserNames() {
        var selUserNames = "";

        $('.sel-item-choosed').each(function() {
            if ($(this).attr("kind") == "<%=DocPriv.TYPE_USER%>") {
                if (selUserNames == "") {
                    selUserNames = $(this).attr("code");
                }
                else {
                    selUserNames += "," + $(this).attr("code");
                }
            }
        });

        return selUserNames;
    }

    function getSelUserRealNames() {
        var selUserRealNames = "";

        $('.sel-item-choosed').each(function() {
            if ($(this).attr("kind") == "<%=DocPriv.TYPE_USER%>") {
                if (selUserRealNames == "") {
                    selUserRealNames = $(this).attr("name");
                }
                else {
                    selUserRealNames += "," + $(this).attr("name");
                }
            }
        });
        return selUserRealNames;
    }

    function setUsers(users, userRealNames) {
        var ary = users.split(",");
        var aryName = userRealNames.split(",");
        var len = ary.length;
        if (len > 0) {
            for (var i=0; i < len; i++) {
                addToScope(ary[i], aryName[i], '<%=DocPriv.TYPE_USER%>');
            }
        }
    }

    function bindClick() {
        $("a").bind("click", function () {
            $("a").css("color", "");
            $(this).css("color", "red");
        });
    }

    // IE中不支持MouseEvent
    if (typeof MouseEvent !== 'function') {
        (function (){
            var _MouseEvent = window.MouseEvent;
            window.MouseEvent = function (type, dict){
                dict = dict | {};
                var event = document.createEvent('MouseEvents');
                event.initMouseEvent(
                    type,
                    (typeof dict.bubbles == 'undefined') ? true : !!dict.bubbles,
                    (typeof dict.cancelable == 'undefined') ? false : !!dict.cancelable,
                    dict.view || window,
                    dict.detail | 0,
                    dict.screenX | 0,
                    dict.screenY | 0,
                    dict.clientX | 0,
                    dict.clientY | 0,
                    !!dict.ctrlKey,
                    !!dict.altKey,
                    !!dict.shiftKey,
                    !!dict.metaKey,
                    dict.button | 0,
                    dict.relatedTarget || null
                );
                return event;
            }
        })();
    }

    function clickTab(tab) {
        var click = new MouseEvent('click', {
            bubbles: true,
            cancelable: true,
            synthetic: true,
            view: window
        });

        tab.dispatchEvent(click);
    }

    function searchRole(roleDesc) {
        $.ajax({
            type: "post",
            url: "../admin/listRoleByDesc.do",
            data: {
                roleDesc: roleDesc
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function(data, status) {
                data = $.parseJSON(data);
                if (data.ret==1) {
                    $('#roleBox').html('');
                    var jsonArr = data.result; // $.parseJSON(data.result);
                    var str = "";
                    for (var i=0; i < jsonArr.length; i++) {
                        var json = jsonArr[i];
                        str += '<div class="sel-item">';
                        str += '<img src="../images/role.png"/><a href="javascript:;" onclick="addToScope(\'' + json['code'] + '\', \'' + json['desc'] + '\', \'<%=DocPriv.TYPE_ROLE%>\');">' + json['desc'] + '</a>';
                        str += '</div>';
                    }
                    $('#roleBox').html(str);

                    $('.sel-item').hover(function() {
                        $(this).toggleClass('sel-item-hover');
                    });
                }
                else {
                    $.toaster({
                        "priority" : "info",
                        "message" : data.msg
                    });
                }
            },
            complete: function(XMLHttpRequest, status){
                $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function searchGroup(groupDesc) {
        $.ajax({
            type: "post",
            url: "../admin/listGroupByDesc.do",
            // contentType : "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                groupDesc: groupDesc
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function(data, status) {
                data = $.parseJSON(data);
                if (data.ret==1) {
                    $('#groupBox').html('');
                    var jsonArr = data.result;
                    var str = "";
                    for (var i=0; i < jsonArr.length; i++) {
                        var json = jsonArr[i];
                        str += '<div class="sel-item">';
                        str += '<img src="../images/group.png"/><a href="javascript:;" onclick="addToScope(\'' + json['code'] + '\', \'' + json['desc'] + '\', \'<%=DocPriv.TYPE_USERGROUP%>\');">' + json['desc'] + '</a>';
                        str += '</div>';
                    }
                    $('#groupBox').html(str);

                    $('.sel-item').hover(function() {
                        $(this).toggleClass('sel-item-hover');
                    });
                }
                else {
                    $.toaster({
                        "priority" : "info",
                        "message" : data.msg
                    });
                }
            },
            complete: function(XMLHttpRequest, status){
                $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    $(function () {
        // 初始化tabs，jquery-ui根据顺序初始化tabs中链接为ui-id-1、ui-id-2...
        $('#tabs').tabs();

        // 记住之前点的是哪个tab
        var lastTabId = 'ui-id-1';
        $("a[role='presentation']").click(function() {
            if ($(this).attr('href') != '#tabs-4') {
                lastTabId = $(this).attr('id');
            }
        });

        $('.tabs-person').click(function() {
            selUsers();
            // 使点击人员选项卡后，仍显示之前最后一次点的选项卡，否则人员选项卡的下方会显示出一块空白的区域
            clickTab($('#' + lastTabId)[0]);

            // 无效，得用clickTab
            // $('#' + lastTabId).trigger("click");
        });

        $('#btnSearchRole').click(function() {
            searchRole($('#role').val());
        });

        $("#role").keyup(function (e) {
            var e = e || event,
                keycode = e.which || e.keyCode;
            if (keycode == 13) {
                searchRole($('#role').val());
            }
        });

        searchRole('');

        $('#btnSearchGroup').click(function() {
            searchRole($('#group').val());
        });

        $("#group").keyup(function (e) {
            var e = e || event,
                keycode = e.which || e.keyCode;
            if (keycode == 13) {
                searchGroup($('#group').val());
            }
        });

        searchGroup('');

        $('#deptTree').jstree({
            "core": {
                "data":  <%=jsonData%>,
                "themes": {
                    "theme": "default",
                    "dots": true,
                    "icons": true
                },
                "check_callback": true,
            },
            "checkbox": {
                "keep_selected_style": true,
                "real_checkboxes": true
            },
            "plugins": ["wholerow", "themes", "ui", "types", "state"],
        }).bind('click.jstree', function (e) {//绑定选中事件
            var eventNodeName = e.target.nodeName;
            if (eventNodeName == 'INS') {
                return;
            } else if (eventNodeName == 'A') {
                var $subject = $(e.target).parent();
                if ($subject.find('ul').length > 0) {
                    var code = $(e.target).parents('li').attr('id');
                    var name = $(e.target).text();
                    addToScope(code, name, "dept");
                } else {
                    var code = $(e.target).parents('li').attr('id');
                    var name = $subject.text();
                    addToScope(code, name, "<%=DocPriv.TYPE_DEPT%>");
                }
            }
        }).bind('ready.jstree', function () {
            $("#deptTree").find("li").each(function () {
                var $this = $(this);
                $("#deptTree").jstree("uncheck_node", $this);
            });

            <%
            if (depts!=null) {
                for (String deptCode : depts) {
            %>
            // 将传入的部门置为选中状态
            $("#deptTree").find("li").each(function () {
                var $this = $(this);
                if ($this.attr("id") == '<%=deptCode%>') {
                    $("#deptTree").jstree("check_node", $this);
                }

                $("#deptTree").jstree("save_selected");
            });
            <%
                }
            }
            %>
        });

        bindClick();
    });

    function addToScope(code, name, kind) {
        var isFound = false;
        $('.sel-item-choosed').each(function() {
            if ($(this).attr('code')==code && $(this).attr('kind')==kind) {
                // 判断是否有重复
                $.toaster({
                    "priority" : "info",
                    "message" : name + " 已被选择"
                });
                isFound = true;
                return;
            }
        });

        if (isFound) {
            return;
        }

        var imgName = "";
        if (kind=="<%=DocPriv.TYPE_DEPT%>") {
            imgName = "dept";
        }
        else if (kind=="<%=DocPriv.TYPE_USERGROUP%>") {
            imgName = "group";
        }
        else if (kind=="<%=DocPriv.TYPE_ROLE%>") {
            imgName = "role";
        }
        else {
            imgName = "user";
        }

        var kindImg = "<img src='../images/" + imgName + ".png'/>";
        $('#scopeBox').append('<div class="sel-item-choosed" code="' + code + '" kind="' + kind + '" name="' + name + '">' + kindImg + name
                + '<img src="../images/delete.png" class="sel-del" onclick="delItem(\'' + code + '\')"/>'
                + '</div>');
        $('div[code="' + code + '"]').hover(function() {
            $(this).toggleClass('sel-item-choosed-hover');
        })
    }

    function delItem(code) {
        $('div[code="' + code + '"]').remove();
    }

    $(function() {
        $('#btnOk').click(function() {
            var jsonArr = [];
            $('.sel-item-choosed').each(function() {
                var code = $(this).attr('code');
                var name = $(this).attr('name');
                var kind = $(this).attr('kind');
                jsonArr.push({code:code, name:name, kind: kind});
            });
            window.opener.setDeptRoleGroup(jsonArr);
            window.close();
        });

        var deptRoleGroup = window.opener.getDeptRoleGroup();
        if (deptRoleGroup!="") {
            var jsonArr = $.parseJSON(deptRoleGroup);
            for (var i=0; i < jsonArr.length; i++) {
                var json = jsonArr[i];
                addToScope(json["code"], json["name"], json["kind"]);
            }
        }
    })
</script>
</html>