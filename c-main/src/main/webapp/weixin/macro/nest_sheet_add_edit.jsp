<%@ page language="java" import="cn.js.fan.util.ParamUtil" pageEncoding="utf-8" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="com.redmoon.oa.flow.Leaf" %>
<%@page import="com.redmoon.oa.flow.WorkflowDb" %>
<%@page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<!DOCTYPE HTML>
<html>
<head>
	<meta charset="utf-8">
	<title>嵌套表</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<meta content="telephone=no" name="format-detection"/>
	<link rel="stylesheet" href="../css/mui.css">
	<link rel="stylesheet" href="../css/iconfont.css"/>
	<link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
	<link rel="stylesheet" href="../css/my_dialog.css"/>
</head>
<style>
	.mui-input-row .input-icon {
		width: 50%;
		float: left;
	}

	.mui-input-row a {
		margin-right: 10px;
		float: right;
		text-align: left;
		line-height: 1.5;
	}

	.div_opinion {
		text-align: left;
	}

	.opinionContent {
		margin: 10px;
		width: 65%;
		float: right;
		font-weight: normal;
	}

	.opinionContent div {
		text-align: right;
	}

	.opinionContent div span {
		padding: 10px;
	}

	.opinionContent .content_h5 {
		color: #000;
		font-size: 17px;
	}
</style>
<body>
<header class="mui-bar mui-bar-nav">
	<a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
	<h1 class="mui-title">
		嵌套表格
	</h1>
</header>
<div class="mui-content">
	<div class="mui-scroll">
		<form class="mui-input-group" id="nest_form"
			  enctype="multipart/form-data">
		</form>
	</div>
</div>

<script type="text/javascript" src="../../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/macro/open_window_macro.js"></script>
<script src="../js/mui.min.js"></script>
<script src="../js/mui.picker.min.js"></script>
<script src="../js/mui.indexedlist.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script src="../js/macro/macro.js"></script>
<script type="text/javascript" src="../js/newPopup.js"></script>
<script type="text/javascript" src="../js/config.js"></script>
<script type="text/javascript" src="../js/base/mui.form.js"></script>
<script type="text/javascript" src="../js/mui.nest_sheet.js"></script>
<%
    String skey = ParamUtil.get(request, "skey");
    String fieldCode = ParamUtil.get(request, "code");
    int flowId = ParamUtil.getInt(request, "flowId", -1);
    String actionId = ParamUtil.get(request, "actionId");
    String dFormCode = ParamUtil.get(request, "dFormCode");
    String sFormCode = ParamUtil.get(request, "sFormCode");
    String parentCode = StrUtil.getNullStr(ParamUtil.get(request, "parentCode"));
    String parentFields = ParamUtil.get(request, "parentFields");
    int isWx = ParamUtil.getInt(request, "isWx", 0);
    String parentModuleCode = ParamUtil.get(request, "parentModuleCode");
    if ("".equals(parentCode)) {
        if (flowId != -1) {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Leaf lf = new Leaf();
            lf = lf.getLeaf(wf.getTypeCode());
            parentCode = lf.getFormCode();
        }
        if (!"".equals(parentModuleCode)) {
            ModuleSetupDb pMsd = new ModuleSetupDb();
            pMsd = pMsd.getModuleSetupDb(parentModuleCode);
            parentCode = pMsd.getString("form_code");
        }
    }
    boolean isEditable = ParamUtil.getBoolean(request, "isEditable", false);
    long parentId = ParamUtil.getLong(request, "parentId", -1);
    String urlParams = "?skey=" + skey + "&cwsId=" + parentId + "&parentModuleCode=" + parentModuleCode + "&code=" + fieldCode + "&flowId=" + flowId + "&actionId=" + actionId + "&dFormCode=" + dFormCode + "&sFormCode=" + sFormCode + "&parentCode=" + parentCode + "&isEditable=" + isEditable + "&parentFields=" + StrUtil.UrlEncode(parentFields);
    int id = ParamUtil.getInt(request, "id", 0);

    String pageType = ParamUtil.get(request, "pageType");
%>
<script src="<%=request.getContextPath()%>/weixin/flow/form_js/<%=dFormCode%>.jsp?flowId=<%=flowId%>&pageType=<%=pageType%>&actionId=<%=actionId%>&skey=<%=skey %>"></script>
<script type="text/javascript" charset="utf-8">
    // 用于在nest_sheet_add_edit.jsp中当post时提取页面的类型，如为add表示在智能模块中添加，edit表示在流程或智能模块编辑页面中添加
    function getParentPageType() {
        return "edit";
    }

    mui('.mui-scroll-wrapper').scroll({
        deceleration: 0.0005 //flick 减速系数，系数越大，滚动速度越慢，滚动距离越小，默认值0.0006
    });
    var params = {
        "isWx":<%=isWx%>,
        "formCode": "<%=dFormCode%>",
        "flowId": "<%=flowId%>",
        "nestFieldName": "<%=fieldCode%>",
        "actionId": "<%=actionId%>",
        "parentCode": "<%=parentCode%>",
        "skey": "<%=skey%>",
        "parentId": "<%=parentId%>",
        "id":<%=id%>,
        "pageType": "<%=pageType%>",
        "urlParams": '<%=urlParams%>'
    };

    var content = document.querySelector('.mui-content');
    window.nest_sheet = new mui.NestSheet(content, params);
    window.nest_sheet.initField();

    mui(".mui-bar").on("tap", ".mui-action-back", function () {
        if (params.isWx == 1) {
            nestSheetJump("列表", "../macro/nest_sheet_select.jsp" + params.urlParams + "&isWx=1", "");
        } else {
            mui.back();
        }
    });
</script>
</body>
</html>
