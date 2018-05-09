<template>
  <div>
    <b-button-group id="form-buttons">
      <b-btn v-if="form.status != 'FINISHED'" @click="addCarToGame(form.id)" v-b-tooltip.hover title="Add car to game" variant="primary"><i class="fas fa-road"></i> Add car to this game</b-btn>
    </b-button-group>

    <b-row>
      <b-col>
        <h3>Game details</h3>
        <b-row>
          <b-col>
            <strong>Game name:</strong>
          </b-col>
          <b-col>
            {{form.name}}
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <strong>Status:</strong>
          </b-col>
          <b-col>
            <span v-if="form.status == 'RUNNING'"><i class="fas fa-history"></i> In progress</span>
            <span v-if="form.status == 'FINISHED'"><i class="fas fa-flag-checkered"></i> Finished</span>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <strong>Start date:</strong>
          </b-col>
          <b-col>
            {{form.startedAt | formatDate}}
          </b-col>
        </b-row>
        <b-row v-if="form.finishedAt">
          <b-col>
            <strong>End date:</strong>
          </b-col>
          <b-col>
            {{form.finishedAt | formatDate}}
          </b-col>
        </b-row>
      </b-col>
      <b-col>
        <h3>Map details</h3>
        <b-row>
          <b-col>
            <strong>Name:</strong>
          </b-col>
          <b-col>
            {{form.map.name}}
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <strong>Size:</strong>
          </b-col>
          <b-col>
            {{form.map.size | square}}
          </b-col>
        </b-row>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <h3>Cars</h3>
        <b-table responsive striped hover :items="form.cars" :fields="carsFields">
          <template slot="name" slot-scope="row">
            <span v-if="row.item.type == 'NORMAL'"><i class="fas fa-car"></i> {{row.item.name}}</span>
            <span v-if="row.item.type == 'MONSTER_TRUCK'"><i class="fas fa-truck"></i> {{row.item.name}}</span>
            <span v-if="row.item.type == 'RACER'"><i class="fab fa-phoenix-framework"></i> {{row.item.name}}</span>
          </template>

          <template slot="crashed" slot-scope="row">
            <i v-if="row.item.crashed" class="far fa-check-square"></i>
            <i v-if="!row.item.crashed" class="far fa-square"></i>
          </template>

          <template slot="inGame" slot-scope="row">
            <i v-if="row.item.inGame" class="far fa-check-square"></i>
            <i v-if="!row.item.inGame" class="far fa-square"></i>
          </template>

          <template slot="options" slot-scope="row">
            <i class="fas option fa-ban" to="form.cars" @click="removeCarFromGame(row.item.name)" v-b-tooltip.hover title="Remove car from game"></i>
            <a :href="`/#/run/${form.id}/${row.item.name}`" v-b-tooltip.hover title="View game"><i class="fas option fa-play"></i></a>
          </template>
        </b-table>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <h3>Movement history</h3>
        <b-table responsive striped hover :items="form.carMovements" :fields="carMovementsFields">
          <template slot="carName" slot-scope="row">
            <span v-if="row.item.carType == 'NORMAL'"><i class="fas fa-car"></i> {{row.item.carName}}</span>
            <span v-if="row.item.carType == 'MONSTER_TRUCK'"><i class="fas fa-truck"></i> {{row.item.carName}}</span>
            <span v-if="row.item.carType == 'RACER'"><i class="fab fa-phoenix-framework"></i> {{row.item.carName}}</span>
          </template>
          <template slot="eventType" slot-scope="row">
            <span v-if="row.item.eventType == 'TURN_LEFT'"><i class="fas fa-arrow-left"></i> Turn left</span>
            <span v-if="row.item.eventType == 'TURN_RIGHT'"><i class="fas fa-arrow-right"></i> Turn right</span>
            <span v-if="row.item.eventType == 'FORWARD'"><i class="fas fa-arrow-up"></i> Forward</span>
          </template>
          <template slot="eventTime" slot-scope="row">
            {{row.item.eventTime | formatDate }}
          </template>
        </b-table>
      </b-col>
    </b-row>
    <div v-if="errors && errors.length">
      <b-alert v-for="error of errors" :key="error" show variant="danger">{{error}}</b-alert>
    </div>

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
      <GameAddCar @cancel="closeAddCarToGameModal" @added="carAddedToGame" :game="gameId" />
    </b-modal>

    <!-- Remove car from game modal -->
    <b-modal
      ref="removeCarFromGame"
      id="removeCarFromGameModal"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Remove car from game?">
      <GameRemoveCar v-if="form.id && carNameToRemove" @cancel="closeRemoveCarFromGameModal" @removed="carRemovedFromGame" :gameId="form.id" :carName="carNameToRemove" />
    </b-modal>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import GameRemoveCar from '@/components/Games/GameRemoveCar'
  import moment from 'moment'
  import GameAddCar from '@/components/Games/GameAddCar'

  const carMovementsFields = {
    carName: {
      label: 'Car',
      sortable: true
    },
    eventType: {
      label: 'Type',
      sortable: true
    },
    eventTime: {
      label: 'Date',
      sortable: true
    }
  };

  const carsFields = {
    name: {
      label: 'Car name',
      sortable: true
    },
    type: {
      label: 'Type',
      sortable: true
    },
    inGame: {
      label: 'In game',
      sortable: true
    },
    options: {
      label: 'Options',
      sortable: false
    }
  };

  export default {
    name: 'Game',
    components: { GameAddCar, GameRemoveCar },
    data() {
      return {
        form: {
          map: {}
        },
        errors: [],
        carMovementsFields: carMovementsFields,
        carsFields: carsFields,
        carNameToRemove: '',
        gameId: 0
      }
    },
    methods: {
      loadGame(id) {
        axiosClient.get('/games/' + id)
        .then(response => {
          this.form = response.data.data;
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
      removeCarFromGame(carName) {
        this.carNameToRemove = carName;
        this.$refs.removeCarFromGame.show();
      },
      closeRemoveCarFromGameModal() {
        this.carNameToRemove = '';
        this.$refs.removeCarFromGame.hide();
      },
      carRemovedFromGame() {
        this.carNameToRemove = '';
        this.loadGame(this.$route.params.id);
        this.$refs.removeCarFromGame.hide();
      },
      addCarToGame(id) {
        this.gameId = id;
        this.$refs.addCarToGame.show();
      },
      closeAddCarToGameModal() {
        this.gameId = 0;
        this.$refs.addCarToGame.hide();
      },
      carAddedToGame() {
        this.loadGame(this.$route.params.id);
        this.gameId = 0;
        this.$refs.addCarToGame.hide();
      }
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
      this.loadGame(this.$route.params.id);
    }

  }
</script>

<style lang="scss" scoped>
  .option:hover {
    color: red;
    cursor: pointer;
  }

  #form-buttons {
    float: right;
    margin-bottom: 10px;
  }

  h3 {
    margin: 10px 0;
  }
</style>
