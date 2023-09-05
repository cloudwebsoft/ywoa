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

var isIE11 = false;
function isIE() {
    return false;
}

// 向下兼容
function consoleLog(msg) {
    console.log(msg);
}

function getRadioValue(radionname) {
    var radioboxs = findObjs(radionname);
    if (radioboxs != null) {
        for (i = 0; i < radioboxs.length; i++) {
            if (radioboxs[i].type === "radio" && radioboxs[i].checked) {
                return radioboxs[i].value;
            }
        }
        return radioboxs.value
    }
    return "";
}

function setRadioValue(myitem, v) {
    var radioboxs = findObjs(myitem);
    if (radioboxs != null) {
        for (i = 0; i < radioboxs.length; i++) {
            if (radioboxs[i].type === "radio") {
                if (v == null) {
                    radioboxs[i].checked = false;
                } else {
                    if (radioboxs[i].value === v) {
                        radioboxs[i].checked = true;
                    }
                }
            }
        }
    }
}

function setRadioValueByFormObj(formObj, myitem, v) {
    var $radioboxs = $(formObj).find('[name=' + myitem + ']');
    if ($radioboxs[0] == null) {
        $radioboxs = $(formObj).find('[id=' + myitem + ']');
    }
    $radioboxs.each(function(k) {
        if (this.type === "radio") {
            if (v == null) {
                this.checked = false;
            } else {
                if (this.value === v) {
                    this.checked = true;
                }
            }
        }
    })
}

function getCheckboxValue(checkboxname) {
    var checkboxboxs = findObjs(checkboxname);
    var CheckboxValue = '';
    if (checkboxboxs != null) {
        // 如果只有一个元素
        if (checkboxboxs.length == null || checkboxboxs.length == 1) {
            // 当控件被disabled即不可写时，其为hidden，而不是checkbox
            if (checkboxboxs[0].type == "checkbox") {
                if (checkboxboxs.checked) {
                    return checkboxboxs.value;
                }
            } else {
                return checkboxboxs.value;
            }
        }
        for (i = 0; i < checkboxboxs.length; i++) {
            if (checkboxboxs[i].type == "checkbox" && checkboxboxs[i].checked) {
                if (CheckboxValue == '') {
                    CheckboxValue += checkboxboxs[i].value;
                } else {
                    CheckboxValue += "," + checkboxboxs[i].value;
                }
            }
        }
    }
    return CheckboxValue;
}

function setCheckboxChecked(checkboxname, v) {
    var checkboxboxs = findObjs(checkboxname);
    if (checkboxboxs != null) {
        // 如果只有一个元素
        if (checkboxboxs.length == null) {
            if (checkboxboxs.value == v) {
                checkboxboxs.checked = true;
            }
        }
        for (i = 0; i < checkboxboxs.length; i++) {
            if (checkboxboxs[i].type == "checkbox" && checkboxboxs[i].value == v) {
                checkboxboxs[i].checked = true;
            }
        }
    }
}

/*置多个checkbox，参数v如果有多个值，则通过,号分隔*/
function setMultiCheckboxChecked(checkboxname, v) {
    var checkboxboxs = findObjs(checkboxname);
    if (checkboxboxs != null) {
        // 如果只有一个元素
        if (checkboxboxs.length == null) {
            if (checkboxboxs.value == v) {
                checkboxboxs.checked = true;
            }
        }
        var ary = v.split(",");
        for (i = 0; i < checkboxboxs.length; i++) {
            if (checkboxboxs[i].type == "checkbox") {
                for (j = 0; j < ary.length; j++) {
                    if (checkboxboxs[i].value == ary[j]) {
                        checkboxboxs[i].checked = true;
                    }
                }
            }
        }
    }
}

function isNumeric(str) {
    if (str == null || str == "")
        return false;
    return !isNaN(str);
}

function isNotCn(str) {
    if (/[^\x00-\xff]/g.test(str))
        return false;
    else
        return true;
}

String.prototype.trim = function () {
    return this.replace(/(^\s*)|(\s*$)/g, "");
}

String.prototype.replaceAll = function (s1, s2) {
    return this.replace(new RegExp(s1, "gm"), s2);
}

function openWin(url, width, height) {
    if (width > window.screen.width)
        width = window.screen.width;
    if (height > window.screen.height)
        height = window.screen.height;
    var l = (window.screen.width - width) / 2;
    var t = (window.screen.height - height) / 2;
    // 使兼容360极速模式，否则取窗口的window.opener为null
    var pop = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=" + t + ",left=" + l + ",width=" + width + ",height=" + height);
    pop.opener = window;
    return pop;
}

function openWinMax(url) {
    if (isIE()) {
        // channelmode 只对IE 有效
        return window.open(url, '', 'scrollbars=yes,resizable=yes,channelmode'); // 开启一个被F11化后的窗口起作用的是最后那个特效
    } else {
        var myWin = window.open(url, '', 'scrollbars=yes,resizable=yes');
        myWin.moveTo(0, 0);
        myWin.resizeTo(screen.availWidth, screen.availHeight);
        return myWin;
    }
}

function selAllCheckBox(checkboxname) {
    var checkboxboxs = document.getElementsByName(checkboxname);
    if (checkboxboxs != null) {
        // 如果只有一个元素
        if (checkboxboxs.length == null) {
            checkboxboxs.checked = true;
        }
        for (i = 0; i < checkboxboxs.length; i++) {
            checkboxboxs[i].checked = true;
        }
    }
}

function deSelAllCheckBox(checkboxname) {
    var checkboxboxs = document.getElementsByName(checkboxname);
    if (checkboxboxs != null) {
        if (checkboxboxs.length == null) {
            checkboxboxs.checked = false;
        }
        for (i = 0; i < checkboxboxs.length; i++) {
            checkboxboxs[i].checked = false;
        }
    }
}

// 格式化时间
Date.prototype.Format = function (fmt) {
    // (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
    // (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

function getContextPath() {
    var strFullPath = window.document.location.href;
    var strPath = window.document.location.pathname;
    var pos = strFullPath.indexOf(strPath);
    var prePath = strFullPath.substring(0, pos);
    var postPath = strPath.substring(0, strPath.substr(1).indexOf('/') + 1);
    return (prePath + postPath);
}

function includFile(includePath, file) {
    var files = typeof file == "string" ? [file] : file;
    for (var i = 0; i < files.length; i++) {
        var name = files[i].replace(/^\s|\s$/g, "");
        var att = name.split('.');
        var ext = att[att.length - 1].toLowerCase();
        var isCSS = ext === "css";
        // var tag = isCSS ? "link" : "script";
        // var attr = isCSS ? " type='text/css' rel='stylesheet' " : " language='javascript' type='text/javascript' ";
        // var link = (isCSS ? "href" : "src") + "='" + includePath + name + "'";
        // if ($(tag + "[" + link + "]").length === 0) {
        //     document.write("<" + tag + attr + link + "></" + tag + ">");
        // }

        console.log('includeFile name', name);
        if (!isCSS) {
            const s0 = document.createElement('script');
            s0.src = includePath + name;
            s0.id = `${500}-src`;
            document.body.appendChild(s0);
        } else {
            const element = document.createElement('style');
            element.src = includePath + name;
            element.setAttribute('id', `${600}-srcCss`);
            document.head.appendChild(element);
        }
    }
}

// 判断obj是否为json对象
function isJson(obj) {
    var isjson = typeof (obj) == "object" && Object.prototype.toString.call(obj).toLowerCase() == "[object object]" && !obj.length;
    return isjson;
}

var getJsParams = function(fileName) {
    var script = $("script[src*='" + fileName + ".js']");
    // var script = $("script[src*='macro_user_select_win_ctl.js']");
    var src = script.attr("src");
    var requestParam = new Object();
    if (src.indexOf("?") != -1) {
      var str = src.substr(src.indexOf('?') + 1);
      var strs = str.split("&");
      for (var i = 0; i < strs.length; i++) {
        requestParam[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
      }
    }
    return requestParam;
}

function isImage(fileName) {
    var p = fileName.lastIndexOf('.');
    var ext = fileName.substr(p + 1);
    return ext == 'jpg' || ext == 'jpeg' || ext == 'gif' || ext == 'png' || ext == 'bmp';
}