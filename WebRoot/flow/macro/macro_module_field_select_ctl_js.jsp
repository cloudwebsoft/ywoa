<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.util.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "java.util.regex.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "org.json.*"%>
<%
Privilege pvg = new Privilege();
int flowId = ParamUtil.getInt(request, "flowId", -1);
String fieldName = ParamUtil.get(request, "fieldName");
String formCode = ParamUtil.get(request, "formCode");
boolean isReadonly = ParamUtil.getBoolean(request, "isReadonly", false);
String pageType = ParamUtil.get(request, "pageType");

if ("show".equals(pageType)) {
    return;
}

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
FormField ff = fd.getFormField(fieldName);

String sourceFormCode="", byFieldName="", showFieldName="", filter="";
boolean canManualInput = false;
boolean isMulti = false;
boolean isSimilar = false;
JSONArray mapAry = new JSONArray();
String strDesc = StrUtil.getNullStr(ff.getDescription());
// 向下兼容
if ("".equals(strDesc)) {
   	strDesc = ff.getDefaultValueRaw();
}            
try {
	strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
	JSONObject json = new JSONObject(strDesc);
	sourceFormCode = json.getString("sourceFormCode");
    byFieldName = json.getString("idField");
    showFieldName = json.getString("showField");
	mapAry = (JSONArray)json.get("maps");    
	filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));
	
	if (json.has("canManualInput")) {
		canManualInput = json.getBoolean("canManualInput");
	}
	if (json.has("isSimilar")) {
		isSimilar = json.getBoolean("isSimilar");
	}	
	if (json.has("isMulti")) {
		isMulti = json.getBoolean("isMulti");
	}
}
catch (JSONException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

String op = ParamUtil.get(request, "op");
if ("onSelect".equals(op)) {
    JSONArray ary = ModuleFieldSelectCtl.getOnSelect(request, ff, fd);
	out.print(ary);
	return;
}
else if ("get".equals(op)) {
	response.setHeader("Content-Type", "application/json");
	JSONArray ary = ModuleFieldSelectCtl.getAjaxOptions(request, ff);
	out.print(ary);
	return;
}

int mode = ParamUtil.getInt(request, "mode", 0); // 默认为下拉菜单方式
ArrayList<String> fieldList = new ArrayList();
if (mode==0) {
	%>
	var condStr = "";
	<%
	// 处理filter，解析出其中的主表字段
	Pattern p = Pattern.compile(
			"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	Matcher m = p.matcher(filter);
	while (m.find()) {
		String fieldNm = m.group(1);
	    if (fieldNm.equals("cwsCurUser") || fieldNm.equals("curUser") 
	         	|| fieldNm.equals("curUserDept") || fieldNm.equals("curUserRole") || fieldNm.equals("admin.dept") || fieldNm.equals("parentId")) {
	       	continue;
		}
		fieldList.add(fieldNm);
		// 当条件为包含时，fieldName以@开头
		if (fieldNm.startsWith("@")) {
			fieldNm = fieldNm.substring(1);
		}			
	    %>
		if (o("<%=fieldNm%>")==null) {
			alert("条件<%=fieldNm%>不存在！");
		}
		else {
			condStr += "&<%=fieldNm%>=" + encodeURI(o("<%=fieldNm%>").value);
		}
	    <%
	}
	%>
	// 组装filter中会用到的字段值
	function getCondFieldsValue() {
		var json = {};
		<%
			for (int k=0; k<fieldList.size(); k++) {
				String fname = fieldList.get(k);
		%>
				json["<%=fname%>"] = o("<%=fname%>").value;
		<%
			}
		%>
		return json;
	}
	$(function () {
		<%
		// defaultOptVal是当request传入的参数，参数名为ff.getName()，用于从菜单项上带入值，如传入项目的ID
		// 在ModuleFieldSelectCtl中写js时，编辑记录时也会带入值
		String defaultOptVal = ParamUtil.get(request, "defaultOptVal");
		String defaultOptText = ParamUtil.get(request, "defaultOptText");
		%>
		if (o('<%=fieldName %>').tagName=='SELECT') {
			$("#<%=fieldName %>").select2({
				<%if (canManualInput) { %>
				tags: true,
				<%} %>
				<%if (isMulti) { %>
				multiple: true,		
				<%}%>
				ajax: {
		    		url: "<%=request.getContextPath()%>/flow/macro/macro_module_field_select_ctl_js.jsp?op=get&fieldName=<%=fieldName %>&formCode=<%=formCode %>&flowId=<%=flowId %>",
				  	dataType: 'json',
				  	delay: 250,
				  	data: function (params) {
						var d = getCondFieldsValue();
						d["q"] = params.term;
						return d;
				  	},
				  	cache: false,
				  	processResults: function (data, params) {
					    return {
					    	results: data
					    };
				  	}
				},
				// tags: true,			// 允许手动添加
				placeholder: '请输入关键词',	// 默认文字提示
		  		language: 'zh-CN',		
				escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
				<%if (isSimilar) { %>
				minimumInputLength: 2,
				<%}
				else if (isMulti) {%>
				minimumInputLength: 0,
				<%}
				else {%>
				minimumInputLength: 1,
				<%} %>
		  		templateResult: function formatRepo(repo){return repo.text},
		  		templateSelection: function formatRepoSelection(repo){return repo.text} 
			});
		}
		
		<%
		if (!defaultOptText.equals("")) {
			// System.out.println(getClass() + " " + defaultOptVal + " " + defaultOptText);
			%>
			var deOptVal = "<%=defaultOptVal %>".split(",");
			var deOptText = "<%=defaultOptText %>".split(",");
			// $("#<%=fieldName %>").val(deOptVal).trigger('change'); // 对于ajax型的无效
			for (var i=0; i < deOptVal.length; i++) {
				var option = new Option(deOptText[i], deOptVal[i], true, true);
    			$("#<%=fieldName %>").append(option).trigger('change');		
    		}	
			<%
		}
		%>
		<%if (!defaultOptText.equals("")) {
		%>
			// 根据request传入的值进行映射
			onSelect<%=fieldName %>("<%=defaultOptVal %>", "<%=defaultOptText %>");
		<%
		}%>
		 
		// module_show.jsp中，不需要處理事件
		if (o("<%=fieldName %>").tagName=='SELECT') {
			$("#<%=fieldName %>").on("select2:select",function(e){
				var id = e.params.data.id;
				var text= e.params.data.text;
				<%if (isReadonly) { %>
				// 如果是只读模式，则因为select2新版中没有只读模式，所以此处强制恢复为request传过来的默认值
				var defaultOptVal = "<%=defaultOptVal %>";
				if (defaultOptVal!="") {
					if (id!=defaultOptVal) {
						$('#<%=fieldName %>').html('<option value="<%=defaultOptVal %>"><%=defaultOptText %></option>');
						return;
					}
				}
				<%} %>
				onSelect<%=fieldName %>(id, text);
			});
		}
		
		<%
		if (isReadonly) {
		%>
			// $("#<%=fieldName %>").prop("disabled", true);	//设置下拉框不可用
			// $("#<%=fieldName %>").select2("readonly", true);	 
		<%
		}
		%>
	});
<%
}
else {
	boolean isValueFromRequest = ParamUtil.getBoolean(request, "isValueFromRequest", false);
	// 如果为true，则表示从request中传入了参数，需要自动映射
	if (isValueFromRequest) {
		String value = ParamUtil.get(request, "value");
		String valueShow = ParamUtil.get(request, "valueShow");
		// System.out.println(getClass() + " value=" + value + " valueShow=" + valueShow);
		%>
		$(function() {
			onSelect<%=fieldName %>("<%=value%>", "<%=valueShow%>");
		});
		<%
	}
}
%>

// 选择后重新映射
function onSelect<%=fieldName %>(id, text) {
	<%if ("show".equals(pageType)) {%>
	return;
	<%}%>
	$.ajax({
		url: "<%=request.getContextPath() %>/flow/macro/macro_module_field_select_ctl_js.jsp",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
		type: "post",
		data: {
			op: "onSelect",
			fieldName: "<%=fieldName %>",
			formCode: "<%=formCode %>",
			flowId: "<%=flowId %>",
			id: id,
			text: text
		},
		dataType: "json",
		beforeSend: function(XMLHttpRequest) {
		},
		success: function(data, status) {
			 for(var i=0,l=data.length; i < l; i++) {
			 	var json = data[i];
				// console.log(json);
			 	// 不替换本表单域选择宏控件
			 	if (json.fieldName!="<%=fieldName %>") {
				    if(json.isMacro){
				    	replaceValue(json.fieldName, json.setValue, json.value);
				    }else{
				    	if (o(json.fieldName).type == "checkbox") {
				    		if (o(json.fieldName).value == json.setValue) {
				    			o(json.fieldName).checked = true;
				    		}
				    		else {
				    			o(json.fieldName).checked = false;
				    		}
				    	}
				    	else if (o(json.fieldName).type == "radio") {
				    		setRadioValue(json.fieldName, json.setValue);
				    	}
				    	else {
							o(json.fieldName).value = json.setValue;
						}
					}
				}
			 }
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
			alert(textStatus);
		}
	});
}
