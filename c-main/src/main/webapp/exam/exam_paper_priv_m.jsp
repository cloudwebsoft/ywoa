<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.exam.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int paperId = ParamUtil.getInt(request, "paperId", -1);
    String title = ParamUtil.get(request, "title");
//if (paperId == -1) {
    //return;
//}
    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "name";
    String sort = ParamUtil.get(request, "sort");

    String op = ParamUtil.get(request, "op");

    String isAll = ParamUtil.get(request, "isAll");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>管理试卷权限</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script>
        var curOrderBy = "<%=orderBy%>";
        var sort = "<%=sort%>";

        function doSort(orderBy) {
            if (orderBy == curOrderBy)
                if (sort == "asc")
                    sort = "desc";
                else
                    sort = "asc";

            window.location.href = "exam_paper_priv_m.jsp?id=<%=paperId%>&isAll=<%=isAll%>&orderBy=" + orderBy + "&sort=" + sort;
        }
    </script>
</head>
<body>
<jsp:useBean id="paperPriv" scope="page" class="com.redmoon.oa.exam.PaperPriv"/>
<%
    paperPriv.setPaperId(paperId);
    PaperDb pd = new PaperDb();
    pd = pd.getPaperDb(paperId);

    if (op.equals("add")) {
        String name = ParamUtil.get(request, "name");
        String[] nameArr = name.split(",");
        for (int i = 0; i < nameArr.length; i++) {
            if (nameArr[i].equals("")) {
                out.print(StrUtil.Alert_Back("名称不能为空！"));
                return;
            }
            int type = ParamUtil.getInt(request, "type");
            if (type == paperPriv.TYPE_USER) {
                UserDb user = new UserDb();
                user = user.getUserDb(nameArr[i]);
                if (!user.isLoaded()) {
                    out.print(StrUtil.Alert_Back("该用户不存在！"));
                    return;
                }
            }
            try {
                if (paperPriv.add(nameArr[i], type, paperId))
                    out.print(StrUtil.Alert_Redirect("添加成功！", "exam_paper_priv_m.jsp?isAll=" + isAll + "&paperId=" + paperId + "&title=" + title));
            } catch (ErrMsgException e) {
                out.print(StrUtil.Alert_Back(e.getMessage()));
            }
        }
        return;
    } else if (op.equals("setrole")) {
        try {
            String roleCodes = ParamUtil.get(request, "roleCodes");
            //String leafCode = ParamUtil.get(request, "dirCode");
            PaperPriv lp = new PaperPriv(paperId);
            lp.setRoles(paperId, roleCodes);
            out.print(StrUtil.Alert_Redirect("操作成功！", "exam_paper_priv_m.jsp?isAll=" + isAll + "&paperId=" + paperId + "&title=" + title));
        } catch (Exception e) {
            out.print(StrUtil.Alert_Back(e.getMessage()));
        }
    } else if (op.equals("modify")) {
        int id = ParamUtil.getInt(request, "id");
        int see = 0, append = 0, del = 0, modify = 0, examine = 0;
        String strsee = ParamUtil.get(request, "see");
        if (StrUtil.isNumeric(strsee)) {
            see = Integer.parseInt(strsee);
        }

        paperPriv.setId(id);
        paperPriv.setSee(see);
        System.out.println("Id:" + id);
        if (paperPriv.save()) {
            if (isAll.equals("y"))
                out.print(StrUtil.Alert_Redirect("修改成功！", "exam_paper_priv_m.jsp?isAll=" + isAll));
            else
                out.print(StrUtil.Alert_Redirect("修改成功！", "exam_paper_priv_m.jsp?isAll=" + isAll + "&paperId=" + paperId + "&title=" + title));
        } else
            out.print(StrUtil.Alert_Back("修改失败！"));
        return;
    } else if (op.equals("del")) {
        int id = ParamUtil.getInt(request, "id");
        PaperPriv lp = new PaperPriv();
        lp = lp.getPaperPriv(id);
        if (lp.del())
            out.print(StrUtil.Alert_Redirect("删除成功！", "exam_paper_priv_m.jsp?isAll=" + isAll + "&paperId=" + paperId));
        else
            out.print(StrUtil.Alert_Back("删除失败！"));
        return;
    }
%>
<%@ include file="exam_paper_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<div class="spacerH"></div>
<%
    String sql = "select id from oa_exam_paper_priv" + " order by " + orderBy + " " + sort;
    Vector result = null;
    if (isAll.equals("y")) {
        result = paperPriv.list(sql);
    } else
        result = paperPriv.list();
    Iterator ir = result.iterator();

    if (!isAll.equals("y")) {
%>
<table class="percent98" width="80%" align="center">
    <tr>
        <td align="right">
            <input class="btn" name="button" type="button" onclick="javascript:location.href='exam_paper_priv_add.jsp?title=<%=title%>&paperId=<%=paperPriv.getPaperId()%>';" value="添加权限" width=80height=20/>
        </td>
    </tr>
</table>
<%}%>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tbody>
    <tr>
        <td class="tabStyle_1_title" noWrap width="17%" style="cursor:hand" onClick="doSort('name')">用户
            <%
                if (orderBy.equals("name")) {
                    if (sort.equals("asc"))
                        out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                    else
                        out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
                }%>
        </td>
        <td class="tabStyle_1_title" nowrap="nowrap" width="13%" style="cursor:pointer" onclick="doSort('priv_type')">类型<span class="right-title" style="cursor:pointer">
        <%
            if (orderBy.equals("priv_type")) {
                if (sort.equals("asc"))
                    out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                else
                    out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
            }
        %>
      </span></td>
        <td class="tabStyle_1_title" noWrap width="47%">权限</td>
        <td width="20%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
        int i = 0;

//Directory dir = new Directory();
        while (ir.hasNext()) {
            PaperPriv lp = (PaperPriv) ir.next();
            i++;
    %>
    <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
        <tr id="tr<%=i%>" class="highlight">
            <td>
                <%
                    if (lp.getType() == lp.TYPE_USER) {
                        UserDb ud = new UserDb();
                        ud = ud.getUserDb(lp.getName());
                        out.print(ud.getRealName());
                    } else if (lp.getType() == lp.TYPE_ROLE) {
                        RoleDb rd = new RoleDb();
                        rd = rd.getRoleDb(lp.getName());
                        out.print(rd.getDesc());
                    } else if (lp.getType() == lp.TYPE_USERGROUP) {
                        UserGroupDb ug = new UserGroupDb();
                        ug = ug.getUserGroupDb(lp.getName());
                        out.print(ug.getDesc());
                    }
                %>
                <input type=hidden name="id" value="<%=lp.getId() %>">
                <input type=hidden name="paperId" value="<%=lp.getPaperId()%>">
                <input type=hidden name="isAll" value="<%=isAll%>">
                <input type=hidden name="title" value="<%=title %>">
            </td>
            <td><%
                if (lp.getType() == lp.TYPE_USER) {
            %>
                用户
                <%
                } else if (lp.getType() == lp.TYPE_ROLE) {
                %>
                角色
                <%
                } else if (lp.getType() == lp.TYPE_USERGROUP) {
                %>
                用户组
                <%
                    }
                %></td>
            <td>
                <input name=see type=checkbox <%=lp.getSee()==1?"checked":""%> value="1">
                参加考试
            </td>
            <td align="center">
                <input class="btn" type=submit value="修改">
                &nbsp;<input class="btn" type=button onClick="sureToDel(<%=lp.getId()%>,<%=paperId %>)" value="删除"/></td>
        </tr>
    </form>
    <%}%>
    </tbody>
</table>
<br>
<script>
    function sureToDel(id, docid) {
        if (confirm('您确定要删除吗?')) {
            window.location.href = "exam_paper_priv_m.jsp?op=del&id=" + id + "&paperId=" + docid;
        }
    }

    function checkPrivDownload(trId) {
        var isChecked = $("#" + trId + " input[name='modify']").attr("checked");
        if (isChecked) {
            $("#" + trId + " input[name='see']").attr("checked", true);
        }
    }

    function checkPrivPrint(trId) {
        var isChecked = $("#" + trId + " input[name='officePrint']").attr("checked");
        if (isChecked) {
            $("#" + trId + " input[name='officeSee']").attr("checked", true);
        }
    }
</script>
</body>
</html>