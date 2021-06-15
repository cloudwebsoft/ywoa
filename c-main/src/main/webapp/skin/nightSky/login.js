$(function(){
	$('body').prepend('<div class="star"></div>');
	$('.star').animate({
		'left':'+=584px',
		'top':'511px',
		'opacity' : 0
	},2800,function(){
		$(this).css({
			"left":"600px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 
	
	$('body').prepend('<div class="star1"></div>');
	$('.star1').delay(300).animate({
		'left':'+=584px',
		'top':'511px',
		'opacity' : 0
	},2500,function(){
		$(this).css({
			"left":"400px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 
	
	$('body').prepend('<div class="star2"></div>');
	$('.star2').delay(600).animate({
		'left':'+=584px',
		'top':'511px',
		'opacity' : 0
	},2200,function(){
		$(this).css({
			"left":"200px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 
	
	$('body').prepend('<div class="star3"></div>');
	$('.star3').delay(500).animate({
		'left':'292px',
		'top':'+=365px',
		'opacity' : 0
	},1500,function(){
		$(this).css({
			"left":"-73px",
			'top':'150px',
			'opacity' : 0.3
		});
	}); 
	
	$('body').prepend('<div class="star4"></div>');
	$('.star4').delay(100).animate({
		'left':'+=438px',
		'top':'365px',
		'opacity' : 0
	},1500,function(){
		$(this).css({
			"left":"800px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 
});


setInterval("running()", 3000);

function running(){
	$('.star').animate({
		'left':'+=584px',
		'top':'511px',
		'opacity' : 0
	},2800,function(){
		$(this).css({
			"left":"600px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 

	$('.star1').delay(300).animate({
		'left':'+=584px',
		'top':'511px',
		'opacity' : 0
	},2500,function(){
		$(this).css({
			"left":"400px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 
	
	$('.star2').delay(600).animate({
		'left':'+=584px',
		'top':'511px',
		'opacity' : 0
	},2200,function(){
		$(this).css({
			"left":"200px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 
	
	$('.star3').delay(500).animate({
		'left':'292px',
		'top':'+=365px',
		'opacity' : 0
	},1500,function(){
		$(this).css({
			"left":"-73px",
			'top':'150px',
			'opacity' : 0.3
		});
	}); 
	
	$('.star4').delay(100).animate({
		'left':'+=438px',
		'top':'365px',
		'opacity' : 0
	},1500,function(){
		$(this).css({
			"left":"800px",
			'top':'-73px',
			'opacity' : 0.3
		});
	}); 
}

