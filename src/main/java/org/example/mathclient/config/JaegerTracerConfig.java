package org.example.mathclient.config;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JaegerTracerConfig {

    @Bean
    public Tracer jaegerTracer() {
        return new io.jaegertracing.Configuration("math-client")
                .withSampler(io.jaegertracing.Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1))
                .withReporter(io.jaegertracing.Configuration.ReporterConfiguration.fromEnv().withLogSpans(true))
                .getTracer();
    }

    @Bean
    public RestTemplate restTemplate(Tracer tracer) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new TracingRestTemplateInterceptor(tracer));
        return restTemplate;
    }
}
