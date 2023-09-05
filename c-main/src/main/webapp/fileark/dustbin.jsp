<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>回收站</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = StrUtil.getNullString(request.getParameter("op"));
    String dir_code = ParamUtil.get(request, "dir_code");
    Leaf leaf = dir.getLeaf(dir_code);
    String dir_name = "";
    if (leaf != null) {
        dir_name = leaf.getName();
    }
%>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">回收站</td>
    </tr>
    </tbody>
</table>
<%
    String what = ParamUtil.get(request, "what");
    String kind = ParamUtil.get(request, "kind");

    Document doc = new Document();
    String sql = doc.getListSqlOfDustbin(request, dir_code, op, what, kind);

    String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
    if ("".equals(strcurpage)) {
        strcurpage = "1";
    }
    if (!StrUtil.isNumeric(strcurpage)) {
        out.print(StrUtil.makeErrMsg(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "err_id"), "提示")));
        return;
    }
    int pagesize = 15;
    int curpage = Integer.parseInt(strcurpage);
    PageConn pageconn = new PageConn(Global.getDefaultDB(), Integer.parseInt(strcurpage), pagesize);
    ResultIterator ri = pageconn.getResultIterator(sql);
    ResultRecord rr = null;

    Paginator paginator = new Paginator(request, pageconn.getTotal(), pagesize);
    //设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<br>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
    <form name="form1" action="dustbin.jsp?op=search" method="post">
        <tr>
            <td align="center">
                <select id="kind" name="kind">
                    <option value="title">标题</option>
                    <option value="keywords">关键词</option>
                </select>
                &nbsp;
                <input id="what" name="what" size=20 value="<%=what%>">
                &nbsp;
                <input class="btn" type="submit" value="查询"/>
            </td>
        </tr>
    </form>
</table>
<table width="98%" border="0" align="center" class="p9">
    <tr>
        <td width="50%" align="left">
            <input class="btn btn-default" type="button" onclick="doDel()" value="删除"/>
            &nbsp;
            <input class="btn btn-default" type="button" onclick="doResume()" value="恢复"/>
            &nbsp;
            <input class="btn btn-default" type="button" onclick="cleanUp()" value="清空" title="清空回收站">
        </td>
        <td height="24" align="right">
            共<b><%=paginator.getTotal() %></b>页&nbsp;
            每页<b><%=paginator.getPageSize() %></b>条&nbsp;
            页数<b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b>
        </td>
    </tr>
</table>
<table class="tabStyle_1" cellSpacing="0" cellPadding="3" width="98%" align="center">
    <tbody>
    <tr>
        <td width="5%" align="center" noWrap class="tabStyle_1_title">
            <input id="chk" type="checkbox" value="on"/>
        </td>
        <td width="7%" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px">编号</td>
        <td width="28%" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px">标题</td>
        <td width="13%" align="center" noWrap class="tabStyle_1_title">栏目</td>
        <td width="9%" align="center" noWrap class="tabStyle_1_title">作者</td>
        <td width="9%" align="center" noWrap class="tabStyle_1_title">类型</td>
        <td width="9%" align="center" noWrap class="tabStyle_1_title">修改日期</td>
        <td width="20%" align="center" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
        while (ri.hasNext()) {
            rr = (ResultRecord) ri.next();
            String color = StrUtil.getNullStr(rr.getString("color"));
            boolean isBold = rr.getInt("isBold") == 1;
            java.util.Date expireDate = DateUtil.parse(rr.getString("expire_date"));
            int docId = rr.getInt("id");
            String title = rr.getString("title");
    %>
    <tr id="tr<%=docId%>" onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
        <td align="center"><input name="ids" type="checkbox" value="<%=rr.getInt("id")%>"></td>
        <td align="center">
            <%=docId%>
        </td>
        <td style="PADDING-LEFT: 10px"><%if (DateUtil.compare(new java.util.Date(), expireDate) == 2) {%>
            <a href="../doc_show.jsp?id=<%=docId%>" title="<%=rr.getString(2)%>">
                <%
                    if (isBold) {
                        out.print("<B>");
                    }
                    if (!color.equals("")) {
                %>
                <font color="<%=color%>">
                    <%}%>
                    <%=(String) rr.get(2)%>
                    <%if (!color.equals("")) {%>
                </font>
                <%}%>
                <%
                    if (isBold) {
                        out.print("</B>");
                    }
                %>
            </a>
            <%} else {%>
            <a target="_blank" href="../doc_show.jsp?id=<%=docId%>" title="<%=rr.getString(2)%>"><%=(String) rr.get(2)%>
            </a>
            <%}%></td>
        <td align="center">
            <%
                Leaf lf6 = dir.getLeaf(rr.getString("class1"));
                if (lf6 != null) {
                    out.print("<a href='document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(lf6.getCode()) + "'>" + lf6.getName() + "</a>");
                }
            %>
        </td>
        <td align="center">
            <%
                ud = ud.getUserDb(rr.getString("author"));
                String realName = "";
                if (ud != null && ud.isLoaded()) {
                    out.print(StrUtil.getNullStr(ud.getRealName()));
                }
            %>
        </td>
        <td align="center">
            <%
                if (rr.getInt("type") == Document.TYPE_DOC) {
                    out.print("文章");
                } else if (rr.getInt("type") == Document.TYPE_VOTE) {
                    out.print("投票");
                } else if (rr.getInt("type") == Document.TYPE_FILE) {
                    out.print("文件");
                } else {
                    out.print("链接");
                }
            %>
        </td>
        <td align="center"><%
            java.util.Date d = DateUtil.parse(rr.getString("modifiedDate"));
            if (d != null) {
                out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
            }
        %></td>
        <td align="center">
            <%
                if (rr.getInt("type") == Document.TYPE_DOC) {
            %>
            <a href="javascript:;" onclick="addTab('<%=title%>', '<%=request.getContextPath()%>/fwebedit_new.jsp?op=edit&id=<%=docId%>&dir_code=<%=StrUtil.UrlEncode((String)rr.get(1))%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>')">编辑</a>
            &nbsp;&nbsp;
            <%
                }
            %>
            <a href="javascript:;" onclick="addTab('<%=title%>', '<%=request.getContextPath()%>/doc_show.jsp?id=<%=docId%>')">查看</a>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td align="right">
        <%
        String querystr = "op=" + op + "&dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=" + op + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what);
        out.print(paginator.getCurPageBlock("dustbin.jsp?" + querystr));
        %>
        </td>
    </tr>
</table>
</body>
<script>
    $(function () {
        $('#chk').click(function () {
            if ($(this).prop("checked")) {
                selAllCheckBox('ids');
            } else {
                deSelAllCheckBox('ids');
            }
        })
    });

    function doResume() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert("请选择记录", "提示");
            return;
        }

        jConfirm('您确定要恢复么？', '提示', function (r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "resume.do",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        ids: ids,
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            var ary = ids.split(",");
                            for (var i=0; i<ary.length; i++) {
                                $('#tr' + ary[i]).remove();
                            }
                        }
                        jAlert(data.msg, "提示");
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

    function doDel() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert("请选择记录", "提示");
            return;
        }
        jConfirm('您确定要删除么？', '提示', function (r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "delBatchFromDustbin.do",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        ids: ids,
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            var ary = ids.split(",");
                            for (var i=0; i<ary.length; i++) {
                                $('#tr' + ary[i]).remove();
                            }
                        }
                        jAlert(data.msg, "提示");
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

    function cleanUp() {
        jConfirm('您确定要清空回收站么？', '提示', function (r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "cleanUpDustbin.do",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert(data.msg, "提示", function() {
                               window.location.reload();
                            });
                        }
                        else {
                            jAlert(data.msg, "提示");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

    $(function () {
        $('#kind').val('<%=kind%>');
    });
</script>
</html>