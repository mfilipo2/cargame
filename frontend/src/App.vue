<template>
  <div id="app">
    <Header></Header>
    <div class="content-wrapper">
      <div id="content" class="container-fluid">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            Administration
          </li>
          <li class="breadcrumb-item active">{{$route.name}}</li>
        </ol>
        <router-view/>
      </div>
      <footer class="sticky-footer">
        <div class="container">
          <div class="text-center">
            <small>SpeedApp Recruitment Task</small>
          </div>
        </div>
      </footer>
      <a class="scroll-to-top rounded" href="#page-top" style="display: none;">
        <i class="fa fa-angle-up"></i>
      </a>
    </div>
  </div>
</template>

<script>
  import Header from '@/components/Header'
  import 'bootstrap/dist/css/bootstrap.css'
  import 'bootstrap-vue/dist/bootstrap-vue.css'

  export default {
    components: {
      Header
    },
    name: 'App'
  }
</script>

<style lang="scss">
  #app {
  }
</style>
