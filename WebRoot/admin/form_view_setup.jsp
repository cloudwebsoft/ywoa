<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String code = ParamUtil.get(request, "code");
FormDb fd = new FormDb();
fd = fd.getFormDb(code);

if ("save".equals(op)) {
	String xml = ParamUtil.get(request, "xml");
	fd.setViewSetup(xml);
	boolean re = fd.saveContent();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}
	out.print(json);		
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>表单显示设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery.xmlext.js"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language="JavaScript">
var divCount = 0;
var count = 0;

function onload() {
	var viewSetup = '<%=fd.getViewSetup()%>';
	if (viewSetup=="") {
		return;
	}
	
	var xml = $.parseXML(viewSetup);
	$xml = $(xml);
	
	$xml.find("views").children().each(function(i){
		// 删除第一行
		$("#views0").remove();

		count ++;
		
		var str = "";
		var json = $.parseJSON($(this).find("display").text());
		
		var jsonLen = 0;
		for(var key in json) {
			jsonLen ++;
		}
		
		if (jsonLen==0)
			return;
		
		str += "<tr id=\"views" + count + "\">";
		str += "  <td width=\"9%\" height=\"22\" align=\"left\">";
		str += "  如果</td>";
		str += "  <td width=\"50%\" height=\"22\"><input id=\"condition" + count + "\" name=\"condition" + count + "\" value='" + $(this).find("condition").text() + "' class='condition' /></td>";
		str += "  <td width=\"4%\">则";
		str += "  </td>";
		str += "  <td width=\"28%\" id=\"td" + count + "\">";
		
		var m = 0;
		for(var key in json) { 
			str += "  <div id=\"divtd" + count + divCount + "\">";
			str += "  <span id=\"span" + count + "\">";
			str += "  <input id=\"field" + count + "\" name=\"field" + count + "\" value='" + key + "' class='field' /><select id=\"field" + count + "_display\" name=\"field" + count + "_display\">";
			str += "    <option value=\"show\" " + ((json[key]=="show")?"selected":"") + ">显示</option>";
			str += "    <option value=\"hide\" " + ((json[key]=="hide")?"selected":"") + ">隐藏</option>";
			str += "  </select></span>";
			
			if (m==0)
				str += "  &nbsp;<a href=\"javascript:;\" onclick=\"addDisplay('td" + count + "', o('span" + count + "').innerHTML)\">+</a>";
			else				
				str += "  &nbsp;<a href='javascript:;' onclick=\"$('#divtd" + count + divCount + "').remove()\" class='delBtn'>×</a>";
							
			str += "  </div>";
			
			m ++;
			divCount ++;
		}
		
		str += "  </td>";
		str += "<td class='delBtn'><a href='javascript:;' onclick=\"$('#views" + count + "').remove();\">×</a></td>";
		
		str += "</tr>";
		
		count++;
		
		$("#tabCond").append(str);
	});

	if (count==0)
		count = 1;				
}
</script>
</head>
<body onload="onload()">
<%@ include file="form_edit_inc_menu_top.jsp"%>
<script>
o("menu5").className="current";
</script>
<div class="spacerH"></div>
<table width="100%" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent60" id="tabCond" style="padding:0px; margin:0px; margin-top:3px; width:100%">
  <tr>
    <td height="22" colspan="5" align="center"><input name="okbtn" type="button" class="btn" onclick="ModifyView()" value=" 保存 " />
    &nbsp;&nbsp;
<input type="button" value="增加" onclick="addCond()" class="btn" /></td>
  </tr>
  <tr id="views0">
    <td width="9%" height="22" align="left">
    	如果
    </td>
    <td width="50%" height="22"><input class="condition" id="condition0" name="condition0" /></td>
    <td width="4%">则
    </td>
    <td width="28%" id="td0">
    <div id="div">
    <span id="span0">
    <input class="field" id="field0" name="field0" /><select id="field0_display" name="field0_display">
      <option value="show">显示</option>
      <option value="hide">隐藏</option>
    </select></span>&nbsp;<a href="javascript:;" onclick="addDisplay('td0', span0.innerHTML)">+</a>
    </div>
    </td>
    <td width="9%">
    <a href='javascript:;' onclick="$('#views0').remove();" title="删除">×</a>
    </td>
  </tr>
</table>
</BODY>
<style>
.delBtn {
	font-size:16px;
}
.condition {
	width:200px;
}
.field {
	width:80px;
	margin-right:10px;	
}
</style>
<script>
function addDisplay(tdId, html) {
	html = "<div id='div" + tdId + divCount + "'>" + html + "&nbsp;<a href='javascript:;' onclick=\"$('#div" + tdId + divCount + "').remove()\" class='delBtn' title='删除'>×</a>";
	$("#" + tdId).append(html);
	divCount ++;
}

function addCond() {
  var str = "<tr id='views" + count + "'>";
  str += "<td>如果</td>";
  str += "<td ><input id='condition" + count + "' name='condition" + count + "' class='condition' size=6 /></td>";
  str += "<td>则</td>";
  str += "<td id='td" + count + "'>";
  str += "<div><span id='span" + count + "'><input class='field' id='field" + count + "' name='field" + count + "' />";
  str += "<select id='field" + count + "_display' name='field" + count + "_display'><option value='show'>显示</option><option value='hide'>隐藏</option></select></span>";
  str += "&nbsp;<a href='javascript:;' onclick=\"addDisplay('td" + count + "', span" + count + ".innerHTML)\">+</a>";
  str += "</div>";
  str += "</td>";
  str += "<td class='delBtn'><a href='javascript:;' onclick=\"$('#views" + count + "').remove();\" title='删除'>×</a></td>";
  str += "</tr>";
  $("#tabCond").append(str);
  
  count ++;
}

function makeViews() {
	var str = "";
	$("tr[id^='views']").each(function () {
		var trId = $(this).attr("id");
		var id = trId.substring("views".length);
		
		var displays = "";
		$("input[name='field" + id + "']").each(function (k) {
			if ($(this).val().trim()=="")
				return;
			
			var nm = $(this).attr("name");
			
			var ary = document.getElementsByName(nm + "_display");
			
			var dis = ary[k].value;
			if (displays=="")
				displays += "\"" + $(this).val() + "\":\"" + dis + "\"";
			else
				displays += ",\"" + $(this).val() + "\":\"" + dis + "\"";
		});
		displays = "{" + displays + "}";
		
		str += "<view>";
		str += "<condition>" + $("#condition" + id).val().replaceAll("'", "\"") + "</condition>";
		str += "<display>" + displays + "</display>";
		str += "</view>";
	});
		
	var xmlStr = "<config></config>";
	var xml = $.parseXML(xmlStr);
	$xml = $(xml);
		
	var $elem = $($.parseXML("<views>" + str + "</views>"));
	var newNode = null;
	if (typeof document.importNode == 'function') { 
		newNode = document.importNode($elem.find('views').get(0),true); 
	} else { 
		newNode = $elem.find('views').get(0);
	}	
	$xml.find("config").get(0).appendChild(newNode); 
	
	$.ajax({
		type: "post",
		url: "form_view_setup.jsp",
		data: {
			op: "save",
			code: "<%=code%>",
			xml: $xml.xml()
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('body').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			jAlert(data.msg, "提示");
		},
		complete: function(XMLHttpRequest, status){
			$('body').hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function ModifyView() {
	makeViews();
}
</script>
</HTML>