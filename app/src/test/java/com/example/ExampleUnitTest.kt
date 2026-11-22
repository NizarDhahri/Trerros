package com.example

import com.example.model.GladeCatalog
import com.example.model.GladeId
import com.example.model.TimeOfDayMood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `every glade has a lit starting lantern and valid connections`() {
        for (glade in GladeCatalog.glades) {
            assertTrue("${glade.title} should start with a lit lantern", glade.lanterns.any { it.startsLit })
            val ids = glade.lanterns.map { it.id }.toSet()
            for (lantern in glade.lanterns) {
                for (targetId in lantern.connectedToIds) {
                    assertTrue(
                        "Connection $targetId in ${glade.title} must exist",
                        targetId in ids
                    )
                }
            }
            assertTrue(
                "Spirit required lantern must exist in ${glade.title}",
                glade.spirit.requiredLanternId in ids
            )
        }
        assertEquals(4, GladeCatalog.glades.size)
        assertEquals(GladeId.POSTHOUSE, GladeCatalog.glades.first().id)
        assertEquals(20, GladeCatalog.totalLanternCount)
    }

    @Test
    fun `keepsakes reference valid glades and mood keys round trip`() {
        val gladeIds = GladeCatalog.glades.map { it.id }.toSet()
        for (item in GladeCatalog.keepsakes) {
            assertTrue(item.gladeId in gladeIds)
            assertTrue(item.cost > 0)
        }
        for (mood in TimeOfDayMood.entries) {
            assertEquals(mood, TimeOfDayMood.fromName(mood.name))
        }
    }
}
