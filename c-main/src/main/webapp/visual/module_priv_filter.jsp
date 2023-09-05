<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModulePrivDb" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>智能模块设计 - 管理权限</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
    <script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>
</head>
<body>
<%
    int id = ParamUtil.getInt(request, "id", -1);
    if (id == -1) {
        out.print(StrUtil.makeErrMsg(SkinUtil.LoadString(request, "err_id")));
        return;
    }

    String code = ParamUtil.get(request, "code");
    ModulePrivDb mpd = new ModulePrivDb();
    mpd = mpd.getModulePrivDb(id);
    if (!mpd.isLoaded()) {
        out.print(StrUtil.makeErrMsg(SkinUtil.LoadString(request, "err_id")));
        return;
    }

    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(code);
    if (msd == null) {
        out.print(StrUtil.jAlert_Back("该模块不存在！", "提示"));
        return;
    }

    String formCode = msd.getFormCode();
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    if (!fd.isLoaded()) {
        out.print(StrUtil.jAlert_Back("该表单不存在！", "提示"));
        return;
    }

    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<br/>
<form id="formModuleFilter" method="post" name="frmFilter">
    <table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
        <tr>
            <td align="center" class="tabStyle_1_title">过滤条件</td>
        </tr>
        <tr>
            <td width="91%" align="left">
                <%
                    String filter = StrUtil.getNullStr(mpd.getFilterCond()).trim();
                    boolean isComb = filter.startsWith("<items>") || "".equals(filter);
                    String cssComb = "", cssScript = "";
                    String kind;
                    if (isComb) {
                        cssComb = "in active";
                        kind = "comb";
                    } else {
                        cssScript = "in active";
                        kind = "script";
                %>
                <script>
                    $(function () {
                        $('#trOrderBy').hide();
                    });
                </script>
                <%
                    }
                %>
                <ul id="myTab" class="nav nav-tabs">
                    <li class="dropdown active">
                        <a href="#" id="myTabDrop1" class="dropdown-toggle" data-toggle="dropdown">
                            条件<b class="caret"></b></a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="myTabDrop1">
                            <li><a href="#comb" kind="comb" tabindex="-1" data-toggle="tab">组合条件</a></li>
                            <li><a href="#script" kind="script" tabindex="-1" data-toggle="tab">脚本条件</a></li>
                        </ul>
                    </li>
                </ul>
                <div id="myTabContent" class="tab-content">
                    <div class="tab-pane fade <%=cssComb %>" id="comb">
                        <div style="margin:10px">
                            <img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>&nbsp;<a
                                href="javascript:;" onclick="openCondition(o('condition'), o('imgId'))">配置条件</a>&nbsp;
                            <img src="../admin/images/gou.png"
                                 style="margin-bottom:-5px;width:20px;height:20px;display:<%=(isComb && !filter.equals(""))?"":"none" %>;"
                                 id="imgId"/>
                            <textarea id="condition" name="condition" style="display:none" cols="80"
                                      rows="5"><%=filter %></textarea>
                        </div>
                    </div>
                    <div class="tab-pane fade <%=cssScript %>" id="script">
                        <textarea id="filter" name="filter"
                                  style="width:98%; height:200px"><%=StrUtil.HtmlEncode(filter)%></textarea>
                        <br/>
                        字段：
                        <select id="filterField" name="filterField"
                                onchange="if (o('filterField').value!='') o('filter').value += o('filterField').value">
                            <option value="">请选择字段</option>
                            <%
                                for (FormField ff : fd.getFields()) {
                            %>
                            <option value="<%=ff.getName()%>"><%=ff.getTitle()%>
                            </option>
                            <%}%>
                        </select>
                        &nbsp;&nbsp;
                        <a href="javascript:" onclick="o('filter').value += '{$request.key}';" title="从request请求中获取参数">request参数</a>
                        &nbsp;&nbsp;
                        <a href="javascript:" onclick="o('filter').value += ' {$curDate}';" title="当前日期">当前日期</a>
                        &nbsp;&nbsp;
                        <a href="javascript:" onclick="o('filter').value += ' ={$curUser}';" title="当前用户">当前用户</a>
                        &nbsp;&nbsp;
                        <a href="javascript:" onclick="o('filter').value += ' in ({$curUserDept})';" title="当前用户">当前用户所在的部门</a>
                        &nbsp;&nbsp;
                        <a href="javascript:" onclick="o('filter').value += ' in ({$curUserRole})';" title="当前用户的角色">当前用户的角色</a>
                        &nbsp;&nbsp;
                        <a href="javascript:" onclick="o('filter').value += ' in ({$admin.dept})';" title="用户可以管理的部门">当前用户管理的部门</a>
                        &nbsp;&nbsp;
                        <span style="text-align:center">
      	<input type="button" value="设计器" class="btn btn-default" onclick="openIdeWin()"/>
      	<br/>
        (注：条件不能以and开头，可以直接输入条件，也可以使用脚本，脚本中必须返回ret)
      	</span>
                    </div>
                </div>
            </td>
        </tr>
        <tr>
            <td align="center">
                <input id="btnOk" class="btn btn-default" type="submit" value="确定"/>
                &nbsp;&nbsp;
                <input class="btn btn-default" type="button" onclick="window.close()" value="关闭"/>
                <input type="hidden" name="id" value="<%=id%>"/>
            </td>
        </tr>
    </table>
</form>
<br>
<script src="../js/layui/layui.js" charset="utf-8"></script>
</body>
<script>
    var layer;
    layui.use('layer', function(){
        layer = layui.layer;
    });

    var kind = "<%=kind%>";

    $(function () {
        $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
            kind = $(e.target).attr("kind");
            if (kind == "script") {
                if (o("filter").value.indexOf("<items>") == 0) {
                    o("filter").value = "";
                }
                $('#trOrderBy').hide();
            } else {
                $('#trOrderBy').show();
            }
        });
    });

    $('#btnOk').click(function(e) {
        e.preventDefault();

        if (kind == "comb") {
            o("filter").value = o("condition").value;
        }

        $.ajax({
            type: "post",
            url: "setModulePrivFilter",
            data: $('#formModuleFilter').serialize(),
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                // console.log('kind=' + kind);
                // console.log('filter=' + $('#filter').val());
                if ($('#filter').val() == '') {
                    window.opener.showGouImg(<%=id%>, false);
                }
                else {
                    window.opener.showGouImg(<%=id%>, true);
                }
                layer.open({
                    content: '<div style="padding: 20px 100px;">' + data.msg + '</div>'
                    ,btn: '确定'
                    ,yes: function(index, layero){
                        window.close();
                    }
                });
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    });

    function openWin(url, width, height) {
        var newwin = window.open(url, "fieldWindow", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
        return newwin;
    }

    var curCondsObj, curImgObj;

    function openCondition(condsObj, imgObj) {
        curCondsObj = condsObj;
        curImgObj = imgObj

        openWin("", 1024, 568);

        var url = "module_combination_condition.jsp";
        var tempForm = document.createElement("form");
        tempForm.id = "tempForm1";
        tempForm.method = "post";
        tempForm.action = url;

        var hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "condition";
        hideInput.value = curCondsObj.value;
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "fromValue";
        hideInput.value = "";
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "toValue";
        hideInput.value = ""
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "moduleCode";
        hideInput.value = "<%=code %>";
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "operate";
        hideInput.value = "";
        tempForm.appendChild(hideInput);

        document.body.appendChild(tempForm);
        tempForm.target = "fieldWindow";
        tempForm.submit();
        document.body.removeChild(tempForm);
    }

    function setCondition(val) {
        curCondsObj.value = val;
        if (val == "") {
            $(curImgObj).hide();
        } else {
            $(curImgObj).show();
        }
    }

    <%
	com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
	com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
	String version = StrUtil.getNullStr(oaCfg.get("version"));
	String spVersion = StrUtil.getNullStr(spCfg.get("version"));
    %>

    var ideUrl = "../admin/script_frame.jsp";
    var ideWin;
    var cwsToken = "";

    function openIdeWin() {
        ideWin = openWinMax(ideUrl);
    }

    function getScript() {
        return $('#filter').val();
    }

    function setScript(script) {
        $('#filter').val(script);
    }

    var onMessage = function(e) {
        var d = e.data;
        var data = d.data;
        var type = d.type;
        if (type=="setScript") {
            setScript(data);
            if (d.cwsToken!=null) {
                cwsToken = d.cwsToken;
                ideUrl = "../admin/script_frame.jsp?cwsToken=" + cwsToken;
            }
        }
        else if (type=="getScript") {
            var data={
                "type":"openerScript",
                "version":"<%=version%>",
                "spVersion":"<%=spVersion%>",
                "scene":"module.filter",
                "data":getScript()
            }
            ideWin.leftFrame.postMessage(data, '*');
        }
        else if (type == "setCwsToken") {
            cwsToken = d.cwsToken;
            ideUrl = "../admin/script_frame.jsp?cwsToken=" + cwsToken;
        }
    };

    $(function() {
        if (window.addEventListener) { // all browsers except IE before version 9
            window.addEventListener("message", onMessage, false);
        } else {
            if (window.attachEvent) { // IE before version 9
                window.attachEvent("onmessage", onMessage);
            }
        }
    });
</script>
</html>