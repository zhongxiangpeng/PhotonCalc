package com.photoncalc.app.core

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 阻抗匹配与反射计算。
 */
object Impedance {

    /** 反射系数与派生量。VSWR 在 |Γ|≥1(纯电抗极端/负阻)时无定义,返回 NaN。 */
    data class Reflection(
        val gammaRe: Double,
        val gammaIm: Double,
        val gammaMag: Double,
        val gammaPhaseDeg: Double,
        val vswr: Double,
        val rlDb: Double,
        val mismatchLossDb: Double,
        val reflectedPercent: Double,
    )

    fun reflection(z0: Double, zlReal: Double, zlImag: Double): Reflection {
        val nR = zlReal - z0
        val nX = zlImag
        val dR = zlReal + z0
        val dX = zlImag
        val den = dR * dR + dX * dX
        val gRe = (nR * dR + nX * dX) / den
        val gIm = (nX * dR - nR * dX) / den
        val mag = sqrt(gRe * gRe + gIm * gIm)
        val vswr = if (mag < 1.0) (1.0 + mag) / (1.0 - mag) else Double.NaN
        return Reflection(
            gammaRe = gRe,
            gammaIm = gIm,
            gammaMag = mag,
            gammaPhaseDeg = Math.toDegrees(atan2(gIm, gRe)),
            vswr = vswr,
            rlDb = if (mag > 0.0) -20.0 * log10(mag) else Double.POSITIVE_INFINITY,
            mismatchLossDb = -10.0 * log10(1.0 - mag * mag),
            reflectedPercent = mag * mag * 100.0,
        )
    }

    /** 串联 Rs + jXs 的等效并联 Rp ∥ jXp。返回 Pair(Rp, Xp),Xp 与 Xs 同号。 */
    fun seriesToParallel(rs: Double, xs: Double): Pair<Double, Double> {
        val q = abs(xs / rs)
        val rp = rs * (1.0 + q * q)
        val xp = xs * (1.0 + q * q) / q
        return rp to xp
    }

    /** 并联 Rp ∥ jXp 的等效串联 Rs + jXs。返回 Pair(Rs, Xs)。 */
    fun parallelToSeries(rp: Double, xp: Double): Pair<Double, Double> {
        val q = abs(rp / xp)
        val rs = rp / (1.0 + q * q)
        val xs = xp / (1.0 + 1.0 / (q * q))
        return rs to xs
    }
}
