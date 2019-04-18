mui.init();
mui.ready(function() {
	var slider = document.getElementById('Gallery');
	var group = slider.querySelector('.mui-slider-group');
	var items = mui('.mui-slider-item', group);
	// 克隆第一个节点
	var first = items[0].cloneNode(true);
	first.classList.add('mui-slider-item-duplicate');
	// 克隆最后一个节点
	var last = items[items.length - 1].cloneNode(true);
	last.classList.add('mui-slider-item-duplicate');
	// 轮播图片定时
	var slider = mui("#slider");
	slider.slider({
		interval : 4000
	});
});
