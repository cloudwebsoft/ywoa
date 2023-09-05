<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.oa.flow.macroctl.*" %>
<%@page import="com.redmoon.oa.flow.FormDb" %>
<%@page import="com.redmoon.oa.flow.FormField" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%@page import="com.redmoon.oa.android.*" %>
<%@ page import="cn.js.fan.util.RandomSecquenceCreator" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="org.json.JSONObject" %>
<%
    String skey = ParamUtil.get(request, "skey");
    long id = ParamUtil.getInt(request, "id", 0);
    String moduleCode = ParamUtil.get(request, "moduleCode");
    boolean isOnlyCamera = false;
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(moduleCode);
    if (msd == null) {
        out.print("模块不存在！");
        return;
    }
    FormDb fd = new FormDb();
    fd = fd.getFormDb(msd.getString("form_code"));
    if (fd == null) {
        out.print("表单不存在！");
        return;
    }

    boolean isLocation = false;
    MacroCtlUnit mu;
    MacroCtlMgr mm = new MacroCtlMgr();
    Iterator ir = fd.getFields().iterator();
    while (ir.hasNext()) {
        FormField ff = (FormField) ir.next();
        if (ff.getType().equals("macro")) {
            mu = mm.getMacroCtlUnit(ff.getMacroType());
            if (mu.getCode().equals("macro_location_ctl")) {
                isLocation = true;
                break;
            }
        }
    }

    isOnlyCamera = fd.isOnlyCamera();
    String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
    long parentId = ParamUtil.getLong(request, "parentId", 0);
    // 通过uniapp的webview载入
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE>
<html>
<head>
    <meta charset="utf-8">
    <title><%=msd.getString("name")%></title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
    <link href="../css/mui.indexedlist.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
</head>
<style>
    .mui-input-row .input-icon {
        width: 50%;
        float: left;
    }

    .mui-input-row a {
        margin-right: 10px;
        float: right;
        text-align: left;
        line-height: 1.5;
    }

    .div_opinion {
        text-align: left;
    }

    .opinionContent {
        margin: 10px;
        width: 65%;
        float: right;
        font-weight: normal;
    }

    .opinionContent div {
        text-align: right;
    }

    .opinionContent div span {
        padding: 10px;
    }

    .opinionContent .content_h5 {
        color: #000;
        font-size: 17px;
    }

    #captureFile {
        display: none;
    }
</style>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <a class="mui-icon mui-pull-right mui-a-color"></a>
    <h1 class="mui-title"><%=msd.getString("name")%></h1>
</header>
<div class="mui-content">
    <form class="mui-input-group" id="module_form">

    </form>
    <input type="file" id="captureFile" name="upload" accept="image/*"/>
</div>
<%
    if (isLocation) {
%>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=3.0&ak=3dd31b657f333528cc8b581937fd066a"></script>
<%
    }
%>
<link rel="stylesheet" href="../css/photoswipe.css">
<link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
<script type="text/javascript" src="../js/photoswipe.js"></script>
<script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
<script type="text/javascript" src="../js/photoswipe-init-manual.js"></script>

<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script type="text/javascript" src="../js/newPopup.js"></script>
<%--微信小程序SDK--%>
<%--<script type="text/javascript" src="../js/jweixin-1.4.0.js"></script>
<script type="text/javascript" src="../js/uni.webview.1.5.2.js"></script>--%>
<script type="text/javascript" src="../js/weixin.js"></script>
<script type="text/javascript" src="../js/uniapps.js"></script>

<script src="../js/macro/macro.js"></script>
<script src="../js/mui.min.js"></script>
<script src="../js/mui.picker.min.js"></script>
<script type="text/javascript" src="../js/config.js"></script>
<script type="text/javascript" src="../js/base/mui.form.js"></script>

<script type="text/javascript" src="../js/visual/mui_module.js"></script>
<script type="text/javascript" src="../../js/jquery.raty.min.js"></script>

<script type="text/javascript" charset="utf-8">
    var isUniWebview = <%=isUniWebview%>;
    if(mui.os.plus) {
        // 如果是通过uniapp的webview载入
        if (isUniWebview) {
            $('.mui-bar').remove();
        }

        // 注册beforeback方法，以使得在流程处理完后退至待办列表页面时能刷新页面
        if (isUniWebview) {
            mui.init({
                keyEventBind: {
                    backbutton: false // 关闭back按键监听
                }
            });
        }
    }
    else {
        $('.mui-bar').remove();
    }

    var appProp = {"type": "module", "isOnlyCamera": "<%=isOnlyCamera%>"};

    function callJS() {
        return appProp;
    }

    var iosCallJS = JSON.stringify(appProp);

    function setIsOnlyCamera(isOnlyCamera) {
        appProp.isOnlyCamera = isOnlyCamera;
        iosCallJS = JSON.stringify(appProp);
    }

    function resetIsOnlyCamera() {
        appProp.isOnlyCamera = "<%=fd.isOnlyCamera()%>"; // 用以在图像宏控件拍完照后，恢复底部拍照按钮的设置
    }

    // 用于在nest_sheet_add_edit.jsp中当post时提取页面的类型，如为add表示在智能模块中添加，edit表示在流程或智能模块编辑页面中添加
    function getParentPageType() {
        return "<%=id==0?"add":"edit"%>";
    }

    var pageType = "<%=id==0?"add":"edit"%>";

    $(function () {
        <%
        CloudConfig cloudConfig = CloudConfig.getInstance();
        int photoMaxSize = cloudConfig.getIntProperty("photoMaxSize");
        int intPhotoQuality = cloudConfig.getIntProperty("photoQuality");
        %>
        maxSize = {
            width: <%=photoMaxSize%>,
            height: <%=photoMaxSize%>,
            level: <%=intPhotoQuality%>
        };

        var content = document.querySelector('.mui-content');
        var skey = '<%=skey%>';
        var moduleCode = '<%=moduleCode%>';

        <%
        if (id==0) {
            id = StrUtil.toLong(RandomSecquenceCreator.getId(10), 0);
        }

        JSONObject extraData = new JSONObject();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = ParamUtil.getParameters(request, paramName); // 因为参数来自于url链接中，所以一定得通过ParamUtil.getParameters转换，否则会为乱码
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                // 过滤掉formCode、code等，code是企业微信端传过来的，不能被二次消费
                if (!("id".equals(paramName) || paramName.equals("moduleCode") || paramName.equals("formCodeRelated") || paramName.equals("parentId") || paramName.equals("pageType") || paramName.equals("skey"))) {
                     extraData.put(paramName, paramValue);
                }
            }
        }
        %>
        var id = <%=id%>;
        var formCodeRelated = '<%=formCodeRelated%>';
        var parentId = <%=parentId%>;
        var isUniWebview = <%=isUniWebview%>;
        var options = {
            "skey": skey,
            "moduleCode": moduleCode,
            "id": id,
            "formCodeRelated": formCodeRelated,
            "pageType": pageType,
            "parentId": parentId,
            "isUniWebview": isUniWebview
        };
        options.extraData = '<%=extraData%>';

        window.ModuleForm = new mui.ModuleForm(content, options);
        window.ModuleForm.moduleInit();
    });

    // 在moduleInit中调用
    function onModuleInited() {

    }
</script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
