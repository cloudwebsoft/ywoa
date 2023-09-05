<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.security.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>管理服务器访问权限</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <script src="../inc/common.js"></script>
    <script>
        function setPerson(deptCode, deptName, user, userRealName) {
            form1.name.value = user;
            form1.userRealName.value = userRealName;
        }

        function setRoles(roles, descs) {
            formRole.roleCodes.value = roles;
            formRole.roleDescs.value = descs
        }
    </script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int serverIPId = ParamUtil.getInt(request, "serverIPId");
    ServerIPDb sid = new ServerIPDb();
    sid = (ServerIPDb) sid.getQObjectDb(new Integer(serverIPId));
%>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">设置目录 <a
                href="ip_priv_list.jsp?serverIPId=<%=serverIPId%>"><%=sid.getString("description")%>
        </a> 权限
        </td>
    </tr>
    </tbody>
</table>
<br>
<table class="tabStyle_1 percent60" cellspacing="0" cellpadding="3" width="50%" align="center">
    <form name="formRole" method="post" action="ip_priv_list.jsp?op=setrole">
        <tbody>
        <tr>
            <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
        </tr>
        <%
            RoleMgr roleMgr = new RoleMgr();
            ServerIPPriv lp = new ServerIPPriv();
            Vector vrole = lp.getRolesOfServerIP(sid.getInt("id"));

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
        <tr class="row" style="BACKGROUND-COLOR: #ffffff">
            <td align="center"><textarea name=roleDescs cols="45" rows="3"><%=descs%></textarea>
                <input name="roleCodes" value="<%=roleCodes%>" type=hidden>
                <input name="serverIPId" value="<%=serverIPId%>" type=hidden></td>
        </tr>
        <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
            <td style="PADDING-LEFT: 10px"><input name="button2" type="button" class="btn"
                                                  onClick="openWin('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>', 526, 435)"
                                                  value="选择角色">
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input name="Submit3" type="submit" class="btn" value=" 提 交 "></td>
        </tr>
        </tbody>
    </form>
</table>
<%
    String code;
    String desc;
    UserGroupDb ugroup = new UserGroupDb();
    Vector result = ugroup.list();
    Iterator ir = result.iterator();
%>
<br>
<br>
<table class="tabStyle_1 percent60" cellSpacing="0" cellPadding="3" width="50%" align="center">
    <tbody>
    <tr>
        <td class="tabStyle_1_title" noWrap width="26%">用户组</td>
        <td class="tabStyle_1_title" noWrap width="53%">描述</td>
        <td width="21%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
        while (ir.hasNext()) {
            UserGroupDb ug = (UserGroupDb) ir.next();
            code = ug.getCode();
            desc = ug.getDesc();
    %>
    <tr class="highlight">
        <td><%=code%>
        </td>
        <td><%=desc%>
        </td>
        <td align="center">
            <a href="ip_priv_list.jsp?op=add&serverIPId=<%=serverIPId%>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=ServerIPPriv.TYPE_USERGROUP%>">[
                添加 ]</a></td>
    </tr>
    <%}%>
    </tbody>
</table>
<br>
<table class="tabStyle_1 percent60" width="492" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tabStyle_1_title">添加用户</td>
    </tr>
    <form name="form1" action="ip_priv_list.jsp?op=add" method=post>
        <tr>
            <td height="25" align="center">
                用户名：
                <input name="userRealName" value="" readonly>
                <input name="name" value="" type="hidden"><input type=hidden name=type value=1>
                <input type=hidden name=serverIPId value="<%=serverIPId%>">
                &nbsp;&nbsp;<input class="btn" onclick="openWin('../user_sel.jsp', 800, 600)" value="选择用户"
                                   type="button"/>
                &nbsp;&nbsp;
                <input class="btn" type="submit" align="middle" value="确定"/></td>
        </tr>
    </form>
</table>
<br>
</body>
</html>