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
