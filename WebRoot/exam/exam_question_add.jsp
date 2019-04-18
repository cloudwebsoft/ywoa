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
<%@ page import="com.redmoon.oa.exam.Config" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
    <title>添加题目</title>
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
<div id="bodyBox">
    <%
        if (!privilege.isUserPrivValid(request, "admin.exam")) {
            out.println(SkinUtil.makeErrMsg(request, privilege.MSG_INVALID));
            return;
        }
        String major = ParamUtil.get(request, "major");
        int type = ParamUtil.getInt(request, "type", -1);
        if (type == QuestionDb.TYPE_SINGLE) {
    %>
    <form name="form1" method="post" action="?type=<%=type %>&major=<%=major %>" onSubmit="return submitit();" enctype="MULTIPART/FORM-DATA">
        <table id="single_tab" class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
            <tr>
                <td class="tabStyle_1_title" colspan="3" align="center">添加题目<input type="hidden" name="subId" value="<%=ParamUtil.get(request,"id") %>"/></td>
            </tr>
            <tr>
                <td align="center">类型</td>
                <td colspan="2"><input type="hidden" value="<%=type%>" name="questionType"/>单选题
                </td>
            </tr>
            <%
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(major);
            %>
            <tr>
                <td align="center">专业</td>
                <td colspan="2"><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
                </td>
            </tr>
            <tr>
                <td align="center">题目</td>
                <td><textarea rows="3" id="question" name="question"></textarea></td>
            </tr>
            <tr>
                <td align="center">选项</td>
            </tr>
            <%
                Config cfg = Config.getInstance();
                String singleCount = cfg.getProperty("single_select_count");
                int j = 0;
                for (int i = (int) 'A'; i < 'A' + Integer.parseInt(singleCount); i++) {
                    j++;
            %>
            <tr id="<%=j %>" class="select_options">
                <td align="center"><%=(char) i %>.</td>
                <td>
                    <div style="clear:both"><textarea id="choose<%=(char)i %>" name="choose"></textarea></div>
                </td>
                <td>
                    <img class="move_btn" onclick="moveP(this,<%=i %>)" move_act="up" style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_up.png" alt=""/><br/>
                    <img onclick="moveP(this,<%=i %>)" style="cursor: pointer;" class="move_btn" move_act="down" src="<%=request.getContextPath() %>/images/exam/exam_select_down.png" alt=""/><br/>
                    <img style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_del.png" alt="" onclick="delSelect(this)"/></br>
                    <img style='cursor: pointer;' src='../images/exam/exam_select_add.png' onclick='addSelect()'/>
                </td>
            </tr>
            <%

                }
            %>
            <tr>
                <td align="center" height="20">答案</td>
                <td height="20">
                    <%
                        int t = 0;
                        for (int k = (int) 'A'; k < 'A' + Integer.parseInt(singleCount); k++) {%>
                    <span class="select_answer"><input type="radio" name="answer" value="<%=t %>"/><%=(char) k %></span>
                    <%
                            t++;
                        }%>
                </td>
            </tr>
            <tr>
                <td align="center" height="20" colspan="2">
                    <input type="button" value="确定" name="B1" onclick="questionAdd()" class="s02"/>
                </td>
            </tr>
        </table>
        <script>
            function questionAdd() {
                var count = $(".select_options").length;
                var type = o("questionType").value;
                var answer = $('input[name="answer"]:checked').val();
                var major = o("major").value;
                var selectOption = {};
                var title = UE.getEditor('question').getContent();
                if (title == "") {
                    jAlert('题目不能为空', '提示');
                    return;
                }
                var answer2 = -1;
                for (i = 0; i < form1.answer.length; i++) {
                    if (form1.answer[i].checked) {
                        answer2 = i;
                    }
                }
                if (answer2 == -1) {
                    jAlert('答案不能为空', '提示');
                    return;
                }
                $(".select_options").each(function (i, n) {
                    var str = "A";
                    var textId = String.fromCharCode(str.charCodeAt() + Number(i));
                    selectOption["choose" + textId] = UE.getEditor("choose" + textId).getContent();
                })
                selectOption['title'] = title;
                selectOption['major'] = major;
                selectOption['answer'] = answer;
                selectOption['count'] = count;
                selectOption['type'] = type;
                ajaxPost('../question/addQ.do', selectOption,
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert_Redirect(data.msg, '提示', "exam_question_add.jsp?type=" + type + "&major=" + major);
                        } else {
                            jAlert(data.msg, '提示');
                        }
                    });
            }
        </script>
    </form>
    <% }
        if (type == QuestionDb.TYPE_MULTI) {
    %>
    <form name="form1" method="post" action="?type=<%=type %>&major=<%=major %>" enctype="MULTIPART/FORM-DATA">
        <table class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
            <tr>
                <td class="tabStyle_1_title" colspan="3" align="center">添加题目</td>
            </tr>
            <tr>
                <td align="center">类型</td>
                <td colspan="2"><input type="hidden" value="<%=type%>" name="questionType"/> 多选题</td>
            </tr>
            <%
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(major);
            %>
            <tr>
                <td align="center">专业</td>
                <td colspan="2"><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
                </td>
            </tr>
            <tr>
                <td align="center">题目</td>
                <td><textarea rows="3" id="question" name="question"></textarea></td>
            </tr>
            <tr>
                <td colspan="3" align="center">选项</td>
            </tr>

            <%
                Config cfg = Config.getInstance();
                String singleCount = cfg.getProperty("multi_select_count");
                int j = 0;
                for (int i = (int) 'A'; i < 'A' + Integer.parseInt(singleCount); i++) {
                    j++;
            %>
            <tr id="<%=j %>" class="select_options">
                <td align="center"><%=(char) i %>.</td>
                <td>
                    <div style="clear:both"><textarea id="choose<%=(char)i %>" name="choose"></textarea></div>
                </td>
                <td>
                    <img class="move_btn" onclick="moveP(this,<%=i %>)" move_act="up" style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_up.png" alt=""/><br/>
                    <img style="cursor: pointer;" onclick="moveP(this,<%=i %>)" class="move_btn" move_act="down" src="<%=request.getContextPath() %>/images/exam/exam_select_down.png" alt=""/><br/>
                    <img style="cursor: pointer;" src="<%=request.getContextPath() %>/images/exam/exam_select_del.png" alt="" onclick="delSelect(this)"/></br>
                    <img style='cursor: pointer;' src='../images/exam/exam_select_add.png' onclick='addSelect()'/>
                </td>
            </tr>
            <%
                }
            %>
            <tr>
                <td align="center" height="20">答案</td>

                <td height="20">
                    <%
                        int t = 0;
                        for (int k = (int) 'A'; k < 'A' + Integer.parseInt(singleCount); k++) {%>
                    <span class="select_answer"><input type="checkbox" name="answer" value="<%=t %>"/><%=(char) k %></span>
                    <%
                            t++;
                        }%>
                    <!--  <input type="checkbox" name="answer" value="A" onBlur="this.className='inputnormal'" onFocus="this.className='inputedit';this.select()" class="inputnormal"/>A
                   <input type="checkbox" name="answer" value="B" onBlur="this.className='inputnormal'" onFocus="this.className='inputedit';this.select()" class="inputnormal"/>B
                   <input type="checkbox" name="answer" value="C" onBlur="this.className='inputnormal'" onFocus="this.className='inputedit';this.select()" class="inputnormal"/>C
                   <input type="checkbox" name="answer" value="D" onBlur="this.className='inputnormal'" onFocus="this.className='inputedit';this.select()" class="inputnormal"/>D
                   <input type="checkbox" name="answer" value="E" onBlur="this.className='inputnormal'" onFocus="this.className='inputedit';this.select()" class="inputnormal"/>E
                   -->
                </td>
            </tr>
            <tr>
                <td align="center" height="20" colspan="2">
                    <input type="button" value="确定" name="B1" onclick="questionAdd()" class="s02"/>
                </td>
            </tr>
        </table>
        <script>
            function questionAdd() {
                var count = $(".select_options").length;
                var type = o("questionType").value;
                //选择所有name="'answer'"的对象，返回数组
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
                var major = o("major").value;
                var ue = UE.getEditor('question');
                var title = ue.getContent();
                if (title == "") {
                    jAlert('题目不能为空', '提示');
                    return;
                }
                var answer2 = -1;
                for (i = 0; i < form1.answer.length; i++) {
                    if (form1.answer[i].checked) {
                        answer2 = i;
                    }
                }
                if (answer2 == -1) {
                    jAlert('答案不能为空', '提示');
                    return;
                }
                var selectOption = {};
                $(".select_options").each(function (i, n) {
                    var str = "A";
                    var textId = String.fromCharCode(str.charCodeAt() + Number(i));
                    selectOption["choose" + textId] = UE.getEditor("choose" + textId).getContent();
                })
                selectOption['title'] = title;
                selectOption['major'] = major;
                selectOption['answer'] = answer;
                selectOption['count'] = count;
                selectOption['type'] = type;
                ajaxPost('../question/addQ.do', selectOption,
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert_Redirect(data.msg, '提示', "exam_question_add.jsp?type=" + type + "&major=" + major);
                        } else {
                            jAlert(data.msg, '提示');
                        }
                    });
            }
        </script>
    </form>
    <% }
        if (type == QuestionDb.TYPE_JUDGE) {
    %>
    <form name="form1" method="post" action="?type=<%=type %>&major=<%=major %>" enctype="MULTIPART/FORM-DATA">
        <table class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
            <tr>
                <td class="tabStyle_1_title" colspan="2" align="center">添加题目</td>
            </tr>
            <tr>
                <td align="center">类型</td>
                <td><input type="hidden" value="<%=type%>" name="questionType"/> 判断题</td>
            </tr>
            <%
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(major);
            %>
            <tr>
                <td align="center">专业</td>
                <td><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
                </td>
            </tr>
            <tr>
                <td align="center">题目</td>
                <td><textarea rows="3" id="question" name="question"></textarea></td>
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
                    <input type="button" value="确定" name="B1" onclick="questionAdd()" class="s02"/>
                </td>
            </tr>
        </table>
        <script>
            var uEditor;
            $(function () {
                uEditor = UE.getEditor('question', {
                    //allowDivTransToP: false,//阻止转换div 为p
                    toolleipi: true,//是否显示，设计器的 toolbars
                    textarea: 'question',
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
                        'searchreplace', 'help'
                    ]],
                    //focus时自动清空初始化时的内容
                    //autoClearinitialContent:true,
                    //关闭字数统计
                    wordCount: false,
                    //关闭elementPath
                    elementPathEnabled: false,
                    //默认的编辑区域高度
                    initialFrameHeight: 200,
                    initialFrameWidth: 1000,
                    disabledTableInTable: false
                });
                UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
                UE.Editor.prototype.getActionUrl = function (action) {
                    if (action == 'uploadimage' || action == 'uploadscrawl') {
                        return '<%=request.getContextPath()%>/ueditor/UploadFileExam';
                    } else if (action == 'uploadvideo') {
                        return '<%=request.getContextPath()%>/ueditor/UploadFileExam';
                    } else {
                        return this._bkGetActionUrl.call(this, action);
                    }
                }
            });

            function questionAdd() {
                var type = o("questionType").value;
                var answer = o("answer").value;
                var major = o("major").value;
                var ue = UE.getEditor('question');
                var answer = $('input[name="answer"]:checked').val();
                var title = ue.getContent();
                if (title == "") {
                    jAlert('题目不能为空', '提示');
                    return;
                }
                var answer2 = -1;
                for (i = 0; i < form1.answer.length; i++) {
                    if (form1.answer[i].checked) {
                        answer2 = i;
                    }
                }
                if (answer2 == -1) {
                    jAlert('答案不能为空', '提示');
                    return;
                }
                ajaxPost('../question/addQ.do', {
                        'type': type,
                        'title': title,
                        'major': major,
                        'answer': answer,
                    },
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert_Redirect(data.msg, '提示', "exam_question_add.jsp?type=" + type + "&major=" + major);
                        } else {
                            jAlert(data.msg, '提示');
                        }
                    });
            }
        </script>
    </form>
    <%
        }
        if (type == QuestionDb.TYPE_ANSWER) {
    %>
    <form name="form1" method="post" action="?type=<%=type %>&major=<%=major %>" enctype="MULTIPART/FORM-DATA">
        <table class="tabStyle_1 percent98" cellpadding="2" cellspacing="1" width="100%">
            <tr>
                <td class="tabStyle_1_title" colspan="2" align="center">添加题目</td>
            </tr>
            <tr>
                <td align="center">类型</td>
                <td><input type="hidden" value="<%=type%>" name="questionType"/> 问答题</td>
            </tr>
            <%
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(major);
            %>
            <tr>
                <td align="center">专业</td>
                <td><input type="hidden" value="<%=major%>" name="major"/><%=tsd.getName() %>
                </td>
            </tr>
            <tr>
                <td align="center">题目</td>
                <td><textarea rows="3" id="question" name="question"></textarea></td>
            </tr>
            <tr>
                <td align="center" height="20">答案</td>
                <td height="20">
                    <textarea rows="3" id="answer" name="answer"></textarea></td>
            </tr>
            <tr>
                <td align="center" height="20" colspan="2">
                    <input type="button" value="确定" name="B1" onclick="questionAdd()" class="s02"/>
                </td>
            </tr>
        </table>
        <script>
            $(function () {
                addUe('answer');
            })

            function questionAdd() {
                var type = o("questionType").value;
                var major = o("major").value;
                var answer = UE.getEditor('answer').getContent();
                var title = UE.getEditor('question').getContent();
                if (title == "") {
                    jAlert('题目不能为空', '提示');
                    return;
                }

                if (answer == "") {
                    jAlert('答案不能为空', '提示');
                    return;
                }
                ajaxPost('../question/addQ.do', {
                        'type': type,
                        'title': title,
                        'major': major,
                        'answer': answer,
                        'count': 0
                    },
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert_Redirect(data.msg, '提示', "exam_question_add.jsp?type=" + type + "&major=" + major);
                        } else {
                            jAlert(data.msg, '提示');
                        }
                    });
            }
        </script>
    </form>
    <%
        }
    %>
    <script>
        //ajax提交通用方法
        function ajaxPost(path, parameter, func) {
            $.ajax({
                type: "post",
                url: path,
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

        function moveP(obj, orders) {
            var move_act = $(obj).attr("move_act");
            if (move_act == "up") {
                //var upSelectText = $(param).parent().parent().prev().before().children("td").find("textarea").text();
                //var ownSelectText = $(param).parent().parent().children("td").find("textarea").text();
                //var upTextareId =$(param).parent().parent().prev().before().children("td").eq(0).text().substr(0,1) ;
                //var ownTextareId = $(param).parent().parent().children("td").eq(0).text().substr(0,1);
                var upSelectText = UE.getEditor("choose" + String.fromCharCode(orders - 1)).getContent();
                var ownSelectText = UE.getEditor("choose" + String.fromCharCode(orders)).getContent();
                UE.getEditor("choose" + String.fromCharCode(orders - 1)).setContent(ownSelectText);
                UE.getEditor("choose" + String.fromCharCode(orders)).setContent(upSelectText);

            } else if (move_act == 'down') {
                //var downSelectText = $(param).parent().parent().next().after().children("td").find("textarea").text();
                //var ownSelectText = $(param).parent().parent().children("td").find("textarea").text();
                //var downTextareId =$(param).parent().parent().next().after().children("td").eq(0).text().substr(0,1) ;
                //var ownTextareId = $(param).parent().parent().children("td").eq(0).text().substr(0,1);
                //UE.getEditor("choose" + downTextareId).setContent(ownSelectText);
                //UE.getEditor("choose" + ownTextareId).setContent(downSelectText);
                var downSelectText = UE.getEditor("choose" + String.fromCharCode(orders + 1)).getContent();
                var ownSelectText = UE.getEditor("choose" + String.fromCharCode(orders)).getContent();
                UE.getEditor("choose" + String.fromCharCode(orders + 1)).setContent(ownSelectText);
                UE.getEditor("choose" + String.fromCharCode(orders)).setContent(downSelectText);
            }
            hideImg();
        };

        function delSelect(param) {
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

        function initSelectOptions() {
            var count = $(".select_options").length;
            console.log("数量" + count);
            for (var j = 0; j < count; j++) {
                var str = "A";
                addUe("choose" + String.fromCharCode(str.charCodeAt() + j));
            }
        }

        $(function () {
            addUe('question');
            initSelectOptions();
        });

        $(document).ready(function () {
            hideImg();
        })

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
    </script>
</div>
</body>
</html>
