<template>
  <div>
    <b-button-group id="form-buttons">
      <b-btn v-b-modal.addCar variant="primary"><i class="fas fa-plus"></i> Add new car</b-btn>
    </b-button-group>

    <b-table responsive striped hover :items="cars" :fields="fields">
      <template slot="type" slot-scope="row">
        <span v-if="row.item.type == 'NORMAL'"><i class="fas fa-car"></i> Normal</span>
        <span v-if="row.item.type == 'MONSTER_TRUCK'"><i class="fas fa-truck"></i> Monster truck</span>
        <span v-if="row.item.type == 'RACER'"><i class="fab fa-phoenix-framework"></i> Racer</span>
      </template>

      <template slot="crashed" slot-scope="row">
        <i v-if="row.item.crashed" class="far fa-check-square"></i>
        <i v-if="!row.item.crashed" class="far fa-square"></i>
      </template>

      <template slot="inGame" slot-scope="row">
        <i v-if="row.item.inGame" class="far fa-check-square"></i>
        <i v-if="!row.item.inGame" class="far fa-square"></i>
      </template>

      <template slot="options" slot-scope="row">
        <i class="fas option fa-trash-alt" to="cars" @click="deleteCar(row.item.name)" v-b-tooltip.hover title="Delete car"></i>
        <i v-if="row.item.crashed" class="fas option fa-wrench" to="cars" @click="repairCar(row.item.name)" v-b-tooltip.hover title="Repair car"></i>
        <i class="fas option fa-map-marker-alt" to="cars" @click="addCarToGame(row.item.name)" v-b-tooltip.hover title="Add car to game"></i>

      </template>
    </b-table>

    <!-- Add car modal -->
    <b-modal
      ref="addCarModal"
      id="addCar"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Add new car">
      <CarForm @carAdded="carAdded" />
    </b-modal>

    <!-- Delete car modal -->
    <b-modal
      ref="deleteCarModal"
      id="deleteCar"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Are you sure?">
      <CarDelete @cancel="closeDeleteModal" @deleted="carDeleted" :name="nameToDelete" />
    </b-modal>

    <!-- Delete car modal -->
    <b-modal
      ref="repairCarModal"
      id="repairModal"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Are you sure?">
      <CarRepair @cancel="closeRepairModal" @repaired="carRepaired" :name="nameToRepair" />
    </b-modal>

    <!-- Add car to game modal -->
    <b-modal
      ref="addCarToGame"
      id="addCarToGameModal"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Add car to game">
      <CarAddToGame @cancel="closeAddCarToGameModal" @added="carAddedToGame" :name="nameToAdd" />
    </b-modal>
  </div>
</template>

<script>
  import {axiosClient} from '@/router/axios-config'
  import CarForm from '@/components/Cars/CarForm'
  import CarDelete from '@/components/Cars/CarDelete'
  import CarRepair from '@/components/Cars/CarRepair'
  import CarAddToGame from '@/components/Cars/CarAddToGame'
  import {errorMixin} from '@/mixins/errorMixin'

  const fields = {
    name: {
      label: 'Name',
      sortable: true
    },
    type: {
      label: 'Type',
      sortable: true
    },
    crashed: {
      label: 'Crashed',
      sortable: true
    },
    inGame: {
        label: 'In game',
        sortable: true
      },
    options: {
      label: 'Options'
    }
  }

  export default {
    name: 'Cars',
    components: {
      CarForm, CarDelete, CarRepair, CarAddToGame
    },
    data() {
      return {
        fields: fields,
        cars: [],
        nameToDelete: '',
        nameToRepair: '',
        nameToAdd: ''
      }
    },
    methods: {
      carAdded() {
        this.$refs.addCarModal.hide();
        this.loadCars();
      },
      loadCars() {
        axiosClient.get('/cars')
        .then(response => {
          this.cars = response.data.data;
        })
        .catch(e => {
          this.addErrorFromResponse(e.response);
        });
      },
      deleteCar(name) {
        this.nameToDelete = name;
        this.$refs.deleteCarModal.show();
      },
      closeDeleteModal() {
        this.nameToDelete = '';
        this.$refs.deleteCarModal.hide();
      },
      carDeleted() {
        this.nameToDelete = '';
        this.loadCars();
        this.$refs.deleteCarModal.hide();
      },
      repairCar(name) {
        this.nameToRepair = name;
        this.$refs.repairCarModal.show();
      },
      closeRepairModal() {
        this.nameToRepair = '';
        this.$refs.repairCarModal.hide();
      },
      carRepaired() {
        this.nameToRepair = '';
        this.loadCars();
        this.$refs.repairCarModal.hide();
      },
      addCarToGame(name) {
        this.nameToAdd = name;
        this.$refs.addCarToGame.show();
      },
      closeAddCarToGameModal() {
        this.nameToAdd = '';
        this.$refs.addCarToGame.hide();
      },
      carAddedToGame() {
        this.nameToAdd = '';
        this.loadCars();
        this.$refs.addCarToGame.hide();
      },
    },
    created: function() {
      this.loadCars();
    },
    mixins: [
      errorMixin
    ]
  }
</script>

<style lang="scss" scoped>
  .option:hover {
    color: red;
    cursor: pointer;
  }

  #form-buttons {
    float: right;
    margin-bottom: 10px;
  }
</style>
