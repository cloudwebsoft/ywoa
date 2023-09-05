<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.ui.SkinMgr" %>
<%@page import="com.redmoon.oa.pvg.RoleDb" %>
<%@page import="com.redmoon.oa.dept.DeptMgr" %>
<%@page import="com.redmoon.oa.dept.DeptDb" %>
<%@page import="com.redmoon.oa.dept.DeptView" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="org.jdom.input.SAXBuilder" %>
<%@page import="org.xml.sax.InputSource" %>
<%@page import="java.io.StringReader" %>
<%@page import="org.jdom.Element" %>
<%@page import="com.redmoon.oa.flow.FormDb" %>
<%@page import="com.redmoon.oa.flow.FormField" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%@page import="cn.js.fan.web.*" %>
<%@page import="com.redmoon.oa.flow.*" %>
<%@page import="org.json.JSONObject" %>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String moduleCode = ParamUtil.get(request, "moduleCode");
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(moduleCode);
	String formCode = "";
	if (msd!=null) {
		formCode = msd.getString("form_code");
	}
	else {
		out.print(SkinUtil.makeErrMsg(request, "模块" + moduleCode + "不存在，条件已清除，请关闭窗口重新选择源表单再配置条件！"));
		%>
		<script>
		var dlg = window.opener ? window.opener : dialogArguments;	
		dlg.setCondition("");
		</script>
		<%
		return;
	}
	
	String condition = ParamUtil.get(request, "condition");
	// 当condition在post的时候，大于及小于号被转码了，所以此处需再转码回去
	condition = condition.replaceAll("><=</", ">&lt;=</");
	condition = condition.replaceAll("><</", ">&lt;</");
	condition = condition.replaceAll(">>=</", ">&gt;=</");
	condition = condition.replaceAll(">></", ">&gt;</");
	condition = condition.replaceAll("<>", "&lt;&gt;");

	// 主表编码
	String mainFormCode = ParamUtil.get(request, "mainFormCode");
	
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	Vector<FormField> field_v = fd.getFields();
	Iterator<FormField> field_ir = field_v.iterator();
	String options = "";
	while (field_ir.hasNext()) {
		FormField ff = field_ir.next();
		options += "<option value='" + ff.getName() + "' id='"+ff.getFieldType()+"' name='"+ff.getMacroType()+"' lrc='"+ff.getType()+"'>" + ff.getTitle() + "</option>";
	}
	
	String mainFormOptions = "";
	if (!"".equals(mainFormCode)) {
		FormDb mainfd = new FormDb();
		mainfd = mainfd.getFormDb(mainFormCode);
		Iterator<FormField> irNext = mainfd.getFields().iterator();
		while (irNext.hasNext()) {
			FormField ff = irNext.next();
			if (!"DATE".equals(ff.getType())) {
				mainFormOptions += "<option value='{$" + ff.getName() + "}'>" + ff.getTitle() + "</option>";
			}
		}
	}
	
	String operate = ParamUtil.get(request, "operate");
	// 如果是模块验证，则不需要加入
	if (!"validate".equals(operate)) {
		options += "<option id='" + FormField.FIELD_TYPE_TEXT + "' value='cws_status' style='background-color: #ccc'>记录状态</option>";
		options += "<option id='" + FormField.FIELD_TYPE_TEXT + "' value='cws_flag' style='background-color: #ccc'>冲抵状态</option>";
		options += "<option id='" + FormField.FIELD_TYPE_TEXT + "' value='cws_id' style='background-color: #ccc'>关联字段</option>";
		options += "<option id='" + FormField.FIELD_TYPE_INT + "' value='cws_role' style='background-color: #ccc'>用户的角色</option>";
		options += "<option id='" + FormField.FIELD_TYPE_TEXT + "' value='cws_cur_user' style='background-color: #ccc'>当前用户</option>";
	}
	
	String op = ParamUtil.get(request,"op");
	if("selectMactl".equals(op)) {
		JSONObject json = new JSONObject();
		String fieldNameType = ParamUtil.get(request,"fieldNameType");
		String value = ParamUtil.get(request,"val");
		String isMacro = ParamUtil.get(request,"isMacro");
		FormField field = fd.getFormField(value);

		//如果表单字段是宏控件
		if("macro".equals(isMacro)){
			MacroCtlUnit mu;
			MacroCtlMgr mm = new MacroCtlMgr();
			mu = mm.getMacroCtlUnit(fieldNameType);
			
			if (mu == null) {
				return;
			}

			field.setCondType(SQLBuilder.COND_TYPE_NORMAL);
			String str = mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request,field);

			// 如果宏控件是下拉框形式的
			if(str.contains("select")){
				str = str.substring(str.indexOf(">")+1,str.lastIndexOf("<"));
				json.put("ret","1");
				json.put("msg",str);
				out.print(json);
			}else{
				str = "";
				json.put("ret","0");
				json.put("msg","");
				out.print(json);
			}
		} else if ("select".equals(isMacro)) {
			// 如果表单字段不是宏控件，是下拉框形式
			String str = FormParser.getOptionsOfSelect(fd,field);
			// 如果没有空的选项
			if (!str.contains(" value=''") && !str.contains(" value=\"\"")) {
				str = "<option value=''>" + ConstUtil.NONE + "</option>" + str;
			}
			json.put("ret","2");
			json.put("msg",str);
			out.print(json);
		} else{
			json.put("ret","0");
			json.put("msg","");
			out.print(json);
		}
		
		return;
	}

	int a = 0;
	// condition如果不以<开头，则可能是脚本条件
	if (!StrUtil.isEmpty(condition) && condition.startsWith("<")) {
		SAXBuilder parser1 = new SAXBuilder();
		try {
			org.jdom.Document doc1 = parser1.build(new InputSource(new StringReader(condition)));
			Element root1 = doc1.getRootElement();
			List<Element> v1 = root1.getChildren();
			a = v1.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>条件</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
	<script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery.form.js"></script>
	<script src="../inc/map.js"></script>

	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>
</head>
  <script type="text/javascript">
    var map = new Map();
  	map.put("<%=ModuleUtil.FILTER_CUR_USER%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER)%>");
	map.put("<%=ModuleUtil.FILTER_CUR_USER_DEPT%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT)%>");
	map.put("<%=ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN)%>");
  	map.put("<%=ModuleUtil.FILTER_CUR_USER_ROLE%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_ROLE)%>");
	map.put("<%=ModuleUtil.FILTER_ADMIN_DEPT%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_ADMIN_DEPT)%>");
	map.put("<%=ModuleUtil.FILTER_MAIN_ID%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_MAIN_ID)%>");
	map.put("<%=ModuleUtil.FILTER_CUR_DATE%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE)%>");
	map.put("<%=ModuleUtil.FILTER_CUR_YEAR%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_YEAR)%>");
	map.put("<%=ModuleUtil.FILTER_CUR_MONTH%>", "<%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_MONTH)%>");

  	function add() {
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
  		$(cell).hide();
  		cell = row.insertCell();
  		cell.align="left";
  		var html = "<select name='firstBracket' id='firstBracket"+i+"'><option value=''></option><option value='('>(</option><option value=')'>)</option></select>&nbsp;<select name='fieldName' id='fieldName"+i+"' style='display:inline-block;' onchange='changeFieldName("+i+")'>"+fieldNameValue+
  		        "</select>&nbsp;<select name='compare' id='compare"+i+
  		        "'><option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option></select>&nbsp;<div id='columnInput"+i+
  		        "' style='display:inline'><input type='text' name='columnName' id='columnName"+i+"' value='' style='width:100px;'/></div>";
  		$('#templet ul').attr("num", i);
		html += $('#templet').html();
  		html += "&nbsp;<select name='role' id='role"+i+"' style='display:none;width:155px;'>"+roleValue+
  		        "</select><select name='userDept' id='userDept"+i+"' style='display:none;width:155px;'>"+deptValue+"</select>&nbsp;"+"<select name='twoBracket' id='twoBracket"+i+
  		        "'><option value=''></option><option value='('>(</option><option value=')'>)</option></select>&nbsp;<select name='logical' id='logical" + i +
  		        "'><option value='and'>并且</option><option value='or'>或者</option></select>";
  		cell.innerHTML = html;
  		
		// 必须再调用一次，否则事件不生效
		$('.dropdown-menu li').on('click', function() {
			var num = $(this).parent().attr("num");
			if ($(this).attr("val")=="mainFormOpt") {
				$("#columnInput" + num).html("<select name='columnName' id='columnName" + num + "'>"+$('#mainFormOptDiv').html()+"</select>");
			}
			else {
				$("#columnInput" + num).html("<input type='text' name='columnName' id='columnName" + num + "' readonly val='" + $(this).attr("val") + "' value='" + $(this).children(0).text() + "'/>");
			}
		});

  		cell = row.insertCell();
  		cell.innerHTML = "<input type='button' value='添加' class='btn btn-default' onclick='add()'/>&nbsp;&nbsp;&nbsp;<input type='button' class='btn btn-default' value='删除 ' onclick='del(this)'/>";
  		  		
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
		if(tb.rows.length == 1){
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
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");
			
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
			document.getElRementById("userDept"+str).style.display="inline-block";
			
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");
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
		
		// document.getElementById("form").style.overflow="auto";
		// document.getElementById("form").style.width=window.screen.width;
		// document.getElementById("form").style.height=window.screen.height-120;
		
	}
	
	function clickSure(){
		var dlg = window.opener ? window.opener : dialogArguments;
		var tb = document.getElementById("tab");
		var links = "<items>";
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
				var link = "<item id=\""+random+"\">";
				// var from = "<from></from>";
				// var to = "<to></to>";
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
					if ($("#columnName"+i).attr("val")) {
						val = "<value>"+$("#columnName"+i).attr("val")+"</value>";					
					}
					else {
						val = "<value>"+$("#columnName"+i).val()+"</value>";
					}
				}
				if($("#mainColumn"+i).val() == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>"){
					val = "<value>"+$("#role"+i).val()+"</value>";
				}
				if($("#mainColumn"+i).val() == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>"){
					val = "<value>"+$("#userDept"+i).val()+"</value>";
				}
				var fieldType = "<fieldType>"+obj.options[index].getAttribute("id")+"</fieldType>"
				var endLink = "</item>";
				str += link+firstBracket+fieldName+mainColumn+operator+val+twoBracket+logical+fieldType+endLink;
			}
		}
		var endLinks = "</items>";
		dlg.setCondition(links+str+endLinks);
		window.close();
	}
	
	function clearCond() {
		var dlg = window.opener ? window.opener : dialogArguments;	
		dlg.setCondition("");
		window.close();	
	}
	
	function changeFieldName(str){
		var obj = document.getElementById("fieldName"+str);	
		var index = obj.selectedIndex;
		var val = obj.options[index].getAttribute("value");
		if (val=="cws_status") {
			var htmlStr = "<select name='columnName' id='columnName"+str+"'>";
			htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>";
			htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>";
			htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>";
			htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>";
			htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>";
			htmlStr += "</select>";

			$("#columnInput"+str).html(htmlStr);
			
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");			
			
			return;
		}	
		else if (val=="cws_flag") {
			var htmlStr = "<select name='columnName' id='columnName"+str+"'>";
			htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_NO%>'>未冲抵</option>";
			htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_YES%>'>已冲抵</option>";
			htmlStr += "</select>";

			$("#columnInput"+str).html(htmlStr);
			
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option>");			
			
			return;			
		}
		var mainColumn = $("#mainColumn"+str).val();
		var fieldType = obj.options[index].getAttribute("id");
		if (fieldType == "<%=FormField.FIELD_TYPE_TEXT%>" || fieldType == "<%=FormField.FIELD_TYPE_VARCHAR%>"){
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");
		}else{
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
		}
		if(mainColumn == "<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>"){
			document.getElementById("columnInput"+str).style.display="inline";
		}else{
			$("#compare"+str).empty();
			$("#compare"+str).append("<option value='='>=</option><option value='<>'><></option>");
			document.getElementById("columnInput"+str).style.display="none";
		}
		
		var fieldNameType =obj.options[index].getAttribute("name"); 
		var lrc =obj.options[index].getAttribute("lrc"); 
		$.ajax({
				type: "post",
				url: "module_combination_condition.jsp",
				data : {
					op:"selectMactl",
					fieldNameType: fieldNameType,
					val:val,
					isMacro:lrc,
					moduleCode:"<%=moduleCode%>",
					mainFormCode:"<%=mainFormCode%>"					
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
  <div id="mainFormOptDiv" style="display:none"><%=mainFormOptions %></div>
  <div id="templet" style="display:none">
  <div class="dropdown" style="display:inline">
    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">
        <span class="caret"></span>
    </button>
    <ul num="0" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">
            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>
        </li>
		<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_YEAR %>">
			<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_YEAR) %></a>
		</li>
		<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_MONTH %>">
			<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_MONTH) %></a>
		</li>
        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">
            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>
        </li>
        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_DEPT %>">
            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT) %></a>
        </li>
		<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN %>">
			<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN) %></a>
		</li>
        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_ROLE %>">
            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_ROLE) %></a>
        </li>
        <li role="presentation" val="<%=ModuleUtil.FILTER_ADMIN_DEPT %>">
            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_ADMIN_DEPT) %></a>
        </li>
		<li role="presentation" val="<%=ModuleUtil.FILTER_MAIN_ID %>">
			<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_MAIN_ID) %></a>
		</li>
        <%if (!"".equals(mainFormCode)) { %>
        <li role="presentation" val="mainFormOpt">
            <a role="menuitem" tabindex="-1" href="#">主表字段</a>
        </li>        
        <%} %>
    </ul>
</div>	    	
</div>

     <form action="" id="form" style="width:100%">
	     <table id="tab" class="tabStyle_1 percent80"  border="0" align="center" cellpadding="2" cellspacing="0" >
	    	<tr style="border: 1px solid #a8a8a8;border-top:1px;border-bottom:1px">
	    		<td class="tabStyle_1_title" style="display:none">&nbsp;主表字段</td>
	    		<td width="80%" class="tabStyle_1_title" >条件</td>
    		  <td width="20%" class="tabStyle_1_title" >操作</td>
    		  <input type="hidden" name="roleHidden" id="roleHidden" value=""/>
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
	    	
	    	<%if(a==0){ %>
	    	<tr align="center">
	    		<td style="display:none">
	    			<select name="mainColumn" id="mainColumn0" onChange="changeColumn(0)">
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>">表单字段</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>">上一节点用户角色</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>">上一节点用户部门</option>
	    			</select>
	    		</td>
	    		<td align="left">
    			  <select name="firstBracket" id="firstBracket0">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			<select name="fieldName" id="fieldName0" style="display:inline-block;" onChange="changeFieldName(0)">
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
					<div class="dropdown" style="display:inline">
					    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">
					        <span class="caret"></span>
					    </button>
					    <ul num="0" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>
					        </li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_YEAR %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_YEAR) %></a>
							</li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_MONTH %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_MONTH) %></a>
							</li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>
					        </li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_DEPT %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT) %></a>
					        </li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN) %></a>
							</li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_ROLE %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_ROLE) %></a>
					        </li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_ADMIN_DEPT %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_ADMIN_DEPT) %></a>
					        </li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_MAIN_ID %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_MAIN_ID) %></a>
							</li>
					        <%if (!"".equals(mainFormCode)) { %>
					        <li role="presentation" val="mainFormOpt">
					            <a role="menuitem" tabindex="-1" href="#">主表字段</a>
					        </li>        
					        <%} %>					        
					    </ul>
					</div>	    			
	    			<select name="role" id="userRole0" style="display:none;width:155px;">
	    				<% 
	    					RoleDb roleDb = new RoleDb();
	    					Iterator ir = roleDb.list().iterator();
	    					while(ir.hasNext()){
	    						roleDb = (RoleDb)ir.next();
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
	    		<td>
	    			<input type="button" class="btn btn-default" value="添加" onClick="add()"/>
	    		</td>
	    	</tr>
	    	<%}else{ 
	    		int j = 0;
		        List<Element> v = new ArrayList<Element>();;	    		
	    		SAXBuilder parser = new SAXBuilder();
	    		try {
			        org.jdom.Document doc = parser.build(new InputSource(new StringReader(condition)));
			        Element root = doc.getRootElement();
			        v = root.getChildren();
		        }
		        catch(Exception e) {
		        	e.printStackTrace();
		        }
		        int i=0;
	            for (Element e : v) {
					if (true) {
						j++;
	    	%>
	    		<tr align="center">
		    		<td style="display:none">
		    			<select name="mainColumn<%=i%>" id="mainColumn<%=i %>" onChange="changeColumn(<%=i%>)">
		    			<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>">表单字段</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>">上一节点用户角色</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>">上一节点用户部门</option>
		    			</select>
		    			<script>
	    					o("mainColumn<%=i%>").value="<%=e.getChildText("name")%>";
	    				</script>
		    		</td>
		    		<td align="left">
	    		  <select name="firstBracket" id="firstBracket<%=i %>">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			<script>
    					o("firstBracket<%=i %>").value="<%=e.getChildText("firstBracket")%>";
    				</script>
		    		<select name="fieldName<%=i %>" id="fieldName<%=i %>" style="display:none;" onChange="changeFieldName(<%=i%>)">
	    				<%=options %>
	    			</select>
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
	    			
					<div class="dropdown" style="display:inline">
					    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">
					        <span class="caret"></span>
					    </button>
					    <ul num="<%=i %>" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>
					        </li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_YEAR %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_YEAR) %></a>
							</li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_MONTH %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_MONTH) %></a>
							</li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>
					        </li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_DEPT %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT) %></a>
					        </li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN) %></a>
							</li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER_ROLE %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER_ROLE) %></a>
					        </li>
					        <li role="presentation" val="<%=ModuleUtil.FILTER_ADMIN_DEPT %>">
					            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_ADMIN_DEPT) %></a>
					        </li>
							<li role="presentation" val="<%=ModuleUtil.FILTER_MAIN_ID %>">
								<a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_MAIN_ID) %></a>
							</li>
					        <%if (!"".equals(mainFormCode)) { %>
					        <li role="presentation" val="mainFormOpt">
					            <a role="menuitem" tabindex="-1" href="#">主表字段</a>
					        </li>        
					        <%} %>					        
					    </ul>
					</div>	    		

	    			<select name="role<%=i %>" id="role<%=i %>" style="display:none;width:155px;">
	    				<% 
	    					RoleDb roleDb = new RoleDb();
	    					Iterator ir = roleDb.list().iterator();
	    					while(ir.hasNext()){
	    						roleDb = (RoleDb)ir.next();
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
						var objFieldName<%=i%> = o("fieldName<%=i%>");
						objFieldName<%=i%>.value="<%=e.getChildText("fieldName")%>";
	    				o("userDept<%=i%>").value="<%=e.getChildText("value")%>";

	    				if('<%=e.getChildText("name")%>' == "<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>"){
    						document.getElementById("fieldName<%=i%>").style.display="inline-block";
    						document.getElementById("columnName<%=i%>").style.display="inline-block";
    						//document.getElementById("compare<%=i%>").disabled=false;
    						
    						if ("<%=e.getChildText("fieldType")%>" == "<%=FormField.FIELD_TYPE_TEXT%>" || "<%=e.getChildText("fieldType")%>" == "<%=FormField.FIELD_TYPE_VARCHAR%>" ){
	    						$("#compare<%=i%>").empty();
	    						$("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option>");
	    						if ("<%=mainFormCode%>"!="") {
	    							$("#compare<%=i%>").append("<option value='like'>包含</option>");
	    						}
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
    						$("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option>");
    						o("compare<%=i%>").value="<%=e.getChildText("operator")%>";
    					}
	    				else if('<%=e.getChildText("name")%>' == "<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>"){
    						document.getElementById("userDept<%=i%>").style.display="inline-block";
    						document.getElementById("columnInput<%=i%>").style.display="none";
    						//document.getElementById("compare<%=i%>").disabled=true;
    						$("#compare<%=i%>").empty();
    						$("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option>");
    						o("compare<%=i%>").value="<%=e.getChildText("operator")%>";
    					}

						var obj = objFieldName<%=i%>;
						var index = obj.selectedIndex;
						var fieldNameType = obj.options[index].getAttribute("name");
						var val = obj.value; // obj.options[index].getAttribute("value");
						var lrc = obj.options[index].getAttribute("lrc");

						if (val=="cws_status") {
							var htmlStr = "<select name='columnName' id='columnName<%=i%>'>";
							htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>";
							htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>";
							htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>";
							htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>";
							htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>";
							htmlStr += "</select>";
				
							$("#columnInput<%=i%>").html(htmlStr);
							$("#columnName<%=i%>").val("<%=e.getChildText("value")%>");
							
							$("#compare<%=i%>").empty();
							$("#compare<%=i%>").append("<option value='='>=</option><option value='<>'><></option>");			
						}
						else if (val=="cws_flag") {
							var htmlStr = "<select name='columnName' id='columnName<%=i%>'>";
							htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_NO%>'>未冲抵</option>";
							htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_YES%>'>已冲抵</option>";
							htmlStr += "</select>";
				
							$("#columnInput<%=i%>").html(htmlStr);
							$("#columnName<%=i%>").val("<%=e.getChildText("value")%>");
							
							$("#compare<%=i%>").empty();
							$("#compare<%=i%>").append("<option value='='>=</option>");
						}
						else {
	    					$.ajax({
								type: "post",
								url: "module_combination_condition.jsp",
								data: {
									op: "selectMactl",
									fieldNameType: fieldNameType,
									val: val,
									isMacro: lrc,
									moduleCode: "<%=moduleCode%>",
									mainFormCode: "<%=mainFormCode%>"
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
										$("#columnInput<%=i%>").html("<input type='text' name='columnName' id='columnName<%=i%>' value='<%=e.getChildText("value")%>'/>");
									}

									var val = "<%=e.getChildText("value")%>";
									if (val.indexOf("{$")==0) {
										if (val.indexOf("<%=ModuleUtil.FILTER_CUR_DATE%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_CUR_YEAR%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_CUR_MONTH%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_CUR_USER%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_CUR_USER_ROLE%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_CUR_USER_DEPT%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_CUR_USER_DEPT_AND_CHILDREN%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_ADMIN_DEPT%>")==0 || val.indexOf("<%=ModuleUtil.FILTER_MAIN_ID%>")==0) {
											$("#columnInput<%=i%>").html("<input type='text' name='columnName' id='columnName<%=i%>' value='<%=e.getChildText("value")%>'/>");								
											$('#columnName<%=i%>').attr("val", val);
											$('#columnName<%=i%>').val(map.get(val).value);
											$('#columnName<%=i%>').attr("readonly", "readonly");
										}
										else {
											if (""!="<%=mainFormCode%>") {
												$("#columnInput<%=i%>").html("<select name='columnName' id='columnName<%=i%>'>"+$('#mainFormOptDiv').html()+"</select>");
												o("columnName<%=i%>").value = val;
											}
										}
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
	    		<td>
	    			<input type="button" class="btn btn-default" value="添加" onClick="add()"/>&nbsp;&nbsp;&nbsp;<input type="button" class="btn btn-default" value="删除" onclick="del(this)"/>
	    		</td>
	    	</tr>
	    	
	    	<%	    
				i++;
	    	}
	    		}
	    	if(j == 0){%>
	    		<tr align="center">
	    		<td style="display:none">
	    			<select name="mainColumn" id="mainColumn0" onChange="changeColumn(0)">
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_FIELD%>">表单字段</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE%>">上一节点用户角色</option>
	    				<option value="<%=WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT%>">上一节点用户部门</option>
	    			</select>
	    		</td>
	    		<td align="left">
    			  <select name="firstBracket" id="firstBracket0">
	    				<option value=""></option>
	    				<option value="(">(</option>
	    				<option value=")">)</option>
	    			</select>
	    			<select name="fieldName" id="fieldName0" style="display:inline-block;" onChange="changeFieldName(0)">
	    				<%=options %>
	    			</select>
	    			<select name='compare' id="compare0">
	    				<option value="&gt;=">>=</option>
	    				<option value="&lt;="><=</option>
	    				<option value="&gt;">></option>
	    				<option value="&lt;"><</option>
	    				<option value="&lt;&gt;"><></option>
	    				<option value="=">=</option>
	    			</select>
	    			<div id="columnInput0" style="display:inline">
	    				<input type="text" name="columnName" id="columnName0" value="" style="display:inline-block;width:100px;"/>
	    			</div>
	    			<select name="role" id="userRole0" style="display:none;width:155px;">
	    				<% 
	    					RoleDb roleDb = new RoleDb();
	    					Iterator ir = roleDb.list().iterator();
	    					while(ir.hasNext()){
	    						roleDb = (RoleDb)ir.next();
	    					
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
	    		<td>
	    			<input type="button" class="btn btn-default" value="添加" onClick="add()"/>
	    		</td>
	    	</tr>
		    <%
	    		}
	    	}
			%>
	    </table>
	    <div class="text-center" style="margin-top: 10px">
			<input type="hidden" name="maxIndex" id="maxIndex" value=""/>
			<input type="hidden" name="currentMaxIndex" id="currentMaxIndex" value=""/>
			<input type="button" class="btn btn-default" value="确定" onClick="clickSure()"/>
			&nbsp;&nbsp;
			<input type="button" class="btn btn-default" value="清除" onClick="clearCond()"/>
		</div>
    </form>
  </body>
<script>
$('.dropdown-menu li').on('click', function() {
	var num = $(this).parent().attr("num");
	if ($(this).attr("val")=="mainFormOpt") {
		$("#columnInput" + num).html("<select name='columnName' id='columnName" + num + "'>"+$('#mainFormOptDiv').html()+"</select>");
	}
	else {
		$("#columnInput" + num).html("<input type='text' name='columnName' id='columnName" + num + "' readonly val='" + $(this).attr("val") + "' value='" + $(this).children(0).text() + "'/>");
	}	
});

<%
// 如果是用于模块的验证，则不启用下拉菜单
if ("validate".equals(operate)) {
%>
$(".dropdown").hide();
<%}%>
</script>	  
</html>

