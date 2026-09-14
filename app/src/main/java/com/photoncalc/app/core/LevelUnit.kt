package com.photoncalc.app.core

/**
 * 可互相换算的电平/功率/电压单位族。
 * 电压类单位与功率类单位之间的换算需要系统阻抗 Z。
 */
enum class LevelUnit(val label: String, val isVoltage: Boolean) {
    DBM("dBm", false),
    DBW("dBW", false),
    W("W", false),
    MW("mW", false),
    UW("μW", false),
    VRMS("V rms", true),
    VPP("V pp", true),
    DBV("dBV", true),
    DBUV("dBμV", true);

    /** 统一桥接到瓦特。 */
    fun toWatt(value: Double, z: Double): Double = when (this) {
        DBM -> Conversions.dbmToWatt(value)
        DBW -> Conversions.dbmToWatt(value + 30.0)
        W -> value
        MW -> value / 1e3
        UW -> value / 1e6
        VRMS -> Conversions.vrmsToWatt(value, z)
        VPP -> Conversions.vrmsToWatt(Conversions.vppToVrms(value), z)
        DBV -> Conversions.vrmsToWatt(Conversions.dbvToVrms(value), z)
        DBUV -> Conversions.vrmsToWatt(Conversions.dbuvToVrms(value), z)
    }

    /** 从瓦特出发换算到本单位。 */
    fun fromWatt(watt: Double, z: Double): Double = when (this) {
        DBM -> Conversions.wattToDbm(watt)
        DBW -> Conversions.wattToDbm(watt) - 30.0
        W -> watt
        MW -> watt * 1e3
        UW -> watt * 1e6
        VRMS -> Conversions.wattToVrms(watt, z)
        VPP -> Conversions.vrmsToVpp(Conversions.wattToVrms(watt, z))
        DBV -> Conversions.vrmsToDbv(Conversions.wattToVrms(watt, z))
        DBUV -> Conversions.vrmsToDbuv(Conversions.wattToVrms(watt, z))
    }
}
