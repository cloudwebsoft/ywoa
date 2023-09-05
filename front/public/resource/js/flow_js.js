function getServerInfoUrl() {
	return getServerUrl();
	// return window.sessionStorage.getItem('serverInfoUrl');
}

function openWinForFlowAccess(url, width, height) {
	var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=250,left=350,width=" + width + ",height=" + height);
}

// module_list_sel.jsp中替换掉宏控件，checkJs 为字段的校验脚本
function replaceValue(openerField, val, sourceValue, checkJs, macroType) {
	// console.log("openerField=" + openerField + " type=" + findObj(openerField).getAttribute("type") + " val=" + val + " sourceValue=" + sourceValue);
	// console.log('replaceValue getCurrentFormObj', getCurrentFormObj());
	// $(findObj(openerField)).parent().html(val); // 如果元素是放在td中，则td中其它的隐藏字段会被清掉
	$(findObj(openerField + "_realshow")).remove(); // 删除realshow元素，如表单域宏控件中的显示值
	$(findObj(openerField + "_btn")).remove(); // 删除btn元素，如表单域宏控件中的搜索按钮
	var openerFieldObj = findObj(openerField);
	// console.log('replaceValue openerFieldObj', openerFieldObj);
	if (openerFieldObj == null) {
		console.log("openerField:" + openerField + " is not found");
		return;
	}

	// 判断控件是否为textarea，且display为none，如果是则说明该控件为不可写状态，不作替换
	// TODO 暂时这样处理，因为还需要考虑到实时映射的情况，还是要替换才行
	if (openerFieldObj.tagName == 'TEXTAREA' && openerFieldObj.style.display == 'none' && findObj(openerField + '_show')!=null) {
		return;
	}

	// 如果是文件控件
	if (openerFieldObj.getAttribute("type") == "file") {
		// 清除映射过来的文件信息
		$(findObj('helper_' + openerField)).remove();
		$(findObj(openerField + '_mapped')).remove();
		$(openerFieldObj).before(val);
	}
	else {
		// 如果是图标控件
		if (findObj(openerField + "_wrapper")) {
			// 通过outerHTML方式，无法调用val中的js
			// $(o(openerField + "_wrapper")).prop('outerHTML', val);
			// $(o(openerField)).val(sourceValue);

			$(findObj(openerField + "_wrapper")).prop('outerHTML', '<span id="temp_' + openerField + '"></span>');
			var tmp = findObj('temp_' + openerField);
			var $tmp = $(tmp);
			$tmp.after(val);
			$tmp.remove();
			var arr = new Array();
			arr[0] = sourceValue;
			$(openerFieldObj).val(arr).trigger('change');
		}
		else {
			// console.log('val', val, 'sourceValue', sourceValue);
			// $(findObj(openerField.name)).remove();
			// console.log('replaceValue macroType', macroType)
			if ('module_field_select' == macroType) {
				var $nextSib = $(openerFieldObj.nextSibling);
				var pNode = openerFieldObj.parentNode;
				pNode.removeChild(findObj(openerField));
				$(pNode).prepend(val);
				// 如果是下拉菜单模式
				if ($nextSib.hasClass('select2') && $nextSib.find('[aria-labelledby=select2-' + openerFieldObj.name + '-container]')[0]) {
					$nextSib.remove();
				}

				// var pNode = openerFieldObj.parentNode;
				// // 在下拉菜单模式下，只能用html方式全部换掉，因为select2会生成另一个控件，如果用下面的方法，会出现两个控件
				// // pNode.removeChild(findObj(openerField));
				// // $(pNode).prepend(val);
				// $(pNode).html(val);
			}
			else {
				$(openerFieldObj).prop('outerHTML', val);
			}
			// 不能用$(openerFieldObj)，因为在上面该对象已经被替换，不再是新的那个对象
			$(findObj(openerField)).val(sourceValue);
		}
	}

	var frm = getCurForm();

	// console.log('openerField=' + openerField + ' checkJs=' + checkJs);
	// 删除原来的验证，否则会因为原验证中存储的对象不存在而导致验证失效
	var formObj = LiveValidationForm.getInstance(frm);
	if (formObj) {
		if (checkJs) {
			formObj.removeFieldByName(openerField);
			var script = $('<script>' + checkJs + '</script>');
			$('body').append(script);
		}
		else {
			// 删除原来的验证，因为似乎原来绑定的元素不存在了，但是校验还在
			formObj.removeFieldByName(openerField);
		}
	}
}

// module_list_sel.jsp中替换掉宏控件，checkJs 为字段的校验脚本
function replaceValueByFormObj(openerField, val, sourceValue, checkJs, macroType, formObj) {
	// console.log("openerField=" + openerField + " type=" + findObj(openerField).getAttribute("type") + " val=" + val + " sourceValue=" + sourceValue);
	// console.log('replaceValue getCurrentFormObj', getCurrentFormObj());
	// $(findObj(openerField)).parent().html(val); // 如果元素是放在td中，则td中其它的隐藏字段会被清掉
	$(findObjInFormObj(formObj, openerField + "_realshow")).remove(); // 删除realshow元素，如表单域宏控件中的显示值
	$(findObjInFormObj(formObj, openerField + "_btn")).remove(); // 删除btn元素，如表单域宏控件中的搜索按钮
	var openerFieldObj = findObjInFormObj(formObj, openerField);
	// console.log('replaceValue openerFieldObj', openerFieldObj);
	if (openerFieldObj == null) {
		console.log("openerField:" + openerField + " is not found");
		return;
	}

	// 判断控件是否为textarea，且display为none，如果是则说明该控件为不可写状态，不作替换
	// TODO 暂时这样处理，因为还需要考虑到实时映射的情况，还是要替换才行
	if (openerFieldObj.tagName == 'TEXTAREA' && openerFieldObj.style.display == 'none' && findObjInFormObj(formObj, openerField + '_show')!=null && macroType!='macro_opinion') {
		return;
	}

	// 如果是文件控件
	if (openerFieldObj.getAttribute("type") == "file" || macroType == 'macro_attachment') {
		// 清除映射过来的文件信息
		$(findObjInFormObj(formObj, 'helper_' + openerField)).remove();
		$(findObjInFormObj(formObj, openerField + '_mapped')).remove();
		$(openerFieldObj).before(val);
		// 映射时文件宏控件convertToHTMLCtl会多生成一个同名字段，也为hidden类型，因此需删除掉多余的
		$(openerFieldObj).remove();

		console.log(openerField + ' AttachmentCtl file val mapped ', val);
		filterJS(val);
	}
	else {
		// 如果是图标控件
		if (findObjInFormObj(formObj, openerField + "_wrapper")) {
			// 通过outerHTML方式，无法调用val中的js
			// $(o(openerField + "_wrapper")).prop('outerHTML', val);
			// $(o(openerField)).val(sourceValue);

			$(findObjInFormObj(formObj, openerField + "_wrapper")).prop('outerHTML', '<span id="temp_' + openerField + '"></span>');
			var tmp = findObjInFormObj(formObj, 'temp_' + openerField);
			var $tmp = $(tmp);
			$tmp.after(val);
			$tmp.remove();
			var arr = new Array();
			arr[0] = sourceValue;
			$(openerFieldObj).val(arr).trigger('change');
		}
		else {
			// console.log('val', val, 'sourceValue', sourceValue);
			// $(findObj(openerField.name)).remove();
			// console.log('replaceValue macroType', macroType)
			if ('module_field_select' == macroType) {
				var $nextSib = $(openerFieldObj.nextSibling);
				var pNode = openerFieldObj.parentNode;
				pNode.removeChild(findObjInFormObj(formObj, openerField));
				$(pNode).prepend(val);
				// 如果是下拉菜单模式
				if ($nextSib.hasClass('select2') && $nextSib.find('[aria-labelledby=select2-' + openerFieldObj.name + '-container]')[0]) {
					$nextSib.remove();
				}

				// var pNode = openerFieldObj.parentNode;
				// // 在下拉菜单模式下，只能用html方式全部换掉，因为select2会生成另一个控件，如果用下面的方法，会出现两个控件
				// // pNode.removeChild(findObj(openerField));
				// // $(pNode).prepend(val);
				// $(pNode).html(val);
			}
			else {
				if (macroType == 'macro_opinion') {
					// 清除原来的显示 ***_show及_sign
					$(findObjInFormObj(formObj, openerField + '_show')).remove();
					$(findObjInFormObj(formObj, openerField + '_sign')).remove();
				}

				$(openerFieldObj).prop('outerHTML', val);
			}

			// 映射时，意见输入框被赋值后，xml内容会显示于意见输入框的文本框中，因此需隐藏
			if (macroType == 'macro_opinion') {
				$(findObjInFormObj(formObj, openerField)).hide();
				$(findObjInFormObj(formObj, openerField + '_sign')).remove();
			}
			
			// 不能用$(openerFieldObj)，因为在上面该对象已经被替换，不再是新的那个对象
			$(findObjInFormObj(formObj, openerField)).val(sourceValue);
		}
	}

	// console.log('openerField=' + openerField + ' checkJs=' + checkJs);
	// 删除原来的验证，否则会因为原验证中存储的对象不存在而导致验证失效
	var formObj = LiveValidationForm.getInstance(formObj);
	if (formObj) {
		if (checkJs) {
			formObj.removeFieldByName(openerField);
			var script = $('<script>' + checkJs + '</script>');
			$('body').append(script);
		}
		else {
			// 删除原来的验证，因为似乎原来绑定的元素不存在了，但是校验还在
			formObj.removeFieldByName(openerField);
		}
	}
}

var inputObj;

function setInputObjValue(id, v) {
	inputObj.value = id;
	// if (!v && v !== '') {
	// 	return;
	// }
	if (!v) {
		v = '';
	}
	console.log('inputObj', inputObj);
	// 在其父元素里面找对应的_realshow，因为在新的嵌套表格宏控件中会clone表格行，不同行之间存在有同名的元素
	var inputObjName = inputObj.name;
	console.log('inputObj.name', inputObj.name);
	console.log('setInputObjValue v', v);
	var objRealShow = $(inputObj).parent().find("[id='" + inputObjName + "_realshow']")[0];
	if (objRealShow==null) {
		objRealShow = $(inputObj).parent().parent().find("[name='" + inputObjName + "_realshow']")[0];
	}
	console.log('objRealShow', objRealShow);
	if (objRealShow) {
		if (typeof (objRealShow.value) == "string") {
			objRealShow.value = v;
		}
		else {
			objRealShow.innerHTML = v;
		}
	}
}

$(function () {
	//查看某通知公告流程 进行颜色变化的显示
	if (o("t_color_show") != null) {
		var clr = o("t_color_show").innerHTML;
		$(o("t_color_show")).prop("outerHTML", "<div style='width:30px; height:15px; float:left; background:" + clr + "'></div>");
	}
	var dept_code = $("#cws_span_dept_code").html();
})

// 用于多用户选择窗体的调用
function getMultiSelUserNames() {
	return inputObj.value;
}

function getMultiSelUserRealNames() {
	return o(inputObj.name + "_realshow").value;
}

// function addInputObjValue(v) {
//	 inputObj.value += v;
// }

function openWinSign(obj) {
	inputObj = obj;
	openInputPwdModal();
}

var curModuleFieldListParams = null;
function openWinModuleFieldList(objName, moduleCode, byFieldName, showFieldName, filter, openerFormCode, flowId, pageType, condFields) {
	filter = "";
	inputObj = findObj(objName);
	if (inputObj == null) {
		// 如果为空，则可能是在嵌套表格中使用
		console.log('event.target', event.target);
		inputObj = $(event.target).prev()[0];
	}
	console.log('openWinModuleFieldList objName', objName, 'inputObj', inputObj);
	var openerFieldName = inputObj.getAttribute('field');
	console.log('openWinModuleFieldList openerFieldName', openerFieldName);
	curModuleFieldListParams = {
		moduleCode,
		byFieldName,
		showFieldName,
		openerFormCode,
		openerFieldName,
		flowId,
		pageType,
		filter,
	};

	console.log('condFields', condFields);
	var conds = {};
	// 取表单域选择宏控件所设源模块条件中的字段的值
	if (condFields) {
		var ary = condFields.split('\\|');
		for (var i in ary) {
			if (o(ary[i])) {
				conds[ary[i]] = o(ary[i]).value
			}
		}
	}

	let params = {
		listPageParams: curModuleFieldListParams,
		condsOfFilter: conds
	}
	console.log('params', params);
	openSmartModuleSelTableDrawer(1, 1, params)
}

function getCurModuleFieldListParams() {
	return curModuleFieldListParams;
}

function openWinModuleListNest(parentFormCode, nestFormCode, nestFieldName, nestType, parentId, mainId, condFields) {
	var nestParams = {
		parentFormCode,
		nestFormCode,
		nestFieldName,
		nestType,
		parentId,
		mainId,
		moduleCode: nestFormCode,
	}
	curModuleFieldListParams = nestParams;

	console.log('condFields', condFields);
	var conds = {};
	// 取嵌套表格所设源模块条件中的字段的值
	if (condFields) {
		var ary = condFields.split('|');
		console.log('condFields split ary', ary);
		for (var i in ary) {
			if (fo(ary[i])) {
				conds[ary[i]] = fo(ary[i]).value
			}
		}
	}
	console.log('conds', conds);

	let params = {
		listPageParams: nestParams,
		condsOfFilter: conds
	}
	openSmartModuleSelTableDrawer(2, 2, params);
}

// 流程中点击表单域宏控件生成的链接，查看详情，params需为json
function openWinModuleShow(moduleCode, id, visitKey, params) {
	console.log('openWinModuleShow visitKey', visitKey, 'params', params);
	openSmartModuleDrawerForShow(moduleCode, id, visitKey, params);
}

function openWinFlowShow(flowId, visitKey) {
	console.log('flowId', flowId);
	openProcessShowDrawerForShow(flowId, visitKey);
}

function selByModuleListSel(id, mode, openerFieldName, sth) {
	// 如果mode=1为选择窗体
	console.log('selByModuleListSel id=' + id + ' sth=' + sth + ' mode=' + mode);
	if (mode == "1") {
		setInputObjValue(id, sth);
	} else {
			$(findObj(openerFieldName)).empty().append("<option id='" + id + "' value='" + id + "'>" + sth + "</option>").trigger('change');
	}
}

function setFieldValueForMapped(openerField, val, isMacro, sourceValue, checkJs, macroType) {
	var obj = o('helper_' + openerField);
	if (obj) {
			obj.parentNode.removeChild(obj);
	}

	if (isMacro) {
			replaceValue(openerField, val, sourceValue, checkJs, macroType);
	} else {
			var obj = o(openerField);
			if (obj) {
					if (obj.getAttribute("type") == "radio") {
							setRadioValue(obj.name, val);
					} else if (obj.getAttribute("type") == "checkbox") {
							setCheckboxChecked(obj.name, val);
					} else {
							if (obj.tagName != 'SPAN') {
									obj.value = val;
							} else {
									// 不可写字段
									obj.innerHTML = val;
							}
					}
			} else {
				console.error("字段：" + openerField + " 不存在！")
			}

			var objShow = o(openerField + "_show");
			if (objShow) {
					$(objShow).html(val);
			}
	}
}

// function doAtferSel() {
// 	// 应用显示规则
// 	var isDoViewJS = true;
// 	try {
// 			isDoViewJS = isDoViewJSOnModuleListSel();
// 	} catch (e) {
// 		console.warn(e);
// 	}

// 	if (isDoViewJS) {
// 		try {
// 			doViewJS();
// 		} catch (e) {
// 			console.warn(e);
// 		}
// 	}
// }

// 打开部门选择窗体
function openWinDeptsSelect(obj, isSingle, parentCode) {
	inputObj = obj;
	if (isSingle == undefined) {
		isSingle = false;
	}
	openSelDeptModal({isSingle: isSingle, parentCode: parentCode, value: obj.value});
}

// 多角色选择窗体
function openWinRoleMultiSel(obj) {
	inputObj = obj;
	openWin("rootpath/roleMultilSel.do?roleCodes=" + obj.value, 800, 600);
}

// 打开手写板对话框
function openWinWritePad(objName, w, h) {
	console.log('openWinWritePad objName', objName);
	inputObj = findObj(objName);
	console.log('openWinWritePad inputObj', inputObj);
	openWritePadModal(objName, w, h);
}

// 打开选择印章对话框
function openWinSignImg(objName) {
	inputObj = findObj(objName);
	openSelStampModal();
}

// 插入印章
function insertSignImg(stampId, url) {
	$(inputObj).val(stampId);
	var curStampId = inputObj.name;
	var html = "<img class='span_" + curStampId + "' name ='span_" + curStampId + "' id='span_" + curStampId + "' src='" + url + "' style='cursor:pointer' onclick=\"openWinSignImg('"+curStampId+"')\"/>";
	findObj("span_" + inputObj.name).innerHTML = html;
	$(inputObj).attr("type", "hidden");
	// $(".LV_validation_message").hide();
	// $(".LV_presence").hide();
}

// 将图片显示于容器imgId中，原本用于SignImgCtl，暂无用
function showImg(imgId, imgPath) {
	showImage(imgId, imgPath);
}

// 四则运算拆分算式
function getSymbolsWithBracket(str) {
	// 去除空格
	str = str.replaceAll(" ", "");
	if (str.indexOf("+") == 0)
		str = str.substring(1); // 去掉开头的+号
	var list = new Array();
	var curPos = 0;
	var prePos = 0;
	var k = 0;
	for (var i = 0; i < str.length; i++) {
		var s = str.charAt(i);
		if (s == '+' || s == '-' || s == '*' || s == '/' || s == '(' || s == ')') {
			if (prePos < curPos) {
				list[k] = str.substring(prePos, curPos).trim();
				k++;
			}
			list[k] = "" + s;
			k++;
			prePos = curPos + 1;
		}
		curPos++;
	}
	if (prePos <= str.length - 1)
		list[k] = str.substring(prePos).trim();
	return list;
}
// 是否四则运算符
function isOperator(str) {
	if (str == "+" || str == "*" || str == "/" || str == "-" || str == "(" || str == ")") {
		return true;
	}
	else
		return false;
}

function doCalculate(jqueryObj) {
	var formula = jqueryObj.attr('formula');
	var digit = jqueryObj.attr('digit');
	var isRoundTo5 = jqueryObj.attr('isRoundTo5');
	var format = jqueryObj.attr('format');
	var valForInfinity = jqueryObj.attr('valForInfinity');
	var valForNaN = jqueryObj.attr('valForNaN');

	if (formula.toLowerCase().indexOf("subdate") != -1 || formula.toLowerCase().indexOf("adddate") != -1) {
		// 对日期加减进行计算，有可能会出现 subDate(...) + 1 的情况，所以需要eval
		var calValue = eval(callFunc(formula));
		calValue = formatNumber(calValue, isRoundTo5, digit, format, valForInfinity, valForNaN);
		jqueryObj.val(calValue);
		return;
	}

	var ary = getSymbolsWithBracket(formula);

	for (var i = 0; i < ary.length; i++) {
		if (!isOperator(ary[i])) {
			// ary[i]可能为0.2这样的系数
			if (!isNumeric(ary[i])) {
				var $obj = $("input[name='" + ary[i] + "']");
				if (!$obj[0]) {
					$obj = $("select[name='" + ary[i] + "']");
				}
				if (!$obj[0]) {
					$obj = $("#cws_textarea_" + ary[i]);
				}
				var v = $obj.val();
				if ( v != null ) {
					// 去掉千分位逗号
					v = v.replaceAll(',', '');
				}
				else {
					console.warn('doCalculate obj val is null.', ary[i], $obj[0]);
				}

				if (v == "")
					ary[i] = 0;
				else if (isNaN(v))
					ary[i] = 0;
				else
					ary[i] = "(" + v + ")";
			}
		}
	}
	formula = "";
	for (var i = 0; i < ary.length; i++) {
		formula += ary[i];
	}
	try {
		var calValue = parseFloat(eval(formula));
		// 如果NaN，则置为0，以免引起必填项检测不能通过问题
		if (isNaN(calValue) && valForNaN=='') {
			calValue = 0;
		} else {
			calValue = formatNumber(calValue, isRoundTo5, digit, format, valForInfinity, valForNaN);
		}
		jqueryObj.val(calValue);
	}
	catch (e) { }
}

function formatNumber(calValue, isRoundTo5, digit, format, valForInfinity, valForNaN) {
	// var strValue = calValue.toString();
	if (isNaN(calValue)) {
		if (valForNaN != null && valForNaN != '') {
			return valForNaN;
		}
	}

	if (calValue == Infinity || calValue == -Infinity) {
		if (valForInfinity != null && valForInfinity != '') {
			return valForInfinity;
		}
	}

	// 判断是否四舍五入 1：是 0：否
	if (isRoundTo5 != null && isRoundTo5 == 1) {
		var digitNum = parseFloat(digit);
		if (!isNaN(digitNum)) {
			calValue = calValue.toFixed(digitNum);
		}
	}
	else if (isRoundTo5 != null && isRoundTo5 == 0) {
		var digitNum = parseFloat(digit);
		if (!isNaN(digitNum)) {
			calValue = calValue.toFixed(digitNum + 1);
			calValue = changeTwoDecimal_f(calValue, digitNum);
		}
	}

	// 千分位处理
	if ("0" == format) {
		calValue = thousandth(calValue);
	}
	return calValue;
}

function changeTwoDecimal_f(floatvar, digit) {
	var f_x = parseFloat(floatvar);
	if (isNaN(f_x)) {
		alert('function:changeTwoDecimal->parameter error');
		return false;
	}
	//var f_x = Math.round(f_x*100)/100;
	var s_x = f_x.toString();
	var pos_decimal = s_x.indexOf('.');
	if (pos_decimal < 0) {
		pos_decimal = s_x.length;
		s_x += '.';
	}
	else {
		var subString = s_x.substr(pos_decimal + 1);
		if (subString.length >= digit) {
			s_x = s_x.substr(0, pos_decimal + 1 + digit)
		}

	}
	while (s_x.length <= pos_decimal + digit) {
		s_x += '0';
	}

	// 去掉当digit为0时，末尾多余的.
	if (s_x.lastIndexOf(".") == s_x.length - 1) {
		s_x = s_x.substring(0, s_x.length - 1);
	}

	return s_x;
}

function initCalculator() {
	$("input[kind='CALCULATOR']").each(function () {
		var calObj = $(this);
		if ($(this).attr('formula')) {
			var formula = $(this).attr('formula');
			var isSum = false;
			var regStr = /(sum\(([\w|\.]+)\))/gi;
			var mactches = formula.match(regStr)
			var len = 0;
			if (mactches) {
				len = mactches.length;
				isSum = true;
			}
			if (isSum) {
				// 累加列 
				var field = RegExp.$2;
				if (field.indexOf("nest.") == 0) {
					var p = field.indexOf(".");
					field = field.substring(p + 1);
					// 当数据列有更新时，更新计算控件的值，该部分在nest_table_view.jsp中实现
				}
			}
			else {
				if (formula.toLowerCase().indexOf("subdate") != -1 || formula.toLowerCase().indexOf("adddate") != -1) {
					// 对日期绑定事件
					formula = initFuncFieldEvent(formula, calObj);
				} else {
					var ary = getSymbolsWithBracket(formula);

					for (var i = 0; i < ary.length; i++) {
						// ary[i]可能为0.2这样的系数
						if (!isOperator(ary[i]) && !isNumeric(ary[i])) {
							/*
							// change在更改后，点击别处时，才会更新
							$("input[name='" + ary[i] + "']").change(function(){
										doCalculate(calObj);
							});
							*/

							/* IE9不支持下列写法
							$("input[name='" + ary[i] + "']").bind("propertychange", function() { 
										doCalculate(calObj);
							});
							*/
							var isSelect = false;
							var o = $("input[name='" + ary[i] + "']")[0];
							if (!o) {
								o = $("select[name='" + ary[i] + "']")[0];
								if (!o) {
									// 因为字段可能为不可写状态，所以o可能为null，所以此处把提示注释掉，而且表单设计时是选择字段，所以字段名不会错
									// alert("计算控件" + calObj.attr("name") + "，算式" + formula + "中的字段：" + ary[i] + " 不存在！");
								}
								else {
									isSelect = true;
								}
							}
							if (o) {
								bindEvent(o, calObj, isSelect);
							}
						}
					}
				}
			}
		}
	});

	$("input[kind='CALCULATOR']").each(function () {
		if ($(this).attr('formula')) {
			doCalculate($(this));
		}
	});
}

function bindEvent(obj, calObj, isSelect) {
	if (isSelect) {
		obj.addEventListener("change", function (event) { doCalculate(calObj); }, false);
	}
	else {
		var oldValue = obj.value;
		setInterval(function () {
			if (oldValue != obj.value) {
				oldValue = obj.value;
				doCalculate(calObj);
			}
		}, 500);
	}
}

// 对日期相减中涉及的字段的相关事件进行初始化
function initFuncFieldEvent(str, calObj) {
	// 时间相减方法subdate(d1, d2)
	var pat = /subdate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
	str.replace(pat, function (p1, date1, date2) {
		var isSelect = false;
		var o = $("input[name='" + date1 + "']")[0];
		if (!o) {
			o = $("select[name='" + date1 + "']")[0];
			if (!o) {
				alert("计算控件算式" + p1 + "中的字段：" + date1 + " 不存在！");
			}
			else {
				isSelect = true;
			}
		}
		bindEvent(o, calObj, isSelect);

		o = $("input[name='" + date2 + "']")[0];
		if (!o) {
			o = $("select[name='" + date2 + "']")[0];
			if (!o) {
				alert("计算控件算式" + p1 + "中的字段：" + date2 + " 不存在！");
			}
			else {
				isSelect = true;
			}
		}
		bindEvent(o, calObj, isSelect);
	});

	// 时间相加方法addDate(d1, d2)
	var pat = /adddate\(([a-z0-9_-]+),([0-9-]+)\)/ig;
	str.replace(pat, function (p1, date, days) {
		var isSelect = false;
		var o = $("input[name='" + date + "']")[0];
		if (!o) {
			o = $("select[name='" + date + "']")[0];
			if (!o) {
				alert("计算控件算式" + p1 + "中的字段：" + date + " 不存在！");
			}
			else {
				isSelect = true;
			}
		}
		bindEvent(o, calObj, isSelect);
	});

	return str;
}

function callFunc(str) {
	var isDate = false;
	// 时间相减方法subdate(d1, d2)
	var pat = /subdate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
	str = str.replace(pat, function (p1, date1, date2) {
		if (o(date1) == null) {
			alert("字段" + date1 + "不存在！");
			return 0;
		}
		if (o(date2) == null) {
			alert("字段" + date2 + "不存在！");
			return 0;
		}

		isDate = true;
		var mode = "day";
		if (o(date1).getAttribute("kind") == "DATE_TIME" || o(date2).getAttribute("kind") == "DATE_TIME") {
			mode = "hour";
		}

		var v1 = o(date1).value;
		var v2 = o(date2).value;
		if ("" == v1 || "" == v2)
			return 0;
		else {
			return subDate(v1, v2, mode);
		}
	});

	// 时间相加方法addDate(d1, d2)
	var pat = /adddate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
	str = str.replace(pat, function (p1, date, days) {
		if (o(date) == null) {
			alert("字段" + date1 + "不存在！");
			return "";
		}
		if (o(days) == null) {
			if (!isNumeric(days)) {
				alert(days + "不是数字！");
				return "";
			}
		}
		else {
			days = o(days).value;
			if (!isNumeric(days)) {
				alert("字段" + days + "不是数字！");
				return "";
			}
		}

		isDate = true;
		var v = o(date).value;
		if ("" == v)
			return "";
		else {
			return addDate(v, days);
		}
	});
	if (isDate) {
		return str;
	}
	else {
		var v = eval(str);
		return v;
	}
}

function subDate(strDate1, strDate2, mode) {
	if (!strDate1) {
		return;
	}
	strDate1 = strDate1.replace(/-/g, "/");
	strDate2 = strDate2.replace(/-/g, "/");

	var date1 = Date.parse(strDate1);
	var date2 = Date.parse(strDate2);

	var dt = Math.abs(date2 - date1);   //时间差的毫秒数   

	if (mode == "day") {
		return parseInt((dt) / (24 * 60 * 60 * 1000));
	}
	else {
		var days = Math.floor((dt) / (24 * 60 * 60 * 1000));
		// 计算出小时数
		var leave = dt % (24 * 3600 * 1000)    //计算天数后剩余的毫秒数
		var hours = leave / (3600 * 1000) + days * 24;
		return parseInt(hours);
	}
}

function addDate(date, days) {
	var d = new Date(date);
	days = parseInt(days);
	// console.log(d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate() + " days=" + days);

	d.setDate(d.getDate() + days);
	var month = d.getMonth() + 1;
	var day = d.getDate();
	if (month < 10) {
		month = "0" + month;
	}
	if (day < 10) {
		day = "0" + day;
	}
	var val = d.getFullYear() + "-" + month + "-" + day;
	return val;
}

//callFunc(str);

// 基础数据宏控件当change时的默认调用
function onBasciCtlChange(ctlObj) {
}

try {
	$(document).ready(function () {
		try {
			// 因为某些页面中没有导入jquery.js
			initCalculator();
		}
		catch (e) { }
	});
}
catch (e) { }

function getAuthToken() {
	return window.sessionStorage.getItem('token')
}

function openWinUserSelect(obj) {
	inputObj = obj;
	var users = [];
	if(obj.value) {
		users.push({
			realName: o(obj.name + '_realshow').value,
			name: obj.value,
		})
	}
	// 0表示单选
	selectUserInForm(obj.name, users, 0);
}

function openWinUserMultiSelect(obj) {
	if (obj == null) {
		// 如果为空，则可能是在嵌套表格中使用
		console.log('event.target', event.target);
		obj = $(event.target).prev()[0];
	}
	inputObj = obj;
	var users = [];
	if(obj.value) {
		var uNames = obj.value.split(',');
		var uRealNames = o(obj.name + '_realshow').value.split(',');
		for (var i in uNames) {
			users.push({
				name: uNames[i],
				realName: uRealNames[i],
			})
		}
	}
	// 1表示多选
	selectUserInForm(obj.name, users, 1);
	// openWinForFlowAccess("rootpath/user_multi_sel.jsp?unitCode=unitCode&isForm=true", 800, 600);
}

function openWinLocationSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("rootpath/map/location_list_mine.jsp?isForm=true&op=sel", 800, 600);
}

function openWinWorkflowMineSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("rootpath/flow/flowListPage.do?displayMode=2&action=sel", 1024, 768);
}

function openWinQueryFieldList(objName, openerFormCode, fieldName, isScript, queryId) {
	inputObj = o(objName);
	if (isScript) {
		openWinForFlowAccess("rootpath/flow/form_query_script_list_do.jsp?mode=selField&id=" + queryId + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + fieldName, 800, 600);
	}
	else {
		openWinForFlowAccess("rootpath/flow/form_query_list_do.jsp?mode=selField&id=" + queryId + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + fieldName, 800, 600);
	}
}

function openWinPrivilegeSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("rootpath/admin/priv_list_sel.jsp?isForm=true", 520, 480);
}

async function loadNestCtl(ajaxPath, divId, curPage, pageSize, conds, formName) {
	if (curPage) {
		// 去掉&curPage=...&pageSize=...
		var p = ajaxPath.indexOf("&curPage=");
		if (p != -1) {
			ajaxPath = ajaxPath.substring(0, p);
		}
		ajaxPath += "&curPage=" + curPage;
	}
	if (pageSize) {
		ajaxPath += "&pageSize=" + pageSize;
	}
	if (conds) {
		if ('' != conds) {
			ajaxPath += "&" + conds;
		}
	}
	var params = {};
	console.log('loadNestCtl start:' + ajaxPath);
	// await ajaxGet(ajaxPath, params).then(async (data) => {
	// 	console.log('ajaxPath', ajaxPath);
	// 	if (o(divId)) {
	// 		o(divId).innerHTML = data;
	// 	}
	// 	else {
	// 		console.error('loadNestCtl 容器 ' + divId + ' 不存在');
	// 	}
	// 	await filterJS(data);
	// });

	let data = await ajaxGet(ajaxPath, params);
	console.log('loadNestCtl ajaxPath', ajaxPath);
	console.log('loadNestCtl formName', formName);
	if (formName) {
		if (fo(divId, formName)) {
			console.log('loadNestCtl fo(divId, formName)', fo(divId, formName));
			fo(divId, formName).innerHTML = data;
		}
		else {
			console.warn('loadNestCtl 容器 ' + divId + ' 在form: ' + formName + ' 中不存在');
		}
	} else {
		if (fo(divId)) {
			fo(divId).innerHTML = data;
		}
		else {
			console.warn('loadNestCtl 容器 ' + divId + ' 不存在');
		}
	}

	await filterJS(data);

	console.log('loadNestCtl end divId:' + divId);
}

function bindFuncFieldRelateChangeEvent(formCode, targetFieldName, fieldNames) {
	var ary = fieldNames.split(",");
	var len = ary.length;
	for (var i = 0; i < len; i++) {
		var field = ary[i];
		if (field == "") {
			continue;
		}
		if (o(field)) {
			var oldValue = o(field).value;
			checkFuncRelateOnchange(formCode, targetFieldName, fieldNames, field, oldValue);
		}
		else {
			console.error(field + " is not exist");
		}
	}

	// 初始化值
	doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames);
}

function checkFuncRelateOnchange(formCode, targetFieldName, fieldNames, field, oldValue) {
	var sInt = setInterval(function () {
		if (o(field) != null) {
			if (oldValue != o(field).value) {
				oldValue = o(field).value;
				doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames);
			}
		}
		else {
			console.warn('checkFuncRelateOnchange', field + ' is not exist.');
			clearInterval(sInt);
		}
	}, 500);
}

function doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames) {
	var ary = fieldNames.split(",");
	var len = ary.length;
	var data = "formCode=" + formCode + "&fieldName=" + targetFieldName;
	for (var i = 0; i < len; i++) {
		var field = ary[i];
		if (field == "") {
			continue;
		}
		if (o(field)) {
			data += "&" + field + "=" + o(field).value;
		}
		else {
			console.error(field + ' is not exist');
		}
	}

	data += "&fieldNames=" + fieldNames;

	var params = {};
	ajaxPost('/visual/getFuncVal.do?' + data, params).then((data) => {
		console.log(data);
		o(targetFieldName).value = data.val;
	});

	// var rootPath = window.rootPath
	// var interfacePrefix = window.interfacePrefix
	// var Authorization = window.localStorage.getItem('token');

	// $.ajax({
	// 	type: "post",
	// 	url: rootPath + `/${interfacePrefix}/visual/getFuncVal.do`,
	// 	data: data,
	// 	dataType: "json",
	// 	headers:{
	// 		Authorization:Authorization
	// 	},
	// 	beforeSend: function(XMLHttpRequest){
	// 	},
	// 	complete: function(XMLHttpRequest, status){
	// 	},
	// 	success: function(data, status){
	// 		console.log("rootPath",rootPath)
	// 		var ret = data;
	// 		o(targetFieldName).value = ret.val;				
	// 	},
	// 	error: function() {
	// 		alert(XMLHttpRequest.responseText);
	// 	}
	// });		
}

// 唯一性检查
function checkFieldIsUnique(id, formCode, fieldName, fieldsUnique) {
	var eType = "";
	if (o(fieldName).tagName == "SELECT") {
		eType = "select";
	}
	else {
		eType = "input";
	}
	$(eType + '[name="' + fieldName + '"]').change(function () {
		var datas = { "id": id, "formCode": formCode, "fieldName": fieldName };
		var ary = fieldsUnique.split(",");
		var obj = this;
		for (var i in ary) {
			datas[ary[i]] = o(ary[i]).value;
		}

		ajaxPost('/module_check/checkFieldIsUnique', datas).then((data) => {
			console.log(data);
			if (data.ret == 0) {
				$(obj).parent().append("<span id='errMsgBox_" + fieldName + "' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
			}
			else {
				$('#errMsgBox' + fieldName).remove();
			}
		});
	});
}

// 嵌套表中字段唯一性检查
function checkFieldIsUniqueNest(id, formCode, fieldName, fieldsUnique) {
	var fieldObj = findObj(fieldName);
	if (!fieldObj) {
		console.warn('checkFieldIsUniqueNest: ' + fieldName + ' is not exist.');
		return;
	}
	$(fieldObj).change(function () {
		var parentFormCode = '';
		var datas = { "id": id, "formCode": formCode, "fieldName": fieldName, "cwsId": $("#cws_id").val(), "parentFormCode": parentFormCode };
		var ary = fieldsUnique.split(",");
		var obj = this;
		for (var i in ary) {
			datas[ary[i]] = o(ary[i]).value;
		}

		ajaxPost('/module_check/checkFieldIsUniqueNest', datas).then((data) => {
			console.log(data);
			if (data.ret == 0) {
				$(obj).parent().append("<span id='errMsgBox_" + fieldName + "' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
			}
			else {
				$('#errMsgBox' + fieldName).remove();
			}
		});
	});
}

function thousandth(num) {
	num = num + '';
	// 先清掉逗号，再转为千分位，以避已转千分位后再转的问题
	num = num.replaceAll(',', '');

	var p = num.indexOf('.');
	if (p == -1) {
		num += '.'
	}
	return num.replace(/(\d)(?=(\d{3})+\.)/g, function ($0, $1) {
		return $1 + ',';
	}).replace(/\.$/, '');
}

// 千分位处理
function doFormat(fieldName) {
	$('[name="' + fieldName + '"]').change(function () {
		if ($(this).val() != '') {
			$(this).val(thousandth($(this).val()));
		}
	});
}

//自定义事件构造函数
function EventTarget() {
	//事件处理程序数组集合
	this.handlers = {};
}

var EVENT_TYPE = {
	"NEST_ADD": "nest_add",	// 添加一行
	"NEST_EDIT": "nest_edit",	// 编辑一行
	"NEST_DEL": "nest_del",	// 删除一行
	"NEST_AUTO_SEL_DONE": "nest_auto_sel_done",	// 自动拉单
	"NEST_SELECT": "nest_select" // 批量选择
}

//自定义事件的原型对象
EventTarget.prototype = {
	// 设置原型构造函数链
	constructor: EventTarget,
	// 注册给定类型的事件处理程序
	// type -> 自定义事件类型， handler -> 自定义事件回调函数
	addEvent: function (type, handler) {
		// 判断事件处理数组是否有该类型事件
		if (typeof this.handlers[type] == 'undefined') {
			this.handlers[type] = [];
		}
		// 将处理事件push到事件处理数组里面
		this.handlers[type].push(handler);
	},
	// 触发一个事件
	// event -> 为一个js对象，属性中至少包含type属性，
	// 因为类型是必须的，其次可以传一些处理函数需要的其他变量参数。（这也是为什么要传js对象的原因）
	fireEvent: function (event) {
		//模拟真实事件的event
		if (!event.target) {
			event.target = this;
		}
		// 判断是否存在该事件类型
		if (this.handlers[event.type] instanceof Array) {
			var handlers = this.handlers[event.type];
			//在同一个事件类型下的可能存在多种处理事件，找出本次需要处理的事件
			for (var i = 0; i < handlers.length; i++) {
				// 执行触发
				handlers[i](event);
			}
		}
	},
	// 注销事件
	// type -> 自定义事件类型， handler -> 自定义事件回调函数
	removeEvent: function (type, handler) {
		//判断是否存在该事件类型
		if (this.handlers[type] instanceof Array) {
			var handlers = this.handlers[type];
			//在同一个事件类型下的可能存在多种处理事件
			for (var i = 0; i < handlers.length; i++) {
				//找出本次需要处理的事件下标
				if (handlers[i] == handler) {
					break;
				}
			}
			//从事件处理数组里面删除
			handlers.splice(i, 1);
		}
	}
};

var eventTarget = new EventTarget();
function getEventTarget() {
	return eventTarget;
}

function submitFile(containerId, formCode, serialNo) {
	var type ='visual';
	var frm = o("visualForm");
	if (frm == null) {
		frm = o("flowForm");
		type = 'flow';
	}
	
	let formDataFile = new FormData();
	formDataFile.append('type', type);
	formDataFile.append('formCode', formCode);

	let formData = new FormData(frm);
	for (var key of formData.keys()) {
		　console.log(key, formData.get(key));
			if (key === 'uploader') {
				formDataFile.append(key, formData.get(key));
				break;
			}
	}
	submitMyFile(formDataFile).then((data) => {
		console.log(data);
		var ue = UE.getEditor(containerId);
		ue.execCommand('insertimage', {
			src: data.image,
			// width: '500'
		});
	});
}

/* // 记录添加的嵌套表格2记录的ID
function addTempCwsId(formCode, cwsId) {
	var name = "tempCwsId_" + formCode;
	var inp;
	try {
		inp = document.createElement('<input type="hidden" name="' + name + '" />');
	} catch(e) {
		inp = document.createElement("input");
		inp.type = "hidden";
		inp.name = name;
	}
	inp.value = cwsId;

	o('spanTempCwsIds').appendChild(inp);
} */

function doVisual(moduleCodeRelated, formCodeRelated, fdaoId,tds,token){
	// 在nest_sheet_view.jsp中定义了addTempCwsId
	addTempCwsId(formCodeRelated, fdaoId);
	try {
		console.log('moduleCode', moduleCodeRelated);
		console.log('tds', tds);
		console.log('formCodeRelated', formCodeRelated);
		var func = 'insertRow_' + moduleCodeRelated + '("' + formCodeRelated + '",' + fdaoId + ',"' + tds + '","' + token + '")';
		console.log('func', func);
		eval(func);
		// 计算控件合计
		callCalculateOnload();
	}
	catch (e) {
		console.log(e);
	}
}

function doFlow(moduleCodeRelated, formCodeRelated, fdaoId,tds,token,sums){
	// 不能刷新，因为在insertRow还将调用onNestInsertRow事件
	// refreshNestSheetCtl<%=moduleCode%>();
	try {
		var func = 'insertRow_' + moduleCodeRelated + '("' + formCodeRelated + '",' + fdaoId + ',"' + tds + '","' + token + '")';
		console.log('func', func);
		eval(func);
		// 计算控件合计
		// callCalculateOnload();
		callByNestSheet(sums, formCodeRelated);
	}
	catch (e) {
		console.log(e);
	}
}

var mapFormParams = new MyMap();
function putParamInFormMap(formName, paramName, paramValue) {
	if (!mapFormParams.containsKey(formName)) {
		var mapForm = new MyMap();
		mapForm.put(paramName, paramValue);
		mapFormParams.put(formName, mapForm);
	} else {
		var mapForm = mapFormParams.get(formName);
		console.log('mapForm', mapForm);
		mapForm.value.put(paramName, paramValue);
	}
	console.log('mapFormParams', mapFormParams);
}

function getParamFromMap(formName, paramName) {
	var map = mapFormParams.get(formName);
	if (map == null) {
		return null;
	}
	var subMap = map.value.get(paramName);
	if (subMap) {
		return subMap.value;
	} else {
		return null;
	}
}

function onQueryRelateFieldChange(formName) {
	var params = {};
	params['id'] = getParamFromMap(formName, 'queryId');
	params['flowTypeCode'] = getParamFromMap(formName, 'flowTypeCode');

	var queryCond = getParamFromMap(formName, 'queryCond');
	if (!queryCond) {
		return;
	}
	console.log('onQueryRelateFieldChange queryCond', queryCond);
	var keys = Object.keys(queryCond);
	for (var i = 0; i < keys.length; i++) {
		var field = keys[i];
		console.log('onQueryRelateFieldChange field', field);
		var fieldObj = findObjInForm(formName, field);
		if (fieldObj) {
			params[field] = fieldObj.value;
		} else {
			console.warn('onQueryRelateFieldChange 查询绑定的字段: ' + field + ' 在表单' + formName + '中未找到');
		}
	}

	ajaxPost(getParamFromMap(formName, 'queryAjaxUrl'), params).then((data) => {
		console.log(data);
		// 如果存在queryBox（自定义的查询结果div容器）
		if (findObjInForm(formName, "queryBox")) {
			findObjInForm(formName, "queryBox").innerHTML = data;
		} else {
				// 系统默认的查询结果div容器
				findObjInForm(formName, "formQueryBox").innerHTML = data;
		}
	});
}

// 初始化侦听，侦听条件字段的改变
function initQueryCondListener(formName) {
	var queryCond = getParamFromMap(formName, 'queryCond');
	if (!queryCond) {
		return;
	}
	var keys = Object.keys(queryCond);
	for (var i = 0; i < keys.length; i++) {
		var field = keys[i];
		var oldVal = getParamFromMap(formName, field + 'oldVal');
		if (oldVal == null) {
			var fieldObj = findObjInForm(formName, field);
			if (fieldObj) {
				oldVal = fieldObj.value;
				putParamInFormMap(formName, field + 'oldVal', oldVal);
			} else {
				console.warn('initQueryCondListener 查询绑定的字段: ' + field + ' 在表单' + formName + '中未找到');
			}
		}

		var sInt = setInterval(function () {
			var fieldObj = findObjInForm(formName, field);
			if (fieldObj != null) {
				if (oldVal!= fieldObj.value) {
					onQueryRelateFieldChange(formName);
					oldVal = fieldObj.value;
					putParamInFormMap(formName, field + 'oldVal', oldVal);
				}
			} else {
				clearInterval(sInt);
			}
		}, 500);
	}
}