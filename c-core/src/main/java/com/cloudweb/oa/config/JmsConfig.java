package com.cloudweb.oa.config;

import com.cloudweb.oa.mq.MsgConsumer;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.Config;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@Configuration
@EnableJms
public class JmsConfig {

/*    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService brokerService() throws Exception {
        BrokerService brokerService = new BrokerService();
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        // brokerService.addConnector("vm://localhost:0");
        brokerService.addConnector("tcp://localhost:61616");

        brokerService.setBrokerName("broker");
        brokerService.setUseShutdownHook(false);
        return brokerService;
    }*/

    @Bean
    public ConnectionFactory connectionFactory() {
        Config config = Config.getInstance();
        boolean mqIsConsumerOpen = config.getBooleanProperty("mqIsConsumerOpen");
        if (!mqIsConsumerOpen) {
            return new ActiveMQConnectionFactory();
        }
        String mpServer = config.get("mqServer");
        int mqPort = config.getInt("mqPort");
        String mpUser = config.get("mqUser");
        String mpPwd = config.get("mqPwd");
        String serviceURL = "tcp://" + mpServer + ":" + mqPort;

        // ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(mpUser, mpPwd, serviceURL);
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    public ActiveMQQueue defaultDestination() {
        return new ActiveMQQueue(ConstUtil.QUEUE_MESSAGE);
    }

    @Bean(name = "jmsTemplate")
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setDefaultDestination(defaultDestination());
        // jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return jmsTemplate;
    }

    @Bean
    public DefaultMessageListenerContainer jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultMessageListenerContainer containerFactory = new DefaultMessageListenerContainer();
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setDestination(defaultDestination());

        Config config = Config.getInstance();
        boolean mqIsConsumerOpen = config.getBooleanProperty("mqIsConsumerOpen");
        if (!mqIsConsumerOpen) {
            return containerFactory;
        }

        containerFactory.setMessageListener(messageConsumer());
        // 为true时，消费者不会接收来自同一个连接的消息，消费者和生产者有可能使用同一个连接
        containerFactory.setPubSubNoLocal(false);
        // 当消费者要接收topic的消息时，pubSubDomain必须设置为
        // true。当消费者要接收queue的消息时，pubSubDomain必须设置为false
        containerFactory.setPubSubDomain(false);
        return containerFactory;
    }

    @Bean
    public MsgConsumer messageConsumer(){
        return new MsgConsumer();
    }
}
