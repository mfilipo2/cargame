<template>
  <div id="runningGameContainer" v-bind:style="{width: this.gridSize + 'px', height: this.gridSize + 'px'}">
    <Wall
      v-bind:walls="walls"
      v-for="wall in walls"
      :x="wall.x - 1"
      :y="wall.y - 1"
      :tileSize="tileSize"
      :key="'wall'+wall.x+size*wall.y" />
    <Car
      v-bind:cars="cars"
      v-for="car in cars"
      :car="car"
      :tileSize="tileSize"
      :activeCarName="carName"
      :key="'car'+car.name" />
  </div>
</template>

<script>
  import Wall from '@/components/RunningGame/Wall'
  import Car from '@/components/RunningGame/Car'

  export default {
    name: 'Grid',
    components: { Wall, Car },
    props: {
      gameId: {
        type: [String],
        required: true
      },
      carName: {
        type: [String],
        required: true
      },
      walls: {
        type: [Array],
        required: true
      },
      cars: {
        type: [Array],
        required: true
      },
      size: {
        type: [Number],
        required: true
      },
      width: {
        type: [Number],
        required: false
      }
    },
    data() {
      return {
        gridSize: 0,
        tileSize: 0,
      }
    },
    watch: {
      size: function(value) {
        this.size = value;
        this.calculateGridSize();
      }
    },
    methods: {
      calculateGridSize() {
        let containerWidth = document.getElementById('content').clientWidth - 40;
        let containerHeight = window.innerHeight - 40 - 56 - 100 - 60; // calculated as viewportHeight - headerHeight - footerHeight - breadcrumbsHeight & paddings

        // Get lower value if width not present
        if(this.width) {
          this.gridSize = this.width;
        } else {
          if(containerWidth > containerHeight) {
            this.gridSize = containerHeight;
          } else {
            this.gridSize = containerWidth;
          }
        }
        this.tileSize = this.gridSize / this.size;
      }
    },
    created: function() {
      window.addEventListener('resize', this.calculateGridSize);
      window.setTimeout(this.calculateGridSize, 500);
    }
  }
</script>

<style lang="scss" scoped>
  div#runningGameContainer {
    position: relative;
    margin: 0 auto;
    background: #eee;
  }
</style>

