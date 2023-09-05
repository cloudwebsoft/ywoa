
function deAttachment(attId, fieldName, flowId, docId, formName, isObjStoreEnabled, pageType) {
  // 如果是智能模块的添加页面
  if (pageType == 'add') {
    if (fieldName != null) {
      $(fo('helper_' + fieldName)).html('');
      $(fo('helper_' + fieldName)).hide();
    }

    if (isObjStoreEnabled) {
      $('#' + formName + '_' + fieldName).show();
      initAttachmentCtl(formName, fieldName);
      // 清除悬浮栏中的节点
      deleteByField(fieldName);
    }

    return;
  }

  var ajaxData;
  if (pageType == 'flow') {
    ajaxData = {
      "flowId": flowId,
      "docId": docId,
      "attachId": attId
    }
  } else {
    ajaxData = {
      "attachId": attId
    }
  }

	ajaxPost('/flow/delAttach', ajaxData).then((data) => {
		console.log('data', data);
		myMsg(data.msg);
		if (data.code == 200) {
			if (fieldName != null) {
				$(fo('helper_' + fieldName)).html('');
        $(fo('helper_' + fieldName)).hide();

        // 先映射，后上传，然后再删除时，需置映射的值为空，否则后端仍会保存映射的值，并且验证必填也因为有该值而不生效
        $(fo(fieldName + '_mapped')).val('');
			}
			// 刷新附件列表
			reloadAttachment();

      if (isObjStoreEnabled) {
        $('#' + formName + '_' + fieldName).show();
        fo(fieldName).value = '';

        initAttachmentCtl(formName, fieldName);
        // 清除悬浮栏中的节点
        deleteByField(fieldName);
      }
		}
	});
}

// 当文件上传后即隐藏上传文件链接，防止网卡时，用户仍可以点击上传，导致前后上传的文件名交替闪现
function hideUploadLink(formName, fieldName) {
  $('#' + formName + '_' + fieldName).hide();
  console.log(fieldName + ' upload link is hided');
}

function delAtt(attId, fieldName, flowId, docId, formName, isObjStoreEnabled, pageType) {
	myConfirm('提示', '您确定要删除么', function() { deAttachment(attId, fieldName, flowId, docId, formName, isObjStoreEnabled, pageType) });
}

// 置上传进度
function putUploadProgress(data) {
  console.log('putUploadProgress', data);
  console.log("$('.barbox').width", $('.barline').width());
  var formName = data.formName;
  var objForm = o(formName);
  var progress = data.progress;
  var fileName = data.fileName;
  var fieldTitle = data.fieldTitle;

  // 隐藏上传文件链接
  $('#' + formName + '_' + data.fieldName).hide();

  $(objForm).find('#barlineBar_' + formName + '_' + data.fieldName).width(data.progress*($('.barline').width()/100) + 'px');
  $(objForm).find('#barPercent_' + data.fieldName).html(data.progress + '%');

  var fileNameHtml = data.fileName;
  if (progress > 0 || progress != undefined) {
    // 上传没结束时不允许出现删除按钮，因为浏览器还会继续上传
    // fileNameHtml += '<span class="barline-remove" onclick="removeUploadFile(&quot;' + data.formName + '&quot;, &quot;' + data.fieldName + '&quot;)">×</span>';
  }
  $(objForm).find('#barFileName_' + formName + '_' + data.fieldName).html(fileNameHtml);
  if (data.progress == 100) {
    var diskName = data.fieldValue;
    var p = diskName.lastIndexOf('/');
    // 去掉fileName中与filePath重复的部分
    diskName = diskName.substring(p + 1);
    var pageType = data.pageType;

    var params = {
      fieldName: data.fieldName,
      formCode: data.formCode,
      pageType: pageType,
      mainId: data.mainId,
      fileName: fileName,
      fileSize: data.file.size,
      diskName: diskName,
      filePath: data.filePath,
    }
    ajaxPost('/flow/macro/finishUpload', params).then((res) => {
        console.log('res', res);
        if (res.code==200) {
            myMsg(fieldTitle + ": " + fileName + ' 上传成功');

            // 如果是智能模块的添加页面
            if (pageType == 'add' || pageType == 'add_relate') {
              // 如果fieldName不为att开头，则说明是文件宏控件所传，在隐藏表单域中写入diskName
              console.log('diskName', diskName, 'formName', data.formName, 'fieldName', data.fieldName);
              if (data.fieldName.indexOf('att') != 0) {
                fo(data.fieldName, data.formName).value = diskName;
              } else {
                // 通过上传文件按钮上传，则加入隐藏表单域
                $(o(formName)).append('<input name="att" value="' + diskName + '" type="hidden" />');
              }
            }

            params.isEditable = true;
            params.canDel = true;
            params.attId = res.data.id;
            params.attName = data.fileName;
            params.visitKey = res.data.visitKey;
            params.formName = data.formName;

            params.canPreview = res.data.canPreview;
            params.previewUrl = res.data.previewUrl;
            params.visualPath = params.filePath;

            console.log('getServerInfo()', getServerInfo());
            params.isObjStoreEnabled = getServerInfo().isObjStoreEnabled;

            // 置fileTreeList信息，以便于“上传按钮”显示预览按钮
            setUploadFileTreeListAttInfo(data.file, params.attId, params.canPreview, params.previewUrl, params.visitKey);

            // 刷新附件列表
            console.log('putUploadProgress pageType', pageType);
            if (pageType === 'flow') {
              reloadAttachment();
            }
            else if (pageType === 'edit' || data.pageType === 'edit_relate') {
              // else if (pageType === 'edit' && data.fieldName != 'att') {
              reloadAttachment();
            }

            if (data.fieldName.indexOf('att') != 0) {
              displayAttachment(params);
            }
        } else {
            myMsg(res.msg, 'error');
        }
    });
  }
}

// 清除
function removeUploadFile(formName, fieldName) {
  $('#barBox_' + formName + '_' + fieldName).remove();
  initAttachmentCtl(formName, fieldName);
}

function initAttachmentCtl(formName, fieldName) {
  if (!o(formName))
    return;

  $('#barBox_' + formName + '_' + fieldName).remove();

  let str = '';
  str += "<div id=\"barBox_" + formName + '_' + fieldName + "\" class=\"barbox\">";
  str += "<div id=\"barFileName_" + formName + '_' + fieldName + "\" class=\"barline-filename\"></div>";
  str += "<div class=\"barline\" id=\"proBar_" + formName + '_' + fieldName + "\">";
  str += "	<div id=\"barlineBar_" + formName + '_' + fieldName + "\" class=\"barline-bar\" style=\"width:0px;\"></div>";
  str += "</div>";
  str += "<div id=\"barPercent_" + fieldName + "\" class=\"barline-percent\"></div>";
  str += "</div>";
  $('#' + formName + '_' + fieldName).after(str);
  return str;
}

// 显示已上传的文件及预览
function displayAttachment(json) {
  console.log('displayAttachment json', json);
  var isObjStoreEnabled = json.isObjStoreEnabled;
  var formName = json.formName;
  var fieldName = json.fieldName;
  var attId = json.attId;
  var attName = json.attName;
  var isEditable = json.isEditable;
  var visualPath = json.visualPath;
  var diskName = json.diskName;
  var pageType = json.pageType;
  var canDel = json.canDel;
  var visitKey = json.visitKey;
  var rootPath = json.rootPath;
  var mainId = json.mainId; // flowId或模块记录的id
  var isFlow = json.isFlow; // 是否流程
  var canPreview = json.canPreview;
  var previewUrl = json.previewUrl;

  var isImg = isImage(diskName);

  console.log('displayAttachment isObjStoreEnabled', isObjStoreEnabled);
  var str = '';
  if (pageType.toLowerCase().indexOf("show") == -1) {
    if (isObjStoreEnabled) {
      str += "<a href='javascript:;' onclick=\"downloadObsFile('" + visualPath + '/' + diskName + "')\">" + attName + "</a>";
    } else {
      if (isFlow) {
        str += "<a href='javascript:;' onclick=\"downloadFile('" + attName + "', {attachId:" + attId + ", flowId:" + mainId + "})\">" + attName + "</a>";
      }
      else {
        str += "<a href='javascript:;' onclick=\"downloadFileVisual('" + attName + "', {attachId:" + attId + ", visitKey:'" + visitKey + "'})\">" + attName + "</a>";
      }
    }

    if (isImg) {
      str += "&nbsp;&nbsp;<a href='javascript:;' onclick=\"window.open('" + rootPath +"/showImg.do?visitKey=" + visitKey + "&path="+ visualPath + "/" + diskName + "')\">查看</a>";
    }

    if (canPreview) {
      str += "&nbsp;&nbsp;<a href='javascript:;' onclick=\"window.open('" + previewUrl + "')\">预览</a>"
    }

    // 如果是可编辑状态
    if (isEditable) {
      // 如果是流程处理界面，而只有一个元素，说明是映射过来的，不能删除
      if (canDel) {
          str += "&nbsp;&nbsp;<a href='javascript:;' class='btn-att-remove' title='删除' onclick=\"delAtt(" + attId + ", '" + fieldName + "', " + mainId + ", null, '" + formName + "', " + isObjStoreEnabled + ", '" + pageType + "')\">×</a>";
      } else {
        setTimeout(()=>{
          console.log('displayAttachment now show ' + formName + '_' + fieldName);
          // 如果是映射过来的，则使“上传文件”链接出现，以便于能够替换原来映射的文件
          $('#' + formName + '_' + fieldName).show();
        }, 2000);
      }

      // 当删除原来已上传的文件，重新再上传时，需置该元素为input hidden的值，否则在提交后，保存的仍是原来的值
      var fieldVal = '';
      if (pageType == 'flow') {
        fieldVal = mainId + ',' + attId;
      } else {
        fieldVal = diskName;
      }
      fo(fieldName).value = fieldVal;
    }
  } else {
    if (isImg) {
        str += "<a href='javascript:;' onclick=\"window.open('" + rootPath +"/showImg.do?visitKey=" + visitKey + "&path="+ visualPath + "/" + diskName + "')\">" + attName + "</a>";
    } else {
      if (isObjStoreEnabled) {
        str += "<a href='javascript:;' onclick=\"downloadObsFile('" + visualPath + '/' + diskName + "')\">" + attName + "</a>";
      } else {
        if (isFlow) {
          str += "<a href='javascript:;' onclick=\"downloadFile('" + attName + "', {attachId:" + attId + ", flowId:" + mainId + "})\">" + attName + "</a>";
        }
        else {
          str += "<a href='javascript:;' onclick=\"downloadFileVisual('" + attName + "', {attachId:" + attId + ", visitKey:'" + visitKey + "'})\">" + attName + "</a>";
        }
      }
    }

    if (canPreview) {
      str += "&nbsp;&nbsp;<a href='javascript:;' onclick=\"window.open('" + previewUrl + "')\">预览</a>"
    }
  }

  console.log('displayAttachment str', str);
  $('#helper_' + fieldName).html(str);
  $('#helper_' + fieldName).show();

  // 隐藏上传文件链接
  $('#' + formName + '_' + fieldName).hide();
  // 隐藏进度条
  $('#barFileName_' + formName + '_' + fieldName).hide();
  // $('#barBox_' + formName + '_' + fieldName).remove();
}