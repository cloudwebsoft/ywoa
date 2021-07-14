(function($,window,document,undefined){

	$.ajaxSettings.beforeSend = function(xhr, setting) {
		jQuery.myloading();
		// beforeSend演示,也可在$.ajax({beforeSend:function(){}})中设置单个Ajax的beforeSend
		// console.log('beforeSend:::' + JSON.stringify(setting));
	};
	// 设置全局complete
	$.ajaxSettings.complete = function(xhr, status) {
		// console.log('complete:::' + status);
		jQuery.myloading("hide");
	}
	var PAGE_NUM = 1;
	var TOTAL = 0;
	var PAGE_SIZE = 10;
	var OP = "";
	var myScrollClass; // 调用pullToRefresh后得到的对象
	var self ;// 罗珠敏备注 坑爹 调用了下拉刷新控件后 this对象 已经改变为 下拉刷新的this所以 要 设置为全局变量
	$.PullToRefrshList = $.Class.extend({
		init: function (element, options) {
			this.element = element,
				this.default = {
					"pullRefreshContainer": "#pullrefresh",
					"ulContainer": ".mui-table-view",
					"searchContainer": "#search_content",
					"liContainer": ".mui-table-view-cell"
				}
			this.options = $.extend(true, this.default, options);
			this.bindEvent();
		},
		loadListData: function () {
			self = this;
			var pullRefreshSelector = self.options.pullRefreshContainer;
			var options = {
				container:pullRefreshSelector,
				down: {
					callback: this.pulldownRefresh
				},
				up: {
					height:20,// 可选,默认50.触发下拉刷新拖动距离,
					// contentrefresh: '正在加载...',
					// contentnomore:'没有更多数据了',//可选，请求完毕若没有更多数据时显示的提醒内容；
					auto:true,
					callback: this.pullupRefresh
				}
			}

			mui(pullRefreshSelector).pullToRefresh(options);
		},
		loadList: function () {
			TOTAL = 0; // 初始化，否则多个tab点击时，会因上一个tab的ajax_get加载的TOTAL带来影响
			self = this;
			var pullRefreshSelector = self.options.pullRefreshContainer;
			var options = {
				down: {
					callback: this.pulldownRefresh
				},
				up: {
					auto: true,
					callback: this.pullupRefresh
				}
			}
			mui(pullRefreshSelector).pullToRefresh(options);
		},
		pulldownRefresh: function () {
			myScrollClass = this;
			var params = self.options.ajax_params;
			var searchContainer = self.options.searchContainer;
	    	jQuery(searchContainer).val("");
			PAGE_NUM = 1;
			OP = "";
			var ajax_param = $.extend(true,{"pageNum":PAGE_NUM,"pageSize":PAGE_SIZE,"op":OP},params);
			self.ajax_get(self.options.url, 0, ajax_param, myScrollClass);
		},
		pullupRefresh:function(){
			myScrollClass = this;
			if (OP == "search") {
				var _searchContainer = self.options.searchContainer;
				var url_params = jQuery(_searchContainer).serialize();
				var n_url = self.options.url + "?" + url_params;
				PAGE_NUM += 1;
				var params = self.options.ajax_params;
				var ajax_param = $.extend(true, {"pageNum": PAGE_NUM, "pageSize": PAGE_SIZE, "op": OP}, params);
				self.ajax_get(n_url, 1, ajax_param, myScrollClass);
			} else {
				var params = self.options.ajax_params;
				if (TOTAL == 0) {
					PAGE_NUM = 1;
					var ajax_param = $.extend(true, {"pageNum": PAGE_NUM, "pageSize": PAGE_SIZE, "op": OP}, params);
					self.ajax_get(self.options.url, 1, ajax_param, myScrollClass);
				} else if (PAGE_NUM * PAGE_SIZE < TOTAL) {
					PAGE_NUM += 1;
					var ajax_param = $.extend(true, {"pageNum": PAGE_NUM, "pageSize": PAGE_SIZE, "op": OP}, params);
					self.ajax_get(self.options.url, 1, ajax_param, myScrollClass);
				}
			}
		},
		bindEvent: function() {
			var self = this;
			$(".mui-input-group").on("tap", ".f_search_btn", function () {
				// 初始化pullToRefresh
				// myScrollClass.refresh(true);
				OP = "search";
				var _searchContainer = self.options.searchContainer;
				var url_params = jQuery(_searchContainer).serialize();
				var n_url = self.options.url + "?" + url_params;
				PAGE_NUM = 1;
				var params = self.options.ajax_params;
				var data_types = self.options.ajaxDatasType;
				var ajax_param;
				ajax_param = $.extend(true, {"pageNum": PAGE_NUM, "pageSize": PAGE_SIZE, "op": OP}, params);
				self.ajax_get(n_url, -1, ajax_param, myScrollClass);
			});
			$(".mui-input-group").on("tap",".date_btn",function(){
				var optionsJson = this.getAttribute('data-options') || '{}';
				var options = JSON.parse(optionsJson);
				var id = this.getAttribute('id');
				var par = this.parentNode;
				var time_input = par.querySelector(".input-icon");
				var picker = new $.DtPicker(options);
				picker.show(function(rs) {
					if (options.type == "date") {
						jQuery(time_input).val(rs.value);
					}
					else {
						jQuery(time_input).val(rs.value + ":00");
					}
					picker.dispose();
				});
			})
			self.bindTapItemEvent();
			
		},
		bindTapItemEvent:function(){
			var self = this;
			var params = self.options.ajax_params;
			$(".mui-content").on('tap',"#op_add",function(){
				var parentId = 0; 
				var _urlParams = "skey="+params.skey+"&moduleCode="+params.moduleCode;
				if("parentId" in params){
					parentId = params.parentId;
					_urlParams += "&parentId="+parentId;
				}
				var formCodeRelated = "";
				if("formCodeRelated" in params){
					formCodeRelated = params.formCodeRelated; 
					_urlParams += "&formCodeRelated="+formCodeRelated;
				}
				mui.openWindow({
				    "url":"../visual/module_add_edit.jsp?"+_urlParams
				})							
				
			});
			$(self.options.ulContainer).on('tap', '.mui-table-view-cell', function(event) {
				var elem = this;
				var id = elem.getAttribute("rId");
				var moduleCode = params.moduleCode;
				if("formCodeRelated" in params){
					moduleCode = params.formCodeRelated;	
				}
				mui.openWindow({
				    "url":"../visual/module_detail.jsp?skey="+params.skey+"&moduleCode="+moduleCode+"&id="+id,
					"id": "module_detail_" + moduleCode + "_" + id
				})	
			});
			
			$(self.options.ulContainer).on('tap', '.mui-btn', function(event) {
				var elem = this;
				var op = this.getAttribute("op");
				var li = elem.parentNode.parentNode;
				var id = li.getAttribute("rId");
				if(op == "edit"){
					var urlParams = self.options.urlParams;// 超链接
					setTimeout(function() {
						$.swipeoutClose(li);
					}, 0);
					var parentId = 0; 
					var _urlParams = "skey="+params.skey+"&moduleCode="+params.moduleCode+"&id="+id;
					if("parentId" in params){
						parentId = params.parentId;
						_urlParams += "&parentId="+parentId;
					}
					var formCodeRelated = "";
					if("formCodeRelated" in params){
						formCodeRelated = params.formCodeRelated; 
						_urlParams += "&formCodeRelated="+formCodeRelated;
					}
					mui.openWindow({
					    "url":"../visual/module_add_edit.jsp?"+_urlParams
					})							
				}else{
					var btnArray = ['确认', '取消'];
					var moduleCode = params.moduleCode;
					if("formCodeRelated" in params){
						moduleCode = params.formCodeRelated; 
					}
					var datas = {"id":id,"moduleCode":moduleCode};		
					mui.confirm('确认删除该条记录？', '提示', btnArray, function(e) {
						if(e.index == 0){
							mui.get(AJAX_REQUEST_URL.MODULE_DEL,datas,function(data){	
									var res = data.res;
									var msg = data.msg;
									if(res == "0"){
										$.toast("删除成功!");
										if (e.index == 0) {
											li.parentNode.removeChild(li);							
										} else {
											setTimeout(function() {
												$.swipeoutClose(li);
											}, 0);
										}
										
									}else{
										$.toast(msg);
										setTimeout(function() {
											$.swipeoutClose(li);
										}, 0);
									}
								
									
								},"json");
								
						}else{
							setTimeout(function() {
								$.swipeoutClose(li);
							}, 0);	
						}
				
					
					});
				}
			});
		},
		conditionType:function(data_arr){
			var self = this;
			var _searchContainer =  self.options.searchContainer;
			mui.each(data_arr,function(index,data){
				// console.info(data);
				var type = data.fieldType;
				var divContent = "";
				var code = data.fieldName;
				var fieldTitle = data.fieldTitle;
				var fieldCond = data.fieldCond;
				var typeOfField = data.type; 
				if(type == SEARCH_FIELD_TYPE.FIELD_TYPE_DATE || type == SEARCH_FIELD_TYPE.FIELD_TYPE_DATETIME){
				
				    divContent += '	<div class="mui-input-row">';
					divContent += '<label>'+fieldTitle+' ：大于</label>';
					var options = type == SEARCH_FIELD_TYPE.FIELD_TYPE_DATE?'{"type":"date"}':'{}';
					var icon_class = type == SEARCH_FIELD_TYPE.FIELD_TYPE_DATE?"iconfont icon-naozhong":"iconfont icon-rili";
					var fromDate = data.fromDate;
					divContent += '	<input  placeholder="请输入'+fieldTitle+'" type="text" name="'+code+'FromDate" id="'+code+'FromDate" class="input-icon" value="'+fromDate+'" />';
					divContent += '<a class="date_btn" data-options='+options+'><span class="'+icon_class+'"></span> </a>'	
					divContent+= '<input type="hidden" name="'+code+'_cond" value = "'+fieldCond +'" />'
					divContent +="</div>";
					divContent += '	<div class="mui-input-row">';
					
					divContent += '<label>小于</label>';
					var toDate = data.toDate;
					
					divContent += '	<input type="text" placeholder="请输入'+fieldTitle+'" name="'+code+'toDate" id="'+code+'toDate" class="input-icon" value="'+toDate+'" />';
					
					divContent += '<a class="date_btn" data-options='+options+'><span class="'+icon_class+'"></span> </a>'	
					
					divContent +="</div>";
				}else if(type == SEARCH_FIELD_TYPE.FIELD_TYPE_PRICE){
					divContent += '	<div class="search_div">';
					divContent += '<label style="width: 30%;">'+fieldTitle+'</label>';
					divContent+= '<select style="width: 10%;" name="'+code+'_cond">';
					divContent+= '<option value="=">=</option>';
					divContent+= '<option value=">">></option>';
					divContent+= '<option value="<"><</option>';
					divContent+= '<option value=">=">>=</option>';
					divContent+= '<option value="<="><=</option>';
					var fieldValue = data.fieldValue;
					divContent+='</select>';
					divContent += '	<input type="text" placeholder="请输入'+fieldTitle+'" style="width: 30%;" name="'+code+'" id="'+code+'" value="'+fieldValue+'"   />';
					divContent +="</div>";
				}else{
					var isMacroSelect = false;
					if (typeOfField=="macro") {
						if (data.controlType=="select") {
							isMacroSelect = true;
						}
					}
					if (isMacroSelect) {
						divContent += '	<div class="mui-input-row">';
						divContent += '<label style="width: 30%;">'+fieldTitle+'</label>';
						
						divContent += "<select id='" + code + "' name='" + code + "'>"
						var ary = eval(data.fieldOptions);
						$.each(ary, function (n, obj) {
							divContent += "<option value='" + obj.value + "'>" + obj.name + "</option>";
						});
						divContent += "</select>";
						divContent+= '<input type="hidden" name="'+code+'_cond" value = "'+fieldCond +'" />'						
						divContent +="</div>";				
					}
					else {
						var fieldValue = data.fieldValue;
						divContent += '	<div class="mui-input-row">';
						divContent += '<label style="width: 30%;">'+fieldTitle+'</label>';
						divContent += '	<input type="text" placeholder="请输入'+fieldTitle+'"  name="'+code+'" id="'+code+'" value="'+fieldValue+'"   />';
						divContent+= '<input type="hidden" name="'+code+'_cond" value = "'+fieldCond +'" />'
						divContent +="</div>";
					}
				}
				jQuery(_searchContainer).append(divContent);
			});	
			var btn = '<div class="mui-button-row">';
			btn+='<button class="mui-btn mui-btn-primary f_search_btn" type="button" onclick="return false;">搜索</button>';
			btn+= '</div>';
			jQuery(_searchContainer).append(btn);
		},
		ajax_get:function(url,type,datas, myScrollClass){
			var refreshSelector = self.options.pullRefreshContainer;
			var ulSelector =  self.options.ulContainer;
			var liSelector = self.options.liContainer;
			var data_types = self.options.ajaxDatasType;
			var searchContainer = self.options.searchContainer;
			// console.info(datas);
			mui.get(url,datas,function(data){
				var res = data.res;
				if(res == '0'){
					TOTAL = data.result.total;
					if (TOTAL==null) {
						TOTAL = 0;
					}
					// console.info("total=="+TOTAL);
					var canAdd = data.result.canAdd;
					var canEdit = data.result.canEdit;
					var canDel = data.result.canDel;
					var _addContent = canAdd?'<a  class="mui-btn mui-btn-link " id ="op_add"><span class="mui-icon mui-icon-forward">新增</span></a>':'';
					var _opContent = '' ;
					if(canEdit && canDel){
						_opContent+= '<a class="mui-btn mui-btn-red" op="del">删除</a>';
						_opContent+= '<a class="mui-btn mui-btn-yellow" op="edit">编辑</a>';
					}else if(canEdit && !canDel){
						_opContent+= '<a class="mui-btn mui-btn-yellow" op="edit">编辑</a>';
					}else if(!canEdit && canDel ){
						_opContent+= '<a class="mui-btn mui-btn-red" op="del">删除</a>';
					}
					jQuery(".mui-content-padded").html(_addContent);
					if(TOTAL == 0){
						if(type == 1 ){
							myScrollClass.endPullUpToRefresh(true);
						}else if(type == -1){
							jQuery(liSelector).remove();
							myScrollClass.endPullUpToRefresh(true);
						}else{
							jQuery(liSelector).remove();
							myScrollClass.endPullDownToRefresh(); // refresh
						}
					}else{
						if(type == 0 || type == -1){
							jQuery(ulSelector).find("li").remove();
						}
						// console.info(type+"--"+OP);
						if ((type == 1 || type == 0) && OP == "") {
							jQuery(searchContainer + " div").remove();
							if ("conditions" in data.result) {
								var conditions = data.result.conditions;
								if (typeof (conditions) == 'object') {
									if (conditions.length > 0) {
										self.conditionType(conditions);
									}
								}
							}
						}

						var datas = data.result.datas;
						
						mui.each(datas,function(index,data) {
							var _li = '';
							_li += '<li class="mui-table-view-cell  mui-left " rId='+data.id+' >';
							_li += '<div class="mui-slider-right mui-disabled">';
							_li+= _opContent;
							_li+= '</div>';
							_li+= '<div class="mui-slider-handle">';
								mui.each(data.fields,function(index,data){
									var text = data.text == ''?"---":data.text;
									_li+='<div class="mui-table" style="line-height: 2.0; color:#666">';
									_li+='<div class="mui-table-cell mui-col-xs-5">';
									_li+='<span>' + data.title + '</span>';
									_li+='</div>';
									_li+='<div class="mui-table-cell mui-col-xs-5">';
									_li+='<span>' + text + '</span>';
									_li+='</div>';
									_li+='</div>';
								});
							_li+='</div>';
							_li += '</li>';
							if (type == 1) {
								jQuery(ulSelector).append(_li);
								myScrollClass.endPullUpToRefresh(PAGE_NUM * PAGE_SIZE >= TOTAL); // 参数为true代表没有更多数据了。
							} else if (type == 0) {
								jQuery(ulSelector).append(_li);
								myScrollClass.endPullDownToRefresh(); // refresh
								// completed
								if (PAGE_NUM * PAGE_SIZE < TOTAL) {
									myScrollClass.endPullUpToRefresh();
								}
							} else if (type == -1) {
								jQuery(ulSelector).append(_li);
								myScrollClass.endPullUpToRefresh(PAGE_NUM * PAGE_SIZE >= TOTAL); // 参数为true代表没有更多数据了。
							}
						});
					}
				
				}
			},"json");
	    }
	})
})(mui,document,window)
