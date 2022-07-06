import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger

class TestContext : Context {
    override fun getAwsRequestId(): String {
        return "495b12a8-xmpl-4eca-8168-160484189f99"
    }

    override fun getLogGroupName(): String {
        return "/aws/lambda/my-function"
    }

    override fun getLogStreamName(): String {
        return "2020/02/26/[\$LATEST]704f8dxmpla04097b9134246b8438f1a"
    }

    override fun getFunctionName(): String {
        return "my-function"
    }

    override fun getFunctionVersion(): String {
        return "\$LATEST"
    }

    override fun getInvokedFunctionArn(): String {
        return "arn:aws:lambda:us-east-2:123456789012:function:my-function"
    }

    override fun getIdentity(): CognitoIdentity? {
        return null
    }

    override fun getClientContext(): ClientContext? {
        return null
    }

    override fun getRemainingTimeInMillis(): Int {
        return 300000
    }

    override fun getMemoryLimitInMB(): Int {
        return 512
    }

    override fun getLogger(): LambdaLogger {
        return TestLogger()
    }
}