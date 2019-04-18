var menuids=["treemenu"] //Enter id(s) of SuckerTree UL menus, separated by commas
function buildsubmenus_horizontal(){
	for (var i=0; i<menuids.length; i++){
		var menuObj = document.getElementById(menuids[i]);
		if (menuObj==null)
		continue;
		var ultags=menuObj.getElementsByTagName("ul")
		for (var t=0; t<ultags.length; t++){
			if (ultags[t].parentNode.parentNode.id==menuids[i]){ //if this is a first level submenu
				ultags[t].style.top=ultags[t].parentNode.offsetHeight+"px" //dynamically position first level submenus to be height of main menu item
				ultags[t].parentNode.getElementsByTagName("a")[0].className="mainfoldericon"
				ultags[t].parentNode.onmouseover=function(){
					this.getElementsByTagName("ul")[0].style.visibility="visible"
				}
			}
			else{ //else if this is a sub level menu (ul)
				ultags[t].style.left=ultags[t-1].getElementsByTagName("a")[0].offsetWidth+"px" //position menu to the right of menu item that activated it
				ultags[t].parentNode.getElementsByTagName("a")[0].className="subfoldericon"
				ultags[t].parentNode.onmouseover=function(){
					this.getElementsByTagName("ul")[0].style.visibility="visible"
				}
			}

			ultags[t].parentNode.onmouseout=function(){
				this.getElementsByTagName("ul")[0].style.visibility="hidden"
			}
		}
	}
}
/*
if (window.addEventListener)
window.addEventListener("load", buildsubmenus_horizontal, false)
else if (window.attachEvent)
window.attachEvent("onload", buildsubmenus_horizontal)
*/
buildsubmenus_horizontal();