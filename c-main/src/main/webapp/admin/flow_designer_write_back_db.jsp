<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.file.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.query.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.xml.sax.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="org.jdom.output.*"%>
<%@page import="org.jdom.Document"%>
<%@page import="com.cloudwebsoft.framework.db.*"%>
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
FormDb fd = new FormDb();
fd = fd.getFormDb(flowTypeCode);
Iterator irFields = fd.getFields().iterator();
String opts = "";
String op = ParamUtil.get(request,"op");
String formCode = ParamUtil.get(request,"formCode");
if ("field".equals(op)){
	opts += "<option value='empty'>请选择</option>";
	String dbSource = ParamUtil.get(request, "dbSource");
	String sql = "select * from " + formCode;
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		for (int i = 1; i <= colCount; i++) {
			int colType = rm.getColumnType(i);
			int fieldType = QueryScriptUtil.getFieldTypeOfDBType(colType);
			if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                            
				opts += "<option value='" + rm.getColumnName(i) + "' type='number'>" + rm.getColumnName(i) + "</option>";
			}
			else {
				opts += "<option value='" + rm.getColumnName(i) + "' type='string'>" + rm.getColumnName(i) + "</option>";
			}			
		}
		out.print(opts);
	}
	finally {
		conn.close();
	}
	return;
}

SAXBuilder parser = new SAXBuilder();
String writeProp = wpd.getWriteDbProp();                   //从数据库获取回写
org.jdom.Document doc = null;
Element root = null;
Element child = null;
List list = null;
Iterator ir = null;

String dbSource = "";
String writeBackName = "";
String writeTime = "";
boolean hasCondition = false;
List writeBackFieldList = null;
if (writeProp!=null&&!"".equals(writeProp)){
	doc = parser.build(new InputSource(new StringReader(writeProp)));
	root = doc.getRootElement();
	if ("".equals(internalName)){
		Element flowFinish = root.getChild("flowFinish");
		if (flowFinish!=null){
			dbSource = flowFinish.getChildText("dbSource");
			writeBackName = flowFinish.getChildText("writeBackForm");
			Element cond = flowFinish.getChild("condition");
			if (cond!=null){
				List condList = cond.getChildren();
				if (condList.size()>0)
					hasCondition = true;
			}
			writeTime = "flowFinish";
			writeBackFieldList = flowFinish.getChildren("writeBackField");
		}
	} else {
		writeTime = "nodeFinish";		
		list = root.getChildren("nodeFinish");
		if (list!=null)	
			ir = list.iterator();
		while (ir!=null&&ir.hasNext()) {
			Element nodeFinish = (Element)ir.next();
			Element internalNode = nodeFinish.getChild("internalName");
			String internalNodeText = internalNode.getText();
			if (internalName.equals(internalNodeText)){                             // 节点存在先删除
				dbSource = nodeFinish.getChildText("dbSource");
				writeBackName = nodeFinish.getChildText("writeBackForm");
				writeBackFieldList = nodeFinish.getChildren("writeBackField");
				Element cond = nodeFinish.getChild("condition");
				if (cond!=null){
					List condList = cond.getChildren();
					if (condList.size()>0)
						hasCondition = true;
				}
			}
		}
	}
}

// System.out.println(getClass() + " writeBackName=" + writeBackName);
if (!"".equals(writeBackName)){
	opts += "<option value='empty' >请选择</option>";
	
	String sql = "select * from " + writeBackName;
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		for (int i = 1; i <= colCount; i++) {
			int colType = rm.getColumnType(i);
			int fieldType = QueryScriptUtil.getFieldTypeOfDBType(colType);
			if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                            
				opts += "<option value='" + rm.getColumnName(i) + "' type='number'>" + rm.getColumnName(i) + "</option>";
			}
			else {
				opts += "<option value='" + rm.getColumnName(i) + "' type='string'>" + rm.getColumnName(i) + "</option>";
			}			
		}
	}
	finally {
		conn.close();
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
			if (flowFinish!=null)
				root.removeContent(flowFinish);
		} else {
			list = root.getChildren("nodeFinish");
			if (list!=null)	
				ir = list.iterator();
			while (ir!=null&&ir.hasNext()){
				Element nodeFinish = (Element)ir.next();
				String  internalNodeText = nodeFinish.getChildText("internalName");
				if (internalName.equals(internalNodeText)){                             //节点存在先删除
					del = nodeFinish;
				}
			}
			if (del!=null)
				root.removeContent(del);
		}
	}
	Format format = Format.getPrettyFormat();
    ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
    XMLOutputter xmlOut = new XMLOutputter(format);
    xmlOut.output(doc, byteRsp);
    writeProp = byteRsp.toString("utf-8");
	byteRsp.close();
	wpd.setWriteDbProp(writeProp);
	boolean re = wpd.save();
	 if (re)
	 	out.print(StrUtil.jAlert_Redirect("操作成功","提示","flow_designer_write_back_db.jsp?flowTypeCode="+flowTypeCode+"&internalName="+internalName));
	 else 
		out.print(StrUtil.jAlert_Redirect("操作失败","提示","flow_designer_write_back_db.jsp?flowTypeCode="+flowTypeCode+"&internalName="+internalName));
	return;
}
if ("save".equals(op)){
	dbSource = ParamUtil.get(request, "dbSource");
	String writeBackFormCode = ParamUtil.get(request,"relateCode");           //回写表单
	String writeBackTime = ParamUtil.get(request,"timestamp");                //回写时间
	int repeatCount = 	ParamUtil.getInt(request,"repeatCount");              //回写字段数量     <![CDATA[文本内容]]>
	String conditionStr = ParamUtil.get(request,"condition");
	Element del = null;
	Element con = null;
	if (writeProp!=null&&!"".equals(writeProp)){
		doc = parser.build(new InputSource(new StringReader(writeProp)));
		root = doc.getRootElement();
		if ("flowFinish".equals(writeBackTime)){                                
				Element  flowFinish = root.getChild("flowFinish");
				if (flowFinish!=null){
					String writeBackOld = flowFinish.getChildText("writeBackForm");
					if (writeBackOld.equals(writeBackFormCode))
						con = flowFinish.getChild("condition");
					root.removeContent(flowFinish);
				}
				child = new Element("flowFinish");
		} else {
				list = root.getChildren("nodeFinish");
				if (list!=null)	
					ir = list.iterator();
				while (ir!=null&&ir.hasNext()){
					Element nodeFinish = (Element)ir.next();
					Element internalNode = nodeFinish.getChild("internalName");
					String internalNodeText = internalNode.getText();
					if (internalName.equals(internalNodeText)){ // 节点存在先删除
						String writeBackOld = nodeFinish.getChildText("writeBackForm");
						if (writeBackOld.equals(writeBackFormCode))
							con = nodeFinish.getChild("condition");
						del = nodeFinish;
					}
				}
				child = new Element("nodeFinish");
				Element internalNode = new Element("internalName");
				internalNode.setText(internalName);
				child.addContent(internalNode);
		}
		if (del!=null)
			root.removeContent(del);
		
	} else {  // 如果writeProp为空，新建root元素
		doc = new Document();
		root = new Element("root");
		doc.addContent(root);
		if ("flowFinish".equals(writeBackTime)){
			child = new Element("flowFinish");
		} else {
			child = new Element("nodeFinish");
			Element internalNode = new Element("internalName");
			internalNode.setText(internalName);
			child.addContent(internalNode);
		}
	}
	
	child.setAttribute("id",RandomSecquenceCreator.getId(10));
	root.addContent(child);
	
	Element eDbSource = new Element("dbSource");
	eDbSource.setText(dbSource);
	child.addContent(eDbSource);
	
	Element writeBackForm = new Element("writeBackForm");
	writeBackForm.setText(writeBackFormCode);
	child.addContent(writeBackForm);
	Element writeBackTimeStamp = new Element("writeBackTime");
	writeBackTimeStamp.setText(writeBackTime);
	child.addContent(writeBackTimeStamp);
	
	for (int i=0;i<repeatCount;i++){
		String field = ParamUtil.get(request,"formfield"+i);
		String math = ParamUtil.get(request,"math"+i);
		// 判断是否为字符串型，如果是则加单引号
		int fieldType = WorkflowUtil.getColumnType(dbSource, writeBackFormCode, field);                               // int、long、float、double、price类型
		if (fieldType==FormField.FIELD_TYPE_DOUBLE||fieldType==FormField.FIELD_TYPE_FLOAT||fieldType==FormField.FIELD_TYPE_INT||fieldType==FormField.FIELD_TYPE_LONG||fieldType==FormField.FIELD_TYPE_PRICE){                            
			;
		}
		else {
			if (!math.startsWith("'")) {
				math = StrUtil.sqlstr(math);
			}
		}
		
		Element writeBackField = new Element("writeBackField");
		writeBackField.setAttribute("fieldName",field);
		child.addContent(writeBackField);
		Element writeBackMath = new Element("writeBackMath");
		writeBackMath.setText(math);
		writeBackField.addContent(writeBackMath);
	}
	if (!"".equals(conditionStr)){
		 org.jdom.Document cdoc = parser.build(new InputSource(new StringReader(conditionStr)));
		 Element croot = cdoc.getRootElement();
		 Element condition = croot.getChild("condition");
		 child.addContent(condition.detach());
	} else {
		if (con!=null)
			child.addContent(con.detach());
	}
	Format format = Format.getPrettyFormat();
    ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
    XMLOutputter xmlOut = new XMLOutputter(format);
    xmlOut.output(doc, byteRsp);
    writeProp = byteRsp.toString("utf-8");
	byteRsp.close();
	wpd.setWriteDbProp(writeProp);
	boolean re = wpd.save();
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功","提示","flow_designer_write_back_db.jsp?flowTypeCode="+flowTypeCode+"&internalName="+internalName));
	else 
		out.print(StrUtil.jAlert_Redirect("操作失败","提示","flow_designer_write_back_db.jsp?flowTypeCode="+flowTypeCode+"&internalName="+internalName));
	return;
}
if ("getActionNode".equals(op)){
	String flowString = ParamUtil.get(request, "flowString");
	WorkflowDb wf = new WorkflowDb();
	Vector actionVector = wf.getActionsFromString(flowString);
	Iterator actionIterator  = actionVector.iterator();
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
<form id="form1" name="form1" action="flow_designer_write_back_db.jsp?op=save" method="post" onSubmit="return submitValidation()">
	<div style="margin-bottom:5px; text-align:center">
    <input type="button" value="清除" class="btn" onclick="clearup()"/>
    <input type="button" value="模块" class="btn" style="margin-left:20px" onclick="window.location.href='flow_designer_write_back.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=<%=internalName%>'"/>
    <input type="submit" value="保存" class="btn" style="margin-left:20px"/>
    </div>
	<fieldset style="padding-left:10px;">
		<legend>回写数据库设置</legend>
        <div style="margin-bottom:5px;">
                     数据源：
        <select id="dbSource" name="dbSource">
        <option value="">请选择</option>
        <%
        cn.js.fan.web.Config cfg = new cn.js.fan.web.Config();
        Iterator irDb = cfg.getDBInfos().iterator();
        while (irDb.hasNext()) {
            DBInfo di = (DBInfo)irDb.next();
            %>
            <option value="<%=di.name%>" <%=di.isDefault?"selected":""%>><%=di.name%></option>
            <%
        }
        %>
        </select>
        </div>
        <script>
	$(function(){        
		$('#dbSource').val("<%=dbSource%>");
        $('#dbSource').change(function() {
			if ($(this).val()=="")
				return;
			// 取所选数据源的表名
			var str = "op=getTables&dbSource=" + $(this).val();
			var myAjax = new cwAjax.Request(
				"script_left.jsp",
				{
					method:"post", 
					parameters:str,
					onComplete:doGetTableOptions,
					onError:errFunc
				}
			);
		});
	  });
	  
		var errFunc = function(response) {
		    alert('Error ' + response.status + ' - ' + response.statusText);
		    alert(response.responseText);
		}
	  
		function doGetTableOptions(response) {
			var rsp = response.responseText.trim();
			// alert(rsp);
			$("#relateCode").empty();
			$("#relateCode").append(rsp);
		}	  
        </script>
	          回写表格：<select id="relateCode" name="relateCode" onchange="getSelCurrVal()" style="width:190px">
	          <option value="empty">请选择</option>
	          <%
	          if (!dbSource.equals("")) {
				try {
					JdbcTemplate jt = new JdbcTemplate(dbSource);
					Iterator irTable = jt.getTableNames().iterator();
					while (irTable.hasNext()) {
						String tableName = (String)irTable.next();
						%>
						<option value="<%=tableName%>"><%=tableName%></option>		
						<%
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}	
			  }          
	          %>
	        </select>
	     <br/>
	     回写时机：
	     <input style="margin-top:5px;" type="radio"  id="timestamp1" name="timestamp" value="flowFinish" <%="".equals(internalName)?"checked=\"true\"":""%> onClick="onFlowFinishClick()"/>流程结束
	     <input style="margin-top:5px;" type="radio"  id="timestamp2" name="timestamp" value="nodeFinish" <%=!"".equals(internalName)?"checked=\"true\"":""%> onClick="onActionNodeClick()"/>核定结点通过<br/>
	     <div id="show" style="<%="".equals(internalName)?"display:none;":"" %>">
	     选择节点：<select id="actionNode" name="internalName" style="width:190px;" onChange="onActionNodeChange()">
    			</select>
    	 </div>
    	 <div  style="background-color:#dbe1f3;margin-top:10px;height:30px;width:100%;">
    	 	<img src="images/combination.png" style="margin-bottom:-5px;margin-top:5px;"/>&nbsp;<a href="javascript:;" onclick="openCondition()">配置条件</a>&nbsp;
    	 	<%if (hasCondition){ %>
    	 	<img src="images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;" id="imgId"/>
    	 	<%} %>
    	 </div>
		<img style="margin-top:5px;cursor:pointer;" align="absMiddle" onclick="addField()" src="<%=SkinMgr.getSkinPath(request)%>/images/write-back-add.png" />&nbsp;&nbsp;&nbsp;&nbsp;
		<img style="margin-top:5px;cursor:pointer;" align="absMiddle" onclick="delField()" src="<%=SkinMgr.getSkinPath(request)%>/images/write-back-close.png" />&nbsp;&nbsp;&nbsp;&nbsp;
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
					<textarea style="margin-top:5px;" rows="2" cols="30" readonly="true" name="math<%=i%>" id="math<%=i%>" onclick="openRelateWin(this, '<%=i%>')"></textarea>
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
					<textarea style="margin-top:5px;" rows="2" cols="30" readonly="true" name="math0" id="math0" onclick="openRelateWin(this, '0')"></textarea>
					</div>
					<% 
				}
			%>
		</div>
	</fieldset>
	<input type="hidden" id="repeatCount" name="repeatCount" value="<%=count%>"/>
	<!--<input type="hidden" name="internalName" value="" />-->
	<input type="hidden" name="flowTypeCode" value="<%=flowTypeCode%>"/>
	<input type="hidden" name="condition" id="condition"/>
</form>
</BODY>
<script type="text/javascript"><!--
	var formFieldData="<%=opts%>";
	// var count = "<%=count%>";
	function getSelCurrVal(){
		var relateCode = $("#relateCode").val();
		$.ajax({
		type: "post",
		url: "flow_designer_write_back_db.jsp",
		data: {
			op: "field",
			dbSource: o("dbSource").value,
	  		formCode: relateCode,
  			flowTypeCode:"<%=flowTypeCode%>"
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			formFieldData = data;
			var num = $("#repeat").children("div").length;
			for(i=0;i<num;i++){
				$("#formfield"+i).empty();
				$("#formfield"+i).append(data);
				$("#math"+i).val("");
			}
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
	 		jAlert("请选择回写表格","提示");
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
	 
	 function delField() {
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
		$("#formfield" + n).children().each(function(k) {
			if ($(this).val().toLowerCase()==$("#formfield" + n).val().toLowerCase()) {
				optType = $(this).attr("type");
				return;
			}
		});		
		if (optType=="string") {
			$("#math" + n).removeAttr("readonly");
			return;
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
	 	openWin("flow_designer_write_back_db_relate.jsp?dbSource=" + o("dbSource").value + "&mainCode=<%=flowTypeCode%>&relateCode="+relateCode+"&textAreaId="+textAreaId+"&textAreaVal="+encodeURI(textAreaVal), 800, 600);
	 }
	 function setMath(id,content){
	 	$("#"+id).text(content);
	 }
	 function openCondition(){
	 	var writeBackCode = $("#relateCode").val();
	 	var internalName = $("#actionNode").val();
	 	if ("empty"!=writeBackCode&&writeBackCode!=null){
	 		openWin("write_back_db_condition.jsp?dbSource=" + o("dbSource").value + "&mainCode=<%=flowTypeCode%>&writeBackCode="+writeBackCode+"&internalName="+internalName, 800, 460);
	 	}
	 }
	 function submitValidation(){
		var dbSource = o("dbSource").value;
		if (dbSource == "") {
	 		jAlert("请选数据源","提示");
	 		return false;			
		}
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
		 			jAlert("请设置字段值","提示");
		 			return false;
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
			url: "flow_designer_write_back_db.jsp",
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
		window.location.href = "flow_designer_write_back_db.jsp?flowTypeCode=<%=flowTypeCode%>";
	}
	
	function onActionNodeClick() {
		if (""!="<%=internalName%>") {
			window.location.href = "flow_designer_write_back_db.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=<%=internalName%>";
		}
		else {
			// getActionNode();
		 	// setTimeout("setNode()",500);            //设置方法延迟加载		
		 	// $("#show").show();
		 	window.location.href = "flow_designer_write_back_db.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=empty";
		}
	}
	
	function onActionNodeChange() {
		if ("empty"!=$("#actionNode").val() && ""!=$("#actionNode").val()) {
			window.location.href = "flow_designer_write_back_db.jsp?flowTypeCode=<%=flowTypeCode%>&internalName=" + $("#actionNode").val();
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
	})
	function setNode(){
		o("actionNode").value="<%=internalName%>";
	}
	function clearup(){
		var  timestamp = $("input:checked").val();
		var  internalName = $("#actionNode").val();
		window.location.href="flow_designer_write_back_db.jsp?op=clearup&flowTypeCode=<%=flowTypeCode%>&internalName="+internalName+"&timestamp="+timestamp;
	}
--></script>
</HTML>