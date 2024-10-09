//package org.example.mathclient.config;
//
//import io.opentracing.Span;
//import io.opentracing.Tracer;
//import io.opentracing.propagation.Format;
//import io.opentracing.propagation.TextMap;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpRequest;
//import org.springframework.http.client.ClientHttpRequestExecution;
//import org.springframework.http.client.ClientHttpRequestInterceptor;
//import org.springframework.http.client.ClientHttpResponse;
//
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.Map;
//
///**
// * Кастомный интерцептор для расширенного логирования
// */
//
//public class LoggingTracingRestTemplateInterceptor implements ClientHttpRequestInterceptor {
//
//    private final Tracer tracer;
//    private static final Logger log = LoggerFactory.getLogger(LoggingTracingRestTemplateInterceptor.class);
//
//    public LoggingTracingRestTemplateInterceptor(Tracer tracer) {
//        this.tracer = tracer;
//    }
//
//    @Override
//    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//        log.info("Starting HTTP request: {} {}", request.getMethod(), request.getURI());
//        log.info("Request Headers before sending: {}", request.getHeaders());
//
//        Span activeSpan = tracer.activeSpan();
//        if (activeSpan != null) {
//            tracer.inject(activeSpan.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
//                @Override
//                public void put(String key, String value) {
//                    request.getHeaders().add(key, value);
//                }
//
//                @Override
//                public Iterator<Map.Entry<String, String>> iterator() {
//                    throw new UnsupportedOperationException("iterator should never be used with TextMapInjectAdapter");
//                }
//            });
//            log.info("Active span found, injecting trace context");
//        } else {
//            log.warn("No active span found. Cannot inject trace context.");
//        }
//
//        // Логирование заголовков перед отправкой
//        log.info("Request Headers: {}", request.getHeaders());
//
//        ClientHttpResponse response = execution.execute(request, body);
//        log.info("Completed HTTP request: {} {}", request.getMethod(), request.getURI());
//
//        return response;
//    }
//}
