package org.example.mathclient.aspect;

import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MathClientTracingAspect {

    private final Tracer tracer;

    public MathClientTracingAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Before("execution(* org.example.mathclient.rest.MathClientController.*(..))")
    public void beforeMethodExecution() {
        Span activeSpan = tracer.activeSpan();
        if (activeSpan != null) {
            log.info("Starting span for method, traceId: {}", activeSpan.context().toTraceId());
        }
    }

    @After("execution(* org.example.mathclient.rest.MathClientController.*(..))")
    public void afterMethodExecution() {
        Span activeSpan = tracer.activeSpan();
        if (activeSpan != null) {
            log.info("Finishing span for method, traceId: {}", activeSpan.context().toTraceId());
        }
    }
}
