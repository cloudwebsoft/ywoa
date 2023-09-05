/*
 * @Description: It was written by Pony
 * @Version: 2.0
 * @Autor: Pony
 * @Date: 2020-09-16 17:49:54
 * @LastEditors: Pony
 * @LastEditTime: 2020-09-16 17:51:37
 */
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)
const store = new Vuex.Store({
	state: {
		loginInfo: {},
		iStatusBarHeight: 24,
		token: '',
		userInfo: {}
	},
	mutations: {
		//激活信息
		SET_INFO(state, info) {
			state.loginInfo = info['loginInfo']
			state.token = info['token'].token
		},
		//动态获取通知栏目的高度
		CHANGE_STATUS_BAR(state, nums) {
			state.iStatusBarHeight = nums
		},
		//存储个人信息
		SET_USER_INFO(state, userInfo) {
			state.userInfo = userInfo
		}
	}
})

export default store
