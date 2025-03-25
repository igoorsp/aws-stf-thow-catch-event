package com.example.sfn.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@Named("consultarServiceHandler")
public class ConsultarServiceHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultarServiceHandler.class);
    private static final int MAX_RETRIES = 3;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        LOGGER.info("Payload recebido: {}", event);

        try {
            Thread.sleep(2000); // Espera por 2 segundos
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while sleeping", e);
        }

        int retryCount = (int) event.getOrDefault("retryCount", 0);

        Map<String, Object> response = new HashMap<>();
        response.put("businessKey", event.getOrDefault("businessKey", "default-businessKey"));
        response.put("executionId", event.getOrDefault("executionId", "default-executionId"));
        response.put("retryCount", retryCount);
        response.put("processarRequest", retryCount < MAX_RETRIES);
        response.putAll(event);

        return response;
    }

}