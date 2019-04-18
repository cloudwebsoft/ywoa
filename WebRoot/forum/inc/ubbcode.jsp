<%@ page contentType="text/html;charset=utf-8"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
var Quote = 0;
var Bold  = 0;
var Italic = 0;
var Underline = 0;
var Code = 0;
var Center = 0;
var Strike = 0;
var Sound = 0;
var Swf = 0;
var Ra = 0;
var Rm = 0;
var Marquee = 0;
var Fly = 0;
var fanzi=0;
var text_enter_url      = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_url"/>";
var text_enter_txt      = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_txt"/>";
var text_enter_image    = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_image"/>";
var text_enter_sound    = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_sound"/>";
var text_enter_swf      = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_swf"/>";
var text_enter_ra       = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_ra"/>";
var text_enter_rm       = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_rm"/>";
var text_enter_wmv      = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_wmv"/>";
var text_enter_wma      = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_wma"/>";
var text_enter_mov      = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_mov"/>";
var text_enter_sw       = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_sw"/>";
var text_enter_email    = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_email"/>";
var error_no_url        = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_url"/>";
var error_no_txt        = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_txt"/>";
var error_no_title      = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_title"/>";
var error_no_email      = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_email"/>";
var error_no_gset       = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_gset"/>";
var error_no_gtxt       = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_gtxt"/>";
var text_enter_guang1   = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_guang1"/>";
var text_enter_guang2   = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_guang2"/>";
var text_enter_points   = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_points"/>";
var error_no_points     = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_points"/>";
var text_enter_money    = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_money"/>";
var error_no_moeny      = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_moeny"/>";
var text_enter_power    = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_power"/>";
var error_no_power      = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_power"/>";
var text_enter_post     = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_enter_post"/>";
var error_no_post       = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_post"/>";
var text_enter_usercp   = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_usercp"/>";
var error_no_usercp     = "<lt:Label res="res.label.forum.inc.ubbcode" key="error_no_usercp"/>";
var text_t 				= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_t"/>";
var wait_send			= "<lt:Label res="res.label.forum.inc.ubbcode" key="wait_send"/>";
var text_code			= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_code"/>";
var text_flash_info		= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_flash_info"/>";
var text_flash_size		= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_flash_size"/>"
var text_flash_url 		= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_flash_url"/>";
var text_rm_info 		= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_rm_info"/>";
var text_movie_size 	= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_movie_size"/>";
var text_url 			= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_url"/>";
var text_input 			= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_input"/>";
var text_media_info 	= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_media_info"/>";
var text_media_size 	= "<lt:Label res="res.label.forum.inc.ubbcode" key="text_media_size"/>";
var text_quicktime_info = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_quicktime_info"/>";
var text_shockwave_info = "<lt:Label res="res.label.forum.inc.ubbcode" key="text_shockwave_info"/>";

function commentWrite(NewCode) {
document.frmAnnounce.Content.value+=NewCode;
document.frmAnnounce.Content.focus();
return;
}
function storeCaret(text) { 
	if (text.createTextRange) {
		text.caretPos = document.selection.createRange().duplicate();
	}
    if(event.ctrlKey && window.event.keyCode==13){i++;if (i>1) {alert(wait_send);return false;}this.document.form.submit();}
}
function AddText(text) {
	if (document.frmAnnounce.Content.createTextRange && document.frmAnnounce.Content.caretPos) {      
		var caretPos = document.frmAnnounce.Content.caretPos;      
		caretPos.text = caretPos.text.charAt(caretPos.text.length - 1) == ' ' ?
		text + ' ' : text;
	}
	else document.frmAnnounce.Content.value += text;
	document.frmAnnounce.Content.focus(caretPos);
}
function inputs(str)
{
AddText(str);
}
function Curl() {
	var FoundErrors = '';
	var enterURL   = prompt(text_enter_url, "http://");
	var enterTxT   = prompt(text_enter_txt, enterURL);
	if (!enterURL)    {
	FoundErrors += "\n" + error_no_url;
	}
	if (!enterTxT)    {
	FoundErrors += "\n" + error_no_txt;
	}
	if (FoundErrors)  {
	alert(FoundErrors);
	return;
	}
	
	var temp = enterURL;
	var regwhich = new RegExp("http","i")
	enterURL = temp.replace(regwhich,"hhttttpp") // 防止在fchar.ubb中被重复替换地址
	
	var ToAdd = "[URL="+enterURL+"]"+enterTxT+"[/URL]";
	document.frmAnnounce.Content.value+=ToAdd;
	document.frmAnnounce.Content.focus();
}

function Cimage() {
var FoundErrors = '';
var enterURL   = prompt(text_enter_image, "http://");
if (!enterURL) {
FoundErrors += "\n" + error_no_url;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}

var temp = enterURL;
var regwhich = new RegExp("http","i")
enterURL = temp.replace(regwhich,"hhttttpp")//防止在fchar.ubb中被重复替换地址

var ToAdd = "[img]"+enterURL+"[/img]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}
function Cemail() {
var emailAddress = prompt(text_enter_email,"");
if (!emailAddress) { alert(error_no_email); return; }
var ToAdd = "[EMAIL]"+emailAddress+"[/EMAIL]";
commentWrite(ToAdd);
}
function Ccode() {
if (Code == 0) {
ToAdd = "[CODE]";
document.form.code.value = text_code;
Code = 1;
} else {
ToAdd = "[/CODE]";
document.form.code.value = text_code;
Code = 0;
}
commentWrite(ToAdd);
}
function Cquote() {
fontbegin="[QUOTE]";
fontend="[/QUOTE]";
fontchuli();
}
function Cbold() {
fontbegin="[B]";
fontend="[/B]";
fontchuli();
}
function Citalic() {
fontbegin="[I]";
fontend="[/I]";
fontchuli();
}
function Cunder() {
fontbegin="[U]";
fontend="[/U]";
fontchuli();
}
function Ccenter() {
fontbegin="[center]";
fontend="[/center]";
fontchuli();
}
function Cstrike() {
fontbegin="[strike]";
fontend="[/strike]";
fontchuli();
}

function money() {
var FoundErrors = '';
var entermoney  =prompt(text_enter_money,"1000");
if (!entermoney) {
FoundErrors += "\n" + error_no_money;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}
var ToAdd = "[Money="+entermoney+"][/Money]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}
function usercp() {
var FoundErrors = '';
var enterusercp  =prompt(text_enter_usercp,"1000");
if (!enterusercp) {
FoundErrors += "\n" + error_no_usercp;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}
var ToAdd = "[UserCP="+enterusercp+"][/UserCP]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}
function power() {
var FoundErrors = '';
var enterpower  =prompt(text_enter_power,"1000");
if (!enterpower) {
FoundErrors += "\n" + error_no_power;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}
var ToAdd = "[Power="+enterpower+"][/Power]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}
function article() {
var FoundErrors = '';
var enterpost  =prompt(text_enter_post,"1000");
if (!enterpost) {
FoundErrors += "\n" + error_no_post;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}
var ToAdd = "[Post="+enterpost+"][/Post]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}
function replyview() {
var ToAdd = "[replyview][/replyview]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}
function Csound() {
var FoundErrors = '';
var enterURL   = prompt(text_enter_sound, "http://");
if (!enterURL) {
FoundErrors += "\n" + error_no_url;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}
var ToAdd = "[SOUND]"+enterURL+"[/SOUND]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}

helpstat = false;
stprompt = true;
basic = false;
function thelp(swtch){
	if (swtch == 1){
		basic = false;
		stprompt = false;
		helpstat = true;
	} else if (swtch == 0) {
		helpstat = false;
		stprompt = false;
		basic = true;
	} else if (swtch == 2) {
		helpstat = false;
		basic = false;
		stprompt = true;
	}
}

function Cswf() {
 	if (helpstat){
		alert(text_flash_info);
	} else if (basic) {
		AddTxt="[flash=500,350][/flash]";
		AddText(AddTxt);
	} else {                  
		txt2=prompt(text_flash_size,"500,350"); 
		if (txt2!=null) {
                txt=prompt(text_flash_url, "http://");
		if (txt!=null) {
			var temp = txt;
			var regwhich = new RegExp("http","i")
			txt = temp.replace(regwhich,"hhttttpp")//防止在fchar.ubb中被重复替换地址
		
        	if (txt2=="") {             
				AddTxt="[flash=500,350]"+txt;
				AddText(AddTxt);
				AddTxt="[/flash]";
				AddText(AddTxt);
            }else{
				AddTxt="[flash="+txt2+"]"+txt;
				AddText(AddTxt);
				AddTxt="[/flash]";
				AddText(AddTxt);
		 }        
	    }  
       }
    }
}

function Crm() {
	if (helpstat) {
        alert(text_rm_info);
	} else if (basic) {
		AddTxt="[rm=500,350][/rm]";
		AddText(AddTxt);
	} else { 
		txt2=prompt(text_movie_size,"500,350"); 
		if (txt2!=null) {
			txt=prompt(text_url, text_input);
			if (txt!=null) {
				var temp = txt;
				var regwhich = new RegExp("http","i")
				txt = temp.replace(regwhich,"hhttttpp")//防止在fchar.ubb中被重复替换地址
				regwhich = new RegExp("ftp","i")
				txt = txt.replace(regwhich,"ffttpp")//防止在fchar.ubb中被重复替换地址			
				if (txt2=="") {
					AddTxt="[rm=500,350]"+txt;
					AddText(AddTxt);
					AddTxt="[/rm]";
					AddText(AddTxt);
				} else {
					AddTxt="[rm="+txt2+"]"+txt;
					AddText(AddTxt);
					AddTxt="[/rm]";
					AddText(AddTxt);
				}         
			} 
		}
	}
}

function Cwmv() {
	if (helpstat) {
        alert(text_media_info);
	} else if (basic) {
		AddTxt="[mp=500,350][/mp]";
		AddText(AddTxt);
	} else { 
		txt2=prompt(text_movie_size,"500,350"); 
		if (txt2!=null) {
			txt=prompt(text_url, text_input);
			if (txt!=null) {
				var temp = txt;
				var regwhich = new RegExp("http","i")
				txt = temp.replace(regwhich,"hhttttpp")//防止在fchar.ubb中被重复替换地址
				regwhich = new RegExp("ftp","i")
				txt = txt.replace(regwhich,"ffttpp")//防止在fchar.ubb中被重复替换地址			
				if (txt2=="") {
					AddTxt="[mp=500,350]"+txt;
					AddText(AddTxt);
					AddTxt="[/mp]";
					AddText(AddTxt);
				} else {
					AddTxt="[mp="+txt2+"]"+txt;
					AddText(AddTxt);
					AddTxt="[/mp]";
					AddText(AddTxt);
				}         
			} 
		}
	}
}

function Cmov() {
	if (helpstat) {
        alert(text_quicktime_info);
	} else if (basic) {
		AddTxt="[qt=500,350][/qt]";
		AddText(AddTxt);
	} else { 
		txt2=prompt(text_movie_size, "500,350"); 
		if (txt2!=null) {
			txt=prompt(text_url, text_input);
			if (txt!=null) {
				var temp = txt;
				var regwhich = new RegExp("http","i")
				txt = temp.replace(regwhich,"hhttttpp")//防止在fchar.ubb中被重复替换地址
				regwhich = new RegExp("ftp","i")
				txt = txt.replace(regwhich,"ffttpp")//防止在fchar.ubb中被重复替换地址			
				if (txt2=="") {
					AddTxt="[qt=500,350]"+txt;
					AddText(AddTxt);
					AddTxt="[/qt]";
					AddText(AddTxt);
				} else {
					AddTxt="[qt="+txt2+"]"+txt;
					AddText(AddTxt);
					AddTxt="[/qt]";
					AddText(AddTxt);
				}         
			} 
		}
	}
}

function Cdir() {
	if (helpstat) {
        alert(text_shockwave_info);
	} else if (basic) {
		AddTxt="[dir=500,350][/dir]";
		AddText(AddTxt);
	} else { 
		txt2=prompt(text_movie_size,"500,350"); 
		if (txt2!=null) {
			txt=prompt(text_url, text_input);
			if (txt!=null) {
				if (txt2=="") {
					AddTxt="[dir=500,350]"+txt;
					AddText(AddTxt);
					AddTxt="[/dir]";
					AddText(AddTxt);
				} else {
					AddTxt="[dir="+txt2+"]"+txt;
					AddText(AddTxt);
					AddTxt="[/dir]";
					AddText(AddTxt);
				}         
			} 
		}
	}
}

function Cra() {
var FoundErrors = '';
var enterURL   = prompt(text_enter_ra, "http://");
if (!enterURL) {
FoundErrors += "\n" + error_no_url;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}
var ToAdd = "[RA]"+enterURL+"[/RA]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}

function Cfanzi() {
fontbegin="[xray]";
fontend="[/xray]";
fontchuli();
}

function Cwma() {
var FoundErrors = '';
var enterURL   = prompt(text_enter_wma, "http://");
if (!enterURL) {
FoundErrors += "\n" + error_no_url;
}
if (FoundErrors) {
alert(FoundErrors);
return;
}
var ToAdd = "[wma]"+enterURL+"[/wma]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}


function Cmarquee() {
fontbegin="[move]";
fontend="[/move]";
fontchuli();
}
function Cfly() {
fontbegin="[fly]";
fontend="[/fly]";
fontchuli();
}

function paste(text) {
	if (opener.document.frmAnnounce.Content.createTextRange && opener.document.frmAnnounce.Content.caretPos) {      
		var caretPos = opener.document.frmAnnounce.Content.caretPos;      
		caretPos.text = caretPos.text.charAt(caretPos.text.length - 1) == ' ' ?
		text + ' ' : text;
	}
	else opener.document.frmAnnounce.Content.value += text;
	opener.document.frmAnnounce.Content.focus(caretPos);
}

function showsize(size){
fontbegin="[size="+size+"]";
fontend="[/size]";
fontchuli();
}

function showfont(font){
fontbegin="[face="+font+"]";
fontend="[/face]";
fontchuli();
}

function showcolor(color){
fontbegin="[color="+color+"]";
fontend="[/color]";
fontchuli();
}

function fontchuli(){
if ((document.selection)&&(document.selection.type == "Text")) {
var range = document.selection.createRange();
var ch_text=range.text;
range.text = fontbegin + ch_text + fontend;
} 
else {
document.frmAnnounce.Content.value=fontbegin+document.frmAnnounce.Content.value+fontend;
document.frmAnnounce.Content.focus();
}
}

function Cguang() {
var FoundErrors = '';
var enterSET   = prompt(text_enter_guang1, "255,red,2");
var enterTxT   = prompt(text_enter_guang2, text_t);
if (!enterSET)    {
FoundErrors += "\n" + error_no_gset;
}
if (!enterTxT)    {
FoundErrors += "\n" + error_no_gtxt;
}
if (FoundErrors)  {
alert(FoundErrors);
return;
}
var ToAdd = "[glow="+enterSET+"]"+enterTxT+"[/glow]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}

function Cying() {
var FoundErrors = '';
var enterSET   = prompt(text_enter_guang1, "255,blue,1");
var enterTxT   = prompt(text_enter_guang2, text_t);
if (!enterSET)    {
FoundErrors += "\n" + error_no_gset;
}
if (!enterTxT)    {
FoundErrors += "\n" + error_no_gtxt;
}
if (FoundErrors)  {
alert(FoundErrors);
return;
}
var ToAdd = "[SHADOW="+enterSET+"]"+enterTxT+"[/SHADOW]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}

ie = (document.all)? true:false
if (ie){
function ctlent(eventobject){if(event.ctrlKey && window.event.keyCode==13){this.document.frmAnnounce.submit();}}
}
function DoTitle(addTitle) { 
var revisedTitle; 
var currentTitle = document.frmAnnounce.topic.value; 
revisedTitle = currentTitle+addTitle; 
document.frmAnnounce.topic.value=revisedTitle; 
document.frmAnnounce.topic.focus(); 
return; }

function insertsmilie(smilieface){

	document.frmAnnounce.Content.value+=smilieface;
}
function gopreview()
{
document.forms[1].title.value=document.forms[0].subject.value;
document.forms[1].body.value=document.forms[0].Content.value;
var popupWin = window.open('preview.asp', 'preview_page', 'scrollbars=yes,width=750,height=450');
document.forms[1].submit()
}

// 回复可见
function replyCanSee(){
	fontbegin = "[REPLY]";
	fontend = "[/REPLY]";
	if ((document.selection)&&(document.selection.type == "Text")) {
		var range = document.selection.createRange();
		var ch_text=range.text;
		range.text = fontbegin + ch_text + fontend;
	}
	else {
		document.frmAnnounce.Content.value=fontbegin+document.frmAnnounce.Content.value+fontend;
		document.frmAnnounce.Content.focus();
	}
}
function ownerCanSee(){
	var fontbegin = "[OWNER]";
	var fontend = "[/OWNER]";

    var cws_Composition = document.getElementById("cws_Composition");
    if (cws_Composition) {
    	if (cws_Composition.contentWindow.document.selection.type == "Text") {
            var range = cws_Composition.contentWindow.document.selection.createRange();
            var ch_text=range.text;
            range.text = fontbegin + ch_text + fontend;
        }
        else {
			IframeID.document.body.innerHTML += fontbegin+fontend;
        }
    }
    /* OA中document会被认为是上级窗口的document，如addreply_new.jsp 
	if ((document.selection)&&(document.selection.type == "Text")) {
		var range = document.selection.createRange();
		var ch_text=range.text;
		range.text = fontbegin + ch_text + fontend;
	}
    */
	else {
		document.frmAnnounce.Content.value=fontbegin+document.frmAnnounce.Content.value+fontend;
		document.frmAnnounce.Content.focus();
	}
}
function canSee(moneyCode) {
	var FoundErrors = '';
	var enterpoints = prompt(text_enter_points,"1000");
	if (!enterpoints) {
	FoundErrors += "\n" + error_no_points;
	}
	if (FoundErrors) {
	alert(FoundErrors);
	return;
	}

	fontbegin = "[HIDE=" + moneyCode + ","+enterpoints+"]";
	fontend = "[/HIDE]";
    
    var cws_Composition = document.getElementById("cws_Composition");
    if (cws_Composition) {
    	if (cws_Composition.contentWindow.document.selection.type == "Text") {
            var range = cws_Composition.contentWindow.document.selection.createRange();
            var ch_text=range.text;
            range.text = fontbegin + ch_text + fontend;
        }
        else {
			IframeID.document.body.innerHTML += fontbegin+fontend;
        }
    }
	/*    
	if ((document.selection)&&(document.selection.type == "Text")) {
		var range = document.selection.createRange();
		var ch_text=range.text;
		range.text = fontbegin + ch_text + fontend;
	}
    */
	else {
		document.frmAnnounce.Content.value=fontbegin+document.frmAnnounce.Content.value+fontend;
		document.frmAnnounce.Content.focus();
	}
}

function usePoint() {
	var ary = showModalDialog('point_sel.jsp',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;');
	if (ary==null)
		return;
		
	var fontbegin = "[point=" + ary[0] + "," + ary[1] + "]";
	var fontend = "[/point]";
	if ((document.selection)&&(document.selection.type == "Text")) {
		var range = document.selection.createRange();
		var ch_text=range.text;
		range.text = fontbegin + ch_text + fontend;
	}
	else {
		document.frmAnnounce.Content.value=fontbegin+document.frmAnnounce.Content.value+fontend;
		document.frmAnnounce.Content.focus();
	}
}

function payme() {
	var ary = showModalDialog('point_sel.jsp',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;');
	if (ary==null)
		return;
		
	var fontbegin = "[payme=" + ary[0] + "," + ary[1] + "]";
	var fontend = "[/payme]";
	if ((document.selection)&&(document.selection.type == "Text")) {
		var range = document.selection.createRange();
		var ch_text=range.text;
		range.text = fontbegin + ch_text + fontend;
	}
	else {
		document.frmAnnounce.Content.value=fontbegin+document.frmAnnounce.Content.value+fontend;
		document.frmAnnounce.Content.focus();
	}
}
