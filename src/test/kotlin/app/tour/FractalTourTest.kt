import app.painting.convertation.Plain
import app.tour.FractalTour
import app.tour.TourFrame
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class TourFrameTest {

    private val samplePlain = Plain(
        width = 800f,
        height = 600f,
        xMin = -2.0,
        xMax = 1.0,
        yMin = -1.5,
        yMax = 1.5
    )

    @Test
    fun `TourFrame should have correct default duration`() {
        val frame = TourFrame(
            plain = samplePlain,
            fractalName = "Mandelbrot",
            colorSchemeName = "Default"
        )
        assertEquals(3000L, frame.durationMs)
    }

    @Test
    fun `TourFrame should store provided duration correctly`() {
        val frame = TourFrame(
            plain = samplePlain,
            fractalName = "BurningShip",
            colorSchemeName = "Hot",
            durationMs = 5000L
        )
        assertEquals(5000L, frame.durationMs)
    }

    @Test
    fun `TourFrame should be data class with proper equals and hashCode`() {
        val frame1 = TourFrame(samplePlain, "Mandelbrot", "Default", 4000L)
        val frame2 = TourFrame(samplePlain, "Mandelbrot", "Default", 4000L)
        val frame3 = TourFrame(samplePlain, "Mandelbrot", "Default", 5000L)

        assertEquals(frame1, frame2)
        assertEquals(frame1.hashCode(), frame2.hashCode())
        assertNotEquals(frame1, frame3)
    }

    @Test
    fun `TourFrame toString should contain all fields`() {
        val frame = TourFrame(samplePlain, "Julia", "Rainbow", 2000L)
        val str = frame.toString()

        assertTrue(str.contains("plain=$samplePlain"))
        assertTrue(str.contains("fractalName=Julia"))
        assertTrue(str.contains("colorSchemeName=Rainbow"))
        assertTrue(str.contains("durationMs=2000"))
    }
}

class FractalTourTest {

    private val samplePlain = Plain(
        width = 800f,
        height = 600f,
        xMin = -2.0,
        xMax = 1.0,
        yMin = -1.5,
        yMax = 1.5
    )

    private val sampleFrame = TourFrame(
        plain = samplePlain,
        fractalName = "Mandelbrot",
        colorSchemeName = "Default",
        durationMs = 3000L
    )

    @Test
    fun `FractalTour should have correct default values`() {
        val tour = FractalTour()

        assertEquals("Новая экскурсия", tour.name)
        assertTrue(tour.frames.isEmpty())
        assertFalse(tour.loop)
    }

    @Test
    fun `FractalTour should store provided parameters correctly`() {
        val frames = listOf(
            sampleFrame,
            sampleFrame.copy(fractalName = "Julia", durationMs = 4000L)
        )

        val tour = FractalTour(
            name = "Мой тур по Мандельброту",
            frames = frames,
            loop = true
        )

        assertEquals("Мой тур по Мандельброту", tour.name)
        assertEquals(frames, tour.frames)
        assertTrue(tour.loop)
    }

    @Test
    fun `FractalTour should be immutable data class with proper equality`() {
        val tour1 = FractalTour("Tour1", listOf(sampleFrame), true)
        val tour2 = FractalTour("Tour1", listOf(sampleFrame), true)
        val tour3 = FractalTour("Tour1", listOf(sampleFrame), false)

        assertEquals(tour1, tour2)
        assertEquals(tour1.hashCode(), tour2.hashCode())
        assertNotEquals(tour1, tour3)
    }

    @Test
    fun `FractalTour toString should include all fields`() {
        val tour = FractalTour("TestTour", listOf(sampleFrame), true)

        val str = tour.toString()

        assertTrue(str.contains("name=TestTour"))
        assertTrue(str.contains("frames=[TourFrame("))
        assertTrue(str.contains("loop=true"))
    }

    @Test
    fun `empty FractalTour should be equal to another empty one`() {
        val tour1 = FractalTour()
        val tour2 = FractalTour()

        assertEquals(tour1, tour2)
    }
}