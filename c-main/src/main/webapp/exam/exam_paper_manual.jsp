<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>手工组卷</title>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <style type="text/css">
        img {
            width: 400px;
            height: 300px
        }
    </style>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin.exam")) {
        out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String userName = privilege.getUser(request);
%>
<div id="div1">
    <form id="creat_paper" name="creat_paper" method="post">
        <table class="tabStyle_1 percent98" style="margin-top: 10px">
            <div id="bodyBox"></div>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;<b>名称</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="title" size="30"/></td>
            </tr>
            <tr>
                <td width="855" colspan="4">
                    &nbsp;&nbsp;&nbsp;<b>专业</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <select id="major" size="1" name="major">
                        <%
                            TreeSelectDb tsd = new TreeSelectDb();
                            tsd = tsd.getTreeSelectDb(MajorView.ROOT_CODE);
                            MajorView mv = new MajorView(tsd);
                            StringBuffer sb = new StringBuffer();
                            mv.getTreeSelectByUserAsOptions(sb, tsd, 1, userName, "0");
                        %>
                        <%=sb %>
                    </select>
                </td>
            </tr>
            <tr>
                <td align="left">&nbsp;&nbsp;&nbsp;<b>单选题</b></td>
            </tr>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="single_total_score" name="single_total_score" size="5" onchange="updateQuestionPer('single')"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_SINGLE%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="single_count_score">共</span>
                    &nbsp;&nbsp;<span class="single_count_score" id="single_count">0</span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="single_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="single_count_score" id="single_per">0</span><span class="single_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="singleQuestions">&nbsp;&nbsp;&nbsp;&nbsp;</div>
                </td>
            </tr>
            <tr>
                <td><b>&nbsp;&nbsp;&nbsp;多选题</b></td>
            </tr>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="multi_total_score" name="multi_total_score" size="5" onchange="updateQuestionPer('multi')"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_MULTI%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="multi_count_score">共</span>
                    &nbsp;&nbsp;<span class="multi_count_score" id="multi_count">0</span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="multi_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="multi_count_score" id="multi_per">0</span><span class="multi_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;&nbsp;<div id="multiQuestions"></div>
                </td>
            </tr>
            <tr>
                <td><b>&nbsp;&nbsp;&nbsp;判断题</b></td>
            </tr>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="judge_total_score" name="judge_total_score" size="5" onchange="updateQuestionPer('judge')"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_JUDGE%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="judge_count_score">共</span>
                    &nbsp;&nbsp;<span class="judge_count_score" id="judge_count">0</span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="judge_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="judge_count_score" id="judge_per">0</span><span class="judge_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;&nbsp;<div id="judgeQuestions"></div>
                </td>
            </tr>
            <tr>
                <td><b>&nbsp;&nbsp;&nbsp;问答题</b></td>
            </tr>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;分值
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="answer_total_score" name="answer_total_score" size="5" onchange="updateQuestionPer('answer')"/>
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="选 择" onclick="selectQuestion(<%=QuestionDb.TYPE_ANSWER%>)"/>&nbsp;&nbsp;&nbsp;&nbsp;<span class="answer_count_score">共</span>
                    &nbsp;&nbsp;<span class="answer_count_score" id="answer_count">0</span>
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="answer_count_score">题&nbsp;&nbsp;每题</span>&nbsp;&nbsp;<span class="answer_count_score" id="answer_per">0</span><span class="answer_count_score">分</span>
                </td>
            </tr>
            <tr>
                <td>&nbsp;&nbsp;&nbsp;&nbsp;<div id="answerQuestions"></div>
                </td>
            </tr>
            <tr>
                <td align="left">&nbsp;&nbsp;&nbsp;<b>考试模式</b>&nbsp;&nbsp;&nbsp;&nbsp;
                    <select id="mode" name="mode">
                        <option id="1" selected="selected">按指定时间参加考试</option>
                        <option id="2">按有效期参加考试</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>
                    &nbsp;&nbsp;&nbsp;从&nbsp;<input type="text" id="starttime" name="starttime" size="20" value="" class="inputnormal" readonly/>
                    &nbsp;&nbsp;至
                    &nbsp;&nbsp;&nbsp;<input type="text" id="endtime" name="endtime" size="20" value="" class="inputnormal" readonly/>
                    &nbsp;&nbsp;&nbsp;
                    <span class="testtime" style="display: none">考试时长
						&nbsp;&nbsp;<input type="text" id="testtime" name="testtime" value="0" size="5"/>&nbsp;分钟
                        &nbsp;&nbsp;限考&nbsp;<input id="limitCount" name="limitCount" value="1" size="5"/>&nbsp;次
                    </span>
                </td>
            </tr>
            <tr>
                <td>
                    <span style=" float: left;">
                        <b>&nbsp;&nbsp;&nbsp;多选题计分规则&nbsp;&nbsp;&nbsp;</b>
                        <input type="radio" name="multiScoreRule" value="0" checked/>部分答对不计分
                        <input type="radio" name="multiScoreRule" value="1"/>部分答对计分
                    </span>
                    <span style="display: none; float: left;" id="otherMultiPer">
                        &nbsp;&nbsp;&nbsp;分值&nbsp;&nbsp;<input id="notAllRightMuntiper" name="notAllRightMuntiper"/>
                    </span>
                </td>
            </tr>
            <tr>
                <td colspan="6" align="center"><input type="button" value="预 览" onclick="paperShow()"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="确定" onclick="creatPaper()"/></td>
            </tr>
        </table>
        <input type="hidden" id="singleIds" name="singleIds" value=""/>
        <input type="hidden" id="multiIds" name="multiIds" value=""/>
        <input type="hidden" id="judgeIds" name="judgeIds" value=""/>
        <input type="hidden" id="answerIds" name="answerIds" value=""/>
        <input type='hidden' name="singleTotal" value="0"/>
        <input type='hidden' name="multiTotal" value="0"/>
        <input type='hidden' name="judgeTotal" value="0"/>
        <input type='hidden' name="answerTotal" value="0"/>
        <input type='hidden' name="singlecount" value="0"/>
        <input type='hidden' name="multicount" value="0"/>
        <input type="hidden" name="judgecount" value="0"/>
        <input type="hidden" name="answercount" value="0"/>
        <input type="hidden" name="singleper" value="0"/>
        <input type="hidden" name="multiper" value="0"/>
        <input type="hidden" name="judgeper" value="0"/>
        <input type="hidden" name="answerper" value="0"/>
        <input type="hidden" id="isManual" name="isManual" value="1"/>
        <input type="hidden" id="totalper" name="totalper" value=""/>
        <input type="hidden" id="questionIds" name="questionIds" value=""/>
        <input type="hidden" id="sIds" name="sIds" value=""/>
        <input type="hidden" id="mIds" name="mIds" value=""/>
        <input type="hidden" id="jIds" name="jIds" value=""/>
        <input type="hidden" id="aIds" name="aIds" value=""/>
    </form>
</div>
<script>
    //初始化时间验证
    var starttime = new LiveValidation('starttime');
    starttime.add(Validate.Presence);
    var endtime = new LiveValidation('endtime');
    endtime.add(Validate.Presence);
    //分值必须为数字验证
    var singleTotal = new LiveValidation('single_total_score');
    var multiTotal = new LiveValidation('multi_total_score');
    var judgeTotal = new LiveValidation('judge_total_score');
    var answerTotal = new LiveValidation('answer_total_score');
    singleTotal.add(Validate.Numericality);
    multiTotal.add(Validate.Numericality);
    judgeTotal.add(Validate.Numericality);
    answerTotal.add(Validate.Numericality);
    var rootCode = "<%=MajorView.ROOT_CODE%>";
    // 下拉列表框的change事件 选择“按有效期参加考试”弹出考试时长输入框
    $("#mode").change(function () {
        var s1 = $("#mode ").get(0).selectedIndex;
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
        }
        ;
    });
    $('#starttime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
    $('#endtime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});

    //ajax提交通用方法
    function ajaxPost(path, parameter, func) {
        $.ajax({
            type: "post",
            url: path,
            async: false,
            data: parameter,
            dataType: "html",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            success: function (data, status) {
                func(data);
            },
            error: function (XMLHttpRequest, textStatus) {
                $('#bodyBox').hideLoading();
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    $(function () {
        var oldSingleIds = o("singleIds").value;
        var oldMultiIds = o("multiIds").value;
        var oldJudgeIds = o("judgeIds").value;
        var oldAnswerIds = o("answerIds").value;
        $('.single_count_score').hide();
        $('.multi_count_score').hide();
        $('.judge_count_score').hide();
        $('.answer_count_score').hide();
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
                oldAnswerIds = newAnswerIds;
            }
        }, 500);
        // 多选题得分规则单选按钮change事件
        $('input[type=radio][name=multiScoreRule]').change(function () {
            var multiPer = $("#multi_per").text();
            if (this.value == '0') {
                $("#otherMultiPer").hide();
            } else if (this.value == '1') {
                $("#otherMultiPer").show();
                var multiNotAllRightScore = new LiveValidation('notAllRightMuntiper');
                multiNotAllRightScore.add(Validate.Presence);
                multiNotAllRightScore.add(Validate.Numericality, {minimum: 0, tooLowMessage: '必须大于等于0'});
                multiNotAllRightScore.add(Validate.Numericality, {maximum: multiPer, tooLowMessage: '不能大于' + multiPer});
            }
        });
    })

    // 打开窗口方法
    function openWin(url, width, height) {
        var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,stat	us=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
        return newwin;
    }

    //选择题目方法
    function selectQuestion(type) {
        var major = o("major").value;
        var a = o("single_total_score").value;
        var b = o("multi_total_score").value;
        var c = o("judge_total_score").value;
        var d = o("answer_total_score").value;
        if (major == rootCode) {
            jAlert("请先选择专业", "提示");
            return;
        }
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
        var url = "exam_question_select_frame.jsp?type=" + type + "&major=" + major;
        openWin(url, 1000, 800);
    }

    // 生成试卷
    function creatPaper() {
        o("singlecount").value = $("#single_count").text();
        o("multicount").value = $("#multi_count").text();
        o("judgecount").value = $("#judge_count").text();
        o("singleper").value = $("#single_per").text();
        o("multiper").value = $("#multi_per").text();
        o("judgeper").value = $("#judge_per").text();
        o("answercount").value = $("#answer_count").text();
        o("answerper").value = $("#answer_per").text();
        if (!o("single_total_score").value == "") {
            o("singleTotal").value = o("single_total_score").value;
        } else {
            o("singleTotal").value = "0";
        }
        if (!o("multi_total_score").value == "") {
            o("multiTotal").value = o("multi_total_score").value;
        } else {
            o("judgeTotal").value = "0";
        }
        if (!o("judge_total_score").value == "") {
            o("judgeTotal").value = o("judge_total_score").value;
        } else {
            o("judgeTotal").value = "0";
        }
        if (!o("answer_total_score").value == "") {
            o("answerTotal").value = o("answer_total_score").value;
        } else {
            o("answerTotal").value = "0";
        }
        var title = o("title").value;
        var a = Number(o("single_total_score").value);
        var b = Number(o("multi_total_score").value);
        var c = Number(o("judge_total_score").value);
        var d = Number(o("answer_total_score").value);
        var e = a + b + c + d;
        o("totalper").value = e;
        if (title == "") {
            jAlert("请填写试卷名", "提示");
            return;
        }
        if (e != 100) {
            jAlert("试卷总分不等于100", "提示");
            return;
        }
        if (a > 0) {
            if (o("singlecount").value == "0") {
                jAlert("单选题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("singleper").value)) {
                jAlert("单选题每题分值不能为小数", "提示");
                return;
            }
            ;
        }
        if (b > 0) {
            if (o("multicount").value == "0") {
                jAlert("多选题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("multiper").value)) {
                jAlert("多选题每题分值不能为小数", "提示");
                return;
            }
            ;
        }
        if (c > 0) {
            if (o("judgecount").value == "0") {
                jAlert("判断题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("judgeper").value)) {
                jAlert("判断题每题分值不能为小数", "提示");
                return;
            }
            ;

        }
        if (d > 0) {
            if (o("answercount").value == "0") {
                jAlert("问答题题目数量不能为0", "提示");
                return;
            }
            if (!isPositiveNum(o("answerper").value)) {
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
        if ($("input[name='multiScoreRule']:checked").val() == 1) {
            if (o("notAllRightMuntiper").value == "") {
                jAlert("请设置多选题部分答对分值", "提示");
                return;
            }
        }
        var formData = $("#creat_paper").serialize();
        ajaxPost('../exam/paperAdd.do', formData, function (data) {
            data = $.parseJSON(data);
            if (data.ret == 1) {
                jAlert_Redirect(data.msg, '提示', 'exam_paper_manager.jsp');
            } else if (data.ret == 0) {
                jAlert(data.msg, "提示");
            }
        });
    }

    //预览试卷方法
    function paperShow() {
        var singleIds = o("singleIds").value;
        var multiIds = o("multiIds").value;
        var judgeIds = o("judgeIds").value;
        var answerIds = o("answerIds").value;
        var title = o("title").value;
        var url = "exam/exam_paper_show.jsp?singleIds=" + singleIds + "&multiIds=" + multiIds + "&judgeIds=" + judgeIds + "&title=" + title + "&answerIds=" + answerIds;
        url = decodeURI(url);
        addTab("预览试卷", url);
    }

    //展开题目选项和答案
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
    function replaceScore(count, TotalCount) {
        var avgCount;
        if (count != 0) {
            avgCount = TotalCount / count;
        } else {
            avgCount = 0;
        }
        return avgCount;
    }

    // 删除已选的题目 并更新分值
    function delQ(qId, tabId, type) {
        jConfirm('您确定要删除吗？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                if (type == 0) {
                    $('#' + tabId).remove();
                    o("singleIds").value = repalceIds(qId, o("singleIds").value);
                    o("questionIds").value = repalceIds(qId, o("questionIds").value);
                    var a = Number($("#single_count").text()) - Number(1);
                    $("#single_count").html(a);
                    $("#single_per").html(replaceScore(a, Number(o("single_total_score").value)));
                } else if (type == 1) {
                    $('#' + tabId).remove();
                    var a = Number($("#multi_count").text()) - Number(1);
                    var b = Number(o("multi_total_score").value);
                    $("#multi_count").html(a);
                    $("#multi_per").html(replaceScore(a, b));
                    o("multiIds").value = repalceIds(qId, o("multiIds").value);
                    o("questionIds").value = repalceIds(qId, o("questionIds").value);
                } else if (type == 2) {
                    $('#' + tabId).remove();
                    var a = Number($("#judge_count").text()) - Number(1);
                    var b = Number(o("judge_total_score").value);
                    $("#judge_count").html(a);
                    $("#judge_per").html(replaceScore(a, b));
                    o("judgeIds").value = repalceIds(qId, o("judgeIds").value);
                    o("questionIds").value = repalceIds(qId, o("questionIds").value);
                } else if (type == 3) {
                    $('#' + tabId).remove();
                    var a = Number($("#answer_count").text()) - Number(1);
                    var b = Number(o("answer_total_score").value);
                    $("#answer_count").html(a);
                    $("#answer_per").html(replaceScore(a, b));
                    o("answerIds").value = repalceIds(qId, o("answerIds").value);
                    o("questionIds").value = repalceIds(qId, o("questionIds").value);
                }
            }
        });
    }

    //改变题目总分 同步更新每一题的分值
    function updateQuestionPer(type) {
        var a = Number($("#" + type + "_count").text());
        $("#" + type + "_per").html(replaceScore(a, Number(o(type + "_total_score").value)));
    }

    //是否为正整数
    function isPositiveNum(s) {
        var re = /^[0-9]*[1-9][0-9]*$/;
        return re.test(s);
    }
</script>
</body>
</html>
