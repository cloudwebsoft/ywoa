<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.TreeSelectDb" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserLogin(request)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
        return;
    }
    String userName = privilege.getUser(request);
    int id = ParamUtil.getInt(request, "id", -1);
    PaperDb pdb = new PaperDb();
    pdb = pdb.getPaperDb(id);

    SimpleDateFormat sdf = null;
    String major = "";
    int lastTime;

    major = pdb.getMajor();
    Date dd = pdb.getEndTime();
    dd = DateUtil.addDate(dd, 1);
    int ttime = 0;
    int hour = 0;
    int minute = 0;

    ttime = pdb.getTestTime();
    lastTime = ttime * 60; //考试时长换算成秒
    // 用于判断是否第一次计入考试
    boolean testBoo = session.getAttribute("beginTestTime") == null ? false : true;
    if (testBoo) {
        // 剩余时长 = 考试时长 -（系统时间 - 开考时间 ）
        String beginT = (String) session.getAttribute("beginTestTime");
        String[] a = beginT.split(" ");
        String t = a[1];
        String[] b = t.split(":");
        hour = Integer.parseInt(b[0]);
        minute = Integer.parseInt(b[1]);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginTime = sdf.parse(beginT);
        lastTime = ttime * 60 - (int) ((new java.util.Date()).getTime() - beginTime.getTime()) / 1000;
        hour = hour + ttime / 60;
        minute = minute + ttime % 60;
        if (minute >= 60) {
            hour = hour + 1;
            minute = minute % 60;
        }
    } else {
        String curDate = DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss");
        session.setAttribute("beginTestTime", curDate);
        Calendar cc = Calendar.getInstance();
        hour = cc.get(Calendar.HOUR_OF_DAY);
        minute = cc.get(Calendar.MINUTE);
        hour = hour + ttime / 60;
        minute = minute + ttime % 60;
        if (minute >= 60) {
            hour = hour + 1;
            minute = minute % 60;
        }
        session.setAttribute("hour", hour);
        session.setAttribute("minute", minute);
    }

    sdf = new SimpleDateFormat("HH:mm");
    String d = sdf.format(dd);
    PaperPriv pp = new PaperPriv();
    if (!pp.canUserSee(request, id)) {
        out.print(SkinUtil.makeErrMsg(request, "您没有权限参加考试！"));
        return;
    }

    // 检查用户是否已答过试卷
    ScoreDb scoreDb = new ScoreDb();
    Vector vScore = scoreDb.getScoreOfPaper(userName, pdb.getId());
    if (pdb.getMode() == PaperDb.MODE_SPECIFY) {
        if (vScore.size() > 0) {
            out.print(SkinUtil.makeErrMsg(request, "您已参加过该考试！"));
            return;
        }
    } else {
        if (pdb.getLimitCount() <= vScore.size()) {
            out.print(SkinUtil.makeErrMsg(request, "你已考了" + vScore.size() + "次，最多只能考" + pdb.getLimitCount() + "次！"));
            return;
        }
    }

    String questionId = "";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <title>试卷</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        var timeNum;
        function lxfEndtime() {
            $(".lxftime").each(function () {
                var lxfday = $(this).attr("lxfday");//用来判断是否显示天数的变量
                var endtime = new Date($(this).attr("endtime")).getTime();//取结束日期(毫秒值)
                var nowtime = new Date().getTime();        //今天的日期(毫秒值)
                var youtime = endtime - nowtime;//还有多久(毫秒值)
                var seconds = youtime / 1000;
                var minutes = Math.floor(seconds / 60);
                var hours = Math.floor(minutes / 60);
                var days = Math.floor(hours / 24);
                var CDay = days;
                var CHour = hours % 24;
                var CMinute = minutes % 60;
                var CSecond = Math.floor(seconds % 60);//"%"是取余运算，可以理解为60进一后取余数，然后只要余数。
                if (endtime <= nowtime) {
                    // $(this).html("已过期")//如果结束日期小于当前日期就提示过期啦
                    $('#testform').submit();
                    alert("考试时间结束,系统已自动提交您的成绩！");

                } else {
                    if ($(this).attr("lxfday") == "no") {
                        $(this).html("剩余时间：<span>" + CHour + "</span>时<span>" + CMinute + "</span>分<span>" + CSecond + "</span>秒");          //输出没有天数的数据
                    } else {
                        $(this).html("剩余时间：<span>" + days + "</span><em>天</em><span>" + CHour + "</span><em>时</em><span>" + CMinute + "</span><em>分</em><span>" + CSecond + "</span><em>秒</em>");          //输出有天数的数据
                    }
                }
            });

            timeNum = setTimeout("lxfEndtime()", 1000);
        };

        $(function () {
            var lastTime = <%=lastTime%>;
            if (lastTime >= 0) { // 表示模式为考试有效期根据考试时长倒计时
                window.setInterval(function () { //每隔1s定时任务
                    var t = --lastTime;
                    var h = Math.floor(t / 3600);
                    var m = Math.floor((t - (h * 3600)) / 60);
                    var s = t % 60;
                    if (lastTime == 0) {
                        $('#testform').submit();
                        alert("考试时间结束,系统已自动提交您的成绩！");
                    } else {
                        $("#lxftime").html("剩余时间：<span>" + h + "</span>时<span>" + m + "</span>分<span>" + s + "</span>秒");          //输出没有天数的数据
                    }
                }, 1000);
            } else {//考试模式为固定考试时间 根据系统时间进行倒计时
                lxfEndtime();
            }
        });
    </script>
    <script type="text/javascript">
        function form1_onsubmit() {
            jConfirm('您确定要提交么？', '提示', function(r) {
                if (!r) {
                    return;
                }
                o("testform1").submit();
                clearTimeout(timeNum + 1);
            });
        }
    </script>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tdStyle_1"><%=pdb.getTitle()%>
        </td>
    </tr>
    <tr>
        <td align="center" style="height: 30px">
            <%
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(pdb.getMajor());
                if (ttime > 0) {
            %>
            专业分类：&nbsp;&nbsp;<%=tsd.getName() %>&nbsp;&nbsp;
            <%
                if (minute < 10) {
            %>
            结束时间：<%=hour%>:0<%=minute%>&nbsp;&nbsp;&nbsp;&nbsp;<span class="lxftime" id="lxftime" endtime="<%=DateUtil.format(pdb.getEndTime(), "MM/dd/yyyy HH:mm:ss")%>" lxfday="no"></span>
            <%
            } else {
            %>
            结束时间：<%=hour%>:<%=minute%>&nbsp;&nbsp;&nbsp;&nbsp;<span class="lxftime" id="lxftime" endtime="<%=DateUtil.format(pdb.getEndTime(), "MM/dd/yyyy HH:mm:ss")%>" lxfday="no"></span>
            <%
                }
            %>
            <%
            } else {
            %>
            专业分类：&nbsp;&nbsp;<%=tsd.getName() %>&nbsp;&nbsp;&nbsp;结束时间：<%=d%>&nbsp;&nbsp;&nbsp;&nbsp;<span class="lxftime" id="lxftime" endtime="<%=DateUtil.format(pdb.getEndTime(), "MM/dd/yyyy HH:mm:ss")%>" lxfday="no"></span>
            <%
                }
            %>
        </td>
    </tr>
</table>
<form method="post" action="exam_score_result.jsp?paperId=<%=id%>" id="testform" name="testform1">
    <input type="hidden" value="<%=cn.js.fan.util.DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss")%>" name="starttime"/>
    <table class="tabStyle_1 percent80">
        <%
            char[] cs = "零一二三四五六七八九".toCharArray();
            int num = 0;
            QuestionDb sdb = new QuestionDb();
            String sql = "";
            Vector vt1 = null;
            sql = "select id from oa_exam_database where exam_type=" + QuestionDb.TYPE_SINGLE + " and major =" + StrUtil.sqlstr(major) + " order by rand() limit " + pdb.getSingleCount();
            vt1 = sdb.list(sql, 0, pdb.getSingleCount() - 1);

            if (vt1 == null)
                vt1 = new Vector();
            Iterator ir1 = vt1.iterator();
            int i = 0;
            if (pdb.getSingleCount() != 0) {
                num += 1;
        %>
        <tr>
            <td style="font-weight: bold;" colspan="2"><%=cs[num] %>、单选题</td>
        </tr>
        <%
            }
            while (ir1.hasNext()) {
                sdb = (QuestionDb) ir1.next();
                i++;
        %>
        <tr>
            <td class="tabStyle_1_title" align="center" width="25px"><%=i%>、</td>
            <td class="tabStyle_1_title" style="text-align:left">
                <%

                    if (questionId.equals(""))
                        questionId += sdb.getId();
                    else {
                        questionId += ',';
                        questionId += sdb.getId();
                    }
                %>
                <%=sdb.getQuestion()%>
            </td>
        </tr>
        <%
            int question = sdb.getId();
            String selSelectOptionSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(question)) + "order by orders";
            QuestionSelectDb qsd = new QuestionSelectDb();
            Iterator optionIt = qsd.list(selSelectOptionSql).iterator();
            int k = 0;
            while (optionIt.hasNext()) {
                int o = (int) 'A';
                o = o + k;
                qsd = (QuestionSelectDb) optionIt.next();
        %>
        <tr>
            <td align="center" width="25px">&nbsp;&nbsp;<%=(char) o %>、</td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="<%=sdb.getId()%>" value="<%=qsd.getString("id") %>"/><%=(char) o %>、<%=qsd.getString("content").replaceAll("<p>([^<]*?)</p>", "$1") %>
            </td>
        </tr>
        <%
                    k++;
                }
            }
        %>
    </table>
    <table class="tabStyle_1 percent80">
        <%
            QuestionDb qdb = new QuestionDb();
            Vector vt2 = null;
            sql = "select id from oa_exam_database where exam_type=" + QuestionDb.TYPE_MULTI + " and major =" + StrUtil.sqlstr(major) + " order by rand() limit " + pdb.getMultiCount();
            vt2 = qdb.list(sql, 0, pdb.getMultiCount() - 1);
            if (vt2 == null)
                vt2 = new Vector();
            Iterator ir2 = vt2.iterator();
            int j = 0;
            if (pdb.getMultiCount() != 0) {
                num += 1;
        %>
        <tr>
            <td style="font-weight: bold;" colspan="2"><%=cs[num] %>、多选题</td>
        </tr>
        <%
            while (ir2.hasNext()) {
                j++;
                qdb = (QuestionDb) ir2.next();
        %>
        <tr>
            <td class="tabStyle_1_title" align="center" width="25px"><%=j%>、</td>
            <td class="tabStyle_1_title" style="text-align:left">
                <%
                    questionId += ',';
                    questionId += qdb.getId();
                %>
                <%=qdb.getQuestion()%>
            </td>
        </tr>
        <%
            int question = qdb.getId();
            String selSelectOptionSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(question)) + "order by orders";
            QuestionSelectDb qsd = new QuestionSelectDb();
            Iterator optionIt = qsd.list(selSelectOptionSql).iterator();
            int k = 0;
            while (optionIt.hasNext()) {
                int o = (int) 'A';
                o = o + k;
                qsd = (QuestionSelectDb) optionIt.next();
        %>
        <tr>
            <td align="center" width="25px">&nbsp;&nbsp;<%=(char) o %>、</td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" name="<%=qdb.getId()%>" value="<%=qsd.getString("id") %>"/><%=(char) o %>、<%=qsd.getString("content").replaceAll("<p>([^<]*?)</p>", "$1") %>
            </td>
        </tr>
        <%
                        k++;
                    }
                }
            }
        %>
    </table>
    <table class="tabStyle_1 percent80">
        <%
            Vector vt3 = null;
            sql = "select id from oa_exam_database where exam_type=" + QuestionDb.TYPE_JUDGE + " and major =" + StrUtil.sqlstr(major) + "order by rand() limit " + pdb.getJudgeCount();
            vt3 = qdb.list(sql, 0, pdb.getJudgeCount() - 1);
            if (vt3 == null)
                vt3 = new Vector();
            Iterator ir3 = vt3.iterator();
            j = 0;
            if (pdb.getJudgeCount() != 0) {
                num += 1;
        %>
        <tr>
            <td style="font-weight: bold;" colspan="2"><%=cs[num] %>、判断题</td>
        </tr>
        <%
            while (ir3.hasNext()) {
                j++;
                qdb = (QuestionDb) ir3.next();
        %>
        <tr>
            <td class="tabStyle_1_title" align="center" width="25px"><%=j%>、</td>
            <td class="tabStyle_1_title" style="text-align:left">
                <%
                    questionId += ',';
                    questionId += qdb.getId();
                %>
                <%=qdb.getQuestion()%>
            </td>
        </tr>
        <tr>
            <td align="center" width="25px"></td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;
                <input type="radio" name="<%=qdb.getId()%>" value="y"/>
                正确
            </td>
        </tr>
        <tr>
            <td align="center" width="25px"></td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;
                <input type="radio" name="<%=qdb.getId()%>" value="n"/>
                错误
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
    <table class="tabStyle_1 percent80">
        <%
            Vector vt4 = null;
            sql = "select id from oa_exam_database where exam_type=" + QuestionDb.TYPE_ANSWER + " and major =" + StrUtil.sqlstr(major) + "order by rand() limit " + pdb.getAnswerCount();
            vt4 = qdb.list(sql, 0, pdb.getAnswerCount() - 1);
            if (vt4 == null)
                vt4 = new Vector();
            Iterator ir4 = vt4.iterator();
            j = 0;
            if (pdb.getAnswerCount() != 0) {
                num += 1;
        %>
        <tr>
            <td style="font-weight: bold;" colspan="2"><%=cs[num] %>、问答题</td>
        </tr>
        <%
            while (ir4.hasNext()) {
                j++;
                qdb = (QuestionDb) ir4.next();
        %>
        <tr>
            <td class="tabStyle_1_title" align="center" width="25px"><%=j%>、</td>
            <td class="tabStyle_1_title" style="text-align:left">
                <%
                    questionId += ',';
                    questionId += qdb.getId();
                %>
                <%=qdb.getQuestion()%>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <textarea style="width: 100%" name="<%=qdb.getId()%>"></textarea>
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
    <input type="hidden" value="<%=questionId%>" name="questionId"/>
    <div id="persistMenu" style="position: absolute; height:150px; width:230px; left:360px; top:1px;z-index: 100; visibility: hidden" class="blueborder"></div>
    <p align="center"><input name="submit1" type="button" value=" 交 卷 " align="right" onclick="form1_onsubmit()"/></p>
</form>
<script>
    $(function () {
        $("form")[0].reset();
    });
</script>
</body>
</html>
