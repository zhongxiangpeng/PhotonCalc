package com.photoncalc.app.core

import kotlin.math.log10
import kotlin.math.pow

/**
 * 光放大器(EDFA)自发辐射(ASE)工程估算。
 *
 * 约定:P_ASE = NF·hν·(G−1)·B,两偏振之和,NF 取线性倍数
 * (对应常用放大器噪声系数定义 NF ≈ 2·n_sp)。
 */
object Ase {

    /** 光子能量 hν 对应的谱密度基准(dBm/Hz),1550 nm 处约 −158.9 dBm/Hz。 */
    fun photonPsdDbmPerHz(wlMeters: Double): Double =
        10.0 * log10(Optics.H_PLANCK * Optics.C / wlMeters * 1e3)

    /** ASE 功率谱密度(W/Hz,两偏振)。 */
    fun asePsdW(gainDb: Double, nfDb: Double, wlMeters: Double): Double {
        val g = 10.0.pow(gainDb / 10.0)
        val nf = 10.0.pow(nfDb / 10.0)
        val hnu = Optics.H_PLANCK * Optics.C / wlMeters
        return nf * hnu * (g - 1.0)
    }

    /** 积分带宽 B 内的 ASE 总功率(W)。 */
    fun asePowerW(gainDb: Double, nfDb: Double, wlMeters: Double, bandwidthHz: Double): Double =
        asePsdW(gainDb, nfDb, wlMeters) * bandwidthHz

    /** 光信噪比 OSNR(dB):信号功率与 ASE 在同一带宽内之比。 */
    fun osnrDb(signalDbm: Double, aseDbm: Double): Double = signalDbm - aseDbm
}
