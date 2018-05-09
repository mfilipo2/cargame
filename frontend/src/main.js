// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import BootstrapVue from "bootstrap-vue"
import moment from 'moment'

Vue.config.productionTip = false
Vue.use(BootstrapVue)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>',
  filters: {
    formatDate: function (value) {
      if (value) {
        return moment(String(value)).format('DD.MM.YYYY HH:mm:ss:sss')
      }
    }
  }
});
