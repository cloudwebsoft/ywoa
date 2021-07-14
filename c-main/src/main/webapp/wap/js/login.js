$(function(){
	$(".login-btn").on('click',function(){
		var isValid = true;
		var allVal='';
		$("input[name='name']").blur(function(allVal){
			var usernameVal = $(this).val();
			if(usernameVal==''){
				allVal='请输入用户名';
				layerOpen(allVal);
				isValid = false;
				return false;
			}
		});		
		
		if (isValid) {
			$("input[name='pwd']").blur(function(allVal){
				var paswVal = $(this).val();
				if(paswVal.length==''){
					allVal='请输入密码';
					layerOpen(allVal);
					isValid = false;
					return false;
				}
			});
		}
			
		if (!isValid)
			return;
		
		layer.open({
			type: 2
			,content: '登录中...'
			,time: 2
		 });
		 
		 login();
	});
	
	function layerOpen(allVal){
		layer.open({
			content: allVal,
			btn: '确定'
		});
	}	
});