package com.example.bouncyball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.Content
import com.example.bouncyball.ui.theme.AppTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Timer
import kotlin.concurrent.timer

class MainActivity : ComponentActivity() {

    private var timer: Timer? = null
    private val tFlow = MutableStateFlow(0.0)
    private val json = Json { ignoreUnknownKeys = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val json = assets.open("iamachildofgod.json").bufferedReader().use { it.readText() }
        val json = assets.open("bookofmormonstories.json").bufferedReader().use { it.readText() }
//        val json = assets.open("jesuswantsmeforasunbeam.json").bufferedReader().use { it.readText() }
        val songData = this.json.decodeFromString<Content>(json)

        setContent {
            val position by tFlow.collectAsState()
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.secondary) {
                    SongAnimations(position = position, content = songData)
                }
            }
        }
        bounce(songData)
    }

    private fun bounce(content: Content) = MainScope().launch {
        timer?.cancel()
        tFlow.value = 0.0
        val initTime = (content.lines.first().start - content.inOutBallAnimationDuration) - 1
        val endTime = (content.lines.last().start + content.inOutBallAnimationDuration)

        var start: Long? = null

        timer = timer(period = 20L) {
            if (start == null) {
                start = scheduledExecutionTime()
            } else {
                tFlow.value = (scheduledExecutionTime() - start!!) / 1000.0 + initTime
            }

            if (tFlow.value > endTime) this.cancel()
        }
    }
}

//@Composable
//fun SongInfo(position: Double, onClick: () -> Unit, songInfo: Content, modifier: Modifier = Modifier) {
//    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
//        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
//        val lineItem1 = songInfo.getLine(position)
//        val lineItem2 = songInfo.getNextLine(lineItem1)
//        val lineItem3 = songInfo.getNextLine(lineItem2)
//        val point = lineItem1.getBallPosition(position, lineItem2?.syllables?.firstOrNull(), widthPx)
//        var lineHeight: Float by remember { mutableStateOf(0F) }
//        val verticalOffset = with(LocalDensity.current) { songInfo.getVerticalOffset(position, lineHeight).toDp() }
//        val transitionPercentage = songInfo.getTransitionPercentage(position).toFloat()
//        Column(
//            modifier = Modifier
//                .height(500.dp)
//        ) {
//            BouncyBallRow(point, onClick = { onClick() }, modifier)
//            ScrollableLines(modifier = Modifier.offset(y = verticalOffset)) {
//                ScrollableLine(
//                    lineItem = lineItem1,
//                    position = position,
//                    modifier = Modifier.alpha(1F - transitionPercentage),
//                )
//                ScrollableLine(
//                    lineItem = lineItem2,
//                    onTextLayout = { result, forceRecalculations ->
//                        lineItem2?.setBallBouncePositions(result, forceRecalculations)
//                    }
//                )
//                ScrollableLine(
//                    lineItem = lineItem3,
//                    onTextLayout = { result, _ ->
//                        lineHeight = result.getLineTop(0) - result.getLineBottom(0)
//                    },
//                    modifier = Modifier.alpha(transitionPercentage)
//                )
//            }
//        }
//        Text(text = "t: ${String.format("%.3f", position)}  width: $widthPx", color = Color.White)
//    }
//}

//@Composable
//private fun ScrollableLines(modifier: Modifier = Modifier, composable: @Composable () -> Unit) {
//    Column(modifier = modifier) { composable() }
//}

//@Composable
//private fun ScrollableLine(
//    lineItem: LineItem?,
//    modifier: Modifier = Modifier,
//    position: Double = 0.0,
//    onTextLayout: (TextLayoutResult, Boolean) -> Unit = { _, _ -> }
//) {
//    val line = lineItem ?: LineItem(start = 0.0, text = "", syllables = emptyList())
//    val defaultStyle = line.textStyle ?: MaterialTheme.typography.h5
//    val text = line.toAnnotatedString(position)
//    var style by remember(line.text) { mutableStateOf(defaultStyle) }
//    var isReadyToDraw by remember(style) { mutableStateOf(false) }
//    Text(
//        text = text,
//        color = Color.White,
//        textAlign = TextAlign.Center,
//        softWrap = false,
//        modifier = modifier
//            .fillMaxWidth()
//            .drawWithContent {
//                if (isReadyToDraw) {
//                    drawContent()
//                }
//            },
//        style = style,
//        onTextLayout = {
//            onTextLayout(it, !isReadyToDraw)
//            if (it.didOverflowWidth) {
//                style = style.copy(fontSize = style.fontSize * 0.9)
//            } else {
//                line.textStyle = style
//                isReadyToDraw = true
//            }
//        }
//    )
//}

//@Composable
//private fun BouncyBallRow(point: Pair<Int, Int>, onClick: () -> Unit, modifier: Modifier = Modifier) {
//
//    val (x, y) = point
//
//    Column {
//        BoxWithConstraints(
//            modifier = modifier
//                .fillMaxWidth()
//                .clickable(onClick = { onClick() })
//        ) {
//            val xDp = with(LocalDensity.current) { x.toDp() }
//            val yDp = 100.dp - with(LocalDensity.current) { y.toDp() } - 10.dp
//            Ball(xDp, yDp)
//        }
//    }
//}

//@Composable
//private fun Ball(xDp: Dp, yDp: Dp) {
//    Box(
//        modifier = Modifier
//            .offset(xDp, yDp)
//            .size(10.dp)
//            .clip(CircleShape)
//            .background(Color.White)
//    )
//}

//@OptIn(ExperimentalSerializationApi::class)
//@Preview
//@Composable
//private fun SongInfoPreview() {
//    BouncyBallTheme(true) {
//        val json = Json { ignoreUnknownKeys = true }
//        val data = LocalContext.current.assets.open("bookofmormonstories.json").bufferedReader().use { it.readText() }
//        val songData = json.decodeFromString<Content>(data)
//        var t by remember { mutableStateOf(21.0) }
//        val onClick: () -> Unit = { t += 500 }
//
//        SongInfo(
//            position = t,
//            onClick = onClick,
//            songInfo = songData,
//            modifier = Modifier.height(100.dp)
//        )
//    }
//}