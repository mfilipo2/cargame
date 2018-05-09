<template>
  <div>
    <b-form @submit="onSubmit" @reset="onReset">
      <b-form-group id="name"
                    label="Map name:"
                    label-for="mapName"
                    description="Map name should be unique.">
        <b-form-input id="mapName"
                      required
                      type="text"
                      placeholder="Enter map name"
                      v-model="form.name"
                      @focus="$v.form.name.$touch()"
                      :state="!$v.form.name.$error">
        </b-form-input>
        <b-form-invalid-feedback  v-if="!$v.form.name.required">
          This is a required field.
        </b-form-invalid-feedback>
      </b-form-group>
      <b-form-group id="file"
                    label="File:"
                    label-for="mapFile"
                    description="CSV file to import.">
        <b-form-file id="mapFile"
                     placeholder="Choose a CSV file..."
                     accept=".csv"
                     v-if="uploadReady"
                     v-model="form.file"
                     @focus="$v.form.file.$touch()"
                     :state="!$v.form.file.$error">
        </b-form-file>
        <b-form-invalid-feedback v-if="$v.form.file.validFile">
          File is required.
        </b-form-invalid-feedback>
      </b-form-group>

    </b-form>

    <div v-if="errors && errors.length">
      <b-alert v-for="error of errors" :key="error" show variant="danger">{{error}}</b-alert>
    </div>

      <b-button type="reset" variant="secondary" class="pull-right" @click="onReset">Clear</b-button>
      <b-button type="submit" variant="primary" class="pull-right" @click="onSubmit"><i class="fas fa-save"></i> Save</b-button>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import {validationMixin} from "vuelidate"
  import {required} from "vuelidate/lib/validators"
  import {validFile} from "@/validators/file"
  import {errorMixin} from '@/mixins/errorMixin'

  export default {
    name: 'MapForm',
    data() {
      return {
        form: {},
        uploadReady: true
      }
    },
    methods: {
      onSubmit() {
        this.$v.form.$touch();
        let data = new FormData();

        data.append('gameMapName', this.form.name);
        data.append('gameMapFile', this.form.file);
        this.clearErrors();

        if(!this.$v.form.name.$invalid && !this.$v.form.file.$invalid) {
          axiosClient.post('/gamemaps', data)
          .then(response => {
            this.form = {};
            this.$emit('mapAdded');
            this.onReset();
          }).catch(e => {
            this.addErrorFromResponse(e.response);
          });
        }
      },
      onReset() {
        this.clearErrors();
        this.$v.form.$reset();
        this.form.name = '';

        this.uploadReady = false
        this.$nextTick(() => {
          this.uploadReady = true;
        });
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
        file: {
          validFile
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
</style>
