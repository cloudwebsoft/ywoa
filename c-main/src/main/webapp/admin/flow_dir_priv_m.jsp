<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>管理流程类型权限</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<jsp:useBean id="leafPriv" scope="page" class="com.redmoon.oa.flow.LeafPriv"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
    boolean isNav = "".equals(tabIdOpener);
    if (isNav) {
%>
<%@ include file="flow_inc_menu_top.jsp" %>
<script>
    o("menu6").className = "current";
</script>
<%
    }

	String op = ParamUtil.get(request, "op");
    String dirCode = ParamUtil.get(request, "dirCode");

    String isAll = ParamUtil.get(request, "isAll");
    if (dirCode.equals("")) {
        isAll = "y";
    }

    if (isAll.equals("y")) {
        if (!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "admin.unit")) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    if (!dirCode.equals("")) {
        leafPriv.setDirCode(dirCode);
        if (!(leafPriv.canUserExamine(privilege.getUser(request)))) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid") + " 用户需对该节点拥有管理的权限"));
            return;
        }
    }

    String curCode = "", curName = "";
    Leaf leaf = new Leaf();
    if (!dirCode.equals("")) {
        leaf = leaf.getLeaf(dirCode);
        curCode = leaf.getCode();
        curName = leaf.getName(request);
    }

    if (op.equals("add")) {
        String name = ParamUtil.get(request, "name");
        if (name.equals("")) {
            out.print(StrUtil.jAlert_Back("名称不能为空！", "提示"));
            return;
        }
        int type = ParamUtil.getInt(request, "type");
        String[] names = name.split("\\,");
        boolean re = false;
        for (String um : names) {
            if (type == LeafPriv.TYPE_USER) {
                UserDb user = new UserDb();
                user = user.getUserDb(um);
                if (!user.isLoaded()) {
                    continue;
                }
            }
            try {
                re = leafPriv.add(um, type);
            } catch (ErrMsgException e) {
                out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
                return;
            }
        }
        if (re) {
            out.print(StrUtil.jAlert_Redirect("添加成功！", "提示", "flow_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode) + "&tabIdOpener=" + tabIdOpener));
        } else {
            out.print(StrUtil.jAlert_Back("操作失败", "提示"));
        }
        return;
    } else if (op.equals("setrole")) {
        try {
            String roleCodes = ParamUtil.get(request, "roleCodes");
            String leafCode = ParamUtil.get(request, "dirCode");
            LeafPriv lp = new LeafPriv(leafCode);
            lp.setRoles(leafCode, roleCodes);
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "flow_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode) + "&tabIdOpener=" + tabIdOpener));
        } catch (Exception e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
        return;
    } else if (op.equals("modify")) {
        int id = ParamUtil.getInt(request, "id");
        int see = 0, append = 0, del = 0, modify = 0, examine = 0;
        String strsee = ParamUtil.get(request, "see");
        if (StrUtil.isNumeric(strsee)) {
            see = Integer.parseInt(strsee);
        }
        String strappend = ParamUtil.get(request, "append");
        if (StrUtil.isNumeric(strappend)) {
            append = Integer.parseInt(strappend);
        }
        String strmodify = ParamUtil.get(request, "modify");
        if (StrUtil.isNumeric(strmodify)) {
            modify = Integer.parseInt(strmodify);
        }
        String strdel = ParamUtil.get(request, "del");
        if (StrUtil.isNumeric(strdel)) {
            del = Integer.parseInt(strdel);
        }
        String strexamine = ParamUtil.get(request, "examine");
        if (StrUtil.isNumeric(strexamine)) {
            examine = Integer.parseInt(strexamine);
        }

        leafPriv.setId(id);
        leafPriv.setAppend(append);
        leafPriv.setModify(modify);
        leafPriv.setDel(del);
        leafPriv.setSee(see);
        leafPriv.setExamine(examine);
        if (leafPriv.save())
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "flow_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode) + "&tabIdOpener=" + tabIdOpener));
        else
            out.print(StrUtil.jAlert_Back("修改失败！", "提示"));
        return;
    } else if (op.equals("del")) {
        int id = ParamUtil.getInt(request, "id");
        LeafPriv lp = new LeafPriv();
        lp = lp.getLeafPriv(id);
        if (lp.del())
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "flow_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode) + "&tabIdOpener=" + tabIdOpener));
        else
            out.print(StrUtil.jAlert_Back("删除失败！", "提示"));
        return;
    }
%>
<%
    String action = ParamUtil.get(request, "action");
    Vector result = null;
    String what = ParamUtil.get(request, "what");
    if (isAll.equals("y")) {
        String sql = "select id from flow_dir_priv dp, flow_directory d where dp.dir_code=d.code and d.unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by id desc";
        if (action.equals("search")) {
            if (!what.equals(""))
                sql = "select id from flow_dir_priv dp, flow_directory d where dp.dir_code=d.code and dp.name like " + StrUtil.sqlstr("%" + what + "%") + " and d.unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by id desc";
        }
        result = leafPriv.list(sql);
    } else {
        String sql = "select id from flow_dir_priv dp where dp.dir_code=" + StrUtil.sqlstr(dirCode) + " order by id desc";
        if (action.equals("search")) {
            if (!what.equals(""))
                sql = "select id from flow_dir_priv dp where dp.name like " + StrUtil.sqlstr(dirCode) + " and dp.dir_code=" + StrUtil.sqlstr(dirCode) + " order by id desc";
        }
        result = leafPriv.list(sql);
    }

	Iterator ir = result.iterator();
%>
<table width="98%" align="center" class="percent98">
    <tr>
        <td align="right">
            <form action="flow_dir_priv_m.jsp" method="get">
                <!--
				<input name="action" value="search" type="hidden" />
				编码<input id="what" name="what" size="15" value="<%=what%>" />
				<input type="submit" value="查询" />
				-->
                <%if (!isAll.equals("y")) {%>
                &nbsp;&nbsp;<input name="button" class="btn" type="button"
                                   onclick="javascript:location.href='flow_dir_priv_add.jsp?dirCode=<%=StrUtil.UrlEncode(leafPriv.getDirCode())%>&tabIdOpener=<%=tabIdOpener%>';"
                                   value="添加"/>
                <%}%>
            </form>
        </td>
    </tr>
</table>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tbody>
    <tr>
        <td class="tabStyle_1_title" nowrap="nowrap" width="18%">用户</td>
        <td class="tabStyle_1_title" nowrap="nowrap" width="8%">发起</td>
        <td class="tabStyle_1_title" nowrap="nowrap" width="9%">查询</td>
        <td class="tabStyle_1_title" nowrap="nowrap" width="8%">管理</td>
        <td class="tabStyle_1_title" noWrap width="13%">类型</td>
        <td class="tabStyle_1_title" noWrap width="25%">流程类别</td>
        <td width="19%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
        int i = 0;
        Directory dir = new Directory();
        while (ir.hasNext()) {
            LeafPriv lp = (LeafPriv) ir.next();
            i++;
            Leaf lf = dir.getLeaf(lp.getDirCode());
            String dirName = lf == null ? "" : lf.getName(request);
    %>
    <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
        <tr class="highlight">
            <td style="PADDING-LEFT: 10px"><%
                if (lp.getType() == LeafPriv.TYPE_USER) {
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(lp.getName());
                    out.print(ud.getRealName());
                } else if (lp.getType() == LeafPriv.TYPE_ROLE) {
                    RoleDb rd = new RoleDb();
                    rd = rd.getRoleDb(lp.getName());
                    out.print(rd.getDesc());
                } else if (lp.getType() == LeafPriv.TYPE_USERGROUP) {
                    UserGroupDb ug = new UserGroupDb();
                    ug = ug.getUserGroupDb(lp.getName());
                    out.print(ug.getDesc());
                }
            %>
                <input type="hidden" name="id" value="<%=lp.getId()%>"/>
                <input type="hidden" name="dirCode" value="<%=lp.getDirCode()%>"/>
                <input type="hidden" name="isAll" value="<%=isAll%>"/>
                <input type="hidden" name="tabIdOpener" value="<%=tabIdOpener%>"/>
            </td>
            <td align="center" style="PADDING-LEFT: 10px"><input name="see"
                                                                 type="checkbox" <%=lp.getSee() == 1 ? "checked" : ""%>
                                                                 value="1"/>
                <input name="append" type="hidden" <%=lp.getAppend() == 1 ? "checked" : ""%> value="1"/>
                <input name="del" type="hidden" <%=lp.getDel() == 1 ? "checked" : ""%> value="1"/>
            </td>
            <td align="center" style="PADDING-LEFT: 10px"><input name="modify"
                                                                 type="checkbox" <%=lp.getModify() == 1 ? "checked" : ""%>
                                                                 value="1"/></td>
            <td align="center" style="PADDING-LEFT: 10px"><input name="examine"
                                                                 type="checkbox" <%=lp.getExamine() == 1 ? "checked" : ""%>
                                                                 value="1"/></td>
            <td><%=lp.getTypeDesc()%>
            </td>
            <td><a href="flow_dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(lp.getDirCode())%>"><%=dirName%>
            </a></td>
            <td align="center">
                <input class="btn" type=submit value="修改"/>
                &nbsp;<input class="btn" type=button
                             onclick="jConfirm('您确定要删除吗?','提示',function(r){ if(!r){return;}else{window.location.href='flow_dir_priv_m.jsp?op=del&isAll=<%=isAll%>&dirCode=<%=StrUtil.UrlEncode(curCode)%>&id=<%=lp.getId()%>&tabIdOpener=<%=tabIdOpener%>'}}) "
                             style="cursor:pointer" value="删除"/></td>
        </tr>
    </form>
    <%}%>
    </tbody>
</table>
</body>
</html>