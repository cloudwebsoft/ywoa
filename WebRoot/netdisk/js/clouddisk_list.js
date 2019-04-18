/**
 * 云盘一些共通的JS方法
 */
$(function(){
	var isSearch = $("#isSearch").val();
	//搜索和列表界面合并为一个界面  时间 与所在目录的隐藏显示
	if( isSearch == 'select_one' ){
		$("dl dd .dateT").hide();
		$("dl dd .catalogT").show();
		$('.newFolder').hide();//新建文件夹显示
		$('.uploadFile_c').hide();
	}else{
		$("dl dd .dateT").show();
		$("dl dd .catalogT").hide();
		$('.newFolder').show();//新建文件夹显示
		$('.uploadFile_c').show();
		
	}
	//$(".attNameInfo").contextMenu(contextAttachMentMenu);
	/**
	 * 1.列表界面中的一些共通样式（行上下浮动 点击事件）
	 */
	
	/**
	 * 1.1 checkbox 全选
	 */
	var checkAllCbox = function(){
		var $allCbox = $(this).parent().find('#filename_input');
		var $attTitle = $(this).parent().find(".fnameTitle");
		if($allCbox.is(":checked")){
			
			$(".cbox_icon").css("background","url('images/clouddisk/checkbox_1.png')");
			$(this).css("background","url('images/clouddisk/checkbox_1.png')");
			$(".cbox").removeAttr("checked");
			$allCbox.removeAttr("checked");
			$(".fileGroup").css({"background":""});
			$attTitle.text("文件名");
		
		}else{
				$(".cbox_icon").css("background","url('images/clouddisk/checkbox_3.png')");//所有cbox图标变为选中状态
				$(".cbox").attr("checked","checked"); //checkbox选中
				$(".fileGroup").css({"background":"#f0f8fd"});//所有dl颜色改变
				$allCbox.attr("checked","checked");//全选的checkbox选中
				$(this).css("background","url('images/clouddisk/checkbox_3.png')");
				var length = $(".cbox:checked").length;//获得选中的数量
				$attTitle.text("已选择"+length+"项");
		}
	}
	$('.cbox_all').click(checkAllCbox); //全选文件
	
	/**
	 * 1.2 行单击事件
	 */
	var floderAndAttCheck = function(){
		var $allCbox = $("#filename_input");//全选按钮
		var $allCboxIcon = $(".cbox_all");//全选图标
		var $dd = $(this);//当前dd
		var $cboxIcon = $dd.find(".cbox_icon");//选中图标
		var $cbox = $dd.find(".cbox");//checkbox
		var $title = $(".fnameTitle");//标题
		if($allCbox.is(":checked")) {//如果全选按钮选中
			$allCbox.attr("checked",false);
			$allCboxIcon.css("background","url('images/clouddisk/checkbox_1.png')");
			$(".fileGroup").css({"background":""});//所有背景色变色
			$(".cbox").attr("checked",false);//所有checkbox未选中
			$(".cbox_icon").css("background","url('images/clouddisk/checkbox_1.png')");
			$cbox.attr("checked",true);//当前行的 checkbox选中
			$dd.css({"background":"#f0f8fd"});//当前行颜色改变
			$cboxIcon.css("background","url('images/clouddisk/checkbox_3.png')");//图标改变
			$title.text("文件名");
		}else{
			if($cbox.is(":checked")){
				$(".fileGroup").css({"background":""});//所有背景色变色
				$(".cbox").attr("checked",false);//所有checkbox未选中
				$(".cbox_icon").css("background","url('images/clouddisk/checkbox_1.png')");
			}else{
				$(".fileGroup").css({"background":""});//所有背景色变色
				$(".cbox").attr("checked",false);//所有checkbox未选中
				$(".cbox_icon").css("background","url('images/clouddisk/checkbox_1.png')");
				$cbox.attr("checked",true);//当前行的 checkbox选中
				$dd.css({"background":"#f0f8fd"});//当前行颜色改变
				$cboxIcon.css("background","url('images/clouddisk/checkbox_3.png')");//图标改变
				$title.text("文件名");
				$allCbox.css("background","url('images/clouddisk/checkbox_1.png')");
			}
		}
	}
	
	$(".fileGroup").live("click",floderAndAttCheck);
	
	/**
	 * 1.3行中其他元素 不响应行点击事件
	 */
	var stopClick = function(e){
		e.stopPropagation();
	}
	$("dl dd .fileNameDetail").find(".fname").live("click",stopClick);
	$("dl dd .fileNameDetail").find(".cbox").live("click",stopClick);
	$("dl dd .fileNameDetail").find(".file_action").live("click",stopClick);
	$("dl dd .fileNameDetail").find(".op").live("click",stopClick);
	$("dl dd .cooperateCol").live("click",stopClick);

	
	/**
	 * 1.4 行元素中 checkbox图标点击事件
	 */
	//checkboxIconClick事件
	var checkboxIconClick = function(e){
		e.stopPropagation();
		
		var $dd = $(this).parent().parent();
		var $allCbox = $("#filename_input");//全选按钮
		var $allCboxIcon = $(".cbox_all");//全选图标
		var $cbox = $dd.find(".cbox");
		var $cboxIcon = $(this);//选中图标
		var $title = $(".fnameTitle");//标题
		if($allCbox.is(":checked")) {//如果全选按钮选中
			$dd.css({"background":""});//所有背景色变色
			$allCbox.attr("checked",false);//全选取消
			$allCboxIcon.css("background","url('images/clouddisk/checkbox_1.png')");//颜色改变
			$cbox.removeAttr("checked");
			$cboxIcon.css("background","url('images/clouddisk/checkbox_1.png')");
			var length = $(".cbox:checked").length;//获得选中的数量
			if( length > 1) {
				$title.text("已选择"+length+"项");
			}else {
				$title.text("文件夹");
			}
		}else{
			if($cbox.is(":checked")){
				$cbox.attr("checked",false);
				$cboxIcon.css("background","url('images/clouddisk/checkbox_1.png')");
				$dd.css({"background":""});//所有背景色变色
				var length = $(".cbox:checked").length;//获得选中的数量
				if( length > 1) {
					$title.text("已选择"+length+"项");
				}else {
					$title.text("文件夹");
				}
				
			}else{
				$cbox.attr("checked",true);
				$cboxIcon.css("background","url('images/clouddisk/checkbox_3.png')");
				$dd.css({"background":"#f0f8fd"});//所有背景色变色
				var length = $(".cbox:checked").length;//获得选中的数量
				if( length > 1) {
					$title.text("已选择"+length+"项");
				}else {
					$title.text("文件夹");
				}
			}
		}
	
	}
	
	$("dl dd .fileNameDetail").find(".cbox_icon").live("click",checkboxIconClick);
	
	/**
	 * 1.5 文件附件 更多按钮点击事件
	 */
	$(".pulldown").click(function(e){
		 var $moreOp = $(this);
		 var $root = $moreOp.parents("dd");
	     var $operate = $root.find(".fileNameDetail").find(".op");
		 if($operate.is(":hidden")){
			$("dl dd .fileNameDetail .op").hide();
			$(".pulldown").attr("src","images/clouddisk/pulldown_1.gif")//所有下拉按钮换成默认
			$moreOp.attr("src","images/clouddisk/pulldown_2.gif");
			$operate.show();
		}else{
			$operate.hide();
			$moreOp.attr("src","images/clouddisk/pulldown_1.gif");
		}
	 });

	/**
	 * 1.6 dd 行 浮动 离开效果
	 */
	var floderAndAttMouseEnter = function(){
		var $dd = $(this);
		var $cbox = $dd.find(".cbox");
		if($cbox.is(":checked")) {
			$(this).css({"background":"#f0f8fd"});
		}else{
			$(this).css({"background":"#f7fcff"});
		}
		$(".pulldown").attr("src","images/clouddisk/pulldown_1.gif")//所有下拉按钮换成默认				  
	    $(this).find(".fileNameDetail").find(".file_action").show();
	
	}
	var floderAndAttMouseLeave = function(){
		var $dd = $(this);
		var $cbox = $dd.find(".cbox");
		if($cbox.is(":checked")) {
			$(this).css({"background":"#f0f8fd"});
		}else{
			$(this).css({"background":""});
		}
		$(this).find(".fileNameDetail").find(".file_action").hide();
		$(this).find(".fileNameDetail").find(".op").hide();
	}
	
    $("dl .fileGroup").live({"mouseenter":floderAndAttMouseEnter,"mouseleave":floderAndAttMouseLeave});
    
    
    /**
     * 2，共通js效果
     */
    
   /**
    * 2.1分类选择显示时，每张图片点击时 背景色改变
    */
    var categoryAttImageClick = function(){
    	 var src='images/clouddisk/'+$(this).attr("class")+'_2.gif';
		 var srcClass=$(this).attr("class");
		 $(this).find("img").attr("src",src);
		 $(".sortShow ul li").each(function(index, element) {
			 var a=$(this).attr("class");
			 var srcInfo='images/clouddisk/'+a+'_1.gif';
	         if(a!=srcClass){
	        	 $(this).find("img").attr("src",srcInfo);
	         }
		 });
    }
	$(".sortShow ul li").click(categoryAttImageClick);
	
	
	/**
	 * 2.4 所有文件附件 更多操作 hover  mouseleave事件 
	 */
	$(".op").find("li").find("a").hover(
			function(){
				$(this).parent().css({"background":"#666","color":"#fff","height":"24px"});
			}
	)
	$(".op").find("li").find("a").mouseleave(
		function(){
			$(this).parent().css({"background":"#fff","color":"#888","height":"24px"});
		}
	)

})
/**
 * 附件删除
 * @param attach_id
 * @param doc_id
 * @param oldAmId
 * @return
 */
function delAttach(attach_id, doc_id,oldAmId) {
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
			 				delAttachInfo(oldAmId);
			 				 deleteAfterStyleByList();
			 				 $.toaster({ priority : 'info', message : '删除成功！' });
			 				
			 			}else{
			 				 $.toaster({ priority : 'info', message : data.msg });
			 			}
			 			//parent.location.reload();
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
			});
		}
	})
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
 				$("#folder"+curDirCode).find(".extImg").attr("src","images/folder_netdisk_share.gif");
 			}else{
 				$.toaster({ priority : 'info', message : data.msg });
 			}
 		},
 		error:function(XMLHttpRequest, textStatus){
 			//alert(XMLHttpRequest.responseText);
 		}
	});
}
	

