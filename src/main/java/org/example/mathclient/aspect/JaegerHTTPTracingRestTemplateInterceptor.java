package org.example.mathclient.aspect;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * TracingRestTemplateInterceptor не предоставляет возможности для изменения имени заголовков "из коробки"
 */
@Slf4j
public class JaegerHTTPTracingRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;

    public JaegerHTTPTracingRestTemplateInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("Starting HTTP request: {} {}", request.getMethod(), request.getURI());

        Span activeSpan = tracer.activeSpan();
        if (activeSpan != null) {
            tracer.inject(activeSpan.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                @Override
                public void put(String key, String value) {
                    if ("uber-trace-id".equals(key)) {
                        key = "jaeger_traceId"; // Переименовываем заголовок
                    }
                    request.getHeaders().add(key, value);
                    log.info("Injected header: {} -> {}", key, value); // Логируем инжектированные заголовки
                }

                @Override
                public Iterator<Map.Entry<String, String>> iterator() {
                    throw new UnsupportedOperationException("iterator should never be used with TextMapInjectAdapter");
                }
            });
            log.info("Active span found, injecting trace context");
        } else {
            // Логируем предупреждение, если активного спана нет
            log.warn("No active span found. Cannot inject trace context.");
        }

        // Логируем только релевантные заголовки
        String jaegerTraceId = request.getHeaders().getFirst("jaeger_traceId");
        String uberTraceId = request.getHeaders().getFirst("uber-trace-id");

        log.info("Header: jaeger_traceId = {}",
                jaegerTraceId != null ? jaegerTraceId : "jaeger_traceId not found");
        log.info("Header: uber-trace-id = {}",
                uberTraceId != null ? uberTraceId : "uber-trace-id not found");

        // Выполняем запрос
        return execution.execute(request, body);
    }
}