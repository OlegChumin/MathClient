package org.example.mathclient.aspect;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Aspect
@Component("customTracingAspect")
public class JaegerTracingAspect {

    // ff флаг работы аспекта jaeger
    @Value("${feature-flag.ccc-jaeger.tracing.enabled}")
    private boolean tracingEnabled;

    private final Tracer tracer;
    private final JaegerHttpTracingExtractorNew httpTracingExtractor;

    @Autowired
    public JaegerTracingAspect(@Qualifier("customJaegerTracer") Tracer tracer,
                               @Qualifier("httpTracingExtractor") JaegerHttpTracingExtractorNew httpTracingExtractor) {
        this.tracer = tracer;
        this.httpTracingExtractor = httpTracingExtractor;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object traceMethod(ProceedingJoinPoint pjp) throws Throwable {
        log.debug("TracingEnabled {}", tracingEnabled);

        if (!tracingEnabled) {
            return pjp.proceed();
        }

        String methodName = pjp.getSignature().getName();
        log.debug("methodName: {}", methodName);

        // Извлечение HttpServletRequest
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (requestAttributes instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
        }

        //нужен для передачи контекста трассировки между спанами
        SpanContext parentContext = null;

        // Логирование заголовков
        if (request != null) {
            logRelevantRequestHeaders(request);

            // Извлекаем контекст из заголовков
            parentContext = httpTracingExtractor.extract(request);
        } else {
            log.warn("HttpServletRequest not available for method: {}", methodName);
        }

        // Если контекст был извлечен, создаем дочерний спан
        Span span;
        if (parentContext != null) {
            span = tracer.buildSpan(methodName).asChildOf(parentContext).start();
            log.debug("Started tracing method: {} with extracted context", methodName);
        } else {
            span = tracer.buildSpan(methodName).start();
            log.debug("Started tracing method: {} without extracted context", methodName);
        }

        // Логируем начало выполнения в спан -> все последующие действия, происходящие в этом потоке
        // (например, вызовы к другим сервисам, внутренние методы и т.д.), будут ассоциированы с этим активным Span.
        span.log("Starting method execution");

        // Активируем спан с помощью Scope
        /**
         * Переменная scope необходима для активации и деактивации Span, несмотря на то, что она не используется
         * явно в коде. Она управляет временем жизни Span и автоматически закрывает его по завершению метода.
         */
        try (Scope scope = tracer.scopeManager().activate(span)) {
            return pjp.proceed();
        } // scope.close() вызывается автоматически, когда выполнение блока try завершено
        catch (Throwable throwable) {
            span.setTag("error", true);
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("event", "error");
            logMap.put("error.object", throwable);
            span.log(logMap);
            throw throwable;
        } finally {
            span.log("Method execution finished");
            span.finish(); // Вручную завершаем спан
            log.debug("Finished tracing method: {} with Trace ID: {}", methodName, span.context().toTraceId());
        }
    }

    // Метод для логирования заголовков запроса
    private void logRelevantRequestHeaders(HttpServletRequest request) {
        log.info("Header: jaeger_traceId = {}",
                request.getHeader("jaeger_traceId") != null ? request.getHeader("jaeger_traceId") : "jaeger_traceId not found");
        log.info("Header: uber-trace-id = {}",
                request.getHeader("uber-trace-id") != null ? request.getHeader("uber-trace-id") : "uber-trace-id not found");
    }
}