<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.ParamChecker"%>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "cn.js.fan.util.StrUtil"%>
<%@ page import = "com.cloudweb.oa.api.ISQLCtl"%>
<%@ page import = "com.cloudweb.oa.service.MacroCtlService"%>
<%@ page import = "com.cloudweb.oa.utils.SpringUtil"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "org.json.JSONObject"%>
<%@ page import = "java.util.HashMap"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

	int flowId = ParamUtil.getInt(request, "flowId", -1);
	long mainId = ParamUtil.getLong(request, "mainId", -1);
	String fieldName = ParamUtil.get(request, "fieldName");
	String formCode = ParamUtil.get(request, "formCode");
	boolean isHidden = ParamUtil.getBoolean(request, "isHidden", false);
	boolean editable = ParamUtil.getBoolean(request, "editable", false);

	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);

	FormField ff = fd.getFormField(fieldName);
	MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
	ISQLCtl sqlCtl = macroCtlService.getSQLCtl();

	String pageType = ParamUtil.get(request, "pageType");
	boolean isList = false;
	if ("moduleList".equals(pageType) || "moduleListRelate".equals(pageType)){
		isList = true;
	}
	String op = ParamUtil.get(request, "op");
	if ("onChange".equals(op)) {
		// 防漏洞：查询中接受的主体参数
		if ("POST".equalsIgnoreCase(request.getMethod())) {
			response.setContentType("text/html;charset=utf-8");

			JSONObject json = new JSONObject();
			String html = sqlCtl.getCtlHtml(request, flowId, mainId, ff, pageType);

			if (html.contains("<input")) {
				json.put("type", "input");
			} else if (html.contains("<textarea")) {
				json.put("type", "textarea");
			} else {
				json.put("type", "select");
			}

			json.put("ret", "1");
			json.put("html", html);
			out.print(json);
		}

		return;
	}

	response.setContentType("text/javascript;charset=utf-8");

	String[] ary;
	String desc = StrUtil.getNullStr(ff.getDescription());
	if ("".equals(desc)) {
		ary = sqlCtl.getSqlByDesc(ff.getDefaultValue());
	} else {
		ary = sqlCtl.getSqlByDesc(desc);
	}

	String formName = ParamUtil.get(request, "cwsFormName");
	boolean isShow = "flowShow".equals(pageType) || "show".equals(pageType);
%>
<script>
	var formObj_<%=formName%>_<%=fieldName%> = o("<%=formName%>");
	<%
        String sql = ary[0];
        String fieldPairs = "";
        Map<String, String> map = new HashMap<>();
        // 从sql中找出表单中的变量 {$field}
        String regex = "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}";
        Pattern p = Pattern.compile(regex, // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        while (m.find()) {
            String fieldTitle = m.group(1);
            // 向下兼容，可能旧版的会用{$title}作为条件字段
            FormField field = fd.getFormField(fieldTitle);
            if (field == null) {
                field = fd.getFormFieldByTitle(fieldTitle);
            }
            if (field!=null) {
                fieldPairs += field.getName() + ": getPFieldVal_" + formName + "_" + fieldName + "('" + field.getName() + "'),\n";
            }
            else {
                fieldPairs += fieldTitle + ": getPFieldVal_" + formName + "_" + fieldName + "('" + fieldTitle + "'),\n";
                continue;
            }

            // 过滤掉SQL语句中重复出现的字段
            if (map.containsKey(field.getName())) {
                continue;
            }
            map.put(field.getName(), field.getName());
    %>
	var isShow = <%=isShow%>;
	var editable = <%=editable%>;
	function initCheckChange_<%=formName%>_<%=fieldName%>_<%=field.getName()%>() {
		if (!isShow && editable) {
			// var curFormId = getCurFormUtil().get();
			var curFormId = '<%=formName%>';

			var oldValue_<%=field.getName()%> = "cws-65536"; // 一个不存在的值
			var fName = "<%=field.getName()%>";
			if (fo(fName, curFormId)) { // 防止此控件也是SQL控件，并且此时还不存在
				oldValue_<%=field.getName()%> = fo(fName, curFormId).value;
			}

			var sint = setInterval(async function() {
				// 当被缓存时，切换选项卡，o(curFormId)会为null，故不能判断其为null时就清除interval，否则切换回时就无法响应了，
				// 而当点击刷新按钮后，fo(fName, curFormId) 会变为null，此时可以清除interval
				if (o(curFormId)) {
					if (fo(fName, curFormId)) {
						if (oldValue_<%=field.getName()%> != fo(fName, curFormId).value) {
							console.log('setInterval fName', fName, 'curFormId', curFormId, 'changed value:', fo(fName, curFormId).value);
							oldValue_<%=field.getName()%> = fo(fName, curFormId).value;
							await onSQLCtlRelateFieldChange_<%=formName%>_<%=fieldName%>();
						}
					} else {
						// 使关闭抽屉时，能够销毁setInterval，否则仅管前端removeScript了，但检测代码仍会运行。如进入编辑页后，再进入详情页，当存在多个SQL宏控件时，检测代码仍会生效，致生成控件
						window.clearInterval(sint);
						console.warn('macro_sql_ctl_js clearInterval <%=field.getName()%>');
					}
				}
			}, 500);
			// 当菜单项不启用缓存时，只能通过如下方法才能清除interval
			getCurFormUtil().addInterval(sint, '<%=formName%>');
		}
	}

	initCheckChange_<%=formName%>_<%=fieldName%>_<%=field.getName()%>();
	console.log('initCheckChange_<%=formName%>_<%=fieldName%>_<%=field.getName()%>()');
	<%
}

// System.out.println(getClass() + " fieldPairs=" + fieldPairs);
%>
	// 取得本表单或父表单中相应的值
	function getPFieldVal_<%=formName%>_<%=fieldName%>(fieldName) {
		// console.log('getPFieldVal findObj ' + fieldName, 'value:', findObj(fieldName).value);
		if (findObj(fieldName, '<%=formName%>')) {
			// 如果fieldName字段不可写，则其value值为undefined，需从cws_span_中获取
			if (findObj(fieldName, '<%=formName%>').value != undefined) {
				console.log('getPFieldVal formName', '<%=formName%>', fieldName, 'value=', findObj(fieldName, '<%=formName%>').value);
				return findObj(fieldName, '<%=formName%>').value;
			} else if (findObj('cws_span_' + fieldName, '<%=formName%>')) {
				return findObj('cws_span_' + fieldName, '<%=formName%>').innerText;
			}
		} else {
			console.warn('getPFieldVal: ' + fieldName + ' is not exist.');
		}
	}

	async function onSQLCtlRelateFieldChange_<%=formName%>_<%=fieldName%>() {
		var ajaxData = {
			op: "onChange",
			flowId: "<%=flowId%>",
			mainId: "<%=mainId%>",
			fieldName: "<%=fieldName%>",
			isHidden: "<%=isHidden%>",
			editable: "<%=editable %>",
			pageType: "<%=pageType%>",
			<%=fieldPairs%>
			formCode: "<%=formCode%>"
		}

		var curFieldName = "<%=fieldName%>";
		if (curFieldName.indexOf('sshy') != -1 && curFieldName.indexOf('_rd')==-1) {
			console.log('onSQLCtlRelateFieldChange <%=fieldName%> ajaxData', ajaxData);
		}

		// console.log('typeof(ajaxPost)', typeof(ajaxPost));
		if (typeof(ajaxPost) != 'function') {
			console.warn('onSQLCtlRelateFieldChange can not be launch. Application is running in back mode, the function ajaxPost is not found.');
			return;
		}

		var data;
		try {
			/*if (typeof showSpinning == 'function') {
                showSpinning(true);
            }*/
			data = await ajaxPost('/flow/macro/macro_sql_ctl_js.jsp', ajaxData);
		} finally {
			/*if (typeof showSpinning == 'function') {
                showSpinning(false);
            }*/
		}

		var formObj = formObj_<%=formName%>_<%=fieldName%>;
		if (data.ret == "1") {
			// var obj = findObj('<%=fieldName%>', '<%=formName%>');
			var obj = findObjInFormObj(formObj, '<%=fieldName%>');
			if (obj) {
				// $("#<%=fieldName%>").parent().replaceWith(data.html);	// 似乎无效
				// $("#<%=fieldName%>").prop("outerHTML", data.html);	// 不能仅替换控件本身，因为控件外面包裹了父节点span，需一起清除掉，否则当为必填项时会有两个*
				var $parent = $(obj).parent(); // _box

				// $(obj).remove();
				// delete $(obj);
				obj.remove();

				$parent.prop("outerHTML", data.html);

				// 记录当前值
				var curVal;
				// 如果当前该字段被映射，则取映射的值
				if (typeof getModuleFieldMappedValue == 'function') {
					curVal = getModuleFieldMappedValue(obj.name);
				}
				if (curVal == null) {
					curVal = $(obj).val();
				}
				if (curFieldName.indexOf('sshy') != -1 && curFieldName.indexOf('_rd')==-1) {
					console.log('onSQLCtlRelateFieldChange <%=fieldName%> ajaxPost curVal=', curVal, 'result data=', data);
				}

				// var frm = o("<%=formName%>");
				// var frm = formObj;
				// if (frm) {
					initFormCtl('<%=formName%>');
				// }
				// initFormCtl在前端utils/util.ts中定义
				/*if (getCurForm()) {
					initFormCtl(getCurForm().id);
				} else if (o('customForm')) {
					// 模块列表中的查询条件表单
					initFormCtl('customForm');
				} else if (o('customFormChildren')) {
					// 关联模块列表中的查询条件表单
					initFormCtl('customFormChildren');
				} else {
					console.error('SQL宏控件未找到表单，无法对控件样式初始化');
				}*/

				// 20230322 当为select时需赋值，因多级联动时生成的控件为select型，否则会因为未赋予curVal，致值为空
				// 20230326 当为INPUT时(即表单域选择宏控件映射时，如报签约时选择项目映射产业类型)不能赋值curVal，否则会导致联动时，第二级始终因被赋值而检测不到变化，致第三级不会发生变化
				// if (true || data.type == "select") {
				if (data.type == "select") {
					// 注意上面prop("outerHTML")操作后，原来的obj仍存在，会导致赋值无效，故需重新findObj
					// $(fo('<%=fieldName%>', '<%=formName%>')).val(curVal);
					$(findObjInFormObj(formObj, '<%=fieldName%>')).val(curVal);
				}

				/*var frm = o("visualForm");
				if (frm==null) {
					frm = o("flowForm");
				}
				if (frm == null) {
					console.warn("visualForm or flowForm is not exist.");
					return;
				}*/

				// 如果为列表页面，不需要验证
				if(!<%=isList%>) {
					// 删除原来的验证，否则会因为原验证中存储的对象不存在而导致验证失效
					var frmObj = LiveValidationForm.getInstance(formObj);
					if (frmObj) {
						frmObj.removeFieldByName('<%=fieldName%>');
					}
					<%
                        ParamChecker pck = new ParamChecker(request);
                        out.print(com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, ff));
                    %>
				}
			}

			try {
				initCalculator();
			}
			catch(e) {}
			try {
				// 重新绑定显示规则
				doViewJS();
			} catch (e) {}
		}
	}

	<%
    // nest_sheet_edit_relat.jsp中传过来时是edit
    // initCheckChange_中初始化时已调用过onSQLCtlRelateFieldChange_了，此处不需要重复执行，但是去掉了，只读状态也会丢失
    if (!isShow && editable) { // && !"edit".equals(pageType)) {
    %>
	onSQLCtlRelateFieldChange_<%=formName%>_<%=fieldName%>();
	<%}%>
</script>