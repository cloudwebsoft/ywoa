<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@page import="com.cloudwebsoft.framework.base.QObjectDb" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.exam")) {
        out.println(StrUtil.makeErrMsg(privilege.MSG_INVALID, "red", "green"));
        return;
    }
    String qId = ParamUtil.get(request, "questionId");
    int questionId = Integer.parseInt(qId);
    QuestionDb qd = new QuestionDb();
    qd = qd.getQuestionDb(questionId);
    int type = qd.getType();
    String major = qd.getMajor();
%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <title>修改题目</title>
    <script src="nav.js"></script>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<%
    if (type == QuestionDb.TYPE_SINGLE) {
%>
<form name="form1" method="post" action="" onSubmit="return submitit();" enctype="MULTIPART/FORM-DATA">
    <table class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
        <tr>
            <td class="tabStyle_1_title" colspan="2" align="center">修改题目</td>
        </tr>
        <tr>
            <td align="center">类型</td>
            <td><input type="hidden" value="<%=type%>" name="type"/>单选题
            </td>
        </tr>
        <%
            TreeSelectDb tsd = new TreeSelectDb();
            if (!major.equals("other") && major != null) {
                tsd = tsd.getTreeSelectDb(major);
            }
        %>
        <tr>
            <td align="center">专业</td>
            <%if (qd.getSubject().equals("1")) {%>
            <td colspan="2"><input type="hidden" value="other" name="major"/>其它</td>
            <%} else {%>
            <td colspan="2"><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
            </td>
            <%} %>
        </tr>
        <tr>
            <td align="center">题目</td>
            <td><textarea rows="3" id="question" name="question"><%=qd.getQuestion() %></textarea></td>
        </tr>
        <tr>
            <td colspan="3" align="left">选项</td>
        </tr>
        <%
            String selQuestionOptionSql = " select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(qd.getId())) + "order by orders";
            // System.out.println(getClass() + "sql：" + selQuestionOptionSql);
            QuestionSelectDb qsd = new QuestionSelectDb();
            Iterator optionList = qsd.list(selQuestionOptionSql).iterator();
            int i = (int) 'A';
            int j = 0;
            while (optionList.hasNext()) {
                j++;
                qsd = (QuestionSelectDb) optionList.next();
                // System.out.println(getClass() + " 得到的选项：" + qsd.getString("content") );
        %>
        <tr id="<%=j %>" class="select_options">
            <td align="center"><%=(char) i %>.</td>
            <td>
                <div style="clear:both"><textarea id="choose<%=(char)i %>" name="choose<%=(char)i %>"><%=qsd.getString("content") %></textarea></div>
            </td>
            <td>
                <img class="move_btn" onclick="moveP(this,<%=i %>)" move_act="up" style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_up.png" alt=""/><br/>
                <img style="cursor: pointer;" onclick="moveP(this,<%=i %>)" class="move_btn" move_act="down" src="<%=request.getContextPath() %>/images/exam/exam_select_down.png" alt=""/><br/>
                <img style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_del.png" alt="" onclick="delSelect(this)"/></br>
                <img style='cursor: pointer;' src='../images/exam/exam_select_add.png' onclick='addSelect()'/>
            </td>
        </tr>
        <%
                i++;
            }
        %>
        <tr>
            <td align="center" height="20">答案</td>
            <td height="20">
                <%
                    int t = 0;
                    for (int k = (int) 'A'; k < 'A' + j; k++) {%>
                <span class="select_answer"><input type="radio" name="answer" value="<%=t %>"/><%=(char) k %></span>
                <%
                        t++;
                    } %>
            </td>
        </tr>
        <tr>
            <td align="center" height="20" colspan="2">
                <input type="button" value="确定" name="B1" class="s02" onclick="submitForm()"/></td>
        </tr>
    </table>
    <script>
        function submitForm() {
            var questionId = "<%=qId%>";
            var count = $(".select_options").length;
            var answer = $('input[name="answer"]:checked').val();
            if(!answer) {
                jAlert("请选择答案", "提示");
                return;
            }
            var selectOption = {};
            $(".select_options").each(function (i, n) {
                var str = "A";
                var textId = String.fromCharCode(str.charCodeAt() + Number(i));
                selectOption["choose" + textId] = UE.getEditor("choose" + textId).getContent();
            });
            var question = UE.getEditor('question').getContent();
            selectOption['question'] = question;
            selectOption['answer'] = answer;
            selectOption['count'] = count;
            selectOption['questionId'] = questionId;
            selectOption['op'] = "single";
            ajaxPost('../question/editQ.do', selectOption,
                function (data) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                });
        }
    </script>
</form>
<% }
    if (type == QuestionDb.TYPE_MULTI) {
%>
<form name="form1" method="post" action="" onSubmit="return submitit();">
    <table class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
        <tr>
            <td class="tabStyle_1_title" colspan="2" align="center">修改题目</td>
        </tr>
        <tr>
            <td align="center">类型</td>
            <td><input type="hidden" value="<%=type%>" name="type"/>多选题</td>
        </tr>
        <%
            TreeSelectDb tsd = new TreeSelectDb();
            if (!major.equals("other") && major != null) {
                tsd = tsd.getTreeSelectDb(major);
            }
        %>
        <tr>
            <td align="center">专业</td>
            <%if (qd.getSubject().equals("1")) {%>
            <td><input type="hidden" value="other" name="major"/>其它</td>
            <%} else {%>
            <td><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
            </td>
            <%} %>
        </tr>
        <tr>
            <td align="center">题目</td>
            <td><textarea rows="3" id="question" name="question"><%=qd.getQuestion() %></textarea></td>
        </tr>
        <tr>
            <td align="center">选项</td>
        </tr>
        <%
            String selQuestionOptionSql = " select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(qd.getId())) + "order by orders";
            QuestionSelectDb qsd = new QuestionSelectDb();
            Iterator optionList = qsd.list(selQuestionOptionSql).iterator();
            int i = (int) 'A';
            int j = 0;
            while (optionList.hasNext()) {
                j++;
                qsd = (QuestionSelectDb) optionList.next();
        %>
        <tr id="<%=j %>" class="select_options">
            <td align="center"><%=(char) i %>.</td>
            <td>
                <div style="clear:both"><textarea id="choose<%=(char)i %>" name="choose<%=(char)i %>"><%=qsd.getString("content") %></textarea></div>
            </td>
            <td>
                <img class="move_btn" onclick="moveP(this,<%=i %>)" move_act="up" style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_up.png" alt=""/><br/>
                <img style="cursor: pointer;" onclick="moveP(this,<%=i %>)" class="move_btn" move_act="down" src="<%=request.getContextPath() %>/images/exam/exam_select_down.png" alt=""/><br/>
                <img style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_del.png" alt="" onclick="delSelect(this)"/><br/>
                <img style='cursor: pointer;' src='../images/exam/exam_select_add.png' onclick='addSelect()'/>
            </td>

        </tr>
        <%
                i++;
            }
        %>
        <tr>
            <td align="center" height="20">答案</td>
            <td height="20">
                <%
                    int t = 0;
                    for (int k = (int) 'A'; k < 'A' + j; k++) {
                %>
                <span class="select_answer">
                    <input type="checkbox" name="answer" value="<%=t %>" onBlur="this.className='inputnormal'" onFocus="this.className='inputedit';this.select()" class="inputnormal"/><%=(char) k %>
                </span>
                <%
                        t++;
                    }
                %>
            </td>
        </tr>
        <tr>
            <td align="center" height="20" colspan="2">
                <input type="button" value="确定" name="B1" class="s02" onclick="submitForm()"/>
            </td>
        </tr>
    </table>
    <script>
        function submitForm() {
            var questionId = "<%=qId%>";
            var count = $(".select_options").length;
            var obj = document.getElementsByName('answer');
            //取到对象数组后，我们来循环检测它是不是被选中
            var answer = '';
            for (var i = 0; i < obj.length; i++) {
                if (obj[i].checked) {
                    if (answer == "") {
                        answer = obj[i].value;
                    } else {
                        answer += "," + obj[i].value;
                    }
                }
            }
            if(answer=="") {
                jAlert("请选择答案", "提示");
                return;
            }
            var question = UE.getEditor('question').getContent();
            var selectOption = {};
            $(".select_options").each(function (i, n) {
                var str = "A";
                var textId = String.fromCharCode(str.charCodeAt() + Number(i));
                selectOption["choose" + textId] = UE.getEditor("choose" + textId).getContent();
            });
            selectOption['question'] = question;
            selectOption['answer'] = answer;
            selectOption['count'] = count;
            selectOption['questionId'] = questionId;
            selectOption['op'] = "multi";
            ajaxPost('../question/editQ.do', selectOption, function (data) {
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
            });
        }
    </script>
</form>
<% }
    if (type == QuestionDb.TYPE_JUDGE) {
%>
<form name="form1" method="post" action="" onSubmit="return submitit();" enctype="MULTIPART/FORM-DATA">
    <table class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
        <tr>
            <td class="tabStyle_1_title" colspan="2" align="center">修改题目</td>
        </tr>
        <tr>
            <td align="center">类型</td>
            <td><input type="hidden" value="<%=type%>" name="type"/> 判断题</td>
        </tr>
        <%
            TreeSelectDb tsd = new TreeSelectDb();
            if (!major.equals("other") && major != null) {
                tsd = tsd.getTreeSelectDb(major);
            }
        %>
        <tr>
            <td align="center">专业</td>
            <%if (qd.getSubject().equals("1")) {%>
            <td><input type="hidden" value="other" name="major"/>其它</td>
            <%} else {%>
            <td><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
            </td>
            <%} %>
        </tr>
        <tr>
            <td align="center">题目</td>
            <td><textarea rows="3" id="question" name="question"><%=qd.getQuestion() %></textarea></td>
        </tr>
        <tr>
            <td align="center" height="20">答案</td>
            <td height="20">
                <input type="radio" name="answer" value="y"/>正确
                <input type="radio" name="answer" value="n"/>错误
            </td>
        </tr>
        <tr>
            <td align="center" height="20" colspan="2">
                <input type="button" value="确定" name="B1" class="s02" onclick="submitForm()"/>
            </td>
        </tr>
    </table>
    <script>
        function submitForm() {
            var questionId = "<%=qId%>";
            var answer = $('input[name="answer"]:checked').val();
            if(!answer) {
                jAlert("请选择答案", "提示");
                return;
            }
            var question = UE.getEditor('question').getContent();
            ajaxPost('../question/editQ.do', {'questionId': questionId, 'answer': answer, 'question': question, 'op': 'answer'}, function (data) {
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
            });
        }

        $(function () {
            $("input[name='answer'][value='<%=qd.getAnswer()%>']").prop("checked", true);
        })
    </script>
</form>
<%
    }
    if (type == QuestionDb.TYPE_ANSWER) {
%>
<form name="form1" method="post" action="" onSubmit="return submitit();" enctype="MULTIPART/FORM-DATA">
    <table class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
        <tr>
            <td class="tabStyle_1_title" colspan="2" align="center">修改题目</td>
        </tr>
        <tr>
            <td align="center">类型</td>
            <td><input type="hidden" value="<%=type%>" name="type"/> 问答题</td>
        </tr>
        <%
            TreeSelectDb tsd = new TreeSelectDb();
            if (!major.equals("other") && major != null) {
                tsd = tsd.getTreeSelectDb(major);
            }
        %>
        <tr>
            <td align="center">专业</td>
            <%if (qd.getSubject().equals("1")) {%>
            <td><input type="hidden" value="other" name="major"/>其它</td>
            <%} else {%>
            <td><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
            </td>
            <%} %>
        </tr>
        <tr>
            <td align="center">题目</td>
            <td><textarea rows="3" id="question" name="question"><%=qd.getQuestion() %></textarea></td>
        </tr>
        <tr>
            <td align="center" height="20">答案</td>
            <td height="20">
                <textarea rows="3" id="answer" name="answer"><%=qd.getAnswer() %></textarea>
            </td>
        </tr>
        <tr>
            <td align="center" height="20" colspan="2">
                <input type="button" value="确定" name="B1" class="s02" onclick="submitForm()"/>
            </td>
        </tr>
    </table>
    <script>
        function submitForm() {
            var questionId = "<%=qId%>";
            var answer = UE.getEditor('answer').getContent();
            var question = UE.getEditor('question').getContent();
            ajaxPost('../question/editQ.do', {'questionId': questionId, 'answer': answer, 'question': question, 'op': 'answer'}, function (data) {
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
            });
        }
    </script>
</form>
<%}%>
<script>
    // ajax提交通用方法
    function ajaxPost(path, parameter, func) {
        $.ajax({
            type: "post",
            url: path,
            data: parameter,
            dataType: "html",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                func(data);
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    // 初始化选项的ue_edit对象
    function initSelectOptions() {
        var count = $(".select_options").length;
        for (var j = 0; j < count; j++) {
            var str = "A";
            addUe("choose" + String.fromCharCode(str.charCodeAt() + j));
        }
    }

    function addUe(id) {
        uEditor = UE.getEditor(id, {
            //allowDivTransToP: false,//阻止转换div 为p
            toolleipi: true,//是否显示，设计器的 toolbars
            textarea: id,
            enableAutoSave: false,
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', 'anchor', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'insertimage', 'insertvideo', 'emotion', 'map', 'insertframe', 'insertcode', 'pagebreak', 'template', '|',
                'horizontal', 'date', /*'time'*/, 'spechars', '|',
                'inserttable', 'deletetable', 'insertparagraphbeforetable', 'insertrow', 'deleterow', 'insertcol', 'deletecol', 'mergecells', 'mergeright', 'mergedown', 'splittocells', 'splittorows', 'splittocols', '|',
                'print', 'preview', 'searchreplace', 'help'
            ]],
            //focus时自动清空初始化时的内容
            //autoClearinitialContent:true,
            //关闭字数统计
            wordCount: false,
            //关闭elementPath
            elementPathEnabled: false,
            //默认的编辑区域高度
            initialFrameHeight: 200,
            initialFrameWidth: 900,
            disabledTableInTable: false
        });
    }

    UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
    UE.Editor.prototype.getActionUrl = function (action) {
        if (action == 'uploadimage' || action == 'uploadscrawl') {
            return '<%=request.getContextPath()%>/ueditor/ExamUploadFile';
        } else if (action == 'uploadvideo') {
            return '<%=request.getContextPath()%>/ueditor/ExamUploadFile';
        } else {
            return this._bkGetActionUrl.call(this, action);
        }
    }

    function hideImg() {
        var count = $(".select_options").length;
        $(".select_options").each(function (i, n) {
            if (i == 0) {
                $(this).find("img").first().hide();
                $(this).find("img").last().hide();
            } else if (i == count - 1) {
                $(this).find("img").eq(1).hide();
                $(this).find("img").last().show();
            } else {
                $(this).find("img").each(function (i) {
                    $(this).show();
                });
                $(this).find("img").last().hide();
            }
        })
    }

    $(function () {
        addUe('question');
        initSelectOptions();
        <%
            QuestionSelectDb qsd = new QuestionSelectDb();
            if(type == QuestionDb.TYPE_SINGLE){
                String orders = "";
                qsd = (QuestionSelectDb)qsd.getQObjectDb(qd.getAnswer());
                if (qsd!=null) {
                    orders = qsd.getString("orders");
                }
                else {
                    qsd = new QuestionSelectDb();
                }
        %>
        $("input[name='answer'][value='<%=orders%>']").prop("checked", true);
        <%}else if(type == QuestionDb.TYPE_MULTI){
            String [] ids = qd.getAnswer().split(",");
            String orders = "";
            for(int i = 0 ; i < ids.length; i++){
                qsd = (QuestionSelectDb)qsd.getQObjectDb(ids[i]);
                if (qsd!=null) {
                    orders = qsd.getString("orders");
                }
                else {
                    qsd = new QuestionSelectDb();
                }
        %>
        $("input[name='answer'][value='<%=orders%>']").prop("checked", true);
        <%}%>
        <%}else if( type == QuestionDb.TYPE_ANSWER){%>
        addUe('answer');
        <%}
    %>
    });
    $(document).ready(function () {
        hideImg();
    })

    function moveP(obj, orders) {
        var move_act = $(obj).attr("move_act");
        if (move_act == "up") {
            var upSelectText = UE.getEditor("choose" + String.fromCharCode(orders - 1)).getContent();
            var ownSelectText = UE.getEditor("choose" + String.fromCharCode(orders)).getContent();
            UE.getEditor("choose" + String.fromCharCode(orders - 1)).setContent(ownSelectText);
            UE.getEditor("choose" + String.fromCharCode(orders)).setContent(upSelectText);
        } else if (move_act == 'down') {
            var downSelectText = UE.getEditor("choose" + String.fromCharCode(orders + 1)).getContent();
            var ownSelectText = UE.getEditor("choose" + String.fromCharCode(orders)).getContent();
            UE.getEditor("choose" + String.fromCharCode(orders + 1)).setContent(ownSelectText);
            UE.getEditor("choose" + String.fromCharCode(orders)).setContent(downSelectText);
        }
        hideImg();
    };

    function delSelect(param) {
        jConfirm('您确定要删除么？', '提示', function(r) {
            if (!r) {
                return;
            }
            var str = "A";
            $(".select_options").each(function (i, n) {
                UE.getEditor("choose" + String.fromCharCode(str.charCodeAt() + i)).destroy();
            })
            $(param).parent().parent().remove();
            $(".select_options").each(function (i, n) {
                $(this).attr("id", i + 1);
                var texId = "choose" + String.fromCharCode(str.charCodeAt() + i);
                $(this).children("td").eq(0).html(String.fromCharCode(str.charCodeAt() + i) + ".");
                $(this).find("textarea").each(function (i) {
                    $(this).attr('id', texId);
                    $(this).attr('name', texId);
                });
            });
            $("span.select_answer:last").remove();
            initSelectOptions();
            hideImg();
        });
    }

    // 增加选项方法
    function addSelect() {
        var count = $(".select_options").length;
        var addTrC = $(".select_options").length + 1;
        var str = "A";
        var html = "<tr id = '" + addTrC + "' class = 'select_options'><td align='center'>" + String.fromCharCode(str.charCodeAt() + count) + ".</td>'<td><div style='clear:both'><textarea id =choose" + String.fromCharCode(str.charCodeAt() + count) + " name=choose" + String.fromCharCode(str.charCodeAt() + count) + "></textarea></div></td><td><img class='move_btn' onclick = 'moveP(this," + (str.charCodeAt() + count) + ")' move_act='up' style='cursor: pointer;' src='<%=request.getContextPath() %>/images/exam/exam_select_up.png' alt='' /><br /><img onclick = 'moveP(this," + (str.charCodeAt() + count) + ")' move_act='down' style='cursor: pointer;' src='<%=request.getContextPath() %>/images/exam/exam_select_down.png' alt='' /><br/><img src='<%=request.getContextPath() %>/images/exam/exam_select_del.png' alt='' style='cursor: pointer;' onclick='delSelect(this)' /></br><img style='cursor: pointer;' src='../images/exam/exam_select_add.png' onclick='addSelect()'/></td></tr>";
        var answerHtml = "";
        <%
           if(type == QuestionDb.TYPE_SINGLE){%>
        answerHtml = "<span class='select_answer'><input type='radio' name='answer' value='" + count + "' />" + String.fromCharCode(str.charCodeAt() + count) + "</span>";
        <%}else if( type == QuestionDb.TYPE_MULTI){%>
        answerHtml = "<span class='select_answer'><input type='checkbox' name='answer' value='" + count + "' />" + String.fromCharCode(str.charCodeAt() + count) + "</span>";
        <%}%>
        $('#' + count).after(html);
        $("span.select_answer:last").after(answerHtml);
        if (!$("#choose" + String.fromCharCode(str.charCodeAt() + count))) {
            addUe('choose' + String.fromCharCode(str.charCodeAt() + count));
        }
        initSelectOptions();
        hideImg();
    }
</script>
</body>
</html>
