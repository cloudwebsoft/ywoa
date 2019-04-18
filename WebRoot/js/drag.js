//http://boring.youngpup.net/2001/domdrag/

/**
 * Base class of Drag
 * 可拖拽 Element 的原形，用来将 event 绑定到各个钩子，这部分市比较通用的，netvibes 也是基本完全相同的实现，这部分推荐看 dindin 的这个，也会帮助理解，http://www.jroller.com/page/dindin/?anchor=pro_javascript_12
 * @example:
 * Drag.init( header_element, element );
 */
var Drag = {
	// 对这个element的引用，一次只能拖拽一个Element
	obj: null , 
	/**
	 * @param: elementHeader	used to drag..
	 * @param: element			used to follow..
	 */
	init: function(elementHeader, element) {
		var headerChildren = elementHeader.children;
		if (headerChildren.length>0) {
			for (var i=0; i<headerChildren.length; i++) {
				var e = headerChildren[i];
				if (e.tagName=="A") {
					e.onmousedown = function() {window.event.cancelBubble = true;}
				}
			}
		}
		// 将 start 绑定到 onmousedown 事件，按下鼠标触发 start
		elementHeader.onmousedown = Drag.start;
		// 将 element 存到 header 的 obj 里面，方便 header 拖拽的时候引用
		elementHeader.obj = element;
		// 初始化绝对的坐标，因为不是 position = absolute 所以不会起什么作用，但是防止后面 onDrag 的时候 parse 出错了
		if(isNaN(parseInt(element.style.left))) {
			element.style.left = "0px";
		}
		if(isNaN(parseInt(element.style.top))) {
			element.style.top = "0px";
		}
		// 挂上空 Function，初始化这几个成员，在 Drag.init 被调用后才帮定到实际的函数
		element.onDragStart = new Function();
		element.onDragEnd = new Function();
		element.onDrag = new Function();
	},
	// 开始拖拽的绑定，绑定到鼠标的移动的 event 上
	start: function(event) {
		var element = Drag.obj = this.obj;
				
		// 解决不同浏览器的 event 模型不同的问题
		event = Drag.fixE(event);
		// 看看是不是左键点击
		if(event.which != 1){
			// 除了左键都不起作用
			return true ;
		}
		// 参照这个函数的解释，挂上开始拖拽的钩子
		element.onDragStart();
		// 记录鼠标坐标
		element.lastMouseX = event.clientX;
		element.lastMouseY = event.clientY;
		// 绑定事件
		document.onmouseup = Drag.end;
		document.onmousemove = Drag.drag;
		return false ;
	}, 
	// Element正在被拖动的函数
	drag: function(event) {		
		event = Drag.fixE(event);
		if(event.which == 0 ) {
		 	return Drag.end();
		}
		// 正在被拖动的Element
		var element = Drag.obj;
		// 鼠标坐标
		var _clientX = event.clientY;
		var _clientY = event.clientX;
		// 如果鼠标没动就什么都不作
		if(element.lastMouseX == _clientY && element.lastMouseY == _clientX) {
			return	false ;
		}
		// 刚才 Element 的坐标
		var _lastX = parseInt(element.style.top);
		var _lastY = parseInt(element.style.left);
		// 新的坐标
		var newX, newY;
		// 计算新的坐标：原先的坐标+鼠标移动的值差
		newX = _lastY + _clientY - element.lastMouseX;
		newY = _lastX + _clientX - element.lastMouseY;
		// 修改 element 的显示坐标
		element.style.left = newX + "px";
		element.style.top = newY + "px";
		// 记录 element 现在的坐标供下一次移动使用
		element.lastMouseX = _clientY;
		element.lastMouseY = _clientX;
		// 参照这个函数的解释，挂接上 Drag 时的钩子
		element.onDrag(newX, newY);
		return false;
	},
	// Element 正在被释放的函数，停止拖拽
	end: function(event) {
		event = Drag.fixE(event);
		// 解除事件绑定
		document.onmousemove = null;
		document.onmouseup = null;
		// 先记录下 onDragEnd 的钩子，好移除 obj
		var _onDragEndFuc = Drag.obj.onDragEnd();
		// 拖拽完毕，obj 清空
		Drag.obj = null ;
		return _onDragEndFuc;
	},
	// 解决不同浏览器的 event 模型不同的问题
	fixE: function(ig_) {
		if( typeof ig_ == "undefined" ) {
			ig_ = window.event;
		}
		if( typeof ig_.layerX == "undefined" ) {
			ig_.layerX = ig_.offsetX;
		}
		if( typeof ig_.layerY == "undefined" ) {
			ig_.layerY = ig_.offsetY;
		}
		if( typeof ig_.which == "undefined" ) {
			ig_.which = ig_.button;
		}
		return ig_;
	}
};

var DragDrop = Class.create();
DragDrop.prototype = {
	initialize: function(elementHeader_id , element_id){
		var element = document.getElementById(element_id);
		var elementHeader = document.getElementById(elementHeader_id);
		if(elementHeader == null)
		{
		  return;
		}
		this._dragStart = ((typeof this.start_Drag == "function") ? this.start_Drag : start_Drag);
		this._drag = ((typeof this.when_Drag == "function") ? this.when_Drag : when_Drag);
		this._dragEnd = ((typeof this.end_Drag == "function") ? this.end_Drag : end_Drag);
		this._afterDrag = ((typeof this.after_Drag == "function") ? this.after_Drag : after_Drag);
		this.isDragging = false;
		this.elm = element;
		this.header = $(elementHeader.id);
		this.hasIFrame = this.elm.getElementsByTagName("IFRAME").length > 0;
		if( this.header) {
			this.header.style.cursor = "move";
			Drag.init( this.header, this.elm);
			this.elm.onDragStart = this._dragStart.bind(this);
			this.elm.onDrag = this._drag.bind(this);
			this.elm.onDragEnd = this._dragEnd.bind(this);
		}
	}
};

/**
 * four function for Drag..
 */
function start_Drag(){
	var position = Position.positionedOffset(this.elm);
	var offLeft = position[0];
	var offTop = position[1];
	var offW = this.elm.offsetWidth;
	this.elm.style.width = offW + "px";
	this.elm.style.position = "absolute";
	this.elm.style.zIndex = 100;
	this.elm.style.left = offLeft + "px";
	this.elm.style.top = offTop + "px";
	this.isDragging = false;
	return false 
}

function when_Drag(clientX , clientY){
}

function end_Drag(){
	if(this._afterDrag()){
	}
	return true;
}

function after_Drag(){
}