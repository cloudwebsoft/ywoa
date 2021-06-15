/**
 * @author benboerba
 * @date 2018/12/27
 */
//标识当前画布是否已使用，未使用时禁止保存或清空操作，false--未使用   true--已使用
var signFlag=false;
var targetWidth=375;
var $sigdiv;
	
$(document).ready(function() {
	$(".content").height($(window).height());
	$sigdiv = $("#signature").jSignature({'sizeRatio':2,'width':"100%",'height':"100%",'lineWidth':3});
	// 保证图片的宽高比
	var scale=$(window).width()*0.75/$(window).height();// 签名布 宽/高;
	var signW=$(".signImg").css("width").replace("px","");
	$(".signImg").css({"height":signW*scale});
});
/**
 * 重置签名布
 */	
var reset=function(){
	if(signFlag){
		$sigdiv.jSignature('reset');
	}
}
/**
 * 生成签名
 */
var getSign=function(){
	if(signFlag){
		var data = $sigdiv.jSignature('getData', "image");
		if($.isArray(data) && data.length === 2){
			var imgStr=data.join(',');
			var image = new Image();  
	        image.src = "data:"+imgStr;
	        image.onload = function(){
	        	var roateStream=roate(image);
	        	var image2 = new Image();  
		        image2.src = roateStream;
		        image2.onload = function(){
		        	var compressStream=compress(image2);
		        	$("#signImg").attr("src","data:image/png;base64,"+compressStream).show();
		        }
	        };
		} else {
			alert("签名失败，请稍后再试！");
		}
	}
	$(".signWraper").stop().animate({ "left": "100%" }, 500);
	$sigdiv.jSignature('reset');
}

var applySign=function(code) {
	var val = $("#signImg").attr("src");
	doneWritePad(code, val);
}

/**
 * 打开签名布
 */
var goSign=function(){
	$(".signWraper").stop().animate({ "left": "0%" }, 500);
}
/**
 * 旋转图片
 */
var roate=function(image){
	var height=image.height;
	var width=image.width;
	var halfH=height/2;
	var halfW=width/2;
	var cvs = document.createElement('canvas');
	cvs.width = height;    
	cvs.height = width;     
	var ctx = cvs.getContext("2d")
	ctx.translate(halfW, halfH);
	ctx.rotate(-Math.PI/2);
	ctx.drawImage(image, halfH-width,-halfW);
	return cvs.toDataURL("image/png",1);
};
/**
 * 压缩图片
 */
var compress=function(image){
	var cvs = document.createElement('canvas');
	//指定图片压缩大小可以自由设置 但务必保持签名布的宽高比，这里是160*60
    cvs.width = 160;    
    cvs.height = 60;     
    var ctx = cvs.getContext('2d');    
    ctx.drawImage(image, 0, 0, cvs.width,cvs.height); 
    var newImageData = cvs.toDataURL("image/png",1); 
    var sendData = newImageData.replace("data:image/png;base64,",'');  
    return sendData;
};