<%@ page contentType="text/html; charset=utf-8" language="java" import="cn.js.fan.util.ErrMsgException" errorPage="" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.pvg.PrivDb" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.visual.ModulePrivDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="java.util.Vector" %>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="dm" scope="page" class="com.redmoon.oa.basic.TreeSelectMgr"/>
<%
    Privilege privilege = new Privilege();
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String basicCode = ParamUtil.get(request, "basicCode");
    String userName = privilege.getUser(request);
    SelectDb selectDb = new SelectDb();
    selectDb = selectDb.getSelectDb(basicCode);
    if (!selectDb.isLoaded()) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
        return;
    }

    int kind = selectDb.getKind();
    SelectKindPriv skp = new SelectKindPriv();
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        if (skp.canUserAppend(userName, kind) || skp.canUserModify(userName, kind) || skp.canUserDel(userName, kind)) {
        } else {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String root_code = basicCode;
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta HTTP-EQUIV="pragma" CONTENT="no-cache"/>
    <meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate"/>
    <meta HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT"/>
    <title>模块-树形</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/jstree/themes/default/style.css"/>
    <script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
    <script src="<%=request.getContextPath() %>/js/jstree/jstree.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>

    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../inc/map.js"></script>

    <style>
        td {
            height: 20px;
        }

        .unit {
            font-weight: bold;
        }

        .deptNodeHidden {
            color: #cccccc;
        }
    </style>
    <script>
        function form1_onsubmit() {
            o("root_code").value = getRootCode();
        }

        var inst;
        var node;
        var code;
    </script>
</head>
<body>
<Script>
    var map = new Map();
    var root_code = "<%=root_code%>";

    // 使框架的bottom能得到此root_code
    function getRootCode() {
        return root_code;
    }
</Script>
<%
    TreeSelectDb leaf = dm.getTreeSelectDb(root_code);
    String root_name = leaf.getName();
    int root_layer = leaf.getLayer();
    boolean isContextMenu = leaf.isContextMenu();
    String root_description = leaf.getDescription();
    boolean isHome = false;

    Privilege pvg = new Privilege();
    String parent_code = ParamUtil.get(request, "parent_code");
    // String root_code = ParamUtil.get(request, "root_code");
    if (parent_code.equals("")) {
        parent_code = root_code;
    }

    TreeSelectMgr tsm = new TreeSelectMgr();
    TreeSelectDb tsd = tsm.getTreeSelectDb(root_code);
    if (!tsd.isLoaded()) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "基础数据不存在！"));
        return;
    }
    String rootName = tsd.getName();
%>
<div>
    <table width='100%' cellpadding='0' cellspacing='0'>
        <tr>
            <td class="head">&nbsp;</td>
        </tr>
    </table>
    <table cellSpacing=0 cellPadding=0 width="95%" align=center>
        <TBODY>
        <TR>
            <TD height=200 valign="top">
                <%
                    TreeSelectView dv = new TreeSelectView(leaf);
                    String jsonData = dv.getJsonString();
                    List<String> list = dv.getAllUnit();
                %>
                <div id="moduleTree"></div>

            </TD>
        </TR>
        </tbody>
    </table>
</div>
<script>
    var isContextMenu = <%=isContextMenu%>;

    <%
    Vector v = new Vector();
    leaf.getAllChild(v, leaf);
    Iterator<TreeSelectDb> ir = v.iterator();
    while (ir.hasNext()) {
        leaf = ir.next();
    %>
    map.put('<%=leaf.getCode()%>', <%=leaf.isOpen()%>);
    <%
    }
    %>

    var listCode = new Array();
    var i = 0;
    <%
        for(String str : list){
    %>
    listCode[i] = "<%=str%>";
    i++;
    <%
    }
    %>
    var myjsTree;
    $(function () {
        myjsTree = $('#moduleTree')
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
                "ui": {"initially_select": ["<%=root_code %>"]},
                <%
                if (isContextMenu) {
                %>
                "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu", "types", "crrm", "state"],
                <%
                } else {
                %>
                "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "types", "crrm", "state"],
                <%
                }
                %>
                "contextmenu": {	//绑定右击事件
                    "items": {
                        "create": {
                            "label": "添加",
                            "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_add.png",
                            "action": function (data) {
                                var inst = $.jstree.reference(data.reference),
                                    obj = inst.get_node(data.reference);
                                inst.create_node(obj, {}, "last", function (new_node) {
                                    setTimeout(function () {
                                        new_node.text = new_node.text.replace("New node", "新建分类");
                                        inst.edit(new_node);
                                    }, 0);
                                });
                             }
                        },
                        "rename": {
                            "label": "修改",
                            "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                            "action": function (data) {
                                inst = $.jstree.reference(data.reference);
                                node = inst.get_node(data.reference);
                                var code = node.id;
                                var name = node.text;

                                $.ajax({
                                    type: "post",
                                    url: "basic_tree_select_do.jsp",
                                    dataType: "json",
                                    data: {
                                        op: "parent_name",
                                        code: node.parent,
                                        myCode: code
                                    },
                                    success: function (data, status) {
                                        if (data.ret == "1") {
                                            $("#quipbottom").show();
                                            $("#showParent").show();
                                            $('#span_code').html(code);
                                            $('#code').val(code);
                                            $('#name').val(name);
                                            $('#parent_code').val(node.parent);
                                            $('#parent_name').html(data.msg);
                                            $('#parentCode').val(node.parent);
                                            $('#op').val("modify");

                                            $('#link').val(data.link);
                                            $('#preCode').val(data.preCode);
                                            if (data.preCode == "flow") {
                                                $('#flowTypeCode').val(data.formCode);
                                            } else if (data.preCode == "module") {
                                                $('#formCode').val(data.formCode).trigger("change");
                                            }

                                            onChangePreCode();
                                        }
                                    },
                                    complete: function (XMLHttpRequest, status) {
                                    },
                                    error: function (XMLHttpRequest, textStatus) {
                                        jAlert(XMLHttpRequest.responseText, "提示");
                                    }
                                });
                            }
                        },
                        "rename": {
                            "label": "重命名",
                            "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                            "action": function (data) {
                                var inst = $.jstree.reference(data.reference),
                                    obj = inst.get_node(data.reference);
                                inst.edit(obj);
                            }
                        },
                        "remove": {
                            "label": "删除",
                            "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
                            "action": function (data) {
                                var inst = $.jstree.reference(data.reference);
                                var obj = inst.get_node(data.reference);
                                var code = obj.id;
                                var mainWin = window.parent.document.getElementById("mainModuleFrame").contentWindow;
                                if ("<%=root_code %>" == code) {
                                    mainWin.jAlert("根节点不能被删除!", "提示");
                                    return;
                                }
                                mainWin.jConfirm('您确定要删除吗?', '提示', function (r) {
                                    if (!r) {
                                        return;
                                    } else {
                                        $.ajax({
                                            type: "post",
                                            url: "../basicdata/delNode.do",
                                            dataType: "json",
                                            data: {
                                                root_code: " <%=root_code %>",
                                                code: code + ""
                                            },
                                            success: function (data, status) {
                                                //注释代码能支持批量删除
                                                //if(inst.is_selected(obj)) {
                                                //	inst.delete_node(inst.get_selected());
                                                //}
                                                //else {
                                                //	inst.delete_node(obj);
                                                //}
                                                inst.delete_node(obj);
                                            },
                                            complete: function (XMLHttpRequest, status) {
                                            },
                                            error: function (XMLHttpRequest, textStatus) {
                                                jAlert(XMLHttpRequest.responseText, "提示");
                                            }
                                        });
                                    }
                                })
                            }
                        },
                        "open": {
                            "label": "启用",
                            // "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                            "action": function (data) {
                                var inst = $.jstree.reference(data.reference);
                                node = inst.get_node(data.reference);
                                var code = node.id;
                                var name = node.text;

                                $.ajax({
                                    type: "post",
                                    url: "../basicdata/openNode.do",
                                    dataType: "json",
                                    data: {
                                        code: code
                                    },
                                    success: function (data, status) {
                                        $.toaster({priority: 'info', message: data.msg});
                                        setNodeOpen(code, true);
                                    },
                                    complete: function (XMLHttpRequest, status) {
                                    },
                                    error: function (XMLHttpRequest, textStatus) {
                                        jAlert(XMLHttpRequest.responseText, "提示");
                                    }
                                });

                            }
                        },
                        "close": {
                            "label": "停用",
                            // "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                            "action": function (data) {
                                var inst = $.jstree.reference(data.reference);
                                node = inst.get_node(data.reference);
                                var code = node.id;
                                var name = node.text;

                                $.ajax({
                                    type: "post",
                                    url: "../basicdata/closeNode.do",
                                    dataType: "json",
                                    data: {
                                        code: code
                                    },
                                    success: function (data, status) {
                                        $.toaster({priority: 'info', message: data.msg});
                                        setNodeOpen(code, false);
                                    },
                                    complete: function (XMLHttpRequest, status) {
                                    },
                                    error: function (XMLHttpRequest, textStatus) {
                                        jAlert(XMLHttpRequest.responseText, "提示");
                                    }
                                });
                            }
                        }
                    }
                }
            }).bind('move_node.jstree', function (e, data) {//绑定移动节点事件
                //data.node.id移动节点的id
                //data.parent移动后父节点的id
                //data.position移动后所在父节点的位置，第一个位置为0
                $.ajax({
                    type: "post",
                    url: "../basicdata/moveNode.do",
                    dataType: "json",
                    data: {
                        code: data.node.id + "",
                        parent_code: data.parent + "",
                        position: data.position + ""
                    },
                    success: function (data, status) {
                        if (data.ret == 0) {
                            jAlert(data.msg, "提示");
                            window.location.reload(true);
                        }
                        else {
                            $.toaster({priority: 'info', message: '操作成功'});
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        jAlert("移动失败！", "提示");
                        window.location.reload(true);
                    }
                });
                for (var i = 0; i < listCode.length; i++) {
                    //$("#"+listCode[i]+" a").first().css("font-weight","bold");
                }
            }).bind('select_node.jstree', function (e, data) {
                var nodeCode = data.node.id;
                //绑定选中事件
                $.ajax({
                    type: "post",
                    url: "../basicdata/getTreeNodeUrl.do",
                    dataType: "json",
                    data: {
                        basicCode: "<%=basicCode%>",
                        nodeCode: nodeCode
                    },
                    success: function (data, status) {
                        if (data.ret == "1") {
                            var url = data.url;
                            window.parent.document.getElementById("mainModuleFrame").contentWindow.location.href = url;
                        }
                        else {
                            $.toaster({priority: 'info', message: data.msg});
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        jAlert(XMLHttpRequest.responseText, "提示");
                    }
                });
            }).bind('click.jstree', function (event) {

            }).bind("open_node.jstree",function(e, data) {
                var inst = $('#moduleTree').jstree(true);
                var ary = data.node.children;
                for (i in ary) {
                    var child = inst.get_node(ary[i]);
                    setNodeOpen(String(child.id), map.get(String(child.id)).value);
                }
                setNodeOpen(String(data.node.id), map.get(String(data.node.id)).value);
            }).bind('ready.jstree', function () {
                <%
                ir = v.iterator();
                while (ir.hasNext()) {
                    leaf = ir.next();
                    if (!leaf.isOpen()) {
                %>
                setNodeOpen('<%=leaf.getCode()%>', false);
                <%
                    }
                }
                %>

                var inst = $('#moduleTree').jstree(true);
                var treeNode = inst.get_selected(true)[0]; //获取所有选中的节点对象
                // 如果没有选中的节点，则选中根节点
                if (!treeNode) {
                    // 参数 true表示的是不触发默认select_node.change的事件
                    $('#moduleTree').jstree('select_node', '<%=basicCode%>' /*, true */);

                    // var rootNode = inst.get_node('<%=basicCode%>');
                    // inst.select_node(rootNode);
                }
            }).bind("rename_node.jstree", function (e, data) {
                if (!isContextMenu) {
                    return;
                }
                if (data.node.text.indexOf("\"") > 0 || data.node.text.indexOf("'") > 0) {
                    jAlert("名称不能含有单引号、双引号字符", "提示");
                    window.location.reload();
                    return;
                }
                var len = data.node.text.length;
                if (len > 20) {
                    jAlert("名称不能超过20个字符！", "提示");
                    window.location.reload();
                    return;
                }

                var name = data.node.text;
                var code = data.node.id + "";

                $.ajax({
                    type: "post",
                    url: "../basicdata/rename.do",
                    dataType: "json",
                    data: {
                        code: code,
                        newName: name,
                        root_code: "<%=StrUtil.UrlEncode(root_code)%>"
                    },
                    success: function (data, status) {
                        if (data.ret != 2) {
                            $.toaster({priority: 'info', message: data.msg});
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        jAlert("重命名失败！", "提示");
                    }
                });
            }).bind("create_node.jstree", function (e, data) {
                if (!isContextMenu) {
                    return;
                }
                console.log('create_node event');
                var node = data.node;
                $.ajax({
                    type: "post",
                    url: "../basicdata/createNode.do",
                    dataType: "json",
                    data: {
                        parent_code: encodeURI(data.node.parent),
                        root_code: "<%=StrUtil.UrlEncode(root_code)%>",
                        name: data.node.text + ""
                    },
                    success: function (data, status) {
                        if (data.ret != 1) {
                            $.toaster({priority: 'info', message: data.msg});
                        }
                        else {
                            var inst = $('#moduleTree').jstree(true);
                            inst.set_id(node, data.code);
                        }
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        jAlert("新建失败！", "提示");
                        window.location.reload();
                    }
                });
            });

        if (isContextMenu) {
            $.toaster({priority: 'info', message: '右键菜单可管理，菜单项可拖动'});
        }
    });

    function addNewNode(parentCode, myId, myText) {
        if (parentCode == undefined) {
            parentCode = "root";
        }
        myjsTree.jstree('create_node', parentCode + "", {'id': myId + "", 'text': myText + ""}, 'last');

        /*if (parentCode != '<%=root_code%>') {
            myjsTree.jstree("toggle_node", parentCode);
        }
        myjsTree.jstree("deselect_all");
        myjsTree.jstree("select_node", myId);*/
    }

    function modifyTitle(code, name) {
        // console.log('code=' + code + ' name=' + name);
        var inst = $('#moduleTree').jstree(true);
        var node = inst.get_node(code);

        // console.log(node.id + '-' + node.text);
        // inst.set_text(node, name, "zh"); // set_text为内部方法，不推荐
        inst.rename_node(node, name);
        $("#moduleTree").jstree('rename_node', node , name ); // 可用

        // $("#moduleTree").jstree('set_text', node , name );

        // set_text为内部方法，不推荐
        // $("#moduleTree").jstree('set_text',"#" + code, name);
        // 以使用此方法来获取最后一个错误.
        // console.log($('#moduleTree').jstree(true).last_error());
    }

    function mysubmit() {
        o("root_code").value = getRootCode();
        $.ajax({
            url: "basic_tree_select_do.jsp?" + $('#form1').serialize(),
            type: "post",
            dataType: "json",
            //data: $('#form1').serialize(),
            success: function (data, status) {
                if (data.ret == 1) {
                    $("#quipbottom").hide();
                    if ("modify" == $("#op").val()) {
                        modifyTitle($("#name").val());
                        jAlert_Redirect(data.msg, "提示", "basic_tree_select_frame.jsp?root_code=<%=root_code%>");
                    } else if ("AddChild" == $("#op").val()) {
                        addNewNode($("#code").val(), $("#name").val());
                    }
                } else if (data.ret == 2) {
                    jAlert(data.msg, "提示");
                }
            },
            error: function (XMLHttpRequest, textStatus) {
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    function myreset() {
        document.getElementById("name").value = "";
    }

    function delNode(codes) {
        var ref = $('#moduleTree').jstree(true);
        var ary = codes.split(',');
        for (var i in ary) {
            var obj = ref.get_node(ary[i]);
            ref.delete_node(obj);
        }
    }

    function setNodeOpen(code, isOpen) {
        map.put(code, isOpen);
        if (isOpen) {
            $("#" + code + " a").first().css("color", "#000000");
            // $("#" + code + " a").first().removeClass("nodeNotUse"); // 无效
        } else {
            // $("#" + code + " a").first().addClass("nodeNotUse"); // 无效
            $("#" + code + " a").first().css("color", "#cccccc");
        }
    }

    function positionNode(nodeId) {
        myjsTree.jstree("deselect_all");
        myjsTree.jstree("select_node", nodeId);
    }

</script>
</body>
</html>