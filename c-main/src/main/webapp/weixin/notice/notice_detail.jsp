<%@page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.weixin.mgr.WXMessageMgr" %>
<%@page import="com.redmoon.oa.notice.*" %>
<%@page import="com.redmoon.oa.person.*" %>
<%@page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.Global" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();

    UserDb ud = new UserDb();
    ud = ud.getUserDb(userName);
    String realName = ud.getRealName();

    long noticeId = ParamUtil.getLong(request, "id", 0);
    NoticeDb nd = new NoticeDb();
    nd = nd.getNoticeDb(noticeId);
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>通知公告-详情</title>
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

        .author_p {
            text-align: center;
            font-size: 14px;
        }

        .content {
            font-size: 16px;
            color: #666;
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

        .unknow-cell {
            display: none
        }

        .isknow-cell {
            display: none
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">通知公告</h1>
</header>
<div class="mui-content">
    <div class="mui-table-view">
        <div class="mui-table-view-cell">
            <h5 class="title"></h5>
            <p class="author_p"></p>
        </div>
        <div class="mui-table-view-cell">
            <p class="content"></p>
        </div>
        <div class="mui-table-view-cell unknow-cell">
            <p><span class="unknow"></span></p>
        </div>
        <div class="mui-table-view-cell isknow-cell">
            <p><span class="isknow"></span></p>
        </div>
    </div>
    <%
        Vector v = nd.getAttachs();
        if (v.size() > 0) {
    %>
    <ul class="mui-table-view mui-table-view-chevron att_ul">
        <li class="mui-table-view-cell mui-media ">附件列表：</li>
        <%
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                NoticeAttachmentDb nad = (NoticeAttachmentDb) ir.next();
        %>
        <li class="mui-table-view-cell mui-media att-li" fId="<%=nad.getId() %>">
            <div class="mui-slider-handle">
                <a class="attFile" href="javascript:;" link="<%=nad.getVisualPath() + nad.getDiskName() %>">
                    <img class="mui-media-object mui-pull-left"
                         src="../images/file/<%=com.redmoon.oa.android.tools.Tools.getIcon(StrUtil.getFileExt(nad.getDiskName())) %>"/>
                    <div class="mui-media-body">
                        <%=nad.getName() %>
                    </div>
                </a>
            </div>
        </li>
        <%} %>
    </ul>
    <%} %>

    <%
        boolean isReplyExist = true;
        String content = "";
        NoticeReplyDb nnrd = new NoticeReplyDb();
        nnrd.setUsername(userName);
        nnrd.setNoticeid(noticeId);
        nnrd = nnrd.getReply();
        if (nnrd == null) {
            nnrd = new NoticeReplyDb();
            isReplyExist = false;
        } else {
            content = StrUtil.getNullStr(nnrd.getContent());
        }
        if (nd.getIs_reply() == 1) {
            if ("".equals(content) && isReplyExist) {
                if (nd.getIs_forced_response() == 1) {
    %>
    <div class="mui-table-view" id="remindCell">
        <div class="mui-table-view-cell" style="color:red">
            该通知须回复
        </div>
    </div>
    <%} %>
    <form id="formAdd" class="mui-input-group">
        <div class="mui-input-row" data-code="content" data-isnull="false">
            <label><span>回复</span><span style='color:red;'>*</span></label>
            <div style="text-align:center">
                <textarea id="content" name="content" placeholder="请输入回复内容" style="height:150px;"></textarea>
            </div>
        </div>
        <div class="mui-button-row">
            <button type="button" style="margin-left:5px;" class="mui-btn mui-btn-primary mui-btn-outlined btn-ok">确定</button>
        </div>
    </form>
    <%
            }
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        if ((privilege.isUserPrivValid(request, "notice")
                || privilege.isUserPrivValid(request, "notice.dept")) && nd.getUserName().equals(userName)) {
            NoticeReplyDb nrd = new NoticeReplyDb();
            Vector vt = nrd.getNoticeReply((int) noticeId);
            Iterator irnrd = vt.iterator();
    %>
    <ul class="mui-table-view reply-ul" style="display:<%=vt.size()>0?"":"none" %>">
        <%
            while (irnrd.hasNext()) {
                NoticeReplyDb nrd2 = (NoticeReplyDb) irnrd.next();
                //String isReaded = StrUtil.getNullStr(nrd2.getIsReaded());
                if (!StrUtil.getNullStr(nrd2.getContent()).equals("")) {
                    String rRname = ud.getUserDb(nrd2.getUsername()).getRealName();
        %>
        <li class="mui-table-view-cell">
            <div class="reply-header">
                <span class="reply-name"><%=rRname %></span>
                <span class="reply-date"><%=DateUtil.format(nrd2.getReplyTime(), "yyyy-MM-dd HH:mm:ss")%></span>
            </div>
            <div class="reply-content"><%=StrUtil.getNullStr(nrd2.getContent()) %>
            </div>
        </li>
        <% }
        }
        %>
    </ul>
    <%} %>
    <%}%>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script type="text/javascript" src="../js/jq_mydialog.js"></script>
<script>
    var isUniWebview = <%=isUniWebview%>;
    if(!mui.os.plus) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }
    else {
        if (isUniWebview) {
            $('.mui-bar').remove();
        }
    }

    (function ($) {
        $.init({
            swipeBack: true //启用右滑关闭功能
        });
        var ajax_get = function (datas) {
            $.get("../../public/notice/getNotice", datas, function (data) {
                var noticeObj = data.data;
                jQuery(".title").html(noticeObj.title);
                jQuery(".content").html(noticeObj.content);
                jQuery(".author_p").html(noticeObj.userRealName + "发布于：" + noticeObj.createData);
                if (("unKnows" in noticeObj) && ("knows" in noticeObj)) {
                    jQuery(".unknow").html("未查看用户：" + noticeObj.unKnows);
                    jQuery(".isknow").html("已查看用户：" + noticeObj.knows);

                    jQuery('.unknow-cell').show();
                    jQuery('.isknow-cell').show();
                }

            }, "json");
        };
        ajax_get({"id":<%=noticeId%>, "skey":"<%=skey%>"});

    })(mui);

    $(".mui-table-view").on("tap", ".attFile", function () {
        var url = jQuery(this).attr("link");
        var p = url.lastIndexOf(".");
        var ext = url.substring(p + 1);
        if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
            showImg(url);
        }
        else {
            // url得是完整的路径，否则会报400错误
            url = "<%=Global.getFullRootPath(request)%>/" + url;
            if (mui.os.plus) {
                var btnArray = ['是', '否'];
                mui.confirm('您确定要下载么？', '', btnArray, function(e) {
                    if (e.index == 0) {
                        var dtask = plus.downloader.createDownload(url, {}, function (d, status) {
                            if (status == 200) {
                                // 调用第三方应用打开文件
                                plus.runtime.openFile(d.filename, {}, function (e) {
                                    alert('打开失败');
                                });
                            } else {
                                alert("下载失败: " + status);
                            }
                        });
                        dtask.start();
                    }
                });
            }
            else {
                mui.openWindow({
                    "url": url
                })
            }
        }
    });

    function showImg(path) {
        var openPhotoSwipe = function () {
            var pswpElement = document.querySelectorAll('.pswp')[0];
            var items = [{
                src: "<%=request.getContextPath()%>/public/showImg.do?path=" + encodeURI(path),
                w: 964,
                h: 1024
            }
            ];
            // define options (if needed)
            var options = {
                // history & focus options are disabled on CodePen
                history: false,
                focus: false,
                showAnimationDuration: 0,
                hideAnimationDuration: 0
            };
            var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
            gallery.init();
        };
        openPhotoSwipe();
    }

    $(function () {
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

            $.ajax({
                type: "post",
                url: "../../public/notice/addReply.do",
                data: $('#formAdd').serialize() + "&skey=<%=skey%>&id=<%=noticeId%>",
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
                        li += '<span class="reply-name"><%=realName %></span>';
                        li += '<span class="reply-date"><%=DateUtil.format(new Date(), "yyyy-MM-dd")%></span>';
                        li += '</div>';
                        li += '<div class="reply-content">' + $('#content').val() + '</div>';
                        li += '</li>';
                        $ul.prepend(li);

                        $('#formAdd').remove();
                        $('#remindCell').hide();
                    }
                    mui.toast(data.msg);
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    });

    function callJS() {
        return {"btnAddShow": 1, "btnAddUrl": "weixin/notice/notice_add.jsp"};
    }

    var iosCallJS = '{ "btnAddShow":1, "btnAddUrl":"weixin/notice/notice_add.jsp" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
