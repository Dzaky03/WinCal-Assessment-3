package com.dzaky3022.asesment1.ui.component.waterdrops.canvas

import android.content.Context
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.ui.component.waterdrops.text.TextParams
import com.dzaky3022.asesment1.ui.screen.visual.Paths
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.Danger
import com.dzaky3022.asesment1.ui.theme.FadeBlue
import com.dzaky3022.asesment1.ui.theme.FadeGreen
import com.dzaky3022.asesment1.ui.theme.FadeRed
import com.dzaky3022.asesment1.ui.theme.FadeYellow
import com.dzaky3022.asesment1.ui.theme.Success
import com.dzaky3022.asesment1.ui.theme.Warning
import com.dzaky3022.asesment1.ui.theme.Water

fun DrawScope.drawWaves(
    paths: Paths,
    waveProgress: Float? = 0f,
) {
    val progress = waveProgress ?: 0f

    val bgColor = when {
        progress >= 1f -> FadeBlue
        progress > 0.75f -> lerp(FadeGreen, FadeBlue, (progress - 0.75f) / 0.25f)
        progress > 0.5f -> lerp(FadeYellow, FadeGreen, (progress - 0.5f) / 0.25f)
        else -> lerp(FadeRed, FadeYellow, progress / 0.5f)
    }

    val fgColor = when {
        progress >= 1f -> Water
        progress > 0.75f -> lerp(Success, Water, (progress - 0.75f) / 0.25f)
        progress > 0.5f -> lerp(Warning, Success, (progress - 0.5f) / 0.25f)
        else -> lerp(Danger, Warning, progress / 0.5f)
    }

    drawIntoCanvas {
        it.drawPath(paths.pathList[1], paint.apply {
            color = bgColor
        })
        it.drawPath(paths.pathList[0], paint.apply {
            color = fgColor
        })
    }
}


fun DrawScope.drawTextWithBlendMode(
    context: Context,
    mask: Path,
    textParams: TextParams,
    isReadyToTap: Boolean,
) {

    drawText(
        textMeasurer = textParams.textMeasurer,
        topLeft = textParams.textOffset.copy(y = textParams.textOffset.y - 50),
        text = context.getString(R.string.that_s_about),
        style = textParams.textStyle.copy(fontSize = 20.sp)
    )
    drawText(
        textMeasurer = textParams.textMeasurer,
        topLeft = textParams.textOffset,
        text = textParams.text,
        style = textParams.textStyle,
    )
    drawText(
        textMeasurer = textParams.textMeasurer,
        topLeft = textParams.unitTextOffset,
        text = "%",
        style = textParams.unitTextStyle,
    )
    drawText(
        textMeasurer = textParams.textMeasurer,
        topLeft = textParams.textOffset.copy(y = textParams.textOffset.y + 250),
        text = context.getString(R.string.out_of_your_recommended_amount),
        style = textParams.textStyle.copy(fontSize = 16.sp)
    )
    if (isReadyToTap)
        drawText(
            textMeasurer = textParams.textMeasurer,
            topLeft = textParams.textOffset.copy(y = textParams.textOffset.y + 450),
            text = context.getString(R.string.tap_to_continue),
            style = textParams.textStyle.copy(fontSize = 16.sp)
        )
    drawPath(
        path = mask,
        color = BackgroundDark,
        blendMode = BlendMode.SrcIn
    )
}

val paint = Paint().apply {
    this.color = FadeBlue
    pathEffect = PathEffect.cornerPathEffect(100f)
}