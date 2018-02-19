package com.doerapispring.domain;

import java.util.HashMap;
import java.util.Map;

enum Direction {
    UP(-1),
    DOWN(1),
    NONE(0);

    private static Map<Integer, Direction> valueMapping = new HashMap<>();

    static {
        for (Direction direction : Direction.values()) {
            valueMapping.put(direction.value, direction);
        }
    }

    private final int value;

    Direction(int value) {
        this.value = value;
    }

    public static Direction valueOf(int directionValue) {
        return valueMapping.get(directionValue);
    }

    public boolean targetNotExceeded(int currentIndex, int targetIndex) {
        switch (this) {
            case UP:
                return currentIndex >= targetIndex;
            case DOWN:
                return currentIndex <= targetIndex;
            case NONE:
                return false;
            default:
                return false;
        }
    }

    public int getValue() {
        return value;
    }
}
