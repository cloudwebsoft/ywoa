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
<%@ page import="com.cloudwebsoft.framework.base.QObjectDb" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%
    String path = request.getContextPath();
%>
<head>
    <title>题库管理</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script src="../inc/common.js"></script>
        <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
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
    String major = ParamUtil.get(request, "major");
    String selQuestionSql = "select id from oa_exam_database where is_valid = 0";
    if (!major.equals(""))
        selQuestionSql += " and major = " + StrUtil.sqlstr(major);
    String querystr = "";
    String op = ParamUtil.get(request, "op");
    String type = ParamUtil.get(request, "type");
    String prkey = ParamUtil.get(request, "prkey");
    String id = ParamUtil.get(request, "questionNumber");
    String searchType = ParamUtil.get(request, "searchType");
    if (op.equals("search")) {
        prkey = ParamUtil.get(request, "prkey");
        type = ParamUtil.get(request, "type");
        prkey = ParamUtil.get(request, "prkey");
        if (!type.equals(""))
            selQuestionSql += " and exam_type = " + StrUtil.sqlstr(type);
        if (!id.equals(""))
            selQuestionSql += " and id = " + StrUtil.sqlstr(id);
        if (!prkey.equals(""))
            selQuestionSql += " and question like " + StrUtil.sqlstr("%" + prkey + "%");
    }
    querystr += "type=" + StrUtil.UrlEncode(type) + "&prkey=" + StrUtil.UrlEncode(prkey) + "&major=" + StrUtil.UrlEncode(major) + "&op=search";
    int pagesize = 10;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    QuestionDb qd = new QuestionDb();
    ListResult lr = qd.listResult(selQuestionSql, curpage, pagesize);
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
<div id="bodyBox"></div>
<div id="question">
    <form name="form1" action="?op=search&major=<%=major %>" method="post">
        <div id="q_list" style="">
            <table id="tab1" align="center">
                <tr id="2">
                    <td colspan="4" align="center">
                        <select size="1" name="type">
                            <option value="">--题型--</option>
                            <option value="<%=QuestionDb.TYPE_SINGLE%>">单选题</option>
                            <option value="<%=QuestionDb.TYPE_MULTI%>">多选题</option>
                            <option value="<%=QuestionDb.TYPE_JUDGE%>">判断题</option>
                            <option value="<%=QuestionDb.TYPE_ANSWER%>">问答题</option>
                        </select>
                    </td>
                    <td>
                        <select size="1" name="searchType">
                            <option value="content" onclick="changeSearchType()">内容</option>
                            <option value="questionNo" onclick="changeSearchType()">编号</option>
                        </select>
                    </td>
                    <td>
                        <input type="text" id="prkey" name="prkey" size="10" value=" " onfocus="this.select()" style="display: block;"/>
                        <input type="text" id="questionNumber" name="questionNumber" size="10" value=" " style="display: none" onfocus="this.select()"/>
                    </td>
                    <td>
                        <input class="btn" type="submit" value="查 询"/>
                        <input class="btn" id="addQuestion" type="button" value="添加题目" onclick="addit(form1)"/>
                    </td>
                </tr>
            </table>
            <%
                int i = 0;
                while (ir != null && ir.hasNext()) {
                    i++;
                    qd = (QuestionDb) ir.next();
            %>
            <table class="tabStyle_1 percent98" id="tab_<%=qd.getId() %>">
                <tr>
                    <td colspan="2">
                        <img id="plus_<%=qd.getId() %>" src="../forum/images/plus.gif" style="cursor:pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
                        <img id="minus_<%=qd.getId() %>" class="question" src="../forum/images/minus.gif" style="cursor:pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
                        <%=qd.getId()%>、
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
                        &nbsp;&nbsp;<%=qd.getQuestion().replaceAll("</?[^/?(img)][^><]*>", "") %>
                        <span style="float:right">
                    	<a href="#" onClick="editQ('<%=qd.getId()%>')">修改 </a>
                        &nbsp;&nbsp;
                        <a href="#" onclick=" delQ('<%=qd.getId() %>','tab_<%=qd.getId() %>')">删除</a>
                        </span>
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
                    <td width="40" align="center">&nbsp;&nbsp;<%=(char) o %>
                    </td>
                    <td><%=qsd.getString("content").replaceAll("</?[^/?(img)][^><]*>", "") %>
                    </td>
                </tr>
                <%
                        k++;
                    }
                %>
                <tr id="q<%=k+1 %>_<%=qd.getId() %>" class="question">
                    <td align="center">&nbsp;&nbsp;答案</td>
                    <td><%
                        QuestionSelectDb answerOptionDb = new QuestionSelectDb();
                        if (qd.getType() == QuestionDb.TYPE_SINGLE) {
                            answerOptionDb = (QuestionSelectDb) answerOptionDb.getQObjectDb(qd.getAnswer());
                            if (answerOptionDb != null) {
                    %>
                        <%=(char) ((int) 'A' + answerOptionDb.getInt("orders"))%>
                        <%
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
                                        answerShow += "、" + String.valueOf((char) ((int) 'A' + Integer.parseInt(answerOptionDb.getString("orders"))));
                                    }
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
                    <td width="40" align="center">&nbsp;&nbsp;答案</td>
                    <td>
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
    o("type").value = "<%=type%>";
    o("prkey").value = "<%=prkey%>";
    o("major").value = "<%=major%>";
    o("searchType").value = "<%=searchType%>";
    o("questionNumber").value = "<%=id%>";

    function addit(mform) {
        var major = o("major").value;
        var type = o("type").value;
        if (type == "") {
            jAlert("请选择题目类型", "提示");
        } else if (major == "") {
            alert("请选择专业/专项");
        } else {
            addTab("添加题目", "exam/exam_question_add.jsp?type=" + type + "&major=" + major);
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
        var rootMajor = "<%=MajorView.ROOT_CODE%>";
        var major = "<%=major%>";
        if (major != "") {
            if (major == rootMajor) {
                $("#addQuestion").hide();
            } else {
                $("#addQuestion").show();
            }
        } else {
            $("#addQuestion").hide();
        }
        $(".question").hide();
        $("#examMajorTree").hide();
        var searchType = o("searchType").value;
        if (searchType == "content") {
            $("#questionNumber").css('display', 'none');
            $("#prkey").css('display', 'block');
        } else if (searchType == "questionNo") {
            $("#prkey").css('display', 'none');
            $("#questionNumber").css('display', 'block');
        }
    })

    function delQ(questionId, tabId) {
        jConfirm('您确定要删除吗？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                ajaxPost('../question/delQ.do', {'questionId': questionId}, function (data) {
                    data = $.parseJSON(data);
                    if (data.ret == 1) {
                        jAlert(data.msg, "提示");
                        $('#' + tabId).remove();
                    } else {
                        jAlert(data.msg, "提示");
                    }
                });
            }
        });

    }

    function editQ(questionId) {
        ajaxPost('../question/isCanEdit.do', {'questionId': questionId}, function (data) {
            data = $.parseJSON(data);
            if (data.ret == "1") {
                addTab("题目修改", "exam/exam_question_edit.jsp?questionId=" + questionId);
            } else {
                jAlert(data.msg, "提示");
            }
        });
    }

    function showAnswer(id) {
        $("tr[id*=" + id + "]").toggle();
        var plus = 'plus_' + id;
        var minus = 'minus_' + id;
        $('#' + plus).toggle();
        $('#' + minus).toggle();
    }

    function changeSearchType() {
        var searchType = o("searchType").value;
        if (searchType == "content") {
            $("#questionNumber").css('display', 'none');
            $("#prkey").css('display', 'block');
            o("questionNumber").value = "";
        } else if (searchType == "questionNo") {
            $("#prkey").css('display', 'none');
            $("#questionNumber").css('display', 'block');
            o("prkey").value = "";
        }
    }
</script>
</body>
</html>
