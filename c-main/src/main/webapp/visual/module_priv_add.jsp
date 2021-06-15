<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能模块设计 - 添加权限</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script>
        function setPerson(deptCode, deptName, user, userRealName) {
            form1.name.value = user;
            form1.userRealName.value = userRealName;
        }

        function setRoles(roles, descs) {
            o("roleCodes").value = roles;
            o("roleDescs").value = descs;
        }

        var selUserNames = "";
        var selUserRealNames = "";

        function getSelUserNames() {
            return selUserNames;
        }

        function getSelUserRealNames() {
            return selUserRealNames;
        }

        function setUsers(user, userRealName) {
            $('#form1 #name').val(user);
            $('#form1 #userRealName').val(userRealName);
        }

        function openWinUsers() {
            selUserNames = $('#form1 #name').val();
            selUserRealNames = $('#form1 #userRealName').val();
            openWin('../user_multi_sel.jsp', 800, 600);
        }
    </script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String code = ParamUtil.get(request, "code");
    String formCode = ParamUtil.get(request, "formCode");
    FormDb fd = new FormDb(formCode);
    if (!fd.isLoaded()) {
        out.print(StrUtil.Alert_Back("该表单不存在！"));
        return;
    }
    // 表示从哪个tab进入本页面，用于刷新角色菜单页面user_role_menu.jsp
    String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
    boolean isNav = tabIdOpener.equals("") ? true : false;
    if (isNav) {
%>
<%@ include file="module_setup_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<div class="spacerH"></div>
<%}%>
<form id="formRole" name="formRole" method="post" action="module_priv_list.jsp?op=setrole">
    <table class="tabStyle_1 percent60" cellspacing="0" cellpadding="3" width="50%" align="center">
        <tbody>
        <tr>
            <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
        </tr>
        <%
            RoleMgr roleMgr = new RoleMgr();
            ModulePrivDb lp = new ModulePrivDb();
            Vector vrole = lp.getRolesOfModule(code);

            String roleCode;
            String roleCodes = "";
            String descs = "";
            Iterator irrole = vrole.iterator();
            while (irrole.hasNext()) {
                RoleDb rd = (RoleDb) irrole.next();
                roleCode = rd.getCode();
                if (roleCodes.equals(""))
                    roleCodes += roleCode;
                else
                    roleCodes += "," + roleCode;
                if (descs.equals(""))
                    descs += rd.getDesc();
                else
                    descs += "," + rd.getDesc();
            }
        %>
        <tr class="row">
            <td align="center"><textarea name=roleDescs cols="60" rows="3" style="width:100%"><%=descs%></textarea>
                <input name="roleCodes" value="<%=roleCodes%>" type="hidden"/>
                <input name="code" value="<%=code%>" type="hidden"/>
                <input name="formCode" value="<%=formCode%>" type="hidden"/></td>
        </tr>
        <tr align="center" class="row">
            <td style="PADDING-LEFT: 10px">
                <input type="button" class="btn" onclick="openWin('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>&unitCode=<%=privilege.getUserUnitCode(request)%>', 800, 600)" value="选择"/>
                &nbsp;&nbsp;
                <input type="submit" class="btn" value="确定"/>
                <input name="tabIdOpener" value="<%=tabIdOpener%>" type="hidden"/>
                &nbsp;&nbsp;
                <input type="button" class="btn" value="返回" onclick="window.history.back()"/>
            </td>
        </tr>
        </tbody>
    </table>
</form>
<%
    String mycode;
    String desc;
    UserGroupDb ugroup = new UserGroupDb();
    Vector result = ugroup.getUserGroupsOfUnit(privilege.getUserUnitCode(request));
    Iterator ir = result.iterator();
%>
<br>
<table class="tabStyle_1 percent60" cellSpacing="0" cellPadding="3" width="50%" align="center">
    <tbody>
    <tr>
        <td class="tabStyle_1_title" noWrap width="26%">用户组名称</td>
        <td class="tabStyle_1_title" noWrap width="53%">描述</td>
        <td width="21%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
        while (ir.hasNext()) {
            UserGroupDb ug = (UserGroupDb) ir.next();
            mycode = ug.getCode();
            desc = ug.getDesc();
    %>
    <tr class="highlight">
        <td><%=mycode%>
        </td>
        <td><%=desc%>
        </td>
        <td align="center">
            <a href="module_priv_list.jsp?op=add&code=<%=code%>&formCode=<%=formCode%>&name=<%=StrUtil.UrlEncode(mycode)%>&type=<%=ModulePrivDb.TYPE_USERGROUP%>&tabIdOpener=<%=tabIdOpener%>">增加</a></td>
    </tr>
    <%}%>
    </tbody>
</table>
<br/>
<form id="form1" name="form1" action="module_priv_list.jsp?op=add" method=post>
    <table class="tabStyle_1 percent60" width="444" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td width="444" class="tabStyle_1_title">用户</td>
        </tr>
        <tr>
            <td height="25" align="center"><textarea id="userRealName" name="userRealName" readonly="readonly" style="width:100%; height:100px; margin-bottom: 5px"></textarea>
                <input id="name" name="name" value="" type="hidden"/>
                <input type="hidden" name="type" value="1"/>
                <input type="hidden" name="code" value="<%=code%>"/>
                <input type="hidden" name="formCode" value="<%=formCode%>"/>
                <input class="btn" onclick="openWinUsers()" value="选择" type="button"/>
                &nbsp;&nbsp;
                <input class="btn" type="submit" align="middle" value="确定"/>
                &nbsp;&nbsp;
                <input type="button" class="btn" value="返回" onclick="window.history.back()"/>
                <input name="tabIdOpener" value="<%=tabIdOpener%>" type="hidden"/>
            </td>
        </tr>
    </table>
</form>
<br>
</body>
</html>