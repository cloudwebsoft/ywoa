<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>基础数据类型编辑</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<script src="../js/layui/layui.js" charset="utf-8"></script>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    int id = ParamUtil.getInt(request, "id", -1);
    SelectKindDb wptd = new SelectKindDb();
    wptd = wptd.getSelectKindDb(id);
%>
<%@ include file="basic_select_inc_menu_top.jsp" %>
<script>
    $("#menu3").addClass("current");
</script>
<br>
<div class="spacerH"></div>
<table width="494" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
    <tr>
        <td height="23" class="tabStyle_1_title">工作计划类型</td>
    </tr>
    <tr>
        <td align="center">
            <form id="form1" action="?op=modify" method=post>
                类型名称：
                <input name="name" value="<%=wptd.getName()%>" maxlength="30">
                <input name="id" value="<%=id%>" type=hidden>
                序号：<input name="orders" value="<%=wptd.getOrders()%>" size="3">
                &nbsp;
                <input class="btn" id="btnOk" type="button" value="确定"/>
            </form>
        </td>
    </tr>
</table>
<br>
<br>
<script>
	$(function() {
		$('#btnOk').click(function(e) {
			e.preventDefault();
			edit();
		})
	})

	function edit() {
		$.ajax({
			type: "post",
			url: "../basicdata/updateKind.do",
			data: $('#form1').serialize(),
			dataType: "json",
			beforeSend: function (XMLHttpRequest) {
				$('body').showLoading();
			},
			success: function (data, status) {
				layer.msg(data.msg);
			},
			complete: function (XMLHttpRequest, status) {
				$('body').hideLoading();
			},
			error: function () {
				alert(XMLHttpRequest.responseText);
			}
		});
	}
</script>
</body>
</html>
