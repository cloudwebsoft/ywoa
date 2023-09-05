package com.cloudweb.oa.mq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

public class TestActiveMQConsumer extends Thread implements MessageListener {


    private Session session;
    private Destination destination;
    private MessageProducer replyProducer;
    /**
     * @default
     * @return org.apache.activemq.ActiveMQConnectionFactory
     */
    public ActiveMQConnectionFactory getConnectionFactory(){
        // default null
        String user=ActiveMQConnection.DEFAULT_USER;
        // default null
        String password=ActiveMQConnection.DEFAULT_PASSWORD;
        // default failover://tcp://localhost:61616
        String url=ActiveMQConnection.DEFAULT_BROKER_URL;

        user = "redmoon";
        password = "redmoon";
        url = "tcp://localhost:61616";
        return this.getConnectionFactory(user, password, url);
    }
    /**
     *
     * @param user
     *            java.lang.String
     * @param password
     *            java.lang.String
     * @param url
     *            java.lang.String
     * @return org.apache.activemq.ActiveMQConnectionFactory
     */
    public ActiveMQConnectionFactory getConnectionFactory(String user,
                                                          String password, String url) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                user, password, url);
        return connectionFactory;
    }


    /**
     *
     * @return javax.jms.Connection
     */
    public Connection getConnection(){
        ActiveMQConnectionFactory connectionFactory = this.getConnectionFactory();
        return this.getConnection(connectionFactory);
    }

    /**
     *
     * @param connectionFactory org.apache.activemq.ActiveMQConnectionFactory
     * @return javax.jms.Connection
     */
    public Connection getConnection(ActiveMQConnectionFactory connectionFactory) {
        Connection connection = null;
        try {
            if (connectionFactory != null) {
                connection = connectionFactory.createConnection();
                connection.start();
            }
        } catch (JMSException e) {
            LogUtil.getLog(getClass()).error(e);
            return null;
        }
        return connection;
    }

    /**
     *
     * @return javax.jms.Session 会话
     */
    public Session getSession() {
        Connection connection = this.getConnection();
        boolean transacted= false;
        int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
        return this.getSession(connection, transacted, acknowledgeMode);
    }

    /**
     *
     * @param connection
     *            javax.jms.Connection
     * @param transacted
     *            boolean 是否是一个事务
     * @param acknowledgeMode
     *            int acknowledge 标识
     * @return javax.jms.Session 会话
     */
    public Session getSession(Connection connection, boolean transacted,
                              int acknowledgeMode) {
        if (connection != null) {
            if(session==null){
                try {
                    session = connection.createSession(transacted, acknowledgeMode);
                } catch (JMSException e) {
                    LogUtil.getLog(getClass()).error(e);
                    return null;
                }
            }
        }
        return session;
    }

    /**
     *
     * @param subject java.lang.String 消息主题
     * @return javax.jms.Destination
     */
    public Destination createDestination(String subject){
        String mode="Point-to-Point";
        return this.createDestination(mode, subject);
    }
    /**
     *
     * @param mode java.lang.String 消息传送模式
     * @param subject java.lang.String 消息主题
     * @return javax.jms.Destination
     */
    public Destination createDestination(String mode,String subject){
        session = this.getSession();
        return this.createDestination(mode, session, subject);
    }

    /**
     *
     * @param mode
     *            java.lang.String 消息传送模式
     * @param session
     *            javax.jms.Session 会话
     * @param subject
     *            java.lang.String 消息主题
     * @return javax.jms.Destination
     */
    public Destination createDestination(String mode, Session session,
                                         String subject) {

        if (session != null && mode != null && !mode.trim().equals("")) {
            try {
                if (mode.trim().equals("Publisher/Subscriber")) {
                    destination = session.createTopic(subject);
                } else if (mode.trim().equals("Point-to-Point")) {
                    destination = session.createQueue(subject);
                }
            } catch (JMSException e) {
                LogUtil.getLog(getClass()).error(e);
                return null;
            }
        }
        return destination;
    }

    /**
     *
     * @return
     */
    public MessageProducer createReplyer() {
        Session session = this.getSession();
        try {
            replyProducer = session.createProducer(null);
            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (JMSException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return replyProducer;
    }

    /**
     *
     * @return javax.jms.MsgConsumer
     */
    public MessageConsumer createConsumer(){
        Session session = this.getSession();
        MessageConsumer consumer = null;
        Destination destination = this.createDestination("moduleLog");//session.createQueue(subject);
        try {
            consumer = session.createConsumer(destination);
        } catch (JMSException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return consumer;
    }

    @Override
    public void onMessage(Message message) {
        try {

            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;

                String msg = txtMsg.getText();
                int length = msg.length();
                LogUtil.getLog(getClass()).info("[" + this.getName() + "] Received: '" + msg+ "' (length " + length + ")");
            }

            if (message.getJMSReplyTo() != null) {
                Session session =this.getSession() ;
                MessageProducer replyProducer = this.createReplyer();
                replyProducer.send(message.getJMSReplyTo(), session.createTextMessage("Reply: "+ message.getJMSMessageID()));
            }
            message.acknowledge();
        } catch (JMSException e) {
            LogUtil.getLog(getClass()).info("[" + this.getName() + "] Caught: " + e);
            LogUtil.getLog(getClass()).error(e);
        } finally {

        }
    }

    @Override
    public void run() {
        //获取会话
        session = this.getSession();
        //创建Destination
        destination = this.createDestination("moduleLog");
        //创建replyProducer
        replyProducer = this.createReplyer();
        MessageConsumer consumer = this.createConsumer();
        try {
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    @Test
    public void testReceiveMessage(){
        ArrayList<TestActiveMQConsumer> threads = new ArrayList<TestActiveMQConsumer>();
        for (int threadCount = 1; threadCount <= 1; threadCount++) {
            TestActiveMQConsumer consumer = new TestActiveMQConsumer();
            consumer.start();
            threads.add(consumer);
        }
        while (true) {
            Iterator<TestActiveMQConsumer> itr = threads.iterator();
            int running = 0;
            while (itr.hasNext()) {
                TestActiveMQConsumer thread = itr.next();
                if (thread.isAlive()) {
                    running++;
                }
            }
            if (running <= 0) {
                LogUtil.getLog(getClass()).info("All threads completed their work");
                break;
            }
        }
    }

    @Test
    public void get(){
        try{
            String url = "tcp://localhost:61616";
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            // 设置用户名和密码，这个用户名和密码在conf目录下的credentials.properties文件中，也可以在activemq.xml中配置
            connectionFactory.setUserName("redmoon");
            connectionFactory.setPassword("redmoon");
            // 创建连接
            Connection connection = connectionFactory.createConnection();
            connection.start();
            // 创建Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // 创建目标，就创建主题也可以创建队列
            Destination destination = session.createQueue("moduleLog");
            // 创建消息消费者
            MessageConsumer consumer = session.createConsumer(destination);
            // 接收消息，参数：接收消息的超时时间，为0的话则不超时，receive返回下一个消息，但是超时了或者消费者被关闭，返回null
            Message message = consumer.receive(1000);
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                LogUtil.getLog(getClass()).info("Received: " + text);
            } else {
                LogUtil.getLog(getClass()).info("Received: " + message);
            }
            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}