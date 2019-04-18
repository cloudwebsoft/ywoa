function doneChooseField(parentFieldMaps,byValue,showValue,openerFieldName){
	window.parent.doneField(parentFieldMaps,byValue,showValue,openerFieldName);
	window.parent.closeIframe();
}

function nestSheetJump(title, url,nest_sheet){
	window.parent.calByNestSheet(nest_sheet);
	window.parent.nestSheetJumpPage(title,url,nest_sheet);
}

function doneChooseUser(names,realNames,isAt,internalName){
	window.parent.closeChooseUser(names,realNames,isAt,internalName);
	window.parent.closeIframe();
}
function doneSelectUserWin(code,names,realNames){
    window.parent.doneSelectUserWin(code,names,realNames);
    window.parent.closeIframe();

}
function doneLocation(code,lat,lon,address){
	window.parent.closeLocation(code,lat,lon,address);
	window.parent.closeIframe();
	
}

function IsPC(){    
    var userAgentInfo = navigator.userAgent;  
    var Agents = new Array("Android", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod");    
    var flag = true;    
    for (var v = 0; v < Agents.length; v++) {    
        if (userAgentInfo.indexOf(Agents[v]) > 0) { flag = false; break; }    
    }    
    return flag;    
 }  