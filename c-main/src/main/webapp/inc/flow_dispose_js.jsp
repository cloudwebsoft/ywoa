<%@ page contentType="text/html; charset=utf-8"%>
<%
	/**
	 * 用于智能模块设计中
	 *
	 **/
	String rootPath = request.getContextPath();
	response.setContentType("text/javascript;charset=utf-8");
	// 防漏洞：1; mode=block 启用XSS保护，并在检查到XSS攻击时，停止渲染页面
	response.setHeader("X-XSS-Protection", "1; mode=block");
%>
function findObj(objName, theDoc) {
	var flowForm = o("flowForm");
	if (flowForm==null) {
		flowForm = o("visualForm"); // 用于嵌套表格2在流程中处理时
	}
	return $(flowForm).find("[name='" + objName + "']")[0];
}

function getradio(radionname) {
	var radioboxs = document.getElementsByName(radionname);
	if (radioboxs!=null)
	{
		for (i=0; i < radioboxs.length; i++) {
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
		//return checkboxboxs.value
	}
	return CheckboxValue;
}

function getCtlValue(ctlObj, ctlType) {
	try {
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
	catch (e) {
	}
}

function setCtlValue(ctlName, ctlType, ctlValue, nestFormCode) {
	try {
		// @task:当ctlValue为visualForm.cws_textarea_province.value且为空时，赋值后select控件不能显示<option value=''>无</option>，而一定要将ctlValue置为""
		if (ctlValue=="")
			ctlValue = "";
		var obj = findObj(ctlName);
		// 如果是嵌套表格nest_table
		if (nestFormCode != null) {
			$nestTable = $("[id^=nestTable_][formCode=" + nestFormCode + "]");
			if ($nestTable[0]) {
				obj = $nestTable.find('[name=' + ctlName + ']')[0];
			}
			else {
				console.error('Field ' + ctlName + ' is not found in nest table: ' + nestFormCode);
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
		// console.log(e.message);
		if (_canLog()) {
			console.log("Field: " + ctlName + " may not exist or maybe it's nest sheet.");
		}
	}
}

// 禁止控件的同时，在其后插入hidden控件，以使被禁止的控件的值能够上传
function DisableCtl(name, ctlType, ctlValue, ctlValueRaw) {
   var flowForm = o("flowForm");
   if (flowForm==null) {
   	flowForm = o("visualForm"); // 用于嵌套表格2在流程中处理时
   }
   for(var i=0;i < flowForm.elements.length;i++) {
		var obj = flowForm.elements[i];
		var kind = obj.getAttribute(kind);
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
                    obj.outerHTML = "<img src='<%=rootPath%>/images/checkbox_y.gif' align='absMiddle'>";
				}
				else {
					obj.insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='0'>");
					// obj.outerHTML = "(否)";
                    obj.outerHTML = "<img src='<%=rootPath%>/images/checkbox_n.gif' align='absMiddle'>";                    
				}                
			}
			else if (ctlType == "select") {
				var text = $("[name='" + name + "'] option[value='" + ctlValue + "']").text();
				obj.outerHTML = "<span id='" + name + "_show'>" + text + "</span><textarea style='display:none' name='" + name + "'>" + ctlValueRaw + "</textarea>";
			}
            else if (ctlType=="radio") {
                 var radioboxs = document.getElementsByName(name);
                 if (radioboxs!=null) {
                 	for (j=0; j < radioboxs.length; j++) {
                    	radioboxs[j].disabled = true;
                    }
                    radioboxs[0].insertAdjacentHTML("AfterEnd", "<input type=hidden name='" + name + "' value='" + ctlValue + "'>");
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
                
                if (o(name + "_btn")) {
                	o(name + "_btn").style.display = "none";
                }				
			}
			return;
		}
   }	
}

// 用控件的值来替代控件，用于把表单以报表方式显示时
function ReplaceCtlWithValue(name, ctlType, ctlValue) {
	var flowForm;
	if (o("flowForm")) {
		flowForm = o("flowForm");
	} else {
		flowForm = o("visualForm");
	}
	try {
	   for(var i=0;i < flowForm.elements.length;i++) {
			var obj = flowForm.elements[i];
			var kind = obj.getAttribute(kind);

			if (obj.name==name) {
				if (ctlType=="checkbox") {
                	if (obj.value==ctlValue)
                    	obj.outerHTML = "<img src='<%=rootPath%>/images/checkbox_y.gif' align='absMiddle'>";
                    else
                    	obj.outerHTML = "<img src='<%=rootPath%>/images/checkbox_n.gif' align='absMiddle'>";
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
                                arr[j].outerHTML = "<img src='<%=rootPath%>/images/radio_y.gif' align='absMiddle'>";
                            else
                                arr[j].outerHTML = "<img src='<%=rootPath%>/images/radio_n.gif' align='absMiddle'>";
                        }
                    }
                }
				else {
					// 去除日历控件的图片
					if (ctlType=="DATE" || ctlType=="DATE_TIME") {
						if (ctlType=="DATE_TIME") {
							try {
								btnImgObj = findObj(name + "_time_btnImg");
								btnImgObj.outerHTML = "";
							}
							catch (e) {}							
						}
							
						try {
							btnImgObj = findObj(name + "_btnImg");
							btnImgObj.outerHTML = "";
						}
						catch (e) {}
					}
				
					if (ctlType=="DATE_TIME") {
						// 去除时间中的时分秒域
						if (o(name + "_time") != null) {
							var timeObj = findObj(name + "_time");
							timeObj.outerHTML = "";
						}
					}
					if($("img[onclick!='']") != null){
						$("img[onclick!='']").each(function() {
							var oc = $(this).attr('onclick');
							if (typeof(oc) != 'undefined' && oc != '' && (oc.indexOf('SelectDate(') == 0 || oc.indexOf('SelectDateTime(') == 0)) {
								$(this).hide();
							}
						});
					}

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
   catch (e) {}
}

// 清除其它辅助图片按钮等
function ClearAccessory() {
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
				// alert(imgObj.src);
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
		var inputs;
		if (isIE8) {
			inputs = document.all.tags('input');
		}
		else {
			inputs = document.getElementsByTagName('input');
		}
		len = inputs.length;
        
		for(i=0; i < len; i++) { 
			try {
				var btnObj = inputs[i];
				if (btnObj.type=="hidden" || (btnObj.type=="text" && btnObj.name=="title") || btnObj.type=="checkbox" || btnObj.type=="radio")
					continue;
				try {
					if (btnObj.reserve=="true")
						continue;
				}
				catch (e) {
				}
				$(btnObj).remove();
				isFinded = true;
			}
			catch (e) {}
		}
		
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
    
        GetDate = showModalDialog("<%=rootPath%>/util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
    }
}

function SetNewDate() {
	var noName = false;
	$("input[kind='DATE']").each(function() {
    	if($(this).attr("readonly")==true || $(this).attr("readonly")=="readonly") {
			return true;
        }
            
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
		
        var dateObj = this;
        		
		try {
			var dtOption = {
				lang:'ch',
				datepicker:true,
				timepicker:false,
				validateOnBlur:false, // 解决IE8下需点击两次才能选到时间，且选择后livevalidation仍显示不能为空的问题
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
		} catch (e){}
	});
	$("input[kind='DATE_TIME']").each(function() {
    	if($(this).attr("readonly")==true || $(this).attr("readonly")=="readonly") {
			return true;
        }

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
				if ($(this).val().indexOf(':') == -1) {
					$(this).val($(this).val() + " " + time.value);
				}
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

        var dateObj = this;
		
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
				formatDate:'Y-m-d',
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
		} catch (e) {
			console.log(e);
		}
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

// 新时间控件可能无法在dialog中显示
function SetOldDate() {
	$("input[kind='DATE']").each(function() {
		if (typeof($(this).attr('id')) == 'undefined') {
			$(this).attr('id', $(this).attr('name'));
		}
		Calendar.setup({
	        inputField     :    $(this).attr('id'),
	        ifFormat       :    "%Y-%m-%d",
	        showsTime      :    false,
	        singleClick    :    false,
	        align          :    "Tl",
	        step           :    1
	    });
	});
	$("input[kind='DATE_TIME']").each(function() {
		if (typeof($(this).attr('id')) == 'undefined') {
			$(this).attr('id', $(this).attr('name'));
		}
		Calendar.setup({
	        inputField     :    $(this).attr('id'),
	        ifFormat       :    "%Y-%m-%d %H:%M:%S",
	        showsTime      :    true,
	        singleClick    :    false,
	        align          :    "Tl",
	        step           :    1
	    });
	});
}

function SelectNewDate(ObjName,FormatDate) {
	var scFormat = false;
	try {
		if (FormatDate == "yyyy-MM-dd") {
			$('#' + ObjName).datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:false,
				format:'Y-m-d'
			});
		} else {
			scFormat = true;
			$('#' + ObjName).datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:true,
				format:'Y-m-d H:i:00',
				step:10
			});
		}
	} catch (e) {
		if (scFormat) {
			showCalendar(ObjName, '%Y-%m-%d %H:%M:%S', '24', true);
		} else {
			showCalendar(ObjName, '%Y-%m-%d', null, true);
		}
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
    /**var cal = new Calendar(1, null, selected, closeHandler);
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
    cal.create();*/
    //切换时间控件 modify by jfy 2015-08-28
    $('#' + id).datetimepicker({
            lang:'ch',
            datepicker:true,
            timepicker:false,
            format:'Y-m-d'
        });
  }
 // _dynarch_popupCalendar.setDateFormat(format);    // set the specified date format
  //_dynarch_popupCalendar.parseDate(el.value);      // try to parse the text in field
 // _dynarch_popupCalendar.sel = el;                 // inform it what input field we use

  // the reference element that we pass to showAtElement is the button that
  // triggers the calendar.  In this example we align the calendar bottom-right
  // to the button.
  // _dynarch_popupCalendar.showAtElement(el.nextSibling, "Br");        // show the calendar
  //_dynarch_popupCalendar.showAtElement(el, "");        // show the calendar

  return false;
}

var timeObjName;
function SelectDateTime(objName) {
	timeObjName = objName;
	if (isIE()) {
        var dt = showModalDialog("<%=rootPath%>/util/calendar/time.jsp", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
        if (dt!=null)
            findObj(objName + "_time").value = dt;
    }
    else {
    	openWin("<%=rootPath%>/util/calendar/time.jsp", 266, 185);
    }
}

function setDateTime(val) {
	o(timeObjName + "_time").value = val;
}

function HideCtl(name, ctlType, macroType) {
   var len = visualForm.elements.length;
   for(var i=0;i < len;i++) {
		var obj = visualForm.elements[i];
		if (obj.name==name) {
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
			return;
		}
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

// getContextPath不准确，系统有虚拟路径与没有虚拟路径时，得到的目录不一样
// flow_dispose_js.jsp一般在二级目录下，故需加上../
// includFile(getContextPath() + "/../js/colorpicker/",['jquery.bigcolorpicker.css']);
// includFile(getContextPath() + "/../js/colorpicker/",['jquery.bigcolorpicker.min.js']);
includFile("<%=rootPath%>/js/colorpicker/",['jquery.bigcolorpicker.css']);
includFile("<%=rootPath%>/js/colorpicker/",['jquery.bigcolorpicker.min.js']);