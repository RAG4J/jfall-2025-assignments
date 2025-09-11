package org.rag4j.evals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.rag4j.evals", "org.rag4j.agent"})
public class EvalsWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvalsWebApplication.class, args);
    }
}
