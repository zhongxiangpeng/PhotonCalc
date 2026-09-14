package com.photoncalc.app.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.photoncalc.app.core.ChainCalc
import com.photoncalc.app.core.Conversions
import com.photoncalc.app.core.Fmt
import com.photoncalc.app.core.LevelUnit
import com.photoncalc.app.ui.components.NumField
import com.photoncalc.app.ui.components.ResultRow
import com.photoncalc.app.ui.components.SectionCard
import com.photoncalc.app.ui.components.parseNum

private data class StageRow(val name: String, val gain: String)

private val LEVEL_UNITS = listOf(LevelUnit.DBM, LevelUnit.VRMS, LevelUnit.VPP)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CascadePage() {
    var inputText by rememberSaveable { mutableStateOf("0") }
    var unitIndex by rememberSaveable { mutableIntStateOf(0) }
    var zIndex by rememberSaveable { mutableIntStateOf(0) }
    var customZ by rememberSaveable { mutableStateOf("50") }

    val stages = remember {
        mutableStateListOf(
            StageRow("放大器", "+20"),
            StageRow("光纤", "-3"),
        )
    }

    val z = effectiveZ(zIndex, customZ)
    val inUnit = LEVEL_UNITS[unitIndex]
    val inValue = parseNum(inputText) ?: 0.0

    val parsedStages = stages.mapNotNull { s ->
        parseNum(s.gain)?.let { ChainCalc.Stage(s.name.ifBlank { "级" }, it) }
    }
    val totalDb = ChainCalc.totalGainDb(parsedStages)
    val inDbm = Conversions.wattToDbm(inUnit.toWatt(inValue, z))
    val outDbm = ChainCalc.outputDbm(inDbm, parsedStages)
    val outWatt = Conversions.dbmToWatt(outDbm)

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
                suffixText = inUnit.label,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LEVEL_UNITS.forEachIndexed { i, u ->
                    FilterChip(
                        selected = i == unitIndex,
                        onClick = { unitIndex = i },
                        label = { Text(u.label) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
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

        SectionCard(title = "级联(增益为 +,衰减为 −)") {
            stages.forEachIndexed { i, s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = s.name,
                        onValueChange = { stages[i] = s.copy(name = it) },
                        label = { Text("名称") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = s.gain,
                        onValueChange = { stages[i] = s.copy(gain = it) },
                        label = { Text("dB") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { stages.removeAt(i) }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "删除该级",
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
            TextButton(onClick = { stages.add(StageRow("", "0")) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  添加一级")
            }
            ResultRow("总增益", "${Fmt.fixed(totalDb, 2)} dB", highlight = true)
            ResultRow("输入 / 输出(dBm)", "${Fmt.fixed(inDbm, 2)} → ${Fmt.fixed(outDbm, 2)}")
        }

        SectionCard(title = "输出电平(全部单位)") {
            LevelUnit.entries.forEach { u ->
                ResultRow(label = u.label, value = fmtUnit(u, u.fromWatt(outWatt, z)))
            }
        }
    }
}
