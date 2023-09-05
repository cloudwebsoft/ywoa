console.log('macro_uploader_ctl.js is loading1');

var paramsUploader = getJsParams('macro_uploader_ctl');
console.log(paramsUploader);
var pageTypeUploader = paramsUploader.pageType;
var isEditableUploader = paramsUploader.isEditable;
var fieldNameUploader = paramsUploader.fieldName;
var flowIdUploader = paramsUploader.flowId;
var fdaoIdUploader = paramsUploader.id;
var formCodeUploader = paramsUploader.formCode;
var moduleCodeUploader = paramsUploader.moduleCode;

function getDropFiles(fieldName) {
	var dropFiles = null;
	try {
		dropFiles = eval('dropFiles' + fieldName);
		console.log('getDropFiles', dropFiles);
	} catch (e) {
		console.log(e);
	}
	if (dropFiles == null) {
		dropFiles = [];
	}
	return dropFiles;
}

async function initUploader(id, moduleCode, formCode, objName, pageType, editable, isTreeView, treeNodeCode) {
  console.log('initUploader id=' + id + ',' + formCode + ',' + objName);
  var str = '<div id="' + objName + '_box" class="img-preview-box">';
	if (pageType.toLowerCase().indexOf('show')==-1) {
		str += '<div class="upfile-box">';
		str += '    <input type="file" class="upfile-ctrl" id="' + objName + '" name="' + objName + '" multiple="" ';
		str += '      accept="image/png, image/jpeg, image/gif, image/jpg, application/pdf"/>';
		str += '    <div class="upfile-box-tip">';
		str += '        <span>+</span>';
		str += '        <p>点击或拖拽到“+”<br/>上传图片</p>';
		str += '    </div>';
		str += '</div>';
	}
  str += '</div>';
  $(findObjByFormCode(formCode, objName + '_wrapper')).append(str);

	if (id!=-1) {
		if (pageType.indexOf('flow') != -1) {
			var paramsUploader = {"flowId": id, "fieldName": objName, "pageType": pageType};
			var data = await ajaxPost('/flow/listAttachmentByField', paramsUploader);
			console.log(data);
			listUploaderImg(formCode, objName, data.data, pageType, editable);
		} else {
			// 树形视图 isTreeView
			var paramsUploader = {"id": id, "moduleCode":moduleCode, "fieldName": objName, "pageType": pageType,"isTreeView":isTreeView, "treeNodeCode":treeNodeCode};
			var data = await ajaxPost('/visual/listAttByField', paramsUploader);
			console.log(data);
			listUploaderImg(formCode, objName, data.data, pageType, editable);
		}
	}

	if (editable && pageType.toLowerCase().indexOf('show')==-1) {
		// 拖动排序
		Sortable.create($("#" + objName + "_box")[0], {
		// Sortable.create($(".img-preview-box")[0], {
			animation: 150, // 动画参数
			onAdd: function (evt) {   //拖拽时候添加有新的节点的时候发生该事件
					// console.log('onAdd:', [evt.item, evt.from]);
			},
			onUpdate: function (evt) {  //拖拽更新节点位置发生该事件
					// console.log('onUpdate:', [evt.item, evt.from]);
			},
			onRemove: function (evt) {   //删除拖拽节点的时候触发该事件
					// console.log('onRemove:', [evt.item, evt.from]);
			},
			onStart: function (evt) {  //开始拖拽出发该函数
					// console.log('onStart:', [evt.item, evt.from]);
			},
			onSort: function (evt) {  //发生排序发生该事件
					// console.log('onSort:', [evt.item, evt.from]);
			},
			onEnd: function (evt) { //拖拽完毕之后发生该事件
					// console.log('onEnd:', [evt.item, evt.from]);
			}
		});
	}

	bindUploaderEvent(id, moduleCode, pageType, isTreeView, treeNodeCode);
}

// initUploader(fieldNameUploader);

function isImage(diskName) {
	var isImg = false;
	var p = diskName.indexOf('.');
	var ext = '';
	if (p!=-1) {
		ext = diskName.substring(p + 1).toLowerCase();
	}
	if (ext == 'png' || ext=='gif' || ext == 'jpg' || ext=='jpeg' || ext=='bmp') {
		isImg = true;
	}
	return isImg;
}

function listUploaderImg(formCode, objName, ary, pageType, editable) {
	if (ary == null) {
		return;
	}
	var str = '';
	for (var i = 0; i < ary.length; i++) {
		var json = ary[i];
		var id = json.id;
		var fileName = json.name;
		// var visualPath = json.visualPath;
		var diskName = json.diskName;
		// var imgPath = getPublicPath() + '/' + visualPath + '/' + diskName;
		var previewUrl = json.previewUrl;
		
		var isImg = isImage(diskName);

		str += '<div class="upfile-image-box upload-image-box-db" data-id="' + id + '">';
		if (isImg) {
			str += '<img class="upfile-image" id="img_' + diskName + '" src="' + previewUrl + '"/>';
		} else {
			str += '<div class="upfile-image upfile-file" id="img_' + diskName + '" url="' + previewUrl + '">' + fileName + '</div>';
		}
		if (editable && pageType.toLowerCase().indexOf('show')==-1) {
			str += '	<div class="upfile-cover">';
			str += '			<div class="btn-bar">';
			str += '					<span class="btn-del" title="删除">';
			str += '							<i class="fa fa-trash"></i>';
			str += '					</span>';
			str += '			</div>';
			str += '	</div>';
		}
		str += '</div>';
	}
	console.log('listUploaderImg formCode', formCode, 'objName', objName, 'str', str);
	var $obj = $("form[formcode='" + formCode + "']").find('[id=' + objName + '_box]');
	console.log('$obj', $obj[0]);
	$obj.append(str);
}

function showImgList(imgPreviewBox, fileList) {
  // 添加时间戳
  var dateTime = new Date().getTime();
  for (var i = 0; i < fileList.length; i++) {
		if (fileList[i]) {
				var file = fileList[i];
				var isImg = isImage(file.name);

				var picHtml = "<div class='upfile-image-box' >";
				if (isImg) {
					picHtml += "<img class='upfile-image' id='img_" + dateTime + "_" + fileList[i].name + "'/>";
				} else {
					picHtml += "<div class='upfile-image upfile-file' id='img_" + dateTime + "_" + fileList[i].name + "'>" +  file.name + "</div>";
				}
				picHtml += "<div class='upfile-cover'><div class='btn-bar'><span class='btn-del'>×</span></div></div></div>";

				// $(".img-preview-box").append(picHtml);
				imgPreviewBox.append(picHtml);
		
				var imgObjPreview = document.getElementById("img_" + dateTime + "_" + fileList[i].name);
				imgObjPreview.style.display = 'block';
				if (isImg) {
					imgObjPreview.src = window.URL.createObjectURL(fileList[i]);
				}
				var $img = $(imgObjPreview);
				$img.attr('img_type', file.type);
				$img.attr('img_size', file.size);
				$img.attr('img_name', file.name);
				$img.attr('img_lastModified', file.lastModified);
		}
  }
}

function bindUploaderEvent(pageId, moduleCode, pageType, isTreeView, treeNodeCode) {
	$('.upfile-box').on('dragover', '.upfile-ctrl', function (e) {
		e.stopPropagation();
		// 阻止浏览器默认打开文件的操作
		e.preventDefault();
	
		window.event.dataTransfer.dropEffect = 'copy';
	});
	
	$('.upfile-box').on("drop", '.upfile-ctrl', function (e) {
		e.stopPropagation();
		// 阻止浏览器默认打开文件的操作
		e.preventDefault();
	
		var fileList = window.event.dataTransfer.files;
	
		var fieldName = $(this).attr('name');
		var dropFiles = getDropFiles(fieldName);
		for (var i = 0; i < fileList.length; i++) {
				// formData.append("titleImage" + i, fileList[i]);
				var json = {"name": fileList[i].name, "file": fileList[i], "lastModified": fileList[i].lastModified, "size" : fileList[i].size, "type" : fileList[i].type};
				dropFiles.push(json);
		}
	
		var imgPreviewBox = $(this).parent().parent();
		showImgList(imgPreviewBox, fileList);
	});
	
	$('.upfile-box').on("change", '.upfile-ctrl', function (e) {
	// $(".upfile-ctrl").change(function(e) {
		var fieldName = $(this).attr('name');
		console.log('change fieldName', fieldName);
		var dropFiles = getDropFiles(fieldName);
		var fileList = $(this)[0].files;
		for (var i = 0; i < fileList.length; i++) {
				var json = {"name": fileList[i].name, "file": fileList[i], "lastModified": fileList[i].lastModified, "size" : fileList[i].size, "type" : fileList[i].type};
				dropFiles.push(json);
		}
		var imgPreviewBox = $(this).parent().parent();
		showImgList(imgPreviewBox, fileList);
	});
	
	// 删除
	$(".img-preview-box").on("click", ".btn-del", function () {
		var id = $(this).parents(".upfile-image-box").data('id');
		if (id != null) {
				// 删除已存文件
				var self = this;
				myConfirm('提示', '您确定要删除么', function() {
					doUploaderDelAttach(pageId, id, self, moduleCode, pageType, isTreeView, treeNodeCode);
				});
		}	else {
				// 删除新增文件
				var $img = $(this).parents(".upfile-image-box").find('.upfile-image');

				var fieldName = $(this).parents('.img-preview-box').find('.upfile-ctrl').attr('name');
				dropFiles = getDropFiles(fieldName);
				console.log('on del', dropFiles);
	
				var ary = [];
				for (i = 0; i < dropFiles.length; i++) {
						var f = dropFiles[i];
						if (!(f.name == $img.attr('img_name') && f.type == $img.attr('img_type') && (''+f.size) == $img.attr('img_size') && (''+f.lastModified) == $img.attr('img_lastmodified'))) {
								ary.push(f);
						}
				}
				dropFiles = ary;
				console.log(dropFiles);

				$(this).parents(".upfile-image-box").remove();
		}
	});
	
	$('.img-preview-box').on("click", ".upfile-image", function () {
		console.log('upfile-image onclick', $(this)[0]);
		if ($(this).attr('src') && $(this).attr('src').startsWith('http')) {
			window.open($(this).attr('src'));
		} else {
			if ($(this).attr('url') && $(this).attr('url') != '') {
				window.open($(this).attr('url'));
			}
		}
	});	
}

function doUploaderDelAttach(id, attachId, obj, moduleCode, pageType, isTreeView, treeNodeCode) {
	if (pageType.indexOf('flow') != -1) {
		var paramsUploader = {"flowId": id, "attId": attachId};
		ajaxPost('/flow/delAtt', paramsUploader).then((data) => {
			if (data.code === 200) {
				$(obj).parents(".upfile-image-box").remove();
				try {
					reloadAttachment();
				} catch (e) {}
			} else {
				myMsg(data.msg, 'error');
			}
		})
	} else {
		var paramsUploader = {"id": id, "moduleCode": moduleCode, "attachId": attachId, "isTreeView": isTreeView, "treeNodeCode": treeNodeCode};
		ajaxPost('/visual/delAttach', paramsUploader).then((data) => {
			if (data.code === 200) {
				$(obj).parents(".upfile-image-box").remove();
				try {
					reloadAttachment();
				} catch (e) {}
			} else {
				myMsg(data.msg, 'error');
			}
		})
	}
}

function getUploaderImgOrders(formCode) {
	// 取得顺序号
	var imgOrders = "";
	$("form[formcode='" + formCode + "']").find('.img-preview-box .upfile-image').each(function() {
	// $('.img-preview-box .upfile-image').each(function() {
			var imgName = $(this).attr('img_name');
			if (imgName==null) {
					imgName = $(this).parent().data('id');
			}
			if (""==imgOrders) {
					imgOrders = imgName;
			}
			else {
					imgOrders += "," + imgName;
			}
	});
	return imgOrders;
}