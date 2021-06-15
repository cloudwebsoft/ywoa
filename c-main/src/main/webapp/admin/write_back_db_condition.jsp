<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<%@page import="com.redmoon.oa.dept.DeptMgr"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.dept.DeptView"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="com.redmoon.oa.flow.query.*"%>
<%@page import="java.sql.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="java.io.StringReader"%>
<%@page import="org.jdom.Element"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<%@page import="com.redmoon.oa.flow.*" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.flow.FormField"%>
<%@page import="org.json.JSONObject"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String flowTypeCode = ParamUtil.get(request,"mainCode");
String mainCode = "";
if (!"".equals(flowTypeCode)){
	Leaf lf = new Leaf();
	lf = lf.getLeaf(flowTypeCode);
	mainCode = lf.getFormCode();
}

String dbSource = ParamUtil.get(request, "dbSource");
String writeBackCode = ParamUtil.get(request,"writeBackCode");
String internalName = ParamUtil.get(request, "internalName");
String mainOption = "<option value=\"empty\">请选择</option>";                             //主表字段
String backOption = "<option value=\"empty\">请选择</option>";                             //回写字段
String mainNumberOption = "<option value=\"empty\">请选择</option>";
Iterator irFields = null;
JSONObject json = new JSONObject();
FormDb fd = new FormDb();
String op = ParamUtil.get(request,"op");
if ("validate".equals(op)){
	String validateStr = ParamUtil.get(request,"validateStr");
	if ("".equals(validateStr)){
		json.put("ret",1);
	} else  {
		if (!validateStr.contains("(")&&!validateStr.contains(")")){
			json.put("ret",1);
		} else {
			Stack<Character> sc=new Stack<Character>();
	        char[] c=validateStr.toCharArray();
	        boolean flag = true;
	        for (int i = 0; i < c.length; i++) {
	            if (c[i]=='(') {
	                sc.push(c[i]);
	            }
	    	    if (c[i]==')') {
	                if (sc.size() > 0 && sc.peek()=='(') {
	                    sc.pop();
	                }else{
	                	flag = false;     // flag = false;
	                	break;
	                }
	            }
	        }
	        if (sc.empty() && flag) 
	        	json.put("ret",1);
	        else 
	        	json.put("ret",0);
		}
	}
	out.print(json.toString());
	return;
}
if ("field".equals(op)){
	fd = fd.getFormDb(writeBackCode);
	String field = ParamUtil.get(request,"field");
	
	if (!"".equals(field)){
		String sql = "select * from " + writeBackCode;
		com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
		try {
			conn.setMaxRows(1); //尽量减少内存的使用
			ResultSet rs = conn.executeQuery(sql);
			ResultSetMetaData rm = rs.getMetaData();
			int colCount = rm.getColumnCount();
			for (int i = 1; i <= colCount; i++) {
				String colName = rm.getColumnName(i); 
				if (colName.equalsIgnoreCase(field)) {
					int colType = rm.getColumnType(i);
					int fieldType = QueryScriptUtil.getFieldTypeOfDBType(colType);
			 		if (fieldType==FormField.FIELD_TYPE_TEXT||fieldType==FormField.FIELD_TYPE_VARCHAR){
			 			json.put("fieldType","text");
			 		} else {
			 			json.put("fieldType","number");
			 		}
		
					json.put("ret",0);
					json.put("msg","");
					break;
				}
			}
		}
		finally {
			conn.close();
		}
	}	
	out.print(json.toString());
	return;
}
if (!"".equals(mainCode)){
		fd = fd.getFormDb(mainCode);
		irFields = fd.getFields().iterator();
		while(irFields!=null&&irFields.hasNext()){
			FormField ff = (FormField)irFields.next();
			int fieldType = ff.getFieldType();
			if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE)
				mainNumberOption += "<option value=\"{$"+ff.getTitle()+"}\">"+ff.getTitle()+"</option>";
		}
		
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				// System.out.println(getClass() + " ff.getMacroType()=" + ff.getMacroType());
				if (ff.getMacroType().equals("nest_table") || ff.getMacroType().equals("nest_sheet")) {
					String nestFormCode = ff.getDefaultValue();
					try {
						String defaultVal = StrUtil.decodeJSON(ff.getDescription());
						JSONObject json2 = new JSONObject(defaultVal);
						nestFormCode = json2.getString("destForm");
					} catch (Exception e) {
						// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
					}
					
					FormDb nestfd = new FormDb();
					nestfd = nestfd.getFormDb(nestFormCode);
					
					ModuleSetupDb msd = new ModuleSetupDb();
					msd = msd.getModuleSetupDbOrInit(nestFormCode);
					
					Iterator ir2 = nestfd.getFields().iterator();
					while (ir2.hasNext()) {
						FormField ff2 = (FormField)ir2.next();
						int fieldType = ff2.getFieldType();
						if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE)
							// mainNumberOption += "<option value=\"{$nest." + nestFormCode + "." + ff2.getName() + "}\">" + ff2.getTitle() + "(子表：" + nestfd.getName() + ")</option>";
							mainNumberOption += "<option value=\"{$nest." + nestFormCode + "." + ff2.getName() + "}\">" + nestfd.getName() + " - " + ff2.getTitle() + "</option>";
							
					}
				}
			}
		}		
}

if (!"".equals(mainCode)){
	fd = fd.getFormDb(mainCode);
	irFields = fd.getFields().iterator();
	while(irFields!=null&&irFields.hasNext()){
		FormField ff = (FormField)irFields.next();
			mainOption += "<option  value=\"{$"+ff.getTitle()+"}\">"+ff.getTitle()+"</option>";
	}
	
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				// System.out.println(getClass() + " ff.getMacroType()=" + ff.getMacroType());
				if (ff.getMacroType().equals("nest_table") || ff.getMacroType().equals("nest_sheet")) {
					String nestFormCode = ff.getDefaultValue();
					try {
						String defaultVal = StrUtil.decodeJSON(ff.getDescription());
						JSONObject json2 = new JSONObject(defaultVal);
						nestFormCode = json2.getString("destForm");
					} catch (Exception e) {
						// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
					}
					
					FormDb nestfd = new FormDb();
					nestfd = nestfd.getFormDb(nestFormCode);
					
					ModuleSetupDb msd = new ModuleSetupDb();
					msd = msd.getModuleSetupDbOrInit(nestFormCode);
					
					Iterator ir2 = nestfd.getFields().iterator();
					while (ir2.hasNext()) {
						FormField ff2 = (FormField)ir2.next();
						int fieldType = ff2.getFieldType();
						// mainOption += "<option value=\"{$nest." + nestFormCode + "." + ff2.getName() + "}\">" + ff2.getTitle() + "(子表：" + nestfd.getName() + ")</option>";
						mainOption += "<option value=\"{$nest." + nestFormCode + "." + ff2.getName() + "}\">" + nestfd.getName() + " - " + ff2.getTitle() + "</option>";
						
					}
				}
			}
		}		
}
if (!"".equals(writeBackCode)){
	String sql = "select * from " + writeBackCode;
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		for (int i = 1; i <= colCount; i++) {
			String colName = rm.getColumnName(i);
			int colType = rm.getColumnType(i);
			int fieldType = QueryScriptUtil.getFieldTypeOfDBType(colType);
			if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE)
				backOption += "<option isNumber=\"true\" value=\""+colName+"\">"+colName+"</option>";
			else
				backOption += "<option isNumber=\"false\" value=\""+colName+"\">"+colName+"</option>";
		}
	}
	finally {
		conn.close();
	}
}

WorkflowPredefineDb wpd = new WorkflowPredefineDb();
wpd = wpd.getDefaultPredefineFlow(flowTypeCode);
SAXBuilder parser = new SAXBuilder();
String writeProp = wpd.getWriteDbProp();  
org.jdom.Document doc = null;
Element root = null;
Element child = null;
List list = null;
Iterator ir = null;
List conditionList = null;
if (writeProp!=null&&!"".equals(writeProp)){
	doc = parser.build(new InputSource(new StringReader(writeProp)));
	root = doc.getRootElement();
	if ("null".equals(internalName)){
		Element flowFinish = root.getChild("flowFinish");
		if (flowFinish!=null){
			String writeBack = flowFinish.getChildText("writeBackForm");
			if (writeBackCode.equals(writeBack)){
				Element condition = flowFinish.getChild("condition");
				if (condition!=null) {
					conditionList = condition.getChildren("conditionField");
				}
			}
		}
	} else {
		list = root.getChildren("nodeFinish");
		if (list!=null)	
			ir = list.iterator();
		while (ir!=null&&ir.hasNext()){
			Element nodeFinish = (Element)ir.next();
			Element internalNode = nodeFinish.getChild("internalName");
			String internalNodeText = internalNode.getText();
			if (internalName.equals(internalNodeText)){     //节点存在先删除
				String writeBack = nodeFinish.getChildText("writeBackForm");
				if (writeBack.equals(writeBackCode)){
				Element condition = nodeFinish.getChild("condition");
				if (condition!=null)
					conditionList = condition.getChildren("conditionField");
				}
			}
		}
	}
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<title>回写条件</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<script src="../js/jquery.form.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
  <body >
  	<div class="spacerH"></div>
     <form action="">
	     <table  id="tab" class="tabStyle_1 percent80"  border="0" style="text-align:center;" cellpadding="2" cellspacing="0">
	    	<tr style="border: 1px solid #a8a8a8;border-top:1px;border-bottom:1px">
	    		<td colspan="3" class="tabStyle_1_title" >过滤条件</td>
	    		<td class="tabStyle_1_title" >操作</td>
	    	</tr>
	    	<%
	    		int num = 1;
	    		Iterator condIterator = null;
	    		if (conditionList!=null && conditionList.size()>0){
	    			num = conditionList.size();
	    			condIterator = conditionList.iterator();
	    			int i = 0;
	    			while(condIterator!=null&&condIterator.hasNext()){
	    				Element condField = (Element)condIterator.next();
	    				String fieldName = condField.getAttributeValue("fieldName");
	    				String beginBracket = condField.getAttributeValue("beginBracket");
	    				String endBracket = condField.getAttributeValue("endBracket");

	    				int fieldType = WorkflowUtil.getColumnType(dbSource, writeBackCode, fieldName);

	    				String compare = condField.getChildText("compare");
	    				Element opElement = condField.getChild("compareVal");
	    				String opValue = opElement.getText();
	    				String opType = opElement.getAttributeValue("type");
	    				String logical = condField.getChildText("logical");
	    	%>
	    	<tr >
	    		<td>
	    			<select name="beginBracket" id="beginBracket<%=i%>">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			
	    			<select name="backField" id="backField<%=i%>"  onchange="changeField(<%=i%>)">
	    				<%=backOption%>
	    			</select>
	    			<%
	    				if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){
	    			%>
	    			<select name="compare" id="compare<%=i%>">
	    				<option value="&gt;=">&gt;=</option>
	    				<option value="&lt;=">&lt;=</option>
	    				<option value="&gt;">&gt;</option>
	    				<option value="&lt;">&lt;</option>
	    				<option value="!=">!=</option>
	    				<option value="=">=</option>
	    			</select>
	    			<%
	    				} else {
	    			%>
	    			<select name="compare" id="compare<%=i%>">
	    				<option value="!=">!=</option>
	    				<option value="=">=</option>
	    			</select>
	    			<%
	    				}
	    			%>
	    		</td>
	    		<td>
	    			<input type="radio" name="opType<%=i%>" value="custom" id="custom<%=i%>" checked onclick="changeValueType(<%=i%>)"/>指定值&nbsp;&nbsp;
	    			<input type="radio" name="opType<%=i%>" value="main" id="main<%=i%>" onclick="changeValueType(<%=i%>)"/>主表值&nbsp;&nbsp;
	    			<div id="div<%=i%>">
	    				<%
	    					if ("custom".equals(opType)){
	   							out.print("<input name=\"backFieldValue\" id=\"backFieldValue"+i+"\" />");
	    						if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){
	    							out.print("<select name = \"mainFieldValue\" fieldType=\"number\" id=\"mainFieldValue"+i+"\" style=\"display:none;\">"+mainNumberOption+"</select>");
	    						} else {
	    							out.print("<select name = \"mainFieldValue\" fieldType=\"text\" id=\"mainFieldValue"+i+"\" style=\"display:none;\">"+mainOption+"</select>");
	    						}
	    					} else {
	    						if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){
	    							out.print("<select name = \"mainFieldValue\" id=\"mainFieldValue"+i+"\" >"+mainNumberOption+"</select>");
	    						} else {
	    							out.print("<select name = \"mainFieldValue\" id=\"mainFieldValue"+i+"\" >"+mainOption+"</select>");
	    						}
	    						out.print("<input name=\"backFieldValue\" id=\"backFieldValue"+i+"\"  style=\"display:none;\"/>");
	    					}
	    				%>
	    			</div>
	    		</td>
	    		<td>
	    			<select name="endBracket" id="endBracket<%=i%>">
                        <option value=""></option>
                        <option value="(">(</option>
                        <option value=")">)</option>
                    </select>
	    			<select name="logical" id="logical<%=i%>">
	    				<option value="and">并且</option>
	    				<option value="or">或者</option>
	    			</select>
	    		</td>
	    		<td>
	    		<input type="button" value="增加" class="btn" onclick="addRow()"/>
	    		<input type="button" value="删除" class="btn" onclick="delRow(this)"/>
	    		<script type="text/javascript">
	    				$("#beginBracket<%=i%>").val("<%=beginBracket%>");
	    				$("#backField<%=i%>").val("<%=fieldName%>");
	    				$("#compare<%=i%>").val("<%=compare%>");
	    				$("input[name='opType<%=i%>']").each(function (){
							if (this.value=="<%=opType%>"){
								this.checked=true;
							}
						 })
						 $("#endBracket<%=i%>").val("<%=endBracket%>");
						 $("#logical<%=i%>").val("<%=logical%>");
						 if ("custom"=="<%=opType%>"){
						 	$("#backFieldValue<%=i%>").val("<%=opValue%>")
						 } else {
						 	$("#mainFieldValue<%=i%>").val("<%=opValue%>")
						 }
	    		</script>
	    		</td>
	    	</tr>
	    	<%
	    			i++;
	    			}
	    		} else {
	    	%>
	    	<tr >
	    		<td>
	    			<select name="beginBracket" id="beginBracket0">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			<select name="backField" id="backField0"  onchange="changeField(0)">
	    				<%=backOption%>
	    			</select>
	    			<select name="compare" id="compare0">
	    				<option value="&gt;=">&gt;=</option>
	    				<option value="&lt;=">&lt;=</option>
	    				<option value="&gt;">&gt;</option>
	    				<option value="&lt;">&lt;</option>
	    				<option value="!=">!=</option>
	    				<option value="=">=</option>
	    			</select>
	    		</td>
	    		<td>
	    			<input type="radio" name="opType0" value="custom" id="custom0" checked onclick="changeValueType(0)"/>指定值&nbsp;&nbsp;
	    			<input type="radio" name="opType0" value="main" id="main0" onclick="changeValueType(0)"/>主表值&nbsp;&nbsp;
	    			<div id="div0">
	    			<input name="backFieldValue0" id="backFieldValue0" />
	    			<select name = "mainFieldValue0" id="mainFieldValue0" style="display:none">
	    				<%=mainOption%>
	    			</select>
	    			</div>
	    		</td>
	    		<td>
	    			<select name="endBracket" id="endBracket0">
                        <option value=""></option>
                        <option value="(">(</option>
                        <option value=")">)</option>
                    </select>
	    			<select name="logical" id="logical0">
	    				<option value="and">并且</option>
	    				<option value="or">或者</option>
	    			</select>
	    		</td>
	    		<td>
	    		<input type="button" value="增加" class="btn" onclick="addRow()"/>&nbsp;&nbsp;<input type="button" value="删除" class="btn" onclick="delRow(this)"/>
	    		</td>
	    	</tr>
	    	<%} %>
	    </table>
	    <table  width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
     		<tr align="center">
     			<td colspan="3">
     			<!--  <input type="hidden" name="maxRow" id="maxRow" />-->
     			<input type="button" value="确定" class="btn" onclick="set()"/>
     			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     			<input type="button" value="重置" class="btn" onclick="cancel()"/></td>
     		</tr>
     		<tr><td><br />注：条件中只支持一个子表</td></tr>
     	</table>
    </form>
  </body>
  <script type="text/javascript">
  		// 增加行
  		var i =<%=num%>;
		function addRow(){
			var tab = document.getElementById("tab");
			// var i = tab.rows.length-1;       
			var tr = tab.insertRow();
		    tr.insertCell().innerHTML='<select name="beginBracket" id="beginBracket'+i+'">'+
	    				'<option value=""></option>'+
	    				'<option value="(">(</option>'+
	    				'<option value=")">)</option>'+
	    			'</select>'+
	    			'<select name="backField" id="backField'+i+'" onchange="changeField('+i+')"><%=backOption%></select>'+
	    			'<select name="compare" id="compare'+i+'">'+
	    				'<option value="&gt;=">&gt;=</option>'+
	    				'<option value="&lt;=">&lt;=</option>'+
	    				'<option value="&gt;">&gt;</option>'+
	    				'<option value="&lt;">&lt;</option>'+
	    				'<option value="!=">!=</option>'+
	    				'<option value="=">=</option>'+
	    			'</select>';
		    tr.insertCell().innerHTML='<input type="radio" name="opType'+i+'" checked value="custom" id="custom'+i+'" onclick="changeValueType('+i+')"/>指定值&nbsp;&nbsp;'+
	    			'<input type="radio" name="opType'+i+'" value="main" id="main'+i+'" onclick="changeValueType('+i+')"/>主表值&nbsp;&nbsp;'+
	    			'<div id="div'+i+'"><input name="backFieldValue0" id="backFieldValue'+i+'" />'+
	    			'<select name = "mainFieldValue0" id="mainFieldValue'+i+'" style="display:none"><%=mainOption%></select></div>';
		    tr.insertCell().innerHTML='<select name="endBracket" id="endBracket'+i+'">'+
                        '<option value=""></option>'+
                        '<option value="(">(</option>'+
                        '<option value=")">)</option>'+
                    '</select>'+
	    			'<select name="logical" id="logical'+i+'">'+
	    				'<option value="and">并且</option>'+
	    				'<option value="or">或者</option>'+
	    			'</select>';
	    	tr.insertCell().innerHTML='<input type="button" value="增加" class="btn" onclick="addRow()"/>&nbsp;&nbsp;'+
	    		'<input type="button" value="删除" class="btn" onclick="delRow(this)"/>';
	    		
	    	// $("#maxRow").val(tab.rows.length);
			var getObj = document.getElementsByName("logical");
			for(var j=0;j<getObj.length;j++){
				if(j == (getObj.length-1)){
					getObj[j].style.display="none";
				}else{
					getObj[j].style.display="inline-block";
				}
			}
			i++;
		}
		//删除行
		function delRow(obj){
			var tab = document.getElementById("tab");
			var index = obj.parentNode.parentNode.rowIndex
			if (index!=1){
				tab.deleteRow(index);
			}
			var getObj = document.getElementsByName("logical");
			for(var j=0;j<getObj.length;j++){
				if(j == (getObj.length-1)){
					getObj[j].style.display="none";
				}else{
					getObj[j].style.display="inline-block";
				}
			}
		}
		
		function changeField(num){
			var field = $("#backField"+num).val();
			if ("empty"!=field){
				$.ajax({
					type: "post",
					url: "write_back_db_condition.jsp",
					data : {
						op: "field",
						dbSource:"<%=dbSource%>",
			  			writeBackCode:"<%=writeBackCode%>",
					  	field: field
					},
					dataType: "json",
					beforeSend: function(XMLHttpRequest){
						//ShowLoading();
					},
					success: function(data, status){
						var typeValue;
						$("input[name='opType"+num+"']").each(function (){
								if (this.checked){
									typeValue = this.value;
								}
						})
						
						$("#backFieldValue"+num).remove();
						if ("custom"==typeValue){
							$("#div"+num).after('<input type="text" name="backFieldValue" id="backFieldValue'+num+'" />');
						} else {
							$("#div"+num).after('<input type="text" style="display:none" name="backFieldValue" id="backFieldValue'+num+'" />');
						}
						
						if (data.fieldType=="text"){
							$("#compare"+num).html('<option value="!=">!=</option><option value="=">=</option>');
							$("#mainFieldValue"+num).html('<%=mainOption%>');
							$("#backFieldValue"+num).attr("fieldType","text");
						} else {
							$("#compare"+num).html('<option value="&gt;=">&gt;=</option><option value="&lt;=">&lt;=</option>'+
								'<option value="&gt;">&gt;</option><option value="&lt;">&lt;</option><option value="!=">!=</option><option value="=">=</option>');
							$("#mainFieldValue"+num).html('<%=mainNumberOption%>');
							$("#backFieldValue"+num).attr("fieldType","number");
						}
					},
					complete: function(XMLHttpRequest, status){
						// HideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});	
			}
		}
		function changeValueType(num){
			// var typeValue = $("opType").val();
			var typeValue;
			$("input[name='opType"+num+"']").each(function (){
				if (this.checked){
					typeValue = this.value;
				}
			})
			if ("custom"==typeValue){
				$("#backFieldValue"+num).css("display","");
				$("#mainFieldValue"+num).css("display","none");
			} else {
				$("#mainFieldValue"+num).css("display","");
				$("#backFieldValue"+num).css("display","none");
			}
		}
		function set(){
			var xmlstr = "";
			var str = "";
			var beginStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><condition>";
			var endStr = "</condition></root>";
			// var tab = document.getElementById("tab");
			// var rowNum = tab.rows.length;
			// var maxNum = $("#maxRow").val();
			// if (maxNum>rowNum)
			// 	rowNum = maxNum;
			var num = i;
			for (j=0;j<num;j++){
				var backField = document.getElementById("backField"+j);
				if (backField!=null){
					var beginBracket = $("#beginBracket"+j).val();
					var backField = $("#backField"+j).val();
					if (backField=="empty"){
						alert("请选择条件字段");
						return;
					}
					var compare = $("#compare"+j).val();
					var opType ;
					var opValue;
					$("input[name='opType"+j+"']").each(function (){
						if (this.checked){
							opType = this.value;
						}
					})
					if ("custom"==opType){
						opValue = $("#backFieldValue"+j).val();
						var fieldType = $("#backFieldValue"+j).attr("fieldType");
						if ("number"==fieldType){
							if (isNaN(opValue)){
								alert("字段值非数字，请重新输入");
								return;
							}
						}
					} else {
						opValue = $("#mainFieldValue"+j).val();
					}
					var endBracket = $("#endBracket"+j).val();
					var logical = $("#logical"+j).val();
					var test = /</g;
	 				compare = compare.replace(test,"&lt;");
	 				str += beginBracket+backField+compare+opValue+endBracket+logical
	 				xmlstr += "<conditionField beginBracket=\""+beginBracket+"\" endBracket=\""+endBracket+"\" fieldName=\""+backField+"\"><compare>"+compare+"</compare><compareVal type=\""+opType+"\">"+opValue+"</compareVal><logical>"+logical+"</logical></conditionField>"
				}
			}
			$.ajax({
				type: "post",
				url: "write_back_db_condition.jsp",
				data: {
					op:"validate",
		   			validateStr: str
				},
				dataType: "json",
				beforeSend: function(XMLHttpRequest){
				},
				success: function(data, status){
					if (data.ret==1){                        //
						var win = window.opener ? window.opener : dialogArguments;
				 		win.setCondition(beginStr+xmlstr+endStr);
				 		window.close();
					} else {
						alert("条件不匹配,请检查括弧是否匹配");
					}
				},
				complete: function(XMLHttpRequest, status){
				},
				error: function(XMLHttpRequest, textStatus){
				}
			});	
			
		}
		$(document).ready(function (){
			var getObj = document.getElementsByName("logical");
			for(var j=0;j<getObj.length;j++){
				if(j == (getObj.length-1)){
					getObj[j].style.display="none";
				}else{
					getObj[j].style.display="inline-block";
				}
			}
		})
		function cancel(){
			window.location.reload();
		}
  </script>
</html>

