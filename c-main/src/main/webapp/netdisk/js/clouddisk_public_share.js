var curDirCode;//用于右键当前dirCode
var dirCode; //页面中的dirCode
var userName;
var mAttExtType;//附件权限判断 类型
var attId = 0;//右键菜单 attId
var movePDirCode = '';//移动到某个资源目录下的 code
var pageNo;

//公共文件夹菜单
var contextPublicDirMenu = {
	menuId: 'contextPublicDirMenu',
	onContextMenuItemSelected:function(menuItemId, $triggerElement){
	var $dd = $triggerElement.parents("dd");
	var canMappingAddress = $dd.attr("canMappingAddress");
	curDirCode = $dd.attr("dirCode");
		if( menuItemId == 'priv'){
			openWin("../admin/netdisk_public_dir_frame.jsp?root_code="+curDirCode, 800, 600);
		}else if(menuItemId == 'down'){
			if(canMappingAddress == 'true'){
				var mappingAddress = $dd.attr("mappingAddress");
				window.open("clouddisk_download_public_dir.jsp?mappingAddress=" + mappingAddress);
			}else{
				window.open("clouddisk_download_public_dir.jsp?code=" + curDirCode);
			}
		}
		
	},
	onContextMenuShow:function($triggerElement){
		var canUserManage = false;
		var canMappingAddress = "false"
		if( pageNo == 1 ){
			var $dd = $triggerElement.parents("dd");
			canUserManage =$dd.attr("canUserManage"); //公共文件夹上传者
			canMappingAddress = $dd.attr("canMappingAddress");
			curDirCode = $dd.attr("dirCode");
		}else{
			canUserManage = $triggerElement.attr("canUserManage"); //公共文件夹上传者
			canMappingAddress = $triggerElement.attr("canMappingAddress");
			curDirCode = $triggerElement.attr("dirCode");
		}
		if(canMappingAddress == 'true'){
			$("#contextPublicDirMenu #priv").hide();
		}else{
			if(canUserManage == 'true'){
				$("#contextPublicDirMenu #priv").show();
			}else{
				$("#contextPublicDirMenu #priv").hide();
			}		
			
		}
		
	},
	showShadow:false
}

//公共文件附件菜单
var contextPublicAttachMenu = {
	menuId: 'contextPublicAttach',
	onContextMenuItemSelected:function(menuItemId, $triggerElement){
		var canMappingAddress = 'false';
		var mappingAddress = '';
		//列表重命名
		if( pageNo == 1 ){
			 attId = $triggerElement.parents("dd").attr("attId");
			 canMappingAddress = $triggerElement.parents("dd").attr("canMappingAddress");
			 mappingAddress = $triggerElement.parents("dd").attr("mappingAddress");
		}else{
			 attId = $triggerElement.attr("attId");
			 canMappingAddress = $triggerElement.attr("canMappingAddress");
			 mappingAddress = $triggerElement.attr("mappingAddress");
		}
		var extType = 0;
		var $publicShareAtt = $("#publicShareAtt"+attId);
		extType = $publicShareAtt.attr("extType");//获得附件类型
		if(menuItemId == "down"){//下载
			if(canMappingAddress=='true'){
				window.location.href="netdisk_mapping_downloadfile.jsp?mappingAddress="+mappingAddress;
			}else{
				window.location.href="netdisk_public_downloadfile.jsp?id="+attId;
			}
		}else if(menuItemId == "delete"){//删除
			deleteAtt(attId);
		}else if(menuItemId == "see"){//浏览
			if(extType == 1){
				showPublicImg(attId);//图片预览
			}else if(extType == 5){
				showPublicTxt(attId);//文档预览
			}else if(extType == 6){
				editdoc(attId,0);
			}
		}else if(menuItemId == "edit"){//在线编辑
			editdoc(attId);
		}else if(menuItemId == "move"){//移动
			showPublicTree();
		}else if(menuItemId =="reName"){//重命名
			changeNameBefore(attId);
		}
	},
	onContextMenuShow:function($triggerElement){
		var attId = 0;
		var canMappingAddress = 'false';
		if( pageNo == 1 ){
			 attId = $triggerElement.parents("dd").attr("attId");
			 canMappingAddress = $triggerElement.parents("dd").attr("canMappingAddress");
		}else{
			 attId = $triggerElement.attr("attId");
			 canMappingAddress = $triggerElement.attr("canMappingAddress");
		}
		if(canMappingAddress == 'true'){
			menuAttPriv(-1);
		}else{
			menuAttPriv(attId);
		}
		
	},
	showShadow:false
}
$(function(){
	var browserFixfox = navigator.userAgent.indexOf("Firefox")!=-1;
	pageNo = $("#pageNo").val();
	if( pageNo == 1){
		 $("#view_1").css({"background":"url(images/clouddisk/view_list_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_thumbnail_2.gif)"});
	}else{
		 $("#view_2").css({"background":"url(images/clouddisk/view_thumbnail_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_list_2.gif)"});
	}
	//权限判断 显示上传 和 删除
	var canManage = $("#canManage").val();
	var canInsert = $("#canInsert").val();
	//判断是否为隐射目录
	var isMappingDir = $("#isMappingDir").val();
	dirCode = $("#dirCode").val();
	//初始化上传控件
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
	initSwfUpload({
		"upload_url":uploadUrl,
		"post_params":{
			"type": '0',
			"dirCode":dirCode
		},
		"file_types":uploadFileType,
		"file_upload_limit":fileUploadLimit,
		"file_size_limit":fileSizeLimit
	});
	if(isMappingDir == 'true'){
		$(".deleteFile_c").hide();
		$(".managerRoot").hide();
		$(".uploadFile_c").hide();
		
	}else{
		if(canManage == 'true'){
			$(".deleteFile_c").show();
			$(".managerRoot").show();
			if(dirCode=='root'){
				$(".managerRoot").show();
			}else{
				$(".managerRoot").hide();
			}
		}else{
			$(".deleteFile_c").hide();
			$(".managerRoot").hide();
		}
		if(canInsert == 'true'){
			$(".uploadFile_c").show();
		}else{
			$(".uploadFile_c").hide();
		}
	}
	
	//选择文件
	userName = $("#userName").val();
	var which = $("#which").val();
	var cate ='';
	if(which == 1){
		cate = "imgShow";
	}else if(which == 2){
		cate = "fileShow";
	}else if(which == 3){
		cate = "videoShow";
	}else if(which ==4){
		cate = "musicShow";
	}
	if(cate != ''){
		$("."+cate).find("img").attr("src",'images/clouddisk/'+$("."+cate).attr("class")+'_2.gif')
	}
	var content = $("#content").val();
	if(content != ''){
		$("#select_content").val(content);
	}else{
		$("#select_content").val("请输入文件名搜索...");
	}
	//搜索 内容
	$("#select_content").live({"blur":select_con,"keydown":function(e){
		if(e.keyCode==13){
			this.blur();
		}
	 }});
	//搜索按钮
	$("#search_one").click(select_con);
	//图片分类显示
	$(".sortShow ul li").click(attCategory);
	//删除文件夹
	$(".file_action ul li #publicDirDel").click(function(e){
		e.stopPropagation();
		var dirCode = $(this).parents("dd").attr("dirCode");
		deletePublicDir(dirCode);
	})
	if( pageNo == 1){
		//文件夹附件 右键菜单
		$(".dirName").contextMenu(contextPublicDirMenu);
		//文件附件 右键菜单
		$(".attName").contextMenu(contextPublicAttachMenu);
	}else{
		$(".publicShareAtt").contextMenu(contextPublicAttachMenu);
		$(".publicShareDir").contextMenu(contextPublicDirMenu);
	}
	//根目录 管理权限
	$(".managerRoot").click(function(){
		openWin("../admin/netdisk_public_dir_frame.jsp?root_code=root", 800, 600);
	})
	$(".managerRoot").mouseover(function(){
		$(this).css("background","url(images/clouddisk/manage.gif)");
	}).mouseleave(function(){
		$(this).css("background","url(images/clouddisk/manage1.gif)");
	})
	
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
	//附件浮动 重命名
	$(".attReName").live("click",function(){
		var $dd = $(this).parents("dd");
		var attId = $dd.attr("attId");
		changeNameBefore(attId);
		
	})
	//附件重命名 焦点移除事件
	 $(".publicShareAttInput").live({"blur":changeName,"keydown":function(e){
			if(e.keyCode==13){
				this.blur();
			}
	 }});
	//附件树显示事件
	$(".attMove").live("click",function(){
		var $dd = $(this).parents("dd");
		attId = $dd.attr("attId");
		showPublicTree();
		
	})
	//图片在线预览
	$(".attImgShow").live("click",function(){
		var $dd = $(this).parents("dd");
		attId = $dd.attr("attId");
		showPublicImg(attId);//图片预览
	})
	//txt在线预览
	$(".attTxtShow").live("click",function(){
		var $dd = $(this).parents("dd");
		attId = $dd.attr("attId");
		showPublicTxt(attId);//图片预览
		
	})
	//office文件在线编辑
	$(".attOfficeEdit").live("click",function(){
		var $dd = $(this).parents("dd");
		attId = $dd.attr("attId");
		editdoc(attId);
	})
	//附件删除
	$(".delAtt").live("click",function(){
		var $dd = $(this).parents("dd");
		var attId = $dd.attr("attId");
		deleteAtt(attId);
	})
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
			 var id = $(this).attr("id");
			 movePDirCode = id.substring(5,id.length);
		}
		
	}
	$(".showTable").live({"click":treeInfoClick,"mouseenter":treeMouseEnter,"mouseleave":treeMouseLeave});
	//移动确定事件
	$("#SD_confirm").live({"click":function(){
		movePublicAtt(attId,movePDirCode);
	}});
	//
	$("#SD_share_cancel,#SD_share_close").bind("click",function(){
		sd_share_remove()
	});
	
	//平铺列表界面切换
	$("#view_1").click(
		  function(){
		    $(this ).css({"background":"url(images/clouddisk/view_list_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_thumbnail_2.gif)"});
		    changePage(1);
		  }); 
	$("#view_2").click(
		  function(){
		   $(this).css({"background":"url(images/clouddisk/view_thumbnail_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_list_2.gif)"});
		    changePage(2);
		  }); 
	 //图片分页
	$(".showImg_Next").live("click",function(){
		var arrow = $(this).attr("value");
		showImgNext("clouddisk_public_dir_do.jsp",arrow,"");
	})
})
/**
 * 搜索按钮
 * @return
 */
function select_con(){
	var content = $("#select_content").val();
	var url = "";
	if( pageNo == 1 ){
		url = "clouddisk_pubilc_share.jsp";
	}else if(pageNo == 2){
		url = "clouddisk_public_share_tiled.jsp";
	}
	if($.trim(content) == ""){
		return;
	}
	else{
		url += "?op=search&select_sort=select_one&select_content="+encodeURI(content);
		window.location.href = url;
	}
}
/**
 * 附件类别
 * @return
 */
function attCategory(){
	var liClass = $(this).attr("class");
	var which;
	if(liClass=='imgShow'){
		which = 1;
	}else if(liClass == 'fileShow'){
		which = 2;
	}else if(liClass == 'videoShow'){
		which = 3;
	}else if(liClass == 'musicShow'){
		which =4;
	}else{
		which = 1;
	}
	var url = "";
	if( pageNo == 1 ){
		url = "clouddisk_pubilc_share.jsp";
	}else if(pageNo == 2){
		url = "clouddisk_public_share_tiled.jsp";
	}
	
	url += "?op=search&select_file=select_file&select_which="+which;
	window.location.href= url;
}
/**
 * 删除公共文件夹
 * @param dirCode
 * @return
 */
function deletePublicDir(dirCode){
	jConfirm("确定要删除公共文件夹么?","提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
		 		type:"post",
		 		url:"clouddisk_public_dir_do.jsp",
		 		dataType:"json",
		 		data:{ "dirCode":dirCode,"op":"delPublicDir" },
		 		success:function(data,status){
		 			var res = data.result;
		 			if(res){
		 				$("#dir"+dirCode).remove(); //删除但是有歧义
		 				$.toaster({ priority : 'info', message : '文件夹删除成功！' });
		 			}else{
		 				$.toaster({ priority : 'info', message : '文件夹删除失败！' });
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
 * 右键菜单的附件
 * @param dir_code
 * @return
 */
function menuAttPriv(attId){
	if( attId == 0 ){
		$("#contextPublicAttach #down").hide();
		$("#contextPublicAttach #see").hide();
		$("#contextPublicAttach #edit").hide();
		$("#contextPublicAttach #delete").hide();
		$("#contextPublicAttach #move").hide();
		$("#contextPublicAttach #reName").hide();
	}else if(attId == -1){
		$("#contextPublicAttach #see").hide();
		$("#contextPublicAttach #edit").hide();
		$("#contextPublicAttach #delete").hide();
		$("#contextPublicAttach #move").hide();
		$("#contextPublicAttach #reName").hide();
	}else{
		var extType = 0;
		var canUserManage = 'false';
		var canMappingAddress = 'false';
		var $publicShareAtt = $("#publicShareAtt"+attId);
		extType = $publicShareAtt.attr("extType");//获得附件类型
		canUserManage = $publicShareAtt.attr("canUserManage");
		if(extType == 1){//图片类型
			$("#contextPublicAttach #see").show();
			$("#contextPublicAttach #edit").hide();
		}else if(extType == 5){//txt类型
			$("#contextPublicAttach #see").show();
			$("#contextPublicAttach #edit").hide();
		}else if(extType == 6){
			if(canUserManage == 'true'){
				$("#contextPublicAttach #see").hide();
				$("#contextPublicAttach #edit").show();
			}else{
				$("#contextPublicAttach #see").show();
				$("#contextPublicAttach #edit").hide();
			}
		}else{
			$("#contextPublicAttach #see").hide();
			$("#contextPublicAttach #edit").hide();
		}
		$("#contextPublicAttach #down").show();
		if(canUserManage == 'false' ){
			$("#contextPublicAttach #delete").hide();
			$("#contextPublicAttach #move").hide();
			$("#contextPublicAttach #reName").hide();
		}else{
			$("#contextPublicAttach #delete").show();
			$("#contextPublicAttach #move").show();
			$("#contextPublicAttach #reName").show();
		}
		
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
 * 重命名前
 * @param attId
 * @return
 */
function changeNameBefore(attId){
	 var $attName = null;
	 var $parent = $("#publicShareAtt"+attId);
	 var nameText = $parent.attr("attName");
	 var inputInfo = '';
	 if(pageNo == 1){
		$attName = $parent.find(".attName");
		inputInfo = "<input id='"+ attId +"' class='publicShareAttInput dirAttInputText' oldName='"+nameText+"' value='"+nameText+"' />";
	 }else{
		changeLiStyle(attId);
		$attName = $parent.find(".attDirName");
		inputInfo = "<input id='"+ attId +"' class='publicShareAttInput attDirNameInput' oldName='"+nameText+"' value='"+nameText+"' />";
	 }
	//var inputInfo = "<input id='"+ attId +"' class='attInput dirAttInputText' oldName='"+nameText+"' value='"+nameText+"' />";
	$attName.html(inputInfo);
	var val =  $("#"+attId).val();
	var endLen = val.lastIndexOf(".");//文本高亮显示
	if(endLen != 0){
		var name = val.substring(0,endLen);
		setTextSelected($("#"+attId).get(0),0,endLen);
	}else{
		 $("#"+attId).get(0).focus();
	}
}
/**
 * 重命名
 * @return
 */
 function changeName(){
	 var $attInput = $(this);
	 var oldName = $attInput.attr("oldName");
	 var name = $attInput.val();
	 var attId = $attInput.attr("id");
	 if(name == '' || name == null){
		changeNameAfter(attId,oldName);
		$.toaster({ priority : 'info', message : '名称不能为空！' });
		return;
	 }else{
		 var endLen = name.lastIndexOf(".");//文本高亮显示
		 if(endLen == 0){
			 $.toaster({ priority : 'info', message : '名称不能为空！' });
			 changeNameAfter(attId,oldName,'','');
			 return; 
		 }
		 if(oldName == name){
			 changeNameAfter(attId,oldName,'','');
			 return;
		 }
	 }
	 $.ajax({
	 		type:"post",
	 		url:"clouddisk_public_dir_do.jsp",
	 		dataType:"json",
	 		data:{ "attId":attId,"attName":name,"op":"changeAttName" },
	 		success:function(data,status){
	 			if(data.result == 1){
	 				 changeNameAfter(attId,name,data.imgSrc,data.extType);
	 				$.toaster({ priority : 'info', message : '重命名成功！' });
	 			}else{
	 				changeNameAfter(attId,oldName,'','');
	 				$.toaster({ priority : 'info', message : '重命名失败！' });
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
		});
 }
 /**
  * 重命名后样式调整
  * @param attId
  * @param attName
  * @return
  */
 function changeNameAfter(attId,attName,imgSrc,extType){
	 var $attName = null;
	 var $parent = $("#publicShareAtt"+attId);
	 $parent.attr("attName",attName);
	 if(extType!=''){
		 $parent.attr("extType",extType);
	 }
	
	 if(pageNo == 1){
		$attName = $parent.find(".attName");
	 }else{
		$attName = $parent.find(".attDirName");
	 }
	 if(imgSrc!=''){
		 if(pageNo == 1){
			 $parent.find(".extImg").attr("src","images/sort/"+imgSrc); 
		 }else{
			 $parent.find(".extImg").attr("src","images/clouddisk_tiled/"+imgSrc); 
		 }
		
	 }
	
	var content = '<a target="_blank" href="netdisk_public_downloadfile.jsp?id='+attId+'">'+attName+'</a>';
    
	$attName.contextMenu(contextPublicAttachMenu);
    $attName.html(content);
	 
 }
/**
 * 删除文件夹 
 * 附件
 * @param attId
 * @return
 */
 function deleteAtt(attId){
	 jConfirm("确定要删除附件么?","提示",function(r){
			if(!r){return;}
			else{
				 $.ajax({
				 		type:"post",
				 		url:"clouddisk_public_dir_do.jsp",
				 		dataType:"json",
				 		data:{ "attId":attId,"op":"delAtt"},
				 		success:function(data,status){
				 			if(data.result){
				 				$("#publicShareAtt"+attId).remove();
				 				if(pageNo == 1){
				 					deleteAfterStyleByList();
				 				}else{
				 					deleteAfterStyleByTiled();
				 				}
				 				
				 				$.toaster({ priority : 'info', message : '删除成功！' });
				 			}else{
				 				
				 				$.toaster({ priority : 'info', message : '删除失败！' });
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
 * 图片在线预览
 * @param attId
 * @return
 */
 function showPublicImg(attId){
	 $.ajax({
	 		type:"post",
	 		url:"clouddisk_public_dir_do.jsp",
	 		dataType:"json",
	 		data:{ "attId":attId,"op":"showPublicImg" },
	 		success:function(data,status){
	 			if(data.ret == 1){
	 				showDialog("info",attId,"图片预览",data.width,data.height,"showImg",data.downloadUrl);
	 			}else{
	 				jAlert("图片不存在","提示");
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
		});
 }
 /**
  * 文档预览
  * @param attId
  * @return
  */
 function showPublicTxt(attId){
	 $.ajax({
	 		type:"post",
	 		url:"clouddisk_public_dir_do.jsp",
	 		dataType:"json",
	 		data:{ "attId":attId,"op":"showPublicTxt" },
	 		success:function(data,status){
	 			if(data.result){
	 				var txtContent = data.txtContent;
	 				showDialog("info",txtContent,"文档预览",800,350,1);
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
		});
	
 }
 /**
  * 
  * tree显示
  * @return
  */
 function showPublicTree(){
	 $.ajax({
	 		type:"post",
	 		url:"clouddisk_public_dir_do.jsp",
	 		dataType:"json",
	 		data:{"op":"showPublicTree" },
	 		success:function(data,status){
	 			if(data.result){
	 				var treeContent = data.treeContent;
	 				showDialog("confirm",treeContent,"移动文件",500,400);
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
		});
 }
 /**
  * 关闭dialog 插件中写了 依赖于插件
  * @return
  */
 function closeDialog(){
	 
 }
 /**
  * 移动事件
  * @param attId
  * @param publicDirCode
  * @return
  */
 function movePublicAtt(attId,publicDirCode){
	 if(attId != '' && publicDirCode != ''){
		 if(publicDirCode == dirCode){
			 jAlert("移动附件成功！","提示");
		 }else{
			 $.ajax({
			 		type:"post",
			 		url:"clouddisk_public_dir_do.jsp",
			 		dataType:"json",
			 		data:{"op":"movePublicAtt","attId":attId,"publicDirCode":publicDirCode},
			 		success:function(data,status){
			 			if(data.result == 1){
			 				$("#publicShareAtt"+attId).remove();
			 				jAlert("移动附件成功！","提示");
			 			}else if(data.result == -1){
			 				jAlert("移动附件成功！","提示");
			 			}else{
			 				jAlert("目标文件夹内有重名文件存在！请更改文件名称！","提示");
			 			}
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
				});
		 }
	 } 
 }
/**
 * 批量删除
 * @return
 */
 function delBatch() {
		var ids = getAllCheckedValue("input[name='att_ids']");
		if (ids=="") {
			jAlert("请先选择文件！","提示");
			return;
		}
		jConfirm("确定要批量删除么?","提示",function(r){
			if(!r){return;}
			else{
				$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
				$.ajax({
			 		type:"get",
			 		url:"clouddisk_public_dir_do.jsp",
			 		dataType:"json",
			 		data:{"op":"delBatchAtt","att_ids":ids},
			 		success:function(data,status){
			 			$("#loading").css({"display":"none"});
			 			$(".treeBackground").css({"display":"none"});
			 			$(".treeBackground").removeClass("SD_overlayBG2");
			 			if(data.result){
			 				var attId = new Array();
			 				attId = ids.split(",");
			 				for(var i=0; i<attId.length; i++ ){
			 					$("#publicShareAtt"+attId[i]).remove();
			 					if(pageNo == 1){
			 						deleteAfterStyleByList();
			 					}else{
			 						deleteAfterStyleByTiled();
			 					}
			 					
			 				}
			 				$.toaster({ priority : 'info', message : '删除成功！' });
			 			}else{
			 				$.toaster({ priority : 'info', message : '删除失败！' });
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
 
 function sd_share_remove() {
	 $("#SD_share_table").css("margin","-2000px");
 }
 //极速上传
 function showDigFile(){
	 $("#SD_share_table").css("margin","100px"); 
 }

/**
 * 界面切换
 * @param pageNo
 * @return
 */
 function changePage(pageNo){
	var url = "";
	if( pageNo == 1 ){
		url = "clouddisk_pubilc_share.jsp";
	}else if(pageNo == 2){
		url = "clouddisk_public_share_tiled.jsp";
	}
	var select_sort = $("#isSearch").val();
	var which = $("#which").val();
	var select_file = $("#fileType").val();
	var text_content = $("#content").val();
	if(select_sort == 'select_one') {
		url += "?op=search&select_sort=select_one&select_content="+encodeURI(text_content)+"&dir_code="+dirCode;
	}else{
		if(select_file == 'select_file') {
			url += "?op=search&select_file=select_file&select_which="+which+"&dir_code="+dirCode;
    	}else{
    		var mappingAddress = $("#mappingAddress").val();
    		var mappingCode = $("#mappingCode").val();
    		url += "?mappingCode="+mappingCode+"&dir_code="+dirCode+"&mappingAddress="+mappingAddress;
        }
	}
	window.location.href = url;
 }
 /**
  * 重命名前 平铺界面
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


/**
 * 重名名 后 平铺界面
 * @param code
 * @return
 */

function changeLiStyleNoBorder(code){
	var $li = $("#publicShareAtt"+code);
	var $inputDirAtt = $li.find('.attDirName').find('.attDirNameInput');
	var $attDirCheck =$li.find(".attDirCheckIcon");
	var length = $(".attDirCheckBox:checked").length;//获得选中的数量
	
	if(length == 0 ){
		$li.css({"border":"1px solid #fff"});
		$attDirCheck.css("background","");
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
 * 编辑doc文件+
 * @param id
 * @return
 */
function editdoc(id,type)
{
	var isUseNTKO = $("#isUseNTKO").val();
	if(isUseNTKO == 'true'){
		openWin("netdisk_public_office_ntko_edit.jsp?type="+type+"&id=" + id, 1100, 800);	
	}else{
		rmofficeTable.style.display = "";
		redmoonoffice.AddField("id", id);
		redmoonoffice.Open("../netdisk/netdisk_public_office_get.jsp?id=" + id);
	}
}
/**
 * 移动界面 显示展开节点
 * @param imgobj
 * @param name
 * @return
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

function shrink() { 
   for(var i=0; i<document.images.length; i++) {
		var imgObj = document.images[i];
		try {
			if (imgObj.tableRelate!="") {
			ShowChild(imgObj, imgObj.tableRelate);
			}
		}catch (e) {
		}
   }
}
/**
 * 找某个DOM对象
 * @param theObj
 * @param theDoc
 * @return
 */
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}
function onAddFile(index, fileName, filePath, fileSize, modifyDate) {
}

/**
 * 上传
 * @return
 */
function SubmitWithFileThread() {
	webedit.AddField("op", "add")
	webedit.AddField("dirCode", dirCode)
	$("#SD_share_table").css("margin","100px");
	webedit.Upload();
	window.setTimeout("checkResult()",200);
}

function onDropFile(filePaths) {
	var ary = filePaths.split(",");
	var hasFile = false;
	for (var i=0; i<ary.length; i++) {
		var filePath = ary[i].trim();
		if (filePath!="") {
			hasFile = true;
			webedit.InsertFileToList(filePath);
		}
	}
	if (hasFile)
		SubmitWithFileThread();
}

function OfficeOperate() {
	alert(redmoonoffice.ReturnMessage.substring(0, 4)); // 防止后面跟乱码
}
/**
 * 检查结果
 * @return
 */
function checkResult() {
	if (webedit.ReturnMessage.trim() == "操作成功！") {
		doAfter(true,dirCode);
	}else {
		window.setTimeout("checkResult()",200);
	}
}
/**
 * 刷新界面
 * @param isSucceed
 * @param dirCode
 * @return
 */
function doAfter(isSucceed,dirCode) {
	var url = '';
	if(pageNo == 1){
		url = "clouddisk_pubilc_share.jsp";
	}else{
		url = "clouddisk_public_share_tiled.jsp";
	}
	if (isSucceed) {
		window.location.href = url+"?dir_code="+dirCode;
	}
	else {
		alert(webedit.ReturnMessage);
	}
}




	

