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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class TracingAspect {

    private final Tracer tracer;
    private final HttpTracingExtractorNew httpTracingExtractor;

    public TracingAspect(@Qualifier("customJaegerTracer") Tracer tracer, HttpTracingExtractorNew httpTracingExtractor) {
        this.tracer = tracer;
        this.httpTracingExtractor = httpTracingExtractor;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object traceMethod(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();

        // Извлечение HttpServletRequest
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // Извлечение SpanContext из заголовков HTTP запроса
        SpanContext spanContext = httpTracingExtractor.extract(request);

        // Создаем новый спан, используя извлеченный контекст (если он есть)
        Span span;
        if (spanContext != null) {
            span = tracer.buildSpan(methodName).asChildOf(spanContext).start();
            log.info("Started tracing method: {} with extracted Trace ID: {}", methodName, span.context().toTraceId());
        } else {
            span = tracer.buildSpan(methodName).start();
            log.info("Started tracing method: {} without extracted context", methodName);
        }

        // Активируем спан с помощью Scope
        try (Scope scope = tracer.scopeManager().activate(span)) {
            // Логируем активный спан
            Span activeSpan = tracer.activeSpan();
            if (activeSpan != null) {
                log.info("Active span: {}", activeSpan.context().toTraceId());
            } else {
                log.warn("No active span found!");
            }

            // Выполнение метода
            return pjp.proceed();
        } catch (Throwable throwable) {
            span.setTag("error", true);
            throw throwable;
        } finally {
            // Завершаем спан после выполнения метода
            span.finish();
            log.info("Finished tracing method: {} with Trace ID: {}", methodName, span.context().toTraceId());
        }
    }
}

