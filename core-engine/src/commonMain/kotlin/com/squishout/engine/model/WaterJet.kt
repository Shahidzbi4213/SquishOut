package com.squishout.engine.model

/**
 * 90-degree Trajectory Deflector.
 * When a squishy traverses this conveyor cell on the tray floor, its exit trajectory
 * curves into [direction] rather than being blocked.
 */
data class WaterJet(
    val position: Position,
    val direction: Direction
)
