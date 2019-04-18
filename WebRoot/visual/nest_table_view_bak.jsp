<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
* 2010-6-6 备份
* 修改成支持jscalendar更改日期型单元格
*/
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print("表单不存在！");
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
/*
用于显示嵌套表格于父表单中，由NestTableCtl.converToHTML通过url连接调用，注意需在用到此文件的页面中，置request属性cwsId、pageType、action
*/
String op = ParamUtil.get(request, "op");
%>
<style>
#cwsNestTable{
	font-size: 9pt;
	word-break:break-all;
	cursor: default;
	BORDER: black 1px solid;
	background-color:#ffffff;
	border-collapse:collapse;
	border-Color:#999999;
	align:center;
}
.cwsThead {
background-color:#eeeeee;
}
.resizeTd {
	background-color:#eeeeee;   
	width:2px;
	position:relative;
	left:expression(this.parentElement.offsetWidth-4);
	z-index:1;
	cursor:e-resize;
	float:left;
}   
</style>
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
<script language="JavaScript1.2">
/*
 This following code are designed and writen by Windy_sk <[email]seasonx@163.net[/email]>
 You can use it freely, but u must held all the copyright items!
*/

var Main_Tab	= null;
var cur_row		= null;
var cur_col		= null;
var cur_cell	= null;
var Org_con		= "";
var sort_col	= null;

var show_col	= false;
var charMode	= true;
var act_bgc		= "#BEC5DE";
var act_fc		= "black";
var cur_bgc		= "#ccffcc";
var cur_fc		= "black";

var canTdEditable = true;

function init(){
	cur_row		= null;
	cur_col		= null;
	cur_cell	= null;
	sort_col	= null;
	Main_Tab 	= cwsNestTable;
	read_def(Main_Tab)
	Main_Tab.onmouseover = overIt;
	Main_Tab.onmouseout	= outIt;
	Main_Tab.onclick = clickIt;
	Main_Tab.ondblclick	= dblclickIt;
	Org_con = Main_Tab.outerHTML;
	
	arrowUp = document.createElement("SPAN");
	arrowUp.innerHTML = "5";
	arrowUp.style.cssText = "PADDING-RIGHT: 0px; MARGIN-TOP: -3px; PADDING-LEFT: 0px; FONT-SIZE: 10px; MARGIN-BOTTOM: 2px; PADDING-BOTTOM: 2px; OVERFLOW: hidden; WIDTH: 10px; COLOR: blue; PADDING-TOP: 0px; FONT-FAMILY: webdings; HEIGHT: 11px";

	arrowDown = document.createElement("SPAN");
	arrowDown.innerHTML	= "6";
	arrowDown.style.cssText = "PADDING-RIGHT: 0px; MARGIN-TOP: -3px; PADDING-LEFT: 0px; FONT-SIZE: 10px; MARGIN-BOTTOM: 2px; PADDING-BOTTOM: 2px; OVERFLOW: hidden; WIDTH: 10px; COLOR: blue; PADDING-TOP: 0px; FONT-FAMILY: webdings; HEIGHT: 11px";
}
	
function window.onload(){
	// dragDiv = document.createElement("DIV");
	// dragDiv = document.getElementById("dragDiv");
	dragDiv.innerHTML		= "";
	dragDiv.style.textAlign	= "center";
	dragDiv.style.position	= "absolute";
	dragDiv.style.cursor 	= "hand";
	dragDiv.style.border 	= "1 solid black";
	dragDiv.style.display 	= "none";
	dragDiv.style.zIndex 	= "999";
	// document.body.insertBefore(dragDiv);
	
	setInterval("judge_move()",100);
	setInterval("doInterval()",1000);
}

function judge_move(){
	try {
		move[0].disabled=(cur_row == null || cur_row<=1);
		move[1].disabled=(cur_row == null || cur_row==Main_Tab.rows.length-1 || cur_row == 0);
		move[2].disabled=(cur_col == null || cur_col==0);
		move[3].disabled=(cur_col == null || cur_col==Main_Tab.rows[0].cells.length-1);
	}
	catch (e) {}
}

function doInterval() {
try {
	cwsNestTableShowContent.value=Main_Tab.innerHTML;cwsNestTableMonitor.value='cur_row: '+cur_row+'; cur_col: '+cur_col + '; sort_col: ' +sort_col
}
catch (e) {}
}

//document.onselectstart = function(){return false;}
document.onmouseup = drag_end;

<%if (!op.equals("view")) {
	String formName = "visualForm";
	String pageType = ParamUtil.get(request, "pageType");
	if (pageType.equals("flow"))
		formName = "flowForm";
%>

if (document.<%=formName%>) {
	document.<%=formName%>.onsubmit=function(){
		var obj = document.getElementById("cwsNestTable");
		var rows = obj.rows.length;
		var str = "";
		for (var i=1; i<rows; i++) {
			var cel = obj.rows.item(i).cells;
			for (var j=0; j<cel.length; j++) {
				if (cel[j].children.length>0) {
					var cellDiv = cel[j].children[0];
					str += "<input type='hidden' name='cws_cell_" + (i-1) + "_" + j + "' value='" + cellDiv.innerText.trim() + "' />";
				}
				else {
					str += "<input type='hidden' name='cws_cell_" + (i-1) + "_" + j + "' value='" + cel[j].innerText.trim() + "' />";
				}
			}
		}
		str += "<input name='cws_cell_rows' type='hidden' value='" + (rows-1) + "' />";
		cwsNestTableHelper.innerHTML = str;
		return true;
		// return false;
	}
}
<%}%>

function clear_color(){
	the_table=Main_Tab;
	if(cur_col!=null){
		for(i=0;i<the_table.rows.length;i++){
			with(the_table.rows[i].cells[cur_col]){
				style.backgroundColor=oBgc;
				style.color=oFc;
			}
		}
	}
	if(cur_row!=null){
		for(i=0;i<the_table.rows[cur_row].cells.length;i++){
			with(the_table.rows[cur_row].cells[i]){
				style.backgroundColor=oBgc;
				style.color=oFc;
			}
		}
	}
	if(cur_cell!=null){
		if(cur_cell.children[0].tagName=="INPUT") {
			if (_dynarch_popupCalendar==null || (_dynarch_popupCalendar!=null && _dynarch_popupCalendar.hidden)) {
				cur_cell.children[0].outerHTML = "<div>" + cur_cell.children[0].value + "</div>";
			}
		}
		cur_cell.children[0].contentEditable = false;
		with(cur_cell.children[0].runtimeStyle){
			borderLeft=borderTop="";
			borderRight=borderBottom="";
			backgroundColor="";
			paddingLeft="";
			textAlign="";
		}
	}
}

function document.onclick(){
	window.status = "";
	clear_color();
	cur_row  = null;
	cur_col  = null;
	
	if (_dynarch_popupCalendar==null)
		cur_cell = null;
	else {
		if (_dynarch_popupCalendar.hidden) {
			cur_cell = null;
		}
	}
}

function read_def(the_table){
	for(var i=0;i<the_table.rows.length;i++){
		for(var j=0;j<the_table.rows[i].cells.length;j++){
			with(the_table.rows[i]){
				cells[j].oBgc = cells[j].currentStyle.backgroundColor;
				cells[j].oFc  = cells[j].currentStyle.color;
				if(i==0){
					// cells[j].onmousedown= drag_start;
					// cells[j].onmouseup	= drag_end;
				}
			}
		}
	}
}

function get_Element(the_ele,the_tag){
	the_tag = the_tag.toLowerCase();
	if(the_ele.tagName.toLowerCase()==the_tag)return the_ele;
	while(the_ele=the_ele.offsetParent){
		if(the_ele.tagName.toLowerCase()==the_tag)return the_ele;
	}
	return(null);
}

var dragStart		= false;
var dragColStart	= null;
var dragColEnd		= null;

function drag_start(){
	var the_td	= get_Element(event.srcElement,"td");
	if(the_td==null) return;
	dragStart	= true;
	dragColStart	= the_td.cellIndex;
	dragDiv.style.width	= the_td.offsetWidth;
	dragDiv.style.height	= the_td.offsetHeight;
	function document.onmousemove(){
		dragDiv.style.display	= "";
		dragDiv.style.top		= event.y - dragDiv.offsetHeight/2;
		dragDiv.style.left		= event.x - dragDiv.offsetWidth/2;
		for(var i=0;i<Main_Tab.rows[0].cells.length;i++){
			with(Main_Tab.rows[0].cells[i]){
				if((event.y>offsetTop+parseInt(document.body.currentStyle.marginTop) && event.y<offsetTop+offsetHeight+parseInt(document.body.currentStyle.marginTop)) && (event.x>offsetLeft+parseInt(document.body.currentStyle.marginLeft) && event.x<offsetLeft+offsetWidth+parseInt(document.body.currentStyle.marginLeft))){
					runtimeStyle.backgroundColor=act_bgc;
					dragColEnd=cellIndex;
				}else{
					runtimeStyle.backgroundColor="";
				}
			}
		}
		if(!(event.y>Main_Tab.rows[0].offsetTop+parseInt(document.body.currentStyle.marginTop) && event.y<Main_Tab.rows[0].offsetTop+Main_Tab.rows[0].offsetHeight+parseInt(document.body.currentStyle.marginTop))) dragColEnd=null;
	}
	dragDiv.innerHTML = the_td.innerHTML;
	dragDiv.style.backgroundColor = the_td.oBgc;
	dragDiv.style.color = the_td.oFc;
}

function drag_end(){
	dragStart = false;
	dragDiv.style.display="none";
	dragDiv.innerHTML = "";
	dragDiv.style.width = 0;
	dragDiv.style.height = 0;
	for(var i=0;i<Main_Tab.rows[0].cells.length;i++){
		Main_Tab.rows[0].cells[i].runtimeStyle.backgroundColor="";
	}
	if(dragColStart!=null && dragColEnd!=null && dragColStart!=dragColEnd){
		change_col(Main_Tab,dragColStart,dragColEnd);
		if(dragColStart==sort_col)sort_col=dragColEnd;
		else if(dragColEnd==sort_col)sort_col=dragColStart;
		document.onclick();
	}
	dragColStart = null;
	dragColEnd = null;
	document.onmousemove=null;
}

function clickIt(){
	event.cancelBubble=true;
	var the_obj = event.srcElement;
	var i = 0 ,j = 0;
	if(cur_cell!=null && cur_row!=0){
		cur_cell.children[0].contentEditable = false;
		with(cur_cell.children[0].runtimeStyle){
			borderLeft=borderTop="";
			borderRight=borderBottom="";
			backgroundColor="";
			paddingLeft="";
			textAlign="";
		}
	}
	if(the_obj.tagName.toLowerCase() != "table" && the_obj.tagName.toLowerCase() != "tbody" && the_obj.tagName.toLowerCase() != "tr"){
		var the_td	= get_Element(the_obj,"td");
		if(the_td==null) return;
		var the_tr	= the_td.parentElement;
		var the_table	= get_Element(the_td,"table");
		var i 		= 0;
		clear_color();
		cur_row = the_tr.rowIndex;
		cur_col = the_td.cellIndex;
		if(cur_row!=0){
			for(i=0;i<the_tr.cells.length;i++){
				with(the_tr.cells[i]){
					style.backgroundColor=cur_bgc;
					style.color=cur_fc;
				}
			}
		}else{
			if(show_col){
				for(i=1;i<the_table.rows.length;i++){
					with(the_table.rows[i].cells[cur_col]){
						style.backgroundColor=cur_bgc;
						style.color=cur_fc;
					}
				}
			}
			
			the_td.mode = !the_td.mode;
			if(sort_col!=null){
				with(the_table.rows[0].cells[sort_col])
					removeChild(lastChild);
			}
			with(the_table.rows[0].cells[cur_col])
				appendChild(the_td.mode?arrowUp:arrowDown);
			sort_tab(the_table,cur_col,the_td.mode);
			sort_col=cur_col;
		}
	}
}

function selected(cal, date) {
  cal.sel.value = date; // just update the date in the input field.
  if (cal.dateClicked && (cal.sel.id == "sel1" || cal.sel.id == "sel3"))
    // if we add this call we close the calendar on single-click.
    // just to exemplify both cases, we are using this only for the 1st
    // and the 3rd field, while 2nd and 4th will still require double-click.
    cal.callCloseHandler();
}
function closeHandler(cal) {
  cal.hide();                        // hide the calendar
//  cal.destroy();
  _dynarch_popupCalendar = null;
}
function showCalendar(id, format, showsTime, showsOtherMonths) {
  var el = document.getElementById(id);
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

var objId = 1;

function dblclickIt(){
	if (!canTdEditable)
		return;
	event.cancelBubble=true;
	if(cur_row!=0){
		var the_obj = event.srcElement;
		if(the_obj.tagName.toLowerCase() != "table" && the_obj.tagName.toLowerCase() != "tbody" && the_obj.tagName.toLowerCase() != "tr"){
			var the_td	= get_Element(the_obj,"td");
			if(the_td==null) return;
			
			if(the_td.editable=="0") return;
			
			cur_cell = the_td;
			if(the_td.children.length!=1)
				the_td.innerHTML="<div>" + the_td.innerHTML + "</div>";
			else if(the_td.children.length==1 && the_td.children[0].tagName.toLowerCase()!="div")
				the_td.innerHTML="<div>" + the_td.innerHTML + "</div>";
			if (cur_cell.type=="<%=FormField.TYPE_DATE%>" || cur_cell.type=="<%=FormField.TYPE_DATE_TIME%>") {
				cur_cell.children[0].id = "fieldObj" + objId;
				cur_cell.children[0].outerHTML = "<input id='" + cur_cell.children[0].id + "' value='" + cur_cell.children[0].innerText + "' />";
				// showCalendar(cur_cell.children[0].id, '%Y-%m-%d %H:%M', '24', true);
				showCalendar(cur_cell.children[0].id, '%Y-%m-%d', '24', true);
				objId++;
			}
			else
				cur_cell.children[0].contentEditable = true;
			with(cur_cell.children[0].runtimeStyle){
				borderRight=borderBottom="buttonhighlight 1px solid";
				borderLeft=borderTop="black 1px solid";
				backgroundColor="#dddddd";
				paddingLeft="5px";
				//textAlign="center";
			}
		}
	}
}

function overIt(){
	if(dragStart)return;
	var the_obj = event.srcElement;
	var i = 0;
	if(the_obj.tagName.toLowerCase() != "table"){
		var the_td	= get_Element(the_obj,"td");
		if(the_td==null) return;
		var the_tr	= the_td.parentElement;
		var the_table	= get_Element(the_td,"table");
		if(the_tr.rowIndex!=0){
			for(i=0;i<the_tr.cells.length;i++){
				with(the_tr.cells[i]){
					runtimeStyle.backgroundColor=act_bgc;
					runtimeStyle.color=act_fc;					
				}
			}
		}else{
			for(i=1;i<the_table.rows.length;i++){
				with(the_table.rows[i].cells(the_td.cellIndex)){
					runtimeStyle.backgroundColor=act_bgc;
					runtimeStyle.color=act_fc;
				}
			}
			if(the_td.mode==undefined)the_td.mode = false;
			//the_td.style.cursor=the_td.mode?"n-resize":"s-resize";
		}
	}
}

function outIt(){
	var the_obj = event.srcElement;
	var i=0;
	if(the_obj.tagName.toLowerCase() != "table"){
		var the_td	= get_Element(the_obj,"td");
		if(the_td==null) return;
		var the_tr	= the_td.parentElement;
		var the_table	= get_Element(the_td,"table");
		if(the_tr.rowIndex!=0){
			for(i=0;i<the_tr.cells.length;i++){
				with(the_tr.cells[i]){
					runtimeStyle.backgroundColor='';
					runtimeStyle.color='';				
				}
			}
		}else{
			var the_table=the_tr.parentElement.parentElement;
			for(i=0;i<the_table.rows.length;i++){
				with(the_table.rows[i].cells(the_td.cellIndex)){
					runtimeStyle.backgroundColor='';
					runtimeStyle.color='';
				}
			}
		}
	}
}

var charPYStr = "";
var charBHStr = "";
function judge_CN(char1,char2,mode){
	var charSet=charMode?charPYStr:charBHStr;
	for(var n=0;n<(char1.length>char2.length?char1.length:char2.length);n++){
		if(char1.charAt(n)!=char2.charAt(n)){
			if(mode) return(charSet.indexOf(char1.charAt(n))>charSet.indexOf(char2.charAt(n))?1:-1);
			else	 return(charSet.indexOf(char1.charAt(n))<charSet.indexOf(char2.charAt(n))?1:-1);
			break;
		}
	}
	return(0);
}

function sort_tab(the_tab,col,mode){
	var tab_arr = new Array();
	var i;
	var start=new Date;
	for(i=1;i<the_tab.rows.length;i++){
		tab_arr.push(new Array(the_tab.rows[i].cells[col].innerText.toLowerCase(),the_tab.rows[i]));
	}
	function SortArr(mode) {
		return function (arr1, arr2){
			var flag;
			var a,b;
			a = arr1[0];
			b = arr2[0];
			if(/^(\+|-)?\d+($|\.\d+$)/.test(a) && /^(\+|-)?\d+($|\.\d+$)/.test(b)){
				a=eval(a);
				b=eval(b);
				flag=mode?(a>b?1:(a<b?-1:0)):(a<b?1:(a>b?-1:0));
			}else{
				a=a.toString();
				b=b.toString();
				if(a.charCodeAt(0)>=19968 && b.charCodeAt(0)>=19968){
					flag = judge_CN(a,b,mode);
				}else{
					flag=mode?(a>b?1:(a<b?-1:0)):(a<b?1:(a>b?-1:0));
				}
			}
			return flag;
		};
	}
	tab_arr.sort(SortArr(mode));

	for(i=0;i<tab_arr.length;i++){
		the_tab.lastChild.appendChild(tab_arr[i][1]);
	}

	window.status = " (Time spent: " + (new Date - start) + "ms)";
}

function change_row(the_tab,line1,line2){
	the_tab.rows[line1].swapNode(the_tab.rows[line2])
}

function change_col(the_tab,line1,line2){
	for(var i=0;i<the_tab.rows.length;i++)
		the_tab.rows[i].cells[line1].swapNode(the_tab.rows[i].cells[line2]);
}

function Move_up(the_table){
	event.cancelBubble=true;
	if(cur_row==null || cur_row<=1)return;
	change_row(the_table,cur_row,--cur_row);
}

function Move_down(the_table){
	event.cancelBubble=true;
	if(cur_row==null || cur_row==the_table.rows.length-1 || cur_row==0)return;
	change_row(the_table,cur_row,++cur_row);
}

function Move_left(the_table){
	event.cancelBubble=true;
	if(cur_col==null || cur_col==0)return;
	change_col(the_table,cur_col,--cur_col);
	if(cur_col==sort_col)sort_col=cur_col+1;
	else if(cur_col+1==sort_col)sort_col=cur_col;
}

function Move_right(the_table){
	event.cancelBubble=true;
	if(cur_col==null || cur_col==the_table.rows[0].cells.length-1)return;
	change_col(the_table,cur_col,++cur_col);
	if(cur_col==sort_col)sort_col=cur_col-1;
	else if(cur_col-1==sort_col)sort_col=cur_col;
}

function add_row(the_table) {
	event.cancelBubble=true;
	var the_row,the_cell;
	the_row = cur_row==null?-1:(cur_row+1);
	clear_color();
	var newrow=the_table.insertRow(the_row);
	for (var i=0;i<the_table.rows[0].cells.length;i++) {
		the_cell=newrow.insertCell(i);
		the_cell.innerText=" ";
		// the_cell.innerText="NewRow_" + the_cell.parentElement.rowIndex;
		if (i==0) {
		<%if (op.equals("edit")) {%>
		the_cell.setAttribute("editable","0");
		<%}%>
		}
	}
	read_def(the_table);
}

function del_row(the_table) {
	if(the_table.rows.length==1) return;
	var the_row;
	the_row = (cur_row==null || cur_row==0)?-1:cur_row;
	the_table.deleteRow(the_row);
	cur_row = null;
	cur_cell=null;
}

function add_col(the_table) {
	event.cancelBubble=true;
	var the_col,i,the_cell;
	the_col = cur_col==null?-1:(cur_col+1);
	var the_title=prompt("请输入标题: ","Untitled");
	if(the_title==null)return;
	if(the_col!=-1 && the_col<=sort_col && sort_col!=null)sort_col++;
	the_title=the_title==""?"Untitled":the_title
	clear_color();
	for(var i=0;i<the_table.rows.length;i++){
		the_cell=the_table.rows[i].insertCell(the_col);
		the_cell.innerText=i==0?the_title:("NewCol_" + the_cell.cellIndex);
	}
	read_def(the_table);
}

function del_col(the_table) {
	if(the_table.rows[0].cells.length==1) return;
	var the_col,the_cell;
	the_col = cur_col==null?(the_table.rows[0].cells.length-1):cur_col;
	if(the_col!=-1 && the_col<sort_col && sort_col!=null)sort_col--;
	else if(the_col==sort_col)sort_col=null;
	for(var i=0;i<the_table.rows.length;i++) the_table.rows[i].deleteCell(the_col);
	cur_col = null;
	cur_cell=null;
}

function res_tab(the_table){
	the_table.outerHTML=Org_con;
	init();
}

function exp_tab(the_table){
	var the_content="";
	document.onclick();
	the_content=the_table.outerHTML;
	the_content=the_content.replace(/ style=\"[^\"]*\"/g,"");
	the_content=the_content.replace(/ mode=\"(false|true)"/g,"");
	the_content=the_content.replace(/ oBgc=\"[\w#\d]*\"/g,"");
	the_content=the_content.replace(/ oFc=\"[\w#\d]*\"/g,"");
	the_content=the_content.replace(/<DIV contentEditable=false>([^<]*)<\/DIV>/ig,"$1");
	the_content="<style>table{font-size: 9pt;word-break:break-all;cursor: default;BORDER: black 1px solid;background-color:#eeeecc;border-collapse:collapse;border-Color:#999999;align:center;}</style>\n"+the_content;
	var newwin=window.open("about:blank","_blank","");
	newwin.document.open();
	newwin.document.write(the_content);
	newwin.document.close();
	newwin=null;
}

function MouseDownToResize(obj){   
	obj.mouseDownX=event.clientX;   
	obj.pareneTdW=obj.parentElement.offsetWidth;   
	obj.pareneTableW=cwsNestTable.offsetWidth;   
	obj.setCapture();   
}

function MouseMoveToResize(obj){   
	if(!obj.mouseDownX)
		return false;   
	var newWidth=obj.pareneTdW*1+event.clientX*1-obj.mouseDownX;   
	if(newWidth>0){
		obj.parentElement.style.width = newWidth;   
		cwsNestTable.style.width=obj.pareneTableW*1+event.clientX*1-obj.mouseDownX;   
	}
}

function MouseUpToResize(obj){   
obj.releaseCapture();   
obj.mouseDownX=0;   
}
</script>
<div id="dragDiv" style="display:none"></div>
<%		
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
String[] fieldsOrder = StrUtil.split(listFieldOrder, ",");
int len = 0;
if (fields!=null)
	len = fields.length;
if (op.equals("edit") || op.equals("view")) {	
	String cwsId = ParamUtil.get(request, "cwsId");
	String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(cwsId);
	sql += " order by cws_order";
	
	// out.print(sql);
	
	FormDAO fdao = new FormDAO();
	Iterator ir = fdao.list(formCode, sql).iterator();
	%>
	<table id="cwsNestTable" width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
      <tr align="center" class="cwsThead">
	  <%if (!op.equals("view")) {%>
        <td style="display:">
		<div class="resizeTd" onmousedown="MouseDownToResize(this);" onmousemove="MouseMoveToResize(this);" onmouseup="MouseUpToResize(this);"></div>		
		ID
		</td>
	  <%}%>
        <%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String title = "创建者";
		if (!fieldName.equals("cws_creator"))
			title = fd.getFieldTitle(fieldName);
	%>
        <td width="<%=fieldsWidth[i]%>">
		<div class="resizeTd" onmousedown="MouseDownToResize(this);" onmousemove="MouseMoveToResize(this);" onmouseup="MouseUpToResize(this);"></div>		
		<%=title%>
		</td>
        <%}%>
      </tr>
      <%	
			int k = 0;
			UserMgr um = new UserMgr();
			while (ir!=null && ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				k++;
				long id = fdao.getId();
			%>
      <tr align="center">
	  <%if (!op.equals("view")) {%>	  
        <td editable=0 style="display:"><%=fdao.getId()%></td>
	  <%}%>
        <%
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
			FormField ff = fd.getFormField(fieldName);
		%>
        <td align="left" type="<%=ff.getType()%>">
			<%if (!fieldName.equals("cws_creator")) {%>
            <%=fdao.getFieldValue(fieldName)%>
            <%}else{%>
            <%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>
            <%}%>
        </td>
        <%}%>
      </tr>
      <%
	  }
	%>
    </table>
	<%if (op.equals("edit")) {%>
	<table width="100%" id="tableOperate">
	  <tr>
		<td align="center">
			<input type="button" onclick="add_row(Main_Tab)" value="插入行" />
			<input type="button" onclick="del_row(Main_Tab)" value="删除行" />
			<input type=button id=move value=上移 onclick=Move_up(Main_Tab)>
			<input type=button id=move value=下移 onclick=Move_down(Main_Tab)>
			<div id="cwsNestTableHelper"></div>
			<div id=detail style="display:none">
			  <input name="text" type="text" id="text" style="width:200px" size="30" />
			  <textarea name="textarea" cols="100" rows="20" id="textarea"></textarea>
		  </div></td>
	  </tr>
	</table>
	<%}%>
<%}else{%>
	<table width="100%" border="1" cellspacing="0" cellpadding="2" id="cwsNestTable">
		  <tr align="middle" class="cwsThead">
	<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String title = "创建者";
		if (!fieldName.equals("cws_creator"))
			title = fd.getFieldTitle(fieldName);
	%>	  
			<td>
			<div class="resizeTd" onmousedown="MouseDownToResize(this);" onmousemove="MouseMoveToResize(this);" onmouseup="MouseUpToResize(this);"></div>
			<%=title%>
			</td>
	<%}%>	
		  </tr>
	</table>
	<table width="100%">
	  <tr><td align="center">
	<input type=button value=插入行 onclick=add_row(Main_Tab)>
	<input name="button" type=button onClick=del_row(Main_Tab) value=删除行>
	<input type=button id=move value=上移 onclick=Move_up(Main_Tab)>
	<input type=button id=move value=下移 onclick=Move_down(Main_Tab)>
	<!--
	<input type=button value=插入列 onclick=add_col(Main_Tab)>
	<input type=button value=删除列 onclick=del_col(Main_Tab)>
	<input type=button value=Restore onclick=res_tab(Main_Tab)>
	<input type=button value=Export  onclick=exp_tab(Main_Tab)>
	<input type=button id=move value=Left  onclick=Move_left(Main_Tab)>
	<input type=button id=move value=Right onclick=Move_right(Main_Tab)>
	-->
	<div id="cwsNestTableHelper"></div>
	<div id=detail style="display:none">
	<input type=text id=cwsNestTableMonitor size=30 style="width:200px">
	<textarea id=cwsNestTableShowContent cols=100 rows=20></textarea>
	</div>
	</td></tr></table>
<%}%>
<script>
init();
</script>