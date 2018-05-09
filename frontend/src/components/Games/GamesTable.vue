<template>
  <div>
    <b-table responsive striped hover :items="games" :fields="fields">
      <template slot="size" slot-scope="row">{{row.item.size | square}}</template>
      <template slot="started" slot-scope="row">
        {{row.item.started | formatDate }}
      </template>
      <template slot="options" slot-scope="data">
        <router-link :to="`/games/${data.item.id}`" class="fas option fa-search" tag="i" v-b-tooltip.hover title="View game" />
        <i class="fas option fa-road" @click="addCarToGame(data.item.id)" v-b-tooltip.hover title="Add car to game"></i>
      </template>
    </b-table>

    <!-- Add car to game modal -->
    <b-modal
      ref="addCarToGame"
      id="addCarToGameModal"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Add car to game">
      <GameAddCar v-if="nameToAdd" @cancel="closeAddCarToGameModal" @added="carAddedToGame" :game="nameToAdd" />
    </b-modal>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import GameAddCar from '@/components/Games/GameAddCar'
  import moment from 'moment'

  const fields = {
    name: {
      label: 'Map name',
      sortable: true
    },
    started: {
      label: 'Started',
      sortable: true
    },
    size: {
      label: 'Size',
      sortable: true
    },
    options: {
      label: 'Options'
    }
  }

  export default {
    name: 'GamesTable',
    components: {GameAddCar},
    props: {
      status: {
        type: [String],
        required: true
      }
    },
    data() {
      return {
        fields: fields,
        games: [],
        errors: [],
        nameToAdd: ''
      }
    },
    methods: {
      loadGames() {
        axiosClient.get('/games?status=' + this.status)
        .then(response => {
          this.games = [];

          for(let i=0; i < response.data.data.length; i++) {
            let game = {
              id: response.data.data[i].id,
              name: response.data.data[i].name,
              size: response.data.data[i].map.size,
              started: response.data.data[i].startedAt
            }
            this.games.push(game);
          }
        })
        .catch(e => {
          let response = e.response;
          if (response && response.data && response.data.message) {
            this.errors.push(response.data.message)
          } else {
            this.errors.push(e.message);
          }
        });
      },
      addCarToGame(name) {
        this.nameToAdd = name;
        this.$refs.addCarToGame.show();
      },
      closeAddCarToGameModal() {
        this.$refs.addCarToGame.hide();
      },
      carAddedToGame() {
        this.loadGames();
        this.$refs.addCarToGame.hide();
      },
    },
    filters: {
      square: function(value) {
        return '' + value + 'x' + value;
      },
      formatDate: function (value) {
        if (value) {
          return moment(String(value)).format('DD.MM.YYYY HH:mm:ss:sss')
        }
      }
    },
    created: function() {
      this.loadGames();
    }
  }
</script>


<style lang="scss" scoped>
  .option:hover {
    color: red;
    cursor: pointer;
  }
</style>
