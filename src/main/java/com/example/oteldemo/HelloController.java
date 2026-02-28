package com.example.oteldemo;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final Tracer tracer;
    private final LongCounter requestCounter;
    private final LongHistogram requestLatency;
    private final LongUpDownCounter activeRequests;
    private final Logger otelLogger;

    public HelloController(Tracer tracer, Meter meter, Logger logger) {
        this.tracer = tracer;
        this.otelLogger = logger;
        this.requestCounter = meter.counterBuilder("hello_requests_total")
                .setDescription("Counts hello requests")
                .build();
        this.requestLatency = meter.histogramBuilder("hello_request_latency_ms")
                .setDescription("Latency of hello requests")
                .ofLongs()
                .build();
        this.activeRequests = meter.upDownCounterBuilder("hello_active_requests")
                .setDescription("Number of active hello requests")
                .build();
    }

    @GetMapping("/hello")
    public String hello() {
        long startTime = System.currentTimeMillis();
        activeRequests.add(1);
        requestCounter.add(1);

        otelLogger.logRecordBuilder()
                .setSeverity(Severity.INFO)
                .setBody("Hello endpoint was called")
                .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("endpoint"), "/hello")
                .emit();

        Span span = tracer.spanBuilder("hello-endpoint-span").startSpan();

        try (var scope = span.makeCurrent()) {
            span.setAttribute("http.method", "GET");
            span.setAttribute("custom.attribute", "some-value");
            span.addEvent("Executing heavy lifting logic");

            // Simulate work
            Thread.sleep(100);

            Span dbSpan = tracer.spanBuilder("dummy-db-call")
                    .setParent(io.opentelemetry.context.Context.current().with(span))
                    .startSpan();

            try (var dbScope = dbSpan.makeCurrent()) {
                dbSpan.setAttribute("db.system", "mysql");
                dbSpan.setAttribute("db.statement", "SELECT * FROM dummy_table");
                dbSpan.addEvent("Executing dummy query");

                // Simulate database latency
                Thread.sleep(50);
            } catch (Exception e) {
                dbSpan.recordException(e);
                throw e; // rethrow to be caught by outer span
            } finally {
                dbSpan.end();
            }

            return "Hello, OpenTelemetry!";
        } catch (Exception e) {
            span.recordException(e);
            return "Error!";
        } finally {
            span.end();
            activeRequests.add(-1);
            requestLatency.record(System.currentTimeMillis() - startTime);
        }
    }
}
