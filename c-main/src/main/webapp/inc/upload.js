var upfileCount = 0;
function addUploadFiles(){
    upfileCount ++;
    var file=document.createElement("input"); 
    file.type = "file";
    file.id = "f"+ upfileCount.toString();
    file.name = "att"+ upfileCount.toString(); 
    file.hideFocus = true;
    file.size = 1;
    file.style.display = "inline";
    // file.style.cssText = "cursor:pointer;width:1px;border:none;height:22px;padding:0px;margin:0px;margin-left:-120px;opacity:0;filter:alpha(opacity=0);font-size:30px;";
    file.className = "uploadFile";
	file.onchange = function(){
        o("s"+upfileCount.toString()).innerHTML = o("f"+upfileCount.toString()).value.substring(o("f"+upfileCount.toString()).value.lastIndexOf("\\")+1,o("f"+upfileCount.toString()).value.length) + "&nbsp;&nbsp;&nbsp;<span title=\"删除\" onclick=\"delFile('"+upfileCount.toString()+"')\" style=\"cursor:pointer;color:red;font-size:16px\">×</span>";
        this.style.display = "none";
        addUploadFiles();
    };
    // 如果表单不带有附件，当保存草稿返回时需判断upfilePanelHidden是否存在
    if (!o("upfilePanelHidden")) {
    	return;
    }
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

//
// upload
function initUpload() {
	var local = 'zh';
	if (document.getElementById('uploadJs')) {
        local = document.getElementById('uploadJs').getAttribute('local');
    }
	if (local == 'en'){
		document.write("<div class=\"upload\"><span style='float:left;margin-top:-3px;'>Upload</span><a id=\"upfilePanelHidden\" href=\"javascript:void(0);\"></a></div>");
	} else if (local == 'zh'){
		document.write("<div class=\"upload\"><span style='float:left;margin-top:-3px;'>添加文件</span><a id=\"upfilePanelHidden\" href=\"javascript:void(0);\"></a></div>");
	}
	
    document.write("<div id=\"upfilePanelShow\" onload=\"addUploadFiles\" style=\"margin-top:5px\"></div>");
	addUploadFiles();
}