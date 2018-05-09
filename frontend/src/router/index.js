import Vue from 'vue'
import Router from 'vue-router'
import Games from '@/components/Games'
import Maps from '@/components/Maps'
import About from '@/components/About'
import Cars from '@/components/Cars'
import Game from '@/components/Games/Game'
import CarMovements from '@/components/CarMovements'
import RunningGame from '@/components/RunningGame'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/about',
      name: 'About',
      component: About
      // icon: 'fas fa-users'
    },
    {
      path: '/',
      redirect: '/maps'
    },
    {
      path: '/maps',
      name: 'Maps',
      component: Maps,
      icon: 'fas fa-map'
    },
    {
      path: '/cars',
      name: 'Cars',
      component: Cars,
      icon: 'fas fa-car'
    },
    {
      path: '/games',
      name: 'Games',
      component: Games,
      icon: 'fas fa-gamepad'
    },
    {
      path: '/games/:id',
      name: 'Game',
      component: Game
    },
    {
      path: '/carMovements',
      name: 'Car Movements',
      component: CarMovements,
      icon: 'fas fa-flag'
    },
    {
      path: '/run/:id/:name',
      name: 'Running Game',
      component: RunningGame
    }
  ]
})
