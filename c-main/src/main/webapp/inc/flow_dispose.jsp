<%@ page contentType="text/html; charset=utf-8"%>
<%
	response.setContentType("text/javascript;charset=utf-8");
	String rootpath = request.getContextPath();
%>
function findObj(theObj, theDoc) {
  return o(theObj);
}

function getradio(radionname) {
	var radioboxs = document.getElementsByName(radionname);
	if (radioboxs!=null)
	{
		for (i=0; i < radioboxs.length; i++)
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
	var checkboxboxs = document.getElementsByName(checkboxname);
	var CheckboxValue = '';
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked) {
				return checkboxboxs.value;
			}
		}
		for (i=0; i < checkboxboxs.length; i++)
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
		// return checkboxboxs.value
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

function setCtlValue(ctlName, ctlType, ctlValue, nestFormCode) {
    ctlValue = ctlValue.trim();
	try {
		// @task:当ctlValue为visualForm.cws_textarea_province.value且为空时，赋值后select控件不能显示<option value=''>无</option>，而一定要将ctlValue置为""
		if (ctlValue == "")
			ctlValue = "";
		var obj = findObj(ctlName);
		// 如果是嵌套表格nest_table
		if (nestFormCode != null) {
			$nestTable = $("[id^=nestTable_][formCode=" + nestFormCode + "]");
			if ($nestTable[0]) {
				obj = $nestTable.find('[name=' + ctlName + ']')[0];
			}
			else {
				var url = window.location.href;
				var arrUrl = url.split("/");
				var page = arrUrl[arrUrl.length - 1];
				if (page.indexOf("nest_table_iframe") == -1) {
					// nest_table_iframe.jsp中，载入临时表格html中所带的脚本时，也会报此错误，因为iframe中没有nestTable_***表格
					console.error('Field ' + ctlName + ' is not found in nest table: ' + nestFormCode);
				}
			}
		}

		if (ctlType=="checkbox") {
			if (ctlValue=="1")
				obj.checked = true;
			else
				obj.checked = false;
		}
        else if (ctlType=="radio") {
        	setRadioValue(ctlName, ctlValue);
        }
		else {
			var fieldType = obj.getAttribute("fieldType");
			// 如果是数值型
			if (obj.tagName == "SELECT") {
				if (fieldType==2 || fieldType==3 || fieldType==4 || fieldType==5 || fieldType==6) {
					var ctlValueFloat = parseFloat(ctlValue);
					// 取出控件所有的值
					for (var i=0; i < obj.options.length; i++) {
						var optVal = obj.options[i].value;
						var optValFloat = parseFloat(optVal);
						if (optValFloat == ctlValueFloat) {
							obj.value = optVal;
							break;
						}
					}
				}
				else {
					obj.value = ctlValue;
				}
			}
			else {
				obj.value = ctlValue;
			}
		}
	}
	catch (e) {
	}	
}

// 禁止控件的同时，在其后插入hidden控件，以使被禁止的控件的值能够上传, ctlValue中为经过toHtml的值，ctlValueRaw中为原始值
function DisableCtl(name, ctlType, ctlValue, ctlValueRaw) {
	var len = flowForm.elements.length;
	var ary = new Array();
	for(var i=0;i < len;i++) {
		ary[i] = flowForm.elements[i];
	}
	for(var i=0; i < len; i++) {
		var obj = ary[i];
		// console.log('obj.name=' + obj.name + ' name=' + name + ' len=' + len);
		if (obj.name==name) {
			if (ctlType=="DATE" || ctlType=="DATE_TIME") {
				try {
					btnImgObj = findObj(name + "_btnImg");
					btnImgObj.outerHTML = "";
				}
				catch (e) {}
				obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='" + ctlValueRaw + "'>");
				obj.outerHTML = "<span id='" + name + "_show'>" + ctlValue + "</span>&nbsp;";
				$("img[onclick!='']").each(function() {
					var oc = $(this).attr('onclick');
					if (typeof(oc) != 'undefined' && oc != '' && (oc.indexOf('SelectDate(') == 0 || oc.indexOf('SelectDateTime(') == 0)) {
						$(this).hide();
					}
				});
			}
			else if (ctlType=="checkbox") {
				var v = obj.checked;
				if (v) {
					obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='1'>");
					//obj.outerHTML = "(是)";
					obj.outerHTML = "<span id='" + name + "_show'>" + "<img src='<%=rootpath%>/images/checkbox_y.gif' align='absMiddle'></span>";
				}
				else {
					obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='0'>");
					// obj.outerHTML = "(否)";
					obj.outerHTML = "<span id='" + name + "_show'>" + "<img src='<%=rootpath%>/images/checkbox_n.gif' align='absMiddle'></span>";
				}
			}
			else if (ctlType == "select") {
				var text = $("[name='" + name + "'] option[value='" + ctlValue + "']").text();
				obj.outerHTML = "<span id='" + name + "_show'>" + text + "</span><textarea style='display:none' name='" + name + "'>" + ctlValueRaw + "</textarea>";
			}
			else if (ctlType=="radio") {
				 var radioboxs = document.getElementsByName(name);
				 if (radioboxs!=null) {
					var isSomeDisabled = false;
					for (j=0; j < radioboxs.length; j++) {
						if (radioboxs[j].disabled) {
							isSomeDisabled = true;
						}
						else {
							radioboxs[j].disabled = true;
							// chrome下，会使得radioboxs数组的长度发生变化，带来问题
							// radioboxs[j].setAttribute("name", name + "_disabled");
						}
					}
					// 只插入一次
					if (!isSomeDisabled) {
						// 注意顺序，必须在后面一行，因为radioboxs会动态变化
						radioboxs[0].insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='" + ctlValue + "'>");
					}
				 }
			}
			else {
				var pNode = obj.parentNode;
				// 删除用来显示元素的span，以免多次调用DisableCtl时，带来显示值多次重复的问题（如调用表单映射控件的时候）
				var showObj = o(name + "_show");
				var ctlValueShow = ctlValue;
				if (showObj) {
					if (showObj.value)
						ctlValueShow = showObj.value;
					else
						ctlValueShow = showObj.innerHTML;
					showObj.parentNode.removeChild(showObj);
				}

				var child = document.createElement("SPAN");
				child.innerHTML = "<span id='" + name + "_show'>" + ctlValueShow + "</span><textarea style='display:none' name='" + name + "'>" + ctlValueRaw + "</textarea>";
				// 替换掉后，在android中得不到提交，经测试与jquery.form无关
				pNode.replaceChild(child, obj);

				// 这样处理也不行
				// pNode.removeChild(obj);
				// pNode.appendChild(child);

				if (o(name + "_btn")) {
					o(name + "_btn").style.display = "none";
				}
			}

			// 20210511 注释掉，因为如果有多个嵌套表格，存在有name相同的字段，则不能return，需继续处理
			// 注意必须要通过ary来复制flowForm.elements，不能直接用form.elements，否则可能会带来未知问题，因为在disabel过程中会生成name同名的元素
			// return;
		}
	}
}

function HideCtl(name, ctlType, macroType) {
   var len = flowForm.elements.length;
   var isFound = false;
   for(var i=0;i < len;i++) {
		var obj = flowForm.elements[i];
		if (obj.name==name) {
			isFound = true;
			if (ctlType=="DATE" || ctlType=="DATE_TIME") {
				try {
					var btnImgObj = o(name + "_btnImg");
                    if (btnImgObj)
						btnImgObj.style.display = "none";
				}
				catch (e) {}
                // 如果先被disable了
                if (o(name + "_show"))        
	                o(name + "_show").style.display = "none";
                if (o(name))
                	o(name).style.display = "none";
				$("img[onclick!='']").each(function() {
					var oc = $(this).attr('onclick');
					if (typeof(oc) != 'undefined' && oc != '' && (oc.indexOf('SelectDate(') == 0 || oc.indexOf('SelectDateTime(') == 0)) {
						$(this).hide();
					}
				});
			}
            else if (ctlType=="radio") {
                 var radioboxs = document.getElementsByName(name);
                 if (radioboxs!=null) {
                    var isSomeDisabled = false;
                 	for (j=0; j < radioboxs.length; j++) {
	                    radioboxs[j].style.display = "none";
                    }
                 }
            }
			else {
				if (o(name + "_show"))
				  o(name + "_show").style.display = "none";
				if (o(name))
				  o(name).style.display = "none";
			}

			// 20210511 注释掉，因为如果有多个嵌套表格，存在有name相同的字段，则不能return，需继续处理
			// return;
		}
   }

	// 在字段管理中设置的隐藏字段，在flow_modify.jsp页面中，因为form中的元素已不存在，变为了span，所以需另外处理
	if (!isFound) {
		if (o(name + "_show"))
			o(name + "_show").style.display = "none";
		if (o(name))
			o(name).style.display = "none";
	}

    if (macroType=="nest_table") {
        o("cwsNestTable").style.display = "none";
    }
	
    // 屏蔽报表中的显示
	if (o(name + "_show")) {
		$("span[id='" + name + "_show']").each(function () {
			$(this).hide();
		});
	}
}

// 用控件的值来替代控件，用于把表单以报表方式显示时
function ReplaceCtlWithValue(name, ctlType, ctlValue) {
   var len = flowForm.elements.length;
   for(var i=0;i < len;i++) {
		var obj = flowForm.elements[i];
		var kind = obj.getAttribute(kind);
		if (obj.name==name) {
			if (ctlType=="checkbox") {
                if (obj.value==ctlValue)
                    obj.outerHTML = "<span id='" + name + "_show'><img src='<%=rootpath%>/images/checkbox_y.gif' align='absMiddle'></span>";
                else
                    obj.outerHTML = "<span id='" + name + "_show'><img src='<%=rootpath%>/images/checkbox_n.gif' align='absMiddle'></span>";
			}
            else if (ctlType=="radio") {
                var radioboxs = document.getElementsByName(name);
                if (radioboxs!=null) {
                    var arr = new Array();
                    for(var j = 0; j < radioboxs.length; j++) {
                        arr.push(radioboxs[j]);
					}
                    for (j=0; j < arr.length; j++) {
                        if (arr[j].value==ctlValue)
                            arr[j].outerHTML = "<span id='" + name + "_show'><img src='<%=rootpath%>/images/radio_y.gif' align='absMiddle'></span>";
                        else
                            arr[j].outerHTML = "<span id='" + name + "_show'><img src='<%=rootpath%>/images/radio_n.gif' align='absMiddle'></span>";
                    }
                }
            }
			else {
				if (ctlType=="DATE_TIME") {
					// 去除时间中的时分秒域
					// var timeObj = findObj(name + "_time");
					// timeObj.outerHTML = "";
                    var timeObj = o(name + "_time");
                    if (timeObj) {
                        timeObj.parentNode.removeChild(timeObj);
                    }
				}
				$("img[onclick!='']").each(function() {
					var oc = $(this).attr('onclick');
					if (typeof(oc) != 'undefined' && oc != '' && (oc.indexOf('SelectDate(') == 0 || oc.indexOf('SelectDateTime(') == 0)) {
						$(this).hide();
					}
				});

				var formCodeAttrStr = '';
				if (kind == 'CALCULATOR') {
					var formCode = obj.getAttribute('formCode');
					if (formCode != null) {
						formCodeAttrStr = "formcode='" + formCode + "'";
					}
				}
				obj.outerHTML = "<span id='" + name + "' " + formCodeAttrStr + ">" + ctlValue + "</span>";
			}
			return;
		}
   }
}

// 清除其它辅助图片按钮等
function ClearAccessory() {
    $("#formAllDiv input").each(function() {
    	var btnObj = $(this)[0];
        if (btnObj.type=="hidden" || btnObj.type=="checkbox" || btnObj.type=="radio")
        	;
        else {
            try {
                if (btnObj.reserve=="true")
                    ;
                else
                    $(btnObj).remove();
            }
            catch (e) {
            }
        }
    });

	while (true) {
		var isFinded = false;
		var imgs;
		if (isIE8) {
			imgs = document.all.tags('IMG');
		}
		else {
			imgs = document.getElementsByTagName('IMG');
		}
		var len = imgs.length;
		for(var i=0; i < len; i++) { 
			try {
				var imgObj = imgs[i];
				if (imgObj.src.indexOf("gif")!=-1 && imgObj.src.indexOf("file_flow")) {
					// imgObj.outerHTML = ""; // 会清除所有图片，当流程中表单存档时就会出现问题，目录树的图片也会被清除，另外在表单中特意上传的图片也会被清除
					// isFinded = true;
				}
				if (imgObj.src.indexOf("calendar.gif")!=-1) {
					$(imgObj).remove();
					isFinded = true;
				}
				if (imgObj.src.indexOf("clock.gif")!=-1) {
					$(imgObj).remove();
					isFinded = true;
				}				
			}
			catch (e) {}
		}
		// 清除button
        /*
		len = document.all.tags('input').length;
		for(i=0; i < len; i++) { 
			try {
				var btnObj = document.all.tags('input')[i];
				if (btnObj.type=="hidden" || btnObj.type=="checkbox" || btnObj.type=="radio")
					continue;
				try {
					if (btnObj.reserve=="true")
						continue;
				}
				catch (e) {
				}
				btnObj.outerHTML = "";
				isFinded = true;
			}
			catch (e) {}
		}
        */
        
		if (!isFinded)
			break;
	}
}

var GetDate="";
function SelectDate(ObjName, FormatDate) {
    if (true || !isIE()) {
		showCalendar(ObjName, '%Y-%m-%d', null, true);
    }
    else {
        var PostAtt = new Array;
        PostAtt[0]= FormatDate;
        PostAtt[1]= findObj(ObjName);
    
        GetDate = openWin("<%=rootpath%>/util/calendar/calendar.htm", 286, 221);
    }
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

// 用于jscalendar
function selected(cal, date) {
  cal.sel.value = date; // just update the date in the input field.
  if (cal.dateClicked && (cal.sel.id == "sel1" || cal.sel.id == "sel3"))
    // if we add this call we close the calendar on single-click.
    // just to exemplify both cases, we are using this only for the 1st
    // and the 3rd field, while 2nd and 4th will still require double-click.
    cal.callCloseHandler();
}
// 用于jscalendar
function closeHandler(cal) {
  cal.hide();                        // hide the calendar
//  cal.destroy();
  _dynarch_popupCalendar = null;
}
// 用于jscalendar
function showCalendar(id, format, showsTime, showsOtherMonths) {
  // var el = document.getElementById(id);
  var el = o(id);
    
  if (_dynarch_popupCalendar != null) {
    // we already have some calendar created
    _dynarch_popupCalendar.hide();                 // so we hide it first.
  } else {
    // first-time call, create the calendar.
    var cal = new Calendar(1, null, selected, closeHandler);
    // uncomment the following line to hide the week numbers
    // cal.weekNumbers = false;
    if (typeof showsTime == "string") {
      cal.showsTime = true;
      cal.time24 = (showsTime == "24");
    }
    if (showsOtherMonths) {
      cal.showsOtherMonths = true;
    }
    _dynarch_popupCalendar = cal;                  // remember it in the global var
    cal.setRange(1900, 2070);        // min/max year allowed.
    cal.create();
  }
  
  _dynarch_popupCalendar.setDateFormat(format);    // set the specified date format
  _dynarch_popupCalendar.parseDate(el.value);      // try to parse the text in field
  _dynarch_popupCalendar.sel = el;                 // inform it what input field we use

  // the reference element that we pass to showAtElement is the button that
  // triggers the calendar.  In this example we align the calendar bottom-right
  // to the button.
  // _dynarch_popupCalendar.showAtElement(el.nextSibling, "Br");        // show the calendar
  _dynarch_popupCalendar.showAtElement(el, "");        // show the calendar

  return false;
}

var timeObjName;
function SelectDateTime(objName) {
	timeObjName = objName;
	if (isIE()) {
        var dt = openWin("<%=rootpath%>/util/calendar/time.jsp", 266, 185);
        if (dt!=null)
            findObj(objName + "_time").value = dt;
    }
    else {
    	openWin("<%=rootpath%>/util/calendar/time.jsp", 266, 185);
    }
}

function setDateTime(val) {
	o(timeObjName + "_time").value = val;
}

function SetNewDate() {
	var noName = false;
	$("input[kind='DATE']").each(function() {
    	if($(this).attr("readonly")==true || $(this).attr("readonly")=="readonly") {
			return true;
        }
        
        var dateObj = this;
        
		var isNew = $(this).attr("isnewdatetimectl");
		if (typeof(isNew) == 'undefined' || !isNew) {
			var name = $(this).attr('name');
			$(this).attr('id', name);
			//$('#' + name + '_btnImg').hide();
			var dateImg = findObj(name + "_btnImg");
			if (dateImg != null) {
				dateImg.style.display = 'none';
			} else {
				noName = true;
			}
		} else {
			if (typeof($(this).attr("id")) == 'undefined') {
				var name = $(this).attr('name').trim();
				$(this).attr('id', name);
			}
		}
		try {
			var dtOption = {
				lang:'ch',
				datepicker:true,
				timepicker:false,
				validateOnBlur:false,
				onShow : function() {
					if (isIE8) {
						// 解决IE8下需点击两次才能选到时间，且选择后livevalidation仍显示不能为空的问题
						// 这样处理有问题，如果cwsWorkflowResult的位置距离较远的话，会滚动屏幕
        				// $('#cwsWorkflowResult').focus();
        			}
    			},
			    onClose: function(dateText, inst) {
			    	if (isIE8) {
			    		// 使livevalidation再次验证
			       		$(dateObj).blur();
			       	}
			    },
				format:'Y-m-d'
			}

			if ($(this).attr('minv') == 'curDate') {
				var date = new Date();
				if ($(this).attr('mint') == 'd') {
					date.setDate(date.getDate()+1);
				}
				var year = date.getFullYear();
				var month = date.getMonth() + 1;
				month = month < 10 ? '0' + month : month;
				var dates = date.getDate();
				dates = dates < 10 ? '0' + dates : dates;
				dtOption.minDate = year+'-'+month+'-'+dates;
			}

			if ($(this).attr('maxv') == 'curDate') {
				var date = new Date();
				if ($(this).attr('maxt') == 'x') {
					date.setDate(date.getDate()-1);
				}
				var year = date.getFullYear();
				var month = date.getMonth() + 1;
				month = month < 10 ? '0' + month : month;
				var dates = date.getDate();
				dates = dates < 10 ? '0' + dates : dates;
				dtOption.maxDate = year+'-'+month+'-'+dates;
			}
			$('#' + $(this).attr("id")).datetimepicker(dtOption);
		} catch (e) {}
	});
	$("input[kind='DATE_TIME']").each(function() {
    	if($(this).attr("readonly")==true || $(this).attr("readonly")=="readonly") {
			return true;
        }

        var dateObj = this;
            
		var isNew = $(this).attr("isnewdatetimectl");
		if (typeof(isNew) == 'undefined' || !isNew) {
			var name = $(this).attr('name');
			$(this).attr('id', name);
			var dateImg = findObj(name + "_btnImg");
			if (dateImg != null) {
				dateImg.style.display = 'none';
			} else {
				noName = true;
			}
			var time = findObj(name + "_time");
			if (time != null) {
				time.style.display = 'none';
				time.value = "";
			}
			var timeImg = findObj(name + "_time_btnImg");
			if (timeImg != null) {
				timeImg.style.display = 'none';
			} else {
				noName = true;
			}
		} else {
			if (typeof($(this).attr("id")) == 'undefined') {
				var name = $(this).attr('name').trim();
				$(this).attr('id', name);
			}
		}
		try {
			var dtOption = {
				lang:'ch',
				datepicker:true,
				timepicker:true,
				format:'Y-m-d H:i:00',
				validateOnBlur:false, // 解决IE8下需点击两次才能选到时间，且选择后livevalidation仍显示不能为空的问题
			    onClose: function(dateText, inst) {
			    	if (isIE8) {
			    		// 使livevalidation再次验证
			       		$(dateObj).blur();
			       	}
			    },
				step:10
			}

			if ($(this).attr('minv') == 'curDate') {
				var date = new Date();
				if ($(this).attr('mint') == 'd') {
					date.setDate(date.getDate()+1);
				}
				var year = date.getFullYear();
				var month = date.getMonth() + 1;
				month = month < 10 ? '0' + month : month;
				var dates = date.getDate();
				dates = dates < 10 ? '0' + dates : dates;
				dtOption.minDate = year+'-'+month+'-'+dates;
			}

			if ($(this).attr('maxv') == 'curDate') {
				var date = new Date();
				if ($(this).attr('maxt') == 'x') {
					date.setDate(date.getDate()-1);
				}
				var year = date.getFullYear();
				var month = date.getMonth() + 1;
				month = month < 10 ? '0' + month : month;
				var dates = date.getDate();
				dates = dates < 10 ? '0' + dates : dates;
				dtOption.maxDate = year+'-'+month+'-'+dates;
			}
			$('#' + $(this).attr("id")).datetimepicker(dtOption);
		} catch (e) {}
	});
	if (noName) {
		$("img[onclick!='']").each(function() {
			var oc = $(this).attr('onclick');
			if (typeof(oc) != 'undefined' && oc != '' && (oc.indexOf('SelectDate(') == 0 || oc.indexOf('SelectDateTime(') == 0)) {
				$(this).hide();
			}
		});
	}
}

var whitePadObj;
// 为了防止HTML对象ID的重复
var whitePadWriteCount = 0;
// 手写板
function openWhitePadWin(objName, width, height){
	whitePadObj = findObj(objName);
	var win = window.open("<%=request.getContextPath()%>/spwhitepad/editor.jsp?width=" + width + "&height=" + height,"spwhitepadeditor","width=420,height=340,left=200,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,status=no");
	win.focus();
}

function insertStroke(code, width, height) {
	whitePadObj.value = code;
	
	var len1 = "[whitepad]".length;
	var len2 = "[/whitepad]".length;
	
	code = code.substring(len1, code.length - len2);
	
    var str = "<textarea style='display:none' id='value_spwhitepad_" + whitePadWriteCount;
	str += "'>" + code + "</textarea><iframe src='<%=request.getContextPath()%>/spwhitepad/show.htm' name='spwhitepad_";
	str += whitePadWriteCount + "' frameborder='0' style='width:" + width + "px;height:" + height + "px;margin:5px;border:1px dashed #CCCCCC;' scrolling='no'></iframe>";
	whitePadWriteCount ++;
	if (o("span_pad_" + whitePadObj.name)==null) {
		whitePadObj.insertAdjacentHTML("AfterEnd", "<span id='span_pad_" + whitePadObj.name + "'></span>");
	}
	o("span_pad_" + whitePadObj.name).innerHTML = str;
}

includFile(getContextPath() + "/js/colorpicker/",['jquery.bigcolorpicker.css']);
includFile(getContextPath() + "/js/colorpicker/",['jquery.bigcolorpicker.min.js']);