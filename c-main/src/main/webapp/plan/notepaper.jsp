<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    Privilege aPriv = new Privilege();
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta charset="utf-8">
    <style>
        /*去除获得焦点后的黑框*/
        *, *:focus, *:hover {
            outline: none;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/colorsticker/color-sticker.css">
    <script type="text/javascript" src="../js/colorsticker/colorsticker.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
</body>
<script>
    <%
    JSONArray ary = new JSONArray();
    PlanDb pd = new PlanDb();
    Iterator ir = pd.listNotepaper(userName).iterator();
    while(ir.hasNext()) {
        pd = (PlanDb)ir.next();
        JSONObject json = new JSONObject();
        json.put("stickerId", String.valueOf(pd.getId()));
        json.put("left", String.valueOf(pd.getX()) + "px");
        json.put("top", String.valueOf(pd.getY()) + "px");
        json.put("content", pd.getContent());
        json.put("startTime", DateUtil.format(pd.getMyDate(), "yyyy-MM-dd HH:mm"));
        json.put("shared", pd.isShared());
        ary.put(json);
    }
    %>
    /**导入的便签Object有以下属性：
     *stickerId--用户自定义的便签Id,用于删除便签后便于同时删除后台数据
     *left--便签与浏览器左侧的距离
     *top--便签与浏览器上方的边距
     *content--便签的内容
     *将便签对象存在一个数组中导入
     **/
    var stickers = <%=ary%>;
    $('body').sticker({
        // color:'purple', //便签默认是黄色，可以选择pink,green,blue,purple
        width: '180px',  //便签的宽度
        height: '180px', //便签的高度
        saveStickerCallback: function (sticker) {   //保存便签的回调方法，参数是sticker对象，包括便签的位置和内容信息
            var isAdd = sticker.id < 0;
            var left = parseInt(sticker.left);
            var top = parseInt(sticker.top);
            if (sticker.content == "") {
                $.toaster({
                    "priority": "info",
                    "message": "请输入内容"
                });
                return;
            }
            $.ajax({
                type: "post",
                url: "../public/plan/saveNotepaperContent.do",
                data: {
                    id: sticker.id,
                    startTime: sticker.startTime,
                    left: left,
                    top: top,
                    content: sticker.content
                },
                dataType: "html",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                beforeSend: function (XMLHttpRequest) {
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    $('div[stickerId=' + sticker.id + "]").attr("stickerId", data.id);
                    if (isAdd) {
                        $('div[stickerId=' + data.id + "]").find("button[id='btnShare']").show();
                    }
                    $.toaster({
                        "priority": "info",
                        "message": data.msg
                    });
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        },
        shareStickerCallback: function (sticker) {   //保存便签的回调方法，参数是sticker对象，包括便签的位置和内容信息
            var op = "share";
            var isShared = $('div[stickerId=' + sticker.id + "]").find("button[id='btnShare']").hasClass("share-btn");
            if (isShared) {
                op = "unshare";
            }
            $.ajax({
                type: "post",
                url: "../public/plan/shareNotepaper.do",
                data: {
                    id: sticker.id,
                    op: op
                },
                dataType: "html",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                beforeSend: function (XMLHttpRequest) {
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == "1") {
                        if (op == "share") {
                            $('div[stickerId=' + sticker.id + "]").find("button[id='btnShare']").attr("class", "share-btn");
                        } else {
                            $('div[stickerId=' + sticker.id + "]").find("button[id='btnShare']").attr("class", "unshare-btn");
                        }
                    }
                    $.toaster({
                        "priority": "info",
                        "message": data.msg
                    });
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        },
        closeStickerCallback: function (stickerId) {  //删除便签的回调方法，参数是便签的stickerId
            if (stickerId == -100) {
                jConfirm('您确定要关闭么？', '提示', function (r) {
                    if (r) {
                        $('div[stickerid=' + stickerId + ']').remove();
                    }
                });
                return;
            }
            jConfirm('您确定要置为完成并关闭么？', '提示', function (r) {
                if (r) {
                    $.ajax({
                        type: "post",
                        url: "../public/plan/closeNotepaper.do",
                        data: {
                            id: stickerId,
                        },
                        dataType: "html",
                        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                        beforeSend: function (XMLHttpRequest) {
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "1") {
                                $('div[stickerid=' + stickerId + ']').remove();
                            }
                            $.toaster({
                                "priority": "info",
                                "message": data.msg
                            });
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            });
        },
        dropStickerCallback: function (sticker) {   //保存便签的回调方法，参数是sticker对象，包括便签的位置和内容信息
            var id = sticker.id;
            if (id < 0) {
                return;
            }
            var left = parseInt(sticker.left);
            var top = parseInt(sticker.top);
            $.ajax({
                type: "post",
                url: "../public/plan/saveNotepaperPos.do",
                data: {
                    id: id,
                    left: left,
                    top: top
                },
                dataType: "html",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                beforeSend: function (XMLHttpRequest) {
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == "0") {
                        $.toaster({
                            "priority": "info",
                            "message": data.msg
                        });
                    }
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        }
    }, stickers);//将导入的便签数组作为插件的第二个参数
</script>
</html>