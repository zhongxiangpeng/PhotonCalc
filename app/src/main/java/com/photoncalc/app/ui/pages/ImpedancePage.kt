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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.photoncalc.app.core.Fmt
import com.photoncalc.app.core.Impedance
import com.photoncalc.app.ui.components.NumField
import com.photoncalc.app.ui.components.ResultRow
import com.photoncalc.app.ui.components.SectionCard
import com.photoncalc.app.ui.components.parseNum

@Composable
fun ImpedancePage() {
    var z0Text by rememberSaveable { mutableStateOf("50") }
    var zrText by rememberSaveable { mutableStateOf("75") }
    var zxText by rememberSaveable { mutableStateOf("0") }

    var rsText by rememberSaveable { mutableStateOf("1") }
    var xsText by rememberSaveable { mutableStateOf("1") }

    val z0 = parseNum(z0Text)?.takeIf { it > 0 }
    val zr = parseNum(zrText)
    val zx = parseNum(zxText)
    val rs = parseNum(rsText)?.takeIf { it != 0.0 }
    val xs = parseNum(xsText)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = "反射系数 / 驻波 / 回波损耗") {
            NumField(
                value = z0Text,
                onValueChange = { z0Text = it },
                label = "系统阻抗 Z₀",
                suffixText = "Ω",
            )
            NumField(
                value = zrText,
                onValueChange = { zrText = it },
                label = "负载 ZL 实部",
                suffixText = "Ω",
            )
            NumField(
                value = zxText,
                onValueChange = { zxText = it },
                label = "负载 ZL 虚部(感性 +,容性 −)",
                suffixText = "Ω",
            )
            if (z0 == null || zr == null || zx == null) {
                Text(
                    "请检查输入是否有效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val r = Impedance.reflection(z0, zr, zx)
                ResultRow("反射系数 Γ", "${Fmt.fixed(r.gammaMag, 4)} ∠ ${Fmt.fixed(r.gammaPhaseDeg, 1)}°")
                ResultRow("驻波比 VSWR", if (r.vswr.isFinite()) Fmt.fixed(r.vswr, 3) else "—", highlight = true)
                ResultRow("回波损耗 RL", "${Fmt.fixed(r.rlDb, 2)} dB")
                ResultRow("失配损耗 ML", "${Fmt.fixed(r.mismatchLossDb, 3)} dB")
                ResultRow("反射功率占比", "${Fmt.fixed(r.reflectedPercent, 2)} %")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Γ = (ZL − Z₀)/(ZL + Z₀);共轭匹配条件 ZL = Z₀*。VSWR = 1.5 对应回损约 14 dB。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard(title = "串联 ↔ 并联等效变换") {
            NumField(
                value = rsText,
                onValueChange = { rsText = it },
                label = "串联电阻 Rs",
                suffixText = "Ω",
            )
            NumField(
                value = xsText,
                onValueChange = { xsText = it },
                label = "串联电抗 Xs(感性 +,容性 −)",
                suffixText = "Ω",
            )
            if (rs == null || xs == null || xs == 0.0) {
                Text(
                    "需要 Rs ≠ 0 且 Xs ≠ 0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val q = kotlin.math.abs(xs / rs)
                val (rp, xp) = Impedance.seriesToParallel(rs, xs)
                ResultRow("品质因数 Q", Fmt.fixed(q, 3))
                ResultRow("等效并联 Rp", "${Fmt.eng(rp)} Ω", highlight = true)
                ResultRow("等效并联 Xp", "${Fmt.eng(xp)} Ω")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Q = |Xs/Rs|;Rp = Rs(1+Q²),Xp = Xs(1+Q²)/Q,与 Xs 同号。L 型匹配网络即基于此变换。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
