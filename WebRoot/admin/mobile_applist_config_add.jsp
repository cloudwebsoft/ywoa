<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.account.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.BasicDataMgr" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.basic.SelectMgr" %>
<%@ page import="com.redmoon.oa.basic.SelectDb" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.basic.SelectOptionDb" %>
<%@ page import="com.redmoon.oa.android.system.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <title>添加</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .icon {
            width: 105px;
            height: 105px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
    <script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
    <script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
</head>
<body>
<form name="form1" action="mobile_applist_config_do.jsp?op=add" method="post" onsubmit="return check();">
    <table class=tabStyle_1 cellspacing=0 cellpadding=0 width="98%">
        <tbody>
        <TR>
            <TD class=tabStyle_1_title colspan=4>手机客户端应用增加</TD>
        </TR>
        </tbody>
        <TR>
            <TD width="22%" align=right>类型</TD>
            <TD width="78%" align=left>&nbsp;
                <input id="type_menu" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_MENU%>"
                       onclick="changeType(this);" checked/>菜单项
                <input id="type_flow" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_FLOW%>"
                       onclick="changeType(this);"/>流程项
                <input id="type_module" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_MODULE%>"
                       onclick="changeType(this);"/>
                模块项
                <input id="type_link" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_LINK%>"
                       onclick="changeType(this);"/>
                链接项
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></TD>
        </TR>
        <TR id="type_tr">
            <TD align=right>选择</TD>
            <TD align=left>&nbsp;
                <select id="type_menu_selected" name="type_menu_selected"
                        onchange="chooseMenuType(this.options[this.options.selectedIndex]);">
                    <option value="" selected="selected">请选择</option>
                    <option value="qrcode">扫码</option>
                    <% out.print(MobileAppIconConfigMgr.ShowMenuAsOption(request));%>
                </select>
                <select id="type_flow_selected" name="type_flow_selected"
                        onchange="chooseFlowType(this.options[this.options.selectedIndex]);" style="display:none">
                    <option value="" selected="selected">请选择流程</option>
                    <% out.print(MobileAppIconConfigMgr.ShowFlowAsOption(request));%>
                </select>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </TD>
        </TR>
        <TR id="module_tr" style="display:none;">
            <TD align=right>选择</TD>
            <TD align=left>
                &nbsp;
                <select id="moduleCode" name="moduleCode" style="width:230px"
                        onchange="chooseModuleType(this.options[this.options.selectedIndex]);">
                    <option value="">请选择模块</option>
                    <%
                        ModuleSetupDb msd = new ModuleSetupDb();
                        String sql = "select code from visual_module_setup where is_use=1 order by code";
                        Iterator mir = msd.list(sql).iterator();
                        while (mir.hasNext()) {
                            msd = (ModuleSetupDb) mir.next();
                    %>
                    <option value="<%=msd.getString("code")%>"><%=msd.getString("name")%>
                    </option>
                    <%}%>
                </select>
                <font color="#FF0000">*</font>
                <script>
                    $(function() {
                        $('#moduleCode').select2();
                    })
                </script>
                <input id="isAdd" name="isAdd" type="checkbox" value="1" title="进入增加界面"/>
                增加
            </TD>
        </TR>
        <TR id="link_tr" style="display:none">
            <TD align=right>链接</TD>
            <TD align=left>&nbsp;
                <input id="link" name="link"/>
                &nbsp;&nbsp;<font color="#FF0000">*</font>
            </TD>
        </TR>
        <TR>
            <TD align=right>名称</TD>
            <TD align=left>&nbsp;&nbsp;<input title=名称 name="name" id="name"> &nbsp;&nbsp;<font color="#FF0000">*</font>
            </TD>
        </TR>
        <TR>
            <TD align="right">图标</TD>
            <TD align="left">&nbsp;
                <span id="image"></span>
                <br/>
                <input id="imgUrl" name="imgUrl"/>
                <input name="button" class="btn" type="button" onclick="openWin('mobile_app_icon_sel.jsp', 800, 600)" value="选择"/>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </TD>
        </TR>
        <TR id="isMobileStart_tr">
            <TD align=right>手机端能否发起</TD>
            <TD align=left>&nbsp;
                <input id="isMobileStart_no" type="radio" name="isMobileStart" value=0 checked/>否
                <input id="isMobileStart_yes" type="radio" name="isMobileStart" value=1/>能
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></TD>
        </TR>
        <tr>
            <td align=right>顺序</td>
            <td>
                &nbsp;&nbsp;<input id="orders" name="orders" value="0"/>
            </td>
        </tr>
    </TABLE>
    <table width="98%" align="center">
        <tr>
            <td width="100%" align="center">
                <input type="submit" value="确定" class="btn"/>&nbsp;&nbsp;
                <input type="button" name="button" value="返回 " class="btn" onclick="javascript:history.go(-1)"/>
            </td>
        </tr>
    </table>
</form>
</body>
</html>
<script>
    function changeType(type) {
        document.getElementById("name").value = "";
        if (type.id == "type_menu") {
            document.getElementById("type_menu_selected").style.display = "";
            document.getElementById("type_flow_selected").style.display = "none";
            document.getElementById("isMobileStart_tr").style.display = "";
            document.getElementById("module_tr").style.display = "none";
            document.getElementById("type_tr").style.display = "";
            document.getElementById("module_tr").style.display = "none";
        } else if (type.id == "type_flow") {
            document.getElementById("type_menu_selected").style.display = "none";
            document.getElementById("type_flow_selected").style.display = "";
            document.getElementById("isMobileStart_tr").style.display = "";
            document.getElementById("module_tr").style.display = "none";
            document.getElementById("type_tr").style.display = "";
            document.getElementById("module_tr").style.display = "none";
            $("#link_tr").hide();
        } else if (type.id == "type_module") {
            document.getElementById("isMobileStart_tr").style.display = "";
            document.getElementById("module_tr").style.display = "";
            document.getElementById("type_tr").style.display = "none";
            $("#link_tr").hide();
        }
        else if (type.id == "type_link") {
            document.getElementById("isMobileStart_tr").style.display = "";
            document.getElementById("module_tr").style.display = "none";
            document.getElementById("type_tr").style.display = "none";
            $("#link_tr").show();
        }
    }

    function chooseMenuType(type) {
        document.getElementById("name").value = type.id;
    }

    function chooseFlowType(type) {
        if (type.value.indexOf("cannot_selected_") == 0) {
            alert(type.id + ' 不能被选择！', '请选择具体事项');
            document.getElementById("name").value = "";
            document.getElementById("type_flow_selected").options.selectedIndex = 0;
            return false;
        } else {
            document.getElementById("name").value = type.id;
        }
    }

    function chooseModuleType(type) {
        document.getElementById("name").value = type.text;
    }

    function check() {
        var radios = document.getElementsByName("type");
        var type;

        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked == true) {
                type = radios[i];
                break;
            }
        }

        if (type.id == "type_menu") {
            if (document.getElementById("type_menu_selected").value == "") {
                alert("选择项不能为空");
                return false;
            }
        } else if (type.id == "type_flow") {
            if (document.getElementById("type_flow_selected").value == "") {
                alert("选择项不能为空");
                return false;
            }
        }
        else if (type.id == "type_link") {
            if ($('#link').val() == "") {
                alert("链接不能为空");
                return false;
            }
        }

        if (document.getElementById("name").value == "") {
            alert("名称不能为空");
            return false;
        }
        ;

        if (document.getElementById("imgUrl").value == "") {
            alert("图标路径不能为空");
            return false;
        }
        ;
    }

    function selIcon(icon) {
        document.getElementById("imgUrl").value = "images/mobileAppIcons/" + icon;
        document.getElementById("image").innerHTML = "<img class='icon' src='<%=request.getContextPath()%>/images/mobileAppIcons/" + icon + "'>";
    }
</script>
