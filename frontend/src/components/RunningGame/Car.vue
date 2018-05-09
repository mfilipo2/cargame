<template>
  <div>
    <div class="car"
         v-bind:style="{ top: position.top, left: position.left, width: tileSize + 'px', height: tileSize + 'px' }"
         v-bind:class="[ position.direction, type, activeCarClass ]"
    ></div>
  </div>
</template>


<script>
  export default {
    name: 'Car',

    props: {
      car: {
        type: [Object],
        required: true
      },
      tileSize: {
        type: [Number],
        required: true
      },
      activeCarName: {
        type: [String],
        required: true
      }
    },

    data() {
      return {
        position: {
          top: 0,
          left: 0,
          direction: ''
        },
        type: '',
        activeCarClass: ''
      }
    },
    watch: {
      tileSize: function(value) {
        this.tileSize = value;
        this.redrawCar();
      },
      car: function(value) {
        this.car = value;
        this.redrawCar();
      }
    },
    methods: {
      redrawCar() {
        this.position = {
          left: parseFloat(this.tileSize) * this.car.currentStatus.x + 'px',
          top: parseFloat(this.tileSize) * this.car.currentStatus.y + 'px',
          direction: this.car.currentStatus.direction.toLowerCase()
        };

        this.activeCarClass = (this.car.name.valueOf() == this.activeCarName.valueOf()) ? 'controlled' : '';

        switch(this.car.type) {
          case 'NORMAL':
            this.type = 'normal';
            break;
          case 'MONSTER_TRUCK':
            this.type = 'monster';
            break;
          case 'RACER':
            this.type = 'racer';
            break;
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  .car {
    position: absolute;
    background: #000;
  }
  .monster, .racer, .normal {
    opacity: 0.3;
    filter: alpha(opacity=30);
  }
  .monster {
    background: url('../../assets/monster.png') no-repeat;
    background-size: contain;
    background-color: rgba(0,0,255,0.3);
  }
  .racer {
    background: url('../../assets/racer.png') no-repeat;
    background-size: contain;
    background-color: rgba(255,0,0,0.3);
  }
  .normal {
    background: url('../../assets/car.png') no-repeat;
    background-size: contain;
    background-color: rgba(0,255,0,0.3);
  }
  .controlled {
    opacity: 1;
  }
  .north {
  }
  .south {
    -webkit-transform: rotate(180deg);
    -moz-transform: rotate(180deg);
    -ms-transform: rotate(180deg);
    -o-transform: rotate(180deg);
    transform: rotate(180deg);
  }
  .east {
    -webkit-transform: rotate(90deg);
    -moz-transform: rotate(90deg);
    -ms-transform: rotate(90deg);
    -o-transform: rotate(90deg);
    transform: rotate(90deg);
  }
  .west {
    -webkit-transform: rotate(270deg);
    -moz-transform: rotate(270deg);
    -ms-transform: rotate(270deg);
    -o-transform: rotate(270deg);
    transform: rotate(270deg);
  }
  .square {
    width: 100%;
    padding-bottom: 100%;
    background-size: cover;
  }
</style>

