package org.rag4j.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.rag4j.webapp", "org.rag4j.agent", "org.rag4j.tokens"})
public class WorkshopAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkshopAgentApplication.class, args);
    }

}
