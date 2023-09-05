<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.android.system.MobileAppIconConfigDb" %>
<%@ page import="com.redmoon.oa.android.system.MobileAppIconConfigMgr" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>编辑</title>
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
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp"></script>
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
<%
    long id = ParamUtil.getLong(request, "id", -1);
    if (id == -1) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
        return;
    }

    MobileAppIconConfigDb mobileAppIconConfigDb = new MobileAppIconConfigDb();
    mobileAppIconConfigDb = mobileAppIconConfigDb.getMobileAppIconConfigDb(id);
    if (mobileAppIconConfigDb == null) {
        out.print(SkinUtil.makeErrMsg(request, "记录不存在"));
        return;
    }

    String tabIdOpener = ParamUtil.get(request, "tabIdOpener");

    String code = "";
    String name = "";
    int type = -1;
    String imgUrl = "", icon = "";
    int isMobileStart = -1;
    int isAdd = 0;
    int orders = 0;

    code = mobileAppIconConfigDb.getString("code");
    name = mobileAppIconConfigDb.getString("name");
    type = mobileAppIconConfigDb.getInt("type");
    imgUrl = mobileAppIconConfigDb.getString("imgUrl");
    isMobileStart = mobileAppIconConfigDb.getInt("isMobileStart");
    isAdd = mobileAppIconConfigDb.getInt("is_add");
    orders = mobileAppIconConfigDb.getInt("orders");
    icon = StrUtil.getNullStr(mobileAppIconConfigDb.getString("icon"));
%>
<body>
<table cellSpacing=0 cellPadding=0 width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">手机客户端应用</td>
    </tr>
    </tbody>
</table>
<br>
<form id="form1" name="form1" method="post">
    <table class="tabStyle_1 percent80" cellSpacing=0 cellPadding=0 width="98%">
        <tbody>
        <tr>
            <td class=tabStyle_1_title colSpan=4>修改应用</td>
        </tr>
        </tbody>
        <tr>
            <td width="22%" align=right>类型</td>
            <td align=left width="78%">&nbsp;
                <input id="type_menu" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_MENU%>" onclick="changeType(this);"/>菜单项
                <input id="type_flow" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_FLOW%>" onclick="changeType(this);"/>流程项
                <input id="type_module" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_MODULE%>" onclick="changeType(this);"/>模块项
                <input id="type_link" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_LINK%>" onclick="changeType(this);"/>链接项
                <span style="display: none">
                    <input id="type_front" type="radio" name="type" value="<%=MobileAppIconConfigDb.TYPE_FRONT%>" onclick="changeType(this);"/>前端项
                </span>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></td>
        </tr>
        <tr id="select_tr">
            <td width="17%" align=right>选择</td>
            <td width="40%" align=left>&nbsp;
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
                        Iterator mir = msd.listUsed().iterator();
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
            </td>
        </tr>
        <tr id="link_tr">
            <td align=right>链接</td>
            <td align=left>&nbsp;
                <input id="link" name="link"/>
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </td>
        </tr>
        <tr>
            <td width="22%" align=right>名称</td>
            <td align=left>&nbsp;&nbsp;<INPUT title="名称" name="name" id="name">&nbsp;&nbsp;<font
                    color="#FF0000">*</font></td>
        </tr>
	<tr>
            <td align="right">图片型图标</td>
            <td align="left">&nbsp;
                <span id="image"></span>
                <br/>
                &nbsp;&nbsp;<input id="imgUrl" name="imgUrl" value="<%=imgUrl%>">
                <input name="button" class="btn" type="button" onclick="openWin('mobile_app_icon_sel.jsp', 800, 600)" value="选择"/>
                &nbsp;&nbsp;<font color="#FF0000">*</font>
            </td>
        </tr>
        <tr style="display: none">
            <td align="right">矢量型图标</td>
            <td align="left">&nbsp;
                <div id="iconBox" class="icon-box">
                    <svg class="icon svg-icon" aria-hidden="true">
                        <use id="useFontIcon" xlink:href="<%=icon%>"></use>
                    </svg>
                </div>
                &nbsp;&nbsp;<input id="icon" name="icon" value="<%=icon%>"/>
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
            <td width="22%" align=right>手机端能否快速添加</td>
            <td align=left>&nbsp;
                <input type="radio" name="isMobileStart" value="0" checked />否
                <input type="radio" name="isMobileStart" value="1" />能
                &nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></td>
        </tr>
        <tr>
            <td align=right>顺序</td>
            <td>
                &nbsp;&nbsp;<input id="orders" name="orders" value="<%=orders %>"/>
            </td>
        </tr>
    </table>
    <table width="98%" align="center">
        <tr>
            <td width="100%" align="center">
                <input id="btnOk" type="button" value="确定" class="btn"/>
                <input id="id" name="id" value="<%=id%>" type="hidden"/>
                <input id="myType" name="myType" value="<%=type%>" type="hidden"/>
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
        else if (type.id == "type_link" || type.id == "type_front") {
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
                jAlert("选择项不能为空", "提示");
                return false;
            }
        } else if (type.id == "type_flow") {
            if (document.getElementById("type_flow_selected").value == "") {
                jAlert("选择项不能为空", "提示");
                return false;
            }
        }
        else if (type.id == "type_link" || type.id == "type_front") {
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

    $(document).ready(function () {
        var type = <%=type%>;
        $('#type_module_selected').select2();
        $('#type_module_selected').select2('val', ['<%=code%>']);
        setRadioValue("isMobileStart", <%=isMobileStart%>);

        if (type == 1) {
            document.getElementById("type_menu").checked = true;
            document.getElementById("type_menu").disabled = true;
            document.getElementById("type_flow").disabled = true;
            document.getElementById("type_module").disabled = true;
            document.getElementById("type_link").disabled = true;
            document.getElementById("type_front").disabled = true;
            document.getElementById("type_flow_selected").style.display = "none";
            document.getElementById("type_menu_selected").value = "<%=code%>";
            // document.getElementById("type_menu_selected").disabled=true;

            // document.getElementById("type_module_selected").style.display = "none";
            $('#type_module_selected').hide();
            // 隐藏对应type_module_selected的select2控件
            $('#select2-type_module_selected-container').parent().parent().parent().hide();

            $("#spanIsAdd").hide();
            $('#link_tr').hide();
        } else if (type == 2) {
            document.getElementById("type_flow").checked = true;
            document.getElementById("type_flow").disabled = true;
            document.getElementById("type_menu").disabled = true;
            document.getElementById("type_module").disabled = true;
            document.getElementById("type_link").disabled = true;
            document.getElementById("type_front").disabled = true;
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
            document.getElementById("type_front").disabled = true;
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
            document.getElementById("type_front").disabled = true;
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
        else if (type == 5) {
            document.getElementById("type_link").disabled = true;
            document.getElementById("type_front").checked = true;
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
        document.getElementById("image").innerHTML = "<img class='icon' src='<%=request.getContextPath()%>/static/<%=imgUrl%>'>";
    });

    $(function() {
        $('#btnOk').click(function(e) {
            e.preventDefault();

            if (!check()) {
                return;
            }

            $.ajax({
                type: "post",
                url: "updateMobileAppIcon.do",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                data: $('#form1').serialize(),
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
                    if (data.ret == 1) {
                        reloadTab('<%=tabIdOpener%>');
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

    function selIcon(icon) {
        document.getElementById("imgUrl").value = "images/mobileAppIcons/" + icon;
        document.getElementById("image").innerHTML = "<img class='icon' src='<%=request.getContextPath()%>/static/images/mobileAppIcons/" + icon + "'>";
    }
</script>
