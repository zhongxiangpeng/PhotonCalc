package com.photoncalc.app.core

import kotlin.math.log10

/**
 * 热噪声底估算:kTB + 噪声系数。
 */
object NoiseFloor {

    /** 玻尔兹曼常数,J/K。 */
    const val K_B: Double = 1.380649e-23

    /** 标准室温,K。 */
    const val T0: Double = 290.0

    /** kTB 噪声谱密度(dBm/Hz),290 K 时约 -174 dBm/Hz。 */
    fun thermalDbmPerHz(tK: Double = T0): Double =
        10.0 * log10(K_B * tK * 1e3)

    /** 带宽 B、噪声系数 NF 下的积分噪声底(dBm)。 */
    fun floorDbm(bandwidthHz: Double, nfDb: Double, tK: Double = T0): Double =
        thermalDbmPerHz(tK) + nfDb + 10.0 * log10(bandwidthHz)
}
