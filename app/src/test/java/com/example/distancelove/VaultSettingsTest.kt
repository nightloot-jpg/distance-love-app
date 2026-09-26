package com.example.distancelove

import com.example.distancelove.data.CountdownTime
import com.example.distancelove.data.local.VaultSettingsEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class VaultSettingsTest {

    @Test
    fun defaultVaultSettings_hasCorrectDefaults() {
        val settings = VaultSettingsEntity()
        assertEquals(1, settings.id)
        assertEquals("1402", settings.pinCode)
        assertTrue(settings.biometricsEnabled)
        assertEquals(1792231200000L, settings.reunionDateMillis)
    }

    @Test
    fun customReunionDate_calculatesCountdownCorrectly() {
        val now = ZonedDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneId.systemDefault())
        val targetMillis = ZonedDateTime.of(2025, 1, 11, 12, 30, 15, 0, ZoneId.systemDefault()).toInstant().toEpochMilli()

        val targetZdt = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(targetMillis), ZoneId.systemDefault())
        val duration = Duration.between(now.toLocalDateTime(), targetZdt.toLocalDateTime())

        val totalSeconds = duration.seconds
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val countdown = CountdownTime(days, hours, minutes, seconds)

        assertEquals(10L, countdown.days)
        assertEquals(2L, countdown.hours)
        assertEquals(30L, countdown.minutes)
        assertEquals(15L, countdown.seconds)
    }
}
