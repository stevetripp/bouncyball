package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow

@Serializable
data class SongData(
    val inOutBallAnimationDuration: Int = 1,
    val lyricsAnimationDuration: Double = 0.2,
    val lines: List<LineItem>,
//    @SerialName("image_transitions")
//	val imageTransitions: List<ImageTransitionsItem?>? = null,
//    @SerialName("cross_fade_image_animation_duration")
//    val crossFadeImageAnimationDuration: Double? = null,
    @SerialName("title_transition_start")
    val titleTransitionStart: Double,
//    val verses: List<VersesItem?>? = null,
//    val tintColor: Int? = null
) {
    fun getLine(t: Double) = lines.findLast { it.start <= t }

    fun getNextLine(line: LineItem): LineItem? {
        val nextLineIndex = lines.indexOf(line) + 1
        return lines.getOrNull(nextLineIndex)
    }
}

//data class ImageTransitionsItem(
//	val image: Image? = null,
//	val effect: Int? = null,
//	val start: Int? = null
//)

//data class Image(
//	val color: Int? = null,
//	val regionOfInterest: Any? = null,
//	val renditions: String? = null,
//	val crop: String? = null
//)

//data class VersesItem(
//    val start: Double? = null
//)

@Serializable
data class LineItem(
    val image: String? = null,
    val start: Double,
    val text: String,
    val syllables: List<SyllableItem>
) {
    fun toAnnotatedString(t: Double): AnnotatedString {
        // Find the current syllable
        val syllable = syllables.findLast { it.start < t }

        // Calculate index of last character of syllable
        val syllableRange = syllable?.range?.toRange() ?: (-1..-1)

        return buildAnnotatedString {
            withStyle(SpanStyle(Color.Red)) {
                append(text.subSequence(0, syllableRange.last + 1).toString())
            }
            append(text.subSequence(syllableRange.last + 1, text.length).toString())
        }
    }

    fun setBallBouncePositions(textLayoutResult: TextLayoutResult) {
        if (syllables.none { it.horizontalMidpoint < 0 }) return
        syllables.forEach { syllable ->
            val range = syllable.range.toRange()
            val rect1 = textLayoutResult.getBoundingBox(range.first)
            val rect2 = textLayoutResult.getCursorRect(range.last)
            syllable.horizontalMidpoint = ((rect1.left + rect2.right) / 2).toInt()
        }
    }

    fun getBallPosition(t: Double, nextLineSyllable: SyllableItem?): Pair<Int, Int> {
        // Find the syllable for t
        val syllable = syllables.findLast { it.start < t } ?: return Pair(0, 0)
        val nextSyllable = getNextSyllable(syllable) ?: nextLineSyllable
        val x0 = syllable.horizontalMidpoint
        val t0 = syllable.start
        val xf = nextSyllable?.horizontalMidpoint ?: x0 + 10
        val tf = nextSyllable?.start ?: syllable.start + 2.0

        val xPos = xPos(t, x0, t0, xf, tf)
        val yPos = yPos(xPos, x0, xf)
        return Pair(xPos, yPos)
    }

    private fun xPos(t: Double, x0: Int, t0: Double, xf: Int, tf: Double): Int {
        val tTotal = tf - t0
        val v = (xf - x0) / tTotal
        return when {
            t < t0 -> x0
            t > tf -> xf
            else -> (x0 + (v * (t - t0))).toInt()
        }
    }

    private fun yPos(x: Int, x0: Int, xf: Int): Int {
        val h = 100
        val xOffset = (x - x0).toFloat()
        val m = (xf - x0) / 2F

        return ((-1 * h * xOffset.pow(2)) / m.pow(2) + (2 * h * xOffset) / m).toInt()
    }

    private fun getNextSyllable(syllable: SyllableItem): SyllableItem? {
        val index = syllables.indexOf(syllable) + 1
        return syllables.getOrNull(index)
    }

    private fun String.toRange(): IntRange {
        val regex = Regex("\\{([^,]*),([^}]*)\\}")
        return regex.matchEntire(this)?.let { result ->
            val start = result.groups[1]?.value?.toInt() ?: return@let null
            val range = result.groups[2]?.value?.toInt() ?: return@let null
            start until start + range
        } ?: 0..0
    }
}

@Serializable
data class SyllableItem(
    @SerialName("ball_range")
    val ballRange: String,
    val start: Double,
    val range: String
) {
    var horizontalMidpoint = -10
}