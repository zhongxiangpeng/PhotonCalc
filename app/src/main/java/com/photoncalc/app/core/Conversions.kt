package com.photoncalc.app.core

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 功率/电压/电平换算引擎。
 *
 * 物理约定:
 * - dBm 以 1 mW 为参考,dBW 以 1 W 为参考,均为 10·lg(P/Pref)。
 * - 电压与功率之间的换算依赖系统阻抗 Z:P = V_rms² / Z。
 * - V_pp 与 V_rms 之间按正弦信号换算:V_pp = 2·sqrt(2)·V_rms。
 * - dBV / dBμV 为纯电压比(20·lg V),换算到 dBm 时必须经过阻抗 Z。
 */
object Conversions {

    const val SQRT2: Double = 1.4142135623730951

    // ---- 功率 <-> 对数电平 ----

    fun dbmToWatt(dbm: Double): Double = 10.0.pow(dbm / 10.0) / 1000.0

    fun wattToDbm(watt: Double): Double = 10.0 * log10(watt * 1000.0)

    fun dbmToDbw(dbm: Double): Double = dbm - 30.0

    fun dbwToDbm(dbw: Double): Double = dbw + 30.0

    // ---- 功率 <-> 电压(依赖阻抗) ----

    fun wattToVrms(watt: Double, z: Double): Double = sqrt(watt * z)

    fun vrmsToWatt(vrms: Double, z: Double): Double = vrms * vrms / z

    // ---- 电压表示之间的换算 ----

    fun vrmsToVpp(vrms: Double): Double = vrms * 2.0 * SQRT2

    fun vppToVrms(vpp: Double): Double = vpp / (2.0 * SQRT2)

    fun vrmsToDbv(vrms: Double): Double = 20.0 * log10(vrms)

    fun dbvToVrms(dbv: Double): Double = 10.0.pow(dbv / 20.0)

    fun vrmsToDbuv(vrms: Double): Double = vrmsToDbv(vrms) + 120.0

    fun dbuvToVrms(dbuv: Double): Double = dbvToVrms(dbuv - 120.0)

    // ---- 跨族换算(依赖阻抗) ----

    /** 50 Ω 时 0 dBm 对应约 +107 dBμV。 */
    fun dbmToDbuv(dbm: Double, z: Double): Double =
        vrmsToDbuv(wattToVrms(dbmToWatt(dbm), z))

    fun dbuvToDbm(dbuv: Double, z: Double): Double =
        vrmsToWatt(dbuvToVrms(dbuv), z).let { wattToDbm(it) }

    // ---- 增益 / 衰减 ----

    /** 电压经过 G dB 增益(同阻抗系统)。 */
    fun gainVrms(vrmsIn: Double, gainDb: Double): Double =
        vrmsIn * 10.0.pow(gainDb / 20.0)

    /** 功率电平经过 G dB 增益。 */
    fun gainDbm(dbmIn: Double, gainDb: Double): Double = dbmIn + gainDb
}
