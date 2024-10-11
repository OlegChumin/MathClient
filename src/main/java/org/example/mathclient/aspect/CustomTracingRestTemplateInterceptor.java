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

@Slf4j
public class CustomTracingRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;

    public CustomTracingRestTemplateInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Span activeSpan = tracer.activeSpan();
        if (activeSpan != null) {
            tracer.inject(activeSpan.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                @Override
                public void put(String key, String value) {
                    if ("uber-trace-id".equals(key)) {
                        key = "jaeger_traceId";  // Переименовываем заголовок
                    }
                    request.getHeaders().add(key, value);
                }

                @Override
                public Iterator<Map.Entry<String, String>> iterator() {
                    throw new UnsupportedOperationException("iterator should never be used with TextMapInjectAdapter");
                }
            });
        } else {
            // Логируем предупреждение, если активного спана нет
            log.warn("No active span found. Cannot inject trace context.");
        }

        // Выполняем запрос
        return execution.execute(request, body);
    }
}
