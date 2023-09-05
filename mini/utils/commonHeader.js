
import store from '../store' // 引入vuex
function interUrl() {
	return ''
}
//拿缓存里面token
function getToken() {
	try {
		const Token = uni.getStorageSync('loginInfo').token
		if (Token) {
			return Token
		} else {
			return ""
		}
	} catch (e) {
		return ""
	}

}
//获取skey
function getSkey() {
	try {
		const loginInfo = uni.getStorageSync('loginInfo').loginInfo
		if (loginInfo && loginInfo.skey) {
			return loginInfo.skey
		} else {
			return ""
		}
	} catch (e) {
		return ""
	}
}
//获取openId
function getOpenId() {
	try {
		const OpenId = uni.getStorageSync('openId').openId
		if (OpenId) {
			return OpenId
		} else {
			return ""
		}
	} catch (e) {
		return ""
	}
}
//获取curRoleCode  角色
function getCurRoleCode() {
	try {
		const CurRoleCode = uni.getStorageSync('curRoleCode')
		if (CurRoleCode) {
			return CurRoleCode
		} else {
			return ""
		}
	} catch (e) {
		return ""
	}
}
//缓存token
function setToken(res) {
	let token = ''
	//1.缓存token

	if (res.header) {
		token = res.header.authorization
			? res.header.authorization
			: res.header.Authorization
				? res.header.Authorization
				: "";
	}
	// #ifdef MP-WEIXIN || APP-PLUS
	// token = res.header.Authorization ? res.header.Authorization : "";
	// #endif

	// #ifdef H5
	// token = res.header.authorization ? res.header.authorization : "";
	// #endif
	//缓存登录头部
	let header = res.header ? res.header : {};
	let loginInfo = uni.getStorageSync('loginInfo').loginInfo
	let headerInfo = uni.getStorageSync('loginInfo').header
	uni.setStorage({
		key: "loginInfo",
		data: {
			token: token,
			loginInfo: loginInfo,
			header: Object.assign(headerInfo, header),
		},
		success() {
		},
	});
}
//拿缓存里的deptCode
function getDeptCode() {
	try {
		const header = uni.getStorageSync('loginInfo').header
		if (header) {
			return header.curDeptCode
		} else {
			return ""
		}
	} catch (e) {
		return ""
	}
}
//缓存,默认有效期1天 cache('times', null, 30)
function cache(key, value, seconds) {
	var timestamp = Date.parse(new Date()) / 1000
	if (key && value === null) {
		//删除缓存
		//获取缓存
		var val = uni.getStorageSync(key);
		var tmp = val.split("|")
		if (!tmp[1] || timestamp >= tmp[1]) {
			console.log("key已失效")
			uni.clearStorageSync();
			//以下代码不生效
			// reCheck()
			// setTimeout(() => {
			// 	uni.reLaunch({
			// 		url: "./pages/login/login"
			// 	})
			// })
			uni.redirectTo({
				url: "/pages/login/login",
			});
			return ""
		} else {
			console.log("key未失效")
			return tmp[0]
		}
	} else if (key && value) {
		//设置缓存
		if (!seconds) {
			var expire = timestamp + (3600 * 24 * 1)
		} else {
			var expire = timestamp + (seconds - 0)
		}
		value = value + "|" + expire
		uni.setStorageSync(key, value);
	} else {
		console.log("key不能空")
	}
}
//拿缓存里面sysCompanyId
function getCompanyId() {
	try {
		const sysCompanyId = uni.getStorageSync('loginInfo').companyConfig.sysCompanyId
		if (sysCompanyId) {
			return sysCompanyId
		} else {
			return ""
		}
	} catch (e) {
		return ""
	}
}

function deteleObject(obj) {
	var uniques = [];
	var stringify = {};
	for (var i = 0; i < obj.length; i++) {
		var keys = Object.keys(obj[i]);
		keys.sort(function (a, b) {
			return (Number(a) - Number(b));
		});
		var str = '';
		for (var j = 0; j < keys.length; j++) {
			str += JSON.stringify(keys[j]);
			str += JSON.stringify(obj[i][keys[j]]);
		}
		if (!stringify.hasOwnProperty(str)) {
			uniques.push(obj[i]);
			stringify[str] = true;
		}
	}
	uniques = uniques;
	return uniques;
}
/*通用提示*/
function Toast(icon, title) {
	uni.showToast({
		icon: icon,
		title: title,
		// #ifdef MP-WEIXIN
		duration: 3000,
		// #endif
		mask: true
	})
}
//导向
function reCheck(path) {
	//获取缓存的信息 同步去获取缓存信息
	let info = uni.getStorageSync("loginInfo");
	console.log("reCheck loginInfo", info);
	console.log('reCheck path', path);
	//用户信息存在时候跳转到首页
	if (info) {
		// 存储信息
		store.commit('SET_INFO', info);
		//状态栏的高度
		let iStatusBarHeight = uni.getSystemInfoSync().statusBarHeight;
		//传递状态的高度
		store.commit('CHANGE_STATUS_BAR', iStatusBarHeight);
		if (path && path.indexOf('pages/index/indexPage')!=-1) {
			console.log('reCheck reLaunch /pages/index/index');		
			setTimeout(() => {
				uni.reLaunch({
					url: "./pages/index/index"
				})
			}, 500);
		} else {
			setTimeout(() => {
				uni.reLaunch({
					url: "./pages/login/login"
				})
			})
		}
	} else {
		console.log('reCheck reLaunch /pages/login/login');
		setTimeout(() => {
			uni.reLaunch({
				url: "./pages/login/login"
			})
		})
	}
	// uni.getStorage({
	// 	key: 'info',
	// 	success: e => {
	// 		console.log("success", e)
	// 		//信息
	// 		let info = e.data
	// 		// //存储信息
	// 		store.commit('SET_INFO', info);
	// 		//状态栏的高度
	// 		let iStatusBarHeight = uni.getSystemInfoSync().statusBarHeight;
	// 		//传递状态的高度
	// 		store.commit('CHANGE_STATUS_BAR', iStatusBarHeight);
	// 		uni.reLaunch({
	// 			url: './pages/index/index'
	// 		});
	// 	},
	// });
}
//存储个人信息
function setUserInfo() {
	let uesrInfo = uni.getStorageSync("uesrInfo");
	if (uesrInfo) {
		// //存储信息
		store.commit('SET_USER_INFO', uesrInfo);
	}
}

function getPlatform() {
	let platform = undefined;
	// #ifdef APP-PLUS
	platform = 'APP';
	// #endif
	
	// #ifdef MP-WEIXIN
	platform = 'WX';
	// #endif
	
	// #ifdef H5
	platform = 'H5';
	// #endif
	
	return platform;
}

export {
	getToken,
	getCompanyId,
	deteleObject,
	Toast,
	reCheck,
	interUrl,
	setToken,
	getDeptCode,
	cache,
	setUserInfo,
	getOpenId,
	getSkey,
	getCurRoleCode,
	getPlatform,
}
