package com.brspayment.messages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import java.util.Map;

@Configuration
public class MessageBroker {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageBroker(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendInventoryUpdateMessage(String destination, BusBookingMessage busBookingMessage) {
        Map<String, Object> object = objectMapper
                .convertValue(busBookingMessage, new TypeReference<>() {
                });

        jmsTemplate.convertAndSend(destination, object);
        System.out.println("Sent message: " + object);
    }
}
