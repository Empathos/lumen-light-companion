package org.lumen.lightcompanion.domain.event

data class NormalizedCoordinate(val x: Float, val y: Float) {
    init {
        require(x.isFinite() && y.isFinite()) { "Coordinates must be finite." }
        require(x in 0f..1f && y in 0f..1f) { "Coordinates must be normalized." }
    }
}

data class NormalizedDelta(val dx: Float, val dy: Float) {
    init {
        require(dx.isFinite() && dy.isFinite()) { "Deltas must be finite." }
    }
}
