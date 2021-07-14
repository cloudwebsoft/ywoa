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
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudweb.oa.api.IModuleUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.api.IModuleFieldSelectCtl" %>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

	int flowId = ParamUtil.getInt(request, "flowId", -1);
	String fieldName = ParamUtil.get(request, "fieldName");
	String formCode = ParamUtil.get(request, "formCode");
	boolean isReadonly = ParamUtil.getBoolean(request, "isReadonly", false);
	String pageType = ParamUtil.get(request, "pageType");

	/*
	20210529因为实时映射，在module_show.jsp也需生效，故注释掉
	if ("show".equals(pageType)) {
		response.setContentType("text/javascript;charset=utf-8");
		return;
	}*/

	if ("".equals(formCode)) {
		response.setContentType("text/javascript;charset=utf-8");
		DebugUtil.e("macro_module_field_select_ctl_js.jsp", "formCode", "不能为空");
		return;
	}

	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	if (!fd.isLoaded()) {
		response.setContentType("text/javascript;charset=utf-8");
		DebugUtil.e("macro_module_field_select_ctl_js.jsp", "表单", "不存在，编码：" + formCode);
		return;
	}

	FormField ff = fd.getFormField(fieldName);
	if (ff == null) {
		response.setContentType("text/javascript;charset=utf-8");
		DebugUtil.e("macro_module_field_select_ctl_js.jsp", "字段", "不存在，字段名：" + fieldName + "，表单：" + fd.getName());
		return;
	}

	String sourceFormCode = "", byFieldName = "", showFieldName = "", filter = "";
	boolean canManualInput = false;
	boolean isMulti = false;
	boolean isSimilar = false;
	boolean isAjax = true;
	boolean isRealTime = false;
	JSONArray mapAry = new JSONArray();
	String strDesc = StrUtil.getNullStr(ff.getDescription());
	// 向下兼容
	if ("".equals(strDesc)) {
		strDesc = ff.getDefaultValueRaw();
	}
	try {
		MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
		IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
		strDesc = moduleFieldSelectCtl.formatJSONString(strDesc);
		JSONObject json = new JSONObject(strDesc);
		sourceFormCode = json.getString("sourceFormCode");
		byFieldName = json.getString("idField");
		showFieldName = json.getString("showField");
		mapAry = (JSONArray) json.get("maps");
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
		if (json.has("isAjax")) {
			isAjax = json.getBoolean("isAjax");
		}
		if (json.has("isRealTime")) {
			isRealTime = json.getBoolean("isRealTime");
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}

	String op = ParamUtil.get(request, "op");
	/*if ("onSelect".equals(op)) {
		response.setContentType("text/html;charset=utf-8");
		JSONArray ary = ModuleFieldSelectCtl.getOnSelect(request, ff, fd);
		out.print(ary);
		return;
	} else if ("get".equals(op)) {
		response.setContentType("text/html;charset=utf-8");
		response.setHeader("Content-Type", "application/json");
		JSONArray ary = ModuleFieldSelectCtl.getAjaxOptions(request, ff);
		out.print(ary);
		return;
	}*/

	response.setContentType("text/javascript;charset=utf-8");

	int mode = ParamUtil.getInt(request, "mode", ConstUtil.MODE_SELECT); // 默认为下拉菜单方式
	ArrayList<String> fieldList = new ArrayList<>();
	if (mode == ConstUtil.MODE_SELECT) {
		// 处理filter，解析出其中的主表字段
		Pattern p = Pattern.compile(
				"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(filter);
		while (m.find()) {
			String fieldNm = m.group(1);
			if ("cwsCurUser".equals(fieldNm) || "curUser".equals(fieldNm)
					|| "curUserDept".equals(fieldNm) || "curUserRole".equals(fieldNm) || "admin.dept".equals(fieldNm) || "parentId".equals(fieldNm)) {
				continue;
			}
			// 当条件为包含时，fieldName以@开头
			if (fieldNm.startsWith("@")) {
				fieldNm = fieldNm.substring(1);
			}
			fieldList.add(fieldNm);
		}
	%>
	// 组装filter中会用到的字段值
	function getCondFieldsValue() {
		var json = {};
		<%
			for (String fname : fieldList) {
		%>
		if (o("<%=fname%>")) {
			json["<%=fname%>"] = o("<%=fname%>").value;
		}
		else {
			console.error('条件字段:<%=fname%>在表单中不存在');
		}
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
			if (!<%=isAjax%>) {
				$sel2 = $("#<%=fieldName %>").select2();
			}
			else {
				$sel2 = $("#<%=fieldName %>").select2({
					<%if (canManualInput) { %>
					tags: true,	// 允许手动添加
					<%} %>
					<%if (isMulti) { %>
					multiple: true,
					<%}%>
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
					ajax: {
						// url: "<%=request.getContextPath()%>/flow/macro/macro_module_field_select_ctl_js.jsp?op=get&fieldName=<%=fieldName %>&formCode=<%=formCode %>&flowId=<%=flowId %>",
						url: "<%=request.getContextPath()%>/flow/macro/getAjaxOptions.do?fieldName=<%=fieldName %>&formCode=<%=formCode %>&flowId=<%=flowId %>",
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

					templateResult: function formatRepo(repo){return repo.text},
					templateSelection: function formatRepoSelection(repo){return repo.text}
				});
			}

			// 加载初始options，当select2为ajax型时无效，点击下拉箭头并不会显示这些初始options
			<%--$.ajax({
				url: "<%=request.getContextPath()%>/flow/macro/macro_module_field_select_ctl_js.jsp?op=get&fieldName=<%=fieldName %>&formCode=<%=formCode %>&flowId=<%=flowId %>",
				data: {
				},
				dataType:'json',
				success: function (data) {
					console.log(data);
					for (var d = 0; d < data.length; d++) {
						var item = data[d];
						var option = new Option(item.text, item.id, true, true);
						$sel2.append(option);
					}
					$sel2.trigger('change'); // 使用这个方法显示到select2上.
				}
			});--%>
		}

		<%
		if (isAjax && !"".equals(defaultOptText)) {
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

		if (!"".equals(defaultOptText)) {
			if (isRealTime) {
		%>
			// 根据request传入的值实时进行映射
			onSelect<%=fieldName %>("<%=defaultOptVal %>", "<%=defaultOptText %>");
		<%
			}
		}
		%>
		 
		// module_show.jsp中，不需要处理事件
		var ctlFieldName = "<%=fieldName %>";
		if (o(ctlFieldName).tagName=='SELECT') {
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
} else {
	// 窗口选择
	boolean isValueFromRequest = ParamUtil.getBoolean(request, "isValueFromRequest", false);
	// 如果为true，则表示从request中传入了参数，需要自动映射
	if (isValueFromRequest) {
		String value = ParamUtil.get(request, "value");
		String valueShow = ParamUtil.get(request, "valueShow");
%>
	$(function() {
		onSelect<%=fieldName %>("<%=value%>", "<%=valueShow%>");
	});
<%
	}

	// defaultOptVal是当request传入的参数，参数名为ff.getName()，用于从菜单项上带入值，如传入项目的ID
	// 在ModuleFieldSelectCtl中写js时，编辑记录时也会带入值
	String defaultOptVal = ParamUtil.get(request, "defaultOptVal");
	String defaultOptText = ParamUtil.get(request, "defaultOptText");
	if (!"".equals(defaultOptText)) {
		if (isRealTime) {
	%>
	$(function() {
		// 根据request传入的值实时进行映射
		onSelect<%=fieldName %>("<%=defaultOptVal %>", "<%=defaultOptText %>");
	});
	<%
		}
	}
	%>
	// 如果值发生改变，比如控件自身为被另一个表单域选择宏控件映射的字段
	$(function() {
		var ctlFieldName = "<%=fieldName%>";
		var oldValue = o(ctlFieldName).value;
		// console.log("oldValue=" + oldValue);
		setInterval(function(){
			if (oldValue != o(ctlFieldName).value) {
				console.log("newValue=" + o(ctlFieldName).value + " oldValue=" + oldValue);
				if (o(ctlFieldName).value!="") {
					var text = o(ctlFieldName + "_realshow").value;
					console.log("onSelect: id=" + o(ctlFieldName).value + " text=" + text);
					onSelect<%=fieldName %>(o(ctlFieldName).value, text);
				}
				oldValue = o(ctlFieldName).value;
			}
		},200);
	});
<%
}
%>

var isPageShow = false;
<%
	if (ConstUtil.PAGE_TYPE_SHOW.equals(pageType) || ConstUtil.PAGE_TYPE_FLOW_SHOW.equals(pageType)) {
%>
		isPageShow = true;
<%
	}

	IModuleUtil iModuleUtil = (IModuleUtil)SpringUtil.getBean("moduleUtilService");
	List<String> list = iModuleUtil.parseFieldNameInFilter(request, formCode, filter);
%>
// 选择后重新映射
function onSelect<%=fieldName %>(id, text) {
	var pageType = '<%=pageType%>';
	var ajaxData = {
		fieldName: "<%=fieldName %>",
		formCode: "<%=formCode %>",
		flowId: "<%=flowId %>",
		id: id,
		text: text,
		pageType: pageType
	}
<%
	for (String fName : list) {
%>
		var fName = '<%=fName%>';
		var obj = o(fName);
		var objVal = '';
		if (obj) {
			if (obj.tagName=='RADIO') {
				objVal = getRadioValue(fName);
			}
			else if (obj.tagName == "CHECKBOX") {
				objVal = getCheckboxValue(fName);
			}
			else {
				objVal = obj.value;
			}
		}
		ajaxData[fName] = objVal;
<%
	}
%>
	$.ajax({
		url: "<%=request.getContextPath() %>/flow/macro/onFieldCtlSelect.do",
		// contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
		type: "post",
		data: ajaxData,
		dataType: "json",
		beforeSend: function(XMLHttpRequest) {
		},
		success: function(data, status) {
			if (isPageShow) {
				for(var i=0,l=data.length; i < l; i++) {
					var json = data[i];
					// 不替换本表单域选择宏控件
					if (json.fieldName!="<%=fieldName %>") {
						var obj = o(json.fieldName);
						if (obj) {
							if (obj.tagName != 'SPAN') {
								$(obj).val(json.value);
							}
							else {
								$(obj).html(json.setValue);
							}
						}
						// 当字段不可写时
						var objShow = o(json.fieldName + "_show");
						if (objShow == null) {
							// 宏控件的显示值
							objShow = o(json.fieldName + "_realshow");
						}
						if (objShow) {
							$(objShow).html(json.setValue);
						}
					}
				}
			}
			else {
				for(var i=0,l=data.length; i < l; i++) {
					var json = data[i];
					// console.log(json);
					// 不替换本表单域选择宏控件
					if (json.fieldName!="<%=fieldName %>") {
						if(json.isMacro){
							// 清除文件宏控件中的文件链接
							$('#helper_' + json.fieldName).remove();
							replaceValue(json.fieldName, json.setValue, json.value);
						} else {
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
								var obj = o(json.fieldName);
								if (obj) {
									if (obj.tagName != 'SPAN') {
										$(obj).val(json.value);
									}
									else {
										// 不可写字段
										$(obj).html(json.setValue);
									}
								}
								var objShow = o(json.fieldName + "_show");
								if (objShow) {
									$(objShow).html(json.setValue);
								}
							}
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
