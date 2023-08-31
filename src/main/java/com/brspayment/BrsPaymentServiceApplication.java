package com.brspayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;
import java.util.HashMap;

@SpringBootApplication
@EnableJms
public class BrsPaymentServiceApplication {

    public static void main(String[] args) {
//        SpringApplication.run(BrsPaymentServiceApplication.class, args);
        new SpringApplicationBuilder()
                .profiles("prod")
                .sources(BrsPaymentServiceApplication.class)
                .run(args);
    }
}
