// -----------ad_float begin---------------------
var ad_float_x = 50,ad_float_y = 60
var ad_float_xin = true, ad_float_yin = true
var ad_float_step = 1 
var ad_float_delay = 10
var ad_float=document.getElementById("ad_float") 
function floatAD() {
	var L=T=0
	var R= document.body.clientWidth-ad_float.offsetWidth
	var B = document.body.clientHeight-ad_float.offsetHeight
	ad_float.style.left = ad_float_x + document.body.scrollLeft
	ad_float.style.top = ad_float_y + document.body.scrollTop
	ad_float_x = ad_float_x + ad_float_step*(ad_float_xin?1:-1) 
	if (ad_float_x < L) { ad_float_xin = true; ad_float_x = L} 
	if (ad_float_x > R){ ad_float_xin = false; ad_float_x = R} 
	ad_float_y = ad_float_y + ad_float_step*(ad_float_yin?1:-1) 
	if (ad_float_y < T) { ad_float_yin = true; ad_float_y = T } 
	if (ad_float_y > B) { ad_float_yin = false; ad_float_y = B } 
}
var ad_float_it1= setInterval("floatAD()", ad_float_delay) 
ad_float.onmouseover=function(){clearInterval(ad_float_it1)} 
ad_float.onmouseout=function(){ad_float_it1=setInterval("floatAD()", ad_float_delay)}
// -----------ad_float end---------------------
