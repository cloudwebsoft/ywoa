<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>添加文件权限</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script>
        var selUserNames = "";
        var selUserRealNames = "";

        function getSelUserNames() {
            return selUserNames;
        }

        function getSelUserRealNames() {
            return selUserRealNames;
        }

        function setUsers(user, userRealName) {
            form1.name.value = user;
            form1.userRealName.value = userRealName;
        }

        function setRoles(roles, descs) {
            formRole.roleCodes.value = roles;
            formRole.roleDescs.value = descs
        }

        function openWinUsers() {
            selUserNames = form1.name.value;
            selUserRealNames = form1.userRealName.value;
            openWin('../user_multi_sel.jsp', 800, 600);
        }

        function openWinDepts(formDept) {
            openWin('../dept_multi_sel.jsp', 800, 600);
        }

        function setDepts(formDept) {
            var strdepts = "";
            var strDeptNames = "";
            for (var i = 0; i < formDept.length; i++) {
                strdepts += formDept[i][0] + ",";
                strDeptNames += formDept[i][1] + ",";
            }
            strdepts = strdepts.substring(0, strdepts.length - 1);
            strDeptNames = strDeptNames.substring(0, strDeptNames.length - 1);
            o("deptCodes").value = strdepts;
            o("deptNames").value = strDeptNames;
        }

        function getDepts() {
            return o("deptCodes").value;
        }
    </script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String docId = ParamUtil.get(request, "id");
    int doc_id = Integer.valueOf(docId);
    Document doc = new Document();
    doc = doc.getDocument(doc_id);
    String title = doc.getTitle();
%>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">设置文件 <a href="doc_priv_m.jsp?doc_id=<%=doc_id %>"><%=title%>
        </a> 权限
        </td>
    </tr>
    </tbody>
</table>
<br>
<form name="formRole" method="post" action="doc_priv_m.jsp?op=setrole">
<table class="tabStyle_1 percent60" cellspacing="0" cellpadding="3" width="50%" align="center">
        <tbody>
        <tr>
            <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
        </tr>
        <%
            DocPriv lp = new DocPriv();
            Vector vrole = lp.getRolesOfDocPriv(doc_id);

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
            <td align="center"><textarea name=roleDescs cols="45" rows="3" style="width: 100%;"><%=descs%></textarea>
                <input name="roleCodes" value="<%=roleCodes%>" type=hidden>
                <input name="doc_id" value="<%=doc_id%>" type=hidden>
                <input name="title" value="<%=title%>" type=hidden></td>
        </tr>
        <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
            <td style="PADDING-LEFT: 10px"><input name="button2" type="button" class="btn" onClick="openWin('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>', 800, 600)" value="选择">
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="submit" class="btn" value="确定"></td>
        </tr>
        </tbody>
</table>
</form>
<br>
<%
    Vector<DeptDb> vDept = lp.getDeptsOfDocPriv(doc_id);
    String strdepts = "";
    String strDeptNames = "";
    DeptDb ddb = new DeptDb();
    Iterator<DeptDb> irDept = vDept.iterator();
    while (irDept.hasNext()) {
        ddb = (DeptDb) irDept.next();
        if (strdepts.equals("")) {
            strdepts = ddb.getCode();
            strDeptNames = ddb.getName();
        } else {
            strdepts += "," + ddb.getCode();
            strDeptNames += "," + ddb.getName();
        }
    }
%>
<form action="doc_priv_m.jsp?op=setDept" method="post" name="formDept" id="formDept">
    <table class="tabStyle_1 percent60" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="center" class="tabStyle_1_title">属于部门</td>
        </tr>
        <tr>
            <td align="center">
                <input type="hidden" name="deptCodes" value="<%=strdepts%>"/>
                <input name="doc_id" value="<%=doc_id%>" type=hidden>
                <textarea rows="3" readonly wrap="yes" style="width:100%" id="deptNames"><%=strDeptNames%></textarea>
            </td>
        </tr>
        <tr>
            <td align="center"><input class="btn" title="选择部门" onclick="openWinDepts(formDept)" type="button" value="选择" name="button3"/>
                &nbsp;&nbsp;
                <input class="btn" name="submit2" type="submit" value="确 定"/></td>
        </tr>
    </table>
</form>
<%
    String code;
    String desc;
    UserGroupDb ugroup = new UserGroupDb();
    Vector result = ugroup.list();
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
            code = ug.getCode();
            desc = ug.getDesc();
    %>
    <tr class="highlight">
        <td><%=code%>
        </td>
        <td><%=desc%>
        </td>
        <td align="center">
            <a href="doc_priv_m.jsp?op=add&title=<%=StrUtil.UrlEncode(title) %>&doc_id=<%=doc_id %>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=DocPriv.TYPE_USERGROUP%>">[ 添加 ]</a></td>
    </tr>
    <%}%>
    </tbody>
</table>
<br>
<form name="form1" action="doc_priv_m.jsp?op=add" method=post>
<table class="tabStyle_1 percent60" width="492" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tabStyle_1_title">用户</td>
    </tr>
        <tr>
            <td height="25" align="center">
                用户名：
                <input name="userRealName" value="" readonly>
                <input name="name" value="" type="hidden"><input type=hidden name=type value=1>
                <input type="hidden" name="doc_id" value="<%=doc_id %>">
                <input type="hidden" name="title" value="<%=title %>">
                &nbsp;&nbsp;<input class="btn" onClick="openWinUsers()" value="选择" type="button"/>
                &nbsp;&nbsp;
                <input class="btn" type="submit" align="middle" value="确定"/></td>
        </tr>
</table>
</form>
<br>
</body>
</html>