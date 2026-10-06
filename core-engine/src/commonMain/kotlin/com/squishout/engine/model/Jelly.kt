package com.squishout.engine.model

enum class JellyType(
    val defaultDirection: Direction,
    val hexColor: String,
    val displayName: String,
    val length: Int = 1
) {
    STRAWBERRY(Direction.NORTH, "#FF4D6D", "Strawberry Blobby"),
    BLUEBERRY(Direction.EAST, "#00B4D8", "Blueberry Drop"),
    LEMON(Direction.WEST, "#FFB703", "Lemon Zest"),
    KIWI(Direction.SOUTH, "#06D6A0", "Kiwi Gummy"),
    GRAPE_EEL(Direction.NORTH, "#9D4EDD", "Grape Eel Duo", length = 2);
}

enum class EyeState {
    AWAKE,  // Unblocked path to board edge: eyes sparkling ( ✦‿✦ ), rim glowing
    ASLEEP  // Path blocked: eyes peacefully closed ( ˘◡˘ )
}

data class Jelly(
    val id: String,
    val type: JellyType,
    val direction: Direction,
    val tiles: List<Position>,
    val eyeState: EyeState = EyeState.ASLEEP,
    val linkedJellyId: String? = null
) {
    init {
        require(tiles.isNotEmpty()) { "Jelly must occupy at least one tile" }
    }

    val isLinked: Boolean get() = linkedJellyId != null

    val headPosition: Position
        get() = when (direction) {
            Direction.NORTH -> tiles.minByOrNull { it.y } ?: tiles.first()
            Direction.SOUTH -> tiles.maxByOrNull { it.y } ?: tiles.first()
            Direction.WEST -> tiles.minByOrNull { it.x } ?: tiles.first()
            Direction.EAST -> tiles.maxByOrNull { it.x } ?: tiles.first()
        }

    fun occupies(pos: Position): Boolean = pos in tiles

    fun withEyeState(newState: EyeState): Jelly = copy(eyeState = newState)

    fun withLinkedJellyId(newLinkedId: String?): Jelly = copy(linkedJellyId = newLinkedId)
}

enum class ObstacleType(val symbol: String, val hexColor: String) {
    ROCK_MOUNTAIN("▲", "#4A5568"),
    ROCK_TREE("♣", "#2D3748"),
    ICE_BLOCK("❄", "#93C5FD"),
    HONEY_POT("🍯", "#F59E0B")
}

data class Obstacle(
    val id: String,
    val type: ObstacleType,
    val position: Position,
    val health: Int = 1,
    val maxHealth: Int = 1
) {
    val isCrackable: Boolean get() = type == ObstacleType.ICE_BLOCK || type == ObstacleType.HONEY_POT
}
