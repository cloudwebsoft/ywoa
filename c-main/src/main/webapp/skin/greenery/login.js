$(function(){
	$('.loginbar_frame').prepend('<div class="balloon"></div>');
	$('.balloon').animate({
		'top':'-135px'
	},7000,function(){
		$(this).css("top","350px");
	}); 
	
	$('.loginbar_frame').prepend('<div class="cloud1"></div>');
	$('.cloud1').animate({
		'left':'+=200px'
	},6000).animate({
		'left':'-=200px'
	},6000); 
	
	$('.loginbar_frame').prepend('<div class="cloud2"></div>');
	$('.cloud2').animate({
		'left':'+=200px'
	},8000).animate({
		'left':'-=200px'
	},8000); 
});


setInterval("running()", 5000);

function running(){
	$('.balloon').animate({
		'top':'-135px'
	},7000,function(){
		$(this).css("top","350px");
	});
	
	$('.cloud1').animate({
		'left':'+=200px'
	},8000).animate({
		'left':'-=200px'
	},8000); 
	
	$('.cloud2').animate({
		'left':'+=200px'
	},6000).animate({
		'left':'-=200px'
	},6000);
}

