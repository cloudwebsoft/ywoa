// 已停用，换为flow_dispose.jsp
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  // 避免当form中存在title时，找到的却是document.title，已通过检查表单中的标识是否合法来避免此问题
  // if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function getradio(radionname) {
	var radioboxs = document.all.item(radionname);
	if (radioboxs!=null)
	{
		for (i=0; i<radioboxs.length; i++)
		{
			if (radioboxs[i].type=="radio" && radioboxs[i].checked)
			{ 
				return radioboxs[i].value;
			}
		}
		return radioboxs.value
	}
	return "";
}

function getcheckbox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	var CheckboxValue = '';
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked) {
				return checkboxboxs.value;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].type=="checkbox" && checkboxboxs[i].checked)
			{
				if (CheckboxValue==''){
					CheckboxValue += checkboxboxs[i].value;
				}
				else{
					CheckboxValue += ","+ checkboxboxs[i].value;
				}
			}
		}
		//return checkboxboxs.value
	}
	return CheckboxValue;
}

function getCtlValue(ctlObj, ctlType) {
	var ctlName = ctlObj.name;
	var value = "";
	if (ctlType=="radio")
		value = getradio(ctlName);
	else if (ctlType=="checkbox")
		value = getcheckbox(ctlName);
	else
		value = ctlObj.value;
	return value;
}

function setCtlValue(ctlName, ctlType, ctlValue) {
	try {
		var obj = findObj(ctlName);
		if (ctlType=="checkbox") {
			if (ctlValue=="1")
				obj.checked = true;
			else
				obj.checked = false;
		}
		else
			obj.value = ctlValue;
	}
	catch (e) {
	}	
}

// 禁止控件的同时，在其后插入hidden控件，以使被禁止的控件的值能够上传, ctlValue中为经过toHtml的值，ctlValueRaw中为原始值
function DisableCtl(name, ctlType, ctlValue, ctlValueRaw) {
   var len = flowForm.elements.length;
   for(var i=0;i<len;i++) {
		var obj = flowForm.elements[i];
		// alert(obj.type);
		if (obj.name==name) {
			// var value = getCtlValue(obj, ctlType);
			// obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='" + obj.value + "'>");
			// obj.disabled = true;
			if (ctlType=="DATE" || ctlType=="DATE_TIME") {
				try {
					btnImgObj = findObj(name + "_btnImg");
					btnImgObj.outerHTML = "";
				}
				catch (e) {}
				obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='" + ctlValueRaw + "'>");
				obj.outerHTML = ctlValue + "&nbsp;";
			}
			else if (ctlType=="checkbox") {
				var v = obj.checked;
				if (v) {
					obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='1'>");
				 	obj.outerHTML = "(是)";
				}
				else {
					obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='0'>");
					obj.outerHTML = "(否)";
				}
			}
			else {
				obj.insertAdjacentHTML("AfterEnd", "<textarea style='display:none' name='" + name + "'>" + ctlValueRaw + "</textarea>");
				obj.outerHTML = ctlValue;
			}
			return;
		}
   }	
}

// 用控件的值来替代控件，用于把表单以报表方式显示时
function ReplaceCtlWithValue(name, ctlType, ctlValue) {
   var len = flowForm.elements.length;
   for(var i=0;i<len;i++) {
		var obj = flowForm.elements[i];
		if (obj.name==name) {
			if (ctlType=="checkbox") {
			}
			else {
				if (ctlType=="DATE_TIME") {
					// 去除时间中的时分秒域
					var timeObj = findObj(name + "_time");
					timeObj.outerHTML = "";
				}

				obj.outerHTML = ctlValue;
			}
			return;
		}
   }	
}

// 清除其它辅助图片按钮等
function ClearAccessory() {
	while (true) {
		var isFinded = false;
		var len = document.all.tags('IMG').length;
		for(var i=0; i<len; i++) { 
			try {
				var imgObj = document.all.tags('IMG')[i];
				// alert(imgObj.src);
				if (imgObj.src.indexOf("gif")!=-1 && imgObj.src.indexOf("file_flow")) {
					// imgObj.outerHTML = ""; // 会清除所有图片，当流程中表单存档时就会出现问题，目录树的图片也会被清除，另外在表单中特意上传的图片也会被清除
					// isFinded = true;
				}
				if (imgObj.src.indexOf("calendar.gif")!=-1) {
					imgObj.outerHTML = "";
					isFinded = true;
				}
				if (imgObj.src.indexOf("clock.gif")!=-1) {
					imgObj.outerHTML = "";
					isFinded = true;
				}				
			}
			catch (e) {}
		}
		if (!isFinded)
			break;
	}
}

var GetDate=""; 
function SelectDate(ObjName,FormatDate){
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:231px;status:no;help:no;");
}

function SelectNewDate(ObjName,FormatDate) {
	if (FormatDate == "yyyy-MM-dd") {
		$('#' + ObjName).datetimepicker({
			lang:'ch',
			datepicker:true,
			timepicker:false,
			format:'Y-m-d'
		});
	} else {
		$('#' + ObjName).datetimepicker({
			lang:'ch',
			datepicker:true,
			timepicker:true,
			format:'Y-m-d H:i:00',
			step:10
		});
	}
}

function SetDate(){ 
	findObj(ObjName).value = GetDate;
}

/**function SelectDateTime(objName) {
	var dt = showModalDialog("util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
	if (dt!=null)
		findObj(objName + "_time").value = dt;
}
*/
function SelectDateTime(objName) {
    var dt = openWin("util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        findObj(objName + "_time").value = dt;
}
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var whitePadObj;
// 为了防止HTML对象ID的重复
var whitePadWriteCount = 0;
// 手写板
function openWhitePadWin(objName, width, height){
	whitePadObj = findObj(objName);
	var win = window.open("spwhitepad/editor.jsp?width=" + width + "&height=" + height,"spwhitepadeditor","width=420,height=340,left=200,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,status=no");
	win.focus();
}

function insertStroke(code, width, height) {
	whitePadObj.value = code;
	
	var len1 = "[whitepad]".length;
	var len2 = "[/whitepad]".length;
	
	code = code.substring(len1, code.length - len2);
	
    var str = "<textarea style='display:none' id='value_spwhitepad_" + whitePadWriteCount;
	str += "'>" + code + "</textarea><iframe src='spwhitepad/show.htm' name='spwhitepad_";
	str += whitePadWriteCount + "' frameborder='0' style='width:" + width + "px;height:" + height + "px;margin:5px;border:1px dashed #CCCCCC;' scrolling='no'></iframe>";
	whitePadWriteCount ++;
	
	if (findObj("span_pad_" + whitePadObj.name)==null) {
		whitePadObj.insertAdjacentHTML("AfterEnd", "<span id='span_pad_" + whitePadObj.name + "'></span>");
	}
	findObj("span_pad_" + whitePadObj.name).innerHTML = str;
	
}