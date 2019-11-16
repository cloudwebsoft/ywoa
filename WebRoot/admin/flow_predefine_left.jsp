<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.*" %>
<%
    Directory dir = new Directory();
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

    String op = ParamUtil.get(request, "op");

    if ("setDebug".equals(op)) {
        String code = ParamUtil.get(request, "code");
        boolean isDebug = ParamUtil.getBoolean(request, "isDebug", true);
        JSONObject json = new JSONObject();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        String myUnitCode = privilege.getUserUnitCode(request);
        LeafPriv lp = new LeafPriv(code);
        if (privilege.isUserPrivValid(request, "admin.unit") && lf.getUnitCode().equals(myUnitCode))
            ;
        else if (!lp.canUserExamine(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", "权限非法！");
            out.print(json);
            return;
        }

        boolean hasChild = false;
        if (lf.getChildCount() > 0) {
            hasChild = true;
            Vector v = new Vector();
            lf.getAllChild(v, lf);
            v.addElement(lf);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                lf = (Leaf) ir.next();
                if (lf.getType() != Leaf.TYPE_NONE) {
                    lf.setDebug(isDebug);
                    lf.update();
                }
            }
        } else {
            lf.setDebug(isDebug);
            lf.update();
        }
        json.put("ret", "1");
        if (hasChild) {
            if (isDebug) {
                json.put("msg", lf.getName() + "下的流程已置为调试模式！");
            } else {
                json.put("msg", lf.getName() + "下的流程已置为正常模式！");
            }
        } else {
            if (isDebug) {
                json.put("msg", lf.getName() + " 已置为调试模式！");
            } else {
                json.put("msg", lf.getName() + " 已置为正常模式！");
            }
        }
        out.print(json);
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

        Leaf moveleaf = dir.getLeaf(code);
        String oldParentCode = moveleaf.getParentCode();
        int old_position = moveleaf.getOrders();//得到被移动节点原来的位置，从1开始

        Leaf oldParentLeaf = dir.getLeaf(oldParentCode);
        Leaf newParentLeaf;
        if (parent_code.equals(oldParentCode)) {
            newParentLeaf = oldParentLeaf;
        } else {
            newParentLeaf = dir.getLeaf(parent_code);
        }
        // 移动后的层级需一致
        if (oldParentLeaf.getLayer() != newParentLeaf.getLayer()) {
            json.put("ret", "0");
            json.put("msg", "层级不一致，不能移动！");
            out.print(json.toString());
            return;
        }
        int p = position + 1;  // jstree的position是从0开始的，而orders是从1开始的
        moveleaf.setParentCode(parent_code);
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

        // 父节点有变化
        if (!isSameParent) {
            // 只有二级节点的父节点才会有变化，此时需变动其表单所属的类别
            FormDb fd = new FormDb();
            fd = fd.getFormDb(moveleaf.getFormCode());
            fd.setFlowTypeCode(parent_code);
            fd.saveContent();
        }

        json.put("ret", "1");
        json.put("msg", "操作成功！");
        out.print(json.toString());
        return;
    }

    String toa = ParamUtil.get(request, "toa");
    String msg = ParamUtil.get(request, "msg");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程目录</title>
    <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
    <script src="../js/jstree/jstree.js"></script>
    <link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css"/>
    <script src="../js/jquery.toaster.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script>
        function findObj(theObj, theDoc) {
            var p, i, foundObj;

            if (!theDoc) theDoc = document;
            if ((p = theObj.indexOf("?")) > 0 && parent.frames.length) {
                theDoc = parent.frames[theObj.substring(p + 1)].document;
                theObj = theObj.substring(0, p);
            }
            if (!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
            for (i = 0; !foundObj && i < theDoc.forms.length; i++)
                foundObj = theDoc.forms[i][theObj];
            for (i = 0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++)
                foundObj = findObj(theObj, theDoc.layers[i].document);
            if (!foundObj && document.getElementById) foundObj = document.getElementById(theObj);

            return foundObj;
        }

        function ShowChild(imgobj, name) {
            var tableobj = findObj("childof" + name);
            if (!tableobj)
                return;
            if (tableobj.style.display == "none") {
                tableobj.style.display = "";
                if (imgobj.src.indexOf("i_puls-root-1.gif") != -1)
                    imgobj.src = "images/i_puls-root.gif";
                if (imgobj.src.indexOf("i_plus-1-1.gif") != -1)
                    imgobj.src = "images/i_plus2-2.gif";
                if (imgobj.src.indexOf("i_plus-1-0.gif") != -1)
                    imgobj.src = "images/i_plus2-1-0.gif";
            } else {
                tableobj.style.display = "none";
                if (imgobj.src.indexOf("i_puls-root.gif") != -1)
                    imgobj.src = "images/i_puls-root-1.gif";
                if (imgobj.src.indexOf("i_plus2-2.gif") != -1)
                    imgobj.src = "images/i_plus-1-1.gif";
                if (imgobj.src.indexOf("i_plus2-1-0.gif") != -1)
                    imgobj.src = "images/i_plus-1-0.gif";
            }
        }

        function selectDir(dirCode, dirName) {
            form1.dirCode.value = dirCode;
            form1.submit();
        }

        function bindClick() {
            $("a").bind("click", function () {
                $("a").css("color", "");
                $(this).css("color", "red");
            });
        }

        $(document).ready(bindClick);
    </script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        a.disabled {
            color: #cccccc;
        }
    </style>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td width="33%" align="left">
            <div id="flowTree"></div>
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
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?toa=ok";
                $.toaster({priority: 'info', message: "操作成功！"});
            </script>
            <%
                    //out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "flow_predefine_left.jsp"));

                }
            } catch (ErrMsgException e) {
            %>
            <script>
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?toa=ok&msg=<%=e.getMessage()%>";
            </script>
            <%
                    //out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
                }
                return;
            } else if (op.equals("del")) {
                String delcode = ParamUtil.get(request, "delcode");
                try {
                    dir.del(request, delcode);
            %>
            <script>
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?toa=ok";
                $.toaster({priority: 'info', message: "操作成功！"});
            </script>
            <%
            } catch (ErrMsgException e) {%>
            <script>
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?toa=ok&msg=<%=e.getMessage()%>";
            </script>
            <%
                    }
                    return;
                }
                if (op.equals("modify")) {
                    boolean re = true;
                    try {
                        re = dir.update(request);
                        if (re) {
                            response.sendRedirect("flow_predefine_left.jsp?toa=ok");
                        }
                    } catch (ErrMsgException e) {
            %>
            <script>
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?toa=ok&msg=<%=e.getMessage()%>";
            </script>
            <%
                }
                return;
            } else if (op.equals("move")) {
                try {
                    dir.move(request);
                    response.sendRedirect("flow_predefine_left.jsp?toa=ok");
                } catch (ErrMsgException e) {
            %>
            <script>
                window.parent.flowPredefineMainFrame.location.href = "flow_predefine_list.jsp?dirCode=<%=StrUtil.UrlEncode(dir.getDirCode())%>";
                window.location.href = "flow_predefine_left.jsp?toa=ok&msg=<%=e.getMessage()%>";
            </script>
            <%
                    }
                    return;
                }
                Leaf rootLeaf = dir.getLeaf(Leaf.CODE_ROOT);
                DirectoryView dv = new DirectoryView(rootLeaf);
                String jsonData = dv.getJsonString();
                ArrayList<String> list = dv.getAllUnused();
            %>
        </td>
    </tr>
</table>
</body>
<script>
    function bindClick() {
        $("a").bind("click", function () {
            $("a").css("color", "");
            $(this).css("color", "red");
        });
    }

    $(document).ready(function () {
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
            "ui": {"initially_select": ["root"]},
            <%
            License lic = License.getInstance();
            if (lic.isPlatformSrc()) {
            %>
            "plugins": ["wholerow", "dnd", "themes", "ui", "contextmenu", "types", "state"],
            <%
            }
            else {
            %>
            "plugins": ["wholerow", "dnd", "themes", "ui", "types", "state"],
            <%}%>
            "contextmenu": {	//绑定右击事件
                "items": {
                    "enableDebug": {
                        "label": "调试模式",
                        // "icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                        "action": function (data) {
                            inst = $.jstree.reference(data.reference);
                            node = inst.get_node(data.reference);
                            selectNodeId = node.id;
                            $.ajax({
                                type: "post",
                                url: "flow_predefine_left.jsp",
                                dataType: "json",
                                data: {
                                    op: "setDebug",
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
                                url: "flow_predefine_left.jsp",
                                dataType: "json",
                                data: {
                                    op: "setDebug",
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
        }).bind('click.jstree', function (e, data) {//绑定选中事件
            for (var i = 0; i < listCode.length; i++) {
                $("#" + listCode[i] + " a").first().css("color", "#999");
            }
            var eventNodeName = e.target.nodeName;
            if (eventNodeName == 'INS') {
                return;
            } else if (eventNodeName == 'A') {
                var $subject = $(e.target).parent();
                //选择的id值
                //alert($(e.target).parents('li').attr('id'));
                //alert($subject.text());
                var code = $(e.target).parents('li').attr('id');
                window.open("flow_predefine_list.jsp?dirCode=" + code, "flowPredefineMainFrame");
            }
        }).bind('move_node.jstree', function (e, data) {//绑定移动节点事件
            //data.node.id移动节点的id
            //data.parent移动后父节点的id
            //data.position移动后所在父节点的位置，第一个位置为0
            node = data.node;
            $.ajax({
                type: "post",
                url: "flow_predefine_left.jsp",
                dataType: "json",
                data: {
                    op: "move",
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
            myjsTree.jstree("deselect_all");
            myjsTree.jstree("select_node", "<%=Leaf.CODE_ROOT%>");
        });

        for (var i = 0; i < listCode.length; i++) {
            $("#" + listCode[i] + " a").first().css("color", "#999");
        }
        bindClick();
    });

</script>
</html>
