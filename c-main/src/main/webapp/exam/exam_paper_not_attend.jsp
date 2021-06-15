<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals(""))
        skincode = UserSet.defaultSkin;
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();

    String op = ParamUtil.get(request, "op");
    int paperId = ParamUtil.getInt(request, "paperId");
    PaperDb pd = new PaperDb();
    pd = pd.getPaperDb(paperId);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>未参加考试人员</title>
    <%@ include file="../inc/nocache.jsp" %>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css"/>
    <script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
    <style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="exam_paper_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<div class="spacerH"></div>
<%
    // 取出所有已考的分数
    ScoreDb sd = new ScoreDb();
    StringBuffer sb = new StringBuffer();
    Iterator ir = sd.getScores(paperId).iterator();
    while (ir.hasNext()) {
        sd = (ScoreDb)ir.next();
        sb.append(",");
        sb.append(sd.getUserName());
    }

    String usersReaded = sb.toString();
    if (!"".equals(usersReaded))
        usersReaded += ",";

    Vector v = PaperPriv.getUsersCanSee(paperId);
    int userCount = v.size();
    ir = v.iterator();
%>
<table width="93%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
    <thead>
    <tr>
        <td width="5%" align="center" name="name"><span class="right-title">
            <input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')"/>
          </span></td>
        <td width="61%" name="name">用户</td>
        <td width="34%" name="log_date">操作</td>
    </tr>
    </thead>
    <tbody>
    <%
        UserDb user = new UserDb();
        while (ir.hasNext()) {
            user = (UserDb) ir.next();
            String userName = user.getName();
            if (usersReaded.indexOf("," + userName + ",") != -1) {
                userCount = userCount - 1;
                continue;
            }
    %>
    <tr>
        <td align="center"><input type="checkbox" id="ids" name="ids" value="<%=user.getName()%>"/></td>
        <td><%=user.getRealName()%>
        </td>
        <td align="center"><a href="javascript:;" onclick="send('<%=user.getName()%>')">提醒用户</a></td>
    </tr>
    <%}%>
    <tr>
        <td colspan="3"><input class="btn" style="margin-left:3px" name="button2" type="button" onclick="sendBatch()" value="提醒用户"/>
            &nbsp;&nbsp;共<%=userCount%>人尚未参加
        </td>
    </tr>
    </tbody>
</table>
<%
    String querystr = "op=" + op;
%>
<form name="hidForm" action="../message_oa/message_frame.jsp" method="post">
    <input name="op" type="hidden" value="send"/>
    <input name="title" type="hidden" value="请参加考试：<%=pd.getTitle()%>"/>
    <input name="content" type="hidden" value="<a href=<%=request.getContextPath()%>/exam/exam_select_subject.jsp>点击此处进入考试页面</a>"/>
    <input id="receiver" name="receiver" type="hidden"/>
</form>
</body>
<script>
    function selAllCheckBox(checkboxname) {
        var checkboxboxs = document.getElementsByName(checkboxname);
        if (checkboxboxs != null) {
            // 如果只有一个元素
            if (checkboxboxs.length == null) {
                checkboxboxs.checked = true;
            }
            for (i = 0; i < checkboxboxs.length; i++) {
                checkboxboxs[i].checked = true;
            }
        }
    }

    function deSelAllCheckBox(checkboxname) {
        var checkboxboxs = document.getElementsByName(checkboxname);
        if (checkboxboxs != null) {
            if (checkboxboxs.length == null) {
                checkboxboxs.checked = false;
            }
            for (i = 0; i < checkboxboxs.length; i++) {
                checkboxboxs[i].checked = false;
            }
        }
    }

    function send(userName) {
        //hidForm.receiver.value = userName;
        o("receiver").value = userName;
        hidForm.submit();
    }

    function sendBatch() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            alert("请先选择用户！");
            return;
        }
        o("receiver").value = ids;
        //hidForm.receiver.value = ids;
        hidForm.submit();
    }
</script>
</html>