<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@page import="com.redmoon.oa.basic.TreeSelectDb" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int paperId = ParamUtil.getInt(request, "paperId");
    PaperDb pd = new PaperDb();
    pd = pd.getPaperDb(paperId);

    if (!pd.isLoaded()) {
        out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "err_id")));
        return;
    }

    int testtime = pd.getTestTime();
    int lastTime = testtime * 60; //考试时长换算成秒

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <title>试卷-手工组卷</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style type="text/css">
        <!--
        .inputborder {
            color: #00FF00;
            text-align: center;
            border-style: solid;
            border-width: 1px;
            background-color: #333333
        }

        .outborder {
            border-left: 1px solid #333333;
            border-right: 1px solid #000000;
            border-top: 1px solid #99CCFF;
            border-bottom: 1px solid #000000;
            background-color: #336699
        }

        .STYLE1 {
            font-size: medium;
            font-weight: bold;
        }

        .STYLE2 {
            font-size: 14px;
            font-weight: bold;
        }

        -->
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
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
            var testTime = "<%=testtime%>";
            // 表示模式为考试有效期根据考试时长倒计时
            if (lastTime > 0) {
                window.setInterval(function () { //每隔1s定时任务
                    var t = --lastTime;
                    var h = Math.floor(t / 3600);
                    var m = Math.floor((t - (h * 3600)) / 60);
                    var s = t % 60;
                    console.log(h + ":" + m + ":" + s);
                    if (lastTime == 0) {
                        $('#testform').submit();
                        alert("考试时间结束,系统已自动提交您的成绩！");
                    } else {
                        $("#lxftime").html("剩余时间：<span>" + h + "</span>时<span>" + m + "</span>分<span>" + s + "</span>秒");          //输出没有天数的数据
                    }
                }, 1000);
            } else {
                lxfEndtime();
            }

        });
    </script>
    <script type="text/javascript">
        function form1_onsubmit() {
            if (!window.confirm('您确定要提交吗？')) {
                return false;
            }
            testform1.submit();
        }
    </script>
    <style type="text/css">
        img {
            width: 400px;
            height: 300px
        }
    </style>
</head>
<body>
<%
    if (!privilege.isUserLogin(request)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
        return;
    }

    PaperPriv pp = new PaperPriv();
    if (!pp.canUserSee(request, paperId)) {
        out.print(SkinUtil.makeErrMsg(request, "您没有权限参加考试！"));
        return;
    }

    if (!(DateUtil.compare(new Date(), pd.getStartTime()) == 1 && DateUtil.compare(new Date(), pd.getEndTime()) == 2)) {
        out.print(SkinUtil.makeErrMsg(request, "答题有效时间为" + DateUtil.format(pd.getStartTime(), "yyyy-MM-dd HH:mm:ss") + "至" + DateUtil.format(pd.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
        return;
    }

    String userName = privilege.getUser(request);
    SimpleDateFormat sdf = null;
    ScoreDb scoreDb = new ScoreDb();
    Vector vScore = scoreDb.getScoreOfPaper(userName, pd.getId());
    if (pd.getMode() == PaperDb.MODE_SPECIFY) {
        if (vScore.size() > 0) {
            out.print(SkinUtil.makeErrMsg(request, "您已参加过该考试！"));
            return;
        }
    } else {
        if (pd.getLimitCount() <= vScore.size()) {
            out.print(SkinUtil.makeErrMsg(request, "你已考了" + vScore.size() + "次，最多只能考" + pd.getLimitCount() + "次！"));
            return;
        }
    }

    TreeSelectDb tsd = new TreeSelectDb();
    tsd = tsd.getTreeSelectDb(pd.getMajor());
    String title = pd.getTitle();
    Date endtime = pd.getEndTime();
    PaperQuestionDb pqd = new PaperQuestionDb();
    Vector v = pqd.listOfPaper(paperId);
    Iterator ir = v.iterator();
    String ids = "";
    while (ir.hasNext()) {
        pqd = (PaperQuestionDb) ir.next();
        if (ids == "") {
            ids = pqd.getString("question_id");
        } else {
            ids += "," + pqd.getString("question_id");
        }
    }
    int hour = 0;
    int minute = 0;

    // 定义布尔变量用于判断是否第一次进入考试
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
        lastTime = testtime * 60 - (int) ((new java.util.Date()).getTime() - beginTime.getTime()) / 1000;
        hour = hour + testtime / 60;
        minute = minute + testtime % 60;
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
        hour = hour + testtime / 60;
        minute = minute + testtime % 60;
        if (minute >= 60) {
            hour = hour + 1;
            minute = minute % 60;
        }
        session.setAttribute("hour", hour);
        session.setAttribute("minute", minute);
    }
%>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tdStyle_1"><%=title%>
        </td>
    </tr>
    <tr>
        <td align="center" style="height:30px">
            <%if (testtime > 0) {%>
            专业分类：&nbsp;&nbsp;<%=tsd.getName() %>&nbsp;&nbsp;&nbsp;
            <%
                if (minute < 10) {%>
            结束时间：<%=hour%>:0<%=minute%>&nbsp;&nbsp;&nbsp;&nbsp;<span class="lxftime" id="lxftime" endtime="<%=DateUtil.format(endtime, "MM/dd/yyyy HH:mm:ss")%>" lxfday="no"></span>
            <%} else {%>
            结束时间：<%=hour%>:<%=minute%>&nbsp;&nbsp;&nbsp;&nbsp;<span class="lxftime" id="lxftime" endtime="<%=DateUtil.format(endtime, "MM/dd/yyyy HH:mm:ss")%>" lxfday="no"></span>
            <%
                }
            } else {
            %>
            专业分类：&nbsp;&nbsp;<%=tsd.getName() %>&nbsp;&nbsp;&nbsp;结束时间：<%=DateUtil.format(pd.getEndTime(), "HH:mm:ss")%>&nbsp;&nbsp;&nbsp;&nbsp;<span class="lxftime" id="lxftime" endtime="<%=DateUtil.format(pd.getEndTime(), "MM/dd/yyyy HH:mm:ss")%>" lxfday="no"></span>
            <%} %>
        </td>
    </tr>
</table>
<form method="post" action="exam_score_result.jsp?paperId=<%=paperId%>" id="testform" name="testform1">
    <input type="hidden" value="<%=cn.js.fan.util.DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss")%>" name="starttime"/>
    <table class="tabStyle_1 percent80">
        <%
            char[] cs = "零一二三四五六七八九".toCharArray();
            int num = 0;
            if (pd.getSingleCount() != 0) {
                num += 1;
        %>
        <tr>
            <td style="font-weight: bold;" colspan="2"><%=cs[num] %>、单选题</td>
        </tr>
        <%
            }
        %>
        <%
            QuestionDb sdb = new QuestionDb();
            String sql = "";
            Vector vt1 = null;
            StringBuffer sb = new StringBuffer("select id from oa_exam_database where exam_type=" + QuestionDb.TYPE_SINGLE + " and id in (");
            sb.append(ids);
            sb.append(");");
            vt1 = sdb.list(sb.toString());

            if (vt1 == null)
                vt1 = new Vector();
            Iterator ir1 = vt1.iterator();
            int i = 0;
            while (ir1.hasNext()) {
                sdb = (QuestionDb) ir1.next();
                i++;
        %>

        <tr>
            <td class="tabStyle_1_title" align="center" width="25px"><%=i%>、</td>
            <td class="tabStyle_1_title" style="text-align:left">
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
            }%>
    </table>
    <table class="tabStyle_1 percent80">
        <%
            if (pd.getMultiCount() != 0) {
                num += 1;
        %>
        <tr>
            <td style="font-weight: bold;" colspan="2"><%=cs[num] %>、多选题</td>
        </tr>
        <%
            } %>

        <%
            QuestionDb qdb = new QuestionDb();
            Vector vt2 = null;
            StringBuffer sb2 = new StringBuffer("select id from oa_exam_database where exam_type= " + QuestionDb.TYPE_MULTI + " and id in (");
            sb2.append(ids);
            sb2.append(");");

            vt2 = qdb.list(sb2.toString());
            if (vt2 == null)
                vt2 = new Vector();

            Iterator ir2 = vt2.iterator();
            int j = 0;
            while (ir2.hasNext()) {
                j++;
                qdb = (QuestionDb) ir2.next();
        %>
        <tr>
            <td class="tabStyle_1_title" align="center" width="25px"><%=j%>、</td>
            <td class="tabStyle_1_title" style="text-align:left"><%=qdb.getQuestion()%>
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
            }%>
    </table>
    <table class="tabStyle_1 percent80">
        <%
            if (pd.getJudgeCount() != 0) {
                num += 1;
        %>
        <tr>
            <td style="font-weight: bold;" colspan="2"><%=cs[num] %>、判断题</td>
        </tr>
        <%
            }
        %>

        <%
            Vector vt3 = null;
            StringBuffer sb3 = new StringBuffer("select id from oa_exam_database where exam_type = " + QuestionDb.TYPE_JUDGE + " and id in (");
            sb3.append(ids);
            sb3.append(");");
            vt3 = qdb.list(sb3.toString());

            if (vt3 == null)
                vt3 = new Vector();
            Iterator ir3 = vt3.iterator();
            j = 0;
            while (ir3.hasNext()) {
                j++;
                qdb = (QuestionDb) ir3.next();
        %>
        <tr>
            <td class="ta
      bStyle_1_title" align="center" width="25px"><%=j%>、
            </td>
            <td class="tabStyle_1_title" style="text-align:left"><%=qdb.getQuestion()%>
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
            }%>
    </table>
    <table class="tabStyle_1 percent80">
        <%
            StringBuffer sb4 = new StringBuffer("select id from oa_exam_database where exam_type=" + QuestionDb.TYPE_ANSWER + " and id in (");
            sb4.append(ids);
            sb4.append(");");
            Vector vt4 = null;
            vt4 = qdb.list(sb4.toString());
            if (vt4 == null)
                vt4 = new Vector();
            Iterator ir4 = vt4.iterator();
            j = 0;
            if (pd.getAnswerCount() != 0) {
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
                <%=qdb.getQuestion()%>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <textarea style="width:100%" name="<%=qdb.getId()%>"></textarea>
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
    <input type="hidden" value="<%=ids%>" name="questionId"/>
    <div id="persistMenu" style="position: absolute; height:150px; width:230px; left:360px; top:1px;z-index: 100; visibility: hidden" class="blueborder"></div>
    <p align="center"><input name="submit1" type="button" value=" 交  卷 " align="right" onclick="form1_onsubmit()"/></p>
</form>
</body>

</html>
