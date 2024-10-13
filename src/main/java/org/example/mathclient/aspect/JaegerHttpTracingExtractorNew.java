package org.example.mathclient.aspect;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JaegerHttpTracingExtractorNew {

    private final Tracer tracer;

    @Autowired
    public JaegerHttpTracingExtractorNew(@Qualifier("customJaegerTracer") Tracer tracer) {
        this.tracer = tracer;
    }

    public SpanContext extract(HttpServletRequest request) {
        // Создаем карту для заголовков
        Map<String, String> headers = new HashMap<>();

        // Извлекаем только те заголовки, которые нас интересуют
        String jaegerTraceId = request.getHeader("jaeger_traceId");
        String uberTraceId = request.getHeader("uber-trace-id");

//        // Логируем и добавляем в карту только если заголовки присутствуют
//        if (jaegerTraceId != null) {
//            headers.put("jaeger_traceId", jaegerTraceId);
//            log.info("Found jaeger_traceId: {}", jaegerTraceId);
//        }

        if (uberTraceId != null) {
            //headers.put("jaeger_traceId", uberTraceId);  // Переименовываем uber-trace-id в jaeger_traceId
            headers.put("uber-trace-id", uberTraceId);  // Переименовываем uber-trace-id в jaeger_traceId
            log.info("Found uber-trace-id, renamed to jaeger_traceId: {}", uberTraceId);
        }

        // Логируем, если ни один из заголовков не найден
        if (headers.isEmpty()) {
            log.warn("No tracing headers found in the request");
        }

        // Извлечение контекста трассировки из заголовков
        SpanContext spanContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));

        // Логируем результат извлечения SpanContext
        if (spanContext != null) {
            log.info("Extracted SpanContext: traceId = {}, spanId = {}", spanContext.toTraceId(), spanContext.toSpanId());
        } else {
            log.warn("Failed to extract SpanContext from headers");
        }

        return spanContext;
    }
}