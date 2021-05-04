import org.junit.jupiter.api.Test
import com.eliottgray.kotlin.main

class TestMain {

    @Test
    fun test_no_args(){
        main(arrayOf())
    }

    @Test
    fun test_args(){
        main(arrayOf("A", "Whole", "New", "World!"))
    }
}