<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.exam.Config" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>试卷得分</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
</head>
<body>
<%
    if (!privilege.isUserLogin(request)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
        return;
    }

    int scoreId = ParamUtil.getInt(request, "scoreId");
    ScoreMgr sm = new ScoreMgr();
    ScoreDb sd = new ScoreDb();
    sd = sd.getScoreDb(scoreId);

    int paperId = sd.getPaperId();
    UserAnswerDb uad = new UserAnswerDb();
    Vector v = uad.getUserAnswersOfScore(scoreId);

    QuestionDb qdb = new QuestionDb();
    PaperDb pdb = new PaperDb();
    pdb = pdb.getPaperDb(paperId);
%>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">试卷得分</td>
    </tr>
    </tbody>
</table>
<div style="text-align:center; height:30px">本套试题总分：<%=pdb.getTotalper()%> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;得分：<%=sd.getScore()%>
</div>
<table class="tabStyle_1 percent98" width="80%" align="center" cellpadding="0" cellspacing="1" id="AutoNumber2" style="border-collapse: collapse">
    <tr>
        <td class="tabStyle_1_title" height="9%" width="3%">序号</td>
        <td class="tabStyle_1_title" width="34%">题目</td>
        <td class="tabStyle_1_title" width="22%" align="center">答案</td>
        <td class="tabStyle_1_title" width="16%">正确答案</td>
        <td class="tabStyle_1_title" width="6%">结果</td>
        <td class="tabStyle_1_title" width="9%" align="center">分值</td>
        <td class="tabStyle_1_title" width="10%" align="center">得分</td>
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
        <td align="center" height="20" width="3%"><%=s%>
        </td>
        <td><%=qdb.getQuestion()%>
        </td>
        <td align="left"><%
            if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                QuestionSelectDb qsd = new QuestionSelectDb();
                qsd = (QuestionSelectDb) qsd.getQObjectDb(uad.getUserAnswer());
                if (qsd != null) {
                    int r = (int) 'A';
                    r = r + qsd.getInt("orders");
                    out.print((char) r + "      ");
                }
            } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                String selOptionIds[] = uad.getUserAnswer().split(",");
                QuestionSelectDb qsd = new QuestionSelectDb();
                for (int t = 0; t < selOptionIds.length; t++) {
                    qsd = (QuestionSelectDb) qsd.getQObjectDb(selOptionIds[t]);
                    if (qsd != null) {
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
                if (qsd != null && qsd.isLoaded()) {
                    int k = qsd.getInt("orders");
                    int o = (int) 'A';
                    o = o + k;
                    out.print((char) o + "      " + qsd.getString("content").replaceAll("</?[^/?(img)][^><]*>", ""));
                } else {
                    out.print("改题目已不存在");
                }

            } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                String optionIds[] = qdb.getAnswer().split(",");
                QuestionSelectDb qsd = new QuestionSelectDb();
                for (int t = 0; t < optionIds.length; t++) {
                    qsd = (QuestionSelectDb) qsd.getQObjectDb(optionIds[t]);
                    if (qsd != null && qsd.isLoaded()) {
                        int k = qsd.getInt("orders");
                        int o = (int) 'A';
                        o = o + k;
                        out.print((char) o + "      ");
                    } else {
                        out.print("改题目已不存在");
                    }
                }
            } else if (qdb.getType() == QuestionDb.TYPE_JUDGE) {
                if ("n".equals(qdb.getAnswer())) {
                    out.print("错");
                } else {
                    out.print("对");
                }
            } else {
                out.print(qdb.getAnswer() + "      ");
            }
        %></td>
        <td align="center">
            <%if (uad.getIsCorrect() == 1) {%>
            √
            <%
            } else {
                if (qdb.getType() == QuestionDb.TYPE_ANSWER) {
            %>
            人工评分
            <%
            } else {%>
            <span style="color: red">×</span>
            <%
                    }
                }
            %>
        </td>
        <td align="center">
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
            %>
        </td>
        <td align="center">
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
                    if (sm.isAnswerChecked(sd.getId())) {
                        out.print("待人工评分");
                    }
                    else {
                        out.print("" + uad.getScore());
                    }
                } %>
        </td>
    </tr>
    <%
            s++;
        }%>
</table>
<table class="tabStyle_1 percent98" width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td align="center" class="tabStyle_1_title">
            评语
        </td>
    </tr>
    <tr>
        <td>
            <%=StrUtil.getNullStr(sd.getRemarks())%>
        </td>
    </tr>
</table>
</body>
<script>
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
            if (width > maxWidth) {
                ratio = maxWidth / width; // 计算缩放比例
                $img.css("width", maxWidth); // 设定实际显示宽度
                height = height * ratio; // 计算等比例缩放后的高度
                $img.css("height", height); // 设定等比例缩放后的高度
            }

            // 检查图片是否超高
            if (height > maxHeight) {
                ratio = maxHeight / height; // 计算缩放比例
                $img.css("height", maxHeight); // 设定实际显示高度
                width = width * ratio; // 计算等比例缩放后的高度
                $img.css("width", width * ratio); // 设定等比例缩放后的高度
            }

            $img.css("cursor", "pointer");
            $img.click(function () {
                openWin($img.attr('src'));
            })
        }
    }

    $(function () {
        adjustImg();
    });
</script>
</html>
