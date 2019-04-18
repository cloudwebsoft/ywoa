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
var text_enter_url      = "请输入连接网址";
var text_enter_txt      = "请输入连接说明";
var text_enter_image    = "请输入图片网址";
var text_enter_sound    = "请输入声音文件网址";
var text_enter_swf      = "请输入FLASH动画网址";
var text_enter_ra      = "请输入Real音乐网址";
var text_enter_rm      = "请输入Real影片网址";
var text_enter_wmv      = "请输入Media影片网址";
var text_enter_wma      = "请输入Media音乐网址";
var text_enter_mov      = "请输入QuickTime音乐网址";
var text_enter_sw      = "请输入shockwave音乐网址";
var text_enter_email    = "请输入邮件网址";
var error_no_url        = "您必须输入网址";
var error_no_txt        = "您必须连接说明";
var error_no_title      = "您必须输入首页标题";
var error_no_email      = "您必须输入邮件网址";
var error_no_gset       = "必须正确按照各式输入！";
var error_no_gtxt       = "必须输入文字！";
var text_enter_guang1   = "文字的长度、颜色和边界大小";
var text_enter_guang2   = "要产生效果的文字！";
var text_enter_points    = "请输入数值,如：1000 (即限制积分在1000点以下的用户不能浏览该内容!)";
var error_no_points       = "您必须输入数值,如：1000";
var text_enter_money    = "请输入数值,如：1000 (即限制金钱在1000点以下的用户不能浏览该内容!)";
var error_no_moeny       = "您必须输入数值,如：1000";
var text_enter_power    = "请输入数值,如：1000 (即限制威望在1000点以下的用户不能浏览该内容!)";
var error_no_power       = "您必须输入数值,如：1000";
var text_enter_post    = "请输入数值,如：1000 (即限制发表文章在1000点以下的用户不能浏览该内容!)";
var error_no_post       = "您必须输入数值,如：1000";
var text_enter_usercp    = "请输入数值,如：1000 (即限制魅力值在1000点以下的用户不能浏览该内容!)";
var error_no_usercp       = "您必须输入数值,如：1000";
function commentWrite(NewCode) {
document.frmAnnounce.Content.value+=NewCode;
document.frmAnnounce.Content.focus();
return;
}
function storeCaret(text) { 
	if (text.createTextRange) {
		text.caretPos = document.selection.createRange().duplicate();
	}
        if(event.ctrlKey && window.event.keyCode==13){i++;if (i>1) {alert('帖子正在发出，请耐心等待！');return false;}this.document.form.submit();}
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
alert("错误！"+FoundErrors);
return;
}
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
alert("错误！"+FoundErrors);
return;
}
var ToAdd = "[IMG]"+enterURL+"[/IMG]";
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
document.form.code.value = " 代码*";
Code = 1;
} else {
ToAdd = "[/CODE]";
document.form.code.value = " 代码 ";
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
function point() {
var FoundErrors = '';
var enterpoints  =prompt(text_enter_points,"1000");
if (!enterpoints) {
FoundErrors += "\n" + error_no_points;
}
if (FoundErrors) {
alert("错误！"+FoundErrors);
return;
}
var ToAdd = "[Point="+enterpoints+"][/Point]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}
function money() {
var FoundErrors = '';
var entermoney  =prompt(text_enter_money,"1000");
if (!entermoney) {
FoundErrors += "\n" + error_no_money;
}
if (FoundErrors) {
alert("错误！"+FoundErrors);
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
alert("错误！"+FoundErrors);
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
alert("错误！"+FoundErrors);
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
alert("错误！"+FoundErrors);
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
alert("错误！"+FoundErrors);
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
		alert("Flash\nFlash 动画.\n用法: [flash=宽度, 高度]Flash 文件的地址[/flash]");
	} else if (basic) {
		AddTxt="[flash=500,350][/flash]";
		AddText(AddTxt);
	} else {                  
		txt2=prompt("flash宽度，高度","500,350"); 
		if (txt2!=null) {
                txt=prompt("Flash 文件的地址","http://");
		if (txt!=null) {
                          if (txt2=="") {             
			AddTxt="[flash=500,350]"+txt;
			AddText(AddTxt);
			AddTxt="[/flash]";
			AddText(AddTxt);
               } else {
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
               alert("realplay\n播放realplay文件.\n用法: [rm=宽度, 高度]文件地址[/rm]");
	} else if (basic) {
		AddTxt="[rm=500,350][/rm]";
		AddText(AddTxt);
	} else { 
		txt2=prompt("视频的宽度，高度","500,350"); 
		if (txt2!=null) {
			txt=prompt("视频文件的地址","请输入");
			if (txt!=null) {
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
               alert("Media Player\n播放Media Player文件.\n用法: [mp=宽度, 高度]文件地址[/mp]");
	} else if (basic) {
		AddTxt="[mp=500,350][/mp]";
		AddText(AddTxt);
	} else { 
		txt2=prompt("视频的宽度，高度","500,350"); 
		if (txt2!=null) {
			txt=prompt("视频文件的地址","请输入");
			if (txt!=null) {
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
               alert("QuickTime\n播放QuickTime文件.\n用法: [qt=宽度, 高度]文件地址[/qt]");
	} else if (basic) {
		AddTxt="[qt=500,350][/qt]";
		AddText(AddTxt);
	} else { 
		txt2=prompt("视频的宽度，高度","500,350"); 
		if (txt2!=null) {
			txt=prompt("视频文件的地址","请输入");
			if (txt!=null) {
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
               alert("Shockwave\n插入Shockwave文件.\n用法: [dir=宽度, 高度]文件地址[/dir]");
	} else if (basic) {
		AddTxt="[dir=500,350][/dir]";
		AddText(AddTxt);
	} else { 
		txt2=prompt("Shockwave文件的宽度，高度","500,350"); 
		if (txt2!=null) {
			txt=prompt("Shockwave文件的地址","请输入地址");
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
alert("错误！"+FoundErrors);
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
alert("错误！"+FoundErrors);
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
var enterTxT   = prompt(text_enter_guang2, "文字");
if (!enterSET)    {
FoundErrors += "\n" + error_no_gset;
}
if (!enterTxT)    {
FoundErrors += "\n" + error_no_gtxt;
}
if (FoundErrors)  {
alert("错误！"+FoundErrors);
return;
}
var ToAdd = "[glow="+enterSET+"]"+enterTxT+"[/glow]";
document.frmAnnounce.Content.value+=ToAdd;
document.frmAnnounce.Content.focus();
}

function Cying() {
var FoundErrors = '';
var enterSET   = prompt(text_enter_guang1, "255,blue,1");
var enterTxT   = prompt(text_enter_guang2, "文字");
if (!enterSET)    {
FoundErrors += "\n" + error_no_gset;
}
if (!enterTxT)    {
FoundErrors += "\n" + error_no_gtxt;
}
if (FoundErrors)  {
alert("错误！"+FoundErrors);
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
var currentTitle = document.frmAnnounce.subject.value; 
revisedTitle = currentTitle+addTitle; 
document.frmAnnounce.subject.value=revisedTitle; 
document.frmAnnounce.subject.focus(); 
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