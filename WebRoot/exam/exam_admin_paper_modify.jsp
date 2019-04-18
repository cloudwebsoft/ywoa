<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>试卷修改</title>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <%
        if (!privilege.isUserPrivValid(request, "admin.exam")) {
            out.println(StrUtil.makeErrMsg(privilege.MSG_INVALID, "red", "green"));
            return;
        }
        int paperId = ParamUtil.getInt(request, "paperid");
    %>
    <script language="JavaScript" type="text/JavaScript">
        function SelectDateTime(objName) {
            var dt = openWin("../util/calendar/time.htm?divId" + objName, "266px", "185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
        }

        function sel(dt, objName) {
            if (dt != null && objName != "")
            //document.getEelemenById(objName).value=dt;
                o(objName).value = dt;
        }

        function openWin(url, width, height) {
            var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
        }

        function checktotal() {
            var myform = document.form1;
            var a = myform.singlecount.value;
            var b = myform.singleper.value;
            var c = myform.multicount.value;
            var d = myform.multiper.value;
            var e = myform.judgecount.value;
            var f = myform.judgeper.value;
            var g = myform.answercount.value;
            var h = myform.answerper.value;

            var i = myform.totalper.value;
            var j = a * b + c * d + e * f + g * h;
            ;
            if (j != i) {
                jAlert("当前卷面总分:" + j + ",与卷面总分不相符！", "提示");
                return false;
            } else {
                return true;
            }
        }

        function form1_onsubmit() {
            return checktotal();
        }

        //-->
    </script>
</head>
<%
    String op = ParamUtil.get(request, "op");
    PaperDb pdb = new PaperDb();
    pdb = pdb.getPaperDb(paperId);
    if (op.equals("modify")) {
        PaperMgr pm = new PaperMgr();
        boolean re = false;
        try {
            re = pm.modify(request);
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(StrUtil.toHtml(e.getMessage()), "提示"));
        }
        if (re)
            out.print(StrUtil.jAlert_Redirect("修改成功！", "提示", "exam_admin_paper_modify.jsp?paperid=" + paperId));
        return;
    }
%>
<body>
<div class="spacerH"></div>
<form method="post" name="form1" action="exam_admin_paper_modify.jsp?op=modify&id=<%=pdb.getId()%>&paperid=<%=pdb.getId()%>" onsubmit="return form1_onsubmit()">
    <table class="tabStyle_1 percent80" cellpadding="0" cellspacing="1" width="100%">
        <tr height="20">
            <td colspan="5" class="tabStyle_1_title"><b>试卷基本信息</b></td>
        </tr>
        <tr height="20">
            <td width="120"><p align="center">专业类别</td>
            <td colspan="4">
                <%
                    TreeSelectDb tsd = new TreeSelectDb();
                    tsd = tsd.getTreeSelectDb(pdb.getMajor());
                %>
                <input type="text" name="subject_show" size="20" value="<%=tsd.getName()%>" class="inputnormal" readonly="readonly"/>
                <input type="hidden" name="major" value="<%=pdb.getMajor() %>"/>
            </td>
        </tr>
        <tr height="20">
            <td align="center">&nbsp;名称</td>
            <td colspan="4"><input type="text" name="title" value="<%=pdb.getTitle()%>" class="inputnormal"/></td>
        </tr>
        <tr height="20">
            <td colspan="5" class="tabStyle_1_title">试卷信息</td>
        </tr>
        <tr height="20">
            <td align="center">卷面总分(<font color="#FF0000">分</font>)</td>
            <td colspan="4">
                <p align="left"><input type="text" name="totalper" size="20" value="<%=pdb.getTotalper()%>" class="inputnormal"/></p></td>
        </tr>
        <tr height="20">
            <td align="center"></td>
            <td><p align="center"><b>单选题</b></p></td>
            <td><p align="center"><b>多选题</b></p></td>
            <td><p align="center"><b>判断题</b></p></td>
            <td><p align="center"><b>问答题</b></p></td>
        </tr>
        <tr height="20">
            <td align="center">题型分布(<font color="#FF0000">个</font>)</td>
            <td><p align="center"><input type="text" name="singlecount" size="12" value="<%=pdb.getSingleCount()%>" class="inputnormal"/></p></td>
            <td><p align="center"><input type="text" name="multicount" size="12" value="<%=pdb.getMultiCount()%>" class="inputnormal"/></p></td>
            <td><p align="center"><input type="text" name="judgecount" size="12" value="<%=pdb.getJudgeCount()%>" class="inputnormal"/></p></td>
            <td><p align="center"><input type="text" name="answercount" size="12" value="<%=pdb.getAnswerCount()%>" class="inputnormal"/></p></td>
        </tr>
        <tr height="20">
            <td align="center">分值分布(<font color="#FF0000">分</font>)</td>
            <td><p align="center"><input type="text" name="singleper" size="12" value="<%=pdb.getSingleper()%>" class="inputnormal"/></p></td>
            <td><p align="center"><input type="text" name="multiper" size="12" value="<%=pdb.getMultiper()%>" class="inputnormal"/></p></td>
            <td><p align="center"><input type="text" name="judgeper" size="12" value="<%=pdb.getJudgeper()%>" class="inputnormal"/></p></td>
            <td><p align="center"><input type="text" name="answerper" size="12" value="<%=pdb.getAnswerper()%>" class="inputnormal"/></p></td>
        </tr>
    </table>
    <table class="tabStyle_1 percent80" cellpadding="0" cellspacing="0" width="100%">
        <tr>
            <%
                Date d = new Date();
                String dt = DateUtil.format(d, "yyyy-MM-dd hh:mm:ss");
            %>
            <td align="center" width="13%">考试模式</td>
            <td align="left" colspan="4">
                <select id="mode" name="mode">
                    <option id="sel1" value="<%=PaperDb.MODE_SPECIFY%>">按指定时间参加考试</option>
                    <option id="sel2" value="<%=PaperDb.MODE_PERIOD%>">按有效期参加考试</option>
                </select>
            </td>
        </tr>
        <tr height="20">
            <td align="center">时间</td>
            <td align="left" colspan="2">
                从<input type="text" id="starttime" name="starttime" size="20" value="<%=DateUtil.format(pdb.getStartTime(),"yyyy-MM-dd")%>" class="inputnormal"/>至
                <input type="text" id="endtime" name="endtime" size="20" value="<%=DateUtil.format(pdb.getEndTime(),"yyyy-MM-dd")%>" class="inputnormal"/>
                <span id="testtime1" style="display: none">
                    考试时长&nbsp;&nbsp;&nbsp;<input type="text" id="testtime" name="testtime" size="5" value="<%=pdb.getTestTime() %>"/>分钟
                    限考<input id="limitCount" name="limitCount" size="5" value="<%=pdb.getLimitCount()%>"/>次
                </span>
            </td>
        </tr>
        <tr>
            <td>多选题计分规则</td>
            <td colspan="4">
    		<span style=" float: left;">
	    		<input type="radio" name="multiScoreRule" value="0"/>部分答对不计分
	    		<input type="radio" name="multiScoreRule" value="1"/>部分答对计分
    		</span>
                <span style="display: none; float: left;" id="otherMultiPer">
    			&nbsp;&nbsp;&nbsp;分值&nbsp;&nbsp;<input id="notAllRightMuntiper" name="notAllRightMuntiper" value="<%=pdb.getNotAllRightMuntiper() %>"/>
    	  	</span>
            </td>
        </tr>
        <tr height="20">
            <td colspan="4" align="center">
                <input type="submit" value="确定" class="button1"/>
                &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="返回" onclick="window.history.back()"/>
            </td>
        </tr>
    </table>
    <input type="hidden" name="singleTotal" size="12" value="<%=pdb.getSingleTotal()%>" class="inputnormal"/>
    <input type="hidden" name="multiTotal" size="12" value="<%=pdb.getMultiTotal()%>" class="inputnormal"/>
    <input type="hidden" name="judgeTotal" size="12" value="<%=pdb.getJudgeTotal()%>" class="inputnormal"/>
    <input type="hidden" name="isManual" size="12" value="<%=pdb.isManual()?1:0%>" class="inputnormal"/>
    <input type="hidden" name="answerTotal" size="12" value="<%=pdb.getAnswerTotal()%>" class="inputnormal"/>
</form>
<script>
    $("#mode").change(function () {
        var s1 = $("#mode ").get(0).selectedIndex;
        if (s1 == 0) {
            $("#testtime1").hide();
            $("#testtime").val(-1);
            $('#starttime').datetimepicker({value: '<%= dt%>', step: 10, format: 'Y-m-d H:i:00'});
            $('#endtime').datetimepicker({value: '<%= dt%>', step: 10, format: 'Y-m-d H:i:00'});
        } else if (s1 == 1) {
            $("#testtime1").show();
            $("#testtime").val(null);
            $('#starttime').val('');
            $('#endtime').val('');
            $('#starttime').datetimepicker({value: '', step: 10, format: 'Y-m-d 07:00:00'});
            $('#endtime').datetimepicker({value: '', step: 10, format: 'Y-m-d 22:00:00'});
        }
    });
    $(function () {
        var testTime = <%=pdb.getTestTime()%>;
        if (testTime > 0) {
            $("#mode option:eq(1)").attr('selected', 'selected');
            $("#testtime1").show();
        } else {
            $("#mode option:eq(0)").attr('selected', 'selected');
        }
        $("#starttime").datetimepicker({value: '<%=DateUtil.format(pdb.getStartTime(),"yyyy-MM-dd HH:mm:ss") %>', step: 10, format: 'Y-m-d H:i:00'});
        var starttime = new LiveValidation('starttime');
        starttime.add(Validate.Presence);
        $("#endtime").datetimepicker({value: '<%=DateUtil.format(pdb.getEndTime(),"yyyy-MM-dd HH:mm:ss") %>', step: 10, format: 'Y-m-d H:i:00'});
        var endtime = new LiveValidation('endtime');
        endtime.add(Validate.Presence);
        var testtime = new LiveValidation('testtime');
        testtime.add(Validate.Presence);
        var multiScoreRule = "<%=pdb.getMultiScoreRule()%>";
        if (multiScoreRule == "0") {
            $("input:radio[value='0']").attr('checked', 'true');
        } else if (multiScoreRule == "1") {
            $("input:radio[value='1']").attr('checked', 'true');
        }

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
        if ($("input[name='multiScoreRule']:checked").val() == "1") {
            $("#otherMultiPer").show();
            var multiNotAllRightScore = new LiveValidation('notAllRightMuntiper');
            multiNotAllRightScore.add(Validate.Presence);
            multiNotAllRightScore.add(Validate.Numericality, {minimum: 0, tooLowMessage: '必须大于等于0'});
            multiNotAllRightScore.add(Validate.Numericality, {maximum: o("multiper").value, tooLowMessage: '不能大于' + o("multiper").value});
        } else {
            $("#otherMultiPer").hide();
        }
    })
</script>
</body>
</html>
