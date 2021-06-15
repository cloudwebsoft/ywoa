(function($){
  $.dragInit = function(c) {
    var trigger = c.trigger ? $('#'+c.trigger) : $('#'+c.target);
		trigger.css('cursor', 'move');
		var target = $('#'+c.target);
		var d = $(document);
		trigger.mousedown(function(e){
			var positionX = e.clientX - target.offset().left;
			var positionY = e.clientY - target.offset().top;
			d.mousemove(function(e){
				target.css({
					position: 'absolute',
					left: e.clientX - positionX,
					top: e.clientY - positionY
				});
			});
			d.mouseup(function(){
				d.unbind('mousemove');
				d.unbind('mouseup');
			});
		});
  }
}(jQuery));