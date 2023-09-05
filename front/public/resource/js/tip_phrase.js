var curTipObj;
function tipPhrase(curTipObjParam, posObj){
	console.log('tipPhrase curTipObjParam', curTipObjParam);
	curTipObj = o(curTipObjParam);

	var x = $(posObj).offset().left + document.documentElement.scrollLeft + 15;
	var y = $(posObj).offset().top + document.documentElement.scrollTop + 10;

	if ($("#phraseBox")[0]) {
		$("#phraseBox")[0].style.display="block";

		$("#phraseBox").offset({left: x-20, top: y + 20});

		$("#phraseBox")[0].style.zIndex = 1000;
	}
}

function closeTipPhrase(){
	if ($("#phraseBox")[0])
		$("#phraseBox")[0].style.display="none";
}

function insertPhrase(em) {
	// curTipObj.value = em + curTipObj.value;
	try {
		var oRange;
		if(curTipObj.createTextRange){//IE浏览器     
			oRange = curTipObj.createTextRange();     
		}else{//非IE浏览器     
			oRange = curTipObj.setSelectionRange();     
		}
	}
	catch(e){
	}
  
	curTipObj.focus();
	var contlen = curTipObj.value.length;

	if(typeof document.selection != "undefined") {
			document.selection.createRange().text = em;
	}
	else {
			curTipObj.value = curTipObj.value.substr(0, curTipObj.selectionStart) + em + curTipObj.value.substring(curTipObj.selectionStart,contlen);
	}

	/*
	if (oRange.findText(em)!=false) {
	   oRange.select();
	}
	*/	
}

function onclickForPhrase(e) {
	var obj=e.target;
	// if (obj.tagName != "SPAN")
		closeTipPhrase();
}

$(document).click(function(e) {
	onclickForPhrase(e);
});

function insertText(txt) {
	if (typeof (insertCkeditorText) == "function") {
			insertCkeditorText(txt);
	} else {
			insertPhrase(txt);
	}
}
