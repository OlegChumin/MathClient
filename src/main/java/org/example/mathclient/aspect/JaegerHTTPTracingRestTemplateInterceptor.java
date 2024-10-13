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
        log.info("Request Headers before sending: {}", request.getHeaders());

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

        log.info("Request Headers after injection: {}", request.getHeaders()); // Логируем заголовки после инжекции

//        ClientHttpResponse response = execution.execute(request, body);
//        log.debug("Completed HTTP request: {} {}", request.getMethod(), request.getURI());

        // Выполняем запрос
        return execution.execute(request, body);
    }
}