<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.exam.Config" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="org.json.JSONObject" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <title>试卷得分</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/livevalidation_standalone.js"></script>
    <%
        if (!privilege.isUserLogin(request)) {
            out.print(StrUtil.Alert_Back("请先登录！"));
            return;
        }
        int scoreId = ParamUtil.getInt(request, "scoreId");
        ScoreMgr sm = new ScoreMgr();

        ScoreDb sd = new ScoreDb();
        sd = sd.getScoreDb(scoreId);
        String name = "";
        if (sd.getIsprj() == ScoreDb.IS_PRJ_PERSON) {
            name = sd.getUserName();
        } else {
            UserDb ud = new UserDb();
            ud = ud.getUserDb(sd.getUserName());
            name = ud.getRealName();
        }
        int paperId = sd.getPaperId();
        UserAnswerDb uad = new UserAnswerDb();
        Vector v = uad.getUserAnswersOfScore(scoreId);
        QuestionDb qdb = new QuestionDb();
        PaperDb pdb = new PaperDb();
        pdb = pdb.getPaperDb(paperId);
    %>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1"><%=name %>&nbsp;&nbsp;的试卷得分</td>
    </tr>
    </tbody>
</table>
<div style="text-align:center; height: 30px">本套试题总分：<%=pdb.getTotalper()%> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;得分：<%=sd.getScore()%></div>
<form id="form1" method="post">
    <table class="tabStyle_1 percent98" width="80%" align="center" cellpadding="0" cellspacing="1" id="AutoNumber2" style="border-collapse: collapse">
        <tr>
            <td class="tabStyle_1_title" height="9%" width="4%">序号</td>
            <td class="tabStyle_1_title" width="25%">题目</td>
            <td class="tabStyle_1_title" width="17%" align="center">答案</td>
            <td class="tabStyle_1_title" width="14%">正确答案</td>
            <td class="tabStyle_1_title" width="7%">结果</td>
            <td class="tabStyle_1_title" width="5%" align="center">分值</td>
            <td class="tabStyle_1_title" width="5%" align="center">得分</td>
        </tr>
        <%
            int s = 1;
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                uad = (UserAnswerDb) ir.next();
                int questionId1 = uad.getQuestionId();
                qdb = qdb.getQuestionDb(questionId1);
        %>
        <tr>
            <td align="center" height="20" width="4%"><%=s%>
            </td>
            <td><%=qdb.getQuestion()%>
            </td>
            <td align="left"><%
                if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                    QuestionSelectDb qsd = new QuestionSelectDb();
                    qsd = (QuestionSelectDb) qsd.getQObjectDb(uad.getUserAnswer());
                    if (qsd!=null) {
                        int r = (int) 'A';
                        r = r + qsd.getInt("orders");
                        out.print((char) r + "      ");
                    }
                } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                    String selOptionIds[] = uad.getUserAnswer().split(",");
                    QuestionSelectDb qsd = new QuestionSelectDb();
                    for (int t = 0; t < selOptionIds.length; t++) {
                        qsd = (QuestionSelectDb) qsd.getQObjectDb(selOptionIds[t]);
                        if (qsd!=null) {
                            int r = (int) 'A' + qsd.getInt("orders");
                            out.print((char) r + "      ");
                        }
                    }
                } else if (qdb.getType() == QuestionDb.TYPE_JUDGE) {
                    if ("n".equals(uad.getUserAnswer())) {
                        out.print("错          ");
                    } else {
                        out.print("对          ");
                    }
                } else {
                    out.print(uad.getUserAnswer() + "      ");
                }
            %></td>
            <td align="left"><%
                if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                    QuestionSelectDb qsd = new QuestionSelectDb();
                    qsd = (QuestionSelectDb) qsd.getQObjectDb(qdb.getAnswer());
                    if (qsd!=null) {
                        int k = qsd.getInt("orders");
                        int o = (int) 'A';
                        o = o + k;
                        out.print((char) o + "      " + qsd.getString("content").replaceAll("</?[^/?(img)][^><]*>", ""));
                    }
                } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                    String optionIds[] = qdb.getAnswer().split(",");
                    QuestionSelectDb qsd = new QuestionSelectDb();
                    for (int t = 0; t < optionIds.length; t++) {
                        qsd = (QuestionSelectDb) qsd.getQObjectDb(optionIds[t]);
                        if (qsd!=null) {
                            int k = qsd.getInt("orders");
                            int o = (int) 'A';
                            o = o + k;
                            out.print((char) o + "      ");
                        }
                    }
                } else if (qdb.getType() == QuestionDb.TYPE_JUDGE) {
                    if ("n".equals(qdb.getAnswer())) {
                        out.print("错");
                    } else {
                        out.print("对");
                    }
                } else {
                    out.print(qdb.getAnswer());
                }
            %></td>
            <td align="center">
			<%
                if (uad.getIsCorrect() == 1) {%>
            √
            <%
            } else {
                if (qdb.getType() == QuestionDb.TYPE_ANSWER) {
            %>
            人工评分
            <%
            } else {%>
            <span style="color: red" align="center">×</span>
            <%
                    }
                }
            %>
			</td>
            <td width="3%" align="center">
                <%
                    if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                        out.print("" + pdb.getSingleper());
                    } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                        if (pdb.getMultiScoreRule() == PaperDb.MULTI_ALL_RIGHT_SCORE) {
                            out.print("" + pdb.getMultiper());
                        } else {
                            out.print("" + pdb.getNotAllRightMuntiper());
                        }
                    } else if (qdb.getType() == QuestionDb.TYPE_JUDGE) {
                        out.print("" + pdb.getJudgeper());
                    } else {
                        out.print("" + pdb.getAnswerper());
                    }
                %></td>
            <td width="20%" align="center" nowrap="nowrap">
                <%
                    if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                        if (uad.getIsCorrect() == 1) {
                            out.print("" + pdb.getSingleper());
                        } else {
                            out.print("0");
                        }
                    } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                        if (uad.getIsCorrect() == 1) {
                            if (pdb.getMultiScoreRule() == PaperDb.MULTI_ALL_RIGHT_SCORE) {
                                out.print("" + pdb.getMultiper());
                            } else {
                                out.print("" + pdb.getNotAllRightMuntiper());
                            }
                        } else {
                            out.print("0");
                        }
                    } else if (qdb.getType() == QuestionDb.TYPE_JUDGE) {
                        if (uad.getIsCorrect() == 1) {
                            out.print("" + pdb.getJudgeper());
                        } else {
                            out.print("0");
                        }
                    } else {
                        if (sm.isAnswerChecked(scoreId)) {%>
                <input class="answer_score" id="<%=uad.getId() %>" size="5%" type="text" value=""/>
                <%} else {%>
                <input class="answer_score" id="<%=uad.getId() %>" size="5%" type="text" value="<%=uad.getScore() %>"/>
                <%
                    }
                %>
                <script>
                    var a_<%=qdb.getId() %> = new LiveValidation('<%=uad.getId() %>');
                    a_<%=qdb.getId() %>.add(Validate.Presence);
                    a_<%=qdb.getId() %>.add(Validate.Numericality, {minimum: 0, tooLowMessage: '必须大于等于0'});
                    a_<%=qdb.getId() %>.add(Validate.Numericality, {maximum: <%=pdb.getAnswerper()%>, tooLowMessage: '不能大于<%=pdb.getAnswerper()%>'});
                </script>
            </td>
        </tr>
        <%
                }
                s++;
            }
        %>
    </table>
    <table width="80%" class="tabStyle_1 percent98" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="center">
            <strong>评语</strong></td>
        </tr>
        <tr>
            <td align="center">
                <textarea id="remarks" name="remarks" rows="3" style="width:100%"><%=StrUtil.getNullStr(sd.getRemarks()) %></textarea>
            </td>
        </tr>
    </table>
</form>
<p align="center">
    <%
        JSONObject nestScoreJson = sm.getNextScore(sd.getPaperId(), sd.getId(), sd.getIsprj());
        JSONObject lastScoreJson = sm.getLastScore(sd.getPaperId(), sd.getId(), sd.getIsprj());
        int nextScoreId;
        int lastScoreId;
        if (lastScoreJson.getInt("ret") == 1) {
            lastScoreId = lastScoreJson.getInt("scoreId");
    %>
    <input id="toLast" type="button" onclick="changeScore('<%=lastScoreId %>','0')" value="上一个"/>&nbsp;&nbsp;&nbsp;&nbsp;
    <%
        }
    %>
    <input id="submitComment" type="button" onclick="submitComment()" value="确定"/>
    <%
        if (nestScoreJson.getInt("ret") == 1) {
            nextScoreId = nestScoreJson.getInt("scoreId");
    %>
    &nbsp;&nbsp;&nbsp;&nbsp;<input id="toNext" type="button" onclick="changeScore('<%=nextScoreId %>','1')" value="下一个"/>
    <%
        }
    %>
</p>
<script>
    function submitComment() {
        var sum = 0;
        var scoreId = "<%=scoreId%>";
        var oldScore = "<%=sd.getScore()%>";
        var papaerId = "<%=sd.getPaperId()%>";
        var re = true;
        var jsonData = {};
        var id_array = new Array();
        $(".answer_score").each(function (i, n) {
            if ($(this).val() == "") {
                jAlert("问答题未评分", "提示");
                re = false;
            } else {
                jsonData['answer_' + $(this).attr('id')] = $(this).val();
                sum += Number($(this).val());
                id_array.push($(this).attr('id'));
            }
        });
        var idstr = id_array.join(',');//将数组元素连接起来以构建一个字符串
        var newScore = sum + Number(oldScore);
        var remarks = o("remarks").value;
        jsonData['scoreId'] = scoreId;
        jsonData['newScore'] = newScore;
        jsonData['remarks'] = remarks;
        jsonData['idstr'] = idstr;
        jsonData['papaerId'] = papaerId;
        if (re) {
            ajaxPost('../examScore/answerCheck.do', jsonData, function (data) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    jAlert_Redirect(data.msg, "提示", "exam_score_comment.jsp?scoreId=<%=scoreId%>");
                } else {
                    jAlert_Redirect(data.msg, "提示", "exam_score_comment.jsp?scoreId=<%=scoreId%>");
                }
            });
        }
    }

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

    function changeScore(scoreId, flg) {
        if (flg == "0") {
            jConfirm("您确定要评上一个吗？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    window.location.href = "exam_score_comment.jsp?scoreId=" + scoreId;
                }
            })
        } else {
            jConfirm("您确定要评下一个吗？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    window.location.href = "exam_score_comment.jsp?scoreId=" + scoreId;
                }
            })
        }
    }

    // 调整图片的宽高
    function adjustImg() {
        var maxWidth = 200; // 图片最大宽度
        var maxHeight = 200; // 图片最大高度
        var charImg = document.getElementsByTagName("img");
        var imgURLs = "";
        for (var i = 0; i < charImg.length; i++) {
            var $img = $(charImg[i]);

            var ratio = 0; // 缩放比例
            var width = $img.width(); // 图片实际宽度
            var height = $img.height(); // 图片实际高度

            // 检查图片是否超宽
            if(width > maxWidth){
                ratio = maxWidth / width; // 计算缩放比例
                $img.css("width", maxWidth); // 设定实际显示宽度
                height = height * ratio; // 计算等比例缩放后的高度
                $img.css("height", height); // 设定等比例缩放后的高度
            }

            // 检查图片是否超高
            if(height > maxHeight){
                ratio = maxHeight / height; // 计算缩放比例
                $img.css("height", maxHeight); // 设定实际显示高度
                width = width * ratio; // 计算等比例缩放后的高度
                $img.css("width", width * ratio); // 设定等比例缩放后的高度
            }

            $img.css("cursor", "pointer");
            $img.click(function() {
                openWin($img.attr('src'));
            })
        }
    }

    $(function() {
        adjustImg();
    });
</script>
</body>
</html>
