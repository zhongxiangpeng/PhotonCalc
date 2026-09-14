package com.photoncalc.app.core

/**
 * 光波长 / 频率 / 光子能量换算(真空)。
 */
object Optics {

    /** 真空光速,m/s。 */
    const val C: Double = 299_792_458.0

    /** 普朗克常数,J·s。 */
    const val H_PLANCK: Double = 6.62607015e-34

    /** 元电荷,C。 */
    const val Q_E: Double = 1.602176634e-19

    fun wavelengthToFrequency(wlMeters: Double): Double = C / wlMeters

    fun frequencyToWavelength(freqHz: Double): Double = C / freqHz

    /** 波长小间隔 Δλ 对应的频率间隔 Δf = c·Δλ/λ²(1550 nm 处 1 nm ≈ 124.8 GHz)。 */
    fun deltaFreqHz(wlMeters: Double, deltaWlMeters: Double): Double =
        C * deltaWlMeters / (wlMeters * wlMeters)

    /** 频率小间隔 Δf 对应的波长间隔 Δλ = Δf·λ²/c。 */
    fun deltaWlMeters(wlMeters: Double, deltaFreqHz: Double): Double =
        deltaFreqHz * wlMeters * wlMeters / C

    /** 光子能量(eV)。 */
    fun photonEnergyEv(wlMeters: Double): Double =
        H_PLANCK * C / wlMeters / Q_E
}
