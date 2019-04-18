<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.jdom.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.security.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>安全配置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ include file="config_m_inc_menu_top.jsp" %>
<div class="spacerH"></div>
<script>
    o("menu6").className = "current";
</script>
<%
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    Config.getInstance().refresh();
    Config myconfig = Config.getInstance();
    Element root = myconfig.getRoot();

    String name = "", value = "";
    name = request.getParameter("name");
    if (name != null && !name.equals("")) {
        value = ParamUtil.get(request, "value");
        myconfig.setProperty(name, value);

        myconfig.refresh();
        out.println(fchar.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "config_security.jsp"));
        return;
    }
%>
<table width="100%" class="tabStyle_1 percent80" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="3" class="tabStyle_1_title">安全配置信息</td>
    </tr>
    <%
        int k = 0;
        Iterator ir = root.getChildren().iterator();
        String desc = "";
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            name = e.getName();

            String isDisplay = StrUtil.getNullStr(e.getAttributeValue("isDisplay"));
            if (isDisplay.equals("false")) {
                continue;
            }

            value = e.getValue();
            desc = (String) e.getAttributeValue("desc");
            List list = e.getChildren();
            if (list.size() > 0) {
                Iterator irChild = list.iterator();
                while (irChild.hasNext()) {
                    Element eChild = (Element) irChild.next();
                    String childName = eChild.getName();
                    childName = name + "." + childName;
                    isDisplay = StrUtil.getNullStr(eChild.getAttributeValue("isDisplay"));
                    if (isDisplay.equals("false")) {
                        continue;
                    }
                    value = eChild.getValue();
                    desc = (String) eChild.getAttributeValue("desc");
    %>
    <form method="post" id="form<%=k%>" name="form<%=k%>" action='config_security.jsp'>
        <tr>
            <td width='52%'><input type="hidden" name="name" value="<%=childName%>"/>
                &nbsp;<%=desc%>
            </td>
            <td width='34%'>
                <%if (value.equals("true") || value.equals("false")) {%>
                <select id="attr<%=k%>" name="value">
                    <option value="true">
                        <lt:Label key="yes"/>
                    </option>
                    <option value="false">
                        <lt:Label key="no"/>
                    </option>
                </select>
                <script>
                    $('#attr<%=k%>').val("<%=value%>");
                </script>
                <%
                } else {
                    String opts = StrUtil.getNullStr(eChild.getAttributeValue("options"));
                    if ("".equals(opts)) {
                %>
                <input type=text value="<%=value%>" name="value" size=30>
                <%
                } else {
                %>
                <select id="attr<%=k%>" name="value">
                    <%
                        String[] ary = StrUtil.split(opts, ",");
                        for (String item : ary) {
                            String[] aryOpts = StrUtil.split(item, "\\|");
                            if (aryOpts != null && aryOpts.length == 2) {
                    %>
                    <option value="<%=aryOpts[0]%>"><%=aryOpts[1]%>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>
                <script>
                    $(function () {
                        $('#attr<%=k%>').val('<%=value%>');
                    })
                </script>
                <%
                        }
                    }
                %></td>
            <td width="14%" align="center"><input class="btn" type="submit" name='edit' value='<lt:Label key="op_modify"/>'/>
            </td>
        </tr>
    </form>
    <%
                    k++;
                }
            } else {
    %>
    <form method="post" id="form<%=k%>" name="form<%=k%>" action='config_security.jsp'>
        <tr>
            <td width='52%'><input type="hidden" name="name" value="<%=name%>"/>
                &nbsp;<%=desc%>
            </td>
            <td width='34%'>
                <%if (value.equals("true") || value.equals("false")) {%>
                <select id="attr<%=k%>" name="value">
                    <option value="true">
                        <lt:Label key="yes"/>
                    </option>
                    <option value="false">
                        <lt:Label key="no"/>
                    </option>
                </select>
                <script>
                    $('#attr<%=k%>').val("<%=value%>");
                </script>
                <%
                } else if ("isPwdCanReset".equals(name)) {
                %>
                <select id="attr<%=k%>" name="value">
                    <option value="1">
                        是
                    </option>
                    <option value="0">
                        否
                    </option>
                </select>
                <script>
                    $('#attr<%=k%>').val("<%=value%>");
                </script>
                <%
                } else {
                    String opts = StrUtil.getNullStr(e.getAttributeValue("options"));
                    if ("".equals(opts)) {
                %>
                <input type=text value="<%=value%>" name="value" size=30>
                <%
                } else {
                %>
                <select id="attr<%=k%>" name="value">
                    <%
                        String[] ary = StrUtil.split(opts, ",");
                        for (String item : ary) {
                            String[] aryOpts = StrUtil.split(item, "\\|");
                            if (aryOpts != null && aryOpts.length == 2) {
                    %>
                    <option value="<%=aryOpts[0]%>"><%=aryOpts[1]%>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>
                <script>
                    $(function () {
                        $('#attr<%=k%>').val('<%=value%>');
                    })
                </script>
                <%
                        }
                    }
                %></td>
            <td width="14%" align="center"><input class="btn" type="submit" name='edit' value='<lt:Label key="op_modify"/>'/>
            </td>
        </tr>
    </form>
    <%
                k++;
            }
        }
    %>
</table>
</body>
</html>