function selDate(name,condition){ 
   if($("#"+condition).val() == "1"){
      $("#"+name + " #SEGMENT_DATE_TD").hide();
      $("#"+name + " #POINT_DATE_TD").show();
	  $("#"+name + " #VAGUE_SEGMENT_YEAR_TD").hide()
	  $("#"+name + " #VAGUE_POINT_YEAR_TD").hide();  
   }else{
      if($("#"+condition).val() == "2"){
		  $("#"+name + " #SEGMENT_DATE_TD").hide();
		  $("#"+name + " #POINT_DATE_TD").hide();
		  $("#"+name + " #VAGUE_SEGMENT_YEAR_TD").show();
		  $("#"+name + " #VAGUE_POINT_YEAR_TD").hide();
	  }else{
		  if($("#"+condition).val() == "3"){
			  $("#"+name + " #SEGMENT_DATE_TD").hide();
			  $("#"+name + " #POINT_DATE_TD").hide();
			  $("#"+name + " #VAGUE_SEGMENT_YEAR_TD").hide()
			  $("#"+name + " #VAGUE_POINT_YEAR_TD").show();  
		  }else{
			  $("#"+name + " #SEGMENT_DATE_TD").show();
			  $("#"+name + " #POINT_DATE_TD").hide();
			  $("#"+name + " #VAGUE_SEGMENT_YEAR_TD").hide()
			  $("#"+name + " #VAGUE_POINT_YEAR_TD").hide();  
		  }
	  }
   }
}

var spanId = 10000;

function addORConditionSel(btnObj,name){
	var text;
	if (document.getElementsByName(name + "_OPTION").length==undefined)
    	text = "<span id='span" + spanId + "'>或者&nbsp;<select name='" + name + "'>" + document.getElementsByName(name + "_OPTION").value + "</select><span onclick=\"o('span" + spanId + "').outerHTML=''\" style='color:red;cursor:hand'>&nbsp;×&nbsp;</span></span>";
	else
    	text = "<span id='span" + spanId + "'>或者&nbsp;<select name='" + name + "'>" + document.getElementsByName(name + "_OPTION")[0].value + "</select><span onclick=\"o('span" + spanId + "').outerHTML=''\" style='color:red;cursor:hand'>&nbsp;×&nbsp;</span></span>";
	
	btnObj.insertAdjacentHTML("BeforeBegin", text);
	
	spanId++;
}

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

var GetDate="";
function SelectDate(ObjName, FormatDate) {
	showCalendar(ObjName, '%Y-%m-%d', null, true);
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
  _dynarch_popupCalendar.showAtElement(el, "Br");        // show the calendar

  return false;
}