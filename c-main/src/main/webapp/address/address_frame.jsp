<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.address.*" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>通讯录框架</title>
</head>
<%
    String type = ParamUtil.get(request, "type");
    String mode = ParamUtil.get(request, "mode");
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "type", type, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    String userName = privilege.getUser(request);
    if (type.equals(String.valueOf(AddressDb.TYPE_PUBLIC))) {
        userName = AddressTypeDb.PUBLIC;
    }
    String root_code = ParamUtil.get(request, "root_code");
    if (root_code.equals("")) {
        root_code = userName;
    }

    String unitCode = privilege.getUserUnitCode(request);
    if (type.equals(String.valueOf(AddressDb.TYPE_PUBLIC))) {
        unitCode = Leaf.CODE_ROOT;
    }

    Directory dir = new Directory();
    Leaf leaf = dir.getLeaf(root_code);
    if (leaf == null) {
        Leaf.initRootOfUser(root_code, unitCode);
    }
%>
<frameset rows="*" cols="220,*" framespacing="2" border="0">
    <frame src="address_left.jsp?type=<%=type%>&mode=<%=mode%>" name="leftAddressFrame">
    <frame src="list.do?type=<%=type%>&mode=<%=mode%>" name="mainAddressFrame">
    <%--
      <frame src="address.jsp?type=<%=type%>&mode=<%=mode%>" name="mainAddressFrame">
    --%>
</frameset>
<noframes>
    <body>
    </body>
</noframes>
</html>
