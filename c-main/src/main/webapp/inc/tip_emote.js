var curTipObj;
function tipEmote(curTipObjParam, posObj){	
	curTipObj = curTipObjParam;
	var x = posObj.getBoundingClientRect().left+document.documentElement.scrollLeft;
	var y = posObj.getBoundingClientRect().bottom+document.documentElement.scrollTop;
	$("#emoteBox")[0].style.display="block";
	$("#emoteBox")[0].style.left = (x-20) + "px";
	$("#emoteBox")[0].style.top = (y-10) + "px";
	$("#emoteBox")[0].style.zIndex = 1000;
}

function closeTipEmote(){
	if ($("#emoteBox"))
		$("#emoteBox")[0].style.display="none";
}

function insertEmote(em) {
	curTipObj.value = em + curTipObj.value;
}

function onclickForEmote() {
	if (window.event.srcElement.tagName != "SPAN")
		closeTipEmote();
}

/*
if ( window.addEventListener )
	document.addEventListener("onclick",onclickForEmote,true)
else
	document.onclick = onclickForEmote;
*/

$(document).click(function() {
	onclickForEmote();
});
