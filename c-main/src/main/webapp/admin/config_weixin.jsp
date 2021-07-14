<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.jdom.*" %>
<%@ page import="com.redmoon.weixin.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.weixin.mgr.WXBaseMgr" %>
<%@ page import="com.redmoon.weixin.mgr.AgentMgr" %>
<%@ page import="org.json.JSONObject" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>微信配置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    if (op.equals("setAgent")) {
        String r = "";
        try {
            r = AgentMgr.setAgent(application, request);
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }
        JSONObject json = new JSONObject(r);
        if (json.getInt("ret") == 1) {
            out.print(StrUtil.jAlert_Redirect(json.getString("msg"), "提示", "config_weixin.jsp"));
        } else {
            out.print(StrUtil.jAlert_Back(json.getString("msg"), "提示"));
        }
        return;
    }

    Config.reload();
    Config myconfig = Config.getInstance();
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
    <TBODY>
    <TR>
        <TD class="tdStyle_1">微信配置</TD>
    </TR>
    </TBODY>
</TABLE>
<br>
<%
    Element root = myconfig.getRoot();

    String name = "", value = "";
    name = request.getParameter("name");
    if (name != null && !name.equals("")) {
        value = ParamUtil.get(request, "value");
        myconfig.setProperty(name, value);

        if ("isUserIdUseMobile".equals(name)) {
            if ("1".equals(value)) {
                myconfig.setProperty("isUserIdUseEmail", "false");
                myconfig.setProperty("isUserIdUseMobile", "false");
                myconfig.setProperty("isUserIdUseAccount", "false");
            } else if ("2".equals(value)) {
                myconfig.setProperty("isUserIdUseEmail", "true");
                myconfig.setProperty("isUserIdUseMobile", "false");
                myconfig.setProperty("isUserIdUseAccount", "false");
            } else if ("3".equals(value)) {
                myconfig.setProperty("isUserIdUseEmail", "false");
                myconfig.setProperty("isUserIdUseMobile", "true");
                myconfig.setProperty("isUserIdUseAccount", "false");
            } else if ("4".equals(value)) {
                myconfig.setProperty("isUserIdUseEmail", "false");
                myconfig.setProperty("isUserIdUseMobile", "false");
                myconfig.setProperty("isUserIdUseAccount", "true");
            }
        }

        Config.reload();
        out.println(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "config_weixin.jsp"));
        return;
    }
%>
<table width="100%" class="tabStyle_1 percent80" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="3" class="tabStyle_1_title">配置管理</td>
    </tr>
    <%
        int k = 0;
        Iterator ir = root.getChildren().iterator();
        String desc = "";
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            name = e.getName();
            if ("agentMenu".equals(name))
                continue;

            String isDisplay = StrUtil.getNullStr(e.getAttributeValue("isDisplay"));
            // System.out.println(getClass() + " name=" + name + " isDisplay=" + isDisplay);
            if ("false".equals(isDisplay)) {
                continue;
            }

            value = e.getValue();
            desc = (String) e.getAttributeValue("desc");
    %>
    <form method="post" id="form<%=k%>" name="form<%=k%>" action='config_weixin.jsp'>
        <tr>
            <td width='52%'><input type="hidden" name="name" value="<%=name%>"/>
                &nbsp;<%=desc%>
            </td>
            <td width='34%'>
                <%if (!"isSyncWxToOA".equals(name) && !"isUserIdUseMobile".equals(name) && (value.equals("true") || value.equals("false"))) {%>
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
                } else if (name.equals("isUserIdUseMobile")) {
                %>
                <select id="attr<%=k%>" name="value">
                    <option value="1">系统帐户</option>
                    <option value="2">邮箱</option>
                    <option value="3">手机号</option>
                    <option value="4">工号</option>
                </select>
                <script>
                    <%
                    String relateVal = "1";
                    if (myconfig.isUserIdUseEmail())
                        relateVal = "2";
                    else if (myconfig.isUserIdUseMobile())
                        relateVal = "3";
                    else if (myconfig.isUserIdUseAccount())
                        relateVal = "4";
                    %>
                    $('#attr<%=k%>').val('<%=relateVal%>');
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
            <td width="14%" align="center"><input class="btn" type="submit" name='edit'
                                                  value='<lt:Label key="op_modify"/>'/>
            </td>
        </tr>
    </form>
    <%
            k++;
        }
    %>
</table>
<%
    Element agentMenu = root.getChild("agentMenu");
    List<Element> menus = agentMenu.getChildren("item");
    for (Element ele : menus) {
        String agentId = ele.getChild("agentId").getText();
        String agentName = ele.getChild("agentName").getText();
        String logo = ele.getChild("logo").getText();
        String secret = ele.getChild("secret").getText();
        String homeUrl = ele.getChild("homeUrl").getText();
%>
<form id="form<%=agentId%>" method="post" action="config_weixin.jsp?op=setAgent&agentId=<%=agentId%>"
      enctype="multipart/form-data">
    <table width="100%" class="tabStyle_1 percent80" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td colspan="2" class="tabStyle_1_title">应用管理</td>
        </tr>
        <tr>
            <td>应用ID</td>
            <td><input id="agentId" name="agentId" value="<%=agentId%>"/>
                <input id="oldAgentId" name="oldAgentId" type="hidden" value="<%=agentId%>"/></td>
        </tr>
        <tr>
            <td>应用名称</td>
            <td><input id="agentName" name="agentName" value="<%=agentName%>"/></td>
        </tr>
        <tr>
            <td>应用secret</td>
            <td><input id="secret" name="secret" value="<%=secret%>" style="width:400px;"/></td>
        </tr>
        <tr>
            <td>应用主页</td>
            <td><input id="homeUrl" name="homeUrl" value="<%=homeUrl%>" style="width:400px;"/></td>
        </tr>
        <tr>
            <td>应用LOGO</td>
            <td width="84%">
                <input id="media" name="media" type="file"/>
                <%if (!"".equals(logo)) {%>
                LOGO已设置，可以设置应用信息
                <%} else {%>
                请选择图片，需小于1M，类型为jpg或png
                <%}%>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input type="submit" id="btnSetApp" value="确定"/>
            </td>
        </tr>
    </table>
</form>
<%
    }
%>
</body>
</html>