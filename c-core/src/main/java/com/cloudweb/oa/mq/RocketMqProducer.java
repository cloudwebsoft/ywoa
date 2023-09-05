package com.cloudweb.oa.mq;

import com.cloudweb.oa.api.IMsgProducer;
import com.cloudweb.oa.utils.ConstUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "mq.type", matchIfMissing = false, havingValue = "RocketMQ")
@Component
public class RocketMqProducer implements IMsgProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public String sendMessage(String message) {
        rocketMQTemplate.syncSend("queue_topic", MessageBuilder.withPayload(message).build());
        return "";
    }

    @Override
    public void sendMsgInfo(MsgInfo msgInfo) {
        rocketMQTemplate.syncSend("queue_topic", MessageBuilder.withPayload(msgInfo).build());
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
}