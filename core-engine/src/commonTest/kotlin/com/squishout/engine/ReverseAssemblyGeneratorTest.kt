package com.squishout.engine

import com.squishout.engine.generator.LevelConfig
import com.squishout.engine.generator.ReverseAssemblyGenerator
import kotlin.test.Test
import kotlin.test.assertTrue

class ReverseAssemblyGeneratorTest {

    private val generator = ReverseAssemblyGenerator()

    @Test
    fun testGeneratedLevelsAre100PercentSolvable() {
        // Test 5 different seeds across various difficulty levels
        val configs = listOf(
            LevelConfig(stageNumber = 1, jellyCount = 8, obstacleCount = 0, seed = 42L),
            LevelConfig(stageNumber = 5, jellyCount = 12, obstacleCount = 1, seed = 101L),
            LevelConfig(stageNumber = 15, jellyCount = 14, obstacleCount = 2, includeMultiCell = true, seed = 202L),
            LevelConfig(stageNumber = 24, jellyCount = 16, obstacleCount = 2, includeMultiCell = true, seed = 303L),
            LevelConfig(stageNumber = 40, jellyCount = 18, obstacleCount = 3, includeMultiCell = true, seed = 404L)
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
        }
    }
}
