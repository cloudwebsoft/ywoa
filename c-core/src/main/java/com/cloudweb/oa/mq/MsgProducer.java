package com.cloudweb.oa.mq;

import com.cloudweb.oa.api.IMsgProducer;
import com.cloudweb.oa.utils.ConstUtil;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Destination;

@ConditionalOnProperty(name = "mq.type", matchIfMissing = false, havingValue = "ActiveMQ")
@Component
public class MsgProducer implements IMsgProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    /**JmsMessagingTemplate 对 JmsTemplate 进行了封装*/
    // @Autowired
    // private JmsMessagingTemplate jmsMessagingTemplate;

    @Override
    public String sendMessage(String message) {
        String formatMessage = messageFormat(message);
        Destination destination = new ActiveMQQueue(ConstUtil.QUEUE_MESSAGE);
        jmsTemplate.convertAndSend(destination, formatMessage);
        return formatMessage;
    }

    @Override
    public void sendMsgInfo(MsgInfo msgInfo) {
        Destination destination = new ActiveMQQueue(ConstUtil.QUEUE_MESSAGE);
        jmsTemplate.convertAndSend(destination, msgInfo);
    }

    @Override
    public void sendSysMsg(String userName, String title, String content, String action) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setUserName(userName);
        msgInfo.setTitle(title);
        msgInfo.setContent(content);
        msgInfo.setAction(action);
        msgInfo.setType(ConstUtil.MQ_MSG_TYPE_MSG);
        sendMsgInfo(msgInfo);
    }

    @Override
    public void sendSysMsg(String userName, String title, String content, String actionType, String actionSubType, String action) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setUserName(userName);
        msgInfo.setTitle(title);
        msgInfo.setContent(content);
        msgInfo.setAction(action);
        msgInfo.setActionType(actionType);
        msgInfo.setActionSubType(actionSubType);
        msgInfo.setType(ConstUtil.MQ_MSG_TYPE_MESSAGE);
        sendMsgInfo(msgInfo);
    }

    @Override
    public void sendSysMsg(String[] userNameArr, String title, String content, String actionType, String actionSubType, String action) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setUserNameArr(userNameArr);
        msgInfo.setTitle(title);
        msgInfo.setContent(content);
        msgInfo.setAction(action);
        msgInfo.setActionType(actionType);
        msgInfo.setActionSubType(actionSubType);
        msgInfo.setType(ConstUtil.MQ_MSG_TYPE_MESSAGE);
        sendMsgInfo(msgInfo);
    }

    @Override
    public void sendEmail(String email, String senderName, String title, String content) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setEmail(email);
        msgInfo.setTitle(title);
        msgInfo.setContent(content);
        msgInfo.setType(ConstUtil.MQ_MSG_TYPE_EMAIL);
        sendMsgInfo(msgInfo);
    }

    @Override
    public void sendSms(String userName, String title) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setUserName(userName);
        msgInfo.setTitle(title);
        msgInfo.setType(ConstUtil.MQ_MSG_TYPE_SMS);
        sendMsgInfo(msgInfo);
    }

    @Override
    public void sendSms(String userName, String title, String sender) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setUserName(userName);
        msgInfo.setTitle(title);
        msgInfo.setType(ConstUtil.MQ_MSG_TYPE_SMS);
        msgInfo.setSender(sender);
        sendMsgInfo(msgInfo);
    }

    @Override
    public void sendSmsBatch(String[] userNameArr, String title, String sender) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setUserNameArr(userNameArr);
        msgInfo.setTitle(title);
        msgInfo.setType(ConstUtil.MQ_MSG_TYPE_SMS);
        msgInfo.setSender(sender);
        sendMsgInfo(msgInfo);
    }

    /**发送消息
     * @param message：待发送的消息
     * @return
     */
    /*public String sendMessage2(String message) {
        String formatMessage = messageFormat(message);
        Destination destination = new ActiveMQQueue("moduleLog");
        jmsMessagingTemplate.convertAndSend(destination, formatMessage);
        return formatMessage;
    }*/

    /**
     * 将待发送的消息先进行 json 格式化一下，便于传输与取值。
     *
     * @param message ：用户待发送的原始消息
     * @return ：返回转换好的 json 格式的消息
     */
    private String messageFormat(String message) {
        JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
        ObjectNode jsonNodes = jsonNodeFactory.objectNode();
        jsonNodes.put("message", message);//message 为 null 时，照样可以 put
        jsonNodes.put("status", 200);
        jsonNodes.put("timeStamp", System.currentTimeMillis());
        return jsonNodes.toString();
    }

    @Test
    public void testSendMessage() {

        sendMessage("it is 中文");
    }
}
