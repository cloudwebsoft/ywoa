(function($, document, window, undefined) {
	$.ajaxSettings.beforeSend = function(xhr, setting) {
		jQuery.myloading();
		//beforeSend演示,也可在$.ajax({beforeSend:function(){}})中设置单个Ajax的beforeSend
		//console.log('beforeSend:::' + JSON.stringify(setting));
	};
	//设置全局complete
	$.ajaxSettings.complete = function(xhr, status) {
		//console.log('complete:::' + status);
		jQuery.myloading("hide");
	}
	var PAGE_NUM = 1;
	var TOTAL = 0;
	var PAGE_SIZE = 10;
	var OP = "";
	var FLOWS = "flows";
	var MYFLOWS = "myflows";
	var NOTICES = "notices";
	var MESSAGES = "messages";
	var NOTEPAPERS = "notepapers";
	var DOCUMENTS = "documents";
	var WORKPLANS = "workplans";

	var self ;//lzm备注 坑，调用了下拉刷新控件后 this对象 已经改变为 下拉刷新的this所以要设置为全局变量
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
			var downStyle = '';
			if (mui.os.plus) {
				downStyle = 'circle'; // 当plus启用时的下拉刷新样式，目前支持原生5+ ‘circle’ 样式
			}
			mui.init({
				keyEventBind: {
					backbutton: !self.options.isUniWebview //关闭back按键监听
				},
				pullRefresh: {
					container:pullRefreshSelector ,
					down: {
						style: downStyle,
						callback: this.pulldownRefresh
					},
					up: {
						height:20,//可选,默认50.触发下拉刷新拖动距离,
						contentrefresh: '正在加载...',
						contentnomore: '', // 去掉“没有更多数据了”
						auto:true,
						callback: this.pullupRefresh
					}
				}
			});
			if (mui.os.plus) {
				mui.plusReady(function() {
					setTimeout(function() {
						// mui(pullRefreshSelector).pullRefresh().pullupLoading();
					}, 1000);
				});
			} else {
				mui.ready(function() {
					mui(pullRefreshSelector).pullRefresh().pullupLoading();
				});
			}
		},
		pulldownRefresh: function () {
			// 下拉回到第一页
			var params = self.options.ajax_params;
			var searchContainer = self.options.searchContainer;
			jQuery(searchContainer).val("");
			PAGE_NUM = 1;
			OP = "";
			var ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP}, params);
			self.ajax_get(0, ajax_param);
		},
		pullupRefresh: function () {
			var params = self.options.ajax_params;
			var ajax_param;
			var data_types = self.options.ajaxDatasType;
			var searchContainer = self.options.searchContainer;
			var val = jQuery(searchContainer).val();
			if (TOTAL == 0) {
				PAGE_NUM = 1;
				if (data_types == FLOWS || data_types == MYFLOWS) {
					// 如果在流程页面上，搜索操作时，且搜索框内不为空，则进行搜索分页
					if (OP == "search" && val != "") {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP, "title": val}, params);
					}
					else {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP}, params);
					}
				}
				else {
					if (OP == "search" && val != "") {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP, "cond": "title", "what": val}, params);
					}
					else {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP}, params);
					}
				}
			} else if (PAGE_NUM * PAGE_SIZE < TOTAL) {
				PAGE_NUM += 1;
				if (data_types == FLOWS || data_types == MYFLOWS) {
					// 如果在流程页面上，搜索操作时，且搜索框内不为空，则进行搜索分页
					if (OP == "search" && val != "") {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP, "title": val}, params);
					}
					else {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP}, params);
					}
				}
				else {
					if (OP == "search" && val != "") {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP, "cond": "title", "what": val}, params);
					}
					else {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP}, params);
					}
				}
			}
			self.ajax_get(1, ajax_param);
		},
		liContentByType:function(type, data){
			var li = '';
			if(type == FLOWS || type == MYFLOWS){
				var flowId = data.flowId;
				var myActionId = data.myActionId;
				var beginDate = data.beginDate;
				var status = data.status;
				var typeName = data.typeName;
				var name = data.name;
				var imgUrl = type == FLOWS ? "../images/flow_wait.png" : "../images/flow.png";
				var li = '<li class="mui-table-view-cell" flowId="' + flowId + '" myActionId ="' + myActionId + '" >';
				li += '<div class="mui-table">';
				li += '<div class="mui-table-cell div-col-xs mui-left">';
				li += '<img class="mui-pull-left img-center" align="center" src="' + imgUrl + '">';
				li += '</div>'
				li += '<div class="mui-table-cell mui-col-xs-11">';
				li += '<h4 class="mui-ellipsis">' + typeName + '</h4>';
				if (("lastUser" in data) && type == MYFLOWS) {
					var lastUser = data.lastUser;
					if (lastUser != '') {
						li += '<h5 class="mui-ellipsis">ID: ' + flowId + '  ' + status + '(' + data.lastUser + ')</h5>';
					} else {
						li += '<h5 class="mui-ellipsis">ID: ' + flowId + '  ' + status + '</h5>';
					}
				} else {
					li += '<h5 class="mui-ellipsis">ID: ' + flowId + "  " + status + '</h5>';
				}
				li += '<p class="mui-h6 mui-ellipsis">' + name + '</p>';
				li += '</div>';
				li += '<div class="mui-table-cell mui-col-xs-2 mui-text-right">';
				li += '<span class="mui-h6">' + beginDate + '</span>';
				li += '</div>'
				li += '</div>';
			}else if(type == NOTICES){
				li += '<li class="mui-table-view-cell mui-media" id="'+data.id+'">';
				li += '<div class="mui-slider-right mui-disabled">';
				li += '<a class="mui-btn mui-btn-red op-del">删除</a>';
				li += '<a class="mui-btn mui-btn-yellow op-edit">编辑</a>';
				li += '</div>';
				li += '<div class="mui-slider-handle">';
				li += ' <img class="mui-media-object mui-pull-left" src="../images/notice.png" />'
				li += '	<div class="mui-media-body">';
				li += ' <span>'+data.title+'</span><span class="mui-pull-right createdate">'+data.createdate+'</span><p class="mui-ellipsis">'+data.sender+'</p>'
				li += '</div>';
				li += '</div>';
				li += '</li>';
				return li;
			}else if(type == MESSAGES){
				var li = '<li class="mui-table-view-cell mui-media" id="'+data.id+'">';
				li+= '<a class="mui-navigate-right">';
				var haveread = data.haveread;
				if(haveread == "false"){
					li+= '<img class="mui-media-object mui-pull-left" src="../images/mail_noread.png">';
				}else{
					li+= '<img class="mui-media-object mui-pull-left" src="../images/mail_read.png">';
				}
				li+='<div class="mui-media-body">';
				li += '<span>'+data.title+'</span><span class="mui-pull-right createdate">'+data.createdate+'</span><p class="mui-ellipsis">'+data.sender+'</p>'
				li+='</div>';
				li+= '</a>'
				li+= '</li>';
				return li;
			}
			else if(type == NOTEPAPERS){
				li = '<li class="mui-table-view-cell mui-media" id="'+data.id+'">';
				li+= '<a class="mui-navigate-right">';
				if (data.isClosed=="true") {
					li+= '<img class="mui-media-object mui-pull-left" src="../../images/task_complete.png" style="width:24px; height:24px">';
				}
				else {
					li+= '<img class="mui-media-object mui-pull-left" src="../../images/task_ongoing.png" style="width:24px; height:24px">';
				}
				li+='<div class="mui-media-body">';
				li += '<span>'+data.title+'</span><span class="mui-pull-right createdate">'+data.startTime+'</span>'
				li+='</div>';
				li+= '</a>';
				li+= '</li>';
				return li;
			}
			else if (type==DOCUMENTS) {
				var li = '<li class="mui-table-view-cell mui-media" id="' + data.id + '">';
				li += '<a class="mui-navigate-right">';
				li += '<div class="mui-media-body">';
				li += '<div><span><img src="../../fileark/images/' + data.icon + '" style="margin-right:10px;height:24px;vertical-align: middle;margin-bottom: 3px"/></span><span>' + data.title + '</span></div>';
				li += '<p class="mui-pull-left mui-ellipsis">' + data.author + '</p><p class="mui-pull-right mui-ellipsis">' + data.createdate + '</p>'
				li += '</div>';
				li += '</a>';
				li += '</li>';
				return li;
			}
			else if(type == WORKPLANS) {
				var li = '<li class="mui-table-view-cell mui-media" id="' + data.id + '">';
				li += '<a class="mui-navigate-right">';
				if (data.progress == 100) {
					li += '<i class="fa fa-calendar-check-o mui-pull-left" style="margin-right: 10px" aria-hidden="true"></i>';
				} else {
					li += '<i class="fa fa-calendar-times-o mui-pull-left" style="margin-right: 10px" aria-hidden="true"></i>';
				}
				li+= '<div class="mui-media-body">';
				li+= '<span>'+data.title+'</span><span class="mui-pull-right createdate">'+data.begindate+'</span><p class="mui-ellipsis">'+data.realName+'&nbsp;&nbsp;' + data.progress + '%&nbsp;&nbsp;</p>'
				li+= '</div>';
				li+= '</a>'
				li+= '</li>';
				return li;
			}
			return li;
		},
		bindEvent: function() {
			var self = this;
			self.bindTapItemEvent();
			self.bindSearchEvent();
		},
		bindSearchEvent: function () {
			var self = this;
			var searchContainer = self.options.searchContainer;
			var search = self.element.querySelector(searchContainer);
			search.addEventListener("keyup", function (e) {
				if (e.keyCode == 13) {
					OP = "search";
					PAGE_NUM = 1;
					var val = jQuery(searchContainer).val();
					var params = self.options.ajax_params;
					var data_types = self.options.ajaxDatasType;
					var ajax_param;
					if (data_types == FLOWS || data_types == MYFLOWS) {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP, "title": val}, params);
					} else {
						ajax_param = $.extend(true, {"pagenum": PAGE_NUM, "pagesize": PAGE_SIZE, "op": OP, "cond": "title", "what": val}, params);
					}

					self.ajax_get(-1, ajax_param);
				}
			});
		},
		bindTapItemEvent:function(){
			var self = this;
			var params = self.options.ajax_params;

			var ulSelector =  self.options.ulContainer;
			var liSelector = self.options.liContainer;
			var data_types = self.options.ajaxDatasType;
			var url = '';
			$(ulSelector).on("tap",liSelector,function(){
				if(data_types == FLOWS){
					var myActionId = this.getAttribute("myActionId");
					var flowId = this.getAttribute("flowId");
					var skey = params.skey;
					url = "../flow/flow_dispose.jsp?skey="+skey+"&flowId="+flowId+"&myActionId="+myActionId + "&isUniWebview=" + self.options.isUniWebview;
				}else if(data_types == MYFLOWS){
					var flowId = this.getAttribute("flowId");
					var skey = params.skey;
					url = "../flow/flow_attend_detail.jsp?skey="+skey+"&flowId="+flowId + "&isUniWebview=" + self.options.isUniWebview;
				}else if(data_types == NOTICES){
					var id = this.getAttribute("id");
					var skey = params.skey;
					url = "../notice/notice_detail.jsp?skey="+skey+"&id="+id + "&isUniWebview=" + self.options.isUniWebview;
				}else if(data_types == MESSAGES){
					var skey = params.skey;
					var id = this.getAttribute("id");
					url = "../message/message_detail.jsp?id="+id+"&skey="+skey + "&isUniWebview=" + self.options.isUniWebview;
				}
				else if(data_types == NOTEPAPERS){
					var skey = params.skey;
					var id = this.getAttribute("id");
					url = "../calendar/calendar_show.jsp?id="+id+"&skey="+skey + "&isUniWebview=" + self.options.isUniWebview
				}
				else if (data_types==DOCUMENTS) {
					var skey = params.skey;
					var id = this.getAttribute("id");
					url = "../fileark/doc_show.jsp?id="+id+"&skey="+skey + "&isUniWebview=" + self.options.isUniWebview
				}
				else if (data_types==WORKPLANS) {
					var skey = params.skey;
					var id = this.getAttribute("id");
					url = "../workplan/workplan_show.jsp?id="+id+"&skey="+skey + "&isUniWebview=" + self.options.isUniWebview
				}

				// 加入其它相关的参数
				var arr = self.options.params;
				for(var i in arr){
					url += "&" + arr[i].name + "=" + encodeURI(arr[i].value);
				}
				// console.log(url);
				if (self.options.isUniWebview) {
					window.location.href = url;
					// 会导致后退至九宫格
					/*mui.openWindow({
						"url":url,
						"styles": {
							top: '80px'
						}
					})*/
				}
				else {
					mui.openWindow({
						"url":url,
					})
				}
			})
		},
		ajax_get:function(type, datas){
			var refreshSelector = self.options.pullRefreshContainer;
			var ulSelector = self.options.ulContainer;
			var liSelector = self.options.liContainer;
			var data_types = self.options.ajaxDatasType;
			mui.post(self.options.url, datas,function(data){
				console.log('mui.PullToRefresh.wx.js ajax_get self.options.url=' + self.options.url);
				var res = data.res;
				var data_arrs;
				if(res == '0'){
					// console.log(data_types);
					if(data_types == FLOWS || data_types == MYFLOWS){
						data_arrs = data.result.flows;
					}else if(data_types == NOTICES){
						data_arrs = data.result.notices;
					}else if(data_types == MESSAGES){
						data_arrs = data.result.messages;
					}
					else if (data_types == NOTEPAPERS) {
						data_arrs = data.result.notepapers;
					}
					else if (data_types==DOCUMENTS) {
						data_arrs = data.result.documents;
					}
					else if (data_types==WORKPLANS) {
						data_arrs = data.result.workplans;
					}
					TOTAL = data.total;
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
						mui.each(data_arrs,function(index,data){
							var li = self.liContentByType(data_types,data);
							if(type == 1){
								setTimeout(function() {
									mui(refreshSelector).pullRefresh().endPullupToRefresh(PAGE_NUM*PAGE_SIZE >=TOTAL); //参数为true代表没有更多数据了。
								}, 1000);
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
				}
			},"json");
		}
	})
})(mui,document,window)
