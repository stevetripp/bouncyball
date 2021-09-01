package com.example.bouncyball

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.Content
import com.example.LineItem
import com.example.bouncyball.ui.theme.AppTheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val BALL_SIZE = 6.dp

@SuppressLint("ComposableNaming")
@Composable
private fun DEFAULT_TEXT_STYLE() = MaterialTheme.typography.h5

@Composable
fun SongAnimations(position: Double, content: Content, modifier: Modifier = Modifier, ballRowHeightDp: Dp = 60.dp) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.LightGray)))
            .offset(y = 10.dp)
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
        val maxBallHeight = with(LocalDensity.current) { (ballRowHeightDp - 20.dp).toPx() }.toInt()
        val lineItem1 = content.getLine(position)
        val lineItem2 = content.getNextLine(lineItem1)
        val lineItem3 = content.getNextLine(lineItem2)
        val point = lineItem1.getBallPosition(position, lineItem2?.syllables?.firstOrNull(), widthPx, maxBallHeight)
        var lineHeight: Float by remember { mutableStateOf(0F) }
        val verticalOffset = with(LocalDensity.current) { content.getVerticalOffset(position, -lineHeight).toDp() }
        val transitionPercentage = content.getTransitionPercentage(position).toFloat()
        Column {
            BouncyBallRow(point, ballRowHeightDp, modifier)
            ScrollableLines(modifier = Modifier.offset(y = verticalOffset)) {
                ScrollableLine(
                    lineItem = lineItem1,
                    position = position,
                    modifier = Modifier.alpha(1F - transitionPercentage),
                )
                ScrollableLine(
                    lineItem = lineItem2,
                    onTextLayout = { result, _ ->
                        lineItem2?.setBallBouncePositions(result)
                    }
                )
                ScrollableLine(
                    lineItem = lineItem3,
                    onTextLayout = { result, _ ->
                        lineHeight = result.getLineBottom(0) - result.getLineTop(0)
                    },
                    modifier = Modifier.alpha(transitionPercentage)
                )
            }
        }
        Text(text = "t: ${String.format("%.3f", position)}  width: $widthPx", color = Color.White)
    }
}

@Composable
private fun ScrollableLines(modifier: Modifier = Modifier, composable: @Composable () -> Unit) {
    Column(modifier = modifier) { composable() }
}

@Composable
private fun ScrollableLinePreview() {
    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
        ScrollableLine(lineItem = songData().lines.first())
    }
}

@Composable
private fun ScrollableLine(
    lineItem: LineItem?,
    modifier: Modifier = Modifier,
    position: Double = 0.0,
    onTextLayout: (TextLayoutResult, Boolean) -> Unit = { _, _ -> }
) {
    val line = lineItem ?: LineItem(start = 0.0, text = "", syllables = emptyList())
    val defaultStyle = line.textStyle ?: DEFAULT_TEXT_STYLE()
    val text = line.toAnnotatedString(position, MaterialTheme.colors.primary)
    var style by remember(line.text) { mutableStateOf(defaultStyle) }
    var isReadyToDraw by remember(style) { mutableStateOf(false) }
    Text(
        text = text,
        color = MaterialTheme.colors.onPrimary,
        textAlign = TextAlign.Center,
        softWrap = false,
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                if (isReadyToDraw) {
                    drawContent()
                }
            },
        style = style,
        onTextLayout = {
            onTextLayout(it, !isReadyToDraw)
            if (it.didOverflowWidth) {
                style = style.copy(fontSize = style.fontSize * 0.9)
            } else {
                line.textStyle = style
                isReadyToDraw = true
            }
        }
    )
}

@Preview
@Composable
private fun ScrollableLinePreviewLight() {
    AppTheme { ScrollableLinePreview() }
}

@Preview
@Composable
private fun ScrollableLinePreviewDark() {
    AppTheme(true) { ScrollableLinePreview() }
}

@Composable
private fun BouncyBallRow(point: Pair<Int, Int>, height: Dp, modifier: Modifier = Modifier) {

    val (x, y) = point

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val xDp = with(LocalDensity.current) { x.toDp() }
        val yDp = height - with(LocalDensity.current) { y.toDp() } - BALL_SIZE
        Ball(xDp, yDp)
    }
}

@Composable
private fun BouncyBallRowPreview() {
    BouncyBallRow(point = Pair(20, 20), 50.dp)
}

@Preview
@Composable
private fun BouncyBallRowPreviewLight() {
    Box(modifier = Modifier.background(MaterialTheme.colors.background)) { AppTheme { BouncyBallRowPreview() } }
}

@Preview
@Composable
private fun BouncyBallRowPreviewDark() {
    AppTheme(true) { BouncyBallRowPreview() }
}

@Composable
private fun Ball(xDp: Dp, yDp: Dp) {
    Box(
        modifier = Modifier
            .offset(xDp, yDp)
            .size(BALL_SIZE)
            .clip(CircleShape)
            .background(MaterialTheme.colors.onPrimary)
    )
}

@Preview
@Composable
fun SongAnimationsPreview() {

    AppTheme(true) {
        var position by remember { mutableStateOf(14.0) }
        val onClick: () -> Unit = { position += 500 }

        SongAnimations(
            position = position,
            content = songData(),
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
private fun songData(): Content {
    val json = Json { ignoreUnknownKeys = true }
    val data = LocalContext.current.assets.open("bookofmormonstories.json").bufferedReader().use { it.readText() }
    return json.decodeFromString(data)
}