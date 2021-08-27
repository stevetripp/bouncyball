package com.example.bouncyball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.LineItem
import com.example.SongData
import com.example.SyllableItem
import com.example.bouncyball.ui.theme.BouncyBallTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
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

        val json = assets.open("iamachildofgod.json").bufferedReader().use { it.readText() }
        val songData = this.json.decodeFromString<SongData>(json)

        setContent {
            BouncyBallTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    SongInfo(
                        tFlow = tFlow,
                        onClick = { bounce(songData) },
                        songInfo = songData,
                        modifier = Modifier.height(100.dp)
                    )
                }
            }
        }
        bounce(songData)
    }

    private fun bounce(songData: SongData) = MainScope().launch {
        timer?.cancel()
        tFlow.value = 0.0
        val initTime = (songData.lines.first().start - songData.inOutBallAnimationDuration) - 1
        val endTime = (songData.lines.last().start + songData.inOutBallAnimationDuration)

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

@Composable
fun SongInfo(tFlow: Flow<Double>, onClick: () -> Unit, songInfo: SongData, modifier: Modifier = Modifier) {
    val position = tFlow.collectAsState(initial = 0.0)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
        val lineItem1 = songInfo.getLine(position.value)
        val lineItem2 = songInfo.getNextLine(lineItem1)
        val point = lineItem1.getBallPosition(position.value, lineItem2?.syllables?.firstOrNull(), widthPx)
        var lineHeight: Float by remember { mutableStateOf(0F) }
        val verticalOffset = with(LocalDensity.current) { songInfo.getVerticalOffset(position.value, lineHeight).toDp() }
        val transitionPercentage = songInfo.getTransitionPercentage(position.value).toFloat()
        Column(
            modifier = Modifier
                .background(Color.Blue)
                .height(500.dp)
        ) {
            BouncyBallRow(point, onClick = { onClick() }, modifier)
            ScrollableLines(modifier = Modifier.offset(y = verticalOffset)) {
                ScrollableLine(
                    annotatedString = lineItem1.toAnnotatedString(position.value),
                    modifier = Modifier.alpha(1F - transitionPercentage)
                )
                ScrollableLine(
                    annotatedString = lineItem2?.toAnnotatedString(position.value) ?: AnnotatedString(""),
                    onTextLayout = { result: TextLayoutResult ->
                        lineItem2?.setBallBouncePositions(result)
                        lineHeight = result.getLineTop(0) - result.getLineBottom(0)
                    }
                )
                ScrollableLine(
                    annotatedString = songInfo.getNextLine(lineItem2)?.toAnnotatedString(position.value) ?: AnnotatedString(""),
                    modifier = Modifier.alpha(transitionPercentage)
                )
            }
        }
        Text(text = "t: ${String.format("%.3f", position.value)}  width: $widthPx", color = Color.White)
    }
}

@Composable
fun ScrollableLines(modifier: Modifier = Modifier, composable: @Composable () -> Unit) {
    Column(modifier = modifier) { composable() }
}

@Composable
fun ScrollableLine(
    annotatedString: AnnotatedString,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    Text(text = annotatedString,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.h5,
        onTextLayout = { onTextLayout(it) }
    )
}

@Preview
@Composable
private fun ScrollableLinePreview() {
    BouncyBallTheme(true) {
        ScrollableLine(
            annotatedString = AnnotatedString("I am a child of God,"),
            modifier = Modifier.alpha(1f)
        )
    }
}

@Composable
private fun BouncyBallRow(point: Pair<Int, Int>, onClick: () -> Unit, modifier: Modifier = Modifier) {

    val (x, y) = point

    Column {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = { onClick() })
        ) {
            val xDp = with(LocalDensity.current) { x.toDp() }
            val yDp = 100.dp - with(LocalDensity.current) { y.toDp() } - 10.dp
            Ball(xDp, yDp)
        }
    }
}

@Composable
fun Ball(xDp: Dp, yDp: Dp) {
    Box(
        modifier = Modifier
            .offset(xDp, yDp)
            .size(10.dp)
            .clip(CircleShape)
            .background(Color.White)
    )
}

@Preview
@Composable
fun SongInfoPreview() {
    BouncyBallTheme(true) {
        val tFlow = MutableStateFlow(0.0)
        var t by remember { mutableStateOf(0.0) }
        val onClick: () -> Unit = { t += 500 }

        SongInfo(
            tFlow = tFlow, onClick = onClick, songInfo =
            SongData(
                lines = listOf(
                    LineItem(
                        start = 0.0,
                        text = "I am a child of God,",
                        syllables = listOf(SyllableItem("{0,1}", 0.0, "{0,1}"))
                    ),
                    LineItem(
                        start = 10.0,
                        text = "And he has sent me here.",
                        syllables = listOf(SyllableItem("{0,2}", 0.0, "{0,2}"))
                    )
                ), titleTransitionStart = 1.0
            ), modifier = Modifier.height(100.dp)
        )
    }
}