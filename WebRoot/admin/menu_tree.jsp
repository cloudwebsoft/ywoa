<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.ui.menu.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.cache.jcs.*" %>
<%@ page import="org.json.*" %>
<%@ page import="java.io.*" %>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String op = ParamUtil.get(request, "op");
    if (op.equals("del")) {
        JSONObject json = new JSONObject();

        String delcode = ParamUtil.get(request, "delcode");
        try {
            Directory dir = new Directory();
            dir.del(delcode);
        } catch (ErrMsgException e) {
            json.put("ret", 3);
            json.put("msg", e.getMessage());
            out.print(json);
            return;
        }
        json.put("ret", 1);
        out.print(json.toString());
        return;
    } else if (op.equals("move")) {
        JSONObject json = new JSONObject();
        String code = ParamUtil.get(request, "code");
        String parent_code = ParamUtil.get(request, "parent_code");
        int position = Integer.parseInt(ParamUtil.get(request, "position"));
        if ("root".equals(code)) {
            json.put("ret", "0");
            json.put("msg", "根节点不能移动！");
            out.print(json.toString());
            return;
        }
        if ("#".equals(parent_code)) {
            json.put("ret", "0");
            json.put("msg", "不能与根节点平级！");
            out.print(json.toString());
            return;
        }

        Directory dir = new Directory();
        Leaf moveleaf = dir.getLeaf(code);
        int old_position = moveleaf.getOrders();//得到被移动节点原来的位置
        String oldParentCode = moveleaf.getParentCode();
        Leaf newParentLeaf = dir.getLeaf(parent_code);

        moveleaf.setParentCode(parent_code);
        int p = position + 1;
        moveleaf.setOrders(p);
        moveleaf.update();

        boolean isSameParent = oldParentCode.equals(parent_code);

        // 重新梳理orders
        Iterator ir = newParentLeaf.getChildren().iterator();
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            // 跳过自己
            if (lf.getCode().equals(code)) {
                continue;
            }
            // 如果移动后父节点变了
            if (!isSameParent) {
                if (lf.getOrders() >= p) {
                    lf.setOrders(lf.getOrders() + 1);
                    lf.update();
                }
            }
            else {
                if (p < old_position) {//上移
                    if (lf.getOrders() >= p) {
                        lf.setOrders(lf.getOrders() + 1);
                        lf.update();
                    }
                } else {//下移
                    if (lf.getOrders() <= p && lf.getOrders() > old_position) {
                        lf.setOrders(lf.getOrders() - 1);
                        lf.update();
                    }
                }
            }
        }

        // 原节点下的孩子节点通过修复repairTree处理
        Leaf rootLeaf = dir.getLeaf(Leaf.CODE_ROOT);
        Directory dm = new Directory();
        dm.repairTree(rootLeaf);

        json.put("ret", "1");
        json.put("msg", "操作成功！");
        out.print(json.toString());
        return;
    }

    String root_code = ParamUtil.get(request, "root_code");
    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }
    if (root_code.equals("")) {
        root_code = Leaf.CODE_ROOT;
    }

    String nodeSelected = ParamUtil.get(request, "nodeSelected");
    if (nodeSelected.equals("")) {
        nodeSelected = Leaf.CODE_ROOT;
    }
    Leaf leaf = new Leaf();
    leaf = leaf.getLeaf(root_code);
    DirectoryView dv = new DirectoryView(request, leaf);
    String jsonData = dv.getJsonString();
    int flag = ParamUtil.getInt(request, "flag", 0);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=8"/>
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="Cache-Control" content="no-cache, must-revalidate"/>
    <meta http-equiv="expires" content="Wed, 26 Feb 1997 08:21:57 GMT"/>
    <title>菜单管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/jstree/themes/default/style.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css"/>
    <script src="../inc/common.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
    <script src="<%=request.getContextPath() %>/js/jstree/jstree.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.toaster.email.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>
    <style>
        body {
            overflow-y: auto;
        }
        td {
            height: 20px;
        }
        html {
            height: 100%;
            margin: 0;
            overflow-y: hidden;
        }
    </style>
</head>
<body>
<div class="organize-leftbox">
    <div class="organize-btn" onclick="add()"><img src="<%=request.getContextPath()%>/skin/images/organize/btnicon-add.png" width="20" height="20"/>增加</div>
    <div class="organize-btn" onclick="modify()"><img src="<%=request.getContextPath()%>/skin/images/organize/btnicon-alter.png" width="20" height="20"/>修改</div>
    <div class="organize-btn" onclick="del()"><img src="<%=request.getContextPath()%>/skin/images/organize/btnicon-del.png" alt="" width="20" height="20"/>删除</div>
</div>
<table cellSpacing=0 cellPadding=0 width="95%" align=center>
    <TBODY>
    <TR>
        <TD height=200 valign="top">
            <div id="departmentTree"></div>
        </TD>
    </TR>
    </TBODY>
</table>
</body>
<script type="text/javascript">
    var selectNodeId, selectNodeName;
    var inst, obj;
    var node;
    var code;
    var myjsTree;
    $(function () {
        myjsTree = $('#departmentTree')
            .jstree({
                "core": {
                    "data":  <%=jsonData%>,
                    "themes": {
                        "theme": "default",
                        "dots": true,
                        "icons": true
                    },
                    "check_callback": true,
                },
                "ui": {"initially_select": ["root"]},
                "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu", "types", "crrm"],
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
                                window.parent.rightFrame.location.href = "menu_right.jsp?parent_code=" + selectNodeId + "&op=AddChild";
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
                                window.parent.rightFrame.location.href = "menu_right.jsp?op=modify&code=" + selectNodeId;
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
            }).bind('move_node.jstree', function (e, data) {//绑定移动节点事件
                //data.node.id移动节点的id
                //data.parent移动后父节点的id
                //data.position移动后所在父节点的位置，第一个位置为0
                node = data.node;
                $.ajax({
                    type: "post",
                    url: "<%=request.getContextPath() %>/admin/menu_tree.jsp",
                    dataType: "json",
                    data: {
                        op: "move",
                        code: data.node.id + "",
                        parent_code: data.parent + "",
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
                        alert("移动失败！");
                        window.location.reload(true);
                    }
                });
            }).bind('select_node.jstree', function (e, data) {     //绑定选中事件
                if (data.event) {
                    // 点击了鼠标右键
                    if (data.event.button == 2) {
                        return;
                    }
                }

                node = data.node;
                selectNodeName = data.node.text;
                selectNodeId = data.node.id;
                window.parent.rightFrame.location.href = "menu_right.jsp?code=" + selectNodeId + "&op=modify";
            }).bind('click.jstree', function (event) {

            }).bind('ready.jstree', function () {
                positionNode("<%=nodeSelected%>");

                <%
                Vector vt = new Vector();
                leaf.getAllChild(vt, leaf);
                Iterator ir = vt.iterator();
                while (ir.hasNext()) {
                    Leaf lf  = (Leaf)ir.next();
                    if (!lf.isUse()) {
                    %>
                setNode("<%=lf.getCode()%>", false);
                <%
                }
            }
            %>
            });

        if (0 ==<%=flag%>) {
            setToaster("右键菜单可管理或拖动");
        }
    });

    function setNode(code, isUse, name) {
        if (name != null) {
            // 需放在前面，因为set_text会覆盖样式
            myjsTree.jstree("set_text", code, name);
        }
        if (isUse) {
            $("#" + code + " a").first().css("color", "#000000");
            // $("#" + code + " a").first().removeClass("nodeNotUse"); // 无效
        } else {
            // $("#" + code + " a").first().addClass("nodeNotUse"); // 无效
            $("#" + code + " a").first().css("color", "#cccccc");
        }
    }

    //定位节点 myId=code
    function positionNode(myId) {
        myjsTree.jstree("deselect_all");
        myjsTree.jstree("select_node", myId);
    }

    function deleteLeaf(code) {
        if ("root" == code) {
            setToaster("根节点不能被删除");
            return;
        }
        window.parent.rightFrame.jConfirm("您确定要删除吗?", "提示", function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "<%=request.getContextPath()%>/admin/menu_tree.jsp",
                    dataType: "json",
                    data: {
                        op: "del",
                        root_code: "root",
                        delcode: code + ""
                    },
                    beforeSend: function (XMLHttpRequest) {
                        // parent.parent.showLoading();
                    },
                    success: function (data, status) {
                        if (data.ret == 1) {
                            // lte界面parent.parent为lte/index.jsp页面，因启用了高版本jquery，不支持jAlert
                            // parent.parent.jAlert("删除成功", "提示");
                            window.parent.rightFrame.jAlert("删除成功", "提示");
                            var node = myjsTree.jstree("get_node", code);
                            myjsTree.jstree('delete_node', node);
                            window.parent.rightFrame.location.reload();
                        } else {
                            window.parent.rightFrame.jAlert(data.msg, "提示");
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

    function add() {
        if (selectNodeId == undefined) {
            setToaster("请选择操作节点");
            return;
        }
        window.parent.rightFrame.location.href = "menu_right.jsp?parent_code=" + selectNodeId + "&op=AddChild";
    }

    function modify() {
        if (selectNodeId == undefined) {
            setToaster("请选择操作节点");
            return;
        }
        window.parent.rightFrame.location.href = "menu_right.jsp?op=modify&code=" + selectNodeId;
    }

    function del() {
        if (selectNodeId == undefined) {
            setToaster("请选择操作节点");
            return;
        }
        deleteLeaf(selectNodeId);
    }

    function setToaster(mess) {
        $.toaster({priority: 'info', message: mess});
    }
</script>
</html>




