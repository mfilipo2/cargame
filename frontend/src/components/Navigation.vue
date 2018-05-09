<template>
  <ul class="navbar-nav navbar-sidenav" id="exampleAccordion">
    <router-link v-if="route.icon" active-class="active" exact v-for="route in this.$router.options.routes" tag="li" :to="route.path" class="nav-item" data-toggle="tooltip" data-placement="right" title="" :data-original-title="route.name" :key="route.name">
      <a class="nav-link">
        <i :class="route.icon"></i>
        &nbsp;
        <span class="nav-link-text">{{ route.name }}</span></a>
    </router-link>
  </ul>
</template>


<script>
  export default {
    name: 'Navigation'
  }
</script>
