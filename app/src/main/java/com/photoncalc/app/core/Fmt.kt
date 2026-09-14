package com.photoncalc.app.core

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * 工程计数格式化:自动 SI 前缀(f p n μ m 空 k M G T),默认 4 位有效数字。
 */
object Fmt {

    private val PREFIXES = mapOf(
        -15L to "f", -12L to "p", -9L to "n", -6L to "μ", -3L to "m",
        0L to "", 3L to "k", 6L to "M", 9L to "G", 12L to "T",
    )

    /** 把数值格式化为带 SI 前缀的字符串,例如 0.2236 -> "223.6 m"。 */
    fun eng(value: Double, sigDigits: Int = 4): String {
        if (!value.isFinite()) return "—"
        if (value == 0.0) return "0"

        val exp = floor(log10(abs(value)))
        val engExp = (floor(exp / 3.0) * 3.0).toLong().coerceIn(-15L, 12L)
        val mant = value / 10.0.pow(engExp.toDouble())
        val prefix = PREFIXES[engExp] ?: ""
        val digits = (sigDigits - 1 - floor(log10(abs(mant)))).toInt().coerceIn(0, 6)
        val mStr = String.format(Locale.US, "%.${digits}f", mant).trimEnd('0').trimEnd('.')
        return if (prefix.isEmpty()) mStr else "$mStr $prefix"
    }

    /** 固定小数位格式(用于 dB 等对数量纲)。 */
    fun fixed(value: Double, decimals: Int = 2): String {
        if (!value.isFinite()) return "—"
        return String.format(Locale.US, "%.${decimals}f", value)
    }

    /** 普通有效数字显示(用于频率、波长等原始值)。 */
    fun plain(value: Double, sigDigits: Int = 6): String {
        if (!value.isFinite()) return "—"
        return String.format(Locale.US, "%.${sigDigits - 1}g", value)
    }
}
