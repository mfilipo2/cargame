<template>
  <div>
    <b-form @submit="onSubmit" @reset="onReset">
      <b-form-group id="map"
                    label="Map:"
                    label-for="map"
                    description="Select map.">

        <multiselect class="mb-2 mr-sm-2 mb-sm-0"
                       v-model="form.map"
                       :options="maps"
                       id="inlineFormCustomSelectPref">
          <option slot="first" :value="null">Choose...</option>
        </multiselect>

      </b-form-group>
    </b-form>

    <div v-if="errors && errors.length">
      <b-alert v-for="error of errors" :key="error" show variant="danger">{{error}}</b-alert>
    </div>

    <b-button type="reset" variant="secondary" class="pull-right" @click.prevent="onReset">Clear</b-button>
    <b-button type="submit" variant="primary" class="pull-right" @click.prevent="onSubmit"><i class="fas fa-save"></i> Save</b-button>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import Multiselect from 'vue-multiselect'
  import {errorMixin} from '@/mixins/errorMixin'

  export default {
    name: 'GameForm',
    components: { Multiselect },
    data() {
      return {
        form: {
          map: null
        },
        maps: []
      }
    },
    methods: {
      onSubmit() {
        let data = {};

        data.name = this.form.map;
        this.clearErrors();

        if(data.name) {
          axiosClient.post('/games', data)
            .then(response => {
              this.form = {};
              this.$emit('gameAdded');
              this.onReset();
            }).catch(e => {
              this.addErrorFromResponse(e.response);
            });
        } else {
          this.addError('Select map first!')
        }
      },
      onReset() {
        this.form.map = null;
        this.clearErrors();
        this.loadAvailableMaps();
      },
      loadAvailableMaps() {
        axiosClient.get('/gamemaps')
        .then(response => {
          let data = response.data.data;
          for(let i=0; i < data.length; i++) {
            data[i] = data[i].name;
          }
          this.maps = data;
        })
        .catch(e => {
          this.addErrorFromResponse(e.response);
        });
      },
    },
    created: function() {
      this.loadAvailableMaps();
    },
    mixins: [
      errorMixin
    ]
  }
</script>

<style lang="scss" scoped>
  #form-buttons {
    float: right;
  }
  .error {
    border: 1px solid red;
  }
  .valid {
    border: 1px solid red;
  }
</style>
<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

