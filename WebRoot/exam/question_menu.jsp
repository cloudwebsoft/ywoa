<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<%
    String path = request.getContextPath();
%>
<head>
    <title>题目管理</title>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../js/pagination/jquery.pagination.js"></script>
    <script type="text/javascript" src="<%=path %>/js/jstree/jstree_inc_children.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=path %>/js/jstree/themes/default/style.css"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/pagination/pagination.css" rel="stylesheet" type="text/css" media="screen"/>
    <style type="text/css">
    </style>
</head>
<body>
<div id="q_menu" style="margin-left: 20px;margin-top: 30px;">
    <input type="hidden" id="major" name="major" value=""/>
    <%
        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(MajorView.ROOT_CODE);
        MajorView mv = new MajorView(tsd);
        String jsonData = mv.getJsonStringByUser(new Privilege().getUser(request));
    %>
    <div id="examMajorTree" class="examMajorTree"></div>
    <script>
        var framec = window.parent.document.getElementById("questionListId");
        var rootCode = "<%=MajorView.ROOT_CODE%>";
        var major = o("major").value;
        $("#examMajorTree").jstree({
            'core': {
                "multiple": false,
                'data': <%=jsonData%>,
                "check_callback": true,
                "themes": {
                    "theme": "default",
                    "dots": true,
                    "icons": true
                },
                'dblclick_toggle': false
                //禁用tree的双击展开
            },
            "ui": {"initially_select": ["root"]},
            "plugins": ["unique", "dnd", "wholerow", "themes", "contextmenu", "ui", "types", "crrm", "state"],
            "contextmenu": {	//绑定右击事件
                "items": {
                    "create": {
                        "label": "添加子项",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_add.png",
                        "action": function (data) {
                            var inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            code = node.id;
                            var name = node.text;
                            framec.src = "question_kind_add.jsp?code=" + code + "&name=" + name + "&op=add";
                        }
                    },
                    "rename": {
                        "label": "修改",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                        "action": function (data) {
                            var inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            code = node.id;
                            var name = node.text;
                            var parentCode = node.parent;
                            if (code == rootCode) {
                                parent.parent.jAlert("没有权限", "提示");
                            } else {
                                framec.src = "question_kind_edit.jsp?code=" + code + "&name=" + name + "&op=edit" + "&parentCode=" + parentCode;
                            }
                        }
                    },
                    "remove": {
                        "label": "删除",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            var obj = inst.get_node(data.reference);
                            node = inst.get_node(data.reference);
                            code = node.id;
                            if (rootCode == code) {
                                parent.parent.jAlert("根节点不能被删除!", "提示");
                                return;
                            }
                            parent.parent.jConfirm("您确定要删除吗?", "提示", function (r) {
                                if (!r) {
                                    return;
                                } else {
                                    ajaxPost('../question/questionKindDel.do', {delcode: code + ""}, function (data) {
                                        data = $.parseJSON(data);
                                        if (data.ret == "1") {
                                            inst.delete_node(obj);
                                            parent.parent.jAlert(data.msg, "提示");
                                        } else if (data.ret == "0") {
                                            parent.parent.jAlert(data.msg, "提示");
                                        } else if (data.ret == "2") {
                                            parent.parent.jAlert(data.msg, "提示");
                                        }
                                    });
                                }
                            })
                        }
                    },
                    "privilege": {
                        "label": "权限",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_privilege.png",
                        "action": function (data) {
                            var inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            code = node.id;
                            var name = node.text;
                            var parentCode = node.parent;
                            framec.src = "exam_major_priv_manage.jsp?majorCode=" + code;
                        }
                    }
                }
            }
        })
            .bind('select_node.jstree', function (e, data) {//绑定选中事件
                o("major").value = data.node.id;
                framec.src = "exam_question_manage.jsp?op=search&major=" + data.node.id;
            })
            .bind('move_node.jstree', function (e, data) {//绑定移动节点事件
                //data.node.id移动节点的id
                //data.parent移动后父节点的id
                //data.position移动后所在父节点的位置，第一个位置为0
                node = data.node;
                //alert("移动："+data.node.id);
                $.ajax({
                    type: "post",
                    url: "<%=request.getContextPath()%>/question/moveNode.do",
                    dataType: "json",
                    data: {
                        code: data.node.id + "",
                        parentCode: data.parent + "",
                        position: data.position + ""
                    },
                    success: function (data, status) {
                        if (data.ret == 0) {
                            alert(data.msg);
                            window.location.reload(true);
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                        window.location.reload(true);
                    }
                })
            })
            .on("loaded.jstree", function (e, data) {//弄人选中根节点
                $("#examMajorTree").jstree("deselect_all", true);
                $('#examMajorTree').jstree('select_node', '<%=MajorView.ROOT_CODE%>', true);
            });
        $(function () {
        })

        function ajaxPost(path, parameter, func) {
            $.ajax({
                type: "post",
                url: path,
                data: parameter,
                dataType: "html",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                success: function (data, status) {
                    func(data);
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        }
    </script>
</div>
</body>
</html>

