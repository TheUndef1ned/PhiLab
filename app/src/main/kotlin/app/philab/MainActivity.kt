package app.philab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.defaultComponentContext
import app.philab.common.presentation.theme.PhiLabTheme
import app.philab.common.utils.LocalHazeState
import app.philab.root.DefaultRootComponent
import app.philab.root.RootContent
import dev.chrisbanes.haze.rememberHazeState
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootComponentFactory: DefaultRootComponent.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        (application as PhiLabApplication).appComponent.inject(this)

        val root = rootComponentFactory.create(componentContext = defaultComponentContext())

        setContent {
            PhiLabTheme {
                val hazeState = rememberHazeState()
                CompositionLocalProvider(LocalHazeState provides hazeState) {
                    RootContent(component = root)
                }
            }
        }
    }
}
