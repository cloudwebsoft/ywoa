import Vue from 'vue'
import App from './App'
import store from './store'   //在main.js中引入vuex
import uView from "uview-ui";
import share from './pages/mixin/share.js'
Vue.prototype.$store = store // 注册原型==>   把vuex 注册在$store原型上
Vue.config.productionTip = false
Vue.mixin(share);
Vue.use(uView);

App.mpType = 'app'

const app = new Vue({
	...App
})
app.$mount()
