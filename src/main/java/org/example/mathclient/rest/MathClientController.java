package org.example.mathclient.rest;

import org.example.mathclient.dto.OperationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Value;


@RestController
@RequestMapping("/api/mathclient")
public class MathClientController {

    private final RestTemplate restTemplate;

    @Value("${calculator-service.url}")
    private String calculatorServiceUrl;

    public MathClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/sum")
    public String sum(@RequestParam double a, @RequestParam double b) {
        String url = calculatorServiceUrl + "/sum";
        OperationRequest request = new OperationRequest(a, b);

        return restTemplate.postForObject(url, request, String.class);
    }
}
