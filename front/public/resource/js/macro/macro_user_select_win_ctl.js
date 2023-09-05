console.log('macro_user...ctl.js is loading1');

function bindUserSelectWinCtlEvent(userSelObjName, formCode, deptField) {
	var obj = findObjByFormCode(formCode, userSelObjName);
	if (obj==null)
		return;
	var oldValue = obj.value;
	setInterval(function(){
									if (obj!=null && oldValue != obj.value) {
											oldValue = obj.value;
											onUserSelectWinCtlchange(obj.value, userSelObjName, formCode, deptField);
									}
							},500);
}

console.log('macro_user...ctl.js is loading2');

function onUserSelectWinCtlchange(newVal, userSelObjName, formCode, deptField) {
	var params = {"op":"getRealName", "fieldName":userSelObjName, "userName":newVal, "formCode":formCode};
  ajaxPost('/flow/macro/macro_user_select_win_ctl_js.jsp', params).then((data) => {
		console.log(data);
		var obj = findObjByFormCode(formCode, userSelObjName);
		if ($("#" + obj.name + "_realshow")!= null) {
			$("#" + obj.name + "_realshow").val(data.realName);
		}

		var json = data.data;
		for(var key in json) {
			$(o(key)).val(json[key]);
		}
	})
}

var params = getJsParams('macro_user_select_win_ctl');
console.log(params);
if (params.pageType && params.pageType.indexOf('show') == -1) {
	bindUserSelectWinCtlEvent(params.fieldName, params.formCode, params.deptField);
}
