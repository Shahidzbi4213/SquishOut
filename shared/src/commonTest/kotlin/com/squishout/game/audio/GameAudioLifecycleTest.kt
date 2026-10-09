package com.squishout.game.audio

import com.squishout.game.data.FakeDailyPuzzleDao
import com.squishout.game.data.FakeJellySkinDao
import com.squishout.game.data.FakeLevelDao
import com.squishout.game.data.FakeUserSessionDao
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.data.repository.GameRepository
import com.squishout.game.presentation.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameAudioLifecycleTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)
    private lateinit var sessionDao: FakeUserSessionDao
    private lateinit var repository: GameRepository
    private lateinit var viewModel: GameViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionDao = FakeUserSessionDao()
        repository = GameRepository(
            levelDao = FakeLevelDao(),
            skinDao = FakeJellySkinDao(),
            sessionDao = sessionDao,
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
    fun testInitialSessionHasMusicEnabled() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.sessionState.value.musicEnabled)
        assertTrue(repository.session.value.musicEnabled)
    }

    @Test
    fun testAudioPausingDoesNotMutateMusicEnabledPreference() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.sessionState.value.musicEnabled)

        // Simulate app backgrounding
        viewModel.pauseAllAudio()
        advanceUntilIdle()

        // Setting MUST remain enabled
        assertTrue(viewModel.sessionState.value.musicEnabled)
        assertTrue(repository.session.value.musicEnabled)

        viewModel.pauseMusic()
        advanceUntilIdle()
        assertTrue(viewModel.sessionState.value.musicEnabled)

        // Simulate app foregrounding
        viewModel.resumeAllAudio()
        advanceUntilIdle()
        assertTrue(viewModel.sessionState.value.musicEnabled)

        viewModel.resumeMusic()
        advanceUntilIdle()
        assertTrue(viewModel.sessionState.value.musicEnabled)
    }

    @Test
    fun testAudioPausingWhenUserDisabledMusicPreservesDisabledState() = runTest(testDispatcher) {
        advanceUntilIdle()

        // User explicitly disables music in settings
        viewModel.toggleMusic(false)
        advanceUntilIdle()

        assertFalse(viewModel.sessionState.value.musicEnabled)
        assertFalse(repository.session.value.musicEnabled)

        // Background app
        viewModel.pauseAllAudio()
        advanceUntilIdle()
        assertFalse(viewModel.sessionState.value.musicEnabled)

        // Foreground app
        viewModel.resumeAllAudio()
        advanceUntilIdle()

        // Setting MUST still remain false
        assertFalse(viewModel.sessionState.value.musicEnabled)
        assertFalse(repository.session.value.musicEnabled)
    }

    @Test
    fun testToggleMusicUpdatesSessionAndPersistsAcrossAudioLifecycleCalls() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.toggleMusic(false)
        advanceUntilIdle()
        assertFalse(viewModel.sessionState.value.musicEnabled)

        viewModel.pauseMusic()
        viewModel.resumeMusic()
        advanceUntilIdle()
        assertFalse(viewModel.sessionState.value.musicEnabled)

        viewModel.toggleMusic(true)
        advanceUntilIdle()
        assertTrue(viewModel.sessionState.value.musicEnabled)

        viewModel.pauseAllAudio()
        viewModel.resumeAllAudio()
        advanceUntilIdle()
        assertTrue(viewModel.sessionState.value.musicEnabled)
    }
}
