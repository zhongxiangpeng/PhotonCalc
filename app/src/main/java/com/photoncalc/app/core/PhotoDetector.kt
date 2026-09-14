package com.photoncalc.app.core

/**
 * 光电探测:平均光功率 -> 光电流 -> 负载电压 / 耗散功率。
 * 适用于直流或低频(平均)量的工程估算。
 */
object PhotoDetector {

    /** 光电流 I = R·P,P 为光功率(W),R 为响应度(A/W)。 */
    fun photoCurrentA(pOptDbm: Double, responsivityAW: Double): Double =
        responsivityAW * Conversions.dbmToWatt(pOptDbm)

    /** 负载(或跨阻)上的电压降。 */
    fun loadVoltage(currentA: Double, zOhm: Double): Double = currentA * zOhm

    /** 负载耗散功率。 */
    fun dissipatedPowerW(currentA: Double, zOhm: Double): Double =
        currentA * currentA * zOhm
}
