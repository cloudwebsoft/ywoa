<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = StrUtil.getNullString(request.getParameter("op"));

    if (op.equals("getTree")) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf("root");
        DirectoryView dv = new DirectoryView(lf);
        dv.ShowDirectoryAsOptions(request, out, lf, 1);
        return;
    }

    String typeCode = ParamUtil.get(request, "typeCode");
    String title = ParamUtil.get(request, "title");
    String starter = ParamUtil.get(request, "starter");
    String noteStr = LocalUtil.LoadString(request, "res.flow.Flow", "prompt");
    String by = ParamUtil.get(request, "by");

    String fromDate = ParamUtil.get(request, "fromDate");
    String toDate = ParamUtil.get(request, "toDate");

    String myname = ParamUtil.get(request, "userName");
    if (myname.equals("")) {
        myname = privilege.getUser(request);
    }

    String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
    if (strcurpage.equals(""))
        strcurpage = "1";
    if (!StrUtil.isNumeric(strcurpage)) {
        String str = LocalUtil.LoadString(request, "res.flow.Flow", "identifyIllegal");
        out.print(StrUtil.makeErrMsg(str));
        return;
    }

    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();

    int pagesize = ParamUtil.getInt(request, "pageSize", 20);
    int curpage = Integer.parseInt(strcurpage);

    String querystr = "op=" + op + "&userName=" + StrUtil.UrlEncode(myname) + "&typeCode=" + typeCode + "&by=" + by + "&title=" + StrUtil.UrlEncode(title) + "&fromDate=" + fromDate + "&toDate=" + toDate + "&starter=" + StrUtil.UrlEncode(starter);
    String action = ParamUtil.get(request, "action");
    String toa = ParamUtil.get(request, "toa");
    String msg = ParamUtil.get(request, "msg");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>待办流程</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script type="text/javascript" src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="js/flexigrid.js"></script>

    <script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.toaster.flow.js"></script>

    <link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
    <script src="js/datepicker/jquery.datetimepicker.js"></script>

    <style>
        .unreaded {
            font-weight: bold;
        }
    </style>
    <script>
        function onTypeCodeChange(obj) {
            if (obj.options[obj.selectedIndex].value == 'not') {
                jAlert(obj.options[obj.selectedIndex].text + ' <lt:Label res="res.flow.Flow" key="notBeSelect"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                return false;
            }
            window.location.href = "flow_list_doingorreturn.jsp?op=search&typeCode=" + obj.options[obj.selectedIndex].value;
        }

        // 设置选项卡标题
        $(document).ready(function () {
            //setActiveTabTitle(window.document.title);
        });
    </script>
</head>
<body>

<%
    if (toa.equals("ok") && !msg.equals("")) {
%>
<script>
    $.toaster({priority: 'info', message: '<%=msg%>'});
</script>
<%
        msg = "";
    }
    if (action.equals("finishBatch")) {
        WorkflowMgr wm = new WorkflowMgr();
        int count = 0;
        try {
            count = wm.FinishActionBatch(request);
        } catch (ErrMsgException e) {
            String alertStr = e.getMessage();
            alertStr = alertStr.replace("\r\n", "");
            out.print(StrUtil.jAlert_Redirect(alertStr, noteStr, "flow_list_doingorreturn.jsp?" + querystr + "&CPages=" + curpage + "&pageSize=" + pagesize));
            return;
        }

        String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
        out.print(StrUtil.jAlert_Redirect(str, noteStr, "flow_list_doingorreturn.jsp?" + querystr + "&CPages=" + curpage + "&pageSize=" + pagesize));

        return;
    }

    if (!myname.equals(privilege.getUser(request))) {
        if (!(privilege.canAdminUser(request, myname))) {
            out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"), noteStr));
            return;
        }
    }
%>
<table id="searchTable" width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td height="30" align="center">
            <form name="formSearch" action="flow_list_doingorreturn.jsp" method="get">
                &nbsp;<lt:Label res="res.flow.Flow" key="type"/>
                <span id="spanTypeCode">
        <select id="typeCode" name="typeCode" onChange="onTypeCodeChange(this)">
          <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
        </select>
        </span>
                <select id="by" name="by">
                    <option value="title"><lt:Label res="res.flow.Flow" key="tit"/></option>
                    <option value="flowId"><lt:Label res="res.flow.Flow" key="number"/></option>
                </select>
                <input type="text" name="title" value="<%=title%>" style="width:60px">
                <lt:Label res="res.flow.Flow" key="organ"/>
                <input type="text" name="starter" value="<%=starter%>" style="width:60px">
                <lt:Label res="res.flow.Flow" key="startDate"/>
                <input size="8" id="fromDate" name="fromDate" value="<%=fromDate%>"/>
                -
                <input size="8" id="toDate" name="toDate" value="<%=toDate%>"/>
                <input name="op" value="search" type="hidden">
                <input name="userName" value="<%=myname%>" type="hidden">
                <input name="submit" type=submit value='<lt:Label res="res.flow.Flow" key="search"/>' class="tSearch">
            </form>
        </td>
    </tr>
</table>
<%
    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "receive_date";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "desc";

    MyActionDb mad = new MyActionDb();
    String sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and (user_name=" + StrUtil.sqlstr(myname) + " or proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE;
    if (op.equals("search")) {
        sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE;
        if (!"".equals(starter)) {
            sql = "select m.id from flow_my_action m, flow f, users u where m.flow_id=f.id and f.userName=u.name and f.status<>" + WorkflowDb.STATUS_NONE + " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE;
        }
        if (!typeCode.equals("")) {
            sql += " and f.type_code=" + StrUtil.sqlstr(typeCode);
        }

        if (by.equals("title")) {
            if (!title.equals("")) {
                sql += " and f.title like " + StrUtil.sqlstr("%" + title + "%");
            }
        } else if (by.equals("flowId")) {
            if (!StrUtil.isNumeric(title)) {
                String str = LocalUtil.LoadString(request, "res.flow.Flow", "mustNumber");
                out.print(StrUtil.Alert_Back(str));
                return;
            } else {
                sql += " and f.id=" + title;
            }
        }
        if (!fromDate.equals("")) {
            sql += " and f.mydate>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
        }
        if (!toDate.equals("")) {
            java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
            d = DateUtil.addDate(d, 1);
            String toDate2 = DateUtil.format(d, "yyyy-MM-dd");
            sql += " and f.mydate<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd");
        }
        if (!"".equals(starter)) {
            sql += " and u.realname like " + StrUtil.sqlstr("%" + starter + "%");
        }
    }

    sql += " and f.status<>" + WorkflowDb.STATUS_DELETED + " and f.status<>" + WorkflowDb.STATUS_DISCARDED;
    sql += " order by " + orderBy + " " + sort;

    // out.print(sql);

    ListResult lr = mad.listResult(sql, curpage, pagesize);
    long total = lr.getTotal();
    Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

    int start = (curpage - 1) * pagesize;
    int end = curpage * pagesize;
%>
<table id="grid" width="1000">
    <thead>
    <tr>
        <th width="50" align="center" abbr="id">ID</th>
        <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>
        <th width="32" align="center" abbr="flow_level"><lt:Label res="res.flow.Flow" key="rating"/></th>
        <%}%>
        <th width="600" abbr="title"><lt:Label res="res.flow.Flow" key="tit"/></th>
        <th width="70" abbr="userName"><lt:Label res="res.flow.Flow" key="organ"/></th>
        <th width="120" abbr="begin_date"><lt:Label res="res.flow.Flow" key="startTime"/></th>
        <th width="98"><lt:Label res="res.flow.Flow" key="remainTime"/></th>
        <th width="60" abbr="is_checked"><lt:Label res="res.flow.Flow" key="state"/></th>
        <th width="130" align="center"><lt:Label res="res.flow.Flow" key="operate"/></th>
    </tr>
    <thead>
    <tbody>
    <%
        java.util.Iterator ir = lr.getResult().iterator();
        com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
        Directory dir = new Directory();
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        while (ir.hasNext()) {
            mad = (MyActionDb) ir.next();
            WorkflowDb wfd = new WorkflowDb();
            wfd = wfd.getWorkflowDb((int) mad.getFlowId());
            String userName = wfd.getUserName();
            String userRealName = "";
            if (userName != null) {
                UserDb user = um.getUserDb(wfd.getUserName());
                userRealName = user.getRealName();
            }
            Leaf ft = dir.getLeaf(wfd.getTypeCode());

    %>
    <tr id="<%=mad.getId()%>">
        <td align="center">
            <%
                String cls = "class=\"readed\"";
                if (!mad.isReaded()) {
                    cls = "class=\"unreaded\"";
                }
            %>
            <%if (ft.getType() == Leaf.TYPE_LIST) {%>
            <a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" <%=cls%> link="flow_dispose.jsp?myActionId=<%=mad.getId()%>" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', 'flow_dispose.jsp?myActionId=<%=mad.getId()%>')"><%=wfd.getId()%>
            </a>
            <%
            } else {
                wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                if (wpd.isLight()) {
            %>
            <a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" <%=cls%> link="flow_dispose_light.jsp?myActionId=<%=mad.getId()%>'"
               onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', 'flow_dispose_light.jsp?myActionId=<%=mad.getId()%>')"><%=wfd.getId()%>
            </a>
            <%
            } else {
            %>
            <a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" <%=cls%> link="flow_dispose_free.jsp?myActionId=<%=mad.getId()%>'"
               onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', 'flow_dispose_free.jsp?myActionId=<%=mad.getId()%>')"><%=wfd.getId()%>
            </a>
            <%
                    }
                }
            %>
        </td>
        <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>
        <td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%>
        </td>
        <%}%>
        <td>
            <%if (ft.getType() == Leaf.TYPE_LIST) {%>
            <a href="javascript:;" <%=cls%> title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" onclick="addTab('<%=wfd.getTitle()%>', 'flow_dispose.jsp?myActionId=<%=mad.getId()%>')"><%=wfd.getTitle()%>
            </a>
            <%
            } else {
                wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                if (wpd.isLight()) {
            %>
            <a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" <%=cls%> link="flow_dispose_light.jsp?myActionId=<%=mad.getId()%>'"
               onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "").replace("&#039;","\\&#039;")%>', 'flow_dispose_light.jsp?myActionId=<%=mad.getId()%>')"><%=MyActionMgr.renderTitle(request, wfd)%>
            </a>
            <%
            } else {
            %>
            <a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" <%=cls%> link="flow_dispose_free.jsp?myActionId=<%=mad.getId()%>'"
               onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', 'flow_dispose_free.jsp?myActionId=<%=mad.getId()%>')"><%=wfd.getTitle()%>
            </a>
            <%
                    }
                }
            %>
            <%
                if (!mad.isReaded()) {
            %>
            &nbsp;
            <img src="images/icon_new.gif"/>
            <%
                }
            %>
        </td>
        <td align="center"><%=userRealName%>
        </td>
        <td align="center"><%=DateUtil.format(wfd.getBeginDate(), "MM-dd HH:mm")%>
        </td>
        <td align="center">
            <%
                String remainDateStr = "";
                if (mad.getExpireDate() != null && DateUtil.compare(new java.util.Date(), mad.getExpireDate()) == 2) {
                    int[] ary = DateUtil.dateDiffDHMS(mad.getExpireDate(), new java.util.Date());
                    String str_day = LocalUtil.LoadString(request, "res.flow.Flow", "day");
                    String str_hour = LocalUtil.LoadString(request, "res.flow.Flow", "h_hour");
                    String str_minute = LocalUtil.LoadString(request, "res.flow.Flow", "minute");
                    remainDateStr = ary[0] + " " + str_day + ary[1] + " " + str_hour + ary[2] + " " + str_minute;

                    out.print(remainDateStr);
                }%>
        </td>
        <td align="center" class="<%=WorkflowActionDb.getStatusClass(mad.getActionStatus())%>"><%=WorkflowActionDb.getStatusName(mad.getActionStatus())%>
        </td>
        <td align="center">
            <%
                String suspend = "";
                if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_SUSPEND) {
                    suspend = mad.getCheckStatusName();
                }
                if (ft.getType() == Leaf.TYPE_LIST) {%>
            <a href="flow_dispose.jsp?myActionId=<%=mad.getId()%>"><lt:Label res="res.flow.Flow" key="chandle"/><%=suspend%>
            </a>
            <%
            } else {
                wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                // out.print("wpd.isLight()=" + wpd.isLight());
                if (wpd.isLight()) {
            %>
            <a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" <%=cls%> link="flow_dispose_light.jsp?myActionId=<%=mad.getId()%>'"
               onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;","\\&#039;")%>', 'flow_dispose_light.jsp?myActionId=<%=mad.getId()%>')"><lt:Label res="res.flow.Flow" key="chandle"/></a>
            <%
            } else {
            %>
            <a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='processingFlow'/>：<%=wfd.getTitle()%>" <%=cls%> link="flow_dispose_free.jsp?myActionId=<%=mad.getId()%>'"
               onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', 'flow_dispose_free.jsp?myActionId=<%=mad.getId()%>')"><lt:Label res="res.flow.Flow" key="chandle"/></a>
            <%
                    }
                }
            %>
            &nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='focusProcess'/>" onclick="favorite(<%=wfd.getId()%>)"><lt:Label res="res.flow.Flow" key="attention"/></a>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>
<%
    // out.print(paginator.getCurPageBlock("flow_list_doingorreturn.jsp?"+querystr));
%>
</body>
<script>
    var flex;

    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    <%
                    if (cfg.getBooleanProperty("canFlowDisposeBatch")) {
                    %>
                    {name: '<lt:Label res="res.flow.Flow" key="agree"/>', bclass: 'pass', onpress: action},
                    <%}%>
                    {name: '<lt:Label res="res.flow.Flow" key="condition"/>', bclass: 'btnseparator', type: 'include', id: 'searchTable'}
                ],
                /*
                searchitems : [
                    {display: 'ISO', name : 'iso'},
                    {display: 'Name', name : 'name', isdefault: true}
                    ],
                */
                sortname: "<%=orderBy%>",
                sortorder: "<%=sort%>",
                url: false,
                usepager: true,
                checkbox: false,
                page: <%=curpage%>,
                total: <%=total%>,
                useRp: true,
                rp: <%=pagesize%>,

                //title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: true,
                checkbox: true,

                onChangeSort: changeSort,

                onChangePage: changePage,
                onRpChange: rpChange,
                onReload: onReload,
                /*
                onRowDblclick: rowDbClick,
                onColSwitch: colSwitch,
                onColResize: colResize,
                onToggleCol: toggleCol,
                */
                autoHeight: true,
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );

        $('#fromDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        $('#toDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });

        <%if (!"".equals(by)) {%>
        o("by").value = "<%=by%>";
        <%}%>

        $(function () {
            $.ajax({
                type: "post",
                url: "flow_list_doingorreturn.jsp",
                data: {
                    op: "getTree",
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                },
                success: function (data, status) {
                    $("#typeCode").empty();

                    data = "<option value=''><lt:Label res="res.flow.Flow" key="limited"/></option>" + data;

                    $("#typeCode").append(data);

                    o("typeCode").value = "<%=typeCode%>";
                },
                complete: function (XMLHttpRequest, status) {
                },
                error: function (XMLHttpRequest, textStatus) {
                    jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                }
            });

        });

    });


    function changeSort(sortname, sortorder) {
        window.location.href = "flow_list_doingorreturn.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "flow_list_doingorreturn.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "flow_list_doingorreturn.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    function action(com, grid) {
        if (com == '<lt:Label res="res.flow.Flow" key="agree"/>') {
            var selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('<lt:Label res="res.flow.Flow" key="selectRecord"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                return;
            }
            //if (!confirm('<lt:Label res="res.flow.Flow" key="isArgee"/>'))
            //return;
            jConfirm('<lt:Label res="res.flow.Flow" key="isArgee"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
                if (!r) {
                    return;
                } else {
                    var ids = "";
                    $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                        if (ids == "")
                            ids = $(this).val();
                        else
                            ids += "," + $(this).val();
                    });

                    window.location.href = "flow_list_doingorreturn.jsp?action=finishBatch&ids=" + ids + "&<%=querystr%>&CPages=<%=curpage%>&pageSize=" + flex.getOptions().rp;

                }
            });

        }
    }

    function onClickDoc(e) {
        var obj = isIE() ? event.srcElement : e.target
        if (isIE() && event.shiftKey) {
            if (obj.tagName == "A") {
                // if (obj.target=="mainFrame") {
                window.open(obj.getAttribute("link"));
                // }
                return false;
            }
        }
    }

    $(document).ready(function () {
        document.onclick = onClickDoc;
    });

    function favorite(id) {
        $.ajax({
            type: "post",
            url: "flow/favorite.do",
            data: {
                flowId: id
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#grid').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                } else {
                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#grid').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    }

    $(function () {
        <%if (myname.equals(privilege.getUser(request))) {%>
        setActiveTabTitle(document.title);
        <%} else {%>
        setActiveTabTitle('<%=new UserDb(myname).getRealName()%>的' + document.title);
        <%}%>
    });
</script>
</html>
