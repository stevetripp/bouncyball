package com.example.bouncyball

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.BallManager
import com.example.BallPath
import com.example.SongData
import com.example.bouncyball.ui.theme.BouncyBallTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val tFlow = MutableStateFlow<Float>(0F)

    init {
        bounce()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ballManager = getBallManager()

        setContent {
            BouncyBallTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    BouncyBallRow(
                        tFlow = tFlow,
                        onClick = {
                            Log.i("SMT", "onClick")
                            bounce()
                        },
                        ballManager = ballManager,
                        modifier = Modifier.height(100.dp)
                    )
                }
            }
        }
    }

    private fun getBallManager(): BallManager {
        val bounceInTime = 1
        val bounceOutTime = 1
        val ballManager = BallManager()
        val songData = SongData.defaultData
        var first = true

        for (lineIndex in songData.lines.indices) {
            val lineItem1 = songData.lines[lineIndex]

            val syllables = lineItem1.syllables
            val syllableCount = syllables.count()
            val spacePx = pxFromDp(baseContext, 392f).toInt() / (syllableCount + 1)

            for (itemIndex in 1 until syllableCount) {
                val item1 = syllables[itemIndex - 1]
                val item2 = syllables[itemIndex]

                if (first) {
                    ballManager.add(BallPath(-1 * spacePx, item1.start.toFloat() - bounceInTime, spacePx * itemIndex, item1.start.toFloat()))
                    first = false
                }

                ballManager.add(BallPath(spacePx * itemIndex, item1.start.toFloat(), spacePx * (itemIndex + 1), item2.start.toFloat()))

                val lastSyllable = itemIndex == syllableCount - 1
                val lastLine = lineIndex + 1 >= songData.lines.size

                if (lastSyllable && !lastLine) {
                    // Processed last syllable with more lines available
                    val nextLine = songData.lines[lineIndex + 1]
                    val item2Start = nextLine.start.toFloat()// syllables. first().start.toFloat()
                    val initialSyllablePos = pxFromDp(baseContext, 392f).toInt() / (nextLine.syllables.size + 1)
                    ballManager.add(BallPath(spacePx * (itemIndex + 1), item2.start.toFloat(), initialSyllablePos, item2Start))
                } else if (lastSyllable && lastLine) {
                    // Processed last syllable with no more lines available
                    val x0 = spacePx * (itemIndex + 1)
                    val t0 = item2.start.toFloat()
                    val xf = x0 + (spacePx * 2)
                    val tf = t0 + bounceOutTime
                    ballManager.add(BallPath(x0, t0, xf, tf))
                }
            }
        }

        return ballManager
    }

    private fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    private fun bounce() = MainScope().launch {
        tFlow.value = 21f
        while (tFlow.value < 38F) {
            delay(3)
            tFlow.value += .005F
        }
    }
}

@Composable
fun Greeting(name: String, value: Int, onClick: () -> Unit) {
    Text(text = "$value: Hello $name!", modifier = Modifier.clickable { onClick() })
}

@Composable
fun BouncyBallRow(tFlow: Flow<Float>, onClick: () -> Unit, ballManager: BallManager, modifier: Modifier = Modifier) {

    val t = tFlow.collectAsState(initial = 0F)
    val (x, y) = ballManager.position(t.value)
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.Blue)
    ) {
        BoxWithConstraints(
            modifier = modifier
                .background(MaterialTheme.colors.background)
                .fillMaxWidth()
                .clickable(onClick = { onClick() })
        ) {
            val xDp = with(LocalDensity.current) { x.toDp() }
            val yDp = 100.dp - with(LocalDensity.current) { y.toDp() } - 10.dp
            Ball(xDp, yDp)
            Text(text = "t: ${t.value}", color = Color.White)
        }
    }
}

@Composable
fun BouncyBallRow1(x0: Dp, xf: Int, time: Float, modifier: Modifier = Modifier) {
//    val coroutineScope = rememberCoroutineScope()
//    var showDialog by remember { mutableStateOf(false) }
    val horizontal = remember { Animatable(initialValue = 0f) }
    val vertical = remember { Animatable(initialValue = 20f) }
    val size = 10f// with(LocalDensity.current) { 10.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .background(MaterialTheme.colors.background)
            .fillMaxWidth()
    ) {
        LaunchedEffect(horizontal) {
            horizontal.animateTo(
                targetValue = maxWidth.value - size,
//                animationSpec = tween(durationMillis = 3000,easing = LinearEasing)
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2_000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        LaunchedEffect(vertical) {
            vertical.animateTo(
                targetValue = maxHeight.value - size,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        Ball(horizontal.value.dp, vertical.value.dp)
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    var value by remember { mutableStateOf(0) }
    BouncyBallTheme {
        Greeting("Android", value) { value += 1 }
    }
}

@Preview
@Composable
fun BallRowPreview() {
    val tFlow = MutableStateFlow(0f)
    val ballPath = BallPath(0, 0f, 50, 3000f)
    var t by remember { mutableStateOf(0f) }
    val onClick: () -> Unit = { t += 500 }

    BouncyBallTheme(true) {
        BouncyBallRow(
            tFlow,
            onClick = onClick,
            BallManager(),
            modifier = Modifier.height(100.dp)
        )//1(0.dp, 0, 0f, modifier = Modifier.height(100.dp))
    }
//
//    LaunchedEffect(t) {
//        delay(10)
//        t += 10
//    }
}
