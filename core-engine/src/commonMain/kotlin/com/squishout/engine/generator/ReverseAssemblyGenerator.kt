package com.squishout.engine.generator

import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.ObstacleType
import com.squishout.engine.model.Position
import kotlin.random.Random

data class LevelConfig(
    val stageNumber: Int,
    val jellyCount: Int = 14,
    val obstacleCount: Int = 1,
    val includeMultiCell: Boolean = false,
    val seed: Long? = null
)

data class Level(
    val stageNumber: Int,
    val initialBoard: Board,
    val optimalMoves: Int,
    val targetScore: Int
)

class ReverseAssemblyGenerator {

    fun generate(config: LevelConfig): Level {
        val random = if (config.seed != null) Random(config.seed) else Random.Default
        var attempts = 0
        val maxAttempts = 50

        while (attempts < maxAttempts) {
            attempts++
            val level = attemptGeneration(config, random)
            if (level != null && verifySolvable(level.initialBoard)) {
                return level
            }
        }

        // Fallback guaranteed starter level if random search exceeds max attempts
        return createFallbackLevel(config.stageNumber)
    }

    private fun attemptGeneration(config: LevelConfig, random: Random): Level? {
        val width = 6
        val height = 6
        val obstacles = mutableListOf<Obstacle>()

        // 1. Place obstacles in interior (1..4, 1..4) to avoid blocking entire perimeter
        val interiorPositions = (1 until width - 1).flatMap { x ->
            (1 until height - 1).map { y -> Position(x, y) }
        }.shuffled(random)

        val obstacleCount = config.obstacleCount.coerceAtMost(interiorPositions.size)
        for (i in 0 until obstacleCount) {
            val type = when {
                config.stageNumber >= 20 && i % 3 == 0 -> ObstacleType.ICE_BLOCK
                config.stageNumber >= 10 && i % 2 == 1 -> ObstacleType.HONEY_POT
                i % 2 == 0 -> ObstacleType.ROCK_MOUNTAIN
                else -> ObstacleType.ROCK_TREE
            }
            val health = if (type == ObstacleType.ICE_BLOCK) 2 else 1
            obstacles.add(Obstacle("obs_$i", type, interiorPositions[i], health = health, maxHealth = health))
        }

        var board = Board(width = width, height = height, obstacles = obstacles)
        val placedJellies = mutableListOf<Jelly>()
        var jellyIndex = 0

        // 2. Reverse-assembly placement loop
        // We iterate and place jellies that have an open exit ray at the moment of placement
        var consecutiveFailures = 0
        while (placedJellies.size < config.jellyCount && consecutiveFailures < 100) {
            val emptyTiles = (0 until width).flatMap { x ->
                (0 until height).map { y -> Position(x, y) }
            }.filterNot { board.isTileOccupied(it) }.shuffled(random)

            if (emptyTiles.isEmpty()) break

            var placed = false
            for (pos in emptyTiles) {
                val candidateDirs = Direction.entries.shuffled(random)

                for (dir in candidateDirs) {
                    val isMultiCell = config.includeMultiCell &&
                            (jellyIndex == 3 || jellyIndex == 7) &&
                            pos.step(dir.opposite).isWithinBounds(width, height) &&
                            !board.isTileOccupied(pos.step(dir.opposite))

                    val tiles = if (isMultiCell) {
                        listOf(pos, pos.step(dir.opposite))
                    } else {
                        listOf(pos)
                    }

                    // Check if path from head along 'dir' to edge is currently free of obstructions
                    val tempJelly = Jelly(
                        id = "jelly_$jellyIndex",
                        type = pickJellyType(dir, isMultiCell),
                        direction = dir,
                        tiles = tiles
                    )

                    if (board.canJellyEscape(tempJelly)) {
                        placedJellies.add(tempJelly)
                        board = board.copy(jellies = placedJellies)
                        jellyIndex++
                        placed = true
                        consecutiveFailures = 0
                        break
                    }
                }
                if (placed) break
            }

            if (!placed) {
                consecutiveFailures++
            }
        }

        if (placedJellies.size < (config.jellyCount * 0.7).toInt().coerceAtLeast(4)) {
            return null
        }

        val finalizedBoard = board.withUpdatedEyeStates()
        if (finalizedBoard.awakeJellies.isEmpty()) {
            return null
        }

        val optimalMoves = placedJellies.size
        val targetScore = optimalMoves * 100 + 300

        return Level(
            stageNumber = config.stageNumber,
            initialBoard = finalizedBoard,
            optimalMoves = optimalMoves,
            targetScore = targetScore
        )
    }

    private fun pickJellyType(direction: Direction, isMultiCell: Boolean): JellyType {
        if (isMultiCell) return JellyType.GRAPE_EEL
        return when (direction) {
            Direction.NORTH -> JellyType.STRAWBERRY
            Direction.EAST -> JellyType.BLUEBERRY
            Direction.WEST -> JellyType.LEMON
            Direction.SOUTH -> JellyType.KIWI
        }
    }

    /**
     * Verifies with a Greedy unblocking simulation that the board can be 100% cleared.
     */
    fun verifySolvable(initialBoard: Board): Boolean {
        var current = initialBoard.withUpdatedEyeStates()
        val totalJellies = current.jellies.size
        var steps = 0

        while (!current.isSolved && steps <= totalJellies * 2) {
            val awake = current.awakeJellies
            if (awake.isEmpty()) {
                return false // Deadlock detected
            }
            // In a greedy step, remove the first awake jelly
            current = current.removeJelly(awake.first().id)
            steps++
        }

        return current.isSolved
    }

    private fun createFallbackLevel(stageNumber: Int): Level {
        val jellies = listOf(
            Jelly("j_0", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(0, 0))),
            Jelly("j_1", JellyType.BLUEBERRY, Direction.EAST, listOf(Position(3, 1))),
            Jelly("j_2", JellyType.LEMON, Direction.WEST, listOf(Position(1, 2))),
            Jelly("j_3", JellyType.KIWI, Direction.SOUTH, listOf(Position(2, 4))),
            Jelly("j_4", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(4, 2))),
            Jelly("j_5", JellyType.BLUEBERRY, Direction.EAST, listOf(Position(1, 4)))
        )
        val board = Board(width = 6, height = 6, jellies = jellies).withUpdatedEyeStates()
        return Level(
            stageNumber = stageNumber,
            initialBoard = board,
            optimalMoves = jellies.size,
            targetScore = jellies.size * 100 + 300
        )
    }
}
