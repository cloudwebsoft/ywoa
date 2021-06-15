$(function(){
	$('.loginbar_frame').prepend('<div class="car"></div>');
	$('.car').animate({
		'left':'800px'
	},4000,function(){
		$(this).css("left","120px");
	}); 
});


setInterval("running()", 4000);

function running(){
	$('.car').animate({
		'left':'800px'
	},4000,function(){
		$(this).css("left","120px");
	});
}

