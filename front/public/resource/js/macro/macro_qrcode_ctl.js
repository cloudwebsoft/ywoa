console.log('macro_user...ctl.js is loading1');
$('form').on("mouseover", ".qrcode-img", function () {
	// 把logo居中，并且放在一个两倍大背景为白色的div中，视觉效果并不好
	return;
	console.log('qrcode-img mouseover', $(this)[0]);
	if ($(this).attr('src')) {
		var w = $(this).width();
		var h = $(this).height();
		console.log('w', w, 'h', h);
		var str = '<div id="tempImgBox" style="position: absolute; width:' + 2*w + 'px; height:' + 2*h + 'px;background-color:#fff;z-index: 10000;"><img id="tempImg" class="temp-hover-img" src="' + $(this).attr('src') + '" style="margin-left:' + w/2 +'px;margin-top:' + h/2 + 'px;width: ' + w + 'px; height: ' + h + 'px;" /><div>';
		$('body').append(str);
		$('#tempImgBox').css("top", Math.max(0, (($(window).height() - $('#tempImgBox').outerHeight()) / 2) + 
				$(window).scrollTop()) + "px");
    $('#tempImgBox').css("left", Math.max(0, (($(window).width() - $('#tempImgBox').outerWidth()) / 2) + 
				$(window).scrollLeft()) + "px");
		console.log($('#tempImgBox')[0]);
	}
});

$('form').on("mouseout", ".qrcode-img", function () {
	$('#tempImgBox').remove();
});