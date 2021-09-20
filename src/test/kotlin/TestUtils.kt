import com.eliottgray.kotlin.mutateSeed
import com.eliottgray.kotlin.xyzToNWCorner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.math.round
import kotlin.random.Random
import kotlin.test.assertEquals

class MutateSeedTest {

    @Test
    fun test_small_numbers(){
        val result = mutateSeed(0.1, 0.2)
        assertTrue(result in -1.0..1.0)
    }

    @Test
    fun test_large_numbers(){
        val result = mutateSeed(999999.0, 999999.0)
        assertTrue(result in -1.0..1.0)
    }

    @Test
    fun test_distribution(){
        // Iterate through a large number of results to test the bounds.
        var counter = 0
        val resultSet: HashSet<Double> = HashSet()
        val newRandom = Random.Default
        while (counter < 50000){
            counter += 1
            val one = newRandom.nextDouble()
            val two = newRandom.nextDouble()
            val result = mutateSeed(one, two)

            // Round to 2 decimal places.
            val rounded = round(result / 0.01) * 0.01
            resultSet.add(rounded)
        }
        // Expect range -1.0 to 1.0, inclusive.
        assertTrue(resultSet.contains(1.0))
        assertTrue(resultSet.contains(-1.0))
        assertTrue(resultSet.contains(0.0))
        assertTrue(resultSet.contains(0.29))
        assertFalse(resultSet.contains(1.1))
        assertFalse(resultSet.contains(-1.1))
    }
}

class XyzToNWCornerTest {
    @Test
    fun test_level_zero() {
        val nw = xyzToNWCorner(0, 0, 0)
        val nwLon = nw.first
        val nwLat = nw.second

        assertEquals(nwLon, -180.0)
        assertEquals(nwLat, 85.051128, 0.000001)
    }
}