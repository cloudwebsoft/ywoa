<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<%@page import="com.redmoon.oa.dept.DeptMgr"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.dept.DeptView"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="java.io.StringReader"%>
<%@page import="org.jdom.Element"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.flow.FormField"%>
<%@page import="com.redmoon.oa.flow.WorkflowPredefineDb"%>
<%@page import="com.redmoon.oa.flow.*"%>
<%@page import="org.json.JSONObject"%>
<%@page import="com.redmoon.oa.flow.macroctl.DeptSelectCtl"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String flowTypeCode = ParamUtil.get(request,"flowTypeCode");
	String fromValue = ParamUtil.get(request,"fromValue");
	String toValue = ParamUtil.get(request,"toValue");
	String linkProp = ParamUtil.get(request,"linkProp");

	RoleDb roleDb1 = new RoleDb();
	Iterator ir1 = roleDb1.list().iterator();
	String strToghter = "";
	while(ir1.hasNext()){
		roleDb1 = (RoleDb)ir1.next();
		if (roleDb1.getCode().equals(ConstUtil.ROLE_MEMBER)) {
			continue;
		}
		strToghter += "<option value='"+roleDb1.getCode()+"'>"+roleDb1.getDesc()+"</option>";
	}
	
	Leaf lf = new Leaf();
	lf = lf.getLeaf(flowTypeCode);
	FormDb fd = new FormDb();
	fd = fd.getFormDb(lf.getFormCode());
	Vector field_v = fd.getFields();
	Iterator field_ir = field_v.iterator();
	String options = "";
	while (field_ir.hasNext()) {
		FormField ff = (FormField) field_ir.next();
		if(!ff.getType().equals("DATE")){
			options += "<option value='" + ff.getName() + "' id='"+ff.getFieldType()+"' name='"+ff.getMacroType()+"' lrc='"+ff.getType()+"'>" + ff.getTitle() + "</option>";
		}
	}
	
	String op = ParamUtil.get(request,"op");
	if(op.equals("selectMactl")){
		JSONObject json = new JSONObject();
		String fieldNameType = ParamUtil.get(request,"fieldNameType");
		String value = ParamUtil.get(request,"val");
		String isMacro = ParamUtil.get(request,"isMacro");
		
		Leaf lf1 = new Leaf();
		lf1 = lf1.getLeaf(flowTypeCode);
		FormDb fd1 = new FormDb();
		fd1 = fd1.getFormDb(lf.getFormCode());
		FormField field = fd1.getFormField(value);
		
		if(isMacro.equals("macro")){//如果表单字段是宏控件
			
			MacroCtlUnit mu;
			MacroCtlMgr mm = new MacroCtlMgr();
			mu = mm.getMacroCtlUnit(fieldNameType);
			
			if (mu == null) {
				return;
			}
			
			String str = mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request,field);
			
			if(str.indexOf("select") != -1){//如果宏控件是下拉框形式的
				str = str.substring(str.indexOf(">")+1,str.lastIndexOf("<"));
				
				json.put("ret","1");
				json.put("msg",str);
				out.print(json);
			}else{
				json.put("ret","0");
				json.put("msg","");
				out.print(json);
			}
		}else if (isMacro.equals("select")){//如果表单字段不是宏控件，是下拉框形式
			String str = FormParser.getOptionsOfSelect(fd1,field);
			
			json.put("ret","2");
			json.put("msg",str);
			out.print(json);
		}else{
			json.put("ret","0");
			json.put("msg","");
			out.print(json);
		}
		return;
	}

	int a =0;
	if(!linkProp.equals("")){
		SAXBuilder parser1 = new SAXBuilder();
		System.out.println(getClass() + " " + linkProp);
		org.jdom.Document doc1 = parser1.build(new InputSource(new StringReader(linkProp)));
		Element root1 = doc1.getRootElement();
		List<Element> v1 = root1.getChildren();
		for (Element e : v1) {
			String from = e.getChildText("from");
			String to = e.getChildText("to");
			if(from.equals(fromValue) && to.equals(toValue)){
				a++;
			}
		}
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>组合条件</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery.form.js"></script>
  </head>
  <script type="text/javascript">
  	function add(){
  		var roleValue = $("#roleHidden").val();
  		var deptValue = $("#deptHidden").val();
  		var fieldNameValue = $("#fieldNameHidden").val();
  		
  		var tb = document.getElementById("tab");
  		//var i = tab.rows.length-1;
  		var i = $("#maxIndex").val()-1;
  		var rnum = tb.rows.length+1;
  		var row = tb.insertRow();
  		row.align="center";
  		var cell = row.insertCell();
  		cell.innerHTML = "<select name='mainColumn' id='mainColumn"+i+"' onchange='changeColumn("+i+")'><option value='<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>'>表单字段</option><option value='<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>'>上一节点用户角色</option><option value='<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>'>上一节点用户部门</option></select>";
  		cell = row.insertCell();
  		cell.align="left";
  		cell.innerHTML = "<select name='firstBracket' id='firstBracket"+i+"'><option value=''></option><option value='('>(</option><option value=')'>)</option></select>&nbsp;<select name='fieldName' id='fieldName"+i+"' style='display:inline-block;' onchange='changeFieldName("+i+")'>"+fieldNameValue+
  		        "</select>&nbsp;<select name='compare' id='compare"+i+
  		        "'><option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option></select>&nbsp;<div id='columnInput"+i+
  		        "' style='display:inline'><input type='text' name='columnName' id='columnName"+i+"' value='' style='width:100px;'/></div>&nbsp;<select name='role' id='role"+i+"' style='display:none;width:155px;'>"+roleValue+
  		        "</select><select name='userDept' id='userDept"+i+"' style='display:none;width:155px;'>"+deptValue+"</select>&nbsp;"+"<select name='twoBracket' id='twoBracket"+i+
  		        "'><option value=''></option><option value='('>(</option><option value=')'>)</option></select>&nbsp;<select name='logical' id='logical" + i +
  		        "'><option value='and'>并且</option><option value='or'>或者</option></select>";
  		cell = row.insertCell();
  		cell.innerHTML = "<input class='btn btn-default' type='button' value='添加' onclick='add()'/>&nbsp;&nbsp;&nbsp;<input class='btn btn-default' type='button' value='删除 ' onclick='del(this)'/>";
  		
  		changeFieldName(i);
  		
  		var getObj = document.getElementsByName("logical");
		for(var j=0;j<getObj.length;j++){
			if(j == (getObj.length-1)){
				getObj[j].style.display="none";
			}else{
				getObj[j].style.display="inline-block";
			}
		}
		
		$("#currentMaxIndex").val(i+2);
		$("#maxIndex").val(i+2);
  	}
  	
  	function del(obj){
		var tb = document.getElementById("tab");   		
		var rowIndex = obj.parentElement.parentElement.rowIndex;		
		tb.deleteRow(rowIndex);	
		if(tb.rows.length == 1) {
			add();
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
	
	function changeColumn(str){
		var columnValue = $("#mainColumn"+str).val();
		if(columnValue == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>"){
			document.getElementById("fieldName"+str).style.display="none";
			document.getElementById("columnInput"+str).style.display="none";
			document.getElementById("columnName"+str).style.display="none";
			document.getElementById("role"+str).style.display="inline-block";
			document.getElementById("userDept"+str).style.display="none";
			
			$("#compare"+str).empty();
			// $("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");
			$("#compare"+str).append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
		}
		if(columnValue == "<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>"){
			document.getElementById("fieldName"+str).style.display="inline-block";
			document.getElementById("columnInput"+str).style.display="inline-block";
			document.getElementById("role"+str).style.display="none";
			document.getElementById("userDept"+str).style.display="none";
			o("compare"+str).value = ">=";

			document.getElementById("columnName"+str).value = "";
			
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
		}
		if(columnValue == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>"){
			document.getElementById("columnInput"+str).style.display="none";
			document.getElementById("columnName"+str).style.display="none";
			document.getElementById("fieldName"+str).style.display="none";
			document.getElementById("role"+str).style.display="none";
			document.getElementById("userDept"+str).style.display="inline-block";
			
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option><option value='=>'>=></option>");
		}
		
		changeFieldName(str);
	}
	
	function windowLoad(){
		$("#deptHidden").val($("#userDept").html());
		var tb = document.getElementById("tab");  	
		var rowIndex = tb.rows.length;
		
		$("#maxIndex").val(rowIndex);
		
		document.getElementById("logical"+(rowIndex-2)).style.display="none";
		
		<%
			if(a == 0){
		%>
			changeFieldName(0);
		<%}%>
		
		document.getElementById("form").style.overflow="auto";
		document.getElementById("form").style.width=window.screen.width;
		document.getElementById("form").style.height=window.screen.height-120;
		
	}
	
	function clickSure(){
		var dlg = window.opener ? window.opener : dialogArguments;
		var tb = document.getElementById("tab");
		var links = "<links>";
		var str = "";
		var val = "";
		var len = tab.rows.length-1;
		var maxIndex = $("#maxIndex").val();
		var currentMaxIndex = parseInt($("#currentMaxIndex").val())+1;
		if(currentMaxIndex>maxIndex){
			len = currentMaxIndex;
		}else{
			if(maxIndex>len){
				len = maxIndex;
			}
		}
		
		for(var i=0;i<len;i++){
			var obj = document.getElementById("fieldName"+i);
			if(obj != null){
				var index = obj.selectedIndex;
				
				var random = Math.random();
				var link = "<link id=\""+random+"\">";
				var from = "<from><%=fromValue%></from>";
				var to = "<to><%=toValue%></to>";
				var mainColumn = "<name>"+$("#mainColumn"+i).val()+"</name>";
				var firstBracket = "<firstBracket>"+$("#firstBracket"+i).val()+"</firstBracket>";
				var fieldName = "<fieldName>"+$("#fieldName"+i).val()+"</fieldName>";
				var compare = $("#compare"+i).val();
				// > < &gt; &lt;
				compare = compare.replaceAll("<", "&lt;");
				compare = compare.replaceAll(">", "&gt;");
				var operator = "<operator>"+compare+"</operator>";
				var logical = "<logical>"+$("#logical"+i).val()+"</logical>";
				var twoBracket = "<twoBracket>"+$("#twoBracket"+i).val()+"</twoBracket>";
				
				if($("#mainColumn"+i).val() == "<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>"){
					val = "<value>"+$("#columnName"+i).val()+"</value>";
				}
				if($("#mainColumn"+i).val() == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>"){
					val = "<value>"+$("#role"+i).val()+"</value>";
				}
				if($("#mainColumn"+i).val() == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>"){
					val = "<value>"+$("#userDept"+i).val()+"</value>";
				}
				var fieldType = "<fieldType>"+obj.options[index].getAttribute("id")+"</fieldType>"
				var endLink = "</link>";
				str += link+from+to+firstBracket+fieldName+mainColumn+operator+val+twoBracket+logical+fieldType+endLink;
			}
		}
		var endLinks = "</links>";
		dlg.setCondition(links+str+endLinks);
		window.close();
	}
	
	function changeFieldName(str) {
		var obj = document.getElementById("fieldName"+str);
		var mainColumn = $("#mainColumn"+str).val();
		var index = obj.selectedIndex;
		var fieldType = obj.options[index].getAttribute("id");
		var fieldNameType =obj.options[index].getAttribute("name");

		var isText = false; // 是否为字符型
		if (fieldType == "<%=FormField.FIELD_TYPE_TEXT%>" || fieldType == "<%=FormField.FIELD_TYPE_VARCHAR%>") {
			// 如果不是公式宏控件
			if (fieldNameType!="macro_formula_ctl") {
				isText = true;
			}
		}
		if (isText) {
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");
		}else{
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
		}
		if(mainColumn == "<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>"){
			document.getElementById("columnInput"+str).style.display="inline";
		}else if (mainColumn == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>") {
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='<>'><></option>");
		}
		else {
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option><option value='=>'>=></option>");
			document.getElementById("columnInput"+str).style.display="none";
		}

		var fieldNameType =obj.options[index].getAttribute("name"); 
		var val =obj.options[index].getAttribute("value"); 
		var lrc =obj.options[index].getAttribute("lrc"); 
		
		$.ajax({
			type: "post",
			url: "combination_condition.jsp",
			data : {
				op:"selectMactl",
				fieldNameType: fieldNameType,
				val:val,
				isMacro:lrc,
				flowTypeCode:"<%=flowTypeCode%>"
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="1") {
					$("#columnInput"+str).html("<select name='columnName' id='columnName"+str+"'>"+data.msg+"</select>");
				}else if (data.ret == "2"){
					$("#compare"+str).empty();
					$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");
					$("#columnInput"+str).html("<select name='columnName' id='columnName"+str+"'>"+data.msg+"</select>");
				}else{
					$("#columnInput"+str).html("<input type='text' name='columnName' id='columnName"+str+"' value=''/>");
				}
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				//alert(XMLHttpRequest.responseText);
			}
		});
	}
  </script>
  <body onload="windowLoad()">
     <form action="" id="form" >
	     <table id="tab" class="tabStyle_1 percent80"  border="0" align="center" cellpadding="2" cellspacing="0" >
	    	<tr style="border: 1px solid #a8a8a8;border-top:1px;border-bottom:1px">
	    		<td class="tabStyle_1_title" >&nbsp;条件字段</td>
	    		<td class="tabStyle_1_title" >跳转条件</td>
	    		<td class="tabStyle_1_title" >操作</td>
	    		<input type="hidden" name="roleHidden" id="roleHidden" value="<%=strToghter %>"/>
	    		<input type="hidden" name="deptHidden" id="deptHidden" value=""/>
	    		<input type="hidden" name="fieldNameHidden" id="fieldNameHidden" value="<%=options %>"/>
	    		<select name="userDept" id="userDept" style="display:none;width:155px;">
			        <%
						 DeptDb dd = new DeptDb();
					     DeptView dv = new DeptView(request, dd);
					     dd = dd.getDeptDb(privilege.getUserUnitCode(request));
					     dv.ShowDeptAsOptions(out, dd, 0);
					%>
    			</select>
	    	</tr>
	    	<%if(linkProp.equals("")){ %>
	    	<tr align="center">
	    		<td width="20%">
	    			<select name="mainColumn" id="mainColumn0" onchange="changeColumn(0)">
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>">表单字段</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>">上一节点用户角色</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>">上一节点用户部门</option>
	    			</select>
	    		</td>
	    		<td width="40%" align="left">
	    			<select name="firstBracket" id="firstBracket0">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			<select name="fieldName" id="fieldName0" style="display:inline-block;" onchange="changeFieldName(0)">
	    				<%=options %>
	    			</select>
	    			
	    			<select name="compare0" id="compare0">
	    				<option value=">=">>=</option>
	    				<option value="<="><=</option>
	    				<option value=">">></option>
	    				<option value="&lt;"><</option>
	    				<option value="<>"><></option>
	    				<option value="=">=</option>
	    			</select>
	    			
	    			<div id="columnInput0" style="display:inline">
		    			<input type="text" name="columnName" id="columnName0" value="" style="display:inline-block;width:100px;"/>
	    			</div>
	    			<select name="role" id="role0" style="display:none;width:155px;">
	    				<% 
	    					RoleDb roleDb = new RoleDb();
	    					Iterator ir = roleDb.list().iterator();
	    					while(ir.hasNext()){
	    						roleDb = (RoleDb)ir.next();
								if (roleDb.getCode().equals(ConstUtil.ROLE_MEMBER)) {
									continue;
								}
	    				%>
	    					<option value="<%=roleDb.getCode() %>"><%=roleDb.getDesc() %></option>
	    				<%
	    					}
	    				%>
	    			</select>
	    			<select name="userDept" id="userDept0" style="display:none;width:155px;">
				        <%
							 DeptDb dd2 = new DeptDb();
						     DeptView dv2 = new DeptView(request, dd2);
						     dd2 = dd2.getDeptDb(privilege.getUserUnitCode(request));
						     dv2.ShowDeptAsOptions(out, dd2, 0);
						%>
	    			</select>
	    			<select name="twoBracket" id="twoBracket0">
                        <option value=""></option>
                        <option value="(">(</option>
                        <option value=")">)</option>
                    </select>
	    			<select name="logical" id="logical0">
	    				<option value="and">并且</option>
	    				<option value="or">或者</option>
	    			</select>
	    			
	    		</td>
	    		<td width="20%">
	    			<input class="btn btn-default" type="button" value="添加" onclick="add()"/>
	    		</td>
	    	</tr>
	    	<%}else{ 
	    		int j = 0;
	    		SAXBuilder parser = new SAXBuilder();
		        org.jdom.Document doc = parser.build(new InputSource(new StringReader(linkProp)));
		        Element root = doc.getRootElement();
		        List<Element> v = root.getChildren();
		        int i=0;
	            for (Element e : v) {
	            	String from = e.getChildText("from");
	            	String to = e.getChildText("to");
	            	String fieldName = e.getChildText("fieldName");
	            	FormField ff = fd.getFormField(fieldName);
				if (ff==null) {
					System.out.println(getClass() + ":字段 " + fieldName + " 不存在");
					continue;
				}
				boolean isMacroFormulaCtl = false;

				if (FormField.TYPE_MACRO.equals(ff.getType())) {
					if ("macro_formula_ctl".equals(ff.getMacroType())) {
						isMacroFormulaCtl = true;
					}
				}

		            if(from.equals(fromValue) && to.equals(toValue)){
		            	j++;
	    	%>
	    		<tr align="center">
		    		<td width="20%">
		    			<select name="mainColumn<%=i%>" id="mainColumn<%=i %>" onchange="changeColumn(<%=i%>)">
		    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>">表单字段</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>">上一节点用户角色</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>">上一节点用户部门</option>
		    			</select>
		    			<script>
	    					o("mainColumn<%=i%>").value="<%=e.getChildText("name")%>";
	    				</script>
		    		</td>
		    		<td width="40%" align="left">
		    		<select name="firstBracket" id="firstBracket<%=i %>">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			<script>
    					o("firstBracket<%=i %>").value="<%=e.getChildText("firstBracket")%>";
    				</script>
		    		<select name="fieldName<%=i %>" id="fieldName<%=i %>" style="display:none;" onchange="changeFieldName(<%=i%>)">
	    				<%=options %>
	    			</select>
	    			<script>
    					o("fieldName<%=i %>").value="<%=e.getChildText("fieldName")%>";
    				</script>
	    			<select name="compare<%=i%>" id="compare<%=i %>">
	    				<option value=">=" >>=</option>
	    				<option value="<=" ><=</option>
	    				<option value=">" >></option>
	    				<option value="&lt;"><</option>
	    				<option value="<>"><></option>
	    				<option value="=" >=</option>
	    			</select>
	    			<script>
    					o("compare<%=i %>").value="<%=e.getChildText("operator")%>";
    				</script>
		    		<div id="columnInput<%=i %>" style="display:inline;width:150px;">
	    				<input type="text" name="columnName" id="columnName<%=i%>" value="<%=e.getChildText("value") %>" style="display:none;width:100px;"/>
	    			</div>
	    			<select name="role<%=i %>" id="role<%=i %>" style="display:none;width:155px;">
	    				<% 
	    					RoleDb roleDb = new RoleDb();
	    					Iterator ir = roleDb.list().iterator();
	    					while(ir.hasNext()){
	    						roleDb = (RoleDb)ir.next();
								if (roleDb.getCode().equals(ConstUtil.ROLE_MEMBER)) {
									continue;
								}
	    				%>
	    					<option value="<%=roleDb.getCode() %>"><%=roleDb.getDesc() %></option>
	    				<%
	    					}
	    				%>
	    			</select>
	    			<script>
	    				o("role<%=i%>").value="<%=e.getChildText("value")%>";
	    			</script>
	    			<select name="userDept<%=i %>" id="userDept<%=i %>" style="display:none;width:155px;">
				        <%
							 DeptDb dd1 = new DeptDb();
						     DeptView dv1 = new DeptView(request, dd1);
						     dd1 = dd1.getDeptDb(privilege.getUserUnitCode(request));
						     dv1.ShowDeptAsOptions(out, dd1, 0);
						%>
	    			</select>
	    			<script>
	    				o("userDept<%=i%>").value="<%=e.getChildText("value")%>";
	    				if('<%=e.getChildText("name")%>' == "<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>"){
    						document.getElementById("fieldName<%=i%>").style.display="inline-block";
    						document.getElementById("columnName<%=i%>").style.display="inline-block";
    						//document.getElementById("compare<%=i%>").disabled=false;

							var isText = false; // 是否为字符型
							if ("<%=e.getChildText("fieldType")%>" == "<%=FormField.FIELD_TYPE_TEXT%>" || "<%=e.getChildText("fieldType")%>" == "<%=FormField.FIELD_TYPE_VARCHAR%>") {
								// 如果不是公式宏控件
								<%if (!isMacroFormulaCtl) {%>
									isText = true;
								<%}%>
							}

    						if ( isText ){
	    						$("#compare<%=i%>").empty();
	    						$("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option>");
	    						o("compare<%=i%>").value="<%=e.getChildText("operator")%>";
	    					}else{
	    						$("#compare<%=i%>").empty();
	    						$("#compare<%=i%>").append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
	    						o("compare<%=i%>").value="<%=e.getChildText("operator")%>";
	    					}
    					}
	    				if('<%=e.getChildText("name")%>' == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>"){
    						document.getElementById("role<%=i%>").style.display="inline-block";
    						document.getElementById("columnInput<%=i%>").style.display="none";
    						//document.getElementById("compare<%=i%>").disabled=true;
    						$("#compare<%=i%>").empty();
    						// $("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option>");
							$("#compare<%=i%>").append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
    						o("compare<%=i%>").value="<%=e.getChildText("operator")%>";
    					}
	    				if('<%=e.getChildText("name")%>' == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>"){
    						document.getElementById("userDept<%=i%>").style.display="inline-block";
    						document.getElementById("columnInput<%=i%>").style.display="none";
    						//document.getElementById("compare<%=i%>").disabled=true;
    						$("#compare<%=i%>").empty();
						$("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option><option value='=>'>=></option>");
    						o("compare<%=i%>").value="<%=e.getChildText("operator")%>";
    					}

    					var obj = document.getElementById("fieldName<%=i%>");
						var index = obj.selectedIndex;
						var fieldNameType =obj.options[index].getAttribute("name"); 
						var val =obj.options[index].getAttribute("value"); 
						var lrc =obj.options[index].getAttribute("lrc");
						
    					$.ajax({
							type: "post",
							url: "combination_condition.jsp",
							data : {
								op:"selectMactl",
								fieldNameType: fieldNameType,
								val:val,
								isMacro:lrc,
								flowTypeCode:"<%=flowTypeCode%>"
					        },
							dataType: "html",
							beforeSend: function(XMLHttpRequest){
							},
							success: function(data, status){
								data = $.parseJSON(data);
								if (data.ret=="1") {
									$("#columnInput<%=i%>").html("<select name='columnName' id='columnName<%=i%>'>"+data.msg+"</select>");
									o("columnName<%=i%>").value="<%=e.getChildText("value")%>";
								}else if (data.ret == "2"){
									$("#compare<%=i%>").empty();
									$("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option>");	
									o("compare<%=i%>").value="<%=e.getChildText("operator")%>";
									$("#columnInput<%=i%>").html("<select name='columnName' id='columnName<%=i%>'>"+data.msg+"</select>");
									$("#columnName<%=i%>").val("<%=e.getChildText("value")%>");
									
								}else{
									$("#columnInput<%=i%>").html("<input type='text' name='columnName' id='columnName<%=i%>' value=''/>");
									$("#columnName<%=i%>").val("<%=e.getChildText("value")%>");
								}
							},
							complete: function(XMLHttpRequest, status){
							},
							error: function(XMLHttpRequest, textStatus){
								// 请求出错处理
								//alert(XMLHttpRequest.responseText);
							}
						});
	    			</script>
	    			<select name="twoBracket" id="twoBracket<%=i %>">
                        <option value=""></option>
                        <option value="(">(</option>
                        <option value=")">)</option>
                    </select>
                    <script>
                        o("twoBracket<%=i %>").value="<%=e.getChildText("twoBracket")%>";
                    </script>
	    			<select name="logical" id="logical<%=i %>">
	    				<option value="and">并且</option>
	    				<option value="or">或者</option>
	    			</select>
	    			<script>
	    				o("logical<%=i%>").value="<%=e.getChildText("logical")%>";
	    			</script>
	    		</td>
	    		<td width="20%">
	    			<input class="btn btn-default" type="button" name="" id="" value="添加" onclick="add()"/>&nbsp;&nbsp;&nbsp;<input class="btn btn-default" type="button" value='删除' onclick='del(this)'/>
	    		</td>
	    	</tr>
	    	<%	    
				i++;
	    		}
	        }
	    	if(j == 0){%>
	    		<tr align="center">
	    		<td width="20%">
	    			<select name="mainColumn" id="mainColumn0" onchange="changeColumn(0)">
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>">表单字段</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>">上一节点用户角色</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>">上一节点用户部门</option>
	    			</select>
	    		</td>
	    		<td width="40%" align="left">
	    			<select name="firstBracket" id="firstBracket0">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			<select name="fieldName" id="fieldName0" style="display:inline-block;" onchange="changeFieldName(0)">
	    				<%=options %>
	    			</select>
	    			<select name='compare' id="compare0">
	    				<option value=">=">>=</option>
	    				<option value="<="><=</option>
	    				<option value=">">></option>
	    				<option value="&lt;"><</option>
	    				<option value="<>"><></option>
	    				<option value="=">=</option>
	    			</select>
	    			<div id="columnInput0" style="display:inline">
	    				<input type="text" name="columnName" id="columnName0" value="" style="display:inline-block;width:100px;"/>
	    			</div>
	    			<select name="role" id="role0" style="display:none;width:155px;">
	    				<% 
	    					RoleDb roleDb = new RoleDb();
	    					Iterator ir = roleDb.list().iterator();
	    					while(ir.hasNext()){
	    						roleDb = (RoleDb)ir.next();
	    						if (roleDb.getCode().equals(ConstUtil.ROLE_MEMBER)) {
	    							continue;
								}
	    				%>
	    					<option value="<%=roleDb.getCode() %>"><%=roleDb.getDesc() %></option>
	    				<%
	    					}
	    				%>
	    			</select>
	    			<select name="userDept" id="userDept0" style="display:none;width:155px;">
				        <%
							 DeptDb dd2 = new DeptDb();
						     DeptView dv2 = new DeptView(request, dd2);
						     dd2 = dd2.getDeptDb(privilege.getUserUnitCode(request));
						     dv2.ShowDeptAsOptions(out, dd2, 0);
						%>
	    			</select>
	    			<select name="twoBracket" id="twoBracket0">
                        <option value=""></option>
                        <option value="(">(</option>
                        <option value=")">)</option>
                    </select>
	    			<select name="logical" id="logical0">
	    				<option value="and">并且</option>
	    				<option value="or">或者</option>
	    			</select>
	    		</td>
	    		<td width="20%">
	    			<input type="button" value="添加" onclick="add()"/>
	    		</td>
	    	</tr>
		    <%}}%>
	    </table>
	    <table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
     		<tr align="center">
     			<td colspan="3">
	     			<input type="hidden" name="maxIndex" id="maxIndex" value=""/>
	     			<input type="hidden" name="currentMaxIndex" id="currentMaxIndex" value=""/>
	     			<input type="button" class="btn btn-default" value="确定" onclick="clickSure()"/>
				<%--&nbsp;&nbsp;在此清空无效，必须得用flow_designer_link_prop.jsp中的清空按钮来清空，因为条件只是linkProp中的一部分--%>
				<%--<input type="button" value="清除" onclick="clearCond()"/>--%>
     			</td>
     		</tr>
     	</table>
		 <div style="width:90%; margin: 10px auto; line-height: 1.5">
			 注：<br/>
			 1、复选框如果被勾选则值为1，未被勾选则值为0<br/>
			 2、=>表示属于某个部门，包括该部门本身，及其下级所有部门
		 </div>
    </form>
  </body>
<script>
/*	function clearCond() {
		var dlg = window.opener ? window.opener : dialogArguments;
		dlg.setCondition("");
		window.close();
	}*/
</script>
</html>

