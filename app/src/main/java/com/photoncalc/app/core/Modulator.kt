package com.photoncalc.app.core

import kotlin.math.PI
import kotlin.math.cos

/**
 * 马赫-曾德尔调制器(MZM)工程估算。
 *
 * 约定:Vπ 为半波电压——施加 Vπ 产生 π 相移,传输从最大变到最小。
 * 推挽(差分)驱动时等效半波电压减半。
 */
object Modulator {

    /**
     * 小信号强度调制深度(正交偏置点):
     * T = cos²(φ/2),dφ = π·dV/Vπ_eff;Q 点斜率给出 m = π·V_pk/Vπ_eff。
     */
    fun modulationDepth(vppDrive: Double, vpiEffective: Double): Double =
        PI * (vppDrive / 2.0) / vpiEffective

    /** 峰峰相位摆幅(rad)。 */
    fun phaseSwingRad(vppDrive: Double, vpiEffective: Double): Double =
        PI * vppDrive / vpiEffective

    /** 正交偏置点所需偏置电压(单臂电压,推挽时两臂各 Vπ/4)。 */
    fun quadratureBias(vpiEffective: Double): Double = vpiEffective / 2.0

    /** 传输函数 T(V),V 为相对偏置点的驱动电压:T = cos²(πV / 2Vπ)。 */
    fun transmission(voltage: Double, vpiEffective: Double): Double {
        val c = cos(PI * voltage / (2.0 * vpiEffective))
        return c * c
    }

    /** 100% 调制深度对应的正弦驱动 Vpp。 */
    fun vppForFullModulation(vpiEffective: Double): Double = 2.0 * vpiEffective / PI
}
