<%@ page contentType="text/html;charset=utf-8"%>
<%
String editorRootPath = request.getContextPath();
%>
function bbimg(o){
	var zoom=parseInt(o.style.zoom, 10)||100;zoom+=event.wheelDelta/12;if (zoom>0) o.style.zoom=zoom+'%';
	return false;
}
function GetCookie (name) {
var CookieFound = false;
var start = 0;
var end = 0;
var CookieString = document.cookie;
var i = 0;

while (i <= CookieString.length) {
start = i ;
end = start + name.length;
if (CookieString.substring(start, end) == name){
CookieFound = true;
break; 
}
i++;
}

if (CookieFound){
start = end + 1;
end = CookieString.indexOf(";",start);
if (end < start)
end = CookieString.length;
return unescape(CookieString.substring(start, end));
}
return "";
}

function openScript(url, width, height){
	var Win = window.open(url,"openScript",'width=' + width + ',height=' + height + ',resizable=1,scrollbars=yes,menubar=no,status=yes' );
}

var cws_bIsIE5=document.all;
var canusehtml='1';
var PostType=1;
var cws_bIsNC=false;

if (cws_bIsIE5){
	var IframeID=frames["cws_Composition"];
}
else{
	var IframeID=document.getElementById("cws_Composition").contentWindow;
	cws_bIsNC=true;
}

if (cws_bLoad==false)
{
	cws_InitDocument("Body","GB2312");
}
function submits(){
document.all("edit").value=IframeID.document.body.innerHTML;
}
function getHtml() {
	return IframeID.document.body.innerHTML;
}
function setHtml(obj) {
	IframeID.document.body.innerHTML = obj.value;
}

function setHTML(value) {
	IframeID.document.body.innerHTML = value;
}

function initx(){
var c_uname=GetCookie('username')
if (c_uname.indexOf('&')>=0){
	c_uname=c_uname.substring(0,c_uname.indexOf('&'))
}
var c_pass=GetCookie('password')
if (c_pass.indexOf('&')>=0){
	c_pass=c_pass.substring(0,c_pass.indexOf('&'))
}
var c_hpage=GetCookie('userurl')
if (c_hpage.indexOf('&')>=0){
	c_hpage=c_hpage.substring(0,c_hpage.indexOf('&'))
}
}
initx();

function del_space(s)
{
	for(i=0;i<s.length;++i)
	{
	 if(s.charAt(i)!=" ")
		break;
	}

	for(j=s.length-1;j>=0;--j)
	{
	 if(s.charAt(j)!=" ")
		break;
	}

	return s.substring(i,++j);
}

function Verifycomment()
{
	submits(); 
	return true;
}

function reply_quote(id)
{
	IframeID.focus();
	cws_InsertSymbol("<div class='quote'><strong>以下引用"+document.all["n_"+id].innerHTML+"在"+document.all["t_"+id].innerHTML+"发表的评论:</strong><br /><br />"+document.all["c_"+id].innerHTML+"</div><br />")
	IframeID.focus();
}

function DecodeCookie(str)
{
    var strArr;
    var strRtn="";
    strArr=str.split("a");
    try{
        for (var i=strArr.length-1;i>=0;i--)
        strRtn+=String.fromCharCode(eval(strArr[i]));
    }catch(e){
    }
    return strRtn; 
}

if (cws_bIsNC){
document.write('<iframe width="260" height="165" id="colourPalette" src="<%=editorRootPath%>/editor/nc_selcolor.htm" style="visibility:hidden; position: absolute; left: 0px; top: 0px;" frameborder="0" scrolling="no" ></iframe>');
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

// 数据传递
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
		var arr = showModalDialog("<%=editorRootPath%>/editor/selcolor.jsp", "", "dialogWidth:18.5em; dialogHeight:17.5em; status:0; help:0");
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
		var arr = showModalDialog("<%=editorRootPath%>/editor/selcolor.jsp", "", "dialogWidth:18.5em; dialogHeight:17.5em; status:0; help:0");
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
	if (cws_bIsIE5){
	var fonts = IframeID.document.body.all.tags("FONT");
	}else{
	var fonts = IframeID.document.getElementsByTagName("FONT");
	}
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

//选择内容替换文本
function cws_InsertSymbol(str1){
	IframeID.focus();
	if (cws_bIsIE5) {
	 	cws_selectRange();
		cws_edit.pasteHTML(str1);
	}
	else {
		IframeID.document.body.innerHTML = IframeID.document.body.innerHTML + str1;	
	}
}

//选择事件
function cws_selectRange(){
	cws_selection =	IframeID.document.selection;
	cws_edit		=	cws_selection.createRange();
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
	var arr = showModalDialog("<%=editorRootPath%>/editor/smiley.htm", "", "dialogWidth:60em; dialogHeight:15.5em; status:0; help:0");
	if (arr != null){
		var ss;
		ss=arr.split("*")
		path=ss[0];
		ubbstring=ss[1];
		IframeID.document.body.innerHTML+=ubbstring;
	}
	else IframeID.focus();
}



function rand() {
	return parseInt((1000)*Math.random()+1);
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
	var arr = showModalDialog("<%=editorRootPath%>/editor/replace.html", "", "dialogWidth:16.5em; dialogHeight:13em; status:0; help:0");
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
	var arr = showModalDialog("<%=editorRootPath%>/editor/specialchar.jsp", "","dialogWidth:25em; dialogHeight:15em; status:0; help:0");
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

function cws_foremot()
{
	var arr = showModalDialog("<%=editorRootPath%>/editor/emot.jsp", "", "dialogWidth:26em; dialogHeight:13em; status:0; help:0");
	
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

function cws_forlink()
{
if (cws_bIsIE5){		
	var arr=showModalDialog("<%=editorRootPath%>/editor/link.jsp",window, "dialogWidth:23em; dialogHeight:12em; status:0; help:0");
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

function cws_forimg()
{
	if (cws_bIsIE5){	
		var arr=showModalDialog("<%=editorRootPath%>" + "/editor/img.jsp",window, "dialogWidth:26em; dialogHeight:20.5em; status:0; help:0");
		IframeID.focus();
		if (arr != null)
		{
			cws_InsertSymbol(arr[1]);
			IframeID.focus();
		}
		else IframeID.focus();
	}
	else {
		cws_UserDialog('InsertImage');
	}
}

function cws_forswf()
{
	var arr = showModalDialog("<%=editorRootPath%>" + "/editor_full/images/swf.jsp", "", "dialogWidth:30em; dialogHeight:10em; status:0; help:0");
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

function rand() {
	return parseInt((1000)*Math.random()+1);
}

function cws_forwmv()
{
	var arr = showModalDialog("<%=editorRootPath%>" + "/editor_full/images/wmv.jsp", "", "dialogWidth:30em; dialogHeight:14em; status:0; help:0");
	
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

function cws_forrm()
{
	var arr = showModalDialog("<%=editorRootPath%>" + "/editor_full/images/rm.jsp", "", "dialogWidth:30em; dialogHeight:14em; status:0; help:0");
	
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