//Pop-it menu- By Dynamic Drive - Modified by Wbird
//For full source code and more DHTML scripts, visit http://www.dynamicdrive.com
//This credit MUST stay intact for use
var menuOffX=0	//菜单距连接文字最左端距离
var menuOffY=18	//菜单距连接文字顶端距离

var fo_shadows=new Array()
var linkset=new Array()

////No need to edit beyond here

var ie4=document.all&&navigator.userAgent.indexOf("Opera")==-1
var ns6=document.getElementById&&!document.all
var ns4=document.layers
function openScript(url, width, height){
	var Win = window.open(url,"openScript",'width=' + width + ',height=' + height + ',resizable=1,scrollbars=yes,menubar=no,status=no' );
}
function getPosFF(oElement){
   var pos = {x:0,y:0};
   if( !oElement ) return pos;
   pos.x = oElement.offsetLeft;
   pos.y = oElement.offsetTop;
   var x = 0, y = 0;
   while( oElement.offsetParent ){
    //累加从该元素起至父元素的offsetLeft和offsetTop
    x += oElement.offsetParent.offsetLeft;
    y += oElement.offsetParent.offsetTop;
    //遇到table标签则表明累计的量都应该累加到该元素的offsetLeft和offsetTop(Table中按IE方式计算)
    if( oElement.offsetParent.tagName.toLowerCase() == "table" ){
     pos.x += x;
     pos.y += y;
     x = 0;
     y = 0;
    }
    oElement = oElement.offsetParent;
   }
   return pos;
}
function showmenu(e,vmenu,mod){
	if (!document.all&&!document.getElementById&&!document.layers)
		return

	which=vmenu
	clearhidemenu()
	ie_clearshadow()
	menuobj=ie4? document.all.popmenu : ns6? document.getElementById("popmenu") : ns4? document.popmenu : ""
	menuobj.thestyle=(ie4||ns6)? menuobj.style : menuobj
	if (ie4||ns6)
		menuobj.innerHTML=which
	else{
		menuobj.document.write('<layer name=gui bgColor=#E6E6E6 width=175 onmouseover="clearhidemenu()" onmouseout="hidemenu()">'+which+'</layer>')
		menuobj.document.close()
	}
	menuobj.contentwidth=(ie4||ns6)? menuobj.offsetWidth : menuobj.document.gui.document.width
	menuobj.contentheight=(ie4||ns6)? menuobj.offsetHeight : menuobj.document.gui.document.height
	menuobj.style.offsetWidth = 60;
	
	eventX=ie4? event.clientX : ns6? getPosFF(e.target).x : e.x
	eventY=ie4? event.clientY : ns6? getPosFF(e.target).y+e.target.offsetHeight-window.pageYOffset : e.y
	
	var rightedge=ie4? document.documentElement.clientWidth-eventX : window.innerWidth-eventX
	var bottomedge=ie4? document.documentElement.clientHeight-eventY : window.innerHeight-eventY
	
	if (rightedge<menuobj.contentwidth)
		menuobj.thestyle.left=ie4? document.documentElement.scrollLeft+eventX-menuobj.contentwidth+menuOffX+"px" : ns6? (window.pageXOffset+eventX-menuobj.contentwidth)+"px" : eventX-menuobj.contentwidth
	else
		menuobj.thestyle.left=ie4? ie_x(event.srcElement)+menuOffX+"px" : ns6? (window.pageXOffset+eventX)+"px" : eventX
		
	if (bottomedge<menuobj.contentheight&&mod!=0)
		menuobj.thestyle.top=ie4? document.documentElement.scrollTop+eventY-menuobj.contentheight-event.offsetY+menuOffY-23+"px" : ns6? (window.pageYOffset+eventY-menuobj.contentheight-10)+"px" : eventY-menuobj.contentheight
	else
		menuobj.thestyle.top=ie4? ie_y(event.srcElement)+menuOffY+"px" : ns6? (window.pageYOffset+eventY+3)+"px" : eventY
	menuobj.thestyle.visibility="visible"
	ie_dropshadow(menuobj,"#999999",3)
	
	return false
}

function ie_y(e){  
	var t=e.offsetTop;  
	while(e=e.offsetParent){  
		t+=e.offsetTop;  
	}  
	return t;  
}  
function ie_x(e){
	var l=e.offsetLeft;  
	while(e=e.offsetParent){  
		l+=e.offsetLeft;  
	}  
	return l;  
}  
function ie_dropshadow(el, color, size)
{
	var i;
	for (i=size; i>0; i--)
	{
		var rect = document.createElement('div');
		var rs = rect.style
		rs.position = 'absolute';
		rs.left = (el.offsetLeft + i) + 'px';
		rs.top = (el.style.posTop + i) + 'px';
		rs.width = el.offsetWidth + 'px';
		rs.height = el.offsetHeight + 'px';
		rs.zIndex = el.style.zIndex - i;
		rs.backgroundColor = color;
		var opacity = 1 - i / (i + 1);
		rs.filter = 'alpha(opacity=' + (100 * opacity) + ')';
		//el.insertAdjacentElement('afterEnd', rect);
		fo_shadows[fo_shadows.length] = rect;
	}
}
function ie_clearshadow()
{
	for(var i=0;i<fo_shadows.length;i++){
		if (fo_shadows[i])
			fo_shadows[i].style.display="none"
	}
	fo_shadows=new Array();
}

function contains_ns6(a, b) {
	while (b.parentNode)
		if ((b = b.parentNode) == a)
			return true;
	return false;
}

function hidemenu(){
	if (window.menuobj)
		menuobj.thestyle.visibility=(ie4||ns6)? "hidden" : "hide"
	ie_clearshadow()
}

function dynamichide(e){
	if (ie4&&!menuobj.contains(e.toElement))
		hidemenu()
	else if (ns6&&e.currentTarget!= e.relatedTarget&& !contains_ns6(e.currentTarget, e.relatedTarget))
		hidemenu()
}

function delayhidemenu(){
	if (ie4||ns6||ns4)
		delayhide=setTimeout("hidemenu()",500)
}

function clearhidemenu(){
	if (window.delayhide)
		clearTimeout(delayhide)
}

function highlightmenu(e,state){
	if (document.all)
		source_el=event.srcElement
	else if (document.getElementById)
		source_el=e.target
	if (source_el.className=="menuitems"){
		source_el.id=(state=="on")? "mouseoverstyle" : ""
	}
	else{
		while(source_el.id!="popmenu"){
			source_el=document.getElementById? source_el.parentNode : source_el.parentElement
			if (source_el.className=="menuitems"){
				source_el.id=(state=="on")? "mouseoverstyle" : ""
			}
		}
	}
}

if (ie4||ns6)
	document.onclick=hidemenu
