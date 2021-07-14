/**
 *  mac.core 1.2
 */
var mac = { Msg: Msg || {} };
if (jQuery)(function($) {
	$.extend($.fn, {
		mac : function() {
			var func = arguments[0];
			arguments[0] = this;
			return eval('mac.' + func).apply(this, arguments);
		},
		seek : function(name) {
			return $(this).find('[name=' + name + ']');
		}
	});
})(jQuery);
mac.getMousePos = function(e){
	var e = e || window.event, d = document
		, de = d.documentElement, db = d.body;
	return {
		x: e.pageX || (e.clientX + (de.scrollLeft || db.scrollLeft)), 
		y: e.pageY || (e.clientY + (de.scrollTop || db.scrollTop))
	}
}
mac.eval = function(str) {
	return str ? eval('(' + str + ')') : {};
};
mac.getMsg = function(msg, params) {
	if (params && params.length)
		for ( var i = 0; i < params.length; i++)
			msg = msg.replace('{' + i + '}', params[i]);
	return msg;
};