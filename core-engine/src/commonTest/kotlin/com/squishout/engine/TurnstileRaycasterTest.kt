package com.squishout.engine

import com.squishout.engine.generator.Level
import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.EyeState
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.ObstacleType
import com.squishout.engine.model.Position
import com.squishout.engine.model.SunflowerTurnstile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TurnstileRaycasterTest {

    @Test
    fun testTurnstileDeflectsCorridor90Degrees() {
        // Strawberry jelly at (1, 3) facing NORTH
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 3)))
        // Obstacle at (1, 0) blocks straight northern exit
        val rock = Obstacle("rock", ObstacleType.ROCK_MOUNTAIN, Position(1, 0))
        // Sunflower turnstile at (1, 1) deflects trajectory 90 degrees EAST
        val turnstile = SunflowerTurnstile("t1", Position(1, 1), Direction.EAST)

        val board = Board(
            jellies = listOf(jelly),
            obstacles = listOf(rock),
            turnstiles = listOf(turnstile)
        ).withUpdatedEyeStates()

        val path = board.getEscapePath(jelly)
        // Path should step (1, 2) -> (1, 1) -> (2, 1) -> (3, 1) -> (4, 1) -> (5, 1) -> (6, 1)
        assertTrue(path.contains(Position(1, 1)))
        assertTrue(path.contains(Position(2, 1)))
        assertTrue(path.contains(Position(6, 1))) // Exited East off board!

        assertTrue(board.canJellyEscape(jelly))
        assertEquals(EyeState.AWAKE, board.getJellyById("j1")?.eyeState)
    }

    @Test
    fun testMultipleTurnstilesChainDeflections() {
        // Jelly at (1, 5) facing NORTH
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 5)))
        // Obstacles blocking straight exits if not deflected
        val rockNorth = Obstacle("rockN", ObstacleType.ROCK_MOUNTAIN, Position(1, 0))
        val rockEast = Obstacle("rockE", ObstacleType.ROCK_MOUNTAIN, Position(5, 3))
        // Turnstile 1 at (1, 3) deflects EAST
        val t1 = SunflowerTurnstile("t1", Position(1, 3), Direction.EAST)
        // Turnstile 2 at (4, 3) deflects SOUTH
        val t2 = SunflowerTurnstile("t2", Position(4, 3), Direction.SOUTH)
        // Turnstile 3 at (4, 4) deflects EAST
        val t3 = SunflowerTurnstile("t3", Position(4, 4), Direction.EAST)

        val board = Board(
            jellies = listOf(jelly),
            obstacles = listOf(rockNorth, rockEast),
            turnstiles = listOf(t1, t2, t3)
        ).withUpdatedEyeStates()

        val path = board.getEscapePath(jelly)
        val expectedSequence = listOf(
            Position(1, 4),
            Position(1, 3), // t1 deflects EAST
            Position(2, 3),
            Position(3, 3),
            Position(4, 3), // t2 deflects SOUTH
            Position(4, 4), // t3 deflects EAST
            Position(5, 4),
            Position(6, 4)  // Exit off board
        )
        assertEquals(expectedSequence, path)
        assertTrue(board.canJellyEscape(jelly))
        assertEquals(EyeState.AWAKE, board.getJellyById("j1")?.eyeState)

        // Verify all three turnstiles are included in getAllEscapeTiles
        val escapeTiles = board.getAllEscapeTiles(jelly)
        assertTrue(Position(1, 3) in escapeTiles)
        assertTrue(Position(4, 3) in escapeTiles)
        assertTrue(Position(4, 4) in escapeTiles)
    }

    @Test
    fun testEscapingThroughTurnstileRotatesItClockwise() {
        // Jelly at (1, 3) facing NORTH
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 3)))
        val rock = Obstacle("rock", ObstacleType.ROCK_MOUNTAIN, Position(1, 0))
        // Turnstile at (1, 1) initially facing EAST
        val turnstile = SunflowerTurnstile("t1", Position(1, 1), Direction.EAST)

        val board = Board(
            jellies = listOf(jelly),
            obstacles = listOf(rock),
            turnstiles = listOf(turnstile)
        ).withUpdatedEyeStates()

        val engine = GameEngine()
        engine.loadLevel(Level(stageNumber = 1, initialBoard = board, optimalMoves = 1, targetScore = 100))

        // Tap the awake jelly
        val result = engine.onTileTapped(Position(1, 3))
        assertIs<TapResult.Launched>(result)

        // Turnstile rotated clockwise from EAST to SOUTH
        assertEquals(1, result.rotatedTurnstiles.size)
        val rotated = result.rotatedTurnstiles.first()
        assertEquals("t1", rotated.id)
        assertEquals(Direction.SOUTH, rotated.direction)

        // State board has the rotated turnstile
        val turnstileOnBoard = engine.state.value.board.getTurnstileAt(Position(1, 1))
        assertEquals(Direction.SOUTH, turnstileOnBoard?.direction)
    }

    @Test
    fun testTurnstileRotationCycleClockwise() {
        val t = SunflowerTurnstile("t", Position(0, 0), Direction.NORTH)
        val tEast = t.rotateClockwise()
        assertEquals(Direction.EAST, tEast.direction)
        val tSouth = tEast.rotateClockwise()
        assertEquals(Direction.SOUTH, tSouth.direction)
        val tWest = tSouth.rotateClockwise()
        assertEquals(Direction.WEST, tWest.direction)
        val tNorth = tWest.rotateClockwise()
        assertEquals(Direction.NORTH, tNorth.direction)
    }

    @Test
    fun testTurnstileRotationCausesBlockedJelliesToWakeUpOrFallAsleep() {
        // Jelly at (2, 4) facing NORTH
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 4)))
        // Obstacle at (2, 0) blocks straight NORTH exit, obstacle at (2, 5) blocks straight SOUTH exit
        val rockNorth = Obstacle("rockN", ObstacleType.ROCK_MOUNTAIN, Position(2, 0))
        val rockSouth = Obstacle("rockS", ObstacleType.ROCK_MOUNTAIN, Position(2, 5))
        // Turnstile at (2, 2) initially facing NORTH (deflects into rockNorth)
        val turnstile = SunflowerTurnstile("t1", Position(2, 2), Direction.NORTH)

        // 1. Initial: turnstile NORTH -> jelly blocked by rockNorth -> ASLEEP
        val board1 = Board(
            jellies = listOf(jelly),
            obstacles = listOf(rockNorth, rockSouth),
            turnstiles = listOf(turnstile)
        ).withUpdatedEyeStates()
        assertFalse(board1.canJellyEscape(jelly))
        assertEquals(EyeState.ASLEEP, board1.getJellyById("j1")?.eyeState)

        // 2. Rotate to EAST: jelly deflects EAST into open boundary -> WAKES UP (AWAKE)
        val (board2, rotated1) = board1.rotateTurnstiles(setOf(Position(2, 2)))
        assertEquals(1, rotated1.size)
        assertEquals(Direction.EAST, rotated1.first().direction)
        assertTrue(board2.canJellyEscape(jelly))
        assertEquals(EyeState.AWAKE, board2.getJellyById("j1")?.eyeState)

        // 3. Rotate to SOUTH: jelly deflects SOUTH into rockSouth -> FALLS ASLEEP (ASLEEP)
        val (board3, rotated2) = board2.rotateTurnstiles(setOf(Position(2, 2)))
        assertEquals(1, rotated2.size)
        assertEquals(Direction.SOUTH, rotated2.first().direction)
        assertFalse(board3.canJellyEscape(jelly))
        assertEquals(EyeState.ASLEEP, board3.getJellyById("j1")?.eyeState)

        // 4. Rotate to WEST: jelly deflects WEST into open boundary -> WAKES UP (AWAKE)
        val (board4, rotated3) = board3.rotateTurnstiles(setOf(Position(2, 2)))
        assertEquals(1, rotated3.size)
        assertEquals(Direction.WEST, rotated3.first().direction)
        assertTrue(board4.canJellyEscape(jelly))
        assertEquals(EyeState.AWAKE, board4.getJellyById("j1")?.eyeState)
    }

    @Test
    fun testTurnstileRotationInGameEngineCausesJellyToFallAsleep() {
        // Jelly 1 at (1, 3) facing NORTH: corridor steps to (1, 1) [t1 facing EAST] -> exits EAST
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 3)))
        // Jelly 2 at (1, 0) facing SOUTH: corridor steps to (1, 1) [t1 facing EAST] -> exits EAST
        val j2 = Jelly("j2", JellyType.BLUEBERRY, Direction.SOUTH, listOf(Position(1, 0)))
        // Rock at (1, 5) blocking the straight SOUTH corridor
        val rockSouth = Obstacle("rockS", ObstacleType.ROCK_MOUNTAIN, Position(1, 5))
        // Turnstile at (1, 1) initially facing EAST
        val turnstile = SunflowerTurnstile("t1", Position(1, 1), Direction.EAST)

        val board = Board(
            jellies = listOf(j1, j2),
            obstacles = listOf(rockSouth),
            turnstiles = listOf(turnstile)
        ).withUpdatedEyeStates()

        val engine = GameEngine()
        engine.loadLevel(Level(stageNumber = 1, initialBoard = board, optimalMoves = 2, targetScore = 200))

        // Before launch: j2 is AWAKE (deflects East off the board)
        assertEquals(EyeState.AWAKE, engine.state.value.board.getJellyById("j2")?.eyeState)

        // Launch j1 through the turnstile at (1, 1)
        val result = engine.onTileTapped(Position(1, 3))
        assertIs<TapResult.Launched>(result)

        // Turnstile rotated from EAST to SOUTH
        val updatedBoard = engine.state.value.board
        assertEquals(Direction.SOUTH, updatedBoard.getTurnstileAt(Position(1, 1))?.direction)

        // Because turnstile now deflects SOUTH into rockSouth at (1, 5), j2 FALLS ASLEEP!
        assertEquals(EyeState.ASLEEP, updatedBoard.getJellyById("j2")?.eyeState)

        // When we undo, turnstile returns to EAST and j2 WAKES UP again!
        engine.useUndo()
        val revertedBoard = engine.state.value.board
        assertEquals(Direction.EAST, revertedBoard.getTurnstileAt(Position(1, 1))?.direction)
        assertEquals(EyeState.AWAKE, revertedBoard.getJellyById("j2")?.eyeState)
    }

    @Test
    fun testUndoRevertsTurnstileDirection() {
        // Jelly 1 at (1, 3) facing NORTH
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 3)))
        // Jelly 2 at (5, 5) facing NORTH (keeps game active after j1 launches)
        val j2 = Jelly("j2", JellyType.BLUEBERRY, Direction.NORTH, listOf(Position(5, 5)))
        val rock = Obstacle("rock", ObstacleType.ROCK_MOUNTAIN, Position(1, 0))
        val turnstile = SunflowerTurnstile("t1", Position(1, 1), Direction.EAST)

        val board = Board(
            jellies = listOf(j1, j2),
            obstacles = listOf(rock),
            turnstiles = listOf(turnstile)
        ).withUpdatedEyeStates()

        val engine = GameEngine()
        engine.loadLevel(Level(stageNumber = 1, initialBoard = board, optimalMoves = 2, targetScore = 200))

        // Initial direction: EAST
        assertEquals(Direction.EAST, engine.state.value.board.getTurnstileAt(Position(1, 1))?.direction)

        // Launch jelly 1
        val result = engine.onTileTapped(Position(1, 3))
        assertIs<TapResult.Launched>(result)

        // Game not yet completed because j2 remains
        assertFalse(engine.state.value.isCompleted)

        // After launch, turnstile rotated to SOUTH
        assertEquals(Direction.SOUTH, engine.state.value.board.getTurnstileAt(Position(1, 1))?.direction)

        // Perform Undo
        val undoSuccess = engine.useUndo()
        assertTrue(undoSuccess)

        // Turnstile direction should be restored to EAST!
        val restoredBoard = engine.state.value.board
        val restoredTurnstile = restoredBoard.getTurnstileAt(Position(1, 1))
        assertEquals(Direction.EAST, restoredTurnstile?.direction)

        // Jelly is back on board and AWAKE
        val restoredJelly = restoredBoard.getJellyById("j1")
        assertEquals(EyeState.AWAKE, restoredJelly?.eyeState)
    }

    @Test
    fun testTurnstileLoopDetectionPreventsEscape() {
        // Whirlpool loop of 4 turnstiles
        val turnstiles = listOf(
            SunflowerTurnstile("t1", Position(1, 1), Direction.EAST),
            SunflowerTurnstile("t2", Position(2, 1), Direction.SOUTH),
            SunflowerTurnstile("t3", Position(2, 2), Direction.WEST),
            SunflowerTurnstile("t4", Position(1, 2), Direction.NORTH)
        )
        // Jelly at (1, 3) faces NORTH into the whirlpool
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 3)))

        val board = Board(jellies = listOf(jelly), turnstiles = turnstiles).withUpdatedEyeStates()

        // Loop is safely detected and jelly cannot escape
        assertFalse(board.canJellyEscape(jelly))
        assertEquals(EyeState.ASLEEP, board.getJellyById("j1")?.eyeState)
    }
}
