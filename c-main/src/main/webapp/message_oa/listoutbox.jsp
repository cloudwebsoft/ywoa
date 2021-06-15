<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp" %>
<%@ page import="com.redmoon.oa.message.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="org.json.*" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
    if (!privilege.isUserLogin(request)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String name = privilege.getUser(request);

    String op = ParamUtil.get(request, "op");
    String kind = ParamUtil.get(request, "kind");
    if (kind.equals(""))
        kind = "title";
    String what = ParamUtil.get(request, "what");
    try {
        com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "kind", kind, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    MessageDb md = new MessageDb();

    String sql = "select id from oa_message where sender=" + StrUtil.sqlstr(name) + " and box=" + MessageDb.OUTBOX + " and is_sender_dustbin = 0";
    if (op.equals("search") && !"".equals(what)) {
        if (kind.equals("receiver")) {
            sql = "select distinct m.send_msg_id from oa_message m, users u where m.receiver=u.name and u.realname like " + StrUtil.sqlstr("%" + what + "%") + " and m.sender=" + StrUtil.sqlstr(name) + " and m.box=" + MessageDb.INBOX;
        } else if (kind.equals("title")) {
            sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
        } else {
            sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
        }
    }
    sql += " order by rq desc";

    int pagesize = 20;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();

    int total = md.getObjectCount(sql);
    paginator.init(total, pagesize);
    //设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

    int id, type;
    String title = "", sender = "", receiver = "", rq = "";
    String bg = "";
    int i = 0;
    Iterator ir = md.list(sql, (curpage - 1) * pagesize, curpage * pagesize - 1).iterator();

    //信息删除
    JSONObject json = new JSONObject();
    boolean isSuccess = false;
    if (op.equals("del")) {
        try {
            isSuccess = Msg.delMsgBySenderDustbin(request);
        } catch (ErrMsgException e) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "消息删除失败：" + e.getMessage()));
            return;
        }
        String querystr = "CPages=" + curpage + "&pagesize=" + pagesize;
        if (isSuccess) {
            json.put("ret", "1");
            json.put("url", querystr);
            json.put("msg", "删除成功!");
        } else {
            json.put("ret", "0");
            json.put("url", "");
            json.put("msg", "删除失败!");
        }
        out.print(json);
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>消息中心</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function selAllCheckBox(checkboxname) {
            var checkboxboxs = document.getElementsByName(checkboxname);
            if (checkboxboxs != null) {
                // 如果只有一个元素
                if (checkboxboxs.length == null) {
                    checkboxboxs.checked = true;
                }
                for (i = 0; i < checkboxboxs.length; i++) {
                    checkboxboxs[i].checked = true;
                }
            }
        }

        function deSelAllCheckBox(checkboxname) {
            var checkboxboxs = document.getElementsByName(checkboxname);
            if (checkboxboxs != null) {
                if (checkboxboxs.length == null) {
                    checkboxboxs.checked = false;
                }
                for (i = 0; i < checkboxboxs.length; i++) {
                    checkboxboxs[i].checked = false;
                }
            }
        }
    </script>
    <style>
        .tabStyle_1 td {
            border: 1px;
            solid: #dadada;
            font-family: "宋体";
        }

        .tabStyle_1 .tabStyle_1_title {
            background-color: #efefef;
            color: black;

        }

        .tabStyle_1_title a {
            color: black;
        }
    </style>
</head>
<body>
<div class="message_content">
    <table cellSpacing="0" cellPadding="0" width="100%">
        <tbody>
        <tr>
            <td class="message_tdStyle_1">发件箱</td>
        </tr>
        </tbody>
    </table>
    <table width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
        <tr>
            <td class="message_btnbox" colspan="2">
                <!-- <img src="../skin/bluethink/images/message/message_back.png" onclick="location.href='javascript:history.go(-1)'" /> -->
                <img src="../skin/bluethink/images/message/message_furbish.png" onclick="window.location.reload()"/>
                <img src="../skin/bluethink/images/message/message_close.png" onClick="doDel()"/>
                <img src="../skin/bluethink/images/message/message_transmit.png" onclick="doTransmit()"/>
            </td>
        </tr>
        <tr class="message_search">
            <td align="left">
                <form action="listoutbox.jsp" method="get" name="formSearch" id="formSearch">
                    按
                    <select id="kind" name="kind">
                        <option value="title">标题</option>
                        <option value="content">内容</option>
                        <option value="receiver">收件人</option>
                    </select>
                    <script>
                        $(function () {
                            o("kind").value = "<%=kind%>";
                        })
                    </script>
                    &nbsp;
                    <input name="what" size="20" value="<%=what%>"/>
                    <input name="button" type="submit" value="搜索" class="btn"/>
                    <input name="op" value="search" type="hidden"/>
                </form>
            </td>
            <td align="right" class="grey5">共 <b><%=paginator.getTotal() %>
            </b> 条　每页<b><%=paginator.getPageSize() %>
            </b> 条　<b><%=curpage %>/<%=totalpages %>
            </b></td>
        </tr>
    </table>
    <form name="form1" method="post" action="msg_do.jsp">
        <input name="box" value="<%=MessageDb.OUTBOX%>" type="hidden"/>
        <table width="98%" align="center" class="message_tabStyle_1">
            <tr class="message_tabStyle_1_tr">
                <td align="center"><input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')"/></td>
                <td width="30%" align="center">标题</td>
                <td align="center" width="24%">收件人</td>
                <td align="center">日期</td>
            </tr>
            <%
                UserMgr um = new UserMgr();
                while (ir.hasNext()) {
                    md = (MessageDb) ir.next();
                    if (!md.isLoaded()) {
                        continue;
                    }
                    i++;
                    id = md.getId();
                    title = md.getTitle();
                    String title1 = md.getTitle();
                    title = md.getTitle();
                    if (title.length() > 30) {
                        title = title.substring(0, 30) + "...";
                    }
                    sender = md.getSender();
                    receiver = md.getReceiver();
                    rq = md.getRq();
                    type = md.getType();

                    String receiversAll = md.getReceiversAll();
                    String[] ary = StrUtil.split(receiversAll, ",");
                    String realNames = "";
                    for (int k = 0; k < ary.length; k++) {
                        UserDb user = um.getUserDb(ary[k]);
                        if (realNames == null || realNames.equals(""))
                            realNames = user.getRealName();
                        else
                            realNames += "," + user.getRealName();
                    }
            %>
            <%
                if (i % 2 == 0)
                    bg = "#E6F7FF";
                else
                    bg = "#ffffff";
                i++; %>
            <tr bgcolor="<%=bg%>">
                <td align="center" width="5%" class="message_line"><input type="checkbox" id="ids" name="ids" value="<%=id%>"></td>
                <td class="message_line"><a href="draft_send.jsp?id=<%=id%>" title="<%=title1 %>"><%=title%>
                </a></td>
                <td align="center" width="27%" class="message_line"><%=realNames%>
                </td>
                <td align="center" width="13%" class="message_line"><%=rq%>
                </td>
            </tr>
            <%}%>
        </table>
    </form>
    <% if (paginator.getTotal() > 0) { %>
    <table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
        <tr>
            <td align="right">
                <%
                    String querystr = "op=" + op + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what);
                    out.print(paginator.getCurPageBlock("listoutbox.jsp?" + querystr));
                %>
            </td>
        </tr>
    </table>
    <%}%>
    <br/>
</div>
</body>
<script>
    function doDel() {
        var ids = getCheckboxValue("ids");
        if (ids == '') {
            jAlert("请选择消息!", "提示");
            return;
        }
        jConfirm('您确定要删除么？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "listoutbox.jsp",
                    data: {
                        op: "del",
                        ids: ids
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        //ShowLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        jAlert(data.msg, "提示");
                        if (data.ret == "1") {
                            window.location.href = "listoutbox.jsp?" + data.url;
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        //HideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, "提示");
                    }
                });
            }
        })
    }

    function doTransmit() {
        var id = getCheckboxValue("ids");
        if (id.indexOf(",") >= 0) {
            jAlert("只能单个转发", "提示");
            return;
        }
        if (id == '') {
            jAlert("请选择消息!", "提示");
            return;
        }
        parent.rightFrame.location.href = 'transmit.jsp?id=' + id;
    }
</script>
</html>
