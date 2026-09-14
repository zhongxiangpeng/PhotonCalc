package com.photoncalc.app.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.photoncalc.app.CrashReporter
import com.photoncalc.app.core.Fmt
import com.photoncalc.app.core.Optics
import com.photoncalc.app.ui.components.ChoiceChips
import com.photoncalc.app.ui.components.NumField
import com.photoncalc.app.ui.components.ResultRow
import com.photoncalc.app.ui.components.SectionCard
import com.photoncalc.app.ui.components.parseNum

@Composable
fun ToolsPage() {
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0: λ(nm)→f  1: f(THz)→λ
    var input by rememberSaveable { mutableStateOf("1550") }

    var dMode by rememberSaveable { mutableIntStateOf(0) } // 0: Δλ→Δf  1: Δf→Δλ
    var dWl by rememberSaveable { mutableStateOf("1550") }
    var dVal by rememberSaveable { mutableStateOf("1") }

    val wlM: Double? = when (mode) {
        0 -> parseNum(input)?.takeIf { it > 0 }?.times(1e-9)
        else -> parseNum(input)?.takeIf { it > 0 }?.let { Optics.frequencyToWavelength(it * 1e12) }
    }

    val dWlM = parseNum(dWl)?.takeIf { it > 0 }?.times(1e-9)
    val dResult: Double? = when {
        dWlM == null || dWlM <= 0.0 -> null
        dMode == 0 -> parseNum(dVal)?.let { Optics.deltaFreqHz(dWlM, it * 1e-9) }
        else -> parseNum(dVal)?.let { Optics.deltaWlMeters(dWlM, it * 1e9) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = "光波长 ↔ 频率 ↔ 光子能量(真空)") {
            ChoiceChips(
                options = listOf("波长 nm → 频率", "频率 THz → 波长"),
                selectedIndex = mode,
                onSelect = { mode = it },
            )
            Spacer(Modifier.height(8.dp))
            NumField(
                value = input,
                onValueChange = { input = it },
                label = if (mode == 0) "真空波长" else "光频率",
                suffixText = if (mode == 0) "nm" else "THz",
            )
            if (wlM == null) {
                Text(
                    "请输入正数",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                ResultRow("频率", "${Fmt.plain(Optics.wavelengthToFrequency(wlM) / 1e12, 6)} THz")
                ResultRow("波长", "${Fmt.plain(wlM * 1e9, 6)} nm")
                ResultRow("光子能量", "${Fmt.fixed(Optics.photonEnergyEv(wlM), 3)} eV")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "c = 299 792 458 m/s(真空);E = hc/λ。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(title = "波长间隔 ↔ 频率间隔(小量近似)") {
            ChoiceChips(
                options = listOf("Δλ nm → Δf", "Δf GHz → Δλ"),
                selectedIndex = dMode,
                onSelect = { dMode = it },
            )
            Spacer(Modifier.height(8.dp))
            NumField(
                value = dWl,
                onValueChange = { dWl = it },
                label = "中心波长 λ₀",
                suffixText = "nm",
            )
            NumField(
                value = dVal,
                onValueChange = { dVal = it },
                label = "波长 / 频率间隔",
                suffixText = if (dMode == 0) "nm" else "GHz",
            )
            if (dResult == null) {
                Text(
                    "请检查输入(需正数)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (dMode == 0) {
                ResultRow(
                    "频率间隔 Δf",
                    "${Fmt.fixed(dResult / 1e9, 3)} GHz",
                    highlight = true,
                )
            } else {
                ResultRow(
                    "波长间隔 Δλ",
                    "${Fmt.fixed(dResult * 1e9, 4)} nm",
                    highlight = true,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Δf = c·Δλ/λ²。1550 nm 处 1 nm ≈ 124.8 GHz,1310 nm 处 ≈ 174.7 GHz," +
                    "滤波器/信道间隔换算常用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(title = "工程速查") {
            ResultRow("0 dBm", "1 mW = −30 dBW")
            ResultRow("0 dBm @ 50 Ω", "224 mV rms ≈ 632 mV pp")
            ResultRow("10 dBm @ 50 Ω", "707 mV rms = 2 V pp")
            ResultRow("0 dBm @ 50 Ω 电平", "+107 dBμV")
            ResultRow("热噪底 @ 290 K", "−174 dBm/Hz")
            ResultRow("噪底 @ 1 GHz、NF = 0 dB", "−84 dBm")
            ResultRow("1550 nm", "≈ 193.4 THz,0.80 eV")
            ResultRow("1550 nm 处 1 nm", "≈ 124.8 GHz")
            ResultRow("1310 nm 处 1 nm", "≈ 174.7 GHz")
            ResultRow("常用 PD 响应度(1550 nm)", "0.8 ~ 0.9 A/W")
        }

        SectionCard(title = "运行诊断") {
            val ctx = LocalContext.current
            var cleared by remember { mutableStateOf(false) }
            val crash = if (cleared) null else CrashReporter.latest(ctx)
            if (crash == null) {
                ResultRow("崩溃记录", "无", highlight = true)
            } else {
                ResultRow("崩溃记录", crash.name, highlight = true)
                Text(
                    remember(crash) {
                        crash.readText().lineSequence().take(4).joinToString("\n")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = {
                    CrashReporter.clear(ctx)
                    cleared = true
                }) { Text("清除崩溃日志") }
            }
            ResultRow("隐私", "完全离线:无网络权限,不收集任何数据")
        }
    }
}
