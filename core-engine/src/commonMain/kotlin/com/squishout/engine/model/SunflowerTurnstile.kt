package com.squishout.engine.model

/**
 * Sunflower Turnstile: A directional raycast deflector on the tray floor.
 * When traversed by a jelly, it deflects the jelly's trajectory into [direction],
 * and rotates 90 degrees clockwise (NORTH -> EAST -> SOUTH -> WEST -> NORTH).
 */
data class SunflowerTurnstile(
    val id: String,
    val position: Position,
    val direction: Direction
) {
    fun rotateClockwise(): SunflowerTurnstile {
        val nextDirection = when (direction) {
            Direction.NORTH -> Direction.EAST
            Direction.EAST -> Direction.SOUTH
            Direction.SOUTH -> Direction.WEST
            Direction.WEST -> Direction.NORTH
        }
        return copy(direction = nextDirection)
    }
}
