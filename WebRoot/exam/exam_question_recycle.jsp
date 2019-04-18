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
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@page import="com.cloudwebsoft.framework.base.QObjectDb" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%
    String path = request.getContextPath();
%>
<head>
    <title>题库回收站</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../js/pagination/jquery.pagination.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/pagination/pagination.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <style type="text/css">
        img {
            width: 160px;
            height: 120px
        }
    </style>
</head>
<body>
<%
    String selQuestionSql = "select id from oa_exam_database where is_valid = 1 ";
    String querystr = "";
    int pagesize = 10;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    QuestionDb qd = new QuestionDb();
    ListResult lr = qd.listResult(selQuestionSql, curpage, pagesize);
    int total = lr.getTotal();
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
<div id="bodyBox"></div>
<div id="question">
    <form name="form1" action="" method="post">
        <div id="q_list" style="">
            <p>&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" id="selAll" name="selAll" onclick="selectAll()" value="全选"/>&nbsp;&nbsp;<input type="button" onclick="deleteQuestion()" value="删除"/>&nbsp;&nbsp;<input type="button" onclick="recoveryQuestion()" value="恢复"/></p>
            <%
                int i = 0;
                while (ir != null && ir.hasNext()) {
                    i++;
                    qd = (QuestionDb) ir.next();
            %>
            <table class="tabStyle_1 percent98" id="tab_<%=qd.getId() %>">
                <tr>
                    <td colspan="3">
                        <input type="checkbox" name="questionList" value="<%=qd.getId() %>"/>&nbsp;&nbsp;
                        <img id="plus_<%=qd.getId() %>" src="../forum/images/plus.gif" style="cursor: pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
                        <img id="minus_<%=qd.getId() %>" class="question" src="../forum/images/minus.gif" style="cursor: pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
                        <%if (qd.getType() == QuestionDb.TYPE_SINGLE) {%>
                        【单选题】
                        <%
                        } else if (qd.getType() == QuestionDb.TYPE_MULTI) {%>
                        【多选题】
                        <%
                        } else if (qd.getType() == QuestionDb.TYPE_JUDGE) {%>
                        【判断题】
                        <%} else if (qd.getType() == QuestionDb.TYPE_ANSWER) {%>
                        【问答题】
                        <%}%>
                        <%=qd.getId()%>、<%=qd.getQuestion().replaceAll("</?[^/?(img)][^><]*>", "") %>
                    </td>
                </tr>
                <%
                    if (qd.getType() == QuestionDb.TYPE_SINGLE || qd.getType() == QuestionDb.TYPE_MULTI) {
                        QuestionSelectDb qsd = new QuestionSelectDb();
                        String selSelectOptionSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(qd.getId()));
                        Iterator optionIt = qsd.list(selSelectOptionSql).iterator();
                        int k = 0;
                        while (optionIt.hasNext()) {
                            int o = (int) 'A';
                            o = o + k;
                            qsd = (QuestionSelectDb) optionIt.next();
                %>
                <tr id="q<%=k+1 %>_<%=qd.getId() %>" class="question">
                    <td width="40">&nbsp;&nbsp;<%=(char) o %>、</td>
                    <td colspan="2"><%=qsd.getString("content").replaceAll("</?[^/?(img)][^><]*>", "") %>
                    </td>
                </tr>
                <%
                        k++;
                    }
                %>
                <tr id="q<%=k+1 %>_<%=qd.getId() %>" class="question">
                    <td width="40">&nbsp;&nbsp;答案</td>
                    <td colspan="3">
                        <%
                            QuestionSelectDb answerOptionDb = new QuestionSelectDb();
                            if (qd.getType() == QuestionDb.TYPE_SINGLE) {
                                answerOptionDb = (QuestionSelectDb) answerOptionDb.getQObjectDb(qd.getAnswer());
                                if (answerOptionDb != null) {
                        %>
                        <%=(char) ((int) 'A' + answerOptionDb.getInt("orders"))%>
                        <%
                            }
                            else {
                                answerOptionDb = new QuestionSelectDb();
                            }
                        } else if (qd.getType() == QuestionDb.TYPE_MULTI) {
                            String[] answerStr = qd.getAnswer().split(",");
                            String answerShow = "";
                            for (int j = 0; j < answerStr.length; j++) {
                                answerOptionDb = (QuestionSelectDb) answerOptionDb.getQObjectDb(answerStr[j]);
                                if (answerOptionDb != null) {
                                    if ("".equals(answerShow)) {
                                        answerShow = String.valueOf((char) ((int) 'A' + Integer.parseInt(answerOptionDb.getString("orders"))));
                                    } else {
                                        answerShow += "," + String.valueOf((char) ((int) 'A' + Integer.parseInt(answerOptionDb.getString("orders"))));
                                    }
                                } else {
                                    answerOptionDb = new QuestionSelectDb();
                                }
                            }
                        %>
                        <%=answerShow%>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%} else if (qd.getType() == QuestionDb.TYPE_JUDGE || qd.getType() == QuestionDb.TYPE_ANSWER) {%>
                <tr id="q_<%=qd.getId() %>" class="question">
                    <td width="40">&nbsp;&nbsp;答案</td>
                    <td colspan="3">
                        <%
                            if (qd.getType() == QuestionDb.TYPE_ANSWER) {
                                out.print(qd.getAnswer().replaceAll("</?[^/?(img)][^><]*>", ""));
                            }
                            else {
                                if ("n".equals(qd.getAnswer())) {
                                    out.print("错误");
                                }
                                else {
                                    out.print("正确");
                                }
                            }
                        %>
                    </td>
                </tr>
                <%}%>
            </table>
            <%}%>
            <table class="percent98" width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td align="right">
                        <%
                            out.print(paginator.getCurPageBlock("?" + querystr));
                        %>
                    </td>
                </tr>
            </table>
            <input type="hidden" id="major" name="major" value=""/>
            <div class="M-box" style="width:500px;margin-left: 200px" align="center"></div>
        </div>
    </form>
</div>
<script>
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
            beforeSend: function (XMLHttpRequest) {
                $('#bodyBox').showLoading();
            },
            complete: function (XMLHttpRequest, status) {
                $('#bodyBox').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                $('#bodyBox').hideLoading();
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    $(function () {
        $(".question").hide();
    })

    function showAnswer(id) {
        $("tr[id*=" + id + "]").toggle();
        var plus = 'plus_' + id;
        var minus = 'minus_' + id;
        $('#' + plus).toggle();
        $('#' + minus).toggle();
    }

    function selectAll() {
        var isCheckAll = false;
        var a = o("selAll").value;
        if (a == "全选") {
            isCheckAll = false;
            o("selAll").value = "取消全选";
        } else if (a == "取消全选") {
            isCheckAll = true;
            o("selAll").value = "全选";
        }
        if (isCheckAll) {
            $("input[name='questionList']").each(function () {
                this.checked = false;
            });
        } else {
            $("input[name='questionList']").each(function () {
                this.checked = true;
            });
        }
    }

    function deleteQuestion() {
        jConfirm('您确定要删除么？', '提示', function (r) {
            if (!r) {
                return;
            }
            var id_array = new Array();
            $('input[name="questionList"]:checked').each(function () {
                id_array.push($(this).val());//向数组中添加元素
            });
            var idstr = id_array.join(',');//将数组元素连接起来以构建一个字符串
            ajaxPost('../question/deleteQuestion.do', {'idstr': idstr}, function (data) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    jAlert_Redirect(data.msg, "提示", "exam_question_recycle.jsp");
                } else {
                    jAlert(data.msg, "提示");
                }
            });
        });
    }

    function recoveryQuestion() {
        jConfirm('您确定要恢复么？', '提示', function (r) {
            if (!r) {
                return;
            }
            var id_array = new Array();
            $('input[name="questionList"]:checked').each(function () {
                id_array.push($(this).val());//向数组中添加元素
            });
            var idstr = id_array.join(',');//将数组元素连接起来以构建一个字符串
            ajaxPost('../question/recoveryQuestion.do', {'idstr': idstr}, function (data) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    jAlert_Redirect(data.msg, "提示", "exam_question_recycle.jsp");
                } else {
                    jAlert(data.msg, "提示");
                }
            });
        });
    }
</script>
</body>
</html>
