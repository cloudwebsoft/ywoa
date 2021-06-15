<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>${pageTitle}的工作日报</title>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/mywork/mywork_list.css"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/goToTop/goToTop.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/goToTop/goToTop.css"/>

    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>

    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>

    <script src="<%=request.getContextPath() %>/mywork/js/c_worklog.js"></script>
</head>
<body id="dayBody">
<%@ include file="mywork_inc_menu.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<div class="mywork-list-wrap">
    <!--日报时间选择-->
    <div class="mywork-list-time">
        <ul>
            <li class="mywork-list-time-span1" id="before" onclick="before()"></li>
            <li style="list-style:none;"><input id="sendDate" name="sendDate" value="${dateCond}" size=10 class='mywork-list-time-span2'/></li>
            <li class="mywork-list-time-span3" id="after" onclick="after()"></li>
            <li class="mywork-list-time-span4" onclick="returnToday()">返回今天</li>
        </ul>
        <form id="queryFrm" name="queryFrm" action="<%=request.getContextPath()%>/mywork/queryMyWorkForShow" method="post" enctype="multipart/form-data">
            <div class="mywork-list-search">
                <input type="text" name="contentCond" id="contentCond" title="按“Enter”执行查询" value="${contentCond}" onkeypress="if (event.keyCode == 13) search();">
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-search.png" onclick="search()" class="searchImg"/>
            </div>
            <input type="hidden" name="dateCond" id="dateCond" value="${dateCond}"/>
            <input type="hidden" name="userName" id="userName" value="${userName}"/>
            <input type="hidden" name="dayLimit" id="dayLimit" value="${dayLimit}"/>
            <input type="hidden" name="logType" id="logType" value="${logType}"/>
            <input type="hidden" name="curPage" id="curPage" value="${curPage}"/>
            <input type="hidden" name="beforeOrAfter" id="beforeOrAfter" value="0"/>
        </form>
        <form id="initForm" name="initForm" action="<%=request.getContextPath()%>/mywork/showWorkLogInfo" method="post" enctype="multipart/form-data">
            <input type="hidden" name="userName" value="${userName}"/>
        </form>
        <input type="hidden" name="workLogId" id="workLogId" value=""/>
        <input type="hidden" name="isPreparedTodys" id="isPreparedTodys" value="${isPreparedTodys}"/>
        <input type="hidden" name="lastBeginTime" id="lastBeginTime" value="${lastBeginTime}"/>
        <input type="hidden" name="contextPath" id="contextPath" value="<%=request.getContextPath()%>"/>
        <input type="hidden" name="skinPath" id="skinPath" value="<%=SkinMgr.getSkinPath(request)%>"/>
    </div>
    <!--发布日报-->
    <c:forEach var="wl" items="${list}" varStatus="statu">
        <div class="mywork-list-master">
            <div id="mywork_${wl.id}">
                <div class="mywork-list-master-p1">${wl.myDate}</div>
                <div class="mywork-list-master-tabb1" id="${statu.index}">
                        ${wl.content}
                </div>
                <c:if test="${wl.content != '暂未填写'}">
                    <div class="divComment">
                        <div class="mywork-list-comment" title="评论" onclick="showReply('div_${wl.id}')"></div>
                        <c:set var="isApraise" value="false"/>
                        <c:if test="${fn:length(wl.workLogPraises)>0}">
                            <c:forEach var="wp" items="${wl.workLogPraises}">
                                <c:if test="${wp.name eq userName}">
                                    <c:set var="isApraise" value="true"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <c:choose>
                            <c:when test="${isApraise}">
                                <div class="mywork-list-praise mywork-list-cancel-praise-bg" praiseCount="${wl.praiseCount}" title="取消点赞" apraiseType="0" id="${wl.id}"></div>
                            </c:when>
                            <c:otherwise>
                                <div class="mywork-list-praise mywork-list-praise-bg" praiseCount="${wl.praiseCount}" id="${wl.id}" apraiseType="1" title="点赞"></div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
                <c:if test="${fn:length(wl.workLogAttachs) > 0}">
                    <div class="mywork-list-master-file" id="files_div_${wl.id}">
                        <c:forEach var="wa" items="${wl.workLogAttachs}">
                            <div id="attach_${wa.id}">
                                <img src="<%=SkinMgr.getSkinPath(request)%>/images/message/inbox-adnexa.png" width="15" height="15"/>
                                <a target="_blank" href="<%=request.getContextPath() %>/mywork/mywork_getfile.jsp?attachId=${wa.id}">${wa.name}</a>
                                <a href="javascript:void(0);" name="attDel_${wl.id}" onclick="delAttach(${wa.id},'${wa.id}')" style="display:none">
                                    <img src="<%=request.getContextPath()%>/images/del.png" width="16" height="16"/>
                                </a>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
            <div id="div_${wl.id}" style="display:none">
           <span class="mywork-list-master-p4">
		       <textarea name="textarea2" rows="2" class="mywork-list-master-comment" id="textarea_${wl.id}"></textarea>
	       </span>
                <div class="mywork-list-master-restore">
                    <div class="mywork-list-master-restore-btn" onclick="replyWorkLog('div_${wl.id}',${wl.id})">评论</div>
                </div>
            </div>
            <div id="expands_${wl.id}">
                <div class="mywork-list-triangle"></div>
                <div class="mywork-list-filleting" id="content_expands_${wl.id}">
                    <c:if test="${wl.workLogPraises != null}">
                        <p class="p_praise_detail_${wl.id}">
                            <img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon_praise_count.png" width="20" height="20"/>
                            <span class="span_praisecount">赞(${wl.praiseCount})</span>
                            <c:set var="users" value=""/>
                            <c:forEach var="wp" items="${wl.workLogPraises}">
                                <c:choose>
                                    <c:when test="${users==''}"><c:set var="users" value="${wp.userName}"/></c:when>
                                    <c:otherwise><c:set var="users" value="${users.concat(',').concat(wp.userName)}"/></c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <span class="span_praiseusers">${users}</span>
                        </p>
                    </c:if>
                    <div class="div_review">
                        <c:choose>
                            <c:when test="${wl.workLogExpands != null}">
                                <p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;(<span id="reviewCount_${wl.id}">${wl.reviewCount}</span>)</span></p>
                                <div id="newExpands_${wl.id}"></div>
                                <c:forEach var="we" items="${wl.workLogExpands}">
                                    <div id="review_${we.id}/>"><label class="reviewNameLabel">${we.userName}</label>：${we.review}</div>
                                    <div class="mywork-list-filleting-r">${we.reviewTime}
                                        <div class="mywork-list-filleting-restore" onclick="showQuoteReply('${we.id}','review_${we.id}')"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20"></div>
                                    </div>
                                    <div id="reply_${we.id}" style="display:none">
                                        <textarea class="mywork-list-restore-box" id="quote_txt_${we.id}"></textarea>
                                        <div class="mywork-list-box-f">
                                            <div class="mywork-list-box-f-1" onclick="reply('${we.id}',${wl.id})">发布</div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                暂无评论
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </c:forEach>
    <div id="moreInfos" style="dispose:none">
    </div>
</div>
<div class="mywork-list-more" onclick="showMore()">————&nbsp;&nbsp;查看更多记录&nbsp;&nbsp;————</div>
<input type="hidden" value="${dayLimit}" id="dayLimit"/>
<input type="hidden" value="0" id="saveOrCreate"/>
<input type="hidden" id="tempContent" value=""/>
</body>
<script type="text/javascript">
    var uEditor = null;

    //加载更多日志
    function showMore() {
        var lastBeginTime = $("#lastBeginTime").val();
        var contentCond = $("#contentCond").val();
        var dateCond = "";
        if (contentCond != "") {
            $("#beforeOrAfter").val('3');
            dateCond = $("#dateCond").val();
        } else {
            $("#beforeOrAfter").val('0');
            dateCond = lastBeginTime;
        }
        $("#dayBody").showLoading();
        $.ajax({
            type: "post",
            url: '<%=request.getContextPath()%>/mywork/queryMoreMyWork',
            data: {
                logType: $("#logType").val(),
                curPage: $("#curPage").val(),
                contentCond: contentCond,
                beforeOrAfter: $("#beforeOrAfter").val(),
                dateCond: dateCond,
                userName: $("#userName").val()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.message.indexOf("成功") == -1) {
                    jAlert(data.message, "提示");
                    return false;
                }
                //加载查询内容
                for (var i = 0; i < data.list.length; i++) {
                    var logId = data.list[i].id;
                    var inhtml = '<div class="mywork-list-master">'
                        + '<div  id="mywork_' + logId + '">'
                        + '<div class="mywork-list-master-p1">' + data.list[i].myDate + '</div> '
                        + '<div class="mywork-list-master-tabb1" id="' + logId + '" >'
                        + data.list[i].content
                        + '</div>';


                    if (data.list[i].content != "暂未填写") {
                        inhtml = inhtml + '<div class="divComment">'
                            + '<div class="mywork-list-comment" title="评论" id="showReplyBtn_' + logId + '"></div>';
                        var p_res = praiseStatus('<%=privilege.getUser(request)%>', data.list[0].workLogPraises);
                        if (p_res) {
                            inhtml += '<div class="mywork-list-praise mywork-list-cancel-praise-bg"  praiseCount="' + data.list[i].praiseCount + '"  title="取消点赞" apraiseType ="0" id="' + logId + '"></div>';
                        } else {
                            inhtml += '<div class="mywork-list-praise mywork-list-praise-bg" praiseCount="' + data.list[i].praiseCount + '"  id="' + logId + '" apraiseType ="1"   title="点赞"></div>';

                        }
                        inhtml += '</div>';
                    }
                    if (data.list[i].workLogAttachs != null && data.list[i].workLogAttachs.length > 0) {
                        inhtml = inhtml + '<div class="mywork-list-master-file" id="files_div_' + logId + '">';
                        for (var j = 0; j < data.list[i].workLogAttachs.length; j++) {
                            var wa = data.list[i].workLogAttachs[j];
                            var waId = wa.id;
                            inhtml = inhtml + '<div id="attach_' + logId + '">'
                                + '<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/inbox-adnexa.png" width="15" height="15"/> '
                                + '<a target="_blank" href="<%=request.getContextPath() %>/mywork/mywork_getfile.jsp?attachId=' + waId + '">' + data.list[i].workLogAttachs[j].name + '</a>'
                                + '<a href="javascript:void(0);" id="del_' + waId + '" name="attDel_' + logId + '" style="display:none">  <img src="<%=request.getContextPath()%>/images/del.png" width="16" height="16"/></a>'
                                + '</div> ';

                        }
                        inhtml = inhtml + '</div>';
                    }
                    inhtml = inhtml + ' </div>'
                        + '<div id="div_' + logId + '" style="display:none">'
                        + '<span class="mywork-list-master-p4">'
                        + '<textarea name="textarea2" rows="2" class="mywork-list-master-comment" id="textarea_' + logId + '"></textarea>'
                        + '</span>'
                        + '<div class="mywork-list-master-restore">'
                        + '<div class="mywork-list-master-restore-btn" id="replyBtn_' + logId + '">评论</div>'
                        + '</div>'
                        + '</div>'
                        + '<div id="expands_' + logId + '">'
                        + '<div class="mywork-list-triangle"></div>'
                        + '<div class="mywork-list-filleting " id="content_expands_' + logId + '">';

                    if (data.list[i].workLogPraises != null && data.list[i].workLogPraises.length > 0) {

                        var p_name = praiseUsers(data.list[i].workLogPraises);
                        inhtml += '<p class ="p_praise_detail_' + logId + '">';
                        inhtml += '<img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon_praise_count.png" width="20" height="20"/>';
                        inhtml += '<span class="span_praisecount">赞(' + data.list[i].praiseCount + ')</span>';
                        inhtml += ' <span class="span_praiseusers">' + p_name + '</span>';
                        inhtml += '</p>'
                    }

                    inhtml += '<div class = "div_review">'
                    if (data.list[i].workLogExpands != null && data.list[i].workLogExpands.length > 0) {
                        inhtml = inhtml + ' <p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;'
                            + '(<span id="reviewCount_' + logId + '">' + data.list[i].reviewCount + '</span>)</span></p>'
                            + '<div id="newExpands_' + logId + '"></div>';

                        for (var k = 0; k < data.list[i].workLogExpands.length; k++) {
                            var we = data.list[i].workLogExpands[k];
                            inhtml = inhtml + '<div id="review_' + we.id + '"><label class="reviewNameLabel">' + we.userName + '</label>：' + we.review + '</div>'
                                + '<div class="mywork-list-filleting-r">' + we.reviewTime
                                + '<div class="mywork-list-filleting-restore" id="quoteReplyImg_' + we.id + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20"></div>'
                                + '</div>'
                                + '<div id="reply_' + we.id + '" style="display:none">'
                                + '<textarea class="mywork-list-restore-box" id="quote_txt_' + we.id + '"></textarea>'
                                + '<div class="mywork-list-box-f">'
                                + '<div class="mywork-list-box-f-1" id="quoteReplyBtn_' + we.id + '">发布</div>'
                                + '</div>'
                                + '</div>';

                        }

                    } else {
                        inhtml = inhtml + '暂无评论';
                    }
                    inhtml += "</div>";
                    inhtml = inhtml + '</div>'
                        + '</div>'
                        + '</div>';
                    $("#moreInfos").before(inhtml);
                    if (data.list[i].content != "暂未填写") {
                        $("#showReplyBtn_" + logId).bind("click", {'id': logId}, function (v) {
                            showReply("div_" + v.data["id"]);
                        });
                    }
                    $("#replyBtn_" + logId).bind("click", {'id': logId}, function (v) {
                        replyWorkLog("div_" + v.data["id"], v.data["id"]);
                    });
                    $("#addPraise_" + logId).bind("click", {'id': logId}, function (v) {
                        addPraiseCount(v.data["id"]);
                    });
                    if (data.list[i].workLogAttachs != null && data.list[i].workLogAttachs.length > 0) {
                        for (var j = 0; j < data.list[i].workLogAttachs.length; j++) {
                            var wa = data.list[i].workLogAttachs[j];
                            var waId = wa.id;
                            $("#del_" + waId).bind("click", {'id': waId}, function (v) {
                                delAttach(v.data["id"], "attach_" + v.data["id"]);
                            });
                        }
                    }

                    if (data.list[i].workLogExpands != null && data.list[i].workLogExpands.length > 0) {
                        for (var k = 0; k < data.list[i].workLogExpands.length; k++) {
                            var we = data.list[i].workLogExpands[k];
                            $("#quoteReplyImg_" + we.id).bind("click", {'id': we.id}, function (v) {
                                showQuoteReply(v.data["id"], "review_" + v.data["id"]);
                            });
                            $("#quoteReplyBtn_" + we.id).bind("click", {'id': logId, 'weId': we.id}, function (v) {
                                reply(v.data["weId"], v.data["id"]);
                            });
                        }
                    }

                }
                $("#lastBeginTime").val(data.lastBeginTime);
                $("#curPage").val(data.curPage);
            },
            complete: function (XMLHttpRequest, status) {
                $("#dayBody").hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    //回复框显示控制
    function showReply(id) {
        $('#' + id).toggle();
        if ($('#' + id).is(':visible')) {
            $("#textarea_" + id.split("_")[1]).focus();
        }
    }

    //HTML反转义
    function HTMLDecode(text) {
        var temp = document.createElement("div");
        temp.innerHTML = text;
        var output = temp.innerText || temp.textContent;
        temp = null;
        return output;
    }

    //获取前一天记录
    function before() {
        $("#after").removeClass("mywork-list-time-span3");
        $("#after").addClass("mywork-list-time-span5");
        $("#beforeOrAfter").val('0');
        queryFrm.submit();
    }

    //后一天
    function after() {

        var myDate = new Date();
        myDate = myDate.Format("yyyy/MM/dd");
        if ($("#dateCond").val() != myDate.toLocaleString()) {
            $("#beforeOrAfter").val('1');
            queryFrm.submit();
        }
    }

    //选择时间
    function changeDay() {
        var selectDate = $("#sendDate").val();
        //控制头部显示
        var myDate = new Date();
        myDate = myDate.Format("yyyy/MM/dd");
        if (selectDate == myDate.toLocaleString()) {
            $("#after").addClass("mywork-list-time-span3");
            $("#after").removeClass("mywork-list-time-span5");
        } else {
            $("#after").removeClass("mywork-list-time-span3");
            $("#after").addClass("mywork-list-time-span5");
            $("#editDiv").hide();
        }
        $("#dateCond").val(selectDate);
        $("#beforeOrAfter").val('2');
        queryFrm.submit();

    }

    //比较日期大小
    function compareDate(checkStartDate, checkEndDate) {
        var arys1 = new Array();
        var arys2 = new Array();
        if (checkStartDate != null && checkEndDate != null) {
            arys1 = checkStartDate.split('-');
            var sdate = new Date(arys1[0], parseInt(arys1[1] - 1), arys1[2]);
            arys2 = checkEndDate.split('-');
            var edate = new Date(arys2[0], parseInt(arys2[1] - 1), arys2[2]);
            if (sdate > edate) {
                return false;
            } else {
                return true;
            }
        }
    }

    //返回今天
    function returnToday() {
        initForm.submit();
    }

    //删除附件
    function delAttach(attachId, imgId) {
        $("#dayBody").showLoading();
        $.ajax({
            type: "post",
            url: '<%=request.getContextPath()%>/mywork/delAttach',
            data: {
                attachId: attachId
            },
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                if (data.message = "删除成功！") {
                    $("#" + imgId).empty();
                    $("#" + imgId).remove();
                } else {
                    jAlert(data.message, "提示");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $("#dayBody").hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    //根据内容搜索
    function search() {
        var contentCond = $("#contentCond").val();
        $("#curPage").val("0");
        if (contentCond != "") {
            $("#beforeOrAfter").val("3");
            queryFrm.submit();
        } else {
            $("#beforeOrAfter").val("0");
            $("#dateCond").val("");
            queryFrm.submit();
        }
    }

    //评论
    function replyWorkLog(index, id) {
        var reviewContent = $("#textarea_" + id).val();
        if ($.trim(reviewContent).length == 0) {
            jAlert("评论内容不能为空！", "提示");
            return false;
        }
        if (reviewContent.length > 200) {
            jAlert("评论内容不能超过200字！", "提示");
            return false;
        }
        var reviewCount = 0;
        $.ajax({
            type: "post",
            url: '<%=request.getContextPath()%>/mywork/saveReviewExpands',
            data: {
                reviewContent: encodeURIComponent(reviewContent),
                workLogId: id
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.message.indexOf("成功") == -1) {
                    jAlert(data.message, "提示");
                    return false;
                }
                var exId = data.reWle.id;
                if ($("#newExpands_" + id).length > 0) {//存在评论
                    var newExpands = $("#newExpands_" + id).html();
                    var inHtml = '<div id="review_' + exId + '"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
                    inHtml = inHtml + '<div class="mywork-list-filleting-r">' + data.reWle.reviewTime;
                    inHtml = inHtml + '<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                    inHtml = inHtml + '</div>';
                    inHtml = inHtml + '<div id="reply_' + exId + '" style="display:none">';
                    inHtml = inHtml + '<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                    inHtml = inHtml + '<div class="mywork-list-box-f">';
                    inHtml = inHtml + '<div class="mywork-list-box-f-1" id="release_' + exId + '" >发布</div>'
                    inHtml = inHtml + '</div>'
                    inHtml = inHtml + '</div>';
                    inHtml = inHtml + newExpands;
                    $("#newExpands_" + id).html(inHtml);
                    $("#release_" + exId).bind("click", function () {
                        reply(exId, id);
                    });
                    $("#quoteReply_" + exId).bind("click", function () {
                        showQuoteReply(exId, "review_" + exId);
                    });
                    reviewCount = parseInt($("#reviewCount_" + id).html()) + 1;
                    $("#reviewCount_" + id).html(reviewCount);
                } else {//不存在评论
                    var inHtml = '<div class="div_review">'
                    inHtml = inHtml + '<p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;(<span id="reviewCount_' + id + '">1</span>)</span>&nbsp;&nbsp;</span></p>';
                    inHtml = inHtml + '<div id="newExpands_' + id + '"></div>';
                    inHtml = inHtml + '<div id="review' + exId + '"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
                    inHtml = inHtml + '<div class="mywork-list-filleting-r">' + data.reWle.reviewTime;
                    inHtml = inHtml + '<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                    inHtml = inHtml + '</div>';
                    inHtml = inHtml + '<div id="reply_' + exId + '" style="display:none">';
                    inHtml = inHtml + '<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                    inHtml = inHtml + '<div class="mywork-list-box-f">';
                    inHtml = inHtml + '<div class="mywork-list-box-f-1" id="release_' + exId + '">发布</div>';
                    inHtml = inHtml + '</div>';
                    inHtml = inHtml + '</div>';
                    $("#content_expands_" + id).find(".div_review").remove();
                    $("#content_expands_" + id).append(inHtml);
                    $("#release_" + exId).bind("click", function () {
                        reply(exId, id);
                    });
                    $("#quoteReply_" + exId).bind("click", function () {
                        showQuoteReply(exId, "review_" + exId);
                    });
                }

                $("#" + index).hide();
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "");
            }
        });
    }

    //引用评论
    function showQuoteReply(id, contentId) {
        var content = $("#" + contentId).text();
        $("#reply_" + id).toggle();
        $("#quote_txt_" + id).html("//@" + content);
    }

    //引用评论回复
    function reply(id, workLogId) {
        var content = $("#quote_txt_" + id).val();
        if ($.trim(content).length == 0) {
            jAlert("评论内容不能为空！", "提示");
            return false;
        }
        if (content.length > 200) {
            jAlert("评论内容不能超过200字！", "提示");
            return false;
        }
        var reviewCount = parseInt($("#reviewCount_" + workLogId).html()) + 1;
        $.ajax({
            type: "post",
            url: '<%=request.getContextPath()%>/mywork/saveReviewExpands',
            data: {
                reviewContent: encodeURIComponent(content),
                workLogId: workLogId
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                var exId = data.reWle.id;
                if ($("#newExpands_" + workLogId).length > 0) {//存在评论
                    var newExpands = $("#newExpands_" + workLogId).html();
                    var inHtml = '<div id="review_' + exId + '"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
                    inHtml = inHtml + '<div class="mywork-list-filleting-r">' + data.reWle.reviewTime;
                    inHtml = inHtml + '<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                    inHtml = inHtml + '</div>';
                    inHtml = inHtml + '<div id="reply_' + exId + '" style="display:none">';
                    inHtml = inHtml + '<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                    inHtml = inHtml + '<div class="mywork-list-box-f">';
                    inHtml = inHtml + '<div class="mywork-list-box-f-1" id="release_' + exId + '">发布</div>'
                    inHtml = inHtml + '</div>'
                    inHtml = inHtml + '</div>';
                    inHtml = inHtml + newExpands;
                    $("#newExpands_" + workLogId).html(inHtml);
                    $("#release_" + exId).bind("click", function () {
                        reply(exId, workLogId);
                    });
                    $("#quoteReply_" + exId).bind("click", function () {
                        showQuoteReply(exId, "review_" + exId);
                    });
                    reviewCount = parseInt($("#reviewCount_" + workLogId).html()) + 1;
                    $("#reviewCount_" + workLogId).html(reviewCount);
                }

                $("#reply_" + id).hide();
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    //初始化页面
    $(document).ready(function () {
        //控制头部显示
        var myDate = new Date();
        myDate = myDate.Format("yyyy/MM/dd");
        if ($("#dateCond").val() == myDate.toLocaleString()) {
            $("#after").addClass("mywork-list-time-span3");
            $("#after").removeClass("mywork-list-time-span5");
        } else {
            $("#after").removeClass("mywork-list-time-span3");
            $("#after").addClass("mywork-list-time-span5");
            $("#editDiv").hide();
        }
        $(window).goToTop({
            showHeight: 1,//设置滚动高度时显示
            speed: 500 //返回顶部的速度以毫秒为单位
        });

        $('#sendDate').datetimepicker({
            lang: 'ch',
            id: 'selectDate',
            timepicker: false,
            format: 'Y/m/d',
            formatDate: 'Y/m/d',
            step: 1,
            maxDate: '+1970/01/1',
            onSelectDate: changeDay
        });
    });
</script>
</html>
