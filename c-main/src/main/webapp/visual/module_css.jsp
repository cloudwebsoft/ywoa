<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.util.file.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<%
    String op = ParamUtil.get(request, "op");
    String code = ParamUtil.get(request, "code");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(code);
    if (msd == null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
        return;
    }

    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String pageType = ParamUtil.get(request, "pageType");
    if (pageType.equals("")) {
        pageType = ConstUtil.PAGE_TYPE_LIST;
    }

    String cssStr = "";
    switch (pageType) {
        case ConstUtil.PAGE_TYPE_ADD:
            cssStr = StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_ADD));
            break;
        case ConstUtil.PAGE_TYPE_EDIT:
            cssStr = StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_EDIT));
            break;
        case ConstUtil.PAGE_TYPE_SHOW:
            cssStr = StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_SHOW));
            break;
        default:
            cssStr = StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_LIST));
            break;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>页面样式</title>
    <style type="text/css" media="screen">
        #editor {
            position: absolute;
            top: 40px;
            right: 0;
            bottom: 0;
            left: 0;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <script src="../js/bootstrap/js/bootstrap.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body style="padding:0px; margin:0px">
<div class="form-inline form-group text-center">
    <select id="pageType" name="pageType" onchange="onPageTypeChange()" class="form-control">
		<option value="<%=ConstUtil.PAGE_TYPE_LIST%>">列表页</option>
		<option value="<%=ConstUtil.PAGE_TYPE_ADD%>">添加页</option>
		<option value="<%=ConstUtil.PAGE_TYPE_EDIT%>">修改页</option>
		<option value="<%=ConstUtil.PAGE_TYPE_SHOW%>">详情页</option>
    </select>
    <script>
        o("pageType").value = "<%=pageType%>";
    </script>
    <input type="button" onclick="saveCss()" value="保存" class="btn btn-default"/>
</div>
<textarea id="content" style="display:none"><%=cssStr %></textarea>
<pre id="editor"></pre>
<script src="../js/ace-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<script>
    function openWinMax(url) {
        return window.open(url, '', 'scrollbars=yes,resizable=yes,channelmode'); // 开启一个被F11化后的窗口起作用的是最后那个特效
    }

    var editor = ace.edit("editor");
    // editor.setTheme("ace/theme/eclipse");
    // editor.setTheme("ace/theme/terminal");
    editor.setTheme("<%=cfg.get("aceTheme")%>");
    editor.getSession().setMode("ace/mode/java");
	editor.setShowPrintMargin(true);//显示打印边线

	editor.setOptions({
        readOnly: false,
        highlightActiveLine: true,
        highlightGutterLine: true
    })
	// 显示光标
    editor.renderer.$cursorLayer.element.style.opacity = 1;
    editor.setValue($('#content').val());
</script>
</body>
<script>
    function onPageTypeChange() {
        var type = o("pageType").value;
        window.location.href = "module_css.jsp?code=<%=code%>&pageType=" + type;
    }

    function saveCss() {
        $.ajax({
            url: "updateCss.do",
			method: "post",
            data: {
                code: "<%=code%>",
                pageType: o("pageType").value,
                css: editor.getValue()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function getScript() {
        return editor.getValue();
    }

    function setScript(script) {
        editor.setValue(script);
    }
</script>
</HTML>