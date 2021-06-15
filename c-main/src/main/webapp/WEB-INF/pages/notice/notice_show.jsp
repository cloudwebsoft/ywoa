<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>通知-详情</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css/common/common.css"/>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css/message/message.css"/>
    <style>
        .reply_div {
            margin: 0 auto;
            width: 96%;
            font-family: "microsoft yahei";
            font-size: 14px;
            border: 10px solid #efefef;
        }

        .reply_title_div {
            margin: 10px;
        }

        .con_btn {
            color: white;
            background-color: #85c4f0;
            text-align: center;
            font-weight: bold;
            line-height: 20px;
            padding-right: 10px;
            padding-left: 10px;
            height: 24px;
            border: 1px solid #85c4f0;
            cursor: pointer;
            -moz-border-radius: 3px;
            -webkit-border-radius: 3px;
            border-radius: 3px;
        }

        .reply_title {
            font-weight: bold;
        }

        .reply_btn {
            float: right;
        }

        * {
            margin: 0;
            padding: 0;
        }

        .myTextarea {
            display: block;
            margin: 8px auto;
            overflow: hidden;
            width: 100%;
            font-size: 14px;
            line-height: 24px;
            text-indent: 1em;
            height: 48px;
            border: solid 1px #ffa200;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            margin: 10px 0px;
            height: 48px;
        }

        ul li {
            list-style: none;
        }

        .org_btn {
            background: #ffc24d;
            border: 0px;
        }

        .right {
            float: right;
        }

        .reply_date {
            margin-left: 20px
        }

        .clearfix {
            zoom: 1;
        }

        .clearfix:before, .clearfix:after {
            content: '';
            display: table;
        }

        .clearfix:after {
            clear: both;
        }

        .reply_ul li {
            font-size: 14px;
            margin: 10px;
            margin-bottom: 0px;
            padding: 10px 0px;
            border-bottom: 1px solid #EEEEEE;
        }

        .reply_name {
            color: #85c4f0;
        }

        .tips {
            background-color: #FFE081;
            margin: 0 auto;
            font-size: 14px;
            color: #ab701b;
            padding-top: 10px;
            position: absolute;
            top: 0px;
            margin-top: 10px;
            margin-right: 10px;
            width: 100%;
            padding-bottom: 10px;
            text-align: center;

        }

        .tips .icon_close {
            display: inline-block;
            width: 16px;
            height: 16px;
            position: absolute;
            right: 0px;
            top: 0px;

            background: url(../images/close.png) no-repeat;
        }

        .li_re_content {
            padding: 5px;
            word-break: break-all;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../inc/upload.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery.form.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
    <link type="text/css" rel="stylesheet" href="../ueditor/js/ueditor/third-party/video-js/video-js.css"/>
    <script language="javascript" type="text/javascript" src="../ueditor/js/ueditor/third-party/video-js/video.js"></script>
    <script language="javascript" type="text/javascript" src="../ueditor/js/ueditor/third-party/video-js/html5media.min.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script>
        var uEditor;

        function loadMenu() {
            if (parent.leftFrame != null) {
                parent.leftFrame.location.href = "left_menu.jsp";
            }

            uEditor = UE.getEditor('myEditor', {
                initialContent: '<span style="color:gray;font-size:14px;">快速回复</span>',//初始化编辑器的内容
                toolleipi: true,//是否显示，设计器的 toolbars
                textarea: 'content',
                enableAutoSave: false,
                //选择自己需要的工具按钮名称,此处仅选择如下五个
                toolbars: [[
                    'fullscreen', 'undo', 'redo', '|',
                    'bold', 'italic', 'underline', '|', 'forecolor',
                    'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                    'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                    'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify'
                ]],
                //focus时自动清空初始化时的内容
                //autoClearinitialContent:true,
                //关闭字数统计
                wordCount: false,
                //关闭elementPath
                elementPathEnabled: false,
                //默认的编辑区域高度
                initialFrameHeight: 150
                ///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
                //更多其他参数，请参考ueditor.config.js中的配置项
            });

            uEditor.addListener('focus', function () {
                var content = uEditor.getContentTxt();
                if (content == "快速回复") {
                    uEditor.setContent("");
                }
            });
            uEditor.addListener('blur', function () {
                var content = uEditor.getContent();
                if (content == "") {
                    uEditor.setContent("<span style='color:gray;font-size:14px;'>快速回复</span>");
                }
            });
        }

        function form_onsubmit() {
            errmsg = "";
            if (uEditor.getContentTxt() == "" || uEditor.getContentTxt() == '快速回复')
                errmsg += "请填写内容！\n"

            if (errmsg != "") {
                jAlert(errmsg, "提示");
                return false;
            } else {
                return true;
            }
        }
    </script>
    <style>
        .attImg {
            border: none;
            max-width: 800px;
            cursor: pointer;
        }
        .mybtn {
            background-color: #87c3f1 !important;
            font-weight: bold;
            text-align: center;
            line-height: 35px;
            height: 35px;
            width: 120px;
            padding-right: 8px;
            padding-left: 8px;
            -moz-border-radius: 3px;
            -webkit-border-radius: 3px;
            border-radius: 3px;
            behavior: url(../../../skin/common/ie-css3.htc);
            cursor: pointer;
            color: #fff;
            border-top-width: 0px;
            border-right-width: 0px;
            border-bottom-width: 0px;
            border-left-width: 0px;
            border-top-style: none;
            border-right-style: none;
            border-bottom-style: none;
            border-left-style: none;
        }
    </style>
</head>
<body onload="loadMenu()">
<c:if test="${notice.isForcedResponse==1 && notice.notReplied}">
<div class="tips">
    <span class="icon_close"></span>
    该通知须回复
</div>
</c:if>
<div id="treeBackground" class="treeBackground"></div>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1" style="border: none">
            <img src="../images/left/icon-notice.gif"/>
            <a href="list.do">通知公告</a>
        </td>
    </tr>
    </tbody>
</table>
<table width="100%" border="0" class="tabStyle_1 percent98"
       style="table-layout: fixed; word-wrap: break-word; margin-top: 15px;">
    <tr>
        <td colspan=2 align="center" valign="middle"
            class="tabStyle_1_title">${notice.title}
        </td>
    </tr>
    <tr>
        <td colspan=2 valign="top">
            <div class="msgContent">
                <div style="margin: 10px 0px 15px 0px; text-align: center">
                    发布者：${notice.user.realName}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    发布日期：
                    <c:if test="${notice.createDate!=null}">
                        ${notice.createDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
                    </c:if>
                </div>
                <div style="line-height: 1.5">${notice.content}
                </div>
                <div style="margin-top: 10px">
                    <c:if test="${notice.flowId>0}">
                    <a href="javascript:;" onclick="addTab('流程消息','flow_modify.jsp?flowId=${notice.flowId}')">
                        <span style="color:blue">查看流程</span>
                    </a>
                    </c:if>
                </div>
                <c:forEach items="${notice.oaNoticeAttList}" var="att">
                <div>
                    <img src="../images/attach2.gif" width="17" height="17"/>
                    <a target="_blank" href="getfile.do?noticeId=${notice.id}&attachId=${att.id}">${att.name}
                    </a>
                </div>
                <c:if test="${att.image}">
                <div style="margin-top: 10px">
                    <img class="attImg"
                         src="../../../img_show.jsp?path=${att.visualPath}/${att.diskName}"
                         title="点击查看原图"
                         onclick="window.open('../../../img_show.jsp?path=${att.visualPath}/${att.diskName}')"/>
                </div>
                </c:if>
                </c:forEach>
                <c:if test="${notice.isShow && (isNoticeAll || isNoticeMgr) && (myUserName==notice.userName)}">
                    <div style="margin-top: 15px">
                        已查看通知的用户：${fn:length(readedList)}人
                        <br/>
                        <c:forEach items="${readedList}" var="reply">
                            <c:url value="user_info.jsp" var="url">
                                <c:param name="userName" value="${reply.userName}"/>
                            </c:url>
                            <div class='userItem'><a href="javascript:;" onclick="addTab('${reply.user.realName}', '${url}')">${reply.user.realName}</a></div>
                        </c:forEach>
                    </div>
                    <br/>
                    <div style="clear: both; padding-top: 5px">
                        未查看通知的用户：${fn:length(notReadedList)}人
                        <br/>
                        <c:forEach items="${notReadedList}" var="reply">
                            <c:url value="user_info.jsp" var="url">
                                <c:param name="userName" value="${reply.user.userName}"/>
                            </c:url>
                            <div class='userItem'><a href="javascript:;" onclick="addTab('${reply.user.realName}', '${url}')">${reply.user.realName}</a></div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </td>
    </tr>
    <tr class="message_style_tr">
        <td align="center" colspan="2">
            <input type="button" class="mybtn" onclick="window.location.href='list.do'" value="返回"/>
        </td>
    </tr>
</table>
<form name="form2" id="form2" action="" method="post" enctype="multipart/form-data">
    <div style="display: none; width: 100%">
        <table width="98%">
            <tr>
                <td colspan="2" class="showMsg_Table_td" style="width: 100%;" style="border:none">
                    <div id="myEditor" style="height: 100px; width: 100%"></div>
                    <input type="hidden" id="noticeid" name="noticeid" value="${notice.id}"/>
                    <input type="hidden" id="uName" name="uName" value="${myUserName}"/>
                    <input type="hidden" id="isShow" name="isShow" value="${notice.isShow}"/>
                </td>
            </tr>
            <tr class="message_style_tr">
                <td colspan="2" class="showMsg_Table_td" align="center" style="border: none">
                    <input name="button" type="submit" value="确定" style="margin-top: 10px" class="blue_btn_90"/>
                </td>
            </tr>
        </table>
    </div>
</form>
<c:if test="${notice.isReply==1}">
<div class="reply_div">
    <div class="reply_title_div clearfix">
        <span class="reply_title">
        回复
        </span>
        <c:if test="${canReply}">
        <textarea name="myReplyTextareaContent" id="myReplyTextareaContent" class="myTextarea"></textarea>
        <input type="button" name="hf" class="con_btn org_btn right btnReply" value="回复" noticeId="${notice.id}"/>
        <div style="clear:both"></div>
        </c:if>
        <c:if test="${!canReply && myReply != null}">
        <ul class="reply_ul" >
            <li>
                <div>
                    <span class="reply_name">${myReply.user.realName}</span>
                    <span class="reply_date">
                        ${myReply.replyTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
                    </span>
                </div>
                <div class="li_re_content">${myReply.content}</div>
            </li>
        </ul>
        </c:if>
    </div>
    <c:if test="${isNoticeAll || isNoticeMgr}">
    <ul class="reply_ul">
        <c:forEach items="${replyList}" var="reply">
        <li>
            <div>
                <span class="reply_name">${reply.user.realName}</span>
                <span class="reply_date">
                    ${reply.replyTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
                </span>
            </div>
            <div class="li_re_content">${reply.content}
            </div>
        </li>
        </c:forEach>
    </ul>
    </c:if>
</div>
</c:if>

</body>
<script>
    $(document).ready(function () {
        $(".icon_close").click(function () {
            $(".tips").hide();

        })

        $(".btnReply").click(function () {
            var myTextarea = $(".myTextarea").val();
            var noticeId = $(this).attr("noticeId");
            var data = {"content": myTextarea, "noticeId": noticeId}
            $.ajax({
                type: "post",
                url: "reply/reply.do",
                data: data,
                dataType: "json",
                beforeSend: function (XMLHttpRequest) {
                    $("body").showLoading();
                },
                complete: function (XMLHttpRequest, status) {
                    $("body").hideLoading();
                },
                success: function (data, status) {
                    var res = data.ret;
                    if (res == 1) {
                        jAlert_Redirect("操作成功！", "提示", window.location.href);
                    } else {
                        jAlert(data.msg, "提示");
                    }
                },
                error: function () {
                    jAlert("操作失败！", "提示");
                }
            });
        })

        var options = {
            success: showResponse,  // post-submit callback
            beforeSubmit: form_onsubmit,
            url: "show.do?op=reply&noticeid=${notice.id}&${myUserName}&isShow=${notice.isShow}"
        };
        $('#form2').submit(function () {
            $(this).ajaxSubmit(options);
            return false;
        });
    });

    function showResponse(data) {
        data = $.parseJSON(data);
        if (data.ret == "1") {
            jAlert(data.msg, "提示");
            window.location.href = "show.do?isShow=${notice.isShow}&id=" + $("#noticeid").val();
        }
    }
</script>
</html>
