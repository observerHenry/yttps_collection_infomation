import 'babel-polyfill'
import Vue from 'vue'
Vue.config.debug = true;
import App from './App.vue'
import Element from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import VueKonva from 'vue-konva'

Vue.use(VueKonva)

//import {plus} from 'vue-html5plus';
Vue.use(Element)

import MintUI from 'mint-ui'
import 'mint-ui/lib/style.css'
Vue.use(MintUI)

// 引用路由
import VueRouter from 'vue-router'
// 光引用不成，还得使用

Vue.use(VueRouter)
// 引用路由配置文件
import routes from './config/routes'
// 使用配置文件规则
const router = new VueRouter({
  //mode:'history',
  routes
})

var onPlusReady = function (callback, context = this) {
    if (window.plus) {
        callback.call(context)
    } else {
        document.addEventListener('plusready', callback.bind(context))
    }
}
Vue.mixin({
    beforeCreate () {
        onPlusReady(() => {
            this.plusReady = true
        }, this)
    },
    methods: {
        onPlusReady: onPlusReady
    }
})

router.beforeEach((to, from, next) => {
//NProgress.start();
    next();
})
new Vue({
  router,
  el: '#app',
  render: h => h(App)
})

