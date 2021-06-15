<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.fileark.Leaf" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>文件柜</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">

    <link rel="Stylesheet" href="../js/jqm-tree/css/jquery.mobile-1.4.2.min.css"/>
    <script src="../js/jqm-tree/js/jquery-1.11.0.min.js"></script>
    <script src="../js/jqm-tree/js/jquery.mobile-1.4.2.min.js"></script>
    <script src="../js/jqm-tree/js/jqm-tree.js"></script>
</head>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">文件柜</h1>
</header>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
%>
<body>
<div class="mui-content">
    <div id="divContent" class="jqm-tree-dir" data-role="content">
        <div id="tree"></div>
    </div>
</div>

<script type="text/javascript" src="../js/mui.min.js"></script>
<script>
    if(!mui.os.plus) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    var skey = '<%=skey%>';

    $("#tree").jqmtree({
        title: '文件柜',
        collapsed: false,
        data: [
/*            {"id": 1, "title": "item1"},
            {"id": 2, "title": "item1_1", "pid": 1},
            {"id": 3, "title": "item1_2", "pid": 1},
            {"id": 4, "title": "item2", "pid": 0},
            {"id": 5, "title": "item3", "pid": 0},
            {"id": 6, "title": "item1_2_1", "pid": 3}*/
                <%
                Vector v = new Vector();
                Leaf lf = new Leaf();
                lf = lf.getLeaf(Leaf.ROOTCODE);
                int rootId = lf.getId();
                lf.getAllChild(v, lf);
                int c = v.size();
                Iterator ir = v.iterator();
                int k = 0;
                Leaf plf = new Leaf();
                while (ir.hasNext()) {
                    lf = (Leaf)ir.next();
                    // 根节点不显示
                    if (lf.getParentCode().equals("-1")) {
                        continue;
                    }
                    plf = plf.getLeaf(lf.getParentCode());
                    int pid = plf.getId();
                    // 根目录下面的节点的pid为0方可显示
                    if (pid==rootId)
                        pid = 0;
                    %>
                    {"id": "<%=lf.getId()%>", "title": "<%=lf.getName()%>", "dirCode":"<%=lf.getCode()%>", "pid": "<%=pid%>"}
                    <%
                    k++;
                    if (c!=k) {
                        out.print(",");
                    }
                }
                %>
        ]
    });

    $(function () {
        // 用mui的tap事件，会导致无法展开目录
        $(".ui-btn").on("tap", function(e) {
            var dirCode = $(this).attr("dirCode");
            if (dirCode) {
                window.location.href = "doc_list.jsp?dirCode=" + encodeURI(dirCode);
            }
        });
    })

    function callJS() {
        return {"btnAddShow": 0, "btnAddUrl": "", "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"", "btnBackUrl":"" }';
</script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBtnAddShow" value="false"/>
    <jsp:param name="barBtnAddUrl" value="doc_add.jsp"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
