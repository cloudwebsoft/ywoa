<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@page import="com.redmoon.oa.pvg.Privilege" %>
<%
    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals("")) skincode = UserSet.defaultSkin;
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>文件柜-菜单</title>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/frame.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
    <script src="inc/common.js"></script>
	<script src="js/jquery-1.9.1.min.js"></script>
	<script src="js/jquery-migrate-1.2.1.min.js"></script>
    <script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="js/jquery.my.js"></script>
    <script src="js/jstree/jstree.js"></script>
    <link type="text/css" rel="stylesheet" href="js/jstree/themes/default/style.css"/>
    <style>
        #directoryTree {
            margin-top: 10px;
        }
    </style>
</head>
<body>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<div id="directoryTree"></div>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
<%
    String dirCode = ParamUtil.get(request, "dir_code");
    if (dirCode.equals("")) {
        dirCode = Leaf.ROOTCODE;
    }
    Leaf leaf = dir.getLeaf(dirCode);
    DirView tv = new DirView(request, leaf);
    String jsonData = tv.getJsonStringByUser(leaf, new Privilege().getUser(request));
    String op = ParamUtil.get(request, "op");
%>
<div style="margin:20px;text-align:center">
    <div style="display:inline-block;">
        <input type="button" class="btn" value="确定" onclick="sub('<%=op %>')"/>
    </div>
</div>
</body>
<script>
    //$(document).ready(bindClick);
    var code = 0;
    var user;
    $(document).ready(function () {
        $('#directoryTree').jstree({
            "core": {
                "data":  <%=jsonData%>,
                "themes": {
                    "theme": "default",
                    "dots": true,
                    "icons": true
                },
                "check_callback": true,
            },
            "plugins": ["wholerow", "themes", "ui", , "types", "state"],
        }).bind('click.jstree', function (e, data) {//绑定选中事件
            //alert(data.node.id);
            var eventNodeName = e.target.nodeName;
            if (eventNodeName == 'INS') {
                return;
            } else if (eventNodeName == 'A') {
                var $subject = $(e.target).parent();
                //选择的id值
                //alert($(e.target).parents('li').attr('id'));
                //alert($subject.text());
                code = $(e.target).parents('li').attr('id');
                user = $("#" + code).find("a").html();
                var a = user.indexOf("</I>");
                if (a < 0) {
                    a = user.indexOf("</i>")
                }
                user = user.substring(a + 4);
                //window.open("fileark_main.jsp?dir_code="+code,"mainFileFrame");
            }
        });
    });

    function sub(op) {
        window.opener.document.getElementById("directory").value = user;
        if (op == "search") {
            // window.opener.form1.action = "document_list_m.jsp?keywords=" + code;
            window.opener.o("dirCodeSearch").value = code;
        } else if (op == "changeDir") {
            window.opener.selectNode(code, name);
        } else {
            window.opener.addform.action = "fileark/operate.do?action=fckwebedit_new&dir_code=" + code;
        }
        window.close();
    }
</script>
</html>
