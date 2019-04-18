var cws_edit;	//selectRang
var cws_RangeType;
var cws_selection;
var cws_filterScript = true;
var cws_charset="UTF-8";
var cws_bLoad=false
var cws_pureText=true
var cws_bTextMode=1			//默认为Design模式

//预览
function cws_InitDocument(hiddenid, charset)
{	
	if (charset!=null)
	cws_charset=charset;
	//if (cws_bIsIE5){
		//var cws_bodyTag="<style type=text/css>.quote{margin:5px 20px;border:1px solid #CCCCCC;padding:5px; background:#F3F3F3 }\nbody{boder:0px}.HtmlCode{margin:5px 20px;border:1px solid #CCCCCC;padding:5px;background:#FDFDDF;font-size:14px;font-family:Tahoma;font-style : oblique;line-height : normal ;font-weight:bold;}\nbody{boder:0px}</style></head><BODY bgcolor=\"#FFFFFF\" title=\"Ctrl+Enter直接提交贴子\" onkeydown=\"ctlent();\">";
	//}else
	//{
		var cws_bodyTag="<style type=text/css>.quote{margin:5px 20px;border:1px solid #CCCCCC;padding:5px; background:#F3F3F3 }\nbody{boder:0px}.HtmlCode{margin:5px 20px;border:1px solid #CCCCCC;padding:5px;background:#FDFDDF;font-size:14px;font-family:Tahoma;font-style : oblique;line-height : normal ;font-weight:bold;}\nbody{boder:0px}</style></head><BODY bgcolor=\"#FFFFFF\">";
	//}
	
	if (navigator.appVersion.indexOf("MSIE 6.0",0)==-1){
	IframeID.document.designMode="On"
	}
	IframeID.document.open();
	IframeID.document.write ('<html><head>');
	if (cws_bIsIE5){
	IframeID.document.write ('<script language="javascript">');
	IframeID.document.write ('var ispost=0;');
	IframeID.document.write ('	function ctlent(eventobject)');
	IframeID.document.write ('	{');
	IframeID.document.write ('		if(event.ctrlKey && window.event.keyCode==13&&ispost==0)');
	IframeID.document.write ('		{');
	IframeID.document.write ('			ispost=1;');
	IframeID.document.write ('			parent.cws_CopyData("'+hiddenid+'"); ');
	IframeID.document.write ('			parent.document.ob_form.Submit.disabled=true;');
	IframeID.document.write ('			parent.document.ob_form.Submit2.disabled=true;');
	IframeID.document.write ('			parent.document.ob_form.submit();');
	IframeID.document.write ('		}');
	IframeID.document.write ('	}');
	IframeID.document.write ('<\/script>');
	}
	IframeID.document.write(cws_bodyTag);
	IframeID.document.write("</body>");
	IframeID.document.write("</html>");
	IframeID.document.close();
	IframeID.document.body.contentEditable = "True";
	IframeID.document.charset=cws_charset;
	cws_bLoad=true;
	cws_setStyle();
	//IframeID.focus();
}

function cws_setMode(n)
{
	cws_setStyle();
	var cont;
	var cws_Toolbar0=document.getElementById("ExtToolbar0");
	var cws_Toolbar1=document.getElementById("ExtToolbar1");
	var cws_Toolbar2=document.getElementById("ExtToolbar2");
	switch (n){
		case 1:
				cws_Toolbar0.style.display="";
				cws_Toolbar1.style.display="";
				cws_Toolbar2.style.display="";
				if (document.getElementById("cws_TabHtml").className=="cws_TabOn"){
					if (cws_bIsIE5){
						cont=IframeID.document.body.innerText;
						cont=cws_correctUrl(cont);
						if (cws_filterScript)
						cont=cws_FilterScript(cont);
						IframeID.document.body.innerHTML="<a>　</a>"+cont;
						//IframeID.document.body.innerHTML=cws_correctUrl(IframeID.document.body.innerHTML);
					}else{
						var html = IframeID.document.body.ownerDocument.createRange();
						html.selectNodeContents(IframeID.document.body);
						IframeID.document.body.innerHTML = html.toString();
					}
				}
				break;
		case 2:
				if (canusehtml=="1" || canusehtml=="3")
				{
					cws_Toolbar0.style.display="none";	//关闭工具栏
					cws_Toolbar1.style.display="none";
					cws_Toolbar2.style.display="none";
					cws_cleanHtml();
					cont=IframeID.document.body.innerHTML;
					cont=cws_rCode(IframeID.document.body.innerHTML,"<a>　</a>","");
					cont=cws_correctUrl(cont);
					if (cws_filterScript){cont=cws_FilterScript(cont);}
					if (cws_bIsIE5){					//IE
						IframeID.document.body.innerText=cont;
					}else{								//Nc
						var html=document.createTextNode(cont);
						IframeID.document.body.innerHTML = "";
						IframeID.document.body.appendChild(html);
					}
				}else{
				alert("您不能使用这个功能!")
				}
				break;

		case 3:
				cws_Toolbar0.style.display="";
				cws_Toolbar1.style.display="none";
				cws_Toolbar2.style.display="none";
				var mhtml=document.getElementById("cws_TabHtml");
				var mdesign=document.getElementById("cws_TabDesign");
				if (mhtml.className=="cws_TabOn")
				{
					if (cws_bIsIE5){
						cont=IframeID.document.body.innerText;
						cont=cws_correctUrl(cont);
						if (cws_filterScript)
						cont=cws_FilterScript(cont);
						IframeID.document.body.innerHTML=cont;
					}else{
						var html = IframeID.document.body.ownerDocument.createRange();
						html.selectNodeContents(IframeID.document.body);
						IframeID.document.body.innerHTML = html.toString();
					}
				}
				break;
	}
	cws_setTab(n);
	cws_bTextMode=n
}

function cws_setTab(n)
{
	//html和design按钮的样式更改
	var mhtml=document.getElementById("cws_TabHtml");
	var mdesign=document.getElementById("cws_TabDesign");
	if (n==1)
	{
		mhtml.className="cws_TabOff";
		mdesign.className="cws_TabOn";		
	}
	else if (n==2)
	{
		mhtml.className="cws_TabOn";
		mdesign.className="cws_TabOff";
	}
	else if (n==3)
	{
		mhtml.className="cws_TabOff";
		mdesign.className="cws_TabOff";
	}
}

function cws_setStyle()
{
	//var bs = IframeID.document.body.runtimeStyle;
	var bs = IframeID.document.body.style;
	//根据mode设置iframe样式表	
	if (cws_bTextMode==2) {
		bs.fontFamily="Arial";
		bs.fontSize="10pt";
	}else{
		bs.fontFamily="Arial";
		bs.fontSize="10.5pt";
	}
	bs.scrollbar3dLightColor= '#D4D0C8';
	bs.scrollbarArrowColor= '#000000';
	bs.scrollbarBaseColor= '#D4D0C8';
	bs.scrollbarDarkShadowColor= '#D4D0C8';
	bs.scrollbarFaceColor= '#D4D0C8';
	bs.scrollbarHighlightColor= '#808080';
	bs.scrollbarShadowColor= '#808080';
	bs.scrollbarTrackColor= '#D4D0C8';
	bs.border='0';
}

function cws_validateMode()
{
	if (cws_bTextMode!=2) return true;
	alert("请取消“查看HTML源代码”选项再使用系统编辑功能或者提交!");
	IframeID.focus();
	return false;
}

function cws_CleanCode()
{
	var editor=IframeID;
	editor.focus();
	if (cws_bIsIE5){
	// 0bject based cleaning
		var body = editor.document.body;
		for (var index = 0; index < body.all.length; index++) {
			tag = body.all[index];
		//*if (tag.Attribute["className"].indexOf("mso") > -1)
			tag.removeAttribute("className","",0);
			tag.removeAttribute("style","",0);
		}
	// Regex based cleaning
		var html = editor.document.body.innerHTML;
		html = html.replace(/\<p>/gi,"[$p]");
		html = html.replace(/\<\/p>/gi,"[$\/p]");
		html = html.replace(/\<br>/gi,"[$br]");
		html = html.replace(/\<[^>]*>/g,"");        ///过滤其它所有"<...>"标签
		html = html.replace(/\[\$p\]/gi,"<p>");
		html = html.replace(/\[\$\/p\]/gi,"<\/p>");
		html = html.replace(/\[\$br\]/gi,"<br>");
		editor.document.body.innerHTML = html;
	}else
	{
		var html = IframeID.document.body.ownerDocument.createRange();
		html.selectNodeContents(IframeID.document.body);
		IframeID.document.body.innerHTML = html.toString();
	}
}

function cws_ChekEmptyCode(html)
{
	html = html.replace(/\<[^>]*>/g,"");        ///过滤其它所有"<...>"标签
	html = html.replace(/&nbsp;/gi, "");
	html = html.replace(/o:/gi, "");
	html = html.replace(/\s/gi, "");
	return html;
}

var colour
function FormatText(command, option)
{
var codewrite
if (cws_bIsIE5){
		if (option=="removeFormat"){
		command=option;
		option=null;}
		IframeID.focus();
	  	IframeID.document.execCommand(command, false, option);
		cws_pureText = false;
		IframeID.focus();
		
}else{
		if ((command == 'forecolor') || (command == 'backcolor')) {
			parent.command = command;
			buttonElement = document.getElementById(command);
			IframeID.focus();
			document.getElementById("colourPalette").style.left = getOffsetLeft(buttonElement) + "px";
			document.getElementById("colourPalette").style.top = (getOffsetTop(buttonElement) + buttonElement.offsetHeight) + "px";
		
			if (document.getElementById("colourPalette").style.visibility=="hidden")
				{document.getElementById("colourPalette").style.visibility="visible";
			}else {
				document.getElementById("colourPalette").style.visibility="hidden";
			}
		
			//get current selected range
			var sel = IframeID.document.selection; 
			if (sel != null) {
				colour = sel.createRange();
			}
		}
		else{
		IframeID.focus();
	  	IframeID.document.execCommand(command, false, option);
		cws_pureText = false;
		IframeID.focus();
		}
	}

}

function setColor(color)
{
	IframeID.focus();
	IframeID.document.execCommand(parent.command, false, color);
	IframeID.focus();
	document.getElementById("colourPalette").style.visibility="hidden";
}

//----------------
function cws_FilterScript(content)
{
	content = cws_rCode(content, 'javascript:', '<b>javascript</b> :');
	var RegExp = /<script[^>]*>(.*)<\/script>/gi;
	content = content.replace(RegExp, "<div class=HtmlCode>&lt;!-- Script 代码开始 --&gt;<br>$1<br>&lt;!-- Script 代码结束 --&gt;</div>");
	RegExp = /<P>&nbsp;<\/P>/gi;
	content = content.replace(RegExp, "");
	return content;
}

function cws_rCode(s,a,b,i){
	//s原字串，a要换掉pattern，b换成字串，i是否区分大小写
	a = a.replace("?","\\?");
	if (i==null)
	{
		var r = new RegExp(a,"gi");
	}else if (i) {
		var r = new RegExp(a,"g");
	}
	else{
		var r = new RegExp(a,"gi");
	}
	return s.replace(r,b); 
}
//cws_InitDocument("Body","GB2312");