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
import com.photoncalc.app.core.Ase
import com.photoncalc.app.core.Conversions
import com.photoncalc.app.core.Fmt
import com.photoncalc.app.core.NoiseFloor
import com.photoncalc.app.core.Optics
import com.photoncalc.app.ui.components.NumField
import com.photoncalc.app.ui.components.ResultRow
import com.photoncalc.app.ui.components.SectionCard
import com.photoncalc.app.ui.components.parseNum

private val BW_UNITS = listOf("Hz", "kHz", "MHz", "GHz")
private val BW_MULT = listOf(1.0, 1e3, 1e6, 1e9)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoisePage() {
    var bwText by rememberSaveable { mutableStateOf("1") }
    var bwUnit by rememberSaveable { mutableIntStateOf(3) } // 默认 GHz
    var nfText by rememberSaveable { mutableStateOf("3") }
    var tempText by rememberSaveable { mutableStateOf("290") }

    // ASE 输入
    var gainText by rememberSaveable { mutableStateOf("20") }
    var aseNfText by rememberSaveable { mutableStateOf("5") }
    var wlText by rememberSaveable { mutableStateOf("1550") }
    var aseBwText by rememberSaveable { mutableStateOf("0.1") }
    var aseBwUnit by rememberSaveable { mutableIntStateOf(0) } // 0: nm / 1: GHz
    var psigText by rememberSaveable { mutableStateOf("10") }

    val bwHz = parseNum(bwText)?.takeIf { it > 0 }?.times(BW_MULT[bwUnit])
    val nf = parseNum(nfText)
    val tK = parseNum(tempText)?.takeIf { it > 0 }

    val wlM = parseNum(wlText)?.takeIf { it > 0 }?.times(1e-9)
    val gain = parseNum(gainText)
    val aseNf = parseNum(aseNfText)
    val aseBwHz: Double? = parseNum(aseBwText)?.takeIf { it > 0 }?.let {
        if (aseBwUnit == 0) {
            wlM?.let { wl -> Optics.deltaFreqHz(wl, it * 1e-9) }
        } else {
            it * 1e9
        }
    }
    val psig = parseNum(psigText)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = "电域热噪底(kTB + NF)") {
            NumField(
                value = bwText,
                onValueChange = { bwText = it },
                label = "噪声带宽 B",
                suffixText = BW_UNITS[bwUnit],
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BW_UNITS.forEachIndexed { i, label ->
                    FilterChip(
                        selected = i == bwUnit,
                        onClick = { bwUnit = i },
                        label = { Text(label) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            NumField(
                value = nfText,
                onValueChange = { nfText = it },
                label = "噪声系数 NF",
                suffixText = "dB",
            )
            NumField(
                value = tempText,
                onValueChange = { tempText = it },
                label = "等效噪声温度 T(一般保持 290)",
                suffixText = "K",
            )
            if (bwHz == null || nf == null || tK == null) {
                Text(
                    "请检查输入是否有效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val floor = NoiseFloor.floorDbm(bwHz, nf, tK)
                ResultRow(
                    "热噪声谱密度 kT",
                    "${Fmt.fixed(NoiseFloor.thermalDbmPerHz(tK), 2)} dBm/Hz",
                )
                ResultRow("积分噪声底", "${Fmt.fixed(floor, 2)} dBm", highlight = true)
                ResultRow("换算 dBW", "${Fmt.fixed(floor - 30.0, 2)} dBW")
                val v50 = Conversions.wattToVrms(Conversions.dbmToWatt(floor), 50.0)
                ResultRow("50 Ω 等效电压", "${Fmt.eng(v50)} V rms")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "−174 dBm/Hz 对应 290 K 热噪声;若系统含光放大器,ASE 噪声见下方。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(title = "光放大器 ASE 与 OSNR") {
            NumField(
                value = gainText,
                onValueChange = { gainText = it },
                label = "放大器增益 G",
                suffixText = "dB",
            )
            NumField(
                value = aseNfText,
                onValueChange = { aseNfText = it },
                label = "放大器噪声系数 NF",
                suffixText = "dB",
            )
            NumField(
                value = wlText,
                onValueChange = { wlText = it },
                label = "中心波长",
                suffixText = "nm",
            )
            NumField(
                value = aseBwText,
                onValueChange = { aseBwText = it },
                label = "积分带宽 B",
                suffixText = if (aseBwUnit == 0) "nm" else "GHz",
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = aseBwUnit == 0,
                    onClick = { aseBwUnit = 0 },
                    label = { Text("带宽用 nm") },
                )
                FilterChip(
                    selected = aseBwUnit == 1,
                    onClick = { aseBwUnit = 1 },
                    label = { Text("带宽用 GHz") },
                )
            }
            NumField(
                value = psigText,
                onValueChange = { psigText = it },
                label = "信号输出功率(用于 OSNR)",
                suffixText = "dBm",
            )
            if (wlM == null || gain == null || aseNf == null || aseBwHz == null || psig == null) {
                Text(
                    "请检查输入是否有效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val aseDbm = Conversions.wattToDbm(
                    Ase.asePowerW(gain, aseNf, wlM, aseBwHz),
                )
                ResultRow(
                    "光子谱密度基准 hν",
                    "${Fmt.fixed(Ase.photonPsdDbmPerHz(wlM), 2)} dBm/Hz",
                )
                ResultRow("ASE 谱密度", "${Fmt.fixed(aseDbm - 10.0 * kotlin.math.log10(aseBwHz), 2)} dBm/Hz")
                ResultRow("ASE 总功率(@B)", "${Fmt.fixed(aseDbm, 2)} dBm", highlight = true)
                ResultRow(
                    "OSNR(@B)",
                    "${Fmt.fixed(Ase.osnrDb(psig, aseDbm), 2)} dB",
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "计算:P_ASE = NF·hν·(G−1)·B(两偏振);经验速算 @1550 nm、0.1 nm:" +
                    "ASE(dBm) ≈ G + NF − 58。OSNR = P_sig − P_ASE(同带宽)。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
