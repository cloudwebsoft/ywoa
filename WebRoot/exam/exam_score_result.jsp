<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@page import="com.redmoon.oa.flow.FormDb" %>
<%@page import="com.redmoon.oa.visual.FormDAO" %>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    //清除session
    session.removeAttribute("beginTestTime");
    if (!privilege.isUserLogin(request)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
        return;
    }
    String userName = privilege.getUser(request);
    String questionId = ParamUtil.get(request, "questionId");
    if (questionId.equals("")) {
        out.print(StrUtil.Alert_Back("现在没有题目！"));
        return;
    }
    int paperId = ParamUtil.getInt(request, "paperId");

    // 检查用户是否已答过试卷
    ScoreDb std = new ScoreDb();
    //if (std.isUserExamed(userName, paperId)) {
    //out.print(StrUtil.Alert_Back("您已经参加过该考试！"));
    //return;
    //}

    String no = "";
    String[] questionAry = questionId.split(",");
    String str[];
    String answerStr = "";
    String result = "";
    String replaceStr = "";
    int total = 0;
    QuestionDb qdb = new QuestionDb();
    PaperDb pdb = new PaperDb();
    pdb = pdb.getPaperDb(paperId);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <title>试卷评分</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tdStyle_1">答卷</td>
    </tr>
</table>
<table class="tabStyle_1 percent80" width="80%">
    <tr>
        <td width="5%" class="tabStyle_1_title">序号</td>
        <td width="29%" class="tabStyle_1_title">题目</td>
        <td width="18%" class="tabStyle_1_title">我的答案</td>
        <td width="22%" class="tabStyle_1_title">正确答案</td>
        <td width="13%" class="tabStyle_1_title">结果</td>
        <td width="13%" class="tabStyle_1_title">分值</td>
    </tr>
    <%
        int s = 0;
        Vector v = new Vector();
        for (int i = 0; i < questionAry.length; i++) {
            s++;
            if (questionAry[i].equals(""))
                continue;
            int questionId1 = Integer.parseInt(questionAry[i]);
            qdb = qdb.getQuestionDb(questionId1);
    %>
    <tr>
        <td align="center"><%=s%>
        </td>
        <td><%=qdb.getQuestion()%>
        </td>
        <td>
            <%
                no = Integer.toString(qdb.getId());
                str = request.getParameterValues(no);
                String[] ary = new String[3];
                if (str != null) {
                    for (int k = 0; k < str.length; k++) {
                        str[k] = StrUtil.UnicodeToUTF8(str[k]);
                        if (answerStr.equals(""))
                            answerStr = str[k];
                        else
                            answerStr += "," + str[k];
                    }
                    replaceStr = qdb.getAnswer();
                    if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                        if ("".equals(answerStr)) {
                            out.print("无");
                        }
                        else {
                            QuestionSelectDb qsd = new QuestionSelectDb();
                            qsd = (QuestionSelectDb) qsd.getQObjectDb(answerStr);
                            if (qsd != null) {
                                int r = (int) 'A';
                                r = r + qsd.getInt("orders");
                                out.print((char) r + "      ");
                            } else {
                                out.print("不存在");
                            }
                        }
                    } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                        replaceStr = replaceStr.replaceAll(" ", "");
                        answerStr = answerStr.replaceAll(" ", "");
                        String selOptionIds[] = answerStr.split(",");
                        if (selOptionIds==null) {
                            out.print("无");
                        }
                        else {
                            QuestionSelectDb qsd = new QuestionSelectDb();
                            for (int t = 0; t < selOptionIds.length; t++) {
                                qsd = (QuestionSelectDb) qsd.getQObjectDb(selOptionIds[t]);
                                if (qsd != null) {
                                    int r = (int) 'A' + qsd.getInt("orders");
                                    out.print((char) r + "      ");
                                } else {
                                    out.print("不存在");
                                }
                            }
                        }
                    } else if (qdb.getType() == QuestionDb.TYPE_JUDGE) {
                        if ("n".equals(answerStr)) {
                            out.print("错误");
                        } else {
                            out.print("正确");
                        }
                    } else {
                        out.print(answerStr);
                    }
                    ary[0] = no;
                    ary[1] = answerStr;
                    if (answerStr.equals(replaceStr)) {
                        result = "√";
                        ary[2] = "1";
                    } else {
                        if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                            if (pdb.getMultiScoreRule() == PaperDb.MULTI_NOT_ALL_RIGHT_SCORE) {
                                if (replaceStr.contains(answerStr)) {
                                    result = "√";
                                    ary[2] = "1";
                                } else {
                                    result = "×";
                                    ary[2] = "0";
                                }
                            } else {
                                result = "×";
                                ary[2] = "0";
                            }
                        } else if (qdb.getType() == QuestionDb.TYPE_ANSWER) {
                            result = "";
                            ary[2] = "2";
                        } else {
                            result = "×";
                            ary[2] = "0";
                        }
                    }
                    v.add(ary);
                    answerStr = "";
                } else {
                    result = "×";
                    ary[0] = no;
                    ary[1] = "";
                    ary[2] = "0";
                    v.add(ary);
                }
            %>
        </td>
        <td>
            <%
                String va = qdb.getAnswer();
                if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                    QuestionSelectDb qsd = new QuestionSelectDb();
                    qsd = (QuestionSelectDb) qsd.getQObjectDb(qdb.getAnswer());
                    if (qsd!=null) {
                        int k = qsd.getInt("orders");
                        int o = (int) 'A';
                        o = o + k;
                        out.print((char) o + "、" + qsd.getString("content").replaceAll("</?[^/?(img)][^><]*>", ""));
                    }
                    else {
                        out.print("无");
                    }
                } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                    String optionIds[] = va.split(",");
                    QuestionSelectDb qsd = new QuestionSelectDb();
                    for (int t = 0; t < optionIds.length; t++) {
                        qsd = (QuestionSelectDb) qsd.getQObjectDb(optionIds[t]);
                        if (qsd!=null) {
                            int k = qsd.getInt("orders");
                            int o = (int) 'A';
                            o = o + k;
                            out.print((char) o + "、" + qsd.getString("content").replaceAll("</?[^/?(img)][^><]*>", "") + "<BR/>");
                        }
                        else {
                            out.print("无<br/>");
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
            %>
        </td>
        <td>
            <%
                if (qdb.getType() == QuestionDb.TYPE_ANSWER) {
                    out.print("待人工评分");
                } else {
                    out.print(result);
                }
            %>
        </td>
        <td>
            <%
                if (result.equals("√")) {
                    if (qdb.getType() == QuestionDb.TYPE_SINGLE) {
                        out.print("+" + pdb.getSingleper());
                        total += pdb.getSingleper();
                    } else if (qdb.getType() == QuestionDb.TYPE_MULTI) {
                        if (pdb.getMultiScoreRule() == PaperDb.MULTI_ALL_RIGHT_SCORE) {
                            out.print("+" + pdb.getMultiper());
                            total += pdb.getMultiper();
                        } else {
                            out.print("+" + pdb.getNotAllRightMuntiper());
                            total += pdb.getNotAllRightMuntiper();
                        }
                    } else if (qdb.getType() == QuestionDb.TYPE_ANSWER) {

                    } else {
                        out.print("+" + pdb.getJudgeper());
                        total += pdb.getJudgeper();
                    }
                }
            %>
        </td>
    </tr>
    <%}%>
    <tr>
        <td align="center" colspan="6">总分：<%=total%><input type="hidden" value=<%=total%> name="score"/>
            <%
                ScoreMgr smg = new ScoreMgr();
                String starttime = ParamUtil.get(request, "starttime");
                boolean re = false;
                try {
                    std.setUserName(userName);
                    std.setPaperId(pdb.getId());
                    std.setScore(total);
                    std.setEndtime(new Date());
                    // std.setMobile(sud.getMobile());
                    re = std.create();
                    if (re) {
                        UserAnswerMgr uam = new UserAnswerMgr();
                        Iterator ir = v.iterator();
                        while (ir.hasNext()) {
                            String[] ary = (String[]) ir.next();
                            System.out.println(getClass() + " 0:" + ary[0] + " 1:" + ary[1] + " 2:" + ary[2]);
                            uam.create(std.getId(), StrUtil.toInt(ary[0]), ary[1], StrUtil.toInt(ary[2]));
                        }
                    }
                } catch (ErrMsgException e) {
                    out.print(StrUtil.Alert(e.getMessage()));
                }
            %></td>
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
