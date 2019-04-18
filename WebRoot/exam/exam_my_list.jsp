<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.db.ListResult" %>
<%@ page import="cn.js.fan.db.Paginator" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserLogin(request)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
        return;
    }
    String userName = privilege.getUser(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>我的考试</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <script src="../js/pagination/jquery.pagination.js"></script>
    <link href="../js/pagination/pagination.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
</head>
<body>
<%
    int kind = ParamUtil.getInt(request, "kind", 1); // 默认为待考
    String querystr = "op=search&kind=" + kind;

    String major = ParamUtil.get(request, "major");
    PaperDb pd = new PaperDb();

    int pagesize = 10;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    ListResult lr = pd.listExam(userName, major, kind, curpage, pagesize);
    int total = (int) lr.getTotal();
    paginator.init(total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
    Vector v = lr.getResult();
    Iterator ir = v.iterator();
%>
<%@ include file="exam_my_inc_menu.jsp" %>
<form action="?op=search&kind=<%=kind%>" name="form1" method="post">
    <table width="100%" border="0" align="center" cellpadding="2" cellspacing="0" class="percent98" id="AutoNumber2">
        <tr height="20" align="center">
            <td width="855" colspan="3">
                <select id="major" size="1" name="major">
                    <%
                        TreeSelectDb tsd = new TreeSelectDb();
                        tsd = tsd.getTreeSelectDb(MajorView.ROOT_CODE);
                        MajorView mv = new MajorView(tsd);
                        StringBuffer sb = new StringBuffer();
                        mv.getTreeSelectByUserAsOptions(sb, tsd, 1, "admin", "0");
                    %>
                    <%=sb %>
                </select>
                <script>
                    o("major").value = "<%=major%>";
                </script>
                <input class="btn" type="submit" value="查 询"/>
            </td>
        </tr>
    </table>
</form>
<table width="98%" class="tabStyle_1 percent98" style="margin-top: 10px">
    <tr style="font-size: 14px">
        <td width="67" class="tabStyle_1_title">序号</td>
        <td width="228" class="tabStyle_1_title">试卷</td>
        <td width="233" class="tabStyle_1_title">专业分类</td>
        <%
            if (kind == 0) {
        %>
        <td width="100" class="tabStyle_1_title" id="score">得分</td>
        <%
            }
        %>
        <td class="tabStyle_1_title" width="249" style="display: none">有效期</td>
        <td width="113" class="tabStyle_1_title">操作</td>
    </tr>
    <%
        ScoreDb sdb = new ScoreDb();
        PaperPriv pp = new PaperPriv();
        int i = 0;
        while (ir.hasNext()) {
            pd = (PaperDb) ir.next();
            // 判断用户是否有权限参加考试
            if (!pp.canUserSee(userName, pd.getId())) {
                continue;
            }

            ScoreDb mysdb = null;

            Vector vScore = sdb.getScoreOfPaper(userName, pd.getId());

            if (kind == 1) { // 待考
                // 如果是指定时间内考试，则检查用户是否已考过，如果是有效期内的考试，则根据限考次数判断
                if (pd.getMode() == PaperDb.MODE_SPECIFY) {
                    if (vScore.size() > 0) {
                        continue;
                    }
                } else {
                    if (pd.getLimitCount() <= vScore.size()) {
                        continue;
                    }
                }
            }
            else {
                if (vScore.size()>0) {
                    mysdb = (ScoreDb)vScore.elementAt(0);
                }
            }
            i++;
    %>
    <tr>
        <td align="center"><%=i%>
        </td>
        <td><%=pd.getTitle()%>
        </td>
        <td>
            <%
                TreeSelectDb tsd1 = tsd.getTreeSelectDb(pd.getMajor());
            %>
            <%=tsd1.getName()%>
        </td>
        <%
            if (kind == 0) {
        %>
        <td align="center">
            <%=kind == 0 ? String.valueOf(mysdb.getScore()) : "" %>
        </td>
        <%
            }
        %>
        <td style="display: none"><%=DateUtil.format(pd.getStartTime(), "yyyy-MM-dd HH:mm:ss")%>&nbsp;~&nbsp;<%=DateUtil.format(pd.getEndTime(), "yyyy-MM-dd HH:mm:ss")%>
        </td>
        <td align="center">
            <%if (kind == 0) {%>
            <a href="#" onClick="addTab('<%=pd.getTitle()%>成绩', 'exam/exam_score_detail.jsp?paperid=<%=pd.getId()%>')">成绩</a>
            <%
            } else {
                if (!pd.isManual()) {
            %>
            <a href="exam_paper.jsp?id=<%=pd.getId()%>">答题</a>
            <%} else {%>
            <a href="exam_manual.jsp?paperId=<%=pd.getId()%>">答题</a>
            <%
                    }
                }
            %>
        </td>
    </tr>
    <%
        }
    %>
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
<script type="text/javascript">
    $(function () {
        if ("<%=kind%>" == "1") {
            o("menu1").className = "current";
        } else {
            o("menu2").className = "current";
        }
    });
</script>
</body>
</html>