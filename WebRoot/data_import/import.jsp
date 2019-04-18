<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.dataimport.*"%>
<%@page import="com.redmoon.oa.dataimport.service.IDataImport"%>
<%@page import="com.redmoon.oa.dataimport.bean.DataImportBean"%>
<%@page import="java.util.*"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="cn.js.fan.web.Global"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request,"op");
String code = ParamUtil.get(request, "code");
String item = ParamUtil.get(request, "menuItem");

String priv = code;
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

DataImportConfig cfg = new DataImportConfig();
DataImportMgr dim = cfg.getDataImportMgr(code);
if (dim == null) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "配置文件存在问题"));
	return;
}
IDataImport idi = dim.getIDataImport();
DataImportBean dib = idi.getDataImportBean();

String allFieldNames = "";
for (int i = 0; i < dib.getFieldsName().size(); i++) {
	allFieldNames += (allFieldNames.equals("") ? "" : " | ") + dib.getFieldsName().get(i);
}

String allSplitFields = "";
for (Map.Entry<String, String> entry : dib.getSplitFields().entrySet()) {
	allSplitFields += (allSplitFields.equals("") ? "" : " , ") + entry.getValue();
}

String jsonstr = "";
int sucCount = 0;
int errCount = 0;
JSONArray jsonAry = null;
JSONArray errjsonAry = null;

%>	
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=dib.getName() %>导入</title>
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/js/hopscotch/hopscotch.js"></script>
<script src="<%=request.getContextPath() %>/inc/upload.js"></script>
<link type="text/css" rel="stylesheet" href="css/import.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<style>
/*兼容IE8下上传文件样式问题IE8*/
.upload {
	width:88px;
}
.uploadFile {
	margin-top:-30px;
}
.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<%
if (op.equals("import") || op.equals("confirm")) {
	if (op.equals("import")) {
		jsonstr = idi.importExcel(application, request);
	} else if (op.equals("confirm")) {
		jsonstr = idi.create(application, request);
	}
	JSONObject json = JSONObject.fromObject(StrUtil.getNullStr(jsonstr));
	if (!json.containsKey("ret")) {
		out.print(StrUtil.jAlert_Redirect("导入失败！", "提示", "import.jsp?code=" + code + "&menuItem=" + item));
		return;
	}
	int ret = json.getInt("ret");
	if (ret != 1) {
		out.print(StrUtil.jAlert_Redirect(json.getString("data"), "提示", "import.jsp?code=" + code + "&menuItem=" + item));
		return;
	}
	if (json.containsKey("data")) {
		jsonAry = json.getJSONArray("data");
	}
	if (json.containsKey("err")) {
		errjsonAry = json.getJSONArray("err");
	}

	if (jsonAry != null) {
		sucCount = jsonAry.size();
	}
	if (errjsonAry != null) {
		errCount = errjsonAry.size();
	}
}

int thCount = dib.getFields().size() + dib.getSplitFields().size() + 2;
%>
<body>
<%if (code.equals("officeequip")) {%>
<%@ include file="../officeequip/officeequip_inc_menu_top.jsp"%>
<%} %>
<script>
o("menu<%=item%>").className="current";
</script>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<div class="import-number">
<!--最底层灰色条--><div id="step_line_2" class="import-number-linegray"></div>
<!--蓝色条1--><div class="import-number-lineblue1" ></div>
<!--灰色条2--><div style="display:none;" class="import-number-lineblue3"></div>


<!--1步--><div id="step_1" class="import-blue1">1</div>
<!--2步--><div id="step_2" class="import-gray2">2</div>
<!--3步--><div id="step_3" class="import-gray3">3</div>
<!--1步文字--><div id="step_txt_1" class="import-txt1 import-txt-sel">导入Excel</div>
<!--2步文字--><div id="step_txt_2" class="import-txt2">确认信息</div>
<!--3步文字--><div id="step_txt_3" class="import-txt3">完成</div>
</div>
<div id="step_import" class="import-data">
<form action="?op=import&code=<%=code%>&menuItem=<%=item %>" method="post" enctype="multipart/form-data" name="form1" id="form1" onsubmit="return submitCheck()">
<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
	<thead>
    <tr>
      <td class="tabStyle_1_title">导入excel</td>
    </tr>
	</thead>
    <tr>
      <td align="left">1.编辑Excel电子表格信息，将<%=dib.getName() %>信息按照模板（<a href="javascript:;" style="color:blue;"onclick="downloadTemplate() ">下载模板</a>）进行整理</td>
    </tr>
    <tr>
      <td align="left">2.选择整理完成的Excel文件进行上传</td>
    </tr>
    <tr>
      <td align="left" ><script>initUpload()</script></td>
    </tr>
     <tr>
      <td align="left"></td>
    </tr>
    <tr>
      <td align="left"></td>
    </tr>
     <tr>
      <td align="left" >导入须知</td>
    </tr>
    <tr>
      <td align="left">1.Excel中的表格首行为字段名，不能更改或删除</td>
    </tr>
    <tr>
      <td align="left">2.Excel中的列请按以下顺序排列：<%=allFieldNames %></td>
    </tr>
     <tr>
      <td align="left">注意：若存在多级<%=allSplitFields %>请按 A\B\C 的方式填写<%=allSplitFields %>信息 </td>
    </tr>
    <tr>
      <td align="center"><input type="submit" value="下一步" class="org-btn" /></td>
    </tr>
</table>
</form>
</div>
<div id="step_confirm" class="import-data" style="display:none">
<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
<tr>
<td>
您共填写<font color="red" ><%=sucCount + errCount%></font>条数据，格式正确数据<font color="red" ><%=sucCount%></font>条，格式错误数据<font color="red" ><%=errCount%></font>条。<%if (errCount > 0){out.print("由于存在错误数据您需要修改" + dib.getName() + "资料后重新上传表格。");} %><br />
导入失败的原因包括：<br/>
<%=idi.getErrorMessages()%> <br/>

</td>
</tr>
</table>
<span style="margin-left:280px;" >格式正确数据<%=sucCount%>条</span>
<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80" style="width:100%;table-layout:fixed;">
	<thead>
		<tr>
			<%
			for (int i = 0; i < dib.getFields().size(); i++) {
			%>
			<th class="tabStyle_1_title" width="<%=dib.getSplitFields().containsKey(dib.getFields().get(i)) ? 2 / thCount : 1 / thCount %>"><%=dib.getFieldsName().get(i) %></th>
			<%} %>
			<th class="tabStyle_1_title" width="<%=2 / thCount %>">提示</th>
		</tr>
	</thead>
	<tbody>
		<%
			for (int i = 0; i < sucCount; i++){
				JSONObject subjson = jsonAry.getJSONObject(i);
		%>
		<tr>
		<%
		for (String field : dib.getFields()) {
			%>
			<td><%=subjson.getString(field) %></td>
			<%
		}
		%>
			<td align="center"><img src="images/icon-finish.png"/></td>
		</tr>
		<%
		}
		%>
	</tbody>
</table>
<br />
<%if (errjsonAry != null && !errjsonAry.isEmpty()) {%>
<span style="margin-left:280px;">格式错误数据<%=errCount%>条</span>
<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80" style="width:100%;table-layout:fixed;">
	<thead>
		<tr>
			<%
			for (int i = 0; i < dib.getFields().size(); i++) {
			%>
			<th class="tabStyle_1_title" width="<%=dib.getSplitFields().containsKey(dib.getFields().get(i)) ? 2 / thCount : 1 / thCount %>"><%=dib.getFieldsName().get(i) %></th>
			<%} %>
			<th class="tabStyle_1_title" width="<%=2 / thCount %>">提示</th>
		</tr>
	</thead>
	<tbody>
	<%
			for (int i = 0; i < errCount; i++){
				JSONObject subjson = errjsonAry.getJSONObject(i);
		%>
		<tr>
		<%
		String errMsg = "";
		int k = 1;
		for (int j = 0; j < dib.getFields().size(); j++) {
			String field = dib.getFields().get(j);
			boolean isErr = subjson.containsKey("err_" + field);
			String fieldValue = subjson.getString(field);
			%>
			<td <%=isErr ? "style=\"border:1px solid #F90\"" : "" %>><%=fieldValue %></td>
			<%
			if (isErr) {
				int errNo = subjson.getInt("err_" + field);
				errMsg += (errMsg.equals("") ? "" : "</br>") + (k++) + "：" + dib.getFieldsName().get(j) + (errNo == DataImportBean.ERR_OUT_RANGE ? idi.getRangeErrMsg(field, fieldValue) : DataImportBean.getErrMsg(errNo));
			}
		}
		%>
			<td><%=errMsg %></td>
		</tr>
		<%
		}
		%>
	</tbody>
</table>
<%} %>
<table align="center">
	<tr>
	<td>
	<%if(errCount > 0 || sucCount == 0){ %>
	<input type="button" value="上一步" onclick="previous()" class="org-btn"/>
	<%} else { %>
	<form action="?op=confirm&code=<%=code%>&menuItem=<%=item %>" method="post" enctype="multipart/form-data" name="form2" id="form2" onsubmit="return submitCheck()">
	<input type="button" value="上一步" onclick="previous()" class="org-btn"/>
	<input type="hidden" id="datastr" name="datastr" value="<%=StrUtil.UrlEncode(jsonAry.toString()) %>" />
	<input type="submit" value="下一步" class="org-btn" style="margin-left:100px;"/>
	</form>
	<%} %>
	</td></tr>
</table>
</div>
<div id="step_finish" class="import-data" style="display:none">
<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
	<thead>
		<tr>
			<%
			for (int i = 0; i < dib.getFields().size(); i++) {
			%>
			<th class="tabStyle_1_title" width="<%=dib.getSplitFields().containsKey(dib.getFields().get(i)) ? 2 / thCount : 1 / thCount %>"><%=dib.getFieldsName().get(i) %></th>
			<%} %>
			<th class="tabStyle_1_title" width="<%=2 / thCount %>">提示</th>
		</tr>
	</thead>
	<tbody>
		<%
			for (int i = 0; i < sucCount; i++) {
				JSONObject subjson = jsonAry.getJSONObject(i);
		%>
		<tr>
		<%
		for (String field : dib.getFields()) {
			%>
			<td><%=subjson.getString(field) %></td>
			<%
		}
		%>
			<td align="center"><img src="images/icon-finish.png"/></td>
		</tr>
		<%
		}
		%>
	</tbody>
</table>
<div style="margin-left:5%;"><%=dib.getHint() %></div>
<table align="center">
	<tr>
	<td>
	<input type="button" value="完成" onclick="finish()" class="org-btn" onsubmit="return submitCheck()" />
	</td></tr>
</table>
</body>
</div>
</body>
<script>
	function downloadTemplate(){
		window.location.href = "<%=idi.getTemplatePath(request) %>";
	}
	// 表单提交校验
	function submitCheck(){
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	}
	function import_step_1() {
		$.ajax({
			url: "import_do.jsp?op=import&code=<%=code%>",
			type: "post",
			dataType: "json",
			beforeSend: function(XMLHttpRequest) {
				$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
			},
			success: function(data, status){
				if (data.ret == 1) {
					$('#tip_info').html("");
				} else {
					$('#tip_info').html(data.msg);
				}
				$('#step_1').css("import-gray1");
				$('#step_1').css("import-blue2");
				$('#step_1').css("import-gray3");
			},
			complete: function(XMLHttpRequest, status) {
				$(".loading").css({"display":"none"});
				$(".treeBackground").css({"display":"none"});
				$(".treeBackground").removeClass("SD_overlayBG2");
			},
			error: function(XMLHttpRequest, textStatus) {
				jAlert(XMLHttpRequest.responseText, "提示");
			}
		});	
	}
	$(function() {
		if ('<%=op%>' == 'import') {
			$('#step_2').removeClass("import-gray2");
			$('#step_2').addClass("import-blue2");
			$('#step_2').addClass("import-roundness-blue2");
			//$('#step_3').addClass("import-gray3");
			$('#step_txt_1').removeClass("import-txt-sel");
			$('#step_txt_2').addClass("import-txt-sel");

			$('#step_import').hide();
			$('#step_confirm').show();
			$('#step_finish').hide();
		} else if ('<%=op%>' == 'confirm') {
			$('#step_2').removeClass("import-gray2");
			$('#step_2').addClass("import-blue2");
			$('#step_2').addClass("import-roundness-blue2");
			$('#step_3').removeClass("import-gray3");
			$('#step_3').addClass("import-blue3");
			$('#step_3').addClass("import-roundness-blue3");
			$('#step_txt_1').removeClass("import-txt-sel");
			$('#step_txt_3').addClass("import-txt-sel");
			$('#step_line_2').removeClass("import-number-linegray");
			$('#step_line_2').addClass("import-number-lineblue1");
			$('#step_line_2').addClass("import-number-lineblue2");

			$('#step_import').hide();
			$('#step_confirm').hide();
			$('#step_finish').show();
		}
	});
	
	function previous() {
		$('#step_2').removeClass("import-blue2");
		$('#step_2').removeClass("import-roundness-blue2");
		$('#step_2').addClass("import-gray2");
		$('#step_3').removeClass("import-blue3");
		$('#step_3').removeClass("import-roundness-blue3");
		$('#step_3').addClass("import-gray3");
		$('#step_txt_1').addClass("import-txt-sel");
		$('#step_txt_2').removeClass("import-txt-sel");

		$('#step_import').show();
		$('#step_confirm').hide();
		$('#step_finish').hide();
	}
	
	function next() {
		//$('#step_1').css("import-gray1");
		//$('#step_2').css("import-gray2");
		//$('#step_3').css("import-blue3");
		//$('#step_txt_2').removeClass("import-txt-sel");
		//$('#step_txt_3').addClass("import-txt-sel");

		//$('#step_import').hide();
		//$('#step_confirm').hide();
		//$('#step_finish').show();

		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
		form2.submit();
	}

	function finish() {
		location.href = '<%=Global.getFullRootPath(request) + dib.getReturnURL()%>';
	}
</script>
</html>