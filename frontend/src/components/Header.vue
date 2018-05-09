<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top" id="mainNav">
    <a class="navbar-brand" href="/"><i class="fas fa-car"></i> CarGame</a>
    <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse"
            data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false"
            aria-label="Toggle navigation" @click="toogleNavbarCollapse()">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" :class="{ show: navbarCollapsed }" id="navbarResponsive">
      <Navigation></Navigation>
      <ul class="navbar-nav sidenav-toggler">
        <li class="nav-item">
          <a class="nav-link text-center" id="sidenavToggler" @click="toogleMenuCollapsed()">
            <i class="fa fa-fw fa-angle-left"></i>
          </a>
        </li>
      </ul>
      <ul class="navbar-nav ml-auto">
      </ul>
    </div>
  </nav>
</template>

<script>
  import Navigation from '@/components/Navigation'

  export default {
    components: {
      Navigation
    },
    name: 'Header',
    data() {
      return {
        navbarCollapsed: false,
        menuCollapsed: false
      }
    },
    methods: {
      toogleNavbarCollapse() {
        this.navbarCollapsed = !this.navbarCollapsed;
      },
      toogleMenuCollapsed() {
        if(!this.menuCollapsed) {
          this.menuCollapsed = true;
          document.body.classList.add('sidenav-toggled');
        } else {
          this.menuCollapsed = false;
          document.body.classList.remove('sidenav-toggled')
        }
      }
    }
  }
</script>
