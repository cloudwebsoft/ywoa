<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>选择角色</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="js/bootstrap/css/bootstrap.min.css"/>
    <style>
        .fields-selected {
            width: 100%;
            height: 370px !important;
        }
    </style>
    <script type="text/javascript" src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <%@ include file="inc/nocache.jsp" %>
    <%
        RoleDb rd = new RoleDb();
        Vector v;
        String unitCode = ParamUtil.get(request, "unitCode");
        if (unitCode.equals("") || privilege.isUserPrivValid(request, "admin")) {
            v = rd.list();
        } else {
            v = rd.getRolesOfUnit(unitCode, true);
        }
        // v.add(0, rd.getRoleDb(RoleDb.CODE_MEMBER));

        Iterator ir = v.iterator();
        String options = "";
        String roles = ParamUtil.get(request, "roleCodes");
        String[] fds = roles.split(",");
        int len = fds.length;
        if (roles.equals("")) {
            len = 0; // 当为空时，split所得的数组长度为1
        }
        String[] fdsText = new String[len];
        while (ir.hasNext()) {
            rd = (RoleDb) ir.next();
            boolean isFinded = false;
            for (int i = 0; i < len; i++) {
                if (rd.getCode().equals(fds[i])) {
                    isFinded = true;
                    fdsText[i] = rd.getDesc();
                }
            }
            if (!isFinded) {
                options += "<option value='" + rd.getCode() + "'>" + rd.getDesc() + "</option>";
            }
        }

        String selOptions = "";
        for (int i = 0; i < len; i++) {
            selOptions += "<option value='" + fds[i] + "'>" + rd.getRoleDb(fds[i]).getDesc() + "</option>";
        }
    %>
    <script language="JavaScript">
        function setRoles() {
            var str = "";
            var strText = "";
            var opts = document.getElementById("fieldsSelected").options;
            var len = opts.length;
            for (var i = 0; i < len; i++) {
                if (str == "") {
                    str = opts[i].value;
                    strText = opts[i].text;
                } else {
                    str += "," + opts[i].value;
                    strText += "," + opts[i].text;
                }
            }
            var dlg = window.opener ? window.opener : dialogArguments;
            dlg.setRoles(str, strText);
            window.close();
        }

        function sel() {
            var opts = document.getElementById("fieldsNotSelected").options;
            var len = opts.length;
            var ary = new Array(len);
            for (var i = 0; i < len; i++) {
                ary[i] = "0";
                if (opts[i].selected) {
                    document.getElementById("fieldsSelected").options.add(new Option(opts[i].text, opts[i].value));
                    ary[i] = opts[i].value;
                }
            }
            for (var i = 0; i < len; i++) {
                for (var j = 0; j < len; j++) {
                    if (ary[i] != "0") {
                        try {
                            // 删除项目后，options会变短，因此用异常捕获来防止出错
                            if (opts[j].value == ary[i])
                                opts.remove(j);
                        } catch (e) {
                        }
                    }
                }
            }
        }

        function notsel() {
            var opts = document.getElementById("fieldsSelected").options;
            var len = opts.length;
            var ary = new Array(len);
            for (var i = 0; i < len; i++) {
                ary[i] = "0";
                if (opts[i].selected) {
                    document.getElementById("fieldsNotSelected").options.add(new Option(opts[i].text, opts[i].value));
                    ary[i] = opts[i].value;
                }
            }

            for (var i = 0; i < len; i++) {
                for (var j = 0; j < len; j++) {
                    if (ary[i] != "0") {
                        try {
                            // 删除项目后，options会变短，因此用异常捕获来防止出错
                            if (opts[j].value == ary[i])
                                opts.remove(j);
                        } catch (e) {
                        }
                    }
                }
            }

        }
    </script>
    <META content="Microsoft FrontPage 4.0" name=GENERATOR>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<table width="501" height="293" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
    <tr>
        <td height="23" colspan="3" class="tabStyle_1_title">&nbsp;&nbsp;<span>选择角色</span></td>
    </tr>
    <tr>
        <td width="231" height="22" align="center">已选角色</td>
        <td width="37">&nbsp;</td>
        <td width="231" height="22" align="center">备选角色</td>
    </tr>
    <tr>
        <td height="22" align="left">&nbsp;</td>
        <td>&nbsp;</td>
        <td height="22">
            <div class="form-inline">
                <input type="text" id="role" name="role" style="width: 200px;display: inline !important;" class="form-control" onkeypress="return findRoles()"/>
                <input type="button" value="查找" onclick="findRoles()" class="btn btn-default"/>
            </div>
        </td>
    </tr>
    <tr>
        <td align="right">
            <select id="fieldsSelected" name="fieldsSelected" class="fields-selected" size=15 multiple ondblclick="notsel();">
            <%=selOptions%>
            </select>
        </td>
        <td align="center" valign="middle">
            <input type="button" name="sel" value=" &lt; " onClick="sel()" style="font-family:'宋体'" class="btn">
            <br>
            <br>
            <input type="button" name="notsel" value=" &gt; " onClick="notsel()" style="font-family:'宋体'" class="btn">
        </td>
        <td>
            <select id="fieldsNotSelected" name="fieldsNotSelected" class="fields-selected" size=15 multiple
                    ondblclick="sel();">
                <%=options%>
            </select></td>
    </tr>
    <tr align="center">
        <td height="28" colspan="3"><input class="btn btn-default" type="button" name="okbtn" value=" 确 定 " onClick="setRoles()">
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input class="btn btn-default" type="button" name="cancelbtn" value=" 取 消 " onClick="window.close()">
        </td>
    </tr>
</table>
</body>
<script>
    function findRoles() {
        var obj = o("fieldsNotSelected");
        for (var i = 0; i < obj.options.length; i++) {
            if (obj.options[i].text.indexOf(o("role").value) != -1) {
                obj.options[i].selected = true;
            } else {
                obj.options[i].selected = false;
            }
        }
    }
</script>
</html>
