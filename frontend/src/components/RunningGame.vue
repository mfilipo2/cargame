<template>
  <div id="runningGame">
    <Grid ref="grid"
          :gameId="id"
          :carName="currentCarName"
          :walls="walls"
          :cars="cars"
          :size="gameSize" />

    <div class="actions" v-if="car && !car.currentStatus.revertingFromHistoryInProgress">
        <b-button @click="goAhead()" variant="primary">Ahead<span> [W]</span></b-button>
        <b-button v-if="car.type == 'RACER'" @click="goAhead(2)" variant="primary">Fast ahead<span> [E]</span></b-button>
        <b-button @click="goLeft()" variant="primary">Left<span> [A]</span></b-button>
        <b-button @click="goRight()" variant="primary">Right<span> [D]</span></b-button>
        <b-button @click="revert()" variant="primary">Revert<span> [R]</span></b-button>
        <b-form-input id="numberOfMovements"
                    :min="1"
                    required
                    type="number"
                    placeholder="Number of movements to revert"
                    v-model="numberOfMovements" />
    </div>
    <div class="actions red" v-if="!car">
      Car crashed!
    </div>
    <div class="actions" v-if="car && car.currentStatus.revertingFromHistoryInProgress">
      Reverting moves from history, please wait...
    </div>

    <div id="errors" v-if="errors && errors.length">
      <b-alert v-for="error of errors" :key="error" show variant="danger">{{error}}</b-alert>
    </div>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import Grid from '@/components/RunningGame/Grid'
  import vueSlider from 'vue-slider-component'
  import {errorMixin} from '@/mixins/errorMixin'

  export default {
    name: 'RunningGame',
    components: {
      Grid, vueSlider
    },
    data() {
      return {
        id: '',
        currentCarName: '',
        gameSize: 0,
        walls: [],
        cars: [],
        car: {
          name: '',
          currentStatus: {
            revertingFromHistoryInProgress: false
          }
        },
        interval: 500,
        numberOfMovements: 1,
        timer: {},
        errors: []
      }
    },
    methods: {
      goAhead(distance) {
        this.errors = [];
        if(!this.car.currentStatus.revertingFromHistoryInProgress) {
          axiosClient.post('/cars/' + this.car.name + '/forward', {distance: distance})
          .then(response => {
            this.loadGame(this.id);
          }).
          catch(e => {
            let response = e.response;
            if (response && response.data && response.data.message) {
              this.errors.push(response.data.message)
            } else {
              this.errors.push(e.message);
            }
          });

        }
      },
      goLeft() {
        this.errors = [];
        if(!this.car.currentStatus.revertingFromHistoryInProgress) {
          axiosClient.post('/cars/' + this.car.name + '/left')
          .then(response => {
            this.loadGame(this.id);
          }).catch(e => {
            let response = e.response;
            if (response && response.data && response.data.message) {
              this.errors.push(response.data.message)
            } else {
              this.errors.push(e.message);
            }
          });
        }
      },
      goRight() {
        this.errors = [];
        if(!this.car.currentStatus.revertingFromHistoryInProgress) {
          axiosClient.post('/cars/' + this.car.name + '/right')
          .then(response => {
            this.loadGame(this.id);
          }).catch(e => {
            let response = e.response;
            if (response && response.data && response.data.message) {
              this.errors.push(response.data.message)
            } else {
              this.errors.push(e.message);
            }
          });
        }
      },
      revert() {
        this.errors = [];
        if(!this.car.currentStatus.revertingFromHistoryInProgress) {
          let payload = {
            gameId: this.id,
            numberOfMoves: this.numberOfMovements
          };

          axiosClient.post('/cars/' + this.car.name + '/back', payload)
          .then(response => {
            this.loadGame(this.id);
          }).catch(e => {
            let response = e.response;
            if (response && response.data && response.data.message) {
              this.errors.push(response.data.message)
            } else {
              this.errors.push(e.message);
            }
          });
        }
      },
      loadGame() {
      this.clearErrors();
        axiosClient.get('/games/' + this.id)
        .then(response => {
          let game = response.data.data;
          this.gameSize = game.map.size;
          this.walls = game.map.walls;
          axiosClient.get('/run/' + this.id)
          .then(response => {
            let self = this;
            this.cars = response.data.data.cars;
            this.car = this.cars.filter(function(car) {
              return car.name.valueOf() == self.currentCarName.valueOf();
            })[0];
          })
          .catch(e => {
            let response = e.response;
            if (response && response.data && response.data.message) {
              this.errors.push(response.data.message)
            } else {
              this.errors.push(e.message);
            }
          });
        })
        .catch(e => {
          let response = e.response;
          if (response && response.data && response.data.message) {
            this.errors.push(response.data.message)
          } else {
            this.errors.push(e.message);
          }
        });
      }
    },
    created: function() {
      this.currentCarName = this.$route.params.name;
      this.id = this.$route.params.id;
      this.loadGame();
      let self = this;
      this.errors.push('Lorem ipsum');

      this.timer = setInterval(this.loadGame, this.interval);

      window.addEventListener('keyup', function(event) {
        if(event.key == 'W' || event.key == 'w') {
          self.goAhead();
        }
        if(event.key == 'A' || event.key == 'a') {
          self.goLeft();
        }
        if(event.key == 'D' || event.key == 'd') {
          self.goRight();
        }
        if(self.car.type == 'RACER' && (event.key == 'E' || event.key == 'e')) {
          self.goAhead(2);
        }
        if(event.key == 'R' || event.key == 'r') {
          self.revert();
        }
      });
    },
    beforeDestroy() {
      clearInterval(this.timer);
    },
    mixins: [
      errorMixin
    ]
  }
</script>

<style lang="scss" scoped>
  div#runningGame {
    position: relative;
  }
  input#numberOfMovements {
    display: inline;
    width: 60px;
  }
  div#errors {
    margin-top: 4px;
  }
  div.actions {
    text-align: center;
    padding: 5px 0;

    @media screen and (max-width: 993px) {
      span {
        display: none;
      }
    }
  }
  .red {
    font-size: 2em;
    color: red;
  }
  #errors {
    position: absolute;
    bottom: 5px;
  }
</style>

