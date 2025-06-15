package com.dzaky3022.asesment1.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.FileProvider
import com.dzaky3022.asesment1.ui.screen.visual.ElementParams
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.pow

fun isAboveElement(waterLevel: Int, bufferY: Float, position: Offset) =
    waterLevel < position.y - bufferY

fun atElementLevel(
    waterLevel: Int,
    buffer: Float,
    elementParams: ElementParams,
) = (waterLevel >= (elementParams.position.y - buffer)) &&
        (waterLevel < (elementParams.position.y + elementParams.size.height * 0.33))

fun isWaterFalls(
    waterLevel: Int,
    elementParams: ElementParams,
) = waterLevel >= (elementParams.position.y + elementParams.size.height * 0.33) &&
        waterLevel <= (elementParams.position.y + elementParams.size.height)

@Stable
data class PointF(
    var x: Float,
    var y: Float
)

fun List<PointF>.copy(): List<PointF> = map {
    it.copy()
}.toMutableList()

@Stable
class Parabola(
    point1: PointF,
    point2: PointF,
    point3: PointF
) {
    private val a: Float
    private val b: Float
    private val c: Float

    init {
        val denom = (point1.x - point2.x) * (point1.x - point3.x) * (point2.x - point3.x)
        a =
            (point3.x * (point2.y - point1.y) + point2.x * (point1.y - point3.y) + point1.x * (point3.y - point2.y)) / denom
        b =
            (point3.x.pow(2) * (point1.y - point2.y) + point2.x.pow(2) * (point3.y - point1.y) + point1.x.pow(
                2
            ) * (point2.y - point3.y)) / denom
        c =
            (point2.x * point3.x * (point2.x - point3.x) * point1.y + point3.x * point1.x * (point3.x - point1.x) * point2.y + point1.x * point2.x * (point1.x - point2.x) * point3.y) / denom
    }

    fun calculate(x: Float): Float {
        return a * x.pow(2) + (b * x) + c
    }

}

fun lerpF(start: Float, stop: Float, fraction: Float): Float =
    (1 - fraction) * start + fraction * stop

fun parabolaInterpolation(fraction: Float): Float {
    return ((-40) * (fraction - 0.5).pow(2) + 11).toFloat()
}

fun Int.toBoolean(): Boolean = this != 0

@Stable
fun TextUnit.toPx(density: Density): Float = with(density) { this@toPx.roundToPx().toFloat() }

fun shareData(context: Context, bitmap: Bitmap, caption: String) {
    val file = File(context.cacheDir, "screenshot.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, caption)
        type = "image/png"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Screenshot"))
}


fun Float.roundUpTwoDecimals(): Float {
    return BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)
        .toFloat()
}

fun Double.roundUpTwoDecimals(): Float {
    return BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)
        .toFloat()
}

fun Instant.toFormattedDate(locale: Locale): String =
    formatDate(this, locale)

fun formatDate(instant: Instant, locale: Locale): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}