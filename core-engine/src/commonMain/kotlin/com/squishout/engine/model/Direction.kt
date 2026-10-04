package com.squishout.engine.model

enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    val opposite: Direction
        get() = when (this) {
            NORTH -> SOUTH
            EAST -> WEST
            SOUTH -> NORTH
            WEST -> EAST
        }
}

data class Position(val x: Int, val y: Int) {
    fun step(direction: Direction, distance: Int = 1): Position =
        Position(x + direction.dx * distance, y + direction.dy * distance)

    fun isWithinBounds(width: Int, height: Int): Boolean =
        x in 0 until width && y in 0 until height
}
