<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.menu.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>目录</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/jstree/themes/default/style.css"/>
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
    <script src="../inc/common.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
    <script src="<%=request.getContextPath() %>/js/jstree/jstree.js"></script>
</head>
<body>
<%
    long portalId = ParamUtil.getLong(request, "portalId");
    String root_code = ParamUtil.get(request, "root_code");
    if (root_code.equals("")) {
        root_code = Leaf.CODE_ROOT;
    }
    String tabId = ParamUtil.get(request, "tabId");
    Leaf leaf = new Leaf();
    leaf = leaf.getLeaf(root_code);
    DirectoryView dv = new DirectoryView(request, leaf);
    String jsonData = dv.getJsonString();
%>
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
                    "data":
                    <%=jsonData%>,
                    "themes": {
                        "theme": "default",
                        "dots": true,
                        "icons": true
                    },
                    "check_callback": true,
                },
                "ui": {"initially_select": ["root"]},
                "plugins": ["unique", "dnd", "wholerow", "themes", "ui", "types", "crrm"]
            }).bind('select_node.jstree', function (e, data) { //绑定选中事件
                if (data.event) {
                    // 点击了鼠标右键
                    if (data.event.button == 2) {
                        return;
                    }
                }

                node = data.node;
                selectNodeName = data.node.text;
                selectNodeId = data.node.id;
                window.parent.mainFileFrame.location.href = "portal_menu_main.jsp?tabId=<%=tabId%>&portalId=<%=portalId%>&code=" + selectNodeId;
            }).bind('click.jstree', function (event) {

            });
    });
</script>
</html>
