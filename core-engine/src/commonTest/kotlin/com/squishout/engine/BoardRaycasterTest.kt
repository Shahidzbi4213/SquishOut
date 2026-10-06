package com.squishout.engine

import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.EyeState
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.ObstacleType
import com.squishout.engine.model.Position
import com.squishout.engine.model.WaterJet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardRaycasterTest {

    @Test
    fun testUnblockedJellyIsAwake() {
        // Strawberry at (2, 0) facing North has direct exit at top
        val strawberry = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 0)))
        val board = Board(jellies = listOf(strawberry)).withUpdatedEyeStates()

        assertTrue(board.canJellyEscape(strawberry))
        assertEquals(EyeState.AWAKE, board.jellies.first().eyeState)
    }

    @Test
    fun testBlockedJellyBehindAnotherJellyIsAsleep() {
        // Strawberry 1 at (2, 0) facing North
        val front = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 0)))
        // Strawberry 2 at (2, 2) facing North (blocked by j1)
        val back = Jelly("j2", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 2)))

        val board = Board(jellies = listOf(front, back)).withUpdatedEyeStates()

        assertTrue(board.canJellyEscape(front))
        assertFalse(board.canJellyEscape(back))
        assertEquals(EyeState.AWAKE, board.getJellyById("j1")?.eyeState)
        assertEquals(EyeState.ASLEEP, board.getJellyById("j2")?.eyeState)

        // When front jelly is removed, back jelly becomes AWAKE
        val updatedBoard = board.removeJelly("j1")
        assertEquals(EyeState.AWAKE, updatedBoard.getJellyById("j2")?.eyeState)
    }

    @Test
    fun testJellyBlockedByObstacleIsAsleep() {
        val rock = Obstacle("rock1", ObstacleType.ROCK_MOUNTAIN, Position(2, 1))
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 3)))

        val board = Board(jellies = listOf(jelly), obstacles = listOf(rock)).withUpdatedEyeStates()

        assertFalse(board.canJellyEscape(jelly))
        assertEquals(EyeState.ASLEEP, board.jellies.first().eyeState)
    }

    @Test
    fun testMultiCellGrapeEelUnblocking() {
        // 1x2 Grape Eel facing North at (3, 2) and (3, 3)
        val grapeEel = Jelly(
            "eel",
            JellyType.GRAPE_EEL,
            Direction.NORTH,
            listOf(Position(3, 2), Position(3, 3))
        )
        val board = Board(jellies = listOf(grapeEel)).withUpdatedEyeStates()

        assertTrue(board.canJellyEscape(grapeEel))
        assertEquals(EyeState.AWAKE, board.jellies.first().eyeState)
    }

    @Test
    fun testWaterJet90DegreeDeflectionAllowsEscape() {
        // Jelly at (1, 3) facing NORTH
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 3)))
        // Obstacle at (1, 0) blocks straight northern exit
        val rock = Obstacle("rock", ObstacleType.ROCK_MOUNTAIN, Position(1, 0))
        // WaterJet at (1, 1) deflects trajectory 90 degrees EAST
        val jet = WaterJet(Position(1, 1), Direction.EAST)

        val board = Board(
            jellies = listOf(jelly),
            obstacles = listOf(rock),
            waterJets = listOf(jet)
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
    fun testWaterJetLoopDetectedDoesNotEscape() {
        // Whirlpool loop of 4 water jets
        val jets = listOf(
            WaterJet(Position(1, 1), Direction.EAST),
            WaterJet(Position(2, 1), Direction.SOUTH),
            WaterJet(Position(2, 2), Direction.WEST),
            WaterJet(Position(1, 2), Direction.NORTH)
        )
        // Jelly at (1, 3) faces NORTH into the whirlpool
        val jelly = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(1, 3)))

        val board = Board(jellies = listOf(jelly), waterJets = jets).withUpdatedEyeStates()

        // Loop is safely detected and jelly cannot escape
        assertFalse(board.canJellyEscape(jelly))
        assertEquals(EyeState.ASLEEP, board.getJellyById("j1")?.eyeState)
    }

    @Test
    fun testSymbioticSchoolingPairSynchronizedUnblock() {
        // j1 at (2, 2) facing NORTH, j2 at (3, 2) facing NORTH, linked together
        val j1 = Jelly("j1", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(2, 2)), linkedJellyId = "j2")
        val j2 = Jelly("j2", JellyType.STRAWBERRY, Direction.NORTH, listOf(Position(3, 2)), linkedJellyId = "j1")

        // Rock at (2, 0) blocks j1
        val rock = Obstacle("rock", ObstacleType.ROCK_MOUNTAIN, Position(2, 0))

        val board = Board(jellies = listOf(j1, j2), obstacles = listOf(rock)).withUpdatedEyeStates()

        // Because j1 is blocked, BOTH j1 and j2 are ASLEEP
        assertFalse(board.canJellyEscape(j1))
        assertFalse(board.canJellyEscape(j2))
        assertEquals(EyeState.ASLEEP, board.getJellyById("j1")?.eyeState)
        assertEquals(EyeState.ASLEEP, board.getJellyById("j2")?.eyeState)

        // Now remove the rock
        val clearBoard = Board(jellies = listOf(j1, j2)).withUpdatedEyeStates()
        // Both are unblocked -> both AWAKE
        assertTrue(clearBoard.canJellyEscape(j1))
        assertTrue(clearBoard.canJellyEscape(j2))
        assertEquals(EyeState.AWAKE, clearBoard.getJellyById("j1")?.eyeState)
        assertEquals(EyeState.AWAKE, clearBoard.getJellyById("j2")?.eyeState)
    }

    @Test
    fun testBubbleFogClearing() {
        val fog = setOf(Position(2, 1), Position(5, 5))
        val board = Board(fogTiles = fog)

        // Squishy launches through (2, 2) and (2, 1)
        val (clearedBoard, clearedTiles) = board.clearFogAdjacentTo(setOf(Position(2, 2), Position(2, 1)))

        assertTrue(clearedTiles.contains(Position(2, 1)))
        assertFalse(clearedBoard.isFoggy(Position(2, 1)))
        assertTrue(clearedBoard.isFoggy(Position(5, 5)))
    }
}
