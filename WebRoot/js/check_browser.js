function getBrowserInfo(){
	var ua = navigator.userAgent.toLocaleLowerCase();
	var browserType=null;
    if (ua.match(/edge/) != null) {
        browserType = 'edge';
    }
	else if (ua.match(/firefox/) != null) {
		browserType = "firefox";
	}else if (ua.match(/ubrowser/) != null) {
		browserType = "UC";
	}else if (ua.match(/opera/) != null) {
		browserType = "opera";
	} else if (ua.match(/bidubrowser/) != null) {
		browserType = "百度";
	}else if (ua.match(/metasr/) != null) {
		browserType = "搜狗";
	}else if (ua.match(/tencenttraveler/) != null || ua.match(/qqbrowser/) != null) {
		browserType = "QQ";
	}else if (ua.match(/maxthon/) != null) {
		browserType = "遨游";
	}
    else if (ua.match(/msie/) != null || ua.match(/trident/) != null) {
        browserType = "IE";
        browserVersion = ua.match(/msie ([\d.]+)/) != null ? ua.match(/msie ([\d.]+)/)[1] : ua.match(/rv:([\d.]+)/)[1];
    }
    else if (ua.match(/chrome/) != null) {
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
		if(is360){
			browserType = '360';
		}else{
			browserType = 'chrome';
		}
	}else if (ua.match(/safari/) != null) {
		browserType = "safari";
	}
	return browserType;
}

// 已弃用
function getBro() {
	var broName = 'Runing'
	var strStart = 0
	var strStop = 0
	var temp = ''
	var userAgent = window.navigator.userAgent // 包含以下属性中所有或一部分的字符串：appCodeName,appName,appVersion,language,platform
	// FireFox
	if (userAgent.indexOf('Firefox') !== -1) {
		strStart = userAgent.indexOf('Firefox')
		temp = userAgent.substring(strStart)
		broName = temp.replace('/', ' 版本号')
	}
	// Edge
	if (userAgent.indexOf('Edge') !== -1) {
		/* broName = 'Edge浏览器'; */
		strStart = userAgent.indexOf('Edge')
		temp = userAgent.substring(strStart)
		broName = temp.replace('/', ' 版本号')
	}
	// IE浏览器
	if (userAgent.match(/msie ([\d.]+)/)) {
		/* broName = 'IE浏览器'; */
		var s = userAgent.match(/msie ([\d.]+)/)
		var res = 'IE ' + s[1]
		broName = res + res.replace(/[^0-9.]/ig, '')
	}
	// 360极速模式可以区分360安全浏览器和360极速浏览器
	if (userAgent.indexOf('WOW') !== -1 && userAgent.indexOf('NET') < 0 && userAgent.indexOf('Firefox') < 0) {
		if (navigator.javaEnabled()) {
			broName = '360安全浏览器-极速模式'
		} else {
			broName = '360极速浏览器-极速模式'
		}
	}
	// 360兼容
	if (userAgent.indexOf('WOW') !== -1 && userAgent.indexOf('NET') !== -1 && userAgent.indexOf('MSIE') !== -1 && userAgent.indexOf('rv') < 0) {
		broName = '360兼容模式'
	}
	// Chrome浏览器
	if (userAgent.match(/Chrome\/([\d.]+)/)) {
		/* broName = 'Chrome浏览器'; */
		strStart = userAgent.indexOf('Chrome')
		strStop = userAgent.indexOf('Safari')
		temp = userAgent.substring(strStart, strStop)
		broName = temp.replace('/', ' 版本号')
	}
	// safari
	if (userAgent.match(/Version\/([\d.]+).*Safari/)) {
		strStop = userAgent.indexOf('Safari')
		temp = userAgent.substring(strStop)
		broName = temp.replace('/', ' 版本号')
	}
	// opera
	if (userAgent.match(/Opera.([\d.]+)/)) {
		strStop = userAgent.indexOf('Opera')
		temp = userAgent.substring(strStop)
		broName = temp.replace('/', ' 版本号')
	}
	return broName;
}