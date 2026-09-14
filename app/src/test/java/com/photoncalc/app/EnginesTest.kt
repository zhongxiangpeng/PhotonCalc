package com.photoncalc.app

import com.photoncalc.app.core.Ase
import com.photoncalc.app.core.ChainCalc
import com.photoncalc.app.core.Fmt
import com.photoncalc.app.core.Impedance
import com.photoncalc.app.core.Modulator
import com.photoncalc.app.core.NoiseFloor
import com.photoncalc.app.core.Optics
import com.photoncalc.app.core.PhotoDetector
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.log10

class EnginesTest {

    private val eps = 1e-9

    // ---- 级联 ----

    @Test
    fun `cascade adds gains in dB`() {
        val stages = listOf(
            ChainCalc.Stage("EDFA", 20.0),
            ChainCalc.Stage("fiber", -3.0),
        )
        assertEquals(17.0, ChainCalc.outputDbm(0.0, stages), eps)
        assertEquals(17.0, ChainCalc.totalGainDb(stages), eps)
    }

    @Test
    fun `voltage cascade uses 20 log convention`() {
        val stages = listOf(ChainCalc.Stage("amp", 6.0))
        assertEquals(1.995262315, ChainCalc.outputVrms(1.0, stages), 1e-8)
    }

    // ---- 光电探测 ----

    @Test
    fun `0 dBm optical with 0_85 A per W gives 0_85 mA`() {
        assertEquals(0.85e-3, PhotoDetector.photoCurrentA(0.0, 0.85), 1e-12)
    }

    @Test
    fun `0_85 mA into 50 ohm gives 42_5 mV`() {
        assertEquals(42.5e-3, PhotoDetector.loadVoltage(0.85e-3, 50.0), 1e-15)
    }

    // ---- 噪声底 ----

    @Test
    fun `kTB at 290 K is about minus 174 dBm per Hz`() {
        assertEquals(-173.98, NoiseFloor.thermalDbmPerHz(290.0), 0.05)
    }

    @Test
    fun `1 GHz with NF 3 dB gives about minus 81 dBm`() {
        assertEquals(-80.98, NoiseFloor.floorDbm(1e9, 3.0, 290.0), 0.05)
    }

    // ---- MZM ----

    @Test
    fun `single arm modulation depth pi over 4`() {
        val m = Modulator.modulationDepth(vppDrive = 2.0, vpiEffective = 4.0)
        assertEquals(Math.PI / 4, m, 1e-12)
    }

    @Test
    fun `push pull halves effective vpi`() {
        val m = Modulator.modulationDepth(vppDrive = 2.0, vpiEffective = 4.0 / 2.0)
        assertEquals(Math.PI / 2, m, 1e-12)
    }

    @Test
    fun `quadrature bias is half vpi`() {
        assertEquals(2.0, Modulator.quadratureBias(4.0), eps)
    }

    @Test
    fun `transmission is 1 at 0 V and 0 at vpi`() {
        assertEquals(1.0, Modulator.transmission(0.0, 4.0), 1e-12)
        assertEquals(0.0, Modulator.transmission(4.0, 4.0), 1e-12)
    }

    // ---- 光学 ----

    @Test
    fun `1550 nm is about 193_41 THz`() {
        val f = Optics.wavelengthToFrequency(1550e-9)
        assertEquals(193.4145e12, f, 0.001e12)
    }

    @Test
    fun `photon energy at 1550 nm is about 0_8 eV`() {
        assertEquals(0.7999, Optics.photonEnergyEv(1550e-9), 0.001)
    }

    // ---- ASE ----

    @Test
    fun `EDFA ase in 0_1 nm at 1550 nm is about minus 33 dBm`() {
        val bw = Optics.deltaFreqHz(1550e-9, 0.1e-9)
        val p = 10.0 * log10(Ase.asePowerW(20.0, 5.0, 1550e-9, bw) * 1e3)
        assertEquals(-33.0, p, 0.1)
    }

    @Test
    fun `photon psd at 1550 nm is about minus 158_9 dBm per Hz`() {
        assertEquals(-158.92, Ase.photonPsdDbmPerHz(1550e-9), 0.02)
    }

    // ---- 阻抗 ----

    @Test
    fun `75 ohm load on 50 ohm system gives gamma 0_2 vswr 1_5`() {
        val r = Impedance.reflection(50.0, 75.0, 0.0)
        assertEquals(0.2, r.gammaMag, 1e-12)
        assertEquals(1.5, r.vswr, 1e-12)
        assertEquals(13.98, r.rlDb, 0.01)
        assertEquals(0.177, r.mismatchLossDb, 0.01)
        assertEquals(4.0, r.reflectedPercent, 1e-9)
    }

    @Test
    fun `matched load gives zero reflection`() {
        val r = Impedance.reflection(50.0, 50.0, 0.0)
        assertEquals(0.0, r.gammaMag, 1e-12)
        assertEquals(1.0, r.vswr, 1e-12)
    }

    @Test
    fun `series 1 plus j1 equals parallel 2 parallel j2`() {
        val (rp, xp) = Impedance.seriesToParallel(1.0, 1.0)
        assertEquals(2.0, rp, 1e-12)
        assertEquals(2.0, xp, 1e-12)
        val (rs, xs) = Impedance.parallelToSeries(2.0, 2.0)
        assertEquals(1.0, rs, 1e-12)
        assertEquals(1.0, xs, 1e-12)
    }

    // ---- Δλ ↔ Δf ----

    @Test
    fun `1 nm at 1550 nm is about 124_8 GHz`() {
        assertEquals(124.784e9, Optics.deltaFreqHz(1550e-9, 1e-9), 0.01e9)
    }

    @Test
    fun `1 nm at 1310 nm is about 174_7 GHz`() {
        assertEquals(174.72e9, Optics.deltaFreqHz(1310e-9, 1e-9), 0.2e9)
    }

    // ---- 格式化 ----

    @Test
    fun `engineering prefix formatting`() {
        assertEquals("223.6 m", Fmt.eng(0.22360679))
        assertEquals("1 k", Fmt.eng(1000.0))
        assertEquals("0", Fmt.eng(0.0))
        assertEquals("—", Fmt.eng(Double.NaN))
        assertEquals("2.00", Fmt.fixed(2.0000004, 2))
    }
}
