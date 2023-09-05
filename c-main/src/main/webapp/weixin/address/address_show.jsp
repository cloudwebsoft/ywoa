<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.Document" %>
<%@ page import="com.redmoon.oa.fileark.Attachment" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="org.htmlparser.Parser" %>
<%@ page import="org.htmlparser.filters.TagNameFilter" %>
<%@ page import="org.htmlparser.util.NodeList" %>
<%@ page import="org.htmlparser.tags.ImageTag" %>
<%@ page import="org.htmlparser.util.ParserException" %>
<%@ page import="com.redmoon.oa.address.AddressDb" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);

    int id = ParamUtil.getInt(request, "id", -1);
    if (id==-1) {
        out.print(StrUtil.p_center(SkinUtil.LoadString(request, "err_id")));
    }
    AddressDb addr = new AddressDb();
    addr = addr.getAddressDb(id);
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>通讯录</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta content="telephone=no" name="format-detection"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <link rel="stylesheet" href="../css/photoswipe.css">
    <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
    <script type="text/javascript" src="../js/photoswipe.js"></script>
    <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
    <script type="text/javascript" src="../js/photoswipe-init.js"></script>
    <style type="text/css">
        h5 {
            font-weight: bold;
            text-align: center;
            font-size: 16px;
            color: #000;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">通讯录</h1>
</header>
<div class="mui-content">
    <ul class="mui-table-view">
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">姓名</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getPerson()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">单位</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getCompany()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">职务</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getJob()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">手机</span></div>
                <div class="mui-table-cell mui-col-xs-10">
                    <span class="mui-h5 mui-pull-left"><%=addr.getMobile()%></span>
                    <%if (!"".equals(addr.getMobile())) {%>
                    <a href="tel:<%=addr.getMobile()%>" class="mui-icon mui-icon-phone mui-pull-right" style="font-size:20px"></a>
                    <%}%>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">Email</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getEmail()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">短号</span></div>
                <div class="mui-table-cell mui-col-xs-10">
                    <span class="mui-h5 mui-pull-left"><%=addr.getMSN()%></span>
                    <%if (!"".equals(addr.getMSN())) {%>
                    <a href="tel:<%=addr.getMSN()%>" class="mui-icon mui-icon-phone mui-pull-right" style="font-size:20px"></a>
                    <%}%>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">微信</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getWeixin()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">电话</span></div>
                <div class="mui-table-cell mui-col-xs-10">
                    <span class="mui-h5 mui-pull-left"><%=addr.getTel()%></span>
                    <%if (!"".equals(addr.getTel())) {%>
                    <a href="tel:<%=addr.getTel()%>" class="mui-icon mui-icon-phone mui-pull-right" style="font-size:20px"></a>
                    <%}%>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">传真</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getFax()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">QQ</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getQQ()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">网页</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getWeb()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">邮编</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getPostalcode()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">地址</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getAddress()%></span></div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-table">
                <div class="mui-table-cell mui-col-xs-2"><span class="mui-h5">附注</span></div>
                <div class="mui-table-cell mui-col-xs-10"><span class="mui-h5"><%=addr.getIntroduction()%></span></div>
            </div>
        </li>
    </ul>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../css/mui.css"></script>
<script type="text/javascript" src="../js/mui.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script>
    var isUniWebview = <%=isUniWebview%>;

    if(!mui.os.plus || isUniWebview) {        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    mui.init({
        keyEventBind: {
            backbutton: !isUniWebview //关闭back按键监听
        }
    });

    function callJS() {
        return {"btnAddShow": 0, "btnAddUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
