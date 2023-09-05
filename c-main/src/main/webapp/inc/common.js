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

function fo(s) {
    return o(s);
}

function obj(s) {
    if (o(s) != null)
        return o(s);
    var objs = document.getElementsByName(s);
    return objs[0];
}

function exist(s) {
    return $(s) != null;
}

function hide(s) {
    $(s).style.display = $(s).style.display == "none" ? "" : "none";
}

function isNull(_sVal) {
    return (_sVal == "" || _sVal == null || _sVal == "undefined");
}

function detectBrowser() {
    var sUA = navigator.userAgent.toLowerCase();
    var sIE = sUA.indexOf("msie");
    var sOpera = sUA.indexOf("opera");
    var sMoz = sUA.indexOf("gecko");
    if (sOpera != -1) return "opera";
    if (sIE != -1) {
        nIeVer = parseFloat(sUA.substr(sIE + 5));
        if (nIeVer >= 6) return "ie6";
        else if (nIeVer >= 5.5) return "ie55";
        else if (nIeVer >= 5) return "ie5";
    }
    if (sMoz != -1) return "moz";
    return "other";
}

var isMSIE = !!window.ActiveXObject;

function isIE() {
    // return detectBrowser().indexOf("ie")>-1;
    return isMSIE;
}

// 不兼容IE11
var IE_VER = (function () {
    var undef,
        v = 3,
        div = document.createElement('div'),
        all = div.getElementsByTagName('i');

    while (
        div.innerHTML = '<!--[if gt IE ' + (++v) + ']><i></i><![endif]-->',
            all[0]
        ) ;

    return v > 4 ? v : undef;
}());

// 兼容IE11
var _IE = (function (d, w) {
    return d.querySelector ? d.documentMode : (d.compatMode == "CSS1Compat" ? "XMLHttpRequest" in w ? 7 : 6 : 5);
}(document, this));

// var isIE6=isMSIE&&!window.XMLHttpRequest;
// var isIE8=isMSIE&&!!document.documentMode;
// var isIE7=isMSIE&&!isIE6&&!isIE8;
var isIE6 = isMSIE && (IE_VER == 6);
var isIE7 = isMSIE && (IE_VER == 7);
var isIE8 = isMSIE && (IE_VER == 8);
var isIE9 = isMSIE && (IE_VER == 9);
var isIE10 = isMSIE && (IE_VER == 10);
var isIE11 = (_IE == 11);

if (isIE11) {
    isMSIE = true;
}

if (document.documentMode == 10) {
	isIE10 = true;
}

function getOS() {
    if (isIE()) return 1;//IE
    if (isFirefox = navigator.userAgent.indexOf("Firefox") > 0) return 2;//Firefox
    if (isChrome = navigator.userAgent.indexOf("Chrome") > 0) return 3;//Chrome
    if (isSafari = navigator.userAgent.indexOf("Safari") > 0) return 4;//Safari
    if (isCamino = navigator.userAgent.indexOf("Camino") > 0) return 5;//Camino
    if (isMozilla = navigator.userAgent.indexOf("Gecko/") > 0) return 6;//Gecko
    //other...
    return 0;
}

function consoleLog(msg) {
    if (_canLog()) {
        console.log(msg);
    }
}

function _canLog() {
    if (isIE9 || isIE10 || isIE11) {
        return true;
    } else if (getOS() == 2 || getOS() == 3) {
        return true;
    }
    return false;
}

function getRadioValue(radionname) {
    var radioboxs = document.getElementsByName(radionname);
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
    var radioboxs = document.getElementsByName(myitem);
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

function getCheckboxValue(checkboxname) {
    var checkboxboxs = document.getElementsByName(checkboxname);
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
    var checkboxboxs = document.getElementsByName(checkboxname);
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
    var checkboxboxs = document.getElementsByName(checkboxname);
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

var cwAjax = {
    xmlhttp: function () {
        try {
            return new ActiveXObject('Msxml2.XMLHTTP');
        } catch (e) {
            try {
                return new ActiveXObject('Microsoft.XMLHTTP');
            } catch (e) {
                return new XMLHttpRequest();
            }
        }
    }
};

cwAjax.Request = function () {
    if (arguments.length < 2) return;
    var _p = {asynchronous: true, method: "GET", parameters: ""};
    for (var key in arguments[1]) {
        _p[key] = arguments[1][key];
    }
    var _x = cwAjax.xmlhttp();
    var _url = arguments[0];
    if (_p["parameters"].length > 0) _p["parameters"] += '&_=';
    if (_p["method"].toUpperCase() == "GET") _url += (_url.match(/\?/) ? '&' : '?') + _p["parameters"];
    _x.open(_p["method"], _url, _p["asynchronous"]);
    _x.onreadystatechange = function () {
        if (_x.readyState == 4) {
            if (_x.status == 200) {
                _p["onComplete"] ? _p["onComplete"](_x) : "";
            } else {
                _p["onError"] ? _p["onError"](_x) : "";
            }
        }
    }
    if (_p["method"].toUpperCase() == "POST") _x.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    _x.send(_p["method"].toUpperCase() == "POST" ? _p["parameters"] : null);
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

function get_cookie(Name) {
    var search = Name + "=";
    var returnvalue = "";
    // console.log(document.cookie);
    if (document.cookie.length > 0) {
        offset = document.cookie.indexOf(search)
        // if cookie exists
        if (offset != -1) {
            offset += search.length;
            // set index of beginning of value
            end = document.cookie.indexOf(";", offset);
            // set index of end of cookie value
            if (end == -1) end = document.cookie.length;
            returnvalue = unescape(document.cookie.substring(offset, end));
        }
    }
    return returnvalue;
}

function set_cookie(name, value, expires, path, domain, secure) {
    var today = new Date();
    today.setTime(today.getTime());
    if (expires) {
        expires = expires * 1000 * 60 * 60 * 24;
    }
    var expires_date = new Date(today.getTime() + (expires));
    document.cookie = name + "=" + escape(value) +
        ((expires) ? ";expires=" + expires_date.toGMTString() : "") +
        ((path) ? ";path=" + path : "") +
        ((domain) ? ";domain=" + domain : "") +
        ((secure) ? ";secure" : "");
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

function cwAddLoadEvent(func) {
    if (isIE() && !isIE11) {
        window.attachEvent('onload', func)//对于IE
    } else {
        window.addEventListener('load', func, false);//对于FireFox
    }
}

function isApple() {
    return navigator.userAgent.match(/(iPad)|(iPhone)|(iPod)/i) != null; //boolean check for popular mobile browsers
}

function getBrowserInfo() {
    var ua = navigator.userAgent.toLocaleLowerCase();
    var browserType = null;
    if (ua.match(/edge/) != null) {
        browserType = 'edge';
    } else if (ua.match(/firefox/) != null) {
        browserType = "firefox";
    } else if (ua.match(/ubrowser/) != null) {
        browserType = "UC";
    } else if (ua.match(/opera/) != null) {
        browserType = "opera";
    } else if (ua.match(/bidubrowser/) != null) {
        browserType = "百度";
    } else if (ua.match(/metasr/) != null) {
        browserType = "搜狗";
    } else if (ua.match(/tencenttraveler/) != null || ua.match(/qqbrowser/) != null) {
        browserType = "QQ";
    } else if (ua.match(/maxthon/) != null) {
        browserType = "遨游";
    } else if (ua.match(/msie/) != null || ua.match(/trident/) != null) {
        browserType = "IE";
        browserVersion = ua.match(/msie ([\d.]+)/) != null ? ua.match(/msie ([\d.]+)/)[1] : ua.match(/rv:([\d.]+)/)[1];
    } else if (ua.match(/chrome/) != null) {
        // 360极速浏览
        // 只限于在windows电脑上，在mac上谷歌浏览器和360极速浏览器里面的mimeTypes是完全一样的
        var is360 = _mime("type", "application/vnd.chromium.remoting-viewer");

        function _mime(option, value) {
            var mimeTypes = navigator.mimeTypes;
            for (var mt in mimeTypes) {
                if (mimeTypes[mt][option] == value) {
                    return true;
                }
            }
            return false;
        }

        if (is360) {
            browserType = '360';
        } else {
            browserType = 'chrome';
        }
    } else if (ua.match(/safari/) != null) {
        browserType = "safari";
    }
    return browserType;
}

/**
 * 修复jstree在chrome中点击右侧链接addTab后，左侧目录点击失效的问题
 */
function setJsTreeNotDraggable() {
    var browserInfo = getBrowserInfo().toLowerCase();
    if (browserInfo === "chrome") {
        if (window.parent) {
            var leftFrame = window.parent.frames[0];
            if (leftFrame) {
                if (typeof (leftFrame.setDraggable) == 'function') {
                    // 使左侧目录树节点不可拖动，以使得点击目录树上的节点时在addTab后能够生效
                    leftFrame.setDraggable(false);
                }
            }
        }
    }
}

var winCount = new Date().getTime();

function addTab(title, url, tabIndex) {
    var isSameOrigin = true;
    try{
        var doc = parent.document;
        if(!doc)
            throw new Error('Unaccessible');
    }catch(e){
        isSameOrigin = false;
    }
    // 再往上一层检测
    if (isSameOrigin) {
        try{
            var doc = parent.parent.document;
            if(!doc)
                throw new Error('Unaccessible');
        }catch(e){
            isSameOrigin = false;
        }
    }

    if (isSameOrigin) {
        if (window.parent.mainFrame) {
            window.parent.mainFrame.addTab(title, url);
        } else {
            if (window.parent.myDesktop) {
                var data = {
                    'iconSrc': 'desktop/icon/default.png',
                    'windowsId': 'tempWin' + winCount,
                    'windowTitle': title,
                    'iframSrc': url,
                    // 'windowWidth':800,
                    // 'windowHeight':600,
                    'parentPanel': "div.currDesktop",
                    'isWidget': false
                }

                window.parent.myDesktop.myWindow.init(data);

                //添加到状态栏
                if (!o("taskTab_" + data.windowsId) || !$("#taskTab_" + data.windowsId).size()) {
                    window.parent.myDesktop.taskBar.addTask(data.windowsId, data.windowTitle, data.iconSrc);
                }

                winCount++;
            } else if (window.top.o("content-main")) {
                if (url.indexOf("/") != 0 && url.indexOf("http") != 0) {
                    url = "../" + url
                }
                window.top.showMenuItem(url, tabIndex, title);
            } else {
                window.open(url);
                // openWin(url, 800, 600);
            }

            try {
                setJsTreeNotDraggable();
            } catch (e) {
            }
        }
    }
    else {
        // 如果跨域，直接打开新窗口
        window.open(url);
    }
}

function setActiveTabTitle(title) {
    if (window.parent.mainFrame) {
        window.parent.mainFrame.setActiveTabTitle(title);
    } else if (typeof (window.parent.getActiveTabName) != "undefined") {
        window.parent.setActiveTabTitle(title);
    }
}

function isWow64() {
    var agent = navigator.userAgent.toLowerCase();
    if (agent.indexOf("win64") >= 0 || agent.indexOf("wow64") >= 0) {
        return true;
    }
    return false;
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
        var tag = isCSS ? "link" : "script";
        var attr = isCSS ? " type='text/css' rel='stylesheet' " : " language='javascript' type='text/javascript' ";
        var link = (isCSS ? "href" : "src") + "='" + includePath + name + "'";
        if ($(tag + "[" + link + "]").length === 0) {
            document.write("<" + tag + attr + link + "></" + tag + ">");
        }
    }
}

// 取得当前打开的tab
function getActiveTabId() {
    var tabId = '';

    var isSameOrigin = true;
    try{
        var doc = parent.document;
        if(!doc)
            throw new Error('Unaccessible');
    }catch(e){
        isSameOrigin = false;
    }

    if (isSameOrigin) {
        if (window.parent.mainFrame) {
            tabId = window.parent.mainFrame.getActiveTab().id;
        } else {
            // lte风格
            if (typeof (window.parent.getActiveTabName) != "undefined") {
                tabId = window.parent.getActiveTabName();
            }
        }
    }

    return tabId;
}

// 重新加载tab
function reloadTab(tabIdOpener) {
    var isSameOrigin = true;
    try{
        var doc = parent.document;
        if(!doc)
            throw new Error('Unaccessible');
    }catch(e){
        isSameOrigin = false;
    }
    if (isSameOrigin) {
        if (window.parent.mainFrame) {
            window.parent.mainFrame.reloadTabById(tabIdOpener);
        } else {
            // lte风格
            if (window.parent.o("content-main")) {
                window.parent.reloadTabFrame(tabIdOpener);
            }
        }
    }
}

function getTabWindow(tabId) {
    var win;
    if (window.parent.mainFrame) {
        win = window.parent.mainFrame.getTabWin(tabIdOpener);
    } else {
        // lte风格
        if (window.parent.o("content-main")) {
            win = window.parent.getTabWin(tabId);
        }
    }
    return win;
}

function closeActiveTab() {
    var tabId = getActiveTabId();
    if (window.parent.mainFrame) {
        window.parent.mainFrame.closeTabById(tabId);
    } else {
        // lte风格
        if (window.parent.o("content-main")) {
            window.parent.closeLteTab(tabId);
        }
    }
}

// 判断obj是否为json对象
function isJson(obj) {
    var isjson = typeof (obj) == "object" && Object.prototype.toString.call(obj).toLowerCase() == "[object object]" && !obj.length;
    return isjson;
}

/*
* 打印 JavaScript 函数调用堆栈
*/
function printCallStack() {
    var i = 0;
    var fun = arguments.callee;
    do {
        fun = fun.arguments.callee.caller;
        consoleLog(++i + ': ' + fun);
    } while (fun);
}

// 为了使前后端分离后，后端用到控件的时候能兼容
function ajaxGetJS(myUrl, params) {
    $.get(
        getContextPath() + "/" + myUrl,
        params,
        function(data){
            if (data.indexOf('<script') === -1) {
                data = '<script>' + data + '</script>';
            }
            filterJS(data);
        }
    );
}

// AJAX加载的javascript无效，需作处理
function filterJS(html) {
    const obj = document.createElement('div');
    const srcId = '-src';
    obj.innerHTML = html;
    const _parseScripts = function () {
        const s = obj.getElementsByTagName('script');
        // For browsers which discard scripts when inserting innerHTML, extract the scripts using a RegExp
        if (s.length == 0) {
            // const re =
            //   /(?:<script.*(?:src=[\"\'](.*)[\"\']).*>.*<\/script>)|(?:<script.*>([\S\s]*?)<\/script>)/gi; // assumes HTML well formed and then loop through it.
            // let match;
            // while ((match = re.exec(html))) {
            //   const s0 = document.createElement('script');
            //   console.log('s001', s0);
            //   if (match[1]) {
            //     s0.src = match[1];
            //     s0.id = `${300}${srcId}`;
            //   } else if (match[2]) {
            //     s0.text = match[2];
            //   }
            //   document.body.appendChild(s0);
            // }
        } else {
            for (let i = 0; i < s.length; i++) {
                const s0 = document.createElement('script');
                if (s[i].text) {
                    s0.text = s[i].text;
                    s0.id = `${500}${srcId}`;
                } else {
                    s0.src = s[i].src;
                    s0.id = `${500}${srcId}`;
                }
                document.body.appendChild(s0);
            }
        }
    };
    setTimeout(_parseScripts, 10);
}