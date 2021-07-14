;(function ($, window, document) {
	/**
	  opt: {
		linkNum: 5,		// 中间按钮个数 		默认5
		current: 1,		// 页面初始当前页 	默认1
		size: 10,		// 每页显示的条数 	默认10
		layout: 'total, totalPage, sizes, prev, pager, next, jumper',	// 设置显示的内容		// 默认'total, prev, pager, next, jumper'
		prevHtml: '&lt;',	// 上一页html	默认&lt;
		nextHtml: '&gt;',	// 下一页html	默认&gt;
		jump: fn 		// 跳转时执行方法 	必须
	  }
	  jump方法中获取当前页数this.current，获取显示条数this.current
	  jump中必须调用this.setTotal(100)方法设置总页数
	 */
	function MyPaging (el, opt) {

		this.oPagingParent = $(el);			// 初始化分页的盒子
		this.total =  0;					// 总条数
		this.totalPage =  0;				// 总页数

		this.linkNum = opt.linkNum || 5;	// 中间按钮个数
		this.current = opt.current || 1;	// 当前页
		this.size = opt.size || 20;			// 每页多少条
		this.sizes = opt.sizes || [5, 10, 20, 50, 100, 200]; // 每页多少条
		this.prevHtml = opt.prevHtml || '&lt;';	// 上一页html
		this.nextHtml = opt.nextHtml || '&gt;';	// 下一页html

		this.layout = ['total', 'prev', 'pager', 'next', 'jumper'];
		if (opt.layout) {
			this.layout = opt.layout.split(',');
		}

		if (!opt.jump) {
			return;
		}
		this.jump = opt.jump;

		this._init();
	}
	var prototype = {
		_init: function () {
			this.jump();
		},

		// 跳转指定页
		setCurrent: function (data) {
			if (data > 0 && data <= this.totalPage) {
				this.current = data;
			} else {
				this.current = 1;
			}
			this.jump();
		},

		// 设置总页数方法 调用设置html方法
		setTotal: function (data) {
			if (data >= 0) {
				this.total = data;
				this.totalPage = Math.ceil(this.total / this.size);
				
				this._setPagingHtml();
			}
		},

		// 设置html
		_setPagingHtml: function () {
			var html = '<div class="_my-paging-box">';
			if (this.totalPage > 0) {
				for (var iKey = 0; iKey < this.layout.length; iKey++) {
					var key = this.layout[iKey].replace(/\s/g, '');

					// 总条数
					if (key == 'total') {
						html += '<div class="total pg-item">共<span>' + this.total + '</span>条</div>';
					}

					// 总页数
					if (key == 'totalPage') {
						html += '<div class="total-page pg-item"><span>' + this.totalPage + '</span>页</div>'
					}

					// 每页显示多少条
					if (key == 'sizes') {
						html += '<select class="sizes pg-item">';
						for (var i = 0; i < this.sizes.length; i++) {
							html += '<option value="' + this.sizes[i] + '"' + (this.size == this.sizes[i] ? ' selected' : '') + '>' + this.sizes[i] + '条/页</option>';
						}
						html += '</select>';
					}

					// 上一条
					if (key == 'prev') {
						html += '<div class="link-btn prev pg-item' + (this.current == 1 ? ' disabled' : '') + '" data-current="prev">' + this.prevHtml + '</div>';
					}

					// 分页按钮
					if (key == 'pager') {
						html += '<ul class="link-list pg-item">';

						
						var start = end = 0;
						var sPager = ''
						// 总页数小于按钮个数
						if (this.totalPage <= this.linkNum) {
							start = 1;
							end = this.totalPage;
							for (var i = 1; i <= this.totalPage; i++) {
								sPager += '<li class="link-btn' + (this.current == i ? ' active' : '') + '" data-current="' + i + '">' + i + '</li>';
							}

						// 当前页小于2分之最大按钮数
						} else if (this.current < Math.ceil(this.linkNum / 2)) {
							start = 1;
							end = this.linkNum;
							for (var i = 1; i <= this.linkNum; i++) {
								sPager += '<li class="link-btn' + (this.current == i ? ' active' : '') + '" data-current="' + i + '">' + i  + '</li>';
							}

						// 当前页大于总条数减2分之最大按钮数
						} else if (this.current > this.totalPage - Math.ceil(this.linkNum / 2)) {
							start = this.totalPage - this.linkNum + 1;
							end = this.totalPage;
							for (var i = this.totalPage - this.linkNum + 1; i <= this.totalPage; i++) {
								sPager += '<li class="link-btn' + (this.current == i ? ' active' : '') + '" data-current="' + i + '">' + i  + '</li>';
							}

						// 其它
						} else {
							start = this.current - Math.ceil(this.linkNum / 2) + 1;
							end = this.current - Math.ceil(this.linkNum / 2) + this.linkNum;
							for (var i = 1; i <= this.linkNum; i++) {
								var idx = this.current - Math.ceil(this.linkNum / 2) + i;
								sPager += '<li class="link-btn' + (this.current == idx ? ' active' : '') + '" data-current="' + idx + '">' + idx  + '</li>';
							}
						}

						// 当前页大于按钮页一般及总页数大于按钮数
						if (this.current > Math.ceil(this.linkNum / 2) && this.totalPage > this.linkNum) {
							html += '<li class="link-btn" data-current="1">1</li>';
							if (start > 2) {
								html += '<li>···</li>';
							}
						}

						html += sPager;

						// 当前页小于按钮数一般并且总页数大于按钮数
						if (this.current <= this.totalPage - Math.ceil(this.linkNum / 2) && this.totalPage > this.linkNum) {
							if (end < this.totalPage - 1) {
								html += '<li>···</li>';
							}
							html += '<li class="link-btn" data-current="' + this.totalPage + '">' + this.totalPage + '</li>';
						}

						html += '</ul>';
					}

					// 下一条
					if (key == 'next') {
						html += '<div class="link-btn next pg-item' + (this.current == this.totalPage ? ' disabled' : '') + '" data-current="next">' + this.nextHtml + '</div>';
					}

					// 跳输入框
					if (key == 'jumper') {
						html += '<div class="jumper pg-item"><span>前往</span><input type="text"><span>页</span></div>';
					}
				}
			}
			html += '</div>';

			this.oPagingParent.html(html);
			this._setPagingEvent();//设置事件
		},

		// 设置分页事件
		_setPagingEvent: function () {
			var _this = this;
			var oMyPaging = this.oPagingParent.find('._my-paging-box'); // 分页盒子元素
			var oSizes = oMyPaging.find('.sizes'); // 每页多少条下拉框
			var oLinkBtn = oMyPaging.find('.link-btn'); // 页码按钮
			var oIpt = oMyPaging.find('.jumper input'); // 跳转输入框

			// 每页显示条数改变
			oSizes.on('change', function () {
				_this.size = $(this).val();
				_this.setCurrent(1);
			})

			// 按钮点击事件
			oLinkBtn.on('click', function () {
				var oTag = $(this);
				var current = oTag.data('current');
				var to = _this.current;

				if (current == 'prev') {
					to = to > 1 ? to - 1 : 1;
				} else if (current == 'next') {
					to = to <  _this.totalPage ? to + 1 : _this.totalPage;
				} else {
					to = current;
				}

				if (to == _this.current) {
					return;
				}

				_this.current = to;
				_this.jump();
			})

			// 输入框回车事件
			oIpt.on('keydown', function (event) {
				var code = event.keyCode;

				if (code == 13) {
					var to = $(this).val();

					if (!(to >= 1 && to <= _this.totalPage)) {
						to = 1;
					}

					_this.current = to;
					_this.jump();
				}
			})
		},
	}
	for (var i in prototype) {
		MyPaging.prototype[i] = prototype[i];
	}

	window.MyPaging = MyPaging;
})(jQuery, window, document);