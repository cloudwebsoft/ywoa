<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.*" %>
<%@page import="cn.js.fan.util.*" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();

    int id = ParamUtil.getInt(request, "id", -1);
    PlanDb pd = new PlanDb();
    pd = pd.getPlanDb(id);
    String createDate = DateUtil.format(pd.getZdrq(), "yyyy-MM-dd");
    String bDate = DateUtil.format(pd.getMyDate(), "yyyy-MM-dd");
    String eDate = DateUtil.format(pd.getEndDate(), "yyyy-MM-dd");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>日程安排</title>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta name="format-detection" content="telephone=no,email=no,adress=no">
    <link rel="stylesheet" href="../css/mui.css">
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">查看日程</h1>
</header>
<div class="mui-content">
    <ul class="mui-table-view">
        <li class="mui-table-view-cell" style="text-align:center"><b><%=pd.getTitle() %>
        </b><br></li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">开始时间</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=bDate %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">结束时间</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=eDate %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">便笺</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=pd.isNotepaper() ? "是" : "否" %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">状态</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=pd.isClosed() ? "<img id='imgStatus' src='../../images/task_complete.png' style='width:16px'>" : "<img id='imgStatus' src='../../images/task_ongoing.png' style='width:16px'"%>
                </div>
            </div>
        </li>
        <%
            com.redmoon.oa.pvg.Privilege oapvg = new com.redmoon.oa.pvg.Privilege();
            if (oapvg.isUserPrivValid(request, "plan.share")) {%>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">共享</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=pd.isShared() ? "是" : "否"%>
                </div>
            </div>
        </li>
        <%} %>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">创建日期</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=createDate %>
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell"><%=pd.getContent() %>
        </li>
    </ul>
    <div class="mui-button-row">
        <%
            boolean canDo = true;
            if (!pvg.getUserName().equals(oapvg.getUser(request))) {
                if (!(oapvg.canAdminUser(request, pvg.getUserName()))) {
                    canDo = false;
                }
            }
            if (canDo) {
        %>
        <a type="button" class="mui-btn mui-btn-primary mui-btn-outlined btn-modify">修改</a>
        &nbsp;&nbsp;
        <a type="button" class="mui-btn mui-btn-primary mui-btn-outlined btn-del">删除</a>
        &nbsp;&nbsp;
        <a type="button" class="mui-btn mui-btn-primary mui-btn-outlined btn-close">完成</a>
        <a type="button" class="mui-btn mui-btn-primary mui-btn-outlined btn-open">未完成</a>
        <%} %>
    </div>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="js/simple-calendar.js"></script>
<script type="text/javascript" src="js/hammer-2.0.8-min.js"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script type="text/javascript">
	if(!mui.os.plus) {
		// 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
		$('.mui-bar').remove();
	}

    $(function () {
        <%if (pd.isClosed()) { %>
        $('.btn-close').hide();
        <%}else{%>
        $('.btn-open').hide();
        <%}%>
        $('.btn-modify').click(function () {
            window.location.href = "calendar_edit.jsp?id=<%=id%>";
        });

        $('.btn-del').click(function () {
            var btnArray = ['否', '是'];
            mui.confirm('您确定要删除么？', '提示', btnArray, function (e) {
                if (e.index == 1) {
                    $.ajax({
                        url: "../../public/plan/delPlan.do",
                        type: "post",
                        data: {
                            id: <%=id%>
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == 1) {
                                var arr = ['确定'];
                                mui.alert(data.msg, '提示', arr, function (e) {
                                    window.history.back();
                                });
                            } else {
                                mui.toast(data.msg);
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                        },
                        error: function (XMLHttpRequest, textStatus) {
                        }
                    });
                }
            })
        });

        $('.btn-open').click(function () {
            var btnArray = ['否', '是'];
            mui.confirm('您确定要设为未完成状态么？', '提示', btnArray, function (e) {
                if (e.index == 1) {
                    $.ajax({
                        url: "../../public/plan/openPlan.do",
                        type: "post",
                        data: {
                            id: <%=id%>
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == 1) {
                                $('.btn-close').show();
                                $('.btn-open').hide();
                                $('#imgStatus').attr('src', '../../images/task_ongoing.png');
                            }
                            mui.toast(data.msg);
                        },
                        complete: function (XMLHttpRequest, status) {
                        },
                        error: function (XMLHttpRequest, textStatus) {
                        }
                    });
                }
            })
        });

        $('.btn-close').click(function () {
            var btnArray = ['否', '是'];
            mui.confirm('您确定要设为完成状态么？', '提示', btnArray, function (e) {
                if (e.index == 1) {
                    $.ajax({
                        url: "../../public/plan/closePlan.do",
                        type: "post",
                        data: {
                            id: <%=id%>
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == 1) {
                                $('.btn-close').hide();
                                $('.btn-open').show();
                                $('#imgStatus').attr('src', '../../images/task_complete.png');
                            }
                            mui.toast(data.msg);
                        },
                        complete: function (XMLHttpRequest, status) {
                        },
                        error: function (XMLHttpRequest, textStatus) {
                        }
                    });
                }
            })
        });
    });

    function callJS() {
        // return { "btnAddShow":0, "btnAddUrl":"weixin/calendar/calendar_add.jsp", "btnBackUrl":"weixin/calendar/calendar.jsp" };
        return {"btnAddShow": 1, "btnAddUrl": "weixin/calendar/calendar_add.jsp", "btnBackUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":1, "btnAddUrl":"weixin/calendar/calendar_add.jsp", "btnBackUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>