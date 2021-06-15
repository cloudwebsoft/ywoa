<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../inc/inc.jsp" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
    <meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><lt:Label res="res.label.forum.admin.forum_m" key="filter_info"/></title>
    <link rel="stylesheet" href="../../common.css">
    <LINK href="default.css" type=text/css rel=stylesheet>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<%
    ForumDb fd = new ForumDb();
    fd = fd.getForumDb();
    String op = ParamUtil.get(request, "op");
    if (op.equals("setNotice")) {
        String ids = ParamUtil.get(request, "notices");
        fd.setNotices(ids);
        if (fd.save())
            out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "forum_m.jsp"));
        else
            out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
        return;
    }
    if (op.equals("setFilterName")) {
        String filterName = ParamUtil.get(request, "filterName");
        fd.setFilterUserName(filterName);
        if (fd.save())
            out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "forum_m.jsp"));
        else
            out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
        return;
    }
    if (op.equals("setFilterMsg")) {
        String filterMsg = ParamUtil.get(request, "filterMsg");
        fd.setFilterMsg(filterMsg);
        if (fd.save())
            out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "forum_m.jsp"));
        else
            out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
        return;
    }

    fd = ForumDb.getInstance();
%>
<table width='100%' cellpadding='0' cellspacing='0'>
    <tr>
        <td class="head"><lt:Label res="res.label.forum.admin.forum_m" key="security_manage"/></td>
    </tr>
</table>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
    if (!privilege.isMasterLogin(request)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }%>
<br>
<TABLE class="frame_gray" cellSpacing=0 cellPadding=0 width="95%" align=center>
    <TBODY>
    <TR>
        <TD valign="top" bgcolor="#FFFBFF" class="thead"><lt:Label res="res.label.forum.admin.forum_m" key="filter_info"/></TD>
    </TR>
    <TR>
        <TD height=200 valign="top" bgcolor="#FFFBFF"><br>
            <table width="60%" border='0' align="center" cellpadding='0' cellspacing='0' class="tableframe_gray">

                <tr>
                    <td valign="top">
                        <form METHOD=POST ACTION="?op=setFilterName" name="form2">
                            <table width="100%" border='0' cellspacing='0' cellpadding='0'>
                                <tr>
                                    <td width="100%" class="stable">
                                <tr>
                                    <td height="23" colspan=3 align="center" class="stable">
                                        <table width="100%">
                                            <tr>
                                                <td height="22"><lt:Label res="res.label.forum.admin.forum_m" key="filter_user"/></td>
                                            </tr>
                                            <tr>
                                                <td height="22"><textarea name="filterName" cols="50" rows="3" style='border:1pt solid #636563;font-size:9pt'><%=fd.getFilterUserName()%></textarea></td>
                                            </tr>
                                            <tr>
                                                <td height="22" align="center"><input name="submit4" type="submit" value="<lt:Label key="ok"/>"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </TABLE>
                        </form>
                    </td>
                </tr>
            </table>
            <br>
            <FORM METHOD=POST ACTION="?op=setFilterMsg" name="form3">
                <table width="60%" align="center" class="tableframe_gray">
                    <tr>
                        <td width="18%" height="22"><lt:Label res="res.label.forum.admin.forum_m" key="filter_keywords"/></td>
                    </tr>
                    <tr>
                        <td height="22"><textarea name="filterMsg" cols="50" rows="3" id="filterMsg" style='border:1pt solid #636563;font-size:9pt'><%=fd.getFilterMsg()%></textarea></td>
                    </tr>
                    <tr>
                        <td height="22" align="center"><input name="submit3" type="submit" value="<lt:Label key="ok"/>"></td>
                    </tr>
                </table>
            </FORM>

            <br></TD>
    </TR>
    </TBODY>
</TABLE>
<br>
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>
<script>
    function form5_onsubmit() {
        form5.reason.value = getHtml();
        if (form5.reason.value.length > 3000) {
            alert('<lt:Label res="res.label.forum.admin.forum_m" key="too_long"/>' + 3000);
            return false;
        }
    }
</script>
</html>                            
  