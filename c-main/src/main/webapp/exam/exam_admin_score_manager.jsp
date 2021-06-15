<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@page import="com.redmoon.oa.dept.DeptUserDb" %>
<%@page import="de.javawi.jstun.attribute.Username" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <title>查看成绩</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="nav.js"></script>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.exam")) {
        out.println(SkinUtil.makeErrMsg(request, privilege.MSG_INVALID));
        return;
    }

    String op = ParamUtil.get(request, "op");
    if (op.equals("del")) {
        ScoreMgr sm = new ScoreMgr();
        boolean re = false;
        try {
            re = sm.del(request);
        } catch (ErrMsgException e) {
            out.print(StrUtil.Alert(e.getMessage()));
        }
        if (re)
            out.print(StrUtil.Alert("删除成功！"));
    }
%>
<body>
<script language="JavaScript" type="text/JavaScript">
    var GetDate = "";

    function SelectDate(ObjName, FormatDate) {
        var PostAtt = new Array;
        PostAtt[0] = FormatDate;
        PostAtt[1] = o(ObjName);
        GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt, "dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
    }

    function SetDate() {
        o(ObjName).value = GetDate;
    }
</script>
<%
    int paperid = ParamUtil.getInt(request, "paperid");
    String sql = "select es.id from oa_exam_score es, users u where es.userName = u.name and es.paperid =" + paperid + " and es.is_prj=0 ";
    ScoreDb sdb = new ScoreDb();
    int i = 0;
    String querystr = "";
    String op1 = ParamUtil.get(request, "op");
    String beginDate = ParamUtil.get(request, "starttime");
    String endDate = ParamUtil.get(request, "endtime");
    String name = ParamUtil.get(request, "name");
    String mobile = ParamUtil.get(request, "mobile");
    java.util.Date endtime = new java.util.Date();
    endtime = DateUtil.addDate(DateUtil.parse(endDate, "yyyy-MM-dd"), 1);
    if (op1.equals("search")) {
        if (!beginDate.equals(""))
            sql += " and es.endtime >=" + StrUtil.sqlstr(beginDate);
        if (!endDate.equals(""))
            sql += " and es.endtime <=" + StrUtil.sqlstr(endDate);
        if (!name.equals(""))
            sql += " and u.realName like " + StrUtil.sqlstr("%" + name + "%");
        if (!mobile.equals(""))
            sql += " and es.mobile = " + StrUtil.sqlstr(mobile);
    }
    sql += " order by es.id desc";
    querystr += "paperid=" + paperid + "&op=search";
    int pagesize = 10;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    ListResult lr = sdb.listResult(sql, curpage, pagesize);
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
<script src="nav.js"></script>
<div class="spacerH"></div>
<form name="form1" action="?op=search&paperid=<%=paperid%>" method="post">
    <table class="percent98" width="100%" align="center" cellpadding="0" cellspacing="1">
        <tr>
            <td width="2990" height="22" colspan="5" align="center"><span style="font-size: 9pt; color: #000000">从
	        <input type="text" id="starttime" name="starttime" size="10" value="<%=beginDate %>"/>
	        至
	       <input type="text" id="endtime" name="endtime" size="10" value="<%=endDate %>" value=""/>
			&nbsp;&nbsp;姓名&nbsp;&nbsp;
	        <input type="text" name="name" size="10" onBlur="this.className='inputnormal'" onfocus="this.select()" value=""/>
	        <script>
	         o("name").value = "<%=name%>";
	        </script>
	        <input name="submit" type="submit" value="查 询"/>
			<input name="paperid" type="hidden" value="<%=paperid%>"/>
		    </span></td>
        </tr>
    </table>
</form>
<table class="tabStyle_1 percent98" width="100%" align="center" cellpadding="0" cellspacing="1">
    <tr>
        <td class="tabStyle_1_title" height="22" width="30">序号</td>
        <td class="tabStyle_1_title" width="125"><b>姓名</b></td>
        <td class="tabStyle_1_title" width="274"><b>部门</b></td>
        <td class="tabStyle_1_title" width="118"><b>考试时间</b></td>
        <td class="tabStyle_1_title" width="127">
            <strong>分数</strong></td>
        <td class="tabStyle_1_title" width="123"><b>操作</b></td>
    </tr>
    <%
        ScoreMgr sm = new ScoreMgr();
        UserMgr um = new UserMgr();
        while (ir.hasNext()) {
            i++;
            sdb = (ScoreDb) ir.next();
            String realName = um.getUserDb(sdb.getUserName()).getRealName();
            DeptUserDb dub = new DeptUserDb(sdb.getUserName());
            String deptName = dub.getDeptName();
    %>
    <tr align="center">
        <td align="center" height="20" width="17"><%=i%>
        </td>
        <td><%=realName%>
        </td>
        <td><%=deptName %>
        </td>
        <td><%=DateUtil.format(sdb.getEndtime(), "yyyy-MM-dd HH:mm:ss")%>
        </td>
        <td><%=sdb.getScore()%>
        </td>
        <td align="center">
            <a href="javascript:;" onclick="addTab('<%=realName%>', '<%=request.getContextPath()%>/exam/exam_show.jsp?scoreId=<%=sdb.getId()%>')">查看</a>
            &nbsp;&nbsp;
            <a href="javascript:;" onClick="if (confirm('您确定要删除么？')) window.location.href='exam_admin_score_manager.jsp?op=del&paperid=<%=paperid%>&id=<%=sdb.getId()%>'">删除</a>
            &nbsp;&nbsp;
            <a style="<%=!sm.isAnswerChecked(sdb.getId()) ? "color: red": ""%>" href="javascript:;" onclick="addTab('<%=realName%>', '<%=request.getContextPath()%>/exam/exam_score_comment.jsp?scoreId=<%=sdb.getId()%>&paperId=<%=paperid %>')">评分</a>
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
<script type="text/javascript">
    $(function () {
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
    });
</script>
<p>&nbsp;</p>
</body>
</html>