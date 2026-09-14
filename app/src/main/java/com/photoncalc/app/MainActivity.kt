package com.photoncalc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.photoncalc.app.ui.pages.CascadePage
import com.photoncalc.app.ui.pages.ConvertPage
import com.photoncalc.app.ui.pages.ImpedancePage
import com.photoncalc.app.ui.pages.ModulatorPage
import com.photoncalc.app.ui.pages.NoisePage
import com.photoncalc.app.ui.pages.PhotoDetectPage
import com.photoncalc.app.ui.pages.ToolsPage
import com.photoncalc.app.ui.theme.PhotonCalcTheme

private data class TabSpec(val label: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotonCalcTheme {
                AppRoot()
            }
        }
    }
}

private val TABS = listOf(
    TabSpec("换算", Icons.Filled.SwapHoriz),
    TabSpec("链路", Icons.Filled.StackedLineChart),
    TabSpec("探测", Icons.Filled.Sensors),
    TabSpec("噪声", Icons.Filled.GraphicEq),
    TabSpec("阻抗", Icons.Filled.Tune),
    TabSpec("调制", Icons.Filled.Waves),
    TabSpec("工具", Icons.Filled.Functions),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot() {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "微波光子计算器",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                TABS.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        // 自适应:窄屏占满全宽;平板/横屏/折叠屏内容限宽居中,避免行太长
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(modifier = Modifier.widthIn(max = if (maxWidth > 640.dp) 620.dp else maxWidth)) {
                when (tab) {
                    0 -> ConvertPage()
                    1 -> CascadePage()
                    2 -> PhotoDetectPage()
                    3 -> NoisePage()
                    4 -> ImpedancePage()
                    5 -> ModulatorPage()
                    else -> ToolsPage()
                }
            }
        }
    }
}
