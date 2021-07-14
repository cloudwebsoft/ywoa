<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.util.file.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.xml.sax.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="org.jdom.output.*"%>
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
String internalName = ParamUtil.get(request, "internalName");
WorkflowPredefineMgr wpfm = new WorkflowPredefineMgr();
WorkflowPredefineDb wpd = new WorkflowPredefineDb();
wpd = wpd.getDefaultPredefineFlow(flowTypeCode);
if (wpd==null) {
	%>
 	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(SkinUtil.makeInfo(request, "请先创建并保存流程图！"));
	return;
}

Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());
Iterator irFields = fd.getFields().iterator();
String opts = "";
String op = ParamUtil.get(request,"op");
String formCode = ParamUtil.get(request,"formCode");
if ("field".equals(op)){
	if (!"empty".equals(formCode)&&!"".equals(formCode)) {
		fd = fd.getFormDb(formCode);
	}
	
	JSONArray arr = new JSONArray();
	irFields = fd.getFields().iterator();
	opts += "<option value='empty' >请选择</option>";
	while (irFields!=null&&irFields.hasNext()) {
		FormField ff = (FormField)irFields.next();
		int fieldType = ff.getFieldType();
		String type;
		if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                            
			opts += "<option value='" + ff.getName() + "' type='number'>" + ff.getTitle() + "</option>";
			type = "number";
		}
		else {
			opts += "<option value='" + ff.getName() + "' type='string'>" + ff.getTitle() + "</option>";
			type = "string";
		}
		JSONObject js = new JSONObject();
		js.put("name", ff.getName());
		js.put("type", type);
		js.put("title", ff.getTitle());
		arr.put(js);
	}
	// out.print(opts);
	
	JSONObject json = new JSONObject();
	json.put("opts", opts);
	json.put("fields", arr);
	out.print(json);
	
	return;
}
else if ("getInsertFields".equals(op)) {
	if (!"empty".equals(formCode)&&!"".equals(formCode)) {
		fd = fd.getFormDb(formCode);
	}
	
	JSONArray arr = new JSONArray();
	irFields = fd.getFields().iterator();
	while (irFields!=null&&irFields.hasNext()) {
		FormField ff = (FormField)irFields.next();
		int fieldType = ff.getFieldType();
		String type;
		if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                            
			type = "number";
		}
		else {
			type = "string";
		}
		JSONObject js = new JSONObject();
		js.put("name", ff.getName());
		js.put("type", type);
		js.put("title", ff.getTitle());
		arr.put(js);
	}
	
	JSONObject json = new JSONObject();
	json.put("opts", opts);
	json.put("fields", arr);
	out.print(json);
	
	return;	
}
SAXBuilder parser = new SAXBuilder();
String writeProp = wpd.getWriteProp();                   //从数据库获取回写
org.jdom.Document doc = null;
Element root = null;
Element child = null;
List list = null;
Iterator ir = null;

String writeBackName = "";
String writeTime = "";
String writeBackType = "";
String primaryKey = "";
boolean hasCondition = false;
List writeBackFieldList = null;
JSONObject insertFields = null;
if (writeProp!=null&&!"".equals(writeProp)){
	doc = parser.build(new InputSource(new StringReader(writeProp)));
	root = doc.getRootElement();
	if ("".equals(internalName)){
		Element flowFinish = root.getChild("flowFinish");
		if (flowFinish!=null){
			writeBackName = flowFinish.getChildText("writeBackForm");
			writeBackType = flowFinish.getChildText("writeBackType");
			primaryKey = flowFinish.getChildText("primaryKey");
			if (writeBackType==null) {
				writeBackType = String.valueOf(WorkflowPredefineDb.WRITE_BACK_UPDATE);
			}
			Element cond = flowFinish.getChild("condition");
			if (cond!=null){
				List condList = cond.getChildren();
				if (condList.size()>0) {
					hasCondition = true;
				}
			}
			writeTime = "flowFinish";
			writeBackFieldList = flowFinish.getChildren("writeBackField");
			String insertFieldsText = flowFinish.getChildText("insertFields");
			if (insertFieldsText!=null) {
				insertFields = new JSONObject(insertFieldsText);
			}
		}
	} else {
		writeTime = "nodeFinish";
		list = root.getChildren("nodeFinish");
		if (list!=null) {
			ir = list.iterator();
		}
		while (ir!=null&&ir.hasNext()){
			Element nodeFinish = (Element)ir.next();
			Element internalNode = nodeFinish.getChild("internalName");
			String internalNodeText = internalNode.getText();
			if (internalName.equals(internalNodeText)) {                             //节点存在先删除
				writeBackName = nodeFinish.getChildText("writeBackForm");
				writeBackType = nodeFinish.getChildText("writeBackType");
				if (writeBackType==null) {
					writeBackType = String.valueOf(WorkflowPredefineDb.WRITE_BACK_UPDATE);
				}
				primaryKey = nodeFinish.getChildText("primaryKey");
				writeBackFieldList = nodeFinish.getChildren("writeBackField");
				Element cond = nodeFinish.getChild("condition");
				if (cond!=null){
					List condList = cond.getChildren();
					if (condList.size()>0)
						hasCondition = true;
				}
				String insertFieldsText = nodeFinish.getChildText("insertFields");
				if (insertFieldsText!=null) {
					insertFields = new JSONObject(insertFieldsText);
				}
			}
		}
	}
}

if (!"".equals(writeBackName)){
	fd = fd.getFormDb(writeBackName);
	irFields = fd.getFields().iterator();
	opts += "<option value='empty' >请选择</option>";
	while (irFields!=null&&irFields.hasNext()) {
		FormField ff = (FormField)irFields.next();
		int fieldType = ff.getFieldType();                                    // int、long、float、double、price类型
		if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                            
			opts += "<option value='" + ff.getName() + "' type='number'>" + ff.getTitle() + "</option>";
		}
		else {
			opts += "<option value='" + ff.getName() + "' type='string'>" + ff.getTitle() + "</option>";
		}
		// System.out.println(getClass() + " opts=" + opts);
	}
}
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
</head>
<body style="padding:0px; margin:0px">
<%
if ("clearup".equals(op)){
	Element del = null;
	String timestamp = ParamUtil.get(request,"timestamp");
	if (writeProp!=null&&!"".equals(writeProp)){
		doc = parser.build(new InputSource(new StringReader(writeProp)));
		root = doc.getRootElement();
		if ("flowFinish".equals(timestamp)){
			Element  flowFinish = root.getChild("flowFinish");
			if (flowFinish!=null) {
				root.removeContent(flowFinish);
			}
		} else {
			list = root.getChildren("nodeFinish");
			if (list!=null) {
				ir = list.iterator();
			}
			while (ir!=null&&ir.hasNext()){
				Element nodeFinish = (Element)ir.next();
				String  internalNodeText = nodeFinish.getChildText("internalName");
				if (internalName.equals(internalNodeText)){                             //节点存在先删除
					del = nodeFinish;
				}
			}
			if (del!=null) {
				root.removeContent(del);
			}
		}
	}

	if (doc != null) {
		Format format = Format.getPrettyFormat();
		ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
		XMLOutputter xmlOut = new XMLOutputter(format);
		xmlOut.output(doc, byteRsp);
		writeProp = byteRsp.toString("utf-8");
		byteRsp.close();
		wpd.setWriteProp(writeProp);
		boolean re = wpd.save();
		if (re) {
			out.print(StrUtil.jAlert_Redirect("操作成功", "提示", "flow_designer_write_back.jsp?flowTypeCode=" + flowTypeCode + "&internalName=" + internalName));
		} else {
			out.print(StrUtil.jAlert_Redirect("操作失败", "提示", "flow_designer_write_back.jsp?flowTypeCode=" + flowTypeCode + "&internalName=" + internalName));
		}
	}
	else {
		out.print(StrUtil.jAlert_Redirect("操作成功", "提示", "flow_designer_write_back.jsp?flowTypeCode=" + flowTypeCode + "&internalName=" + internalName));
	}
	return;
}
else if ("save".equals(op)){
	WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
	boolean re = wpm.saveWriteProp(request);
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功","提示","flow_designer_write_back.jsp?flowTypeCode="+flowTypeCode+"&internalName="+internalName));
	} else {
		out.print(StrUtil.jAlert_Redirect("操作失败","提示","flow_designer_write_back.jsp?flowTypeCode="+flowTypeCode+"&internalName="+internalName));
	}
	return;
}
else if ("getActionNode".equals(op)){
	String flowString = ParamUtil.get(request, "flowString");
	WorkflowDb wf = new WorkflowDb();
	Vector actionVector = wf.getActionsFromString(flowString);
	Iterator actionIterator = actionVector.iterator();
	String actionopts = "<option value=''>请选择</option>";
	while (actionIterator.hasNext()) {
		WorkflowActionDb wa = (WorkflowActionDb)actionIterator.next();
		actionopts += "<option value='" + wa.getInternalName() + "'>" + wa.getJobName() + "：" + wa.getTitle() + "</option>";
	}
	out.print(actionopts);
	return;
}
%>
<div class="spacerH"></div>
<form id="form1" name="form1" action="flow_designer_write_back.jsp?op=save" method="post" onSubmit="return submitValidation()">
	<div style="margin-bottom:5px; text-align:center">
	<input type="button" value="清除" class="btn" onClick="clearup()"/>
    <input type="button" value="外部" class="btn" style="margin-left:20px" onClick="window.location.href='flow_designer_write_back_db.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=<%=internalName%>'"/>
    <input type="submit" value="保存" class="btn" style="margin-left:20px"/>
    </div>
	<fieldset style="padding-left:10px;">
		<legend>回写模块设置</legend>
	          回写表单：
              <select id="relateCode" name="relateCode" onChange="getSelCurrVal()" style="width:190px">
	          		<option value="empty">请选择</option>
	          		<%
	         			// com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
						String sql = "select code from form order by code asc"; // orders asc";
						ir = fd.list(sql).iterator();
						while (ir!=null&&ir.hasNext()) {
							 fd = (FormDb)ir.next();
						%>
	          				<option value="<%=fd.getCode()%>"><%=fd.getName()%></option>
	          			<%
						}
					%>
	        </select>
	     <br/>
         回写方式：
         <select id="writeBackType" name="writeBackType">
         <option value="<%=WorkflowPredefineDb.WRITE_BACK_UPDATE%>" selected>仅更新数据</option>
         <option value="<%=WorkflowPredefineDb.WRITE_BACK_INSERT%>">仅插入数据</option>
         <option value="<%=WorkflowPredefineDb.WRITE_BACK_UPDATE_INSERT%>">更新并插入新数据</option>
         </select>
         <br />
	     回写时机：
	     <input style="margin-top:5px;" type="radio"  id="timestamp1" name="timestamp" value="flowFinish" <%="".equals(internalName)?"checked=\"true\"":""%> onClick="onFlowFinishClick()"/>流程结束
	     <input style="margin-top:5px;" type="radio"  id="timestamp2" name="timestamp" value="nodeFinish" <%=!"".equals(internalName)?"checked=\"true\"":""%> onClick="onActionNodeClick()"/>核定结点通过<br/>
	     <div id="show" style="<%="".equals(internalName)?"display:none;":"" %>">
	     选择节点：<select id="actionNode" name="internalName" style="width:190px;" onChange="onActionNodeChange()">
    			</select>
    	 </div>
    	 <div style="background-color:#dbe1f3;margin-top:10px;height:30px;width:100%;">
    	 	<img src="images/combination.png" style="margin-bottom:-5px;margin-top:5px;"/>&nbsp;<a href="javascript:;" onClick="openCondition()">配置条件</a>&nbsp;
    	 	<%if (hasCondition){ %>
    	 	<img src="images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;" id="imgId"/>
    	 	<%} %>
    	 </div>
		<img style="margin-top:5px;cursor:pointer;" align="absMiddle" onClick="addField()" src="<%=SkinMgr.getSkinPath(request)%>/images/write-back-add.png" />&nbsp;&nbsp;&nbsp;&nbsp;
		<img style="margin-top:5px;cursor:pointer;" align="absMiddle" onClick="delField()" src="<%=SkinMgr.getSkinPath(request)%>/images/write-back-close.png" />&nbsp;&nbsp;&nbsp;&nbsp;
		<br/>
		<div id="repeat">
			<%
				int count = 1;
				Iterator wrIterator = null;
				if (writeBackFieldList!=null){
					wrIterator = writeBackFieldList.iterator();
					count = writeBackFieldList.size();
				 }
				if (wrIterator!=null){
					int i = 0;
					while(wrIterator.hasNext()){
						Element e = (Element)wrIterator.next();
						String field = e.getAttributeValue("fieldName");
					%>
					<div>
					<span style="margin-top:5px;">设置字段值为：</span>
					<select id="formfield<%=i%>" name="formfield<%=i%>" style="margin-top:5px;width:160px"  >
						<%=opts %>
					</select>
					<textarea style="margin-top:5px;" rows="2" cols="30" readOnly="true" name="math<%=i%>" id="math<%=i%>" onClick="openRelateWin(this, '<%=i%>')"></textarea>
					</div>
					<script>
					o("formfield<%=i%>").value="<%=field%>";
					o("math<%=i%>").value="<%=e.getChildText("writeBackMath")%>";
					</script>
					<%	
						i++;
					}
				} else {
					%>
					<div>
					<span style="margin-top:5px;">设置字段值为：</span>
					<select id="formfield0" name="formfield0" style="margin-top:5px;width:160px"  >
						<%=opts %>
					</select>
					<textarea style="margin-top:5px;" rows="2" cols="30" readonly="true" name="math0" id="math0" onClick="openRelateWin(this, '0')"></textarea>
					</div>
					<% 
				}
			%>
		</div>
	</fieldset>
	<input type="hidden" id="repeatCount" name="repeatCount" value="<%=count%>"/>
	<!--<input type="hidden" name="internalName" value="" />-->
	<input type="hidden" name="flowTypeCode" value="<%=flowTypeCode%>"/>
	<input type="hidden" name="condition"  id="condition"/>

	<fieldset style="padding-left:10px;">
		<legend>插入模块设置</legend>
        主键字段&nbsp;&nbsp;
        <select id="primaryKey" name="primaryKey" title="仅插入主键字段记录不重复的数据">
        <%=opts %>
        </select>
        <br />
        <div id="insertTableFields">
        
        </div>
    </fieldset>
    
</form>
</BODY>
<script type="text/javascript">
<!--
	var formFieldData="<%=opts%>";
	// var count = "<%=count%>";
	function getSelCurrVal(){
		var relateCode = $("#relateCode").val();
		$.ajax({
			type: "post",
			url: "flow_designer_write_back.jsp",
			data: {
				op: "field",
		  		formCode: relateCode,
	  			flowTypeCode:"<%=flowTypeCode%>"
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				data = $.parseJSON(data);
				formFieldData = data.opts;
				var num = $("#repeat").children("div").length;
				for(i=0;i<num;i++){
					$("#formfield"+i).empty();
					$("#formfield"+i).append(data.opts);
					$("#math"+i).val("");
				}
				
				$('#primaryKey').empty();
				$('#primaryKey').append(data.opts);
				
				var str = "";
				var k = 1000;
				var fields = data.fields;
				for (i=0; i<fields.length; i++) {
					k += i;
					str += "<div style='line-height:30px; clear:both;'>";
					str += "<span id='formfield" + k + "' name='" + fields[i].name + "' type='" + fields[i].type + "' style='float:left; width:120px'>" + fields[i].title + "</span>";
					str += "<input readonly='true' kind='insert' name='" + fields[i].name + "_formula' id='math" + k + "' onclick=\"openRelateWin(this, '" + k + "')\" style='float:left' />";
					str += "</div>";
				}
				$('#insertTableFields').html(str);
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
			}
		});	
	}
	/**
	function changeField(obj){
		var formField = $(obj).val();
		if ("empty"!=formField){
			$(obj).next().val("{$"+formField+"}=");
		} else {
			$(obj).next().val("");
		}
	}*/
	function addField(){
	 	var relateCode = $("#relateCode").val();
	 	if (relateCode == "empty"){
	 		jAlert("请选择回写表单","提示");
	 		return ;
	 	}
	 	var count = $("#repeat").children("div").length;
	 	var last = $("#repeat").children("div").last().clone();
	 	$("#repeat").append(last);
	 	var select = $("#repeat").children().last().find("select");
	 	var textarea = $("#repeat").children().last().find("textarea");
	 	$(select).attr("id","formfield"+count);
	 	$(select).attr("name","formfield"+count);
	 	$(select).empty();
	 	$(select).append(formFieldData);
	 	$(textarea).attr("id","math"+count);
	 	$(textarea).attr("name","math"+count);
	 	$(textarea).text("");
	 	count++;
	 	$("#repeatCount").val($("#repeat").children("div").length);
	 }
	 function delField(){
	 	if ($("#repeat").children().length!=1){
	 		var last = $("#repeat").children("div").last().remove();
	 		$("#repeatCount").val($("#repeat").children("div").length);
	 	}
	 }
	 
	 function openRelateWin(obj, n){
	 	// n有可能不正确，因为math文本框可能会是clone得到的，会把onclick事件一起clone过来
	 	// 这样事件将为onclick="openRelateWin(obj, 0)"，导致n始终为0
	 	var objId = obj.id;
	 	n = objId.substring(4); 
	 	var optType = "number";
		if (!obj.getAttribute("kind")) {
			$("#formfield" + n).children().each(function(k) {
				if ($(this).val()==$("#formfield" + n).val()) {
					optType = $(this).attr("type");
					return;
				}
			});
		}
		else {
			optType = $('#formfield' + n).attr("type");
		}

		var isNum = true;
		if (optType=="string") {
			$("#math" + n).removeAttr("readonly");
			isNum = false;
		}
		else {
			$("#math" + n).attr("readonly", true);
		}
		
	 	var textAreaId  = $(obj).attr("id");
	 	var textAreaVal  = $(obj).val();
	 	var fieldVal = $(obj).prev().val();
	 	if (fieldVal=="empty"||fieldVal==null){
	 		return;
	 	}
	 	var test = /\+/g;
	 	textAreaVal = textAreaVal.replace(test,"\\u002B");
	 	var relateCode = $("#relateCode").val();
		
		var kind = "";
		if (obj.getAttribute("kind")!=null) {
			kind = obj.getAttribute("kind");
		}
	 	openWin("flow_designer_write_back_relate.jsp?kind=" + kind + "&isNum=" + isNum + "&mainCode=<%=flowTypeCode%>&relateCode="+relateCode+"&textAreaId="+textAreaId+"&textAreaVal="+encodeURI(textAreaVal), 800, 600);
	 }
	 
	 function setMath(id,content){
	 	$("#"+id).val(content);
	 }
	 
	 function openCondition(){
	 	var writeBackCode = $("#relateCode").val();
	 	var internalName = $("#actionNode").val();
	 	if ("empty"!=writeBackCode&&writeBackCode!=null){
	 		openWin("write_back_condition.jsp?mainCode=<%=flowTypeCode%>&writeBackCode="+writeBackCode+"&internalName="+internalName, 1024, 768);
	 	}
	 	else {
	 		jAlert("请选择回写表单！", "提醒");
	 	}
	 }
	 
	 function submitValidation(){
	 	var relateCode = $("#relateCode").val();
	 	if (relateCode == "empty"){
	 		jAlert("请选择回写表单","提示");
	 		return false;
	 	}
	 	var count = $("#repeat").children("div").length;
	 	var arr = new Array();
	 	for ( i=0;i<count;i++){
	 			var formfield = $("#formfield"+i).val();
		 		var mathVal = $("#math"+i).val();
		 		if (formfield == "empty"||mathVal==""){
					if ($('#writeBackType').val()=="<%=WorkflowPredefineDb.WRITE_BACK_UPDATE%>" || $('#writeBackType').val()=="<%=WorkflowPredefineDb.WRITE_BACK_UPDATE_INSERT%>") {
		 				jAlert("请设置字段值","提示");
			 			return false;
					}
		 		} else {
		 			arr[i]=formfield;
		 		}
	 	}
	 	return checkRepeat(arr);
	 }
	 function checkRepeat(arr){
	 	 var narr=arr.sort();
		 for(i=0;i<narr.length;i++){
		    if (narr[i]==narr[i+1]){
		        jAlert("字段值重复","提示");
	 			return false;
		    }
		 }
	 }

	 function setCondition(s){
	 	$("#condition").val(s);
	 }

	 function getFlowString() {
		return window.parent.getFlowString();
	}

	function getActionNode() {
		var flowStr = getFlowString();
		$("#show").css("display","");
		$.ajax({
			type: "post",
			url: "flow_designer_write_back.jsp",
			data : {
				op: "getActionNode",
	  			flowTypeCode:"<%=flowTypeCode%>",
				flowString: flowStr
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				//ShowLoading();
			},
			success: function(data, status){
				$("#actionNode").html(data);
			},
			complete: function(XMLHttpRequest, status){
				// HideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});	
	}
	
	function onFlowFinishClick(){
		// $("#show").css("display","none");
		window.location.href = "flow_designer_write_back.jsp?flowTypeCode=<%=flowTypeCode%>";
	}
	
	function onActionNodeClick() {
		if (""!="<%=internalName%>") {
			window.location.href = "flow_designer_write_back.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=<%=internalName%>";
		}
		else {
			// getActionNode();
		 	// setTimeout("setNode()",500);            //设置方法延迟加载		
		 	// $("#show").show();
		 	window.location.href = "flow_designer_write_back.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=empty";
		}
	}
	
	function onActionNodeChange() {
		if ("empty"!=$("#actionNode").val() && ""!=$("#actionNode").val()) {
			window.location.href = "flow_designer_write_back.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=" + $("#actionNode").val();
		}
	}
	
	$(document).ready(function (){	
		o("relateCode").value="<%=writeBackName%>";		
		// if ("flowFinish"=="<%=writeTime%>"){
		if (""=="<%=internalName%>"){
		 	o("timestamp1").checked=true;
		} 
		else {
			o("timestamp2").checked=true;
			getActionNode();
			if ("empty"!="<%=internalName%>") {
				setTimeout("setNode()",500);            //设置方法延迟加载
			}
		}
		<%if (!"".equals(writeBackName)) {%>
		initInsertFields();
		<%}%>
		
		$('#writeBackType').val("<%=writeBackType%>");
		$('#primaryKey').val("<%=primaryKey%>");
	})
	
	function initInsertFields() {
		var relateCode = $("#relateCode").val();
		$.ajax({
			type: "post",
			url: "flow_designer_write_back.jsp",
			data: {
				op: "getInsertFields",
		  		formCode: relateCode,
	  			flowTypeCode:"<%=flowTypeCode%>"
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				data = $.parseJSON(data);
				
				var insertFields = <%=insertFields%>;							
				var str = "";
				var k = 1000;
				var fields = data.fields;
				for (i=0; i<fields.length; i++) {
					k += i;
					str += "<div style='line-height:30px; clear:both;'>";
					str += "<span id='formfield" + k + "' name='" + fields[i].name + "' type='" + fields[i].type + "' style='float:left; width:120px'>" + fields[i].title + "</span>";
					var val = eval("insertFields." + fields[i].name);
					console.log(fields[i].name + " val=" + val);
					
					str += "<input readonly='true' kind='insert' name='" + fields[i].name + "_formula' id='math" + k + "' onclick=\"openRelateWin(this, '" + k + "')\" style='float:left' value=\"" + val + "\" />";
					str += "</div>";
				}
				$('#insertTableFields').html(str);
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
			}
		});		
	}
	
	function setNode(){
		o("actionNode").value="<%=internalName%>";
	}
	function clearup(){
		var  timestamp = $("input:checked").val();
		var  internalName = $("#actionNode").val();
		window.location.href="flow_designer_write_back.jsp?op=clearup&flowTypeCode=<%=flowTypeCode%>&internalName="+internalName+"&timestamp="+timestamp;
	}
--></script>
</HTML>