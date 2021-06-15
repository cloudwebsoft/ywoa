<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.pvg.RoleDb" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>试卷管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="../js/pagination/jquery.pagination.js"></script>
    <link href="../js/pagination/pagination.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.exam")) {
        out.println(SkinUtil.makeErrMsg(request, privilege.MSG_INVALID));
        return;
    }

    String op = ParamUtil.get(request, "op");
    String userName = privilege.getUser(request);
    PaperDb pd = new PaperDb();
    String major = ParamUtil.get(request, "major");
    if (op.equals("del")) {
        major = ParamUtil.get(request, "major");
        PaperMgr atm = new PaperMgr();
        boolean re = false;
        try {
            re = atm.del(request);
        } catch (ErrMsgException e) {
            out.print(StrUtil.Alert_Back(e.getMessage()));
        }
        if (re)
            out.print(StrUtil.Alert_Redirect("操作成功！", "exam_paper_manager.jsp"));
        return;
    }

    int i = 0;
    String beginDate = ParamUtil.get(request, "starttime");
    String endDate = ParamUtil.get(request, "endtime");

    String title = ParamUtil.get(request, "title");
    String querystr = "title=" + StrUtil.UrlEncode(title) + "&major=" + major + "&starttime=" + beginDate + "&endtime=" + endDate;

    PaperDb qd = new PaperDb();
    String sql = qd.getSearchSql(userName, title, major, beginDate, endDate);

    int pagesize = 10;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    ListResult lr = qd.listResult(sql, curpage, pagesize);
    long total = lr.getTotal();
    Vector v = lr.getResult();
    Iterator ir = null;
    if (v != null)
        ir = v.iterator();
    paginator.init(total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<div class="spacerH"></div>
<form name="form1" action="?op=search" method="post">
    <table class="percent98" width="100%" align="center" cellpadding="0" cellspacing="1">
        <tr>
            <td width="2990" height="22" colspan="5" align="center">
                <select id="major" size="1" name="major">
                    <%
                        TreeSelectDb tsd = new TreeSelectDb();
                        tsd = tsd.getTreeSelectDb("exam_major");
                        TreeSelectView tsv = new TreeSelectView(tsd);
                        StringBuffer sb = new StringBuffer();
                        tsv.getTreeSelectAsOptions(sb, tsd, 1);
                    %>
                    <%=sb %>
                </select>
                <script>
                    o("major").value = "<%=major%>";
                </script>
                <span style="font-size: 9pt; color: #000000">时间&nbsp;&nbsp;</span>
                从<input type="text" id="starttime" name="starttime" size="10" value="<%=beginDate %>"/>
                至<input type="text" id="endtime" name="endtime" size="10" value="<%=endDate %>"/>
                &nbsp;&nbsp;考试名称&nbsp;&nbsp;<input id="title" name="title" type="text" size="10" value=""/>
                &nbsp;&nbsp;<input name="submit" type="submit" value="查 询"/>
            </td>
        </tr>
    </table>
</form>
<table class="tabStyle_1 percent98" width="100%" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tabStyle_1_title" height="22" width="30">序号</td>
        <td width="159" class="tabStyle_1_title">考试名称</td>
        <td width="104" class="tabStyle_1_title">专业类别</td>
        <td width="313" class="tabStyle_1_title">考试时间</td>
        <td width="85" class="tabStyle_1_title">试卷类型</td>
        <td width="224" class="tabStyle_1_title">操作</td>
    </tr>
    <%
        while (ir.hasNext()) {
            i++;
            pd = (PaperDb) ir.next();
    %>
    <tr>
        <td align="center" width="30"><%=i%>
        </td>
        <td><%=pd.getTitle()%>
        </td>
        <td>
            <%
                TreeSelectDb tsd1 = tsd.getTreeSelectDb(pd.getMajor());
            %>
            <%=tsd1.getName()%>
        </td>
        <td>
		<%
        if (pd.getTestTime() > 0) {%>
        <%=DateUtil.format(pd.getStartTime(), "yyyy-MM-dd")%>&nbsp;至&nbsp;<%=DateUtil.format(pd.getEndTime(), "yyyy-MM-dd")%>&nbsp;&nbsp;&nbsp;(时长：<%=pd.getTestTime() %>分钟)
        <%} else {%>
        <%=DateUtil.format(pd.getStartTime(), "yyyy-MM-dd HH:mm:ss")%>&nbsp;至&nbsp;<%=DateUtil.format(pd.getEndTime(), "yyyy-MM-dd HH:mm:ss")%>
        <%} %>
        </td>
        <td align="center">
        <%
		if (!pd.isManual()) {
			out.print("自动");
		}
		else {
			out.print("手动");
		}
		%>
        </td>
        <td align="center">
            <a href="exam_admin_paper_modify.jsp?paperid=<%=pd.getId()%>&major=<%=major %>">修改</a>
            &nbsp;&nbsp;<a href="javascript:;" onClick="if (confirm('您确定要删除吗？')) window.location.href='exam_paper_manager.jsp?op=del&id=<%=pd.getId()%>&major=<%=major %>'">删除</a>
            &nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=pd.getTitle()%>', '<%=request.getContextPath()%>/exam/exam_paper_priv_m.jsp?paperId=<%=pd.getId()%>')">权限</a>
            &nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=pd.getTitle()%>成绩', '<%=request.getContextPath()%>/exam/exam_admin_score_manager.jsp?paperid=<%=pd.getId()%>&isPrj=0')">成绩</a>
        </td>
    </tr>
    <%}%>
</table>
<table class="percent98" width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td align="right">
            <%
                out.print(paginator.getCurPageBlock("?" + querystr));
            %>
        </td>
    </tr>
</table>
</body>
<script type="text/javascript">
    $(function () {
        o("title").value = "<%=title%>";
        $('#starttime').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
        $('#endtime').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
    })
</script>
</html>
