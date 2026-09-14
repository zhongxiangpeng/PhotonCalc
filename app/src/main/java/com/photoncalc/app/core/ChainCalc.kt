package com.photoncalc.app.core

/**
 * 级联链路计算:起始电平依次经过各级增益(+)/衰减(−)。
 * 全程假定同一阻抗系统,电压增益与功率增益数值上等效。
 */
object ChainCalc {

    data class Stage(
        val name: String,
        val gainDb: Double,
    )

    /** 输入电平(dBm)经过全部级后,输出电平(dBm)。 */
    fun outputDbm(inputDbm: Double, stages: List<Stage>): Double =
        stages.fold(inputDbm) { acc, s -> acc + s.gainDb }

    /** 输出电压(V rms),输入为电压时用 20·lg 定义。 */
    fun outputVrms(inputVrms: Double, stages: List<Stage>): Double =
        stages.fold(inputVrms) { acc, s -> Conversions.gainVrms(acc, s.gainDb) }

    /** 总增益(dB)。 */
    fun totalGainDb(stages: List<Stage>): Double =
        stages.sumOf { it.gainDb }
}
