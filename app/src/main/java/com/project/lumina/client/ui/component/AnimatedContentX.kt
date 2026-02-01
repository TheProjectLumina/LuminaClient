import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> AnimatedContentX(
    targetState: T,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent, //Color(0xFF060606),
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        label = "animatedPage",
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) { state ->
        Box(Modifier.fillMaxSize()) {
            content(state)
        }
    }
}
