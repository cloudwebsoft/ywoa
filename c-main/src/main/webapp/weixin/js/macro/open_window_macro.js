function doneChooseField(parentFieldMaps,byValue,showValue,openerFieldName){
	window.parent.doneField(parentFieldMaps,byValue,showValue,openerFieldName);
	window.parent.closeIframe();
}

function getParentPageType() {
    return window.parent.getParentPageType();
}

function nestSheetJump(title, url, nest_sheet, nestFormCode) {
    window.parent.calByNestSheet(nest_sheet, nestFormCode);
    window.parent.nestSheetJumpPage(title, url, nest_sheet);
}

function doneChooseUser(names,realNames,isAt, isFree, internalName, isCondition, workflowActionIdStr){
	window.parent.closeChooseUser(names,realNames,isAt, isFree, internalName, isCondition, workflowActionIdStr);
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
function doneWritePad(code, val) {
    window.parent.closeWritePad(code, val);
    window.parent.closeIframe();
}
function doneSelectUserWinForPlus(userNames,realNames,plusType,plusMode,myActionId) {
    window.parent.doneSelectUserWinForPlus(userNames,realNames,plusType,plusMode,myActionId);
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