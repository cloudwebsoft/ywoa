var dirCode;
var userName;
var mode = 0;
var cooperateId =0;
var queueErrorArray;
var listItem = '';
var uploadCount = 0;//记录当前上传的个数
var numSelect = 0;//实际上传的个数
$(function(){
	dirCode = $("#dirCode").val();
	userName = $("#userName").val();
	cooperateId = $("#cooperateId").val();
	mode = $("#mode").val();
	$("#SD_cancel,#SD_close").live("click",function(){
		listItem = '';
	})
	
})

/**
* 打开文件选择对话框时响应
*/
function fileDialogStart() {
	if (queueErrorArray) {
		queueErrorArray = null;
	}
}
function initSwfUpload(swf){
	var swfUpload = new SWFUpload({
		upload_url:swf.upload_url,
		flash_url: 'swfupload/swfupload.swf',
		file_post_name: 'fileData',
		use_query_string: true,//get方式上传
		post_params:swf.post_params,
		file_types:swf.file_types,
		file_types_description: '上传文件',
		file_size_limit:swf.file_size_limit,
		file_upload_limit:swf.file_upload_limit,
		// handlers
		file_dialog_start_handler: fileDialogStart,
		file_queued_handler: fileQueued,
		file_queue_error_handler: fileQueueError,
		file_dialog_complete_handler: fileDialogComplete,
		upload_start_handler: uploadStart,
		upload_progress_handler: uploadProgress,
		upload_success_handler: uploadSuccess,
		upload_complete_handler: uploadComplete,
		
		button_placeholder_id: 'spanButtonPlaceholder',
		button_text: '普通',
		button_text_left_padding : 35,  
		button_text_top_padding : 2, 
		button_width: 106,
		button_height: 20,
		button_cursor: SWFUpload.CURSOR.HAND,
		button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
		
		debug: false,
		
		custom_settings: {}
	});
}
/**
* 文件被加入上传队列时的回调函数,增加文件信息到列表并自动开始上传.<br />
* <p></p>
* SWFUpload.startUpload(file_id)方法导致指定文件开始上传,
* 如果参数为空,则默认上传队列第一个文件;<br />
* SWFUpload.cancelUpload(file_id,trigger_error_event)取消指定文件上传并从队列删除,
* 如果file_id为空,则删除队列第一个文件,trigger_error_event表示是否触发uploadError事件.
* @param file 加入队列的文件
*/
function fileQueued(file) {
	var swfUpload = this;
	listItem += '<li id="' + file.id + '">';
	listItem += '<span><em>' + file.name + '</em>(' + Math.round(file.size/1024) + ' KB)</span>';
	listItem += '<div></div><div class="progressBars"><div class="progress"></div></div>'
			  + '<div class="progressValue"></div>'
			  + '</li>';
	
//	swfUpload.startUpload();
}

/**
* 文件加入上传队列失败时触发,触发原因包括:<br />
* 文件大小超出限制<br />
* 文件类型不符合<br />
* 上传队列数量限制超出等.
* @param file 当前文件
* @param errorCode 错误代码(参考SWFUpload.QUEUE_ERROR常量)
* @param message 错误信息
*/
function fileQueueError(file,errorCode,message) {
	if (!queueErrorArray) {
		queueErrorArray = [];
	}
	var errorFile = {
		file: file,
		code: errorCode,
		error: ''
	};
	switch (errorCode) {
	case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
		errorFile.error = '文件大小超出限制.';
		break;
	case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
		errorFile.error = '文件类型受限.';
		break;
	case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
		errorFile.error = '文件为空文件.';
		break;
	case SWFUpload.QUEUE_ERROR.QUEUE_LIMIT_EXCEEDED:
		errorFile.error = '超出文件数量限制.';
		break;
	default:
		alert('加载入队列出错.');
		break;
	}
	queueErrorArray.push(errorFile);
}

/**
* 选择文件对话框关闭时触发,报告所选文件个数、加入上传队列文件数及上传队列文件总数
* @param numSelected 选择的文件数目
* @param numQueued 加入队列的文件数目
* @param numTotalInQueued 上传文件队列中文件总数
*/
function fileDialogComplete(numSelected,numQueued,numTotalInQueued) {
	
	var swfupload = this;
	if (queueErrorArray && queueErrorArray.length) {
		for(var i in queueErrorArray) {
			jAlert(queueErrorArray[i].error,"提示");
		}
	} else {
		if(numSelected >= 1){
			numSelect = numSelected;//实际选择上传数目 赋值给全局变量
			showDialog("info",'<ol id="logList">'+listItem+'</ol>',"普通上传",400,75);
			this.startUpload();
		}
		
	}
}

/**
* 文件开始上传时触发
* @param file 开始上传目标文件
*/
function uploadStart(file) {
}

/**
* 文件上传过程中定时触发,更新进度显示
* @param file 上传的文件
* @param bytesCompleted 已上传大小
* @param bytesTotal 文件总大小
*/
function uploadProgress(file,bytesCompleted,bytesTotal) {
	var percentage = Math.round((bytesCompleted / bytesTotal) * 100);
	$("#logList li#" + file.id).find('div.progress').css('width',percentage + '%');
	$("#logList li#" + file.id).find('div.progressValue').text(percentage + '%');
}

/**
* 文件上传完毕并且服务器返回200状态码时触发
* @param file 上传的文件
* @param serverData 
* @param response
*/
function uploadSuccess(file,serverData,response) {
	uploadCount += 1;
	if(numSelect == uploadCount){
		numSelect = 0;
		uploadCount = 0;
		window.location.href = "?dir_code="+dirCode+"&userName="+userName+"&mode="+mode+"&cooperateId="+cooperateId;
	}
}

/**
* 在一个上传周期结束后触发(uploadError及uploadSuccess均触发)
* 在此可以开始下一个文件上传(通过上传组件的uploadStart()方法)
* @param file 上传完成的文件对象
*/
function uploadComplete(file) {
	this.uploadStart();
}


	

