<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>管理登录</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    int doc_id = ParamUtil.getInt(request, "doc_id");
    Document doc = new Document();
    doc = doc.getDocument(doc_id);

    CommentMgr cm = new CommentMgr();
    if (op.equals("del")) {
        try {
            if (cm.del(request, privilege))
                out.print(StrUtil.Alert("删除成功！"));
        } catch (ErrMsgException e) {
            out.print(StrUtil.Alert(e.getMessage()));
        }
    }

    if (op.equals("delall")) {
        try {
            cm.delAll(request, privilege);
            out.print(StrUtil.Alert("删除成功！"));
        } catch (ErrMsgException e) {
            out.print(StrUtil.Alert(e.getMessage()));
        }
    }
%>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">管理评论</td>
    </tr>
    </tbody>
</table>
<%
    java.util.Iterator ir = cm.getList(doc_id);
%>
<br>
<table class="percent98" align="center">
    <tr>
        <td>
            <a href="../fwebedit.jsp?op=edit&id=<%=doc.getID()%>&dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>"><%=doc.getTitle()%></a>
        </td>
        <td align="right">
            <input name="button" class="btn" type="button" onclick="if (confirm('您确定要全部删除么？')) window.location.href='comment_m.jsp?op=delall&amp;doc_id=<%=doc_id%>'" value="全部删除"/>
        </td>
    </tr>
</table>
<br>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tbody>
    <tr>
        <td width="88%" noWrap class="tabStyle_1_title">
            评论
        </td>
        <td width="12%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
        while (ir.hasNext()) {
            Comment cmt = (Comment) ir.next();
    %>
    <tr class="highlight">
        <td>
            <div><span class="style1">&nbsp;<a href="cmt.getLink()"><img border="0" src="images/arrow.gif" align="absmiddle"/>&nbsp;<a href="#"><%=cmt.getNick()%></a>&nbsp;发表于&nbsp;<%=cmt.getAddDate()%> </span></div>
                <%=cmt.getContent()%><span class="tableframe_comment"> </span>
        <td height="43" align="center"><span><a onClick="if (!confirm('您确定要删除吗？')) return false" href="comment_m.jsp?op=del&doc_id=<%=doc_id%>&id=<%=cmt.getId()%>">删除</a></span></td>
    </tr>
    <%}%>
    </tbody>
</table>
</body>
</html>