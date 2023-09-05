<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.*" %>
<%@ page import="com.cloudweb.oa.config.JwtProperties" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%
    Directory dir = new Directory();
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

    String op = ParamUtil.get(request, "op");

    String toa = ParamUtil.get(request, "toa");
    String msg = ParamUtil.get(request, "msg");
    // 用于前端集成
    JwtProperties jwtProperties = SpringUtil.getBean(JwtProperties.class);
    String header = jwtProperties.getHeader();
    String headerVal = ParamUtil.get(request, header);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程目录</title>
    <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        a.disabled {
            color: #cccccc;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery.my.js"></script>
    <script src="../js/jstree/jstree.js"></script>
    <link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css"/>
    <script src="../js/jquery.toaster.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td width="33%" align="left">
            <div style="text-align: center; margin-top: 10px">
                <select id="flowType" style="width: 80%;">
                    <%
                        Leaf rootLeaf = dir.getLeaf(Leaf.CODE_ROOT);
                        DirectoryView dv = new DirectoryView(rootLeaf);
                        StringBuffer outStr = new StringBuffer();
                        dv.getFlowTypeAsOptions(request, outStr, rootLeaf, rootLeaf.getLayer(), "");
                        out.print(outStr);
                    %>
                </select>
                <script>
                    $("#flowType").val('');
                    $('#flowType').select2({placeholder: '请选择类型'});
                    $("#flowType").on("select2:select", function(e){
                        var id = e.params.data.id;
                        // var text= e.params.data.text;
                        window.open("flow_predefine_list.jsp?<%=header%>=<%=headerVal%>&dirCode=" + id, "flowPredefineMainFrame");

                        myjsTree.jstree("deselect_all", true);
                        // id是选中的节点id，参数 true表示的是不触发默认select_node.change的事件
                        myjsTree.jstree('select_node', id/*, true*/);
                    });
                </script>
            </div>
            <div id="flowTree" style="margin-top: 10px"></div>
            <%
                if (toa.equals("ok") && msg.equals("")) {
            %>
            <script>
                $.toaster({priority: 'info', message: "操作成功！"});
            </script>
            <%
            } else if (toa.equals("ok")) {
            %>
            <script>
                window.parent.flowPredefineMainFrame.err("<%=msg%>");
            </script>
            <%
                }
                if (op.equals("AddChild")) {
                    boolean re = false;
                    try {
                        re = dir.AddChild(request);
                        if (!re) {
                            out.print(StrUtil.jAlert_Back("添加节点失败，请检查编码是否重复！", "提示"));
                        } else {
            %>
            <script>
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?<%=header%>=<%=headerVal%>&dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?<%=header%>=<%=headerVal%>&toa=ok";
                $.toaster({priority: 'info', message: "操作成功！"});
            </script>
            <%
                    //out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "flow_predefine_left.jsp"));

                }
            } catch (ErrMsgException e) {
            %>
            <script>
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?<%=header%>=<%=headerVal%>&dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?toa=ok&<%=header%>=<%=headerVal%>&msg=<%=StrUtil.UrlEncode(e.getMessage())%>";
            </script>
            <%
                    //out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
                }
                return;
            }

                String jsonData = dv.getJsonString();
                ArrayList<String> list = dv.getAllUnused();
            %>
        </td>
    </tr>
</table>
</body>
<script>
    var isDraggable = true;
    function setDraggable(draggable) {
        isDraggable = draggable;
    }

    $(document).ready(function () {
        var listCode = new Array();
        var i = 0;
        <%
            for(String str : list) {
        %>
        listCode[i] = "<%=str%>";
        i++;
        <%
            }
        %>
        myjsTree = $('#flowTree').jstree({
            "core": {
                "data":  <%=jsonData%>,
                "themes": {
                    "theme": "default",
                    "dots": true,
                    "icons": true
                },
                "check_callback": true,
            },
            // "ui": {"initially_select": ["root"]},
            "plugins": ["wholerow", "dnd", "themes", "ui", "contextmenu", "types", "state"],
            "dnd": {    // 拖放插件配置
                drag_selection: false,
                is_draggable : function () {
                    var tmp = isDraggable; // return false后，chrome中点击右侧页面打开addTab，再回到本页有效了
                    if (!isDraggable) {
                        isDraggable = true;
                    }
                    return tmp;
                }
            },
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
                            window.parent.flowPredefineMainFrame.location.href = "flow_predefine_dir.jsp?parent_code=" + selectNodeId + "&<%=header%>=<%=headerVal%>&op=AddChild";
                        }
                    },
                    "modify": {
                        "label": "修改",
                        "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            selectNodeId = node.id;
                            selectNodeName = node.text;
                            window.parent.flowPredefineMainFrame.location.href = "flow_predefine_dir.jsp?op=modify&<%=header%>=<%=headerVal%>&code=" + selectNodeId;
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
                    },
                    "enableDebug": {
                        "label": "调试模式",
                        // "icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            selectNodeId = node.id;
                            $.ajax({
                                type: "post",
                                url: "setFlowDebug.do",
                                dataType: "json",
                                data: {
                                    isDebug: "true",
                                    code: node.id
                                },
                                success: function (data, status) {
                                    if (data.ret == "0") {
                                        jAlert(data.msg, "提示");
                                    } else {
                                        $.toaster({priority: 'info', message: data.msg});
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
                    "disableDebug": {
                        "label": "正常模式",
                        // "icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            selectNodeId = node.id;
                            $.ajax({
                                type: "post",
                                url: "setFlowDebug.do",
                                dataType: "json",
                                data: {
                                    isDebug: "false",
                                    code: node.id
                                },
                                success: function (data, status) {
                                    if (data.ret == "0") {
                                        jAlert(data.msg, "提示");
                                    } else {
                                        $.toaster({priority: 'info', message: data.msg});
                                    }
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
        }).bind('select_node.jstree', function (e, data) {//绑定选中事件
            for (var i = 0; i < listCode.length; i++) {
                $("#" + listCode[i] + " a").first().css("color", "#999");
            }
            // 如果允许”多窗口“，则在chrome下会在别的选项卡下的flowPredefineMainFrame中打开
            // window.open("flow_predefine_list.jsp?dirCode=" + data.node.id, "flowPredefineMainFrame");
            window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?<%=header%>=<%=headerVal%>&dirCode=" + data.node.id;
        })/*.bind('click.jstree', function (e, data) {    // 绑定选中事件，此段代码与select_node.jstree效果一样，也可用
            for (var i = 0; i < listCode.length; i++) {
                $("#" + listCode[i] + " a").first().css("color", "#999");
            }
            var eventNodeName = e.target.nodeName;
            if (eventNodeName == 'INS') {
                return;
            } else if (eventNodeName == 'A') {
                var $subject = $(e.target).parent();
                var code = $(e.target).parents('li').attr('id');
                window.open("flow_predefine_list.jsp?dirCode=" + code, "flowPredefineMainFrame");
            }
        })*/.bind('move_node.jstree', function (e, data) {//绑定移动节点事件
            //data.node.id移动节点的id
            //data.parent移动后父节点的id
            //data.position移动后所在父节点的位置，第一个位置为0
            node = data.node;
            $.ajax({
                type: "post",
                url: "moveFlowNode.do",
                dataType: "json",
                data: {
                    code: data.node.id + "",
                    parent_code: data.parent + "",
                    position: data.position + ""
                },
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    if (data.ret == 0) {
                        jAlert(data.msg, "提示", function () {
                            window.location.reload(true);
                        });
                    }
                    else {
                        $.toaster({priority: 'info', message: data.msg});
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    jAlert("移动失败！", "提示");
                    window.location.reload(true);
                }
            });
        }).bind('ready.jstree', function () {
            // myjsTree.jstree("deselect_all");

            <%
            String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
            if (!"".equals(flowTypeCode)) {
            %>
            // id是选中的节点id，参数 true表示的是不触发默认select_node.change的事件
            myjsTree.jstree('select_node', "<%=flowTypeCode%>"/*, true*/);
            <%
            } else {
            %>
            // myjsTree.jstree("select_node", "<%=Leaf.CODE_ROOT%>");
            <%
            }
            %>
        });

        for (var i = 0; i < listCode.length; i++) {
            $("#" + listCode[i] + " a").first().css("color", "#999");
        }
    });

    function deleteLeaf(code) {
        if("root" == code) {
            $.toaster({priority: 'info', message: "根节点不能被删除"});
            return;
        }
        jConfirm("您确定要删除么？相关流程也将会一起被删除！\n此操作不可逆，请预先做好数据备份！","提示",function(r){
            if (r) {
                $.ajax({
                    type: "post",
                    url: "delFlowNode.do",
                    dataType: "json",
                    data: {
                        root_code: "root",
                        code: code + ""
                    },
                    beforeSend: function(XMLHttpRequest) {
                        // parent.parent.showLoading();
                    },
                    success: function(data, status){
                        if (data.ret==1){
                            $.toaster({priority: 'info', message: "删除成功"});
                            var node = myjsTree.jstree("get_node", code);
                            myjsTree.jstree('delete_node', node);
                            window.parent.flowPredefineMainFrame.location.reload();
                        } else {
                            jAlert(data.msg,"提示");
                        }
                    },
                    complete: function(XMLHttpRequest, status){
                        // parent.parent.hiddenLoading();
                        // shrink();
                    },
                    error: function(XMLHttpRequest, textStatus){
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
</script>
</html>
