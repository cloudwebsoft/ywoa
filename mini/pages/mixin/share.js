// 允许小程序全局分享，在main.js中引入share.js，并Vue.mixin(share);
export default {
	created() {
		//#ifdef MP-WEIXIN
		wx.showShareMenu({
			withShareTicket: true,
			menus: ['shareAppMessage', 'shareTimeline']
		});
		//#endif
	},
}
