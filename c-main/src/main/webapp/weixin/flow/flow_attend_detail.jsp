<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.cloudwebsoft.framework.util.*" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>查看流程</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css"/>
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" href="../css/mui.picker.min.css"/>
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
    <%
        String skey = ParamUtil.get(request, "skey");
        Privilege pvg = new Privilege();
        pvg.auth(request);
        String userName = pvg.getUserName();
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);

        int flowId = ParamUtil.getInt(request, "flowId", 0);
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
        boolean isRecall = wpd.isRecall();
        // 通过uniapp的webview载入
        boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
    %>
    <style type="text/css">
        body {
            font-size: 17px;
            background-color: #efeff4;
        }

        .mui-h5 {
            color: #000;
        }

        .reply-date {
            margin-left: 10px;
        }

        .reply-header {
            color: #666;
        }

        .reply-content {
            margin: 20px 0px 10px 0px;
            color: #666;
        }

        .reply-progress {
            margin: 0px 10px;
        }

        .remark {
            color: #3b86a0;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">查看流程</h1>
</header>
<div class="mui-content">
    <%if (!wpd.isLight()) {%>
    <div style="padding: 10px 10px;">
        <div id="segmentedControl" class="mui-segmented-control">
            <a class="mui-control-item mui-active" href="#item1">
                详情
            </a>
            <a class="mui-control-item" href="#item2">
                过程
            </a>
        </div>
    </div>
    <%} %>
    <div>
        <div id="item1" class="mui-control-content mui-active">

            <ul class="mui-table-view">
            </ul>

            <div class="annex-group">
                <div class="reply-form" style="display:none; margin-bottom:10px">
                    <div class="mui-input-row mui-input-range">
                        <label>进度<span id="progressLabel" style="margin-left:10px"></span></label>
                        <input id="progress" name="progress" type="range" min="0" max="100"
                               onchange="$('#progressLabel').text(mui('#progress')[0].value)">
                    </div>
                    <div class="mui-input-row" data-code="content" data-isnull="false">
                        <label><span>回复</span><span style='color:red;'>*</span></label>
                        <div style="text-align:center">
                            <textarea id="content" name="content" placeholder="请输入回复内容"
                                      style="width:96%; height:150px;"></textarea>
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
            <div style="height:160px"></div>
        </div>

        <%if (!wpd.isLight()) {%>
        <div id="item2" class="mui-control-content">
            <div id="vertical-timeline" class="vertical-container light-timeline">
                <%
                    UserMgr um = new UserMgr();
                    String sql = "select id from flow_my_action where flow_id=" + flowId + " order by receive_date asc";
                    MyActionDb mad = new MyActionDb();
                    Vector v = mad.list(sql);
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        mad = (MyActionDb) ir.next();
                        String userRealName;
                        if (!mad.getProxyUserName().equals("")) {
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
                            <%=userRealName %> <br>
                            <small><%=DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm")%>
                            </small>
                        </span>
                    </div>
                </div>
                <%} %>
            </div>
        </div>
        <%} %>
    </div>
</div>
<script src="form_js/<%=lf.getFormCode()%>.jsp?flowId=<%=flowId%>&myActionId=-1"></script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>

<script type="text/javascript" src="../js/jq_mydialog.js"></script>
<script type="text/javascript" src="../js/newPopup.js"></script>
<script type="text/javascript" src="../js/macro/macro.js"></script>
<script type="text/javascript" src="../js/mui.picker.min.js"></script>
<script type="text/javascript" src="../js/weixin.js"></script>
<script type="text/javascript" src="../js/uniapps.js"></script>

<link rel="stylesheet" href="../css/photoswipe.css">
<link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
<script type="text/javascript" src="../js/photoswipe.js"></script>
<script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
<script type="text/javascript" src="../js/photoswipe-init-manual.js"></script>

<script type="text/javascript" src="../js/base/mui.form.js"></script>
<script type="text/javascript" src="../js/mui.flow.wx.js"></script>
<script type="text/javascript" charset="utf-8">
    var isUniWebview = <%=isUniWebview%>;

    if(!mui.os.plus || isUniWebview) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    if(mui.os.plus) {
        // 注册beforeback方法，以使得在流程处理完后退至待办列表页面时能刷新页面
        if (isUniWebview) {
            mui.init({
                keyEventBind: {
                    backbutton: false // 关闭back按键监听
                }
            });
        }
    }

    var skey = '<%=skey%>';
    var flowId = <%=flowId%>;
    var content = document.querySelector('.mui-content');
    var options = {"skey": skey, "flowId": flowId};
    var flow = new mui.Flow(content, options);
    flow.flowAttendDetail();

    $(function () {
        $('.btn-recall').click(function () {
            var myActionId = $(this).attr('myActionId');
            var btnArray = ['否', '是'];
            mui.confirm('您确定要撤回么？', '提示', btnArray, function (e) {
                if (e.index == 1) {
                    $.ajax({
                        type: "get",
                        url: "../../public/flow_dispose_do.jsp",
                        data: {
                            "action": "recall",
                            "skey": skey,
                            "flowId": flowId,
                            "myActionId": myActionId
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "1") {
                                mui.alert(data.msg, '提示', ['确定'], function (e) {
                                    window.location.reload();
                                });
                            } else {
                                mui.alert(data.msg, '提示', ['确定']);
                            }
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            //alert(XMLHttpRequest.responseText);
                        }
                    })
                }
            });
        });

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
                data: "skey=<%=skey%>&content=" + jQuery('#content').val() + "&isSecret=" + isSecret + "&progress=" + progress + "&flowId=<%=flowId%>",
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
