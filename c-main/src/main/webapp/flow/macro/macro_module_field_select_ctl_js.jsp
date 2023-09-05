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
<%@ page import="com.redmoon.oa.flow.FormDAO" %>
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

	String formName = ParamUtil.get(request, "cwsFormName");

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
		if (json.has("maps")) {
			mapAry = (JSONArray) json.get("maps");
		}
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
	response.setContentType("text/javascript;charset=utf-8");

	// 是否需要映射
	boolean isNeedMap = mapAry.length() > 0;
	boolean isPageShow = false;
	if (ConstUtil.PAGE_TYPE_SHOW.equals(pageType) || ConstUtil.PAGE_TYPE_FLOW_SHOW.equals(pageType)) {
		isPageShow = true;
	}
%>
<script>
	var isPageShow = <%=isPageShow%>;
	var isNeedMap<%=fieldName%><%=formName%> = <%=isNeedMap%>;
<%
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
		if (findObj("<%=fname%>")) {
			json["<%=fname%>"] = findObj("<%=fname%>").value;
		}
		else {
			console.error('条件字段:<%=fname%>在表单中不存在');
		}
		<%
			}
		%>
		return json;
	}

	// 注意initModuleFieldCtl的内容不能放在$(function())中，因为一把手招商添加时，其嵌套表格在添加时，打开smartModuleRelateTableDrawer时，不会被调用到
	// $(function () {
	function initModuleFieldCtl<%=fieldName%>() {
		<%
			if (pageType.contains("show")) {
		%>
				return;
		<%
			}
		// defaultOptVal是当request传入的参数，参数名为ff.getName()，用于从菜单项上带入值，如传入项目的ID
		// 在ModuleFieldSelectCtl中写js时，编辑记录时也会带入值
		String defaultOptVal = ParamUtil.get(request, "defaultOptVal");
		String defaultOptText = ParamUtil.get(request, "defaultOptText");
		%>
		console.log("findObj('<%=fieldName %>')", findObj('<%=fieldName %>'));
		if (findObj('<%=fieldName %>') && findObj('<%=fieldName %>').tagName=='SELECT') {
			if (!<%=isAjax%>) {
				$sel2 = $(findObj('<%=fieldName %>')).select2();
			}
			else {
				$sel2 = $(findObj('<%=fieldName %>')).select2({
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
						url: "/flow/macro/getAjaxOptions.do?fieldName=<%=fieldName %>&formCode=<%=formCode %>&flowId=<%=flowId %>",
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
		}

		<%
		if (isAjax && !"".equals(defaultOptText)) {
			%>
			var deOptVal = "<%=defaultOptVal %>".split(",");
			var deOptText = "<%=defaultOptText %>".split(",");
			// $("#<%=fieldName %>").val(deOptVal).trigger('change'); // 对于ajax型的无效
			for (var i=0; i < deOptVal.length; i++) {
				var option = new Option(deOptText[i], deOptVal[i], true, true);
    			$(findObj('<%=fieldName %>')).append(option).trigger('change');
    		}	
			<%
		}

		if (!"".equals(defaultOptText)) {
			if (isRealTime) {
		%>
			console.log('do onSelect');
			// 根据request传入的值实时进行映射
			onSelect<%=fieldName %><%=formName%>("<%=defaultOptVal %>", "<%=defaultOptText %>", isPageShow);
		<%
			}
		}
		%>
		 
		// module_show.jsp中，不需要处理事件
		var ctlFieldName = "<%=fieldName %>";
		if (findObj(ctlFieldName) && findObj(ctlFieldName).tagName=='SELECT') {
			$(findObj('<%=fieldName %>')).on("select2:select",function(e){
				var id = e.params.data.id;
				var text= e.params.data.text;
				<%if (isReadonly) { %>
				// 如果是只读模式，则因为select2新版中没有只读模式，所以此处强制恢复为request传过来的默认值
				var defaultOptVal = "<%=defaultOptVal %>";
				if (defaultOptVal!="") {
					if (id!=defaultOptVal) {
						$(findObj('<%=fieldName %>')).html('<option value="<%=defaultOptVal %>"><%=defaultOptText %></option>');
						return;
					}
				}
				<%} %>
				onSelect<%=fieldName %><%=formName%>(id, text, isPageShow);
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
	}
	// });

	initModuleFieldCtl<%=fieldName%>();
<%
} else {
	// 窗口选择
	boolean isValueFromRequest = ParamUtil.getBoolean(request, "isValueFromRequest", false);
	// 如果为true，则表示从request中传入了参数，需要自动映射
	if (isValueFromRequest) {
		String value = ParamUtil.get(request, "value");
		String valueShow = ParamUtil.get(request, "valueShow");
	}

	// defaultOptVal是当request传入的参数，参数名为ff.getName()，用于从菜单项上带入值，如传入项目的ID
	// 在ModuleFieldSelectCtl中写js时，编辑记录时也会带入值
	String defaultOptVal = ParamUtil.get(request, "defaultOptVal");
	String defaultOptText = ParamUtil.get(request, "defaultOptText");
	if (!"".equals(defaultOptText)) {
		if (isRealTime) {
	%>
	// $(function() {
		console.log('real time onSelect');
		// 根据request传入的值实时进行映射
		onSelect<%=fieldName %><%=formName%>("<%=defaultOptVal %>", "<%=defaultOptText %>", isPageShow);
	// });
	<%
		}
	}
	%>

	var formName = "<%=formName%>";
	var formObj_<%=formName%>_<%=fieldName%> = o("<%=formName%>");
	// 侦听宏控件的值有没有发生变化，如果值发生改变，需进行映射，另外控件自身为被另一个表单域选择宏控件映射的字段改变时，也需重新映射
	function addChangeListener(formName) {
		<%
			if (pageType.contains("show")) {
		%>
				return;
		<%
			}
		%>
		var ctlFieldName = "<%=fieldName%>";
		var obj = findObj(ctlFieldName, formName);
		if (!obj) {
			console.warn('字段' + ctlFieldName + '不存在，注意：嵌套表格宏控件中不支持检测变化');
			return;
		}
		var oldValue = obj.value;
		var pageShow = isPageShow;
		// var curFormId = getCurFormUtil().get();
		var curFormId = formName;

		console.log('ctlFieldName', ctlFieldName, 'oldValue', oldValue);
		var sint = setInterval(function(){
			// 在嵌套表格宏控件中字段名称会被改写，故此处需检测是否存在
			// 当被缓存时，切换选项卡，o(curFormId)会为null，故不能判断其为null时就清除interval，否则切换回时就无法响应了，
			// 而当点击刷新按钮后，ctlObj会变为null，此时可以清除interval
			if (o(curFormId)) {
				var ctlObj = fo(ctlFieldName, curFormId);
				// 右上角点击刷新按钮，刷新流程的时候，会出现fo(ctlFieldName, curFormId)为null的情况
				// 应该是此时curFormId存在，而内容却被清掉了所致，因为此时刷新后的内容还没有被赋予到form里面
				if (!ctlObj) {
					console.warn('ModuleFieldSelectCtl Field: clearInterval for form ' + ctlFieldName + ' is not found.');
					window.clearInterval(sint);
					return;
				}
				var val = ctlObj.value;
				if (val && oldValue != val) {
					console.log("formName=" + formName + " newValue=" + val + " oldValue=" + oldValue);
					if (val != "") {
						var text = fo(ctlFieldName + "_realshow", curFormId).value;
						console.log("onSelect: id=" + val + " text=" + text);
						onSelect<%=fieldName %><%=formName%>(val, text, pageShow);
					}
					oldValue = val;
				}
			}
		},100);
		// 当菜单项不启用缓存时，只能通过如下方法才能清除interval
		getCurFormUtil().addInterval(sint, formName);
	}

	console.log('macro_module_field_select_ctl_js check val');
	// 此方法不能放在$(function()中，因为在vue中$(function()多次打开抽屉后并不会响应
	addChangeListener("<%=formName%>");
<%
}

	// 取出表单域选择宏控件的过滤条件
	IModuleUtil iModuleUtil = (IModuleUtil)SpringUtil.getBean("moduleUtilService");
	List<String> list = iModuleUtil.parseFieldNameInFilter(request, formCode, filter);
%>

var curModuleFieldName = "";
if (typeof moduleFieldMap === 'undefined') {
	// 只创建1次
	moduleFieldMap = new MyMap();
}

function getModuleFieldMap(moduleFieldName) {
	var json = moduleFieldMap.get(moduleFieldName);
	if (!json) {
		var map = new MyMap();
		moduleFieldMap.put(moduleFieldName, map);
		return map;
	} else {
		return json.value;
	}
	/*console.log('getModuleFieldMap moduleFieldName', moduleFieldName);
	console.log('getModuleFieldMap moduleFieldMap', moduleFieldMap);
	console.log('getModuleFieldMap map', map);
	return map;*/
}

function clearModuleFieldMap(moduleFieldName) {
	if (moduleFieldMap.containsKey(moduleFieldName)) {
		moduleFieldMap.remove(moduleFieldName);
	}
}

function putModuleFieldValue(moduleFieldName, fieldName, value) {
	getModuleFieldMap(moduleFieldName).put(fieldName, value);
}

function getModuleFieldMappedValue(fieldName) {
	if (curModuleFieldName !== '') {
		var map = getModuleFieldMap(curModuleFieldName);
		// console.log('curModuleFieldName', curModuleFieldName, 'map', map);
		if (map.containsKey(fieldName)) {
			return map.get(fieldName).value;
		} else {
			return null;
		}
	} else {
		return null;
	}
}

	// 因为前端common.js缓存严重，故将此方法暂时放在这里
	/*function setRadioValueByFormObj(formObj, myitem, v) {
		var $radioboxs = $(formObj).find('[name=' + myitem + ']');
		if ($radioboxs[0] == null) {
			$radioboxs = $(formObj).find('[id=' + myitem + ']');
		}
		$radioboxs.each(function(k) {
			if (this.type === "radio") {
				if (v == null) {
					this.checked = false;
				} else {
					if (this.value === v) {
						this.checked = true;
					}
				}
			}
		})
	}*/

// 选择后重新映射
function onSelect<%=fieldName %><%=formName%>(id, text, isPageShow) {
	var formObj = formObj_<%=formName%>_<%=fieldName%>;

	if (!isNeedMap<%=fieldName%><%=formName%>) {
		return;
	}

	console.log('curForm', getCurrentFormObj());
	var pageType = '<%=pageType%>';
	// 此处需传openerFormCode，否则点击被映射得到的其它表单域选择宏控件的放大镜时，弹出module_list_sel.jsp时会报错：openerFormCode不能为空
	// 因为convertToHTML时，会从request中获取openerFormCode，当点击放大镜时，传入module_list_sel.jsp
	var ajaxData = {
		fieldName: "<%=fieldName %>",
		formCode: "<%=formCode %>",
		openerFormCode: "<%=formCode %>",
		flowId: "<%=flowId %>",
		id: id,
		text: text,
		pageType: pageType
	}
<%
	for (String fName : list) {
		if (isPageShow) {
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(flowId, fd);
		%>
			ajaxData['<%=fName%>'] = '<%=StrUtil.toHtml(fdao.getFieldValue(fName))%>';
		<%
		}
		else {
%>
		var fName = '<%=fName%>';
		console.log('formObj.name', getCurrentFormObj());
		var obj = findObj(fName, '<%=formName%>');
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
		// console.log(fName + '=' + objVal);
		ajaxData[fName] = objVal;
<%
		}
	}
%>
	ajaxPost('/flow/macro/onFieldCtlSelect.do', ajaxData).then((data) => {
		console.log('onFieldCtlSelect start <%=fieldName%>');
		console.log(data);

		curModuleFieldName = "<%=fieldName%>";
		var data = data.data;
		if (isPageShow) {
			for(var i=0, l=data.length; i < l; i++) {
				var json = data[i];
				// 不替换本表单域选择宏控件
				if (json.fieldName != "<%=fieldName%>") {
					// var obj = findObj(json.fieldName, '<%=formName%>');
					var obj = findObjInFormObj(formObj, json.fieldName);
					if (obj) {
						if (obj.tagName == 'SPAN') {
							$(obj).html(json.setValue);
						} else {
							if (obj.tagName == 'INPUT' && obj.getAttribute("type") == 'file') {
								console.warn('文件宏控件 ' + obj.getAttribute('title') + ' ' + obj.name + ' 的值不能直接被映射');
							} else {
								$(obj).val(json.value);
							}
						}
					}
					// 当字段不可写时
					// var objShow = findObj(json.fieldName + "_show", '<%=formName%>');
					var objShow = findObjInFormObj(formObj, json.fieldName + "_show");
					if (objShow == null) {
						// 宏控件的显示值
						// objShow = findObj(json.fieldName + "_realshow", '<%=formName%>');
						objShow = findObjInFormObj(formObj, json.fieldName + "_realshow");
					}
					if (objShow) {
						$(objShow).html(json.setValue);
					}
				}
			}
		}
		else {
			clearModuleFieldMap("<%=fieldName%>");

			// 清除SQL控件原来的setInterval检测，以免与映射后新生成sql控件的setInterval交叉产生混乱
			// 不能清除，因为映射后，并不会再带入macro_sql_ctl_js.jsp
			<%--if (typeof mapSQLCtl != 'undefined') {
				var elements = mapSQLCtl.getElements();
				console.log('elements', elements);
				var mapSize = elements.length;
				for (var k=0; k < mapSize; k++) {
					window.clearInterval(elements[k].key);
				}
			}--%>

			for(var i=0, l = data.length; i < l; i++) {
				var json = data[i];

				/*if (!findObj(json.fieldName, '<%=formName%>')) {
					console.warn("onSelect 映射字段 " + json.fieldName + " 不存在");
					continue;
				}*/
				var obj = findObjInFormObj(formObj, json.fieldName);
				if (!obj) {
					console.warn("onSelect 映射字段 " + json.fieldName + " 不存在");
					continue;
				}

				// console.log('onSelect map json', json);
				// 不替换本表单域选择宏控件
				if (json.fieldName != "<%=fieldName %>") {
					putModuleFieldValue("<%=fieldName %>", json.fieldName, json.value);

					if (json.isMacro) {
						// 清除realname
						// var objRealName = fo(json.fieldName + "_realname", '<%=formName%>');
						var objRealName = findObjInFormObj(formObj, json.fieldName + "_realname");
						if (objRealName) {
							$(objRealName).remove();
						}
						// 清除文件宏控件中的文件链接
						// $('#helper_' + json.fieldName).remove();
						$(formObj).find('#helper_' + json.fieldName).remove();

						// replaceValue(json.fieldName, json.setValue, json.value, json.checkJs, json.macroType);
						replaceValueByFormObj(json.fieldName, json.setValue, json.value, json.checkJs, json.macroType, formObj);

						initFormCtl('<%=formName%>');
					} else {
						if (obj.type == "radio") {
							// setRadioValue(json.fieldName, json.setValue);
							setRadioValueByFormObj(formObj, json.fieldName, json.setValue);
						}
						else if (obj.type == 'checkbox') {
							if (json.setValue == '1') {
								$(obj).prop('checked', true);
							} else {
								$(obj).prop('checked', false);
							}
						}
						else {
							// var obj = findObj(json.fieldName, '<%=formName%>');
							// if (obj) {
								if (obj.tagName != 'SPAN') {
									if (obj.tagName == "SELECT") {
										$(obj).val(json.setValue);
									} else {
										$(obj).val(json.value);
									}
								}
								else {
									// 不可写字段
									$(obj).html(json.setValue);
								}
							// }
							// var objShow = findObj(json.fieldName + "_show", '<%=formName%>');
							var objShow = findObjInFormObj(formObj, json.fieldName + "_show");
							if (objShow) {
								$(objShow).html(json.setValue);
							}
						}
					}
				}
			}
		}

		// 选择后应用显示规则
		var isDoViewJS = true;
		try {
			isDoViewJS = isDoViewJSOnModuleListSel();
		} catch (e) {
			console.warn(e);
		}
		if (isDoViewJS) {
			try {
				doViewJS();
			} catch (e) {
				console.warn(e);
			}
		}

		console.log('onFieldCtlSelect end <%=fieldName%>');
	});
}
</script>