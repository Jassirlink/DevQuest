package com.devquest.motion

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.*
import org.junit.Test

class MotionTest {
    @Test
    fun cameraAndFrameBounds() {
        assertEquals(25f, MotionMath.tilt(100f))
        assertEquals(-25f, MotionMath.tilt(-100f))
        assertEquals(0f, MotionMath.tilt(Float.NaN))
        assertEquals(1f, MotionMath.advance(0.99f, 5f))
        assertEquals(3, MotionMath.phase(1f))
    }

    @Test
    fun projectionRoundTrip() {
        val h = FloatArray(9)
        Homography.of(10f, 20f, 0f, 1f, 0f, 0f, 0f, 1f, 0.2f, 50f, 50f, 300f, h)
        val p = Homography.project(h, 25f, 30f)
        val result = Homography.unproject(h, p)!!
        assertEquals(25f, result.x, 0.001f)
        assertEquals(30f, result.y, 0.001f)
    }

    @Test
    fun singularInverseIsRejected() {
        assertNull(Homography.unproject(FloatArray(9), Offset.Zero))
    }
}
