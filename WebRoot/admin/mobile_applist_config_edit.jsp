<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.account.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.BasicDataMgr" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.basic.SelectMgr" %>
<%@ page import="com.redmoon.oa.basic.SelectDb" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.basic.SelectOptionDb" %>
<%@ page import="com.redmoon.oa.android.system.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>编辑</title>
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
<%
    String id = ParamUtil.get(request, "id");
    String sql = "select * from mobile_app_icon_config where id= " + Integer.parseInt(id);

    JdbcTemplate jt = new JdbcTemplate();
    ResultIterator ri = null;
    ResultRecord rr = null;

    String code = "";
    String name = "";
    int type = -1;
    String imgUrl = "";
    String setTime = "";
    int isMobileStart = -1;
    int isAdd = 0;
    int orders = 0;
    try {
        ri = jt.executeQuery(sql);
        if (ri.hasNext()) {
            rr = (ResultRecord) ri.next();
            code = rr.getString("code");
            name = rr.getString("name");
            type = rr.getInt("type");
            imgUrl = rr.getString("imgUrl");
            setTime = rr.getString("setTime");
            isMobileStart = rr.getInt("isMobileStart");
            isAdd = rr.getInt("is_add");
            orders = rr.getInt("orders");
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        jt.close();
    }
%>
<body>
<form name="form1" action="mobile_applist_config_do.jsp?op=edit&id=<%=Integer.parseInt(id) %>" method="post" onsubmit="return check();">
    <TABLE class=tabStyle_1 cellSpacing=0 cellPadding=0 width="98%">
        <TBODY>
        <TR>
            <TD class=tabStyle_1_title colSpan=4>手机客户端应用修改</TD>
        </TR>
        </TBODY>
        <TR>
            <TD width="22%" align=right>类型</TD>
            <TD align=left width="78%">&nbsp;
                <input id="type_menu" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_MENU%>"
                       onclick="changeType(this);"/>菜单项
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
        <TR id="select_tr">
            <TD width="17%" align=right>选择</TD>
            <TD width="40%" align=left>&nbsp;
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
                <select id="type_module_selected" name="type_module_selected"
                        onchange="chooseModuleType(this.options[this.options.selectedIndex]);" style="display:none">
                    <option value="">请选择模块</option>
                    <%
                        com.redmoon.oa.flow.FormDb fd = new com.redmoon.oa.flow.FormDb();
                        ModuleSetupDb msd = new ModuleSetupDb();
                        sql = "select code from visual_module_setup where is_use=1 order by code";
                        Iterator mir = msd.list(sql).iterator();
                        while (mir.hasNext()) {
                            msd = (ModuleSetupDb) mir.next();
                    %>
                    <option value="<%=msd.getString("code")%>"><%=msd.getString("name")%>
                    </option>
                    <%}%>
                </select>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            <span id="spanIsAdd">
            <input id="isAdd" name="isAdd" type="checkbox" value="1" <%=isAdd == 1 ? "checked" : ""%> title="进入增加界面"/>
            增加  
            </span>
            </TD>
        </TR>
        <TR id="link_tr">
            <TD align=right>链接</TD>
            <TD align=left>&nbsp;
                <input id="link" name="link"/>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </TD>
        </TR>
        <TR>
            <TD width="22%" align=right>名称</TD>
            <TD align=left>&nbsp;&nbsp;<INPUT title="名称" name="name" id="name">&nbsp;&nbsp;<font
                    color="#FF0000">*</font></TD>
        </TR>
        <TR>
            <TD align="right">选择图标</TD>
            <TD align="left">&nbsp;
                <span id="image"></span>
                <br/>
                <input id="imgUrl" name="imgUrl">
                <input name="button" class="btn" type="button" onclick="openWin('mobile_app_icon_sel.jsp', 800, 600)" value="选择"/>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </TD>
        </TR>
        <TR id="isMobileStart_tr">
            <TD width="22%" align=right>手机端能否发起</TD>
            <TD align=left>&nbsp;
                <input id="isMobileStart_no" type="radio" name="isMobileStart" value="0" checked />否
                <input id="isMobileStart_yes" type="radio" name="isMobileStart" value="1" />能
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></TD>
        </TR>
        <tr>
            <td align=right>顺序</td>
            <td>
                &nbsp;&nbsp;<input id="orders" name="orders" value="<%=orders %>"/>
            </td>
        </tr>
    </TABLE>
    <table width="98%" align="center">
        <tr>
            <td width="100%" align="center">
                <input type="submit" value="确定" class="btn"/>&nbsp;&nbsp;
                <input type="button" name="button" value="返回 " class="btn"
                       onclick="window.location.href='mobile_applist_config_list.jsp'"/>
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
            document.getElementById("type_module_selected").style.display = "";
            $('#link_tr').hide();
        } else if (type.id == "type_flow") {
            document.getElementById("type_menu_selected").style.display = "none";
            document.getElementById("type_flow_selected").style.display = "";
            document.getElementById("type_module_selected").style.display = "";
            $('#link_tr').hide();
        }
        else if (type.id == "type_module") {
            document.getElementById("type_menu_selected").style.display = "";
            document.getElementById("type_flow_selected").style.display = "";
            document.getElementById("type_module_selected").style.display = "none";
            $('#link_tr').hide();
        }
        else if (type.id == "type_link") {
            document.getElementById("type_menu_selected").style.display = "";
            document.getElementById("type_flow_selected").style.display = "";
            document.getElementById("type_module_selected").style.display = "none";
            $('#link_tr').show();
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

    $(document).ready(function () {
        var type = <%=type%>;
        var isMobileStart = <%=isMobileStart%>;
        $('#type_module_selected').select2();

        if (type == 1) {
            document.getElementById("type_menu").checked = true;
            document.getElementById("type_menu").disabled = true;
            document.getElementById("type_flow").disabled = true;
            document.getElementById("type_module").disabled = true;
            document.getElementById("type_link").disabled = true;
            document.getElementById("type_flow_selected").style.display = "none";
            document.getElementById("type_menu_selected").value = "<%=code%>";
            // document.getElementById("type_menu_selected").disabled=true;

            // document.getElementById("type_module_selected").style.display = "none";
            $('#type_module_selected').hide();
            // 隐藏对应type_module_selected的select2控件
            $('#select2-type_module_selected-container').parent().parent().parent().hide();

            if (isMobileStart == 0) {
                document.getElementById("isMobileStart_no").checked = true;
            } else if (isMobileStart == 1) {
                document.getElementById("isMobileStart_yes").checked = true;
            }

            $("#spanIsAdd").hide();
            $('#link_tr').hide();
        } else if (type == 2) {
            document.getElementById("type_flow").checked = true;
            document.getElementById("type_flow").disabled = true;
            document.getElementById("type_menu").disabled = true;
            document.getElementById("type_module").disabled = true;
            document.getElementById("type_link").disabled = true;
            document.getElementById("type_menu_selected").style.display = "none";
            document.getElementById("type_flow_selected").style.display = "";
            document.getElementById("type_flow_selected").value = "<%=code%>";
            // document.getElementById("type_flow_selected").disabled=true;
            document.getElementById("isMobileStart_tr").style.display = "";

            // document.getElementById("type_module_selected").style.display = "none";
            $('#type_module_selected').hide();
            // 隐藏对应type_module_selected的select2控件
            $('#select2-type_module_selected-container').parent().parent().parent().hide();

            $("#spanIsAdd").hide();
            $('#link_tr').hide();
        }
        else if (type == 3) {
            document.getElementById("type_module").checked = true;
            document.getElementById("type_flow").disabled = true;
            document.getElementById("type_menu").disabled = true;
            document.getElementById("type_module").disabled = true;
            document.getElementById("type_link").disabled = true;
            document.getElementById("type_menu_selected").style.display = "none";
            document.getElementById("type_flow_selected").style.display = "none";
            document.getElementById("type_module_selected").style.display = "";
            // document.getElementById("type_module_selected").disabled=true;
            document.getElementById("type_module_selected").value = "<%=code%>";
            document.getElementById("isMobileStart_tr").style.display = "";
            $('#link_tr').hide();
        }
        else if (type == 4) {
            document.getElementById("type_link").checked = true;
            document.getElementById("type_module").disabled = true;
            document.getElementById("type_flow").disabled = true;
            document.getElementById("type_menu").disabled = true;
            document.getElementById("type_menu_selected").style.display = "none";
            document.getElementById("type_flow_selected").style.display = "none";
            document.getElementById("type_module_selected").style.display = "";
            // document.getElementById("type_module_selected").disabled=true;
            document.getElementById("link").value = "<%=code%>";
            document.getElementById("isMobileStart_tr").style.display = "";
            $('#select_tr').hide();
        }

        document.getElementById("name").value = "<%=name%>";
        document.getElementById("imgUrl").value = "<%=imgUrl%>";
        document.getElementById("image").innerHTML = "<img class='icon' src='<%=request.getContextPath()%>/<%=imgUrl%>'>";
    });
</script>
