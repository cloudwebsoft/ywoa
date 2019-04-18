/**
 * mac.pager
 * version: 2.0
 * author: Mac_J
 * need: core.js
 */
(function($){
mac.pager = function(self, cfg) {
	cfg.msg = cfg.msg || {
		page: '{0}, {1}/{2}',
		go: 'Go',
		total: 'Total'
	};
	self.config = cfg;
	self.update = function(tt, ps, pc, pn) {
		self.empty();
		self.total = tt;
		self.pageCount = pc;
		self.pageNo = pn;
		self.pageSize = ps;
		var msg = cfg.msg;
		self.append(mac.getMsg(msg.page, [ tt, pn, pc ]));
		var pl = cfg.pagerLength, hf = Math.floor(pl / 2), hm = pl % 2;
		var start = Math.max(1, Math.min(pn - hf, pc - pl + 1));
		var end = Math.min(start + pl, pc + 1);
		if (start > 1)
			self.append('<span action="1" class="pageNo">|&lt;&lt;</span>');
		if (start > pl) {
			var a = $('<span class="pageNo">&lt;&lt;</span>');
			a.attr('action', start - hf + (hm > 0) ? 0 : 1);
			self.append(a);
		}
		for ( var i = start; i < end; i++) {
			var a = $('<span class="pageNo"></span>');
			if (i != pn)
				a.attr('action', i);
			self.append(a.append(i));
		}
		if (end <= pc - pl) {
			var a = $('<span class="pageNo">&gt;&gt;</span>');
			a.attr('action', end + hf);
			self.append(a);
		}
		if (end < pc) {
			var a = $('<span class="pageNo">&gt;&gt;|</span>');
			self.append(a.attr('action', pc));
		}
		self.children('span[action]').click(function() {
			self.pageNo = $(this).attr('action');
			cfg.loadPage.call(self, self.pageNo, self.pageSize);
		});
		var pnTf = $('<input type="text" name="pageNo" />');
		pnTf.attr('maxlength', (''+pc).length);
		pnTf.change(function() {
			this.value = Math.max(1, Math.min(this.value, pc));
		});
		self.append(pnTf);
		$('<span class="button"></span>').click(function() {
			var n = pnTf.val() || 1;
			if (isNaN(n) || n > self.pageCount)
				n = 1;
			self.pageNo = n;
			cfg.loadPage.call(self, n, self.pageSize);
		}).append(msg.go).appendTo(self);
	};
	return self;
}
})(jQuery);