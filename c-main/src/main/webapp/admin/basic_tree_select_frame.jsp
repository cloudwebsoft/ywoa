<%@ page contentType="text/html; charset=utf-8" language="java" import="cn.js.fan.util.ErrMsgException" errorPage="" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.basic.SelectKindPriv" %>
<%@ page import="com.redmoon.oa.basic.TreeSelectDb" %>
<%@ page import="com.redmoon.oa.basic.TreeSelectMgr" %>
<%@ page import="com.redmoon.oa.basic.TreeSelectView" %>
<%@ page import="com.redmoon.oa.pvg.PrivDb" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta HTTP-EQUIV="pragma" CONTENT="no-cache"/>
    <meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate"/>
    <meta HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT"/>
    <title>树形基础数据管理</title>
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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dm" scope="page" class="com.redmoon.oa.basic.TreeSelectMgr"/>
<%
    String userName = privilege.getUser(request);
    int kind = ParamUtil.getInt(request, "kind", -1);
    SelectKindPriv skp = new SelectKindPriv();
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        if (skp.canUserAppend(userName, kind) || skp.canUserModify(userName, kind) || skp.canUserDel(userName, kind)) {
        } else {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String root_code = ParamUtil.get(request, "root_code");
    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    if (root_code.equals("")) {
        root_code = privilege.getUserUnitCode(request);
    }
%>
<Script>
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
    String root_description = leaf.getDescription();
    boolean isHome = false;

    Privilege pvg = new Privilege();
    String parent_code = ParamUtil.get(request, "parent_code");
    //String root_code = ParamUtil.get(request, "root_code");
    if (parent_code.equals("")) {
        parent_code = root_code;
    }

    TreeSelectMgr tsm = new TreeSelectMgr();
    TreeSelectDb tsd = tsm.getTreeSelectDb(root_code);
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
                    //DeptView dv = new DeptView(leaf);
                    TreeSelectView dv = new TreeSelectView(leaf);
                    //dv.listAjax(request, out, true);
                    String jsonData = dv.getJsonString();
                    List<String> list = dv.getAllUnit();

                %>
                <div id="officeequipmentTree"></div>
                <script>
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
                        myjsTree = $('#officeequipmentTree')
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
                                "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu", "types", "crrm", "state"],
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

                                                $.ajax({
                                                    type: "post",
                                                    url: "../basicdata/getNewNodeCode.do",
                                                    dataType: "json",
                                                    data: {
                                                        parent_code: code,
                                                        root_code: "<%=root_code %>"
                                                    },
                                                    success: function (data, status) {
                                                        if (data.ret == "1") {
                                                            $("#quipbottom").show();
                                                            $("#showParent").hide();
                                                            $('#span_code').html(data.msg);
                                                            $('#code').val(data.msg);
                                                            $('#name').val("");
                                                            $('#parent_code').val(code);
                                                            $('#parent_name').html(name);
                                                            $('#parentCode').val(code);
                                                            $('#op').val("AddChild");

                                                            $('#preCode').val('');
                                                            onChangePreCode();
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

                                                //window.location.href="officeequip_frame.jsp?flag1=1&op=AddChild&root_code=<%=StrUtil.UrlEncode(root_code)%>&parent_code="+code+"&parent_name="+name+"&number="+Math.random(),"dirbottomFrame";
                                                //window.open("officeequip_frame.jsp?op=AddChild&root_code=<%=StrUtil.UrlEncode(root_code)%>&parent_code="+code+"&parent_name="+name+"&number="+Math.random(),"dirbottomFrame");
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
                                                    url: "../basicdata/getParentNodeName.do",
                                                    dataType: "json",
                                                    data: {
                                                        parentCode: node.parent,
                                                        code: code
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
                                                            $('#isOpen').val(data.isOpen?1:0);
                                                            $('#isContextMenu').val(data.isContextMenu?1:0);
                                                            $('#description').val(data.description);
                                                            $('#metaData').val(data.metaData);

                                                            // console.log(data);
                                                            onChangePreCode();
                                                        }
                                                    },
                                                    complete: function (XMLHttpRequest, status) {
                                                    },
                                                    error: function (XMLHttpRequest, textStatus) {
                                                        jAlert(XMLHttpRequest.responseText, "提示");
                                                    }
                                                });

                                                //window.location.href="officeequip_frame.jsp?flag1=1&op=modify&root_code=<%=StrUtil.UrlEncode(root_code)%>&code="+code+"&parent_name="+name+"&number="+Math.random(),"dirbottomFrame";
                                            }
                                        },
                                        "remove": {
                                            "label": "删除",
                                            "icon": "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
                                            "action": function (data) {
                                                var inst = $.jstree.reference(data.reference);
                                                var obj = inst.get_node(data.reference);
                                                var code = obj.id;
                                                if ("<%=root_code %>" == code) {
                                                    jAlert("根节点不能被删除!", "提示");
                                                    return;
                                                }
                                                jConfirm('您确定要删除吗?', '提示', function (r) {
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
                                                                $.toaster({priority: 'info', message: '操作成功'});
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
                            }).bind('select_node.jstree', function (e, data) {//绑定选中事件
                            }).bind('click.jstree', function (event) {
                                $('#quipbottom').hide();
                            });
                        $.toaster({priority: 'info', message: '右键菜单可管理，菜单项可拖动'});
                    });

                    function addNewNode(myId, myText) {
                        if (code == undefined) {
                            code = "root";
                        }
                        myjsTree.jstree('create_node', code + "", {'id': myId + "", 'text': myText + ""}, 'last');
                        if (code != '<%=root_code%>') {
                            myjsTree.jstree("toggle_node", code);
                        }
                        myjsTree.jstree("deselect_all");
                        myjsTree.jstree("select_node", myId);
                    }

                    function modifyTitle(name) {
                        inst.set_text(node, name, "zh");
                        myjsTree.jstree("deselect_all");
                        myjsTree.jstree("select_node", node.id);
                        for (var i = 0; i < listCode.length; i++) {
                            if (listCode[i] == node.id + "") {
                                listCode.splice(i, 1);
                                break;
                            }
                        }
                    }
                </script>
            </TD>
        </TR>
        </tbody>
    </table>
</div>
<div id="quipbottom" style="width: 100%; position:absolute; bottom:0; left:0; z-index: 100; background-color: white">
    <table cellspacing="0" cellpadding="0" width="100%">
        <tbody>
        <tr>
            <td class="tdStyle_1"><span class="thead" style="PADDING-LEFT: 10px"><%=rootName %>增加或修改</span></td>
        </tr>
        </tbody>
    </table>
    <form action="" method="post" name="form1" target="dirhidFrame" id="form1" onsubmit="return form1_onsubmit()">
        <table width="434" align="center" class="tabStyle_1 percent80">
            <tr>
                <td width="120" rowspan="9" align="left" valign="top" style="word-break:break-all"><br/>
                    当前节点：<br/>
                    <font color="blue" id="parent_name"></font></td>
                <td align="left" id='codeText'>编码&nbsp;<span id="span_code"></span><input type="hidden" name="code" id="code"/></td>
            </tr>
            <tr>
                <td align="left">名称
                    <input type="hidden" name="op" id="op"/>
                    <input name="name" id="name" size=12/>
                    <input type="hidden" name="parent_code" id="parent_code"/>
                    <input type="hidden" name="root_code" id="root_code"/></td>
            </tr>
            <tr>
                <td align="left">
                    启用
                    <select id="isOpen" name="isOpen">
                        <option value="1" selected>启用</option>
                        <option value="0">停用</option>
                    </select>
                    &nbsp;&nbsp;显示右键菜单
                    <select id="isContextMenu" name="isContextMenu">
                        <option value="1" selected>是</option>
                        <option value="0">否</option>
                    </select>
                </td>
            </tr>
            <tr>
            <td align="left">
                链接
                <input id="link" name="link" value=""/>
            </td>
        </tr>
            <tr>
                <td align="left">
                    类型
                    <select id="preCode" name="preCode" onchange="onChangePreCode()">
                        <option value="">
                            无
                        </option>
                        <option value="<%=TreeSelectDb.PRE_CODE_FLOW %>">流程</option>
                        <option value="<%=TreeSelectDb.PRE_CODE_MODULE %>">模块</option>
                    </select>
                    <span id="spanModule">
                        <select id="formCode" name="formCode" onchange="o('preCode').value='module';onChangePreCode()">
                        <option value="">选择智能模块</option>
                        <%
                            com.redmoon.oa.visual.ModuleSetupDb msd = new com.redmoon.oa.visual.ModuleSetupDb();
                            Iterator mir = msd.listUsed().iterator();
                            while (mir.hasNext()) {
                                msd = (com.redmoon.oa.visual.ModuleSetupDb) mir.next();
                        %>
                          <option value="<%=msd.getString("code")%>"><%=msd.getString("name")%></option>
                        <%
                            }
                        %>
                        </select>
                        <span id="moduleBasicTreeLink" style="display: none">
                            <a href="javascript:;" onclick="addTab('基础数据维护', 'visual/module_basic_tree_frame.jsp?basicCode=<%=root_code%>')">维护</a>
                        </span>
                    </span>
                    <span id="spanFlow">
                    <select id="flowTypeCode" name="flowTypeCode" onchange="if (this.value=='not') {jAlert('请选择流程类型！','提示'); return;} o('preCode').value='flow'; onChangePreCode()">
                    <%
                        com.redmoon.oa.flow.Leaf flowrootlf = new com.redmoon.oa.flow.Leaf();
                        flowrootlf = flowrootlf.getLeaf(com.redmoon.oa.flow.Leaf.CODE_ROOT);
                        if (flowrootlf != null) {
                            com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(flowrootlf);
                            flowdv.ShowDirectoryAsOptions(request, out, flowrootlf, flowrootlf.getLayer());
                        }
                    %>
                    </select>
                    </span>
                    <br/>
                    （注：如根节点选择了“树形结构节点描述”模块，则子节点的设置将被忽略）
                </td>
            </tr>
            <tr>
                <td>
                    <span id="showParent" align="left" style="display:none">
                     父节点
                    <input id="parentCode" name="parentCode" type="hidden" value=""/>
                    <input id="flag" name="flag" type="hidden" value=""/>
                    <span id="parentName"></span>
                    &nbsp;&nbsp;<a href="javascript:;" onclick="window.open('basic_tree_sel.jsp?root_code=<%=root_code %>','_blank','toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=640,height=480')">选择</a>
                    </span>
                </td>
            </tr>
            <tr>
                <td align="left" colspan="2">
                    描述
                    <input id="description" name="description"/>
                </td>
            </tr>
            <tr>
                <td align="left" colspan="2">
                    数据
                    <input id="metaData" name="metaData"/>
                </td>
            </tr>
            <tr>
                <td align="center" colspan="2">
                    <input type="button" class="btn" onclick="mysubmit()" value="确定"/>&nbsp;&nbsp;&nbsp;<input type="button" class="btn" title="隐藏面板" onclick="hideBottom()" value="隐藏"/>
                </td>
            </tr>
        </table>
    </form>
</div>
<script>
    $(function () {
        if ($('#op').val()=="modify") {
            if ($('#preCode').val()=='<%=TreeSelectDb.PRE_CODE_MODULE %>') {
                if ($('#formCode').val() == '<%=ConstUtil.BASIC_TREE_NODE%>') {
                   $('#moduleBasicTreeLink').show();
                }
            }
        }

        $('#formCode').change(function() {
            if ($('#op').val()=="modify") {
                if ($(this).val() == '<%=ConstUtil.BASIC_TREE_NODE%>') {
                    $('#moduleBasicTreeLink').show();
                }
                else {
                    $('#moduleBasicTreeLink').hide();
                }
            }
        });

        $('#formCode').select2();
        $('#quipbottom').hide();
        $('#spanFlow').hide();
        $('#spanModule').hide();
    });

    function mysubmit() {
        o("root_code").value = getRootCode();
        var url;
        if ("AddChild" == $("#op").val()) {
            url = "../basicdata/createNode.do"
        }
        else if ("modify" == $("#op").val()) {
            url = "../basicdata/updateNode.do";
        }
        $.ajax({
            url: url,
            data: $('#form1').serialize(),
            type: "post",
            dataType: "json",
            success: function (data, status) {
                if (data.ret == 1) {
                    $("#quipbottom").hide();
                    if ("modify" == $("#op").val()) {
                        modifyTitle($("#name").val());
                        jAlert_Redirect(data.msg, "提示", "basic_tree_select_frame.jsp?root_code=<%=root_code%>");
                    } else if ("AddChild" == $("#op").val()) {
                        addNewNode($("#code").val(), $("#name").val());
                        $.toaster({priority: 'info', message: '操作成功'});
                    }
                } else {
                    $.toaster({priority: 'info', message: data.msg});
                }
            },
            error: function (XMLHttpRequest, textStatus) {
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    function hideBottom() {
        $('#quipbottom').hide();
    }

    function onChangePreCode() {
        if (o("preCode").value == "") {
            o("link").disabled = false;
            $('#spanFlow').hide();
            $('#spanModule').hide();
            $('#moduleBasicTreeLink').hide();
        } else if (o("preCode").value == "flow") {
            o("link").value = "";
            o("link").disabled = true;
            $('#spanFlow').show();
            $('#spanModule').hide();
            $('#moduleBasicTreeLink').hide();
        } else if (o("preCode").value == "module") {
            o("link").value = "";
            o("link").disabled = true;
            $('#spanFlow').hide();
            $('#spanModule').show();
        }
    }
</script>
</body>
</html>
