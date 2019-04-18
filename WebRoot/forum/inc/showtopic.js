function zoomimg(o){
	return;
	var zoom = parseInt(o.style.zoom, 10)||100;
	zoom += event.wheelDelta/12;
	if (zoom>0)
		o.style.zoom = zoom + "%";
	return false;
}
function SymError(){
  return true;
}
window.onerror = SymError;

function checkclick(msg)
{
	if(confirm(msg)) {
		event.returnValue=true;
		document.frmAnnounce.Content.value = "";
		setHtml(document.frmAnnounce.Content);
	}
	else
		event.returnValue=false;
}
function copyText(obj) {
	var rng = document.body.createTextRange();
	rng.moveToElementText(obj);
	rng.select();
	rng.execCommand('Copy');
}

function randomChar(l) {
	var x="¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄ";
	var tmp="";
	for(var i=0;i<l;i++) {
	tmp += x.charAt(Math.ceil(Math.random()*100000000)%x.length);
	}
	return tmp;
}
function appendWaterMark($1) {
	return $1+"<span style=\"DISPLAY:none\">"+randomChar(warterMarkLen)+copyright+randomChar(warterMarkLen)+"</span>";
}
function waterMarkCode(objName) {
	var cnt = document.getElementById(objName).innerHTML;
	cnt = cnt.replace(/<br>/gi, appendWaterMark);	
	cnt = cnt.replace(/<br \/>/gi, appendWaterMark);
	cnt = cnt.replace(/<\/p>/gi, appendWaterMark);
	document.getElementById(objName).innerHTML = cnt;
}
function runJS(obj) {
    var win = window.open('', "_blank", '');
    win.document.open('text/html', 'replace');
	win.opener = null // 防止代码对论坛页面修改
    win.document.write(obj.value);
    win.document.close();
}
function copyJS(obj) {
	if(isIE() && obj.style.display != 'none') {
		var range = document.body.createTextRange();
		range.moveToElementText(obj);
		range.scrollIntoView();
		range.select();
		range.execCommand("Copy");
		range.collapse(false);
	}
}
function saveJS(obj) {
    var win = window.open('', '_blank', 'top=10000');
    win.document.open('text/html', 'replace');
    win.document.write(obj.value);
    win.document.execCommand('saveas','','code.htm');
    win.close();
}