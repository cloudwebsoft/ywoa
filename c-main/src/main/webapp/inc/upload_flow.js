var upfileCount = 0;
function addUploadFiles(){
    upfileCount ++;
    var file=document.createElement("input"); 
    file.type = "file";
    file.id = "f"+ upfileCount.toString();
    file.name = "att"+ upfileCount.toString(); 
    file.hideFocus = true;
    file.size = 1;
    //file.style.cssText = "cursor:pointer;width:1px;border:none;height:22px;padding:0px;margin:0px;margin-left:-120px;opacity:0;filter:alpha(opacity=0);font-size:30px;";
    //file.className = "uploadFile";
    
    file.className = "uploadFileFlow";
    
	file.onchange = function(){
        o("s"+upfileCount.toString()).innerHTML = o("f"+upfileCount.toString()).value.substring(o("f"+upfileCount.toString()).value.lastIndexOf("\\")+1,o("f"+upfileCount.toString()).value.length) + "&nbsp;&nbsp;&nbsp;<span title=\"删除\" onclick=\"delFile('"+upfileCount.toString()+"')\" style=\"cursor:pointer;color:red;font-size:16px\">×</span>";
        this.style.display = "none";
        addUploadFiles();
    };
    o("upfilePanelHidden").appendChild(file);
    var div=document.createElement("div");
    div.id = "s"+ upfileCount.toString();
    o("upfilePanelShow").appendChild(div);
}
function delFile(str){
    o("upfilePanelHidden").removeChild(o("f"+str));
    o("upfilePanelShow").removeChild(o("s"+str));
}
function delAllUploadFile() {
	for (var i=1; i<=upfileCount; i++) {
		if (o("f" + i.toString()))
			o("upfilePanelHidden").removeChild(o("f" + i.toString()));
		if (o("s" + i.toString()))
			o("upfilePanelShow").removeChild(o("s" + i.toString()));
	}
	addUploadFiles();	
}
function delEmpty(){
    for (var i=1;i<=upfileCount;i++){
        if (o("f"+i.toString())){
            if (o("f"+i.toString()).value == "")
                o("upfilePanelHidden").removeChild(o("f"+i.toString()));
        }
    }
}
//upload
function initUpload() {
	document.write("<div class=\"leftbox-2\" style=\"cursor:pointer\"><span style=\"position:absolute; left:30px;\">请<u>点击此处添加</u>文件</span><span id=\"upfilePanelHidden\" href=\"javascript:void(0);\" style=\"z-index:100\"></span></div>");
    document.write("<div id=\"upfilePanelShow\" onload=\"addUploadFiles\" style=\"margin-top:5px\"></div>");
	addUploadFiles();
}