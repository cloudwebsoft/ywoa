var DragUtil = new Object();
// 获得浏览器信息
DragUtil.getUserAgent = navigator.userAgent;
DragUtil.isGecko = DragUtil.getUserAgent.indexOf("Gecko") != -1;
DragUtil.isOpera = DragUtil.getUserAgent.indexOf("Opera") != -1;
// 计算每个可拖拽的元素的坐标
DragUtil.reCalculate = function(el) {
	for( var i = 0 ; i < DragUtil.dragArray.length; i++ ) {
		var ele = DragUtil.dragArray[i];
		var position = Position.positionedOffset(ele.elm);
		ele.elm.pagePosLeft = position[0];
		ele.elm.pagePosTop = position[1];
	}
};
// 拖动元素时显示的占位框
DragUtil.ghostElement = null;
DragUtil.getGhostElement = function(){
	if(!DragUtil.ghostElement){
		DragUtil.ghostElement = document.createElement("DIV");
		DragUtil.ghostElement.className = "modbox";
		DragUtil.ghostElement.style.border = "2px dashed #aaa";
		DragUtil.ghostElement.innerHTML = "&nbsp;";
	}
	return DragUtil.ghostElement;
};

// 修改顶部元素的位置
DragUtil.moveTopBlock = function(){
	var topBlock = $('col_0');
	var childs = document.getElementsByClassName('drag_div' , 'col_0');
	if(childs.length >= 8){
		for(var i = (childs.length - 1) ; i > 0 ; i--){
			var tmpElementId = childs[i].id;
			var col_1_childs = document.getElementsByClassName('drag_div' , 'col_1');
			var tmpColfirstChildId = col_1_childs[0].id;
			$('col_1').insertBefore(childs[i] ,  $(tmpColfirstChildId));
		}
	} else if(childs.length == 0){
		var childs = document.getElementsByClassName('drag_div' , 'col_1');
		if(childs.length == 1){
			childs = document.getElementsByClassName('drag_div' , 'col_2');
		}
		if(childs.length == 1){
			childs = document.getElementsByClassName('drag_div' , 'col_3')
		}
		if(childs.length == 1){
			return false;
		}
		$('col_0').appendChild(childs[0]);
	}
	
	// 调整，使每列至少必须有一个元素，否则将无法拖入元素进去
	childs = document.getElementsByClassName('drag_div' , 'col_2');
	if(childs.length == 0){
		var childs = document.getElementsByClassName('drag_div' , 'col_1');
		if(childs.length == 1){
			childs = document.getElementsByClassName('drag_div' , 'col_0');
		}
		if(childs.length == 1){
			childs = document.getElementsByClassName('drag_div' , 'col_3')
		}
		if(childs.length == 1){
			return false;
		}
		$('col_2').appendChild(childs[0]);
	}
	
	childs = document.getElementsByClassName('drag_div' , 'col_1');
	if(childs.length == 0){
		var childs = document.getElementsByClassName('drag_div' , 'col_2');
		if(childs.length == 1){
			childs = document.getElementsByClassName('drag_div' , 'col_0');
		}
		if(childs.length == 1){
			childs = document.getElementsByClassName('drag_div' , 'col_3')
		}
		if(childs.length == 1){
			return false;
		}
		$('col_1').appendChild(childs[0]);
	}	
	
	return true;
}

DragUtil.getSortIndex = function(){
	var col_array = ['col_0' , 'col_1' , 'col_2' , 'col_3'];
	//alert(col_array.toString());
	var sortIndex = '';
	for(var i = 0; i < col_array.length ; i++){
		sortIndex += col_array[i] + ":";
		var childs = document.getElementsByClassName('drag_div' , col_array[i]);
		for(var j = 0 ; j < childs.length ; j++){
			if(!Element.hasClassName(childs[j] , 'no_drag')){
				sortIndex += childs[j].id + ',';
			}
		}
		sortIndex += "\n";
	}
	return sortIndex;
}

DragUtil.getSortIndexOfCol = function(colId){
	var sortIndex = '';
	var childs = document.getElementsByClassName('drag_div' , colId);
	for(var j = 0 ; j < childs.length ; j++){
		if(!Element.hasClassName(childs[j] , 'no_drag')){
			if (sortIndex=='')
				sortIndex = childs[j].id;
			else
				sortIndex += "," + childs[j].id;
		}
	}
	return sortIndex;
}

// 初始化所有可拖拽的元素，依靠 className 来确定是否可拖拽，可拖拽的部分的 id 为该元素 id 加上 _h
var initDrag = function() {
	var tmpElements = document.getElementsByClassName('drag_div');
	
	if (DragUtil.dragArray == null)
	{
	   DragUtil.dragArray = new Array();
	}
	
	for(var i = 0 ; i < tmpElements.length ; i++){
		var tmpElement = tmpElements[i];
		var tmpElementId = tmpElement.id;
		var tmpHeaderElementId = tmpElement.id + '_h';
		DragUtil.dragArray[i] = new DragDrop(tmpHeaderElementId, tmpElementId);
	}
};

// 开始拖拽
function start_Drag(){
	DragUtil.reCalculate(this);	// 重新计算所有可拖拽元素的位置
	this.origNextSibling = this.elm.nextSibling;
	var _ghostElement = DragUtil.getGhostElement();
	var offH = this.elm.offsetHeight;
	if(DragUtil.isGecko){	// 修正 Gecko 引擎的怪癖
		offH -= parseInt(_ghostElement.style.borderTopWidth)  *   2 ;
	}
	var offW = this.elm.offsetWidth;
	var position = Position.positionedOffset(this.elm);
	var offLeft = position[0];
	var offTop = position[1];
	// 在元素的前面插入占位虚线框
	_ghostElement.style.height = offH + "px";
	this.elm.parentNode.insertBefore(_ghostElement,  this.elm.nextSibling);
	// 设置元素样式属性
	this.elm.style.width = offW + "px";
	this.elm.style.position = "absolute";
	this.elm.style.zIndex = 100;
	this.elm.style.left = offLeft + "px";
	this.elm.style.top = offTop + "px";
	this.isDragging = false;
	return false;
}
// 拖动时触发这个函数（每次鼠标坐标变化时）
function when_Drag(clientX , clientY){
	if (!this.isDragging){	// 第一次移动鼠标，设置它的样式
		this.elm.style.filter = "alpha(opacity=70)";
		this.elm.style.opacity = 0.7;
		this.isDragging = true;
	}
	// 计算离当前鼠标位置最近的可拖拽的元素，把该元素放到 found 变量中
	var found = null;
	var max_distance = 100000000;
	for(var i = 0 ; i < DragUtil.dragArray.length; i++) {
		var ele = DragUtil.dragArray[i];
		var distance = Math.sqrt(Math.pow(clientX - ele.elm.pagePosLeft, 2 ) + Math.pow(clientY - ele.elm.pagePosTop, 2 ));
		if(ele == this ) {
			continue;
		}
		if(isNaN(distance)){
			continue;
		}
		if(distance < max_distance){
			max_distance = distance;
			found = ele;
		}
	}
	// 把虚线框插到 found 元素的前面
	var _ghostElement = DragUtil.getGhostElement();
	if(found != null && _ghostElement.nextSibling != found.elm) {
		found.elm.parentNode.insertBefore(_ghostElement, found.elm);
		if(DragUtil.isOpera){	// Opera 的毛病
			document.body.style.display = "none";
			document.body.style.display = "";
		}
	}
}

// 结束拖拽
function end_Drag(){
	if(this._afterDrag()){
		// 在这可以做一些拖拽成功后的事，比如 Ajax 通知服务器端修改坐标，以便下次用户进来时位置不变
		if (DragUtil.onAfterDrag) {
			DragUtil.onAfterDrag();
		}
	}
	DragUtil.moveTopBlock();
	return true;
}
// 结束拖拽时调用的函数
function after_Drag(){
	var returnValue = false;
	// 把拖动的元素的样式回复到原来的状态
	this.elm.style.position = "";
	this.elm.style.top = "";
	this.elm.style.left = "";
	this.elm.style.width = "";
	this.elm.style.zIndex = "";
	this.elm.style.filter = "";
	this.elm.style.opacity = "";
	// 在虚线框的地方插入拖动的这个元素
	var ele = DragUtil.getGhostElement();
	if(ele.nextSibling != this.origNextSibling) {
		ele.parentNode.insertBefore( this.elm, ele.nextSibling);
		returnValue = true;
	}
	// 删除虚线框
	ele.parentNode.removeChild(ele);
	if(DragUtil.isOpera) {
		document.body.style.display = "none";
		document.body.style.display = "" ;
	}
	return returnValue;
}
