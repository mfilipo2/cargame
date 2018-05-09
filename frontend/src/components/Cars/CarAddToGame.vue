<template>
  <div>
    <b-row>
      <b-col v-if="errors && errors.length">
        <b-alert v-for="error of errors" :key="error" show variant="danger">{{error}}</b-alert>
      </b-col>
    </b-row>
    <b-row>
      <b-col>

        <b-form-group id="game"
                      label="Game:"
                      label-for="game"
                      description="Select game.">
          <multiselect class="mb-2 mr-sm-2 mb-sm-0"
                       v-model="form.game"
                       :options="games"
                       label="name"
                       id="game"
                       track-by="id">
            <option slot="first" :value="null">Choose...</option>
          </multiselect>
        </b-form-group>

        <b-form-group id="x"
                      label="X:"
                      label-for="x"
                      description="Select X position.">

          <b-form-input id="x"
                        :min="1"
                        required
                        type="number"
                        placeholder="Enter value"
                        v-model="form.x"
                        @focus="$v.form.x.$touch()"
                        :state="!$v.form.x.$error" />
        </b-form-group>

        <b-form-group id="y"
                      label="Y:"
                      label-for="y"
                      description="Select Y position.">

          <b-form-input id="y"
                        :min="1"
                        required
                        type="number"
                        placeholder="Enter value"
                        v-model="form.y"
                        @focus="$v.form.y.$touch()"
                        :state="!$v.form.y.$error" />
        </b-form-group>
      </b-col>
    </b-row>
    <b-row>
      <b-col>
        <b-button type="reset" block variant="secondary" class="pull-right" @click.prevent="cancel">Cancel</b-button>
      </b-col>
      <b-col>
        <b-button type="submit" block variant="warning" class="pull-right" @click.prevent="addCarToGame"><i class="fas fa-map-marker-alt"></i> Add</b-button>
      </b-col>
    </b-row>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import Multiselect from 'vue-multiselect'
  import {validationMixin} from "vuelidate"
  import {required} from "vuelidate/lib/validators"
  import {errorMixin} from '@/mixins/errorMixin'

  export default {
    name: 'CarDelete',
    components: { Multiselect },
    props: {
      name: {
        type: [String],
        required: true
      }
    },
    data() {
      return {
        form: {
          game: null,
          x: null,
          y: null
        },
        games: []
      }
    },
    methods: {
      addCarToGame() {
        this.clearErrors();
        this.$v.form.$touch();
        if(this.form.game) {
          let car = {
            name: this.name,
            x: parseInt(this.form.x),
            y: parseInt(this.form.y)
          };

          if(!this.$v.form.x.$invalid && !this.$v.form.y.$invalid) {
            axiosClient.post('/games/' + this.form.game.id + '/cars', car)
            .then(response => {
              this.$emit('added');
              this.onReset();
            }).
            catch(e => {
              this.addErrorFromResponse(e.response);
            });
          }
        } else {
          this.addError("Choose game!");
        }

      },
      loadGames() {
        axiosClient.get('/games?status=RUNNING')
        .then(response => {
          this.clearErrors();
          this.games = [];

          for(let i=0; i < response.data.data.length; i++) {
            let game = {
              name: response.data.data[i].name,
              id: response.data.data[i].id
            }
            this.games.push(game);
          }
        })
        .catch(e => {
            this.errors.push(e);
        });
      },
      onReset() {
        this.form.game = null;
        this.form.x = null;
        this.form.y = null;
        this.clearErrors();
        this.$v.form.$reset();
      },
      cancel() {
        this.onReset();
        this.$emit('cancel');
      }
    },
    mixins: [
      validationMixin, errorMixin
    ],
      validations: {
      form: {
        name: {
          required
        },
        x: {
          required
        },
        y: {
          required
        }
      }
    },
    created: function() {
      this.onReset();
      this.loadGames();
    }
  }
</script>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
