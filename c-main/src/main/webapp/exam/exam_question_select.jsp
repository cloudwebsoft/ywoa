<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%
    String type = ParamUtil.get(request, "type");
%>
<head>
    <title>手工组卷-题目选择</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery.cookie.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../js/pagination/jquery.pagination.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/pagination/pagination.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<div id="question">
    <input type="hidden" id="questionIds" name="questionIds" value="" />
    <div style="text-align: center;margin-top:15px;font-size: 20px;margin-bottom: 10px;">
        <%
            if (type.equals(QuestionDb.TYPE_SINGLE)) {%>
        <b style="color: #666666">单选题题目选择</b>
        <%} else if (type.equals(QuestionDb.TYPE_MULTI)) {%>
        <b style="color: #666666">多选题题目选择</b>
        <%} else if (type.equals(QuestionDb.TYPE_JUDGE)) {%>
        <b style="color: #666666">判断题题目选择</b>
        <%} else if (type.equals(QuestionDb.TYPE_ANSWER)) {%>
        <b style="color: #666666">问答题题目选择</b>
        <%
            }
            String major = ParamUtil.get(request, "major");
            String selQuestionSql = "select id from oa_exam_database where is_valid = 0 and major = " + StrUtil.sqlstr(major) + " and exam_type=" + StrUtil.sqlstr(type);
            String querystr = "";
            String op = ParamUtil.get(request, "op");
            String cond = "";
            String id = ParamUtil.get(request, "questionNumber");
            String searchType = ParamUtil.get(request, "searchType");
            String prkey = ParamUtil.get(request, "prkey");
            if (op.equals("search")) {
                prkey = ParamUtil.get(request, "prkey");
                type = ParamUtil.get(request, "type");
                prkey = ParamUtil.get(request, "prkey");
                if (!id.equals(""))
                    cond = " and id = " + StrUtil.sqlstr(id);
                if (!prkey.equals(""))
                    cond = " and question like " + StrUtil.sqlstr("%" + prkey + "%");
                if (!major.equals(""))
                    cond = " and major = " + StrUtil.sqlstr(major);
                selQuestionSql += cond;
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
    </div>
    <form name="form1" action="?type=<%=type %>&op=search" method="post">
        <table id="tab1" align="center">
            <tr>
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
                </td>
            </tr>
            <tr>
                <td align="left"></td>
            </tr>
        </table>
        <table>
            <tr>
                <td>&nbsp;&nbsp;<input type="button" id="selectAll" name="selectAll" value="全选" onclick="swapCheck()"/></td>
            </tr>
        </table>
        <%
            int i = 0;
            while (ir != null && ir.hasNext()) {
                i++;
                qd = (QuestionDb) ir.next();
                if (qd.getType() == QuestionDb.TYPE_SINGLE || qd.getType() == QuestionDb.TYPE_MULTI) {
        %>
        <table class="tabStyle_1 percent98" id="tab_<%=qd.getId() %>">
            <tr>
                <td width="40" align="left"><input type="checkbox" name="quesyionSelect" value="<%=qd.getId() %>" onclick="updateCheckedIds('<%=qd.getId() %>',this)"/></td>
                <td>
                    <img id="plus_<%=qd.getId() %>" src="../forum/images/plus.gif" style="display:none;cursor:pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
                    <img id="minus_<%=qd.getId() %>" class="question" src="../forum/images/minus.gif" style="cursor:pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
				<%=qd.getId()%>、<%=StrUtil.getAbstract(request, qd.getQuestion(), 1000).replaceAll("</?[^/?(img)][^><]*>", "") %>
                </td>
            </tr>
            <%
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
                <td>&nbsp;&nbsp;答案</td>
                <td colspan="3">
                    <%
                        if (qd.getType() == QuestionDb.TYPE_SINGLE) {
                            int orders = 0;
                            qsd = (QuestionSelectDb) qsd.getQObjectDb(qd.getAnswer());
                            if (qsd != null) {
                                orders = qsd.getInt("orders");
                            }
                            else {
                                qsd = new QuestionSelectDb();
                            }
                    %>
                        <%=(char) ((int) 'A' + orders)%>
                    <%
                    } else if (qd.getType() == QuestionDb.TYPE_MULTI) {
                        String [] ids = qd.getAnswer().split(",");
                        int orders = 0;
                        String answerShow = "";
                        for(int j = 0 ; j < ids.length; j++) {
                            qsd = (QuestionSelectDb) qsd.getQObjectDb(ids[j]);
                            if (qsd != null) {
                                orders = qsd.getInt("orders");
                                if ("".equals(answerShow)) {
                                    answerShow = String.valueOf((char) ('A' + orders));
                                } else {
                                    answerShow += "," + String.valueOf((char) ('A' + orders));
                                }
                            }
                            else {
                                qsd = new QuestionSelectDb();
                            }
                        }
                    %>
                    <%=answerShow%>
                    <%}%>
                </td>
            </tr>
        </table>
        <%} else if (qd.getType() == QuestionDb.TYPE_JUDGE || qd.getType() == QuestionDb.TYPE_ANSWER) {%>
        <table class="tabStyle_1 percent98" id="tab_<%=qd.getId() %>">
            <tr>
                <td align="left" width="40"><input type="checkbox" name="quesyionSelect" value="<%=qd.getId() %>" onclick="updateCheckedIds('<%=qd.getId() %>',this)"/></td>
                <td>
                    <img id="plus_<%=qd.getId() %>" src="../forum/images/plus.gif" style="cursor:pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>
                    <img id="minus_<%=qd.getId() %>" class="question" src="../forum/images/minus.gif" style="cursor:pointer; width: 12px;height: 12px;" onclick="showAnswer('<%=qd.getId() %>');"/>				
				<%=qd.getId()%>、
				<%=StrUtil.getAbstract(request, qd.getQuestion(), 1000).replaceAll("</?[^/?(img)][^><]*>", "") %>
                </td>
            </tr>
            <tr id="q_<%=qd.getId() %>" class="question">
                <td>&nbsp;&nbsp;答案</td>
                <td colspan="3"><%=qd.getAnswer().replaceAll("</?[^/?(img)][^><]*>", "") %>
                </td>
            </tr>
        </table>
        <%
                }
            }
        %>
        <table class="percent98" width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td align="right">
                    <%
                        out.print(paginator.getCurPageBlock("?" + querystr));
                    %>
                </td>
            </tr>
        </table>
        <div style="text-align: center;">
            <input type="button" value="确 定" onclick="getQuestion()"/>&nbsp;&nbsp;&nbsp;
            <input type="hidden" name="singleIds"/>
            <input type="hidden" name="multiIds"/>
            <input type="hidden" name="judgeIds"/>
            <input type="hidden" name="major"/>
        </div>
    </form>
</div>

<script>
    var arr = new Array();//用于翻页保留选中项
    var arr1 = new Array();//单选题数组
    var arr2 = new Array();//多选题数组
    var arr3 = new Array();//判断题数组
    var arr4 = new Array();//问答题数组
    var all = window.parent.opener.document.getElementById("questionIds").value;
    var single = window.parent.opener.document.getElementById("sIds").value;
    var multi = window.parent.opener.document.getElementById("mIds").value;
    var judge = window.parent.opener.document.getElementById("jIds").value;
    var answer = window.parent.opener.document.getElementById("aIds").value;
    arr = all.split(",");
    arr1 = single.split(",");
    arr2 = multi.split(",");
    arr3 = judge.split(",");
    arr4 = answer.split(",");
    o("major").value = "<%=major%>";
    o("prkey").value = "<%=prkey%>";
    o("searchType").value = "<%=searchType%>";
    o("questionNumber").value = "<%=id%>";

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

    //选择题目
    function getQuestion() {
        var type = "<%=type%>";
        arr = arrunique(arr);
        arr1 = arrunique(arr1);
        arr2 = arrunique(arr2);
        arr3 = arrunique(arr3);
        arr4 = arrunique(arr4);
        var singleTotle = Number(window.parent.opener.document.getElementById("single_total_score").value);
        var multiTotle = Number(window.parent.opener.document.getElementById("multi_total_score").value);
        var judgeTotle = Number(window.parent.opener.document.getElementById("judge_total_score").value);
        var answerTotle = Number(window.parent.opener.document.getElementById("answer_total_score").value);
        window.parent.opener.document.getElementById("questionIds").value = arr.join();
        if (type == "0") {
            window.parent.opener.document.getElementById("singleIds").value = arr1.join();
            window.parent.opener.document.getElementById("single_count").innerText = arr1.length;
            window.parent.opener.document.getElementById("single_per").innerText = singleTotle / arr1.length;
            window.parent.opener.document.getElementById("questionIds").value = arr.join();
        } else if (type == "1") {
            window.parent.opener.document.getElementById("multiIds").value = arr2.join();
            window.parent.opener.document.getElementById("multi_count").innerText = arr2.length;
            window.parent.opener.document.getElementById("multi_per").innerText = multiTotle / arr2.length;
            window.parent.opener.document.getElementById("questionIds").value = arr.join();
        } else if (type == "2") {
            window.parent.opener.document.getElementById("judgeIds").value = arr3.join();
            window.parent.opener.document.getElementById("judge_count").innerText = arr3.length;
            window.parent.opener.document.getElementById("judge_per").innerText = judgeTotle / arr3.length;
            window.parent.opener.document.getElementById("questionIds").value = arr.join();
        } else if (type == "3") {
            window.parent.opener.document.getElementById("answerIds").value = arr4.join();
            window.parent.opener.document.getElementById("answer_count").innerText = arr4.length;
            window.parent.opener.document.getElementById("answer_per").innerText = answerTotle / arr4.length;
            window.parent.opener.document.getElementById("questionIds").value = arr.join();
        }
        window.parent.close();
    }

    //单选取消更新id方法
    function cancleSel(arr, qId) {
        for (var i = 0; i < arr.length; i++) {
            if (qId == arr[i]) {
                arr.splice(i, 1);
            }
        }
        return arr;
    }

    // 更新已选择的题目id
    function updateCheckedIds(questionId, box) {
        var type = "<%=type%>";
        //获取隐藏域中保存的questionIds
        var questionIds = o("questionIds").value;
        var qId = String(questionId);
        if (o("major").value == "<%=MajorView.ROOT_CODE%>" || o("major").value == "") {
            jAlert("请先选择专业", "提示");
            return;
        }
        if (box.checked) {//勾选  逗号分隔追加
            if (type == "0") {
                arr1.push(questionId);
                window.parent.opener.document.getElementById("sIds").value = arr1.join();
            } else if (type == "1") {
                arr2.push(questionId);
                window.parent.opener.document.getElementById("mIds").value = arr2.join();
            } else if (type == "2") {
                arr3.push(questionId);
                window.parent.opener.document.getElementById("jIds").value = arr3.join();
            } else if (type == "3") {
                arr4.push(questionId);
                window.parent.opener.document.getElementById("aIds").value = arr4.join();
            }
            arr.push(questionId);
        } else {//如果是取消选中则删掉数组中题目id
            if (type == "0") {
                arr1 = cancleSel(arr1, qId);
                window.parent.opener.document.getElementById("sIds").value = arr1.join();
            } else if (type == "1") {
                arr2 = cancleSel(arr2, qId);
                window.parent.opener.document.getElementById("mIds").value = arr2.join();
            } else if (type == "2") {
                arr3 = cancleSel(arr3, qId);
                window.parent.opener.document.getElementById("jIds").value = arr3.join();
            } else if (type == "3") {
                arr4 = cancleSel(arr4, qId);
                window.parent.opener.document.getElementById("aIds").value = arr4.join();
            }
            arr = cancleSel(arr, qId);
        }
        o("questionIds").value = arr.join();//最新的所选值保存到隐藏域中
        window.parent.opener.document.getElementById("questionIds").value = arr.join();
    }

    //checkbox 全选/取消全选
    function arrcontains(arr, needle) {
        for (i in arr) {
            if (arr[i] == needle) return true;
        }
        return false;
    }

    // 数组去重
    function arrunique(arr) {
        var newArr = new Array();
        for (var key in arr) {
            if (!arrcontains(newArr, arr[key])) {
                if (arr[key] != "") {
                    newArr.push(arr[key]);
                }
            }
        }
        return newArr;
    }

    //全选与全不选更新id
    var isCheckAll = false;

    function swapCheck() {
        if (o("major").value == "<%=MajorView.ROOT_CODE%>" || o("major").value == "") {
            jAlert("请先选择专业", "提示");
            return;
        }
        var type = "<%=type%>";
        var a = o("selectAll").value;
        if (a == "全选") {
            isCheckAll = false;
            o("selectAll").value = "取消全选";
        } else if (a == "取消全选") {
            isCheckAll = true;
            o("selectAll").value = "全选";
        }
        var obj = document.getElementsByName('quesyionSelect');
        //取到对象数组后，我们来循环检测它是不是被选中
        var questionIds = o("questionIds").value;
        if (isCheckAll) {
            $("input[name='quesyionSelect']").each(function () {
                this.checked = false;
            });
            //遍历题目列表复选框
            for (var i = 0; i < obj.length; i++) {
                //从题目id字符串中删除取消选中的题目id
                if (type == "0") {
                    for (var j = 0; j < arr1.length; j++) {
                        if (obj[i].value == arr1[j]) {
                            arr1.splice(j, 1);
                        }
                    }
                } else if (type == "1") {
                    for (var j = 0; j < arr2.length; j++) {
                        if (obj[i].value == arr2[j]) {
                            arr2.splice(j, 1);
                        }
                    }
                } else if (type == "2") {
                    for (var j = 0; j < arr3.length; j++) {
                        if (obj[i].value == arr3[j]) {
                            arr3.splice(j, 1);
                        }
                    }
                } else if (type == "3") {
                    for (var j = 0; j < arr4.length; j++) {
                        if (obj[i].value == arr4[j]) {
                            arr4.splice(j, 1);
                        }
                    }
                }
                for (var j = 0; j < arr.length; j++) {
                    if (obj[i].value == arr[j]) {
                        arr.splice(j, 1);
                    }
                }
            }
            window.parent.opener.document.getElementById("questionIds").value = arr.join();
            o("questionIds").value = questionIds;//最新的所选值保存到隐藏域中
            isCheckAll = false;
        } else {  //全选
            $("input[name='quesyionSelect']").each(function () {
                this.checked = true;
            });

            //遍历题目列表的复选框对象
            for (var i = 0; i < obj.length; i++) {
                //如果该复选框选中，数组arr中新增一条记录
                if (obj[i].checked) {
                    if (type == "0") {
                        arr1.push(obj[i].value);
                    } else if (type == "1") {
                        arr2.push(obj[i].value);
                    } else if (type == "2") {
                        arr3.push(obj[i].value);
                    } else if (type == "3") {
                        arr4.push(obj[i].value);
                    }
                    arr.push(obj[i].value);
                }
            }
            //数组arr去重
            arr = arrunique(arr);
            arr1 = arrunique(arr1);
            arr2 = arrunique(arr2);
            arr3 = arrunique(arr3);
            arr4 = arrunique(arr4);
            //把用逗号分隔的题目id字符串保存到隐藏域
            o("questionIds").value = arr.join();
            window.parent.opener.document.getElementById("questionIds").value = arr.join();
            isCheckAll = true;
        }
    }

    //数组赋值
    function getQuestionIds(ids, arr) {
        var str = ids.split(",");
        for (var key in str) {
            if (!str[key] == "") {
                arr.push(str[key]);
            }
        }
        return arr;
    }

    $(function () {
        var searchType = o("searchType").value;
        if (searchType == "content") {
            $("#questionNumber").css('display', 'none');
            $("#prkey").css('display', 'block');
        } else if (searchType == "questionNo") {
            $("#prkey").css('display', 'none');
            $("#questionNumber").css('display', 'block');
        }

        $(".question").hide();
        var a = window.parent.opener.document.getElementById("questionIds").value;
        var b = window.parent.opener.document.getElementById("singleIds").value;
        var c = window.parent.opener.document.getElementById("multiIds").value;
        var d = window.parent.opener.document.getElementById("judgeIds").value;
        var e = window.parent.opener.document.getElementById("answerIds").value;
        arr = getQuestionIds(a, arr);
        for (var key in arr) {
            $("input[name='quesyionSelect'][value='" + arr[key] + "']").prop("checked", true);
        }
        arr1 = getQuestionIds(b, arr1);
        arr2 = getQuestionIds(c, arr2);
        arr3 = getQuestionIds(d, arr3);
        arr4 = getQuestionIds(e, arr4);

    })

    function showAnswer(id) {
        $("tr[id*=" + id + "]").toggle();
        var plus = 'plus_' + id;
        var minus = 'minus_' + id;
        $('#' + plus).toggle();
        $('#' + minus).toggle();
    }

    // 改变搜索的内容
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
