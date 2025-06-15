package com.dzaky3022.asesment1.ui.component.waterdrops.plottedpoints

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.dzaky3022.asesment1.ui.component.waterdrops.LevelState
import com.dzaky3022.asesment1.utils.Parabola
import com.dzaky3022.asesment1.utils.PointF

@Composable
fun createParabolaAsState(
    position: Offset,
    elementSize: IntSize,
    waterLevel: Float,
    buffer: Float,
    levelState: LevelState,
    dropWaterDuration: Int,
): State<Parabola> {

    val parabolaHeightMultiplier = animateFloatAsState(
        targetValue = if (levelState == LevelState.WaveIsComing) 0f else -1f,
        animationSpec = tween(
            durationMillis = dropWaterDuration,
            easing = { OvershootInterpolator(6f).getInterpolation(it) }
        ),
        label = "",
    )

    val point1 by remember(position, elementSize, waterLevel, parabolaHeightMultiplier) {
        mutableStateOf(
            PointF(
                position.x,
                waterLevel + (elementSize.height / 3f + buffer / 5) * parabolaHeightMultiplier.value
            )
        )
    }

    val point2 by remember(position, elementSize, waterLevel, parabolaHeightMultiplier) {
        mutableStateOf(
            PointF(
                position.x + elementSize.width,
                waterLevel + (elementSize.height / 3f + buffer / 5) * parabolaHeightMultiplier.value
            )
        )
    }

    val point3 by remember(position, elementSize, parabolaHeightMultiplier, waterLevel) {
        mutableStateOf(
            PointF(
                position.x + elementSize.width / 2,
                waterLevel + (elementSize.height / 3f + buffer) * parabolaHeightMultiplier.value
            )
        )
    }

    return remember(point1, point2, point3) {
        derivedStateOf {
            Parabola(point1, point2, point3)
        }
    }
}