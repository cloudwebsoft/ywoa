<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.attendance.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>打卡结果</title>
    <%@ include file="../../inc/nocache.jsp" %>
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <style>
        .iconBox {
            padding: 0px;
            margin: 70px auto;
            text-align: center;
        }

        .infoBox {
            width: 80%;
            height: 80px;
            border-top: 1px solid #ccc;
            text-align: center;
            margin: 40px auto 10px;
        }
    </style>
    <link rel="stylesheet" href="../css/mui.css">
    <script type="text/javascript"
            src="http://api.map.baidu.com/api?v=3.0&ak=3dd31b657f333528cc8b581937fd066a"></script>
    <script src="../../inc/common.js"></script>
    <script src="../../js/jquery-1.9.1.min.js"></script>

    <link rel="stylesheet" href="../../js/toastr/toastr.min.css">
    <script src="../../js/toastr/toastr.min.js"></script>

    <script type="text/javascript" src="../js/mui.js"></script>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">打卡结果</h1>
</header>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);

    int result = ParamUtil.getInt(request, "result", -1);
    int min = ParamUtil.getInt(request, "min", 0);
    // String address = ParamUtil.get(request, "address");
    String address = request.getParameter("address");
    boolean isLocationAbnormal = ParamUtil.getBoolean(request, "isLocationAbnormal", false);

    long id = ParamUtil.getLong(request, "id", -1);

    address = StrUtil.UnicodeToUTF8(address);
    Date dt = new Date();
    String dtStr = DateUtil.format(dt, "HH:mm");
%>
<div class="iconBox">
    <div>
        <%if (result == AttendanceMgr.NORMAL) { %>
        <img src="../images/punch_normal.png"/>
        <div style="margin-top: 15px">打卡成功</div>
        <%} else { %>
        <img src="../images/punch_abnormal.png"/>
        <%if (result == AttendanceMgr.LATE) { %>
        <div>迟到打卡(<%=min %>分钟)</div>
        <%} else if (result == AttendanceMgr.EARLY) { %>
        <div>早退打卡(<%=min %>分钟)</div>
        <%
                }
            }
        %>
        <%
            if (isLocationAbnormal) {
        %>
        <div>位置异常</div>
        <%}%>
    </div>
</div>
<div class="infoBox">
    <div style="margin:10px auto; clear:both; line-height:30px">
        <div style="float:left; width:30%; text-align:left;">
            时间
        </div>
        <div style="float:right; width:70%; text-align:right;">
            <%=dtStr %>
        </div>
    </div>
    <div style="margin:30px auto; clear:both;">
        <div style="float:left; width:30%; text-align:left;">
            位置
        </div>
        <div style="float:right; width:70%; text-align:right;">
            <%if (isLocationAbnormal) { %>
            <div><%=address %>
            </div>
            <div>(位置异常)</div>
            <%} else { %>
            有效范围内
            <%} %>
        </div>
    </div>
</div>
<div style="width:80%; margin:0px auto;">
    <%if (isLocationAbnormal || result != AttendanceMgr.NORMAL) { %>
    <div style="color:#6da3dc;margin:20px 0px 10px">报告原因</div>
    <div id="remarkBox">
<textarea id="remark" name="remak" style="width:100%;height:80px">
</textarea>
    </div>
    <div class="mui-button-row">
        <button id="btnOk" type="button" class="mui-btn">确定</button>
    </div>
    <div class="mui-row" style="height:20px">
        &nbsp;
    </div>
    <%} %>
</div>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
<script>
    var isUniWebview = <%=isUniWebview%>;

    if(!mui.os.plus || isUniWebview) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    mui.init({
        keyEventBind: {
            backbutton: !isUniWebview //关闭back按键监听
        }
    });

    $(function () {
        $('#btnOk').click(function () {
            if ($('#remark').val() == "") {
                mui.toast("请输入原因");
                return;
            }
            $.ajax({
                type: "post",
                url: "../../attendance/punchRemark.do",
                data: {
                    "userName": "<%=pvg.getUserName()%>",
                    "id":<%=id%>,
                    "remark": $('#remark').val()
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == 1) {
                        $('#remarkBox').html($('#remark').val());
                        $('#btnOk').hide();
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
</html>