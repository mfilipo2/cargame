package pl.speedapp.cargame.util;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import io.reactivex.Observable;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@UtilityClass
@Slf4j
public class GameMapUtil {
    public int[][] convertCsvMapToArray(InputStream csvWithMapInputStream) {
        List<int[]> map = Lists.newArrayList();
        streamLines(new BufferedInputStream(csvWithMapInputStream)).map(line -> Arrays.stream(line).mapToInt(s -> Integer.parseInt(s.trim())).toArray()).subscribe(map::add).dispose();
        return map.toArray(new int[0][]);
    }

    public boolean checkRoads(int[][] map) {
        int[][] copyMap = copyOfMap(map);

        int[] initPosition = findFirstValueInMap(copyMap, 1);
        // if there is no roads then return true
        if (initPosition == null) {
            return true;
        }

        // mark init position with -1
        copyMap[initPosition[0]][initPosition[1]] = -1;
        Queue<int[]> positionsToCheck = new LinkedList<>();
        positionsToCheck.add(initPosition);

        // mark continuous road with -1
        while (!positionsToCheck.isEmpty()) {
            int[] position = positionsToCheck.poll();
            int row = position[0];
            int column = position[1];
            copyMap[row][column] = -1;
            // check field above
            int newRow = row - 1;
            if (newRow >= 0 && copyMap[newRow][column] == 1) {
                copyMap[newRow][column] = -1;
                positionsToCheck.add(new int[]{newRow, column});
            }
            // check field below
            newRow = row + 1;
            if (newRow < copyMap.length && copyMap[newRow][column] == 1) {
                copyMap[newRow][column] = -1;
                positionsToCheck.add(new int[]{newRow, column});
            }
            // check field on the left
            int newColumn = column - 1;
            if (newColumn >= 0 && copyMap[row][newColumn] == 1) {
                copyMap[row][newColumn] = -1;
                positionsToCheck.add(new int[]{row, newColumn});
            }
            // check field on the right
            newColumn = column + 1;
            if (newColumn < copyMap[row].length && copyMap[row][newColumn] == 1) {
                copyMap[row][newColumn] = -1;
                positionsToCheck.add(new int[]{row, newColumn});
            }
        }

        // check if any field with value 1 left on the map
        return findFirstValueInMap(copyMap, 1) == null;
    }

    public int[][] copyOfMap(int[][] map) {
        return Arrays.stream(map).map(ints -> Arrays.copyOf(ints, ints.length)).toArray(int[][]::new);
    }

    private Observable<String[]> streamLines(InputStream csvWithMapInputStream) {
        return Observable.fromIterable(() -> new CSVReader(new InputStreamReader(csvWithMapInputStream)).iterator());
    }

    private int[] findFirstValueInMap(int[][] map, int value) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == value) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }
}
