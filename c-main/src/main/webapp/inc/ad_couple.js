function initEcAd() {	
	if(window.document.body.offsetWidth<=820)
    {
          document.all.AdLayer1.style.visibility = 'hidden';
		  document.all.AdLayer2.style.visibility = 'hidden';
		  return;
    }
	document.all.AdLayer1.style.posTop = -200;
	document.all.AdLayer1.style.visibility = 'visible'
	document.all.AdLayer2.style.posTop = -200;
	document.all.AdLayer2.style.visibility = 'visible'
	MoveLeftLayer('AdLayer1');
	MoveRightLayer('AdLayer2');
}
function MoveLeftLayer(layerName) {
var x = 10;
var y = 100;
var diff = (document.body.scrollTop + y - document.all.AdLayer1.style.posTop)*.40;
var y = document.body.scrollTop + y - diff;
eval("document.all." + layerName + ".style.posTop = y");
eval("document.all." + layerName + ".style.posLeft = x");
setTimeout("MoveLeftLayer('AdLayer1');", 20);
}
function MoveRightLayer(layerName) {
var x = 10;
var y = 100;
var diff = (document.body.scrollTop + y - document.all.AdLayer2.style.posTop)*.40;
var y = document.body.scrollTop + y - diff;
eval("document.all." + layerName + ".style.posTop = y");
eval("document.all." + layerName + ".style.posRight = x");
setTimeout("MoveRightLayer('AdLayer2');", 20);
}
function DobAdv_Show(s)
{
    document.getElementById("AdLayer1").style.visibility = document.getElementById("AdLayer2").style.visibility = s?"visible":"hidden";
}
// document.write("<div id=AdLayer1 style='position: absolute;visibility:hidden;z-index:1'><a href='#' onClick='window.location.href=\"http://www.m-zj.cn/forum/listtopic.jsp?boardcode=mytp\"'><img border=0 src='../images/xx-1.gif' quality=high  WIDTH=60 HEIGHT=271 id=EccoolAd usemap='#Map' border='0'></a><map name='Map'><area shape='rect' coords='31,4,55,23' href='javascript:DobAdv_Show(false);void(0);'></map></div>"
//	+"<div id=AdLayer2 style='position: absolute;visibility:hidden;z-index:1'><a href='#' onClick='window.location.href=\"http://www.m-zj.cn/forum/listtopic.jsp?boardcode=zjgg\"'><img border=0 src='../images/xx-2.gif' quality=high  WIDTH=60 HEIGHT=271 id=EccoolAd usemap='#Map2' border='0'></a><map name='Map2'><area shape='rect' coords='7,2,26,23' href='javascript:DobAdv_Show(false);void(0);'></map></div>");
initEcAd()