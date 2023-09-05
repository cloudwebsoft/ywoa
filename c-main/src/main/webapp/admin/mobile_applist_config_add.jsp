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
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>添加</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .icon-box {
            margin-left: 10px;
            margin-bottom: 10px;
        }
        .icon {
            width: 1em;
            height: 1em;
            vertical-align: -0.15em;
            fill: currentColor;
            overflow: hidden;
            font-size: 42px;
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
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../fonts/mobile/iconfont.js"></script>
</head>
<body>
<table cellSpacing=0 cellPadding=0 width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">手机客户端应用</td>
    </tr>
    </tbody>
</table>
<br/>
<form id="form1" name="form1" method="post">
    <table class="tabStyle_1 percent80" cellspacing=0 cellpadding=0 width="98%">
        <tbody>
        <tr>
            <td class=tabStyle_1_title colspan=4>增加应用</td>
        </tr>
        </tbody>
        <tr>
            <td width="22%" align=right>类型</td>
            <td width="78%" align=left>&nbsp;
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
                <span style="display: none">
                <input id="type_front" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_FRONT%>"
                       onclick="changeType(this);"/>
                前端项
                </span>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </td>
        </tr>
        <tr id="type_tr">
            <td align=right>选择</td>
            <td align=left>&nbsp;
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
            </td>
        </tr>
        <tr id="module_tr" style="display:none;">
            <td align=right>选择</td>
            <td align=left>
                &nbsp;
                <select id="moduleCode" name="moduleCode" style="width:230px"
                        onchange="chooseModuleType(this.options[this.options.selectedIndex]);">
                    <option value="">请选择模块</option>
                    <%
                        ModuleSetupDb msd = new ModuleSetupDb();
                        Iterator mir = msd.listUsed().iterator();
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
            </td>
        </tr>
        <tr id="link_tr" style="display:none">
            <td align=right>链接</td>
            <td align=left>&nbsp;
                <input id="link" name="link"/>
                &nbsp;&nbsp;<font color="#FF0000">*</font>
            </td>
        </tr>
        <tr>
            <td align=right>名称</td>
            <td align=left>&nbsp;&nbsp;<input title=名称 name="name" id="name"> &nbsp;&nbsp;<font color="#FF0000">*</font>
            </td>
        </tr>
        <tr>
            <td align="right">图片型图标</td>
            <td align="left">&nbsp;
                <span id="image"></span>
                <br/>
                &nbsp;&nbsp;<input id="imgUrl" name="imgUrl"/>
                <input name="button" class="btn" type="button" onclick="openWin('mobile_app_icon_sel.jsp', 800, 600)" value="选择"/>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </td>
        </tr>	
        <tr style="display: none">
            <td align="right">矢量型图标</td>
            <td align="left">&nbsp;
                <div id="iconBox" class="icon-box">
                    <svg class="icon svg-icon" aria-hidden="true">
                        <use id="useFontIcon" xlink:href=""></use>
                    </svg>
                </div>
                &nbsp;&nbsp;<input id="icon" name="icon"/>
                <input name="button" class="btn" type="button" onclick="openWin('../fonts/font.jsp?kind=mobile', 800, 600)" value="选择"/>
                &nbsp;&nbsp;<font color="#FF0000">*</font>&nbsp;( 用于5+App，如不设置，则显示图片型图标 )
                <script>
                    function setFontIcon(fontIcon) {
                        $('#useFontIcon').attr('xlink:href', fontIcon);
                        $('#icon').val(fontIcon);
                    }
                </script>
            </td>
        </tr>
        <tr id="isMobileStart_tr">
            <td align=right>手机端能否快速添加</td>
            <td align=left>&nbsp;
                <input id="isMobileStart_no" type="radio" name="isMobileStart" value="0" checked/>否
                <input id="isMobileStart_yes" type="radio" name="isMobileStart" value="1"/>能
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></td>
        </tr>
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
                <input id="btnOk" type="button" value="确定" class="btn"/>&nbsp;&nbsp;
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
        else if (type.id == "type_link" || type.id == 'type_front') {
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

    $(function() {
       $('#btnOk').click(function(e) {
           e.preventDefault();

           if (!check()) {
               return;
           }

           $.ajax({
               type: "post",
               url: "createMobileAppIcon.do",
               contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
               data: $('#form1').serialize(),
               dataType: "html",
               beforeSend: function (XMLHttpRequest) {
                   $('body').showLoading();
               },
               success: function (data, status) {
                   data = $.parseJSON(data);
                   if (data.ret == "0") {
                       jAlert(data.msg, "提示");
                   } else {
                       jAlert_Redirect(data.msg, "提示", "mobile_applist_config_list.jsp");
                   }
               },
               complete: function (XMLHttpRequest, status) {
                   $('body').hideLoading();
               },
               error: function (XMLHttpRequest, textStatus) {
                   // 请求出错处理
                   alert(XMLHttpRequest.responseText);
               }
           });
       })
    });

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
                jAlert("选择项不能为空", "提示");
                return false;
            }
        } else if (type.id == "type_flow") {
            if (document.getElementById("type_flow_selected").value == "") {
                jAlert("选择项不能为空", "提示");
                return false;
            }
        }
        else if (type.id == "type_link" || type.id == 'type_front') {
            if ($('#link').val() == "") {
                jAlert("链接不能为空", "提示");
                return false;
            }
        }

        if (document.getElementById("name").value == "") {
            jAlert("名称不能为空", "提示");
            return false;
        }

        if (document.getElementById("imgUrl").value == "") {
            jAlert("图标路径不能为空", "提示");
            return false;
        }

        return true;
    }

    function selIcon(icon) {
        document.getElementById("imgUrl").value = "images/mobileAppIcons/" + icon;
        document.getElementById("image").innerHTML = "<img class='icon' src='<%=request.getContextPath()%>/static/images/mobileAppIcons/" + icon + "'>";
    }
</script>
