package org.example.mathclient.aspect;

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Aspect
@Component("customTracingAspect")
public class TracingAspect {

    // ff флаг работы аспекта jaeger
    @Value("${feature-flag.ccc-jaeger.tracing.enabled}")
    private boolean tracingEnabled;

    private final Tracer tracer;
    private final HttpTracingExtractorNew httpTracingExtractor;

    @Autowired
    public TracingAspect(@Qualifier("customJaegerTracer") Tracer tracer, @Qualifier("httpTracingExtractor") HttpTracingExtractorNew httpTracingExtractor) {
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

//        // Извлечение HttpServletRequest
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        SpanContext spanContext = null; // Инициализация SpanContext

//        if (requestAttributes instanceof ServletRequestAttributes) {
//            HttpServletRequest request = (HttpServletRequest) ((ServletRequestAttributes) requestAttributes).getRequest();
//
//            // Извлечение SpanContext из заголовков HTTP запроса
//            spanContext = httpTracingExtractor.extract(request);
//        } else {
//            log.warn("Cannot retrieve HttpServletRequest");
//            return pjp.proceed();  // Продолжаем выполнение метода, если HttpServletRequest недоступен
//        }

        // Создаем новый спан, используя извлеченный контекст (если он есть)
        Span span;
        if (spanContext != null) {
            span = tracer.buildSpan(methodName).asChildOf(spanContext).start();
            log.debug("Span ID: {}", spanContext.toSpanId());
            log.debug("Trace ID: {}", spanContext.toTraceId());
            log.debug("Started tracing method: {} with extracted Trace ID: {}", methodName, span.context().toTraceId());

            // Проверяем, если есть родительский Span ID (зависит от реализации SpanContext)
            if (spanContext instanceof JaegerSpanContext) {
                JaegerSpanContext jaegerSpanContext = (JaegerSpanContext) spanContext;
                log.info("Parent Span ID: {}", jaegerSpanContext.getParentId());
            }

            // Извлекаем baggage items (если есть)
            spanContext.baggageItems().forEach(entry -> {
                log.debug("Baggage item: {} -> {}", entry.getKey(), entry.getValue());
            });
        } else {
            span = tracer.buildSpan(methodName).start();
            log.debug("Started tracing method: {} without extracted context", methodName);
        }

        // Логируем начало выполнения в спан
        span.log("Starting method execution");

        // Активируем спан с помощью Scope
        try (Scope scope = tracer.scopeManager().activate(span)) {
            // Логируем активный спан
            Span activeSpan = tracer.activeSpan();
            if (activeSpan != null) {
                log.debug("Active span: {}", activeSpan.context().toTraceId());
            } else {
                log.warn("No active span found!");
            }

            // Выполнение метода
            return pjp.proceed();
        } catch (Throwable throwable) {
            span.setTag("error", true);
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("event", "error");
            logMap.put("error.object", throwable);
            span.log(logMap);
            throw throwable;
        } finally {
            // Логируем завершение в спан
            span.log("Method execution finished");
            // Завершаем спан после выполнения метода
            span.finish();
            log.debug("Finished tracing method: {} with Trace ID: {}", methodName, span.context().toTraceId());
        }
    }
}