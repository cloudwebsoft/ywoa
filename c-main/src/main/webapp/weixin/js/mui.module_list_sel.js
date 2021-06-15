(function($,window,document,undefined){

	$.ajaxSettings.beforeSend = function(xhr, setting) {
		jQuery.myloading();
	};
	//设置全局complete
	$.ajaxSettings.complete = function(xhr, status) {
		jQuery.myloading("hide");
	}
	var win = window;
	var doc = document;
	var PAGE_NUM = 1;
	var TOTAL = 0;
	var PAGE_SIZE = 10;
	var OP = "";
	var FORM_FIELD = "form_field";

	var self ;//罗备注 坑爹 调用了下拉刷新控件后 this对象 已经改变为 下拉刷新的this所以要设置为全局变量
	$.PullToRefrshList = $.Class.extend({
		init: function(element, options) {
					 this.element = element,
					 this.default = {
					 	"pullRefreshContainer":"#pullrefresh",
					 	"ulContainer":".mui-table-view",
					 	"liContainer":".mui-table-view-cell",
					 	"searchContainer":".mui-input-clear"
					 }
					 this.options = $.extend(true,this.default,options);
					 this.bindEvent();
			 },
		loadListDate:function(){
			self = this;
			var pullRefreshSelector = self.options.pullRefreshContainer;
			mui.init({
				pullRefresh: {
					container:pullRefreshSelector ,
					down: {
						callback: this.pulldownRefresh
					},
					up: {
						height:20,//可选,默认50.触发下拉刷新拖动距离,
						contentrefresh: '正在加载...',
						auto:true,
						callback: this.pullupRefresh
					}
				}
			});
			if (mui.os.plus) {
				mui.plusReady(function() {
					setTimeout(function() {
						mui(pullRefreshSelector).pullRefresh().pullupLoading();
					}, 1000);
				});
			} else {
				mui.ready(function() {
					mui(pullRefreshSelector).pullRefresh().pullupLoading();
				});
			}
		},
		pulldownRefresh:function(){
			var params = self.options.ajax_params;
			var searchContainer = self.options.searchContainer;
	    	jQuery(searchContainer).val("");
			PAGE_NUM = 1;
			OP = "";
			var ajax_param = $.extend(true,{"CPages":PAGE_NUM},params);
			self.ajax_get(self.options.url,0,ajax_param);
		},
		pullupRefresh:function(){
			var params = self.options.ajax_params;
			if(TOTAL == 0){
				PAGE_NUM = 1;
				var ajax_param = $.extend(true,{"CPages":PAGE_NUM},params);
				self.ajax_get(self.options.url,1,ajax_param);
			}else if(PAGE_NUM*PAGE_SIZE <TOTAL){
				PAGE_NUM += 1;
				var ajax_param = $.extend(true,{"CPages":PAGE_NUM},params);
				self.ajax_get(self.options.url,1,ajax_param);
			}
			
		},
		liContentByType:function(dataType,data){
			var type = dataType.type;
			
			var li = '';
			var data_arrs = data.fields;
			if(type == LIST_TYPE.FORM_FIELD){
				var byValue = data.byValue;
				var showValue= data.showValue;
				var parentFieldMaps ='';
				parentFieldMaps = JSON.stringify(data.parentFieldMaps); //可以将json对象转换成json对符串 	
				li += '<li class="mui-table-view-cell mui-checkbox mui-left" byValue="'+byValue+'" showValue="'+showValue+'" parentFieldMaps='+parentFieldMaps+' >';
				li += '<input name="checkbox1" class="ck" value="0" type="checkbox">';
				mui.each(data_arrs,function(index,data){
					li+='<div class="mui-media-body">';
					li+= data.title+ '<p >'+data.text+'</p>';
					li+='</div>';
				});
				
				
			}else if(type == LIST_TYPE.NEST_SHEET_CHOOSE_SELECT){ //嵌套表选择
				var row_id = data.rId;
				li += '<li class="mui-table-view-cell mui-checkbox mui-left " rId='+row_id+' >';
				li += '<input name="checkbox1" class="ck" value="0" type="checkbox">';
				mui.each(data_arrs,function(index,data){
					li+='<div class="mui-media-body">';
					li+= data.title+ '<p >'+data.text+'</p>';
					li+='</div>';
				});
			}else if(type == LIST_TYPE.NEST_SHEET_SELECT)
			{
				var op = dataType.op;
				var isEditable = op.isEditable;
				var canDel = op.canDel;
				var canEdit = op.canEdit;
				var row_id = data.rId;
				if((!canDel && !canEdit) || !isEditable){
					li += '<li class="mui-table-view-cell  mui-left " rId='+row_id+' >';
					mui.each(data_arrs,function(index,data){
						li+='<div class="mui-media-body">';
						li+= data.title+ '<p >'+data.text+'</p>';
						li+='</div>';
					});
					
				}else{
					var opContent = '';
					if(canDel && canEdit && isEditable){ 
						opContent+= '<a class="mui-btn mui-btn-red" op="del">删除</a>';
						opContent+= '<a class="mui-btn mui-btn-yellow" op="edit">编辑</a>';
					}else if(!canDel && canEdit && isEditable){
						opContent+= '<a class="mui-btn mui-btn-yellow" op="edit">编辑</a>';	
					}else if(canDel && !canEdit && isEditable){
						opContent+= '<a class="mui-btn mui-btn-red" op="del">删除</a>';
					}
					
					var row_id = data.rId;
					li += '<li class="mui-table-view-cell mui-checkbox mui-left " rId='+row_id+' >';
					li += '<div class="mui-slider-right mui-disabled">';
					li+= opContent;
					li+= '</div>';
					li+= '<div class="mui-slider-handle">'
					mui.each(data_arrs,function(index,data){
						var text = data.text == ''?"---":data.text;
						li+='<div class="mui-media-body">';
						li+= data.title+ '<p >'+text+'</p>';
						li+='</div>';
					});
					li+='</div>';
				}
			}
			li+= '</li>';
			return li;
		},
		conditionType:function(data_arr){
			mui.each(data_arr,function(index, data){
				var type = data.fieldType;
				var divContent = "";
				var code = data.fieldName;
				var fieldTitle = data.fieldTitle;
				var fieldCond = data.fieldCond;
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
					divContent += '<label style="width: 10%;">'+fieldTitle+'</label>';
					divContent+= '<select style="width: 30%;" name="'+code+'_cond">';
					divContent+= '<option value="=">等于</option>';
					divContent+= '<option value=">">></option>';
					divContent+= '<option value="<"><</option>';
					divContent+= '<option value=">=">>=</option>';
					divContent+= '<option value="<="><=</option>';
					var fieldValue = data.fieldValue;
					divContent+='</select>';
					divContent += '	<input type="text" placeholder="请输入'+fieldTitle+'" style="width: 30%;" name="'+code+'" id="'+code+'" value="'+fieldValue+'"   />';
					divContent +="</div>";
				}else{
					divContent += '	<div class="mui-input-row">';
					if (data.type && data.type=="checkbox") {
						divContent += ' <div class="mui-input-row mui-checkbox">';
					}
					divContent += '<label style="width: 10%;">'+fieldTitle+'</label>';
					
					var fieldValue = data.fieldValue;					
					var fieldOptions = "";
					if("fieldOptions" in data){
						fieldOptions = data.fieldOptions;
					}
					if (fieldOptions!="") {
						divContent += '<select name="' + code + '" id="' + code + '">';
						var arrOpt = eval(fieldOptions);
						$.each(arrOpt,function(index,item){
							divContent += '<option value="' + item.value + '">' + item.name + '</option>';
						});
						divContent += "</select>";
					}
					else {
						if (data.type && data.type=="checkbox") {
							var checked = "";
							if (fieldValue=="1") {
								checked = "checked";
							}
							divContent += '	<input type="checkbox" name="'+code+'" id="'+code+'" ' + checked + ' value="1"   />';
						}
						else {						
							divContent += '	<input type="text" placeholder="请输入'+fieldTitle+'"  name="'+code+'" id="'+code+'" value="'+fieldValue+'"   />';
						}
					}
					divContent+= '<input type="hidden" name="'+code+'_cond" value = "'+fieldCond +'" />'
					divContent +="</div>";
				}
				
				jQuery("#search_content").append(divContent);
			});	

			var btn = '<div class="mui-button-row">';
			btn+='<button class="mui-btn mui-btn-primary f_search_btn"  type="button" onclick="return false;">搜索</button>';
			btn+= '</div>';
			jQuery("#search_content").append(btn);
		},
		bindEvent: function() {
			var self = this;
			var data_types = self.options.ajaxDatasType;
			var isWx = self.options.isWx;
			switch (data_types) {
				// 来自nest_sheet_choose_select.jsp中
				case LIST_TYPE.NEST_SHEET_CHOOSE_SELECT:
					var done = win.getElementById("done");
					done.addEventListener('tap', function () {
						var ul_list = win.getElementById("ul_nest_sheet_choose_select");
						var count = ul_list.querySelectorAll('input[type="checkbox"]:checked').length;
						if (count > 0) {
							var checkboxArray = [].slice.call(ul_list.querySelectorAll('input[type="checkbox"]'));
							var checkedValues = [];
							checkboxArray.forEach(function (box) {
								if (box.checked) {
									checkedValues.push(box.parentNode.getAttribute("rid"));
								}
							});
							if (checkedValues.length > 0) {
								var params = self.options.ajax_params;
								var cVals = checkedValues.toString();
								params.ids = cVals;
								params.nestType = "nest_sheet";
								params.op = "selBatch";
								var urlParams = self.options.urlParams;
								mui.get(self.options.url, params, function (data) {
									var res = data.res;
									if (res == 0) {
										$.toast("选择成功!");
										if (isWx == 1) {
											nestSheetJump("请选择", "../macro/nest_sheet_select.jsp" + self.options.urlParams + "&isWx=1", data.sums, data.nestFormCode);
										} else {
											var params = self.options.urlParams;
											if (typeof (data.sums) == 'object') {
												var str = JSON.stringify(data.sums);
												if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
													params += "&flow_nestsheet=" + encodeURI(str);
												} else if (/(Android)/i.test(navigator.userAgent)) {
													javascript:nestSheetInterface.calculateNestSheet(str);
												} else {
												}
												;
											}
											mui.openWindow({
												"url": "../macro/nest_sheet_select.jsp" + params
											})
										}
									} else {
										$.toast("选择失败 ！");
									}
								}, "json");
							} else {
								mui.alert('你没选择数据');
							}
						} else {
							mui.alert('你没选择数据!');
						}

					}, false);

					mui('#ul_nest_sheet_choose_select').on('change', 'input', function () {

						var ul_list = win.getElementById("ul_nest_sheet_choose_select");
						var count = ul_list.querySelectorAll('input[type="checkbox"]:checked').length;
						var value = count ? "完成(" + count + ")" : "完成";

						done.innerHTML = value;
					});
			 		break;
			 	case LIST_TYPE.FORM_FIELD:
				 var done = win.getElementById("done");
					done.addEventListener('tap', function() {
						var ul_list = win.getElementById("form_field_ul");
						var count = ul_list.querySelectorAll('input[type="checkbox"]:checked').length;
						if(count>0){
							var ck = ul_list.querySelectorAll('input[type="checkbox"]:checked')[0];
							var parent = ck.parentNode;
							var btnArray = ['确认', '取消'];
								mui.confirm('确认选择该条记录？', '提示', btnArray, function(e) {
								if(e.index == 0){
									$.toast("选择成功!");
									var parentFieldMaps = parent.getAttribute('parentFieldMaps');
									var byValue = parent.getAttribute("byValue");
									var showValue = parent.getAttribute("showValue");
									var openerFieldName = self.options.ajax_params.openerFieldName;
									if(self.options.isWx == 1){
										doneChooseField(parentFieldMaps,byValue,showValue,openerFieldName);
									}else{
										if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
											var ios_json = {"byValue":byValue,"showValue":showValue,"openerFieldName":openerFieldName,"parentFieldMaps":parentFieldMaps == ""?"":JSON.parse(parentFieldMaps)};
											var ios_json_str = JSON.stringify(ios_json);
											window.location.href="?"+ios_json_str;	
										} else if (/(Android)/i.test(navigator.userAgent)) {
											javascript:jsFieldInterface.getParentMaps(parentFieldMaps,byValue,showValue,openerFieldName);
										} else {
										}
									}
								}					
							});
						}else{
							mui.alert('你没选择数据!');
							
						}
						
					}, false);
					
				 mui('#form_field_ul').on('change','input',function(){
					 var ul_list = win.getElementById("form_field_ul");
					 var checks = ul_list.querySelectorAll('input[type="checkbox"]');
						var obj = this;
						if(obj.checked)
						{
							for(var i=0;i<checks.length;i++){
							checks[i].checked = false;
							}
							obj.checked = true;
						}else
						{
							for(var i=0;i<checks.length;i++){
							checks[i].checked = false;
							}
						}
											
						var count = ul_list.querySelectorAll('input[type="checkbox"]:checked').length;
						var value = count ? "完成(" + count + ")" : "完成";
					
						done.innerHTML = value;
					});
				// self.singleSelection();
			 break;
			 case LIST_TYPE.NEST_SHEET_SELECT:
				 	var urlParams = self.options.urlParams;//超链接
					$(".mui-content").on('tap','.mui-btn',function(){
						var id = this.getAttribute("id");
						if(id == 'op_sel'){
							var parentId = this.getAttribute("parentId");
							if(self.options.isWx == 1){
								nestSheetJump("请选择","../macro/nest_sheet_choose_select.jsp"+urlParams+"&parentId="+parentId+"&isWx=1",null);
							}else{
								mui.openWindow({
								    "url":"../macro/nest_sheet_choose_select.jsp"+urlParams+"&parentId="+parentId
								})		
							}
						
						}else if(id == 'op_add'  ){
							if(self.options.isWx == 1){
								nestSheetJump("列表","../macro/nest_sheet_add_edit.jsp"+urlParams+"&isWx=1",null);
							}else{
								mui.openWindow({
									 "url":"../macro/nest_sheet_add_edit.jsp"+urlParams
								})		
							}
						}
					});
					self.deleteBtnEvent();
			 break;
			 default:break;
			}
			
			$(".mui-input-group").on("tap",".f_search_btn",function(){
				var url_params =  jQuery(".mui-input-group").serialize() ;
				var n_url = self.options.url; // +"?"+url_params; 以免url_params中有中文致服务器端接收乱码，故改为将数据转为json通过post发送

				var o = {};
				var a = jQuery(".mui-input-group").serializeArray();
				$.each(a, function() {
					if (o[this.name] !== undefined) {
						if (!o[this.name].push) {
							o[this.name] = [o[this.name]];
						}
						o[this.name].push(this.value || '');
					} else {
						o[this.name] = this.value || '';
					}
				});
				
				PAGE_NUM = 1;
				var params = self.options.ajax_params;
				var data_types = self.options.ajaxDatasType;
				var ajax_param;
			    ajax_param = $.extend(true,{"pagenum":PAGE_NUM,"pagesize":PAGE_SIZE,"op":"search"},params);
				ajax_param = $.extend(true,o,ajax_param);
				self.ajax_get(n_url, -1, ajax_param);
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
		},
		singleSelection:function(){
			var self = this;
			var selector = self.options.ulContainer;
		/*	$(selector).on('tap','.mui-table-view-cell',function(){
				var child = this.childNodes[0]; //获得子元素
				var parentFieldMaps = this.getAttribute('parentFieldMaps');
				var byValue = this.getAttribute("byValue");
				var showValue = this.getAttribute("showValue");
				var openerFieldName = self.options.ajax_params.openerFieldName;
				if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
					var ios_json = {"byValue":byValue,"showValue":showValue,"openerFieldName":openerFieldName,"parentFieldMaps":parentFieldMaps == ""?"":JSON.parse(parentFieldMaps)};
					var ios_json_str = JSON.stringify(ios_json);
					window.location.href="?"+ios_json_str;	
				} else if (/(Android)/i.test(navigator.userAgent)) {
					javascript:jsFieldInterface.getParentMaps(parentFieldMaps,byValue,showValue,openerFieldName);
				} else {
				};

			}); */
		},
		deleteBtnEvent:function(){
			var self = this;
		
			var btnArray = ['确认', '取消'];
			$(self.options.ulContainer).on('tap', '.mui-btn', function(event) {
				var elem = this;
				var op = this.getAttribute("op");
				var li = elem.parentNode.parentNode;
				var formCodeRelated = self.options.ajax_params.formCode;
				var id = li.getAttribute("rId");
				if(op == "edit"){
					var urlParams = self.options.urlParams;//超链接
					setTimeout(function() {
						$.swipeoutClose(li);
					}, 0);
					if(self.options.isWx == 1){
						nestSheetJump("","../macro/nest_sheet_add_edit.jsp"+urlParams+"&isWx=1&id="+id,null);
					}else{
						mui.openWindow({
						    "url":"../macro/nest_sheet_add_edit.jsp"+urlParams+"&id="+id
						})		
					}
				}else{
					var parentCode = self.options.ajax_params.parentCode;
					var datas = {"formCodeRelated":formCodeRelated,"id":id,"formCode":parentCode};		
					mui.confirm('确认删除该条记录？', '提示', btnArray, function(e) {
						if(e.index == 0){
						
						mui.get(AJAX_REQUEST_URL.NEST_SHEET_DELETE,datas,function(data){	
								var res = data.res;
								var msg = data.msg;
							
								if(res == "0"){
									$.toast("删除成功!");
									jQuery.myloading("hide");									
									if (e.index == 0) {
										li.parentNode.removeChild(li);
										if(self.options.isWx == 1){
											if(typeof(data.sums) == 'object'){
												calNestSheet(data.sums);
											}		
										}else{
											if(typeof(data.sums) == 'object'){
												var str = JSON.stringify(data.sums);
												if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
													window.location.href="url?"+str;	
												} else if (/(Android)/i.test(navigator.userAgent)) {
													javascript:nestSheetInterface.calculateNestSheet(str);
												} else {		   
												}
										    }
										}							
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
		ajax_get:function(url,type,datas){
			
			var refreshSelector = self.options.pullRefreshContainer;
			var ulSelector =  self.options.ulContainer;
			var liSelector = self.options.liContainer;
			var data_types = self.options.ajaxDatasType;
			mui.get(url,datas,function(data){

				var res = data.res;
				var data_arrs = [];
				if(res == '0'){
					var liTypeJson = {"type":data_types};
					if(data_types == LIST_TYPE.FORM_FIELD)
					{
						TOTAL = data.result.totalCount;
						data_arrs = data.datas;
					}else if(data_types == LIST_TYPE.NEST_SHEET_CHOOSE_SELECT){
						data_arrs = data.datas;
						TOTAL = data.result.totalCount;
					}else if(data_types == LIST_TYPE.NEST_SHEET_SELECT){
						var parentId = data.result.parentId;

						var canSel = data.result.canSel;
						var canDel = data.result.canDel;
						var canAdd = data.result.canAdd;
						var canEdit = data.result.canEdit;
						var isEditable = self.options.ajax_params.isEditable;
						if("datas" in data.result){
							data_arrs = data.result.datas;
							TOTAL = data_arrs.length;
						}else{
							TOTAL = 0;
						}

						var opContent = '';
						if(isEditable){
							opContent = '<div class="mui-content-padded">';
							opContent += canSel?'<a class="mui-btn mui-btn-link " parentId="'+parentId+'" id="op_sel"><span class="mui-icon mui-icon-forward">选择</span></a>':"";
							opContent += canAdd?'<a class="mui-btn mui-btn-link " id="op_add"><span class="mui-icon mui-icon-forward">新增</span></a>':'';
							opContent += '</div>';
						}

						var op = {"canDel":canDel,"canEdit":canEdit,"isEditable":isEditable};
						liTypeJson.op = op;

						win.getElementById("op_con").innerHTML = opContent;
					}
					if(TOTAL == 0){
						if(type == 1){
							mui(refreshSelector).pullRefresh().endPullupToRefresh(true);
						}else{
							jQuery(liSelector).remove();
							mui(refreshSelector).pullRefresh().endPulldownToRefresh(); //refresh completed
						}
					}else{
						if(type == 0 || type == -1){
							jQuery(liSelector).remove();
						}
						if(type == 1 || type == 0){
							jQuery("#search_content div").remove();
							if("conditions" in   data.result){
								var conditions = data.result.conditions;
								if(typeof(conditions) == 'object'){
									if(conditions.length>0){
										self.conditionType(conditions);
									}
								}
							}
						}

						mui.each(data_arrs,function(index,data){
							var li = self.liContentByType(liTypeJson,data);
							if(type == 1){
								mui(refreshSelector).pullRefresh().endPullupToRefresh(PAGE_NUM*PAGE_SIZE >=TOTAL); //参数为true代表没有更多数据了。
								jQuery(ulSelector).append(li);
							}else if(type == 0){
								jQuery(ulSelector).append(li);
								mui(refreshSelector).pullRefresh().endPulldownToRefresh(); //refresh completed
								if(PAGE_NUM*PAGE_SIZE <TOTAL){
									mui(refreshSelector).pullRefresh().enablePullupToRefresh();
								}
							}else if(type == -1){
								jQuery(ulSelector).append(li);
								mui(refreshSelector).pullRefresh().endPullupToRefresh(PAGE_NUM*PAGE_SIZE >=TOTAL); //参数为true代表没有更多数据了。
							}
						})
					}
				}else{
					mui(refreshSelector).pullRefresh().endPullupToRefresh(true);
					var msg = data.msg;
					$.toast(msg);
				}
			},"json");
	    }
	})
})(mui,document,window)
