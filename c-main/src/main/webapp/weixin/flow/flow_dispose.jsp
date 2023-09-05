<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.android.CloudConfig" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%
    // 通过uniapp的webview载入
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>处理流程</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" href="../css/mui.picker.min.css">
    <link rel="stylesheet" href="../css/at_flow.css"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>

    <link href="../../lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="../../lte/css/font-awesome.css?v=4.4.0" rel="stylesheet">
    <link href="../../lte/css/animate.css" rel="stylesheet">
    <link href="../../lte/css/style.css?v=4.1.0" rel="stylesheet">
    <!-- mui.js 不要放到</body>的前面，因为页面中如果return了，后退按钮就不生效了 -->
    <script type="text/javascript" src="../../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="../js/mui.js"></script>
    <script type="text/javascript" src="../js/config.js"></script>
    <script type="text/javascript" src="http://api.map.baidu.com/getscript?v=3.0&ak=3dd31b657f333528cc8b581937fd066a"></script>
</head>
<style>
    body {
        font-size: 17px;
        background-color: #efeff4;
    }

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

    .reply-date {
        margin-left: 10px;
    }

    .reply-header {
        color: #666;
    }

    .reply-content {
        margin: 20px 0 10px 0;
        color: #666;
    }

    .reply-progress {
        margin: 0 10px;
    }

    .info-box {
        width: 80%;
        margin: 120px auto;
        height: 60px;
        padding: 15px;
        border: 1px solid #ddd;
        text-align: center;
        font-size: 12px;
    }
    .info-box .mui-icon {
        font-size: 12px;
        margin-right: 10px;
    }

    .img-area {
        margin: 10px;
    }
    .img-box {
        border: 1px solid #C1C0C0;
        width: 100px;
        height: 100px;
        float: left;
        margin: 5px;
        position: relative;
    }
    .img-box .capture_btn {
        margin: 5px 0 0 5px;
        width: 80px;
        height: 80px;
    }
    .img-box-img {
        width: 100px;
        height: 100px;
    }
    .btn-del-img {
        width: 32px;
        height: 32px;
        position: absolute;
        left: 90px;
        top: -10px;
        z-index: 1;
    }
    .btn-del-img img {
        width: 16px;
        height: 16px;
    }
    .remark {
        color: #3b86a0;
    }
    #plusDescBox {
        margin: 10px;
    }
    #btnDelPlus {
        color: red;
        margin-left: 10px;
    }
    .mui-radio input[type='radio'] {
        margin-top: -5px;
    }
</style>
</head>
<body>
<header class="mui-bar mui-bar-nav" style="display: <%=isUniWebview?"none":""%>">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">处理流程</h1>
</header>
<%
    String skey;
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print("<div class=\"info-box\">请登录</a>");
        return;
    }
    else {
        skey = pvg.getSkey();
    }

    String userName = pvg.getUserName();
    UserDb ud = new UserDb();
    ud = ud.getUserDb(userName);

    int myActionId = ParamUtil.getInt(request, "myActionId", 0);
    String flowTypeCode = ParamUtil.get(request, "flowTypeCode"); // 流程类型
    int type = ParamUtil.getInt(request, "type", 0);
    String title = ParamUtil.get(request, "title");
    int flowId = -1;
    long actionId = -1;
    if (myActionId != 0) {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        /*
        if (mad == null || !mad.isLoaded()) {
            out.print("<div class=\"info-box\">待办记录已不存在！</div>");
            return;
        }
        if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_CHECKED) {
            out.print("<div class=\"info-box\">流程已处理！</div>");
            return;
        }*/
        actionId = mad.getActionId();
        flowId = (int) mad.getFlowId();
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        flowTypeCode = wf.getTypeCode();

        // 判断能否提交
        try {
            WorkflowActionDb wa = new WorkflowActionDb();
            wa = wa.getWorkflowActionDb((int)actionId);
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

            WorkflowMgr.canSubmit(request, wf, wa, mad, userName, wpd);
        } catch (ErrMsgException e) {
            out.print("<div class=\"info-box\"><span class=\"mui-icon mui-icon-info\"></span>" + e.getMessage() + "</div>");
            return;
        }

        // System.out.println(getClass() + " flowId=" + flowId + " flowTypeCode=" + flowTypeCode);
    }

    boolean isInit = myActionId == 0; // 是否为发起流程
    WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    boolean isRecall = true;
    FormDb fd = new FormDb();
    String formCode = "";
    Leaf lf = new Leaf();
    lf = lf.getLeaf(flowTypeCode);
    boolean isError = false;
    if (lf == null || !lf.isLoaded()) {
        isError = true;
        out.print("<div class=\"info-box\">流程类型已不存在！</a>");
    } else {
        formCode = lf.getFormCode();
        fd = fd.getFormDb(formCode);
        if (fd == null && !fd.isLoaded()) {
            isError = true;
            out.print("<div class=\"info-box\">表单类型已不存在！</a>");
        }
    }

    wpd = wpd.getPredefineFlowOfFree(flowTypeCode);
    if (wpd != null) {
        isRecall = wpd.isRecall();
    }

    if (wpd != null) {
%>
<div class="mui-content">
    <%if (!isInit && !wpd.isLight()) {%>
    <div style="padding: 10px 10px;">
        <div id="segmentedControl" class="mui-segmented-control">
            <a class="mui-control-item mui-active" href="#item1">
                待办
            </a>
            <a class="mui-control-item" href="#item2">
                过程
            </a>
        </div>
    </div>
    <%}%>
    <div>
        <div id="item1" class="mui-control-content mui-active">
            <form id="free_flow_form" action="../../public/flow_dispose_free_do.jsp" method="post" enctype="multipart/form-data">
            </form>
            <form class="mui-input-group" id="flow_form">
            </form>
            <input type="file" id="captureFile" <%--capture="camera"--%> name="upload" style="cursor: pointer"/>
            <%
                String dis = "";
                if (!fd.isProgress()) {
                    dis = "display:none";
                }
                if (wpd.isReply() && !"at".equals(flowTypeCode) && !wpd.isLight() && lf.getType() != Leaf.TYPE_FREE) { %>
            <div class="annex-group">
                <div class="reply-form" style="display:none; margin-bottom:10px">
                    <div class="mui-input-row mui-input-range" s>
                        <label>进度<span id="progressLabel" style="margin-left:10px"></span></label>
                        <input id="progress" name="progress" type="range" min="0" max="100" onchange="$('#progressLabel').text(mui('#progress')[0].value)">
                    </div>
                    <div class="mui-input-row" data-code="content" data-isnull="false">
                        <label><span>回复</span><span style='color:red;'>*</span></label>
                        <div style="text-align:center">
                            <textarea id="content" name="content" placeholder="请输入回复内容" style="width:96%; height:150px;"></textarea>
                        </div>
                    </div>
                    <div class="mui-input-row mui-checkbox" data-code="isSecret">
                        <label><span>隐藏</span><span style='color:red;'>*</span></label>
                        <input type="checkbox" id="isSecret" name="isSecret" value="1"/>
                    </div>
                    <div class="mui-button-row">
                        <button type="button" class="mui-btn mui-btn-primary mui-btn-outlined btn-ok">确定</button>
                    </div>
                </div>
            </div>
            <%} %>
        </div>

        <%if (!isInit && !wpd.isLight()) {%>
        <div id="item2" class="mui-control-content">
            <div id="vertical-timeline" class="vertical-container light-timeline">
                <%
                    UserMgr um = new UserMgr();
                    MyActionDb mad = new MyActionDb();
                    Vector<MyActionDb> v = mad.getMyActionDbOfFlow(flowId);
                    for (MyActionDb myActionDb : v) {
                        mad = myActionDb;
                        String userRealName;
                        if (!"".equals(mad.getProxyUserName())) {
                            userRealName = um.getUserDb(mad.getProxyUserName()).getRealName();
                        } else {
                            UserDb user = um.getUserDb(mad.getUserName());
                            userRealName = user.getRealName();
                        }

                        WorkflowActionDb wad = new WorkflowActionDb();
                        wad = wad.getWorkflowActionDb((int) mad.getActionId());
                %>
                <div class="vertical-timeline-block" style="margin-bottom:10px">
                    <div class="vertical-timeline-icon blue-bg">
                        <i class="fa fa-user"></i>
                    </div>
                    <div class="vertical-timeline-content">
                        <h3><%=StrUtil.getNullStr(wad.getTitle())%>
                        </h3>
                        <p>
                            <%
                                if (mad.getChecker().equals(UserDb.SYSTEM)) {
                                    String str = LocalUtil.LoadString(request, "res.flow.Flow", "skipOverTime");
                                    out.print(str);
                                } else {
                            %>
                            <%=mad.getCheckStatusName()%>
                            <%
                                }
                                if (mad.getCheckStatus() != 0 && mad.getCheckStatus() != MyActionDb.CHECK_STATUS_TRANSFER && mad.getCheckStatus() != MyActionDb.CHECK_STATUS_SUSPEND) {
                                    if (mad.getResultValue() != WorkflowActionDb.RESULT_VALUE_RETURN) {
                                        if (mad.getSubMyActionId() == MyActionDb.SUB_MYACTION_ID_NONE) {
                                            out.print("(" + WorkflowActionDb.getResultValueDesc(mad.getResultValue()) + ")");
                                        }
                                    }
                                }
                            %>
                        </p>
                        <p class="remark">
                            留言: <%=mad.getResult()%>
                        </p>
                        <%
                            if (isRecall && mad.canRecall(userName)) {
                        %>
                        <a href="#" class="btn btn-sm btn-success btn-recall" myActionId="<%=mad.getId()%>">撤回</a>
                        <%} %>
                        <span class="vertical-date">
                            <%=userRealName %> <br/>
                            <small><%=DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm")%></small>
                        </span>
                    </div>
                </div>
                <%} %>
            </div>
        </div>
        <%}%>
    </div>
</div>
<%
    }
%>
<script src="form_js/<%=formCode%>.jsp?flowId=<%=flowId%>&myActionId=<%=myActionId%>&skey=<%=skey %>&Fcode=<%=flowTypeCode %>&Ftype=<%=type %>&Ftitle=<%=title %>"></script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
<script>
    var isUniWebview = <%=isUniWebview%>;
    // 用于HBuilderX手机端
    if(!mui.os.plus || isUniWebview) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    if(mui.os.plus) {
        // 注册beforeback方法，以使得在流程处理完后退至待办列表页面时能刷新页面
        mui.init({
            <%
            if (isUniWebview) {
            %>
            keyEventBind: {
                backbutton: false // 关闭back按键监听
            },
            <%
            }
            %>
            beforeback: function() {
                //获得父页面的webview
                var list = plus.webview.currentWebview().opener();
                //触发父页面的自定义事件(refresh),从而进行刷新
                mui.fire(list, 'refreshList');
                //返回true,继续页面关闭逻辑
                return true;
            }
        });
    }

    var appProp = {"type": "module", "isOnlyCamera": "<%=fd.isOnlyCamera()%>", "btnAddShow": 0, "btnBackUrl": ""};

    function callJS() {
        return appProp;
    }

    var iosCallJS = JSON.stringify(appProp);
    // var iosCallJS = '{ "type": "module", "isOnlyCamera": "<%=fd.isOnlyCamera()%>", "btnAddShow": 0, "btnBackUrl": "" }';

    function setIsOnlyCamera(isOnlyCamera) {
        appProp.isOnlyCamera = isOnlyCamera;
        iosCallJS = JSON.stringify(appProp);
    }

    function resetIsOnlyCamera() {
        appProp.isOnlyCamera = "<%=fd.isOnlyCamera()%>"; // 用以在图像宏控件拍完照后，恢复底部拍照按钮的设置
    }
</script>
<script src="../js/jq_mydialog.js"></script>
<script type="text/javascript" src="../js/newPopup.js"></script>
<script src="../js/macro/macro.js"></script>
<%--<script src="../js/mui.picker.min.js"></script>--%>
<script>
    console.log('mui.os.ios', mui.os.ios, 'parseFloat(mui.os.version)', parseFloat(mui.os.version));
    if (mui.os.ios) {
        if (parseFloat(mui.os.version) < 16.5) {
            includFile(getContextPath() + "/weixin/js/",['mui.picker_ios16.min.js']);
            includFile(getContextPath() + "/weixin/css/",['mui.picker_ios16.min.css']);
        }
        else {
            includFile(getContextPath() + "/weixin/js/",['mui.picker.min.js']);
            includFile(getContextPath() + "/weixin/css/",['mui.picker.min.css']);
        }
    } else {
        includFile(getContextPath() + "/weixin/js/",['mui.picker_ios16.min.js']);
        includFile(getContextPath() + "/weixin/css/",['mui.picker_ios16.min.css']);
    }
</script>
<%--微信小程序SDK--%>
<%--<script type="text/javascript" src="../js/jweixin-1.4.0.js"></script>
<script type="text/javascript" src="../js/uni.webview.1.5.2.js"></script>--%>
<script type="text/javascript" src="../js/weixin.js"></script>
<script type="text/javascript" src="../js/uniapps.js"></script>

<link rel="stylesheet" href="../css/photoswipe.css">
<link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
<script type="text/javascript" src="../js/photoswipe.js"></script>
<script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
<script type="text/javascript" src="../js/photoswipe-init-manual.js"></script>

<script type="text/javascript" src="../js/base/mui.form.js"></script>
<script type="text/javascript" src="../js/mui.flow.wx.js"></script>
<!--解决iphone拍照变横向的问题-->
<script type="text/javascript" src="../js/exif.js"></script>
<script type="text/javascript" charset="utf-8">
    <%
    if (isError) {
        return;
    }
    %>
    var content = document.querySelector('.mui-content');
    var skey = '<%=skey%>';
    var myActionId = '<%=myActionId%>';
    var flowTypeCode = '<%=flowTypeCode%>';
    var title = '<%=title%>';
    var type = <%=type%>;
    var isUniWebview = <%=isUniWebview%>;
    var options = {
        "skey": skey,
        "title": title,
        "myActionId": myActionId,
        "type": type,
        "formCode": '<%=formCode%>',
        "code": flowTypeCode,
        "isUniWebview": isUniWebview
    };

    <%
    // myActionId为0表示发起流程
    JSONObject extraData = new JSONObject();
    // 在小程序端的webview待办列表中，再转至此页面时要带入参数，如：blockId
    if (true || myActionId==0) {
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = ParamUtil.getParameters(request, paramName); // 因为参数来自于url链接中，所以一定得通过ParamUtil.getParameters转换，否则会为乱码
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                // 过滤掉formCode、code等，code是企业微信端传过来的，不能被二次消费
                if (!("flowId".equals(paramName) || paramName.equals("code") || paramName.equals("myActionId") || paramName.equals("title") || paramName.equals("type") || paramName.equals("flowTypeCode") || paramName.equals("skey"))) {
                     extraData.put(paramName, paramValue);
                }
            }
        }
    }
    DebugUtil.i(getClass(), " extraData", extraData.toString());
    %>

    options.extraData = '<%=extraData%>';

    window.flow = new mui.Flow(content, options);
    window.flow.flowDisposeInit();

    // 当发起流程时，actionId为-1，所以在发起点上此方法不可行
    /*function setAgreeBtnName() {
        // 置同意按钮的名称
        <%
            String btnAgreeName = "";
            if (actionId != -1) {
                WorkflowActionDb wa = new WorkflowActionDb();
                wa = wa.getWorkflowActionDb((int)actionId);
                btnAgreeName = WorkflowActionDb.getActionProperty(wpd, wa.getInternalName(), "btnAgreeName");
            }
            String btnName = "";
            if (btnAgreeName!=null && !"".equals(btnAgreeName)) {
                btnName = btnAgreeName;
            }
            else {
                com.redmoon.oa.flow.FlowConfig conf = new com.redmoon.oa.flow.FlowConfig();
                btnName = conf.getBtnName("FLOW_BUTTON_AGREE").startsWith("#") ? LocalUtil.LoadString(request, "res.flow.Flow", "agree") : conf.getBtnName("FLOW_BUTTON_AGREE");
            }
        %>

        $('.flow_submit').html('<%=btnName%>');
    }*/

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

        // setTimeout(setAgreeBtnName, 1000);

        $('.btn-ok').click(function () {
            var _tips = "";
            jQuery("div[data-isnull='false']").each(function (i) {
                var _code = jQuery(this).data("code");
                var _val = jQuery("#" + _code).val();
                if (_val == undefined || _val == "") {
                    var _text = jQuery(this).find("span:first").text();
                    _tips += _text + " 不能为空<BR/>"
                }
            });
            if (_tips != null && _tips != "") {
                mui.toast(_tips);
                return;
            }

            var progress = mui('#progress')[0].value;
            var isSecret = jQuery('#isSecret').is(":checked") ? 1 : 0;
            $.ajax({
                type: "post",
                url: "../../public/flow/addReply.do",
                data: "skey=<%=skey%>&content=" + jQuery('#content').val() + "&isSecret=" + isSecret + "&progress=" + progress + "&flowId=<%=flowId%>&actionId=<%=actionId%>",
                dataType: "html",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                beforeSend: function (XMLHttpRequest) {
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == "1") {
                        var $ul = $('.reply-ul');
                        $ul.show();
                        var li = '<li class="mui-table-view-cell">';
                        li += '<div class="reply-header">';
                        li += '<span class="reply-name"><%=ud.getRealName() %></span>';
                        li += '<span class="reply-progress">' + progress + '%</span>';
                        li += '<span class="reply-date"><%=DateUtil.format(new Date(), "yyyy-MM-dd")%></span>';
                        li += '</div>';
                        li += '<div class="reply-content">' + $('#content').val() + '</div>';
                        li += '</li>';
                        $ul.append(li);

                        $('#progressLabel').text(progress);
                        $('#content').val('');
                        // 不删除，使可以继续回复
                        // $('.reply-form').remove();
                    }
                    mui.toast(data.msg);
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    });
</script>
</body>
</html>
