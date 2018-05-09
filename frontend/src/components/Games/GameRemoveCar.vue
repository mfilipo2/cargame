<template>
  <div>
    <b-row>
      <b-col v-if="errors && errors.length">
        <b-alert v-for="error of errors" :key="error" show variant="danger">{{error}}</b-alert>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <b-button type="reset" block variant="secondary" class="pull-right" @click.prevent="cancel">Cancel</b-button>
      </b-col>
      <b-col>
        <b-button type="submit" block variant="warning" class="pull-right" @click.prevent="deleteCarFromGame"><i class="fas fa-ban"></i> Remove</b-button>
      </b-col>
    </b-row>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import {errorMixin} from '@/mixins/errorMixin'

  export default {
    name: 'GameRemoveCar',
    props: {
      gameId: {
        type: [Number],
        required: true
      },
      carName: {
        type: [String],
        required: true
      }
    },
    data() {
      return {
      }
    },
    methods: {
      deleteCarFromGame() {
        this.clearErrors();
        let uri = '/games/' + this.gameId + '/cars/' + this.carName;
        axiosClient.delete(uri)
        .then(response => {
          this.errors = [];
          this.$emit('removed');
        })
        .catch(e => {
          this.addErrorFromResponse(e.response);
        });
      },
      cancel() {
        this.clearErrors();
        this.$emit('cancel');
      }
    },
    mixins: [
      errorMixin
    ]
  }
</script>

<style lang="scss" scoped>
</style>
