package com.squishout.engine

import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.EyeState
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.ObstacleType
import com.squishout.engine.model.Position
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
}
