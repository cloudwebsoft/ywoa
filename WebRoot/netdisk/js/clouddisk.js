/**
 * 云盘一些共通的JS方法
 */
var curDirCode; //当前code
var attId  //当前id
var userName;
var dirCode;//本页面的dirCode

//列表界面 平铺界面中 文件夹右键菜单

var contextFloderMenu = {
	menuId: 'contextFloderMenu',
	onContextMenuItemSelected:function(menuItemId, $triggerElement){
		curDirCode = $triggerElement.attr('dirCode');
		var curPage = $("#curPage").val();//当前页
		var unit_code = $("#unitCode").val();
		var dirName = '';
		var $attDirName;
		if(curPage == 1){
			dirName = $triggerElement.attr('dirName');
		}else{
			dirName = $triggerElement.find("a").text();//获得文件夹名
		}
		if(menuItemId == 'reName'){
			CDirNameBefore(curDirCode);
		}else if(menuItemId == 'download'){
			window.open("clouddisk_downloaddir.jsp?code=" + curDirCode );
		}else if(menuItemId == 'delete'){
			delFolder(dirName,curDirCode,curPage);//删除文件夹
		}else if(menuItemId == 'move'){
			showDialogTree(curDirCode,curDirCode);
		}else if(menuItemId == 'template'){
			template(unit_code,curDirCode);
		}else if(menuItemId == 'cooperateFolder'){
			$("#contextFloderMenu").hide();
			//openWinUsers(curDirCode);
			openWinUsers();
		}else if(menuItemId == 'deCooperateFolder'){
			deCooperateFolder();
		}
	},
	onContextMenuShow:function($triggerElement){
		
	},
	showShadow:false
}

/**
 * 2.2 附件右键 list
 */
var contextAttachMentMenu = {
		menuId: 'contextAttachMentMenu',
		onContextMenuItemSelected:function(menuItemId, $triggerElement){
			attId = $triggerElement.attr('attId');
			var oldAmId = $triggerElement.attr('amOldId');
			var docId = $triggerElement.attr('docId');
			var ext = $triggerElement.attr('ext');
			var isImgSearch = $triggerElement.attr('isImgSearch');
			var curPage = $("#curPage").val();//当前页
			var unit_code = $("#unitCode").val();
			var extType = $(".attExtType"+oldAmId).val();//获得该att的config参数
			var dirName = '';
			var $attDirName;
			if(curPage == 1){
				dirName = $triggerElement.attr('dirName');
				$attDirName = $triggerElement.find(".attDirName");
			}else{
				if(menuItemId == 'reName'){
					dirName = $triggerElement.find("a").text();//获得文件名
					var fileInput = "<input id='"+attId+"' class='singleboarder single_item dirAttInputText' docId='"+docId+"' oldAmName='"+dirName+"' oldAmId='"+oldAmId+"' ext='"+ext+"' value='" + dirName + "' />";
					$triggerElement.html(fileInput);
					var val =  $("#"+attId).val();
					var endLen = val.lastIndexOf(".");//文本高亮显示
					if(endLen != 0){
						var name = val.substring(0,endLen);
						setTextSelected($("#"+attId).get(0),0,endLen);
					}else{
						 $("#"+attId).get(0).focus();
					}
				}else if(menuItemId == 'download'){
					window.open("clouddisk_downloadfile.jsp?attachId="+attId );
				}else if(menuItemId == 'open'){
					if(extType == 1){
						showImg(attId,isImgSearch);
					}else if(extType == 5){
						edittxt(docId,attId,ext);
					}else if(extType == 6){
						editdoc(docId,attId,ext);
					}
				}else if(menuItemId == 'delete'){
					delAttach(attId, docId,oldAmId);//删除附件
				}else if(menuItemId == 'move'){
					showDialogTree(parseInt(attId),parseInt(oldAmId), parseInt(docId));
				}else if(menuItemId == 'history'){
					showDigHistory(attId);
				}else if(menuItemId == 'publicShare'){
					window.location.href = "netdisk_public_share.jsp?pageNo=1&attachId=" + attId;
				}
			}
		},
		onContextMenuShow:function($triggerElement){
			var oldAmId = $triggerElement.attr('amOldId');
			var ext = $triggerElement.attr('ext');
			var extType = $(".attExtType"+oldAmId).val();
			$("#contextAttachMentMenu li:eq(0)").show();
			if(extType != "1" && extType != "5" && extType != "6"){
				$("#contextAttachMentMenu li:eq(0)").hide();
			}
		},
		showShadow:false
	}

$(function(){
	//判断赋予角色是否显示 应用于新建文件夹
	
	var type= $("#type").val();
	var root_code = $("#rootCode").val();
	var code = $("#code").val();
    userName = $("#userName").val();
    var curPage = $("#curPage").val();//当前页
    dirCode = $("input[name='dir_code']").val();
    //是都显示角色模板菜单
    curDirCode = $("#curDirCode").val();
    if(curDirCode != "" &&  root_code != "" && userName != ""){
		if( userName == "system") {
				$("#contextMenuAttachMent #publicShare").hide();//角色模板中附件不能发布
				$("#contextAttachMentMenu #publicShare").hide();
			if( root_code == curDirCode) {
				$("#contextFloderMenu #template").show();
				$.toaster({ priority : 'info', message : '请单击文件夹右键赋予角色！' });
			}else {
				$("#contextFloderMenu #template").hide();
			}			
		} else {
			$("#contextFloderMenu #template").hide();
			$("#contextMenuAttachMent #publicShare").show();//角色模板中附件不能发布
			$("#contextAttachMentMenu #publicShare").show();
		}
 	}
    if(curPage == 1 || curPage == 0){
        // swfUpload批量上传
      	var browserFixfox = navigator.userAgent.indexOf("Firefox")!=-1;
        var uploadUrl = $("#uploadUrl").val();
    	var fileSizeLimit = $("#fileSizeLimit").val();
    	var fileUploadLimit = $("#fileUploadLimit").val();
    	var uploadFileType = '';
    	uploadFileType = $("#uploadFileType").val();
    	/*if(browserFixfox){
    		uploadFileType = $("#uploadFileType").val();
    	}else{
    		uploadFileType = $("#uploadFileType").val();
    	}*/
    	try{
	    	initSwfUpload({
	    		"upload_url":uploadUrl,
	    		"post_params":{
	    			"type": 0
	    		},
	    		"file_types":uploadFileType,
	    		"file_upload_limit":fileUploadLimit,
	    		"file_size_limit":fileSizeLimit
	    	});
    	}catch (ex){
    		jAlert(ex,"提示");
    	}
    }

	 //文件上传
	$('.uploadFile_c').mouseover(function(){
				$(this).css({"background":"url(images/clouddisk/uploadFile_2.gif)"});
				$("#upload_sel").show();
				$("#upload_sel").css("margin","0px");
		}).mouseleave(function(){
				$(this).css({"background":"url(images/clouddisk/uploadFile_1.gif)"});
				$("#upload_sel").css("margin","2000px");
			
	});
	
	//文件上传切换
	$(".upload_sel").find("ul").find("li").find("a").hover(
		function(){
			$(this).css({"background":"#666","color":"#fff"});
		}
	 )
	$(".upload_sel").find("ul").find("li").find("a").mouseleave(
		function(){
			$(this).css({"background":"#fff","color":"#000"});
		}
	)
	
	//批量删除按钮
	$('.deleteFile_c').mousedown(function(){
		$(this).css({"background":"url(images/clouddisk/deleteFile_2.gif)"});
	}).mouseup(function(){
		$(this).css({"background":"url(images/clouddisk/deleteFile_1.gif)"});
	});
	
	//新建文件夹
	var newFolder = function (){
		name = "新建文件夹";
	    var dataDetail = {"op":"AddChild","type":type,"parent_code":dirCode,"root_code":root_code,"code":code,"name":name};
	    newDir(dataDetail,userName);
	}
	
	//新建文件夹事件绑定
	$('.newFolder').mousedown(function(){
		$(this).css({"background":"url(images/clouddisk/newFolder_2.gif)"});
	}).mouseup(function(){
		$(this).css({"background":"url(images/clouddisk/newFolder_1.gif)"});
	})
	$(".newFolder").live({"click":newFolder});
	
	//平铺列表界面切换
	$("#view_1").click(
		  function(){
		    $(this ).css({"background":"url(images/clouddisk/view_list_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_thumbnail_2.gif)"});
		  }); 
	$("#view_2").click(
		  function(){
		   $(this).css({"background":"url(images/clouddisk/view_thumbnail_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_list_2.gif)"});
		  }); 
	
	//角色模板 
	 $('#template_confirm').live("click",roleTemplateConfirm);
	 
	 
	 //平铺列表界面右键菜单绑定
	 if(curPage == 1){
		 $(".dir").contextMenu(contextFloderMenu);
	 }else{
		 $(".floderNameInfo").contextMenu(contextFloderMenu);	//文件夹右键绑定
		 $(".attNameInfo").contextMenu(contextAttachMentMenu);  //附件右键绑定
	 }
	 //文件夹重命名 焦点移除事件
	 $(".dirInput").live({"blur":dirChangeName,"keydown":function(e){
			if(e.keyCode==13){
				this.blur();
			}
		 }});
	
	 //图片分页
	$(".showImg_Next").live("click",function(){
		var arrow = $(this).attr("value");
		var isImgSearch = $(this).attr("isImgSearch");
		showImgNext("clouddisk_list_do.jsp",arrow,isImgSearch);
		
	})
		
	//根据文件夹是否已经分享，判断是否隐藏取消分享的选项（右键控制）  tiled.jsp
	$(".dir").mousedown(function(e){
		if (e.which==3) {
			if($(this).attr("isShared")== "true" ? 1 : 0){
				$("#deCooperateFolder").show();
			}else{
				$("#deCooperateFolder").hide();
			}
		}
	})
	
	//根据文件夹是否已经分享，判断是否隐藏取消分享的选项（右键控制）  list.jsp
	$(".fileGroup").mousedown(function(e){
		if (e.which==3) {
			if($(this).attr("isShared")== "true" ? 1 : 0){
				$("#deCooperateFolder").show();
			}else{
				$("#deCooperateFolder").hide();
			}
		}
	})
})

/**
 * 平铺界面
 * @param dir_code
 * @return
 */
function tiled_list(dir_code){ 
	var expdate = new Date();
	var expday = 60;
	expdate.setTime(expdate.getTime() +  (24 * 60 * 60 * 1000 * expday));
	document.cookie="netdiskDefaultStatus"+"="+escape(1)+";expires="+expdate.toGMTString();//1代表平铺界面
	var select_sort = $("#isSearch").val();
	var which = $("#which").val();
	var select_file = $("#fileType").val();
	var text_content = $("#content").val();
	if(select_sort == 'select_one') {
		window.location.href="clouddisk_tiled.jsp?userName="+userName+"&select_sort=select_one&select_content="+text_content;
	}else{
		if(select_file == 'select_file') {
			window.location.href="clouddisk_tiled.jsp?userName="+userName+"&select_file=select_file&select_which="+which;
    	}else{
    		window.location.href="clouddisk_tiled.jsp?userName="+userName+"&dir_code="+dir_code;
        }
	}
}
/**
 * 列表界面切换
 * @param dir_code
 * @return
 */
function cloud_list(dir_code){
	var expdate = new Date();
	var expday = 60;
	expdate.setTime(expdate.getTime() +  (24 * 60 * 60 * 1000 * expday));
	document.cookie="netdiskDefaultStatus"+"="+escape(0)+";expires="+expdate.toGMTString();//0代表列表界面
	var select_sort = $("#isSearch").val();
	var which = $("#which").val();
	var select_file = $("#fileType").val();
	var text_content = $("#content").val();
	var mode = $("#mode").val();
	if(select_sort == 'select_one') {
		window.location.href="clouddisk_list.jsp?mode=" + mode + "&userName="+userName+"&select_sort=select_one&select_content="+text_content;
	}else{
		if(select_file == 'select_file') {
			window.location.href="clouddisk_list.jsp?mode=" + mode + "&userName="+userName+"&select_file=select_file&select_which="+which;
    	}else{
    		window.location.href="clouddisk_list.jsp?mode=" + mode + "&userName="+userName+"&dir_code="+dir_code;
        }
	}
}
/**
 * 批量删除 根据不同的curPage 调用不同删除后显示层
 * 0代表列表界面
 * 1代表平铺界面
 * @return
 */
function delBatch() {
	var curPage = $("#curPage").val(); //判断当前页
	var ids = getAllCheckedValue("input[name='att_ids']");
	var ids_folder = getAllCheckedValue("input[name='floder_ids']"); 
	if (ids=="" && ids_folder=="") {
		jAlert("请先选择文件！","提示");
		return;
	}
	jConfirm("确定要批量删除么?<br/>删除后可以在回收站找回!","提示",function(r){
		if(!r){return;}
		else{
			$(".treeBackground").addClass("SD_overlayBG2");
			$(".treeBackground").css({"display":"block"});
			$(".loading").css({"display":"block"});
			$.ajax({
		 		type:"get",
		 		url:"clouddisk_list_do.jsp",
		 		data:{"op":"delBatch_tiled","att_ids":ids,"dir_ids":ids_folder},
		 		success:function(data,status){
		 			$("#loading").css({"display":"none"});
		 			$(".treeBackground").css({"display":"none"});
		 			$(".treeBackground").removeClass("SD_overlayBG2");
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				var att_ids = data.att_ids;
		 				var dir_ids = data.dir_ids;
		 				if(dir_ids != ""){
		 					var dirCode = new Array();
		 					dirCode = dir_ids.split(",");
		 					for(var i=0; i<dirCode.length; i++ ){
		 						if(curPage == 1){
		 							$("#dirAtt"+dirCode[i]).remove();
		 						}else{
		 							$("#folder"+dirCode[i]).remove();//列表界面删除
		 						}
		 						
		 					}
		 				}
		 				if(att_ids != ""){
			 				var attId = new Array();
			 				attId = att_ids.split(",");
			 				for(var i=0; i<attId.length; i++ ){
			 					if(curPage == 1){
			 						$("li[attId='"+attId[i]+"'").remove();
			 					}else{
			 						$("#tree"+attId[i]).remove();
			 					}
			 					
			 				}
		 				}
		 				 if(curPage == 1){
		 					 deleteAfterStyleByTiled()
		 				 }else{
		 					 deleteAfterStyleByList();
		 				 }
		 				$.toaster({ priority : 'info', message : '批量删除成功！' });
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
	})
}
/**
 * 列表批量删除后样式
 * @return
 */
function deleteAfterStyleByList(){
	var length = $(".cbox:checked").length;//获得选中的数量
	var $attTitle = $('.fnameTitle');//文件名标题
	var $allCbox = $("#filename_input");//全选checkbox
	var $allCboxIcon = $('.cbox_all');//全选图标
	if(length == 0){
		$allCbox.attr("checked",false);//全选取消
		$allCboxIcon.css("background","url('images/clouddisk/checkbox_1.png')");//颜色改变
		$attTitle.text("文件名");
	}else{
		$attTitle.text("已选择"+length+"项");
		$allCbox.attr("checked",true);//全选取消
		$allCboxIcon.css("background","url('images/clouddisk/checkbox_3.png')");//颜色改变
	}
}
/**
 * 平铺界面删除后的样式
 * @return
 */
function deleteAfterStyleByTiled(){
	var length = $(".attDirCheckBox:checked").length;//获得选中的数量
	var cboxLength = $(".attDirCheckBox").length;
	var $cbInfo = $('.rHead2 .checkboxDetail');
	if(length == 0){
		$(".attDirGroup li").css({"border":"1px solid #fff","background":""});
		$(".attDirGroup li .attDirCheckIcon").css("background","");
		var $attDirIcon = $('.attDirGroup li .attDirIcon a');
		var $cbInfo = $('.rHead2 .checkboxDetail');
		//恢复所有的超链接属性
		$attDirIcon.each(function(i){
			var url = $(this).attr("url");
			$(this).attr("href",url);
		})
		$cbInfo.hide();
		$("#dirAttAllCbox").attr("checked",false);
		$("#dirAttAllCboxIcon").css("background","url('images/clouddisk/checkbox_1.png')");
	}else{
		if(length == cboxLength){
			$cbInfo.show();
			$cbInfo.find(".length").text('已选择了'+length+'个文件,');
			$cbInfo.find('.cancle').text('取消');
			$("#dirAttAllCbox").attr("checked",true);
			$("#dirAttAllCboxIcon").css("background","url('images/clouddisk/checkbox_3.png')");
		}else{
			$cbInfo.show();
			$cbInfo.find(".length").text('已选择了'+length+'个文件,');
			$cbInfo.find('.cancle').text('取消');
			$("#dirAttAllCbox").attr("checked",false);
			$("#dirAttAllCboxIcon").css("background","url('images/clouddisk/checkbox_1.png')");
		}
	
	}
}


/**
 * 新建文件夹
 * @param dataDetail
 * @return
 */
function newDir(dataDetail,userName){
	var curPage = $("#curPage").val(); //判断当前页
	var cooperateId = $("#cooperateId").val(); //判断是否是cooperateid的页面
	$.ajax({
		type:"post",
		url:"clouddisk_list_do.jsp",
		data :dataDetail,
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="1") {
				if(curPage == 1){
					var li ='<li class="dir" style="border:1px solid #c0e0f4;"  title="'+data.name+'"  id="dirAtt'+data.code+'"   dirCode="'+data.code+'"  dirName="'+data.name+'">';
					li+='<input class="attDirCheckBox"  name="floder_ids" type="checkbox" value="'+data.code+'" />';
					li+='<div class="attDirCheckIcon" style="background:url(images/clouddisk_tiled/check_default.png);" ></div>';
					li+='<div class="attDirIcon">';
					li+='<a url ="clouddisk_tiled.jsp?userName='+userName+'&op=editarticle&dir_code='+data.code+ '"  href="clouddisk_tiled.jsp?userName='+userName+'&op=editarticle&dir_code='+data.code+'" >';
					li+='<img src="images/clouddisk_tiled/folder.png" />';
					li+='</a>';
					li+='</div>'
					li+='<div class="attDirName" id="dirName'+data.code+'">';
					li+='<input type="text" class="attDirNameInput  dirInput"  id="'+data.code+'"   value="'+data.name+'" oldName="'+data.name+'" />';
					li+='</div></li>';
					$(".attDirGroup").prepend(li);	
					$("#dirAtt"+data.code).contextMenu(contextFloderMenu);
					$("#"+data.code).get(0).select();
				}else{
					var dlInfo = '<dl><dd class="fileGroup" id="folder'+data.code+'" >';
					dlInfo += '<div class="fileNameDetail">';
					dlInfo += '<span class="cbox_icon"></span>';
					dlInfo += '<input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" value="'+data.code+'" />'
					dlInfo += '<img  src="images/sort/folder.png" class="extImg"/>';
					dlInfo += '<span class="fname floderNameInfo" id="span'+data.code+'" dirCode="'+data.code+'" name="span'+data.code+'">';
					dlInfo += "<input id='"+data.code+"' class='singleboarder dirAttInputText dirInput' oldName='"+data.name+"'  value='" + data.name + "' /></span>";
					dlInfo += "<div class='file_action' style='display:none'><ul><li><a id='download"+data.code+"' href='clouddisk_downloaddir.jsp?code="+data.code+"' target='_blank'><img title='下载' src='images/clouddisk/download_1.gif'> </a></li>";
					dlInfo += "<li><a id='reName"+data.code+"' onclick=CDirNameBefore('"+data.code+"')> <img title='重命名' src='images/clouddisk/rename.gif'></a></li>";
					dlInfo += "<li><a id='move"+data.code+"' href=javascript:showDialogTree('"+data.code+"','"+data.code+"','0')><img title='移动' src='images/clouddisk/move.gif'></a></li>";
					dlInfo += "<li><a id='delete"+data.code+"' onclick=delFolder('新建文件夹','"+ data.code+"',0)><img title='删除' src='images/clouddisk/recycler_1.gif'></a></li></ul>";
					dlInfo += '</div></div>';
					dlInfo += '<div class="col" ><span></span></div>';
					dlInfo += '<div class="col" ><span>文件夹</span></div>';
					dlInfo += '<div class="col" ><span></span></div>';
					dlInfo += '</dd></dl>';
					$(".containtCenter").prepend($(dlInfo));
					$("#span"+data.code).contextMenu(contextFloderMenu);
					$(".singleboarder").get(0).select();
				}
				
			}
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
}
/**展示图片
 * 
 * @param attId
 * @return
 */
function showImg(attId,isImgSearch) {
	$.ajax({
	 		type:"get",
	 		url:"clouddisk_list_do.jsp",
	 		data:{"op":"showImg","attId":attId},
	 		success:function(data,status){
	 			data = $.parseJSON(data);
	 			if(data.ret == 1){
	 				showDialog("info",attId,"图片预览",data.width,data.height,"showImg",data.downloadUrl,isImgSearch);
	 			}else{
	 				jAlert(data.msg,"提示");
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
	});
}

/**txt预览
 * @param id
 * @param attachId
 * @param ext
 * @return
 */
function edittxt(id, attachId, ext) {
	$.ajax({
	 		type:"get",
	 		url:"clouddisk_list_do.jsp",
	 		data:{"op":"showTxt","attId":attachId},
	 		success:function(data,status){
	 			data = $.parseJSON(data);
	 			if(data.ret == "1"){
	 				showDialog("info",data.msg,"文档预览",1200,440,ext);
	 			}else{
	 				jAlert(data.msg,"提示");
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
	});
}

/**
 * 历史版本弹出窗口
 */
function showDigHistory(attId){
	$.ajax({
		type:"post",
		url:"clouddisk_list_do.jsp",
		data:{"op":"history","attId":attId},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.result=="1") {
				var contentCenter =' <div class="windowContainCenter"><div class="row"><span class="cols first" >文件名</span><span  class="cols name">大小</span><span class="cols name2">版本</span><span class="cols actionName">更新时间</span><span class="cols name">操作</span></div>';
				var row ="";
				var currentNum = data.total;
				var amId = "";
				$.each(data.historyLogs,function(index,data){
					if(data.isCurrent == "1"){
						amId = data.id;
						row +='<div class="row" id=history_'+ data.id +'><span class="cols first" >'+data.name+'</span><span  class="cols name">'+data.size+'</span><span class="cols name2" style="color:red">最新</span><span class="cols actionName">'+data.versionDate+'</span><span class="cols name"><a style="color:#336699" target="_blank" href="clouddisk_downloadfile.jsp?attachId='+data.id+'">下载</a></span></div>';
					}else{
						row +='<div class="row" id=history_'+ data.id +'><span class="cols first" >'+data.name+'</span><span  class="cols name">'+data.size+'</span><span class="cols name2">'+currentNum+'</span><span class="cols actionName">'+data.versionDate+'</span><span class="cols name"><a style="color:#336699" onclick="restoreCurrent('+amId+','+data.id+')">还原</a>&nbsp;&nbsp;<a target="_blank" style="color:#336699" href="clouddisk_downloadfile.jsp?attachId='+data.id+'">下载</a>&nbsp;&nbsp;<a style="color:#336699" onclick="attDel('+amId+','+data.id+')">删除</a></span></div>';
					}
					currentNum--;
				});
				var result = contentCenter+row+"</div>";
				showDialog("info", result,"历史版本", 800 ,250,"history");
				//showDialogHistory('window',result,"历史版本",800);
				//showDialogHistory("info" ,data.msg, "历史版本", 500);
			}
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
}

/**
 * 历史版本还原方法
 */
function restoreCurrent(newId, attId){
	jConfirm("您确定要还原文件吗？","提示",function(r){
		if(!r){
			return;
		}else{
			//window.location.href="clouddisk_list_do.jsp?op=restoreCurrent&newCurId="+newId+"&attachId="+attId;
			$.ajax({
				type:"post",
				url:"clouddisk_list_do.jsp",
				data:{"op":"restoreCurrent","attId":attId,"porposeId":newId},
				success: function(data, status){
					data = $.parseJSON(data);
					if (data.ret=="1") {
						$('#SD_window').hide();
						$("#SD_overlay").hide();
						$.toaster({ priority : 'info', message : data.msg });
						restoreAttAfter(data.newId,data.oldId,data.name,data.docId,data.ext,data.url);
					}else{
						$('#SD_window').hide();
						$("#SD_overlay").hide();
						$.toaster({ priority : 'info', message : data.msg });
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
 * 历史版本还原后HTML
 */
function restoreAttAfter(attId, attOldId, attName,docId,ext,url){
	var curPage = $("#curPage").val();//当前页
	if(curPage == "1"){
		var $li = $("#dirAtt"+attOldId); 
		$li.attr({"title":attName,"attId":attId,"docId":docId,"attExt":ext,"oldAttId":attId,"attName":attName,"id":"dirAtt"+attId});
		
		var $attDirCheckBox = $li.find(".attDirCheckBox");
		$attDirCheckBox.val(attId);
		var $attDirIcon = $li.find(".attDirIcon");
		var $attDirIconA = $attDirIcon.find("a");
		$attDirIconA.attr({"url":"javascript:editdoc('" +docId+ "','" +attId+ "','"+ext+ "')","href":"javascript:editdoc('" +docId+ "','" +attId+ "','"+ext+ "')"});
		var $attDirIconImg = $attDirIcon.find("img");
		if(attId != attOldId){
			 $attDirIconImg.attr("src","images/clouddisk_tiled/"+url);
		}
		var $attDirName = $li.find(".attDirName");
		$attDirName.attr({"id":"attName"+attId}); 
		$attDirName.html("<a href=javascript:editdoc("+docId+ "," +attId+",'"+ext+"')>"+attName+"</a>");
		changeLiStyleNoBorder(attId);
	}else{
		if(attId != "" && attOldId!=""){//如果当前id与最新id都不为空
			var $dl = $("#tree"+attOldId);
			
			$dl.attr({"title":attName,"attId":attId,"docId":docId,"attExt":ext,"oldAttId":attId,"attName":attName,"id":"tree"+attId});
			
			var $fname = $dl.find("dd").find(".fileNameDetail").find(".fname");
			var $openImg = $(".open"+attOldId);
			var name ;
			if(attId == attOldId){//如果当前id与最新id一致说明 文件名相同
				name = "<a href='javascript:editdoc("+docId+", "+attId+", \""+ext+"\")'>"+attName+"</a>";
			}else{
				name = "<a href='javascript:editdoc("+docId+", "+attId+", \""+ext+"\")'>"+attName+"</a>";
				var $cbox = $dl.find("dd").find(".fileNameDetail").find(".cbox");//更新checkbox属性
				$cbox.attr("name","filename"+attId);
				$cbox.val(attId);
				//更新extImg图标
				var $extImg = $dl.find("dd").find(".fileNameDetail").find(".extImg");
				$extImg.attr("src","images/sort/"+url);
				$fname.attr({"id":"span"+attId,"name":"span"+attId,"attId":attId,"amOldId":attId,"ext":ext,"amId":attId,"docId":docId});
				var $file_action = $dl.find("dd").find(".fileNameDetail").find(".file_action").find("ul");
				if(ext!="doc"&& ext!="docx"&& ext!="xls"&& ext!="xlsx"&& ext!="pptx"&& ext!="ppt"&& ext!="dps"&& ext!="wpt"&& ext!="wps"&& ext!="et"){
					$openImg.hide();
					$file_action.find("li:eq(1)").find("a").attr({"id":"history"+attId,"name":"history"+attId,"href":"clouddisk_history_list.jsp?cur=current&attachName="+attName+"&attachId="+attId});
					//$file_action.find("li:eq(1)").find("a").attr({"id":"cooperate"+attId,"name":"cooperate"+attId,"href":"netdisk_public_share.jsp?attachId"+attId});
					$file_action.find("li:eq(2)").find("a").attr({"id":"download"+attId,"name":"download"+attId,"href":"clouddisk_downloadfile.jsp?attachId="+attId});
				}else{
					$openImg.show();
					$file_action.find("li:eq(0)").find("a").attr({"href":"javascript:editdoc("+docId+", "+attId+", \""+ext+"\")"});
					$file_action.find("li:eq(1)").find("a").attr({"id":"history"+attId,"name":"history"+attId,"onclick":""}).click(function(){showDigHistory(attId);});
					//$file_action.find("li:eq(1)").find("a").attr({"id":"cooperate"+attId,"name":"cooperate"+attId,"href":"netdisk_public_share.jsp?attachId"+attId});
					$file_action.find("li:eq(2)").find("a").attr({"id":"download"+attId,"name":"download"+attId,"href":"clouddisk_downloadfile.jsp?attachId="+attId});
				}
				var $op = $dl.find("dd").find(".fileNameDetail").find(".op");
				$op.attr("id","operate"+attId);
				$op.find("li:eq(0)").find("a").attr({"amOldId":attId,"amId":attId,"docId":docId,"oldAmId":attId});
				$op.find("li:eq(1)").find("a").attr({"href":"javascript:showDialogTree('"+attId+"','"+attId+"',"+docId+")"});
				$op.find("li:eq(2)").find("a").attr({"href":"javascript:delAttach('"+attId+"','"+docId+"','"+attId+"')"});
			}
			$fname.html(name);
		}
	}
}
/**
 * 历史版本附件彻底删除（参照 recycler的附件删除）
 * @param attId
 * @return
 */
function attDel(porposeId,attId){
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
			 			$("#history_"+attId).remove();
			 			
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
 * li选中改变li样式 主要应用于重命名 平铺界面
 * @param code
 * @return
 */
function changeLiStyle(code){
	var $li = $("#dirAtt"+code);
	var $attDirCheck =$li.find(".attDirCheckIcon");
	var $attDirCheckBox = $li.find(".attDirCheckBox");
	if($attDirCheckBox.is(":checked")){
		$attDirCheck.css("background","url(images/clouddisk_tiled/check_click.png)");
	}else{
		$attDirCheck.css("background","url(images/clouddisk_tiled/check_default.png)");
	}
	
	$li.css({"border":"1px solid #c0e0f4"});
	
}  

function changeLiStyleNoBorder(code){
	var $li = $("#dirAtt"+code);
	var $inputDirAtt = $li.find('.attDirName').find('.attDirNameInput');
	var $attDirCheck =$li.find(".attDirCheckIcon");
	var length = $(".attDirCheckBox:checked").length;//获得选中的数量
	
	if(length == 0 ){
		$li.css({"border":"1px solid #fff"});
		$attDirCheck.css("background","");
	}
}

/**
 * 删除文件夹
 * @param dirName
 * @param dirCode
 * @return
 */
function delFolder(dirName,dirCode,curPage){
	if (dirCode!="") {
		jConfirm("确定要删除文件夹么?<br/>删除后可以在回收站找回!","提示",function(r){
				if(!r){
					return;
				}
				else{
					$.ajax({
					 		type:"get",
					 		url:"clouddisk_list_do.jsp",
					 		data:{"op":"delFile","dirCode":dirCode},
					 		success:function(data,status){
					 			data = $.parseJSON(data);
					 			if(data.ret == "1"){
						 			//页面上删除文件夹
					 				if(curPage == 1){
					 					$("#dirAtt"+data.dirCode).remove();
							 			deleteAfterStyleByTiled()
					 				}else{
					 					var $dd = $("#folder"+data.dirCode);
							 			var $dl = $dd.parent();
						 				$dl.remove();
							 			deleteAfterStyleByList()
					 				}
						 			 $.toaster({ priority : 'info', message : '删除成功！' });
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
}

/**
 * 角色模板 选择人员
 * @param unit_code
 * @param dirCode
 * @return
 */
function template(unit_code,dirCode){
	$.ajax({
 		type:"get",
 		url:"clouddisk_list_do.jsp",
 		dataType:"json",
 		data:{"op":"templateRole","unit_code":unit_code,"dirCode":dirCode},
 		success:function(data,status){
	 		var table = "<div>";
	 		if(data.result) {
	 			$.each(data.roles,function(index,data){
		 			if(data.isChecked) {
		 				table += "<div><input name='template_ids' class='template_role' type='checkbox' value='"+data.code+"' checked='checked' /><span style='font-size:12px;padding-left:5px; color:#757373'>"+data.name+"</span></div>"
			 		}else {
			 			table += "<div><input name='template_ids' class='template_role' type='checkbox' value='"+data.code+"'/><span style='font-size:12px;padding-left:5px; color:#757373'>"+data.name+"</span></div>";
					 }
				});
				table += "</div>";
				showDialogTemplate('info',table,'请选择角色', 350); 
		 	}else {
		 		jAlert(data.result,"提示");
			}
 			
 			
 		},
 		error:function(XMLHttpRequest, textStatus){
 			alert(XMLHttpRequest.responseText);
 		}
});

}

/**
 * 获取checkbox选中的值
 * @param obj
 * @return
 */
function getAllCheckedValue(obj){
	var checkedObjs = '';
	  $(obj).each(function(i){
		  if($(this).is(":checked")){
			  if(checkedObjs == ''){
				  checkedObjs += $(this).val();
			   }else {
				   checkedObjs += ',' +$(this).val();
			   }
		   }
	  });
	
	  return checkedObjs;
}
/**
 * 角色模板确认
 * @return
 */
function roleTemplateConfirm(){
	 var checkedRoles = '';
	 var unCheckedRoles = '';
	 
	  $('.template_role').each(function(i){
		  if($(this).is(":checked")){
			  if(checkedRoles == ''){
				  checkedRoles += $(this).val();
			   }else {
				  checkedRoles += ',' +$(this).val();
			   }
		   }else{
			   if(unCheckedRoles == ''){
				   unCheckedRoles += $(this).val();
			   }else {
				   unCheckedRoles += ',' +$(this).val();
			   }
		   }
	  });
	var dir_code = curDirCode;
	if (checkedRoles=="") {
			jAlert("请选择要赋予的角色","提示");
			return;
	}
	jConfirm("您确定要赋予这些角色么？","提示",function(r){
		if(!r){
			return;
		}
		else{
			$.ajax({
			 		type:"get",
			 		url:"clouddisk_list_do.jsp",
			 		dataType:"json",
			 		data:{"op":"role_template","check_role_codes":checkedRoles,"uncheck_role_codes":unCheckedRoles,"dir_code":dir_code},
			 		success:function(data,status){
			 			if(data.result){
			 				sd_remove();
			 				jAlert("设定成功！","提示");
			 			}
			 			else{
			 				jAlert("设定失败！","提示");
			 			}
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
			});
		}
	})
}

/**
 * 文件夹重命名 blur
 * @return
 */
function dirChangeName(){
	 var $dir = $(this);
	 var dirCode = $(this).attr("id");
	 var oldName = $(this).attr("oldName");
	 var curPage = $("#curPage").val();//当前页
	 var name = $(this).val();
	 if($.trim(name) == ''){
		 if(curPage == 1){
			 dirChangeNameAfterByTiled(dirCode, oldName);
		 }else{
			 dirChangeNameAfterByList(dirCode,oldName);
		 }
		 $.toaster({ priority : 'info', message : '名称不能为空！' });
		 return;
	 } else{
		if (oldName == name) {
			if(curPage == 1){
				 dirChangeNameAfterByTiled(dirCode, oldName);
			 }else{
				 dirChangeNameAfterByList(dirCode,oldName);
			 }
	 		return;		 	
	 	}else{
	 		$.ajax({
		 		type:"post",
		 		url:"clouddisk_list_do.jsp",
		 		data :{"op":"changeName","code":dirCode,"name":name},
		 		success:function(data, status){
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				if(curPage == 1){
							 dirChangeNameAfterByTiled(data.code,data.name);
						 }else{
							 dirChangeNameAfterByList(data.code,data.name);
						 }
		 				 $.toaster({ priority : 'info', message : '重命名成功！' });
		 				return;
		 			} else {
		 				if(curPage == 1){
							 dirChangeNameAfterByTiled(dirCode, oldName);
						 }else{
							 dirChangeNameAfterByList(dirCode,oldName);
						 }
		 				$.toaster({ priority : 'info', message : data.msg});
			 			return;
					}
		 		},
		 		error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
		 	});
		}
		 
	 }
	 
}
/**
 * 文件夹重命名input框 列表界面
 * @return
 */
function CDirNameBefore(dirCode){
	curDirCode = dirCode;
	var curPage = $("#curPage").val();//当前页
	if(curPage == 1){
		//changeLiStyle(curDirCode);//文件夹重名后 li样式改变
		var dirName = $("#dirName"+dirCode).find("a").html().trim(); 
		var dirInput = '<input type="text" class="attDirNameInput dirInput" id="'+curDirCode+'"  class="attDirNameInput" value="'+dirName+'" oldName="'+dirName+'" />'
 		$("#dirName"+dirCode).html(dirInput);
		$(".dirInput").get(0).select();
	}else{
		var dirName = $("#span"+dirCode).find("a").attr("title");
		var dirInput = "<input id='"+curDirCode+"' class='singleboarder dirInput' oldName='"+dirName+"' value='" + dirName + "' />";
		$("#span"+dirCode).html(dirInput);
		$(".singleboarder").get(0).select();
	}
	
}
/**
 * 文件夹重命名后 平铺界面
 * @return
 */
function dirChangeNameAfterByTiled(dirCode,dirName){
	 var $attDirName =  $("#dirName"+dirCode);
	 var $dirAtt = $("#dirAtt"+dirCode);
	 $dirAtt.attr({"title":dirName,"dirName":dirName});
	 $attDirName.html('<a href="clouddisk_tiled.jsp?userName='+userName+'&op=editarticle&dir_code='+dirCode+'">'+dirName+'</a>');
	 changeLiStyleNoBorder(dirCode)

}

/**目录折叠
 */
function ShowChild(imgobj, name){
	var tableobj = findObj("childof"+name);
	if (tableobj==null){ 
		return;}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("n_puls-root-1.gif")!=-1)
			imgobj.src = "images/n_puls-root.gif";
		if (imgobj.src.indexOf("n_plus-1-1.gif")!=-1)
			imgobj.src = "images/n_plus2-2.gif";
		if (imgobj.src.indexOf("n_plus-1.gif")!=-1)
			imgobj.src = "images/n_plus2-1.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("n_puls-root.gif")!=-1)
			imgobj.src = "images/n_puls-root-1.gif";
		if (imgobj.src.indexOf("n_plus2-2.gif")!=-1)
			imgobj.src = "images/n_plus-1-1.gif";
		if (imgobj.src.indexOf("n_plus2-1.gif")!=-1)
			imgobj.src = "images/n_plus-1.gif";
	}
}

//显示树时候  浮动事件
var treeMouseEnter = function(){
	if(!$(this).hasClass("treeBg")){
		$(this).css("background","#fcfcfc");
	}
	$(this).find("a").removeAttr("href");
	$(this).find("a").css("color","#888");
}
var treeMouseLeave = function(){
	if(!$(this).hasClass("treeBg")){
		$(this).css("background","");
	}
}
//树点击事件
var treeInfoClick = function(){
	if($(this).hasClass("treeBg")){
		$(this).removeClass("treeBg");
	}else{
		$(".showTable").removeClass("treeBg");
		$(this).addClass("treeBg");
		 //var id = $(this).attr("id");
		 //movePDirCode = id.substring(5,id.length);
	}
}
$(".showTable").live({"click":treeInfoClick,"mouseenter":treeMouseEnter,"mouseleave":treeMouseLeave});
//$("#spanTree"+curDirCode).live({"click":treeInfoClick});

/**
 * 文件夹重命名后 列表界面
 * @param oldCode
 * @param dirName
 * @return
 */
function dirChangeNameAfterByList(oldCode,dirName) {
	var cooperateId = $("#cooperateId").val();
	if(oldCode!=""){
		var $dd = $("#folder"+oldCode);
		var $fname = $dd.find(".fileNameDetail").find(".fname");
		var mode = $("#mode").val();
		if(cooperateId == ""){
			var name = "<a title='"+dirName+"'  href='clouddisk_list.jsp?mode=" + mode + "&userName="+userName+"&op=editarticle&dir_code="+oldCode+"' >"+dirName+"</a>";
		}else{
			var name = "<a title='"+dirName+"'  href='clouddisk_network_neighborhood_list.jsp?op=editarticle&mode=" + mode + "&userName="+userName+"&cooperateId="+cooperateId+"&dir_code="+oldCode+"' >"+dirName+"</a>";
		}
		$fname.html(name);
		
	}
}

/**
 * 历史版本还原后 列表界面
 * @param oldCode
 * @param dirName
 * @return
 */
function restoreNameAfterByList(oldCode,dirName) { 
	if(oldCode!=""){
		var $dd = $("#folder"+oldCode);
		var $fname = $dd.find(".fileNameDetail").find(".fname");
		var mode = $("#mode").val();
		var name = "<a title='"+dirName+"'  href='clouddisk_list.jsp?mode=" + mode + "&userName="+userName+"&op=editarticle&dir_code="+oldCode+"' >"+dirName+"</a>";
		$fname.html(name);
		
	}
}

/**
 * 重命名高亮显示
 * @param inputDom
 * @param startIndex
 * @param endIndex
 * @return
 */
function setTextSelected(inputDom, startIndex, endIndex)
{
    if (inputDom.setSelectionRange)
    {  
        inputDom.setSelectionRange(startIndex, endIndex);  
    }   
    else if (inputDom.createTextRange) //IE 
    {
        var range = inputDom.createTextRange();  
        range.collapse(true);  
        range.moveStart('character', startIndex);  
        range.moveEnd('character', endIndex - startIndex-1);  
        range.select();
    }  
    inputDom.focus();  
}
/**
 * 发起协作窗口及附属方法
 */
function openWinUsers2(curDirCode) {
	if (!isApple()){
			if (navigator.userAgent.indexOf('Firefox') >= 0 || navigator.userAgent.toLowerCase().match(/chrome/)!=null){
				openWin('../user_multi_sel2.jsp?userName='+userName+'&dirCode='+curDirCode,window.self,'dialogWidth:850px;dialogHeight:480px;status:no;help:no;');
			}else{
				showModalDialog('../user_multi_sel2.jsp?userName='+userName+'&dirCode='+curDirCode,window.self,'dialogWidth:850px;dialogHeight:480px;status:no;help:no;');
			}
	}else{
		openWin('../user_multi_sel2.jsp?userName='+userName+'&dirCode='+curDirCode, 850, 480);
	}
}

function openWinUsers() {
	if (!isApple()){
			if (navigator.userAgent.indexOf('Firefox') >= 0 || navigator.userAgent.toLowerCase().match(/chrome/)!=null){
				openWin('../user_multi_sel.jsp',window.self,'dialogWidth:850px;dialogHeight:600px;status:no;help:no;');
			}else{
				showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:850px;dialogHeight:600px;status:no;help:no;');
			}
	}else{
		openWin('../user_multi_sel.jsp', 850, 600);
	}
}

function getSelUserNames() {
	return '';
}

function getSelUserRealNames() {
	return '';
}

/**
 * 判断是否存在共享文件
 */
function getSharedDir(userName){
	$.ajax({
		type:"get",
 		url:"clouddisk_list_do.jsp",
 		//dataType:"json",
 		data:{"op":"isSharedDir","userName":userName},
 		cache:false,
 		success:function(data,status){
 			data = $.parseJSON(data);
 			if(data.ret == "1"){
 				var info = data.list;
 				var str=info.split(",");
				//alert(str[i+2]+" "+str[i+1]);
				jConfirm(str[1]+" 向您分享了文件 '"+str[0]+"' ,您是否同意？","提示",function(r){
					if(!r){
						window.location.href="clouddisk_list_do.jsp?op=refusedShared&userName="+userName+"&dirCode="+str[2];
					}else{
						//alert("clouddisk_list_do.jsp?op=refusedDir&userName="+userName+"&dirCode="+str[2]);
						window.location.href="clouddisk_list_do.jsp?op=agreedShared&userName="+userName+"&dirCode="+str[2];
					}
				});
 			}
 			
 		},
 		error:function(XMLHttpRequest, textStatus){
 			//alert(XMLHttpRequest.responseText);
 		}
	});
}

function deCooperateFolder(){
	jConfirm("取消后他人将无法分享该文件夹，确定要取消分享吗？","提示",function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"get",
				url:"clouddisk_list_do.jsp",
				data:{"op":"deCooperateFolder", "dirCode":curDirCode},
		 		success:function(data,status){
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				jAlert("该文件夹已取消分享。","提示");
		 				window.location.reload();
		 			}else{
		 				jAlert("取消分享失败或者该文件未分享他人。","提示");
		 			}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			//alert(XMLHttpRequest.responseText);
		 		}
			})
		}
	})
}


/**
 * 展示二维码
 */
function showBarcode(attId){
	var divx = window.event.clientX+"px";
	var divy = window.event.clientY+"px";
	$.ajax({
		type:"post",
		url:"clouddisk_list_do.jsp",
		data:{"op":"tdcode","attId":attId},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="1") {
				//var contentCenter =' <div class="windowContainCenter"><div class="row"><span class="cols first" >文件名</span><span  class="cols name">大小</span><span class="cols name2">版本</span><span class="cols actionName">更新时间</span><span class="cols name">操作</span></div>';
				//var row ="";
				//var currentNum = data.total;
				//var amId = "";
				
				//showDialog("info", result,"历史版本", 800 ,250,"history");
				//showDialogHistory('window',result,"历史版本",800);
				//showDialogHistory("info" ,data.msg, "历史版本", 500);
				$(".barcode").show();
				$(".barcode").css({"position":"fixed","top":"15%","left":"30%"});
				$("#barcodeImg").attr({"src":data.showPath});
				var img_w = $("#barcodeImg").css("width");
				var img_h = $("#barcodeImg").css("height");
				var x = 450 - img_h.substring(0,3);
				$(".barcode").css({"width":"500px","height":"450px"});
				$(".barcode_img").css({"width":img_w,"height":img_h,"margin-top":x/2});
			}
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
	
}

function delBarcode(){
	$(".barcode").hide();
}






