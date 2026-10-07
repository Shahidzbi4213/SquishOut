package com.squishout.engine.generator

import com.squishout.engine.model.Board
import com.squishout.engine.model.DifficultyTier
import com.squishout.engine.model.Direction
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.ObstacleType
import com.squishout.engine.model.Position
import com.squishout.engine.model.WaterJet
import kotlin.random.Random

data class LevelConfig(
    val stageNumber: Int,
    val gridWidth: Int = 6,
    val gridHeight: Int = 6,
    val jellyCount: Int = 14,
    val obstacleCount: Int = 1,
    val maxInitialAwake: Int? = null,
    val difficultyTier: DifficultyTier = DifficultyTier.NORMAL,
    val includeMultiCell: Boolean = false,
    val includeWaterJets: Boolean = false,
    val includeSchoolingPairs: Boolean = false,
    val includeBubbleFog: Boolean = false,
    val includeKingJelly: Boolean = false,
    val seed: Long? = null
)

data class Level(
    val stageNumber: Int,
    val initialBoard: Board,
    val optimalMoves: Int,
    val targetScore: Int,
    val difficultyTier: DifficultyTier = DifficultyTier.NORMAL
)

class ReverseAssemblyGenerator {

    fun generate(config: LevelConfig): Level {
        val random = if (config.seed != null) Random(config.seed) else Random.Default
        var attempts = 0
        val maxAttempts = 120

        while (attempts < maxAttempts) {
            attempts++
            // As attempts increase, relax maxInitialAwake constraint progressively
            val effectiveMaxAwake = if (config.maxInitialAwake != null && attempts > 25) {
                config.maxInitialAwake + (attempts - 25) / 5
            } else {
                config.maxInitialAwake
            }
            val workingConfig = if (effectiveMaxAwake != config.maxInitialAwake) {
                config.copy(maxInitialAwake = effectiveMaxAwake)
            } else {
                config
            }

            val level = attemptGeneration(workingConfig, random)
            if (level != null && verifySolvable(level.initialBoard)) {
                return level
            }
        }

        // Fallback guaranteed starter level if random search exceeds max attempts
        return createFallbackLevel(config)
    }

    private fun attemptGeneration(config: LevelConfig, random: Random): Level? {
        val width = config.gridWidth
        val height = config.gridHeight
        val obstacles = mutableListOf<Obstacle>()
        val waterJets = mutableListOf<WaterJet>()

        // 1. Place obstacles in interior to avoid blocking entire perimeter
        val interiorPositions = (1 until width - 1).flatMap { x ->
            (1 until height - 1).map { y -> Position(x, y) }
        }.shuffled(random).toMutableList()

        val obstacleCount = config.obstacleCount.coerceAtMost(interiorPositions.size)
        for (i in 0 until obstacleCount) {
            val pos = interiorPositions.removeAt(0)
            val type = when {
                config.stageNumber >= 20 && i % 3 == 0 -> ObstacleType.ICE_BLOCK
                config.stageNumber >= 10 && i % 2 == 1 -> ObstacleType.HONEY_POT
                i % 2 == 0 -> ObstacleType.ROCK_MOUNTAIN
                else -> ObstacleType.ROCK_TREE
            }
            val health = if (type == ObstacleType.ICE_BLOCK) 2 else 1
            obstacles.add(Obstacle("obs_$i", type, pos, health = health, maxHealth = health))
        }

        // 2. Place Water Jets (90° Trajectory Deflectors) if enabled
        if (config.includeWaterJets && interiorPositions.isNotEmpty()) {
            val jetCount = when {
                config.stageNumber >= 50 -> 3
                config.stageNumber >= 10 -> 2
                else -> 1
            }
            for (i in 0 until jetCount.coerceAtMost(interiorPositions.size)) {
                val jetPos = interiorPositions.removeAt(0)
                // Point toward an edge
                val jetDir = when {
                    jetPos.x >= width / 2 -> Direction.EAST
                    jetPos.x < width / 2 -> Direction.WEST
                    jetPos.y >= height / 2 -> Direction.SOUTH
                    else -> Direction.NORTH
                }
                waterJets.add(WaterJet(jetPos, jetDir))
            }
        }

        var board = Board(
            width = width,
            height = height,
            obstacles = obstacles,
            waterJets = waterJets
        )
        val placedJellies = mutableListOf<Jelly>()
        var jellyIndex = 0

        // 2b. Place 2x2 King Jelly Boss if enabled
        if (config.includeKingJelly && width >= 5 && height >= 5) {
            val kingTopLeftCandidates = (1 until width - 2).flatMap { x ->
                (1 until height - 2).map { y -> Position(x, y) }
            }.shuffled(random)

            for (topLeft in kingTopLeftCandidates) {
                val kingTiles = listOf(
                    topLeft,
                    Position(topLeft.x + 1, topLeft.y),
                    Position(topLeft.x, topLeft.y + 1),
                    Position(topLeft.x + 1, topLeft.y + 1)
                )
                val allFree = kingTiles.none { board.isTileOccupied(it) }
                if (allFree) {
                    val candidateDirs = Direction.entries.shuffled(random)
                    for (dir in candidateDirs) {
                        val tempKing = Jelly(
                            id = "king_boss",
                            type = JellyType.KING_JELLY,
                            direction = dir,
                            tiles = kingTiles
                        )
                        if (board.canJellyEscape(tempKing)) {
                            placedJellies.add(tempKing)
                            board = board.copy(jellies = placedJellies)
                            jellyIndex++
                            break
                        }
                    }
                    if (placedJellies.isNotEmpty()) break
                }
            }
        }

        // 3. Reverse-assembly placement loop
        // We iterate and place jellies that have an open exit ray at the moment of placement
        var consecutiveFailures = 0
        while (placedJellies.size < config.jellyCount && consecutiveFailures < 120) {
            val emptyTiles = (0 until width).flatMap { x ->
                (0 until height).map { y -> Position(x, y) }
            }.filterNot { board.isTileOccupied(it) }.shuffled(random)

            if (emptyTiles.isEmpty()) break

            var placed = false
            for (pos in emptyTiles) {
                val candidateDirs = Direction.entries.shuffled(random)

                for (dir in candidateDirs) {
                    val isMultiCell = config.includeMultiCell &&
                            (jellyIndex % 4 == 3) &&
                            pos.step(dir.opposite).isWithinBounds(width, height) &&
                            !board.isTileOccupied(pos.step(dir.opposite))

                    val tiles = if (isMultiCell) {
                        listOf(pos, pos.step(dir.opposite))
                    } else {
                        listOf(pos)
                    }

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

        // 4. Optionally link two adjacent single-cell jellies into a Symbiotic Schooling Pair
        if (config.includeSchoolingPairs && placedJellies.size >= 4) {
            val singleCellJellies = placedJellies.filter { it.tiles.size == 1 && it.linkedJellyId == null }
            pairLoop@ for (i in singleCellJellies.indices) {
                val j1 = singleCellJellies[i]
                for (j in (i + 1) until singleCellJellies.size) {
                    val j2 = singleCellJellies[j]
                    val p1 = j1.tiles.first()
                    val p2 = j2.tiles.first()
                    val dx = kotlin.math.abs(p1.x - p2.x)
                    val dy = kotlin.math.abs(p1.y - p2.y)
                    // Must be adjacent (cardinal distance = 1)
                    if (dx + dy == 1) {
                        // Link them
                        val updatedJellies = placedJellies.map { jelly ->
                            when (jelly.id) {
                                j1.id -> jelly.copy(linkedJellyId = j2.id)
                                j2.id -> jelly.copy(linkedJellyId = j1.id)
                                else -> jelly
                            }
                        }
                        board = board.copy(jellies = updatedJellies)
                        break@pairLoop
                    }
                }
            }
        }

        // 5. Optionally place Bubble Fog / Ink Clouds
        var fogTiles = emptySet<Position>()
        if (config.includeBubbleFog) {
            val fogCandidates = (1 until width - 1).flatMap { x ->
                (1 until height - 1).map { y -> Position(x, y) }
            }.shuffled(random).take(if (width >= 7) 5 else 3).toSet()
            fogTiles = fogCandidates
            board = board.copy(fogTiles = fogTiles)
        }

        val finalizedBoard = board.withUpdatedEyeStates()
        if (finalizedBoard.awakeJellies.isEmpty()) {
            return null
        }

        // 6. Enforce initial awake constraint if specified (bottleneck depth)
        if (config.maxInitialAwake != null && finalizedBoard.awakeJellies.size > config.maxInitialAwake) {
            return null
        }

        val optimalMoves = finalizedBoard.jellies.count { it.linkedJellyId == null } +
                (finalizedBoard.jellies.count { it.linkedJellyId != null } / 2)
        val targetScore = optimalMoves * 100 + 300

        return Level(
            stageNumber = config.stageNumber,
            initialBoard = finalizedBoard,
            optimalMoves = optimalMoves,
            targetScore = targetScore,
            difficultyTier = config.difficultyTier
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
            // In a greedy step, remove the first awake jelly and its partner if linked
            val first = awake.first()
            val toRemove = if (first.linkedJellyId != null) setOf(first.id, first.linkedJellyId) else setOf(first.id)
            current = current.removeJellies(toRemove)
            steps++
        }

        return current.isSolved
    }

    private fun createFallbackLevel(config: LevelConfig): Level {
        val w = config.gridWidth
        val h = config.gridHeight
        val jellies = mutableListOf<Jelly>()
        if (config.includeKingJelly && w >= 5 && h >= 5) {
            jellies.add(
                Jelly(
                    id = "king_boss",
                    type = JellyType.KING_JELLY,
                    direction = Direction.NORTH,
                    tiles = listOf(
                        Position(w / 2 - 1, 1),
                        Position(w / 2, 1),
                        Position(w / 2 - 1, 2),
                        Position(w / 2, 2)
                    )
                )
            )
        }
        jellies.addAll(listOf(
            Jelly("j_0", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(0, 0))),
            Jelly("j_1", JellyType.BLUEBERRY, Direction.EAST, listOf(Position(w - 2, 0))),
            Jelly("j_2", JellyType.LEMON, Direction.WEST, listOf(Position(0, h - 1))),
            Jelly("j_3", JellyType.KIWI, Direction.SOUTH, listOf(Position(w - 1, h - 1)))
        ))
        val board = Board(width = w, height = h, jellies = jellies).withUpdatedEyeStates()
        return Level(
            stageNumber = config.stageNumber,
            initialBoard = board,
            optimalMoves = jellies.size,
            targetScore = jellies.size * 100 + 300,
            difficultyTier = config.difficultyTier
        )
    }
}
