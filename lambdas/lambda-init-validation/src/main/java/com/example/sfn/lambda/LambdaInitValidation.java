package com.example.sfn.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.sfn.lambda.exception.InvalidMessageException;
import com.example.sfn.lambda.exception.MessageProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
@Named("lambdaInitValidation")
public class LambdaInitValidation implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaInitValidation.class);

    @ConfigProperty(name = "app.processar.request")
    boolean processarRequest;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        Map<String, Object> response = new HashMap<>();

        try {
            String businessKey = (String) event.get("businessKey");
            if (businessKey == null) {
                throw new InvalidMessageException("Campos obrigatórios faltando: businessKey");
            }

            response.put("businessKey", businessKey);
            response.put("processarRequest", processarRequest);
            response.put("executionId", event.get("executionId")); // importantíssimo
            response.put("retryCount", event.getOrDefault("retryCount", 0));


            LOGGER.info("--- Processando mensagem: BusinessKey = {}", businessKey);
            LOGGER.info("--- Processando UUID mensagem: UUID = {}", UUID.randomUUID());
            LOGGER.info("--- processarRequest = {}", processarRequest);

        } catch (InvalidMessageException e) {
            LOGGER.error("Mensagem inválida: {}", event, e);
            throw new MessageProcessingException("Falha ao processar mensagem inválida", e);
        } catch (MessageProcessingException e) {
            LOGGER.error("Erro ao processar mensagem: {}", event, e);
            throw e;
        }

        return response;
    }
}
