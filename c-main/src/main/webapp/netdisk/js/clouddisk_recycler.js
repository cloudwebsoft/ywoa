/**
 * 回收站的js 功能包括 批量删除 批量还原 删除 还原
 */
$(function(){
	var dirContextMenu = {
	 		menuId: 'contextMenu',
	 		onContextMenuItemSelected:function(menuItemId, $triggerElement){
				var dirCode = $triggerElement.attr('dirCode');
 				var floderName = $triggerElement.find("a").text();//获得文件夹名
	 			if( menuItemId == 'restore' ){
	 				restoreFolder(dirCode,floderName);//还原文件夹
	 				
				}else if(menuItemId == 'delete'){
				    delFolder(dirCode,floderName);//删除文件夹
				}
			},
			onContextMenuShow:function($triggerElement){
			
			},
			showShadow:false
	    }
	var attContextMenu = {
	 		menuId: 'contextMenu',
	 		onContextMenuItemSelected:function(menuItemId, $triggerElement){
			 //attId
				var attId = $triggerElement.attr("attId");
	 			if( menuItemId == 'restore' ){
	 				 attRestore(attId);//附件恢复
	 				
				}else if(menuItemId == 'delete'){
					 attDel(attId);
				}
			},
			onContextMenuShow:function($triggerElement){
			
			},
			showShadow:false
	    }
	 //所有文件夹绑定右键操作
	$(".floderNameInfo").contextMenu(dirContextMenu);
	$(".attNameInfo").contextMenu(attContextMenu);
	//附件恢复
	$(".restoreFile").click(function(){
		var attId = $(this).attr("amId");
		attRestore(attId);
	})
	//附件删除
	$(".removeFile").click(function(){
		var attId = $(this).attr("amId");
		attDel(attId);
	})
})


/**
 * 文件夹恢复
 * @param dirCode
 * @param dirName
 * @return
 */
function restoreFolder(dirCode,dirName){
	
	jConfirm("确定要还原文件夹么?","提示",function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"post",
				url:"clouddisk_list_do.jsp",
				dataType:"json",
				data :{"op":"restoreFolder","dir_code":dirCode},
				success: function(data, status){
					if (data.ret=="1") {
						$("#floder"+dirCode).remove();
						deleteAfterStyleByList();
			 			$.toaster({ priority : 'info', message : '文件夹还原成功！' });
					}
					else{
						$.toaster({ priority : 'info', message : data.msg});
					}
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});
}

/**
 * 文件夹恢复
 * @param dirCode
 * @param dirName
 * @return
 */
function delFolder(dirCode,dirName){
	jConfirm("确定要彻底删除文件夹么?","提示",function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"post",
				url:"clouddisk_list_do.jsp",
				data :{"op":"removeFolder","dir_code":dirCode},
				success: function(data, status){
					data = $.parseJSON(data);
					if (data.ret=="1") {
						$("#floder"+dirCode).remove();
						  deleteAfterStyleByList();
			 			  $.toaster({ priority : 'info', message : '文件夹删除成功！' });
					}
					else{
						$.toaster({ priority : 'info', message :data.msg});
					}
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});
}
/**
 * 附件删除
 * @param attId
 * @return
 */
function attDel(attId){
	jConfirm("确定要彻底删除附件么?","提示",function(r){
		if(!r){
			return;
		}else{
			$.ajax({
		 		type:"get",
		 		url:"clouddisk_list_do.jsp",
		 		dataType:"json",
		 		data:{"op":"removeAttach","att_id":attId},
		 		success:function(data,status){
		 			if(data.ret == "1"){
		 				$("#tree"+data.att_id).remove(); //删除该行
		 				deleteAfterStyleByList();
			 			$.toaster({ priority : 'info', message : '文件删除成功' });
		 			}else{
			 			$.toaster({ priority : 'info', message : data.msg });
		 			}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
		});
		}
	});
	
}
/**
 * 附件恢复
 * @param attId
 * @return
 */
function attRestore(attId){
	jConfirm("确定要还原附件么?","提示",function(r){
		if(!r){
			return;
		}else{
			$.ajax({
			 		type:"get",
			 		url:"clouddisk_list_do.jsp",
			 		dataType:"json",
			 		data:{"op":"restoreAttach","att_id":attId},
			 		success:function(data,status){
			 			if(data.ret == "1"){
				 			$("#tree"+data.att_id).remove(); //删除该行
				 			deleteAfterStyleByList();
				 			$.toaster({ priority : 'info', message : '文件还原成功' });
			 			}else{
			 				$.toaster({ priority : 'info', message : data.msg });
			 			}
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
			});
		}
	});
}
/**
 * 批量恢复
 * @return
 */
function restoreBatch() {
	
	var att_ids = getAllCheckedValue("input[name='att_ids']");//文件夹附件的ids
	var ids_folder = getAllCheckedValue("input[name='floder_ids']"); //文件夹附件的IDS

	if (att_ids=="" && ids_folder=="") {
		jAlert("请先选择需要恢复的文件!","提示");
		return;
	}
	jConfirm("确定要恢复附件么?","提示",function(r){
		if(!r){
			return;
		}else{
			$(".treeBackground").addClass("SD_overlayBG2");
			$(".treeBackground").css({"display":"block"});
			$(".loading").css({"display":"block"});
			$.ajax({
		 		type:"get",
		 		url:"clouddisk_list_do.jsp",
		 		data:{"op":"restoreBatch","att_ids":att_ids,"dir_ids":ids_folder},
		 		success:function(data,status){
		 			$("#loading").css({"display":"none"});
		 			$(".treeBackground").css({"display":"none"});
		 			$(".treeBackground").removeClass("SD_overlayBG2");
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				var att_ids = data.att_ids;
		 				var dir_ids = data.dir_ids;
		 				var attId = new Array();
		 				var dirId = new Array();
		 				attId = att_ids.split(",");
		 				dirId = dir_ids.split(",");
		 				for(var i=0; i<attId.length; i++ ){
		 					$("#tree"+attId[i]).remove(); //删除该行
		 				}
		 				for(var i=0; i<dirId.length; i++ ){
		 					$("#folder"+dirId[i]).remove(); //删除该行
		 				}
		 				  deleteAfterStyleByList();
		 				 $.toaster({ priority : 'info', message : '还原成功！' });
		 			}
		 			else{
		 				 $.toaster({ priority : 'info', message : data.msg });
		 			}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
		});
		}
	});
}

/**
 * 批量删除
 * @return
 */
function removeBatch() {
	var att_ids = getAllCheckedValue("input[name='att_ids']");//文件夹附件的ids
	var ids_folder = getAllCheckedValue("input[name='floder_ids']"); //文件夹附件的IDS 
	if (att_ids== "" && ids_folder == "") {
		jAlert("请先选择需要删除的文件!","提示");
		return;
	}
	jConfirm("确定要彻底批量删除文件么?","提示",function(r){
		if(!r){
			return;
		}else{
			$(".treeBackground").addClass("SD_overlayBG2");
			$(".treeBackground").css({"display":"block"});
			$(".loading").css({"display":"block"});
			$.ajax({
		 		type:"get",
		 		url:"clouddisk_list_do.jsp",
		 		data:{"op":"removeBatch","att_ids":att_ids,"dir_ids":ids_folder},
		 		success:function(data,status){
		 			$("#loading").css({"display":"none"});
		 			$(".treeBackground").css({"display":"none"});
		 			$(".treeBackground").removeClass("SD_overlayBG2");
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				var att_ids = data.att_ids;
		 				var dir_ids = data.dir_ids;
		 				var attId = new Array();
		 				var dirId = new Array();
		 				attId = att_ids.split(",");
		 				dirId = dir_ids.split(",");
		 				for(var i=0; i<attId.length; i++ ){
		 					$("#tree"+attId[i]).remove(); //删除该行
		 				}
		 				for(var i=0; i<dirId.length; i++ ){
		 					$("#folder"+dirId[i]).remove(); //删除该行
		 				}
		 				 deleteAfterStyleByList();
		 				 $.toaster({ priority : 'info', message : '删除成功！' });
		 			}
		 			else{
		 				 $.toaster({ priority : 'info', message : data.msg});
		 			}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
		});
		}
	});
}




	

