<template>
  <div>
    <b-form @submit="onSubmit" @reset="onReset">
      <b-form-group id="carName"
                    label="Car name:"
                    label-for="carName"
                    description="Car name must be unique.">

        <b-form-input id="carName"
                      required
                      type="text"
                      placeholder="Enter car name"
                      v-model="form.name"
                      @focus="$v.form.name.$touch()"
                      :state="!$v.form.name.$error" />

      </b-form-group>

      <b-form-group id="carType"
                    label="Car type:"
                    label-for="carType"
                    description="Select car type">
        <b-form-radio-group
          id="carType"
          v-model="form.type"
          name="carType"
          required>
          <b-form-radio value="NORMAL"><i class="fas fa-car"></i> Normal car</b-form-radio>
          <b-form-radio value="MONSTER_TRUCK"><i class="fas fa-truck"></i> Monster truck</b-form-radio>
          <b-form-radio value="RACER"><i class="fab fa-phoenix-framework"></i> Racer car</b-form-radio>
        </b-form-radio-group>
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
  import {validationMixin} from "vuelidate"
  import {required} from "vuelidate/lib/validators"
  import {errorMixin} from '@/mixins/errorMixin'

  export default {
    name: 'CarForm',
    data() {
      return {
        form: {
          name: '',
          type: 'NORMAL'
        },
        errors: []
      }
    },
    methods: {
      onSubmit() {
        this.$v.form.$touch();
        this.clearErrors();

        if(!this.$v.form.name.$invalid) {
          axiosClient.post('/cars', this.form)
          .then(response => {
            this.form = {};
            this.$emit('carAdded');
            this.onReset();
          }).catch(e => {
            this.addErrorFromResponse(e.response);
          });
        }
      },
      onReset() {
        this.clearErrors();
        this.$v.form.$reset();
        this.form = {
          name: '',
          type: 'NORMAL'
        };
      },
    },
    mixins: [
      validationMixin, errorMixin
    ],
    validations: {
      form: {
        name: {
          required
        }
      }
    }
  }
</script>
