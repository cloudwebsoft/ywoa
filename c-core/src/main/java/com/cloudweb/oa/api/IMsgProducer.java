package com.cloudweb.oa.api;

import cn.js.fan.mail.SendMail;
import com.cloudweb.oa.mq.MsgInfo;
import com.redmoon.oa.person.UserDb;

public interface IMsgProducer {

    String sendMessage(String message);

    void sendMsgInfo(MsgInfo msgInfo);

    void sendSysMsg(String userName, String title, String content, String action);

    void sendSysMsg(String userName, String title, String content, String actionType, String actionSubType, String action);

    void sendSysMsg(String[] userArr, String title, String content, String actionType, String actionSubType, String action);

    void sendEmail(String email, String senderName, String title, String content);

    void sendSms(String userName, String title);

    void sendSmsBatch(String[] userNameArr, String title, String sender);

    void sendSms(String userName, String title, String sender);
}
