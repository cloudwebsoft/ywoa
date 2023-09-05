var pop;
var captureFieldName = ""; // 在mui.flow.wx.js tap事件中当点击的是capture（照片）按钮时，记录是由哪个field点击的，默认为upload，当用于图片框宏控件时记录字段名称
var blob_arr = []; // 文件数组
var mSql_arr = [];
var FLOW_ID; // 记录SQL宏控件对应的流程
var CODES_NAME = {
    "CALCULATOR_CODES": "calculator_codes",
    "MACRO_SQL_CODES": "macroSqlCodes"
}

function o(s) {
    var e = document.getElementById(s);
    if (e != null)
        return e;
    e = document.getElementsByName(s);
    if (e.length == 0)
        return null;
    else
        return e[0];
}

function initCaptureFile() {
    var captureFile = document.getElementById("captureFile");
    if (captureFile != null) {
        captureFile.onchange = function () {
            if (!this.files.length) return;
            var files = Array.prototype.slice.call(this.files);
            if (files.length > 9) {
                mui.toast("最多同时只可上传9张图片");
                return;
            }

            files.forEach(function (file, i) {
                if (!/\/(?:jpeg|png|gif)/i.test(file.type)) return;
                var _name = file.name;
                var _size = file.size / 1024 > 1024 ? (~~(10 * file.size / 1024 / 1024)) / 10 + "MB" : ~~(file.size / 1024) + "KB";

                // 如果是图像宏控件的操作，则删除原来拍的照片，防止重复上传，当为upload时，是点击了底部的“照片”按钮
                if (captureFieldName != "upload") {
                    $('li[attField=' + captureFieldName + ']').remove();
                }
                var jsonAry = [];
                for (i = 0; i < blob_arr.length; i++) {
                    var _blobObj = blob_arr[i];
                    var field = "upload";
                    if (_blobObj.field) {
                        field = _blobObj.field; // 图像宏控件的域
                    }
                    if (field=="upload") {
                        jsonAry.push(_blobObj);
                    }
                    else {
                        if (field != captureFieldName) {
                            jsonAry.push(_blobObj);
                        }
                    }
                }
                blob_arr = jsonAry;

                // appendCon(_name, _size, captureFieldName);
                imgResize(file, captureFieldName);
            });
        }
    }
}

initCaptureFile();

function appendCon(_name, _size, captureFieldName) {
    var _length = $(".att_li").length;
    if (_length == 0) {
        var _ul = '<ul class="mui-table-view mui-table-view-chevron att_ul">'
        _ul += '<li class="mui-table-view-cell mui-media att_li">附件列表：</li>';
        _ul += '</ul>';
        $(".mui-button-row").before(_ul);
    }
    var _li = '<li class="mui-table-view-cell mui-media att_li" fId="0" attField="' + captureFieldName + '">';
    _li += '<div class="mui-slider-right mui-disabled">';
    _li += '<a class="mui-btn mui-btn-red att_del" >删除</a>';
    _li += '</div>';
    _li += '<div class="mui-slider-handle">';
    _li += '<a href="javascript:;">';
    _li += '<img class="mui-media-object mui-pull-left" src="../images/file/png.png" />'
    _li += '	<div class="mui-media-body">';
    _li += _name + "(" + _size + ")";
    _li += '</div>';
    _li += '</a>';
    _li += '</div>';
    _li += '</li>';
    $(".att_ul").append(_li);
}

function convertBlob(base64) {
    var buffer = new ArrayBuffer(base64.length);
    var ubuffer = new Uint8Array(buffer);
    for (var i = 0; i < base64.length; i++) {
        ubuffer[i] = base64.charCodeAt(i)
    }
    var blob;
    try {
        blob = new Blob([buffer], {type: 'image/jpg'});
    } catch (e) {
        window.BlobBuilder = window.BlobBuilder || window.WebKitBlobBuilder || window.MozBlobBuilder || window.MSBlobBuilder;
        if (e.name === 'TypeError' && window.BlobBuilder) {
            var blobBuilder = new BlobBuilder();
            blobBuilder.append(buffer);
            blob = blobBuilder.getBlob('image/jpg');
        }
    }
    return blob;
}

// maxSize 是压缩的设置，设置图片的最大宽度和最大高度，等比缩放，level是压缩的质量，数值越小质量越低
var maxSize = {
    width: 800,
    height: 800,
    level: 0.8
};

function getPhotoOrientation(img){
    var orient;
    EXIF.getData(img, function () {
        orient = EXIF.getTag(this, 'Orientation');
    });
    return orient;
}

//对图片旋转处理 added by lzk
function rotateImg(img, direction,canvas) {
    //alert(img);
    //最小与最大旋转方向，图片旋转4次后回到原方向
    var min_step = 0;
    var max_step = 3;
    //var img = document.getElementById(pid);
    if (img == null)return;
    //img的高度和宽度不能在img元素隐藏后获取，否则会出错
    var height = img.height;
    var width = img.width;
    //var step = img.getAttribute('step');
    var step = 2;
    if (step == null) {
        step = min_step;
    }
    if (direction == 'right') {
        step++;
        //旋转到原位置，即超过最大值
        step > max_step && (step = min_step);
    } else {
        step--;
        step < min_step && (step = max_step);
    }
    //旋转角度以弧度值为参数
    var degree = step * 90 * Math.PI / 180;
    var ctx = canvas.getContext('2d');
    switch (step) {
        case 0:
            canvas.width = width;
            canvas.height = height;
            ctx.drawImage(img, 0, 0);
            break;
        case 1:
            canvas.width = height;
            canvas.height = width;
            ctx.rotate(degree);
            ctx.drawImage(img, 0, -height);
            break;
        case 2:
            canvas.width = width;
            canvas.height = height;
            ctx.rotate(degree);
            ctx.drawImage(img, -width, -height);
            break;
        case 3:
            canvas.width = height;
            canvas.height = width;
            ctx.rotate(degree);
            ctx.drawImage(img, -width, 0);
            break;
    }
}

function imgResize(file, captureFieldName) {
    if (maxSize.width == 0)
        return;
    var fileReader = new FileReader();
    fileReader.onload = function () {
        console.log('imgResize onload');
        var base64Str = this.result;
        if (/\/(?:jpeg|png|gif|jpg)/i.test(file.type)) {
            var IMG = new Image();
            IMG.src = base64Str;
            IMG.onload = function () {
                var w = this.naturalWidth, h = this.naturalHeight, resizeW = 0, resizeH = 0;
                if (w > maxSize.width || h > maxSize.height) {
                    var multiple = Math.max(w / maxSize.width, h / maxSize.height);
                    resizeW = w / multiple;
                    resizeH = h / multiple;
                    var canvas = document.createElement('canvas'),
                        ctx = canvas.getContext('2d');
                    if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
                        console.log('imgResize ios');
                        canvas.width = resizeW;
                        canvas.height = resizeH;

                        var orient = getPhotoOrientation(IMG);
                        switch(orient){
                            case 6://需要顺时针（向左）90度旋转
                                rotateImg(IMG,'left',canvas);
                                break;
                            case 8://需要逆时针（向右）90度旋转
                                rotateImg(IMG,'right',canvas);
                                break;
                            case 3://需要180度旋转
                                rotateImg(IMG,'right',canvas);//转两次
                                rotateImg(IMG,'right',canvas);
                                break;
                            default:
                                ctx.drawImage(IMG, 0, 0, resizeW, resizeH);
                        }
                    } else {
                        console.log('imgResize android');
                        canvas.width = resizeW;
                        canvas.height = resizeH;
                        ctx.drawImage(IMG, 0, 0, resizeW, resizeH);
                    }
                    base64Str = canvas.toDataURL('image/jpeg', maxSize.level);
                }

                var imgBox = '<li class="img-box" fId="0" field="' + captureFieldName + '"><img class="img-box-img" src="' + base64Str + '"/>';
                imgBox += '<span class="btn-del-img"><img class="att_del" isImgBox="true" src="' + getContextPath() + '/images/btn_del.png"></span></li>';
                $(".img-area").append(imgBox);

                var blob = convertBlob(window.atob(base64Str.split(',')[1]));
                var blobObj = {"fname": file.name, "blob": blob, "field": captureFieldName};
                blob_arr.push(blobObj);
                // console.info(blob_arr);
            }
        } else {
            console.log('imgResize captureFieldName', captureFieldName);
            var imgBox = '<li class="img-box" fId="0" field="' + captureFieldName + '" style="padding:5px">' + file.name;
            imgBox += '<span class="btn-del-img"><img class="att_del" isImgBox="true" src="' + getContextPath() + '/images/btn_del.png"></span></li>';
            $(".img-area").append(imgBox);

            var blob = convertBlob(window.atob(base64Str.split(',')[1]));
            var blobObj = {"fname": file.name, "blob": blob, "field": captureFieldName};
            blob_arr.push(blobObj);
        }
    };
    fileReader.readAsDataURL(file);
}

function getContextPath() {
    var strFullPath = document.location.href;
    var strPath = document.location.pathname;
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

function getBlob(buffer, format) {
    try {
        return new Blob(buffer, {type: format});
    } catch (e) {
        var bb = new (window.BlobBuilder || window.WebKitBlobBuilder || window.MSBlobBuilder);
        buffer.forEach(function (buf) {
            bb.append(buf);
        });
        return bb.getBlob(format);
    }
}

function getPFieldVal(fieldName) {
    // 先从当前表单中取，如果取不到则从父表单中取
    // console.log("$('#'+fieldName)[0]=" + fieldName + ":" + $('#'+fieldName)[0]);
    if ($('#' + fieldName)[0]) {
        return $('#' + fieldName).val();
    } else {
        var dlg = window.opener ? window.opener : window.parent;
        if (dlg) {
            if (dlg.document.getElementById(fieldName)) {
                // console.log("getPFieldVal " + fieldName + "=" + dlg.document.getElementById(fieldName).value);
                return dlg.document.getElementById(fieldName).value;
            } else {
                console.log(fieldName + " is not found!");
                return "";
            }
        } else {
            return "";
        }
    }
}

function macroCurrentUserInit(flowId,skey,arr,formCode) {
	// console.log(arr);
	$.each(arr, function(index,item) {
		var _code = item.code;
		var _metaData = item.metaData;
		// console.log("meta=" + _metaData);
		if (_metaData == null) {
			return;
		}
		var ary = $.parseJSON(_metaData);
		$.each(ary, function(i, mapItem) {
			// var type = mapItem.type;
			var field = mapItem.field;
			var value = mapItem.value;
			$('#' + field).val(value);
		});
	});
}

function macroSqlInit(flowId, skey, arr, formCode) {
    mSql_arr = arr;
    FLOW_ID = flowId;
    $.each(mSql_arr, function (index, item) {
        var _code = item.code;
        var _metaData = item.metaData;
        if (_metaData == null) {
            return;
        }
        var _metaArr = new Array(); // 定义一数组
        _metaArr = _metaData.split(","); // 字符分割

        var data = {};
        for (i = 0; i < _metaArr.length; i++) {
            var fieldCode = _metaArr[i];
            if ($("#" + fieldCode)[0]) {
                var sqlCodes = $("#" + fieldCode).data(CODES_NAME.MACRO_SQL_CODES);
                if (sqlCodes == undefined) {
                    $("#" + fieldCode).data(CODES_NAME.MACRO_SQL_CODES, _code);
                } else {
                    $("#" + fieldCode).data(CODES_NAME.MACRO_SQL_CODES, sqlCodes + "," + _code);
                }
                if (fieldCode != "") {
                    data[fieldCode] = getPFieldVal(fieldCode);
                }

                var isSelect = false;
                if ($('select[name="' + fieldCode + '"]')[0]) {
                    isSelect = true;
                }
                if (isSelect) {
                    $('select[name="' + fieldCode + '"]').change(function () {
                        var data_param = {};
                        for (i = 0; i < _metaArr.length; i++) {
                            var fieldCode2 = _metaArr[i];
                            if (fieldCode != "") {
                                data_param[fieldCode2] = getPFieldVal(fieldCode2);
                            }
                        }
                        var urlParams = "?pageType=flow&flowId=" + flowId + "&skey=" + skey + "&fieldName=" + _code + "&formCode=" + formCode;
                        onSQLCtlRelateFieldChange(urlParams, data_param);
                    });
                } else {
                    $('input[name="' + fieldCode + '"]').bind('input propertychange', function () {
                        var data_param = {};
                        for (i = 0; i < _metaArr.length; i++) {
                            var fieldCode2 = _metaArr[i];
                            if (fieldCode != "") {
                                data_param[fieldCode2] = getPFieldVal(fieldCode2);
                            }
                        }
                        var urlParams = "?pageType=flow&flowId=" + flowId + "&skey=" + skey + "&fieldName=" + _code + "&formCode=" + formCode;
                        onSQLCtlRelateFieldChange(urlParams, data_param);
                    });
                }
            }
        }
        var urlParams = "?pageType=flow&flowId=" + flowId + "&skey=" + skey + "&fieldName=" + _code + "&formCode=" + formCode;
        onSQLCtlRelateFieldChange(urlParams, data);
    });
}

function onSQLCtlRelateFieldChange(urlParams, data) {
    $.ajax({
        type: "post",
        data: data,
        dataType: "json",
        url: "../../public/android/getSqlCtlOnChange" + urlParams,
        beforeSend: function (XMLHttpRequest) {
            jQuery.myloading();
        },
        complete: function (XMLHttpRequest, status) {
            jQuery.myloading("hide");
        },
        success: function (data_success, status) {
            //	console.log(data_success);
            var res = data_success.res;
            if (res == 1) {
                var _fieldItem = data_success.field;
                var _code = _fieldItem.code;
                var _type = _fieldItem.type;
                var _calCodes = $("#" + _code).data(CODES_NAME.CALCULATOR_CODES);
                var val = $('#' + _code).val();
                var readonly = $('#' + _code).attr("readonly");
                $("#" + _code).remove();
                var divContent = "";
                if (_type == "select") {
                    var options = _fieldItem.options;
                    divContent += "<select name='" + _code + "' id='" + _code + "'";
                    if (readonly != null) {
                        divContent += " readonly='readonly' style='background-color:#eeeeee;' onfocus='this.defaultIndex=this.selectedIndex;' onchange='this.selectedIndex=this.defaultIndex;'";
                    }
                    divContent += " >";

                    $.each(options, function (s_index, s_item) {
                        var o_text = '';
                        var o_val = '';
                        if ("name" in s_item) {
                            o_text = s_item.name;
                        }
                        if ("value" in s_item) {
                            o_val = s_item.value;
                        }
                        if ("deptCode" in s_item) {
                            o_val = s_item.deptCode;
                        }
                        if ("deptName" in s_item) {
                            o_text = s_item.deptName;
                        }
                        var disabled = _fieldItem.editable == "true" ? "" : "disabled";
                        var selected = o_val == _fieldItem.value ? "selected" : "";
                        divContent += '<option value="' + o_val + '"' + selected + '' + disabled + '>' + o_text + '</option>'
                    })
                    divContent += "</select>"
                    $("#row_" + _code).append(divContent);
                    $('#' + _code).val(val);
                } else {
                    var clear_class = readonly == null ? "mui-input-clear" : "";
                    var readonly = readonly == null  ? "" : "readonly";
                    var value = _fieldItem.value;
                    divContent += '	<input type="text" name="' + _code + '" id="' + _code + '" value="' + value + '" class="' + clear_class + '" ' + readonly + ' />';
                    $("#row_" + _code).append(divContent);
                }
                //计算控件
                if (_calCodes != undefined) {
                    $("#" + _code).data(CODES_NAME.CALCULATOR_CODES, _calCodes);
                    reInitCalculator(_calCodes);
                }
            }
        },
        error: function () {
        }
    });
}

//表单域  sql 嵌套表 自动赋值控件
//公式中code realCode  计算控件code calCodes
function reInitCalculator(calCodes) {
    if (calCodes != undefined) {
        var _calCodeArr = new Array(); // 定义一数组
        _calCodeArr = calCodes.split(","); // 字符分割
        for (i = 0; i < _calCodeArr.length; i++) {
            var _calCode = _calCodeArr[i];
            doCalculate($("#" + _calCode));  //计算控件初始化
        }
    }
}


function initCalculator() {
    $("input[kind='CALCULATOR']").each(function () {
        var _calObj = $(this);
        var _code = _calObj.attr("id");
        doCalculate(_calObj);  //计算控件初始化
        var formula = _calObj.attr('formula');

        var isSum = false;
        var regStr = /(sum\(([\w|\.]+)\))/gi;
        var mactches = formula.match(regStr)
        var len = 0;
        if (mactches) {
            len = mactches.length;
            isSum = true;
        }
        if (isSum) {
            var field = RegExp.$2;
            if (field.indexOf("nest.") == 0) {
                var p = field.indexOf(".");
                field = field.substring(p + 1);
            }
        } else {
            if (formula.toLowerCase().indexOf("subdate") != -1) {
                var pat = /subdate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
                var str = formula;
                str = str.replace(pat, function (p1, date1, date2) {
                    console.log("date1=" + date1 + " date2=" + date2);
                    var isSelect = false;
                    var o = $("input[name='" + date1 + "']")[0];
                    bindCalEvent(o, _calObj, isSelect);
                    var o = $("input[name='" + date2 + "']")[0];
                    bindCalEvent(o, _calObj, isSelect);
                });
            } else if (formula.toLowerCase().indexOf("adddate") != -1) {
                // 时间相加方法addDate(d1, d2)
                var pat = /adddate\(([a-z0-9_-]+),([0-9-]+)\)/ig;
                var str = formula;
                str.replace(pat, function (p1, date, days) {
                    var isSelect = false;
                    var o = $("input[name='" + date + "']")[0];
                    if (!o) {
                        o = $("select[name='" + date + "']")[0];
                        if (!o) {
                            alert("计算控件算式" + p1 + "中的字段：" + date + " 不存在！");
                        } else {
                            isSelect = true;
                        }
                    }
                    bindCalEvent(o, _calObj, isSelect);
                });
            } else {
                var ary = getSymbolsWithBracket(formula);
                for (var i = 0; i < ary.length; i++) {
                    // ary[i]可能为0.2这样的系数
                    if (!isOperator(ary[i]) && !isNumeric(ary[i])) {
                        var calCodes = $("#" + ary[i]).data(CODES_NAME.CALCULATOR_CODES);
                        if (calCodes == undefined) {
                            $("#" + ary[i]).data(CODES_NAME.CALCULATOR_CODES, _code);
                        } else {
                            $("#" + ary[i]).data(CODES_NAME.CALCULATOR_CODES, calCodes + "," + _code);
                        }
                        var isSelect = false;
                        var o = $("input[name='" + ary[i] + "']")[0];
                        if (!o) {
                            o = $("select[name='" + ary[i] + "']")[0];
                            if (!o) {
                                alert("计算控件" + _calObj.attr("name") + "，算式" + formula + "中的字段：" + ary[i] + " 不存在！");
                                return;
                            } else {
                                isSelect = true;
                            }
                        }

                        bindCalEvent(o, _calObj, isSelect);
                    }
                }
            }

        }
    });
}

function bindCalEvent(obj, calObj, isSelect) {
    if (isSelect) {
        obj.addEventListener("change", function (event) {
            doCalculate(calObj);
        }, false);
    } else {
        // 日期是由脚本改变的，input事件无法检测到
        var oldValue = obj.value;
        setInterval(function () {
            if (oldValue != obj.value) {
                oldValue = obj.value;
                doCalculate(calObj);
            }
        }, 500);
        // obj.addEventListener("input", function(event){ doCalculate(calObj); }, false);
    }
}

// 四则运算拆分算式
function getSymbolsWithBracket(str) {
    // 去除空格
    str = str.trim();
    if (str.indexOf("+") == 0)
        str = str.substring(1); // 去掉开头的+号
    var list = new Array();
    var curPos = 0;
    var prePos = 0;
    var k = 0;
    for (var i = 0; i < str.length; i++) {
        var s = str.charAt(i);
        if (s == '+' || s == '-' || s == '*' || s == '/' || s == '(' || s == ')') {
            if (prePos < curPos) {
                list[k] = str.substring(prePos, curPos).trim();
                k++;
            }
            list[k] = "" + s;
            k++;
            prePos = curPos + 1;
        }
        curPos++;
    }
    if (prePos <= str.length - 1)
        list[k] = str.substring(prePos).trim();
    return list;
}

// 是否四则运算符
function isOperator(str) {
    if (str == "+" || str == "*" || str == "/" || str == "-" || str == "(" || str == ")") {
        return true;
    } else
        return false;
}

function doCalculate(jqueryObj) {
    var formula = jqueryObj.attr('formula');
    var digit = jqueryObj.attr('digit');
    var isRoundTo5 = jqueryObj.attr('isRoundTo5');
    formula = callFunc(formula);
    // console.log("doCalculate: formula=" + formula);
    var ary = getSymbolsWithBracket(formula);
    for (var i = 0; i < ary.length; i++) {
        if (!isOperator(ary[i])) {
            // ary[i]可能为0.2这样的系数
            if (!isNumeric(ary[i])) {
                var $obj = $("input[name='" + ary[i] + "']");
                if (!$obj[0]) {
                    $obj = $("select[name='" + ary[i] + "']");
                }
                var v = $obj.val();
                if (v == "")
                    ary[i] = 0;
                else if (isNaN(v))
                    ary[i] = 0;
                else
                    ary[i] = "(" + v + ")";
            }
        }
    }
    formula = "";
    for (var i = 0; i < ary.length; i++) {
        formula += ary[i];
    }
    try {
        var calValue = parseFloat(eval(formula));
        var strValue = calValue.toString();
        if (isNaN(calValue)) {
            return false;
        }
        if (isRoundTo5 != null && isRoundTo5 == 1) {
            var digitNum = parseFloat(digit);
            if (!isNaN(digitNum) && digitNum > 0) {
                calValue = calValue.toFixed(digitNum);
            }
        } else if (isRoundTo5 != null && isRoundTo5 == 0) {
            var digitNum = parseFloat(digit);
            if (!isNaN(digitNum) && digitNum > 0) {
                calValue = calValue.toFixed(digitNum + 1);
                calValue = changeTwoDecimal_f(calValue, digitNum);
            }
        }

        jqueryObj.val(calValue);
    } catch (e) {
    }
}

// 保留小数点位数
function changeTwoDecimal_f(floatvar, digit) {
    var f_x = parseFloat(floatvar);
    if (isNaN(f_x)) {
        console.warn('function:changeTwoDecimal_f->parameter error');
        return false;
    }
    var s_x = f_x.toString();
    var pos_decimal = s_x.indexOf('.');
    if (pos_decimal < 0) {
        pos_decimal = s_x.length;
        s_x += '.';
    } else {
        var subString = s_x.substr(pos_decimal + 1);
        if (subString.length >= digit) {
            s_x = s_x.substr(0, pos_decimal + 1 + digit)
        }
    }
    while (s_x.length <= pos_decimal + digit) {
        s_x += '0';
    }

    return s_x;
}

function callFunc(str) {
    // 时间相减方法subdate(d1, d2)
    var pat = /subdate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
    str = str.replace(pat, function (p1, date1, date2) {
        var v1 = $("#" + date1).val();
        var v2 = $("#" + date2).val();
        if ("" == v1 || "" == v2) {
            return 0;
        } else {
            var mode = "day";
            if ($("#" + date1).attr("kind") == "DATE_TIME" || $("#" + date2).attr("kind") == "DATE_TIME") {
                mode = "hour";
            }
            // console.log("callFunc: mode=" + mode + " " + $("#"+date1).attr("kind") + " " + $("#"+date2).attr("kind"));
            var r = subDate(v1, v2, mode);
            return r;
        }
    });

    // 时间相加方法addDate(d1, d2)
    var pat = /adddate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
    str = str.replace(pat, function (p1, date, days) {
        var daysObj = $('#' + days)[0];
        if (daysObj == null) {
            if (!isNumeric(days)) {
                alert(days + "不是数字！");
                return 0;
            }
        } else {
            days = $('#' + days).val();
            if (!isNumeric(days)) {
                alert("字段" + days + "不是数字！");
                return 0;
            }
        }

        var v = $('#' + date).val();
        if ("" == v)
            return 0;
        else {
            return addDate(v, days);
        }
    });

    return str;
}

function subDate(strDate1, strDate2, mode) {
    strDate1 = strDate1.replace(/-/g, "/");
    strDate2 = strDate2.replace(/-/g, "/");

    var date1 = Date.parse(strDate1);
    var date2 = Date.parse(strDate2);

    var dt = Math.abs(date2 - date1);   //时间差的毫秒数   

    if (mode == "day") {
        return Math.round((dt) / (24 * 60 * 60 * 1000));
    } else {
        var days = Math.floor((dt) / (24 * 60 * 60 * 1000));
        // 计算出小时数
        var leave = dt % (24 * 3600 * 1000)    //计算天数后剩余的毫秒数
        var hours = leave / (3600 * 1000) + days * 24;
        return hours;
    }
}

function addDate(date, days) {
    var d = new Date(date);
    days = parseInt(days);
    // console.log(d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate() + " days=" + days);

    d.setDate(d.getDate() + days);
    var month = d.getMonth() + 1;
    var day = d.getDate();
    if (month < 10) {
        month = "0" + month;
    }
    if (day < 10) {
        day = "0" + day;
    }
    var val = d.getFullYear() + "-" + month + "-" + day;
    return val;
}

function isNumeric(str) {
    if (str == null || str == "")
        return false;
    return !isNaN(str);
}


// 大小写转换控件-事件绑定
function lowerToUpper() {
    $("input[lowerFieldCode]").each(function () {
        var $upper = $(this);
        var lowerCode = $upper.attr("lowerFieldCode");
        var f = $("#" + lowerCode).get(0);
        var fName = "";
        if (f.id != null && f.id != "") {
            fName = f.id;
        } else {
            fName = f.name;
        }
        eval("var oldValue_" + fName + "='" + f.value + "';");
        setInterval(function () {
            var oldVal = eval("oldValue_" + fName);
            if (oldVal != $(f).val()) {
                onlowerchange(event, $upper.get(0), f);
                eval("oldValue_" + fName + "=" + f.value);
            }
        }, 500);
        $upper.val(cmycurd(f.value));
    });
}

function onlowerchange(event, obj, srcObj) {
    if (event != null) {
        var srcElement = event.srcElement || event.target;
        obj.value = cmycurd(srcElement.value);
    } else {
        obj.value = cmycurd(srcObj.value);
    }
}

function onUserSelectWinChange(formCode) {
    $("input[macroCode='macro_user_select_win']").each(function() {
        console.log('onUserSelectWinChange', this);
        var $obj = $(this);
        var desc = $obj.attr('desc');
        var json = $.parseJSON(desc);
        console.log('onUserSelectWinChange json', json);
        var deptField = '';
        if (json != null) {
            if (json.deptField) {
                deptField = json.deptField;
            }
        }
        var oldVal = $obj.val();

        setInterval(function () {
            if (oldVal != $obj.val()) {
                $.ajax({
                    type:"get",
                    url: getContextPath() + "/flow/macro/macro_user_select_win_ctl_js.jsp",
                    data: {
                        "op": "getRealName",
                        "fieldName": $obj.attr('name'),
                        "userName": $obj.val(),
                        "formCode": formCode
                    },
                    success:function(data,status){
                        data = $.parseJSON(data);
                        console.log('onUserSelectWinChange', data);
                        if ($("#" + obj.name + "_realshow")[0]!= null) {
                            $("#" + obj.name + "_realshow").val(data.realName);
                        }

                        var name = $(obj).attr('name');

                        if (deptField!="") {
                            if (o(deptField)!=null) {
                                o(deptField).value = data.deptCode;
                            }
                            else {
                                console.warn('对应的部门字段：' + deptField + '不存在！');
                            }
                        }

                        var json = data.data;
                        for(var key in json) {
                            $(o(key)).val(json[key]);
                        }
                    }
                });

                oldVal = $obj.val();
            }
        }, 500);
    });
}

// 身份证变化时验证并取出生日
function onIdCardChange() {
    $("input[idCardBirthField]").each(function () {
        var $obj = $(this);
        var idCardBirthField = $obj.attr("idCardBirthField");
        if (idCardBirthField!="") {
            $obj.change(function() {
                $.ajax({
                    type: "post",
                    url: "../../module_check/checkIDCard.do",
                    data : {
                        val: $(this).val()
                    },
                    dataType: "html",
                    success: function(data, status) {
                        data = $.parseJSON(data);
                        if (data.ret==0) {
                            mui.toast(data.msg);
                        }
                        else {
                            $('#' + idCardBirthField).val(data.birthday);
                        }
                    },
                    error: function(XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            })
        }
    });
}

function openBaiduMap(obj) {
    var code = $(obj).data("code");
    var val = $(obj).data("val");

    var url = "../macro/baidu_map_location.jsp?code=" + code + "&val=" + val;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "位置");
    pop.build();
    pop.show();
}

// 表单域选择宏控件，查看详情
function openModuleShow(obj, skey, isTab) {
    var moduleCode = $(obj).data("sourceformcode");
    var val = $(obj).data("val");
    if (!isTab) {
        isTab = false;
    }

    var url = "../visual/module_detail.jsp?moduleCode=" + moduleCode + "&id=" + val + "&isTab=" + isTab + "&skey=" + skey;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "查看");
    pop.build();
    pop.show();
}

// 明细表-弹出框
function openNestSheet(obj, skey) {
    var $obj = $(obj);
    var _flowId = $obj.attr("flowId");
    var _actionId = $obj.attr("actionId");
    var _codes = $obj.attr("code");
    var _editable = $obj.attr("editable");
    var _destForm = $obj.attr("destForm");
    var _sourceForm = $obj.attr("sourceForm");
    var _parentFields = $obj.attr("parentFields");
    var _cwsId = $obj.attr("cwsId");
    var _parentModuleCode = $obj.attr("parentModuleCode");
    if (_parentModuleCode == undefined) {
        _parentModuleCode = "";
    }

    var _pageType = $obj.attr("pageType");
    if (_pageType == undefined) {
        _pageType = "flow";
    }

    var strs = new Array(); // 定义一数组
    strs = _parentFields.split(","); // 字符分割
    var json = {};
    var index = strs.length;
    for (i = 0; i < index; i++) {
        var _code = strs[i];
        json[_code] = $("#" + _code).val();
    }
    var jsonStr = _parentFields == "" ? "" : JSON.stringify(json);
    var url = "../macro/nest_sheet_select.jsp?pageType=" + _pageType + "&parentModuleCode=" + _parentModuleCode + "&cwsId=" + _cwsId + "&skey=" + skey + "&code=" + _codes + "&flowId=" + _flowId + "&actionId=" + _actionId + "&dFormCode=" + _destForm + "&sFormCode=" + _sourceForm + "&isEditable=" + _editable + "&parentFields=" + encodeURI(jsonStr) + "&isWx=1";
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "列表");
    pop.build();
    pop.show();
}

// 自选用户
function openChooseUser(chooseUsers, isAt, isFree, internalName, isCondition, workflowActionIdStr) {
    var url = "../flow/flow_choose_user.jsp?chooseUsers=" + encodeURI(chooseUsers) + "&isAt=" + isAt + "&isFree=" + isFree + "&isMulti=true&internalName=" + internalName + "&isCondition=" + isCondition + "&workflowActionIdStr=" + workflowActionIdStr;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "请选择");
    pop.build();
    pop.show();
}

function selectUserWin(obj, isMulti) {
    var code = $(obj).attr("code");
    var url = "../flow/flow_choose_user.jsp?code=" + code + "&isMulti=" + isMulti;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "请选择");
    pop.build();
    pop.show();
}

function selectUserWinForPlus(plusType, plusMode, myActionId) {
    console.log('selectUserWinForPlus plusType', plusType, 'plusMode', plusMode, 'myActionId', myActionId);
    if (plusType == undefined) {
        mui.toast('请选择加签类型')
        return;
    }
    if (plusType!=2 && plusMode == undefined) {
        mui.toast('请选择审批方式')
        return;
    }
    var url = "../flow/flow_choose_user.jsp?isMulti=true&isPlus=true&plusType=" + plusType + "&plusMode=" + plusMode + "&myActionId=" + myActionId;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "请选择");
    pop.build();
    pop.show();
}

function doneSelectUserWin(code, names, realNames) {
    var _realNames = realNames.join(",");
    var _names = names.join(",");
    $("#" + code).val(_names);
    $("#" + code + "_realshow").val(_realNames);
}

function doneSelectUserWinForPlus(userNames, realNames, plusType, plusMode, myActionId) {
    var skey = jQuery("#skey").val();
    console.log('userNames', userNames, 'plusType', plusType, 'plusMode', plusMode, 'skey', skey, 'myActionId', myActionId);
    var _realNames = realNames.join(",");
    var _names = userNames.join(",");
    window.flow.onSelUserForPlus(_names, realNames, plusType, plusMode, myActionId);
}

function openWritePadWin(obj) {
    var code = $(obj).data("code");
    var url = "../flow/writepad/writepad.jsp?code=" + code;
    pop = new Popup({
        contentType: 1,
        isReloadOnClose: false,
        isBackgroundCanClick:false,
        scrollType:'no',
        isSupportDraging:false,
        width: 300,
        height: 500
    });
    pop.setContent("contentUrl", url);
    pop.setContent("title", "手写签名");
    pop.build();
    pop.show();
}

function closeWritePad(code, val) {
    $("#" + code).val(val);
    var image = new Image();
    $(image).attr("src", val);
    $('#pad_' + code).html($(image).prop('outerHTML'));
}

function closeLocation(code, lat, lon, address) {
    if (lat != undefined && lon != undefined && address != undefined) {
        var _val = lon + "," + lat + "," + address;
        $("#" + code).val(_val);
        $("#" + code + "_realshow").val(address);
    }
}

function closeChooseUser(names, realNames, isAt, isFree, internalName, isCondition, workflowActionIdStr) {
    if (isAt) {
        var _freeDiv = jQuery("#free_flow_form");
        $(".free_next_user_ck").remove();
        $(".at_user_realnames").remove();
        for (i = 0; i < names.length; i++) {
            var value = names[i];
            _freeDiv.append('<input type="hidden" class="free_next_user_ck" name="nextUsers" value="' + value + '" />')
        }
        var _rNames = realNames.join(",");
        $(".userDiv").append("<span  class='at_user_realnames'>" + realNames.join(",") + "</span>");
    }
  	else if(isFree){
  		console.log("closeChooseUser isFree");
  		var _freeDiv = jQuery("#flow_form");
  		$(".free_next_user_ck").remove();
  		$(".at_user_realnames").remove();
  		for (i=0;i<names.length;i++) {
  			var value = names[i];
  			_freeDiv.append('<input type="hidden" class="free_next_user_ck" name="nextUsers" value="'+value+'" />')
  		}
  		var _rNames = realNames.join(",");
  		$(".userDiv").append("<span  class='at_user_realnames'>"+ realNames.join(",")+"</span>");
  	}
  	else if (isCondition) {
  	    // 当条件中有自选用户时
        window.flow.onSelUserForCondition(names, internalName, workflowActionIdStr);
    }
    else {
        var $btnChoose = jQuery("button[internalName='" + internalName + "']");
        var _name = $btnChoose.attr("name");
        for (i = 0; i < names.length; i++) {
            var value = names[i];
            var userContent = '<span class="mui-checkbox mui-left user_ck_span" style="float: left;">';
            userContent += '<label style="line-height: 45px;">';
            userContent += realNames[i];
            userContent += '</lable>';
            userContent += '<input name="' + _name + '" value="' + value + '" type="checkbox" checked class="next_user_ck" />'
            userContent += '</span>';
            $(".next_user_div").append(userContent);
        }
    }
}

// 签名框
function openSignIn(obj, skey) {
    var code = $(obj).attr("id");
    var url = "../macro/macro_ctl_sign_win.jsp?skey=" + skey + "&code=" + code;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "请选择");
    pop.build();
    pop.show();
}

function closeSignIn(code, realName) {
    $("#" + code).val(realName + " " + formatDate(new Date()));
    pop.close();
}

// 图片签名框
function openSignInImg(obj, skey) {
    var code = $(obj).data("code");
    var url = "../macro/macro_ctl_sign_img_win.jsp?skey=" + skey + "&code=" + code;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "请选择");
    pop.build();
    pop.show();
}

function closeSignInImg(code, stampId, link) {
    $('#' + code).val(stampId);
    $('#stampImg_' + code).html('');
    $('#stampImg_' + code).html('<img style="width:160px" src="../../public/showImg.do?path=' + link + '"/>');
    pop.close();
}

// 表单映射域-弹出框
function openModuleField(obj, skey) {
    var $obj = $(obj);
    var desc = $obj.attr("desc");
    var openerFieldName = $obj.attr("code");
    var descJson = JSON.parse(desc);
    var _parentFields = descJson.parentFields;
    var jsonStr = "{}";
    if (_parentFields) {
        var strs = new Array(); // 定义一数组
        strs = _parentFields.split(","); // 字符分割
        var json = {};
        for (i = 0; i < strs.length; i++) {
            var _code = strs[i];
            json[_code] = $("#" + _code).val();
        }
        jsonStr = JSON.stringify(json); // stringify后字符串会带有多余的双引号
    }
    var url = "../macro/module_field_select.jsp?skey=" + skey + "&openerFieldName=" + openerFieldName + "&parentFields=" + encodeURI(jsonStr) + "&isWx=1&desc=" + encodeURI(desc);
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "请选择");
    pop.build();
    pop.show();
}

function openDeptWin(obj) {
    var $obj = $(obj);
    var _code = $obj.attr("code");
    var url = "../macro/macro_dept_win.jsp?formCode=" + _code;
    pop = new Popup({contentType: 1, isReloadOnClose: false, width: 340, height: 500});
    pop.setContent("contentUrl", url);
    pop.setContent("title", "请选择");
    pop.build();
    pop.show();

}

function selectNode(formCode, code, name) {

    $("#" + formCode + "_realshow").val(name);
    $("#" + formCode).val(code);
}

// 关闭弹出窗口
function closeIframe() {
    pop.close();
}

// 弹出框 ->内部跳转
function nestSheetJumpPage(title, url, nest_sheet) {

    pop.setContent("contentUrl", url);
    pop.setContent("title", title);
    pop.build();
    pop.show();
}

// 表单映射域宏控件
function doneField(parentFieldMaps, byValue, showValue, openerFieldName) {
    $("#" + openerFieldName + "_realshow").val(showValue);
    $("#" + openerFieldName).val(byValue);
    if (parentFieldMaps != "") {
        var json_arr = JSON.parse(parentFieldMaps);
        $.each(json_arr, function (index, item) {
            var _value = item.value;
            var _text = item.text;
            var _name = item.name;
            $("#" + _name).val(_value);
            if ($('#' + _name + '_realshow')[0]) {
                $('#' + _name + '_realshow').val(_text);
            }
            // 计算控件
            var _calCodes = $("#" + _name).data(CODES_NAME.CALCULATOR_CODES);
            reInitCalculator(_calCodes);
            var _macroSqlCodes = $("#" + _name).data(CODES_NAME.MACRO_SQL_CODES);
            // sql控件
            if (_macroSqlCodes != undefined) {
                //console.info(mSql_arr);
                $.each(mSql_arr, function (index, item) {
                    var _code = item.code;
                    if (_macroSqlCodes.indexOf(_code) != -1) {
                        var _metaData = item.metaData;
                        var _metaArr = new Array(); // 定义一数组
                        _metaArr = _metaData.split(","); // 字符分割
                        var data = {};
                        for (i = 0; i < _metaArr.length; i++) {
                            var fieldCode = _metaArr[i];
                            if (fieldCode != "") {
                                data[fieldCode] = $("#" + fieldCode).val();
                            }
                        }
                        var urlParams = "?pageType=flow&flowId=" + FLOW_ID + "&skey=" + skey + "&fieldName=" + _code;
                        // console.info(urlParams);
                        onSQLCtlRelateFieldChange(urlParams, data);
                    }
                });
            }
        })
    }
}

// 大小写转换 控件
function cmycurd(num) { // 转成人民币大写金额形式
    var str1 = "零壹贰叁肆伍陆柒捌玖"; // 0-9所对应的汉字
    var str2 = "万仟佰拾亿仟佰拾万仟佰拾元角分"; // 数字位所对应的汉字
    var str3; // 从原num值中取出的值
    var str4; // 数字的字符串形式
    var str5 = ""; // 人民币大写金额形式
    var i; // 循环变量
    var j; // num的值乘以100的字符串长度
    var ch1; // 数字的汉语读法
    var ch2; // 数字位的汉字读法
    var nzero = 0; // 用来计算连续的零值是几个

    num = Math.abs(num).toFixed(2); // 将num取绝对值并四舍五入取2位小数
    str4 = (num * 100).toFixed(0).toString(); // 将num乘100并转换成字符串形式
    j = str4.length; // 找出最高位
    if (j > 15) {
        return '溢出';
    }
    str2 = str2.substr(15 - j); // 取出对应位数的str2的值。如：200.55,j为5所以str2=佰拾元角分

    // 循环取出每一位需要转换的值
    for (i = 0; i < j; i++) {
        str3 = str4.substr(i, 1); // 取出需转换的某一位的值
        if (i != (j - 3) && i != (j - 7) && i != (j - 11) && i != (j - 15)) { // 当所取位数不为元、万、亿、万亿上的数字时
            if (str3 == '0') {
                ch1 = '';
                ch2 = '';
                nzero = nzero + 1;
            } else {
                if (str3 != '0' && nzero != 0) {
                    ch1 = '零' + str1.substr(str3 * 1, 1);
                    ch2 = str2.substr(i, 1);
                    nzero = 0;
                } else {
                    ch1 = str1.substr(str3 * 1, 1);
                    ch2 = str2.substr(i, 1);
                    nzero = 0;
                }
            }
        } else { // 该位是万亿，亿，万，元位等关键位
            if (str3 != '0' && nzero != 0) {
                ch1 = "零" + str1.substr(str3 * 1, 1);
                ch2 = str2.substr(i, 1);
                nzero = 0;
            } else {
                if (str3 != '0' && nzero == 0) {
                    ch1 = str1.substr(str3 * 1, 1);
                    ch2 = str2.substr(i, 1);
                    nzero = 0;
                } else {
                    if (str3 == '0' && nzero >= 3) {
                        ch1 = '';
                        ch2 = '';
                        nzero = nzero + 1;
                    } else {
                        if (j >= 11) {
                            ch1 = '';
                            nzero = nzero + 1;
                        } else {
                            ch1 = '';
                            ch2 = str2.substr(i, 1);
                            nzero = nzero + 1;
                        }
                    }
                }
            }
        }
        if (i == (j - 11) || i == (j - 3)) { // 如果该位是亿位或元位，则必须写上
            ch2 = str2.substr(i, 1);
        }
        str5 = str5 + ch1 + ch2;

        if (i == j - 1 && str3 == '0') { // 最后一位（分）为0时，加上"整"
            str5 = str5 + '整';
        }
    }
    if (num == 0) {
        str5 = "零元整";
    }
    return str5;
}

function formatDateTime(date) {
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    minute = minute < 10 ? ('0' + minute) : minute;
    var second = date.getSeconds();
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
}

function formatDate(date) {
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? '0' + m : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    return y + '-' + m + '-' + d;
}

//计算控件回调
function calByNestSheet(nest_sheet, nestFormCode) {
    if (nest_sheet != null) {
        console.log('calByNestSheet nest_sheet', nest_sheet);
        if (typeof (nest_sheet) == 'object') {
            /*for (var o in nest_sheet) {
                var $ctl = $("input[formula*='nest." + o + "'][formCode='" + nestFormCode + "']");
                $ctl.val(nest_sheet[o]);
                var _calCodes = $ctl.data(CODES_NAME.CALCULATOR_CODES);
                // 置其它相关的计算控件
                reInitCalculator(_calCodes);
            }
            */
            // 20220730 将o由原来的sum(nest.je)中的je改为计算控件的字段名
            var keys = '';
            for (var o in nest_sheet) {
                if (keys.indexOf(',' + o + ',') != -1) {
                    // 跳过已正常取得的字段，因为可能在sum时两个嵌套表中都含有同名的字段，而其中一个是有formCode属性的
                    continue;
                }
                console.log('keys', keys);
                var $ctl = $("input[name='" + o + "'][formCode='" + nestFormCode + "']");
                if (!$ctl[0]) {
                    // 向下兼容会带来问题，如果在sum时两个嵌套表中都含有同名的字段，会导致出现问题，故需带有formCode属性的计算控件字段记住
                    // 向下兼容，旧版的sum型计算控件中没有formCode
                    $ctl = $("input[name='" + o + "']");
                } else {
                    if (keys == '') {
                        keys = ',' + o + ',';
                    } else {
                        keys += o + ',';
                    }
                }
                $ctl.val(nest_sheet[o]);

                var _calCodes = $ctl.data(CODES_NAME.CALCULATOR_CODES);
                // 置其它相关的计算控件
                reInitCalculator(_calCodes);
            }
        }
    }
}

function bindFuncFieldRelateChangeEvent(formCode, targetFieldName, fieldNames) {
    var ary = fieldNames.split(",");
    var len = ary.length;
    for (var i=0; i < len; i++) {
        var field = ary[i];
        if (field=="") {
            continue;
        }
        if (o(field)) {
            var oldValue = o(field).value;
            checkFuncRelateOnchange(formCode, targetFieldName, fieldNames, field, oldValue);
        }
        else {
            if (isIE11) {
                console.log(field + " is not exist");
            }
        }
    }

    // 初始化值
    doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames);
}

function checkFuncRelateOnchange(formCode, targetFieldName, fieldNames, field, oldValue) {
    setInterval(function(){
        if (oldValue != o(field).value) {
            oldValue = o(field).value;
            doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames);
        }
    },500);
}

function doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames) {
    var ary = fieldNames.split(",");
    var len = ary.length;
    var data = "formCode=" + formCode + "&fieldName=" + targetFieldName;
    for (var i=0; i < len; i++) {
        var field = ary[i];
        if (field=="") {
            continue;
        }
        data += "&" + field + "=" + o(field).value;
    }

    data += "&fieldNames=" + fieldNames;

    $.ajax({
        type: "post",
        url: "../../visual/getFuncVal.do",
        data: data,
        dataType: "html",
        beforeSend: function(XMLHttpRequest){
        },
        complete: function(XMLHttpRequest, status){
        },
        success: function(data, status){
            var ret = $.parseJSON(data);
            o(targetFieldName).value = ret.val;
        },
        error: function() {
            jAlert(XMLHttpRequest.responseText, '提示');
        }
    });
}