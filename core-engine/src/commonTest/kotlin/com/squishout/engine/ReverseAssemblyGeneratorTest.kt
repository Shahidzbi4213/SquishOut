package com.squishout.engine

import com.squishout.engine.generator.LevelConfig
import com.squishout.engine.generator.ReverseAssemblyGenerator
import com.squishout.engine.model.DifficultyTier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReverseAssemblyGeneratorTest {

    private val generator = ReverseAssemblyGenerator()

    @Test
    fun testGeneratedLevelsAre100PercentSolvable() {
        // Test various difficulty levels and grid sizes
        val configs = listOf(
            LevelConfig(stageNumber = 1, gridWidth = 6, gridHeight = 6, jellyCount = 8, obstacleCount = 0, seed = 42L),
            LevelConfig(stageNumber = 5, gridWidth = 6, gridHeight = 6, jellyCount = 12, obstacleCount = 1, difficultyTier = DifficultyTier.HARD, seed = 101L),
            LevelConfig(stageNumber = 15, gridWidth = 6, gridHeight = 6, jellyCount = 14, obstacleCount = 2, includeMultiCell = true, seed = 202L),
            LevelConfig(stageNumber = 35, gridWidth = 7, gridHeight = 7, jellyCount = 20, obstacleCount = 3, includeMultiCell = true, seed = 303L),
            LevelConfig(stageNumber = 75, gridWidth = 8, gridHeight = 8, jellyCount = 28, obstacleCount = 4, includeMultiCell = true, seed = 404L)
        )

        for (config in configs) {
            val level = generator.generate(config)
            assertTrue(
                level.initialBoard.jellies.isNotEmpty(),
                "Stage ${config.stageNumber} should generate jellies"
            )
            assertTrue(
                level.initialBoard.awakeJellies.isNotEmpty(),
                "Stage ${config.stageNumber} must have at least one awake jelly at start"
            )
            assertTrue(
                generator.verifySolvable(level.initialBoard),
                "Stage ${config.stageNumber} must be 100% solvable via reverse-assembly"
            )
            assertEquals(config.gridWidth, level.initialBoard.width)
            assertEquals(config.gridHeight, level.initialBoard.height)
        }
    }

    @Test
    fun testLevelWithKingJellyOnLargeGridIsSolvable() {
        val config = LevelConfig(
            stageNumber = 30,
            gridWidth = 7,
            gridHeight = 7,
            jellyCount = 20,
            obstacleCount = 2,
            includeMultiCell = true,
            includeKingJelly = true,
            difficultyTier = DifficultyTier.SUPER_HARD,
            maxInitialAwake = 2,
            seed = 777L
        )
        val level = generator.generate(config)
        val king = level.initialBoard.jellies.find { it.type == com.squishout.engine.model.JellyType.KING_JELLY }

        assertTrue(king != null, "Level 30 boss should contain a KING_JELLY")
        assertTrue(level.initialBoard.awakeJellies.isNotEmpty(), "Boss board must have awake jellies to start")
        assertTrue(generator.verifySolvable(level.initialBoard), "Boss level with 2x2 King Jelly must be 100% solvable")
    }

    @Test
    fun testLevelGenerationWithTurnstiles() {
        val config = LevelConfig(
            stageNumber = 25,
            gridWidth = 6,
            gridHeight = 6,
            jellyCount = 12,
            obstacleCount = 1,
            includeTurnstiles = true,
            turnstileCount = 2,
            seed = 555L
        )
        val level = generator.generate(config)
        val board = level.initialBoard

        assertEquals(2, board.turnstiles.size)
        for (turnstile in board.turnstiles) {
            assertTrue(turnstile.id.startsWith("turnstile_"))
            // Verify interior placement
            assertTrue(turnstile.position.x in 1 until (config.gridWidth - 1))
            assertTrue(turnstile.position.y in 1 until (config.gridHeight - 1))
            // Verify turnstile does not overlap obstacles
            assertTrue(board.obstacles.none { it.position == turnstile.position })
            // Verify turnstile does not overlap jellies
            assertTrue(board.jellies.none { jelly -> turnstile.position in jelly.tiles })
        }

        assertTrue(board.awakeJellies.isNotEmpty(), "Board with turnstiles must have awake jellies to start")
        assertTrue(
            generator.verifySolvable(board),
            "Generated level with turnstiles must be 100% solvable"
        )
    }

    @Test
    fun testDiverseSeedsSolvabilityWithTurnstiles() {
        val testConfigs = mutableListOf<LevelConfig>()
        val gridSizes = listOf(6 to 6, 7 to 7, 8 to 8)
        val tiers = listOf(
            DifficultyTier.NORMAL,
            DifficultyTier.HARD,
            DifficultyTier.SUPER_HARD,
            DifficultyTier.BREATHER
        )

        // 30 diverse seeds covering combinations of grid sizes, tiers, and features
        for (i in 0 until 30) {
            val (w, h) = gridSizes[i % gridSizes.size]
            val tier = tiers[i % tiers.size]
            val stage = 10 + (i * 3)
            val jellyCount = when (w) {
                6 -> 10 + (i % 4)
                7 -> 16 + (i % 4)
                else -> 22 + (i % 4)
            }
            val seed = 1000L + (i * 73L)

            testConfigs.add(
                LevelConfig(
                    stageNumber = stage,
                    gridWidth = w,
                    gridHeight = h,
                    jellyCount = jellyCount,
                    obstacleCount = 1 + (i % 3),
                    difficultyTier = tier,
                    includeMultiCell = (i % 2 == 0),
                    includeWaterJets = (i % 3 == 0),
                    includeTurnstiles = true,
                    includeSchoolingPairs = (i % 4 == 0),
                    includeBubbleFog = (i % 5 == 0),
                    seed = seed
                )
            )
        }

        for (config in testConfigs) {
            val level = generator.generate(config)
            val board = level.initialBoard

            assertTrue(
                board.turnstiles.isNotEmpty(),
                "Seed ${config.seed} (Stage ${config.stageNumber}) should have turnstiles placed"
            )
            assertTrue(
                board.awakeJellies.isNotEmpty(),
                "Seed ${config.seed} (Stage ${config.stageNumber}) must have awake jellies at start"
            )
            assertTrue(
                generator.verifySolvable(board),
                "Seed ${config.seed} (Stage ${config.stageNumber}, grid ${config.gridWidth}x${config.gridHeight}) must be 100% solvable"
            )
        }
    }

    @Test
    fun testSawtoothTierHelper() {
        assertEquals(DifficultyTier.NORMAL, DifficultyTier.forStage(1))
        assertEquals(DifficultyTier.NORMAL, DifficultyTier.forStage(2))
        assertEquals(DifficultyTier.NORMAL, DifficultyTier.forStage(3))
        assertEquals(DifficultyTier.HARD, DifficultyTier.forStage(5))
        assertEquals(DifficultyTier.BREATHER, DifficultyTier.forStage(6))
        assertEquals(DifficultyTier.SUPER_HARD, DifficultyTier.forStage(10))
        assertEquals(DifficultyTier.BREATHER, DifficultyTier.forStage(11))
        assertEquals(DifficultyTier.HARD, DifficultyTier.forStage(15))
        assertEquals(DifficultyTier.SUPER_HARD, DifficultyTier.forStage(20))
    }
}
