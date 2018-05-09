<template>
  <div>
    <b-button-group id="form-buttons">
      <b-btn v-b-modal.addGame variant="primary"><i class="fas fa-rocket"></i> Run new game</b-btn>
    </b-button-group>

    <h3>Running games</h3>
    <GamesTable :status="'RUNNING'" ref="runningGames"></GamesTable>

    <h3>Completed games</h3>
    <GamesTable :status="'COMPLETED'" ref="completedGames"></GamesTable>

    <!-- Add game modal -->
    <b-modal
      ref="addGameModal"
      id="addGame"
      header-bg-variant="dark"
      header-text-variant="light"
      body-bg-variant="light"
      body-text-variant="dark"
      hide-footer
      title="Start new game">
      <GameForm @gameAdded="gameAdded()"></GameForm>
    </b-modal>
  </div>
</template>

<script>
  import GameForm from '@/components/Games/GameForm'
  import GamesTable from '@/components/Games/GamesTable'

  export default {
    components: {
      GameForm, GamesTable
    },
    name: 'Maps',
    data() {
      return {
      }
    },
    methods: {
      gameAdded() {
        this.refreshTables();
        this.$refs.addGameModal.hide();
      },
      refreshTables() {
        this.$refs.runningGames.loadGames();
        this.$refs.completedGames.loadGames();
        this.$refs.addGameModal.hide();
      }
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
</style>

