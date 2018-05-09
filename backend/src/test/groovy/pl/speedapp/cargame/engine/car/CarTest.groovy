package pl.speedapp.cargame.engine.car

import pl.speedapp.cargame.db.enums.CarType
import pl.speedapp.cargame.engine.exception.CarAlreadyStartedException
import pl.speedapp.cargame.engine.grid.Grid
import pl.speedapp.cargame.engine.grid.commands.MoveForward
import pl.speedapp.cargame.engine.grid.commands.TurnLeft
import pl.speedapp.cargame.engine.grid.commands.TurnRight
import pl.speedapp.cargame.engine.grid.events.Event
import pl.speedapp.cargame.engine.grid.events.GridObjectDestroyed
import pl.speedapp.cargame.engine.grid.movement.Direction
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class CarTest extends Specification {
    def static CAR_ENGINES = Executors.newCachedThreadPool()
    def static BACK_IN_HISTORY_DELAY = 10

    def 'starting engine of the new car succeed'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', CarType.NORMAL, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)

        when:
        car.startEngine(CAR_ENGINES)

        then:
        car.started.get()
    }

    def 'starting engine of the already running car failed'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', CarType.NORMAL, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)
        car.startEngine(CAR_ENGINES)

        when:
        car.startEngine(CAR_ENGINES)

        then:
        thrown(CarAlreadyStartedException)
        car.started.get()
    }

    def 'stopping engine succeed'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', CarType.NORMAL, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)
        car.startEngine(CAR_ENGINES)

        when:
        car.handle(CarCommand.builder().carName('testCar').type(CarCommandType.STOP_ENGINE).build())
        Thread.sleep(10)

        then:
        !car.started.get()
    }

    def 'turning left the car results with turning left the object on the grid'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', CarType.NORMAL, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)
        car.startEngine(CAR_ENGINES)

        when:
        car.handle(CarCommand.builder().carName('testCar').type(CarCommandType.TURN_LEFT).build())
        Thread.sleep(10)

        then:
        1 * grid.handle(_ as TurnLeft)
    }

    def 'turning right the car results with turning right the object on the grid'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', CarType.NORMAL, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)
        car.startEngine(CAR_ENGINES)

        when:
        car.handle(CarCommand.builder().carName('testCar').type(CarCommandType.TURN_RIGHT).build())
        Thread.sleep(10)

        then:
        1 * grid.handle(_ as TurnRight)
    }

    @Unroll
    def 'moving forward the car (type: #carType, with distance: #distance) results with moving forward the object on the grid'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', carType, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)
        car.startEngine(CAR_ENGINES)

        when:
        car.handle(CarCommand.builder()
                .carName('testCar')
                .type(CarCommandType.MOVE_FORWARD)
                .commandProperties(Collections.singletonMap(CarCommandProperty.DISTANCE, distance))
                .build())
        Thread.sleep(10)

        then:
        1 * grid.handle(_ as MoveForward)

        where:
        carType               | distance
        CarType.NORMAL        | null
        CarType.NORMAL        | 1
        CarType.NORMAL        | 2
        CarType.RACER         | null
        CarType.RACER         | 1
        CarType.RACER         | 2
        CarType.MONSTER_TRUCK | null
        CarType.MONSTER_TRUCK | 1
        CarType.MONSTER_TRUCK | 2
    }

    def 'engine stopped when the car is crashed'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', CarType.NORMAL, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)
        car.startEngine(CAR_ENGINES)

        List<Event> destroyedEvent = [new GridObjectDestroyed('testCar', 1, 1, Direction.NORTH)]
        grid.handle(_ as MoveForward) >> destroyedEvent

        when:
        car.handle(CarCommand.builder().carName('testCar').type(CarCommandType.MOVE_FORWARD).build())

        then:
        new PollingConditions(timeout: 0.1, delay: 0.01).eventually {
            assert !car.started.get()
        }
    }

    def 'engine stopped when the car is destroyed'() {
        given:
        Grid grid = Mock(Grid)
        Car car = new Car('testCar', CarType.NORMAL, grid, new LinkedBlockingQueue<>(), BACK_IN_HISTORY_DELAY)
        car.startEngine(CAR_ENGINES)

        when:
        car.handle(CarCommand.builder().carName('testCar').type(CarCommandType.DESTROY).build())

        then:
        new PollingConditions(timeout: 0.1, delay: 0.01).eventually {
            assert !car.started.get()
        }
    }
}
