$(function(){
	$('.loginbar_frame').prepend('<div class="car"></div>');
	$('.car').animate({
		'left':'720px'
	},4000,function(){
		$(this).css("left","140px");
	}); 
	
	$('.loginbar_frame').append('<div class="cloud1"></div>');
	$('.cloud1').animate({
		'left':'+=60px'
	},4000).animate({
		'left':'-=60px'
	},4000); 
});


setInterval("running()", 8000);

function running(){
	$('.car').animate({
		'left':'720px'
	},4000,function(){
		$(this).css("left","140px");
	});
	
	$('.cloud1').animate({
		'left':'+=40px'
	},4000).animate({
		'left':'-=40px'
	},4000); 
}
