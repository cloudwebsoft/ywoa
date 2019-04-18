<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%--
- 功能描述：嵌套表
- 访问规则：服务器端包含于flow_dispose.jsp中
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2011.11.20
- 修改原因：使支持隐藏字段
- 修改点：增加hideNestTableCol方法，在render中根据字段nest.***是否隐藏调用
- 但是发现在flow_modify.jsp中查看过程的时候，因为设置列中单元格的display为none，会使得父表单的显示出现列宽变形问题
--%>
<%
/**
* 获取cellIndex时，IE9中隐藏列会计入，而其它版本则不计入，但是cells[...]中隐藏列却又都是存在的
* 用于显示嵌套表格于父表单中，由NestTableCtl.converToHTML通过url连接调用，注意需在用到此文件的页面中，置request属性cwsId、pageType、action
*/
// 因为内外网访问的问题而注释
/*
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print("表单不存在！");
	return;
}

// 因为内外网访问的问题而注释
/*
ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

String op = ParamUtil.get(request, "op");
%>
<style type="text/css">
@import url("<%=request.getContextPath()%>/visual/nest_table_view.css"); 
</style>
<style type="text/css">
/*
.line{border:1px solid #000000; border-bottom-style:none; border-right-style:none;}
.po{ text-align:center;border:1px solid #000000; border-bottom-style:none; border-right-style:none;}
*/

.nestTable {
width:100%;
border-collapse:collapse;
font-family:"宋体";
font-size:12px;
clear:both;
}
.nestTable td{
border:1px solid #cccccc;
padding:0px 0px;
height:22px;
text-align:center;
}

#cwsNestTable td {
	height:22px;
}

#divBtn {
	text-align:right;
	margin-bottom:5px;
}

#tableOperate {
	margin-top:5px;
}
</style>
<script>
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

var canDrag = true;

// 表格能否被编辑
var canTdEditable = true;
<%
if (op.equals("show")) {
	%>
	canTdEditable = false;
	<%
}
%>

function init(){
	cur_row		= null;
	cur_col		= null;
	cur_cell	= null;
	sort_col	= null;
	Main_Tab 	= document.getElementById("cwsNestTable");
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

$(function(){
	// dragDiv = document.createElement("DIV");
	var dragDiv = document.getElementById("dragDiv");
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
});

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

<%
String pageType = ParamUtil.get(request, "pageType");
if (!op.equals("show")) {
	String formName = "visualForm";
	if (pageType.equals("flow"))
		formName = "flowForm";
%>

// i从1开始
function getCellValue(i, j) {
	var obj = document.getElementById("cwsNestTable");	
	var cel = obj.rows.item(i).cells;

	var fieldType = Main_Tab.rows[0].cells[j].getAttribute("type");
	var macroType = Main_Tab.rows[0].cells[j].getAttribute("macroType");

	// 标值控件
	if (macroType=="macro_raty") {
		if(cel[j].children[0] && cel[j].children[0].tagName=="SPAN") {
			var ch = cel[j].children[0].children;
			for (var k=0; k<ch.length; k++) {
				if (ch[k].tagName=="INPUT") {
					return ch[k].value;
				}
			}
		}
	}
	// 在clear_color时，会置宏控件所在单元格的value属性为控件的值
	else if (cel[j].getAttribute("value")) {
		return cel[j].getAttribute("value");
	}
	else {
		if (cel[j].children.length>0) {
			var cellDiv = cel[j].children[0];
			return cellDiv.innerText.trim();
		}
		else {
			return cel[j].innerText.trim();
		}
	}
	return "";
}

// 置单元格的值
function setCellValue(i, j, value) {
	var obj = document.getElementById("cwsNestTable");
	var cel = obj.rows.item(i).cells;

	var fieldType = Main_Tab.rows[0].cells[j].getAttribute("type");
	var macroType = Main_Tab.rows[0].cells[j].getAttribute("macroType");

	// 在clear_color时，会置宏控件所在单元格的value属性为控件的值
	if (cel[j].getAttribute("value")!=null) {
		cel[j].setAttribute("value", value);
	}
	else {
		if (cel[j].children.length>0) {
			var cellDiv = cel[j].children[0];
			cellDiv.innerText = value
		}
		else {
			cel[j].innerText = value;
		}
	}
}

function AddElement(name, value, type){ 
	var f = document.getElementById("<%=formName%>"); 
	var newInput = document.createElement("input");  
	newInput.type = type;  
	newInput.name = name;
	newInput.value = value;
	f.appendChild(newInput);  
}

if (document.<%=formName%>) {
	// 如果不用jquery，而用onsubmit可能会被livevalidation覆盖
	$('#<%=formName%>').submit(function(){
		// 删除之前在保存草稿时生成的隐藏域控件
		$("[name^=cws_cell_]").remove();
		 
		var obj = document.getElementById("cwsNestTable");
		var rows = obj.rows.length;
		for (var i=1; i<rows; i++) {
			var cel = obj.rows.item(i).cells;
			for (var j=0; j<cel.length; j++) {
				// str += i + "," + j + "<input type='' name='cws_cell_" + (i-1) + "_" + j + "' value='" + getCellValue(i, j) + "' /><BR>";
				AddElement("cws_cell_" + (i-1) + "_" + j, getCellValue(i, j), "hidden");
			}
		}
		
		AddElement("cws_cell_rows", rows-1, "hidden");
		/*
		// ie11下面，不能再用下面的方法
		var str = "";
		for (var i=1; i<rows; i++) {
			var cel = obj.rows.item(i).cells;
			for (var j=0; j<cel.length; j++) {
				str += i + "," + j + "<input type='' name='cws_cell_" + (i-1) + "_" + j + "' value='" + getCellValue(i, j) + "' /><BR>";
			}
		}
		str += "<input name='cws_cell_rows' type='hidden' value='" + (rows-1) + "' />";
		if (o("cwsNestTableHelper")) {
			o("cwsNestTableHelper").innerHTML = str;
			// $('#cwsNestTableHelper').show();
			// alert(o("cwsNestTableHelper").innerHTML);
		}
		*/
	  	return true;
	});

	/*
	document.<%=formName%>.onsubmit=function(){
		var obj = document.getElementById("cwsNestTable");
		var rows = obj.rows.length;
		var str = "";
		
		for (var i=1; i<rows; i++) {
			var cel = obj.rows.item(i).cells;
			for (var j=0; j<cel.length; j++) {
				str += i + "," + j + "<input type='' name='cws_cell_" + (i-1) + "_" + j + "' value='" + getCellValue(i, j) + "' /><BR>";
			}
		}
		str += "<input name='cws_cell_rows' type='hidden' value='" + (rows-1) + "' />";
		if (o("cwsNestTableHelper"))
			o("cwsNestTableHelper").innerHTML = str;
		
		return true;
	}
	*/
}
<%}%>

var errFunc = function(response) {
    alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}

function doCheckField(response) {
	var data = response.responseText;
	data = $.parseJSON(data);
	if (data.ret=="0") {
		alert(data.msg);
	}
}

function getCellIndex(td){   
    if (isIE()){   
        var cells=td.parentNode.cells;   
        for (var i=0,j=cells.length;i<j;i++ ){
            if (cells[i]===td){
                return i;   
            }
        }   
    }
    return td.cellIndex;
}

function getCellIndexByFieldName(fieldName) {
	for(var i=0;i<Main_Tab.rows[0].cells.length;i++){		
		if (Main_Tab.rows[0].cells[i].getAttribute("fieldName")==fieldName)
			return i;
	}
	return -1;
}

function clear_color(){
	var oBgc = "";
	var oFc = "";
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

	if(cur_cell!=null) {
		cur_col = cur_cell.cellIndex;

		var fieldType;
		var macroType;
		var fieldName;
		
		<%if (op.equals("add")) {%>
		fieldType = Main_Tab.rows[0].cells[cur_col].getAttribute("type");
		macroType = Main_Tab.rows[0].cells[cur_col].getAttribute("macroType");
		fieldName = Main_Tab.rows[0].cells[cur_col].getAttribute("fieldName");
		<%}else{%>
		var colNum = getCellIndex(cur_cell);
		
		fieldType = Main_Tab.rows[0].cells[colNum].getAttribute("type");
		macroType = Main_Tab.rows[0].cells[colNum].getAttribute("macroType");
		fieldName = Main_Tab.rows[0].cells[colNum].getAttribute("fieldName");
		<%}%>

		if (fieldType=="<%=FormField.TYPE_DATE%>" || fieldType=="<%=FormField.TYPE_DATE_TIME%>") {
			if (cur_cell.children[0].tagName=="INPUT" && typeof(_dynarch_popupCalendar) != 'undefined') {
				if (_dynarch_popupCalendar==null || (_dynarch_popupCalendar!=null && _dynarch_popupCalendar.hidden)) {
					cur_cell.children[0].outerHTML = "<div>" + cur_cell.children[0].value + "</div>";
				}
			} else {
				if (typeof($('#id_' + cur_row + '_' + cur_col).val()) != 'undefined' && $('#id_' + cur_row + '_' + cur_col).val() != '') {
					cur_cell.children[0].outerHTML = "<div>" + $('#id_' + cur_row + '_' + cur_col).val() + "</div>";
				} else if (typeof(cur_cell.children[0].value) != 'undefined') {
					cur_cell.children[0].outerHTML = "<div>" + cur_cell.children[0].value + "</div>";
				}
			}
		}
		else if (fieldType=="<%=FormField.TYPE_MACRO%>") {
			// 标值控件，这里单元格失去焦点后，cur_cell为null，所以运行不到此处
			if (macroType=="macro_raty") {
				if(cur_cell.children[0].tagName=="SPAN") {
					var ch = cur_cell.children[0].children;
					for (var i=0; i<ch.length; i++) {
						if (ch[i].tagName=="INPUT") {
							cur_cell.setAttribute("value", ch[i].value);
							break;
						}
					}
				}
			}
			else {
				// alert(cur_cell.children[0].tagName);
				if(cur_cell.children[0].tagName=="INPUT") {
					// ProductListWinCtl
					cur_cell.setAttribute("value", cur_cell.children[1].value);
					cur_cell.innerHTML = "<div>" + cur_cell.children[0].value + "</div>";
				}
				else if (cur_cell.children[0].tagName=="SELECT") {
					// BasicSelectCtl
					cur_cell.setAttribute("value", cur_cell.children[0].value);
					// alert("here  " + cur_cell.children[0].value + " " + cur_cell.getAttribute("value"));
					cur_cell.innerHTML = "<div>" + cur_cell.children[0].options(cur_cell.children[0].selectedIndex).text + "</div>";
				}
			}
		}
		if (typeof cur_cell.children[0] != 'undefined') {
			cur_cell.children[0].contentEditable = false;
			with(cur_cell.children[0].runtimeStyle){
				borderLeft=borderTop="";
				borderRight=borderBottom="";
				backgroundColor="";
				paddingLeft="";
				textAlign="";
			}
		}
		
		<%if (op.equals("add")) {%>
		callCalculate(); // fieldName, cur_col);
		<%}else{%>
		var colNum = getCellIndex(cur_cell);	
		callCalculate(); // fieldName, colNum);
		<%}%>
		
		// 类型检测
		var cellValue = "";
		<%if (op.equals("add")) {%>
		cellValue = getCellValue(cur_row, cur_col);
		<%}else{%>
		var colNum = getCellIndex(cur_cell);		
		cellValue = getCellValue(cur_row, colNum);
		// 当日期处于编辑状态时，直接用getCellValue取值为空，需用下面的方式
		if (fieldType=="<%=FormField.TYPE_DATE%>" || fieldType=="<%=FormField.TYPE_DATE_TIME%>") {
			if(cur_cell.children[0].tagName=="INPUT" && typeof(_dynarch_popupCalendar) != 'undefined') {
				cellValue = cur_cell.children[0].value;
			} else {
				if (typeof($('#id_' + cur_row + '_' + cur_col).val()) != 'undefined' && $('#id_' + cur_row + '_' + cur_col).val() != '') {
					cellValue = $('#id_' + cur_row + '_' + cur_col).val();
				} else {
					if (typeof(cur_cell.children[0].val()) != 'undefined' && cur_cell.children[0].val() != '') {
						cellValue = cur_cell.children[0].val();
					}
				}
			}
		}		
		<%}%>

		if (cellValue == '') {
			cellValue = cur_cell.innerText;
		}
				
		var str = "op=check&formCode=<%=formCode%>&" + fieldName + "=" + cellValue + "&field=" + fieldName;
		var myAjax = new cwAjax.Request(
			"<%=Global.getRootPath(request)%>/visual/nest_table_ajax_check.jsp",
			{
				method:"post",
				parameters:str,
				onComplete:doCheckField,
				onError:errFunc
			}
		);		
	}
	
	cur_row  = null;
	cur_col  = null;	
}

function callCalculate() { // fieldName, col) {
	$("td[type='CALCULATOR']").each(function(){
		// alert($(this).attr("formula"));
		// var col = $(this).attr("cellIndex");
		var col = getCellIndex($(this)[0]);

		var obj = document.getElementById("cwsNestTable");
		var rows = obj.rows.length;
		for (var i=1; i<rows; i++) {
			// getCalculateCellValue(i, $(this).attr("formula"));
			// alert(getCalculateCellValue(i, $(this).attr("formula")));
			obj.rows[i].cells[col].innerText = getCalculateCellValue(i, $(this).attr("formula"));
		}
	});
	
	// 更新对嵌套表的列进行sum操作的控件
 	$("input[kind='CALCULATOR']").each(function(){
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
               // alert(mactches[0]);
               var field = RegExp.$2;
               if (field.indexOf("nest.")==0) {
               		var p = field.indexOf(".");
					// 取得列名
                    field = field.substring(p+1);
                    // 当数据列有更新时，更新计算控件的值
					// if (field==fieldName) {
						// 计算
						// alert(getCellValue());
						var obj = document.getElementById("cwsNestTable");
						var rows = obj.rows.length;
						var v = 0;
						var colIndex = getCellIndexByFieldName(field);		
						if (colIndex==-1) {
							// alert(formula + "中的字段" + field + "不存在！");
							return;
						}
						// alert(colIndex + " " + field);
						for (var i=1; i<rows; i++) {
							var cellV = getCellValue(i, colIndex).trim();
							if (cellV!="") {
								if (!isNaN(cellV))
									v += eval(cellV);
							}
						}
						$(this).val(v);
					// }
               }
           }
		}
	});
}

function getCalculateCellValue(row, formula) {            
    // alert(formula);
    var ary = getSymbolsWithBracket(formula);
    for (var i=0; i<ary.length; i++) {
        if (!isOperator(ary[i])) {
            // var col = $("td[fieldName='" + ary[i] + "']").attr("cellIndex");
			
			var col = getCellIndex($("td[fieldName='" + ary[i] + "']")[0]);		

			var v = getCellValue(row, col);
			// alert(ary[i] + " col=" + col + " v=" + v);
			// alert(ary[i] + "=" + v + " col=" + col);
            if (v=="")
                ary[i] = 0;
            else if (isNaN(v))
                ary[i] = 0;
            else
                ary[i] = v;
        }
    }
    formula = "";
    for (var i=0; i<ary.length; i++) {
        formula += ary[i];
    }
	// 需四舍五入，否则数字看起来会比较乱，如出现：60*21.76=1305.6000000000001的情况
    // return eval(formula);
    return roundNum(eval(formula), 2);
}

function roundNum(num,v) {
    var vv = Math.pow(10,v);
    return Math.round(num*vv)/vv;
} 

function docOnClick() {
	window.status = "";

	clear_color();
	cur_row  = null;
	cur_col  = null;

	try {
		if (_dynarch_popupCalendar==null)
			cur_cell = null;
		else {
			if (_dynarch_popupCalendar.hidden) {
				cur_cell = null;
			}
		}
	} catch (e) {
	}
}

document.onclick = docOnClick;

function read_def(the_table){
	if (!the_table.rows)
		return;
	for(var i=0;i<the_table.rows.length;i++){
		for(var j=0;j<the_table.rows[i].cells.length;j++){
			with(the_table.rows[i]){
				cells[j].id = "id_" + i + "_" + j;
				if (typeof(_dynarch_popupCalendar) == 'undefined') {
					var fieldType = the_table.rows[0].cells[j].getAttribute("type");
					if (fieldType=="<%=FormField.TYPE_DATE%>" || fieldType=="<%=FormField.TYPE_DATE_TIME%>") {
						$('#' + cells[j].id).datetimepicker({
							lang:'ch',
							timepicker:false,
					     	format:'Y-m-d',
					     	onShow:function() {
				     			return false;
					     	}
						});
					}
				}
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
	document.onmousemove = function(){
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
	var dragDiv = document.getElementById("dragDiv");
	dragDiv.style.display="none";
	dragDiv.innerHTML = "";
	dragDiv.style.width = 0;
	dragDiv.style.height = 0;
	for(var i=0;i<Main_Tab.rows[0].cells.length;i++){
		if (Main_Tab.rows[0].cells[i].runtimeStyle)
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

document.onmouseup = drag_end;

// 当被编辑的单元格失去焦点时调用
function onCurCellBlur() {
	// 当前被编辑的单元格的值
	var cellValue = getCellValue(cur_row, cur_col);
	// 当前被编辑的字段的类型
	var fieldType = Main_Tab.rows[0].cells[cur_col].getAttribute("type");
	// 当前被编辑的单元格的宏控件的编码
	var macroType = Main_Tab.rows[0].cells[cur_col].getAttribute("macroType");
	// 当前被编辑的单元格的字段的编码
	var fieldName = Main_Tab.rows[0].cells[cur_col].getAttribute("fieldName");
}

function clickIt(){
	if (isIE())
		event.cancelBubble=true;
	else
		event.stopPropagation();

	var the_obj = event.srcElement;
	var i = 0, j = 0;
	
	var eventTd = get_Element(the_obj, "td");
	if (cur_cell==eventTd) {
		// 如果是正在编辑的单元格，则退出
		// window.status = "yes";
		return;
	}

	// 将正在编辑的单元格置为不可编辑状态
	if(cur_cell!=null && cur_row!=0 && typeof cur_cell.children[0] != 'undefined'){
		try {
			cur_cell.children[0].contentEditable = false;
			with(cur_cell.children[0].runtimeStyle){
				borderLeft=borderTop="";
				borderRight=borderBottom="";
				backgroundColor="";
				paddingLeft="";
				textAlign="";
			}
		}
		catch (e) {
			alert(e);
			alert(cur_cell.children[0].tagName);
		}
		// 调用当单元格编辑失去焦点的方法
		try {
			onCurCellBlur();
		}
		catch (e) {
			// 如果元素没有attribute时会报异常，故屏蔽掉
			// alert(e);
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
		
		cur_col = getCellIndex(the_td);		
		
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
			
			cur_col = the_td.cellIndex;

			if(the_td.children.length!=1)
				the_td.innerHTML="<div>" + the_td.innerHTML.trim() + "</div>";
			else if(the_td.children.length==1 && the_td.children[0].tagName.toLowerCase()!="div")
				the_td.innerHTML="<div>" + the_td.innerHTML.trim() + "</div>";
			
			var fieldType;
			var macroType;
			var fieldName;
			
			<%
				// 在流程处理页面中，双击单元格cur_col值会比正常小1，且列在高亮显示时的列的头部与下面的列错开了，如鼠标在第二列头部时，高亮的列却是第一列
				// 原来是因为visual_edit.jsp页面中的兼容性标签应放在包含该文件的页面的CSS的link标签之前
				// <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
				// 如果没有这个兼容性标签，则表头不能被拖动，并且看起来cellspacing似乎变为了0
				// 因此在高亮显示的时候，对列号做了加1的处理
			%>

			<%if (op.equals("add")) {%>
			fieldType = Main_Tab.rows[0].cells[cur_col].getAttribute("type");
			macroType = Main_Tab.rows[0].cells[cur_col].getAttribute("macroType");
			fieldName = Main_Tab.rows[0].cells[cur_col].getAttribute("fieldName");
			<%}else{%>
			var colNum = getCellIndex(the_td);
			fieldType = Main_Tab.rows[0].cells[colNum].getAttribute("type");
			macroType = Main_Tab.rows[0].cells[colNum].getAttribute("macroType");
			fieldName = Main_Tab.rows[0].cells[colNum].getAttribute("fieldName");
			
			<%if (pageType.equals("flow")) {%>
				if (!writableColMap.containsKey(fieldName))
					return;
			<%}%>
			<%}%>

			if (fieldType=="<%=FormField.TYPE_DATE%>" || fieldType=="<%=FormField.TYPE_DATE_TIME%>") {
				cur_cell.children[0].id = "fieldObj" + objId;
				cur_cell.children[0].outerHTML = "<input id='" + cur_cell.children[0].id + "' value='" + cur_cell.children[0].innerText + "' />";
				
				cur_cell.children[0].focus();

				if (typeof(_dynarch_popupCalendar) != 'undefined') {
					showCalendar(cur_cell.children[0].id, '%Y-%m-%d', '24', true);
				} else {
					$('#' + cur_cell.children[0].id).datetimepicker({
						lang:'ch',
						timepicker:false,
				     	format:'Y-m-d',
						onShow:function() {
		     				return true;
			     		}
		     		});
				}
				objId++;
			}
			else if (fieldType=="<%=FormField.TYPE_MACRO%>") {
				$.ajax({
					type: "post",
					url: "<%=request.getContextPath()%>/visual/ajax_nesttable_cell_dbclick.jsp",
					data : {
						macroType : macroType,
						objId : "fieldObj" + objId,
						oldShowValue : cur_cell.children[0].innerText,
						oldValue : cur_cell.getAttribute("value")?cur_cell.getAttribute("value"):"",
						formCode : "<%=formCode%>",
						fieldName : fieldName
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						//ShowLoading();
					},
					success: function(data, status){						
						data = $.parseJSON(data);
						
						// data中可能含有javascript，如标值控件
						var reg = /<script[^>]*>((.|\n)+)<\/script>/i;

						var html = data.html.replace(reg,"");//获取不包含脚本的其他内容
						cur_cell.children[0].outerHTML = html;
						
						var match=data.html.match(reg);
						if(match!=null) {
							var myScript=match[1];
							eval(myScript);
						}
					},
					complete: function(XMLHttpRequest, status){
						//HideLoading();
					},
					error: function(){
						//请求出错处理
					}
				});				
				objId++;
			}
			else {
				cur_cell.children[0].contentEditable = true;
				cur_cell.children[0].focus();
			}
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
	// var the_obj = event.srcElement;
	var the_obj = event.srcElement ? event.srcElement : event.target;
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
				var cIndex = getCellIndex(the_td);		
				
				try {
					with(the_table.rows[i].cells(cIndex)){
						runtimeStyle.backgroundColor=act_bgc;
						runtimeStyle.color=act_fc;
					}
				}
				catch (e) {}
			}
			if(the_td.mode==undefined)the_td.mode = false;
			//the_td.style.cursor=the_td.mode?"n-resize":"s-resize";
		}
	}
}

function outIt(){
	// var the_obj = event.srcElement;
	var the_obj = event.srcElement ? event.srcElement : event.target;
	
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
				// 加1是为了调整因ID列display:none后，导致的列的计算误差
				var cIndex = getCellIndex(the_td);		
				
				try {
					with(the_table.rows[i].cells(cIndex)){
						runtimeStyle.backgroundColor='';
						runtimeStyle.color='';
					}
				} catch (e) {}				
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
	
	var row = the_table.rows[cur_row];
	if (row.getAttribute("editable")) {
		if (row.getAttribute("editable")=="0") {
			alert("该行不能被编辑");
			return;
		}
	}
	
	if(cur_row==null || cur_row<=1)return;
	
	change_row(the_table,cur_row,--cur_row);
}

function Move_down(the_table){
	event.cancelBubble=true;
	
	var row = the_table.rows[cur_row];
	if (row.getAttribute("editable")) {
		if (row.getAttribute("editable")=="0") {
			alert("该行不能被编辑");
			return;
		}
	}
	
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
	if (event)
		event.cancelBubble=true;
	var the_row,the_cell;
	the_row = cur_row==null?-1:(cur_row+1);
	clear_color();
	var newrow=the_table.insertRow(the_row);
	for (var i=0;i<the_table.rows[0].cells.length;i++) {
		the_cell=newrow.insertCell(i);
		the_cell.innerText = " ";
		// the_cell.innerText="NewRow_" + the_cell.parentElement.rowIndex;
		if (i==0) {
		<%if (op.equals("edit")) {%>
		the_cell.style.display = "none";
		the_cell.setAttribute("editable", "0");
		<%}%>
		}
		the_cell.id = "id_" + newrow.rowIndex + "_" + i;
		
		// 隐藏字段
		var fieldName = Main_Tab.rows[0].cells[i].getAttribute("fieldName");
		if (hideColMap.containsKey(fieldName))
			the_cell.style.display = "none";

		if (typeof(_dynarch_popupCalendar) == 'undefined') {
			fieldType = Main_Tab.rows[0].cells[i].getAttribute("type");
			if (fieldType=="<%=FormField.TYPE_DATE%>" || fieldType=="<%=FormField.TYPE_DATE_TIME%>") {
				$('#' + the_cell.id).datetimepicker({
					lang:'ch',
					timepicker:false,
			     	format:'Y-m-d',
			     	onShow:function() {
		     			return false;
			     	}
				});
			}
		}
		
	}
	read_def(the_table);
	
	cur_row = newrow.rowIndex;

	return newrow;
}

function del_row(the_table) {
	if(the_table.rows.length==1) return;
	var the_row;
	the_row = (cur_row==null || cur_row==0)?-1:cur_row;
	
	var row;
	if (the_row==-1)
		row = the_table.rows[the_table.rows.length - 1];
	else
		row = the_table.rows[the_row];
	if (row.getAttribute("editable")) {
		if (row.getAttribute("editable")=="0") {
			alert("该行不能被编辑");
			return;
		}
	}
	
	the_table.deleteRow(the_row);
	cur_row = null;
	cur_cell = null;
	
	callCalculate();
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
	if (!canDrag)
		return;

	if(!obj.mouseDownX)
		return false;   
	var newWidth=obj.pareneTdW*1+event.clientX*1-obj.mouseDownX;   
	if(newWidth>0){
		obj.parentElement.style.width = newWidth + "px";   
		document.getElementById("cwsNestTable").style.width = (obj.pareneTableW*1+event.clientX*1-obj.mouseDownX) + "px";
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
	
// 20131123 fgf 添加nestFieldName，因为其中存储了"选择"按钮需要的配置信息
String parentFormCode = ParamUtil.get(request, "parentFormCode");
String nestFieldName = ParamUtil.get(request, "nestFieldName");
JSONObject json = null;
JSONArray mapAry = new JSONArray();
if (!nestFieldName.equals("")) {
	FormDb parentFd = new FormDb();
	parentFd = parentFd.getFormDb(parentFormCode);
	// System.out.println(getClass() + " parentFormCode=" + parentFormCode + " nestFieldName=" + nestFieldName);
	FormField nestField = parentFd.getFormField(nestFieldName);
	if (nestField!=null) {
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(nestField.getDescription());		
			json = new JSONObject(defaultVal);
			mapAry = (JSONArray)json.get("maps");	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
		}
	}
	else {
		System.out.println(getClass() + " nestField=" + nestFieldName + " is not found in FormDb");
	}
}	
	
boolean isFirstRowBlank = false;
if (op.equals("edit") || op.equals("show")) {	
	String cwsId = ParamUtil.get(request, "cwsId");
	String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(cwsId);
	sql += " order by cws_order";
	
	// out.print(sql);
	
	FormDAO fdao = new FormDAO();
	Vector vt = fdao.list(formCode, sql);
	Iterator ir = vt.iterator();
	%>
	<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="nestTable" id="cwsNestTable" style="table-layout:fixed;">
      <tr class="cwsThead">
	  <%//if (!op.equals("show")) {%>
        <td style="display:none">
		<div class="resizeTd" onmousedown="MouseDownToResize(this);" onmousemove="MouseMoveToResize(this);" onmouseup="MouseUpToResize(this);"></div>		
		ID
		</td>
	  <%//}%>
    <%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String title = "创建者";
		if (!fieldName.equals("cws_creator"))
			title = fd.getFieldTitle(fieldName);

		FormField ff = fd.getFormField(fieldName);
		String macroType = "";
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			macroType = ff.getMacroType();
		}
		
		String formula = "";
		if (ff.getType().equals(FormField.TYPE_CALCULATOR))
			formula = "formula=\"" + ff.getDefaultValueRaw() + "\"";
	%>
        <td fieldName="<%=fieldName%>" width="<%=fieldsWidth[i]%>" type="<%=ff.getType()%>" macroType="<%=macroType%>" <%=formula%>>
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
      <tr>
	  <%//if (!op.equals("show")) {%>	  
        <td editable=0 style="display:none"><%=fdao.getId()%></td>
	  <%//}%>
        <%
		MacroCtlMgr mam = new MacroCtlMgr();
		
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
			
			FormField ff = fd.getFormField(fieldName);
			String attrValue = "";
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				attrValue = fdao.getFieldValue(fieldName);
			}
		%>
        <td align="center" <%=ff.getType().equals(FormField.TYPE_MACRO)?"value='" + attrValue + "'":""%>>
			<%
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mcu = mam.getMacroCtlUnit(ff.getMacroType());
				IFormMacroCtl imc = mcu.getIFormMacroCtl();
				out.print(imc.converToHtml(request, ff, attrValue));
			}
			else if (!fieldName.equals("cws_creator")) {%>
            <%=fdao.getFieldValue(fieldName)%>
            <%}else{%>
            <%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>
            <%}%>
        </td>
        <%}%>
      </tr>
    <%}%>
	<%
	if (vt.size()==0) {
		isFirstRowBlank = true;
	%>
      <tr>
        <td editable=0 style="display:none"></td>
        <%
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
			
			FormField ff = fd.getFormField(fieldName);
			String attrValue = "";
		%>
        <td align="center" <%=ff.getType().equals(FormField.TYPE_MACRO)?"value='" + attrValue + "'":""%>></td>
		<%}%>
	  </tr>	
	<%
	}
	%>
    </table>
	<%if (op.equals("edit")) {%>
	<div id="tableOperate" style="text-align:right">
			<input class="btn" id="addBtn" type="button" onclick="add_row(Main_Tab)" value="增加" />
			<input class="btn" type="button" onclick="del_row(Main_Tab)" value="删除" />
			<input class="btn" type="button" onclick="importExcel(Main_Tab)" value="导入" />
			<input class="btn" type="button" onclick="openWin('<%=request.getContextPath()%>/visual/module_excel_relate.jsp?parentId=<%=cwsId%>&formCode=<%=ParamUtil.get(request, "parentFormCode")%>&formCodeRelated=<%=formCode%>', 480, 80);" value="导出" />
			<input class="btn" type=button id=move value=上移 onclick=Move_up(Main_Tab)>
			<input class="btn" type=button id=move value=下移 onclick=Move_down(Main_Tab)>
			<%
            if (mapAry.length()>0 && pageType.equals("flow")) {%>
            <input class="btn" type="button" onclick="sel_<%=formCode%>(<%=cwsId%>)" value="选择" />
            <%}%>            
			&nbsp;&nbsp;&nbsp;&nbsp;
    <div id="cwsNestTableHelper" style="display:none"></div>
			<div id=detail style="display:none">
			  <input name="text" type="text" id="text" style="width:200px" size="30" />
			  <textarea name="textarea" cols="100" rows="20" id="textarea"></textarea>
		  </div>
    </div>
	<%}
	String action = ParamUtil.get(request, "action");
	if (op.equals("show") && !action.equals("archive")) {
	%>
	<div id="divExport" style="padding-top:5px"><a href="javascript:;" onclick="openWin('<%=request.getContextPath()%>/visual/module_excel_relate.jsp?parentId=<%=cwsId%>&formCode=<%=ParamUtil.get(request, "parentFormCode")%>&formCodeRelated=<%=formCode%>', 480, 80);" title="导出嵌套明细表中的内容">[导出]</a></div>
	<%
	}
	%>
<%}else{
		// 用于visual_add.jsp
        com.redmoon.oa.visual.Config cfg = new com.redmoon.oa.visual.Config();
		boolean isView = true;
		if (!parentFormCode.equals("")) {
			com.redmoon.oa.visual.IModuleChecker imc = cfg.getIModuleChecker(parentFormCode);
			if (imc!=null) {
				if (imc.onNestTableCtlAdd(request, response, out))
					isView = false;
			}
		}
		if (isView) {
		%>
		<table align="center" width="98%" border="0" cellspacing="1" cellpadding="2" id="cwsNestTable" class="percent98" style="table-layout:fixed;">
			  <tr align="middle" class="cwsThead">
				<%
                for (int i=0; i<len; i++) {
                    String fieldName = fields[i];
                    String title = "创建者";
                    if (!fieldName.equals("cws_creator"))
                        title = fd.getFieldTitle(fieldName);
            
                    FormField ff = fd.getFormField(fieldName);
                    String macroType = "";
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        macroType = ff.getMacroType();
                    }
					String formula = "";
					if (ff.getType().equals(FormField.TYPE_CALCULATOR))
						formula = "formula=\"" + ff.getDefaultValueRaw() + "\"";
                %>
                    <td fieldName="<%=fieldName%>" width="<%=fieldsWidth[i]%>" type="<%=ff.getType()%>" macroType="<%=macroType%>" <%=formula%>>
                    <div class="resizeTd" onmousedown="MouseDownToResize(this);" onmousemove="MouseMoveToResize(this);" onmouseup="MouseUpToResize(this);"></div>
                    <%=title%>
                    </td>
                <%}%>
			  </tr>
		</table>
		<%}%>
		<div id="tableOperate" style="text-align:right">
		<input id="addBtn" class="btn" type=button value=增加 onclick=add_row(Main_Tab)>
		<input class="btn" name="button" type=button onClick=del_row(Main_Tab) value=删除>
		<input class="btn" type="button" onclick="importExcel()" value="导入" />
		<input class="btn" type=button id=move value=上移 onclick=Move_up(Main_Tab)>
		<input class="btn" type=button id=move value=下移 onclick=Move_down(Main_Tab)>
&nbsp;&nbsp;&nbsp;&nbsp;		<!--
		<input type=button value=插入列 onclick=add_col(Main_Tab)>
		<input type=button value=删除列 onclick=del_col(Main_Tab)>
		<input type=button value=Restore onclick=res_tab(Main_Tab)>
		<input type=button value=Export  onclick=exp_tab(Main_Tab)>
		<input type=button id=move value=Left  onclick=Move_left(Main_Tab)>
		<input type=button id=move value=Right onclick=Move_right(Main_Tab)>
		-->
		<div id="cwsNestTableHelper" style="display:none"></div>
		<div id=detail style="display:none">
		<input type=text id="cwsNestTableMonitor" size=30 style="width:200px">
		<textarea id="cwsNestTableShowContent" cols=100 rows=20></textarea>
		</div>
		</div>
<%}%>
<script>
init();

$(document).ready(function(){
	if (!canTdEditable) {
		// show状态时，无tableOperate
		if (o("tableOperate"))
			o("tableOperate").style.display = "none";
		if (o("divBtn")) {
			if (o("divExport")) {
				$("#divExport").hide();
				o("divBtn").innerHTML = o("divExport").innerHTML;
			}
		}
	}
	else {
		if (o("divBtn")) {
			try {
			o("divBtn").innerHTML = o("tableOperate").innerHTML;
			}
			catch (e) {}
			try {
			o("tableOperate").outerHTML = "";
			}
			catch (e) {}
		}
	}
	
	<%
    // 主表中嵌套表字段不可写，则不允许添加、删除行，只可以根据权限编辑	
	boolean isNestFormFieldEditable = ParamUtil.get(request, "isNestFormFieldEditable").equals("true");
	if (!isNestFormFieldEditable) {
	%>
		if (o("tableOperate"))
			o("tableOperate").style.display = "none";
		if (o("divBtn"))
			o("divBtn").style.display = "none";
	<%}%>

	// 禁止后退键 作用于Firefox、Opera   
	document.onkeypress=banBackSpace;   
	// 禁止后退键 作用于IE、Chrome   
	document.onkeydown=banBackSpace;
	
	callCalculate();
	
	// 以下方式无效
	// $(document).keypress(...);
	
});


// 处理键盘事件 禁止后退键（Backspace）密码或单行、多行文本框除外	 
function banBackSpace(e){
	var ev = e || window.event;//获取event对象      
	var obj = ev.target || ev.srcElement;//获取事件源
	   
	var t = obj.type || obj.getAttribute('type');//获取事件源类型     
	   
	// 获取作为判断条件的事件类型   f
	var vReadOnly = obj.getAttribute('readonly');   
	var vEnabled = obj.getAttribute('enabled');   
	// 处理null值情况   
	vReadOnly = (vReadOnly == null) ? false : vReadOnly;   
	vEnabled = (vEnabled == null) ? true : vEnabled;   
	   
	if (obj.tagName=="TD" || obj.tagName=="BODY") {
		return false;
	}
	
	if (obj.tagName=="DIV") {
		if (""+obj.getAttribute("contentEditable")=="true") {
			if (ev.keyCode == 8) {
				if (document.selection.createRange().text!="") {
					// 如果全选，必须加以处理，否则backspace键会使得页面后退
					if (obj.innerText==document.selection.createRange().text) {
						// 删除全部文字
						obj.innerText = "";

						// focus不能使光标出现在编辑框中
						// obj.focus();
						
						// 使光标出现在编辑框中
						sel = document.selection.createRange();
						sel.moveStart('character', 0);
						sel.select();
						
						return false;
					}
					else
						return true;
				}
				else
					return true;
			}
		}
		else {
			return false;
		}
	}
		
	// alert("obj=" + obj.tagName + " t=" + t + " " + vReadOnly + " " + vEnabled + " contentEditable=" + obj.getAttribute("contentEditable"));
	   
	// 当敲Backspace键时，事件源类型为密码或单行、多行文本的，   
	// 并且readonly属性为true或enabled属性为false的，则退格键失效   
	var flag1=(ev.keyCode == 8 && (t=="password" || t=="text" || t=="textarea")    
				&& (vReadOnly==true || vEnabled!=true))?true:false;   
	  
	// 当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效   
	var flag2=(ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea")   
				?true:false;           
	   
	// 判断
	if(flag2){   
		return false;
	}
	if(flag1){      
		return false;      
	}
	
}

function importExcel() {
	openWin("<%=request.getContextPath()%>/visual/nest_table_import_excel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>", 480, 80);
}

var isFirstRowBlank = <%=isFirstRowBlank%>;

function doImportExcel(aryStr) {
	var ary = $.parseJSON(aryStr);
	var k = 0;
	for (var key in ary) {
		var rowData = ary[key];
		// 插入空行
		if (k==0 && !isFirstRowBlank)
			add_row(Main_Tab);
		else if (k>0)
			add_row(Main_Tab);
		
		// 遍历表头，获取字段的值
		$("#cwsNestTable tr:first").children().each(function(k) {
			var fName = $(this).attr("fieldName")
			if (!$(this)[0].getAttribute("fieldName")) {
				// 不能加ID，加了在编辑时就会被认为是之前已保存的数据
				// $("#cwsNestTable tr:last").children("td:eq(0)").html(rowData.id); // ID列
			}
			else {
				var c = $("#cwsNestTable tr:last").children("td:eq(" + k + ")");
				c.html(eval("rowData." + fName));
				if ($(this).attr("type")=="<%=FormField.TYPE_MACRO%>") {
					c.attr("value", eval("rowData." + fName));
				}
			}
		});

		// 插入操作后，第一行的数据不可能再为空
		isFirstRowBlank = false;
		
		k++;
	}
	
	callCalculate();
}

var hideColMap = new Map();

function hideNestTableCol(fieldName) {
    var rowsLen = Main_Tab.rows.length;
	var index = getCellIndexByFieldName(fieldName);
	
	hideColMap.put(fieldName, index);
	
	// 用此方法虽然效率高，但是隐藏列位置会出现一条粗线
	// Main_Tab.rows[0].cells[index].style.width = "0px";
	// Main_Tab.tableLayout = "fixed";

	for (var i=0; i<rowsLen; i++) {
		Main_Tab.rows[i].cells[index].style.display = "none";
		// alert(Main_Tab.rows[i].cells[index].innerHTML);
		/*
		Main_Tab.rows[i].cells[index].style.border = "0px";
		Main_Tab.rows[i].cells[index].style.color = "#ffffff";
		// Main_Tab.rows[i].cells[index].style.backgroundColor = "#cccccc";
		Main_Tab.rows[i].cells[index].style.padding = "0px";
		Main_Tab.rows[i].cells[index].style.overflow = "hidden";
		Main_Tab.rows[i].cells[index].style.whiteSpace = "nowrap";
		Main_Tab.rows[i].cells[index].style.fontSize = "0px";
		Main_Tab.rows[i].cells[index].cellpadding = 0;
		Main_Tab.rows[i].cells[index].cellspacing = 0;
		*/
    }

	// 防止列宽被改变
	canDrag = false;
}

var writableColMap = new Map();

function setNestTableColWritable(fieldName) {
	if (Main_Tab) {
		var rowsLen = Main_Tab.rows.length;
		var index = getCellIndexByFieldName(fieldName);
		
		writableColMap.put(fieldName, index);
	}
}

function sel_<%=formCode%>(parentId) {
	openWin("<%=request.getContextPath()%>/visual/module_list_nest_sel.jsp?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_table&parentId=" + parentId, 800, 600);
}
</script>
