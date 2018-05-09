package pl.speedapp.cargame.engine.game

import pl.speedapp.cargame.db.enums.CarMoveType
import pl.speedapp.cargame.db.enums.CarType
import pl.speedapp.cargame.db.model.GameMap
import pl.speedapp.cargame.engine.car.CarHistoryMoveEvent
import pl.speedapp.cargame.engine.exception.CarNotFoundInGameException
import pl.speedapp.cargame.engine.exception.PositionAlreadyTakenException
import pl.speedapp.cargame.engine.exception.WrongDistanceValueException
import pl.speedapp.cargame.engine.grid.movement.Direction
import pl.speedapp.cargame.exception.CarIsBeingUsedInGameException
import pl.speedapp.cargame.exception.CarIsNotBeingUsedInAnyGameException
import pl.speedapp.cargame.exception.GameNotActiveException
import pl.speedapp.cargame.service.GameEventsService
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

class GameManagerTest extends Specification {
    def static GAME_DURATION = 30
    def static BACK_IN_HISTORY_DELAY = 200
    def static MOVE_LEFT = CarHistoryMoveEvent.builder().moveType(CarMoveType.TURN_LEFT).build()
    def static MOVE_RIGHT = CarHistoryMoveEvent.builder().moveType(CarMoveType.TURN_RIGHT).build()
    def static MOVE_FORWARD = CarHistoryMoveEvent.builder().moveType(CarMoveType.FORWARD).distance(1).build()
    def static MOVE_FORWARD_2 = CarHistoryMoveEvent.builder().moveType(CarMoveType.FORWARD).distance(2).build()

    def 'adding and starting game succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()

        when:
        gameManager.addAndStartGame(game)

        then:
        gameManager.checkIfGameWithNameIsRunning('testGame')
        gameManager.getGameById(game.getId()).isPresent()
        gameManager.getGameById(game.getId()).get().isStarted()
    }

    @Unroll
    def 'method for checking if game is running returns proper results - #description'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()

        when:
        gameManager.addAndStartGame(game)

        then:
        gameManager.checkIfGameWithNameIsRunning(gameName) == expectedResult

        where:
        gameName    | expectedResult | description
        'testGame'  | true           | 'existing game'
        'testGame2' | false          | 'non-existing game'
    }

    def 'all games ids returned'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        pl.speedapp.cargame.db.model.Game game2 = getGameDbObject(2L, 2)

        when:
        gameManager.addAndStartGame(game)

        then:
        gameManager.getGamesIds().size() == 1
        gameManager.getGamesIds().contains(game.id)

        when:
        gameManager.addAndStartGame(game2)

        then:
        gameManager.getGamesIds().size() == 2
        gameManager.getGamesIds().containsAll([game.id, game2.id])
    }

    def 'getting existing game by id succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()

        when:
        gameManager.addAndStartGame(game)

        then:
        gameManager.getGameById(game.getId()).isPresent()
    }

    def 'getting non-existing game by id returns nothing'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()

        when:
        def resultGame = gameManager.getGameById(1L)

        then:
        !resultGame.isPresent()
    }

    def 'adding car to the game succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)

        when:
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 1
        gameManager.getCarNamesInGame(game.getId()).contains('testCar')

        when:
        gameManager.addCarToTheGame(game.getId(), 'testCar2', CarType.RACER, 2, 2)

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 2
        gameManager.getCarNamesInGame(game.getId()).containsAll(['testCar', 'testCar2'])
    }

    def 'adding car to not existing game failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()

        when:
        gameManager.addCarToTheGame(1L, 'testCar', CarType.NORMAL, 1, 2)

        then:
        thrown(GameNotActiveException)
        gameManager.getCarNamesInGame(1L).size() == 0
    }

    def 'adding car on the same position failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)

        when:
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)
        gameManager.addCarToTheGame(game.getId(), 'testCar2', CarType.RACER, 1, 2)

        then:
        thrown(PositionAlreadyTakenException)
        gameManager.getCarNamesInGame(game.getId()).size() == 1
        gameManager.getCarNamesInGame(game.getId()).containsAll(['testCar'])
    }

    def 'adding second car with the same name to the other game failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        pl.speedapp.cargame.db.model.Game game2 = getGameDbObject(2l, 2)
        gameManager.addAndStartGame(game)
        gameManager.addAndStartGame(game2)

        when:
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)
        gameManager.addCarToTheGame(game2.getId(), 'testCar', CarType.RACER, 2, 2)

        then:
        thrown(CarIsBeingUsedInGameException)
        gameManager.getCarNamesInGame(game.getId()).size() == 1
        gameManager.getCarNamesInGame(game.getId()).containsAll(['testCar'])
        gameManager.getCarNamesInGame(game2.getId()).size() == 0
    }

    def 'adding the same car to the new game when the first game is finished succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = 1
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        pl.speedapp.cargame.db.model.Game game2 = getGameDbObject(2L, 2)
        gameManager.addAndStartGame(game)

        when:
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        then:
        new PollingConditions(timeout: 1.5, delay: 0.02, initialDelay: 1).eventually {
            assert !gameManager.getGameById(game.getId()).isPresent()
        }

        when:
        gameManager.addAndStartGame(game2)
        gameManager.addCarToTheGame(game2.getId(), 'testCar', CarType.NORMAL, 1, 2)

        then:
        gameManager.getCarNamesInGame(game2.getId()).size() == 1
        gameManager.getCarNamesInGame(game2.getId()).contains('testCar')
    }

    def 'removing existing car from the existing game succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)
        gameManager.addCarToTheGame(game.getId(), 'testCar2', CarType.RACER, 2, 2)

        when:
        gameManager.removeCar(game.getId(), 'testCar')

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 1
        gameManager.getCarNamesInGame(game.getId()).contains('testCar2')

        when:
        gameManager.removeCar(game.getId(), 'testCar2')

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 0
    }

    def 'removing existing car from the non-existing game failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject(1L)
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.removeCar(2L, 'testCar')

        then:
        thrown(CarNotFoundInGameException)
        gameManager.getCarNamesInGame(game.getId()).size() == 1
        gameManager.getCarNamesInGame(game.getId()).contains('testCar')
    }

    def 'removing non-existing car from the existing game failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        pl.speedapp.cargame.db.model.Game game2 = getGameDbObject(2L, 2)
        gameManager.addAndStartGame(game)
        gameManager.addAndStartGame(game2)
        gameManager.addCarToTheGame(game2.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.removeCar(game.getId(), 'testCar')

        then:
        thrown(CarNotFoundInGameException)
        gameManager.getCarNamesInGame(game.getId()).size() == 0
        gameManager.getCarNamesInGame(game2.getId()).size() == 1
        gameManager.getCarNamesInGame(game2.getId()).contains('testCar')
    }

    def 'removing car from the game and adding it again on the sam position succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.removeCar(game.getId(), 'testCar')

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 0

        when:
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 1
        gameManager.getCarNamesInGame(game.getId()).contains('testCar')
    }

    def 'removing car from one game and adding to the next game succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        pl.speedapp.cargame.db.model.Game game2 = getGameDbObject(2L, 2)
        gameManager.addAndStartGame(game)
        gameManager.addAndStartGame(game2)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.removeCar(game.getId(), 'testCar')

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 0
        gameManager.getCarNamesInGame(game2.getId()).size() == 0

        when:
        gameManager.addCarToTheGame(game2.getId(), 'testCar', CarType.NORMAL, 1, 2)

        then:
        gameManager.getCarNamesInGame(game.getId()).size() == 0
        gameManager.getCarNamesInGame(game2.getId()).size() == 1
        gameManager.getCarNamesInGame(game2.getId()).contains('testCar')
    }

    def 'getting game by car name returns proper value'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when: 'getting game by existing car name'
        def gameOptional = gameManager.getGameByCarName('testCar')

        then: 'correct game is returned'
        gameOptional.isPresent()
        gameOptional.get().getGameId() == game.getId()

        when: 'getting game by non-existing car name'
        def gameOptional2 = gameManager.getGameByCarName('testCar2')

        then: 'nothing is returned'
        !gameOptional2.isPresent()
    }

    def 'turning right existing car succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        def resultGameId = gameManager.turnRightCar('testCar')

        then:
        resultGameId == game.getId()
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.direction == Direction.EAST
        }
    }

    def 'turning right non-existing car failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.turnRightCar('testCar2')

        then:
        thrown(CarIsNotBeingUsedInAnyGameException)
    }

    def 'turning left existing car succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        def resultGameId = gameManager.turnLeftCar('testCar')

        then:
        resultGameId == game.getId()
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.direction == Direction.WEST
        }
    }

    def 'turning left non-existing car failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.turnLeftCar('testCar2')

        then:
        thrown(CarIsNotBeingUsedInAnyGameException)
    }

    def 'moving forward existing car succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        def resultGameId = gameManager.moveCarForward('testCar', 1)

        then:
        resultGameId == game.getId()
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 0
            assert carStatus.y == 0
        }
    }

    def 'moving forward non-existing car failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.moveCarForward('testCar2', 1)

        then:
        thrown(CarIsNotBeingUsedInAnyGameException)
    }

    def 'moving forward existing car with wrong distance failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.NORMAL, 1, 2)

        when:
        gameManager.moveCarForward('testCar', 0)

        then:
        thrown(WrongDistanceValueException)
    }

    def 'moving forward existing car out of the map failed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.RACER, 1, 2)

        when:
        def resultGameId = gameManager.moveCarForward('testCar', 2)

        then:
        resultGameId == game.getId()
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            assert gameManager.getCarNamesInGame(resultGameId).isEmpty()
        }
    }

    @Unroll
    def 'in case of collision between two cars (#carType1 and #carType2) driving straight on themselves survives #result'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', carType1, 1, 2)
        gameManager.addCarToTheGame(game.getId(), 'testCar2', carType2, 2, 2)

        when:
        gameManager.turnRightCar('testCar')
        gameManager.turnLeftCar('testCar2')

        then:
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            def carStatus1 = gameManager.getRunningGameDto(game.getId()).cars.find({
                it.name == 'testCar'
            }).currentStatus
            def carStatus2 = gameManager.getRunningGameDto(game.getId()).cars.find({
                it.name == 'testCar2'
            }).currentStatus
            assert carStatus1.direction == Direction.EAST
            assert carStatus2.direction == Direction.WEST
        }

        when:
        gameManager.moveCarForward('testCar', 1)
        gameManager.moveCarForward('testCar2', 1)

        then:
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            assert gameManager.getCarNamesInGame(game.getId()) == result
        }

        where:
        carType1              | carType2              | result
        CarType.NORMAL        | CarType.NORMAL        | []
        CarType.NORMAL        | CarType.RACER         | []
        CarType.NORMAL        | CarType.MONSTER_TRUCK | ['testCar2']
        CarType.RACER         | CarType.RACER         | []
        CarType.RACER         | CarType.MONSTER_TRUCK | ['testCar2']
        CarType.MONSTER_TRUCK | CarType.MONSTER_TRUCK | []
    }

    @Unroll
    def 'in case of collision between two cars (#carType1 and #carType2) driving on the same position survives #result'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        int[][] map = [[0, 0, 0],
                       [0, 0, 0],
                       [1, 1, 1]]
        pl.speedapp.cargame.db.model.Game game = getGameDbObject(1L, '', map)
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', carType1, 1, 3)
        gameManager.addCarToTheGame(game.getId(), 'testCar2', carType2, 3, 3)

        when:
        gameManager.turnRightCar('testCar')
        gameManager.turnLeftCar('testCar2')

        then:
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            def carStatus1 = gameManager.getRunningGameDto(game.getId()).cars.find({
                it.name == 'testCar'
            }).currentStatus
            def carStatus2 = gameManager.getRunningGameDto(game.getId()).cars.find({
                it.name == 'testCar2'
            }).currentStatus
            assert carStatus1.direction == Direction.EAST
            assert carStatus2.direction == Direction.WEST
        }

        when:
        gameManager.moveCarForward('testCar', 1)
        gameManager.moveCarForward('testCar2', 1)

        then:
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            assert gameManager.getCarNamesInGame(game.getId()) == result
        }

        where:
        carType1              | carType2              | result
        CarType.NORMAL        | CarType.NORMAL        | []
        CarType.NORMAL        | CarType.RACER         | []
        CarType.NORMAL        | CarType.MONSTER_TRUCK | ['testCar2']
        CarType.RACER         | CarType.RACER         | []
        CarType.RACER         | CarType.MONSTER_TRUCK | ['testCar2']
        CarType.MONSTER_TRUCK | CarType.MONSTER_TRUCK | []
    }

    @Unroll
    def 'in case of collision between one riding car (#carType1) and one static car (#carType2) survives #result'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        pl.speedapp.cargame.db.model.Game game = getGameDbObject()
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', carType1, 1, 2)
        gameManager.addCarToTheGame(game.getId(), 'testCar2', carType2, 2, 2)

        when:
        gameManager.turnRightCar('testCar')

        then:
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            def carStatus1 = gameManager.getRunningGameDto(game.getId()).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus1.direction == Direction.EAST
        }

        when:
        gameManager.moveCarForward('testCar', 1)

        then:
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            assert gameManager.getCarNamesInGame(game.getId()) == result
        }

        where:
        carType1              | carType2              | result
        CarType.NORMAL        | CarType.NORMAL        | []
        CarType.NORMAL        | CarType.RACER         | []
        CarType.NORMAL        | CarType.MONSTER_TRUCK | ['testCar2']
        CarType.RACER         | CarType.RACER         | []
        CarType.RACER         | CarType.MONSTER_TRUCK | ['testCar2']
        CarType.MONSTER_TRUCK | CarType.MONSTER_TRUCK | []
    }

    @Unroll
    def 'in case of collision with the wall the car (type: #carType) is destroyed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        int[][] map = [[0, 0],
                       [1, 0]]
        pl.speedapp.cargame.db.model.Game game = getGameDbObject(1L, '', map)
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', carType, 1, 2)

        when:
        gameManager.moveCarForward('testCar', 1)

        then:
        new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
            assert gameManager.getCarNamesInGame(game.getId()).isEmpty()
        }

        where:
        carType << [CarType.NORMAL, CarType.RACER, CarType.MONSTER_TRUCK]
    }

    @Unroll
    def 'riding RACER with distance 2 over the #overPositionDescription and finishing on the #finishPositionDescription #result'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        int[][] map = [[finishPosition, 0, 0],
                       [overPosition, 0, 0],
                       [1, 0, 0]]
        pl.speedapp.cargame.db.model.Game game = getGameDbObject(1L, '', map)
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.RACER, 1, 3)
        if (overPositionDescription == 'car') {
            gameManager.addCarToTheGame(game.getId(), 'overCar', CarType.NORMAL, 1, 2)
        }
        if (finishPositionDescription == 'car') {
            gameManager.addCarToTheGame(game.getId(), 'finishCar', CarType.NORMAL, 1, 1)
        }

        when:
        def resultGameId = gameManager.moveCarForward('testCar', 2)

        then:
        resultGameId == game.getId()
        if (result == 'succeed') {
            new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
                def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                    it.name == 'testCar'
                }).currentStatus
                assert carStatus.x == 0
                assert carStatus.y == 0
            }
        } else {
            new PollingConditions(timeout: 0.5, delay: 0.02).eventually {
                gameManager.getCarNamesInGame(resultGameId).isEmpty()
            }
        }


        where:
        overPosition | overPositionDescription | finishPosition | finishPositionDescription | result
        1            | 'valid position'        | 1              | 'valid position'          | 'succeed'
        0            | 'wall'                  | 1              | 'valid position'          | 'succeed'
        1            | 'car'                   | 1              | 'valid position'          | 'succeed'
        1            | 'valid position'        | 0              | 'wall'                    | 'results in collision'
        1            | 'valid position'        | 1              | 'car'                     | 'results in collision'
    }

    def 'back movements without move forward events succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        def timeout = 2 * BACK_IN_HISTORY_DELAY / 1000
        def delay = timeout / 20
        def initialDelay = timeout / 4
        int[][] map = [[1, 1, 0],
                       [1, 0, 0],
                       [1, 1, 1]]
        List<CarHistoryMoveEvent> moves = [MOVE_LEFT, MOVE_RIGHT, MOVE_RIGHT, MOVE_RIGHT, MOVE_LEFT]
        pl.speedapp.cargame.db.model.Game game = getGameDbObject(1L, '', map)
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.RACER, 2, 1)

        when:
        def resultGameId = gameManager.backInHistory('testCar', game.getName(), moves)

        then: 'turn left move reversed'
        new PollingConditions(timeout: timeout, delay: delay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.EAST
        }

        then: 'turn right move reversed'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.NORTH
        }

        then: 'turn right move reversed'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.WEST
        }

        then: 'turn right move reversed'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.SOUTH
        }

        then: 'turn left move reversed'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.WEST
        }
    }

    def 'back movements with move forward events succeed'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        def timeout = 2 * BACK_IN_HISTORY_DELAY / 1000
        def delay = timeout / 20
        def initialDelay = timeout / 4
        int[][] map = [[1, 1, 0],
                       [1, 0, 0],
                       [1, 1, 1]]
        List<CarHistoryMoveEvent> moves = [MOVE_LEFT, MOVE_FORWARD, MOVE_RIGHT, MOVE_FORWARD_2]
        pl.speedapp.cargame.db.model.Game game = getGameDbObject(1L, '', map)
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.RACER, 2, 1)

        when:
        def resultGameId = gameManager.backInHistory('testCar', game.getName(), moves)

        then: 'turn left move reversed'
        new PollingConditions(timeout: timeout, delay: delay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.EAST
        }

        then: 'car turned around'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.WEST
        }

        then: 'move forward'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 0
            assert carStatus.y == 0
            assert carStatus.direction == Direction.WEST
        }

        then: 'turn right move reversed'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 0
            assert carStatus.y == 0
            assert carStatus.direction == Direction.SOUTH
        }

        then: 'move forward with distance 2'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 0
            assert carStatus.y == 2
            assert carStatus.direction == Direction.SOUTH
        }

        then: 'car turned around'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 0
            assert carStatus.y == 2
            assert carStatus.direction == Direction.NORTH
        }
    }


    def 'collision while getting back considered'() {
        given:
        GameEventsService gameEventsService = Mock()
        GameManager gameManager = new GameManager(gameEventsService)
        gameManager.gameDuration = GAME_DURATION
        gameManager.backInHistoryDelay = BACK_IN_HISTORY_DELAY
        gameManager.init()
        def timeout = 2 * BACK_IN_HISTORY_DELAY / 1000
        def delay = timeout / 20
        def initialDelay = timeout / 4
        int[][] map = [[1, 1, 0],
                       [1, 0, 0],
                       [1, 1, 1]]
        List<CarHistoryMoveEvent> moves = [MOVE_LEFT, MOVE_FORWARD, MOVE_RIGHT, MOVE_FORWARD, MOVE_FORWARD]
        pl.speedapp.cargame.db.model.Game game = getGameDbObject(1L, '', map)
        gameManager.addAndStartGame(game)
        gameManager.addCarToTheGame(game.getId(), 'testCar', CarType.RACER, 2, 1)
        gameManager.addCarToTheGame(game.getId(), 'testCar2', CarType.MONSTER_TRUCK, 1, 1)

        when:
        def resultGameId = gameManager.backInHistory('testCar', game.getName(), moves)

        then: 'turn left move reversed'
        new PollingConditions(timeout: timeout, delay: delay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.EAST
        }

        then: 'car turned around'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: initialDelay).eventually {
            def carStatus = gameManager.getRunningGameDto(resultGameId).cars.find({
                it.name == 'testCar'
            }).currentStatus
            assert carStatus.x == 1
            assert carStatus.y == 0
            assert carStatus.direction == Direction.WEST
        }

        then: 'car moves forward and have collision with the second car'
        new PollingConditions(timeout: timeout, delay: delay, initialDelay: 2 * initialDelay).eventually {
            gameManager.getCarNamesInGame(resultGameId).size() == 1
            !gameManager.getCarNamesInGame(resultGameId).contains('testCar')
            gameManager.getCarNamesInGame(resultGameId).contains('testCar2')
        }
    }

    private static def getGameDbObject(Long id = 1L, def nameSuffix = '', int[][] map = [[1, 0], [1, 1]]) {
        GameMap gameMap = GameMap.builder()
                .id(id)
                .name("testGameMap$nameSuffix")
                .mapSize(map.length)
                .roads(map)
                .build()
        pl.speedapp.cargame.db.model.Game game = pl.speedapp.cargame.db.model.Game.builder()
                .id(id)
                .name("testGame$nameSuffix")
                .map(gameMap)
                .build()
        return game
    }
}
