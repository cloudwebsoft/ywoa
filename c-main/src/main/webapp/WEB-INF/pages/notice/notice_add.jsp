<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>通知-添加</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <style>
        .notice_add_toolbar {
            width: 100%;
            height: 30px;
            background-color: #daeaf8;
            border-bottom: 2px solid #92b4d2;
            padding-left: 10px;
            margin-bottom: 10px;
        }

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
            behavior: url(../../../skin/common/ie-css3.htc);
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
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="../inc/upload.js"></script>
    <script src="../js/jquery.form.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function setUsers(users, userRealNames) {
            o("receiver").value = users;
            o("isall").value = '0';
            o("deptNames").value = userRealNames;
        }

        function openWinUsers() {
            openWin("../user_multi_sel.jsp?unitCode=${myUnitCode}&isIncludeChildren=true", 800, 480);
        }
    </script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%" style="margin-bottom: 10px">
    <tbody>
    <tr>
        <td class="tdStyle_1">
            <a href="list.do"><img src="../images/left/icon-notice.gif"/>
                &nbsp;
                <c:choose>
                    <c:when test="${isNoticeMgr}">
                        部门
                    </c:when>
                    <c:otherwise>
                        公共
                    </c:otherwise>
                </c:choose>
                通知
            </a>
        </td>
    </tr>
    </tbody>
</table>
<form id="formAdd" name="formAdd" action="create.do" method="post" enctype="multipart/form-data">
    <table width="100%" class="tabStyle_1">
        <tbody>
        <tr>
            <td colspan="3" class="tabStyle_1_title">发布通知</td>
        </tr>
        <tr>
            <td>标&nbsp;&nbsp;&nbsp;&nbsp;题：</td>
            <td colspan="2">
                <input type="text" name="title" id="title" size="80" maxlength="25"/>
            </td>
        </tr>
        <tr>
            <td id="tdColor">颜&nbsp;&nbsp;&nbsp;&nbsp;色：</td>
            <td colspan="2">
                <select id="color" name="color">
                    <option value="" style="COLOR: black" selected>标题颜色</option>
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
                &nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" id="level" name="level" value="1"/>
                <span title="重要通知将在桌面弹窗显示">重要通知</span>
                <!-- <input type="checkbox" id="isBold" name="isBold" value="true" />
                标题加粗 -->
                <input type="checkbox" name="isShow" checked="checked" value="1"/>
                显示已查看通知人员
                <input type="checkbox" name="isReply" value="1" id="isReply" checked="checked" title="是否可以回复"/>
                回复
                <span class="responseDiv">
                   <input type="checkbox" name="is_forced_response" id="is_forced_response" value="1"/>强制回复
                </span>
                <c:if test="${isUseSMS}">
                    <input type="checkbox" id="isToMobile" name="isToMobile" checked="checked" value="true"/>
                    短信提醒
                </c:if>
            </td>
        </tr>
        <tr>
            <td colspan="3" valign="top">
                <div id="divTmpAttachId" style="display:none"></div>
                <div style="clear:both">
                    <textarea id="content" name="content"></textarea>
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
                                return '<%=request.getContextPath()%>/ueditor/UploadFile?op=notice';
                            } else {
                                return this._bkGetActionUrl.call(this, action);
                            }
                        }
                    });
                </script>
            </td>
        </tr>
        <tr>
            <td><span class="TableContent">有效期：</span></td>
            <td colspan="2">
                <span class="TableData">
                <jsp:useBean id="now" class="java.util.Date"/>
                <fmt:formatDate value="${now}" type="both" dateStyle="long" pattern="yyyy-MM-dd" var="curDate"/>
                开始日期：
                <input type="text" id="beginDate" name="beginDate" size="10" onblur="beginDateCheck()" value="${curDate}"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                结束日期：
                <input type="text" id="endDate" name="endDate" size="10" title="结束日期不填表示永不过期"/>
                </span>
            </td>
        </tr>
        <tr id="trToDept">
            <td>
                <input type="hidden" name="isNoticeMgr" value="${isNoticeMgr ? 1 : 0}"/>
                <input type="hidden" id="depts" name="depts" value="${isNoticeMgr ? depts : myUnitCode}"/>
                <input id="isall" name="isall" value="${isNoticeAll ? 2 : 1}" type="hidden"/>
                <input id="userName" name="userName" value="${userName}" type="hidden"/>
                <input id="unitCode" name="unitCode" value="${myUnitCode}" type="hidden"/>
                发布人员：
            </td>
            <td colspan="2" style="line-height:1.5">
                <input id="radioall" type="radio" name="radio" value="全部用户" onclick="setAllUsers()" checked/> <a href="javascript:setAllUsers();setRadioALLSelected();">全部用户</a>
                <input id="radioselect" type="radio" name="radio" value="选择用户" onclick="setIsAll();openWinUsers();desDepts()"/><a href="javascript:setRadioSelected();setIsAll();openWinUsers();desDepts()">选择用户</a>
                <br/>
                <textarea name="deptNames" cols="90" rows="5" readOnly wrap="yes" id="deptNames" disabled>全部用户</textarea>
                <input type="hidden" name="receiver" id="receiver"/>
            </td>
        </tr>
        <tr>
            <td align="left" colspan="3">
                <script>initUpload();</script>
            </td>
        </tr>
        <tr>
            <td align="center" colspan="3">
                <input class="mybtn" id="btnAdd" type="button" value="确定" onclick='add()'/>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="button" class="mybtn" value="返回" onclick="window.history.back()"/></td>
        </tr>
        </tbody>
    </table>
</form>
<br/>
<script language="javascript">
    function setAllUsers() {
        <c:choose>
        <c:when test="${isNoticeAll}">
        o("isall").value = "2";
        o("unitCode").value = "root";
        </c:when>
        <c:when test="${isNoticeMgr}">
        o("isall").value = "1";
        o("unitCode").value = "${myUnitCode}";
        </c:when>
        </c:choose>
        $("#receiver").val("");
        o("deptNames").value = "全部用户";
    }

    function clearUsers() {
        $("#deptNames").val("");
        $("#receiver").val("");
    }

    function desDepts() {
        o("deptNames").disabled = true;
    }

    function setIsAll() {
        $("#receiver").val("");
        o("isall").value = "0";
        $("#deptNames").val("");
    }

    function setRadioALLSelected() {
        $("#radioall").attr("checked", "checked");
        $("#radioselect").removeAttr("checked");
    }

    function setRadioSelected() {
        $("#radioall").removeAttr("checked");
        $("#radioselect").attr("checked", "checked");
    }

</script>
</body>
<script language="JavaScript">
    var title;

    $(function () {
        $("#is_reply").change(function () {
            if ($("#is_reply").is(":checked")) {
                $(".responseDiv").show();
                $("#is_forced_response").removeAttr("checked");
            } else {
                $(".responseDiv").hide();
            }
        });

        $('#beginDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
        $('#endDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
        $('#color').change(function () {
            $('#color').css("color", $(this).val());
        });
        $("#level").click(function () {
            if ($("#level").attr("checked")) {
                $("#color").val("#ff0000");
            } else {
                $("#color").val("");
            }
            $("#color").change();
        });
        title = new LiveValidation('title');
        title.add(Validate.Presence);
    });

    function add() {
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
        if (o("deptNames").value == "") {
            <c:if test="${isNoticeAll}">
                $("#receiver").val("");
                o("isall").value = "2";
                o("unitCode").value = "root";
                o("deptNames").value = "全部用户";
            </c:if>
            <c:if test="${isNoticeMgr && !isAdmin}">
                $("#receiver").val("");
                o("isall").value = "1";
                o("unitCode").value = "${myUnitCode}";
                o("deptNames").value = "全部用户";
            </c:if>
        }
        $('#formAdd').submit();
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
        $('#formAdd').submit(function () {
            // 通过判断时间，禁多次重复提交
            var curSubmitTime = new Date().getTime();
            // 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
            if (curSubmitTime - lastSubmitTime < 500) {
                lastSubmitTime = curSubmitTime;
                $('#formAdd').hideLoading();
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
        $('#btnAdd').attr("disabled", true);
    }

    function showResponse(responseText, statusText, xhr, $form) {
        $('body').hideLoading();
        $('#btnAdd').attr("disabled", false);
        var data = $.parseJSON($.trim(responseText));
        if (data.ret == "0") {
            if (data.msg != null) {
                data.msg = data.msg.replace(/\\r/ig, "<BR>");
            }
            jAlert(data.msg, "提示");
        } else {
            var url = "list.do";
            jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', url);
        }
    }

    function beginDateCheck() {
        if ($("#beginDate").val() == "") {
            $("#beginDate").val("<%=DateUtil.format(new java.util.Date(), "yyyy-MM-dd") %>");
        }
    }
</script>
</html>
