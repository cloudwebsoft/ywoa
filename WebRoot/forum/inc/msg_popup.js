function msgPopup(){
	var msgPop=document.getElementById("winpop");
	var popH=parseInt(msgPop.style.height);
	if (popH==0){
		msgPop.style.display="block";
		show=setInterval("changeH('up')",2);
	}
	else {
		hide=setInterval("changeH('down')",2);
	}
}
function changeH(str) {
	var msgPop=document.getElementById("winpop");
	var popH=parseInt(msgPop.style.height);
	if(str=="up"){
		if (popH<=100){
			msgPop.style.height=(popH+4).toString()+"px";
		}
		else{  
			clearInterval(show);
		}
	}
	if(str=="down"){ 
		if (popH>=4){
		   msgPop.style.height=(popH-4).toString()+"px";
		}
		else{
		   clearInterval(hide);
		   msgPop.style.display="none";
		}
	}
}
window.onload=function(){
document.getElementById('winpop').style.height='0px';
setTimeout("msgPopup()",80);
}