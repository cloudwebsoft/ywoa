package com.cloudweb.oa.mq;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSON;
import com.cloudweb.oa.api.IMsgService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import java.nio.charset.StandardCharsets;

@Slf4j
@ConditionalOnProperty(name = "mq.type", matchIfMissing = false, havingValue = "RocketMQ")
@Component
@RocketMQMessageListener(topic = "queue_topic", selectorExpression = "*", consumerGroup = "queue_group")
public class RocketMqConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    IMsgService msgService;

    @Override
    public void onMessage(MessageExt message) {
        try {
            byte[] body = message.getBody();
            String msg = new String(body, StandardCharsets.UTF_8);
            if (msg.startsWith("{")) {
                MsgInfo msgInfo = JSON.parseObject(msg, MsgInfo.class);
                log.info("接收到消息：{}", msgInfo.getTitle());
                if (msgInfo.getType() == ConstUtil.MQ_MSG_TYPE_MESSAGE) {
                    if (msgInfo.getUserNameArr() != null) {
                        msgService.sendSysMsg(msgInfo.getUserNameArr(), msgInfo.getTitle(), msgInfo.getContent(), msgInfo.getActionType(), msgInfo.getActionSubType(), msgInfo.getAction());
                    } else {
                        msgService.sendSysMsg(msgInfo.getUserName(), msgInfo.getTitle(), msgInfo.getContent(), msgInfo.getActionType(), msgInfo.getActionSubType(), msgInfo.getAction());
                    }
                    log.info("onMessage MQ_MSG_TYPE_MESSAGE: {}", msgInfo.getTitle());
                } else if (msgInfo.getType() == ConstUtil.MQ_MSG_TYPE_MSG) {
                    msgService.sendSysMsg(msgInfo.getUserName(), msgInfo.getTitle(), msgInfo.getContent(), msgInfo.getAction());
                    log.info("onMessage MQ_MSG_TYPE_MSG: {}", msgInfo.getTitle());
                } else if (msgInfo.getType() == ConstUtil.MQ_MSG_TYPE_EMAIL) {
                    String charset = Global.getSmtpCharset();
                    cn.js.fan.mail.SendMail sendMail = new cn.js.fan.mail.SendMail(charset);
                    String senderName = StrUtil.GBToUnicode(Global.AppName);
                    senderName += "<" + Global.getEmail() + ">";
                    String mailserver = Global.getSmtpServer();
                    int smtpPort = Global.getSmtpPort();
                    String name = Global.getSmtpUser();
                    String pwdRaw = Global.getSmtpPwd();
                    boolean isSsl = Global.isSmtpSSL();
                    try {
                        sendMail.initSession(mailserver, smtpPort, name, pwdRaw, "", isSsl);
                    } catch (Exception ex) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                    }

                    sendMail.initMsg(msgInfo.getEmail(), senderName, msgInfo.getTitle(), msgInfo.getContent(), true);
                    sendMail.send();
                    sendMail.clear();
                } else if (msgInfo.getType() == ConstUtil.MQ_MSG_TYPE_SMS) {
                    if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                        UserMgr um = new UserMgr();
                        UserDb ud = um.getUserDb(msgInfo.getUserName());
                        IMsgUtil imu = SMSFactory.getMsgUtil();
                        if (msgInfo.getUserNameArr() != null) {
                            imu.sendBatch(msgInfo.getUserNameArr(), msgInfo.getTitle(), msgInfo.getSender());
                        } else {
                            if (StringUtils.isEmpty(msgInfo.getSender())) {
                                imu.send(ud, msgInfo.getTitle(), ConstUtil.USER_SYSTEM);
                            } else {
                                imu.send(ud, msgInfo.getTitle(), msgInfo.getSender());
                            }
                        }
                    }
                }
            } else {
                log.info("接收到消息：{}", msg);
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}