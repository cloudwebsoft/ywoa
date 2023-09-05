import {
    reCheck,
    cache,
    setOpenId,
    Toast,
    getDeptCode,
} from "./commonHeader.js";
import { Api } from "./Api.js";
function getLoginCode(infos = {}) {
    let that = this;
    wx.login({
        success(res) {
            if (res.code) {
                console.log("rescode", res);
                let url = "/mobile/loginByCode";
                let baseUrl = Api.baseUrl;
                //部门
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
                                    token = res.header.authorization
                                        ? res.header.authorization
                                        : res.header.Authorization
                                            ? res.header.Authorization
                                            : "";
                                }

                                // #ifdef MP-WEIXIN ||  APP-PLUS
                                // token = res.header.Authorization ? res.header.Authorization : "";
                                // #endif

                                // #ifdef H5
                                // token = res.header.authorization ? res.header.authorization : "";
                                // #endif
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
                                        //跳转
                                        uni.reLaunch({
                                            url: "/pages/index/index",
                                        });
                                    },
                                });
                            } else {
                                if (data.wxMiniLoginMode == 2) {
                                    wx.getUserInfo({
                                        success(resp) {
                                            console.log("getUserInfo", resp);
                                            console.log("infos", infos);
											
											if(Object.keys(infos).lenth!=0){
												return
											}
                                            let userInfos = {};
                                            userInfos = {
                                                encryptedData: resp.encryptedData,
                                                iv: resp.iv,
                                            };
                                            that.getLoginCode(userInfos);
                                        },
                                        fail(resFail) {
                                            console.log("getUserInfo失败", resFail);
                                            // 查看是否授权
                                            wx.getSetting({
                                                success(res) {
                                                    if (res.authSetting["scope.userInfo"]) {
                                                        // 已经授权，可以直接调用
                                                        that.getLoginCode();
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

export {
    getLoginCode
}