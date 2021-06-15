//if (cws_bLoad==false)
//{
	//cws_InitDocument("Body","GB2312");
//}

if (cws_bIsNC){
	document.write('<iframe width="260" height="165" id="colourPalette" src="' + editorRootPath + 'images/nc_selcolor.htm" style="visibility:hidden; position: absolute; left: 0px; top: 0px;" frameborder="0" scrolling="no" ></iframe>');
}

if (PostType == 0)
{
	cws_setMode(3);
	document.getElementById("cws_TabDesign").style.display='none';
	document.getElementById("cws_TabDesign").style.display='none';
	//onpaste
	//document.selection.createRange().text
	//(window.clipboardData.getData("Text")
}

//数据传递
function cws_CopyData(hiddenid)
{
	//document.Dvform.Submit.disabled=true;
	//document.Dvform.Submit2.disabled=true;
	if (PostType == 0 && cws_bTextMode == 3)
	{
		cws_PasteData()
	}
	d = IframeID.document;
	if (cws_bTextMode == 2)
	{
		cont = d.body.innerText;
	}else{
		cont = d.body.innerHTML;  
	}
	var ChekEmptyCode = cws_ChekEmptyCode(cont);
	if (ChekEmptyCode == '' || ChekEmptyCode == null)
	{
		cont='';
	}
	else{
		cont = cws_correctUrl(cont);
		if (cws_filterScript)
		cont=cws_FilterScript(cont);
	}
	document.getElementById(hiddenid).value = cont;
}

function cws_PasteData()
{
	var regExp;
	cont = IframeID.document.body.innerHTML;
	regExp = /<[s|t][a-z]([^>]*)>/ig
	cont = cont.replace(regExp, '');
	regExp = /<\/[s|t][a-z]([^>]*)>/ig
	cont = cont.replace(regExp, '');
	IframeID.document.body.innerHTML = cont
}
//-------------------------------------
function ctlent(eventobject)
{
	if(event.ctrlKey && event.keyCode==13)
	{
		this.document.Dvform.submit();
	}
}

function putEmot(thenNo)
{
	var ToAdd = '['+thenNo+']';
	IframeID.document.body.innerHTML+=ToAdd;
	IframeID.focus();
}

function gopreview()
{
	document.preview.Dvtitle.value=document.Dvform.topic.value;
	document.preview.theBody.value=IframeID.document.body.innerHTML;
	var popupWin = window.open('', 'preview_page', 'scrollbars=yes,width=750,height=450');
	document.preview.submit()
}

//--------------------------------------------------------------------------------

function cws_foreColor()
{
	if (!cws_validateMode()) return;
	if (cws_bIsIE5){
		var arr = showModalDialog(editorRootPath + "/editor_full/images/selcolor.jsp", "", "dialogWidth:18.5em; dialogHeight:17.5em; status:0; help:0");
		if (arr != null) FormatText('forecolor', arr);
		else IframeID.focus();
	}else{
		FormatText('forecolor', '');
		//var arr = openEditScript('images/nc_selcolor.htm',250,100)}
	}
}

function cws_backColor()
{
	if (!cws_validateMode()) return;
	if (cws_bIsIE5)
	{
		var arr = showModalDialog(editorRootPath + "/editor_full/images/selcolor.jsp", "", "dialogWidth:18.5em; dialogHeight:17.5em; status:0; help:0");
		if (arr != null) FormatText('backcolor', arr);
		else IframeID.focus();
	}else
	{
		FormatText('backcolor', '');
	}
}

function cws_correctUrl(cont)
{
	var regExp;
	var url=location.href.substring(0,location.href.lastIndexOf("/")+1);
	cont=cws_rCode(cont,location.href+"#","#");
	cont=cws_rCode(cont,url,"");
	cont=cws_rCode(cont,"<a>　</a>","");
	//regExp = /<a.*href=\"(.*)\"[^>]*>/gi;
	//将连接加上blank标记
	//regExp = /<(a[^>]*) href=([^ |>]*)([^>]*)/gi
	//cont = cont.replace(regExp, "<$1 href=$2 target=\"_blank\" ") ;
	//regExp = /<([^>]*)/gi //转换为小写htm
	//cont = cont.replace(regExp, function($1){return $1.toLowerCase()})
	return cont;
}


function cws_cleanHtml()
{
	var fonts = IframeID.document.getElementsByTagName("FONT");
	var curr;
	for (var i = fonts.length - 1; i >= 0; i--) {
		curr = fonts[i];
		if (curr.style.backgroundColor == "#ffffff") curr.outerHTML = curr.innerHTML;
	}
}

function cws_getPureHtml()
{
	var str = "";
	//var paras = IframeID.document.body.all.tags("P");
	//var paras = IframeID.document.getElementsByTagName("p");
	//if (paras.length > 0){
	  //for	(var i=paras.length-1; i >= 0; i--) str= paras[i].innerHTML + "\n" + str;
	//} else {
	str = IframeID.document.body.innerHTML;
	//}
	str=cws_correctUrl(str);
	return str;
}

function FormatUrl(html)
{
	var regExp = /<a.*href=\"(.*)\"[^>]*>/gi;
	html = html.replace(regExp,"<a href=$1 target=\"_blank\" >")
  return html;
}


function cws_getEl(sTag,start)
{
	while ((start!=null) && (start.tagName!=sTag)) start = start.parentElement;
	return start;
}

//选择事件
function cws_selectRange(){
	if (IframeID.getSelection) {  // all browsers, except IE before version 9
		cws_selection = IframeID.getSelection();
		
		if (cws_selection.rangeCount > 0) {
			cws_edit = cws_selection.getRangeAt(0);
        }
	}
	else {
		cws_selection =	IframeID.document.selection;
		cws_edit	  =	cws_selection.createRange();
	}
	cws_RangeType =	cws_selection.type;
}

//应用html
function cws_specialtype(Mark1, Mark2){
	var strHTML;
	if (cws_bIsIE5){
		cws_selectRange();
		if (cws_RangeType == "Text"){
			if (Mark2==null)
			{
				strHTML = "<" + Mark1 + ">" + cws_edit.htmlText + "</" + Mark1 + ">"; 
			}else{
				strHTML = Mark1 + cws_edit.htmlText +  Mark2; 
			}
			cws_edit.pasteHTML(strHTML);
			IframeID.focus();
			cws_edit.select();
		}
		else{window.alert("请选择相应内容！")}	
	}
	else{
		if (Mark2==null)
		{
		strHTML	=	"<" + Mark1 + ">" + IframeID.document.body.innerHTML + "</" + Mark1 + ">"; 
		}else{
		strHTML = Mark1 + IframeID.document.body.innerHTML +  Mark2; 
		}
		IframeID.document.body.innerHTML=strHTML
		IframeID.focus();
	}
}

// 修改编辑栏高度
function cws_Size(num)
{
	var obj=document.getElementById("cws_edit");
	obj.style.width = "100%";
	
	// 如果直接置为$(window).height()，高度实际会大120的样子，经反复测试，需减去120，可能是因为CSS生效的原因
	if($('#cws_edit') != null)
	   $('#cws_edit').height($(window).height() - 120);
	 
	
	// $('#cws_edit').height($('#cws_edit').height() - 120)
	
	return;

	
	//if (parseInt(obj.style.height)+num>=300) {
		//alert(obj.style.height)
		//obj.style.height = (parseInt(obj.style.height) + num);
	if (num>0){
		obj.style.height=num+"px";
		obj.style.width="100%";
	}
	else{
		obj.style.height="";
		//alert(-num+"px");
		obj.style.width=-num+"px";
	}
}

function cws_getText()
{
	if (cws_bTextMode==2)
		return IframeID.document.body.innerText;
	else
	{
		cws_cleanHtml();
		return IframeID.document.body.innerHTML;
	}
}

function cws_putText(v)
{
	if (cws_bTextMode==2)
		IframeID.document.body.innerText = v;
	else
		IframeID.document.body.innerHTML = v;
}

function cws_doSelectClick(str, el)
{
	var Index = el.selectedIndex;
	if (Index != 0){
		el.selectedIndex = 0;
		FormatText(str,el.options[Index].value);
	}
}
//查找配对字符出现次数,没有结果为0
function TabCheck(word,str){
	var tp=0
	chktp=str.search(word);
	if (chktp!=-1)
	{
	eval("var tp=\""+str+"\".match("+word+").length")
	}
	return tp;
}

function cws_help()
{
	showModalDialog(editorRootPath + "/editor_full/images/help.html", "", "dialogWidth:13.5em; dialogHeight:12.5em; status:0; help:0");
}

function openEditScript(url, width, height){
	var Win = window.open(url,"openEditScript",'width=' + width + ',height=' + height + ',resizable=1,scrollbars=yes,menubar=no,status=no' );
}

//广告标记
function cws_View()
{
	if (cws_bTextMode==2) {
		cont=IframeID.document.body.innerText;
	} else {
		cont=IframeID.document.body.innerHTML;
	}
	cont=cws_correctUrl(cont);
	bodyTag="<html><head><style type=text/css>.quote{margin:5px 20px;border:1px solid #CCCCCC;padding:5px; background:#F3F3F3 }\nbody{boder:0px}.HtmlCode{margin:5px 20px;border:1px solid #CCCCCC;padding:5px;background:#FDFDDF;font-size:14px;font-family:Tahoma;font-style : oblique;line-height : normal ;font-weight:bold;}\nbody{boder:0px}</style></head><BODY bgcolor=\"#FFFFFF\" >";
	if (cws_filterScript)
	cont=cws_FilterScript(cont);
	cont=cws_rCode(cont,"\\[dvnews_ad]","<img src='images/images/pic_ad.jpg' vspace=10 hspace=10 align=left border=1 title='Advertising'>");
	cont=cws_rCode(cont,"\\[dvnews_page]","<br><br><hr size=2 width=95% align=left>&nbsp; <font color=red face='Tahoma,Arail' size=2><b>Next Page ...</b></font><br><hr size=2 width=95% align=left>");
	preWin=window.open('preview','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=0');
	preWin.document.open();
	preWin.document.write(bodyTag);
	preWin.document.write(cont);
	preWin.document.close();
	preWin.document.title="Preview";
	preWin.document.charset=cws_charset;
}

//Colour pallete top offset
function getOffsetTop(elm) {
	var mOffsetTop = elm.offsetTop;
	var mOffsetParent = elm.offsetParent;
	while(mOffsetParent){
		mOffsetTop += mOffsetParent.offsetTop;
		mOffsetParent = mOffsetParent.offsetParent;
	}
	return mOffsetTop;
}

//Colour pallete left offset
function getOffsetLeft(elm) {
	var mOffsetLeft = elm.offsetLeft;
	var mOffsetParent = elm.offsetParent;
	while(mOffsetParent) {
		mOffsetLeft += mOffsetParent.offsetLeft;
		mOffsetParent = mOffsetParent.offsetParent;
	}
	return mOffsetLeft;
}

//Function to hide colour pallete
function hideColourPallete() {
	document.getElementById("colourPalette").style.visibility="hidden";
}

//------------------------------------------------------
function OpenSmiley()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/smiley.htm", "", "dialogWidth:60em; dialogHeight:15.5em; status:0; help:0");
	if (arr != null){
		var ss;
		ss=arr.split("*")
		path=ss[0];
		ubbstring=ss[1];
		IframeID.document.body.innerHTML+=ubbstring;
	}
	else IframeID.focus();
}

function cws_forswf()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/swf.jsp", "", "dialogWidth:30em; dialogHeight:10em; status:0; help:0");
	if (arr != null){
		var ss;
		ss=arr.split("*")
		path=ss[0];
		row=ss[1];
		col=ss[2];
		var string;
		string="<object classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000'  codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=5,0,0,0' width="+row+" height="+col+"><param name=movie value="+path+"><param name=quality value=high><embed src="+path+" pluginspage='http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash' type='application/x-shockwave-flash' width="+row+" height="+col+"></embed></object>"
		//string="[flash="+row+","+col+"]"+path+"[/flash]"
		IframeID.document.body.innerHTML+=string;
	}
	else IframeID.focus();
}

function cws_forwmv()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/wmv.jsp", "", "dialogWidth:30em; dialogHeight:14em; status:0; help:0");
	
	if (arr != null){
		var ss;
		ss=arr.split("*")
		path=ss[0];
		autostart=ss[1];
		width=ss[2];
		height=ss[3];
		ran=rand();
		var string;
		var ubbstring;
		string="<object align=center classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 hspace=5 vspace=5 width="+ width +" height="+ height +"><param name=Filename value="+ path +"><param name=ShowStatusBar value=1><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src="+ path +"  width="+ width +" height="+ height +"></embed></object>";
		string="<EMBED id=MediaPlayer"+ran+" src="+ path +" width="+ width +" height="+ height +" autostart=\""+ autostart +"\" loop=\"false\"></EMBED><p></p>";
		//string="[MP="+ width +","+ height +","+ autostart +"]"+ path +"[/MP]";
		IframeID.document.body.innerHTML+=string;
	}
	else IframeID.focus();
}

function rand() {
	return parseInt((1000)*Math.random()+1);
}

function cws_forrm()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/rm.jsp", "", "dialogWidth:30em; dialogHeight:14em; status:0; help:0");
	
	if (arr != null)
	{
		var ss;
		ss = arr.split("*")
		path = ss[0];
		row = ss[1];
		col = ss[2];
		autostart = ss[3];
		ran = rand();
		var string;
		string="<object classid='clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA' width='"+row+"' height='"+col+"'><param name='CONTROLS' value='ImageWindow'><param name='CONSOLE' value='Clip'><param name='AUTOSTART' value='"+ autostart +"'><param name=src value="+path+"></object><br><object classid='clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA'  width="+row+" height=32><param name='CONTROLS' value='ControlPanel,StatusBar'><param name='CONSOLE' value='Clip'></object>";
		//string = "[RM="+ row +","+ col +","+ autostart +"]"+ path +"[/RM]";
		IframeID.document.body.innerHTML+=string;
	}
	else IframeID.focus();
}

//图片与链接事件
function cws_UserDialog(what)
{
	if (!cws_validateMode()) return;
	IframeID.focus();
	if (what == "CreateLink") {
		if (cws_bIsNC)
		{
			insertLink = prompt("请填写超级链接地址信息：", "http://");			
			if ((insertLink != null) && (insertLink != "") && (insertLink != "undefined")) {
			IframeID.document.execCommand('CreateLink', false, insertLink);
			}else{
			IframeID.document.execCommand('unlink', false, null);
			}
		}
		else {
			IframeID.document.execCommand(what, true, null);
		}
	}
	//去掉添加图片时的src="file://
	if(what == "InsertImage"){
		imagePath = prompt('请填写图片链接地址信息：', 'http://');			
		if ((imagePath != null) && (imagePath != "")) {
			IframeID.document.execCommand('InsertImage', false, imagePath);
		}
		IframeID.document.body.innerHTML = (IframeID.document.body.innerHTML).replace("src=\"file://","src=\"");
	}
	cws_pureText = false;
	IframeID.focus();
}

//--------------------
function cws_GetRangeReference(editor)
{
	editor.focus();
	var objReference = null;
	var RangeType = editor.document.selection.type;
	var selectedRange = editor.document.selection.createRange();
	
	switch(RangeType)
	{
	case 'Control' :
		if (selectedRange.length > 0 ) 
		{
			objReference = selectedRange.item(0);
		}
	break;
	case 'None' :
		objReference = selectedRange.parentElement();
		break;
	case 'Text' :
		objReference = selectedRange.parentElement();
		break;
	}
	return objReference
}

function cws_CheckTag(item,tagName)
{
	if (item.tagName.search(tagName)!= -1)
	{
		return item;
	}
	if (item.tagName == 'BODY')
	{
		return false;
	}
	item=item.parentElement;
	return cws_CheckTag(item,tagName);
}

function cws_code()
{
	cws_specialtype("<div class=HtmlCode style='cursor: pointer'; title='点击运行该代码！' onclick=\"preWin=window.open('','','');preWin.document.open();preWin.document.write(this.innerText);preWin.document.close();\">","</div>");	
	//cws_specialtype("<div class=HtmlCode>","</div>");	
}

function cws_quote()
{
	cws_specialtype("<div style='margin:5px 20px;border:1px solid #CCCCCC;padding:5px; background:#F3F3F3'>","</div>");
}

function cws_replace()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/replace.jsp", "", "dialogWidth:16.5em; dialogHeight:13em; status:0; help:0");
	if (arr != null){
		var ss;
		ss = arr.split("*")
		a = ss[0];
		b = ss[1];
		i = ss[2];
		con = IframeID.document.body.innerHTML;
		if (i == 1)
		{
			con = cws_rCode(con,a,b,true);
		}else{
			con = cws_rCode(con,a,b);
		}
		IframeID.document.body.innerHTML = con;
	}
	else IframeID.focus();
}

function insertSpecialChar()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/specialchar.jsp", "","dialogWidth:25em; dialogHeight:15em; status:0; help:0");
	if (arr != null) cws_InsertSymbol(arr);
	IframeID.focus() ;
}

function doZoom( sizeCombo ) 
{
	if (sizeCombo.value != null || sizeCombo.value != "")
	if (cws_bIsIE5){
	var z = IframeID.document.body.runtimeStyle;}
	else{
	var z = IframeID.document.body.style;
	}
	z.zoom = sizeCombo.value + "%" ;
}
//--------------------

function cws_fortable()
{
	if (!cws_validateMode())	return;
	IframeID.focus();
	var arr = showModalDialog(editorRootPath + "/editor_full/images/table.jsp", window, "dialogWidth:22em; dialogHeight:18.5em; status:0; help:0;scroll:yes;");
	if (arr)
	{
		IframeID.document.body.innerHTML+=arr;
	}
	IframeID.focus();
}

function cws_InsertRow()
{
	editor = IframeID;
	objReference = cws_GetRangeReference(editor);
	objReference = cws_CheckTag(objReference,'/^(TABLE)|^(TR)|^(TD)|^(TBODY)/');
	switch(objReference.tagName)
	{
	case 'TABLE' :
		var newTable = objReference.cloneNode(true);
		var newRow = newTable.insertRow();
		for(x = 0; x<newTable.rows[0].cells.length; x++)
			{
			var newCell = newRow.insertCell();
			}
		objReference.outerHTML = newTable.outerHTML;
		break;
	case 'TBODY' :
		var newTable = objReference.cloneNode(true);
		var newRow = newTable.insertRow();
		for(x = 0; x<newTable.rows[0].cells.length; x++)
			{
			var newCell = newRow.insertCell();
			}
		objReference.outerHTML=newTable.outerHTML;
		break;
	case 'TR' :
		var rowIndex = objReference.rowIndex;
		var parentTable = objReference.parentElement.parentElement;
		var newTable = parentTable.cloneNode(true);
		var newRow = newTable.insertRow(rowIndex+1);
		for(x = 0; x< newTable.rows[0].cells.length; x++)
		{
			var newCell = newRow.insertCell();
		}
		parentTable.outerHTML = newTable.outerHTML;
		break;
	case 'TD' :
		var parentRow = objReference.parentElement;
		var rowIndex = parentRow.rowIndex;
		var cellIndex = objReference.cellIndex;
		var parentTable = objReference.parentElement.parentElement.parentElement;
		var newTable = parentTable.cloneNode(true);
		var newRow = newTable.insertRow(rowIndex+1);
		for(x = 0; x< newTable.rows[0].cells.length; x++)
			{
			var newCell = newRow.insertCell();
			if (x == cellIndex)newCell.id='ura';
			}
		parentTable.outerHTML = newTable.outerHTML;
		var r = editor.document.body.createTextRange();
		var item = editor.document.getElementById('ura');
		item.id = '';
		r.moveToElementText(item);
		r.moveStart('character',r.text.length);
		r.select();
		break;
	default :
	return;
	}
}

function cws_DeleteRow()
{
	editor=IframeID;
	objReference=cws_GetRangeReference(editor);
	objReference=cws_CheckTag(objReference,'/^(TABLE)|^(TR)|^(TD)|^(TBODY)/');
	switch(objReference.tagName)
	{
	case 'TR' :var rowIndex = objReference.rowIndex;//Get rowIndex
	var parentTable=objReference.parentElement.parentElement;
	parentTable.deleteRow(rowIndex);
	break;
	case 'TD' :var cellIndex=objReference.cellIndex;
	var parentRow=objReference.parentElement;//Get Parent Row
	var rowIndex = parentRow.rowIndex;//Get rowIndex
	var parentTable=objReference.parentElement.parentElement.parentElement;
	parentTable.deleteRow(rowIndex);
	if (rowIndex>=parentTable.rows.length)
	{
	rowIndex=parentTable.rows.length-1;
	}
	if (rowIndex>=0)
	{
	var r = editor.document.body.createTextRange();
	r.moveToElementText(parentTable.rows[rowIndex].cells[cellIndex]);
	r.moveStart('character',r.text.length);
	r.select();
	}
	else
	{
	parentTable.removeNode(true);
	}
	break;
	default :return;
	}
}

function cws_InsertColumn()
{
	editor = IframeID;
	objReference= cws_GetRangeReference(editor);
	objReference=cws_CheckTag(objReference,'/^(TABLE)|^(TR)|^(TD)|^(TBODY)/');
	switch(objReference.tagName)
	{
	case 'TABLE' :// IF a table is selected, it adds a new column on the right hand side of the table.
	var newTable=objReference.cloneNode(true);
	for(x=0; x<newTable.rows.length; x++)
	{
	var newCell = newTable.rows[x].insertCell();
	}
	newCell.focus();
	objReference.outerHTML=newTable.outerHTML;
	break;
	case 'TBODY' :// IF a table is selected, it adds a new column on the right hand side of the table.
	var newTable=objReference.cloneNode(true);
	for(x=0; x<newTable.rows.length; x++)
	{
	var newCell = newTable.rows[x].insertCell();
	}
	objReference.outerHTML=newTable.outerHTML;
	break;
	case 'TR' :// IF a table is selected, it adds a new column on the right hand side of the table.
	objReference=objReference.parentElement.parentElement;
	var newTable=objReference.cloneNode(true);
	for(x=0; x<newTable.rows.length; x++)
	{
	var newCell = newTable.rows[x].insertCell();
	}
	objReference.outerHTML=newTable.outerHTML;
	break;
	case 'TD' :// IF the cursor is in a cell, or a cell is selected, it adds a new column to the right of that cell.
	var cellIndex = objReference.cellIndex;//Get cellIndex
	var rowIndex=objReference.parentElement.rowIndex;
	var parentTable=objReference.parentElement.parentElement.parentElement;
	var newTable=parentTable.cloneNode(true);
	for(x=0; x<newTable.rows.length; x++)
	{
	var newCell = newTable.rows[x].insertCell(cellIndex+1);
	if (x==rowIndex)newCell.id='ura';
	}
	parentTable.outerHTML=newTable.outerHTML;
	var r = editor.document.body.createTextRange();
	var item=editor.document.getElementById('ura');
	item.id='';
	r.moveToElementText(item);
	r.moveStart('character',r.text.length);
	r.select();
	break;
	default :
	return;
	}
}

function cws_DeleteColumn()
{
	editor = IframeID;
	objReference=cws_GetRangeReference(editor);
	objReference=cws_CheckTag(objReference,'/^(TABLE)|^(TR)|^(TD)|^(TBODY)/');
	switch(objReference.tagName)
	{
	
	case 'TD' :var rowIndex=objReference.parentElement.rowIndex;
	var cellIndex = objReference.cellIndex;//Get cellIndex
	var parentTable=objReference.parentElement.parentElement.parentElement;
	var newTable=parentTable.cloneNode(true);
	if (newTable.rows[0].cells.length==1)
	{
	parentTable.removeNode(true);
	return;
	}
	for(x=0; x<newTable.rows.length; x++)
	{
	if (newTable.rows[x].cells[cellIndex]=='[object]')
	{
	newTable.rows[x].deleteCell(cellIndex);
	}
	}
	if (cellIndex>=newTable.rows[0].cells.length)
	{
	cellIndex=newTable.rows[0].cells.length-1;
	}
	if (cellIndex>=0)  newTable.rows[rowIndex].cells[cellIndex].id='ura';
	parentTable.outerHTML=newTable.outerHTML;
	if (cellIndex>=0){
	var r = editor.document.body.createTextRange();
	var item=editor.document.getElementById('ura');
	item.id='';
	r.moveToElementText(item);
	r.moveStart('character',r.text.length);
	r.select();
	}
	break;
	default :return;
	}
}

function insertHtmlAfterSelection(html) {
    var sel, range, node;
    if (IframeID.getSelection) {
        sel = IframeID.getSelection();
        if (sel.getRangeAt && sel.rangeCount) {
            range = IframeID.getSelection().getRangeAt(0);
            range.collapse(false);

            // Range.createContextualFragment() would be useful here but is
            // non-standard and not supported in all browsers (IE9, for one)
            var el = IframeID.document.createElement("div");
            el.innerHTML = html;
            var frag = IframeID.document.createDocumentFragment(), node, lastNode;
            while ( (node = el.firstChild) ) {
                lastNode = frag.appendChild(node);
            }
            range.insertNode(frag);
        }
    }
}

var cws_edit;

function cws_InsertSymbol(str1)
{	
	cws_Composition.focus();
 
	if (cws_bIsIE5) cws_selectRange();
	// var cws_edit = document.getElementById("cws_edit");
	// 部分ie9(可能已升级)及ie10以上已不再支持pasteHTML
	if (cws_edit && typeof(cws_edit.pasteHTML)!="undefined") {
		cws_edit.pasteHTML(str1);
	}
	else {
		 insertHtmlAfterSelection(str1);
	}	
	
	/*
	// ie10已不再支持pasteHTML
	if (isIE10 || isIE11) {
		 insertHtmlAfterSelection(str1);
	}
	else {
		if (cws_bIsIE5) cws_selectRange();
		
		// ie9中需要获取cws_edit，ie10和11不需要
		var cws_edit = document.getElementById("cws_edit");	
		
		cws_edit.pasteHTML(str1);
	}
	*/
}

function cws_foremot()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/emot.jsp", "", "dialogWidth:26em; dialogHeight:13em; status:0; help:0");
	
	if (arr != null)
	{
		//content=cws_Composition.document.body.innerHTML;
		//content=content+arr;
		//cws_Composition.document.body.innerHTML=content;
		cws_InsertSymbol(arr);
		IframeID.focus();
	}
	else IframeID.focus();
}

function cws_forimg()
{
if (cws_bIsIE5){	
	var arr=showModalDialog(editorRootPath + "/editor_full/images/img.jsp",window, "dialogWidth:26em; dialogHeight:20.5em; status:0; help:0");
	IframeID.focus();
	if (arr != null)
	{
		//content=cws_Composition.document.body.innerHTML;
		//content=content+arr[1];
		cws_InsertSymbol(arr[1]);
		IframeID.focus();
		//cws_Composition.document.body.innerHTML=content;
		
		//if (document.form2.upfiles.value !="")
		//{
			//document.form2.upfiles.value=document.form2.upfiles.value+"|"+get_upfilename(arr[2]);
		//}
		//else
		//{
			//document.form2.upfiles.value=get_upfilename(arr[2]);
		//}		
	}
	else IframeID.focus();
}
else {
	cws_UserDialog('InsertImage');
}
}

function cws_forlink()
{
if (cws_bIsIE5){		
	var arr=showModalDialog(editorRootPath + "/editor_full/images/link.jsp",window, "dialogWidth:23em; dialogHeight:11em; status:0; help:0");
	IframeID.focus();
	if (arr != null)
	{		
		cws_InsertSymbol(arr);
		IframeID.focus();
		
	}
	else IframeID.focus();
}
else {cws_UserDialog('CreateLink');}
}

function cws_forfile()
{
	var arr = showModalDialog(editorRootPath + "/editor_full/images/file.htm", "", "dialogWidth:23em; dialogHeight:11em; status:0; help:0");
	
	if (arr != null)
	{
		//content=WBTB_Composition.document.body.innerHTML;
		//content=content+arr[1];
		//WBTB_Composition.document.body.innerHTML=content;
		cws_InsertSymbol(arr[1]);
		IframeID.focus();
		//if (document.form2.upfiles.value !="")
		//{
			//document.form2.upfiles.value=document.form2.upfiles.value+"|"+get_upfilename(arr[2]);
		//}
		//else
		//{
			//document.form2.upfiles.value=get_upfilename(arr[2]);
		//}
		
	}
	else IframeID.focus();
}

function getText() {
	return IframeID.document.body.innerText
}

function getHTML() {
	var html;
	if (!cws_bTextMode) 
	{
	html = IframeID.document.body.innerHTML
	}
	else
	{
	html = IframeID.document.body.innerText
	}
	return html;
}

function setHTML(html) {
	IframeID.document.body.innerHTML = html;
}
