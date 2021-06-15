<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%
String jsRootPath = request.getContextPath();
String formCode = ParamUtil.get(request, "formCode");
%>

function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function showProperty() {
	cws_selectRange();
        
	if (true) { // || cws_selection.type == "Control") {
	    var oControlRange, myobj;
    	if (window.getSelection) {
            if (cws_selection.rangeCount > 0) {
                oControlRange = cws_selection.getRangeAt(0);
           		// 在IE11下，cloneContents得到的节点不属于文档树，继承的 parentNode 属性总是 null
                // myobj = oControlRange.startContainer.parentNode;
                
                var objName = oControlRange.cloneContents().childNodes.item(0).name;
                // console.log(objName);
                // 取得iframe中被选中的元素
                // myobj = eval("IframeID." + objName);
                var e = IframeID.document.getElementsByName(objName);
                myobj = e[0];
                // 按如下方式获得的元素脱离了DOM树，其parentNode为null
                // myobj = oControlRange.cloneContents().childNodes.item(0);
                
                // 如果用extractContents()，则控件会被删除掉
                // myobj = oControlRange.extractContents().childNodes.item(0);
            }
        }
        else {
			oControlRange = cws_selection.createRange();
            myobj = oControlRange.item(0);
        }
        
        if (myobj==null) {
        	alert("请选择控件！");
            return;
        }
        
		var tagName = myobj.tagName;
		var params = makeParams('edit', myobj);
		var kind = myobj.getAttribute("kind");
		if (tagName=="INPUT") {
			if (myobj.type=="checkbox") {
				showModalDialog('images/flow_checkbox_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
				return;
			}
            else if (myobj.type=="radio") {
				showModalDialog('images/flow_radio_prop.htm', params, 'dialogWidth:360px;dialogHeight:380px;status:no;help:no;')
    			return;        
            }

			if (kind=="DATE")
				showModalDialog('images/flow_calendar_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
			else if (kind=="DATE_TIME")
				showModalDialog('images/flow_calendar_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
			else if (kind=="macro") {
				// showModalDialog('images/flow_macro_prop.jsp', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
                openWin('images/flow_macro_prop.jsp?mode=edit&editObjName=' + myobj.name, 320, 240);
            }
			else if (kind=="CALCULATOR") {
				showModalDialog('images/flow_calculate_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
			}
			else if (kind=="SQL") {
				showModalDialog('images/flow_sql_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
			}
			else if (kind=="BUTTON") {
				showModalDialog('images/flow_btn_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
			}
			else {
				// 控件没有标题时的情况：日期控件的Time部分
				if (myobj.title=="undefined" || myobj.title=="")
					;
				else
					showModalDialog('images/flow_text_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
			}
		}
		else if (tagName=="TEXTAREA") {
			showModalDialog('images/flow_text_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
		}
		else if (tagName=="SELECT") { // 单选或多选
			showModalDialog('images/flow_select_prop.htm', params, 'dialogWidth:360px;dialogHeight:380px;status:no;help:no;')
		}
	}
}

/* // 遍历被选中的控件
function traverse() {
  if (cws_selection.type == "Control") {
    var oControlRange = cws_selection.createRange();
    for (i = 0; i < oControlRange.length; i++) {
		var obj = oControlRange.item(i);
		var tagName = obj.tagName;
      	//if (oControlRange.item(i).tagName != "IMG")
      	//  oControlRange.item(i).style.color = event.srcElement.style.backgroundColor;
	  	alert(tagName + " name=" + obj.name + " value=" + obj.value + " title=" + obj.title + " kind=" + obj.kind);
	}
  }
}
*/

function insert(content)
{
	cws_InsertSymbol(content);
}

// mode "create" or "edit" 当为create时，obj为fieldType，当为edit时，obj为正在编辑的控件
function makeParams(mode, obj) {
	return new Array(window.self, mode, obj);
}

function cloud_textfield() {
	var params = makeParams('create', 'text');
	showModalDialog('images/flow_text_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
}
/*
function cloud_macro() {
	var params = makeParams('create', 'macro');
	showModalDialog('images/flow_macro_prop.jsp', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
}
*/

function cloud_macro() {
	var params = makeParams('create', 'macro');
	// showModalDialog('images/flow_macro_prop.jsp', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
    openWin('images/flow_macro_prop.jsp?mode=create&ctlType=macro&formCode=<%=StrUtil.UrlEncode(formCode)%>', 320, 240);
}

function cloud_textarea() {
	var params = makeParams('create', 'textarea');
	showModalDialog('images/flow_text_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
}

function cloud_checkbox() {
	var params = makeParams('create', 'checkbox');
	showModalDialog('images/flow_checkbox_prop.htm', params, 'dialogWidth:320px;dialogHeight:210px;status:no;help:no;')
}

function cloud_calendar() {
	var params = makeParams('create', 'calendar');
	showModalDialog('images/flow_calendar_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
}

function cloud_calculate() {
	var params = makeParams('create', 'calculate');
	showModalDialog('images/flow_calculate_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
}

function cloud_btnCtl() {
	var params = makeParams('create', 'btn');
	showModalDialog('images/flow_btn_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
}

function cloud_sql() {
	var params = makeParams('create', 'sql');
	showModalDialog('images/flow_sql_prop.htm', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
}

function cloud_select() {
	var params = makeParams('create', 'select');
	showModalDialog('images/flow_select_prop.htm', params, 'dialogWidth:360px;dialogHeight:440px;status:no;help:no;')
}

function cloud_radio() {
	var params = makeParams('create', 'radio');
	showModalDialog('images/flow_radio_prop.htm', params, 'dialogWidth:360px;dialogHeight:440px;status:no;help:no;')
}

function cloud_list() {
	var params = makeParams('create', 'list');
	showModalDialog('images/flow_select_prop.htm', params, 'dialogWidth:360px;dialogHeight:440px;status:no;help:no;')
}

function isCtlNameValid(ctlName) {
	if (ctlName=="title") {
		alert("标识非法！不能使用title作为标识！");	
		return false;
	}
	return true;
}

function CreateTxtCtl(ctlType, ctlName, ctlTitle, ctlDefaultValue, fieldType, canNull, minT, minV, maxT, maxV) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	var content = "";
	// if (ctlDefaultValue=="") // 使value为空的时候不致于被格式化掉
	//	ctlDefaultValue = "default";
	if (ctlType=="text")
		content = '<input title="' + ctlTitle + '" value="' + ctlDefaultValue + '" name="' + ctlName + '" type=text fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">';
	else if (ctlType=="textarea") {
		content = '<textarea title="' + ctlTitle + '" name="' + ctlName + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">' + ctlDefaultValue + '</textarea>';
	}
	insert(content);
}

function CreateCalculateCtl(ctlName, ctlTitle, ctlDefaultValue, fieldType, canNull, minT, minV, maxT, maxV) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	var content = "";
	// if (ctlDefaultValue=="") // 使value为空的时候不致于被格式化掉
	//	ctlDefaultValue = "default";
	content = '<input title="' + ctlTitle + '" value="' + ctlDefaultValue + '" name="' + ctlName + '" kind="CALCULATOR" formula="' + ctlDefaultValue + '" type=text fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">';
	insert(content);
}

function CreateBtnCtl(ctlName, ctlTitle, ctlDefaultValue, fieldType, canNull, minT, minV, maxT, maxV) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	var content = "";
	// if (ctlDefaultValue=="") // 使value为空的时候不致于被格式化掉
	//	ctlDefaultValue = "default";
	content = '<input title="' + ctlTitle + '" value="' + ctlTitle + '"  name="' + ctlName + '" kind="BUTTON" type=button fieldType="' + fieldType + '" script="' + ctlDefaultValue + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">';
	insert(content);
}

function CreateSQL(ctlName, ctlTitle, ctlDefaultValue, fieldType, canNull, minT, minV, maxT, maxV) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	var content = "";
	// if (ctlDefaultValue=="") // 使value为空的时候不致于被格式化掉
	//	ctlDefaultValue = "default";
	content = '<input title="' + ctlTitle + '" value="' + ctlDefaultValue + '" name="' + ctlName + '" kind="SQL" type=text fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">';
	insert(content);
}

function CreateCheckboxCtl(ctlType, ctlName, ctlTitle, ctlDefaultValue, canNull) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	var content = "";
	content = '<input title="' + ctlTitle + '" type=checkbox ' + ctlDefaultValue + ' value="1" name="' + ctlName + '" canNull="' + canNull + '">';
	insert(content);
}

function CreateCalendarCtl(ctlType, ctlName, ctlTitle, ctlDefaultValue, ctlFormat, canNull, minT, minV, maxT, maxV) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	var content = "";
	if (ctlFormat=="yyyy-MM-dd") {
		content += "<input title='" + ctlTitle + "' value='" + ctlDefaultValue + "' name='" + ctlName + "' kind=DATE canNull='" + canNull + "' minT='" + minT + "' minV='" + minV + "' maxT='" + maxT + "' maxV='" + maxV + "'><img name='" + ctlName + "_btnImg' src='<%=jsRootPath%>/images/form/calendar.gif' width='26' height='26' align='absmiddle' style='cursor:hand' onClick='SelectDate(\"" + ctlName + "\",\"yyyy-mm-dd\")'>";
	}else{
		content += "<input title='" + ctlTitle + "' value='" + ctlDefaultValue + "' name='" + ctlName + "' kind=DATE_TIME canNull='" + canNull + "' minT='" + minT + "' minV='" + minV + "' maxT='" + maxT + "' maxV='" + maxV + "'><img name='" + ctlName + "_btnImg' src='<%=jsRootPath%>/images/form/calendar.gif' width='26' height='26' align='absmiddle' style='cursor:hand' onClick='SelectDate(\"" + ctlName + "\",\"yyyy-mm-dd\")'><input name='" + ctlName + "_time' style='width:50px' value='12:30:30'/>&nbsp;<img name='" + ctlName + "_time_btnImg' src='<%=jsRootPath%>/images/form/clock.gif' align='absmiddle' style='cursor:hand' onClick='SelectDateTime(\"" + ctlName + "\")'>";
	}
	insert(content);
}

// 由于宏控件的值有些是通过程序动态获得的，所以不宜通过ParamChecker来进行有效性验证
function CreateMacroCtl(ctlType, ctlName, ctlTitle, macroDefaultValue, macroType, macroName, canNull) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	// if (macroDefaultValue=="");
	//	macroDefaultValue = "";
	var content = "";
	content += "<input title='" + ctlTitle + "' value='" + macroName + "' name='" + ctlName + "' macroDefaultValue='" + macroDefaultValue + "' macroType=" + macroType + " kind=macro canNull='" + canNull + "'>";
	insert(content);
}

function CreateSelectCtl(ctlType, ctlName, ctlTitle, opts, fieldType, canNull, minT,minV,maxT,maxV) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
	var content = "";
	if (ctlType=="select") {
		content = '<select title="' + ctlTitle + '" name="' + ctlName + '" fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">' + opts + '</select>';
	}
	else {
		content = '<select title="' + ctlTitle + '" name="' + ctlName + '" fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '" size=5 style="width: 60px">' + opts + '</select>';
	}
	insert(content);
}

function CreateRadioCtl(ctlType, ctlName, ctlTitle, ary, fieldType, canNull, minT,minV,maxT,maxV) {
	if (!isCtlNameValid(ctlName)) {
		return;
	}
    
	// var content = '<select title="' + ctlTitle + '" name="' + ctlName + '" fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">' + opts + '</select>';
	var content = "";
    for (var i=0; i < ary.length; i++) {
    	if (ary[i][1]=="1")
			content += '<input title="' + ctlTitle + '" value="' + ary[i][0] + '" name="' + ctlName + '" type="' + ctlType + '" fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '" checked>';
        else
			content += '<input title="' + ctlTitle + '" value="' + ary[i][0] + '" name="' + ctlName + '" type="' + ctlType + '" fieldType="' + fieldType + '" canNull="' + canNull + '" minT="' + minT + '" minV="' + minV + '" maxT="' + maxT + '" maxV="' + maxV + '">';        
    	content += "&nbsp;";
    }
    
    insert(content);
}


function remove(editObj) {
	editObj.parentNode.removeChild(editObj);
}