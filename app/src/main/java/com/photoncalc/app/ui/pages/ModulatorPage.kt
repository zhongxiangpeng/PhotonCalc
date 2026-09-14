package com.photoncalc.app.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.photoncalc.app.core.Fmt
import com.photoncalc.app.core.Modulator
import com.photoncalc.app.ui.components.NumField
import com.photoncalc.app.ui.components.ResultRow
import com.photoncalc.app.ui.components.SectionCard
import com.photoncalc.app.ui.components.parseNum
import kotlin.math.PI

@Composable
fun ModulatorPage() {
    var vpiText by rememberSaveable { mutableStateOf("4") }
    var vppText by rememberSaveable { mutableStateOf("2") }
    var pushPull by rememberSaveable { mutableStateOf(false) }

    val vpi = parseNum(vpiText)?.takeIf { it > 0 }
    val vpp = parseNum(vppText)?.takeIf { it >= 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = "马赫-曾德尔调制器(MZM)") {
            NumField(
                value = vpiText,
                onValueChange = { vpiText = it },
                label = if (pushPull) "推挽差分半波电压 Vπ" else "单臂半波电压 Vπ",
                suffixText = "V",
            )
            NumField(
                value = vppText,
                onValueChange = { vppText = it },
                label = "正弦驱动 Vpp",
                suffixText = "V",
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("推挽(差分)驱动,等效 Vπ 减半")
                Spacer(Modifier.weight(1f))
                Switch(checked = pushPull, onCheckedChange = { pushPull = it })
            }
        }

        SectionCard(title = "结果(正交偏置点)") {
            if (vpi == null || vpp == null) {
                Text(
                    "请检查输入是否有效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val vpiEff = if (pushPull) vpi / 2.0 else vpi
                val m = Modulator.modulationDepth(vpp, vpiEff)
                val phase = Modulator.phaseSwingRad(vpp, vpiEff)
                ResultRow("有效半波电压", "${Fmt.eng(vpiEff)} V")
                ResultRow("正交点偏置电压", "${Fmt.fixed(Modulator.quadratureBias(vpiEff), 3)} V")
                ResultRow(
                    "小信号调制深度 m",
                    "${Fmt.fixed(m, 3)}(${Fmt.fixed(m * 100.0, 1)} %)",
                    highlight = true,
                )
                ResultRow("相位摆幅(峰峰)", "${Fmt.fixed(phase, 3)} rad = ${Fmt.fixed(phase * 180.0 / PI, 1)}°")
                ResultRow(
                    "100% 调制所需 Vpp",
                    "${Fmt.eng(Modulator.vppForFullModulation(vpiEff))} V",
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "约定:m = π·Vpk/Vπ(正交偏置线性近似);推挽驱动以差分电压为参考,等效半波电压减半。" +
                    "m > 0.3 时非线性失真逐渐显著,模拟链路通常取 m ≤ 0.3。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
