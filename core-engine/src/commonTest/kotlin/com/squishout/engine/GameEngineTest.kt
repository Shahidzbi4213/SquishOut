package com.squishout.engine

import com.squishout.engine.generator.Level
import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.EyeState
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.engine.model.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameEngineTest {

    private val engine = GameEngine()

    private fun createSimpleLevel(): Level {
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(0, 0)))
        val j2 = Jelly("j2", JellyType.BLUEBERRY, Direction.EAST, listOf(Position(2, 2)))
        val board = Board(jellies = listOf(j1, j2)).withUpdatedEyeStates()
        return Level(stageNumber = 1, initialBoard = board, optimalMoves = 2, targetScore = 500)
    }

    @Test
    fun testTapAwakeJellyLaunchesAndClearsLevel() {
        val level = createSimpleLevel()
        engine.loadLevel(level)

        // Tap unblocked j1 at (0, 0)
        val result1 = engine.onTileTapped(Position(0, 0))
        assertIs<TapResult.Launched>(result1)
        assertEquals("j1", result1.jelly.id)
        assertEquals(1, engine.state.value.movesUsed)

        // Tap unblocked j2 at (2, 2)
        val result2 = engine.onTileTapped(Position(2, 2))
        assertIs<TapResult.Launched>(result2)
        assertTrue(result2.isSolved)
        assertEquals(3, result2.stars) // Optimal moves = 2, moves used = 2 -> 3 stars!
        assertTrue(engine.state.value.isCompleted)
    }

    @Test
    fun testTapBlockedJellyReturnsBlockedResult() {
        // j1 blocks j2
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 0)))
        val j2 = Jelly("j2", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 2)))
        val board = Board(jellies = listOf(j1, j2)).withUpdatedEyeStates()
        val level = Level(stageNumber = 1, initialBoard = board, optimalMoves = 2, targetScore = 500)

        engine.loadLevel(level)

        // Tap j2 (which is blocked by j1)
        val result = engine.onTileTapped(Position(2, 2))
        assertIs<TapResult.Blocked>(result)
        assertEquals("j2", result.jelly.id)
        assertEquals(Position(2, 0), result.blockerPosition)
    }

    @Test
    fun testUndoBoosterRestoresPreviousBoard() {
        val level = createSimpleLevel()
        engine.loadLevel(level)

        // Launch j1
        engine.onTileTapped(Position(0, 0))
        assertEquals(1, engine.state.value.board.jellies.size)

        // Use undo
        val undoSuccess = engine.useUndo()
        assertTrue(undoSuccess)
        assertEquals(2, engine.state.value.board.jellies.size)
        assertEquals(0, engine.state.value.movesUsed)
        assertEquals(4, engine.state.value.undoCount)
    }

    @Test
    fun testHintBoosterHighlightsAwakeJelly() {
        val level = createSimpleLevel()
        engine.loadLevel(level)

        val hintJellyId = engine.useHint()
        assertNotNull(hintJellyId)
        assertEquals(hintJellyId, engine.state.value.highlightedJellyId)
        assertEquals(2, engine.state.value.hintCount)
    }

    @Test
    fun testMagicWandRemovesAsleepJelly() {
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 0)))
        val j2 = Jelly("j2", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 2)))
        val board = Board(jellies = listOf(j1, j2)).withUpdatedEyeStates()
        val level = Level(stageNumber = 1, initialBoard = board, optimalMoves = 2, targetScore = 500)

        engine.loadLevel(level)

        // j2 is asleep
        val removed = engine.useMagicWand()
        assertNotNull(removed)
        assertEquals("j2", removed.id)
        assertEquals(0, engine.state.value.wandCount)
    }

    @Test
    fun testAdjacentCrackableObstacleTakesDamageAndShatters() {
        // j1 launches NORTH from (2, 2)
        // ice block is adjacent at (1, 2) with health = 1
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 2)))
        val ice = com.squishout.engine.model.Obstacle("ice_1", com.squishout.engine.model.ObstacleType.ICE_BLOCK, Position(1, 2), health = 1, maxHealth = 1)
        val board = Board(jellies = listOf(j1), obstacles = listOf(ice)).withUpdatedEyeStates()
        val level = Level(stageNumber = 20, initialBoard = board, optimalMoves = 1, targetScore = 500)

        engine.loadLevel(level)
        assertEquals(1, engine.state.value.board.obstacles.size)

        val result = engine.onTileTapped(Position(2, 2))
        assertIs<TapResult.Launched>(result)
        assertEquals(1, result.shatteredObstacles.size)
        assertEquals("ice_1", result.shatteredObstacles.first().id)
        assertEquals(0, engine.state.value.board.obstacles.size)
    }

    @Test
    fun testSymbioticSchoolingPairLaunchesBothPartnersSynchronously() {
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 2)), linkedJellyId = "j2")
        val j2 = Jelly("j2", JellyType.BLUEBERRY, Direction.EAST, listOf(Position(3, 2)), linkedJellyId = "j1")
        val board = Board(jellies = listOf(j1, j2)).withUpdatedEyeStates()
        val level = Level(stageNumber = 6, initialBoard = board, optimalMoves = 1, targetScore = 500)

        engine.loadLevel(level)
        assertEquals(2, engine.state.value.board.jellies.size)

        // Tap j1 -> both j1 and j2 launch synchronously!
        val result = engine.onTileTapped(Position(2, 2))
        assertIs<TapResult.Launched>(result)
        assertEquals("j1", result.jelly.id)
        assertNotNull(result.partnerJelly)
        assertEquals("j2", result.partnerJelly.id)
        assertNotNull(result.partnerExitPath)

        // Board is now completely cleared!
        assertEquals(0, engine.state.value.board.jellies.size)
        assertTrue(engine.state.value.isCompleted)
        assertTrue(result.isSolved)
    }

    @Test
    fun testTraversedPathClearsBubbleFog() {
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 2)))
        val fog = setOf(Position(2, 1), Position(5, 5))
        val board = Board(jellies = listOf(j1), fogTiles = fog).withUpdatedEyeStates()
        val level = Level(stageNumber = 8, initialBoard = board, optimalMoves = 1, targetScore = 500)

        engine.loadLevel(level)
        assertTrue(engine.state.value.board.isFoggy(Position(2, 1)))

        val result = engine.onTileTapped(Position(2, 2))
        assertIs<TapResult.Launched>(result)
        assertTrue(result.clearedFog.contains(Position(2, 1)))
        assertFalse(engine.state.value.board.isFoggy(Position(2, 1)))
        assertTrue(engine.state.value.board.isFoggy(Position(5, 5)))
    }
}

