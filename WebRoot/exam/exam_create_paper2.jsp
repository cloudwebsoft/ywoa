<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.exam")) {
        out.println(StrUtil.makeErrMsg(privilege.MSG_INVALID, "red", "green"));
        return;
    }
%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>试卷生成第二步</title>
    <style type="text/css">
        .inputnormal {
            background-color: #ccc;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="nav.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
</head>
<body>
<%
    String major = ParamUtil.get(request, "major");
    String op = ParamUtil.get(request, "op");
    if (op.equals("create")) {
        PaperMgr bm = new PaperMgr();
        boolean re = false;
        try {
            re = bm.create(request);
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(StrUtil.toHtml(e.getMessage()), "提示"));
            return;
        }
        if (re) {
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "exam_paper_manager.jsp"));
            return;
        }
    }
%>
<div class="spacerH"></div>
<form method="post" name="form1" action="exam_create_paper2.jsp?op=create">
    <table class="tabStyle_1 percent98" cellpadding="0" cellspacing="0" width="100%">
        <tr height="20">
            <td class="tabStyle_1_title" colspan="5">试卷基本信息预览</td>
        </tr>
        <tr>
            <td width="120" align="center">专业</td>
            <td colspan="4">
                <%
                    TreeSelectDb tsd = new TreeSelectDb();
                    tsd = tsd.getTreeSelectDb(major);
                %>
                <input type="text" name="major_show" size="20" value="<%=tsd.getName()%>" class="inputnormal" readonly/>
                <input type="hidden" name="major" size="20" value="<%=major %>" class="inputnormal" readonly/>
            </td>
        </tr>
        <tr height="20">
            <td align="center">试卷名称</td>
            <td colspan="4"><input type="text" name="title" value="<%=ParamUtil.get(request,"title")%>" class="inputnormal" readonly/></td>
        </tr>
        <tr height="20">
            <td class="tabStyle_1_title" colspan="5">试卷信息</td>
        </tr>
        <tr height="20">
            <td align="center">卷面总分(<font color="#FF0000">分</font>)</td>
            <td colspan="4">
                <p align="left"><input type="text" name="totalper" size="20" value="<%=ParamUtil.get(request,"totalper")%>" class="inputnormal" readonly/></p></td>
        </tr>
        <tr height="20">
            <td class="tabStyle_1_title" align="center">　</td>
            <td align="center" class="tabStyle_1_title"> 单选题</td>
            <td class="tabStyle_1_title">多选题</td>
            <td class="tabStyle_1_title">判断题</td>
            <td class="tabStyle_1_title">问答题</td>
        </tr>
        <tr height="20">
            <td align="center">题型分布(<font color="#FF0000">个</font>)</td>
            <td><input type="text" name="singlecount" size="12" value="<%=ParamUtil.get(request,"singlecount")%>" class="inputnormal" readonly="readonly"/></td>
            <td><input type="text" name="multicount" size="12" value="<%=ParamUtil.get(request,"multicount")%>" class="inputnormal" readonly="readonly"/></td>
            <td><input type="text" name="judgecount" size="12" value="<%=ParamUtil.get(request,"judgecount")%>" class="inputnormal" readonly="readonly"/></td>
            <td><input type="text" name="answercount" size="12" value="<%=ParamUtil.get(request,"answercount")%>" class="inputnormal" readonly="readonly"/></td>
        </tr>
        <tr height="20">
            <td align="center">分值分布(<font color="#FF0000">分</font>)</td>
            <td><input type="text" name="singleper" size="12" value="<%=ParamUtil.get(request,"singleper")%>" class="inputnormal" readonly="readonly"/></td>
            <td><input type="text" name="multiper" size="12" value="<%=ParamUtil.get(request,"multiper")%>" class="inputnormal" readonly="readonly"/></td>
            <td><input type="text" name="judgeper" size="12" value="<%=ParamUtil.get(request,"judgeper")%>" class="inputnormal" readonly="readonly"/></td>
            <td><input type="text" name="answerper" size="12" value="<%=ParamUtil.get(request,"answerper")%>" class="inputnormal" readonly="readonly"/></td>
        </tr>
    </table>
    <table class="tabStyle_1 percent98" cellpadding="0" cellspacing="0" width="100%">
        <%
            Date d = new Date();
            String dt = DateUtil.format(d, "yyyy-MM-dd hh:mm:ss");
            int singleTotal = ParamUtil.getInt(request, "singlecount") * ParamUtil.getInt(request, "singleper");
            int multiTotal = ParamUtil.getInt(request, "multicount") * ParamUtil.getInt(request, "multiper");
            int judgeTotal = ParamUtil.getInt(request, "judgecount") * ParamUtil.getInt(request, "judgeper");
            int answerTotal = ParamUtil.getInt(request, "answercount") * ParamUtil.getInt(request, "answerper");
        %>
        <tr>
            <td align="center" width="120px;">考试模式</td>
            <td align="left" colspan="4">
                <select id="mode" name="mode">
                    <option id="<%=PaperDb.MODE_SPECIFY%>" selected="selected">按指定时间参加考试</option>
                    <option id="<%=PaperDb.MODE_PERIOD%>">按有效期参加考试</option>
                </select>
            </td>
        </tr>
        <tr height="20">
            <td align="center" id="inTime">时间</td>
            <td align="left" colspan="4">从
                <input type="text" id="starttime" name="starttime" size="20" value="" readonly="readonly"/>
                &nbsp;至
                <input type="text" id="endtime" name="endtime" size="20" value="" readonly="readonly"/>
                <span id="testtime1" style="display: none;">
                    考试时长&nbsp;&nbsp;&nbsp;<input type="text" id="testtime" name="testtime" size="5" value="-1"/>分钟
                    限考<input id="limitCount" name="limitCount" size="5" value="1"/>次
                </span>
            </td>
        </tr>
        <tr>
            <td>多选题计分规则</td>
            <td colspan="4">
    		<span style=" float: left;">
    			<input type="radio" name="multiScoreRule" value="0" checked/>部分答对不计分
    			<input type="radio" name="multiScoreRule" value="1"/>部分答对计分
    		</span>
                <span style="display: none; float: left;" id="otherMultiPer">
    			&nbsp;&nbsp;&nbsp;分值&nbsp;&nbsp;<input id="notAllRightMuntiper" name="notAllRightMuntiper"/>
    		</span>
            </td>
        </tr>
        <tr height="20">
            <td colspan="5" align="center">
                <input type="button" value="上一步" name="B2" class="btn" onclick="javascript:history.go(-1);"/>
                &nbsp;&nbsp;
                <input type="submit" class="btn" value="确定"/></td>
        </tr>
    </table>
    <input type="hidden" name="singleTotal" value="<%=singleTotal %>"/>
    <input type="hidden" name="multiTotal" value="<%=multiTotal %>"/>
    <input type="hidden" name="judgeTotal" value="<%=judgeTotal %>"/>
    <input type="hidden" name="answerTotal" value="<%=answerTotal %>"/>
    <input type="hidden" name="isManual" value="0"/>
    <script>
        // 下拉列表框的change事件 选择“按有效期参加考试”弹出考试时长输入框
        $("#mode").change(function () {
            var s1 = $("#mode ").get(0).selectedIndex;
            if (s1 == 0) {
                $("#testtime1").hide();
                $("#testtime").val(-1);
                $('#inTime').text('时间');
                $('#starttime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
                $('#endtime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
            } else if (s1 == 1) {
                $("#testtime1").show();
                $("#testtime").val(null);
                $('#starttime').val('');
                $('#endtime').val('');
                $('#inTime').text('进入考场时间');
                $('#starttime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
                $('#endtime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
            }
            ;
        });
        $(function () {
            $('#starttime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
            $('#endtime').datetimepicker({value: '', step: 10, format: 'Y-m-d H:i:00'});
            var startTime = o("starttime").value;
            var endTime = o("endtime").value;
            // var testTime = mut(startTime,endTime);
            var starttime = new LiveValidation('starttime');
            starttime.add(Validate.Presence);
            var endtime = new LiveValidation('endtime');
            endtime.add(Validate.Presence);
            var testtime = new LiveValidation('testtime');
            testtime.add(Validate.Presence);
            $('input[type=radio][name=multiScoreRule]').change(function () {
                if (this.value == '0') {
                    $("#otherMultiPer").hide();
                } else if (this.value == '1') {
                    $("#otherMultiPer").show();
                    var multiNotAllRightScore = new LiveValidation('notAllRightMuntiper');
                    multiNotAllRightScore.add(Validate.Presence);
                    multiNotAllRightScore.add(Validate.Numericality, {minimum: 0, tooLowMessage: '必须大于等于0'});
                    multiNotAllRightScore.add(Validate.Numericality, {maximum: o("multiper").value, tooLowMessage: '不能大于' + o("multiper").value});
                }
            });
        });
    </script>
</form>
</body>
</html>
