package com.example.sfn.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.example.sfn.lambda.dto.SqsMessage;
import com.example.sfn.lambda.exception.InvalidMessageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SendTaskFailureRequest;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
@Named("lambdaSqsHandler")
public class LambdaSqsHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaSqsHandler.class);
    private static final Pattern BUSINESS_KEY_PATTERN = Pattern.compile(".*-(\\d+)$");

    private final ObjectMapper objectMapper;
    private final SfnClient stepFunctionsClient;

    @Inject
    public LambdaSqsHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.stepFunctionsClient = SfnClient.builder().region(Region.US_EAST_1).build();
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                SqsMessage sqsMessage = parseMessage(message.getBody());
                LOGGER.info("Processando mensagem: TaskToken={}, BusinessKey={}, RetryCount={}",
                        sqsMessage.getTaskToken(), sqsMessage.getBusinessKey(), sqsMessage.getRetryCount());

                // Lógica para decidir se deve reexecutar baseado no retryCount
                boolean shouldReexecute = shouldReexecute(sqsMessage);

                stepFunctionsClient.sendTaskSuccess(SendTaskSuccessRequest.builder()
                        .taskToken(sqsMessage.getTaskToken())
                        .output(String.format("{\"reexecucao\": %b, \"retryCount\": %d}",
                                shouldReexecute, sqsMessage.getRetryCount()))
                        .build());

            } catch (InvalidMessageException e) {
                LOGGER.error("Invalid message: {}", message.getBody(), e);
                sendTaskFailure("InvalidMessageException", e.getMessage(), "Invalid or missing task token");
            } catch (Exception e) {
                LOGGER.error("Unexpected error processing message: {}", message.getBody(), e);
                sendTaskFailure("Exception", e.getMessage(), "Unexpected error");
            }
        }
        return null;
    }

    private boolean shouldReexecuteBasedOnBusinessKey(final String businessKey) {
        return Optional.ofNullable(businessKey)
                .map(BUSINESS_KEY_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> Integer.parseInt(matcher.group(1)))
                .map(number -> number % 2 == 0)
                .orElse(false);
    }

    private boolean shouldReexecute(SqsMessage sqsMessage) {
        int retryCount = sqsMessage.getRetryCount() != null ? sqsMessage.getRetryCount() : 0;

        if (retryCount >= 2) {
            LOGGER.info("Interrompendo reexecução - limite de tentativas alcançado. RetryCount: {}", retryCount);
            return false;
        }
        return shouldReexecuteBasedOnBusinessKey(sqsMessage.getBusinessKey());
    }

    private SqsMessage parseMessage(final String messageBody) {
        try {
            SqsMessage message = objectMapper.readValue(messageBody, SqsMessage.class);
            if (message.getTaskToken() == null || message.getExecutionId() == null) {
                throw new InvalidMessageException("Campos obrigatórios faltando: taskToken e executionId");
            }
            return message;
        } catch (JsonProcessingException e) {
            throw new InvalidMessageException("JSON inválido: " + messageBody, e);
        }
    }

    private void sendTaskFailure(String error, String cause, String taskToken) {
        stepFunctionsClient.sendTaskFailure(SendTaskFailureRequest.builder()
                .taskToken(taskToken)
                .error(error)
                .cause(cause)
                .build());
    }
}
