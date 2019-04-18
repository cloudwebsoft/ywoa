function switchSide() {
	if($("sideBar").getAttribute("isDisplayed") == 1) {
		$("sideBar").style.display = "none";
		$("outWrapper").className = "";
		$("mainLeft").className = "";
		$("inWrapper").className = "";
		$("sideBar").setAttribute("isDisplayed", 0, 0);
		set_cookie('isSideBar', 'n');
	} else if ($("sideBar").getAttribute("isDisplayed") == 0) {
		$("sideBar").style.display = "";
		$("outWrapper").className = "outWithSide";
		$("mainLeft").className = "mLWithSide";
		$("inWrapper").className = "inWithSide";
		$("sideBar").setAttribute("isDisplayed", 1, 0);
	}
}
function adjustWithoutSide(){
$("outWrapper").className = "";
$("mainLeft").className = "";
$("inWrapper").className = "";
}
function switchMenu(menuName, itemObj) {
	var items = $(menuName).getElementsByTagName("li");
	for(i=0; i<items.length; i++) {
		if(itemObj == items[i]) {
			items[i].className = "currentItem";
			window.status = "i=" + i;
			$(menuName+"Content"+i).style.display="";
		} else {
			items[i].className = "item";
			$(menuName+"Content"+i).style.display="none";
		}
	}
}
function clearSelect(menuName) {
	var items = $(menuName).getElementsByTagName("li");
	for(i=0; i<items.length; i++) {
		items[i].className = "item";
	}
}
function scrollNotice(name, delay, mode) {
	var speed = 50;
	var lineHeight = 32;
	var obj = $(name);
	var scrollMode1 = function() {
		var o = obj.firstChild
		obj.removeChild(o);
		obj.appendChild(o);
	}
	var count = 0;
	var restart = false;
	var scrollMode2 = function() {
		if(count == 0) {
			var offset = obj.scrollTop%lineHeight;
			if(offset != 0) {
				count = offset;
			}
		}
		if(restart) {
			restart = false;
			for(i=0; i<obj.getElementsByTagName("li").length-1; i++) {
				var o = obj.firstChild;
				obj.removeChild(o);
				obj.appendChild(o);
			}
			obj.scrollTop = 0;
			setTimeout(scrollMode2, 0);
			return;
		}
		if(count < lineHeight){
			obj.scrollTop += 1;
			count ++;
			setTimeout(scrollMode2, speed);
		} else {
			if(obj.scrollTop == lineHeight*(obj.getElementsByTagName("li").length-1)) {
				restart = true;
			}
			count = 0;
			setTimeout(scrollMode2, delay);
		}
	}
	switch(mode) {
		case 1:
			setInterval(scrollMode1, delay);
			break;
		case 2:
			setTimeout(scrollMode2, delay);
			break;
		default:
			setInterval(scrollMode1, delay);
	}
}
function scrollUpOrDown(name, direction) {
	var direction = direction.toLowerCase();
	var lineHeight = 32;
	var obj = $(name);
	var offset = obj.scrollTop%lineHeight;
	if(direction == "up") {
		if(obj.scrollTop == lineHeight*(obj.getElementsByTagName("li").length-1)) {
			obj.scrollTop = 0;
		} else {
			obj.scrollTop += (lineHeight - offset);
		}
	}
	if(direction == "down") {
		if(obj.scrollTop == 0){
			obj.scrollTop = lineHeight*(obj.getElementsByTagName("li").length-1);
		} else {
			obj.scrollTop -= (lineHeight + offset);
		}
	}
}
