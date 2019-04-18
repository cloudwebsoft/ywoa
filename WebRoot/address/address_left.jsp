<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.address.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="org.json.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String strtype = ParamUtil.get(request, "type");
    int type = AddressDb.TYPE_USER;
    if (!strtype.equals(""))
        type = Integer.parseInt(strtype);
    String mode = ParamUtil.get(request, "mode");
    if (!mode.equals("show")) {
        if (type == AddressDb.TYPE_PUBLIC) {
            if (!privilege.isUserPrivValid(request, "admin.address.public")) {
                out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
                return;
            }
        }
    }

    String userName = privilege.getUser(request);
    if (type == AddressDb.TYPE_PUBLIC)
        userName = AddressTypeDb.PUBLIC;
    String unitCode = privilege.getUserUnitCode(request);
    if (type == AddressDb.TYPE_PUBLIC) {
        unitCode = Leaf.CODE_ROOT;
    }

    String root_code = ParamUtil.get(request, "root_code");
    if (root_code.equals("")) {
        root_code = userName;
    }

    String op = ParamUtil.get(request, "op");
    if (op.equals("modify")) {
        boolean re = true;
        try {
            Directory dir = new Directory();
            re = dir.update(request);
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
        if (re) {
            String code = ParamUtil.get(request, "code");
%>
<script>
    window.parent.mainFileFrame.location.href = "dir_prop.jsp?op=modify&code=<%=StrUtil.UrlEncode(code)%>";
</script>
<%
            out.print(StrUtil.jAlert_Redirect("修改完成！", "提示", "address_left.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
        }
        return;
    } else if (op.equals("repair")) {
        Directory dir = new Directory();
        dir.repairTree(dir.getLeaf(root_code));
        out.print(StrUtil.jAlert_Redirect("操作完成！", "提示", "address_left.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
        return;
    }

    if (op.equals("changeName")) {
        String newName = URLDecoder.decode(ParamUtil.get(request, "newName"), "utf-8");
        String dirCode = ParamUtil.get(request, "dirCode");
        Directory dir = new Directory();
        Leaf lf = dir.getLeaf(dirCode);
        Leaf plf = dir.getLeaf(lf.getParentCode());
        // 如果是根节点的父节点，则为null
        if (plf != null) {
            java.util.Iterator ir = plf.getChildren().iterator();
            boolean isFound = false;
            while (ir.hasNext()) {
                Leaf lf2 = (Leaf) ir.next();
                if (lf2.getName().equals(newName)) {
                    isFound = true;
                    break;
                }
            }
            if (isFound) {
                out.print(StrUtil.jAlert_Back("指定的名称有重复！", "提示"));
                return;
            }
        }
        lf.rename(newName);
        //response.sendRedirect("address_left.jsp?root_code=" + root_code + "&type=" + type);
        return;
    } else if (op.equals("del")) {
        String delcode = ParamUtil.get(request, "dirCode");
        UserMgr um = new UserMgr();
        UserDb ud = um.getUserDb(privilege.getUser(request));
        if (delcode.equals("" + ud.getName())) {
            out.print(StrUtil.jAlert("根节点不能被删除", "提示"));
        } else {
            Directory dir = new Directory();
            Leaf lf = dir.getLeaf(delcode);
            if (lf != null) { // 防止反复刷新
                try {
                    dir.del(delcode);
                } catch (ErrMsgException e) {
                    out.print(StrUtil.jAlert(e.getMessage(), "提示"));
                }
                //response.sendRedirect("address_left.jsp?root_code=" + root_code + "&type=" + type);
                return;
            }
        }
    } else if (op.equals("AddChild")) {
        JSONObject json = new JSONObject();
        boolean re = false;
        Directory dir = new Directory();
        String autoCode = Leaf.getAutoCode();
        request.setAttribute("code", autoCode);
        try {
            re = dir.AddChild(request);
            if (re) {
                //response.sendRedirect("address_left.jsp?root_code=" + StrUtil.UrlEncode(root_code) + "&type=" + type);
                json.put("ret", "0");
                json.put("msg", autoCode);
                out.print(json.toString());
                return;
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }
        if (!re) {
            out.print(StrUtil.jAlert_Back("添加节点失败，请检查编码是否重复！", "提示"));
            return;
        }
    } else if (op.equals("move")) {
        JSONObject json = new JSONObject();
        String code = ParamUtil.get(request, "code");
        String parent_code = ParamUtil.get(request, "parent_code");
        String rootCode = ParamUtil.get(request, "root_code");
        ;
        int position = Integer.parseInt(ParamUtil.get(request, "position"));
        if (rootCode.equals(code)) {
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
        String old_parent_code = moveleaf.getParentCode();
        Leaf newParentLeaf = dir.getLeaf(parent_code);
        if (!newParentLeaf.getCode().equals(old_parent_code)) {
            json.put("ret", "0");
            json.put("msg", "只能在同级目录之间移动！");
            out.print(json.toString());
            return;
        }

        moveleaf.setParentCode(parent_code);
        int p = position + 1;
        moveleaf.setOrders(p);
        moveleaf.update();

        Iterator ir = newParentLeaf.getChildren().iterator();
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            // 跳过自己
            if (lf.getCode().equals(code)) {
                continue;
            }

            if (lf.getOrders() >= p) {
                lf.setOrders(lf.getOrders() + 1);
                lf.update();
            }
        }

        // 原节点下的孩子节点通过修复repairTree处理
        Leaf rootLeafDb = dir.getLeaf(rootCode);
        Directory dm = new Directory();
        dm.repairTree(rootLeafDb);

        rootLeafDb.removeAllFromCache();


        json.put("ret", "1");
        json.put("msg", "移动成功！");
        out.print(json.toString());
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=8">
    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/jstree/themes/address/style.css"/>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
    <script src="<%=request.getContextPath() %>/js/jstree/jstree.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>
    <title>通讯录目录</title>
    <style>
        <!--
        /* Context menu Script- © Dynamic Drive (www.dynamicdrive.com) Last updated: 01/08/22
        For full source code and Terms Of Use, visit http://www.dynamicdrive.com */
        body {
            margin-left: 0px;
            margin-right: 0px;
            margin-bottom: 0px;
            overflow: auto;
        }

        td {
            font-size: 12px;
        }

        .skin0 {
            padding-top: 2px;
            cursor: default;
            position: absolute;
            text-align: left;
            font-family: "宋体";
            font-size: 9pt;
            width: 80px; /*宽度，可以根据实际的菜单项目名称的长度进行适当地调整*/
            background-color: menu; /*菜单的背景颜色方案，这里选择了系统默认的菜单颜色*/
            visibility: hidden; /*初始时，设置为不可见*/
        }

        /*定义菜单条的显示样式*/
        .menuitems {
            padding: 2px 1px 2px 10px;
        }

        -->

    </style>
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
            if (tableobj == null)
                return;
            if (tableobj.style.display == "none") {
                tableobj.style.display = "";
                if (imgobj.src.indexOf("i_puls-root-1.gif") != -1)
                    imgobj.src = "images/i_puls-root.gif";
                if (imgobj.src.indexOf("i_plus-1-1.gif") != -1)
                    imgobj.src = "images/i_plus2-2.gif";
                if (imgobj.src.indexOf("i_plus-1.gif") != -1)
                    imgobj.src = "images/i_plus2-1.gif";
            }
            else {
                tableobj.style.display = "none";
                if (imgobj.src.indexOf("i_puls-root.gif") != -1)
                    imgobj.src = "images/i_puls-root-1.gif";
                if (imgobj.src.indexOf("i_plus2-2.gif") != -1)
                    imgobj.src = "images/i_plus-1-1.gif";
                if (imgobj.src.indexOf("i_plus2-1.gif") != -1)
                    imgobj.src = "images/i_plus-1.gif";
            }
        }

        var curDirCode = "";
        var curDirName = "";

        var curLeftClickDirCodeOld = "";
        var curLeftClickDirCode = "";
        var oldsrc = "";

        function onMouseUp(dirCode, dirName) {

            var event = window.event || arguments.callee.caller.arguments[0];

            if (event.button < 2) {
                // 点击左键时切换folder图片
                curLeftClickDirCode = dirCode;
                if (curLeftClickDirCodeOld != curLeftClickDirCode) {
                    var curImgObj = findObj("img" + curLeftClickDirCode);
                    var oldImgObj = findObj("img" + curLeftClickDirCodeOld);
                    if (oldImgObj != null) {
                        oldImgObj.src = oldsrc; // "images/folder_01.gif";
                    }
                    oldsrc = curImgObj.src;

                    if (curImgObj.src.indexOf("images/folder_share.gif") == -1)
                        curImgObj.src = "images/folder_open.gif";
                    else
                        curImgObj.src = "images/folder_share_open.gif";

                    curLeftClickDirCodeOld = curLeftClickDirCode;
                }
            }
            if (event.button == 2) {
                curDirCode = dirCode;
                curDirName = dirName;
            }

        }

        var spanInnerHTML = "";
        function changeName() {
            if (curDirCode != "") {
                // alert(curDirCode);
                spanObj = findObj("span" + curDirCode);
                spanInnerHTML = spanObj.innerHTML;
                spanObj.innerHTML = "<input name='newName' class=singleboarder size=10 value='" + curDirName + "' onblur=\"doChange('" + curDirCode + "',this,'" + curDirName + "'," + spanObj.name + ")\" onKeyDown=\"if (event.keyCode==13) this.blur()\">";
                newName.focus();
                newName.select();
                menuobj.style.visibility = "hidden";
            }
        }

        function doChange(dirCode, newName, oldName, spanObj) {
            var len = newName.value.length;
            if (newName.value == "") {
                jAlert("目录名称不能为空！", "提示");
                return;
            }
            if (len > 15) {
                jAlert("目录名称不能超过15个字符！", "提示");
                return;
            }
            if (newName.value != oldName) {
                form10.op.value = "changeName";
                form10.dirCode.value = dirCode;
                form10.newName.value = newName.value;
                form10.root_code.value = "<%=root_code%>";
                form10.submit();
                // 下句发过去会有中文问题
                // window.location.href="?op=changeName&dirCode=" + dirCode + "&newName=" + newName + "&root_code=<%=StrUtil.UrlEncode(root_code)%>";
                // alert(window.location.href);
            }
            else {
                if (spanObj)
                    spanObj.innerHTML = spanInnerHTML;
            }
            curDirCode = "";
        }

        function del() {
            if (curDirCode != "") {
                if (curDirCode == "<%=root_code%>") {
                    jAlert("您不能删除根节点！", "提示");
                    return;
                }
                jConfirm("您确定要删除" + curDirName + "吗？", "提示", function (r) {
                    if (!r) {
                        return;
                    }
                    else {
                        window.location.href = "?op=del&type=<%=type%>&dirCode=" + curDirCode + "&root_code=<%=StrUtil.UrlEncode(root_code)%>";
                    }
                })
                curDirCode = "";
            }
        }

        function create() {
            if (curDirCode != "") {
                window.location.href = "address_left.jsp?op=AddChild&type=<%=type%>&parent_code=" + curDirCode + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&code=<%=Leaf.getAutoCode()%>&name=<%=StrUtil.UrlEncode("新建分类")%>";
                curDirCode = "";
            }
        }

        function move(towhere) {
            if (curDirCode != "") {
                window.location.href = "?op=move&type=<%=type%>&direction=" + towhere + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&code=" + curDirCode;
                curDirCode = "";
            }
        }
    </script>
</head>
<body onLoad="window_onload()">
<%
    Directory dir = new Directory();
    Leaf leaf = dir.getLeaf(root_code);
    if (leaf == null) {
        leaf = new Leaf();
        leaf.initRootOfUser(root_code, unitCode);
    }
    DirectoryView tv = new DirectoryView(leaf);
//tv.ListSimple(out, "mainAddressFrame", "address.jsp", "type=" + type + "&mode=" + mode, "", "" ); // "tbg1", "tbg1sel");
    String jsonData = tv.getJsonString();
%>
<table width="100%" border="0" style="display:none">
    <tr>
        <td width="5" align="left">&nbsp;</td>
        <td align="left">
        </td>
    </tr>
</table>
<table width="100%" border="0" cellpadding="0" cellspacing="0" style="display:none">
    <form name=form10 action="?">
        <tr>
            <td>&nbsp;
                <input name="op" type="hidden">
                <input name="dirCode" type="hidden" value="">
                <input name="newName" type="hidden">
                <input name="root_code" type="hidden">
                <input name="type" type="hidden" value="<%=type%>"/>
            </td>
        </tr>
    </form>
</table>
<div id="ie5menu" class="skin0" onMouseover="highlightie5(event)" onMouseout="lowlightie5(event)"
     onClick="jumptoie5(event)" style="display:none">
    <div class="menuitems" url="javascript:create()">新建</div>
    <div class="menuitems" url="javascript:changeName()">重命名</div>
    <div class="menuitems" url="javascript:del()">删除</div>
    <div class="menuitems" url="javascript:move('up')">上移</div>
    <div class="menuitems" url="javascript:move('down')">下移</div>
    <hr>
    <div class="menuitems" url="address_left.jsp?root_code=<%=StrUtil.UrlEncode(root_code)%>">刷新</div>
</div>
<div id="directoryTree"></div>
<script>
    var myjsTree;
    $(function () {
        myjsTree = $('#directoryTree').jstree({
            "core": {
                "data":  <%=jsonData%>,
                "themes": {
                    "theme": "default",
                    "dots": true,
                    "icons": true
                },
                "attr": {"icon": {"image": "images/folder_share.gif"}},
                "check_callback": true
            },
            <%if (type==AddressDb.TYPE_PUBLIC && !privilege.isUserPrivValid(request, "admin.address.public")) {%>
            "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "types", "crrm", "state"],
            <%}else{%>
            "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu", "types", "crrm", "state"],
            <%}%>
            "contextmenu": {	//绑定右击事件
                "items": {
                    "create": {
                        "label": "新建",
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
                            var inst = $.jstree.reference(data.reference),
                                    obj = inst.get_node(data.reference);
                            var code = obj.id;
                            if ("<%=root_code%>" == code) {
                                jAlert("根节点不能被删除!", "提示");
                                return;
                            }
                            jConfirm('您确定要删除该节点吗?', '提示', function (r) {
                                if (!r) {
                                    return;
                                }
                                else {
                                    $.ajax({
                                        type: "post",
                                        url: "address_left.jsp",
                                        dataType: "html",
                                        data: {
                                            op: "del",
                                            type: "<%=type%>",
                                            dirCode: code + "",
                                            root_code: "<%=StrUtil.UrlEncode(root_code)%>"
                                        },
                                        success: function (datas, status) {
                                            inst.delete_node(obj);
                                            window.open("address.jsp?dir_code=<%=StrUtil.UrlEncode(root_code)%>&type=" + "<%=type%>" + "&mode=" + "<%=mode%>", "mainAddressFrame");
                                        },
                                        error: function (XMLHttpRequest, textStatus) {
                                            jAlert("删除失败", "提示");
                                        }
                                    });
                                }
                            })
                        }
                    }
                }
            }
        }).bind('select_node.jstree', function (e, data) {//绑定选中事件
            window.open("address.jsp?dir_code=" + data.node.id + "&type=" + "<%=type%>" + "&mode=" + "<%=mode%>", "mainAddressFrame");
        }).bind("rename_node.jstree", function (e, data) {
            if (data.node.text.indexOf("\"") > 0 || data.node.text.indexOf("'") > 0) {
                jAlert("名称不能含有单引号、双引号字符", "提示");
                window.location.reload();
                return;
            }
            var len = data.node.text.length;
            if (len > 15) {
                jAlert("目录名称不能超过15个字符！", "提示");
                window.location.reload();
                return;
            }
            var dirCodeId;
            var name = data.node.text;
            if (form10.dirCode.value != "") {
                dirCodeId = form10.dirCode.value;
            } else {
                dirCodeId = data.node.id + "";
            }

            $.ajax({
                type: "post",
                url: "address_left.jsp",
                dataType: "html",
                data: {
                    op: "changeName",
                    dirCode: dirCodeId,
                    newName: encodeURI(name, "UTF-8"),
                    root_code: "<%=StrUtil.UrlEncode(root_code)%>"
                },
                success: function (datas, status) {
                },
                complete: function (XMLHttpRequest, status) {
                    window.location.reload();
                },
                error: function (XMLHttpRequest, textStatus) {
                    jAlert("重命名失败！", "提示");
                }
            });

            form10.dirCode.value = "";
        }).bind("create_node.jstree", function (e, data) {
            $.ajax({
                type: "post",
                url: "address_left.jsp",
                dataType: "json",
                data: {
                    op: "AddChild",
                    type: "<%=type%>",
                    mode: "<%=mode%>",
                    parent_code: encodeURI(data.node.parent),
                    root_code: "<%=StrUtil.UrlEncode(root_code)%>",
                    name: data.node.text + ""
                },
                success: function (datas, status) {
                    if (datas.ret == 0) {//datas.msg是新建节点的id
                        form10.dirCode.value = datas.msg;
                    }
                },
                error: function (XMLHttpRequest, textStatus) {
                    jAlert("新建失败！", "提示");
                    window.location.reload();
                }
            });
        }).bind('move_node.jstree', function (e, data) {//绑定移动节点事件
            $.ajax({
                type: "post",
                url: "address_left.jsp",
                dataType: "json",
                data: {
                    op: "move",
                    code: data.node.id + "",
                    parent_code: data.parent + "",
                    position: data.position + "",
                    root_code: "<%=StrUtil.UrlEncode(root_code)%>"
                },
                success: function (data, status) {
                    if (datas.ret == 0) {
                        jAlert(datas.msg, "提示");
                        window.location.reload(true);
                    }
                },
                error: function (XMLHttpRequest, textStatus) {
                    jAlert("移动失败！", "提示");
                    window.location.reload(true);
                }
            });
        });
    });


    //set this variable to 1 if you wish the URLs of the highlighted menu to be displayed in the status bar
    var display_url = 0;

    var ie5 = document.all && document.getElementById;
    var ns6 = document.getElementById && !document.all;
    if (ie5 || ns6)
        var menuobj = document.getElementById("ie5menu")

    function showmenuie5(e) {
        if (curDirCode == "")
            return;
        //Find out how close the mouse is to the corner of the window
        var rightedge = ie5 ? document.body.clientWidth - event.clientX : window.innerWidth - e.clientX;
        var bottomedge = ie5 ? document.body.clientHeight - event.clientY : window.innerHeight - e.clientY;

        //if the horizontal distance isn't enough to accomodate the width of the context menu

        if (rightedge < menuobj.offsetWidth) {
            //move the horizontal position of the menu to the left by it's width
            menuobj.style.left = ie5 ? document.body.scrollLeft + event.clientX - menuobj.offsetWidth + "px" : window.pageXOffset + e.clientX - menuobj.offsetWidth + "px";
        }
        else {
            //position the horizontal position of the menu where the mouse was clicked
            menuobj.style.left = ie5 ? document.body.scrollLeft + event.clientX + "px" : window.pageXOffset + e.clientX + "px";
        }
        //same concept with the vertical position
        if (bottomedge < menuobj.offsetHeight)
            menuobj.style.top = ie5 ? document.body.scrollTop + event.clientY - menuobj.offsetHeight + "px" : window.pageYOffset + e.clientY - menuobj.offsetHeight + "px";
        else
            menuobj.style.top = ie5 ? document.body.scrollTop + event.clientY + "px" : window.pageYOffset + e.clientY + "px";
        menuobj.style.visibility = "visible";
        return false;
    }

    function hidemenuie5(e) {
        menuobj.style.visibility = "hidden";
    }

    function highlightie5(e) {
        var firingobj = ie5 ? event.srcElement : e.target
        if (firingobj.className == "menuitems" || ns6 && firingobj.parentNode.className == "menuitems") {
            if (ns6 && firingobj.parentNode.className == "menuitems") firingobj = firingobj.parentNode //up one node
            firingobj.style.backgroundColor = "highlight"
            firingobj.style.color = "white"
            if (display_url == 1)
                window.status = event.srcElement.url
        }
    }

    function lowlightie5(e) {
        var firingobj = ie5 ? event.srcElement : e.target
        if (firingobj.className == "menuitems" || ns6 && firingobj.parentNode.className == "menuitems") {
            if (ns6 && firingobj.parentNode.className == "menuitems") firingobj = firingobj.parentNode //up one node
            firingobj.style.backgroundColor = ""
            firingobj.style.color = "black"
            window.status = ''
        }
    }

    function jumptoie5(e) {
        var firingobj = ie5 ? event.srcElement : e.target
        if (firingobj.className == "menuitems" || ns6 && firingobj.parentNode.className == "menuitems") {
            if (ns6 && firingobj.parentNode.className == "menuitems") firingobj = firingobj.parentNode
            if (firingobj.getAttribute("target"))
                window.open(firingobj.getAttribute("url"), firingobj.getAttribute("target"))
            else
                window.location = firingobj.getAttribute("url")
        }
    }
    if (ie5 || ns6) {
        menuobj.style.display = '';
        document.oncontextmenu = showmenuie5;
        document.onclick = hidemenuie5;
    }

    function handlerOnClick() {
        var obj = window.event.srcElement;
        jAlert(obj.type, "提示");
    }

    function window_onload() {
        // window.document.body.onclick = handlerOnClick;
    }

    function bindClick() {
        $("a").bind("click", function () {
            $("a").css("color", "");
            $(this).css("color", "red");
        });
    }
    $(document).ready(bindClick);
    $.toaster({priority: 'info', message: '请点击鼠标右键管理目录'});
</script>
</body>
</html>
