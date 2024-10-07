package org.example.mathclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MathClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MathClientApplication.class, args);
    }

}
