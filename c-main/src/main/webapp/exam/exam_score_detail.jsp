<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@page import="com.redmoon.oa.basic.TreeSelectDb" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <%
        if (!privilege.isUserPrivValid(request, "admin.exam")) {
            out.println(SkinUtil.makeErrMsg(request, privilege.MSG_INVALID));
            return;
        }
        String userName = privilege.getUser(request);
    %>
    <title>查看成绩</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="nav.js"></script>
</head>
<body>
<script language="JavaScript" type="text/JavaScript">
    <!--
    var GetDate = "";

    function SelectDate(ObjName, FormatDate) {
        var PostAtt = new Array;
        PostAtt[0] = FormatDate;
        PostAtt[1] = findObj(ObjName);
        GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt, "dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
    }

    function SetDate() {
        o(ObjName).value = GetDate;
    }
    //-->
</script>
<%
    int paperId;
    PaperDb pdb = new PaperDb();
    int paperid = ParamUtil.getInt(request, "paperid");
    String sql = "select es.id from oa_exam_score es, oa_exam_paper ep where es.paperid=ep.id and es.paperid =" + paperid + " and es.is_prj=0 and es.userName=" + StrUtil.sqlstr(userName);
    ScoreDb sdb = new ScoreDb();
    int i = 0;
    String op1 = ParamUtil.get(request, "op");
    String beginDate = ParamUtil.get(request, "starttime");
    String endDate = ParamUtil.get(request, "endtime");
    String name = ParamUtil.get(request, "name");
    String mobile = ParamUtil.get(request, "mobile");
    java.util.Date endtime = new java.util.Date();
    endtime = DateUtil.addDate(DateUtil.parse(endDate, "yyyy-MM-dd"), 1);
    if (op1.equals("search")) {
        if (!beginDate.equals(""))
            sql += "and es.endtime >=" + StrUtil.sqlstr(beginDate);
        if (!endDate.equals(""))
            sql += "and es.endtime <" + StrUtil.sqlstr(DateUtil.format(endtime, "yyyy-MM-dd"));
        if (!name.equals(""))
            sql += " and es.userName like " + StrUtil.sqlstr("%" + name + "%");
        if (!mobile.equals(""))
            sql += " and es.mobile = " + StrUtil.sqlstr(mobile);
    }
    sql += " order by es.score desc";
%>
<script src="nav.js"></script>
<table class="tabStyle_1 percent98" width="100%" align="center" cellpadding="0" cellspacing="1" id="AutoNumber2" style="margin-top: 30px">
    <tr>
        <td class="tabStyle_1_title" height="22" width="17"></td>
        <td class="tabStyle_1_title" width="125"><b>姓名</b></td>
        <td class="tabStyle_1_title" width="274"><b>专业分类</b></td>
        <td class="tabStyle_1_title" width="118"><b>考试时间</b></td>
        <td class="tabStyle_1_title" width="127">
            <strong>分数</strong></td>
        <td class="tabStyle_1_title" width="123"><b>操作</b></td>
    </tr>
    <%
        UserMgr um = new UserMgr();
        Iterator ir = sdb.list(sql).iterator();
        while (ir.hasNext()) {
            i++;
            sdb = (ScoreDb) ir.next();
            String realName = um.getUserDb(sdb.getUserName()).getRealName();
    %>
    <tr>
        <td align="center" height="20" width="17"><%=i%>
        </td>
        <td><%=realName%>
        </td>
        <td>
            <%
                paperId = sdb.getPaperId();
                pdb = pdb.getPaperDb(paperId);
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(pdb.getMajor());
                out.print(tsd.getName());
            %>
        </td>
        <td><%=DateUtil.format(sdb.getEndtime(), "yyyy-MM-dd HH:mm:ss")%>
        </td>
        <td>
            <%=sdb.getScore()%>
        </td>
        <td align="center"><a href="javascript:;" onclick="addTab('<%=realName%>', '<%=request.getContextPath()%>/exam/exam_show.jsp?scoreId=<%=sdb.getId()%>')">查看</a></td>
    </tr>
    <%}%>
</table>
<p>&nbsp;</p>
</body>
</html>
