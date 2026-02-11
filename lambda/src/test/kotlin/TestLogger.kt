import com.amazonaws.services.lambda.runtime.LambdaLogger

class TestLogger: LambdaLogger {
    override fun log(message: String?) {}

    override fun log(message: ByteArray?) {}
}