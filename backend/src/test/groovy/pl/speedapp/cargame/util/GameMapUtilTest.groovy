package pl.speedapp.cargame.util

import spock.lang.Specification
import spock.lang.Unroll

class GameMapUtilTest extends Specification {

    def 'copy of the map is returned'() {
        given:
        int[][] map = [[1, 0], [0, 1]]

        when:
        int[][] copyMap = GameMapUtil.copyOfMap(map)
        int[][] copyMap2 = GameMapUtil.copyOfMap(map)
        copyMap2[1][1] = 2

        then:
        map == copyMap
        map != copyMap2
    }

    @Unroll
    def 'check if there is only one road on the map: #map'() {
        given:
        int[][] mapToCheck = (int[][]) Eval.me(map)

        when:
        def result = GameMapUtil.checkRoads(mapToCheck)

        then:
        result == expoectedResult

        where:
        expoectedResult | map
        true            | "[[0, 0], [0, 0]]"
        true            | "[[1, 0], [0, 0]]"
        true            | "[[1, 0], [1, 1]]"
        true            | "[[1, 1], [1, 1]]"
        false           | "[[1, 0], [0, 1]]"
        false           | "[[1, 0, 0], [1, 1, 0], [1, 0, 1]]"
        false           | "[[1, 0, 0], [0, 1, 0], [0, 0, 1]]"
        true            | "[[1, 1, 1], [1, 0, 1], [1, 1, 1]]"
        true            | "[[0, 0, 0], [0, 0, 1], [1, 1, 1]]"
    }
}
