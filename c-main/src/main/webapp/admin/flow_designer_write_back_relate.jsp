<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.util.file.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@page import="java.net.URLDecoder"%>
<%
boolean isFieldNumeric = ParamUtil.getBoolean(request, "isNum", true);
String relateCode = ParamUtil.get(request, "relateCode");
String mainCode = ParamUtil.get(request, "mainCode");
if (!"".equals(mainCode)){
	Leaf lf = new Leaf();
	lf = lf.getLeaf(mainCode);
	mainCode = lf.getFormCode();
}
String textAreaId = ParamUtil.get(request, "textAreaId");
String textAreaVal = ParamUtil.get(request, "textAreaVal");
//textAreaVal = URLDecoder.decode(textAreaVal,"utf-8");
// String clearStr = textAreaVal.substring(0,textAreaVal.indexOf("=")+1);
textAreaVal = textAreaVal.replace("\\u002B","+");
FormDb fd = new FormDb();
fd = fd.getFormDb(mainCode);
FormDb mainFd = fd;
Iterator mainFields = fd.getFields().iterator();
fd = fd.getFormDb(relateCode);
Iterator relateFields = fd.getFields().iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>回写</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/ace-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<style>
#jcalculator {
	width: 80px;
}
#jcalculator span{
	cursor: pointer;
    float: left;
    width: 40px;
    height: 30px;
    line-height: 30px;
    font-size: 12px;
    color: #333333;
    box-sizing: border-box;
    -moz-box-sizing: border-box;
    -webkit-box-sizing: border-box;
    border-left: 1px solid #cecece;
    border-top: 1px solid #cecece;
    border-right: 1px solid #cecece;
    border-bottom: 1px solid #cecece;
    text-align:center;
}
</style>

</head>
<body style="padding:0px; margin:0px">
<div class="spacerH"></div>
<div id="dlg" >
	<table style="margin-top:5px;"  class="tabStyle_1 percent98">
		<tr><td colspan="3" style="border-left:0px;border-right:0px;border-top:0px;"><textarea name="mathValue" id="mathValue"  style="width:99.4%;" readOnly="true"><%=textAreaVal %></textarea></td></tr>
		<tr >
			<td width="35%" class="tabStyle_1_title" >
            <%
			String kind = ParamUtil.get(request, "kind");
			if ("insert".equals(kind)) {
				%>
				子表单
				<%
			}
			else {
			%>
            	回写表单
            <%}%>
            </td>
			<td width="30%" class="tabStyle_1_title" >表达式</td>
			<td width="35%" class="tabStyle_1_title">主表单</td></tr>
		<tr >
		<td style="vertical-align:text-top;">
			<ul name="writeBackField" id="writeBackField">
				<%
				if (!"insert".equals(kind)) {
					 while(relateFields!=null&&relateFields.hasNext()){
						FormField ff = (FormField)relateFields.next();
						int fieldType = ff.getFieldType();
						// int、long、float、double、price类型
						if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                                
					%>
						<li style="list-style-type:none;cursor: pointer;" id="{<%=ff.getTitle()%>}"><%=ff.getTitle()%></li>
					<% 
						}
						else if (!isFieldNumeric && ( fieldType==FormField.FIELD_TYPE_VARCHAR || fieldType==FormField.FIELD_TYPE_TEXT)) {
						%>
						<li style="list-style-type:none;cursor: pointer;" id="{<%=ff.getTitle()%>}"><%=ff.getTitle()%></li>						
						<%
						}
					}
				}
				else {
					// 预置插入的值时，取出嵌套表
					MacroCtlMgr mm = new MacroCtlMgr();
					Iterator ir = mainFd.getFields().iterator();
					while (ir.hasNext()) {
						FormField ff = (FormField)ir.next();
						if (ff.getType().equals(FormField.TYPE_MACRO)) {
							MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
							if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
								String nestFormCode = ff.getDefaultValue();
								try {
									String defaultVal;
									if (mu.getNestType()==MacroCtlUnit.NEST_DETAIL_LIST) {
										defaultVal = StrUtil.decodeJSON(ff.getDescription());				
									}
									else {
										defaultVal = StrUtil.decodeJSON(ff.getDescription());
										if ("".equals(defaultVal)) {
											defaultVal = StrUtil.decodeJSON(ff.getDefaultValueRaw());
										}
									}
									JSONObject json = new JSONObject(defaultVal);
									nestFormCode = json.getString("destForm");
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
								FormDb nestfd = new FormDb();
								nestfd = nestfd.getFormDb(nestFormCode);
								Iterator ir2 = nestfd.getFields().iterator();
								while (ir2.hasNext()) {
									FormField ff2 = (FormField)ir2.next();
									%>
									<li style="list-style-type:none;cursor: pointer;" id="{$nest.<%=nestFormCode%>.<%=ff2.getTitle()%>}"><%=nestfd.getName()%>&nbsp;-&nbsp;<%=ff2.getTitle()%></li>						
									<%
								}
							}
						}
					}				
				}
				%>
			</ul>
		</td>
		<td align="center" valign="top"><div id="jcalculator">
			<!-- 
			<span id="C" style="width:80px;">清空</span> -->
			<span id="+">+</span>
			<span id="-">-</span>
			<span id="*">*</span>
			<span id="/">/</span>
			<span id="9">9</span>
		    <span id="8">8</span>
		    <span id="7">7</span>
		    <span id="6">6</span>
		    <span id="5">5</span>
		    <span id="4">4</span>
		    <span id="3">3</span>
		    <span id="2">2</span>
		    <span id="1">1</span>
		    <span id="0">0</span>
		</div>
		</td>
		<td style="vertical-align:text-top;">
		 <ul name="mainField" id="mainField">
			<li style="list-style-type:none;cursor: pointer;" id="{$id}">ID</li>         
		 <% 
		 while(mainFields!=null&&mainFields.hasNext()){
			FormField ff = (FormField)mainFields.next();
			int fieldType = ff.getFieldType();
			if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                                // int、long、float、double类型
		%>
			<li style="list-style-type:none;cursor: pointer;" id="{$<%=ff.getTitle()%>}"><%=ff.getTitle()%></li>
		<% 
			}
			else if (!isFieldNumeric && ( fieldType==FormField.FIELD_TYPE_VARCHAR || fieldType==FormField.FIELD_TYPE_TEXT)) {
			%>
			<li style="list-style-type:none;cursor: pointer;" id="{$<%=ff.getTitle()%>}"><%=ff.getTitle()%></li>			
			<%
			}
			else {
				if ("insert".equals(kind)) {
				%>
				<li style="list-style-type:none;cursor: pointer;" id="{$<%=ff.getTitle()%>}"><%=ff.getTitle()%></li>			
				<%
				}
			}
		}
		
				if (!"insert".equals(kind)) {
					// 预置插入的值时，取出嵌套表
					MacroCtlMgr mm = new MacroCtlMgr();
					Iterator ir = mainFd.getFields().iterator();
					while (ir.hasNext()) {
						FormField ff = (FormField)ir.next();
						if (ff.getType().equals(FormField.TYPE_MACRO)) {
							MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
							if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
								String nestFormCode = ff.getDefaultValue();
								try {
									String defaultVal;
									if (mu.getNestType()==MacroCtlUnit.NEST_DETAIL_LIST) {
										defaultVal = StrUtil.decodeJSON(ff.getDescription());				
									}
									else {
										defaultVal = StrUtil.decodeJSON(ff.getDescription());
										if ("".equals(defaultVal)) {
											defaultVal = StrUtil.decodeJSON(ff.getDefaultValueRaw());
										}
									}
									JSONObject json = new JSONObject(defaultVal);
									nestFormCode = json.getString("destForm");
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
								FormDb nestfd = new FormDb();
								nestfd = nestfd.getFormDb(nestFormCode);
								Iterator ir2 = nestfd.getFields().iterator();
								while (ir2.hasNext()) {
									FormField ff2 = (FormField)ir2.next();
									%>
									<li style="list-style-type:none;cursor: pointer;" id="{$nest.<%=nestFormCode%>.<%=ff2.getTitle()%>}"><%=nestfd.getName()%>&nbsp;-&nbsp;<%=ff2.getTitle()%></li>						
									<%
								}
							}
						}
					}				
				}		
		
		%>
		 </ul></td></tr>
		 <tr>
		 	<td colspan="3" align="center">
		 	 	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="清空" onclick="clearMath()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="确定" onclick="set()" />
		 		<%
		 			if ("".equals(textAreaVal)){
		 		%>
		 			<input type="hidden" id="hiddenVal" name="hiddenVal" />
		 		<% 
		 			} else {
		 		%>
		 			<input type="hidden" id="hiddenVal" name="hiddenVal" value="1"/>
		 		<%} %>
		 	</td>
		 </tr>
	</table>
</div>
</body>
<script type="text/javascript">
	 $(document).ready(function (){
	 	$("#jcalculator").children().each(function(){
	 		$(this).bind("click",function(){
	 			var code = $(this).attr("id").trim();
	 			var math = $("#mathValue").val();
	 			var reg = /^(\{[\w\W]+\})|[\d]+$/;
	 			var reg1 = /^[\+\-\*\/]$/;
	 			var reg2 = /^\d+$/;
	 			var hiddenVal = $("#hiddenVal").val();
		 			if (reg.test(hiddenVal)){
		 				if (!reg2.test(hiddenVal)&&!reg1.test(code)){
		 					return;
		 				}
		 			} else {
		 				if (reg1.test(code)){
		 					return;
		 				}
		 			}
			 		$("#mathValue").val(math+code);
			 		$("#hiddenVal").val(code);
	 		})
	 	})
	 	$("li").live("click",function(){
	 		var math = $("#mathValue").val();
	 		var code = $(this).attr("id");
	 		var reg = /^(\{[\w\W]+\})|[\d]+$/;
	 		var reg1 = /^[\+\-\*\/]$/;
	 		var hiddenVal = $("#hiddenVal").val();
	 		if (hiddenVal!=""&&!reg1.test(hiddenVal)){
	 			return;
	 		}
	 		$("#mathValue").val(math+code);
	 		$("#hiddenVal").val(code);
	 	})
	 }) 
	 function set(){
	 	var mathValue = $("#mathValue").text();
	 	var win = window.opener ? window.opener : dialogArguments;
	 	win.setMath("<%=textAreaId%>",mathValue);
	 	window.close();
	 }
	 function clearMath(){
	 	$("#mathValue").val("");
	 	$("#hiddenVal").val("");
	 }
	 /*
	 function backspace(){
	 	var mathValue = $("#mathValue").val();
	 	$("#mathValue").val(mathValue.substring(mathValue,mathValue.length-1));
	 }*/
</script>
</html>