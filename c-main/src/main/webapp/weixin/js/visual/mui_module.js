(function ($, document, window, undefined) {
	$.ajaxSettings.beforeSend = function(xhr, setting) {
		jQuery.myloading();
	};
	$.ajaxSettings.complete = function(xhr, status) {
		jQuery.myloading("hide");
	}
	var self ;
	var Form;
	$.ModuleForm = $.Class.extend({
		init: function(element, options) {
					 this.element = element,
					 this.default = {
						"formSelector":".mui-input-group",
						"ulSelector":".mui-table-view",
						"flowId":-1
					 }
					 this.options = $.extend(true,this.default,options);
					 Form = new $.Form(this.element, this.options);
			 },
		showImg: function(path) {
			var openPhotoSwipe = function() {
			var pswpElement = jQuery('.pswp')[0];
				var items = [{
					// src: "../../public/showImg.do?path="+encodeURI(path),
					src: path,
					w: 964,
					h: 1024
				}
				];
				// define options (if needed)
				var options = {
					// history & focus options are disabled on CodePen        
					history: false,
					focus: false,
					showAnimationDuration: 0,
					hideAnimationDuration: 0
				};
				var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
				gallery.init();
			};
			openPhotoSwipe();
		},			 
		moduleInit:function(){  // 智能表单初始化
			var self = this;
			var content = self.element;
			var url = AJAX_REQUEST_URL.MODULE_ADD_INIT;
			var skey = self.options.skey;
			var id = self.options.id;
			var moduleCode = self.options.moduleCode;
			var formCodeRelated = self.options.formCodeRelated;
			var parentId = self.options.parentId;
			var pageType = self.options.pageType;
			var datas = {"skey":skey,"id":id,"moduleCode":moduleCode};
			var extraData = self.options.extraData;
			extraData = $.parseJSON(extraData);
			// 合并
			datas = $.extend({}, datas, extraData);

			if (id != 0 && parentId == 0) {
				// 正常编辑
				if ("add" == pageType) {
					url = AJAX_REQUEST_URL.MODULE_ADD_INIT;
				} else {
					url = AJAX_REQUEST_URL.MODULE_EDIT_INIT;
				}
			} else if (id == 0 && parentId == 0) {
				// 正常新增
				url = AJAX_REQUEST_URL.MODULE_ADD_INIT;
			} else if (id = 0 && parentId != 0) {
				// 嵌套表 新增
				url = AJAX_REQUEST_URL.MODULE_CHILD_ADD_INIT;
			} else {
				// 嵌套表编辑
				url = AJAX_REQUEST_URL.MODULE_CHILD_EDIT_INIT;
				datas.formCodeRelated = formCodeRelated;
				datas.parentId = parentId;
			}
			$.post(url, datas, function (data) {
				var res = data.res;
				if (res == '0') {
					var hasAttach = data.hasAttach;
					var fields = data.fields;
					console.log('data', data);
					if (fields.length > 0) {
						Form.initForms(0, -1, fields, data.formCode, data);// 初始化Form表单
					}
					if ("files" in data) {
						var _files = data.files;
						if (_files.length > 0) {
							var _ul = Form.flowInitFiles(_files);
							jQuery(".mui-input-group").append(_ul);
						}
					}
					var btnContent = '<div class="mui-button-row">';
					btnContent += '	<button type="button" class="mui-btn mui-btn-primary mui-btn-outlined form_submit">提交</button>';
					// console.log(hasAttach);
					if (hasAttach) {
						btnContent += '	<button type="button" class="mui-btn mui-btn-primary mui-btn-outlined capture_btn">照片</button>';
					}
					btnContent += '<input type="hidden" id="cws_id" name="cws_id" value="' + parentId + '"/>'
					btnContent += '</div>';
					jQuery(".mui-input-group").append(btnContent);

					if (data.viewJs) {
						var s0 = document.createElement('script');
						s0.text = data.viewJs;
						document.body.appendChild(s0);
					}

					try {
						onModuleInited();
					} catch (e) {
					}
					try {
						initPhotoSwipe();
					} catch (e) {
					}
				}
			}, "json");
			 
			mui(".mui-input-group").on("tap",".form_submit",function(){
				var _tips = "";
				jQuery("div[data-isnull='false']").each(function(i){
					// 如果是嵌套表格，则不检查是否必填，由后台检查
					if(jQuery(this).find('.nestSheetSelect')[0]) {
						return false;
					}
					// 如果是图像宏控件，则不检查是否必填，由后台检查
					if (jQuery(this).find('.capture_btn')[0]) {
						return false;
					}

					var _code = jQuery(this).data("code");
					// var _val = jQuery("#"+_code).val();
					// raty标值控件的name对应字段，而id值为***_raty-scroe
					var _val = jQuery("[name=" + _code + "]").val();
					if (_val == undefined || _val == "") {
						var _text = jQuery(this).find("span:first").text();
						_tips += _text + " 不能为空\n";
					}
				});
				if(_tips != null && _tips !=""){
					$.toast(_tips);
					return;
				}
				self.moduleSendServer();
			});
			Form.bindFileDel();

			// iphone只能用原生的方式来绑定事件
			if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
				var btnCapture = $('.capture_btn')[0];
				if (btnCapture) {
					btnCapture.onclick = function () {
						captureFieldName = jQuery(btnCapture).attr("captureFieldName");
						// 置图像宏控件是否只允许拍照
						if (jQuery(btnCapture).attr("isOnlyCamera")) {
							setIsOnlyCamera(jQuery(btnCapture).attr("isOnlyCamera"));
						}
						else {
							// 恢复默认设置
							resetIsOnlyCamera();
						}
						// 如果只允许拍照
						if (appProp.isOnlyCamera == "true") {
							jQuery("#captureFile").attr('capture', 'camera');
						}
						var cap = jQuery("#captureFile").get(0);
						cap.click();
						// 会出错，因为页面中可能含有多个captureFile
						// document.getElementById('captureFile').click();
					}
				}
			}
			else {
				mui(".mui-input-group").on("tap", ".capture_btn", function () {
					captureFieldName = jQuery(this).attr("captureFieldName");
					// 置图像宏控件是否只允许拍照
					if (jQuery(this).attr("isOnlyCamera")) {
						setIsOnlyCamera(jQuery(this).attr("isOnlyCamera"));
					}
					else {
						// 恢复默认设置
						resetIsOnlyCamera();
					}

					var cap = jQuery("#captureFile").get(0);
					cap.click();
				});
			}
			
			mui('body').on("tap", ".attFile", function(){
				var url = jQuery(this).attr("link");
				var ext = jQuery(this).attr("ext");
				if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
					var w=0, h=0;
					if (this.tagName=="IMG") { // 图像宏控件
						w = jQuery(this).width();
						h = jQuery(this).height();
					}
					self.showImg(url, w, h);
				} else {
					if (mui.os.plus) {
						var btnArray = ['是', '否'];
						mui.confirm('您确定要下载么？', '', btnArray, function(e) {
							if (e.index == 0) {
								var rootPath = self.getContextPath();
								// 链接为../../public/android/flow/getFile，故需转换，否则会报400错误
								var p = url.indexOf("/public/");
								if (p!=-1) {
									url = url.substring(p);
								}
								url = rootPath + url;
								var dtask = plus.downloader.createDownload(url, {}, function (d, status) {
									if (status == 200) {
										// 调用第三方应用打开文件
										plus.runtime.openFile(d.filename, {}, function (e) {
											alert('打开失败');
										});
									} else {
										alert("下载失败: " + status);
									}
								});
								dtask.start();
							}
						});
					}
					else {
						mui.openWindow({
							"url": url
						})
					}
				}
			});	
		},
		moduleSendServer:function(){
			var self = this;
			var skey = self.options.skey;
			// console.info(self.options);
			var moduleCode = self.options.moduleCode;
			var _url = "../visual/module_list.jsp?skey="+skey+"&moduleCode="+moduleCode;
			if("formCodeRelated" in self.options){
				if(self.options.formCodeRelated != ""){
					var _parentId = self.options.parentId;
					_url = "../visual/module_detail.jsp?skey="+skey+"&moduleCode="+moduleCode+"&id="+_parentId;
					moduleCode = self.options.formCodeRelated;
				}	
			}

			var isAdd = true;
			var id = self.options.id;
			var ajax_url = AJAX_REQUEST_URL.MODULE_ADD_DO + "?moduleCode=" + moduleCode + "&skey=" + skey + "&id=" + id;
			if (id != 0 && "add" != self.options.pageType) {
				isAdd = false;
				ajax_url = AJAX_REQUEST_URL.MODULE_EDIT_DO + "?moduleCode=" + moduleCode + "&skey=" + skey + "&id=" + id;
			}
			var formData = new FormData($('#module_form')[0]);
			for (i=0;i<blob_arr.length ;i++ ) {
				var _blobObj = blob_arr[i];
				var field = "upload";
				if (_blobObj.field) {
					field = _blobObj.field; // 图像宏控件的字段
				}
				formData.append(field, _blobObj.blob,_blobObj.fname);
			}
			jQuery.ajax(ajax_url,{
					dataType:'json',// 服务器返回json格式数据
					type:'post',// HTTP请求类型
					data: formData,
					processData: false,
					contentType: false,
					beforeSend: function(XMLHttpRequest){
						jQuery.myloading();
					},
					complete: function(XMLHttpRequest, status){
						jQuery.myloading("hide");
					},
					success:function(data){
						var res = data.res;
						var msg = data.msg;
						$.toast(msg);
						if(res == "0") {
							// 当运行到pullRefresh_native方法中var id = self.getAttribute('data-pullrefresh-plus-' + attrWebviewId)时
							// 以url作为webviewId时，会报DomException，属性非法的错误
							/*mui.openWindow({
							    "url": _url
							});	*/

							// 如果是添加操作，则返回列表页，且刷新，如果是编辑，则不作处理
							if (isAdd) {
								if (self.options.isUniWebview) {
									wx.miniProgram.getEnv(function(res) {
										console.log("当前环境：" + JSON.stringify(res));
									});
									wx.miniProgram.postMessage({
										data: {
											res: 0,
											url: ""
										},
									});
									uni.navigateBack();
								}
								else {
									//获得父页面的webview
									var listView = plus.webview.currentWebview().opener();
									// 触发父页面的自定义事件(refreshList),从而进行刷新
									mui.fire(listView, 'refreshList');
									mui.back();
								}
							}
						}
					},
					error:function(xhr,type,errorThrown){
						console.log(type);
					}
				});
		},
		moduleDetail:function(){
			var self = this;
			var content = self.element;
			var url = AJAX_REQUEST_URL.MODULE_SHOW + "?isTab=" + self.options.isTab;
			var skey = self.options.skey;
			var id = self.options.id;
			var moduleCode = self.options.moduleCode;
			var datas = {"skey":skey,"id":id,"moduleCode":moduleCode};
			$.post(url,datas,function(data){
				 var res = data.res;
				 console.log('moduleDetail data', data);
				 if(res == "0"){
					 var _formRelated = data.formRelated;
					 $.each(_formRelated,function(index,item){
						 var _formCodeRelated = item.formCodeRelated;
						 var _name = item.name;
						 var _tabTitle = '<a class="mui-control-item relate-form-item"  formCodeRelated ="'+_formCodeRelated+'" href="#item_'+_formCodeRelated+'">'+_name+'</a>';
						 var _tabContent = '<div id="item_'+_formCodeRelated+'" class="mui-slider-item mui-control-content">';
					
						 _tabContent += '<div class="mui-scroll-wrapper">';
						 _tabContent += '<div class="mui-content-padded"></div>';	 
						 _tabContent += '<div class="mui-scroll" id="pullrefresh_'+_formCodeRelated+'">';
						 _tabContent += '<form class="search_form mui-input-group" id="search_'+_formCodeRelated+'" >';
						 _tabContent += '</form>';
						 _tabContent += '<ul class="mui-table-view mui-table-view-chevron" id="ul_'+_formCodeRelated+'">';
						 _tabContent += '</ul>';
						 _tabContent += '</div>';
						 _tabContent += '</div>';
						 _tabContent += '</div>'; 
						 jQuery("#tabTitle").append(_tabTitle);
						 jQuery("#tabContent").append(_tabContent);			 
					 });
					 
					 var _subTags = data.subTags;
					 $.each(_subTags,function(index,item){
						 var _tagName = item.tagName;
						 var subTagIndex = item.subTagIndex;
						 var _tabTitle = '<a class="mui-control-item relate-form-item" subTagIndex="' + subTagIndex + '" isSubTag="true" tagName="'+_tagName+'" href="#item_'+subTagIndex+'">'+_tagName+'</a>';
						 var _tabContent = '<div id="item_'+subTagIndex+'" class="mui-slider-item mui-control-content">';
					
						 _tabContent += '<div class="mui-scroll-wrapper">';
						 _tabContent += '<div class="mui-content-padded"></div>';	 
						 _tabContent += '<div class="mui-scroll" id="pullrefresh_'+subTagIndex+'">';
						 _tabContent += '<form class="search_form mui-input-group" id="search_'+subTagIndex+'" >';
						 _tabContent += '</form>';
						 _tabContent += '<ul class="mui-table-view mui-table-view-chevron" id="ul_'+subTagIndex+'">';
						 _tabContent += '</ul>';
						 _tabContent += '</div>';
						 _tabContent += '</div>';
						 _tabContent += '</div>'; 
						 jQuery("#tabTitle").append(_tabTitle);
						 jQuery("#tabContent").append(_tabContent);			 
					 });

					 // 使新增的mui-scroll-wrapper可以滚动
					 mui('.mui-scroll-wrapper').scroll({
						 bounce: true, // 是否启用回弹
						 indicators: true, //是否显示滚动条
						 deceleration: 0.0006 //阻尼系数,系数越小滑动越灵敏，默认0.0006
					 });
					 
					 var _fields = data.fields;
					 if(_fields.length>0){
					 	Form.flowInitDetailForm(_fields); // 加载流程表单
					 }
					 var _files = data.files;
					 
					 if(_files.length>0){
						 var _ul = Form.flowInitFiles(_files);
						 jQuery("#formDetailScroll").append(_ul);
						 Form.bindFileDel();// 加载事件
					 }

					 if (data.viewJs) {
						 var s0 = document.createElement('script');
						 s0.text = data.viewJs;
						 document.body.appendChild(s0);
					 }
					 						
					try {
						initPhotoSwipe();
					}
					catch (e) {}						 
				 }
			},"json");

			mui('body').on("tap", ".attFile", function () {
				var url = jQuery(this).attr("link");
				var ext = jQuery(this).attr("ext");
				console.log('ext', ext);
				if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
					var w=0, h=0;
					if (this.tagName=="IMG") { // 图像宏控件
						w = jQuery(this).width();
						h = jQuery(this).height();
					}
					console.log('url', url);
					self.showImg(url, w, h);
				} else {
					if (mui.os.plus) {
						var btnArray = ['是', '否'];
						mui.confirm('您确定要下载么？', '', btnArray, function(e) {
							if (e.index == 0) {
								var rootPath = self.getContextPath();
								// 链接为../../public/android/flow/getFile，故需转换，否则会报400错误
								var p = url.indexOf("/public/");
								if (p!=-1) {
									url = url.substring(p);
								}
								url = rootPath + url;
								var dtask = plus.downloader.createDownload(url, {}, function (d, status) {
									if (status == 200) {
										// 调用第三方应用打开文件
										plus.runtime.openFile(d.filename, {}, function (e) {
											alert('打开失败');
										});
									} else {
										alert("下载失败: " + status);
									}
								});
								dtask.start();
							}
						});
					}
					else {
						mui.openWindow({
							"url": url
						})
					}
				}
			});		
	
			$('#tabTitle').on('tap', '.relate-form-item', function(event) {
				var ele = this;
				var _formCodeRelated = this.getAttribute("formCodeRelated");
				if (_formCodeRelated!=null) {
					var url = "../../public/android/module/listRelate";
					if(mui.os.plus && mui.os.ios) {
						url = getContextPath() + "/public/android/module/listRelate";
					}

					var options = {
							"pullRefreshContainer":"#pullrefresh_"+_formCodeRelated,
							"ulContainer":"#ul_"+_formCodeRelated,
							"searchContainer":"#search_"+_formCodeRelated,
							"ajax_params":{"skey":self.options.skey,"moduleCode":self.options.moduleCode,"formCodeRelated":_formCodeRelated,"parentId":self.options.id},
							"url": url
							};
					var PullToRefrshListApi = new $.PullToRefrshList(self.element,options);
					PullToRefrshListApi.loadList();
				}
				var tagName = this.getAttribute("tagName");
				if (tagName!=null) {
					var subTagIndex = this.getAttribute("subTagIndex");
					var options = {
							"pullRefreshContainer":"#pullrefresh_"+subTagIndex,
							"ulContainer":"#ul_"+subTagIndex,
							"searchContainer":"#search_"+subTagIndex,
							"ajax_params":{"skey":self.options.skey,"moduleCode":self.options.moduleCode,"subTagIndex":subTagIndex, "mode":"subTagRelated", "parentId":self.options.id},
							"url": url
							};
					if(mui.os.plus) {
						mui.plusReady(function() {
							var PullToRefrshListApi = new $.PullToRefrshList(self.element, options);
							PullToRefrshListApi.loadList();
						});
					}
					else {
						var PullToRefrshListApi = new $.PullToRefrshList(self.element,options);
						PullToRefrshListApi.loadList();
					}
				}				
			});
		},
		getContextPath: function () {
			var strFullPath = window.location.href;
			var strPath = window.location.pathname;
			var pos = strFullPath.indexOf(strPath);
			var prePath = strFullPath.substring(0, pos);
			var postPath = strPath.substring(0, strPath.substr(1).indexOf('/') + 1);
			// 有的服务器上会在路径中带上weixin，如contextPath为：http://****.com/weixin
			var contextPath = prePath + postPath;
			var	p = contextPath.indexOf("/weixin");
			if (p!=-1) {
				contextPath = contextPath.substring(0, p);
			}
			return contextPath;
		}
	})
})(mui,document,window)
