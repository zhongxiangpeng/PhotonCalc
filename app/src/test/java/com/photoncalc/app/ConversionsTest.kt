package com.photoncalc.app

import com.photoncalc.app.core.Conversions
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversionsTest {

    private val eps = 1e-9

    // ---- 功率 <-> dBm/dBW ----

    @Test
    fun `0 dBm equals 1 mW`() {
        assertEquals(1e-3, Conversions.dbmToWatt(0.0), eps)
    }

    @Test
    fun `30 dBm equals 1 W`() {
        assertEquals(1.0, Conversions.dbmToWatt(30.0), eps)
    }

    @Test
    fun `1 W equals 30 dBm`() {
        assertEquals(30.0, Conversions.wattToDbm(1.0), eps)
    }

    @Test
    fun `10 dBm equals 10 mW`() {
        assertEquals(1e-2, Conversions.dbmToWatt(10.0), eps)
    }

    @Test
    fun `dBW is dBm minus 30`() {
        assertEquals(-30.0, Conversions.dbmToDbw(0.0), eps)
        assertEquals(17.3, Conversions.dbwToDbm(Conversions.dbmToDbw(17.3)), 1e-9)
    }

    // ---- 功率 <-> 电压(50 Ω) ----

    @Test
    fun `0 dBm into 50 ohm is 223_6 mV rms`() {
        assertEquals(0.223606798, Conversions.wattToVrms(1e-3, 50.0), 1e-8)
    }

    @Test
    fun `10 dBm into 50 ohm is 2 V pp`() {
        val vpp = Conversions.vrmsToVpp(Conversions.wattToVrms(1e-2, 50.0))
        assertEquals(2.0, vpp, 1e-9)
    }

    @Test
    fun `0 dBm into 75 ohm is 273_9 mV rms`() {
        assertEquals(0.273861279, Conversions.wattToVrms(1e-3, 75.0), 1e-8)
    }

    @Test
    fun `0 dBm is about plus107 dBuv at 50 ohm`() {
        assertEquals(106.9897, Conversions.dbmToDbuv(0.0, 50.0), 1e-3)
    }

    @Test
    fun `dbuv roundtrip at 50 ohm`() {
        val x = -23.4
        assertEquals(x, Conversions.dbmToDbuv(Conversions.dbuvToDbm(x, 50.0), 50.0), 1e-9)
    }

    // ---- 电压 <-> dBV/dBμV ----

    @Test
    fun `1 V rms is 0 dBV and 120 dBuv`() {
        assertEquals(0.0, Conversions.vrmsToDbv(1.0), eps)
        assertEquals(120.0, Conversions.vrmsToDbuv(1.0), eps)
    }

    // ---- 增益 / 衰减 ----

    @Test
    fun `plus 6 dB doubles voltage`() {
        assertEquals(1.995262315, Conversions.gainVrms(1.0, 6.0), 1e-8)
    }

    @Test
    fun `minus 3 dB halves power`() {
        assertEquals(-3.0103, Conversions.gainDbm(0.0, -3.0103), 1e-9)
        assertEquals(5e-4, Conversions.dbmToWatt(Conversions.gainDbm(0.0, -3.0103)), 1e-9)
    }
}
