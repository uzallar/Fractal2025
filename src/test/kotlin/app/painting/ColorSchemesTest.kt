package app.painting

import app.painting.ColorSchemes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ColorSchemesTest {

    @Test
    fun `getColorSchemeByName returns standard`() {
        val scheme = ColorSchemes.getColorSchemeByName("standard")
        assertEquals(ColorSchemes.standard, scheme)
    }

    @Test
    fun `getColorSchemeByName returns fire`() {
        val scheme = ColorSchemes.getColorSchemeByName("fire")
        assertEquals(ColorSchemes.fire, scheme)
    }

    @Test
    fun `getColorSchemeByName returns rainbow`() {
        val scheme = ColorSchemes.getColorSchemeByName("rainbow")
        assertEquals(ColorSchemes.rainbow, scheme)
    }

    @Test
    fun `getColorSchemeByName returns cosmic`() {
        val scheme = ColorSchemes.getColorSchemeByName("cosmic")
        assertEquals(ColorSchemes.cosmic, scheme)
    }

    @Test
    fun `getColorSchemeByName returns standard for unknown`() {
        val scheme = ColorSchemes.getColorSchemeByName("unknown")
        assertEquals(ColorSchemes.standard, scheme)
    }
}
