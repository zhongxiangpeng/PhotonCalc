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
import com.photoncalc.app.core.PhotoDetector
import com.photoncalc.app.ui.components.NumField
import com.photoncalc.app.ui.components.ResultRow
import com.photoncalc.app.ui.components.SectionCard
import com.photoncalc.app.ui.components.parseNum

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoDetectPage() {
    var pText by rememberSaveable { mutableStateOf("0") }
    var pUnitIndex by rememberSaveable { mutableIntStateOf(0) } // 0 dBm / 1 mW
    var responsivity by rememberSaveable { mutableStateOf("0.85") }
    var loadZ by rememberSaveable { mutableStateOf("50") }

    val pWatt: Double? = parseNum(pText)?.let {
        if (pUnitIndex == 0) Conversions.dbmToWatt(it) else it / 1e3
    }
    val r = parseNum(responsivity)
    val z = parseNum(loadZ)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = "输入") {
            NumField(
                value = pText,
                onValueChange = { pText = it },
                label = "平均光功率",
                suffixText = if (pUnitIndex == 0) "dBm" else "mW",
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("dBm", "mW").forEachIndexed { i, label ->
                    FilterChip(
                        selected = i == pUnitIndex,
                        onClick = { pUnitIndex = i },
                        label = { Text(label) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            NumField(
                value = responsivity,
                onValueChange = { responsivity = it },
                label = "探测器响应度 R",
                suffixText = "A/W",
            )
            NumField(
                value = loadZ,
                onValueChange = { loadZ = it },
                label = "负载(或跨阻)阻抗",
                suffixText = "Ω",
            )
        }

        SectionCard(title = "输出(直流 / 平均值)") {
            if (pWatt == null || r == null || z == null || z <= 0.0) {
                Text(
                    "请检查输入是否有效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val iA = r * pWatt
                ResultRow("光电流 I = R·P", "${Fmt.eng(iA)} A")
                ResultRow("负载电压 V = I·Z", "${Fmt.eng(PhotoDetector.loadVoltage(iA, z))} V")
                ResultRow(
                    "负载耗散 P = I²·Z",
                    "${Fmt.eng(PhotoDetector.dissipatedPowerW(iA, z))} W",
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "说明:以上为平均光功率对应的直流(或低频)量;" +
                    "交流小信号输出请回到链路页,按链路增益折算。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
