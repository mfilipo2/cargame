package pl.speedapp.cargame.engine.game

import pl.speedapp.cargame.db.enums.CarType
import pl.speedapp.cargame.engine.car.Car
import pl.speedapp.cargame.engine.exception.CarNotFoundInGameException
import pl.speedapp.cargame.engine.exception.GameAlreadyRunning
import pl.speedapp.cargame.engine.exception.GameNotRunningException
import pl.speedapp.cargame.engine.grid.events.Event
import pl.speedapp.cargame.exception.CarIsBeingUsedInGameException
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class GameTest extends Specification {
    def static GAME_DURATION = 30
    def static THREAD_POOL = Executors.newCachedThreadPool()
    def static BACK_IN_HISTORY_DELAY = 10

    def 'new game is started'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        when:
        game.start(THREAD_POOL)

        then:
        game.isStarted()
    }

    def 'starting game failed if game was already started'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        when:
        game.start(THREAD_POOL)

        then:
        game.isStarted()

        when:
        game.start(THREAD_POOL)

        then:
        thrown(GameAlreadyRunning)
        game.isStarted()
    }

    def 'adding car to the not running game failed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        when:
        game.addCar('testCar', CarType.NORMAL, 1, 2)

        then:
        thrown(GameNotRunningException)
        game.getCarsNames().isEmpty()
    }

    @Unroll
    def 'adding car with type #carType to the running game succeed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)
        game.start(THREAD_POOL)

        when:
        Car car = game.addCar('testCar', carType, 1, 2)

        then:
        car
        game.getCarsNames().size() == 1
        game.getCarsNames().contains('testCar')

        where:
        carType << [CarType.NORMAL, CarType.RACER, CarType.MONSTER_TRUCK]
    }

    def 'adding next car with the same name to the running game failed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 1]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)
        game.start(THREAD_POOL)

        when:
        Car car = game.addCar('testCar', CarType.NORMAL, 1, 2)

        then:
        car
        game.getCarsNames().size() == 1
        game.getCarsNames().contains('testCar')

        when:
        Car car2 = game.addCar('testCar', CarType.NORMAL, 2, 2)

        then:
        thrown(CarIsBeingUsedInGameException)
        !car2
        game.getCarsNames().size() == 1
        game.getCarsNames().contains('testCar')
    }

    def 'removing car from the not running game failed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        when:
        game.removeCar('testCar')

        then:
        thrown(GameNotRunningException)
    }

    def 'removing existing car from the running game succeed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        game.start(THREAD_POOL)
        game.addCar('testCar', CarType.NORMAL, 1, 2)

        when:
        game.removeCar('testCar')

        then:
        game.getCarsNames().isEmpty()
    }

    def 'removing non-existing car from the running game failed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        game.start(THREAD_POOL)

        when:
        game.removeCar('testCar')

        then:
        thrown(CarNotFoundInGameException)
        game.getCarsNames().isEmpty()

    }

    def 'only car with given name is removed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 1]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        game.start(THREAD_POOL)
        game.addCar('testCar', CarType.NORMAL, 1, 2)
        game.addCar('testCar2', CarType.NORMAL, 2, 2)

        when:
        game.removeCar('testCar2')

        then:
        game.getCarsNames().size() == 1
        game.getCarsNames().contains('testCar')
    }

    def 'adding car (which was already added and removed before) to the running game succeed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        game.start(THREAD_POOL)
        game.addCar('testCar', CarType.NORMAL, 1, 2)
        game.removeCar('testCar')

        when:
        game.addCar('testCar', CarType.NORMAL, 1, 2)

        then:
        game.getCarsNames().size() == 1
        game.getCarsNames().contains('testCar')
    }

    def 'fetching car names returns proper values'() {
        given:
        int[][] map = [[0, 0],
                       [1, 1]]
        Game game = new Game('testGame', 1L, map, GAME_DURATION, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        game.start(THREAD_POOL)

        when:
        game.addCar('testCar', CarType.NORMAL, 1, 2)

        then:
        game.getCarsNames().size() == 1
        game.getCarsNames().contains('testCar')

        when:
        game.addCar('testCar2', CarType.NORMAL, 2, 2)

        then:
        game.getCarsNames().size() == 2
        game.getCarsNames().contains('testCar')
        game.getCarsNames().contains('testCar2')

        when:
        game.removeCar('testCar')

        then:
        game.getCarsNames().size() == 1
        game.getCarsNames().contains('testCar2')
    }

    def 'game is interrupted when game duration passed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Game game = new Game('testGame', 1L, map, 1, new LinkedBlockingQueue<Event>(), BACK_IN_HISTORY_DELAY)

        when:
        game.start(THREAD_POOL)

        then:
        new PollingConditions(timeout: 1.2, initialDelay: 1, delay: 0.05).eventually {
            assert !game.isStarted()
        }
    }
}
