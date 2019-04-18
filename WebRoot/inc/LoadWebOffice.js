var s = ""
var webofficeHeight = 576;
s += "<object id=WebOffice1 height=" + webofficeHeight + " width='100%' style='LEFT: 0px; TOP: 0px'  classid='clsid:E77E049B-23FC-4DB8-B756-60529A35FAD5' codebase=WebOffice.cab>"
s +="<param name='_ExtentX' value='6350'><param name='_ExtentY' value='6350'>"
s +="</OBJECT>"
document.write(s)

function setWebofficeHeight(h) {
	document.getElementById("WebOffice1").height = h;
}