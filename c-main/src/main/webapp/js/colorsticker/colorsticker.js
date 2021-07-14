;(function($, window, document, undefined){
	var counter = 0;
	var line = 0;
	var newId = -100;
	var initPosition = 100;
	var hasMovingSticker = false;
	var defaults = {
		width: '200px',
		height: '200px',
		color : '',
		dropStickerCallback : null,
		saveStickerCallback : null,
		shareStickerCallback : null,
		closeStickerCallback : null
 	}
	
	function Sticker(element,options,stickers){
		this.element = $(element);
		this.options = $.extend({}, defaults, options);
		this.init(stickers);
	}
	Sticker.prototype = {
		init : function(existStickers){
			this._createAddingBtn();
			if(existStickers && existStickers.length>0){
				this.loadExistStickers(existStickers);
			}
		},
		loadExistStickers : function(existStickers){
			for(var i=0;i<existStickers.length;i++){
				var stickerEl = document.createElement('div');
				stickerEl.className = 'sticker ' + this.options.color;
				stickerEl.style.width = this.options.width;
				stickerEl.style.height = this.options.height;
				stickerEl.style.left =  existStickers[i].left;
				stickerEl.style.top = existStickers[i].top;
				stickerEl.setAttribute('stickerId',existStickers[i].stickerId);
				stickerEl.setAttribute("shared", existStickers[i].shared);
				var startTime = existStickers[i].startTime;
				var tape = document.createElement('div');
				tape.className = 'tape';
				var dateInput = $("<input class='date-input' readonly unselectable='on' value='" + startTime +"'>");
				var stickerTA = document.createElement('textarea');
				var closeBtn = $('<button type="button" class="close-btn" title="关闭">×</button>');
				var saveBtn = $('<button type="button" class="save-btn" title="保存"></button>');
				var cls, t;
				if (existStickers[i].shared) {
					cls = "share-btn";
					t = "取消共享";
				}
				else {
					cls = "unshare-btn";
					t = "共享";
				}
				var shareBtn = $('<button type="button" id="btnShare" class="' + cls + '" title="' + t + '"></button>');
				stickerEl.appendChild(closeBtn[0]);
				stickerEl.appendChild(saveBtn[0]);
				stickerEl.appendChild(shareBtn[0]);
				stickerEl.appendChild(tape);
				stickerEl.appendChild(dateInput[0]);
				stickerEl.appendChild(stickerTA);
				document.getElementsByTagName('body')[0].appendChild(stickerEl);
				stickerEl.lastElementChild.value = existStickers[i].content;
				$(stickerEl.childNodes[0]).on('click',this,this.closeSticker);
				$(stickerEl.childNodes[1]).on('click',this,this.saveSticker);
				$(stickerEl.childNodes[2]).on('click',this,this.shareSticker);
				$(stickerEl).on('mousedown', this._dragSticker);
				$(window).on('mousemove', this._moveSticker);
				$(window).on('mouseup', this, this._dropSticker);
			}
			
			$('.date-input').datetimepicker({
		  		lang:'ch',
		  	  	step:5, 
		  	  	format:'Y-m-d H:i'
		  	});			
		},
		_createAddingBtn : function(){
			var addingBtn = document.createElement('div');
			addingBtn.className = 'sticker-adding-btn';
			addingBtn.appendChild(document.createTextNode('+'));
			document.getElementsByTagName('body')[0].appendChild(addingBtn);
			$(addingBtn).on('click', this, this._createSticker);
		},
		_createSticker : function(event){
			var Sticker = event.data;
			var stickerEl = document.createElement('div');
			var stickerWidth = Number(Sticker.options.width.replace('px',''));
			var stickerHeight = Number(Sticker.options.height.replace('px',''));
			stickerEl.className = 'sticker ' + Sticker.options.color;
			if((document.body.clientWidth + document.body.scrollLeft) - counter*(stickerWidth+40) < (stickerWidth+40)){
				counter = 0;
				line++;
			} 
			stickerEl.style.width = Sticker.options.width;
			stickerEl.style.height = Sticker.options.height;
			stickerEl.style.left = initPosition + counter*(stickerWidth+40) + 'px';
			stickerEl.style.top = initPosition + line*(stickerHeight+40) + 'px';
			stickerEl.setAttribute("stickerId", newId);
			newId--;
			var tape = document.createElement('div');
			tape.className = 'tape';
			var stickerTA = document.createElement('textarea');
			var closeBtn = $('<button type="button" class="close-btn" title="关闭">×</button>');
			var saveBtn = $('<button type="button" class="save-btn" title="保存"></button>');
			// 保存后才会显示共享按钮
			var shareBtn = $('<button type="button" id="btnShare" class="unshare-btn" title="共享" style="display:none"></button>');
			var d = new Date();
			var startTime = d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate() + " " + d.getHours() + ":" + d.getMinutes();
			var dateInput = $("<input class='date-input' readonly unselectable='on' value='" + startTime +"'>");			
			stickerEl.appendChild(closeBtn[0]);
			stickerEl.appendChild(saveBtn[0]);
			stickerEl.appendChild(shareBtn[0]);
			stickerEl.appendChild(tape);
			stickerEl.appendChild(dateInput[0]);			
			stickerEl.appendChild(stickerTA);
			document.getElementsByTagName('body')[0].appendChild(stickerEl);
			$(stickerEl.childNodes[0]).on('click',Sticker,Sticker.closeSticker);
			$(stickerEl.childNodes[1]).on('click',Sticker,Sticker.saveSticker);
			$(stickerEl.childNodes[2]).on('click',Sticker,Sticker.shareSticker);
			counter++;
			$(stickerEl).on('mousedown', Sticker._dragSticker);
			$(window).on('mousemove', Sticker._moveSticker);
			$(window).on('mouseup', Sticker._dropSticker);
		},
		_dragSticker : function(event){
			if(event.target.className.indexOf('tape') != -1 && !this.moving) {
				this.clientX = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
				this.clientY = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop); 
				// this.style.left = this.clientX + 'px';
				// this.style.top = this.clientY + 'px';
				this.moving = true;
				this.style.cursor = 'pointer';
				hasMovingSticker = true;
			}
		},
		_moveSticker : function(event){
			if(hasMovingSticker){
				var stickers = $('.sticker');
				for(var i=0;i<stickers.length;i++){
					if(stickers[i].moving){
						event.preventDefault();
						var newClientX = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft),
						newClientY = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
						var left = parseInt(stickers[i].style.left) || 0;
						var top = parseInt(stickers[i].style.top) || 0;
						stickers[i].style.left = left + (newClientX - stickers[i].clientX) + 'px';
						stickers[i].style.top = top + (newClientY - stickers[i].clientY) + 'px';
						stickers[i].clientX = newClientX;
						stickers[i].clientY = newClientY;
					}
				}
			}
		},
		_dropSticker : function(event){
			if(hasMovingSticker){
				var stickers = $('.sticker');
				var Sticker = event.data;
				for(var i=0;i<stickers.length;i++){
					if(stickers[i].moving){
						stickers[i].style.left = stickers[i].clientX = event.clientX;  
						stickers[i].style.top = stickers[i].clientY = event.clientY; 
						stickers[i].style.cursor = 'none';
						stickers[i].moving = false;
						
						var stickerItem = {
							left : stickers[i].style.left,
							top : stickers[i].style.top,
							id : stickers[i].getAttribute('stickerId')
						} 						
						
						if($.isFunction(Sticker.options.dropStickerCallback)) {
							Sticker.options.dropStickerCallback.call(this, stickerItem);
						}
					}
				}
			}
		},
		closeSticker : function(event){
			var Sticker = event.data;
			if($.isFunction(Sticker.options.closeStickerCallback)) {
				Sticker.options.closeStickerCallback.call(this, this.parentElement.getAttribute('stickerId'));
			}
			// $(this.parentElement).remove();
		},
		saveSticker : function(event){
			var Sticker = event.data;
			var stickerEl = this.parentElement;
			if($.isFunction(Sticker.options.saveStickerCallback)) {
							var stickerItem = {
								left : stickerEl.style.left,
								top : stickerEl.style.top,
								id : stickerEl.getAttribute('stickerId'),
								startTime : $(this.parentElement).find('.date-input').val(),
								content : this.parentElement.lastElementChild.value
							} 
							Sticker.options.saveStickerCallback.call(this, stickerItem);
						}
		},
		shareSticker : function(event){
			var Sticker = event.data;
			var stickerEl = this.parentElement;
			if($.isFunction(Sticker.options.shareStickerCallback)) {
				var shared = stickerEl.getAttribute("shared");
				var stickerItem = {
					id : stickerEl.getAttribute('stickerId')
				} 
				Sticker.options.shareStickerCallback.call(this, stickerItem);
			}
		}		
	}

	$.fn.sticker = function(options, stickers){
		new Sticker(this,options, stickers);
	}

})(window.jQuery, window, document);