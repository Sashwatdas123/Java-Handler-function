package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.opentracing.util.GlobalTracer;
import com.newrelic.opentracing.aws.LambdaTracing;
import com.newrelic.opentracing.LambdaTracer;
import io.opentracing.Span;

import java.util.Map;

/**
 * Tracing request handler that creates a span on every invocation of a Lambda.
 */
public class MyLambdaHandler implements RequestHandler<Map<String, Object>, String> {
    static {
        // Register the New Relic OpenTracing LambdaTracer as the Global Tracer
        GlobalTracer.registerIfAbsent(LambdaTracer.INSTANCE);
    }

    /**
     * Method that handles the Lambda function request - required by RequestHandler interface
     */
    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        return doHandleRequest(input, context);
    }

    /**
     * Your custom method that handles the Lambda function request with New Relic tracing.
     */
    public String doHandleRequest(Map<String, Object> input, Context context) {
        return LambdaTracing.instrument(input, context, (event, ctx) -> {
            // Add custom span for your business logic
            Span businessSpan = GlobalTracer.get().buildSpan("process-lambda-input").start();
            try {
                businessSpan.setTag("input.size", input.size());
                
                // Your function logic here
                String response = "Processed input with " + input.size() + " keys";
                System.out.println("Generated response: " + response);
                businessSpan.setTag("response.length", response.length());
                return response;
            } finally {
                businessSpan.finish();
            }
        });
    }
}