<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>通知-编辑</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <style>
        .mybtn {
            background-color: #87c3f1 !important;
            font-weight: bold;
            text-align: center;
            line-height: 35px;
            height: 35px;
            width: 120px;
            padding-right: 8px;
            padding-left: 8px;
            -moz-border-radius: 3px;
            -webkit-border-radius: 3px;
            border-radius: 3px;
            behavior: url(../skin/common/ie-css3.htc);
            cursor: pointer;
            color: #fff;
            border-top-width: 0px;
            border-right-width: 0px;
            border-bottom-width: 0px;
            border-left-width: 0px;
            border-top-style: none;
            border-right-style: none;
            border-bottom-style: none;
            border-left-style: none;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery.form.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="../inc/upload.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script>
        function setUsers(users, userRealNames) {
            o("receiver").value = users;
            o("userRealNames").value = userRealNames;
        }

        function getSelUserNames() {
            return o("receiver").value;
        }

        function getSelUserRealNames() {
            return o("userRealNames").value;
        }

        function openWinUsers() {
            showModalDialog('../user_multi_sel.jsp?unitCode=${myUnitCode}', window.self, 'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
        }

        function openWinUserGroup() {
            openWin("../user_usergroup_multi_sel.jsp", 520, 400);
        }

        function openWinUserRole() {
            openWin("../user_role_multi_sel.jsp", 520, 400);
        }
    </script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%" style="margin-bottom: 10px">
    <tbody>
    <tr>
        <td class="tdStyle_1"><img src="../images/left/icon-notice.gif"><a href="list.do">通知</a></td>
    </tr>
    </tbody>
</table>
<form id="form1" name="form1" action="save.do" method="post" enctype="multipart/form-data">
    <table width="100%" class="tabStyle_1">
        <tr>
            <td class="tabStyle_1_title" colspan="3">修改通知</td>
        </tr>
        <tr>
            <td width="100">标&nbsp;&nbsp;&nbsp;&nbsp;题</td>
            <td colspan="2">
                <input name="title" id="title" size="80" maxlength="25" value="${notice.title}"/>
                <input name="userName" value="${myUserName}" type="hidden"/>
                <input name="id" id="id" type="hidden" value="${notice.id}"/>
                <input name="isall" id="isall" type="hidden" value="${notice.isAll}"/>
                <input name="unitCode" id="unitCode" type="hidden" value="${notice.unitCode}"/>
                <input type="hidden" name="isDeptNotice" value="${isDeptNotice ?"1":"0"}"/>
            </td>
        </tr>
        <tr>
            <td>颜&nbsp;&nbsp;&nbsp;&nbsp;色</td>
            <td colspan="2"><select id="color" name="color">
                <option value="" style="COLOR: black" selected="selected">标题颜色</option>
                <option style="BACKGROUND: #000088" value="#000088">标题颜色</option>
                <option style="BACKGROUND: #0000ff" value="#0000ff">标题颜色</option>
                <option style="BACKGROUND: #008800" value="#008800">标题颜色</option>
                <option style="BACKGROUND: #008888" value="#008888">标题颜色</option>
                <option style="BACKGROUND: #0088ff" value="#0088ff">标题颜色</option>
                <option style="BACKGROUND: #00a010" value="#00a010">标题颜色</option>
                <option style="BACKGROUND: #1100ff" value="#1100ff">标题颜色</option>
                <option style="BACKGROUND: #111111" value="#111111">标题颜色</option>
                <option style="BACKGROUND: #333333" value="#333333">标题颜色</option>
                <option style="BACKGROUND: #50b000" value="#50b000">标题颜色</option>
                <option style="BACKGROUND: #880000" value="#880000">标题颜色</option>
                <option style="BACKGROUND: #8800ff" value="#8800ff">标题颜色</option>
                <option style="BACKGROUND: #888800" value="#888800">标题颜色</option>
                <option style="BACKGROUND: #888888" value="#888888">标题颜色</option>
                <option style="BACKGROUND: #8888ff" value="#8888ff">标题颜色</option>
                <option style="BACKGROUND: #aa00cc" value="#aa00cc">标题颜色</option>
                <option style="BACKGROUND: #aaaa00" value="#aaaa00">标题颜色</option>
                <option style="BACKGROUND: #ccaa00" value="#ccaa00">标题颜色</option>
                <option style="BACKGROUND: #ff0000" value="#ff0000">标题颜色</option>
                <option style="BACKGROUND: #ff0088" value="#ff0088">标题颜色</option>
                <option style="BACKGROUND: #ff00ff" value="#ff00ff">标题颜色</option>
                <option style="BACKGROUND: #ff8800" value="#ff8800">标题颜色</option>
                <option style="BACKGROUND: #ff0005" value="#ff0005">标题颜色</option>
                <option style="BACKGROUND: #ff88ff" value="#ff88ff">标题颜色</option>
                <option style="BACKGROUND: #ee0005" value="#ee0005">标题颜色</option>
                <option style="BACKGROUND: #ee01ff" value="#ee01ff">标题颜色</option>
                <option style="BACKGROUND: #3388aa" value="#3388aa">标题颜色</option>
                <option style="BACKGROUND: #000000" value="#000000">标题颜色</option>
            </select>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="checkbox" id="level" name="level" value="1" ${notice.noticeLevel==1?"checked":""} disabled/>
                <span title="重要通知将在桌面弹窗显示">重要通知</span>
                <!-- <input type="checkbox" id="isBold" name="isBold" value="true" ${notice.isBold eq 1?"checked":""} disabled/>
      标题加粗  -->
                <input type="checkbox" name="is_reply" value="1" id="is_reply" ${notice.isReply eq 1?"checked":""} disabled="disabled" title="是否可以回复"/>
                回复
                <c:if test="${notice.isReply==1 && notice.isForcedResponse==1}">
                <span class="responseDiv">
                <input type="checkbox" name="is_forced_response" id="is_forced_response" value="1"  ${notice.isForcedResponse eq 1?"checked":""} disabled="disabled"/>
                强制回复
                </span>
                </c:if>
                <input type="checkbox" name="isShow" ${notice.isShow?"checked":""} value="1" disabled/>
                显示已查看通知人员
            </td>
        </tr>
        <tr>
            <td colspan="3">
                <div style="clear:both">
                    <textarea id="content" name="content">${notice.content}</textarea>
                </div>
                <script>
                    var uEditor;
                    $(function () {
                        uEditor = UE.getEditor('content', {
                            //allowDivTransToP: false,//阻止转换div 为p
                            toolleipi: true,//是否显示，设计器的 toolbars
                            textarea: 'content',
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
                            initialFrameHeight: 300,
                            disabledTableInTable: false
                            ///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
                            //更多其他参数，请参考ueditor.config.js中的配置项
                        });

                        UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
                        UE.Editor.prototype.getActionUrl = function (action) {
                            if (action == 'uploadimage' || action == 'uploadscrawl') {
                                return '<%=request.getContextPath()%>/ueditor/UploadFile?op=notice';
                            } else if (action == 'uploadvideo') {
                                return '<%=request.getContextPath()%>/ueditor/UploadFile';
                            } else {
                                return this._bkGetActionUrl.call(this, action);
                            }
                        }
                    });
                </script>
            </td>
        </tr>
        <tr>
            <td><span class="TableContent">有效期</span></td>
            <td colspan="2">
                <span class="TableData">开始日期：
                <input type="text" id="beginDate" name="beginDate" size="10" value="${notice.beginDate==null ? "" : notice.beginDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}" disabled/>
                &nbsp;
                结束日期：
                <input type="text" id="endDate" name="endDate" size="10" title='${notice.endDate==null ? "结束日期不填表示永不过期" : ""}' value="${notice.endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}" ${notice.endDate==null ? "" : "disabled"}/>
                </span>
            </td>
        </tr>
        <tr id="trToDept">
            <td>
                <input type="hidden" name="isDeptNotice" value="${notice.isDeptNotice ? "1" : "0"}">
                发布人员
            </td>
            <td width="80%" style="line-height:1.5" colspan=2>
                <textarea name="deptNames" cols="80" rows="5" readOnly wrap="yes" id="deptNames" disabled>${notice.isAll == 0 ? realNames : "全部用户"}</textarea>
                <input type="hidden" name="receiver" id="receiver"/>
            </td>
        </tr>
        <tr>
            <td colspan="3">
                <script>initUpload();</script>
            </td>
        </tr>
        <c:forEach items="${notice.oaNoticeAttList}" var="att">
            <tr>
                <td height="30" colspan="3" align="left" style="line-height:1.5">
                    <div id="boxAtt${att.id}">
                        <img src="../images/attach2.gif" width="17" height="17"/>
                        <a target="_blank" href="getfile.do?noticeId=${notice.id}&attachId=${att.id}">${att.name}</a>
                        &nbsp;&nbsp;
                        <a href="javascript:;" onclick="del('${notice.id}', '${att.id}')">删除</a><!--将notice.id、att.id转成字符串防止JS精度问题-->
                    </div>
                </td>
            </tr>
        </c:forEach>
        <tr>
            <td align="center" colspan="3"><input id="btnSave" type="button" class="mybtn" value="确定" onclick="save()">
                <%--&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" class="mybtn" onclick="window.location.href='list.do'" value="返回">--%>
            </td>
        </tr>
    </table>
</form>
<br/>
<script language="javascript">
    var title;

    $(function () {
        $('#endDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        title = new LiveValidation('title');
        title.add(Validate.Presence);
        o("color").value = "${notice.color}";
        $('#color').css("color", $('#color').val());
        $('#color').change(function () {
            $('#color').css("color", $(this).val());
        });
        $("#level").click(function () {
            if ($("#level").attr("checked")) {
                $("#isBold").attr("checked", "checked");
                $("#color").val("#ff0000");
            } else {
                $("#isBold").removeAttr("checked");
                $("#color").val("");
            }
            $("#color").change();
        });
    })

    function save() {
        if (!LiveValidation.massValidate(title.formObj.fields)) {
            jAlert("标题不能为空！", "提示");
            return;
        }
        if ($('#title').val().trim() == '') {
            jAlert("标题不能为空！", "提示");
            return;
        }
        if (uEditor.getContent().trim() == "") {
            jAlert("正文不能为空！", "提示");
            return;
        }
        $("#form1").submit();
    }

    $(function () {
        var options = {
            // beforeSerialize: onBeforeSerialize,
            // target:        '#output2',   // target element(s) to be updated with server response
            beforeSubmit: preSubmit,  // pre-submit callback
            success: showResponse  // post-submit callback

            // other available options:
            //url:       url         // override for form's 'action' attribute
            //type:      type        // 'get' or 'post', override for form's 'method' attribute
            //dataType:  null        // 'xml', 'script', or 'json' (expected server response type)
            //clearForm: true        // clear all form fields after successful submit
            //resetForm: true        // reset the form after successful submit

            // $.ajax options can be used here too, for example:
            //timeout:   3000
        };

        // bind to the form's submit event
        var lastSubmitTime = new Date().getTime();
        $('#form1').submit(function () {
            // 通过判断时间，禁多次重复提交
            var curSubmitTime = new Date().getTime();
            // 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
            if (curSubmitTime - lastSubmitTime < 500) {
                lastSubmitTime = curSubmitTime;
                $('#form1').hideLoading();
                return false;
            } else {
                lastSubmitTime = curSubmitTime;
            }

            $(this).ajaxSubmit(options);
            return false;
        });
    });

    function preSubmit() {
        $('body').showLoading();
        $('#btnSave').attr("disabled", true);
    }

    function showResponse(responseText, statusText, xhr, $form) {
        $('body').hideLoading();
        $('#btnSave').attr("disabled", false);
        var data = $.parseJSON($.trim(responseText));
        if (data.ret == "0") {
            if (data.msg != null) {
                data.msg = data.msg.replace(/\\r/ig, "<BR>");
            }
            jAlert(data.msg, "提示");
        }
        else {
            jAlert(data.msg, "提示", function() {
                window.location.reload();
            });
        }
    }

    function del(noticeId, attId) {
        jConfirm("您确定要删除么？", "提示", function (r) {
            if (!r) {
                return;
            }
            $.ajax({
                type: "post",
                url: "delAtt.do",
                data: {
                    noticeId: noticeId,
                    attId: attId
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $("body").showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                    if (data.ret == "1") {
                        $('#boxAtt' + attId).remove();
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $("body").hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    }
</script>
</body>
</html>