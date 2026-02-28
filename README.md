# OpenTelemetry Java Demo Application

A simple Spring Boot Java application demonstrating manual instrumentation using the [OpenTelemetry Java SDK](https://opentelemetry.io/docs/languages/java/). The project exposes a REST endpoint (`/hello`) and uses the SDK to explicitly record distributed traces, metrics, and logs, exporting them via OTLP (OpenTelemetry Protocol).

## Features
This project demonstrates how to initialize and configure OpenTelemetry programmatically within a Spring Boot context:

- **Tracing**: Creates parent and child spans, adds custom span attributes, and records events and exceptions.
- **Metrics**: Registers counters, histograms, and gauges to track request volume, latency, and active request counts.
- **Logging**: Configures an OpenTelemetry logger bridge to emit logs as OpenTelemetry LogRecords.
- **Exporters**: Ships all telemetry (traces, metrics, logs) via OTLP gRPC to a compatible backend (e.g., OpenTelemetry Collector, Jaeger, Prometheus) on the default endpoint (`localhost:4317`). It also exports spans and logs to the console for local debugging.

## Prerequisites
- Java 17+
- Maven 3.6+

*(Optional)* To view the exported telemetry data, you will need an OpenTelemetry Collector or an OTLP-compatible backend running locally.

## Running the Application

1. **Build and Run**
   Start the Spring Boot application using Maven:
   ```bash
   mvn spring-boot:run
   ```
   The application will start quietly and listen on port `8081`.

2. **Trigger Telemetry**
   Generate some traffic to the endpoint by running:
   ```bash
   curl http://localhost:8081/hello
   ```

   You will receive the response `Hello, OpenTelemetry!`. 

3. **Verify Output**
   Check the console output where you ran the application. You should see span data and log records printed to stdout (via the `LoggingSpanExporter` and `SystemOutLogRecordExporter`). 
   
   If you have an OTLP backend running at `localhost:4317` (the default OpenTelemetry SDK endpoint), the traces, metrics, and logs will also be shipped there.

## Application Structure

- `OtelDemoApplication.java`: Standard entry point for the Spring Boot application.
- `OpenTelemetryConfig.java`: The core configuration class that sets up the `OpenTelemetrySdk` along with the `SdkTracerProvider`, `SdkMeterProvider`, and `SdkLoggerProvider`. It registers the OTLP exporters and exposes the `Tracer`, `Meter`, and `Logger` as Spring Beans.
- `HelloController.java`: A REST controller injected with OpenTelemetry components. It manually creates spans (including a dummy database span), manipulates counters, records latency histograms, and emits OTLP logs when the `/hello` endpoint is invoked.
