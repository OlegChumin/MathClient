package org.example.mathclient.aspect;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpTracingExtractorNew {

    private final Tracer tracer;

    public HttpTracingExtractorNew(Tracer tracer) {
        this.tracer = tracer;
    }

    public SpanContext extract(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        // Извлекаем все заголовки из HTTP-запроса
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // Изменяем имя заголовка на кастомный
            if ("jaeger_traceId".equals(headerName)) {
                headers.put("uber-trace-id", headerValue);
            } else {
                headers.put(headerName, headerValue);
            }
        }

        // Извлечение контекста трассировки из заголовков
        return tracer.extract(Format.Builtin.HTTP_HEADERS,  new HttpServletRequestExtractAdapter(request));
    }
}
