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
        <b-button type="submit" block variant="warning" class="pull-right" @click.prevent="deleteCar"><i class="fas fa-trash"></i> Delete</b-button>
      </b-col>
    </b-row>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import {errorMixin} from '@/mixins/errorMixin'

  export default {
    name: 'CarDelete',
    props: {
      name: {
        type: [String],
        required: true
      }
    },
    data() {
      return {}
    },
    methods: {
      deleteCar() {
        this.clearErrors();

        axiosClient.delete('/cars/' + this.name)
        .then(response => {
          this.$emit('deleted');
        }).catch(e => {
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
