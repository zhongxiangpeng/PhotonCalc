package com.photoncalc.app.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.photoncalc.app.core.Conversions
import com.photoncalc.app.core.Fmt
import com.photoncalc.app.core.LevelUnit
import com.photoncalc.app.ui.components.NumField
import com.photoncalc.app.ui.components.ResultRow
import com.photoncalc.app.ui.components.SectionCard
import com.photoncalc.app.ui.components.parseNum

internal val Z_PRESETS = listOf(50.0, 75.0, 1.0)
internal val Z_LABELS = listOf("50 Ω", "75 Ω", "1 Ω", "自定义")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConvertPage() {
    var inputText by rememberSaveable { mutableStateOf("0") }
    var unitIndex by rememberSaveable { mutableIntStateOf(LevelUnit.DBM.ordinal) }
    var zIndex by rememberSaveable { mutableIntStateOf(0) }
    var customZ by rememberSaveable { mutableStateOf("50") }

    val z = effectiveZ(zIndex, customZ)
    val unit = LevelUnit.entries[unitIndex]
    val value = parseNum(inputText)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = "输入电平") {
            NumField(
                value = inputText,
                onValueChange = { inputText = it },
                label = "数值",
                suffixText = unit.label,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LevelUnit.entries.forEachIndexed { i, u ->
                    FilterChip(
                        selected = i == unitIndex,
                        onClick = { unitIndex = i },
                        label = { Text(u.label) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "系统阻抗 Z(电压 ↔ 功率换算依赖)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Z_LABELS.forEachIndexed { i, label ->
                    FilterChip(
                        selected = i == zIndex,
                        onClick = { zIndex = i },
                        label = { Text(label) },
                    )
                }
            }
            if (zIndex == 3) {
                NumField(
                    value = customZ,
                    onValueChange = { customZ = it },
                    label = "自定义阻抗",
                    suffixText = "Ω",
                )
            }
        }

        SectionCard(title = "全部单位") {
            LevelUnit.entries.forEach { u ->
                val v = value?.let { u.fromWatt(unit.toWatt(it, z), z) } ?: Double.NaN
                ResultRow(label = u.label, value = fmtUnit(u, v), highlight = u == unit)
            }
            if (value == null) {
                Text(
                    "请输入有效数值(支持负号与科学计数,如 1e-3)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        SectionCard(title = "速查(当前阻抗 ${Fmt.fixed(z, 1)} Ω)") {
            val v0 = Conversions.wattToVrms(1e-3, z)
            ResultRow("0 dBm", "1 mW")
            ResultRow(
                "0 dBm 电压",
                "${Fmt.eng(v0)} V rms ≈ ${Fmt.eng(Conversions.vrmsToVpp(v0))} V pp",
            )
            ResultRow("0 dBm 电平", "${Fmt.fixed(Conversions.dbmToDbuv(0.0, z), 2)} dBμV")
            ResultRow(
                "10 dBm 电压",
                "${Fmt.eng(Conversions.vrmsToVpp(Conversions.wattToVrms(1e-2, z)))} V pp",
            )
        }
    }
}

internal fun effectiveZ(zIndex: Int, customZ: String): Double =
    if (zIndex == 3) parseNum(customZ)?.takeIf { it > 0 } ?: 50.0
    else Z_PRESETS[zIndex]

/** 线性量纲用 SI 前缀(如 223.6 m 即 223.6 mV),对数量纲固定小数。 */
internal fun fmtUnit(u: LevelUnit, v: Double): String = when (u) {
    LevelUnit.DBM, LevelUnit.DBW, LevelUnit.DBV, LevelUnit.DBUV -> Fmt.fixed(v, 3)
    else -> Fmt.eng(v)
}
