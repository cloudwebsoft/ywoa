<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>公式选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link href="../js/select2/select2.css" rel="stylesheet" />
<script src="../js/select2/select2.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");
%>
<table class="tabStyle_1" style="padding:0px; margin:0px;" width="100%" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td height="28" colspan="2" class="tabStyle_1_title">&nbsp;请选择</td>
    </tr>
    <tr>
      <td width="13%" align="center">公式</td>
      <td width="87%" align="left"><%
		FormDAO fdao = new FormDAO();
		String sql = "select id from form_table_formula order by id desc";
		java.util.Iterator ir = fdao.list("formula", sql).iterator();
		String opts = "";
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			opts += "<option value='" + fdao.getFieldValue("code") + "'>" + fdao.getFieldValue("name") + "</option>";
		}
		%>
        <select id="sel" name="sel" style="width:200px">
          <option value="">无</option>
          <%=opts%>
      </select>
      </td>
    </tr>
    <tr>
      <td align="center">公式参数</td>
      <td align="left">
      <span id="spanParams"></span>      
      </td>
    </tr>
    <tr>
      <td align="center">实际参数</td>
      <td align="left"><input id="params" name="params" style="width: 100%"/></td>
    </tr>
    <tr>
      <td align="center">参数字段</td>
      <td align="left">
<style>
				.fieldLink {
					width:120px;
					float:left;
					display: block;
				}
			</style>
			<%
				FormDb fd = new FormDb();
				fd = fd.getFormDb(formCode);
				ir = fd.getFields().iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField)ir.next();
					%>
					<a class="fieldLink" href="javascript:;" onclick="addParam('<%=ff.getName()%>')"><%=ff.getTitle()%></a>
					<%
				}
			%>      
      </td>
    </tr>
    <tr>
      <td colspan="2" align="center"><input type="button" value="确定" onclick="doSel()" /></td>
    </tr>
	<tr>
		<td colspan="2" align="center">
			
		</td>
	</tr>
  </tbody>
</table>
</body>
<script language="javascript">
	function addParam(fieldName) {
		var params = $('#params').val();
		if (params=="") {
			$('#params').val(fieldName);
		}
		else {
			$('#params').val(params + "," + fieldName);
		}

	}
	function doSel() {
		var result = {"code":$('#sel').val(), "params":$('#params').val(), "name":sel.options[sel.selectedIndex].text};
		window.opener.setSequence(JSON.stringify(result), sel.options[sel.selectedIndex].text);
		window.close();
	}

	$('#sel').change(function() {
		if ($(this).val()=="") {
			return;
		}
		$.ajax({
			type: "post",
			url: "formula/getParams.do",
			data: {
				code: $(this).val()
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('body').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="0") {
					jAlert(data.msg, "提示");
				}
				else {
					$('#params').val(data.params);
					$('#spanParams').html(data.params);
				}
			},
			complete: function(XMLHttpRequest, status){
				$('body').hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	})

	$(function() {
		$('#sel').select2();
	})
</script>
</html>