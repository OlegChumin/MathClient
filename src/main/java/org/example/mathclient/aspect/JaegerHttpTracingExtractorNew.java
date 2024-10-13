package org.example.mathclient.aspect;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Enumeration;
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

        // HttpServletRequest.getHeaderNames() и другие методы в некоторых старых API возвращают именно Enumeration,
        // потому что они были написаны до появления Iterator
        //Iterator<String> headerNamesIterator = Collections.enumeration(headerNames).asIterator();
        Enumeration<String> headerNames = request.getHeaderNames();

        // Извлекаем все заголовки из HTTP-запроса
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // Логируем все заголовки
            log.info("Received header: {} -> {}", headerName, headerValue);

            // Изменяем имя заголовка на кастомный, если это uber-trace-id
            if ("uber-trace-id".equals(headerName)) {
                headers.put("jaeger_traceId", headerValue);
                log.info("Renamed header uber-trace-id -> jaeger_traceId");
            } else {
                headers.put(headerName, headerValue);
            }
        }


        // Извлечение контекста трассировки из заголовков с помощью TextMapAdapter implements TextMap
        // В TextMapAdapter все операции производятся через ключ-значение, без использования итераторов.
        return tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
    }
}