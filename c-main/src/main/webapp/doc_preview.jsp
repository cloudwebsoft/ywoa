<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="java.util.Date" %>
<%
    Privilege pvg = new Privilege();
    UserDb userDb = new UserDb();
    String realName = userDb.getUserDb(pvg.getUser(request)).getRealName();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>文章预览</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        html, body {
            height: 100%;
        }
        .doc_show_f1 {
            color: #9a9a9a;
        }
        .doc-page {
            background-color: #eeeeee;
            padding: 0 50px;
            height: 100%;
            display: block;
            overflow: auto;
        }
        .doc-page-cont {
            background-color: #fff;
            height: 100%;
            display: block;
            overflow: auto;
        }
        .doc-summary {
            padding: 10px 30px;
        }
        .doc-content {
            padding: 10px 30px;
        }
    </style>
    <script src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="js/goToTop/goToTop.css"/>
    <script type="text/javascript" src="js/goToTop/goToTop.js"></script>
</head>
<body>
<div class="doc-page">
    <div class="doc-page-cont">
        <table cellSpacing="0" cellPadding="5" align="center" border="0" style="border-bottom:1px solid #c0d0dd" width="100%">
            <tbody>
            <tr>
                <td height="30" align="center">
                    <b><span id="title"></span></b>
                </td>
            </tr>
            </tbody>
        </table>
        <table width="100%" align="center">
            <tr>
                <td height="22" align="right">
                    作者：<span class="doc_show_f1"><%=realName%></span>
                    &nbsp;&nbsp;日期：<span class="doc_show_f1"><%=DateUtil.format(new Date(), "yyyy-MM-dd HH:mm")%></span>
                    &nbsp;&nbsp;
                </td>
            </tr>
        </table>
        <div style="line-height:1.5">
            <div class="doc-summary">
                【摘要】
                <span id="summary"></span>
            </div>
            <div id="content" class="doc-content">
            </div>
        </div>
    </div>
</div>
</body>
<script>
    var tabIdOpener = "<%=ParamUtil.get(request, "tabIdOpener")%>";
    $(function() {
        if (window.top.mainFrame) {
            var tabs = window.top.mainFrame.tabpanel.getTabs();
            for (var i=0; i<tabs.length; i++) {
                if (tabs[i].id==tabIdOpener) {
                    var ifrm = tabs[i].content.find('iframe');
                    var win = ifrm[0].contentWindow;
                    if (win==null) {
                        return;
                    }
                    if (typeof(win.getTitle)!="function") {
                        win = win.mainFileFrame;
                    }
                    $('#title').html(win.getTitle());
                    $('#summary').html(win.getSummary());
                    $('#content').html(win.getContent());
                }
            }
        }
        else {
            // lte风格
            if (window.top.o("content-main")) {
                var win = window.top.getTabWin(tabIdOpener);
                if (win==null) {
                    return;
                }
                if (typeof(win.getTitle)!="function") {
                    win = win.mainFileFrame;
                }
                $('#title').html(win.getTitle());
                $('#summary').html(win.getSummary());
                $('#content').html(win.getContent());
            }
        }
    })
</script>
</html>