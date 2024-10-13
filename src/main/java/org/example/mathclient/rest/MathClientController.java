package org.example.mathclient.rest;

import lombok.extern.slf4j.Slf4j;
import org.example.mathclient.dto.OperationRequestDTO;
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

    public MathClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/sum")
    public String sum(@RequestParam double a, @RequestParam double b) {
        String url = "http://localhost:8080/api/calculator/sum";
        OperationRequestDTO request = new OperationRequestDTO(a, b);

        // Выполняем HTTP-запрос с трассировкой через RestTemplate
        String result = restTemplate.postForObject(url, request, String.class);

        log.info("Received response: {}", result);
        return result;
    }

    @GetMapping("/subtract")
    public String subtract(@RequestParam double a, @RequestParam double b) {
        String url = "http://localhost:8080/api/calculator/subtract";
        OperationRequestDTO request = new OperationRequestDTO(a, b);

        // Выполняем HTTP-запрос с трассировкой через RestTemplate
        String result = restTemplate.postForObject(url, request, String.class);

        log.info("Received response: {}", result);
        return result;
    }

    @GetMapping("/multiply")
    public String multiply(@RequestParam double a, @RequestParam double b) {
        String url = "http://localhost:8080/api/calculator/multiply";
        OperationRequestDTO request = new OperationRequestDTO(a, b);

        // Выполняем HTTP-запрос с трассировкой через RestTemplate
        String result = restTemplate.postForObject(url, request, String.class);

        log.info("Received response: {}", result);
        return result;
    }

    @GetMapping("/divide")
    public String divide(@RequestParam double a, @RequestParam double b) {
        String url = "http://localhost:8080/api/calculator/divide";
        OperationRequestDTO request = new OperationRequestDTO(a, b);

        // Выполняем HTTP-запрос с трассировкой через RestTemplate
        String result = restTemplate.postForObject(url, request, String.class);

        log.info("Received response: {}", result);
        return result;
    }
}
