
$(function(){
	var isSearch = $("#isSearch").val();
	//搜索和列表界面合并为一个界面  时间 与所在目录的隐藏显示
	if( isSearch == 'select_one' ){
		$('.newFolder').hide();//新建文件夹显示
		$('.uploadFile_c').hide();
	}else{
		$('.newFolder').show();//新建文件夹显示
		$('.uploadFile_c').show();
		
	}
	/**
	 * 1.平铺界面中 共同的js效果 主要关于li 
	 */
	
	/**
	 * 1.1 li鼠标进入鼠标离开效果
	 */
	var attDirMouseEnter = function(){
		
		var length = $(".attDirCheckBox:checked").length;//获得选中的数量
		var $li = $(this);
		if($li.find(".attDirCheckBox").is(":checkbox")){
			var $inputDirAtt = $('.attDirGroup li .attDirName .attDirNameInput');
			var $attDirCheck =$li.find(".attDirCheckIcon");
			var $cb = $li.find('.attDirCheckBox');
			if(!$cb.is(":checked")){
				$li.css({"border":"1px solid #c0e0f4"});
				$attDirCheck.css("background","url(images/clouddisk_tiled/check_default.png)");
			}else{
				$li.css({"border":"1px solid #c0e0f4"});
				$attDirCheck.css("background","url(images/clouddisk_tiled/check_click.png)");
			}
		}
		
	}
	/**
	 * 1.2鼠标离开
	 */
	
	var attDirMouseLeave = function(){
		var $li = $(this);
		if($li.find(".attDirCheckBox").is(":checkbox")){
			var $inputDirAtt = $li.find('.attDirName').find('.attDirNameInput');
			var $attDirCheck =$li.find(".attDirCheckIcon");
			var length = $(".attDirCheckBox:checked").length;//获得选中的数量
			
			if(length == 0 && !$inputDirAtt.is(":input")){
				$li.css({"border":"1px solid #fff"});
				$attDirCheck.css("background","");
			}
		}
	}

	$(".attDirGroup li").live({"mouseenter":attDirMouseEnter,"click":attDirCheckLiClick,"mouseleave":attDirMouseLeave});
	
	
	/**
	 *1.2 选中图标浮上离开事件
	 */
	var attDirCheckIconMe = function(e){
		e.stopPropagation();
		var $checkIcon = $(this);
		var $li = $checkIcon.parent();
		var $cb = $li.find('.attDirCheckBox');
		if(!$cb.is(":checked")){
			$li.css({"border":"1px solid #c0e0f4"});
			$checkIcon.css("background","url(images/clouddisk_tiled/check_move.png)");
		}
	}
	var attDirCheckIconMl = function(e){
		e.stopPropagation();
		var $checkIcon = $(this);
		var $li = $checkIcon.parent();
		var $cb = $li.find('.attDirCheckBox');
		
		if(!$cb.is(":checked")){
			$li.css({"border":"1px solid #c0e0f4"});
			$checkIcon.css("background","url(images/clouddisk_tiled/check_default.png)");
		}else{
			$li.css({"border":"1px solid #c0e0f4"});
			$checkIcon.css("background","url(images/clouddisk_tiled/check_click.png)");
		}
	}

	$(".attDirGroup li .attDirCheckIcon").live({"mouseenter":attDirCheckIconMe,"mouseleave":attDirCheckIconMl,"click":attDirIconCheck});
	
	/**
	 * 1.3取消全选
	 */
	var cancleCheckMe = function(){
		$(this).css("text-decoration","underline");
	}
	var cancleCheckMl = function(){
		$(this).css("text-decoration","none");
	}
	var cancleClick = function(){
		$('.attDirGroup li .attDirIcon a').each(function(i){
			var url = $(this).attr("url");
			$(this).attr("href",url);
		})
		$(".attDirGroup li").css({"border":"1px solid #fff","background":""});
		$(".attDirGroup li .attDirCheckIcon").css("background","");
		$(".attDirGroup li .attDirCheckBox").attr("checked",false);
		$(this).parent().hide();
		$("#dirAttAllCbox").attr("checked",false);
		$("#dirAttAllCboxIcon").css("background","url('images/clouddisk/checkbox_1.png')");
	}
	
	$('.rHead2 .checkboxDetail .cancle').live({"mouseenter":cancleCheckMe,"mouseleave":cancleCheckMl,"click":cancleClick})
	
	/**
	 * 1.4所有文件
	 */
	
	var  dirAttAllCboxIcon = function (){
		var $allCboxIcon = $(this);
		var $parent = $allCboxIcon.parent();
		var $allCbox = $parent.find("#dirAttAllCbox");
		if($allCbox.is(":checked")){
			$(".attDirGroup li").css({"border":"1px solid #fff","background":""});
			$(".attDirGroup li .attDirCheckIcon").css("background","");
			$(".attDirGroup li .attDirCheckBox").attr("checked",false);
			$('.rHead2 .checkboxDetail').hide();
			$allCbox.attr("checked",false);//全选取消
			$allCboxIcon.css("background","url('images/clouddisk/checkbox_1.png')");//颜色改变
			//恢复所有的超链接属性
			$('.attDirGroup li .attDirIcon a').each(function(i){
				var url = $(this).attr("url");
				$(this).attr("href",url);
			})
		}else{
			$allCbox.attr("checked",true);
			$allCboxIcon.css("background","url('images/clouddisk/checkbox_3.png')");
			$('.attDirGroup li .attDirIcon a').removeAttr("href");
			$(".attDirGroup li .attDirCheckBox").each(function(index){
				$(this).attr("checked",true);
				$(this).parent().css({"border":"1px solid #c0e0f4","background":"#ecf6fe"});
				$(this).parent().find(".attDirCheckIcon").css("background","url(images/clouddisk_tiled/check_click.png)");
			});
			var $cbInfo = $('.rHead2 .checkboxDetail');
			
			var length = $(".attDirCheckBox:checked").length;//获得选中的数量
			if(length > 0){
				$cbInfo.show();
				$cbInfo.find(".length").text('已选择了'+length+'个文件,');
				$cbInfo.find('.cancle').text('取消');
			}
		}
	}
	//所有文件选择取消
	$("#dirAttAllCboxIcon").click(dirAttAllCboxIcon);
	
	/**
	 * 2.2附件右键
	 */
	//平铺图标右键功能 
	var contextMenuInfo = {
 		menuId: 'contextMenuAttachMent',
 		onContextMenuItemSelected:function(menuItemId, $triggerElement){
			var $attDirName = $triggerElement.find(".attDirName");
			var attId = $triggerElement.attr('attId');//附件id
			var attName = $triggerElement.attr('attName');//附件名称
			var attExt = $triggerElement.attr('attExt');//文件后缀名
			var oldAttId = $triggerElement.attr('oldAttId');//附件id
			var docId = $triggerElement.attr('docId');//附件名称
			var isImgSearch = $triggerElement.attr('isImgSearch');
			var extType = $(".attExtType"+oldAttId).val();//获得该att的config参数
 			if( menuItemId == 'reName' ){ //文件重命名
 				changeLiStyle(attId);
 				var dirInput = '<input type="text" class="attDirNameInput attInput" id="'+attId+'" attExt="'+attExt+'" attOldId="'+oldAttId+'" docId="'+docId+'"  value="'+attName+'" oldName="'+attName+'" />'
 				$attDirName.html(dirInput);
 				var val =  $("#"+attId).val();
				var endLen = val.lastIndexOf(".");//文本高亮显示
				if(endLen != 0){
					var name = val.substring(0,endLen);
					setTextSelected($("#"+attId).get(0),0,endLen);
				}else{
					 $("#"+attId).get(0).focus();
				}
			}else if(menuItemId == 'open'){
				if(extType == 1){
					showImg(attId,isImgSearch);
				}else if(extType == 5){
					edittxt(docId,attId,attExt);
				}else if(extType == 6){
					editdoc(docId,attId,attExt);
				}
			}else if(menuItemId == 'delete'){
				delAttach(attId, docId);//批量删除文件
			}else if(menuItemId == 'download'){
				window.location.href("clouddisk_downloadfile.jsp?attachId="+attId);
			}else if(menuItemId == 'move'){
				showDialogTree(parseInt(attId),parseInt(oldAttId), parseInt(docId));
			}else if(menuItemId == 'history'){
				//window.location.href("clouddisk_history_list.jsp?cur=current&attachName="+attName+"&attachId="+attId);
				showDigHistory(attId);
			}else if(menuItemId == 'publicShare'){
				window.location.href = "netdisk_public_share.jsp?pageNo=1&attachId=" + attId;
			}
		},
		onContextMenuShow:function($triggerElement){
			var oldAttId = $triggerElement.attr('oldAttId');
			var ext = $triggerElement.attr('ext');
			var extType = $(".attExtType"+oldAttId).val();
			$("#contextMenuAttachMent li:eq(0)").show();
			if(extType != "1" && extType != "5" && extType != "6"){
				$("#contextMenuAttachMent li:eq(0)").hide();
			}
		},
		showShadow:false
	}
	//所有文件附件绑定右键操作
	$(".att").contextMenu(contextMenuInfo);
	
	//文件夹附件重命名
	 $(".attInput").live({"blur":attChangeName,"keydown":function(e){
			if(e.keyCode==13){
				this.blur();
			}
		 }});
})
/**
 *li选中事件 --------js共同效果中
 * @param $li
 * @return
 */
function attDirCheckClick($li){
		var $attDirIcon = $('.attDirGroup li .attDirIcon a');
		var allLength = $(".attDirCheckBox").length;//获得选中的数量
		var $cIcon = $li.find(".attDirCheckIcon");
		var $cb = $li.find('.attDirCheckBox');
		var $cbInfo = $('.rHead2 .checkboxDetail');
		if($cb.is(":checked")) {
			$cb.attr("checked",false);
			var length = $(".attDirCheckBox:checked").length;//获得选中的数量
			$cbInfo.find(".length").text('已选择了'+length+'个文件,');
			$cbInfo.find('.cancle').text('取消');
			if(length == 0){
				$(".attDirGroup li").css({"border":"1px solid #fff","background":""});
				$(".attDirGroup li .attDirCheckIcon").css("background","");
				//恢复所有的超链接属性
				$attDirIcon.each(function(i){
					var url = $(this).attr("url");
					$(this).attr("href",url);
				})
				$cbInfo.hide();
			}
			$cIcon.css("background","url(images/clouddisk_tiled/check_default.png)");
			$li.css({"border":"1px solid #c0e0f4","background":""});
			$("#dirAttAllCbox").attr("checked",false);
			$("#dirAttAllCboxIcon").css("background","url('images/clouddisk/checkbox_1.png')");
		}else{
			$cb.attr("checked",true);
			$attDirIcon.removeAttr("href");
			var length = $(".attDirCheckBox:checked").length;//获得选中的数量
			
			if(length == 1){
				$cbInfo.show();
				$(".attDirGroup li").each(function(i){
					if($(this).find(".attDirCheckBox").is(":checkbox")){
						$(this).css({"border":"1px solid #c0e0f4","background":""});
						$(this).find(".attDirCheckIcon").css("background","url(images/clouddisk_tiled/check_default.png)");
					}
				});
			}
			$li.css({"border":"1px solid #c0e0f4","background":"#ecf6fe"});
			$cIcon.css("background","url(images/clouddisk_tiled/check_click.png)");
			$cbInfo.find(".length").text('已选择了'+length+'个文件,');
			$cbInfo.find('.cancle').text('取消');
			if(allLength == length){
				$("#dirAttAllCbox").attr("checked",true);
				$("#dirAttAllCboxIcon").css("background","url('images/clouddisk/checkbox_3.png')");
			}
		}
}
/**
 * li选中事件
 * @param e
 * @return
 */
 function attDirCheckLiClick(e){
	e.stopPropagation();
	var length = $(".attDirCheckBox:checked").length;//获得选中的数量
	var $li = $(this);
	if($li.find(".attDirCheckBox").is("checked")){
		if(length > 0){
			attDirCheckClick($li);
		}
	}
}
 /**
  * 图标选中事件
  * @param e
  * @return
  */
 function attDirIconCheck(e){
		e.stopPropagation();
		var $li = $(this).parent();
		attDirCheckClick($li);
 }



/**
 * 附件重命名
 * @return
 */
function attChangeName(){
	//id="'+attId+'" attOldId="'+oldAttId+'" docId="'+docId+'"  value="'+attName+'" oldName="'+attName+'" 
	var $att = $(this);
	var attName = $att.val();
	var attId = $att.attr("id");
	var attOldName = $att.attr("oldName");
	var attOldId= $att.attr("attOldId");
	var docId = $att.attr("docId");
	var attExt = $att.attr("attExt");
	if($.trim(attName) == ''){
		changeAttNameAfter(attId, attOldId, attOldName, docId,attExt);
		$.toaster({ priority : 'info', message : '名称不能为空！' });
		return;
		
	}else{
		if(attOldName == attName){
	 		changeAttNameAfter(attId, attOldId, attName, docId,attExt);
	 		return;
	 	}
		 var endLen = attName.lastIndexOf(".");//文本高亮显示
		 if(endLen == 0){
			 $.toaster({ priority : 'info', message : '名称不能为空！' });
			 changeAttNameAfter(attId, attOldId, attOldName, docId,attExt);
			 return; 
		 }
		
	 	$.ajax({
	 		type:"post",
	 		url:"clouddisk_list_do.jsp",
			contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
	 		data:{"op":"changeFileName","att_id":attId,"att_name":attName,"att_oldId":attOldId, "att_docId":docId},
	 		success:function(data,status){
	 			data = $.parseJSON(data);
	 			if(data.ret == "1"){
	 				changeAttNameAfter(data.attId,data.attOldId,data.name,data.attDocId,data.ext,data.url,data.config); 
	 				$.toaster({ priority : 'info', message : '重命名成功！' });
	 			}else{
	 				changeAttNameAfter(attId, attOldId, attOldName, docId,attExt);
	 				$.toaster({ priority : 'info', message : '重命名失败！' });
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
	 	});
		
	}
 	
	
}

/**
 * 附件重命名
 */
var UrlOld;
var theConfig;
function changeAttNameAfter(attId, attOldId, attName,docId,ext,url,config){
	var $li = $("#dirAtt"+attOldId);
	$li.attr({"title":attName,"attId":attId,"docId":docId,"attExt":ext,"attName":attName});
	var $attDirName = $li.find(".attDirName");
	var $attDirCheckBox = $li.find(".attDirCheckBox");
	$attDirCheckBox.val(attId);
	var $attExtType = $(".attExtType"+attOldId);
	var $attDirIcon = $li.find(".attDirIcon");
	var $attDirIconA = $attDirIcon.find("a");
	if(url != null){
		UrlOld = url;
	}
	if(config != null){
		theConfig = config;
	}
	if(attId == attOldId){
		$attDirName.html("<a href=javascript:editdoc("+docId+ "," +attId+",'"+ext+"')>"+attName+"</a>");
	}
	else if(theConfig==6){
		$attDirIconA.attr({"url":"javascript:editdoc('" +docId+ "','" +attId+ "','"+ext+ "')","href":"javascript:editdoc('" +docId+ "','" +attId+ "','"+ext+ "')"});
		$attDirName.html("<a href=javascript:editdoc("+docId+ "," +attId+",'"+ext+"')>"+attName+"</a>");
		$attExtType.attr({"value":"6"});
	}else if(theConfig==5){
		$attDirIconA.attr({"url":"javascript:edittxt('" +docId+ "','" +attId+ "','"+ext+ "')","href":"javascript:edittxt('" +docId+ "','" +attId+ "','"+ext+ "')"});
		$attDirName.html("<a href=javascript:edittxt("+docId+ "," +attId+",'"+ext+"')>"+attName+"</a>");
		$attExtType.attr({"value":"5"});
	}else if(theConfig==1){
		$attDirIconA.attr({"url":"javascript:showImg('" +attId+ "')","href":"javascript:showImg('" +attId+ "')"});
		$attDirName.html("<a href=javascript:showImg('" +attId+"')>"+attName+"</a>");
		$attExtType.attr({"value":"1"});
	}else{
		$attDirIconA.attr({"url":"javascript:;","href":"clouddisk_downloadfile.jsp?attachId="+attId});
		$attDirName.html("<a href=clouddisk_downloadfile.jsp?attachId="+attId+">"+attName+"</a>");
		$attExtType.attr({"value":"0"});
	}
	var $attDirIconImg = $attDirIcon.find("img");
	if(attId != attOldId){
		 $attDirIconImg.attr("src","images/clouddisk_tiled/"+UrlOld);
	}
	changeLiStyleNoBorder(attId);
}


/**
 * 删除附件
 */
function delAttach(attach_id, doc_id) {
		jConfirm("确定要删除附件么?<br/>删除后可以在回收站找回!","提示",function(r){
			if(!r){
				return;
			}
			else{
				$.ajax({
				 		type:"get",
				 		url:"clouddisk_list_do.jsp",
				 		data:{"op":"delAttach","att_id":attach_id, "att_docId":doc_id},
				 		success:function(data,status){
				 			data = $.parseJSON(data);
				 			if(data.ret == "1"){
				 				$("li[attId='"+data.att_id+"'").remove();
				 				 deleteAfterStyleByTiled();
				 				 $.toaster({ priority : 'info', message : '删除成功！' });
				 				//parent.location.reload();
				 			}else{
				 				jAlert(data.msg,"提示");
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
 * 发起协作 选择人员
 * @param dirCode
 * @return
 */
function setUsers(users, userRealNames) {
	$.ajax({
 		type:"get",
 		url:"clouddisk_list_do.jsp",
 		data:{"op":"actionCooperate","userName":userName,"names":users,"dirCode":curDirCode},
 		success:function(data,status){
 			data = $.parseJSON(data);
 			if(data.ret == "1"){
 				$.toaster({ priority : 'info', message : data.msg });
 				$("#dirAtt"+curDirCode).find(".attDirIcon").find("img").attr("src","images/clouddisk_tiled/cooperate.png");
 			}
 		},
 		error:function(XMLHttpRequest, textStatus){
 			//alert(XMLHttpRequest.responseText);
 		}
	});
}


	
