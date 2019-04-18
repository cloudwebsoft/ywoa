/**
 * IndexedList
 * 类似联系人应用中的联系人列表，可以按首字母分组
 * 右侧的字母定位工具条，可以快速定位列表位置
 * varstion 1.0.0
 * by Houfeng
 * Houfeng@DCloud.io
 **/

(function($, window, document) {
	var classSelector = function(name) {
		return '.' + $.className(name);
	}

	var IndexedList = $.IndexedList = $.Class.extend({
		/**
		 * 通过 element 和 options 构造 IndexedList 实例
		 **/
		init: function(holder, options) {
			var self = this;
			self.options = options || {};
			self.box = holder;
			if (!self.box) {
				throw "实例 IndexedList 时需要指定 element";
			}
			self.createDom();
			self.findElements();
			self.caleLayout();
			self.bindEvent();
		},
		createDom: function() {
			var self = this;
			self.el = self.el || {};
			//styleForSearch 用于搜索，此方式能在数据较多时获取很好的性能
			self.el.styleForSearch = document.createElement('style');
			(document.head || document.body).appendChild(self.el.styleForSearch);
		},
		findElements: function() {
			var self = this;
			self.el = self.el || {};
			self.el.search = self.box.querySelector(classSelector('indexed-list-search'));
			self.el.searchInput = self.box.querySelector(classSelector('indexed-list-search-input'));
			self.el.searchClear = self.box.querySelector(classSelector('indexed-list-search') + ' ' + classSelector('icon-clear'));
			self.el.bar = self.box.querySelector(classSelector('indexed-list-bar'));
			self.el.barItems = [].slice.call(self.box.querySelectorAll(classSelector('indexed-list-bar') + ' a'));
			self.el.inner = self.box.querySelector(classSelector('indexed-list-inner'));
			self.el.items = [].slice.call(self.box.querySelectorAll(classSelector('indexed-list-item')));
			self.el.liArray = [].slice.call(self.box.querySelectorAll(classSelector('indexed-list-inner') + ' li'));
			self.el.alert = self.box.querySelector(classSelector('indexed-list-alert'));
		},
		caleLayout: function() {
			var self = this;
			var withoutSearchHeight = (self.box.offsetHeight - self.el.search.offsetHeight) + 'px';
			self.el.bar.style.height = withoutSearchHeight;
			self.el.inner.style.height = withoutSearchHeight;
			var barItemHeight = ((self.el.bar.offsetHeight - 40) / self.el.barItems.length) + 'px';
			self.el.barItems.forEach(function(item) {
				item.style.height = barItemHeight;
				item.style.lineHeight = barItemHeight;
			});
		},
		scrollTo: function(group) {
			var self = this;
			var groupElement = self.el.inner.querySelector('[data-group="' + group + '"]');
			if (!groupElement || (self.hiddenGroups && self.hiddenGroups.indexOf(groupElement) > -1)) {
				return;
			}
			self.el.inner.scrollTop = groupElement.offsetTop;
		},
		bindBarEvent: function() {
			var self = this;
			var pointElement = null;
			var findStart = function(event) {
				if (pointElement) {
					pointElement.classList.remove('active');
					pointElement = null;
				}
				self.el.bar.classList.add('active');
				var point = event.changedTouches ? event.changedTouches[0] : event;
				pointElement = document.elementFromPoint(point.pageX, point.pageY);
				if (pointElement) {
					var group = pointElement.innerText;
					if (group && group.length == 1) {
						pointElement.classList.add('active');
						self.el.alert.innerText = group;
						self.el.alert.classList.add('active');
						self.scrollTo(group);
					}
				}
				event.preventDefault();
			};
			var findEnd = function(event) {
				self.el.alert.classList.remove('active');
				self.el.bar.classList.remove('active');
				if (pointElement) {
					pointElement.classList.remove('active');
					pointElement = null;
				}
			};
			self.el.bar.addEventListener($.EVENT_MOVE, function(event) {
				findStart(event);
			}, false);
			self.el.bar.addEventListener($.EVENT_START, function(event) {
				findStart(event);
			}, false);
			document.body.addEventListener($.EVENT_END, function(event) {
				findEnd(event);
			}, false);
			document.body.addEventListener($.EVENT_CANCEL, function(event) {
				findEnd(event);
			}, false);
		},
		search: function(keyword) {
			var self = this;
			keyword = (keyword || '').toLowerCase();
			var selectorBuffer = [];
			var groupIndex = -1;
			var itemCount = 0;
			var liArray = self.el.liArray;
			var itemTotal = liArray.length;
			self.hiddenGroups = [];

			var itemFound = [];

			var checkGroup = function(currentIndex, last) {
				if (itemCount >= currentIndex - groupIndex - (last ? 0 : 1)) {
					selectorBuffer.push(classSelector('indexed-list-inner li') + ':nth-child(' + (groupIndex + 1) + ')');
					// self.hiddenGroups.push(liArray[groupIndex]);
				};
				groupIndex = currentIndex;
				itemCount = 0;
			}
			var isFound = false;
			if (keyword!="") {
				liArray.forEach(function(item) {
					var currentIndex = liArray.indexOf(item);
					if (item.classList.contains($.className('indexed-list-group'))) {
						// checkGroup(currentIndex, false);
					} else {
						var text = (item.innerText || '').toLowerCase();
						var value = (item.getAttribute('data-value') || '').toLowerCase();
						var tags = (item.getAttribute('data-tags') || '').toLowerCase();
						// 如果未找到，则置入待隐藏数组selectorBuffer
						if (keyword && text.indexOf(keyword) < 0 &&
							value.indexOf(keyword) < 0 &&
							tags.indexOf(keyword) < 0) {
/*							selectorBuffer.push(classSelector('indexed-list-inner li') + ':nth-child(' + (currentIndex + 1) + ')');
							itemCount++;*/
						}
						else {
							// 如果找到，则置入itemFound
							isFound = true;
							var uName = jQuery(item).attr('uName');
							var gender = jQuery(item).attr('gender');
							var imgSrc = gender == '1'?"../images/user_46_03.png":"../images/user_46_01.png";
							itemFound.push('<li class="mui-indexed-list-item mui-checkbox mui-left" style="clear:both; height:50px; padding-top:10px; border-bottom: 1px solid #ccc">'
								+ '<input type="checkbox" value="' + uName + '"/>'
								+ '<img class="mui-media-object mui-pull-left" style="margin-left: 60px" src="' + imgSrc + '">'
								+ '<div class="mui-media-body">' + text + '</div>'
								+ '</li>');
							// console.log(text + "--" + value + "--" + tags);
						}
						if (currentIndex >= itemTotal - 1) {
							// checkGroup(currentIndex, true);
						}
					}
				});
			}

			// 清除原来可能已生成的搜索结果，以免造成出现多个查询结果
			jQuery('#boxFound').remove();

			if (!isFound) { // 未找到
				self.el.inner.classList.remove('empty');
				jQuery('.mui-indexed-list-bar').show();
				jQuery('#boxFound').remove();
			} else {
				var itemFoundHtml = itemFound.join("");
				var divHtml = '<ul id="boxFound" class="mui-table-view" style="overflow-y: auto">';
				divHtml += itemFoundHtml;
				divHtml += '<li class="box-search" style="margin-top: 10px; text-align: center"><button class="mui-btn mui-btn-link mui-btn-blue btn-end-search">退出查询</button></li>';
				divHtml += '</ul>';

				jQuery('.mui-indexed-list-bar').hide(); // 隐藏右侧A...Z导航条
				jQuery('.mui-indexed-list-inner').before(divHtml); // 将搜索结果插入人员列表至之前
				jQuery('#boxFound').height(jQuery('.mui-indexed-list-inner').height());// 设置高度，使之配合overflow-y:auto可以滚动
				self.el.inner.classList.add('empty'); // 隐藏人员列表

				mui('.box-search').on('tap', '.btn-end-search', function() {
					// 退出查询
					jQuery('.mui-indexed-list-search-input').val('');
					self.el.inner.classList.remove('empty');
					jQuery('.mui-indexed-list-bar').show();
					jQuery('#boxFound').remove();
				})

				// 生成的搜索结果中，如果有已选用户，则置为已选状态
				jQuery('.mui-indexed-list-inner').find('input').each(function(i, item) {
					if (jQuery(this).prop("checked")) {
						var un = item.parentNode.getAttribute("uName");
						jQuery('#boxFound').find('input[value=' + un + ']').prop('checked', true);
					}
				});

				mui('#boxFound').on('change', 'input', function() {
					var isChecked = jQuery(this).prop('checked');
					var uName = jQuery(this).val();
					// jQuery('.mui-indexed-list-inner').find('li[uName=' + uName + '] input').trigger('tap'); // trigger无效
					jQuery('.mui-indexed-list-inner').find('li[uName=' + uName + '] input').prop('checked', isChecked);
					window.user.changeDoneStatus();
				});
			}
		},
		bindSearchEvent: function() {
			var self = this;
			self.el.searchInput.addEventListener('input', function() {
				var keyword = this.value;
				self.search(keyword);
			}, false);
			$(self.el.search).on('tap', classSelector('icon-clear'), function() {
				self.search('');
			}, false);
		},
		bindEvent: function() {
			var self = this;
			self.bindBarEvent();
			self.bindSearchEvent();
		}
	});
	//mui(selector).indexedList 方式
	$.fn.indexedList = function(options) {
		//遍历选择的元素
		this.each(function(i, element) {
			if (element.indexedList) return;
			element.indexedList = new IndexedList(element, options);
		});
		return this[0] ? this[0].indexedList : null;
	};

})(mui, window, document);