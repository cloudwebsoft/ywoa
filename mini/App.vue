<script>
	import {
		reCheck,
		cache,
		setOpenId,
		Toast,
		getSkey,
		getDeptCode,
	} from "./utils/commonHeader.js";
	import {
		Api
	} from "./utils/Api.js";
	import * as dd from 'dingtalk-jsapi';
	import store from './store' // 引入vuex
	import loginInfo from "@/pages/mixin/method.js";
	export default {
		globalData: {
			wgtVer: "",
			version: "",
		},
		mixins: [loginInfo],
		data() {
			return {
				isFromWxWorkOrDd: false,
			}
		},
		onLaunch: function(e) {
			console.log("首页onLaunch", e);
			let that = this;
			// #ifdef H5
			console.log('getSkey()', getSkey());
			// 如果未登录，则尝试登录
			if (getSkey() == '') {
				// 如果在钉钉中运行
				if (dd.env.platform !== "notInDingTalk") {
					that.getDingDingConfig().then(res => {
						let cfg = res;
						console.log('getDingDingConfig', cfg);
						that.isFromWxWorkOrDd = true;
						//进行钉钉登录操作
						dd.ready(function() {
							dd.runtime.permission.requestAuthCode({
								corpId: cfg.corpId,
								onSuccess: function(info) {
									console.log('info', info);
									let code = info.code; // 通过该免登授权码可以获取用户身份
									that.getLoginCodeForDingDingH5(code);
								},
								onFail: function(err) {
									Toast("获取钉钉个人信息异常，请稍后重试! " + JSON.stringify(err));
								}
							});
						});
					}).catch(error => {
						// 当状态改为rejcted及执行体出现错误时,被执行
						console.log('getDingDingConfig 失败:' + error)
					});
				} else {
					var url = window.location.search;
					var params = new Object();
					if (url.indexOf("?") != -1) {
						var str = url.substr(1);
						var strs = str.split("&");
						for (var i = 0; i < strs.length; i++) {
							params[strs[i].split("=")[0]] = (strs[i].split("=")[1]);
						}
					}
					console.log('params', params);

					// 企业微信应用登录
					if (params.code && params.state) {
						that.isFromWxWorkOrDd = true;
						that.getLoginCodeForWorkH5(params.code);
						return;
					}
				}
			} else {
				// 如果是默认的首页（空白页），则指向消息页
				if (e.path == 'pages/index/indexPage') {
					uni.reLaunch({
						url: "/pages/index/index",
					});
				}
			}
			// #endif;

			// console.log('App Launch，app启动')
			// #ifdef MP-WEIXIN
			console.log('ifdef MP-WEIXIN');
			const sysInfo = uni.getSystemInfoSync();
			console.log("sysInfo", sysInfo);
			console.log('wx.qy', wx.qy);

			// 企业微信端小程序，在H5下检测不到
			if (sysInfo.environment == 'wxwork' || wx.qy) {
				that.getLoginCodeForWorkMini();
			} else {
				that.getLoginCode();
			}

			// reCheck();
			//  查看是否授权
			// wx.getSetting({
			//   success(res) {
			//     console.log("是否授权", res);
			//     if (res.authSetting["scope.userInfo"]) {
			//       // 已经授权，可以直接调用
			//       that.getLoginCode();
			//     }
			//   },
			//   fail() {
			//     uni.reLaunch({
			//       url: "/pages/index/auth",
			//     });
			//   },
			// });
			// uni.reLaunch({
			//   url: "/pages/index/auth",
			// });
			// #endif
		},
		onShow: function(e) {
			console.log("首页onShow", e);
			console.log('getSkey()', getSkey(), 'this.isFromWxWorkOrDd', this.isFromWxWorkOrDd);
			// #ifdef H5
			// 如果来自于企业微信或者已登录，则不reCheck，以免闪现登录页或刷新后回到首页
			if (!this.isFromWxWorkOrDd && getSkey() == '') {
				reCheck(e.path);
			}
			// #endif
			
			// #ifdef APP-PLUS
			reCheck(e.path);
			// #endif
			
			// #ifdef MP-WEIXIN
			reCheck(e.path);
			// #endif
						
			// cache("times", null);
			if (!window) {
				// #ifdef APP-PLUS
				this.plusReady();
				// #endif
			}
		},
		onHide: function() {
			// console.log('App Hide，app不再展现在前台')
		},
		created: function() {
			//在页面加载时恢复本地存储空间里的状态信息
			let info = uni.getStorageSync("loginInfo");
			if (info) {
				store.commit('SET_INFO', info);
			}
		
			// 在页面刷新时将vuex里的信息保存到sessionStorage里
			// window.addEventListener('beforeunload', () => {
			//   sessionStorage.setItem('store', JSON.stringify(this.$store.state));
			// });
		},
		methods: {
			// 获取当前版本号
			plusReady() {
				var that = this;
				// 获取本地应用资源版本号
				plus.runtime.getProperty(plus.runtime.appid, function(inf) {
					let wgtVer = inf.version; //获取当前版本版本名称  1.0.0
					let versionCode = inf.versionCode; //获取当前版本号 100
					let version = plus.runtime.version; //代表的是:manifest.json中设置的apk/ipa版本号。
					that.$store.state.version = wgtVer;
					console.log(
						"获取当前版本号",
						wgtVer,
						versionCode,
						version,
						typeof wgtVer,
						typeof versionCode
					);
				});
			},
		},
	};
</script>

<style lang="scss">
	@import "uview-ui/index.scss";
	@import url("@/static/font-awesome-4.7.0/css/font-awesome.css");

	page {
		height: 100%;
	}

	/* 解决头条小程序组件内引入字体不生效的问题 */
	/* #ifdef MP-TOUTIAO */
	@font-face {
		font-family: uniicons;
		src: url("/static/uni.ttf");
	}

	/* #endif */
	/*弹性盒*/
	/*居中*/
	.heri-center {
		display: flex;
		justify-content: center;
		align-items: center;
	}

	/*水平两端对其*/
	.heri-between {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	/*水平开始*/
	.heri-start-center {
		display: flex;
		justify-content: flex-start;
		align-items: center;
	}

	/*水平结束*/
	.heri-end-center {
		display: flex;
		justify-content: flex-end;
		align-items: center;
	}

	/*水平主轴开始*/
	.heri-start {
		display: flex;
		justify-content: flex-start;
		align-items: flex-start;
	}

	/*垂直两端对其*/
	.ver-between {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: center;
	}

	/*垂直上下左右居中*/
	.ver-center {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
	}

	/*垂直上下居中*/
	.ver-item-center {
		display: flex;
		align-items: center;
	}

	/**上边距*/
	.mt-40 {
		margin-top: 40upx;
	}

	/**下边距*/
	.mb-40 {
		margin-bottom: 40upx;
	}

	/*文字溢出*/
	.word-overflow {
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	/*盒子*/
	.mainPage {
		width: 100%;
		min-height: 100vh; //85vh;;
		background: #f7f8fa;
	}

	/*font-size:14*/
	.font-14 {
		font-size: 28upx;
	}

	.ValideRequest {
		//表单必填文字样式
		color: #ff0000;
		font-size: 24upx;
	}
</style>
