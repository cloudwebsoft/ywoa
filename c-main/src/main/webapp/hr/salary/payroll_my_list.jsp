<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.hr.SalaryMgr" %>
<%@ page import="com.redmoon.oa.hr.PersonMgr" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>我的工资</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <script src="../../inc/common.js"></script>
    <script src="../../js/jquery-1.9.1.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String userName = privilege.getUser(request);
    FormDAO fdao = PersonMgr.getPerson(userName);
    if (fdao==null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "用户" + userName + "的人事信息不存在"));
        return;
    }

    Date dt = new Date();
    int curYear = DateUtil.getYear(dt);

    int year = ParamUtil.getInt(request, "year", curYear);
%>
<div style="text-align: center; margin: 10px">
    年份
    <select id="year" name="year">
        <%
            for (int i = curYear; i > curYear - 20; i--) {
        %>
        <option value="<%=i%>"><%=i %>
        </option>
        <%
            }
        %>
    </select>
</div>
<script>
    $(function() {
        $('#year').val("<%=year%>");

        $('#year').change(function () {
            window.location.href = "payroll_my_list.jsp?year=" + $(this).val();
        })
    })
</script>
<%
    long personId = fdao.getId();
    String sqlSubject = "select subject from salary_book_subject where book_id=? and year=? and month=? order by id asc";

    String sql = "select * from salary_payroll where person_id=? and year=? order by month asc";
    JdbcTemplate jt = new JdbcTemplate();
    ResultIterator ri = jt.executeQuery(sql, new Object[]{personId, year});
    if (ri.size() == 0) {
        out.print(SkinUtil.makeInfo(request, "无工资记录"));
    }
    while (ri.hasNext()) {
        ResultRecord rr = (ResultRecord) ri.next();

        int bookId = rr.getInt("book_id");
        int month = rr.getInt("month");
%>
<div style="margin: 5px auto; text-align: center; font-weight: bold" class="percent98"><%=month%>月</div>
<table class="tabStyle_1 percent98">
    <tr>
        <%
            Map<String, Integer> mapDecimals = new HashMap<String, Integer>();
            ResultIterator riSubject = jt.executeQuery(sqlSubject, new Object[]{bookId, year, month});
            while (riSubject.hasNext()) {
                ResultRecord rrSbuject = (ResultRecord) riSubject.next();
                String subjectCode = rrSbuject.getString(1);
                com.redmoon.oa.visual.FormDAO fdaoSubject = SalaryMgr.getSubject(subjectCode);
                String subjectName;
                if (fdaoSubject==null) {
                    subjectName = subjectCode + "不存在";
                    mapDecimals.put(subjectCode, -1);
                }
                else {
                    subjectName = fdaoSubject.getFieldValue("name");
                    int decimals = StrUtil.toInt(fdaoSubject.getFieldValue("decimals"), 2);
                    mapDecimals.put(subjectCode, decimals);
                }
        %>
        <td class="tabStyle_1_title"><%=subjectName%>
        </td>
        <%
            }
        %>
    </tr>
    <tr>
        <%
            riSubject.beforeFirst();
            while (riSubject.hasNext()) {
                ResultRecord rrSbuject = (ResultRecord) riSubject.next();
                String subjectCode = rrSbuject.getString(1);
                int decimals = mapDecimals.get(subjectCode);
                String val;
                if (decimals==-1) {
                    val = "";
                }
                else {
                    val = NumberUtil.round(rr.getDouble(subjectCode), decimals);
                }
        %>
        <td align="right"><%=val%>
        </td>
        <%
            }
        %>
    </tr>
</table>
<%
    }
%>
</body>
</html>