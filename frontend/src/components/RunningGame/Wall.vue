<template>
  <div>
    <div class="wall" v-bind:style="{ top: position.top, left: position.left, width: tileSize + 'px', height: tileSize + 'px' }"></div>
  </div>
</template>


<script>
  export default {
    name: 'Wall',

    props: {
      x: {
        type: [Number],
        required: true
      },
      y: {
        type: [Number],
        required: true
      },
      tileSize: {
        type: [Number],
        required: true
      }
    },
    data() {
      return {
        position: {
          top: 0,
          left: 0
        }
      }
    },
    watch: {
      tileSize: function(value) {
        this.tileSize = value;
        this.redrawWall();
      },
      x: function(value) {
        this.x = value;
        this.redrawWall();
      },
      y: function(value) {
        this.y = value;
        this.redrawWall();
      }
    },
    methods: {
      redrawWall() {
        this.position.left = parseFloat(this.tileSize) * this.x + 'px';
        this.position.top = parseFloat(this.tileSize) * this.y + 'px';
      }
    },
    created: function() {
      this.redrawWall();
    }
  }
</script>

<style lang="scss" scoped>
  .wall {
    position: absolute;
    background: #999;
  }
</style>

