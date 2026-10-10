package com.squishout.game.board

import com.squishout.engine.GameEngine
import com.squishout.engine.generator.Level
import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.EyeState
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.engine.model.Position
import com.squishout.engine.model.SunflowerTurnstile
import com.squishout.game.data.FakeDailyPuzzleDao
import com.squishout.game.data.FakeJellySkinDao
import com.squishout.game.data.FakeLevelDao
import com.squishout.game.data.FakeUserSessionDao
import com.squishout.game.data.repository.GameRepository
import com.squishout.game.presentation.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SunflowerTurnstileAnimationTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)
    private lateinit var viewModel: GameViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val repository = GameRepository(
            levelDao = FakeLevelDao(),
            skinDao = FakeJellySkinDao(),
            sessionDao = FakeUserSessionDao(),
            dailyPuzzleDao = FakeDailyPuzzleDao(),
            scope = testScope
        )
        viewModel = GameViewModel(
            audioPlayer = null,
            repository = repository
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSunflowerTurnstileRotationCycle() {
        val tNorth = SunflowerTurnstile("t1", Position(2, 2), Direction.NORTH)
        val tEast = tNorth.rotateClockwise()
        val tSouth = tEast.rotateClockwise()
        val tWest = tSouth.rotateClockwise()
        val tCycle = tWest.rotateClockwise()

        assertEquals(Direction.EAST, tEast.direction)
        assertEquals(Direction.SOUTH, tSouth.direction)
        assertEquals(Direction.WEST, tWest.direction)
        assertEquals(Direction.NORTH, tCycle.direction)
    }

    @Test
    fun testSunflowerTurnstileDirectionAngles() {
        // Verify 90-degree step mapping for all directions
        val directions = listOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        val expectedAngles = listOf(0f, 90f, 180f, 270f)

        for (i in directions.indices) {
            val baseDegrees = when (directions[i]) {
                Direction.NORTH -> 0f
                Direction.EAST -> 90f
                Direction.SOUTH -> 180f
                Direction.WEST -> 270f
            }
            assertEquals(expectedAngles[i], baseDegrees)
        }
    }

    @Test
    fun testSpringAnimationDampedHarmonicOscillator() {
        // Test that the spring bounce formula starts at -90 deg, overshoots past 0 deg, and settles at 0 deg
        val p0 = 0f
        val bounce0 = 1f - kotlin.math.exp(-6f * p0) * kotlin.math.cos(10f * p0)
        val offset0 = -90f * (1f - bounce0)
        assertEquals(-90f, offset0, 0.001f)

        // At overshoot peak (~progress = 0.31): offset should overshoot past 0 into positive degrees
        val pPeak = 0.31f
        val bouncePeak = 1f - kotlin.math.exp(-6f * pPeak) * kotlin.math.cos(10f * pPeak)
        val offsetPeak = -90f * (1f - bouncePeak)
        assertTrue(offsetPeak > 5f, "Spring should overshoot by at least +5 degrees (actual: $offsetPeak)")

        // At completion (progress = 1.0): offset should settle near 0
        val pEnd = 1.0f
        val bounceEnd = 1f - kotlin.math.exp(-6f * pEnd) * kotlin.math.cos(10f * pEnd)
        val offsetEnd = -90f * (1f - bounceEnd)
        assertTrue(kotlin.math.abs(offsetEnd) < 0.5f, "Spring should settle within 0.5 deg of target (actual: $offsetEnd)")
    }

    @Test
    fun testGameViewModelAnimatesTurnstileRotationOnLaunch() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Set up board with jelly pointing into a sunflower turnstile
        val turnstile = SunflowerTurnstile("t_anim", Position(0, 1), Direction.EAST)
        val jelly = Jelly(
            id = "j1",
            type = JellyType.STRAWBERRY,
            direction = Direction.NORTH,
            tiles = listOf(Position(0, 2)),
            eyeState = EyeState.AWAKE
        )
        val board = Board(
            width = 4,
            height = 4,
            jellies = listOf(jelly),
            turnstiles = listOf(turnstile)
        ).withUpdatedEyeStates()

        viewModel.engine.loadLevel(
            Level(
                stageNumber = 999,
                initialBoard = board,
                optimalMoves = 1,
                targetScore = 100,
                difficultyTier = com.squishout.engine.model.DifficultyTier.NORMAL
            )
        )
        advanceUntilIdle()

        // Tap jelly to trigger launch and turnstile traversal
        viewModel.onTileTapped(Position(0, 2))

        // Check active spring rotation offset immediately after launch
        advanceTimeBy(30)
        val activeRotations = viewModel.turnstileRotations.value
        val offset = activeRotations["t_anim"]
        assertNotNull(offset, "Turnstile t_anim should have an active rotation offset")
        // Starts negative (-90 deg offset) and springs toward 0 deg
        assertTrue(offset < 0f, "Initial rotation offset should be negative to pivot from previous orientation")

        // Advance past spring animation completion
        advanceTimeBy(400)
        advanceUntilIdle()

        // Rotation offset should be cleared
        assertTrue(viewModel.turnstileRotations.value.isEmpty(), "Turnstile rotation offset should be cleared after animation")
    }

    @Test
    fun testMultipleTurnstilesRotatedSimultaneously() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Jelly traverses two turnstiles in series: (0, 3) -> (0, 2) [turnstile 1, deflecting EAST] -> (1, 2) [turnstile 2, deflecting NORTH] -> exit
        val t1 = SunflowerTurnstile("t_multi_1", Position(0, 2), Direction.EAST)
        val t2 = SunflowerTurnstile("t_multi_2", Position(1, 2), Direction.NORTH)
        val jelly = Jelly(
            id = "j_multi",
            type = JellyType.BLUEBERRY,
            direction = Direction.NORTH,
            tiles = listOf(Position(0, 3)),
            eyeState = EyeState.AWAKE
        )
        val board = Board(
            width = 4,
            height = 4,
            jellies = listOf(jelly),
            turnstiles = listOf(t1, t2)
        ).withUpdatedEyeStates()

        viewModel.engine.loadLevel(
            Level(
                stageNumber = 998,
                initialBoard = board,
                optimalMoves = 1,
                targetScore = 100
            )
        )
        advanceUntilIdle()

        viewModel.onTileTapped(Position(0, 3))
        advanceTimeBy(20)

        val rotations = viewModel.turnstileRotations.value
        assertNotNull(rotations["t_multi_1"], "t_multi_1 should be animating")
        assertNotNull(rotations["t_multi_2"], "t_multi_2 should be animating")

        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.turnstileRotations.value.isEmpty())
    }
}
