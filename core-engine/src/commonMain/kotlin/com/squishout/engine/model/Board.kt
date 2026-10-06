package com.squishout.engine.model

data class Board(
    val width: Int = 6,
    val height: Int = 6,
    val jellies: List<Jelly> = emptyList(),
    val obstacles: List<Obstacle> = emptyList(),
    val waterJets: List<WaterJet> = emptyList(),
    val fogTiles: Set<Position> = emptySet()
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

    private val tileToWaterJet: Map<Position, WaterJet> by lazy {
        waterJets.associateBy { it.position }
    }

    fun isTileOccupied(pos: Position): Boolean =
        tileToJelly.containsKey(pos) || tileToObstacle.containsKey(pos)

    fun getJellyAt(pos: Position): Jelly? = tileToJelly[pos]

    fun getObstacleAt(pos: Position): Obstacle? = tileToObstacle[pos]

    fun getWaterJetAt(pos: Position): WaterJet? = tileToWaterJet[pos]

    fun isFoggy(pos: Position): Boolean = pos in fogTiles

    fun getJellyById(id: String): Jelly? = jellies.find { it.id == id }

    /**
     * Checks if a single jelly has an unblocked path to the board edge along its direction,
     * accounting for any 90-degree trajectory deflections from [WaterJet]s.
     * Optional [ignoredJellyIds] lets linked partners ignore each other during coordinated schooling exit.
     */
    fun canSingleJellyEscape(jelly: Jelly, ignoredJellyIds: Set<String> = emptySet()): Boolean {
        val path = getEscapePath(jelly)
        val lastPos = path.lastOrNull() ?: return false
        // If the path didn't reach outside board bounds, it got trapped in a loop or dead end
        if (lastPos.isWithinBounds(width, height)) {
            return false
        }

        // If the path encounters any tile occupied by another object, it is blocked
        for (pos in path) {
            val otherJelly = getJellyAt(pos)
            if (otherJelly != null && otherJelly.id != jelly.id && otherJelly.id !in ignoredJellyIds) {
                return false
            }
            if (getObstacleAt(pos) != null) {
                return false
            }
        }
        return true
    }

    /**
     * Checks if a jelly can escape. If linked to a partner in a Symbiotic Schooling Pair,
     * BOTH partners must be simultaneously unblocked to launch together.
     */
    fun canJellyEscape(jelly: Jelly): Boolean {
        val partnerId = jelly.linkedJellyId
        if (partnerId != null) {
            val partner = getJellyById(partnerId)
            if (partner == null) return false
            return canSingleJellyEscape(jelly, setOf(partnerId)) &&
                   canSingleJellyEscape(partner, setOf(jelly.id))
        }
        return canSingleJellyEscape(jelly)
    }

    /**
     * Returns the sequence of grid positions a jelly traverses from its head to beyond the board edge.
     * If the path encounters a [WaterJet] conveyor tile, its trajectory curves into the jet's direction.
     */
    fun getEscapePath(jelly: Jelly): List<Position> {
        val path = mutableListOf<Position>()
        val visited = mutableSetOf<Pair<Position, Direction>>()
        var currentDir = jelly.direction
        var current = jelly.headPosition.step(currentDir)

        while (current.isWithinBounds(width, height)) {
            val state = current to currentDir
            if (state in visited) {
                // Loop detected (e.g. whirlpool loop of deflecting jets)
                return path
            }
            visited.add(state)
            path.add(current)

            val jet = getWaterJetAt(current)
            if (jet != null) {
                currentDir = jet.direction
            }
            current = current.step(currentDir)
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
     * Removes a single jelly from the board and recalculates eye states for remaining jellies.
     */
    fun removeJelly(jellyId: String): Board = removeJellies(setOf(jellyId))

    /**
     * Removes one or more jellies (such as a launched Symbiotic Schooling Pair) and updates eye states.
     */
    fun removeJellies(jellyIds: Set<String>): Board {
        val remaining = jellies.filterNot { it.id in jellyIds }
        return copy(jellies = remaining).withUpdatedEyeStates()
    }

    /**
     * Clears bubble fog from tiles adjacent to (or equal to) the specified launched positions.
     * Returns the updated board and the set of cleared positions.
     */
    fun clearFogAdjacentTo(positions: Set<Position>): Pair<Board, Set<Position>> {
        if (fogTiles.isEmpty()) return this to emptySet()
        val cleared = mutableSetOf<Position>()
        val remaining = fogTiles.filterNot { fogPos ->
            val isCleared = positions.any { pos ->
                pos == fogPos || isAdjacent(pos, setOf(fogPos))
            }
            if (isCleared) cleared.add(fogPos)
            isCleared
        }.toSet()
        val newBoard = copy(fogTiles = remaining)
        return newBoard to cleared
    }

    /**
     * Damages any crackable obstacle (Ice Block or Honey Pot) adjacent to the specified positions.
     * Obstacles reaching 0 health shatter and are removed from the board.
     * Returns the updated board and the list of shattered obstacles.
     */
    fun damageObstaclesAdjacentTo(positions: Set<Position>): Pair<Board, List<Obstacle>> {
        val shattered = mutableListOf<Obstacle>()
        val updatedObstacles = obstacles.mapNotNull { obs ->
            if (obs.isCrackable && isAdjacent(obs.position, positions)) {
                val newHealth = obs.health - 1
                if (newHealth <= 0) {
                    shattered.add(obs)
                    null // Shattered & cleared
                } else {
                    obs.copy(health = newHealth)
                }
            } else {
                obs
            }
        }
        val newBoard = copy(obstacles = updatedObstacles).withUpdatedEyeStates()
        return newBoard to shattered
    }

    private fun isAdjacent(pos: Position, set: Set<Position>): Boolean {
        for (other in set) {
            val dx = kotlin.math.abs(pos.x - other.x)
            val dy = kotlin.math.abs(pos.y - other.y)
            if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1)) return true
        }
        return false
    }

    val awakeJellies: List<Jelly>
        get() = jellies.filter { it.eyeState == EyeState.AWAKE }

    val isSolved: Boolean
        get() = jellies.isEmpty()
}
