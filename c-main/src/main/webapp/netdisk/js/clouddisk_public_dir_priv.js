/**
 * 权限选择框
 */
$(function(){
	/**
	 * checkbox选择框 图标点击事件
	 */
	var privCboxIconClick = function(){
		var $privCboxIcon = $(this);
		var $privOp = $privCboxIcon.parent();
		var $privCbox = $privOp.find(".cboxPriv");
		if($privCbox.is(":checked")){
			$privCboxIcon.css("background","url('images/clouddisk/checkbox_1.png')");
			$privCbox.attr("checked",false);
			$privCbox.val(0);
		}else{
			$privCboxIcon.css("background","url('images/clouddisk/checkbox_3.png')");
			$privCbox.attr("checked",true);
			$privCbox.val(1);
		}
	}
	//checkbox点击事件
	$(".privOp .cboxIcon").click(privCboxIconClick);
	//修改权限
	$(".modifyOp").click(modifyPriv);
	//删除权限
	$(".delOp").click( delPriv);
	//添加权限
	$(".addPriv").click(function(){
		var dirCode = $(".urlDirCode").val();
		location.href ="../admin/netdisk_public_dir_priv_add.jsp?dirCode="+dirCode;
	})


})
/**
 * 修改权限
 * @return
 */
function modifyPriv(){
	var $dd = $(this).parent().parent();
	var id = $dd.attr("id"); //权限id
	var $privContent = $dd.find(".privDetailContent");
	var $see = $privContent.find(".see").find(":checkbox");
	var $append = $privContent.find(".append").find(":checkbox");
	//var $del = $privContent.find(".del").find(":checkbox");
	//var $modify = $privContent.find(".modify").find(":checkbox");
	var $examine = $privContent.find(".examine").find(":checkbox");	
	var param = $("#privForm"+id).serialize();
	var url = "clouddisk_public_dir_do.jsp?"+param;
	$.ajax({
 		type:"post",
 		url:url,
 		dataType:"json",
 		data:{ "id":id,"op":"modifyPriv" },
 		success:function(data,status){
 			var res = data.result;
 			if(res){
 				if($examine.is(":checked")){
 					$privContent.find(".privOp").find(":checkbox").val(1);
 					$privContent.find(".privOp").find(":checkbox").attr("checked",true);
 					$privContent.find(".privOp").find(".cboxIcon").css("background","url('images/clouddisk/checkbox_3.png')");
 				}
 				/*else if( $del.is(":checked") || $modify.is(":checked")){
 					if($see.is(":checked") && $append.is(":checked") && $del.is(":checked") && $modify.is(":checked")){
 	 					$examine.val(1);
 	 					$examine.attr("checked",true);
 	 					$privContent.find(".examine").find(".cboxIcon").css("background","url('images/clouddisk/checkbox_3.png')");
 	 				}else{
 	 					$see.val(1);
 	 					$see.attr("checked",true);
 	 					$privContent.find(".see").find(".cboxIcon").css("background","url('images/clouddisk/checkbox_3.png')");
 	 				}
 					
 				}*/
 				$.toaster({ priority : 'info', message : '权限修改成功！' });
 			}else{
 				$.toaster({ priority : 'info', message : '权限修改失败！' });
 			}
 		},
 		error:function(XMLHttpRequest, textStatus){
 			alert(XMLHttpRequest.responseText);
 		}
	});
}

/**
 * 删除权限
 * @return
 */
function delPriv(){
	var $dd = $(this).parent().parent();
	var id = $dd.attr("id"); //权限id
	jConfirm("确定要删除文件夹权限么?","提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
		 		type:"post",
		 		url:"clouddisk_public_dir_do.jsp",
		 		dataType:"json",
		 		data:{ "id":id,"op":"delPriv" },
		 		success:function(data,status){
		 			var res = data.result;
		 			if(res){
		 				$dd.remove();
		 				$.toaster({ priority : 'info', message : '权限删除成功！' });
		 			}else{
		 				$.toaster({ priority : 'info', message : '权限删除失败！' });
		 			}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
			});
		}
	})
}