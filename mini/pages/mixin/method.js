import {
	reCheck,
	cache,
	setOpenId,
	Toast,
	getDeptCode,
} from "@/utils/commonHeader.js";
import store from '../../store' // 引入vuex
import {
	Api
} from "@/utils/Api.js";
const loginInfo = {
	data() {
		return {}
	},
	methods: {
		getDingDingConfig() {
			return new Promise(function(resolve, reject) {
				let url = "/public/dingding/getConfig";
				let baseUrl = Api.baseUrl;
				let deptCode = getDeptCode();
				let header = {
					'content-type': 'application/x-www-form-urlencoded',
				};
				
				uni.request({
					url: `${baseUrl}${url}`,
					method: 'POST',
					header: header,
					data: {},
					success(res) {
						console.log('取钉钉配置结果', res)
						let data = res.data;
						if (res.statusCode == 200) {
							// 如果服务端返回的结果中有抛出的异常
							if (data.code && data.code == 500) {
								console.log('获取钉钉配置失败', data.msg);
								Toast('none', data.msg);
							} else {
								resolve(data.data);
							}
						} else {
							resolve('');
							Toast('none', res.data);
						}
					},
					fail: (res) => {reject(res);},
					complete: () => {},
				})
			});
		},
		getLoginCodeForDingDingH5(code) {
			console.log("登录钉钉", code);
			let url = "/mobile/loginByCodeForDingDing";
			let baseUrl = Api.baseUrl;
			let deptCode = getDeptCode();
			let header = {
				'content-type': 'application/x-www-form-urlencoded',
				curDeptCode: deptCode,
			};

			uni.request({
				url: `${baseUrl}${url}`,
				method: 'POST',
				header: header,
				data: {
					code: code
				},
				success(res) {
					console.log('登录结果', res)
					let data = res.data;
					if (res.statusCode == 200) {
						// 如果服务端返回的结果中有抛出的异常
						if (data.code && data.code == 500) {
							console.log('企业微信服务端登录失败', data.msg);
							Toast('none', data.msg);
							uni.clearStorage();
							uni.reLaunch({
								url: "/pages/login/login",
							});
						} else {
							uni.setStorage({
								key: "loginMode",
								data: data.loginMode,
							});
							if (data.res == 0) {
								//登录成功
								let result = data.result;
								let token = "";
								//1.缓存token
								if (res.header) {
									token = res.header.authorization ?
										res.header.authorization :
										res.header.Authorization ?
										res.header.Authorization :
										"";
								}

								//2.缓存用户信息 loginInfo
								let loginInfo = result ? result : {};
								//缓存登录头部
								let header = res.header ? res.header : {};
								uni.setStorage({
									key: "loginInfo",
									data: {
										token: token,
										loginInfo: loginInfo,
										header: header,
									},
									success() {
										let info = uni.getStorageSync("loginInfo");
										store.commit('SET_INFO', info);
										// 没必要再登录检查
										// console.log('getLoginCodeForDingDingH5 reCheck');
										// reCheck();

										uni.setStorage({
											key: "curRoleCode",
											data: header.curRoleCode,
											success() {
												//是否可切换角色
												if (result
													.isRoleSwitchable) {
													//跳转
													uni.redirectTo({
														url: `${'/pages/login/selectRole'}?record=${JSON.stringify(result)}`,
													});
												} else {
													console.log(
														'getLoginCodeForDingDingH5 reLaunch /pages/index/index'
														);
													//跳转
													uni.reLaunch({
														url: "/pages/index/index",
													});
												}
											},
										});
									},
								});
							} else {
								Toast('none', data.msg);
								uni.clearStorage();
								uni.reLaunch({
									url: "/pages/login/login",
								});
							}
						}
					} else {
						Toast('none', res.data);
						uni.clearStorage();
						uni.reLaunch({
							url: "/pages/login/login",
						});
					}
				}
			})
		},
		getLoginCodeForWorkH5(code) {
			console.log("登录企业微信", code);
			let url = "/mobile/loginByCodeForWork";
			let baseUrl = Api.baseUrl;
			let deptCode = getDeptCode();
			let header = {
				'content-type': 'application/x-www-form-urlencoded',
				curDeptCode: deptCode,
			};

			uni.request({
				url: `${baseUrl}${url}`,
				method: 'POST',
				header: header,
				data: {
					code: code
				},
				success(res) {
					console.log('登录结果', res)
					let data = res.data;
					if (res.statusCode == 200) {
						// 如果服务端返回的结果中有抛出的异常
						if (data.code && data.code == 500) {
							console.log('企业微信服务端登录失败', data.msg);
							Toast('none', data.msg);
							uni.clearStorage();
							uni.reLaunch({
								url: "/pages/login/login",
							});
						} else {
							uni.setStorage({
								key: "loginMode",
								data: data.loginMode,
							});
							if (data.res == 0) {
								//登录成功
								let result = data.result;
								let token = "";
								//1.缓存token
								if (res.header) {
									token = res.header.authorization ?
										res.header.authorization :
										res.header.Authorization ?
										res.header.Authorization :
										"";
								}

								//2.缓存用户信息 loginInfo
								let loginInfo = result ? result : {};
								//缓存登录头部
								let header = res.header ? res.header : {};
								uni.setStorage({
									key: "loginInfo",
									data: {
										token: token,
										loginInfo: loginInfo,
										header: header,
									},
									success() {
										let info = uni.getStorageSync("loginInfo");
										store.commit('SET_INFO', info);
										cache("times", 1);
										// 没必要再登录检查
										// console.log('getLoginCodeForWorkH5 reCheck');
										// reCheck();

										uni.setStorage({
											key: "curRoleCode",
											data: header.curRoleCode,
											success() {
												//是否可切换角色
												if (result
													.isRoleSwitchable) {
													//跳转
													uni.redirectTo({
														url: `${'/pages/login/selectRole'}?record=${JSON.stringify(result)}`,
													});
												} else {
													console.log(
														'getLoginCodeForWorkH5 reLaunch /pages/index/index'
														);
													//跳转
													uni.reLaunch({
														url: "/pages/index/index",
													});
												}
											},
										});
									},
								});
							} else {
								Toast('none', data.msg);
								uni.clearStorage();
								uni.reLaunch({
									url: "/pages/login/login",
								});
							}
						}
					} else {
						Toast('none', res.data);
						uni.clearStorage();
						uni.reLaunch({
							url: "/pages/login/login",
						});
					}
				}
			})
		},
		getLoginCodeForWorkMini() {
			//企业微信端逻辑处理
			wx.qy.login({
				success: function(res) {
					console.log(res.code, "登录企业微信")
					if (res.code) {
						let url = "/mobile/loginByCodeForWork";
						let baseUrl = Api.baseUrl;
						let deptCode = getDeptCode();
						let header = {
							"content-type": "application/json", // 默认值
							curDeptCode: deptCode,
						};

						uni.request({
							url: `${baseUrl}${url}`,
							method: 'POST',
							header: header,
							data: {
								code: res.code
							},
							success(res) {
								console.log('系统内部的登录结果', res)
								let data = res.data;
								if (res.statusCode == 200) {
									// 如果服务端返回的结果中有抛出的异常
									if (data.code && data.code == 500) {
										console.log('企业微信服务端登录失败', data.msg);
										Toast('none', data.msg);
										uni.clearStorage();
										uni.reLaunch({
											url: "/pages/login/login",
										});
									} else {
										uni.setStorage({
											key: "loginMode",
											data: data.loginMode,
										});
										if (data.res == 0) {
											//登录成功
											let result = data.result;
											let token = "";
											//1.缓存token
											if (res.header) {
												token = res.header.authorization ?
													res.header.authorization :
													res.header.Authorization ?
													res.header.Authorization :
													"";
											}

											//2.缓存用户信息 loginInfo
											let loginInfo = result ? result : {};
											//缓存登录头部
											let header = res.header ? res.header : {};
											uni.setStorage({
												key: "loginInfo",
												data: {
													token: token,
													loginInfo: loginInfo,
													header: header,
												},
												success() {
													let info = uni.getStorageSync("loginInfo");
													store.commit('SET_INFO', info);
													cache("times", 1);
													// 保存信息至vuex，并进行登录检查
													// reCheck();
													// cache("times", 1);
													uni.setStorage({
														key: "curRoleCode",
														data: header
															.curRoleCode,
														success() {
															//是否可切换角色
															if (result
																.isRoleSwitchable
															) {
																//跳转
																uni.redirectTo({
																	url: `${'/pages/login/selectRole'}?record=${JSON.stringify(result)}`,
																});
															} else {
																//跳转
																uni.reLaunch({
																	url: "/pages/index/index",
																});
															}
														},
													});
												},
											});
										} else {
											Toast('none', data.msg);
											uni.clearStorage();
											uni.reLaunch({
												url: "/pages/login/login",
											});
										}
									}
								} else {
									Toast('none', res.data);
									uni.clearStorage();
									uni.reLaunch({
										url: "/pages/login/login",
									});
								}
							}
						})
					} else {
						console.log('登录失败！' + res.errMsg)
						that.open()
					}
				}
			});
		},
		// 小程序登录
		getLoginCode(infos = {}) {
			let that = this;
			wx.login({
				success(res) {
					if (res.code) {
						console.log("rescode", res);
						let url = "/mobile/loginByCode";
						let baseUrl = Api.baseUrl;
						//小区
						let deptCode = getDeptCode();
						let header = {
							"content-type": "application/json", // 默认值
							deptCode,
						};

						//发起网络请求
						wx.request({
							method: "GET",
							url: `${baseUrl}${url}`,
							data: {
								...infos,
								code: res.code,
								// openId: 'oKEAw49R2-cfemy9jzETzIjzCIZg'
							},
							header: header,
							success: (res) => {
								console.log("loginByCode", res);
								// res: -99 表示获取openid失败
								// res: -100 表示openid配对失败
								// 	res: 0 表示成功，返回内容与mobile/login相同

								let data = res.data;
								if (res.statusCode == 200) {
									uni.setStorage({
										key: "wxMiniLoginMode",
										data: data.wxMiniLoginMode,
									});
									uni.setStorage({
										key: "loginMode",
										data: data.loginMode,
									});
									if (data.res == 0) {
										//登录成功
										let result = res.data.result;
										let token = "";
										//1.缓存token
										if (res.header) {
											token = res.header.authorization ?
												res.header.authorization :
												res.header.Authorization ?
												res.header.Authorization :
												"";
										}

										//2.缓存用户信息 loginInfo
										let loginInfo = result ? result : {};
										//缓存登录头部
										let header = res.header ? res.header : {};
										uni.setStorage({
											key: "loginInfo",
											data: {
												token: token,
												loginInfo: loginInfo,
												header: header,
											},
											success() {
												//存储参数
												reCheck();
												// cache("times", 1);
												uni.setStorage({
													key: "curRoleCode",
													data: header.curRoleCode,
													success() {
														//是否可切换角色
														if (result
															.isRoleSwitchable) {
															//跳转
															uni.redirectTo({
																url: `${'/pages/login/selectRole'}?record=${JSON.stringify(result)}`,
															});
														} else {
															//跳转
															uni.reLaunch({
																url: "/pages/index/index",
															});

														}
													},
												});
											},
										});
									} else {
										if (data.wxMiniLoginMode == 2) {
											wx.getUserInfo({
												success(resp) {
													console.log("getUserInfo", resp);
													console.log("infos", infos);

													if (Object.keys(infos).length !=
														0) {
														return
													}
													let userInfos = {};
													userInfos = {
														encryptedData: resp
															.encryptedData,
														iv: resp.iv,
													};
													that.getLoginCode(userInfos);
												},
												fail(resFail) {
													console.log("getUserInfo失败",
														resFail);
													// 查看是否授权
													wx.getSetting({
														success(res) {
															if (res.authSetting[
																	"scope.userInfo"
																]) {
																// 已经授权，可以直接调用
																that
																	.getLoginCode();
															}
														},
														fail() {
															uni.reLaunch({
																url: "/pages/index/auth",
															});
														},
													});
												},
											});
										} else {
											uni.setStorage({
												key: "openId",
												data: {
													openId: data.openId,
												},
												success() {
													//跳转
													setTimeout(() => {
														uni.reLaunch({
															url: "/pages/login/login",
														});
													});
												},
											});
										}
									}
									// else if (data.res == -100) {
									//   uni.setStorage({
									//     key: "openId",
									//     data: {
									//       openId: data.openId,
									//     },
									//     success() {
									//       //跳转
									//       setTimeout(() => {
									//         uni.reLaunch({
									//           url: "/pages/login/login",
									//         });
									//       });
									//     },
									//   });
									// } else if (data.res == -99) {
									//   Toast("none", data.msg);
									// }
								} else {
									uni.clearStorage();
									uni.reLaunch({
										url: "/pages/login/login",
									});
								}
							},
						});
					} else {
						console.log("登录失败！" + res.errMsg);
					}
				},
			});
		}
	}
}

export default loginInfo
