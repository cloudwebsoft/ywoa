var uploadMap=new Map();
var urlUploadProgress = "../ajax_upload_progress.jsp";
function doUploadClear(response) {
	//alert(response.responseText);
}
function doGetUploadProgress(response) {
	var items = response.responseXML.getElementsByTagName("item");	
	for (var i=0; i<items.length; i++) {
		var item = items[i];
		var serialNo = item.getElementsByTagName("serialNo")[0].firstChild.data;
		var bytesRead = item.getElementsByTagName("bytesRead")[0].firstChild.data;
		var isFinish = item.getElementsByTagName("isFinish")[0].firstChild.data;
		
		var contentLength = item.getElementsByTagName("contentLength")[0].firstChild.data;
		if (contentLength!=0) {
			uploadMap.put(serialNo + "_size", contentLength);
		}
		
		if (bytesRead!=-1) {
			var sizeObj = uploadMap.get(serialNo + "_size");
			if (sizeObj==null) // 上传被取消时
				return;
			var fileSize = sizeObj.value;
			
			var per = 0;
			if (contentLength>0) {
				per = bytesRead/contentLength * 100;
			}

			if (isFinish=="true")
				per = 100;
			// window.status = "contentLength=" + contentLength + " bytesRead=" + bytesRead + " isFinish=" + isFinish + " per " + per;

			document.getElementById("uploadStatusProgress_" + serialNo).style.width = per.toString() + "%";

			if (isFinish!="true")
				document.getElementById("uploadStatus_" + serialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"cancelUpload('" + serialNo + "')\">取消</a>";
			else {
				document.getElementById("uploadStatus_" + serialNo).innerHTML = "";
			}
			//document.getElementById("uploadStatus_" + serialNo).innerHTML += "已读：" + bytesRead + ":" + isFinish + " " + per + " " + fileSize;
			if (per>=100) {
				try {
					onUploadFinish();
				}
				catch(e){}
			}
		}
		else {
			// 返回-1，则说明服务器端此次传输不存在，不再刷新
			var element=uploadMap.get(serialNo);
			if (element)
				window.clearTimeout(element.value);
		}

		if (isFinish=="true") {
			var element=uploadMap.get(serialNo);
			// 当这里clearTimeout后，在refreshUploadProgress中有时因异步操作，仍会记录一个timeoutid
			if (element)
				window.clearTimeout(element.value);
		
			uploadMap.remove(serialNo);
			uploadMap.remove(serialNo + "_size");
			
			var str = "op=clear&serialNo=" + serialNo;
			var myAjax = new cwAjax.Request( 
				urlUploadProgress, 
				{
					method:"post",
					parameters:str,
					onComplete:doUploadClear,
					onError:errUploadFunc
				}
			);
			
			var element = uploadMap.get(serialNo + "_attId");
			if (element!=null){
				document.getElementById("uploadStatus_" + serialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"delAtt('" + element.value + "', '" + serialNo + "')\">删除</a>";	
			}			
		}
	}
}
function doCancelUpload(response) {
	var items = response.responseXML.getElementsByTagName("item");	
	for (var i=0; i<items.length; i++) {
		var item = items[i];
		var serialNo = item.getElementsByTagName("serialNo")[0].firstChild.data;
		window.status = serialNo + " is canceled";
	}
}
function cancelUpload(serialNo) {
	var element=uploadMap.get(serialNo);
	window.clearTimeout(element.value);
	uploadMap.remove(serialNo);
	uploadMap.remove(serialNo + "_size");
	//var ifrm = document.getElementById("uploadFrm");
	//ifrm.contentWindow.document.getElementById("filename").disabled=false; 	
	var str = "op=cancel&serialNo=" + serialNo;
	var myAjax = new cwAjax.Request( 
		urlUploadProgress, 
		{
			method:"post", 
			parameters:str, 
			onComplete:doCancelUpload,
			onError:errUploadFunc
		}
	);
}
function getUploadProgress(serialNo) {
	var str = "serialNo=" + serialNo;
	var myAjax = new cwAjax.Request( 
		urlUploadProgress, 
		{
			method:"post", 
			parameters:str, 
			onComplete:doGetUploadProgress,
			onError:errUploadFunc
		}
	);
}

function clearProgress(serialNo) {
	document.getElementById("uploadStatusProgress_" + serialNo).style.width = "0px";
}

function showProgress(serialNo) {
	clearProgress(serialNo);
	refreshUploadProgress(serialNo);
}

var _st = window.setTimeout;
window.setTimeout = function(fRef, mDelay) {
 if(typeof fRef == 'function'){
  var argu = Array.prototype.slice.call(arguments,2);
  var f = function(){ fRef.apply(this, argu); };
  return _st(f, mDelay);
 }
 return _st(fRef,mDelay);
}

// var i = 0;
function refreshUploadProgress(serialNo) {
	getUploadProgress(serialNo);
	// window.status = i;
	// i++;
	var timeoutid = window.setTimeout(refreshUploadProgress, 2000, serialNo);

	if (!uploadMap.containsKey(serialNo)) {
		uploadMap.put(serialNo, timeoutid);
	}
}

var errUploadFunc = function(response) {
    alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}

function doDelAttachment(response){
	var items = response.responseXML.getElementsByTagName("item");	
	for (var i=0; i<items.length; i++) {
		var item = items[i];
		var uploadSerialNo = item.getElementsByTagName("uploadSerialNo")[0].firstChild.data;
		var result = item.getElementsByTagName("result")[0].firstChild.data;
		document.getElementById("uploadStatus_" + uploadSerialNo).innerHTML = "";
		document.getElementById("uploadStatusProgress_" + uploadSerialNo).style.width = "0px";
		
		var element=uploadMap.get(uploadSerialNo);
		uploadMap.remove(uploadSerialNo);
		uploadMap.remove(uploadSerialNo + "_size");
		
		uploadFrm.location.href="t_upload_img.jsp?uploadSerialNo=" + uploadSerialNo;
		
		var element=document.getElementById("attId");
		if(element)
		 	element.parentNode.removeChild(element);

		alert(result);
	}
}

function delAtt(attId, uploadSerialNo){
	if (!confirm("您确定要删除么？"))
		return;
	var str = "op=delAtt&attId=" + attId + "&uploadSerialNo=" + uploadSerialNo;
	var myAjax = new cwAjax.Request(
		"t_do.jsp",
		{
			method:"post", 
			parameters:str, 
			onComplete:doDelAttachment,
			onError:errFunc
		}
	);
}