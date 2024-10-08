package org.example.mathclient.rest;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.example.mathclient.dto.OperationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/mathclient")
@Slf4j
public class MathClientController {

    private final RestTemplate restTemplate;
    private final Tracer tracer;  // Инжектируем Tracer

    @Autowired
    public MathClientController(RestTemplate restTemplate, Tracer tracer) {
        this.restTemplate = restTemplate;
        this.tracer = tracer;  // Получаем бин Tracer
    }

    @GetMapping("/sum")
    public String sum(@RequestParam double a, @RequestParam double b) {
        String url = "http://localhost:8080/api/calculator/sum";
        OperationRequest request = new OperationRequest(a, b);

        // Создаем новый спан и активируем его
        Span span = tracer.buildSpan("math-client-sum").start();
        try (Scope scope = tracer.scopeManager().activate(span)) {
            // Логируем активный спан
            Span activeSpan = tracer.activeSpan();
            if (activeSpan != null) {
                log.info("Active span found: {}", activeSpan.context().toTraceId());
            } else {
                log.warn("No active span found before making HTTP request!");
            }

            // Выполняем HTTP-запрос с трассировкой
            return restTemplate.postForObject(url, request, String.class);
        } finally {
            // Завершаем спан после выполнения метода
            span.finish();
        }
    }

}
