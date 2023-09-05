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
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <script src="../js/layui/layui.js" charset="utf-8"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
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
            String name = e.getName();
            if ("agentMenu".equals(name)) {
                continue;
            }

            String isDisplay = StrUtil.getNullStr(e.getAttributeValue("isDisplay"));
            // System.out.println(getClass() + " name=" + name + " isDisplay=" + isDisplay);
            if ("false".equals(isDisplay)) {
                continue;
            }

            String value = e.getValue();
            desc = e.getAttributeValue("desc");
    %>
    <form method="post" id="form<%=k%>" name="form<%=k%>">
        <tr>
            <td width='52%'><input type="hidden" name="name" value="<%=name%>"/>
                &nbsp;<%=desc%>
            </td>
            <td width='34%'>
                <%if (!"isSyncWxToOA".equals(name) && !"isUserIdUseMobile".equals(name) && (value.equals("true") || value.equals("false"))) {%>
                <select id="attr<%=k%>" name="value">
                    <option value="true">
                        是
                    </option>
                    <option value="false">
                        否
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
            <td width="14%" align="center">
                <input class="btn" type="button" name='edit' value='修改' onclick="handleSumbit('form<%=k%>')"/>
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
                <input class="btn" type="button" id="btnSetApp" value="确定" onclick="handleSumbit('form<%=agentId%>', true)"/>
            </td>
        </tr>
    </table>
</form>
<%
    }
%>
<script>
    function handleSumbit(formId, isSetAgent) {
        var params = $("#" + formId).serialize();
        if (isSetAgent) {
            var formData = new FormData($('#' + formId)[0]);
            $.ajax({
                url: "../wx/admin/configAgent",
                dataType: 'json',// 服务器返回json格式数据
                type: 'post',// HTTP请求类型
                data: formData,
                processData: false,
                contentType: false,
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                success: function (data) {
                    layer.msg(data.msg, {
                        offset: '6px'
                    });
                },
                error: function (xhr, type, errorThrown) {
                    console.log(type);
                }
            });
            return;
        }

        $.ajax({
            type: "post",
            url: "../wx/admin/configWxWork",
            contentType:"application/x-www-form-urlencoded; charset=UTF-8",
            data: params,
            dataType: "json",
            beforeSend: function(XMLHttpRequest){
                $('body').showLoading();
            },
            success: function(data, status) {
                layer.msg(data.msg, {
                    offset: '6px'
                });
            },
            complete: function(XMLHttpRequest, status){
                $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }
</script>
</body>
</html>