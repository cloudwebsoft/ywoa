<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%
    String path = request.getContextPath();
    int paperId = ParamUtil.getInt(request, "paperId");
    PaperDb pd = new PaperDb();
    pd = pd.getPaperDb(paperId);
    String singleIds = "";
    String multiIds = "";
    String judgeIds = "";
    String answerIds = "";
    String sqll = "select id from oa_exam_paper_question where paper_id = " + paperId;
    PaperQuestionDb pqd = new PaperQuestionDb();
    Vector v = pqd.list(sqll);
    Iterator ir = v.iterator();
    String ids = "";
    while (ir.hasNext()) {
        pqd = (PaperQuestionDb) ir.next();
        if (ids == "") {
            ids = pqd.getString("question_id");
        } else {
            ids += "," + pqd.getString("question_id");
        }
        int questionId = Integer.parseInt(pqd.getString("question_id"));
        QuestionDb qd = new QuestionDb();
        qd = qd.getQuestionDb(questionId);
        if (qd.getType() == QuestionDb.TYPE_SINGLE) {
            if ("".equals(singleIds)) {
                singleIds = String.valueOf(qd.getId());
            } else {
                singleIds += "," + String.valueOf(qd.getId());
            }
        } else if (qd.getType() == QuestionDb.TYPE_MULTI) {
            if ("".equals(multiIds)) {
                multiIds = String.valueOf(qd.getId());
            } else {
                multiIds += "," + String.valueOf(qd.getId());
            }
        } else if (qd.getType() == QuestionDb.TYPE_JUDGE) {
            if ("".equals(judgeIds)) {
                judgeIds = String.valueOf(qd.getId());
            } else {
                judgeIds += "," + String.valueOf(qd.getId());
            }
        } else if (qd.getType() == QuestionDb.TYPE_ANSWER) {
            if ("".equals(answerIds)) {
                answerIds = String.valueOf(qd.getId());
            } else {
                answerIds += "," + String.valueOf(qd.getId());
            }
        }
    }
%>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>手工组卷修改</title>
    <script src="../inc/common.js"></script>
        <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<div id="div1">
    <form id="creat_paper" name="creat_paper" method="post">
        <table class="tabStyle_1 percent98" style="margin-top: 5px">
            <tr>
                <td><b>名称</b>&nbsp;&nbsp;&nbsp;<input type="text" name="title" size="30" value="<%=pd.getTitle() %>"/></td>
            </tr>
            <tr height="20">
                <td width="120"><b>专业类别</b>&nbsp;&nbsp;&nbsp;
                    <%
                        TreeSelectDb tsd = new TreeSelectDb();
                        tsd = tsd.getTreeSelectDb(pd.getMajor());
                    %>
                    <input type="text" name="subject_show" size="20" value="<%=tsd.getName()%>" class="inputnormal" readonly="readonly"/>
                    <input type="hidden" name="major" value="<%=pd.getMajor() %>"/>
                </td>
            </tr>
            <tr>
                <td align="left"><b>单选题</b></td>
            </tr>
            <tr>
                <td>分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="single_totle_score" name="single_totle_score" size="5" value="<%=pd.getSingleTotal() %>" onchange="updateSinglePer()"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_SINGLE%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="single_count_score">共</span>
                    &nbsp;&nbsp;<span class="single_count_score" id="single_count"><%=pd.getSingleCount() %></span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="single_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="single_count_score" id="single_per"><%=pd.getSingleper() %></span><span class="single_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="singleQuestions"></div>
                </td>
            </tr>
            <tr>
                <td><b>多选题</b></td>
            </tr>
            <tr>
                <td colspan="6">分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="multi_totle_score" name="multi_totle_score" size="5" value="<%=pd.getMultiTotal() %>" onchange="updateMultiPer()"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_MULTI%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="multi_count_score">共</span>
                    &nbsp;&nbsp;<span class="multi_count_score" id="multi_count"><%=pd.getMultiCount() %></span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="multi_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="multi_count_score" id="multi_per"><%=pd.getMultiper() %></span><span class="multi_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="multiQuestions"></div>
                </td>
            </tr>
            <tr>
                <td><b>判断题</b></td>
            </tr>
            <tr>
                <td>分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="judge_totle_score" name="judge_totle_score" size="5" value="<%=pd.getJudgeTotal()%>" onchange="updateJudgePer()"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_JUDGE%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="judge_count_score">共</span>
                    &nbsp;&nbsp;<span class="judge_count_score" id="judge_count"><%=pd.getJudgeCount() %></span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="judge_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="judge_count_score" id="judge_per"><%=pd.getJudgeper() %></span><span class="judge_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="judgeQuestions"></div>
                </td>
            </tr>
            <tr>
                <td><b>问答题</b></td>
            </tr>
            <tr>
                <td>分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="answer_totle_score" name="answer_totle_score" size="5" value="<%=pd.getAnswerTotal()%>" onchange="updateJudgePer()"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_ANSWER%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="answer_count_score">共</span>
                    &nbsp;&nbsp;<span class="answer_count_score" id="answer_count"><%=pd.getAnswerCount() %></span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="answer_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="answer_count_score" id="answer_per"><%=pd.getAnswerper() %></span><span class="answer_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="answerQuestions"></div>
                </td>
            </tr>
            <tr>
                <td align="left">考试模式&nbsp;&nbsp;&nbsp;&nbsp;
                    <select id="mode" name="mode">
                        <option id="sel1" value="<%=PaperDb.MODE_SPECIFY%>">按指定时间参加考试</option>
                        <option id="sel2" value="<%=PaperDb.MODE_PERIOD%>">按有效期参加考试</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>从&nbsp;<input type="text" id="starttime" name="starttime" size="20" value="" class="inputnormal" readonly/>
                    &nbsp;&nbsp;至
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="endtime" name="endtime" size="20" value="" class="inputnormal" readonly/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="testtime" style="display: none">考试时长:</span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="testtime" class="testtime" name="testtime" value="<%=pd.getTestTime() %>" size="5" style="display: none"/>&nbsp;<span style="display: none" class="testtime">分</span>
                </td>
            </tr>
            <tr>
                <td>
                    <span style=" float: left;">多选题计分规则&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <span style=" float: left;">
				    		<input type="radio" name="multiScoreRule" value="0"/>部分答对不计分
				    		<input type="radio" name="multiScoreRule" value="1"/>部分答对计分
			    		</span>
                    <span style="display: none; float: left;" id="otherMultiPer">
			    			&nbsp;&nbsp;&nbsp;分值&nbsp;&nbsp;<input id="notAllRightMuntiper" name="notAllRightMuntiper" value="<%=pd.getNotAllRightMuntiper() %>"/>
			    	  	</span>
                </td>
            </tr>
            <tr>
                <td colspan="6" align="center">
                    <input type="button" value="预 览" onclick="paperShow()"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="确定" onclick="editorPaper()"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="返回" onclick="window.history.back()"/>
                </td>
            </tr>
        </table>
        <input type="hidden" id="singleIds" name="singleIds" value="<%=singleIds%>"/>
        <input type="hidden" id="multiIds" name="multiIds" value="<%=multiIds %>"/>
        <input type="hidden" id="judgeIds" name="judgeIds" value="<%=judgeIds %>"/>
        <input type="hidden" id="answerIds" name="answerIds" value="<%=answerIds %>"/>
        <input type='hidden' name="single_count1" value=""/>
        <input type='hidden' name="multi_count1" value=""/>
        <input type="hidden" name="judge_count1" value=""/>
        <input type="hidden" name="answer_count1" value=""/>
        <input type="hidden" name="singleper1" value=""/>
        <input type="hidden" name="multiper1" value=""/>
        <input type="hidden" name="judgeper1" value=""/>
        <input type="hidden" name="answerper1" value=""/>
        <input type="hidden" id="paperId" name="paperId" value="<%=paperId %>"/>
        <input type="hidden" id="questionIds" name="questionIds" value=""/>
        <input type="hidden" id="sIds" name="sIds" value=""/>
        <input type="hidden" id="mIds" name="mIds" value=""/>
        <input type="hidden" id="jIds" name="jIds" value=""/>
        <input type="hidden" id="aIds" name="aIds" value=""/>
    </form>
</div>
<script>
    var starttime = new LiveValidation('starttime');
    starttime.add(Validate.Presence);
    var endtime = new LiveValidation('endtime');
    endtime.add(Validate.Presence);

    $("#mode").change(function () {
        var s1 = $("#mode").get(0).selectedIndex;
        if (s1 == 0) {
            $(".testtime").hide();
            $("#testtime").val(0);
            $('#starttime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
            $('#endtime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
        } else if (s1 == 1) {
            $(".testtime").show();
            $("#testtime").val('');
            $('#starttime').val('');
            $('#endtime').val('');
            $('#starttime').datetimepicker({value: '', step: 10, format: 'Y-m-d 09:00:00'});
            $('#endtime').datetimepicker({value: '', step: 10, format: 'Y-m-d 20:00:00'});
        }T
        ;
    });
    $('#starttime').datetimepicker({value: '<%=pd.getStartTime()%>', step: 10, format: 'Y-m-d H:i:00 '});
    $('#endtime').datetimepicker({value: '<%=pd.getEndTime()%>', step: 10, format: 'Y-m-d H:i:00'});

    //ajax提交通用方法
    function ajaxPost(path, parameter, func) {
        $.ajax({
            type: "post",
            url: path,
            async: false,
            data: parameter,
            dataType: "html",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            success: function (data, status) {
                func(data);
            },
            error: function (XMLHttpRequest, textStatus) {
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    //初始化时间刷新题目列表
    $(function () {
        var testTime = <%=pd.getTestTime()%>;
        if (testTime > 0) {
            $("#mode option:eq(1)").attr('selected', 'selected');
            $(".testtime").show();
        } else {
            $("#mode option:eq(0)").attr('selected', 'selected');
        }
        var oldSingleIds = o("singleIds").value;
        var oldMultiIds = o("multiIds").value;
        var oldJudgeIds = o("judgeIds").value;
        var oldAnswerIds = o("answerIds").value;
        window.setInterval(function () {
            var newSingleIds = o("singleIds").value;
            var newMultiIds = o("multiIds").value;
            var newJudgeIds = o("judgeIds").value;
            var newAnswerIds = o("answerIds").value;
            if (newSingleIds != oldSingleIds) {
                $("#singleQuestions").empty();
                ajaxPost('exam_paper_question.jsp?type=0', {'questionId': newSingleIds}, function (data) {
                    $('#singleQuestions').append(data);
                    $(".question").hide();
                });
                if (newSingleIds != "") {
                    $('.single_count_score').show();
                } else {
                    $('.single_count_score').hide();
                }
                oldSingleIds = newSingleIds;
            }
            if (newMultiIds != oldMultiIds) {
                $("#multiQuestions").empty();
                ajaxPost('exam_paper_question.jsp?type=1', {'questionId': newMultiIds}, function (data) {
                    $('#multiQuestions').append(data);
                    $(".question").hide();
                });
                if (newMultiIds != "") {
                    $('.multi_count_score').show();
                } else {
                    $('.multi_count_score').hide();
                }
                oldMultiIds = newMultiIds;
            }
            if (newJudgeIds != oldJudgeIds) {
                $("#judgeQuestions").empty();
                ajaxPost('exam_paper_question.jsp?type=2', {'questionId': newJudgeIds}, function (data) {
                    $('#judgeQuestions').append(data);
                    $(".question").hide();
                });
                if (newJudgeIds != "") {
                    $('.judge_count_score').show();
                } else {
                    $('.judge_count_score').hide();
                }
                oldJudgeIds = newJudgeIds
            }
            if (newAnswerIds != oldAnswerIds) {
                $("#answerQuestions").empty();
                ajaxPost('exam_paper_question.jsp?type=3', {'questionId': newAnswerIds}, function (data) {
                    $('#answerQuestions').append(data);
                    $(".question").hide();
                });
                if (newAnswerIds != "") {
                    $('.answer_count_score').show();
                } else {
                    $('.answer_count_score').hide();
                }
                oldAnswerIds = newAnswerIds
            }
        }, 500);
        var single = "<%=pd.getSingleTotal() %>";
        var multi = "<%=pd.getMultiTotal() %>";
        var judge = "<%=pd.getJudgeTotal() %>";
        var answer = "<%=pd.getAnswerTotal() %>";
        if (single == "0") {
            $('.single_count_score').hide();
        }
        if (multi == "0") {
            $('.multi_count_score').hide();
        }
        if (judge == "0") {
            $('.judge_count_score').hide();
        }
        if (answer == "0") {
            $('.answer_count_score').hide();
        }
        // 初始化多选题计分规则
        var multiScoreRule = "<%=pd.getMultiScoreRule()%>";
        if (multiScoreRule == "0") {
            $("input:radio[value='0']").attr('checked', 'true');
        } else if (multiScoreRule == "1") {
            $("input:radio[value='1']").attr('checked', 'true');
        }
        var multiPer = $("#multi_per").text();
        $('input[type=radio][name=multiScoreRule]').change(function () {
            if (this.value == '0') {
                $("#otherMultiPer").hide();
            } else if (this.value == '1') {
                $("#otherMultiPer").show();
                o("notAllRightMuntiper").value = "";
                var multiNotAllRightScore = new LiveValidation('notAllRightMuntiper');
                multiNotAllRightScore.add(Validate.Presence);
                multiNotAllRightScore.add(Validate.Numericality, {minimum: 0, tooLowMessage: '必须大于等于0'});
                multiNotAllRightScore.add(Validate.Numericality, {maximum: multiPer, tooLowMessage: '不能大于' + multiPer});
            }
        });
        if ($("input[name='multiScoreRule']:checked").val() == "1") {
            $("#otherMultiPer").show();
            var multiNotAllRightScore = new LiveValidation('notAllRightMuntiper');
            multiNotAllRightScore.add(Validate.Presence);
            multiNotAllRightScore.add(Validate.Numericality, {minimum: 0, tooLowMessage: '必须大于等于0'});
            multiNotAllRightScore.add(Validate.Numericality, {maximum: multiPer, tooLowMessage: '不能大于' + multiPer});
        } else {
            $("#otherMultiPer").hide();
        }
    })

    function openWin(url, width, height) {
        var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,stat	us=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
        return newwin;
    }

    function selectQuestion(type) {
        var a = o("single_totle_score").value;
        var b = o("multi_totle_score").value;
        var c = o("judge_totle_score").value;
        var d = o("answer_totle_score").value;
        if (type == "0") {
            if (a == "") {
                jAlert("请先设置单选题分值", "提示");
                return;
            }
        } else if (type == "1") {
            if (b == "") {
                jAlert("请先设置多选题分值", "提示");
                return;
            }
        } else if (type == "2") {
            if (c == "") {
                jAlert("请先设置判断题分值", "提示");
                return;
            }
        } else if (type == "3") {
            if (d == "") {
                jAlert("请先设置问答题分值", "提示");
                return;
            }
        }
        var url = "exam_question_select_frame.jsp?type=" + type + "&major=<%=pd.getMajor()%>";
        openWin(url, 1000, 800);
    }

    function editorPaper() {
        o("single_count1").value = $("#single_count").text();
        o("multi_count1").value = $("#multi_count").text();
        o("judge_count1").value = $("#judge_count").text();
        o("answer_count1").value = $("#answer_count").text();
        o("singleper1").value = $("#single_per").text();
        o("multiper1").value = $("#multi_per").text();
        o("judgeper1").value = $("#judge_per").text();
        o("answerper1").value = $("#answer_per").text();
        if (!o("single_totle_score").value == "") {
            o("single_totle_score").value = o("single_totle_score").value;
        } else {
            o("single_totle_score").value = "0";
        }
        if (!o("multi_totle_score").value == "") {
            o("multi_totle_score").value = o("multi_totle_score").value;
        } else {
            o("judge_totle_score").value = "0";
        }
        if (!o("judge_totle_score").value == "") {
            o("judge_totle_score").value = o("judge_totle_score").value;
        } else {
            o("judge_totle_score").value = "0";
        }
        if (!o("answer_totle_score").value == "") {
            o("answer_totle_score").value = o("answer_totle_score").value;
        } else {
            o("answer_totle_score").value = "0";
        }
        var title = o("title").value;
        var a = Number(o("single_totle_score").value);
        var b = Number(o("multi_totle_score").value);
        var c = Number(o("judge_totle_score").value);
        var d = Number(o("answer_totle_score").value);
        var e = a + b + c + d;
        if (title == "") {
            jAlert("请填写试卷名", "提示");
            return;
        }
        if (e != 100) {
            jAlert("试卷总分不等于100", "提示");
            return;
        }
        if (a > 0) {
            if (o("single_count1").value == "0") {
                jAlert("单选题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("singleper1").value)) {
                jAlert("单选题每题分值不能为小数", "提示");
                return;
            }
            ;
        }
        if (b > 0) {
            if (o("multi_count1").value == "0") {
                jAlert("多选题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("multiper1").value)) {
                jAlert("多选题每题分值不能为小数", "提示");
                return;
            }
            ;
        }
        if (c > 0) {
            if (o("judge_count1").value == "0") {
                jAlert("判断题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("judgeper1").value)) {
                jAlert("判断题每题分值不能为小数", "提示");
                return;
            }
        }
        if (d > 0) {
            if (o("answer_count1").value == "0") {
                jAlert("问答题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("answerper1").value)) {
                jAlert("问答题每题分值不能为小数", "提示");
                return;
            }
            ;
        }
        if (o("starttime").value == "") {
            jAlert("请设置考试有效开始时间", "提示");
            return;
        }
        if (o("endtime").value == "") {
            jAlert("请设置考试有效结束时间", "提示");
            return;
        }
        if (o("testtime").value == "") {
            jAlert("请设置考试时长", "提示");
            return;
        }
        var formData = $("#creat_paper").serialize();
        ajaxPost('../exam/paperModify.do', formData, function (data) {
            data = $.parseJSON(data);
            if (data.ret == 1) {
                jAlert_Redirect(data.msg, "提示", "exam_paper_manager.jsp");
                //window.location.href="exam_paper_manager.jsp";
            } else if (data.ret == 0) {
                jAlert(data.msg, "提示");
            }
        });
    }

    function paperShow() {
        var singleIds = o("singleIds").value;
        var multiIds = o("multiIds").value;
        var judgeIds = o("judgeIds").value;
        var answerIds = o("answerIds").value;
        var url = "exam/exam_paper_show.jsp?singleIds=" + singleIds + "&multiIds=" + multiIds + "&judgeIds=" + judgeIds + "&answerIds" + answerIds;
        url = decodeURI(url);
        addTab("预览试卷", url);
    }

    function showAnswer(id) {
        $("tr[id*=" + id + "]").toggle();
        var plus = 'plus_' + id;
        var minus = 'minus_' + id;
        $('#' + plus).toggle();
        $('#' + minus).toggle();
    }

    //删除题目替换保存的题目id
    function repalceIds(qId, ids) {
        var str = ids.split(",");
        var newIds = "";
        for (var key in str) {
            if (qId != str[key]) {
                if (newIds == "") {
                    newIds += str[key];
                } else {
                    newIds += "," + str[key];
                }
            }
        }
        ids = newIds;
        return ids;
    }

    //删除题目替换分值

    function replaceScore(count, totleCount) {
        var avgCount;
        if (count != 0) {
            avgCount = totleCount / count;
        } else {
            avgCount = 0;
        }
        return avgCount;
    }

    function delQ(qId, tabId, type) {
        jConfirm('您确定要删除吗？', '提示', function (r) {
            if (!r) {
                return;
            }
            if (type == 0) {
                $('#' + tabId).remove();
                o("singleIds").value = repalceIds(qId, o("singleIds").value);
                o("questionIds").value = repalceIds(qId, o("questionIds").value);
                var a = Number($("#single_count").text()) - Number(1);
                $("#single_count").html(a);
                $("#single_per").html(replaceScore(a, Number(o("single_totle_score").value)));
            } else if (type == 1) {
                $('#' + tabId).remove();
                var a = Number($("#multi_count").text()) - Number(1);
                var b = Number(o("multi_totle_score").value);
                $("#multi_count").html(a);
                $("#multi_per").html(replaceScore(a, b));
                o("multiIds").value = repalceIds(qId, o("multiIds").value);
                o("questionIds").value = repalceIds(qId, o("questionIds").value);
            } else if (type == 2) {
                $('#' + tabId).remove();
                var a = Number($("#judge_count").text()) - Number(1);
                var b = Number(o("judge_totle_score").value);
                $("#judge_count").html(a);
                $("#judge_per").html(replaceScore(a, b));
                o("judgeIds").value = repalceIds(qId, o("judgeIds").value);
                o("questionIds").value = repalceIds(qId, o("questionIds").value);
            } else if (type == 3) {
                $('#' + tabId).remove();
                var a = Number($("#answer_count").text()) - Number(1);
                var b = Number(o("answer_totle_score").value);
                $("#answer_count").html(a);
                $("#answer_per").html(replaceScore(a, b));
                o("answerIds").value = repalceIds(qId, o("answerIds").value);
                o("questionIds").value = repalceIds(qId, o("questionIds").value);
            }
        });
    }

    //初始化页面
    var a = "<%=singleIds%>";
    var b = "<%=multiIds%>";
    var c = "<%=judgeIds%>";
    var d = "<%=answerIds%>";
    var t = a + "," + b + "," + c + "," + d;
    o("questionIds").value = t;
    //初始化单选题
    ajaxPost('exam_paper_question.jsp', {'questionId': a}, function (data) {
        $('#singleQuestions').append(data);
        $(".question").hide();
    });
    //初始化多选题
    ajaxPost('exam_paper_question.jsp', {'questionId': b}, function (data) {
        $('#multiQuestions').append(data);
        $(".question").hide();
    });
    //初始化判断题
    ajaxPost('exam_paper_question.jsp', {'questionId': c}, function (data) {
        $('#judgeQuestions').append(data);
        $(".question").hide();
    });
    //初始化问答题
    ajaxPost('exam_paper_question.jsp', {'questionId': d}, function (data) {
        $('#answerQuestions').append(data);
        $(".question").hide();
    });

    function updateSinglePer() {
        var a = Number($("#single_count").text());
        $("#single_per").html(replaceScore(a, Number(o("single_totle_score").value)));
    }

    function updateMultiPer() {
        var a = Number($("#multi_count").text());
        var b = Number(o("multi_totle_score").value);
        $("#multi_per").html(replaceScore(a, b));
    }

    function updateJudgePer() {
        var a = Number($("#judge_count").text());
        var b = Number(o("judge_totle_score").value);
        $("#judge_per").html(replaceScore(a, b));
    }

    function isPositiveNum(s) {//是否为正整数
        var re = /^[0-9]*[1-9][0-9]*$/;
        return re.test(s);
    }
</script>
</body>
</html>
