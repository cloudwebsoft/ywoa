/**
 * version: 3.0b4
 * author: Mac_J
 * need: core.js, mousewheel.js
 * http://www.boarsoft.com/
 */
(function($){
mac.grid = function(self, cfg) {
	cfg = $.extend({
		onMoveEnd: false,  // 拖动列结束事件
		onResizeEnd: false  // 拖动列结束事件
	  }, cfg);
	
	cfg.rowHeight = cfg.rowHeight || 24;
	self.config = cfg;
	self.loader = cfg.loader;
	self.data = {};
	var yb = $('<div class="ybar"></div>').appendTo(self);
	var yy = $('<div></div>').appendTo(yb);
	var mm = $('<div class="main"></div>').appendTo(self);
	var hd = $('<div class="head"></div>').appendTo(mm);
	mm.append('<div class="clear"></div>');
	var bd = $('<div class="body"></div>').appendTo(mm);
	var tt = $('<div class="tt"></div>').appendTo(bd);
	var pb = cfg.loader.params;
	pb = pb.pageNo && pb.pageSize && cfg.pagerLength;
	if (pb)
		pb = $('<div class="pager"></div>').appendTo(self);
	var _h, _s;
	$('body').mousemove(function(e) {
		if (_s) {
			var dx = e.clientX - _s.mouseX;
			var cd = _s.th;
			var nw = cd.width() + dx;
			if (nw > 0) {
				bd.width(bd.width() + dx);
				tt.width(bd.width());
				hd.width(hd.width() + dx);
				_s.mouseX = e.clientX;
				mm.find('[name="' + _s.name + '"]').width(nw);
			}
		}
		if (_h) {
			var p = mac.getMousePos(e);
			_h.css({
				left : p.x + 5,
				top : p.y + 12
			});
		}
	}).mouseup(function(e) {
		if (_s){
			$.each(cfg.cols, function(n, c){
				if(c.name == _s.name)
					c.width = hd.seek(c.name).width();
				if(_s.group && c.name == _s.group)
					c.width = hd.seek(_s.group).width();
			});
			
			if (cfg.onResizeEnd)
				cfg.onResizeEnd(_s.name); // _s.name为被拖动的列名
			
			// 重新获得列宽
			self.acquireWidth();
				
			_s = 0;
			
		}
		if (_h) {
			_h.remove();
			_h = 0;
		}
	});
	self.sortBy = [];
	var tw = 0;
	$.each(cfg.cols, function(n, c) {
		var f = c.name = c.name || c.field;
		var o = $('<div></div>').attr('name', f);
		var p = c.parent || hd;
		o.appendTo(p);
		if(c.parent){
			o.attr('group', c.group);
			o.parents('.th').css('border-right', 'none');
		}
		if (c.hidden) {
			o.hide();
			return;
		}
		if(!c.isGroup)
			tw += c.width + (c.parent?1:2);
		o.attr('unselectable', 'on');
		if (c.sizeable != false && !c.isGroup) {
			var rh = $('<div class="sizer" unselectable="on"></div>');
			rh.mousedown(function(e) {
				var fcs = cfg.fixedCols || [];
				if ($.inArray(c.field, fcs) < 0
					&& $.inArray(c.group, fcs) < 0) {
					var th = hd.find('[group=' + f + ']').last();
					if(th.length==0)
						th = hd.seek(f);
					_s = {
						th: th,
						name: th.attr('name'),
						group: th.attr('group'),
						mouseX : e.clientX
					}
				}
				return false;
			}).appendTo(o);
		}
		o.width(c.width);
		if (c.sort) {
			var u = $('<span class="icon icon-triangle-1-s"></span>');
			$('<div class="sort"></div>').append(u).appendTo(o);
			self.sortBy.push({
				field : f,
				sort : "asc"
			});
		}
		var a = c.header;
		if (!a) {
			a = $('<div class="title" unselectable="on"></div>').append(c.title);
			var x = (c.parent) ? (c.height || cfg.rowHeight) : (cfg.headerHeight || cfg.rowHeight);
			a.height(x).css('line-height', x + 'px');
		}
		if (c.moveable != false) {
			a.mousedown(function(e) {
				if (_s)
					return;
				var fcs = cfg.fixedCols || []; // cfg.fixedCols || [];
				if ($.inArray(c.field, fcs) < 0
						&& $.inArray(c.group, fcs) < 0) {
					_h = $('<div class="dragHelper"><div>');
					_h.html('&nbsp;' + c.title + '&nbsp;');
					_h.col = $.inArray(f, $.map(cfg.cols, function(c) {
						return c.name || c.field
					}));
					_h.name = f;
					$('body').append(_h);
					var p = mac.getMousePos(e);
					_h.css({
						left : p.x + 5,
						top : p.y + 12
					});
				}
				return false;
			}).mousemove(function(e) {
				if (_h && f != _h.name) {
					var td = bd.find('.td[name=' + f + ']');
					td.addClass("dropable")
				}
			}).mouseout(function(e) {
				var td = bd.find('.td[name=' + f + ']');
				td.removeClass("dropable");
			}).mouseup(function() {
				var fcs = cfg.fixedCols || [];
				if ($.inArray(f, fcs) >= 0)
					return true;
				if (_h && f != _h.name) {
					if (hd.seek(_h.name).attr('group') != o
							.attr('group')) {
						_h.remove();
						return;
					}
					var td = mm.find('.td[name=' + f + '],.th[name=' + f + ']');
					td.removeClass("dropable");
					$.each(td, function(n, c) {
						var s = $(c);
						var t = s.parent().find('.td[name=' + _h.name
							+ '],.th[name=' + _h.name + ']');
						s.before(t);
					});
					var k = $.inArray(f, $.map(cfg.cols, function(c) {
						return c.name || c.field;
					}));

					cfg.cols.splice(k, 0, cfg.cols[_h.col]);
					
					if (k<_h.col)
						cfg.cols.splice(_h.col + 1, 1);
					else
						cfg.cols.splice(_h.col, 1);
					
					_h.remove();
					
					if (cfg.onMoveEnd)
						cfg.onMoveEnd(f, _h.name); // _h.name 被拖动的列	
				}		
				
			});
		}
		o.addClass('th').append(a);
	});
	hd.width(tw-2);
	bd.width(tw-1);
	tt.width(tw-2);
	self.sort = function() {
		var sb = self.sortBy;
		if (cfg.sortLocally) {
			var dd = [];
			$.each(self.data, function(n, c) {
				dd.push(c);
			});
			dd.sort(function(a, b) {
				for ( var i = 0; i < sb.length; i++) {
					var k = sb[i].field;
					var x = a[k];
					var y = b[k];
					var cp = null;
					for ( var j = 0; j < cfg.cols.length; j++) {
						var cc = cfg.cols[j];
						if (cc.field == k) {
							cp = cc.comparator;
							break;
						}
					}
					if (cp)
						return cp(x, y, sb[i].sort, a, b);
					if (x == y) {
						continue
					} else {
						if (sb[i].sort == 'desc') {
							return x > y ? 1 : -1
						} else {
							return x < y ? 1 : -1
						}
					}
				}
				return 0
			});
			self.loadData(dd);
		} else {
			var ob = $.map(sb, function(c) {
				return c.field + ' ' + c.sort
			}).join(',');
			self.load({
				orderBy : ob
			});
		}
	}
	hd.find('.sort').click(function() {
		var el = $(this).hide();
		var tu = 'icon-triangle-1-n';
		var td = 'icon-triangle-1-s';
		var fd = el.parent().attr('name');
		var sp = el.children();
		var sb = self.sortBy;
		for ( var i = 0; i < sb.length; i++) {
			if (sb[i].field == fd)
				break;
		}
		sb.splice(i, 1);
		if (sp.hasClass(tu)) {
			sb = sb.unshift({
				field : fd,
				sort : 'asc'
			});
			sp.removeClass(tu).addClass(td)
		} else {
			sb = sb.unshift({
				field : fd,
				sort : 'desc'
			});
			sp.removeClass(td).addClass(tu)
		}
		self.sort();
		return false;
	});
	self.adjustYBar = function() {
		yb.height(bd.height());
		yy.height(tt.height() + cfg.rowHeight);
		yb.css('overflow-y', (yy.height() > yb.height()) ? 'scroll' : 'auto');
	}
	self.afterAdjust
	self.adjust = function(vp) {
		if (vp)
			self.height(vp.height());
		var h = self.height();
		var g = pb ? 32 : 0;
		h = h - hd.height();
		bd.height(h - g - cfg.rowHeight - (cfg.footerHeight || 0));
		self.adjustYBar();
		if(self.afterAdjust)
			self.afterAdjust();
	};
	self.newRow = function(r, k) {
		var k = cfg.key ? r[cfg.key] : 'r' + k;
		self.data[k] = r;
		var a = $('<div name="' + k + '" class="tr"></div>');
		a.height(cfg.rowHeight);
		$.each(cfg.cols, function(n, c) {
			var b = $('<div class="td"></div>');
			b.height(cfg.rowHeight);
			b.attr('name', c.name || c.field);
			if (c.group) {
				var p = a.find('.td[name=' + c.group + ']');
				p.css('border-right', 'none');
				p.append(b);
				b.attr('group', c.group);
			} else {
				b.appendTo(a);
			}
			if (!c.isGroup) {
				b.append((c.render) ? c.render(r, a, self) : r[c.field]);
				b.append('&nbsp;').css('text-align', c.align || 'left');
				b.width(c.width);
			}
			if (c.hidden)
				b.hide();
		});
		a.click(cfg.onRowClick || function() {
			self.find('.tr').removeClass('selected');
			self.selected = $(this).addClass('selected');
		}).append('<div class="clear"></div>');
		tt.append('<div class="clear"></div>');
		return a;
	}
	self.addRow = function(r, k) {
		self.adjustYBar();
		return self.newRow(r, k).appendTo(tt);
	}
	self.delRow = function(keys) {
		keys = keys || [];
		if (keys.length == 0) {
			var s = self.selected;
			if (s)
				keys.push(s.attr('name'));
			self.selected = null;
		}
		$.each(keys, function(n, k) {
			if (self.data[k])
				delete self.data[k];
			var tr = tt.children('.tr[name=' + k + ']');
			tr.next().remove();
			tr.remove();
		});
		self.adjustYBar();
		return keys;
	}
	self.loadData = function(dd, po) {
		self.data = {};
		tt.empty();
		yy.height(2);
		$.each(dd, function(n, r) {
			self.addRow(r, n);
		});
		tt.find('.tr:odd').addClass('h2o');
		tt.find('.tr:even').addClass('h2e');
		hd.find('.sort').show();
		if (!cfg.autoHeight)
			self.adjust(self.parent());
		if (cfg.afterLoad)
			cfg.afterLoad(dd, po, self);
		return self;
	}
	self.load = function(pms) {
		pms = pms || {};
		var ldr = self.loader;
		$.extend(ldr.params, pms);
		$.post(ldr.url, ldr.params, function(data) {
			var ro = mac.eval(data);
			if (ro.success) {
				ro = ro.data;
				self.loadData(ro.list, ro);
				if (pb)
					pb.update(ro.total, ro.pageSize, ro.pageCount, ro.pageNo)
			}
			hd.find('.sort').show();
		})
	};
	
	self.acquireWidth = function() {
		$.each(cfg.cols, function(n, c){
			c.width = hd.seek(c.name).width();
		});
	};
	
	yb.scroll(function() {
		bd.scrollTop(this.scrollTop)
	});
	bd.mousewheel(function(e, delta, deltaX, deltaY) {
		yb.scrollTop(yb.scrollTop() - deltaY * cfg.rowHeight);
		return false;
	});
	if (pb) {
		self.pager = pb.mac('pager', {
			pagerLength : cfg.pagerLength,
			pageSize : self.loader.pageSize,
			loadPage : function(pn) {
				self.load({
					pageNo : pn
				});
			}
		});
	}
	if (self.loader.autoLoad)
		self.load();
	else if (!cfg.autoHeight)
		self.adjust(self.parent());
	return self;
}
})(jQuery);