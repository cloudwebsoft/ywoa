<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%
    String skincode = UserSet.getSkin(request);
    if (skincode == null || "".equals(skincode)) {
        skincode = UserSet.defaultSkin;
    }
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();

    String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>文件柜-菜单</title>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/frame.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
    <style>
        #directoryTree {
            margin-top: 10px;
        }
    </style>
    <link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jstree/jstree.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>

</head>
<body>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<div id="directoryTree"></div>
<%
    String dirCode = ParamUtil.get(request, "dir_code");
    if ("".equals(dirCode)) {
        dirCode = Leaf.ROOTCODE;
    }
    Leaf leaf = dir.getLeaf(dirCode);
    DirView tv = new DirView(request, leaf);

    String jsonData = tv.getJsonStringByUser(leaf, new Privilege().getUser(request));
%>
</body>
<script>
    var myjsTree;
    $(document).ready(function () {
        myjsTree = $('#directoryTree').jstree({
            "core": {
                "data":  <%=jsonData%>,
                "themes": {
                    "theme": "default",
                    "dots": true,
                    "icons": true
                },
                "check_callback": true,
            },
            "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu", "types", "crrm", "state"],
            "contextmenu": {	//绑定右击事件
                "items": {
                    "create": {
                        "label": "增加",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_add.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            selectNodeId = node.id;
                            selectNodeName = node.text;
                            parent.mainFileFrame.location.href = "dir_right.jsp?parent_code=" + selectNodeId + "&op=AddChild";
                        }
                    },
                    "rename": {
                        "label": "修改",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            selectNodeId = node.id;
                            selectNodeName = node.text;
                            window.parent.mainFileFrame.location.href = "dir_right.jsp?op=modify&code=" + selectNodeId;
                        }
                    },
                    "remove": {
                        "label": "删除",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            selectNodeId = node.id;
                            selectNodeName = node.text;
                            deleteLeaf(selectNodeId, inst, node);
                        }
                    }
                }
            }
        }).bind('click.jstree', function (e, data) {//绑定选中事件
            var eventNodeName = e.target.nodeName;
            if (eventNodeName == 'INS') {
                return;
            } else if (eventNodeName == 'A') {
                var $subject = $(e.target).parent();
                var code = $(e.target).parents('li').attr('id');
                var url = "fileark_main.jsp?dir_code=" + code;
                if (code.indexOf("cws_prj_") == 0) {
                    var project = code.substring(8);
                    var p = project.indexOf("_");
                    if (p != -1) {
                        project = project.substring(0, p);
                    }
                    url += "&projectId=" + project + "&parentId=" + project + "&formCode=project";
                }
                window.open(url, "mainFileFrame");
            }
        }).bind('move_node.jstree', function (e, data) {//绑定移动节点事件
            //data.node.id移动节点的id
            //data.parent移动后父节点的id
            //data.position移动后所在父节点的位置，第一个位置为0
            node = data.node;
            $.ajax({
                type: "post",
                url: "<%=request.getContextPath() %>/fileark/move.do",
                dataType: "json",
                data: {
                    code: data.node.id + "",
                    parent_code: data.parent + "",
                    position: data.position + ""
                },
                success: function (data, status) {
                    $.toaster({priority: 'info', message: data.msg});
                    if (data.ret == 0) {
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
        }).bind('ready.jstree', function () {
            myjsTree.jstree("deselect_all");
            myjsTree.jstree("select_node", "<%=Leaf.ROOTCODE%>");
        });
    });

    function deleteLeaf(code) {
        if ("root" == code) {
            setToaster("根节点不能被删除");
            return;
        }
        window.parent.mainFileFrame.jConfirm("您确定要删除吗?", "提示", function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "<%=request.getContextPath() %>/fileark/delLeaf.do",
                    dataType: "json",
                    data: {
                        root_code: "root",
                        code: code + ""
                    },
                    beforeSend: function (XMLHttpRequest) {
                        // parent.parent.showLoading();
                    },
                    success: function (data, status) {
                        if (data.ret == 1) {
                            window.parent.mainFileFrame.jAlert("删除成功", "提示");
                            var node = myjsTree.jstree("get_node", code);
                            myjsTree.jstree('delete_node', node);
                            window.parent.mainFileFrame.location.reload();
                        } else {
                            parent.parent.jAlert(data.msg, "提示");
                        }
                        positionNode(data.selectCode);
                    },
                    complete: function (XMLHttpRequest, status) {
                        // parent.parent.hiddenLoading();
                        // shrink();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        })
    }

    //定位节点 myId=code
    function positionNode(myId) {
        myjsTree.jstree("deselect_all");
        myjsTree.jstree("select_node", myId);
    }

    function setToaster(mess) {
        $.toaster({priority: 'info', message: mess});
    }
</script>
</html>
