package com.squishout.engine.model

data class Board(
    val width: Int = 6,
    val height: Int = 6,
    val jellies: List<Jelly> = emptyList(),
    val obstacles: List<Obstacle> = emptyList()
) {
    // Quick lookup maps
    private val tileToJelly: Map<Position, Jelly> by lazy {
        val map = mutableMapOf<Position, Jelly>()
        for (jelly in jellies) {
            for (tile in jelly.tiles) {
                map[tile] = jelly
            }
        }
        map
    }

    private val tileToObstacle: Map<Position, Obstacle> by lazy {
        obstacles.associateBy { it.position }
    }

    fun isTileOccupied(pos: Position): Boolean =
        tileToJelly.containsKey(pos) || tileToObstacle.containsKey(pos)

    fun getJellyAt(pos: Position): Jelly? = tileToJelly[pos]

    fun getObstacleAt(pos: Position): Obstacle? = tileToObstacle[pos]

    fun getJellyById(id: String): Jelly? = jellies.find { it.id == id }

    /**
     * Checks if a jelly has a clear, unblocked ray to the board edge along its direction.
     */
    fun canJellyEscape(jelly: Jelly): Boolean {
        val path = getEscapePath(jelly)
        // If the path encounters any tile occupied by another object, it is blocked
        for (pos in path) {
            val otherJelly = getJellyAt(pos)
            if (otherJelly != null && otherJelly.id != jelly.id) {
                return false
            }
            if (getObstacleAt(pos) != null) {
                return false
            }
        }
        return true
    }

    /**
     * Returns the sequence of grid positions a jelly traverses from its head to beyond the board edge.
     */
    fun getEscapePath(jelly: Jelly): List<Position> {
        val path = mutableListOf<Position>()
        var current = jelly.headPosition.step(jelly.direction)
        while (current.isWithinBounds(width, height)) {
            path.add(current)
            current = current.step(jelly.direction)
        }
        // Add the off-screen exit position
        path.add(current)
        return path
    }

    /**
     * Recomputes the EyeState (AWAKE vs ASLEEP) for every jelly on the board.
     * Unblocked jellies become AWAKE with sparkling eyes; blocked ones become ASLEEP.
     */
    fun withUpdatedEyeStates(): Board {
        val updatedJellies = jellies.map { jelly ->
            val isAwake = canJellyEscape(jelly)
            jelly.withEyeState(if (isAwake) EyeState.AWAKE else EyeState.ASLEEP)
        }
        return copy(jellies = updatedJellies)
    }

    /**
     * Removes a jelly from the board and recalculates eye states for remaining jellies.
     */
    fun removeJelly(jellyId: String): Board {
        val remaining = jellies.filterNot { it.id == jellyId }
        return copy(jellies = remaining).withUpdatedEyeStates()
    }

    val awakeJellies: List<Jelly>
        get() = jellies.filter { it.eyeState == EyeState.AWAKE }

    val isSolved: Boolean
        get() = jellies.isEmpty()
}
