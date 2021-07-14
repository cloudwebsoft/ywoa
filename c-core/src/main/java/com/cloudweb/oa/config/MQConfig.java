package com.cloudweb.oa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

@Configuration
public class MQConfig {
/*    @Bean
    public ConnectionFactory getConnectionFactory() {
        TibjmsConnectionFactory connectionFactory = new TibjmsConnectionFactory(urlBrocker);
        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(getConnectionFactory());
        template.setPubSubDomain(false); // false for a Queue, true for a Topic
        return template;
    }*/
}
