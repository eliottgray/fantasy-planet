import com.eliottgray.kotlin.Point
import com.eliottgray.kotlin.Tetrahedron
import org.junit.jupiter.api.Assertions


fun testPointsAlmostEqual(one: Point, two: Point) {
    Assertions.assertEquals(one.x, two.x, 1.0E-7, "X coordinate error.")
    Assertions.assertEquals(one.y, two.y, 1.0E-7, "Y coordinate error.")
    Assertions.assertEquals(one.z, two.z, 1.0E-7, "Z coordinate error.")
    Assertions.assertEquals(one.alt, two.alt, 1.0E-7, "Alt coordinate error.")
    Assertions.assertEquals(one.seed, two.seed, "Seed error.")
}

fun testTetrahedronsAlmostEqual(expected: Tetrahedron, actual: Tetrahedron) {
    testPointsAlmostEqual(expected.a, actual.a)
    testPointsAlmostEqual(expected.b, actual.b)
    testPointsAlmostEqual(expected.c, actual.c)
    testPointsAlmostEqual(expected.d, actual.d)
}
