<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<jsp:useBean id="noticeDb" scope="page" class="com.redmoon.oa.notice.NoticeDb"/>
<jsp:useBean id="deptUserDb" scope="page" class="com.redmoon.oa.dept.DeptUserDb"/>
<%
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals("")) skincode = UserSet.defaultSkin;
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
    boolean isNoticeAll = privilege.isUserPrivValid(request, "notice");
    // boolean isNoticeAdd = privilege.isUserPrivValid(request, "notice.all"); // notice.all权限已取消
    boolean isNoticeMgr = privilege.isUserPrivValid(request, "notice.dept");
    String userName = privilege.getUser(request);
    int curpage = ParamUtil.getInt(request, "CPages", 1);
    String op = ParamUtil.get(request, "op");

    Vector ud = new Vector();
    ud = deptUserDb.getDeptsOfUser(userName);
    Iterator ir = ud.iterator();
    String sql = "";

    String fromDate = ParamUtil.get(request, "fromDate");
    String toDate = ParamUtil.get(request, "toDate");

    String what = ParamUtil.get(request, "what");
    String cond = ParamUtil.get(request, "cond");
    if (cond.equals(""))
        cond = "title";
    if (!cond.equals("title") && !cond.equals("content"))
        return;

    boolean isSearch = op.equals("search");

    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "id";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "desc";

    String strCurDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");

    sql = "select id from oa_notice where 1=1";
    if (!privilege.isUserPrivValid(request, "admin") && !isNoticeAll) {
        sql += " and begin_date<=" + SQLFilter.getDateStr(strCurDay, " yyyy - MM - dd ") + " and(end_date is null or end_date>=" + SQLFilter.getDateStr(strCurDay, " yyyy - MM - dd ") + ")";
    }
    if (isSearch) {
        if (!"".equals(what)) {
            sql += " and " + cond + " like '%" + what + "%'";
        }

        if (!fromDate.equals("") && !toDate.equals("")) {
            java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
            d = DateUtil.addDate(d, 1);
            String toDate2 = DateUtil.format(d, "yyyy-MM-dd");
            sql += " and (create_date>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd") + " and create_date<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd") + ")";
        } else if (!fromDate.equals("")) {
            sql += " and create_date>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
        } else if (fromDate.equals("") && !toDate.equals("")) {
            sql += " and create_date<=" + SQLFilter.getDateStr(toDate, "yyyy-MM-dd");
        }
    }

    if (!privilege.isUserPrivValid(request, "admin") && !isNoticeAll) {
        sql += " and (id in (select notice_id from oa_notice_reply where user_name = " + StrUtil.sqlstr(userName) + ")) or user_name=" + StrUtil.sqlstr(userName);
    }

    sql += " order by " + orderBy + " " + sort;
    // out.print(sql);
    int pagesize = ParamUtil.getInt(request, "pageSize", 20);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>通知列表</title>
    <%@ include file="../inc/nocache.jsp" %>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
<%
    if (op.equals("del")) {
        boolean isUserPrivValid = privilege.isUserPrivValid(request, "notice") || privilege.isUserPrivValid(request, "notice.dept");
        if (!isUserPrivValid) {
            out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
        String ids = ParamUtil.get(request, "id");
        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            out.print(StrUtil.jAlert_Back("请选择通知！", "提示"));
            return;
        }
        try {
            for (int i = 0; i < ary.length; i++) {
                long id = StrUtil.toInt(ary[i]);
                NoticeMgr nd = new NoticeMgr();
                NoticeAttachmentMgr nam = new NoticeAttachmentMgr();
                NoticeReplyMgr nrm = new NoticeReplyMgr();
                nd.del(request, id);
                nrm.delReply(id);
                nam.delAttachment(id);
            }
        } catch (ErrMsgException e) {
%>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
<%
            out.print(SkinUtil.makeErrMsg(request, e.getMessage(), true));
            return;
        }
        out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "notice_list.jsp?CPages=" + curpage));
        return;
    }
    ListResult lr = noticeDb.listResult(sql, curpage, pagesize);
    Iterator iterator = lr.getResult().iterator();
    int total = lr.getTotal();
    Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td width="48%" height="30" align="left">
            <form action="notice_list.jsp" method="get">
                <input id="op" name="op" value="search" type="hidden"/>
                <select id="cond" name="cond">
                    <option value="title">标题</option>
                    <option value="content">内容</option>
                </select>
                <input id="what" name="what" size="15" value="<%=what%>"/>
                &nbsp;从
                <input size="8" id="fromDate" name="fromDate" value="<%=fromDate%>"/>
                至
                <input size="8" id="toDate" name="toDate" value="<%=toDate%>"/>
                <input class="tSearch" value="搜索" type="submit"/>
            </form>
        </td>
    </tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
    <thead>
    <tr>
        <th width="371" style="cursor:pointer">标题</th>
        <th width="136" align="center" style="cursor:pointer">发布者</th>
        <th width="102" align="center" style="cursor:pointer">类别</th>
        <th width="169" align="center" style="cursor:pointer">有效期</th>
        <th width="171" align="center" style="cursor:pointer">发布日期</th>
        <!-- <th width="120" align="center" class="tabStyle_1_title">操作</th> -->
    </tr>
    </thead>
    <tbody>
    <%
        UserDb user = new UserDb();
        java.util.Date curDay = DateUtil.parse(strCurDay, "yyyy-MM-dd");
        while (iterator.hasNext()) {
            NoticeDb nd = (NoticeDb) iterator.next();
    %>
    <tr id="<%=nd.getId()%>">
        <td>
            <%if (nd.getLevel() > 0) {%>
            <img src="../images/important_r.png" align="absmiddle" title="重要通知"/>
            <%}%>
            <a href="notice_detail.jsp?id=<%=nd.getId()%>&isShow=<%=nd.getIsShow()%>&flowId=<%=nd.getFlowId() %>">
                <%if (DateUtil.compare(nd.getEndDate(), curDay) == 2) {%>
                <font color="#cccccc"><%=nd.getTitle()%>
                </font>
                <%} else if (DateUtil.compare(nd.getBeginDate(), curDay) == 1) {%>
                <font color="#ffcccc"><%=nd.getTitle()%>
                </font>
                <%
                } else {
                    String title = nd.getTitle();
                    if (nd.isBold())
                        title = "<b>" + title + "</b>";
                    if (!nd.getColor().equals(""))
                        title = "<font color='" + nd.getColor() + "'>" + title + "</font>";
                %>
                <%=title%>
                <%}%>
            </a>
            <%
                if (!nd.isUserReaded(userName) && (nd.getEndDate() == null || !nd.getEndDate().before(curDay))) {
            %>
            &nbsp;
            <img src="../images/icon_new.gif"/>
            <%
                }
            %>
        </td>
        <td><%=user.getUserDb(nd.getUserName()).getRealName()%>
        </td>
        <td>
            <%=(nd.getIsDeptNotice() == 1 ? "部门通知" : "公共通知")%>
        </td>
        <td>
            <%=(DateUtil.format(nd.getBeginDate(), "yy-MM-dd"))%>
            <%if (nd.getEndDate() != null) {%>
            &nbsp;~&nbsp;<%=(DateUtil.format(nd.getEndDate(), "yy-MM-dd"))%>
            <%}%>
        </td>
        <td align="center"><%=(DateUtil.format(nd.getCreateDate(), "yy-MM-dd"))%>
        </td>
        <!-- <td align="center"><a href="notice_detail.jsp?id=<%=nd.getId()%>&isShow=<%=nd.getIsShow()%>&flowId=<%=nd.getFlowId() %>">查看</a>
          <%if(nd.getFlowId()>0){ %>
          		&nbsp;&nbsp;<a href="../flow_modify.jsp?flowId=<%=nd.getFlowId() %>">流程</a>
          <%} %>
          </td> -->
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<%
    String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&fromDate=" + fromDate + "&toDate=" + toDate + "&cond=" + cond;
    // out.print(paginator.getPageBlock(request,"notice_list.jsp?"+querystr));
%>
<script>
    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "notice_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "notice_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "notice_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    <% if (isNoticeMgr || isNoticeAll){ %>
                    {name: '添加', bclass: 'add', onpress: action},
                    {name: '修改', bclass: 'edit', onpress: action},
                    {name: '删除', bclass: 'delete', onpress: action},
                    <% } %>
                    {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
                ],
                /*
                searchitems : [
                    {display: 'ISO', name : 'iso'},
                    {display: 'Name', name : 'name', isdefault: true}
                    ],
                sortname: "iso",
                sortorder: "asc",
                */
                url: false,
                usepager: true,
                <% if (isNoticeMgr || isNoticeAll){ %>
                checkbox: true,
                <% } %>
                page: <%=curpage%>,
                total: <%=total%>,
                useRp: true,
                rp: <%=pagesize%>,

                // title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: true,

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

        <%if (op.equals("search")) {%>
        o("cond").value = "<%=cond%>";
        <%}%>

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
    });

    function action(com, grid) {
        if (com == '添加') {
            window.location.href = "notice_add.jsp";
        } else if (com == '修改') {
            selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择一条记录!', '提示');
                return;
            }
            if (selectedCount > 1) {
                jAlert('只能选择一条记录!', '提示');
                return;
            }

            var id = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).val();
            window.location.href = "notice_edit.jsp?id=" + id;
        } else if (com == '删除') {
            selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;

            if (selectedCount == 0) {
                jAlert('请选择记录!', '提示');
                return;
            }
            jConfirm("您确定要删除么？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    var ids = "";
                    $(".cth input[type='checkbox'][value!='on'][checked=checked]", grid.bDiv).each(function (i) {
                        if (ids == "")
                            ids = $(this).val();
                        else
                            ids += "," + $(this).val();
                    });
                    window.location.href = 'notice_list.jsp?op=del&CPages=<%=curpage%>&id=' + ids;
                }
            })
        }
    }
</script>
</body>
</html>