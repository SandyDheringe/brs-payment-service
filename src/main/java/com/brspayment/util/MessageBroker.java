package com.brspayment.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Configuration
public class MessageBroker {

    private final JmsTemplate jmsTemplate;

    @Autowired
    MessageBroker(JmsTemplate jmsTemplate){
        this.jmsTemplate = jmsTemplate;
    }


    public void sendMessage(String destination, String message) {
        jmsTemplate.send(destination, session -> session.createTextMessage(message));
        System.out.println("Sent message: " + message);
    }
}
