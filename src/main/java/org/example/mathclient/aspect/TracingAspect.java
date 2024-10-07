package org.example.mathclient.aspect;

import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TracingAspect {

    @Autowired
    private Tracer tracer;

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object traceMethod(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();

        // Создаем новый спан
        Span span = tracer.buildSpan(methodName).start();
        log.info("Started tracing method: {}", methodName);

        Object result;
        try {
            // Выполнение метода
            result = pjp.proceed();
        } catch (Throwable throwable) {
            span.setTag("error", true);
            throw throwable;
        } finally {
            span.finish();
            log.info("Finished tracing method: {}", methodName);
        }

        return result;
    }
}
