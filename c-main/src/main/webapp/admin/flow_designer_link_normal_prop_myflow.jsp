<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <TITLE>流程连接线属性</TITLE>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <%
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    %>
    <script src="../inc/common.js"></script>
    <script language="JavaScript">
        function openWin(url, width, height) {
            var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
        }

        function ModifyLink() {
            if (o("expireHour").value.trim() != "") {
                if (!isNumeric(o("expireHour").value.trim())) {
                    alert("到期时间必须为大于0的数字！");
                    return;
                }
                if (o("expireHour").value < 0) {
                    alert("到期时间必须为大于或等于0的数字！");
                    return;
                }
            }
            window.parent.clearSelectedLinkProperty();
            window.parent.SetSelectedLinkProperty("expireHour", o("expireHour").value);
            window.parent.SetSelectedLinkProperty("expireAction", o("expireAction").value);

            window.parent.submitDesigner();
        }

        function window_onload() {
            o("expireHour").value = window.parent.GetSelectedLinkProperty("expireHour");
            o("expireAction").value = window.parent.GetSelectedLinkProperty("expireAction");
        }
    </script>
</head>
<body onLoad="window_onload()" style="padding:0px; margin:0px">
<table class="tabStyle_1" style="margin:0px;margin-top:3px;padding:0px; width:100%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td height="22" colspan="2" align="center" style="border-left:0px"><input name="okbtn" type="button" class="btn" onclick="ModifyLink()" value=" 保存 "/></td>
    </tr>
    <tr>
        <td width="78" style="border-left:0px" height="22" align="center">到期</td>
        <td height="22" style="border-right:0px"><input title="下一节点用户处理的到期时间" type="text" name="expireHour" style="width: 60px" value="0">
            <%
                Config cfg = new Config();
                String flowExpireUnit = cfg.get("flowExpireUnit");
                if (flowExpireUnit.equals("day")) {
                    out.print("天");
                } else {
                    out.print("小时");
                }
            %>
            (0表示不限时)&nbsp;
        </td>
    </tr>
    <tr>
        <td height="22" align="center">&nbsp;超期</td>
        <td height="22" style="border-right:0px"><select name="expireAction">
            <option value="">等待</option>
            <option value="next">交办至后续节点</option>
            <option value="starter">返回给发起人</option>
        </select></td>
    </tr>
</table>
</body>
</html>
