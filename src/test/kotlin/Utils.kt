import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions


fun testPointEquality(one: Point, two: Point) {
    // TODO: Use equality override, to test all attributes at once?  Is that even necessary?
    Assertions.assertEquals(one.x, two.x, 1.0E-7, "X coordinate error.")
    Assertions.assertEquals(one.y, two.y, 1.0E-7, "Y coordinate error.")
    Assertions.assertEquals(one.z, two.z, 1.0E-7, "Z coordinate error.")
    Assertions.assertEquals(one.alt, two.alt, 1.0E-7, "Alt coordinate error.")
    Assertions.assertEquals(one.seed, two.seed, "Seed error.")
}
