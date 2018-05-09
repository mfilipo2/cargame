<template>
  <div>
    <b-row>
      <b-col>
        <strong>Games</strong>
        <multiselect
          v-model="filterState.gameIDs"
          :options="games"
          :multiple="true"
          track-by="id"
          :custom-label="customLabel"
          @input="loadMovements()">
        </multiselect>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <strong>Maps</strong>
        <multiselect
          v-model="filterState.mapNames"
          :options="maps"
          :multiple="true"
          track-by="id"
          :custom-label="customLabel"
          @input="loadMovements()">
        </multiselect>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <strong>Cars</strong>
        <multiselect
          v-model="filterState.carNames"
          :options="cars"
          :multiple="true"
          track-by="name"
          :custom-label="customLabel"
          @input="loadMovements()">
        </multiselect>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <strong>Max results</strong>
        <b-input
          v-model="filterState.movementsLimit"
          @change="loadMovements()">
        </b-input>
      </b-col>
    </b-row>

    <h3>Results</h3>
    <b-table responsive v-if="carMovements" striped small hover :items="carMovements" :fields="carMovementsFields">
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
      <template slot="carId"></template>
    </b-table>

    <div v-if="errors && errors.length">
      <b-alert v-for="error of errors" :key="error" show variant="danger">{{error}}</b-alert>
    </div>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import Multiselect from 'vue-multiselect'
  import {errorMixin} from '@/mixins/errorMixin'
  import moment from 'moment'

  const carMovementsFields = {
    carName: {
      label: 'Car',
      sortable: true
    },
    gameName: {
      label: 'Game',
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
  }

  export default {
    name: 'CarMovements',
    components: {
      Multiselect
    },
    data() {
      return {
        filterState: {
          gameIDs: [],
          mapNames: [],
          carNames: [],
          movementsLimit: null,
          getValues: function(paramKey, key) {
            let values = this[paramKey];
            let value = '';
            for(let i=0; i < values.length; i++) {
              value += values[i][key] + ((i+1 == values.length) ? '' : ',');
            }
            return value;
          }
        },
        errors: [],
        games: [],
        maps: [],
        cars: [],
        carMovements: [],
        carMovementsFields: carMovementsFields
      }
    },
    methods: {
      loadMovements() {
        this.clearErrors();
        this.carMovements = [];
        axiosClient.get('/cars/movements/' + this.getQueryParameters())
        .then(response => {
          this.carMovements = response.data.data;
        })
        .catch(e => {
          this.addError('Select either or all of the filters.');
        });
      },
      loadGames() {
        axiosClient.get('/games')
        .then(response => {
          this.games = response.data.data;
        })
        .catch(e => {
          this.addErrorFromResponse(e.response);
        });
      },
      loadMaps() {
        axiosClient.get('/gamemaps')
        .then(response => {
          this.maps = response.data.data;
        })
        .catch(e => {
          this.addErrorFromResponse(e.response);
        });
      },
      loadCars() {
        axiosClient.get('/cars')
        .then(response => {
          this.cars = response.data.data;
        })
        .catch(e => {
          this.addErrorFromResponse(e.response);
        });
      },
      getQueryParameters() {
        let params = [{name: 'gameIDs', key: 'id'},{name: 'mapNames', key: 'name'},{name: 'carNames', key: 'name'}];
        let query = [];

        for(let i=0; i<params.length; i++) {
          let paramValue = this.filterState.getValues(params[i].name, params[i].key);
          if(paramValue && paramValue != '') {
            query.push({name: params[i].name, value: paramValue});
          }
        }

        if(this.filterState.movementsLimit && this.filterState.movementsLimit != '') {
            query.push({name: 'movementsLimit', value: this.filterState.movementsLimit});
        }

        let urlQuery = '?';
        for(let i=0; i < query.length; i++) {
          urlQuery += query[i].name + '=' + query[i].value;
          if(i+1 < query.length) urlQuery += '&';
        }

        return urlQuery;
      },
      customLabel(object) {
        return object.name;
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
    mixins: [
      errorMixin
    ],
    created: function() {
      this.clearErrors();
      this.loadGames();
      this.loadMaps();
      this.loadCars();
     }
  }
</script>

<style lang="scss" scoped>
  h3 {
    margin: 10px 0;
  }
</style>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
