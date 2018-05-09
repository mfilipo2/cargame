<template>
  <div>
    <b-button-group id="form-buttons">
      <b-btn v-b-modal.addMap variant="primary"><i class="fas fa-location-arrow"></i> Add new map</b-btn>
    </b-button-group>

    <b-table responsive striped hover :items="maps" :fields="fields">
      <template slot="name" slot-scope="row">
        <span class="mapName" :id="'tooltipButton-' + row.item.name">{{row.item.name}}</span>
        <b-tooltip placement="left" :target="'tooltipButton-' + row.item.name" triggers="hover">
          <div class="gridContainer">
            <Grid :gameId="''"
              :carName="''"
              :walls="row.item.walls"
              :cars="[]"
              :size="row.item.size"
              :width="100" />
          </div>
        </b-tooltip>
      </template>

      <template slot="size" slot-scope="row">{{row.item.size | square}}</template>

      <template slot="used" slot-scope="row">
        <i v-if="row.item.used" class="far fa-check-square"></i>
        <i v-if="!row.item.used" class="far fa-square"></i>
      </template>

      <template slot="options" slot-scope="row">
        <i v-if="!row.item.used" class="fas option fa-trash-alt" to="maps" @click="deleteMap(row.item.id)" v-b-tooltip.hover title="Remove map"></i>
      </template>
    </b-table>

    <!-- Add game modal -->
    <b-modal
      ref="addMapModal"
      id="addMap"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Add new map">
      <MapForm @mapAdded="mapAdded" />
    </b-modal>

    <!-- Delete game modal -->
    <b-modal
      ref="deleteMapModal"
      id="deleteMap"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Are you sure?">
      <MapDelete v-if="idToDelete" @cancel="closeDeleteModal" @deleted="mapDeleted" :id="idToDelete" />
    </b-modal>


  </div>
</template>

<script>
  import MapForm from '@/components/Maps/MapForm'
  import MapDelete from '@/components/Maps/MapDelete'
  import {axiosClient} from '@/router/axios-config'
  import Grid from '@/components/RunningGame/Grid'

  const fields = {
    id: {
      label: 'ID',
      sortable: true
    },
    name: {
      label: 'Name',
      sortable: true
    },
    used: {
      label: 'Used',
      sortable: true
    },
    size: {
      label: 'Size',
      sortable: true
    },
    options: {
      label: 'Options'
    }
  }

  export default {
    name: 'Maps',
    data() {
      return {
        fields: fields,
        idToDelete: null,
        maps: []
      }
    },
    methods: {
      loadMaps() {
        axiosClient.get('/gamemaps')
        .then(response => {
          this.maps = response.data.data;
        })
        .catch(e => {
          this.errors.push(e);
        });
      },
      deleteMap(id) {
        this.idToDelete = id;
        this.$refs.deleteMapModal.show();
      },
      mapAdded() {
        this.loadMaps();
        this.$refs.addMapModal.hide();
      },
      mapDeleted() {
        this.loadMaps();
        this.$refs.deleteMapModal.hide();
      },
      closeDeleteModal() {
        this.$refs.deleteMapModal.hide();
      }
    },
    components: {
      MapForm, MapDelete, Grid
    },
    filters: {
      square: function(value) {
        return '' + value + 'x' + value;
      },
      formatDate: function (value) {
        if (value) {
          return moment(String(value)).format('DD.MM.YYYY HH:mm:ss:sss')
        }
      }
    },
    created: function() {
      this.loadMaps();
    }
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

  span.mapName {
    color: #007bff;
    text-decoration: underline;
  }
  span.mapName:hover {
    cursor: pointer;
    color: red;
  }
  div.gridContainer {
    position: relative;
    width: 100px;
    height: 100px;
  }
</style>

