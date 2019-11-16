(function($,window,document,undefined){
	var w = window;
	var d = document;
	var self ;
	var CHOOSE_USER_AJAX_URL = "../do/flow_do.jsp";//选择用户ajax
	$.User = $.Class.extend({
		init: function(element, options) {
					 this.element = element,
					 this.default = {
						"formSelector":".mui-input-group",
						"ulSelector":".mui-table-view"
					 }
					 this.options = $.extend(true,this.default,options);
			 },
		chooseUserInit:function(){  //选择用户初始化
			var self = this;
			var list = self.element;
			var chooseUsers = self.options.chooseUsers;
			var isMulti = self.options.isMulti;
			var ajax_param = {"op":"user_init_list"};
			jQuery.myloading();
			$.get(CHOOSE_USER_AJAX_URL,ajax_param,function(data){
				jQuery.myloading("hide");
				var res = data.res;
				if(res == 0){
					var arr = data.datas;
					$.each(arr,function(index,item){
						var isGroup = item.isGroup;
						var pyName = item.pyName;
						var name = item.name;
						var li = '';
						if(isGroup){
							li += '<li data-group="'+pyName+'" class="mui-table-view-divider mui-indexed-list-group">'+name+'</li>';
						}else{
							var name = item.user.name;
							var photo = item.user.photo;
							var mobile = item.user.mobile;
							var dName = item.user.dName == ''?'':"("+item.user.dName+")";
							var gender = item.user.gender;
							var realName = item.user.realName;
							var imgSrc = gender == '1'?"../images/user_46_03.png":"../images/user_46_01.png";
							var isChecked = "";
							if(("," + chooseUsers + ",").indexOf("," + name + ",") != -1){
								isChecked = "checked";
							}
							// 注意li的class中不能有mui-table-view-cell，否则当人数多时会超卡甚至导致webview崩溃，故这里手工写了style
							if (isMulti=="true") {
								li += '<li uName='+name+' realName='+realName+' data-tags='+pyName+' gender=' + gender + ' style="clear:both; height:50px; padding-top:10px; border-bottom: 1px solid #ccc" class="mui-indexed-list-item mui-checkbox mui-left">';
								li += '<input type="checkbox" name="user" class="ck" '+isChecked+'/>';
							}
							else {
								li += '<li uName='+name+' realName='+realName+' data-tags='+pyName+' gender=' + gender + ' style="clear:both; height:50px; padding-top:10px; border-bottom: 1px solid #ccc"  class="mui-table-view-cell mui-indexed-list-item mui-radio mui-left">';
								li += '<input type="radio" name="user" class="ck" '+isChecked+'/>';
							}
							li += '<img class="mui-media-object mui-pull-left" style="margin-left: 60px" src="'+imgSrc+'">';
							li += '<div class="mui-media-body">';
							li+= realName+dName;
							li+= "<p class='mui-ellipsis'>";
							li+= mobile;
							li+="</p>"
							li+= '</div>'
							
							li += '</li>';
						}
						jQuery(".mui-table-view").append(li);
					});
				
					window.indexedList = new mui.IndexedList(list);
					self.bindChooseUserEvent();
					self.changeDoneStatus();
				}
			},"json");
		},
		changeDoneStatus:function(){
			var done = jQuery("#done").get(0);
			var self = this;
			var list = self.element;
			var isMulti = self.options.isMulti;			
			var count;
			// 选择不带有value属性的checkbox，因为搜索后生成结果中的checkbox带有value
			if (isMulti=="true") {
				count = list.querySelectorAll('input[type="checkbox"]:not([value]):checked').length;
			}
			else {
				count = list.querySelectorAll('input[type="radio"]:not([value]):checked').length;
			}
			var value = count ? "完成(" + count + ")" : "完成";
			done.innerHTML = value;
			if (count) {
				if (done.classList.contains("mui-disabled")) {
					done.classList.remove("mui-disabled");
				}
			} else {
				if (!done.classList.contains("mui-disabled")) {
					done.classList.add("mui-disabled");
				}
			}
			
		},
		bindChooseUserEvent:function(){ //选择用户事件
			var done = jQuery("#done").get(0);
			var self = this;
			var list = self.element;
			done.addEventListener('tap', function() {
				var isMulti = self.options.isMulti;			
				var checkboxArray;
				if (isMulti=="true") {
					checkboxArray = [].slice.call(list.querySelectorAll('input[type="checkbox"]:not([value]):checked'));
				}
				else {
					checkboxArray = [].slice.call(list.querySelectorAll('input[type="radio"]:not([value]):checked'));
				}
				var checkedValues = [];
				var checkedRealNames = [];
				var nextUsers = "";
				
				var chooseUsersAry = self.options.chooseUsers.split(",");
				checkboxArray.forEach(function(box) {
					// if (box.checked) {
						// 过滤掉重复选择的用户
						for (var m=0; m<chooseUsersAry.length; m++) {
							if (chooseUsersAry[m]==box.parentNode.getAttribute("uName")) {
								return;
							}
						}
						checkedValues.push(box.parentNode.getAttribute("uName"));
						checkedRealNames.push(box.parentNode.getAttribute("realName"));
					// }
				});
				var btnArray = ['确认', '取消'];
				mui.confirm('确认选择该条记录？', '提示', btnArray, function(e) {
					if(e.index == 0) {
						$.toast("选择成功!");
						var isAt = self.options.isAt;
						var isFree = self.options.isFree;
						var code = self.options.code;
						var internalName = self.options.internalName;
						if(code !=''){
                            doneSelectUserWin(code,checkedValues,checkedRealNames)
						}else{
                            doneChooseUser(checkedValues,checkedRealNames,isAt, isFree, internalName);
                        }
					}
				});
			}, false);
			mui('.mui-indexed-list-inner').on('change', 'input', function() {
				self.changeDoneStatus();
			});
		}
	})
})(mui,document,window)
