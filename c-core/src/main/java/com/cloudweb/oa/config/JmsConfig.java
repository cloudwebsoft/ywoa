package com.cloudweb.oa.config;

import cn.js.fan.util.PropertiesUtil;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.mq.MsgConsumer;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.Config;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@DependsOn({"SpringUtil"}) // SpringUtil已设为-100000，优先级最高
@AutoConfigureAfter({DruidConfig.class, SystemConfig.class})
@Configuration
@ConditionalOnProperty(name = "mq.type", matchIfMissing = false, havingValue = "ActiveMQ")
@EnableJms
public class JmsConfig {

    @Value("${activemq.server}")
    private String server;

    @Value("${activemq.port}")
    private String port;

    @Value("${activemq.user}")
    private String user;

    @Value("${activemq.pwd}")
    private String pwd;

    @Value("${activemq.isConsumerOpen}")
    private boolean isConsumerOpen;

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
        // Config config = Config.getInstance();
        // 此处不可行，返回new ActiveMQConnectionFactory，反而会因为没有用户名、密码致出现问题，致linux下面连接错误，启动程序时会卡住
        // 并且mqIsConsumerOpen没什么用处，ActiveMQConnectionFactory 仍然会被初始化，会被执行，如果不通的话，就会报以下错误，且致程序启动失败：
        // Failed to connect to [tcp://localhost:61616] after: 10 attempt(s) continuing to retry.
        /*boolean mqIsConsumerOpen = config.getBooleanProperty("mqIsConsumerOpen");
        if (!mqIsConsumerOpen) {
            // return new ActiveMQConnectionFactory();
            return null;
        }*/

        /*String mpServer = config.get("mqServer");
        int mqPort = config.getInt("mqPort");
        String mpUser = config.get("mqUser");
        String mpPwd = config.get("mqPwd");
        String serviceURL = "tcp://" + mpServer + ":" + mqPort;*/

        String serviceURL = "tcp://" + server + ":" + port;
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(user, pwd, serviceURL);
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
        if (!isConsumerOpen) {
            return null;
        }
        /*Config config = Config.getInstance();
        boolean mqIsConsumerOpen = config.getBooleanProperty("mqIsConsumerOpen");
        if (!mqIsConsumerOpen) {
            return null;
        }*/

        DefaultMessageListenerContainer containerFactory = new DefaultMessageListenerContainer();
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setDestination(defaultDestination());

        // 20220109 注释掉并移至方法开始处并返回null，否则mqIsConsumerOpen为false时，会报错误信息：
        // ERROR jmsListenerContainerFactory-1 | org.springframework.jms.listener.DefaultMessageListenerContainer | Could not refresh JMS Connection for destination 'queue://queueMsg' - retrying using FixedBackOff{interval=5000, currentAttempts=2, maxAttempts=unlimited}. Cause: Cannot send, channel has already failed: tcp://127.0.0.1:61606
        /*if (!mqIsConsumerOpen) {
            return containerFactory;
        }*/

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
