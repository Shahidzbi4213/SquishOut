package com.squishout.engine

import com.squishout.engine.generator.Level
import com.squishout.engine.model.Board
import com.squishout.engine.model.EyeState
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface TapResult {
    data class Launched(
        val jelly: Jelly,
        val exitPath: List<Position>,
        val pointsEarned: Int,
        val isSolved: Boolean,
        val stars: Int,
        val shatteredObstacles: List<Obstacle> = emptyList(),
        val clearedFog: Set<Position> = emptySet(),
        val partnerJelly: Jelly? = null,
        val partnerExitPath: List<Position>? = null
    ) : TapResult

    data class Blocked(
        val jelly: Jelly,
        val blockerPosition: Position?
    ) : TapResult

    data object EmptyTile : TapResult
    data object GameOver : TapResult
}

data class GameState(
    val stageNumber: Int = 1,
    val board: Board = Board(),
    val movesUsed: Int = 0,
    val optimalMoves: Int = 0,
    val score: Int = 0,
    val hearts: Int = 3,
    val maxHearts: Int = 3,
    val undoCount: Int = 5,
    val hintCount: Int = 3,
    val wandCount: Int = 1,
    val highlightedJellyId: String? = null,
    val isCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val starsEarned: Int = 0,
    val history: List<Board> = emptyList()
)

class GameEngine {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    fun loadLevel(level: Level) {
        _state.value = GameState(
            stageNumber = level.stageNumber,
            board = level.initialBoard.withUpdatedEyeStates(),
            movesUsed = 0,
            optimalMoves = level.optimalMoves,
            score = 0,
            hearts = 3,
            undoCount = 5,
            hintCount = 3,
            wandCount = 1,
            highlightedJellyId = null,
            isCompleted = false,
            isGameOver = false,
            starsEarned = 0,
            history = emptyList()
        )
    }

    /**
     * Handles tapping on a grid coordinate.
     * Direct manipulation: If an unblocked jelly is tapped, it launches immediately.
     * If part of a Symbiotic Schooling Pair, both partners launch synchronously!
     */
    fun onTileTapped(pos: Position): TapResult {
        val current = _state.value
        if (current.isCompleted || current.isGameOver) {
            return if (current.isGameOver) TapResult.GameOver else TapResult.EmptyTile
        }

        val jelly = current.board.getJellyAt(pos) ?: return TapResult.EmptyTile

        return if (jelly.eyeState == EyeState.AWAKE) {
            // 1. Unblocked -> Launch!
            val exitPath = current.board.getEscapePath(jelly)
            val partner = jelly.linkedJellyId?.let { current.board.getJellyById(it) }
            val partnerExitPath = partner?.let { current.board.getEscapePath(it) }

            val jelliesToRemove = if (partner != null) setOf(jelly.id, partner.id) else setOf(jelly.id)
            val afterRemoval = current.board.removeJellies(jelliesToRemove)

            // Collect all traversed tiles from launched jelly (and partner if linked)
            val traversedJellyTiles = jelly.tiles + current.board.getAllEscapeTiles(jelly)
            val traversedPartnerTiles = if (partner != null) {
                partner.tiles + current.board.getAllEscapeTiles(partner)
            } else emptyList()
            val allTraversed = (traversedJellyTiles + traversedPartnerTiles).toSet()

            // Damage adjacent crackable obstacles
            val (boardAfterDamage, shattered) = afterRemoval.damageObstaclesAdjacentTo(allTraversed)
            // Clear adjacent bubble fog
            val (updatedBoard, clearedFog) = boardAfterDamage.clearFogAdjacentTo(allTraversed)

            val isSolved = updatedBoard.isSolved
            val schoolingBonus = if (partner != null) 150 else 0
            val bossBonus = if (jelly.isBoss) 300 else 0
            val points = 100 + (current.movesUsed * 5) + (shattered.size * 50) + (clearedFog.size * 25) + schoolingBonus + bossBonus
            val newMoves = current.movesUsed + 1
            val stars = if (isSolved) calculateStars(newMoves, current.optimalMoves) else 0

            _state.update { prev ->
                prev.copy(
                    board = updatedBoard,
                    movesUsed = newMoves,
                    score = prev.score + points,
                    highlightedJellyId = null,
                    isCompleted = isSolved,
                    starsEarned = stars,
                    history = prev.history + prev.board
                )
            }

            TapResult.Launched(
                jelly = jelly,
                exitPath = exitPath,
                pointsEarned = points,
                isSolved = isSolved,
                stars = stars,
                shatteredObstacles = shattered,
                clearedFog = clearedFog,
                partnerJelly = partner,
                partnerExitPath = partnerExitPath
            )
        } else {
            // 2. Blocked -> Wobble refusal and deduce heart
            val blocker = findFirstBlocker(jelly, current.board)
            val newHearts = (current.hearts - 1).coerceAtLeast(0)
            val isGameOver = newHearts <= 0
            _state.update { prev ->
                prev.copy(
                    hearts = newHearts,
                    isGameOver = isGameOver,
                    highlightedJellyId = null
                )
            }
            TapResult.Blocked(jelly = jelly, blockerPosition = blocker)
        }
    }

    /**
     * Booster 1: Undo the last launched jelly.
     */
    fun useUndo(): Boolean {
        val current = _state.value
        if (current.undoCount <= 0 || current.history.isEmpty() || current.isCompleted) {
            return false
        }

        val previousBoard = current.history.last()
        val newHistory = current.history.dropLast(1)

        _state.update { prev ->
            prev.copy(
                board = previousBoard.withUpdatedEyeStates(),
                movesUsed = (prev.movesUsed - 1).coerceAtLeast(0),
                undoCount = prev.undoCount - 1,
                highlightedJellyId = null,
                history = newHistory
            )
        }
        return true
    }

    /**
     * Booster 2: Hint. Highlights one awake jelly to nudge the player.
     */
    fun useHint(): String? {
        val current = _state.value
        if (current.hintCount <= 0 || current.isCompleted) {
            return null
        }

        val awake = current.board.awakeJellies
        val candidate = awake.firstOrNull() ?: return null

        _state.update { prev ->
            prev.copy(
                hintCount = prev.hintCount - 1,
                highlightedJellyId = candidate.id
            )
        }
        return candidate.id
    }

    /**
     * Booster 3: Magic Wand. Vaporizes one asleep blocker jelly.
     */
    fun useMagicWand(): Jelly? {
        val current = _state.value
        if (current.wandCount <= 0 || current.isCompleted) {
            return null
        }

        val asleepJellies = current.board.jellies.filter { it.eyeState == EyeState.ASLEEP }
        val target = asleepJellies.firstOrNull() ?: return null

        val updatedBoard = current.board.removeJelly(target.id)
        _state.update { prev ->
            prev.copy(
                board = updatedBoard,
                wandCount = prev.wandCount - 1,
                highlightedJellyId = null,
                isCompleted = updatedBoard.isSolved,
                history = prev.history + prev.board
            )
        }
        return target
    }

    fun addBoosters(undo: Int = 0, hint: Int = 0, wand: Int = 0) {
        _state.update { prev ->
            prev.copy(
                undoCount = prev.undoCount + undo,
                hintCount = prev.hintCount + hint,
                wandCount = prev.wandCount + wand
            )
        }
    }

    fun refillHearts() {
        _state.update { prev ->
            prev.copy(hearts = prev.maxHearts, isGameOver = false)
        }
    }

    private fun findFirstBlocker(jelly: Jelly, board: Board): Position? {
        val path = board.getEscapePath(jelly)
        val partnerId = jelly.linkedJellyId
        for (pos in path) {
            val other = board.getJellyAt(pos)
            if (other != null && other.id != jelly.id && other.id != partnerId) return pos
            if (board.getObstacleAt(pos) != null) return pos
        }
        if (partnerId != null) {
            val partner = board.getJellyById(partnerId)
            if (partner != null) {
                val partnerPath = board.getEscapePath(partner)
                for (pos in partnerPath) {
                    val other = board.getJellyAt(pos)
                    if (other != null && other.id != partner.id && other.id != jelly.id) return pos
                    if (board.getObstacleAt(pos) != null) return pos
                }
            }
        }
        return null
    }

    private fun calculateStars(movesUsed: Int, optimalMoves: Int): Int {
        val diff = movesUsed - optimalMoves
        return when {
            diff <= 1 -> 3
            diff <= 4 -> 2
            else -> 1
        }
    }
}
