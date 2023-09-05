import {
	getToken,
	setToken,
	getDeptCode,
	cache,
	Toast,
	getSkey,
	getCurRoleCode
} from './commonHeader.js'
import {
	Api
} from './Api.js'
console.log("Api", Api)
/*公共地址*/
let baseUrl = Api.baseUrl;
/*客户环境*/
// let baseUrl = "http://49.73.84.187:8070";
/*本地环境*/
// let baseUrl = "http://192.168.0.109:8070";
/*获取的Token*/
let token = getToken()

/*动态混入Token*/
let defaultSetting = new Object()

export function setRequestBaseUrl(url) {
	baseUrl = Api.baseUrl;
}

/*常见的请求方法*/
/*get请求方法 返回所有，包括头部*/
export function getResultAllAction(url, Data) {
	//检查下是否存在token
	// let token = getToken()
	if (token) {
		defaultSetting.header = {
			// 'Authorization': token,
		}
	} else {
		defaultSetting = {}
	}
	return new Promise((resolve, reject) => {
		let myUrl;
		if (url.startsWith('http')) {
			myUrl = url;
		} else {
			myUrl = `${Api.baseUrl}${url}`;
		}
		console.log('getResultAllAction myUrl', myUrl);
		uni.request({
			...defaultSetting,
			url: myUrl,
			data: Data,
			method: 'get',
		}).then(res => {
			let result = res[1]
			console.log('result', result);
			if (result == null) {
				// Toast("none", "网络连接失败: myUrl=" + myUrl + " baseUrl=" + Api.baseUrl + " url=" + url);
				// Toast("none", "网络连接失败: url=" + url);
				Toast("none", "连接失败: " + JSON.stringify(res));
			}
			if (result.statusCode && result.statusCode == '500') {
				uni.clearStorage();
				uni.reLaunch({
					url: "/pages/login/login"
				})
			} else {
				resolve(result);
			}
		}).catch(e => {
			reject(e)
		});
	})
}
/*get请求方法*/
export function getAction(url, Data) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode
		}
	} else {
		defaultSetting = {}
	}
	console.log('getAction baseUrl', baseUrl);
	return new Promise((resolve, reject) => {
		let myUrl;
		if (url.startsWith('http')) {
			myUrl = url;
		} else {
			myUrl = `${baseUrl}${url}`;
		}
		console.log('getAction myUrl', myUrl);
		uni.request({
			...defaultSetting,
			url: myUrl,
			data: Data,
			method: 'get',
		}).then(res => {
			let result = res[1];
			console.log('getAction result', result);
			if (result.statusCode && result.statusCode == '500') {
				uni.clearStorage();
				uni.reLaunch({
					url: "/pages/login/login"
				})
			} else {
				setToken(result) //每次缓存头部和token
				resolve(result.data);
			}
		}).catch(e => {
			reject(e)
		});
	})
}
/*post请求方法*/
export function postAction(url, Data, method = 'post', header = {}) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode
			// "content-type": 'application/x-www-form-urlencoded'
		}
	} else {
		defaultSetting.header = {
			// "content-type": 'application/x-www-form-urlencoded'
		}
	}
	defaultSetting.header = {
		...defaultSetting.header,
		...header
	}
	let MData = {}
	return new Promise((resolve, reject) => {
		let myUrl;
		if (url.startsWith('http')) {
			myUrl = url;
		} else {
			myUrl = `${baseUrl}${url}`;
		}
		uni.request({
			...defaultSetting,
			url: myUrl,
			data: Data,
			method: method,
		}).then(res => {
			let result = res[1];
			console.log('url', myUrl);
			console.log("result==>post", result)
			if (result == undefined) {
				uni.showToast({
					icon: 'none',
					title: '请求失败'
				})
				return;
			}
			// statusCode
			if (result.statusCode && result.statusCode == '500') {
				uni.removeStorageSync('Authorization');
				uni.reLaunch({
					url: "/pages/login/login"
				})
			} else {
				setToken(result) //每次缓存头部和token
				resolve(result.data);
			}
		}).catch(res => reject('请求失败'));
	})
}
/*post请求方法*/
export function postFormAction(url, Data, method = 'post', header = {}) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode,
			// "content-type": 'application/x-www-form-urlencoded'
			'content-type': 'application/x-www-form-urlencoded',
		}
	} else {
		defaultSetting.header = {
			// "content-type": 'application/x-www-form-urlencoded'
		}
	}
	defaultSetting.header = {
		...defaultSetting.header,
		...header
	}
	let MData = {}
	return new Promise((resolve, reject) => {
		uni.request({
			...defaultSetting,
			url: `${baseUrl}${url}`,
			data: Data,
			method: method,
		}).then(res => {
			let result = res[1]
			console.log("postFormAction result", result)
			// statusCode
			if (result.statusCode && result.statusCode == '500') {
				uni.removeStorageSync('Authorization');
				uni.reLaunch({
					url: "/pages/login/login"
				})
			} else {
				setToken(result) //每次缓存头部和token
				resolve(result.data);
			}
		}).catch(res => reject(res[1].data));
	})
}
/*file请求方法*/
export function fileAction(url, Data, method = 'post', file = [], name = '') {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken();
	//小区
	let deptCode = getDeptCode() ? getDeptCode() : '';
	//skey
	let skey = getSkey();
	//角色
	let curRoleCode = getCurRoleCode();
	console.log('fileAction token', token, 'skey', skey);
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode,
			// 注意不能再设content-type，否则因boundary与uni.uploadFile中生成的不一致，会导致服务端无法解析，在chrome网络中查看时，表单数据中也为空
			// "content-type": 'multipart/form-data; boundary=----WebKitFormBoundaryAOihCT2p5Bt9Kjm3'
		}
	} else {
		defaultSetting.header = {
			// "content-type": 'multipart/form-data; boundary=----WebKitFormBoundaryAOihCT2p5Bt9Kjm3'
		}
	}
	let MData = {}
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			...defaultSetting,
			url: `${baseUrl}${url}`,
			formData: Data,
			method: method,
			files: file,
			name: name,
			filePath: ''
		}).then(res => {
			console.log('res===>', res);
			let result = res[1]
			console.log('result===>', result);
			// statusCode
			if (result.statusCode && result.statusCode == '500') {
				/* uni.removeStorageSync('Authorization');
				uni.reLaunch({
					url: "/pages/login/login"
				})*/
			} else {
				// uni.uploadFile 可能在返回时过滤了header，此处不能再setToken，否则会导致会话丢失
				// setToken(result)//每次缓存头部和token

				// 解析json，似乎uni.upload将json转为了字符串
				let data = result.data;
				if (typeof data == 'string') {
					try {
						let obj = JSON.parse(data);
						if (typeof obj == 'object' && obj) {
							data = obj;
						}
					} catch (e) {
						console.log(e);
					}
				}
				resolve(data);
			}
		}).catch(res => reject(res));
	})
}
/**
 * POST请求，返回数据解析json对象
 * URL：接口
 * postData：参数，json类型
 * doSuccess：成功的回调函数
 * doFail：失败的回调函数
 */
// export function requestPost2(url, postData, doSuccess, doFail) {
// 	if (token) {
// 		defaultSetting.header = {
// 			'Authorization': token,
// 			'Content-type': 'multipart/form-data; boundary=XXX',
// 			// "content-type": 'application/x-www-form-urlencoded'
// 		}
// 	} else {
// 		defaultSetting.header = {
// 			// "content-type": 'application/x-www-form-urlencoded'
// 			'Content-type': 'multipart/form-data; boundary=XXX'
// 		}
// 	}
// 	wx.request({
// 		...defaultSetting,
// 		url: `${baseUrl}${url}`,
// 		method: 'POST',
// 		data: '\r\n--XXX' +
// 			'\r\nContent-Disposition: form-data; name="field"' +
// 			'\r\n' +
// 			'\r\n' + JSON.stringify(postData) +
// 			'\r\n--XXX',
// 		success: function (res) {
// 			//参数值为res.data,直接将返回的数据传入
// 			console.log("直接将返回的数据传入", res);
// 			var jsonStr = res.data;
// 			if (typeof jsonStr != 'object' && jsonStr != "empty") {
// 				jsonStr = jsonStr.replace(/\ufeff/g, ""); //重点
// 				var jobj = JSON.parse(jsonStr); // 转对象
// 				doSuccess(jobj);
// 			}
// 			else
// 				doSuccess(res.data);
// 		},
// 		fail: function (res) {
// 			doFail(res);
// 		}
// 	})
// }

/*put请求方法*/
export function putAction(url, Data) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode
		}
	} else {
		defaultSetting = {}
	}
	return new Promise((resolve, reject) => {
		uni.request({
			...defaultSetting,
			url: `${baseUrl}${url}`,
			data: Data,
			method: 'put',
		}).then(res => {
			let result = res[1]
			// statusCode
			if (result.statusCode && result.statusCode == '500') {
				uni.removeStorageSync('Authorization');
				uni.reLaunch({
					url: "/pages/login/login"
				})
			} else {
				setToken(result) //每次缓存头部和token
				resolve(result.data);
			}

		}).catch(res => reject(res[1].data));
	})
}
/*delete请求方法*/
export function deleteAction(url, Data) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'content-type': 'application/x-www-form-urlencoded',
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode
		}
	} else {
		defaultSetting = {}
	}
	return new Promise((resolve, reject) => {
		uni.request({
			...defaultSetting,
			url: `${baseUrl}${url}`,
			data: Data,
			method: 'DELETE',
		}).then(res => {
			let result = res[1]
			// statusCode
			if (result.statusCode && result.statusCode == '500') {} else {
				setToken(result) //每次缓存头部和token
				resolve(result.data);
			}

		}).catch(res => reject(res[1].data));
	})
}

/*delete请求方法*/
export function deleteDefaultAction(url, Data) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			"content-type": "application/json", // 默认值
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode
		}
	} else {
		defaultSetting = {}
	}
	return new Promise((resolve, reject) => {
		uni.request({
			...defaultSetting,
			url: `${baseUrl}${url}`,
			data: Data,
			method: 'DELETE',
		}).then(res => {
			let result = res[1]
			// statusCode
			if (result.statusCode && result.statusCode == '500') {} else {
				setToken(result) //每次缓存头部和token
				resolve(result.data);
			}

		}).catch(res => reject(res[1].data));
	})
}
/*http请求方法*/
export function httpAction(url, Data, methods) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode
		}
	} else {
		defaultSetting = {}
	}
	return new Promise((resolve, reject) => {
		uni.request({
			...defaultSetting,
			url: `${baseUrl}${url}`,
			data: Data,
			method: methods,
		}).then(res => {
			let result = res[1]
			// statusCode
			if (result.statusCode && result.statusCode == '500') {
				uni.removeStorageSync('Authorization');
				uni.reLaunch({
					url: "/pages/login/login"
				})
			} else {
				setToken(result) //每次缓存头部和token
				resolve(result.data);
			}

		}).catch(res => reject(res));
	})
}
/*文件上传方法*/
export function upload(path) {
	// cache('times', null)
	//检查下是否存在token
	let token = getToken()
	//小区
	let deptCode = getDeptCode()
	//skey
	let skey = getSkey()
	//角色
	let curRoleCode = getCurRoleCode()
	if (token) {
		defaultSetting.header = {
			'Authorization': token,
			'curDeptCode': deptCode,
			'skey': skey,
			'curRoleCode': curRoleCode
		}
	} else {
		defaultSetting = {}
	}
	return new Promise((resolve, reject) => {
		//文件上传地址组合
		let FileUrl = `${baseUrl}/file/upload`;
		uni.uploadFile({
			...defaultSetting,
			url: FileUrl,
			// files:files,
			filePath: path,
			name: 'file',
			success: (uploadFileRes) => {
				resolve(JSON.parse(uploadFileRes.data))
			},
			fail: (res) => {
				reject(res)
			},
		});
	})

}

//字典标签专用（通过code获取字典数组）
export const ajaxGetDictItems = (code, params) => getAction(`/sys/dict/getDictItems/${code}`, params)
